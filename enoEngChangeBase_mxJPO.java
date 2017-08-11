/*
 * enoEngChangeBase
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 *
 */

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.util.StringList;

import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.DomainConstants;
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
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.jsystem.util.StringUtils;

import java.util.Locale;



/**
 * The <code>ChangeBase</code> class contains methods for executing JPO operations related
 * to objects of the admin type  Change.
 * @author Cambridge
 * @version Common X3- Copyright (c) 2007, Enovia MatrixOne, Inc.
 **
 */
public class enoEngChangeBase_mxJPO extends emxDomainObject_mxJPO {

	/********************************* ADMIN TYPE SELECTABLES /*********************************

    /** Relationship "Assigned Affected Item". */
    public static final String RELATIONSHIP_ASSIGNEED_AFFECTED_ITEM = PropertyUtil.getSchemaProperty("relationship_AssignedAffectedItem");



    /** the "Part Specification" policy. */
    public static final String POLICY_PART_SPECIFICATION = PropertyUtil.getSchemaProperty("policy_PartSpecification");

	/* Is Version Attribute */
	public static final String isVersion = PropertyUtil.getSchemaProperty("attribute_IsVersion");
	/* Type MECO  */
	public static final String TYPE_MECO=PropertyUtil.getSchemaProperty("type_MECO");
	

    /** state "Release" for the "EC Part" policy. */
    public static final String STATE_ECPART_RELEASE = PropertyUtil.getSchemaProperty("policy", POLICY_EC_PART, "state_Release");

    public static final String STATE_PARTSPECIFICATION_RELEASE = PropertyUtil.getSchemaProperty("policy", POLICY_PART_SPECIFICATION, "state_Release");

    /** the "Part Markup" type. */
    public static final String TYPE_PART_MARKUP                = PropertyUtil.getSchemaProperty("type_PARTMARKUP");
    /** the "Part Markup" policy. */
    public static final String POLICY_PART_MARKUP              = PropertyUtil.getSchemaProperty("policy_PartMarkup");
	/** the "Proposed Markup" relationship. */
    public static final String RELATIONSHIP_PROPOSED_MARKUP    = PropertyUtil.getSchemaProperty("relationship_ProposedMarkup");
	/** the "Applied Markup" relationship. */
    public static final String RELATIONSHIP_APPLIED_MARKUP     = PropertyUtil.getSchemaProperty("relationship_AppliedMarkup");
    
    /** Relationship "ECO Change Request Input". */
    public static final String RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT = PropertyUtil.getSchemaProperty("relationship_ECOChangeRequestInput");
    
    /** Relationship "Change Responsibility". */
    public static final String RELATIONSHIP_CHANGE_RESPONSIBILITY = PropertyUtil.getSchemaProperty("relationship_ChangeResponsibility");

	public static String sGlobalTypefilterValue="";
	public static String sGlobalRequestedChangeFilter="";
	public static String sGlobalAssigneeName="";

	public final static String RANGE_FOR_UPDATE="For Update";
	public final static String RANGE_FOR_REVISE="For Revise";
	public final static String RANGE_FOR_RELEASE="For Release";
	public final static String RANGE_FOR_OBSOLETE="For Obsolescence";
	public final static String RANGE_NONE="None";
	public static final String RANGE_REVIEW = "Review";
	public static final String RANGE_APPROVAL = "Approval";



	/** A string constant with the value "paramMap". */
	protected static final String SELECT_PARAM_MAP = "paramMap";
	
	/** A string constant with the value "objectId". */
	protected static final String SELECT_OBJECT_ID = "objectId";
	
	/** A string constant with the value "All". */
	protected static final String SELECT_ALL = "All";

	/** A string constant with the value "relId". */
	protected static final String SELECT_REL_ID = "relId";

	/** A string constant with the value "New Value". */
	protected static final String SELECT_NEW_VALUE = "New Value";

	/********************************* STRING RESOURCES FIELDS /*********************************
	/** A string constant with the value "emxComponentsStringResource". */
	protected static String RESOURCE_BUNDLE_COMPONENTS_STR = "emxEngineeringCentralStringResource";


	/** A string constant with the value "|". */
	protected static final String SYMB_COMMA = ",";

	/** A string constant with the value tomid. */
	protected static final String SYMB_TO_MID = "tomid";

	/** A string constant with the value "|". */
	protected static final String SYMB_PIPE = "|";
	
	/** A string constant with the value *. */
	protected static final String SYMB_WILD = "*";



    /**
     * Default Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @throws        Exception if the operation fails
     * @since         Common X3
     **
     */
    public enoEngChangeBase_mxJPO (Context context, String[] args) throws Exception {
        super(context, args);
    }
    /**
     * Checks if the any object is connected to the context change object with AssignedAffectedItem Relationship
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *           0 -  Context
     *           1 -  A String containg the Object Id
     *           2 -  A String Array containg the Rel Ids of Objects Selected
     *           3 -  A String Array containg the Obj Ids of Objects Selected
     *           4 -  A String containing the Change Obj Id.
     * @return        StringList of Person objects which is a combination of Stringlist of "objects which can be removed" and the "objects which have affected Items" connected to them
     * @throws        Exception if the operation fails
     * @since         Common X3
     **
     */

    public StringList checkAssignedAffectedItemRelExists(Context context, String objectId, String[] arrRelIds, String[] strObjectId) throws Exception
    	{

 		//getting the String Array of Rel Ids to be disconnected
    		String[] arrRelationIds = arrRelIds;

    		// Initializing the Stringlist for the Objects which can't be removed
    		StringList strAssigneeRetain = new StringList(arrRelationIds.length);

    		// Initializing  Lists for the name of the Objects to be removed and retained.
    		StringList strAssigneeName = new StringList(arrRelationIds.length);

    		int iDeleteCount = 0;

 		// Initializing Strings for MQL Commands and its Result
    		String strMQLCommand ;
    		String strMQLCommandResult =  "";

    		// Declaring a new MQL Command
    		MQLCommand prMQL  = new MQLCommand();
    		prMQL.open(context);

 		// initializing a list for the person objects which need to be deleted.
    		StringList strAssigneeDeleteList = new StringList(arrRelationIds.length);

    		for (int i=0;i< arrRelationIds.length ;i++ )
    		{
 			// The MQL Command
    			strMQLCommand = "print connection $1 select $2 dump";

    			// Executing the MQL Command
    			prMQL.executeCommand(context,strMQLCommand, arrRelationIds[i], "frommid.id");

    			// getting the MQL Command result
    			strMQLCommandResult = prMQL.getResult();

 			// Conditional Check on the Output received out of the MQL Command.
    			if(strMQLCommandResult.equals("\n"))
    			{
 				strAssigneeDeleteList.add(iDeleteCount, arrRelationIds[i]);
    					iDeleteCount = iDeleteCount+1;

    			}
    			else
    			{
    				strAssigneeRetain.addElement(arrRelationIds[i]);
 			}

   		}
    		prMQL.close(context);

    		for (int i = 0;i<strAssigneeRetain.size();i++ )
    		{
    			//getting the name of the Persons being retained for Returning purposes
    			String relIds[] = new String[1];
    			relIds[0] = (String)strAssigneeRetain.get(i);
    			StringList slSelects = new StringList();
    			slSelects.addElement("from.name");

    			DomainRelationship domrRetainAssignee = new DomainRelationship(relIds[0]);
    			MapList mlList = domrRetainAssignee.getInfo(context,relIds , slSelects);
    			String sName = "";
    			if(!mlList.isEmpty())
    			{
    				Map mName = (Map)(mlList.get(0));
    				sName = (String)mName.get("from.name");
    				strAssigneeName.addElement(sName);
    			}
    		}

 		// A Combined StringList to return the List of Rel Ids to be deleted and the Names of the Persons to be retained.
 		StringList strConsolidatedList = new StringList(arrRelationIds.length);
 		strConsolidatedList.add(0,strAssigneeDeleteList);
 		strConsolidatedList.add(1,strAssigneeName);

 		return strConsolidatedList;
 	}
    
    /**
     * To get all the indirect Affected Item reationship ids
     * @param context the eMatrix <code>Context</code> object.
     * @param changeId The Change Object Id.
     * @param selectedRelIds String array holding all the selected rel ids from the table.
     * @return StringList.
     * @since Common R207
     * @author ZGQ
     * @throws Exception if the operation fails.
     */

     public StringList getIndirectAffectedItemRelIds(Context context, String changeId, String selectedRelIds[]) throws Exception
     {
         String strChangeId = changeId;
         String strSelectedRelIds[] = selectedRelIds;
         StringList resultList = new StringList();

         StringList strlRelSelects = new StringList();
         strlRelSelects.add("to.name");
         strlRelSelects.add("to.type");
         //IR-069736
         strlRelSelects.add("id[connection]");

         try{
             String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
             String strRelWhereclause = "attribute[" + strAttrAffectedItemCategory + "] == Indirect";

             MapList selectedAIList = DomainRelationship.getInfo(context, strSelectedRelIds, strlRelSelects);

             strlRelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
             strlRelSelects.add("to.id");

             DomainObject changeObj = new DomainObject(strChangeId);
             MapList indirectAIList = changeObj.getRelatedObjects(context,RELATIONSHIP_AFFECTED_ITEM, DomainConstants.QUERY_WILDCARD, null,
                     strlRelSelects, false, true, (short) 1, DomainConstants.EMPTY_STRING, strRelWhereclause);

             Iterator dirIterator = selectedAIList.listIterator();
             Iterator indIterator = indirectAIList.listIterator();
             Hashtable directMap = null;
             Map indirectMap = null;
             String directAIName = "";
             String directAIType = "";
             String IndirectId = "";
             String indirectAIName = "";
             String indirectAIType = "";
             String indirectAIRelId = "";
             //IR-069736
             String directRelId = "";
             String indirectRelId = "";

             while (dirIterator.hasNext()){
                 directMap = (Hashtable)dirIterator.next();
                 directAIName = (String)directMap.get("to.name");
                 directAIType = (String)directMap.get("to.type");
                 directRelId = (String)directMap.get("id[connection]");
                 for(int i=0; i<indirectAIList.size(); i++)
                 {
                     indirectMap = (Hashtable)indirectAIList.get(i);
                     IndirectId = (String)indirectMap.get("to.id");
                     indirectAIName = (String)indirectMap.get("to.name");
                     indirectAIType = (String)indirectMap.get("to.type");
                     indirectRelId = (String)indirectMap.get("id[connection]");
                     //IR-069736
                     if(directAIName.equals(indirectAIName) && directAIType.equals(indirectAIType) && !directRelId.equals(indirectRelId)){
                         indirectAIRelId = (String)indirectMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                         resultList.add(IndirectId + "|" + indirectAIRelId);
                         break;
                     }
                 }
             }
         }
         catch(Exception e){
             e.printStackTrace();
             throw e;
         }
         return resultList;
     }

         
    /* This  method Disconnects the assignees selected on the Assignees List Page from the Change Object.
     * @param context The ematrix context of the request.
     * @param args This string array contains following arguments:
     *          0 - context
     *          1 - A String Array of Object Ids of Assignees to be removed
     *          2 - A String Array of relationship Ids of Assignees with the context object.
     *          3 - A String Array of Change Object Id
     * @return a StringList of Person Objects which cannot be removed
     * @throws Exception
     * @throws FrameworkException
     * @since Common X3
     */

     public StringList removeAssignee(Context context, String[] args)
            throws FrameworkException
        {

    			// Initialing a StringList for the Objects which cannot be removed.
    			StringList strCombinedList = new StringList();
    			StringList strAssigneeRetain = new StringList();
    			StringList strAssigneeDelete = new StringList();
    	        try
    	        {
    				//  unpacking the Arguments from variable args
    				HashMap programMap = (HashMap)JPO.unpackArgs(args);

    				//getting the String Array of Rel Ids to be disconnected
    				String[] arrRelIds = (String[])programMap.get("arrRelIds[]");

    				//getting the String Array of object Ids to be removed
    				String[] strObjectIds = (String[])programMap.get("strObjectIds[]");

    				//getting the String of object Id of the parent object.This will be used in check condition of Affected Item.
    				String strChangeObjectId = (String)programMap.get("objectId");

    				String strContextId = PersonUtil.getPersonObjectID(context);

    				//Initialing the for loop over the list of object Ids to be removed
    				for(int i = 0; i < strObjectIds.length; i++)
    				{					
    					String strObjectId = (String) FrameworkUtil.split(strObjectIds[i], "|").get(0);
    					//String strAssigneeRemove = UINavigatorUtil.getI18nString("emxComponents.Common.Message.strAssigneeRemove", RESOURCE_BUNDLE_COMPONENTS_STR, (String)programMap.get("languageStr"));
    					String strAssigneeRemove = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(), "emxEngineeringCentral.Message.strAssigneeRemove");
    					if(strContextId.equals(strObjectId))
    					{
    						emxContextUtil_mxJPO.mqlNotice(context,strAssigneeRemove);
    						throw new FrameworkException(strAssigneeRemove);
    				}
    				}

    				//getting the combined list of Rel Ids and Assignee Retaied Names.
    				strCombinedList = checkAssignedAffectedItemRelExists(context, strChangeObjectId,arrRelIds, strObjectIds);

    				strAssigneeDelete = (StringList)strCombinedList.get(0);
    				strAssigneeRetain = (StringList)strCombinedList.get(1);

    				int iAssigneeDeleteSize= strAssigneeDelete.size();
    				if(iAssigneeDeleteSize != 0)
    				{
    			        Iterator PersonItr = strAssigneeDelete.iterator();
    					while(PersonItr.hasNext())
    					{
    				        String strRelId = (String)PersonItr.next();
    						DomainRelationship.disconnect(context,strRelId);
    					}
    				}
    	        }
    	        catch (Exception ex)
    	        {
    				ex.printStackTrace(System.out);
    	        }

    	         //returning the list of Objects' Names which cannot be removed.
    	         return strAssigneeRetain;
    }
     
     /**
      * DisConnects the ChangeObject with Part
      * If the Part has some Version then Purges the Part.
      * @param	context the eMatrix <code>Context</code> object
      * @param	args holds a HashMap containing the following entries:
      * paramMap - a HashMap containing the following keys, "arrTableRowIds", "arrRelIds"
      * @return	void
      * @throws	Exception if operation fails
      * @since   Common X3
      */

     public void removeAffectedItems(Context context, String[] args)
     	                throws Exception
     	        {
     		try{
     					String temp = "";
     					String partObjectId = "";
     					String attributeValue = "";
     					String sObjectId = "";
     					StringList objectList = null ;
     					StringList relObjectList = null ;

                      	HashMap programMap = (HashMap) JPO.unpackArgs(args);

                         String[] arrTableRowIds = (String[]) programMap.get("arrTableRowIds");
                         String[] arrRelIds = (String[]) programMap.get("arrRelIds");
                         String changeID = (String) programMap.get("changeId");

                         int sRowIdsCount =arrTableRowIds.length;
                         int sRelIdsCount =arrRelIds.length;
     //370192 Start
                         StringList indirectAIList = getIndirectAffectedItemRelIds(context, changeID, arrRelIds);
                         int listSize = indirectAIList.size();
                         String[] strIndirectId = new String[listSize]; //374504
                         String[] strIndirectRelIds = new String[listSize];

                         Map map = null;
                         for(int i=0; i<listSize; i++){
                             objectList = FrameworkUtil.split((String)indirectAIList.get(i), "|");
                             strIndirectId[i] = (String)objectList.elementAt(0);
                             strIndirectRelIds[i] = (String)objectList.elementAt(1);
                         }
     //370192 End

     //374504 Start
                         String strIsVersion = "";

                         StringList objSelects= new StringList();
                         objSelects.add(DomainConstants.SELECT_ID);
                         objSelects.add("attribute[" + isVersion +"]");

                         StringBuffer sbToDeleteIds = new StringBuffer();
                         MapList list = DomainObject.getInfo(context, strIndirectId, objSelects);
                         for(int i=0; i<list.size(); i++){
                             map = (HashMap)list.get(i);
                             strIsVersion = (String)map.get("attribute[" + isVersion +"]");
                             if(strIsVersion.equalsIgnoreCase("true")){
                                 sbToDeleteIds.append((String)map.get(DomainConstants.SELECT_ID));
                                 sbToDeleteIds.append("|");
                             }
                         }
     //374504 End

                         //String changeID = null;
                         // Proposed markup in case of ECR
                         String relName = RELATIONSHIP_PROPOSED_MARKUP;
                         //358689 start
     					boolean isMBOMInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMManufacturing",false,null,null);
                         //358689 end
                         for(int relcount=0; relcount < sRelIdsCount; relcount++)
                           {
                             StringList slSelect = new StringList("from.id");
                             slSelect.add("to.id");
                             MapList mlAffectedItem = DomainRelationship.getInfo(context, new String[]{arrRelIds[relcount]} ,slSelect);
                             Map m=(Map) mlAffectedItem.get(0);
                             String strID = (String) m.get("to.id");
                             changeID = (String) m.get("from.id");
                             DomainObject doChange = new DomainObject(changeID);
                             //358689
                             if(isMBOMInstalled)
                             {
                              String connId= null;
                              String pmId = null;
                              String fromChange="";
                              String fromValue="";
                              String changeName="";
                              changeName=(String)doChange.getInfo(context,DomainConstants.SELECT_NAME);
                              MapList mL=new MapList();
                              DomainObject dob=new DomainObject(strID);
                              StringList busSelList=new StringList();
                              busSelList.add(DomainConstants.SELECT_NAME);
                              Map mapDetails = dob.getInfo(context, busSelList);
                              String   sPartName = (String) mapDetails.get(DomainObject.SELECT_NAME);
                              mL=(MapList)dob.findObjects(context,PropertyUtil.getSchemaProperty(context,"type_PartMaster"),sPartName,"","*","*",null,false,busSelList);
                              if (mL.size()>0) {
                                Map mapData = (Map)mL.get(0);
                                pmId = (String)mapData.get(DomainConstants.SELECT_ID);
                              DomainObject pmobj=new DomainObject(pmId);
                              StringList manuRespChgIds=(StringList)pmobj.getInfoList(context,"to["+PropertyUtil.getSchemaProperty(context,"relationship_ManufacturingResponsibilityChange")+"].id");
                               for (int j = 0; j < manuRespChgIds.size(); j++) {
                                    connId=(String)manuRespChgIds.get(j);
                                    if(connId!=null)
                                    {
                                        fromChange="print connection $1 select $2  dump";
                                        fromValue=MqlUtil.mqlCommand(context,fromChange, connId, "from.id");
                                        if(fromValue.equals(changeID))
                                        {
                                            DomainRelationship.disconnect(context, connId);
                                        }
                                    }
                              }
                               StringList manuRespIds=(StringList)pmobj.getInfoList(context,"to["+DomainConstants.RELATIONSHIP_MANUFACTURING_RESPONSIBILITY+"].id");
                               for (int i = 0; i < manuRespIds.size(); i++) {
                                   connId=(String)manuRespIds.get(i);
                                   if(connId!=null)
                                   {
                                       DomainRelationship dr=new DomainRelationship(connId);
                                       fromValue=dr.getAttributeValue(context,"Doc-In");
                                       if(changeName.equals(fromValue)){
                                    	   DomainRelationship.disconnect(context, connId);
                                    }
                                  }
                             }
                              }
                             }
                             //end 359689
                             if (doChange.isKindOf(context, TYPE_ECO)|| doChange.isKindOf(context, TYPE_MECO))
                             {
                                 // Proposed markup in case of ECO/MECO
                             relName = RELATIONSHIP_APPLIED_MARKUP;
                             }

                             String objectWhere = "(to["+relName+"].from.id=="+changeID+")";
                             // retrieve the markups that are unique to Part and ECR/ECO.
                             MapList mapListMarkups =    new DomainObject(strID).getRelatedObjects(context,
                                                                                          RELATIONSHIP_EBOM_MARKUP,
                                                                                          "*",
                                                                                          new StringList(SELECT_ID),
                                                                                          null,
                                                                                          false,
                                                                                          true,
                                                                                          (short)1,
                                                                                          objectWhere,
                                                                                          null);
                             relObjectList = new StringList();

                             Iterator itr = mapListMarkups.iterator();
                             while(itr.hasNext())
                             {
                                 Map m2=(Map) itr.next();
                                 String strID1 = (String) m2.get(SELECT_ID);
                                 DomainObject dobjBOMMarkup = new DomainObject(strID1);
                                 dobjBOMMarkup.deleteObject(context);

                             }
                           }
      // 374504 - Commented for 374504
                         /*for(int count=0; count < sRowIdsCount; count++)
                         {
                             temp  = arrTableRowIds[count];
                             objectList = FrameworkUtil.split(temp,"|");
                             partObjectId = (String)objectList.elementAt(1) ;
                             DomainObject dobj = new DomainObject(partObjectId);
                             attributeValue =   dobj.getAttributeValue(context,isVersion) ;
                             StringList busSelects = new StringList(2);
                             busSelects.add(DomainConstants.SELECT_ID);
                             StringList relSelects = new StringList(2);
                             relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

                                 if(attributeValue.equalsIgnoreCase("FALSE"))
                                  {
                                     MapList mapList = dobj.getRelatedObjects(context,RELATIONSHIP_PART_VERSION,DomainConstants.TYPE_PART ,busSelects,relSelects,false,true,(short)1,null,null);
                                     if (mapList.size() > 0)
                                     {
                                         Iterator itr = mapList.iterator();
                                         while(itr.hasNext())
                                         {
                                             Map newMap = (Map)itr.next();
                                             sObjectId=(String) newMap.get(DomainConstants.SELECT_ID);
                                             DomainObject dobjPartVersion = new DomainObject(sObjectId);
                                             dobjPartVersion.deleteObject(context);
                                         }
                                     }
                                  }
                          }*/
     //374504 - Commented till here for 374504
     //370192 Start
                         String strDestRelIds[] = new String[arrRelIds.length + strIndirectRelIds.length];
                         System.arraycopy(arrRelIds, 0, strDestRelIds, 0, arrRelIds.length);
                         System.arraycopy(strIndirectRelIds, 0, strDestRelIds, arrRelIds.length, strIndirectRelIds.length);
                         //disconnect(context,arrRelIds, false);
                         DomainRelationship.disconnect(context, strDestRelIds, false);
     //370192 End
                         
                       //Added for IR-143641 start
                         //Relationship "Raised Against ECR" Needs to be removed when ECR Remove action is executed
                           String sRelId = "";
                           String ToId = "";
                           String FromId = "";
                           StringTokenizer st = null;
                             for(int i=0; i < arrTableRowIds.length; i++)
                               {
                                   st = new StringTokenizer(arrTableRowIds[i], "|");
                                   sRelId = st.nextToken();                                             
                                   ToId =st.nextToken();                         
                                   FromId=st.nextToken();                         
                               }
                                                   
                           DomainObject dObj1 =  new DomainObject(ToId);
                           String whereclause = "id==\"" +FromId+"\"" ;//ECR Id
                           StringList selectRelStmts = new StringList(1);
                           selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
                       
                            MapList totalresultList = dObj1.getRelatedObjects(context,
                                               DomainObject.RELATIONSHIP_RAISED_AGAINST_ECR, // relationship pattern
                                               DomainConstants.TYPE_ECR,    // object pattern
                                               new StringList(),            // object selects
                                               selectRelStmts,              // relationship selects
                                               true,                        // to direction
                                               false,                       // from direction
                                               (short)1,                    // recursion level
                                               whereclause,                 // object where clause
                                               null);  
                              
                            if(totalresultList!=null && totalresultList.size()>0)
                            {
                         	   Map sRelatedECRNameMap = (Map)totalresultList.get(0);
                         	   sRelId = (String)sRelatedECRNameMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);             
                         	   if(sRelId != null)
                         	   {
                         		   DomainRelationship.disconnect(context, sRelId);               
                                }
                            }
                           //Added for IR-143641 end 
                                             
     //374504 Start
                         String tempStr = sbToDeleteIds.toString();
                         if(!tempStr.equals("") && (tempStr!=null)){
                             String strToDeleteIds[] = StringUtils.split(tempStr, "[|]");
                             DomainObject.deleteObjects(context, strToDeleteIds);
                         }
     //374504 End
                 }catch (Exception e) {
                     e.printStackTrace();
                 }

             }
     /**
 	 * getIndirectAffectedItems, Method to retrieve the new revision
 	 * for a given change and part context
 	 * @param context the eMatrix <code>Context</code> object.
 	 * @param args contains a packed HashMap
 	 * @return String.
      * @since Common X3
 	 * @throws Exception if the operation fails.
 	 */

 	public String getIndirectAffectedItems(Context context, String args[])	throws Exception
 	{
 		String strPartId = args[0];
 		String strECOId = args[1];

 		String strNewPartId = null;

 		StringList strlPartSelects =  new StringList(2);
 		strlPartSelects.add(SELECT_NAME);
 		strlPartSelects.add(SELECT_TYPE);

 		DomainObject doPart = new DomainObject(strPartId);

 		Map mapPartDetails = doPart.getInfo(context, strlPartSelects);
 		String strPartName = (String) mapPartDetails.get(SELECT_NAME);
 		String strPartType = (String) mapPartDetails.get(SELECT_TYPE);

 		DomainObject doECO = new DomainObject(strECOId);

 		StringList strlObjectSelects = new StringList(1);
 		strlObjectSelects.add(SELECT_ID);

 		String strObjWhereclause = "name == \"" + strPartName + "\"";

 		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
 		String strRelWhereclause = "attribute[" + strAttrAffectedItemCategory + "] == Indirect && attribute[" + ATTRIBUTE_REQUESTED_CHANGE + "] == \"" + RANGE_FOR_RELEASE + "\"";


 		MapList mapListParts = doECO.getRelatedObjects(context,
                     RELATIONSHIP_AFFECTED_ITEM, strPartType, strlObjectSelects,
                     null, false, true, (short) 1, strObjWhereclause, strRelWhereclause);

         if (mapListParts.size() > 0)
         {
 			Map mapPart = (Map) mapListParts.get(0);
 			strNewPartId = (String) mapPart.get(SELECT_ID);
 		}

 		//should consider whether comparing against revision list and version list is required here

 		return strNewPartId;

 	}
     /**
      * Creates new Revision for Affected Item
      * If the Affected Item's Requested change value is Revise
      * it will create version object
      * @param	context the eMatrix <code>Context</code> object
      * @param	args holds a HashMap containing the following entries:
      * paramMap - a HashMap containing the following keys, "arrTableRowIds", "arrRelIds"
      * @return	void
      * @throws	Exception if operation fails
      * @since   Common X3
     */

    public void createNewRevisionForAffectedItem(Context context, String[] args)
                 throws Exception

 	{
 		HashMap programMap = (HashMap)JPO.unpackArgs(args);
 		String[] strObjectIds = (String[])programMap.get("strObjectIds");
 		String[] arrTableRowIds = (String[])programMap.get("arrTableRowIds");
 		String strChangeObjectId = (String)programMap.get("objectId");

 		boolean blnIsError = false;

 		String relAffectedItems = PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem");
 		String attrRequestedChange = PropertyUtil.getSchemaProperty(context,"attribute_RequestedChange");
 		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
 		String selectAttr="relationship[" + relAffectedItems+ "].attribute[" + attrRequestedChange+ "].value";
 		String languageStr = (String)programMap.get("languageStr");
 		Locale strLocale = context.getLocale();
 		String attrRequestedChangeValue= EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale,"emxFramework.Range.Requested_Change.For_Revise");
 		

 		String strMessage=EnoviaResourceBundle.getProperty(context,  RESOURCE_BUNDLE_COMPONENTS_STR, strLocale, "emxEngineeringCentral.Common.AffectedItem.CreateNewRevision") + ": ";

 		DomainObject domObjAffectedItem = DomainObject.newInstance(context);

 		String strErrorParts = "";

 		for(int i=0;i<strObjectIds.length;i++)
 		{
 			int t=strObjectIds[i].indexOf('|');
 			String affectedItemId=(strObjectIds[i].substring(0,t));
 			int iS=arrTableRowIds[i].indexOf('|');
 			String affectedItemRelId=(arrTableRowIds[i].substring(0,iS));
 			DomainObject domObj=new DomainObject(affectedItemId);
 			DomainRelationship domRelAI =new DomainRelationship(affectedItemRelId);
 			String attrValue=domRelAI.getAttributeValue(context,attrRequestedChange);

 			String[] inputArgs = new String[2];

 			inputArgs[0] = affectedItemId;
 			inputArgs[1] = strChangeObjectId;
 			String strNewPartId = (String) getIndirectAffectedItems(context, inputArgs);

 			String strAIName = domObj.getInfo(context, DomainConstants.SELECT_NAME);

 			if(attrValue.equals(attrRequestedChangeValue) && strNewPartId == null)
 			{
 				BusinessObject lastRevObj = domObj.getLastRevision(context);
 				String nextRev = lastRevObj.getNextSequence(context);
 				String objectId=lastRevObj.getObjectId();
 				String lastRevVault = lastRevObj.getVault();
 				domObjAffectedItem.setId(objectId);
 				BusinessObject revBO = domObjAffectedItem.revise(context, nextRev, lastRevVault);
 				DomainObject revPart = new DomainObject(revBO);
 				revPart.getBasics(context);
 				String currentUser = context.getUser();
 				String revPartId = revPart.getObjectId(context);
 				//added
 				String mqlCmd = "print connection $1 select $2 dump $3";
 				MQLCommand mCmd = new MQLCommand();
 				mCmd.executeCommand(context,mqlCmd, affectedItemRelId, "tomid.fromrel["+DomainConstants.RELATIONSHIP_ASSIGNED_EC+"].from.id", "|");
 				String personId = mCmd.getResult().trim();
 				DomainObject dobjPerson  = new DomainObject(personId);

 				String strAssigneeUserName = dobjPerson.getName(context);

 				context.setUser(strAssigneeUserName);
 				DomainRelationship doAffectedItemRel = DomainRelationship.connect(context,
 											new DomainObject(strChangeObjectId),
 											RELATIONSHIP_AFFECTED_ITEM,
 											new DomainObject(revPartId));
 				try
 				{
 					ContextUtil.pushContext(context);
 					doAffectedItemRel.setAttributeValue(context, strAttrAffectedItemCategory, "Indirect");
 					ContextUtil.popContext(context);
 				}
 				catch (Exception ex)
 				{
 				}
 				finally
 				{
 					ContextUtil.popContext(context);
 				}

 				context.setUser(currentUser);
 			}//end for if(attrValue.equals(attrRequestedChangeValue))
 			else
 			{
 				blnIsError = true;
 				if ("".equals(strErrorParts))
 				{
 					strErrorParts = strAIName;
 				}
 				else
 				{
 					strErrorParts = strErrorParts + ", " + strAIName;
 				}

 			}
 		}//end for FOR loop

 		if (blnIsError)
 		{
 			strMessage = strMessage + strErrorParts;
 				emxContextUtil_mxJPO.mqlNotice(context,strMessage);
 			}
 }//end for method createNewRevisionForAffectedItem

    /**
     * Gets connected reltorel ids to the relationship.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param relid relationship id from which reltorel is connected
     * @return the StringList of reltorel id for further processing
     * @throws FrameworkException if the operation fails
     * @since Common X3
   */
    	    public StringList getTomids(Context context, String relid)
    	        throws FrameworkException
    	    {

    			ContextUtil.startTransaction(context, true);
    			String Res;
    			StringList slmidId;
               MqlUtil.mqlCommand(context, "verb $1", "on");

                try
                {
   			      ContextUtil.pushContext(context, null, null, null);
                     Res= MqlUtil.mqlCommand(context,"print connection $1 select $2 dump $3", relid, "tomid.id", "|");
    				  slmidId				= FrameworkUtil.split(Res,"|");
                     ContextUtil.commitTransaction(context);
    			}
               catch (Exception e)
                {
                    // Abort transaction.
                    ContextUtil.abortTransaction(context);
                    throw new FrameworkException(e);
                }
               finally
   		    {
   				ContextUtil.popContext(context);
   			}

    		return slmidId;
          }
    	    
    /**
     * Gets ID of the object connected to the reltorel in the from side of the relationship.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param relid relationship id from which reltorel is connected
     * @param relationshipName relationship Name
     * @return the String, the name of the object for further processing
     * @throws FrameworkException if the operation fails
     * @since Common X3
   */
    	    public String getTomidFromRelationshipFromID(Context context, String relid, String relationshipName)
   	        throws FrameworkException
   	    {

   			ContextUtil.startTransaction(context, true);
   			String strRes;
   			MqlUtil.mqlCommand(context, "verb on");
   			try
   			{

   				ContextUtil.pushContext(context);
   				strRes= MqlUtil.mqlCommand(context,"print connection $1 select $2 dump", relid, "tomid.fromrel["+relationshipName+"].from.id");
   				ContextUtil.commitTransaction(context);
   			}
   			catch (Exception e)
   			{
   				// Abort transaction.
   				ContextUtil.abortTransaction(context);
   				throw new FrameworkException(e);
   			}
   			finally
   			{
   				ContextUtil.popContext(context);
   			}
   			return strRes;
   		}
    	    
	    /**
		* Connects two relationship using the given relationship type.
		*
		* @param context the eMatrix <code>Context</code> object
		* @param srelationship name the object/relationship to connect from
		* @param fromId the object/relationship to connect from
		* @param toId the object/relationship to connect to
		* @param isFromObj true if from side is Object
		* @param isToObj true if to side is Object
		* @return the connection id for further processing
		* @throws FrameworkException if the operation fails
		* @since Common X3
		*/
		    public String connect(Context context, String srelationship,String fromId,String toId,boolean isFromObj,boolean isToObj)
		        throws FrameworkException
		    {
				String sFrom;
				String sTo;
				String Res;
				String strebomsubstititeId;
				ContextUtil.startTransaction(context, true);
	            MqlUtil.mqlCommand(context, "verb $1", "on");

	            try {
	            	ContextUtil.pushContext(context, null, null, null);
	            	sFrom = isFromObj ? "from": "fromrel";
	            	sTo = isToObj ? "to": "torel";


	            	StringBuffer cmd = new StringBuffer();
	            	cmd.append("add connection $1 ");
	                cmd.append(sFrom);
	            	cmd.append(" $2 ");
	                cmd.append(sTo);
	            	cmd.append(" $3");
	                Res= MqlUtil.mqlCommand(context, cmd.toString(), srelationship, fromId, toId);
					
					//Getting the created Relationship id
					int findx                       = Res.indexOf("'");
					int lindx                       = Res.lastIndexOf("'");
					strebomsubstititeId      = Res.substring(findx+1,lindx);
	                // End successful transaction.
	                ContextUtil.commitTransaction(context);
	            } catch (Exception e) {
	                // Abort transaction.
	                ContextUtil.abortTransaction(context);
	                throw new FrameworkException(e);
	            } finally {
					 ContextUtil.popContext(context);
				}
		return strebomsubstititeId;
	       }
    		    
    /**
     * Split Delegate the Assignments of one or more selected Affected Items to a single Assignee
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @throws Exception if the operation fails.
     * @since EC-V6R2009-1.
    */

    public void splitDelegateAssignees(Context context, String[] args)throws Exception
        {
    		HashMap programMap = (HashMap)JPO.unpackArgs(args);

    		// Obtaining the Change Object Id
    		String[] strChangeObjID = (String[])programMap.get("strChangeObjID");

    		// Obtaining the Object Id of the New Assignee
    		String[] strNewAssigneeID = (String[])programMap.get("strNewAssigneeID");

    		// Obtaining the "Affected Item" Rel Ids of the Affected Items selected initially on the List Page
    		String[] arrAffectedItemsRelIds = (String[])programMap.get("arrAffectedItemRelID");

    		// Getting the Login Person and his ID
    		com.matrixone.apps.common.Person loginPerson = (com.matrixone.apps.common.Person) DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
    		loginPerson = com.matrixone.apps.common.Person.getPerson(context);
    		String strLoginPersonID = (String)loginPerson.getObjectId();

    		try{

    			// Iterating over the List of Rel Ids of the Objects initially selected on the Affected Items List Page
    			for (int k=0; k<arrAffectedItemsRelIds.length; k++)
    			{
    				ContextUtil.startTransaction(context, true);

    				// Taking the Affected Items Rel Ids in String Tokenizer for getting them in usable format. Tokenizing on the basis of "," values
    				StringTokenizer stzRelId = new StringTokenizer(arrAffectedItemsRelIds[k],",", false);

    				// Taking the Object Ids in String Tokenizer for getting them in usable format. Tokenizing on the basis of "," values

    				while(stzRelId.hasMoreElements()) // For each Rel Id get the corresponding Obj Id
    				{
    					// Converting the tokens to String
    					String strTokenRelId = stzRelId.nextToken().toString();
    					String strTempString1 = StringUtils.replace(strTokenRelId, "]", "");
    					// Obtaining the Affected Item Relationship id in usable format
    					String strFinalAffectedItemRelId = StringUtils.replace(strTempString1, "[", "");

    					DomainObject domChangeObj = new DomainObject(strChangeObjID[0]);

    						// Getting the name of the Logged in Person
    					DomainObject domLoginPerson = new DomainObject(strLoginPersonID);
    					String strLoginPersonName = (String)domLoginPerson.getInfo(context,DomainConstants.SELECT_NAME);

    					// Getting the owner of the Change Object
    					String strChangeObjOwner = (String)domChangeObj.getOwner(context).toString();


    					if (((getTomidFromRelationshipFromID(context, strFinalAffectedItemRelId, DomainConstants.RELATIONSHIP_ASSIGNED_EC).equals(strLoginPersonID)) || strChangeObjOwner.equals(strLoginPersonName))&&(!strNewAssigneeID[k].equals(strLoginPersonID)))
    					{
    					String whrClause	= "id"+"=='"+strNewAssigneeID[k]+"'";

    					MapList mlPersons = domChangeObj.getRelatedObjects( context,
    																		DomainConstants.RELATIONSHIP_ASSIGNED_EC,
    																		DomainConstants.TYPE_PERSON,
    																		new StringList(DomainConstants.SELECT_ID),
    																		new StringList (DomainConstants.SELECT_RELATIONSHIP_ID),
    																		true,
    																		false,
    																		(short)1,
    																		whrClause,
    																		"");

    					Iterator mlPersonsItr = mlPersons.iterator();
    					String strAssignedECRelId = "";
    					while (mlPersonsItr.hasNext())
    					{
    						Map mapPersonObject = (Map) mlPersonsItr.next();
    						strAssignedECRelId = (String)mapPersonObject.get("id[connection]");
    					}

    					StringList slExistingAssignedECRelId =	getTomids(	context,
    																strFinalAffectedItemRelId);
    					Iterator itrExistingAssignedECRelId =  slExistingAssignedECRelId.iterator();

    					String strExistingAssignedECRelId= (String)itrExistingAssignedECRelId.next();
    					//Disconnecting the Assigned Affected Item Rel originally.
    					if(!("".equals(strExistingAssignedECRelId)))
    					{
    						DomainRelationship.disconnect(context,strExistingAssignedECRelId);
    					}

    					if("".equals(strAssignedECRelId))
    					{
    						DomainRelationship strDR =
    						DomainRelationship.connect(context,
    												   new DomainObject(strNewAssigneeID[k]),
    												   DomainConstants.RELATIONSHIP_ASSIGNED_EC,
    												   new DomainObject(strChangeObjID[0]));
    						strAssignedECRelId = strDR.toString();
    					}


    					 // Creating new ReltoRel START
    					connect( context,
    										RELATIONSHIP_ASSIGNEED_AFFECTED_ITEM,
    										strAssignedECRelId,
    										strFinalAffectedItemRelId,
    										false,
    										false);
    				 }
    			}
    			}
    			ContextUtil.commitTransaction(context);
    		   }catch (Exception e)
    			{
    				ContextUtil.abortTransaction(context);
    				throw (new FrameworkException(e));
    			}
        }
    /**
	 * getDirectAffectedItems, Method to retrieve the old revision
	 * for a given change and part context
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap
	 * @return String.
     * @since Common X3
	 * @throws Exception if the operation fails.
	*/
	public String getDirectAffectedItems(Context context, String args[])	throws Exception
	{
		String strPartId = args[0];
		String strECOId = args[1];

		String strOldPartId = null;

		StringList strlPartSelects =  new StringList(2);
		strlPartSelects.add(SELECT_NAME);
		strlPartSelects.add(SELECT_TYPE);

		DomainObject doPart = new DomainObject(strPartId);

		Map mapPartDetails = doPart.getInfo(context, strlPartSelects);
		String strPartName = (String) mapPartDetails.get(SELECT_NAME);
		String strPartType = (String) mapPartDetails.get(SELECT_TYPE);

		DomainObject doECO = new DomainObject(strECOId);

		StringList strlObjectSelects = new StringList(1);
		strlObjectSelects.add(SELECT_ID);

		String strObjWhereclause = "name == \"" + strPartName + "\"";

		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
		String strRelWhereclause = "attribute[" + strAttrAffectedItemCategory + "] == Direct && attribute[" + ATTRIBUTE_REQUESTED_CHANGE + "] == \"" + RANGE_FOR_REVISE + "\"";

		MapList mapListParts = doECO.getRelatedObjects(context,
                    RELATIONSHIP_AFFECTED_ITEM, strPartType, strlObjectSelects,
                    null, false, true, (short) 1, strObjWhereclause, strRelWhereclause);

        if (mapListParts.size() > 0)
        {
			Map mapPart = (Map) mapListParts.get(0);
			strOldPartId = (String) mapPart.get(SELECT_ID);
		}

		//should consider whether comparing against revision list and version list is required here

		return strOldPartId;

	}
	/**
     * Displays the policy drop down based on the change type and property settings.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following Strings, "objectId".
     * requestMap - a HashMap containing the request.
     * @return Object - String object which contains the policy drop down.
     * @throws Exception if operation fails.
     * @since Common X3
     */

    public Object getPolicy(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjectId = (String) paramMap.get("objectId");
        String strSymType = (String) requestMap.get("type");
        String languageStr = (String) paramMap.get("languageStr");
        String suiteKey = (String) requestMap.get("suiteKey");

        String strType = "";

        if (strSymType != null && strSymType.length() > 0 && !"null".equals(strSymType))
        {
        	int intIndex = strSymType.indexOf(",");
        	if (intIndex != -1)
        	{
        		strSymType = strSymType.substring(intIndex + 1, strSymType.length());
			}
	 	}
        if (strSymType != null && strSymType.length()>0 && !"null".equals(strSymType))
        {
            strType = PropertyUtil.getSchemaProperty(context, strSymType);
        }
        StringBuffer sbPolicy = new StringBuffer("<select name=\"Policy\">");
        String sCurrentPolicyName = "";
        if (strObjectId!=null && strObjectId.length() > 0)
        {
            setId(strObjectId);
            sCurrentPolicyName = getInfo(context,SELECT_POLICY);
            strType = getInfo(context, SELECT_TYPE);
        }
        String strMode = (String) requestMap.get("mode");
        //if in edit mode, can only change to the same type of PolicyClassification
        String currentPolicyClassification = "";
        if(sCurrentPolicyName.equals("") || "edit".equalsIgnoreCase(strMode))
        {
        	try {
                if (!sCurrentPolicyName.equals(""))
                {
        		    currentPolicyClassification = FrameworkUtil.getPolicyClassification(context, sCurrentPolicyName);
                }
                else //create page - get default policy setting
                {
                    strMode = "create";
                    // 374591
                    String policy = "";
                    //construct property key based on type
                    //String propKey = "emx" + suiteKey + ".Create";

                    //determine kind of object type based on the symbolic name for this instance
                    //grab the key after the _ in the symbolic name
                    //propKey += strSymType.substring(strSymType.indexOf("_") + 1, strSymType.length());
                    policy = "policy_" + strSymType.substring(strSymType.indexOf("_") + 1, strSymType.length());

                    //propKey += "PolicyDefault";
                    //sCurrentPolicyName = PropertyUtil.getSchemaProperty(context,(String)FrameworkProperties.getProperty(propKey));
                    sCurrentPolicyName = PropertyUtil.getSchemaProperty(context,policy);
        		    currentPolicyClassification = FrameworkUtil.getPolicyClassification(context, sCurrentPolicyName);
                }
        	}
        	catch (Exception err){
        		// PolicyClassification not set, default to Static Approval
        		//currentPolicyClassification = "StaticApproval";
        		  currentPolicyClassification = "DynamicApproval";

        	}
        	//Get the policies associated with the ECR
            MapList policies = mxType.getPolicies(context, strType, false); //getPolicies(context);

        	Iterator listItr = policies.iterator();
        	Map object = null;
        	String strPolicyName = "";
        	String sOtherPolicyName = "";

        	//Construct the policy dropdown
        	while (listItr.hasNext())
        	{
        		object = (Map) listItr.next();
        		String sPolicySelected ="selected=\"true\"";
        		strPolicyName = (String) object.get(SELECT_NAME);
        		String policyClassification = "";
        		try {
                    policyClassification = FrameworkUtil.getPolicyClassification(context, strPolicyName);
        		}
        		catch (Exception err){
            		// PolicyClassification not set, default to Static Approval
        			policyClassification = "StaticApproval";
        		}

        		if (policyClassification.equals(currentPolicyClassification))
        		{
        			sOtherPolicyName=i18nNow.getAdminI18NString("Policy", strPolicyName ,languageStr);
        			sbPolicy.append("<option value=\""+strPolicyName+"\" "+((strPolicyName.equals(sCurrentPolicyName))?sPolicySelected:"")+">"+sOtherPolicyName+"</option>");
        		}
        	}
        	sbPolicy.append("</select>");
        }

        String strPolicy = "";
        if("create".equals(strMode) || "edit".equalsIgnoreCase(strMode)) {
            strPolicy = sbPolicy.toString();
        } else {
            strPolicy = i18nNow.getAdminI18NString("Policy", sCurrentPolicyName.trim() ,languageStr);
        }
        return strPolicy;
 }
    /** In some scenrio DomainObject.getInfo method returns String OR StringList dynamically on selectable, This method is used to get StringList in such scerios.   
     * @param map contains selected data using domainobject getInfo method.
     * @param key contains key for accessing the data from the map
     * @return StringList.
     */
    private StringList getSelectedListFromGetInfoMap(Map map, String key) {
    	 Object obj = map.get(key);
    	 if (obj == null)
    		 return new StringList(0);
    	 return (obj instanceof String) ? new StringList((String) obj) : (StringList) obj;
     }


    	/**
         * this method gets the List of Affected Items. Gets the List
         * of Persons to be Notified. The following steps are performed: - The
         * Affected Parts,Specifications Connected to this Pariticular ECO are
         * Disconnected. - The related ECRs are disconnected - The Related Routes
         * Connected to this Particualr ECO are Disconnected
         *
         *
         * @param context
         *            the eMatrix <code>Context</code> object.
         * @param args
         *            holds the following input arguments: - The ObjectID of the
         *            Change Process
         * @throws Exception
         *             if the operation fails.
         * @since Common X3.
         */

public void cancelChangeProcess(Context context, String[] args)
 throws Exception // MethodName
{

		    HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		    String objectId = (String) paramMap.get("objectId");
		    String sReason = (String) paramMap.get("sReason");
		    String sDeleteAffectedItems = (String) paramMap.get("sDeleteAffectedItems");
		    String sDisconnectECRs = (String) paramMap.get("sDisconnectECRs");
		
		    boolean deleteAffectedItems = "true".equals(sDeleteAffectedItems);
		    boolean disconnectECRs = "true".equals(sDisconnectECRs);
		
		    DomainObject changeObj = DomainObject.newInstance(context, objectId,
		         DomainConstants.ENGINEERING);
		    String ATTRIBUTE_BRANCH_TO = PropertyUtil.getSchemaProperty(context,"attribute_BranchTo");
		    /** attribute "Reason For Cancel". */
		    String ATTRIBUTE_REASON_FOR_CANCEL = PropertyUtil.getSchemaProperty(context,"attribute_ReasonForCancel");
		    /** Person Corporate */
		    String PERSON_CORPORATE = PropertyUtil.getSchemaProperty(context,"person_Corporate");
		
		    String sOwner = null;
		    boolean isContextPushed = false;
		
		    try {
		
		        //PDCM Check for the sub changes
		        boolean isECHInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionEnterpriseChange",false,null,null);
		        if(isECHInstalled) {
		            Boolean canCancel = (Boolean)JPO.invoke(context, "emxChangeTask", null, "canCancelChange", JPO.packArgs(paramMap), Boolean.class);
		            if (!canCancel.booleanValue()) {
		                String strNotice = EnoviaResourceBundle.getProperty(context,                             
		                                "emxEnterpriseChangeStringResource",
		                                context.getLocale(),"emxEnterpriseChange.Pdcm.CanNotCancelChangeProcess");
		                MqlUtil.mqlCommand(context, "notice $1", strNotice);
		                return;
		            }
		        }
		
		    ContextUtil.pushContext(context, null, null, null);
		
		        // Send notification
		     String id = changeObj.getId();
		
		     String argsmail[] = { id, "ChangeCancelNotify" };
		     JPO.invoke(context, "emxNotificationUtil", null,
		             "objectNotification", argsmail);
		
		
		
		     isContextPushed = true;
		     // Disconnect affected items from this ECO
		     //String relPattern = RELATIONSHIP_AFFECTED_ITEM; // Relationship Name
		     // modified for 081643
		     String relPattern = RELATIONSHIP_AFFECTED_ITEM + "," + RELATIONSHIP_RAISED_AGAINST_ECR;
		     // ECO-PART,ECO-SPEC, shd be replaced here
		     StringList selectStmts = new StringList(2);
		     selectStmts.addElement(DomainConstants.SELECT_ID);
		     selectStmts.addElement(DomainConstants.SELECT_TYPE);
		     //HF-016433
		     selectStmts.addElement(DomainConstants.SELECT_CURRENT);
		
		     StringList selectRelStmts = new StringList(1);
		     selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
		
		     MapList affectedItems = new MapList();
		     Map objMap = null;
		    //373461
		     String policyName = changeObj.getInfo(context, DomainConstants.SELECT_POLICY);
		     String policyClassification = "";
		     if(policyName!=null && policyName.length() != 0){
		         policyClassification = FrameworkUtil.getPolicyClassification(context, policyName);
		     }
		     affectedItems = changeObj.getRelatedObjects(context, relPattern, // relationship
		                                                                 // pattern
		             DomainConstants.QUERY_WILDCARD, // object pattern
		             selectStmts, // object selects
		             selectRelStmts, // relationship selects
		             false, // to direction
		             true, // from direction
		             (short) 1, // recursion level
		             null, // object where clause
		             null); // relationship where clause
		
		     Iterator objItr = (Iterator) affectedItems.iterator();
		     // Create an object for use in the loop.
		     DomainObject object = new DomainObject();
		     while (objItr.hasNext()) {
		         objMap = (Map) objItr.next();
		         //HF-016433
		         String currentState = (String)objMap.get(DomainConstants.SELECT_CURRENT);
		         // If the user has chosen to delete the affected items from the
		         // database,there is no need to disconnect (it will happen through delete)
		
		         if (deleteAffectedItems)
		         {
		             //Start : HF-016433 - Added a condition
		             if(!((currentState.equalsIgnoreCase(STATE_ECPART_RELEASE))||
		                 (currentState.equalsIgnoreCase(STATE_PARTSPECIFICATION_RELEASE))||
		                 (currentState.equalsIgnoreCase(STATE_CADMODEL_RELEASE))||
		                 (currentState.equalsIgnoreCase(STATE_CADDRAWING_RELEASE))||
		                 (currentState.equalsIgnoreCase(STATE_DRAWINGPRINT_RELEASE)))){
		             object.setId((String) objMap.get(DomainConstants.SELECT_ID));
		             object.deleteObject(context);
		             }
		             else {
		                 DomainRelationship.disconnect(context, (String) objMap
		                         .get(DomainConstants.SELECT_RELATIONSHIP_ID));
		             }
		             //End : HF-016433
		         } else {
		             DomainRelationship.disconnect(context, (String) objMap
		                     .get(DomainConstants.SELECT_RELATIONSHIP_ID));
		         }
		     }
		
		
		
		     // Check for associated ECRs
		     // expand ECO to get associated ECRs
		     MapList relatedECRs = changeObj.getRelatedObjects(context,
		             DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT, // relationship
		                                                                     // pattern
		             DomainConstants.TYPE_ECR, // object pattern
		             selectStmts, // object selects
		             selectRelStmts, // relationship selects
		             false, // to direction
		             true, // from direction
		             (short) 1, // recursion level
		             null, // object where clause
		             null); // relationship where clause
		
		     if (relatedECRs.size() > 0 && disconnectECRs) {
		         objItr = (Iterator) relatedECRs.iterator();
		          while (objItr.hasNext()) {
		             objMap = (Map) objItr.next();
		                  DomainRelationship.disconnect(context, (String) objMap
		                         .get(DomainConstants.SELECT_RELATIONSHIP_ID));
		             }
		         }
		
		
		
		     MapList routeList = new MapList();
		     
		     String SELECT_INBOX_TASK_ID = "to[" + DomainConstants.RELATIONSHIP_ROUTE_TASK + "].from[" + DomainConstants.TYPE_INBOX_TASK + "].id";
		     StringList listRouteTaskIds = new StringList();
		
		     // Object selects for route
		     selectStmts.addElement(DomainConstants.SELECT_OWNER);
		     selectStmts.addElement(SELECT_INBOX_TASK_ID);
		
		     // expand ECO to get associated Routes
		     routeList = changeObj.getRelatedObjects(context,
		             DomainConstants.RELATIONSHIP_OBJECT_ROUTE, // relationship
		                                                         // pattern
		             DomainConstants.TYPE_ROUTE+ SYMB_COMMA + DomainConstants.TYPE_ROUTE_TEMPLATE, // object pattern
		             selectStmts, // object selects
		             selectRelStmts, // relationship selects
		             false, // to direction
		             true, // from direction
		             (short) 1, // recursion level
		             null, // object where clause
		             null); // relationship where clause
		
		
		
		
		     StringList toListRoutes = new StringList();
		
		     String strRouteType = null;
		
		     if (routeList.size() > 0) {
		
		         objItr = (Iterator) routeList.iterator();
		         while (objItr.hasNext()) {
		             objMap = (Map) objItr.next();
		
		             strRouteType = (String) objMap.get(DomainConstants.SELECT_TYPE);
		             
		             if (objMap.get(SELECT_INBOX_TASK_ID) != null) {
		            	 listRouteTaskIds.addAll(getSelectedListFromGetInfoMap(objMap, SELECT_INBOX_TASK_ID));
		             }
		
		             // Disconnect this ECO from the Route
		             DomainRelationship.disconnect(context, (String) objMap
		                     .get(DomainConstants.SELECT_RELATIONSHIP_ID));
		
		    		// send notification in case of route objects
		    		if ((DomainConstants.TYPE_ROUTE).equals(strRouteType))
		    		{
		             // Send route owner notification of ECO cancel
		             if (!toListRoutes.contains((String) objMap
		                     .get(DomainConstants.SELECT_OWNER)))
		                 toListRoutes.addElement((String) objMap
		                         .get(DomainConstants.SELECT_OWNER));
		
		             // If this is the only object connected to the route,
		             // terminate the route
		             try {
		                 Route route = (Route) DomainObject.newInstance(context,
		                         (String) objMap.get(SELECT_ID), ENGINEERING);
		                 String routeStatus = (String) route
		                         .getAttributeValue(context,
		                                 DomainConstants.ATTRIBUTE_ROUTE_STATUS);
		                 // Stop only if current status is "Started"
		                 if (routeStatus.equals("Started"))
		                     route.setAttributeValue(context,
		                             ATTRIBUTE_ROUTE_STATUS, "Stopped");
		             } catch (ClassCastException ex) {
		
		             }
		         }
		     }
		     } 
		     
		     if (listRouteTaskIds.size() > 0) {
		    	 DomainObject.deleteObjects(context, (String[]) listRouteTaskIds.toArray(new String[listRouteTaskIds.size()]));
		     }
		
		
		    changeObj.setAttributeValue(context, ATTRIBUTE_BRANCH_TO, "Cancel");
		     // Set the reason
		     changeObj.setAttributeValue(context, ATTRIBUTE_REASON_FOR_CANCEL,
		             sReason);
		     // Promote ECO to Cancel State
		
		     String ChangeObjectOwner=changeObj.getOwner(context).getName();
		
		
		     changeObj.promote(context);
		
		     changeObj.setOwner(context, PERSON_CORPORATE);
		     ContextUtil.popContext(context);
		     isContextPushed=false;
		    } catch (Exception e) {
		    	throw e;
		    } finally {
			     if (isContextPushed)
			     {
			         ContextUtil.popContext(context);
			     }
		    }
    }
/**
* Updates the Review list field values in ECR WebForm.
* @param context the eMatrix <code>Context</code> object
* @param args holds a Map with the following input arguments:
* objectId objectId of the context Engineering Change
* New Value objectId of updated Review List value
* @throws Exception if the operations fails
* @since Common X3
*/
   public void connectApproverReviewerList (Context context, String[] args) throws Exception {
       try{
           //unpacking the Arguments from variable args
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap paramMap   = (HashMap)programMap.get("paramMap");
           String strRelationship = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
           //Calling the common connect method to connect objects
			String strNewValue = (String)paramMap.get("New OID");
//			String strObjectId = (String)paramMap.get("objectId");
			DomainRelationship drship=null;
//			DomainRelationship newRelationship=null;



if (strNewValue == null || "".equals(strNewValue) || "Unassigned".equalsIgnoreCase(strNewValue) || "null".equalsIgnoreCase(strNewValue) || " ".equals(strNewValue))
			{

							strNewValue = (String)paramMap.get("New Value");
			}




			if((strNewValue != null) && !(strNewValue.equals("")) ||  "Unassigned".equalsIgnoreCase(strNewValue) || "null".equalsIgnoreCase(strNewValue)) {
//			DomainObject domObjectChangeNew =  new DomainObject(strObjectId);
			DomainObject newValue =  new DomainObject(strNewValue);
			String strAttribute = newValue.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);

			if(strAttribute.equals("Review")){
			drship = connect(context,paramMap,strRelationship);
			

			//	new DomainRelationship(newRelationship.connect(context,domObjectChangeNew,strRelationship,newValue)) ;
			drship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strAttribute);
			}
			if(strAttribute.equals("Approval")){
			drship = connect(context,paramMap,strRelationship);
			//drship = new DomainRelationship(newRelationship.connect(context,domObjectChangeNew,strRelationship,newValue)) ;
			drship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strAttribute);
			}
			}
          }catch(Exception ex){
           throw  new FrameworkException((String)ex.getMessage());
         }
   }
   
   
   /**
    * Connects ECR/ECO with the Passed Object.
    * @param context the eMatrix <code>Context</code> object
    * @param Hashmap holds the input arguments:
    * strRelationship holds relationship with which ECR will be connected
    * New Value is object Id of updated Object
    * @throws Exception if the operations fails
    * @since Common X3.
 */

     public DomainRelationship connect(Context context , HashMap paramMap ,String strRelationship)throws Exception {
 		 try{
 			 DomainRelationship drship=null;
 						//Relationship name
 		 				DomainObject oldListObject = null;
 		 				DomainObject newListObject = null;
 		                //Getting the ECR Object id and the new MemberList object id
 		                String strChangeobjectId = (String)paramMap.get("objectId");
 		                DomainObject changeObj =  new DomainObject(strChangeobjectId);
 						//for bug 343816 and 343817 starts
 						String strNewToTypeObjId = (String)paramMap.get("New OID");

 		                if (strNewToTypeObjId == null || "null".equals(strNewToTypeObjId) || strNewToTypeObjId.length() <= 0 
                                 || "Unassigned".equals(strNewToTypeObjId)) {
 		                    strNewToTypeObjId = (String)paramMap.get("New Value");
 						}
 						//for bug 343816 and 343817 ends
 		                String strOldToTypeObjId = (String)paramMap.get("Old OID");
 		                try {
 		                	ContextUtil.pushContext(context);
 		                	DomainRelationship newRelationship=null;
 		                	RelationshipType relType = new RelationshipType(strRelationship);
 		                	if (strOldToTypeObjId != null && !"null".equals(strOldToTypeObjId) && strOldToTypeObjId.length() > 0 
 		                			&& !"Unassigned".equals(strOldToTypeObjId)) {
 		                		oldListObject = new DomainObject(strOldToTypeObjId);
 		                		changeObj.disconnect(context,relType,true,oldListObject);
 		                	}

 		                	if(strNewToTypeObjId != null && !"null".equals(strNewToTypeObjId) && strNewToTypeObjId.length() > 0 
 		                			&& !"Unassigned".equals(strNewToTypeObjId)) {
 		                		newListObject = new DomainObject(strNewToTypeObjId);
 		                		drship = new DomainRelationship(newRelationship.connect(context,changeObj,relType,newListObject)) ;
 		                	}
 		                } 
 		                catch(Exception ex){
 		                	//ex.printStackTrace();
 		                }
 		                finally{
 		                	ContextUtil.popContext(context);
 		                }
                         return drship;
          } catch(Exception ex){
              throw  new FrameworkException((String)ex.getMessage());
          }

     }
   
   
  }


