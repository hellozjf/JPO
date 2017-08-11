/*
 ** ${CLASS:enoECMChangeActionBase}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.AttributeTypeList;
import matrix.db.BusinessInterface;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.db.Vault;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.UOMUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.dassault_systemes.enovia.changeaction.constants.ActivitiesOperationConstants;
import com.dassault_systemes.enovia.changeaction.factory.ChangeActionFactory;
import com.dassault_systemes.enovia.changeaction.factory.ProposedActivityFactory;
import com.dassault_systemes.enovia.changeaction.interfaces.IBusinessObjectOrRelationshipObject;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeAction;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeActionServices;
import com.dassault_systemes.enovia.changeaction.interfaces.IOperationArgument;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedActivity;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedChanges;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeManagement;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrder;
import com.dassault_systemes.enovia.enterprisechangemgt.admin.ECMAdmin;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;
import com.dassault_systemes.enovia.changeaction.dictionaryservices.AllowedOperationsServices;

public class enoECMChangeActionBase_mxJPO extends emxDomainObject_mxJPO 
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static final String ATTR_VALUE_MANDATORY  = "Mandatory";
	protected static String RESOURCE_BUNDLE_COMPONENTS_STR = "emxEnterpriseChangeMgtStringResource";
	protected static final String SELECT_NEW_VALUE = "New Value";
	private ChangeManagement changeManagement = null;

    private static final String FORMAT_DATE = "date";
    private static final String FORMAT_NUMERIC = "numeric";
    private static final String FORMAT_INTEGER = "integer";
    private static final String FORMAT_BOOLEAN = "boolean";
    private static final String FORMAT_REAL = "real";
    private static final String FORMAT_TIMESTAMP = "timestamp";
    private static final String FORMAT_STRING = "string";
    protected static final String INPUT_TYPE_TEXTBOX = "textbox";
    private static final String INPUT_TYPE_TEXTAREA = "textarea";
    private static final String INPUT_TYPE_COMBOBOX = "combobox";
    protected static final String SETTING_INPUT_TYPE = "Input Type";
    private static final String SETTING_FORMAT = "format";
    private static final String FORMAT_CHOICES = "choices";
    public static final String SUITE_KEY = "EnterpriseChangeMgt";

	/**
	 * Constructor
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public enoECMChangeActionBase_mxJPO(Context context, String[] args) throws Exception {
		super(context, args);
	}

	/**
	 * Method to return CA Affecetd items
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAffectedItems(Context context, String[] args)throws Exception {
		Map programMap = (HashMap)JPO.unpackArgs(args);
		String changeObjId = (String)programMap.get("objectId");
		return new ChangeAction(changeObjId).getAffectedItems(context);
	}
	/**
	 * This method is used as access function for is Old Legacy Mode
	 * @param context
	 * @return True or False
	 * @throws Exception
	 */
	public boolean isOldLegacyMode(Context context,String []args) throws Exception {
		return ChangeUtil.isLegacyEnable(context);
	}
	/**
	 * This method is used as access function for is New Legacy Mode
	 * @param context
	 * @return True or False
	 * @throws Exception
	 */
	public boolean isNewLegacyMode(Context context,String []args) throws Exception {
		return ChangeUtil.isLegacyDisable(context);
	}
	/**
	 * Method to update Requested change value
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int updateRequestedChange(Context context, String[] args) throws Exception
	{
		try
		{

			String message="";
			String  SELECT_PHYSICALID = "physicalid";
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
			HashMap requestMap = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
			String strChangeObjId    = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			String strChangeActionId       = (String)requestMap.get(ChangeConstants.OBJECT_ID);
			String strNewRequestedChangeValue = (String)paramMap.get(ChangeConstants.NEW_VALUE);
			String strProposedActivityRelId = (String)paramMap.get(ChangeConstants.SELECT_REL_ID);
			StringList objectSelects=new StringList(DomainObject.SELECT_TYPE);
			objectSelects.add(DomainObject.SELECT_POLICY);
			objectSelects.add(SELECT_PHYSICALID);
			DomainObject affecteditemObj=new DomainObject(strChangeObjId);
			String language = context.getSession().getLanguage();
			Map affectediteminfoMap= affecteditemObj.getInfo(context,objectSelects);
			String typeName=(String)affectediteminfoMap.get(DomainObject.SELECT_TYPE);
			String policyName=(String)affectediteminfoMap.get(DomainObject.SELECT_POLICY);
			String[] relationshipIds=new String[1];
			  StringList selectable=new StringList( DomainRelationship.SELECT_TYPE);
			  selectable.add(DomainRelationship.SELECT_TO_ID);
			  selectable.add(DomainRelationship.SELECT_FROM_ID);
			  relationshipIds[0]=strProposedActivityRelId;
			  DomainRelationship ProposedActivityRel =new DomainRelationship(strProposedActivityRelId);
			  MapList mapList=ProposedActivityRel.getInfo(context, relationshipIds,selectable);
			  Map map=(Map)mapList.get(0);
			  strChangeActionId=(String)map.get(DomainRelationship.SELECT_FROM_ID);
			String targetStatus="";
			String strPreviousReasonForChange="";
			  

			//IF THIS IS USED FOR BOTH MODE
			//1 Get the CA
			IChangeAction iChangeAction=ChangeAction.getChangeAction(context, strChangeActionId);

			//2 Get the proposed
			List<IProposedChanges> proposedChanges = iChangeAction.getProposedChanges(context);



			//3 Loop on the proposed until you find your objectSelects
			for(IProposedChanges proposed : proposedChanges)
			{
				BusinessObject targetDo = proposed.getWhere();
				String targetDoId=targetDo.getObjectId(context);
				String relId =	new DomainObject(proposed.getBusinessObject()).getInfo(context, "to["+ChangeConstants.RELATIONSHIP_PROPOSED_ACTIVITIES+"].id");

				if(affectediteminfoMap.get(SELECT_PHYSICALID).equals(targetDoId) && relId.equalsIgnoreCase(strProposedActivityRelId))
				{
					//Now we find the good proposed

					//1 Get activities
					List<IProposedActivity> activities = proposed.getActivites();						
					strPreviousReasonForChange=proposed.getWhy();
					//2 If I want to update proposed change: 
					if(ChangeConstants.FOR_REVISE.equalsIgnoreCase(strNewRequestedChangeValue) 
							|| ChangeConstants.FOR_MAJOR_REVISE.equalsIgnoreCase(strNewRequestedChangeValue)
							|| ChangeConstants.FOR_NONE.equalsIgnoreCase(strNewRequestedChangeValue)
							|| ChangeConstants.FOR_EVOLUTION.equalsIgnoreCase(strNewRequestedChangeValue))
					{
						//Major and / or minor allowedOperation
				List allowedOperation = AllowedOperationsServices.getAllowedOperationsFromPolicy(context, policyName);
    			boolean isMajorReviseSupported = false;
    			boolean isMinorReviseSupported = false;
						for(int i=0; i<allowedOperation.size(); i++)
						{
    				String operation = (String)allowedOperation.get(i);
    				if(ChangeConstants.MINOR_REVISE_MODELER.equalsIgnoreCase(operation))
							{
    					isMinorReviseSupported = true;
							}
    				else if(ChangeConstants.MAJOR_REVISE_MODELER.equalsIgnoreCase(operation))
							{
    					isMajorReviseSupported = true;
    			}
						}
    			
						//Minor Revise
						if(ChangeConstants.FOR_REVISE.equalsIgnoreCase(strNewRequestedChangeValue))
						{
    				if(isMinorReviseSupported){
				String newRevision=affecteditemObj.getNextSequence(context);

								//First delete the activities behind proposed
								for( int i=0;i<activities.size();i++)
								{
									IProposedActivity activity=activities.get(i);
									if(strPreviousReasonForChange.isEmpty()){

										strPreviousReasonForChange=activity.getWhy();

									}
									activity.delete(context);
								}
								//Then change the proposed change
								proposed.setProposedChangeAsNewMinorVersion(context, newRevision);

								proposed.SetWhyComment(context, strPreviousReasonForChange);
			}
    				else{
								message = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"EnterpriseChangeMgt.Alert.RequestedChangeMinorReviseNotSupported",language);
    				}
    			}
						//Major Revise
						else if(ChangeConstants.FOR_MAJOR_REVISE.equalsIgnoreCase(strNewRequestedChangeValue))
						{
    				if(isMajorReviseSupported){
    					String newMajorRevision=affecteditemObj.getNextMajorSequence(context);
								//First delete the activities behind proposed
								for( int i=0;i<activities.size();i++)
								{
									IProposedActivity activity=activities.get(i);
									if(strPreviousReasonForChange.isEmpty()){
										strPreviousReasonForChange=activity.getWhy();
									}
									activity.delete(context);
								}
								//Then change the proposed change
								proposed.setProposedChangeAsNewVersion(context, newMajorRevision);
								proposed.SetWhyComment(context, strPreviousReasonForChange);
			}
    				else{
								message = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"EnterpriseChangeMgt.Alert.RequestedChangeMajorReviseNotSupported",language);
							}
						}
	    				
						//None
						else if(ChangeConstants.FOR_NONE.equalsIgnoreCase(strNewRequestedChangeValue))
						{
							for( int i=0;i<activities.size();i++)
							{
								IProposedActivity activity=activities.get(i);
								if(strPreviousReasonForChange.isEmpty()){
									strPreviousReasonForChange=activity.getWhy();
    				}    				
								activity.delete(context);
							}
							proposed.setProposedChangeAsNone(context);
							proposed.SetWhyComment(context, strPreviousReasonForChange);
    			}

						//Evolution
						else if(ChangeConstants.FOR_EVOLUTION.equalsIgnoreCase(strNewRequestedChangeValue))
						{
							for( int i=0;i<activities.size();i++)
							{
								IProposedActivity activity=activities.get(i);
								if(strPreviousReasonForChange.isEmpty()){
									strPreviousReasonForChange=activity.getWhy();
								}    				
								activity.delete(context);
							}
							proposed.setProposedChangeAsNewEvolution(context, "", "");
							proposed.SetWhyComment(context, strPreviousReasonForChange);
						}

			}			
					else if(ChangeConstants.FOR_RELEASE.equalsIgnoreCase(strNewRequestedChangeValue)
							||ChangeConstants.FOR_OBSOLETE.equalsIgnoreCase(strNewRequestedChangeValue)
							||ChangeConstants.FOR_UPDATE.equalsIgnoreCase(strNewRequestedChangeValue))
					{

				
						//FOR RELEASE
					if(ChangeConstants.FOR_RELEASE.equalsIgnoreCase(strNewRequestedChangeValue))
						{
							//1 First delete the activities behind proposed
							for( int i=0;i<activities.size();i++)
							{
								IProposedActivity activity=activities.get(i);
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
						targetStatus=ChangeConstants.RElEASE;
							proposed.createChangeStatusActivity(context, 0, affecteditemObj, targetStatus,strPreviousReasonForChange, null, null);
						}
						//FOR OBSOLETE
						if(ChangeConstants.FOR_OBSOLETE.equalsIgnoreCase(strNewRequestedChangeValue))
						{

							//1 First delete the activities behind proposed
							/*	for(IProposedActivity activity : activities)
								{
									strPreviousReasonForChange=activity.getWhy();
									activity.delete(context);
								}*/
							for( int i=0;i<activities.size();i++)
							{
								IProposedActivity activity=activities.get(i);
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
						targetStatus=ChangeConstants.OBSOLETE;
							proposed.createChangeStatusActivity(context, 0, affecteditemObj, targetStatus,strPreviousReasonForChange, null, null);
						}
						//FOR UPDATE
						if(ChangeConstants.FOR_UPDATE.equalsIgnoreCase(strNewRequestedChangeValue))
						{

							//1 First delete the activities behind proposed
							for( int i=0;i<activities.size();i++)
							{
								IProposedActivity activity=activities.get(i);
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
					IBusinessObjectOrRelationshipObject  iBusinessObjectOrRelationshipObject=ProposedActivityFactory.CreateIProposedActivityArgument(affecteditemObj);
							proposed.createModifyActivity(context, 0, iBusinessObjectOrRelationshipObject, null,strPreviousReasonForChange, null, null);
						}

					}

				}
			}
			//if else block is for handling validation TODO
			if ("".equals(message)) {

				 return 0;//operation success
			 }
			 else {
				 emxContextUtil_mxJPO.mqlNotice(context, message);
				 return 1;// for failure
			 }
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
	}
	/**
	 * Method to update the Reason for Change Value
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public void updateReasonForChangeValue(Context context, String[] args) throws Exception
	  {

		try {
			String SELECT_PHYSICALID = "physicalid";
			  HashMap programMap = (HashMap)JPO.unpackArgs(args);
			  HashMap paramMap   = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
			  HashMap requestMap = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
			String strChangeObjId = (String) paramMap.get(ChangeConstants.OBJECT_ID);
			  String strChangeActionId       = (String)requestMap.get(ChangeConstants.OBJECT_ID);
			String strNewRequestedChangeValue = (String) paramMap.get(ChangeConstants.NEW_VALUE);
			  String strProposedActivityRelId = (String)paramMap.get(ChangeConstants.SELECT_REL_ID);
			StringList objectSelects = new StringList(DomainObject.SELECT_TYPE);
			objectSelects.add(DomainObject.SELECT_POLICY);
			objectSelects.add(SELECT_PHYSICALID);
			DomainObject affecteditemObj = new DomainObject(strChangeObjId);
			String strNewReasonForChangeValue = (String) paramMap.get(ChangeConstants.NEW_VALUE);

			Map affectediteminfoMap = affecteditemObj.getInfo(context, objectSelects);

			  String[] relationshipIds=new String[1];
			  StringList selectable=new StringList( DomainRelationship.SELECT_TYPE);
			  selectable.add(DomainRelationship.SELECT_TO_ID);
			selectable.add(DomainRelationship.SELECT_FROM_ID);
			  relationshipIds[0]=strProposedActivityRelId;
			  DomainRelationship ProposedActivityRel =new DomainRelationship(strProposedActivityRelId);
			  MapList mapList=ProposedActivityRel.getInfo(context, relationshipIds,selectable);
			  Map map=(Map)mapList.get(0);
			strChangeActionId = (String) map.get(DomainRelationship.SELECT_FROM_ID);


			// 1 Get the CA
			IChangeAction iChangeAction = ChangeAction.getChangeAction(context, strChangeActionId);

			// 2 Get the proposed
			List<IProposedChanges> proposedChanges = iChangeAction.getProposedChanges(context);


			// 3 Loop on the proposed until you find your objectSelects
			for (IProposedChanges proposed : proposedChanges) {

				BusinessObject targetDo = proposed.getWhere();
				
				String relId =	new DomainObject(proposed.getBusinessObject()).getInfo(context, "to["+ChangeConstants.RELATIONSHIP_PROPOSED_ACTIVITIES+"].id");
				
				String targetDoId = targetDo.getObjectId(context);

				if (affectediteminfoMap.get(SELECT_PHYSICALID).equals(targetDoId) && relId.equalsIgnoreCase(strProposedActivityRelId)) {
					List<IProposedActivity> activities = proposed.getActivites();

					for (IProposedActivity activity : activities) {
						activity.SetWhyComment(context, strNewReasonForChangeValue);
					}

					proposed.SetWhyComment(context, strNewReasonForChangeValue);

			  }			  
		  }

		} catch (Exception ex) {
			  ex.printStackTrace();
			  throw new FrameworkException(ex.getMessage());
		  }
	  }
 	/**
	 * Method to return implemented items
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getImplementedItems(Context context, String[] args)throws Exception {
		Map programMap = (HashMap)JPO.unpackArgs(args);
		String changeObjId = (String)programMap.get("objectId");
	//	return new ChangeAction(changeObjId).getImplementedItems(context); calling the new API to support mode switch
		return new ChangeAction(changeObjId).getRealizedChanges(context);
	}

	/**
	 * Method to return prerequisites
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getPrerequisites(Context context, String[] args)throws Exception {
		Map programMap = (HashMap)JPO.unpackArgs(args);
		String changeObjId = (String)programMap.get("objectId");
		return new ChangeManagement(changeObjId).getPrerequisites(context,ChangeConstants.TYPE_CHANGE_ACTION);
	}

	/**
	 * Method to return prerequisites
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getRelatedItems(Context context, String[] args)throws Exception {
		Map programMap = (HashMap)JPO.unpackArgs(args);
		String changeObjId = (String)programMap.get("objectId");
		return new ChangeAction(changeObjId).getRelatedItems(context);
	}
	/**
	 * Method to return Referential 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getReferntial(Context context, String[] args)throws Exception {
		Map programMap = (HashMap)JPO.unpackArgs(args);
		String changeObjId = (String)programMap.get("objectId");
		MapList mapListReferential=new MapList();

		IChangeAction mChangeAction=ChangeAction.getChangeAction(context, changeObjId);
		List referentialObjList=mChangeAction.GetReferentialObjects(context);
		Iterator referentialList=referentialObjList.iterator();
		while(referentialList.hasNext()){
			BusinessObject refObject=(BusinessObject) referentialList.next();
			Map referential =new HashMap();
			referential.put(DomainConstants.SELECT_ID, refObject.getObjectId());
			mapListReferential.add(referential);
			}
		return  mapListReferential;
	}
    /**
	 * excludeReferentialOIDs() method returns OIDs of Referential
	 * which are already connected to context change object
	 * @param context Context : User's Context.
	 * @param args String array
	 * @return The StringList value of OIDs
	 * @throws Exception if searching Parts object fails.
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeReferentialOIDs(Context context, String args[])throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String  strChangeId = (String) programMap.get("objectId");
		StringList strlReferentialList = new StringList();

		if (ChangeUtil.isNullOrEmpty(strChangeId))
			return strlReferentialList;

		try
		{
			setId(strChangeId);
			strlReferentialList.addAll(getInfoList(context, "from["+ChangeConstants.RELATIONSHIP_RELATED_ITEM+"].to.id"));

		}
		catch (Exception e)
		{
			throw new FrameworkException(e.getMessage());
		}
		return strlReferentialList;
	}		
/**
	 * Method to add Responsible Organizatoin for CA.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public void connectResponsibleOrganization(Context context, String[] args)throws Exception
	{
		try
		{
			Map programMap = (HashMap)JPO.unpackArgs(args);
			HashMap hmParamMap = (HashMap)programMap.get("paramMap");
			String strChangeObjId = (String)hmParamMap.get("objectId");
			String strNewResponsibleOrgName = (String)hmParamMap.get(ChangeConstants.NEW_VALUE);
		    this.setId(strChangeObjId);
		    if(UIUtil.isNotNullAndNotEmpty(strNewResponsibleOrgName)) {
				this.setPrimaryOwnership(context, ChangeUtil.getDefaultProject(context), strNewResponsibleOrgName);
	  		}
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}

	/**
	 * connectTechAssignee -
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public void connectTechAssignee(Context context, String[] args)throws Exception
	{
		try
		{
			Map programMap = (HashMap)JPO.unpackArgs(args);
			HashMap hmParamMap = (HashMap)programMap.get("paramMap");
			String strChangeObjId = (String)hmParamMap.get("objectId");
			String strNewTechAssignee = (String)hmParamMap.get("New OID");
		    String relTechAssignee = PropertyUtil.getSchemaProperty(context, "relationship_TechnicalAssignee");

		    ChangeAction changeAction = new ChangeAction(strChangeObjId);
		    changeAction.connectTechAssigneeToCA(context, strChangeObjId, strNewTechAssignee, relTechAssignee);
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}
	/**
	 * connectContributor - Connect Change Action and Person with technical Assignee rel
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public void connectContributor(Context context, String[] args)throws Exception
	{
		try
		{
			boolean currentIsOwner = false;
			Map programMap = (HashMap)JPO.unpackArgs(args);
			HashMap hmParamMap = (HashMap)programMap.get("paramMap");
			String strChangeObjId = (String)hmParamMap.get("objectId");
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			IChangeAction iChangeAction=ChangeAction.getChangeAction(context, strChangeObjId);
			
			String strCurrentUser = context.getUser();
			String strCAOwner = iChangeAction.getOwner(context);
			currentIsOwner = strCurrentUser.equalsIgnoreCase(strCAOwner)?true:false;
			//This code is for to skip connect operation while performing with Assignee of CA.
			if(!currentIsOwner)
			{
				return;
			}
			
			String[] strNewContributorArr = (String[])requestMap.get("ContributorHidden");
			String strNewContributors=null;
			if(strNewContributorArr != null && strNewContributorArr.length > 0){
				strNewContributors = strNewContributorArr[0];				
			}
			List newContributorNameList=new MapList();
			StringTokenizer strNewContributorList = new StringTokenizer(strNewContributors,",");
			while (strNewContributorList.hasMoreTokens()){
				String strContributor = strNewContributorList.nextToken().trim();
				Person personObj=new Person(strContributor);
				String personName=personObj.getInfo(context,SELECT_NAME);
				newContributorNameList.add(personName);
			}			
			List oldContributorNameList=iChangeAction.GetContributors(context);
			List contributorDisconnectList=differenceBetweenList(oldContributorNameList, newContributorNameList);
			List contributorConnectList=differenceBetweenList(newContributorNameList, oldContributorNameList);
			Iterator OldContributorDisconnectList=contributorDisconnectList.iterator();
			while(OldContributorDisconnectList.hasNext()){
				String contributorName=(String) OldContributorDisconnectList.next();
				//BusinessObject objContributer=PersonUtil.getPersonObject(context,contributorName);
				iChangeAction.RemovePersonFromContributors(context, contributorName);
			}
			Iterator NewContributorConnectList=contributorConnectList.iterator();
			while(NewContributorConnectList.hasNext()){
				String contributorName=(String) NewContributorConnectList.next();
				//BusinessObject objContributer=PersonUtil.getPersonObject(context,contributorName);
				iChangeAction.AddPersonAsContributor(context, contributorName);
			}
		}
		catch(Exception e)
		{
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * connectFollower - Connect Change Action with Person with Change Follower rel
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public void connectFollower(Context context, String[] args)throws Exception
	{
		try
		{
			boolean currentIsOwner = false;
			Map programMap = (HashMap)JPO.unpackArgs(args);
			HashMap hmParamMap = (HashMap)programMap.get("paramMap");
			String strChangeObjId = (String)hmParamMap.get("objectId");
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			IChangeAction iChangeAction=ChangeAction.getChangeAction(context, strChangeObjId);

			String strCurrentUser = context.getUser();
			String strCAOwner = iChangeAction.getOwner(context);
			currentIsOwner = strCurrentUser.equalsIgnoreCase(strCAOwner)?true:false;
			//This code is for to skip connect operation while performing with Assignee of CA.
			if(!currentIsOwner)
			{
				return;
			}
			
			String[] strNewFollowerArr = (String[])requestMap.get("FollowerHidden");
			String strNewFollowers=null;
			if(strNewFollowerArr != null && strNewFollowerArr.length > 0){
				strNewFollowers = strNewFollowerArr[0];				
			}
			List newFollowerList=new MapList();
			StringTokenizer strNewFollowerList1 = new StringTokenizer(strNewFollowers,",");
			while (strNewFollowerList1.hasMoreTokens()){
				String strFollower = strNewFollowerList1.nextToken().trim();
				Person personObj=new Person(strFollower);
				String personName=personObj.getInfo(context,SELECT_NAME);
				newFollowerList.add(personName);
			}
			List oldFollowerList=iChangeAction.GetFollowers(context);
			List followerDisconnectList=differenceBetweenList(oldFollowerList, newFollowerList);
			List followerConnectList=differenceBetweenList(newFollowerList, oldFollowerList);
			Iterator oldfollowerDisconnectList=followerDisconnectList.iterator();
			while(oldfollowerDisconnectList.hasNext()){
				String personName=(String) oldfollowerDisconnectList.next();
				iChangeAction.RemovePersonFromFollowers(context, personName);
				}
			Iterator newFollowerConnectList=followerConnectList.iterator();
			while(newFollowerConnectList.hasNext()){
				String personName=(String) newFollowerConnectList.next();
				iChangeAction.AddPersonAsFollower(context, personName);
				}
		}
		catch(Exception e){
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * differenceBetweenList - return the difference(A-B) between firstList and secondList
	 * @param context
	 * @param List firstList
	 * @param List secondList
	 * @return List result(Fist-Second)
	 * @throws Exception
	 */

	public List differenceBetweenList(List firstList,List secondList)throws Exception
	{
		List resulList=new MapList();
		try
		{
			if(!firstList.isEmpty()){
				resulList.addAll(firstList);
			}
			if(!resulList.isEmpty()){
				resulList.removeAll(secondList);
			}			
		}
		catch(Exception e){
			throw new FrameworkException(e.getMessage());
		}
		return resulList;
	}
	/**
  	 * Select the Follower Field 
  	 * @param Context context
  	 * @param args holds information about object.
  	 * @return Follower Field.
  	 * @throws Exception if operation fails.
  	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public String selectFollower(Context context,String[] args)throws Exception
	{
		boolean isEditable = false;
		StringBuilder sb = new StringBuilder();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String strMode = (String) requestMap.get("mode");
		String changeActionId = (String) requestMap.get("objectId");
		String followers = DomainObject.EMPTY_STRING;
		StringList finalFollowerList=new StringList();
		String functionality = (String) requestMap.get("functionality");
		if("edit".equalsIgnoreCase(strMode))
		isEditable = isTeamEditable(context, "AddFollower",changeActionId);
		
		
		// For export to CSV
		String exportFormat = null;
		boolean exportToExcel = false;
		if(requestMap!=null && requestMap.containsKey("reportFormat")){
			exportFormat = (String)requestMap.get("reportFormat");
		}
		if("CSV".equals(exportFormat)){
			exportToExcel = true;
		}
		
		if("AddToNewChangeAction".equals(functionality) || "AddToNewCA".equals(functionality)){
			changeActionId = null;
		}
		
		if(null != changeActionId){
		IChangeAction mChangeAction=ChangeAction.getChangeAction(context, changeActionId);
		String strCAOwner = mChangeAction.getOwner(context);
		String currentUser = context.getUser();
		List followerNameList=mChangeAction.GetFollowers(context);
		Iterator followersList=followerNameList.iterator();
		while(followersList.hasNext()){
			String followerName=(String)followersList.next();
			String followerId=PersonUtil.getPersonObjectID(context, followerName);
			followers=followers.concat(followerId+",");
			finalFollowerList.addElement(followerId);
		}
		}	
		
		
		if(followers.length()>0&&!followers.isEmpty()){
			followers = followers.substring(0,followers.length()-1);	
			}
		if(("edit".equalsIgnoreCase(strMode) && isEditable)|| "create".equalsIgnoreCase(strMode))
		{
			String addFollower= EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
					"EnterpriseChangeMgt.Command.AddFollower", context.getSession().getLanguage()); 
			String remove = EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
					"EnterpriseChangeMgt.Command.Remove", context.getSession().getLanguage()); 
			//XSSOK
			sb.append("<input type=\"hidden\" name=\"FollowerHidden\" id=\"FollowerHidden\" value=\""+followers+"\" readonly=\"readonly\" />");
			sb.append("<table>");
			sb.append("<tr>");
			sb.append("<th rowspan=\"2\">");
			sb.append("<select name=\"Follower\" style=\"width:200px\" multiple=\"multiple\">");

			if (finalFollowerList!=null && !finalFollowerList.isEmpty()){
				for (int i=0;i<finalFollowerList.size();i++) {
					String followersId = (String) finalFollowerList.get(i);
					if (followersId!=null && !followersId.isEmpty()) {
						String followerName = new DomainObject(followersId).getInfo(context, DomainConstants.SELECT_NAME);
						if (followerName!=null && !followerName.isEmpty()) {
							//XSSOK
							sb.append("<option value=\""+followersId+"\" >");
							//XSSOK
							sb.append(followerName);
							sb.append("</option>");

						}
					}
				}
			}

			sb.append("</select>");
			sb.append("</th>");
			sb.append("<td>");		
			sb.append("<a href=\"javascript:addPersonAsFollower()\">");
			sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
			sb.append("</a>");
			sb.append("<a href=\"javascript:addPersonAsFollower()\">");
			//XSSOK
			sb.append(addFollower);
			sb.append("</a>");
			//sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<a href=\"javascript:removeFollower()\">");
			sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
			sb.append("</a>");
			sb.append("<a href=\"javascript:removeFollower()\">");
			//XSSOK
			sb.append(remove);
			sb.append("</a>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
		}else
		{
			if(!exportToExcel)
			//XSSOK
			sb.append("<input type=\"hidden\" name=\"FollowerHidden\" id=\"FollowerHidden\" value=\""+followers+"\" readonly=\"readonly\" />");
			if (finalFollowerList!=null && !finalFollowerList.isEmpty()){
				for (int i=0;i<finalFollowerList.size();i++) {
					String  lastFollowerId=(String)finalFollowerList.get(finalFollowerList.size()-1);
					String followersId = (String) finalFollowerList.get(i);
					if (followersId!=null && !followersId.isEmpty()) {
						String followerName = new DomainObject(followersId).getInfo(context, DomainConstants.SELECT_NAME);
						if (followerName!=null && !followerName.isEmpty()) {
							if(!exportToExcel)
							//XSSOK
							sb.append("<input type=\"hidden\" name=\""+followerName+"\" value=\""+followersId+"\" />");
							//XSSOK
							sb.append(followerName);
							if(!lastFollowerId.equalsIgnoreCase(followersId))
								if(!exportToExcel)
								sb.append("<br>");							
								else
									sb.append("\n");
						}
					}
				}
			}
		}
		return sb.toString();
	}
	/**
  	 * Select the Contributor Field 
  	 * @param Context context
  	 * @param args holds information about object.
  	 * @return Contributor Field.
  	 * @throws Exception if operation fails.
  	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public String selectContributor(Context context,String[] args)throws Exception
	{
		boolean isEditable = false;
		StringBuilder sb = new StringBuilder();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String strMode = (String) requestMap.get("mode");
		String changeActionId = (String) requestMap.get("objectId");
		String contributors = DomainObject.EMPTY_STRING;
		StringList finalContributorList=new StringList();
		String functionality = (String) requestMap.get("functionality");
		if("edit".equalsIgnoreCase(strMode))
		isEditable = isTeamEditable(context,"AddContributor",changeActionId);
		
		
		// For export to CSV
		String exportFormat = null;
		boolean exportToExcel = false;
		if(requestMap!=null && requestMap.containsKey("reportFormat")){
			exportFormat = (String)requestMap.get("reportFormat");
		}
		if("CSV".equals(exportFormat)){
			exportToExcel = true;
		}
	
		
		
		if("AddToNewChangeAction".equals(functionality) || "AddToNewCA".equals(functionality)){
			changeActionId = null;
		}
		
		if(null != changeActionId){
		IChangeAction mChangeAction=ChangeAction.getChangeAction(context, changeActionId);
		String strCAOwner = mChangeAction.getOwner(context);
		String currentUser = context.getUser();
		
		List contributorNameList=mChangeAction.GetContributors(context);
		Iterator contributorsItr=contributorNameList.iterator();
		while(contributorsItr.hasNext()){
			String contributorName=(String)contributorsItr.next();
			String contributorId=PersonUtil.getPersonObjectID(context, contributorName);
			contributors=contributors.concat(contributorId+",");
			finalContributorList.addElement(contributorId);
		}
		}		
		
		if(contributors.length()>0&&!contributors.isEmpty()){
			contributors = contributors.substring(0,contributors.length()-1);	
			}
		if(("edit".equalsIgnoreCase(strMode) && isEditable)|| "create".equalsIgnoreCase(strMode))
		{
			String addContributor= EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
					"EnterpriseChangeMgt.Command.AddContributor", context.getSession().getLanguage());
			
			String remove = EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
					"EnterpriseChangeMgt.Command.Remove", context.getSession().getLanguage()); 
			//XSSOK
			sb.append("<input type=\"hidden\" name=\"ContributorHidden\" id=\"ContributorHidden\" value=\""+contributors+"\" readonly=\"readonly\" />");
			sb.append("<table>");
			sb.append("<tr>");
			sb.append("<th rowspan=\"2\">");
			sb.append("<select name=\"Contributor\" style=\"width:200px\" multiple=\"multiple\">");

			if (finalContributorList!=null && !finalContributorList.isEmpty()){
				for (int i=0;i<finalContributorList.size();i++) {
					String contributorsId = (String) finalContributorList.get(i);
					if (contributorsId!=null && !contributorsId.isEmpty()) {
						String contributorName = new DomainObject(contributorsId).getInfo(context, DomainConstants.SELECT_NAME);
						if (contributorName!=null && !contributorName.isEmpty()) {
							//XSSOK
							sb.append("<option value=\""+contributorsId+"\" >");
							//XSSOK
							sb.append(contributorName);
							sb.append("</option>");

						}
					}
				}
			}

			sb.append("</select>");
			sb.append("</th>");
			sb.append("<td>");		
			sb.append("<a href=\"javascript:addPersonAsContributor()\">");
			sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
			sb.append("</a>");
			sb.append("<a href=\"javascript:addPersonAsContributor()\">");
			//XSSOK
			sb.append(addContributor);
			sb.append("</a>");
			//sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<a href=\"javascript:removeContributor()\">");
			sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
			sb.append("</a>");
			sb.append("<a href=\"javascript:removeContributor()\">");
			//XSSOK
			sb.append(remove);
			sb.append("</a>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
		}else
		{
			if(!exportToExcel)
			//XSSOK
			sb.append("<input type=\"hidden\" name=\"ContributorHidden\" id=\"ContributorHidden\" value=\""+contributors+"\" readonly=\"readonly\" />");
			if (finalContributorList!=null && !finalContributorList.isEmpty()){
				for (int i=0;i<finalContributorList.size();i++) {
				String  lastContributorId=(String)finalContributorList.get(finalContributorList.size()-1);
					String contributorsId = (String) finalContributorList.get(i);
					if (contributorsId!=null && !contributorsId.isEmpty()) {
						String contributorName = new DomainObject(contributorsId).getInfo(context, DomainConstants.SELECT_NAME);
						if (contributorName!=null && !contributorName.isEmpty()) {
							if(!exportToExcel)
							//XSSOK
							sb.append("<input type=\"hidden\" name=\""+contributorName+"\" value=\""+contributorsId+"\" />");
							//XSSOK
							sb.append(contributorName);
							if(!lastContributorId.equalsIgnoreCase(contributorsId))
								if(!exportToExcel)
							sb.append("<br>");							
								else
									sb.append("\n");
						}
					}
				}
			}
		}
		return sb.toString();
	}
 	/**
  	 * Get Reviewers Field 
  	 * @param Context context
  	 * @param args holds information about object.
  	 * @return Reviewers Field.
  	 * @throws Exception if operation fails.
  	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public String selectReviewers(Context context,String[] args)throws Exception
	{
		boolean isEditable = false;
		StringBuilder sb = new StringBuilder();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String strMode = (String) requestMap.get("mode");
		String changeActionId = (String) requestMap.get("objectId");
		String styleDisplayPerson="block";
		String styleDisplayRouteTemplate="block";
		StringList finalReviewersList=new StringList();
		String relPattern =  new StringBuffer(ChangeConstants.RELATIONSHIP_CHANGE_REVIEWER).append(",").append(ChangeConstants.RELATIONSHIP_OBJECT_ROUTE).toString();
		String typePattern =  new StringBuffer(ChangeConstants.TYPE_PERSON).append(",").append(ChangeConstants.TYPE_ROUTE_TEMPLATE).toString();
		StringList objectSelects=new StringList(DomainObject.SELECT_ID);
		objectSelects.add(DomainObject.SELECT_TYPE);
		StringList personReviewersList=new StringList();
		StringList routeTemplateReviewersList=new StringList();
		String functionality = (String) requestMap.get("functionality");
		if("edit".equalsIgnoreCase(strMode))
		isEditable = isTeamEditable(context, "AddReviewer",changeActionId);
		
		
		// For export to CSV
		String exportFormat = null;
		boolean exportToExcel = false;
		if(requestMap!=null && requestMap.containsKey("reportFormat")){
			exportFormat = (String)requestMap.get("reportFormat");
		}
		if("CSV".equals(exportFormat)){
			exportToExcel = true;
		}
		
		
		if("AddToNewChangeAction".equals(functionality) || "AddToNewCA".equals(functionality)){
			changeActionId = null;
		}
		
		if(null != changeActionId){
			IChangeAction iCa=ChangeAction.getChangeAction(context, changeActionId);
			String strCAOwner = iCa.getOwner(context);
			String currentUser = context.getUser();
		DomainObject changeAction = new DomainObject(changeActionId);
		MapList mapList=changeAction.getRelatedObjects(context,
				  relPattern,
				  typePattern,
				  objectSelects,
				  new StringList(DomainRelationship.SELECT_ID),
				  false,
				  true,
				  (short) 2,
				  null, null, (short) 0);
		 
		if(!mapList.isEmpty()){
			Iterator iterator=mapList.iterator();
			while(iterator.hasNext()){
				Map dataMap=(Map)iterator.next();
				String objectType=(String)dataMap.get(DomainObject.SELECT_TYPE);
				String objectId=(String)dataMap.get(DomainObject.SELECT_ID);
				if(objectType.equalsIgnoreCase(ChangeConstants.TYPE_PERSON)){
					personReviewersList.add(objectId);
				}else if(objectType.equalsIgnoreCase(ChangeConstants.TYPE_ROUTE_TEMPLATE)){
					routeTemplateReviewersList.add(objectId);
				}					
			}
		}
		}
		
		
		String reviewers = DomainObject.EMPTY_STRING;
		String reviewerstype = DomainObject.EMPTY_STRING;
		if(!routeTemplateReviewersList.isEmpty() && personReviewersList.isEmpty())
		{
			 styleDisplayPerson="none";
		}
		else if(routeTemplateReviewersList.isEmpty() && !personReviewersList.isEmpty())
		{
			styleDisplayRouteTemplate="none";
		}
		else if(!routeTemplateReviewersList.isEmpty() && !personReviewersList.isEmpty()){
			styleDisplayPerson="none";
		}
		if (personReviewersList!=null && !personReviewersList.isEmpty() && routeTemplateReviewersList.isEmpty()){
			for (int i=0;i<personReviewersList.size();i++) {
				String reviewersId = (String) personReviewersList.get(i);
				String reviewerType = new DomainObject(reviewersId).getInfo(context, DomainConstants.SELECT_TYPE);
				reviewers=reviewers.concat(reviewersId+",");
				reviewerstype=reviewerstype.concat(reviewerType+",");
				finalReviewersList.addElement(reviewersId);
			}
		}
			if (routeTemplateReviewersList!=null && !routeTemplateReviewersList.isEmpty()){
				for (int i=0;i<routeTemplateReviewersList.size();i++) {
					String reviewersId = (String) routeTemplateReviewersList.get(i);
					String reviewerType = new DomainObject(reviewersId).getInfo(context, DomainConstants.SELECT_TYPE);
					reviewers=reviewers.concat(reviewersId+",");
					reviewerstype=reviewerstype.concat(reviewerType+",");
					finalReviewersList.addElement(reviewersId);
				}

		}
			
		if(reviewers.length()>0&&!reviewers.isEmpty()){
			reviewers = reviewers.substring(0,reviewers.length()-1);
			reviewerstype = reviewerstype.substring(0,reviewerstype.length()-1);
		}
		if(("edit".equalsIgnoreCase(strMode) && isEditable)|| "create".equalsIgnoreCase(strMode))
		{
			String addRouteTemplate= EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
					"EnterpriseChangeMgt.Command.AddRouteTemplate", context.getSession().getLanguage()); 
			String addPeople= EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
					"EnterpriseChangeMgt.Command.AddPeople", context.getSession().getLanguage()); 
			String remove = EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
					"EnterpriseChangeMgt.Command.Remove", context.getSession().getLanguage()); 
			//XSSOK
			sb.append("<input type=\"hidden\" name=\"ReviewersHidden\" id=\"ReviewersHidden\" value=\""+reviewers+"\" readonly=\"readonly\" />");
			//XSSOK
			sb.append("<input type=\"hidden\" name=\"ReviewersHiddenType\" id=\"ReviewersHiddenType\" value=\""+reviewerstype+"\" readonly=\"readonly\" />");
			sb.append("<table>");
			sb.append("<tr>");
			sb.append("<th rowspan=\"3\">");
			sb.append("<select name=\"Reviewers\" style=\"width:200px\" multiple=\"multiple\">");

			if (finalReviewersList!=null && !finalReviewersList.isEmpty()){
				for (int i=0;i<finalReviewersList.size();i++) {
					String reviewersId = (String) finalReviewersList.get(i);
					if (reviewersId!=null && !reviewersId.isEmpty()) {
						String reviewerName = new DomainObject(reviewersId).getInfo(context, DomainConstants.SELECT_NAME);
						if (reviewerName!=null && !reviewerName.isEmpty()) {

							sb.append("<option value=\""+reviewersId+"\" >");
							//XSSOK
							sb.append(reviewerName);
							sb.append("</option>");

						}
					}
				}
			}

			sb.append("</select>");
			sb.append("</th>");
			sb.append("<td>");
			sb.append("<div style=\"display:"+styleDisplayPerson+"\" name=\"ReviewrHidePerson\" id=\"ReviewrHidePerson\">");			
			sb.append("<a href=\"javascript:addReviewSelectors()\">");
			sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
			sb.append("</a>");
			sb.append("<a href=\"javascript:addReviewSelectors()\">");
			//XSSOK
			sb.append(addPeople);
			sb.append("</a>");
			sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<div style=\"display:"+styleDisplayRouteTemplate+"\" name=\"ReviewrHideRouteTemplate\" id=\"ReviewrHideRouteTemplate\">");
			sb.append("<a href=\"javascript:addRouteSelectors()\">");
			sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
			sb.append("</a>");
			sb.append("<a href=\"javascript:addRouteSelectors()\">");
			//XSSOK
			sb.append(addRouteTemplate);
			sb.append("</a>");
			sb.append("</div>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("<tr>");
			sb.append("<td>");
			sb.append("<a href=\"javascript:removeReviewers()\">");
			sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
			sb.append("</a>");
			sb.append("<a href=\"javascript:removeReviewers()\">");
			//XSSOK
			sb.append(remove);
			sb.append("</a>");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
		}else
		{
			if(!exportToExcel)
			//XSSOK
			sb.append("<input type=\"hidden\" name=\"ReviewersHidden\" id=\"ReviewersHidden\" value=\""+reviewers+"\" readonly=\"readonly\" />");
			if (finalReviewersList!=null && !finalReviewersList.isEmpty()){
				for (int i=0;i<finalReviewersList.size();i++) {
				String  lastReviewerId=(String)finalReviewersList.get(finalReviewersList.size()-1);
					String reviewersId = (String) finalReviewersList.get(i);
					if (reviewersId!=null && !reviewersId.isEmpty()) {
						String reviewerName = new DomainObject(reviewersId).getInfo(context, DomainConstants.SELECT_NAME);
						if (reviewerName!=null && !reviewerName.isEmpty()) {
							if(!exportToExcel)
							//XSSOK
							sb.append("<input type=\"hidden\" name=\""+reviewerName+"\" value=\""+reviewersId+"\" />");
							//XSSOK
							sb.append(reviewerName);
							if(!lastReviewerId.equalsIgnoreCase(reviewersId))
								if(!exportToExcel)
							sb.append("<br>");							
								else
									sb.append("\n");
						}
					}
				}
			}
		}
		return sb.toString();
	}
	/**
	 * connectReviewers - Connect Change Action and Person or Route Template 
	 *if Route Template Connect then People Associate with Route Template also connect to CA 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public void connectReviewers(Context context, String[] args)throws Exception
	{
		try{
			boolean currentIsOwner = false;
			Map programMap = (HashMap)JPO.unpackArgs(args);
			HashMap hmParamMap = (HashMap)programMap.get("paramMap");
			String strChangeObjId = (String)hmParamMap.get("objectId");
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			IChangeAction iChangeAction=ChangeAction.getChangeAction(context, strChangeObjId);
			String[] strNewReviwersArr = (String[])requestMap.get("ReviewersHidden");
			String strNewReviwers=null;
			 
			String strCurrentUser = context.getUser();
			String strCAOwner = iChangeAction.getOwner(context);
			currentIsOwner = strCurrentUser.equalsIgnoreCase(strCAOwner)?true:false;
			//This code is for to skip connect operation while performing with Assignee of CA.
			if(!currentIsOwner)
			{
				return;
			}
			
			String relRouteObjet=PropertyUtil.getSchemaProperty(context, "relationship_ObjectRoute");
			if(strNewReviwersArr != null && strNewReviwersArr.length > 0){
				strNewReviwers = strNewReviwersArr[0];				
			}
			DomainObject changeAction = new DomainObject(strChangeObjId);
			StringList listRouteTemplaterelId=new StringList();
			MapList mapList=changeAction.getRelatedObjects(context,
					  DomainConstants.RELATIONSHIP_OBJECT_ROUTE,
					  DomainConstants.TYPE_ROUTE_TEMPLATE,
					  new StringList(DomainObject.SELECT_ID),
					  new StringList(DomainRelationship.SELECT_ID),
					  false,
					  true,
					  (short) 2,
					  null, null, (short) 0);
			if(!mapList.isEmpty()){
				Iterator itr=mapList.iterator();
				while(itr.hasNext()){
					Map dataMap=(Map)itr.next();
					String routeTemplateRelId=(String) dataMap.get(DomainRelationship.SELECT_ID);
					listRouteTemplaterelId.add(routeTemplateRelId);
				}	
			}
			List reviewerPersonlistOld=iChangeAction.GetReviewers(context);
			Iterator reviewersPersonListOld=reviewerPersonlistOld.iterator();
			while(reviewersPersonListOld.hasNext()){
				String reviewerNameOld=(String) reviewersPersonListOld.next();
				//BusinessObject objReviewer=PersonUtil.getPersonObject(context,reviewerNameOld);
				iChangeAction.RemovePersonFromReviewers(context, reviewerNameOld);
			}
			if(!listRouteTemplaterelId.isEmpty()){
				if(strNewReviwers.isEmpty()||!checkRouteTemplate(context,strNewReviwers)){
					String strOldReviwerRoute=	(String) listRouteTemplaterelId.get(0);
					DomainRelationship.disconnect(context, strOldReviwerRoute);
				}
			}
			if(!strNewReviwers.isEmpty()){
				if(!checkRouteTemplate(context,strNewReviwers)){
					StringTokenizer strNewReviwersList = new StringTokenizer(strNewReviwers,",");
					while (strNewReviwersList.hasMoreTokens()){
						String strReviwer = strNewReviwersList.nextToken().trim();
						strReviwer = DomainObject.newInstance(context, strReviwer).getInfo(context, DomainConstants.SELECT_NAME);
						//BusinessObject objReviwer=new BusinessObject(strReviwer);
						iChangeAction.AddPersonAsReviewer(context, strReviwer);					
					}
				}else
				{
					DomainObject objReviwer=new DomainObject(strNewReviwers);
					if(!listRouteTemplaterelId.isEmpty()&&!(listRouteTemplaterelId==null)){
						String strOldReviwerRoute=	(String) listRouteTemplaterelId.get(0);
						DomainRelationship.setToObject(context, strOldReviwerRoute, objReviwer);
					}
					else{
						DomainRelationship.connect(context,strChangeObjId,relRouteObjet,strNewReviwers,true);
					}
					connectRouteTemplatePersonToCA(context,strChangeObjId,strNewReviwers);		
				}		
			}		}
		catch(Exception e){
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * connectRouteTemplatePersonToCA - Connect Change Action and Person Associate with Route Template with Change Reviewer relationship
	 * @param context
	 * @param changeActionId
	 * @param routeTemplateNewId
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public static void connectRouteTemplatePersonToCA (Context context,String changeActionId,String routeTemplateNewId)throws Exception{
		try {
			String  strRouteTemplateId=routeTemplateNewId;
			String  strChangeObjId=changeActionId;
			IChangeAction iChangeAction=ChangeAction.getChangeAction(context, strChangeObjId);
			DomainObject routeTemplate=new DomainObject(strRouteTemplateId);
			StringList stringListPerson=new StringList();
			MapList mapList=routeTemplate.getRelatedObjects(context,
					  DomainConstants.RELATIONSHIP_ROUTE_NODE,
					  DomainConstants.TYPE_PERSON,
					  new StringList(DomainObject.SELECT_ID),
					  new StringList(DomainRelationship.SELECT_ID),
					  false,
					  true,
					  (short) 2,
					  null, null, (short) 0);
			if(!mapList.isEmpty()){
				Iterator itr=mapList.iterator();
				while(itr.hasNext()){
					Map dataMap=(Map)itr.next();
					String personId=(String) dataMap.get(DomainObject.SELECT_ID);
					stringListPerson.add(personId);
				}			
			}
			Iterator strOldReviewersrelId = stringListPerson.iterator();
			while (strOldReviewersrelId.hasNext()){
				String strReviwer = (String) strOldReviewersrelId.next();
				strReviwer = DomainObject.newInstance(context, strReviwer).getInfo(context, DomainConstants.SELECT_NAME);
				//BusinessObject objReviwer=new BusinessObject(strReviwer);
				iChangeAction.AddPersonAsReviewer(context, strReviwer);
			}			
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
	}
	/**
	 * checkRouteTemplate - Check whether given string contain Route Template type or Not
	 * @param Context  context
	 * @param String reviewers
	 * @return boolean -true/false
	 * @throws Exception
	 */
	public static boolean checkRouteTemplate(Context context,String reviewers)throws Exception{

		try {
			StringTokenizer strNewReviewersList = new StringTokenizer(reviewers,",");
			while (strNewReviewersList.hasMoreTokens())
			{
				String strReviewer = strNewReviewersList.nextToken().trim();
				DomainObject domainObj=new DomainObject(strReviewer);			
				String objType=domainObj.getInfo(context,SELECT_TYPE);
				if(objType.equalsIgnoreCase(DomainConstants.TYPE_ROUTE_TEMPLATE)){
					return true;
				}
			}			
		}
		catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return false;		
	}

	/**
	 * connectSeniorTechAssignee - Connect new Senior Tech Assignee -Update Program
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public void connectSeniorTechAssignee(Context context, String[] args)throws Exception
	{
		try
		{
			Map programMap = (HashMap)JPO.unpackArgs(args);
			HashMap hmParamMap = (HashMap)programMap.get("paramMap");
			String strChangeObjId = (String)hmParamMap.get("objectId");
			String strNewSeniorTechAssig = (String)hmParamMap.get("New OID");
			String relSeniorTechAssignee = PropertyUtil.getSchemaProperty(context, "relationship_SeniorTechnicalAssignee");
			this.setId(strChangeObjId);
			String strSrTechAssigneeRelId = getInfo(context, "from["+ relSeniorTechAssignee +"].id");

			if(!ChangeUtil.isNullOrEmpty(strSrTechAssigneeRelId))
			{
				DomainRelationship.disconnect(context, strSrTechAssigneeRelId);
			}
			if(!ChangeUtil.isNullOrEmpty(strNewSeniorTechAssig)){
				DomainRelationship.connect(context, new DomainObject(strChangeObjId), new RelationshipType(relSeniorTechAssignee), new DomainObject(strNewSeniorTechAssig));
			}
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}

	/**
	 * This is a check trigger method on (Pending --> InWork) to validate Estimated Completion Date and Technical Assignee on the Change Action
	 * @param context
	 * @param args Change Action Id and Notice
	 * @return integer (0 = pass, 1= block with notice)
	 * @throws Exception
	 */
	public int validateCompletionDateAndTechAssignee(Context context, String args[])throws Exception
	{
		int iReturn = 0;
		try
		{
			if (args == null || args.length < 1)
			{
				throw (new IllegalArgumentException());
			}
			String strChangeId = args[0];
			this.setId(strChangeId);

			// Getting the Tecchnical Assignee connected
			String strTechAssigneeId = getInfo(context, "from["+ ChangeConstants.RELATIONSHIP_TECHNICAL_ASSIGNEE +"].to.id");
			String strEstCompletionDateNotice = EnoviaResourceBundle.getProperty(context, args[3], context.getLocale(),args[1]);
			String strTechAssigneeNotice = EnoviaResourceBundle.getProperty(context, args[3], context.getLocale(),args[2]);
			// Getting the Estimated Completion Date Value
			String strCompletionDate = (String) getAttributeValue(context, ChangeConstants.ATTRIBUTE_ESTIMATED_COMPLETION_DATE);

			// Validating If both are not empty, if so accordingly sending the notice.
			if (ChangeUtil.isNullOrEmpty(strCompletionDate))
			{
				emxContextUtilBase_mxJPO.mqlNotice(context, strEstCompletionDateNotice);
				iReturn = 1;
			}

			if(ChangeUtil.isNullOrEmpty(strTechAssigneeId))
			{
				emxContextUtilBase_mxJPO.mqlNotice(context, strTechAssigneeNotice);
				iReturn = 1;
			}
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw new FrameworkException(Ex.getMessage());
		}
		return iReturn;
	}

	/**
	 * Subsidiary method to add the new String to the StringBuffer
	 * @param context
	 * @param sbOutput - StringBuffer Output
	 * @param message - String need to be added
	 * @return String Buffer
	 * @throws Exception
	 */
	private StringBuffer addToStringBuffer(Context context, StringBuffer sbOutput, String message)throws Exception
	{
		try
		{
			if(sbOutput != null && sbOutput.length() != 0)
			{
				sbOutput.append(", ");
				sbOutput.append(message);
			}
			else
			{
				sbOutput.append(message);
			}
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
		return sbOutput;
	}

	/**
	 * Check Trigger on (Pending --> InWork) to check whether Prerequisites Parent CA (Hard Dependency) are all in Complete State,
	 * Hard Dependency - Parent Change Action Id will  be having attribute value "Type of Dependency" as "Hard".
	 * @param context
	 * @param args - Change Action Id
	 * @return (0 = pass, 1= block with notice)
	 * @throws Exception
	 */
	public int checkForDependency(Context context, String args[])throws Exception
	{
		int iReturn = 0;
		try
		{
			if (args == null || args.length < 1)
			{
				throw (new IllegalArgumentException());
			}

			String strCAName = "";
			Map tempMap = null;
			String strChangeId = args[0];
			this.setId(strChangeId);
			StringBuffer sBusWhere = new StringBuffer();
			StringBuffer sRelWhere = new StringBuffer();
			StringBuffer sbMessage = new StringBuffer();

			String strNotice = EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Notice.HardDependency");

			sBusWhere.append("("+SELECT_CURRENT + " != \"");
			sBusWhere.append(ChangeConstants.STATE_CHANGE_ACTION_COMPLETE);
			sBusWhere.append("\" && ");
			sBusWhere.append(SELECT_CURRENT + " != \"");
			sBusWhere.append(ChangeConstants.STATE_CHANGE_ACTION_CANCEL);
			sBusWhere.append("\" && ");
			sBusWhere.append(SELECT_CURRENT + " != \"");
			sBusWhere.append(ChangeConstants.STATE_CHANGE_ACTION_HOLD);
			sBusWhere.append("\")");
			sRelWhere.append("attribute[");
			sRelWhere.append(ChangeConstants.ATTRIBUTE_PREREQUISITE_TYPE);
			sRelWhere.append("] == ");
			sRelWhere.append(ATTR_VALUE_MANDATORY);

			// Get all the Prerequisites which are not in complete state and that are Hard Dependency.
			MapList mlPrerequisites = new ChangeAction(strChangeId).getPrerequisites(context,ChangeConstants.TYPE_CHANGE_ACTION, sBusWhere.toString(),sRelWhere.toString());

			if(mlPrerequisites != null && !mlPrerequisites.isEmpty())
			{
				for (Object var : mlPrerequisites)
				{
					tempMap = (Map) var;
					strCAName = (String) tempMap.get(SELECT_NAME);
					sbMessage = addToStringBuffer(context, sbMessage, strCAName);
				}
			}

			// If Message is not empty, send the notice with Change Action which are not completed.
			if(sbMessage.length() != 0)
			{
				emxContextUtilBase_mxJPO.mqlNotice(context, strNotice + "  "+sbMessage.toString());
				iReturn = 1;
			}
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw new FrameworkException(Ex.getMessage());
		}
		return iReturn;
	}

	/**
	 * Check Trigger on (Pending --> InWork) to check whether parent CO is in In Work state,
	 * @param context
	 * @param args - Change Action Id
	 * @return (0 = pass, 1= block with notice)
	 * @throws Exception
	 */
	public int checkCOState(Context context, String args[]) throws Exception {
		String strFunc = null;
		String strNotice = null;		

		String strChangeId = args[0];
		String nextState = args[9]; // for custom change on cancel the check trigger will be fired on promting to cancel state.
		String type = args[8]; // for custom change on cancel the check trigger will be fired on promting to cancel state.
		
		if (ChangeConstants.TYPE_CCA.equals(type)) {
			String policyConfiguredPart = PropertyUtil.getSchemaProperty(context, "policy_PUEECO"); 
			String stateCancelled = PropertyUtil.getSchemaProperty(context, "policy", policyConfiguredPart, "state_Cancelled");
			
			if (stateCancelled.equals(nextState)) {
				return 0;
			}
		}
		if (UIUtil.isNotNullAndNotEmpty(strChangeId)) {
			this.setId(strChangeId);
			
			String coObjIdSelect = "to[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "].from[" + ChangeConstants.TYPE_CHANGE_ORDER + "].id";
			String coPolicySelect = "to[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "].from[" + ChangeConstants.TYPE_CHANGE_ORDER + "].policy";
			String coCurrentState = "to[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "].from[" + ChangeConstants.TYPE_CHANGE_ORDER + "].current";
			StringList select = new StringList();
			select.add(coObjIdSelect);
			select.add(coPolicySelect);
			select.add(coCurrentState);
		
			Map resultList = getInfo(context, select);
			String coObjId = (String) resultList.get(coObjIdSelect);
			String copolicy = (String) resultList.get(coPolicySelect);
			
			if (UIUtil.isNullOrEmpty(coObjId)) {
				String crObjectId = getInfo(context, "to[" + ChangeConstants.RELATIONSHIP_CHANGE_ACTION + "].from[" + ChangeConstants.TYPE_CHANGE_REQUEST + "].id");
	
				if (UIUtil.isNotNullAndNotEmpty(crObjectId)) {
					strNotice = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(), "EnterpriseChangeMgt.Notice.COIsNotConnectedToCA");
				}
			} else {
				strFunc = context.getCustomData("massFunctionality");
				
				if (ChangeConstants.POLICY_FASTTRACK_CHANGE.equals(copolicy)) {
					if (ChangeConstants.TYPE_CCA.equals(type)) {
						String coState = (String) resultList.get(coCurrentState);
						String STATE_CO_ON_HOLD = PropertyUtil.getSchemaProperty(context, "policy", ChangeConstants.POLICY_FORMAL_CHANGE, "state_OnHold");
						if (coState.equals(STATE_CO_ON_HOLD)) {
							strNotice = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(), "EnterpriseChangeMgt.Notice.ConnectedCOInOnHoldState");
						}
					}
				} else {
					String STATE_CO_INWORK = PropertyUtil.getSchemaProperty(context, "policy", ChangeConstants.POLICY_FORMAL_CHANGE, "state_InWork");
					if ((new ChangeUtil().checkObjState(context, coObjId, STATE_CO_INWORK, ChangeConstants.NE) == 0)&&!(ChangeConstants.FOR_RELEASE.equals(strFunc)||ChangeConstants.FOR_OBSOLETE.equals(strFunc))) {
						strNotice = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(), "EnterpriseChangeMgt.Notice.ConnectedCONotInInWorkState");
					}
				}
			}
		}
		
		if (strNotice != null) {
			emxContextUtilBase_mxJPO.mqlNotice(context, strNotice);
		}
		
		if (strFunc != null) {
			context.clearCustomData();
		}
		
		return (strNotice == null) ? 0 : 1;
	}

	/**
	 * The Action trigger  method on (Pending --> In Work)  to Revise and Connect object to implemented items of CA.
	 * This can be used for generic purpose.
	 * @param context
	 * @param args
	 * @return void
	 * @throws Exception
	 */

	public int reviseAndConnectToImplementedItems(Context context, String[] args) throws Exception
	{
		try
		{
			if (args == null || args.length < 1)
			{
				throw (new IllegalArgumentException());
			}
			String strId = "";
			String strType = "";
			String strName = "";
			String strCurrent = "";
			String strRevision = "";
			String strAttrRC = "";
			String strWhere = "";
			String strPolicy = "";
			String strLatestRevCurrent = "";
			String strLatestRevision = "";
			String strLatestRevisionId = "";
			String strObjectStateName = "";
			RelationshipType relType = null;
			Map tempMap = null;
			Map mapTemp = null;
			boolean bSpec = true;
			String nonCDMTypes = EnoviaResourceBundle.getProperty(context, "EnterpriseChangeMgt.Integration.NonCDMTypes");
			StringList typeList = new StringList();
			String typeSpec = "";
			String LastestRevId = "";
			
			HashMap latestRevisionMap=new HashMap();
			String strObjId = args[0];
			DomainObject dObj = new DomainObject(strObjId);

			ChangeUtil changeUtil = new ChangeUtil();
			StringList slNameList = new StringList();
			StringList slBusSelect = new StringList(4);
			slBusSelect.addElement(SELECT_ID);
			slBusSelect.addElement(SELECT_TYPE);
			slBusSelect.addElement(SELECT_NAME);
			slBusSelect.addElement(SELECT_CURRENT);
			slBusSelect.addElement(SELECT_REVISION);
			slBusSelect.addElement(SELECT_POLICY);

			ChangeAction changeAction = new ChangeAction(strObjId);

			StringList slRelSelect = new StringList();
			String strRequestedChange = ChangeConstants.SELECT_ATTRIBUTE_REQUESTED_CHANGE;
			slRelSelect.addElement(strRequestedChange);

			// Getting all the connected objects of context object
			MapList mlRelatedObjects = dObj.getRelatedObjects(context, ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM, "*", slBusSelect, slRelSelect, false, true, (short) 1, "", "", 0);

			Iterator i = mlRelatedObjects.iterator();
			
			DomainObject domObj = new DomainObject();

			// Iterating all the Affected Items Objects
			while (i.hasNext()) {
				mapTemp = (Map) i.next();
				// Fetching all the values
				strId = (String) mapTemp.get(SELECT_ID);
				strType = (String) mapTemp.get(SELECT_TYPE);
				strName = (String) mapTemp.get(SELECT_NAME);
				strCurrent = (String) mapTemp.get(SELECT_CURRENT);
				strRevision = (String) mapTemp.get(SELECT_REVISION);
				strAttrRC = (String) mapTemp.get(ChangeConstants.SELECT_ATTRIBUTE_REQUESTED_CHANGE);
				strPolicy = (String) mapTemp.get(SELECT_POLICY);

				strObjectStateName = ECMAdmin.getReleaseStateValue(context, strType, strPolicy);

				// Checking if the object is "For Revise" and state is Release
				if (strAttrRC.equalsIgnoreCase(ChangeConstants.FOR_REVISE) && strCurrent.equalsIgnoreCase(strObjectStateName)) {
					strWhere = "name == '"+ strName +"' && revision == last";
					setId(strId);

					// Considering only one Latest Revision with that Name
					LastestRevId = getInfo(context, "last.id");

					setId(LastestRevId);

					tempMap = getInfo(context, slBusSelect);
					// Checking for the current state and revision of the object
					strLatestRevCurrent = (String) tempMap.get(SELECT_CURRENT);
					strLatestRevision = (String) tempMap.get(SELECT_REVISION);

					// Check if latest revision exists and which is not released in the system
				   if (!strRevision.equalsIgnoreCase(strLatestRevision) && changeUtil.checkObjState(context, LastestRevId, strObjectStateName, ChangeConstants.LT) == 0) {
						// Getting the latest revision of object, if already connected in Implemented Items
						MapList mlImplementedItems = dObj.getRelatedObjects(context,
								ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM,
								"*",
								slBusSelect,
								null,
								false,
								true,
								(short) 1,
								strWhere,
								EMPTY_STRING,
								0);

						if (mlImplementedItems.size() == 0) {
							// Connecting to Change Action object if latest revision is not connected as Implemented Item
							changeAction.connectImplementedItems(context, new StringList(LastestRevId));
						}
					} else {
						domObj.setId(strId);
						
						if(UIUtil.isNotNullAndNotEmpty(nonCDMTypes))
							typeList = FrameworkUtil.split(nonCDMTypes, ",");						
						//Modified for IR-264331 start
						if (domObj.isKindOf(context,CommonDocument.TYPE_DOCUMENTS)){
							Iterator itr = typeList.iterator();
	                        while(itr.hasNext()){
	                            typeSpec = (String)itr.next();
	                            typeSpec = PropertyUtil.getSchemaProperty(context,typeSpec);
	                            if(domObj.isKindOf(context, typeSpec)){
	                                bSpec = false;
	                                break;
	                            }
	                        }
                        } 
						
						if(domObj.isKindOf(context,CommonDocument.TYPE_DOCUMENTS) && bSpec){
                        	CommonDocument docItem = new CommonDocument(domObj);
                            docItem.revise(context, true);
                        } else {
                        	domObj.reviseObject(context, false);
                        }
						
						//Modified for IR-264331 end
						
						// Selecting the latest revision business id
						strLatestRevisionId = (String) domObj.getInfo(context, "last.id");
						relType = new RelationshipType(ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM);
						// Adding the above latest revision object to Implemented Item of CA
						changeAction.connectImplementedItems(context, new StringList(strLatestRevisionId));
						latestRevisionMap.put(strId, strLatestRevisionId);
					}
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}

		return 0;
	}

	/**
	 * The Action trigger  method on (Pending --> In Work) to set current date as the Actual Start Date of Change Action
	 * @param context
	 * @param args (Change Action Id)
	 * @throws Exception
	 */
	public void setActualStartDate(Context context, String[] args)throws Exception
	{
		try
		{
			if (args == null || args.length < 1)
			{
				throw (new IllegalArgumentException());
			}
			String strObjId = args[0];
			this.setId(strObjId);
			SimpleDateFormat _mxDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
			String strActualStartDate = _mxDateFormat.format(new Date());

			// Setting the Current Date to the Actual Start Date.
			setAttributeValue(context, ATTRIBUTE_ACTUAL_START_DATE, strActualStartDate);
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}

	/**
	 * Check Trigger on (Pending --> In Work) to check whether the context change Action's implemented items are not connected
	 * to the other Change Actions which are in "In Work" State.
	 * @param context
	 * @param args (Change Action Id and Notice)
	 * @return (0 = pass, 1= block with notice)
	 * @throws Exception
	 */
	public int validateImplementedItems(Context context, String[] args)throws Exception
	{
		int iReturn = 0;
		try
		{
			if (args == null || args.length < 1)
			{
				throw (new IllegalArgumentException());
			}
			Map tempMap = null;
			String strImplementedObjId = "";
			String strObjId = args[0];
			String strMessage = "";

			String strNotice = EnoviaResourceBundle.getProperty(context, args[2], context.getLocale(),args[1]);

			// Get All Implemented items from the Chnage Action
			MapList mlImplementedItems = new ChangeAction(strObjId).getImplementedItems(context);

			for (Object var : mlImplementedItems)
			{
				tempMap = (Map) var;
				strImplementedObjId = (String) tempMap.get(SELECT_ID);
				// Get Change Action which are in In Work State
				strMessage = getChangeAction(context, strImplementedObjId, strObjId);
			}

			// If Message Is not empty, send send a notice and block the Promotion.
			if(strMessage != null && !strMessage.isEmpty())
			{
				emxContextUtilBase_mxJPO.mqlNotice(context, strNotice + strMessage);
				iReturn = 1;
			}
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
		return iReturn;
	}

	/**
	 * Subsidiary method to get the change action connected to the Item, which are "In Work" state.
	 * @param context
	 * @param strImplementedObjId
	 * @param strObjId
	 * @return String name of Change Actions which are in InWork State
	 * @throws Exception
	 */
	public String getChangeAction(Context context, String strImplementedObjId, String strObjId)throws Exception
	{
		StringBuffer sbOutput = new StringBuffer();
		try
		{
			String strChangeActionName = "";
			String strChangeActionId = "";
			Map tempMap = null;
			String STATE_CA_IN_WORK = PropertyUtil.getSchemaProperty(context, "policy", ChangeConstants.POLICY_CHANGE_ACTION, "state_InWork");
			this.setId(strImplementedObjId);
			StringList slObjectSelects = new StringList();
			slObjectSelects.add(SELECT_NAME);
			slObjectSelects.add(SELECT_ID);
			slObjectSelects.add(SELECT_CURRENT);

			String strWhere = SELECT_CURRENT + " == \"" + STATE_CA_IN_WORK +"\"";

			MapList mlChangeActionList = getRelatedObjects(context, ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM, // relationship pattern
					ChangeConstants.TYPE_CHANGE_ACTION, // object pattern
					slObjectSelects, // object selects
					new StringList(DomainRelationship.SELECT_ID), // relationship selects
					true, // to direction
					false, // from direction
					(short) 1, // recursion level
					strWhere, // object where clause
					"",
					0); // relationship where clause

			for (Object var : mlChangeActionList)
			{
				tempMap = (Map) var;
				strChangeActionName = (String) tempMap.get(SELECT_NAME);
				strChangeActionId = (String) tempMap.get(SELECT_ID);

				if (strChangeActionId != null && !strChangeActionId.equals(strObjId))
				{
					sbOutput = addToStringBuffer(context, sbOutput, strChangeActionName);
				}
			}
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
		return sbOutput.toString();
	}

	/**
	 * Check Trigger on (In Work -->> In Approval) to check whether the Route Template or the Senior technical Assignee is connected to Change Action.
	 * @param context
	 * @param args (Change Action ID and Notice)
	 * @return (0 = pass, 1 = block the promotion)
	 * @throws Exception
	 */
	public int checkForSrTechnicalAssigneeAndRouteTemplate(Context context, String[] args)throws Exception
	{
		int iReturn = 0;
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		String objectId = args[0];// Change Object Id
		String strReviewerRouteTemplate = args[1];
		MapList mapRouteTemplate = new MapList();

		try
		{
			// create change object with the context Object Id
			setId(objectId);
			StringList selectStmts = new StringList(1);
			selectStmts.addElement("attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]");

			String whrClause = "attribute["
				+ ATTRIBUTE_ROUTE_BASE_PURPOSE + "] match '"
				+ strReviewerRouteTemplate + "' && current == Active";

			// get route template objects from change object
			mapRouteTemplate = getRelatedObjects(context,
					RELATIONSHIP_OBJECT_ROUTE, TYPE_ROUTE_TEMPLATE,
					selectStmts, null, false, true, (short) 1, whrClause, null, 0);

			// get the Senior Technical Assignee connected
			String strResponsibleOrgRelId = getInfo(context, "from["+ ChangeConstants.RELATIONSHIP_SENIOR_TECHNICAL_ASSIGNEE +"].id");

			// Send notice and block promotion if both are not connected.
			if ((mapRouteTemplate == null || mapRouteTemplate.isEmpty()) && ChangeUtil.isNullOrEmpty(strResponsibleOrgRelId))
			{
				emxContextUtil_mxJPO.mqlNotice(context,EnoviaResourceBundle.getProperty(context, args[3], context.getLocale(),args[2]));
				iReturn = 1;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return iReturn;
	}

	/**
	 * Action trigger on (InWork--> In Approval) to Promote all the Implemented/Affected  Items connected to the Change Action to Approved State.
	 * @param context
	 * @param args (Change Action Id)
	 * @throws Exception
	 */
	public void promoteItemsToApproved(Context context, String[] args) throws Exception
	{
		try
		{
			String strItem = "";
			String strItemType = "";
			String strItemPolicy= "";
			String strChangeActionId = args[0];
			String strCurrentState   = args[1];
			String strTargetState    = args[2];
			setId(strChangeActionId);
			String STATE_CA_INAPPROVAL = PropertyUtil.getSchemaProperty(context, "policy", ChangeConstants.POLICY_CHANGE_ACTION, "state_InApproval");
			StringList objSelects = new StringList(SELECT_ID);
			objSelects.addElement(SELECT_TYPE);
			objSelects.addElement(SELECT_POLICY);

			MapList ImplementedItems = null;
			Map<String,String> implementedItemMap;
			String relWhereClause = "attribute["+ATTRIBUTE_REQUESTED_CHANGE+"] == '"+ChangeConstants.FOR_RELEASE+"'";
			String stateApprovedMapping = "";
			boolean strAutoApproveValue = false;

			//if(!ChangeUtil.isNullOrEmpty(strTargetState) && STATE_CA_INAPPROVAL.equalsIgnoreCase(strTargetState))
			if(!ChangeUtil.isNullOrEmpty(strTargetState) && "state_InApproval".equalsIgnoreCase(strTargetState))
			{
				// Get the Implemented Items connected.
				ImplementedItems = getRelatedObjects(context,
													 ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+","+ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM, // relationship pattern
													  "*" , 		  								// object pattern
													  objSelects, 					// object selects
													  new StringList(DomainRelationship.SELECT_ID), // relationship selects
													  false, 										// to direction
													  true, 										// from direction
													  (short) 1, 									// recursion level
													  EMPTY_STRING, 								// object where clause
													  relWhereClause,
													  (short)0); 									// relationship where clause

				Map relItemTypPolicyDtls = new HashMap();
				// Set the Approved State on the Implemented Items
				for (Object var : ImplementedItems)
				{
					implementedItemMap = (Map<String,String>) var;
					strItem       = implementedItemMap.get(SELECT_ID);
					strItemType   = implementedItemMap.get(SELECT_TYPE);
					strItemPolicy = implementedItemMap.get(SELECT_POLICY);
					stateApprovedMapping = ECMAdmin.getApproveStateValue(context, strItemType, strItemPolicy);
					strAutoApproveValue  = ECMAdmin.getAutoApprovalValue(context, strItemType, strItemPolicy);
					if(strAutoApproveValue && !ChangeUtil.isNullOrEmpty(stateApprovedMapping)) {
						relItemTypPolicyDtls.put(strItem, strItemPolicy + "|" + strItemType);
					}
				}

				if(!relItemTypPolicyDtls.isEmpty())
					ECMAdmin.enforceApproveOrder(context, relItemTypPolicyDtls);
			}
		} catch(Exception Ex) {
			Ex.printStackTrace();
			throw new FrameworkException(Ex.getMessage());
		}
	}

	/**
	 * Action Trigger on (InApproval-- > Approved) to Set the current date as the Actual Completion Date
	 * @param context
	 * @param args (Cahnge Action Id)
	 * @throws Exception
	 */
	public void setActualCompletionDate(Context context, String[] args)throws Exception
	{
		try
		{
			if (args == null || args.length < 1)
			{
				throw (new IllegalArgumentException());
			}
			String strObjId = args[0];
			this.setId(strObjId);
			SimpleDateFormat _mxDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
			String strActualCompletionDate = _mxDateFormat.format(new Date());
			// Set the Actual Completion Date
			setAttributeValue(context, ATTRIBUTE_ACTUAL_COMPLETION_DATE, strActualCompletionDate);
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}

	/**
	 * Action trigger on (Approved --> Complete) to Promote the Implemented Items as per the Requested Change attribute on the relationship.
	 * @param context
	 * @param args (Change Action Id)
	 * @throws Exception
	 */
	public void promoteImplementedItemsAsRequestedChange(Context context, String[] args) throws Exception
	{
		try
		{
			Map tempMap = null;
			String strRequestedChange = "";
			String strItem = "";
			String strItemPolicy = "";
			String strItemType ="";
			String targetStateName = "";
			String strRelType = "";
			String strChangeActionId = args[0];
			setId(strChangeActionId);
			StringList busSelects = new StringList(SELECT_ID);
			busSelects.add(SELECT_POLICY);
			busSelects.add(SELECT_TYPE);
			StringList relSelects = new StringList(SELECT_RELATIONSHIP_ID);
			relSelects.add(ChangeConstants.SELECT_ATTRIBUTE_REQUESTED_CHANGE);
			relSelects.add(SELECT_RELATIONSHIP_TYPE);
			StringList obsoleteItems = new StringList();
			StringList releasedItems = new StringList();
			StringList updatedItems  = new StringList();

			// Get the implemented items & Affected Items connected.
			MapList listItems = getRelatedObjects(context,
														ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM+","+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM,
														"*",
														busSelects, relSelects,
														false, true,(short) 1,
														EMPTY_STRING, EMPTY_STRING,
														(short)0);


			Set allReleaseItems = new HashSet();
			Map relItemTypPolicyDtls = new HashMap();

			// promote Implemented items as per the Requested change attribute
			for (Object var : listItems)
			{
				targetStateName = null;
				tempMap = (Map) var;
				strRequestedChange = (String) tempMap.get(ChangeConstants.SELECT_ATTRIBUTE_REQUESTED_CHANGE);
				strItem 	  = (String)tempMap.get(SELECT_ID);
				strItemPolicy = (String)tempMap.get(SELECT_POLICY);
				strItemType   = (String)tempMap.get(SELECT_TYPE);
				strRelType    = (String)tempMap.get(SELECT_RELATIONSHIP_TYPE);

				if(ChangeConstants.FOR_OBSOLESCENCE.equalsIgnoreCase(strRequestedChange)) {
					targetStateName = ECMAdmin.getObsoleteStateValue(context, strItemType, strItemPolicy);

					//Obsoleting items
					setId(strItem);
					setState(context, targetStateName);

					if(ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM.equalsIgnoreCase(strRelType))
						obsoleteItems.addElement(strItem);
				}

				if(ChangeConstants.FOR_RELEASE.equalsIgnoreCase(strRequestedChange)) {
					allReleaseItems.add(strItem);
					relItemTypPolicyDtls.put(strItem, strItemPolicy + "|" + strItemType);

					targetStateName = ECMAdmin.getReleaseStateValue(context, strItemType, strItemPolicy);
					if(ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM.equalsIgnoreCase(strRelType))
						releasedItems.addElement(strItem);
				}

				if(ChangeConstants.FOR_UPDATE.equalsIgnoreCase(strRequestedChange) && ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM.equalsIgnoreCase(strRelType)) {
					updatedItems.addElement(strItem);
				}

				/*if(!ChangeUtil.isNullOrEmpty(targetStateName)) {
					setId(strItem);
					setState(context, targetStateName);
				}*/
			}
			PropertyUtil.setRPEValue(context, "MX_SKIP_PART_PROMOTE_CHECK", "true", false);
			//Logic to RELEASE affecited/Implemented items in order as per the admin settings
			ECMAdmin.enforceReleaseOrder(context, relItemTypPolicyDtls);

			ChangeAction changeAction = new ChangeAction(strChangeActionId);
			//Connects all the affected items
			changeAction.connectImplementedItems(context,obsoleteItems,ChangeConstants.FOR_OBSOLESCENCE);
			changeAction.connectImplementedItems(context,releasedItems,ChangeConstants.FOR_RELEASE);
			changeAction.connectImplementedItems(context,updatedItems,ChangeConstants.FOR_UPDATE);

		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}


	/**
	 * Action Trigger (InWork --> In Approval) to check whether Route Template is connected to the Change Action,
	 * If not get the Senior Technical Assignee and set as the Owner.
	 * @param context
	 * @param args (Change Action Id)
	 * @throws Exception
	 */
	public void setOwner(Context context, String args[])throws Exception
	{
		if (args == null || args.length < 1)
		{
			throw (new IllegalArgumentException());
		}
		try
		{
			String objectId = args[0];// Change Object Id
			String strReviewerRouteTemplate = args[1];
			MapList mapRouteTemplate = new MapList();

			setId(objectId);
			StringList selectStmts = new StringList(1);
			selectStmts.addElement("attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]");

			String whrClause = "attribute["
				+ ATTRIBUTE_ROUTE_BASE_PURPOSE + "] match '"
				+ strReviewerRouteTemplate + "' && current == Active";

			// get route template objects from change object
			mapRouteTemplate = getRelatedObjects(context,
					RELATIONSHIP_OBJECT_ROUTE, TYPE_ROUTE_TEMPLATE,
					selectStmts, null, false, true, (short) 1, whrClause, null, 0);

			// If not Route template is connected to the Change Action, the get the Senior Technical Assignee and set as change Action Owner.
			if(mapRouteTemplate == null || mapRouteTemplate.isEmpty())
			{

				String strSeniorTechAssignee = getInfo(context, "from["+ ChangeConstants.RELATIONSHIP_SENIOR_TECHNICAL_ASSIGNEE +"].to.name");
				setOwner(context, strSeniorTechAssignee);
			}
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}

	/**
	 * Action Trigger on (InApproval --> Approved) to check whether the context Change Action is the ast Change Action to be Approved.
	 * If so then Promote the Change Order to "In Approval" state and notify CO Owner.
	 * @param context
	 * @param args (Change Action Id and Notice)
	 * @throws Exception
	 */
	public void checkForLastCA(Context context, String args[]) throws Exception {
		try {
			String strCAId;
			String strCAState;
			String strCAPolicy;
			String strChangeOrderId = null;
			String strChangeOrderPolicy = null;
			StringList strRouteList = new StringList();
			String strRoutetemplate = null;
			String strCCAId = null;
			Map tempMap = null;
			
			StringList listChangeActionAllStates;
			boolean pendingChangeExists = false;
			String objectId = args[0];// Change Object Id
			setId(objectId);


			StringList slObjectSelect = new StringList(4);
			slObjectSelect.add(SELECT_ID);
			slObjectSelect.add(SELECT_POLICY);
			slObjectSelect.add("from["+RELATIONSHIP_OBJECT_ROUTE+"|to.type=='"+TYPE_ROUTE_TEMPLATE+"' && to.revision == to.last &&  attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]==Approval].to.name");
			slObjectSelect.add("from["+RELATIONSHIP_OBJECT_ROUTE+"|to.type=='"+TYPE_ROUTE+"' &&  attribute["+ATTRIBUTE_ROUTE_BASE_STATE+"]=='state_InApproval'].to.name");
		
			MapList resultList= getRelatedObjects(context,
								ChangeConstants.RELATIONSHIP_CHANGE_ACTION,
								ChangeConstants.TYPE_CHANGE_ORDER,
								slObjectSelect,
								null,
								true,
								false,
								(short) 1,
								"",
								EMPTY_STRING,
								0);
			
			if(resultList != null && !resultList.isEmpty())
			{
				for (Object var : resultList)
				{
					tempMap = (Map) var;
					strChangeOrderId = (String) tempMap.get(SELECT_ID);
					strChangeOrderPolicy = (String) tempMap.get(SELECT_POLICY);
					if(tempMap.get("from["+RELATIONSHIP_OBJECT_ROUTE+"].to.name") instanceof StringList){
						strRouteList = (StringList) tempMap.get("from["+RELATIONSHIP_OBJECT_ROUTE+"].to.name");
						strRoutetemplate = (String) strRouteList.get(0);
					}else{
						strRoutetemplate = (String) tempMap.get("from["+RELATIONSHIP_OBJECT_ROUTE+"].to.name");
					}
					
				}
			}

			if (UIUtil.isNotNullAndNotEmpty(strChangeOrderId)) {
				// Get Change Actions connected to Change Order
				MapList mlChangeActions = getChangeActions(context, strChangeOrderId);
				HashMap releaseStateMap = ChangeUtil.getReleasePolicyStates(context);

				Map mapTemp;
				for (Object var : mlChangeActions) {
					mapTemp = (Map) var;
					strCAId = (String) mapTemp.get(SELECT_ID);
					if (!strCAId.equals(objectId)) {
						strCAState = (String) mapTemp.get(SELECT_CURRENT);					
						strCAPolicy = (String) mapTemp.get(SELECT_POLICY);					
						listChangeActionAllStates = ChangeUtil.getAllStates(context, strCAPolicy);
						if (new ChangeUtil().checkObjState(context, listChangeActionAllStates, strCAState, (String) releaseStateMap.get(strCAPolicy), ChangeConstants.LT) == 0) {
							if (ChangeConstants.TYPE_CCA.equals((String) mapTemp.get(SELECT_TYPE))) {
								String affectedItemExits = DomainObject.newInstance(context, strCAId).getInfo(context, "from[" + DomainConstants.RELATIONSHIP_AFFECTED_ITEM + "]");
								if ("True".equalsIgnoreCase(affectedItemExits)) {
									pendingChangeExists = true;
									break;
								} else {
									strCCAId = strCAId;
								}
							} else {
								pendingChangeExists = true;
								break;
							}
						}
					}
				}

				//If flag is empty, then set the CO state and notify the owner.
				if (!pendingChangeExists) {
					setId(strChangeOrderId);
					if(UIUtil.isNotNullAndNotEmpty(strRoutetemplate)){
						setState(context, PropertyUtil.getSchemaProperty(context, "policy", strChangeOrderPolicy, "state_InApproval"));
					}else{
						// This code has been changed to address the scenario wherein, when the only one CA of CO is promoted to Approved it would promote CO to complete. This in turn would lead to promotion auto promotion of CA
						setState(context, PropertyUtil.getSchemaProperty(context, "policy", strChangeOrderPolicy, "state_InApproval"));
					}
					
					emxNotificationUtilBase_mxJPO.sendNotification(context, 
																			strChangeOrderId, 
																			new StringList(getOwner(context).getName()), 
																			new StringList(), 
																			new StringList(), 
																			args[1], 
																			args[2], 
																			new StringList(), 
																			args[3], 
																			null, null, null);
					if (strCCAId != null) {
						DomainObject.deleteObjects(context, new String[] {strCCAId});
				}
			}
		}
		} catch (Exception Ex) {
			Ex.printStackTrace();
			throw Ex;
		}
	}

	/**
	 * Subsidiary method to get Change Actions connected to the Change Order
	 * @param context
	 * @param strChangeOrderId
	 * @return
	 * @throws Exception
	 */
	public MapList getChangeActions(Context context, String strChangeOrderId)throws Exception
	{
		StringList slObjectSelect = new StringList(4);
		slObjectSelect.add(SELECT_ID);
		slObjectSelect.add(SELECT_NAME);
		slObjectSelect.add(SELECT_CURRENT);
		slObjectSelect.add(SELECT_TYPE);
		slObjectSelect.add(SELECT_POLICY);
		StringList slRelSelect = new StringList(SELECT_RELATIONSHIP_ID);
		setId(strChangeOrderId);
		return getRelatedObjects(context,
				ChangeConstants.RELATIONSHIP_CHANGE_ACTION,
											DomainConstants.QUERY_WILDCARD,
				slObjectSelect,
				slRelSelect,
				false,
				true,
				(short) 1,
				"",
				EMPTY_STRING,
				0);
	}

/**
	 * Method is called from TransferOwnerShip commands in Dashboard.
	 * It will identify the Person objects with specific roles from the XML depending on the affected Item's Type and Policy.
	 * It will identify the person from Responsible Organization of the CA. If not present, it will fetch the RO from Change Order.
	 * Based on the functionality it will either be TechAssignee or SrTechAssignee
	 * @param context
	 * @param args
	 * @return String - representing the Org ID and Role
	 * @throws Exception
	 */
	public String checkAssigneeRole(Context context, String []args) throws Exception
	{
		try
		{
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
			String strObjectId = (String) requestMap.get(ChangeConstants.OBJECT_ID);
			boolean isTechAssignee=true;
			boolean isSrTechAssignee=false;
			String sfunctionality = (String) requestMap.get("sfunctionality");
			if("transferOwnershipToSrTechnicalAssignee".equals(sfunctionality)){
				isTechAssignee = false;
				isSrTechAssignee = true;
			}
			return getRoleDynamicSearchQuery(context, strObjectId, isTechAssignee, isSrTechAssignee);
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}

	/**
	 * Method is called from CATransferOwnerShip commands in Properties page.
	 * It will identify the Person objects with specific roles from the XML depending on the affected Item's Type and Policy.
	 * It will identify the person from Responsible Organization of the CA. If not present, it will fetch the RO from Change Order.
	 * @param context
	 * @param args
	 * @return String - representing the Org ID and Role
	 * @throws Exception
	 */
	public String getTechAssigneeandSrTechAssigneeRoleDynamicSearchQuery(Context context, String []args) throws Exception
	{
		try
		{
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
			String strObjectId = (String) requestMap.get(ChangeConstants.OBJECT_ID);
			return getRoleDynamicSearchQuery(context, strObjectId, true, true);
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}


	/**
	 * Method is called from Href of the Technical Assignee on the CA Property field.
	 * It will identify the Person objects with specific roles from the XML depending on the affected Item's Type and Policy.
	 * It will identify the person from Responsible Organization of the CA. If not present, it will fetch the RO from Change Order.
	 * @param context
	 * @param args
	 * @return String - representing the Org ID and Role
	 * @throws Exception
	 */
	public String getTechAssigneeRoleDynamicSearchQuery(Context context, String []args) throws Exception
	{
		try
		{
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
			HashMap fieldMap = (HashMap)programMap.get(ChangeConstants.FIELD_VALUES);
			HashMap typeAheadMap = (HashMap)programMap.get("typeAheadMap");
			String strObjectId = fieldMap!=null?(String) fieldMap.get(ChangeConstants.ROW_OBJECT_ID): "";
			if(UIUtil.isNullOrEmpty(strObjectId) && typeAheadMap!=null){
				strObjectId = (String) typeAheadMap.get(ChangeConstants.ROW_OBJECT_ID);
			}
			if(UIUtil.isNullOrEmpty(strObjectId)){
				strObjectId = (String) requestMap.get(ChangeConstants.OBJECT_ID);
			}
			return getRoleDynamicSearchQuery(context, strObjectId, true, false);
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}

	/**
	 * Method is called from Href of the Senior Technical Assignee on the CA Property field.
	 * It will identify the Person objects with specific roles from the XML depending on the affected Item's Type and Policy.
	 * It will identify the person from Responsible Organization of the CA. If not present, it will fetch the RO from Change Order.
	 * @param context
	 * @param args
	 * @return String - representing the Org ID and Role
	 * @throws Exception
	 */
	public String getSrTechAssigneeRoleDynamicSearchQuery(Context context, String []args) throws Exception
	{
		try
		{
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);
			HashMap fieldMap = (HashMap)programMap.get(ChangeConstants.FIELD_VALUES);
			HashMap typeAheadMap = (HashMap)programMap.get("typeAheadMap");
			String strObjectId = fieldMap!=null?(String) fieldMap.get(ChangeConstants.ROW_OBJECT_ID): "";
			if(UIUtil.isNullOrEmpty(strObjectId) && typeAheadMap!=null){
				strObjectId = (String) typeAheadMap.get(ChangeConstants.ROW_OBJECT_ID);
			}
			if(UIUtil.isNullOrEmpty(strObjectId)){
				strObjectId = (String) requestMap.get(ChangeConstants.OBJECT_ID);
			}
			return getRoleDynamicSearchQuery(context, strObjectId, false, true);
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
	}

	/**
	 * Subsidiary method for the getTechAssigneeRoleDynamicSearchQuery & getSrTechAssigneeRoleDynamicSearchQuery
	 * @param context
	 * @param strObjectId - Change Action Id
	 * @param isTechRole - boolean for TechAssignee or Senior TechAssignee
	 * @return String
	 * @throws Exception
	 */
	 public String getRoleDynamicSearchQuery(Context context, String strObjectId, boolean isTechRole, boolean isSrTechRole) throws Exception
	 {
		 try
		 {
			 setId(strObjectId);
			 String strTechRole = "";
			 String strSrTechRole = "";
			 String strResponsibleOrg = "";
			 String strIndiviAffectedItemType = "";
			 String strIndiviAffectedItemPolicy = "";
			 ChangeAction changeActionInstance = new ChangeAction();
			 String strAffectedItemType = "from[" + ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM + "].to.type";
			 String strAffectedItemPolicy = "from[" + ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM + "].to.policy";
			 StringList slSelects = new StringList();
			 slSelects.add(strAffectedItemType);
			 slSelects.add(strAffectedItemPolicy);

			 StringBuffer strRole = new StringBuffer();
			 strResponsibleOrg = changeActionInstance.getResponsibleOrganization(context, strObjectId);

			 // Get the Affected item connected to the Change Action.
			 Map mapAffectedItemDetails = getInfo(context, slSelects);

			 if(mapAffectedItemDetails != null && !mapAffectedItemDetails.isEmpty())
			 {
				 strIndiviAffectedItemType = (String)mapAffectedItemDetails.get(strAffectedItemType);
				 strIndiviAffectedItemPolicy = (String)mapAffectedItemDetails.get(strAffectedItemPolicy);

				 // Get the Role from XML with the Type and Policy of Affected Item.
				 if(isTechRole){
					 strTechRole  = ECMAdmin.getTechAssigneeRole(context, strIndiviAffectedItemType, strIndiviAffectedItemPolicy);
				 }
				 if(isSrTechRole){
					 strSrTechRole  = ECMAdmin.getSrTechAssigneeRole(context, strIndiviAffectedItemType, strIndiviAffectedItemPolicy);
				 }

			 }
			 if(UIUtil.isNotNullAndNotEmpty(strTechRole)){
				 strRole.append(strTechRole);
			 }
			 if(UIUtil.isNotNullAndNotEmpty(strSrTechRole)){
				 if(strRole.length()>0){
					 strRole.append(",");
				 }
				 strRole.append(strSrTechRole);
			 }

			 return "MEMBER_ID=" + strResponsibleOrg + ":USERROLE=" + strRole.toString();
		 }
		 catch(Exception Ex)
		 {
			 Ex.printStackTrace();
			 throw Ex;
		 }
	 }

	  /**
		 * Displays the Range Values on Edit for Attribute Requested Change at COAffectedItemsTable/CAAffectedItemsTable..
		 * @param	context the eMatrix <code>Context</code> object
		 * @param	args holds a HashMap containing the following entries:
		 * @param   HashMap containing the following keys, "objectId"
	     * @return  HashMap contains actual and display values
		 * @throws	Exception if operation fails
		 * @since   ECM R211
		 */
	    public HashMap displayRequestedChangeRangeValues(Context context,String[] args) throws Exception
	    {
	    	String strLanguage  	   =  context.getSession().getLanguage();
	    	StringList requestedChange = new StringList();
	    	StringList strListRequestedChange = FrameworkUtil.getRanges(context , ATTRIBUTE_REQUESTED_CHANGE);
	    	HashMap rangeMap = new HashMap ();

	    	StringList listChoices     = new StringList();
	    	StringList listDispChoices = new StringList();

	    	String attrValue = "";
	    	String dispValue = "";
	    	String key = "";

	    	for (int i=0; i < strListRequestedChange.size(); i++)
	    	{
	    		attrValue = (String)strListRequestedChange.get(i);
	    		if(!ChangeConstants.FOR_NONE.equals(attrValue)){
	    		key = attrValue.replace(" ", "_");
	    		dispValue = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"EnterpriseChangeMgt.Range.Requested_Change."+key,strLanguage);
	    		listDispChoices.add(dispValue);
	    		listChoices.add(attrValue);
	    	}
	    	}

	    	if(ChangeUtil.isLegacyDisable(context)){
	    		attrValue = ChangeConstants.FOR_EVOLUTION;
	    		key = attrValue.replace(" ", "_");
	    		dispValue = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"EnterpriseChangeMgt.Range.Requested_Change."+key,strLanguage);
	    		listDispChoices.add(dispValue);
	    		listChoices.add(attrValue);
	    	}
	    	
	    	rangeMap.put("field_choices", listChoices);
	    	rangeMap.put("field_display_choices", listDispChoices);

	    	return rangeMap;
	    }

	    /**
		 * excludeAffectedItems() method returns OIDs of Affect Items
		 * which are already connected to context change object
		 * @param context Context : User's Context.
		 * @param args String array
		 * @return The StringList value of OIDs
		 * @throws Exception if searching Parts object fails.
		 */
		@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
		public StringList excludeAffectedItems(Context context, String args[])throws Exception
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String  strChangeId = (String) programMap.get("objectId");
			StringList strlAffItemList = new StringList();

			if (ChangeUtil.isNullOrEmpty(strChangeId))
				return strlAffItemList;

			try
			{
				MapList affectedItemMapList = getAffectedItems(context, args);
				Map affectedItemMap = null;
				Iterator itr = affectedItemMapList.iterator();
				
				while(itr.hasNext()){
					affectedItemMap = (Map)itr.next();
					strlAffItemList.add(affectedItemMap.get(SELECT_ID));
				}
				//setId(strChangeId);
				//strlAffItemList.addAll(getInfoList(context, "from["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].to.id"));

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return strlAffItemList;
		}
		 /**
		 * Updates the Range Values for Attribute RequestedChange Based on User Selection
		 * @param	context the eMatrix <code>Context</code> object
		 * @param	args holds a HashMap containing the following entries:
		 * paramMap - a HashMap containing the following keys, "relId","RequestedChange"
		 * @return	int
		 * @throws	Exception if operation fails
		 * @since
		 **/
	  public int updateRequestedChangeValues(Context context, String[] args) throws Exception
	  {
		  try
		  {
			  HashMap programMap = (HashMap)JPO.unpackArgs(args);
			  HashMap paramMap   = (HashMap)programMap.get(ChangeConstants.PARAM_MAP);
			  HashMap requestMap = (HashMap)programMap.get(ChangeConstants.REQUEST_MAP);

			  String changeActionId    = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			  String changeObjId       = (String)requestMap.get(ChangeConstants.OBJECT_ID);
			  String affectedItemRelId = (String)paramMap.get(ChangeConstants.SELECT_REL_ID);
			  String strNewRequestedChangeValue = (String)paramMap.get(ChangeConstants.NEW_VALUE);
			  changeManagement  = new ChangeManagement(changeActionId);
			  String affectedItemObjId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump",affectedItemRelId,"to.id");
			  String message    = changeManagement.updateRequestedChangeValues(context, affectedItemObjId, affectedItemRelId, strNewRequestedChangeValue);
			  if ("".equals(message)) {
				  return 0;//operation success
			  }
			  else {
				  emxContextUtil_mxJPO.mqlNotice(context, message);
				  return 1;// for failure
			  }
		  }
		  catch (Exception ex) {
			  ex.printStackTrace();
			  throw new FrameworkException(ex.getMessage());
		  }
	  }

	/**
	 *
	 * @return
	 */
    public MapList getCustomAttributes(Context context, String[] args) throws Exception {

    	Map programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap = (Map)programMap.get("requestMap");

        MapList mlColumns = new MapList();
        Map mapColumn = null;
        Map mapSettings = null;
        StringList attributeList = null;
        Iterator<String> attrItr = null;
        String interfaceName = "";
        String attrName = "";
        String sGroupHeader =EnoviaResourceBundle.getProperty(context, ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(),"EnterpriseChangeMgt.Label.CustomAttributes");
        // Find object's type
        String strObjectId = (String)requestMap.get("objectId");
        DomainObject dmoObject = new DomainObject(strObjectId);
        Map objAttributeMap = null;

        StringList interfaceList = FrameworkUtil.split(MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3", strObjectId, "from[" + ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM + "].interface", "|"), "|");
        Set interfaceSet = new HashSet(interfaceList);
        Iterator<String> interfaceItr = interfaceSet.iterator();
        while(interfaceItr.hasNext()) {
        	interfaceName = interfaceItr.next();
        	objAttributeMap = new HashMap();
        	try {
        		BusinessInterface intface = new BusinessInterface(interfaceName, new Vault(""));
        		AttributeTypeList attributeTypeList = intface.getAttributeTypes(context);
        	    if (attributeTypeList != null) {
        	        objAttributeMap = FrameworkUtil.toMap(context, attributeTypeList);
        	        Iterator<String> keyItr = objAttributeMap.keySet().iterator();
        	        while(keyItr.hasNext()) {
                		attrName = keyItr.next();
                    	mapColumn = new HashMap();
                        mapSettings = new HashMap();
                        mapColumn.put("settings", mapSettings);
                        mapColumn.put("name", attrName);
                        mapColumn.put("expression_relationship", "attribute[" + attrName + "].value");
                        mapColumn.put("label", "emxFramework.Attribute." + attrName.replaceAll(" ", "_"));
                        mapSettings.put("Group Header", sGroupHeader);
                        mapSettings.put("Field Type", "attribute");
                        mapSettings.put("Admin Type", PropertyUtil.getAliasForAdmin(context, "attribute", attrName, true));
                        mapSettings.put("Editable", "true");
                        mapSettings.put("Registered Suite","Framework");

                        HashMap attrMap = (HashMap)objAttributeMap.get(attrName);
                        setColumnSettings(context, mapSettings, (String)attrMap.get(UICache.TYPE), (StringList) attrMap.get(FORMAT_CHOICES),
                        		(String)attrMap.get("multiline"));
                        mapColumn.put(UICache.UOM_ASSOCIATEDWITHUOM, UOMUtil.isAssociatedWithDimension(context, attrName) + "");
                        mapColumn.put(UICache.DB_UNIT, UOMUtil.getSystemunit(context, null, attrName, null));
                        mapColumn.put(UICache.UOM_UNIT_LIST, UOMUtil.getDimensionUnits(context, attrName));
                        if(UOMUtil.isAssociatedWithDimension(context, attrName)) {
                        	mapSettings.remove(SETTING_FORMAT);
                        }
                        mlColumns.add(mapColumn);
        	        }
        	    }


        	} catch(Exception ex) {
        		ex.printStackTrace();
        	}

        }

        return mlColumns;
    }

    /**
     * Method to get the proper Input Type/Format settings for interface attributes
     * @param context
     * @param columnMap
     * @param attrType
     * @param choicesList
     * @param sMultiLine
     * @throws MatrixException
     */
    private void setColumnSettings(Context context, Map columnMap, String attrType, StringList choicesList,
    		String sMultiLine) throws MatrixException {
    	String strFieldFormat = "";
    	String strFieldIPType = INPUT_TYPE_TEXTBOX;

        if(FORMAT_STRING.equalsIgnoreCase(attrType)) {
        	strFieldIPType = INPUT_TYPE_TEXTBOX;
            if(choicesList != null && choicesList.size() > 0) {
                strFieldIPType = INPUT_TYPE_COMBOBOX;
            } else if ("true".equalsIgnoreCase(sMultiLine)) {
                strFieldIPType = INPUT_TYPE_TEXTAREA;
            }
        } else if(FORMAT_BOOLEAN.equalsIgnoreCase(attrType)) {
                strFieldIPType = INPUT_TYPE_COMBOBOX;
        } else if(FORMAT_REAL.equalsIgnoreCase(attrType)) {
            if(choicesList != null && choicesList.size() > 0) {
                strFieldIPType = INPUT_TYPE_COMBOBOX;
            }
            strFieldFormat = FORMAT_NUMERIC;
        } else if(FORMAT_TIMESTAMP.equalsIgnoreCase(attrType)) {
            strFieldFormat = FORMAT_DATE;
        } else if(FORMAT_INTEGER.equalsIgnoreCase(attrType)) {
            if(choicesList != null && choicesList.size() > 0) {
                strFieldIPType = INPUT_TYPE_COMBOBOX;
            }
            strFieldFormat = FORMAT_INTEGER;
        }

        columnMap.put(SETTING_INPUT_TYPE, strFieldIPType);
        if(strFieldFormat.length()>0)
        	columnMap.put(SETTING_FORMAT, strFieldFormat);
    }
    
	/**
	 * The Action trigger  method on (Pending --> In Work) to Promote Connected CO to In Work State
	 * @param context
	 * @param args (Change Action Id)
	 * @throws Exception
	 */
	public void promoteConnectedCO(Context context, String[] args)throws Exception {
		new ChangeAction().promoteConnectedCO(context, args);
	}
	
	/**
	 * Reset Owner on demote of ChangeAction
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 * 0 - String holding the object id.
	 * 1 - String to hold state.
	 * @returns void.
	 * @throws Exception if the operation fails
	 * @since ECM R417
	 */
	public void resetOwner(Context context, String[] args)
			throws Exception
			{
		try
		{
			String objectId = args[0];                              //changeObject ID
			setObjectId(objectId);
			String strCurrentState = args[1];                       //current state of ChangeObject
			
			StringList select	   = new StringList(SELECT_OWNER);
			select.add(SELECT_ORIGINATOR);
			select.add(SELECT_POLICY);
			select.add(ChangeConstants.SELECT_TECHNICAL_ASSIGNEE_NAME);
			Map resultList 		   = getInfo(context, select);
			String currentOwner    = (String) resultList.get(SELECT_OWNER);
			String sOriginator     = (String) resultList.get(SELECT_ORIGINATOR);
			String sPolicy		   = (String) resultList.get(SELECT_POLICY);
			String previousOwner =  (String) resultList.get(ChangeConstants.SELECT_TECHNICAL_ASSIGNEE_NAME);
									
			if(ChangeConstants.POLICY_CHANGE_ACTION.equalsIgnoreCase(sPolicy)&& ChangeConstants.STATE_CHANGE_ACTION_INAPPROVAL.equalsIgnoreCase(strCurrentState) && !ChangeUtil.isNullOrEmpty(previousOwner) && !currentOwner.equalsIgnoreCase(previousOwner))
			{ 
				setOwner(context, previousOwner);
				
			}
            
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
			}

	@com.matrixone.apps.framework.ui.ProgramCallable
	public static StringList getStateWithHoldInfoForTable(Context context,
			String[] args) throws FrameworkException {
		try {
			//String strLanguage = context.getSession().getLanguage();
			HashMap inputMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");
			String objID = null;
			String[] objIDArr = new String[objectMap.size()];
			
			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = new HashMap();
				outerMap = (Map) objectMap.get(i);
				objID = (String) outerMap.get(ChangeConstants.SELECT_ID);
				objIDArr[i] = objID;
			}
			
			StringList returnStringList = ChangeAction.getStateWithHoldInfo(context, objIDArr);
			
			return returnStringList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
	}
	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public String getStateWithHoldInfoForForm(Context context,String[] args)throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");		
		String changeActionId = (String) requestMap.get("objectId");
		String[] objIDArr = new String[]{changeActionId};
		StringList returnStringList = ChangeAction.getStateWithHoldInfo(context, objIDArr);
		
		return (String)returnStringList.get(0);
	}

	/**
	 * Transfers the ownership for CA
	 * @param context - context (the Matrix <code>Context</code> object).
	 * @param args containing CA Object, reason for transfer etc
	 * @throws Exception if the operation fails
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void transferOwnerShipCASummary(Context context, String[] args)throws Exception {

		HashMap programMap   = (HashMap) JPO.unpackArgs(args);		
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String strObjectIds    = (String)paramMap.get("objectId");
		String sReason     = (String)paramMap.get("TransferReason");

		String newOwner 		 = (String)paramMap.get(ChangeConstants.NEW_OWNER);
		
		String objectId = "";
		StringTokenizer strIds = new StringTokenizer(strObjectIds,",");
		ChangeAction caObj = new ChangeAction();
		while(strIds.hasMoreTokens()){
			objectId = (String)strIds.nextToken();
			caObj.setId(objectId);
			caObj.transferOwnership(context, sReason,newOwner);
		}
	}
	
	
    /**Method to transfer the ownership of CO from properties page
    *
    */
   @com.matrixone.apps.framework.ui.PostProcessCallable
   public void transferOwnership(Context context, String[] args) throws Exception {

       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       HashMap requestMap = (HashMap) programMap.get(ChangeConstants.REQUEST_MAP);

       String transferReason = (String)requestMap.get(ChangeConstants.TRANSFER_REASON);
       String objectId 		 = (String)requestMap.get(ChangeConstants.OBJECT_ID);
       String newOwner 		 = (String)requestMap.get(ChangeConstants.NEW_OWNER);
      // String []params 	     = {transferReason,newOwner};
       ChangeAction caObj = new ChangeAction();
       caObj.setId(objectId);
       caObj.transferOwnership(context, transferReason, newOwner);
       
       //new ChangeOrder(objectId).transferOwnership(context, transferReason,newOwner);

   }
	/**
	 * Program to get Column(Reviewer) value For CA Summary table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  Object
	 * @return        Vector of column value
	 * @throws        Exception if the operation fails
	 **
	 */
	public Vector showReviewerColumn(Context context, String args[])throws Exception
	{
		//XSSOK
		Vector columnVals = new Vector();
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
			ChangeUtil changeUtil=new ChangeUtil();
			StringList strObjectIdList = changeUtil.getStringListFromMapList(objectList,DomainObject.SELECT_ID);
			
			String[] strArrObjIds = new String[strObjectIdList.size()];
			strArrObjIds = (String[])strObjectIdList.toArray(strArrObjIds);
			
			MapList objectTypeList = DomainObject.getInfo(context, strArrObjIds, new StringList(DomainObject.SELECT_TYPE));
			StringList strObjectTypeList = changeUtil.getStringListFromMapList(objectTypeList,DomainObject.SELECT_TYPE);

			if (strObjectIdList == null || strObjectIdList.size() == 0){
				return columnVals;
			} else{
				columnVals = new Vector(strObjectIdList.size());
			}
			for(int i=0;i<strObjectIdList.size();i++){
				String 	strChangeActionId = (String) strObjectIdList.get(i);
				String 	strChangeType = (String) strObjectTypeList.get(i);
				if(mxType.isOfParentType(context, strChangeType, ChangeConstants.TYPE_CHANGE_ACTION)){
				columnVals.add(getReviewers(context, strChangeActionId));
			}
				else{
					columnVals.add("");
				}
				
			}
			return columnVals;

		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}//end of method
	
	/**
  	 * Get Reviewers column value 
  	 * @param Context context
  	 * @param args holds information about object.
  	 * @return Reviewers Column.
  	 * @throws Exception if operation fails.
  	 */
	public String getReviewers(Context context,String strChangeActionId)throws Exception
	{
		StringBuilder sb = new StringBuilder();
		StringList finalReviewersList=new StringList();
		String relPattern =  new StringBuffer(ChangeConstants.RELATIONSHIP_CHANGE_REVIEWER).append(",").append(ChangeConstants.RELATIONSHIP_OBJECT_ROUTE).toString();
		String typePattern =  new StringBuffer(ChangeConstants.TYPE_PERSON).append(",").append(ChangeConstants.TYPE_ROUTE_TEMPLATE).toString();
		StringList objectSelects=new StringList(DomainObject.SELECT_ID);
		objectSelects.add(DomainObject.SELECT_TYPE);
		StringList personReviewersList=new StringList();
		StringList routeTemplateReviewersList=new StringList();
		DomainObject changeAction = new DomainObject(strChangeActionId);
		MapList mapList=changeAction.getRelatedObjects(context,
				  relPattern,
				  typePattern,
				  objectSelects,
				  new StringList(DomainRelationship.SELECT_ID),
				  false,
				  true,
				  (short) 2,
				  null, null, (short) 0);
		 
		if(!mapList.isEmpty()){
			Iterator iterator=mapList.iterator();
			while(iterator.hasNext()){
				Map dataMap=(Map)iterator.next();
				String objectType=(String)dataMap.get(DomainObject.SELECT_TYPE);
				String objectId=(String)dataMap.get(DomainObject.SELECT_ID);
				if(objectType.equalsIgnoreCase(ChangeConstants.TYPE_PERSON)){
					personReviewersList.add(objectId);
				}else if(objectType.equalsIgnoreCase(ChangeConstants.TYPE_ROUTE_TEMPLATE)){
					routeTemplateReviewersList.add(objectId);
				}					
			}
		}
		String reviewers = DomainObject.EMPTY_STRING;
		String reviewerstype = DomainObject.EMPTY_STRING;
		if (personReviewersList!=null && !personReviewersList.isEmpty() && routeTemplateReviewersList.isEmpty()){
			for (int i=0;i<personReviewersList.size();i++) {
				String reviewersId = (String) personReviewersList.get(i);
				String reviewerType = new DomainObject(reviewersId).getInfo(context, DomainConstants.SELECT_TYPE);
				reviewers=reviewers.concat(reviewersId+",");
				reviewerstype=reviewerstype.concat(reviewerType+",");
				finalReviewersList.addElement(reviewersId);
			}
		}
			if (routeTemplateReviewersList!=null && !routeTemplateReviewersList.isEmpty()){
				for (int i=0;i<routeTemplateReviewersList.size();i++) {
					String reviewersId = (String) routeTemplateReviewersList.get(i);
					String reviewerType = new DomainObject(reviewersId).getInfo(context, DomainConstants.SELECT_TYPE);
					reviewers=reviewers.concat(reviewersId+",");
					reviewerstype=reviewerstype.concat(reviewerType+",");
					finalReviewersList.addElement(reviewersId);
				}

		}
			sb.append("<input type=\"hidden\" name=\"ReviewersHidden\" id=\"ReviewersHidden\" value=\""+XSSUtil.encodeForHTMLAttribute(context, reviewers)+"\" readonly=\"readonly\" />");
			if (finalReviewersList!=null && !finalReviewersList.isEmpty()){
				for (int i=0;i<finalReviewersList.size();i++) {
				String  lastReviewerId=(String)finalReviewersList.get(finalReviewersList.size()-1);
					String reviewersId = (String) finalReviewersList.get(i);
					if (reviewersId!=null && !reviewersId.isEmpty()) {
						String reviewerName = new DomainObject(reviewersId).getInfo(context, DomainConstants.SELECT_NAME);
						if (reviewerName!=null && !reviewerName.isEmpty()) {
							sb.append("<input type=\"hidden\" name=\""+XSSUtil.encodeForHTMLAttribute(context, reviewerName)+"\" value=\""+XSSUtil.encodeForHTMLAttribute(context, reviewersId)+"\" />");
							sb.append(XSSUtil.encodeForHTML(context, reviewerName));
							if(!lastReviewerId.equalsIgnoreCase(reviewersId))
							sb.append("<br/>");							
						}
					}
				}
			}
		return sb.toString();
	}
	/**
	 * Program to get Column(Contributor) value For CA Summary table
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds the following input arguments:
	 *           0 -  Object
	 * @return        Vector of column value
	 * @throws        Exception if the operation fails
	 **
	 */
	public Vector showContributorColumn(Context context, String args[])throws Exception
	{
		//XSSOK
		Vector columnVals = new Vector();
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
			ChangeUtil changeUtil=new ChangeUtil();
			StringList strObjectIdList = changeUtil.getStringListFromMapList(objectList,DomainObject.SELECT_ID);
			
			String[] strArrObjIds = new String[strObjectIdList.size()];
			strArrObjIds = (String[])strObjectIdList.toArray(strArrObjIds);
			
			MapList objectTypeList = DomainObject.getInfo(context, strArrObjIds, new StringList(DomainObject.SELECT_TYPE));
			StringList strObjectTypeList = changeUtil.getStringListFromMapList(objectTypeList,DomainObject.SELECT_TYPE);

			if (strObjectIdList == null || strObjectIdList.size() == 0){
				return columnVals;
			} else{
				columnVals = new Vector(strObjectIdList.size());
			}
			for(int i=0;i<strObjectIdList.size();i++){
				String 	strChangeActionId = (String) strObjectIdList.get(i);
				String strChangeType = (String) strObjectTypeList.get(i);
				
				if(mxType.isOfParentType(context, strChangeType, ChangeConstants.TYPE_CHANGE_ACTION)){
				columnVals.add(getContributors(context, strChangeActionId));
			}
				else{
					columnVals.add("");
				}
			}
			return columnVals;

		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}//end of method
	
	/**
  	 * Get Contributor column value 
  	 * @param Context context
  	 * @param args holds information about object.
  	 * @return Reviewers Column.
  	 * @throws Exception if operation fails.
  	 */
	public String getContributors(Context context,String strChangeActionId)throws Exception
	{
		StringBuilder sb = new StringBuilder();
	//	StringList finalContributorList=new StringList();
		String relPattern =  new StringBuffer(ChangeConstants.RELATIONSHIP_CHANGE_REVIEWER).append(",").append(ChangeConstants.RELATIONSHIP_OBJECT_ROUTE).toString();
		String typePattern =  new StringBuffer(ChangeConstants.TYPE_PERSON).append(",").append(ChangeConstants.TYPE_ROUTE_TEMPLATE).toString();
		StringList objectSelects=new StringList(DomainObject.SELECT_ID);
		DomainObject changeAction = new DomainObject(strChangeActionId);
		IChangeAction iChangeAction=ChangeAction.getChangeAction(context, strChangeActionId);
		List contributorList=iChangeAction.GetContributors(context);		
			sb.append("<input type=\"hidden\" name=\"ReviewersHidden\" id=\"ReviewersHidden\" value=\""+XSSUtil.encodeForHTMLAttribute(context, contributorList.toString())+"\" readonly=\"readonly\" />");
			if (contributorList!=null && !contributorList.isEmpty()){
				for (int i=0;i<contributorList.size();i++) {
				String  lastContributorName=(String)contributorList.get(contributorList.size()-1);
					String contributorName = (String) contributorList.get(i);
				//	if (contributorName!=null && !contributorName.isEmpty()) {
				//		String contributorName = new DomainObject(contributorId).getInfo(context, DomainConstants.SELECT_NAME);
						if (contributorName!=null && !contributorName.isEmpty()) {
						//	sb.append("<input type=\"hidden\" name=\""+XSSUtil.encodeForHTMLAttribute(context, contributorName)+"\" value=\""+XSSUtil.encodeForHTMLAttribute(context, contributorId)+"\" />");
							sb.append(XSSUtil.encodeForHTML(context, contributorName));
							if(!lastContributorName.equalsIgnoreCase(contributorName))
							sb.append("<br/>");							
						}
					}
				}
			//}
		return sb.toString();
	}
	
	
	/**
	 * To create the Change Action from Create Component
	 *
	 * @author
	 * @param context the eMatrix code context object
	 * @param args packed hashMap of request parameter
	 * @return Map contains change object id
	 * @throws Exception if the operation fails
	 * @Since ECM R418
	 */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public Map createChangeAction(Context context, String[] args) throws Exception {

	    HashMap programMap   = (HashMap) JPO.unpackArgs(args);
	    HashMap requestValue = (HashMap) programMap.get(ChangeConstants.REQUEST_VALUES_MAP);
	    HashMap requestMap   = (HashMap) programMap.get(ChangeConstants.REQUEST_MAP);
	    Map sAttritubeMap				  = new HashMap(); 
	    
	    String strTimeZone = getStringFromArr((String[])requestValue.get("timeZone"),0); 
	    double clientTZOffset = Double.parseDouble(strTimeZone);
	    Locale local = context.getLocale();
	    
	    String sType   = getStringFromArr((String[])requestValue.get("TypeActual"),0);
	    String sEstimatedCompletionDate   = getStringFromArr((String[])requestValue.get("Estimated Completion Date"),0);
	    String sEstimatedStartDate   = getStringFromArr((String[])requestValue.get("EstimatedStartDate"),0);
	    String sDescription   = getStringFromArr((String[])requestValue.get("Description"),0);
	    String sContributor   = getStringFromArr((String[])requestValue.get("ContributorHidden"),0);
	    String sReviewers   = getStringFromArr((String[])requestValue.get("ReviewersHidden"),0);
	    String sFollower   = getStringFromArr((String[])requestValue.get("FollowerHidden"),0);
	    String sGoverningCO   = getStringFromArr((String[])requestValue.get("GoverningCOOID"),0);
	    String sAbstract   = getStringFromArr((String[])requestValue.get("Abstract"),0);
	    String sSeverity   = getStringFromArr((String[])requestValue.get("Severity"),0);
	    String sReviewersType   = getStringFromArr((String[])requestValue.get("ReviewersHiddenType"),0);
	    String sEstimatedCompletionDate_msvalue   = getStringFromArr((String[])requestValue.get("Estimated Completion Date_msvalue"),0);
	    sEstimatedCompletionDate_msvalue = eMatrixDateFormat.getDateValue(context,sEstimatedCompletionDate_msvalue,strTimeZone,local);
	    if(!ChangeUtil.isNullOrEmpty(sEstimatedCompletionDate_msvalue))
	    	sEstimatedCompletionDate_msvalue = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context,sEstimatedCompletionDate_msvalue,clientTZOffset,local);
	    
	    String sEstimatedStartDate_msvalue   = getStringFromArr((String[])requestValue.get("EstimatedStartDate_msvalue"),0);
	    sEstimatedStartDate_msvalue = eMatrixDateFormat.getDateValue(context,sEstimatedStartDate_msvalue,strTimeZone,local);
	    if(!ChangeUtil.isNullOrEmpty(sEstimatedStartDate_msvalue))
	    	sEstimatedStartDate_msvalue = com.matrixone.apps.domain.util.eMatrixDateFormat.getFormattedInputDate(context,sEstimatedStartDate_msvalue,clientTZOffset,local);
	    
	    String sOwner = context.getUser();

	    sAttritubeMap.put(ATTRIBUTE_ORIGINATOR, sOwner);
		sAttritubeMap.put(ChangeConstants.ATTRIBUTE_ESTIMATED_COMPLETION_DATE, sEstimatedCompletionDate_msvalue);
		sAttritubeMap.put(ChangeConstants.ATTRIBUTE_ESTIMATED_START_DATE, sEstimatedStartDate_msvalue);
		sAttritubeMap.put(ATTRIBUTE_SEVERITY, sSeverity);
		sAttritubeMap.put(ChangeConstants.ATTRIBUTE_SYNOPSIS, sAbstract);
		String changeId   = "";
	    Map returnMap     = new HashMap();
	   
	    try {
	    	//Check license
			String[] app = { "ENO_ECM_TP"};
			ComponentsUtil.checkLicenseReserved(context, app);
			String newCAId = new ChangeAction().create(context);
			
			//to handle error :Unexpected publication status value null while promoting CO to Complete
            /*
			String strType=ChangeConstants.TYPE_CHANGE_ACTION;
			String strObjectGeneratorName = FrameworkUtil.getAliasForAdmin(context, DomainConstants.SELECT_TYPE, strType, true);
			String strAutoName = DomainObject.getAutoGeneratedName(context,strObjectGeneratorName, null);
			IChangeActionServices iCaServices = ChangeActionFactory.CreateChangeActionFactory();
			IChangeAction iCa=iCaServices.CreateChangeAction(context,strType, strAutoName, null);
			String newCAId =iCa.getCaBusinessObject().getObjectId(context);
            */
            
	    	ChangeAction changeAction = new ChangeAction(newCAId);
	    	changeAction.setAttributeValues(context, sAttritubeMap);
	    	changeAction.setDescription(context, sDescription);
	    	
	        returnMap.put(ChangeConstants.ID, newCAId);

	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new FrameworkException(e);
	    }

	    return returnMap;
	}

	private String getStringFromArr(String[] StringArr, int intArrIndex) {
		return (StringArr != null) ? (String)StringArr[intArrIndex] : EMPTY_STRING;
	}
	
	/**
	 * Updates the Change Order in CA WebForm.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a MapList with the following as input arguments or entries:
	 * objectId holds the context CA object Id
	 * @throws Exception if the operations fails
	 * @since ECM-R418
	 */
	public void connectChangeOrder(Context context, String[] args) throws Exception {

		try {
			//unpacking the Arguments from variable args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get("paramMap");
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String CAId    = (String)paramMap.get(ChangeConstants.OBJECT_ID);
			String COId    = (String)paramMap.get("New Value");
            String relChangeAction = PropertyUtil.getSchemaProperty(context, "relationship_ChangeAction");
            this.setId(CAId);
            String strChangeActionRelId = getInfo(context, "to["+ relChangeAction +"].id");
            if(!ChangeUtil.isNullOrEmpty(strChangeActionRelId))
			{
				DomainRelationship.disconnect(context, strChangeActionRelId);
			}
			if(!ChangeUtil.isNullOrEmpty(CAId) && !ChangeUtil.isNullOrEmpty(COId))
			{
				ChangeAction changeAction = new ChangeAction(CAId);
				ChangeOrder changeOrder = new ChangeOrder(COId);
				
				DomainRelationship.connect(context, changeOrder, ChangeConstants.RELATIONSHIP_CHANGE_ACTION, changeAction);
			}
		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
	}
	
	
	/**
     * this method performs the cancel process of change Action 
     * 
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: - The ObjectID of the
     *            Change Action
     * @throws Exception
     *             if the operation fails.
     * @since ECM R418.
     */

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void cancelChangeAction(Context context, String[] args) throws Exception

    {
        HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) paramMap.get(ChangeConstants.REQUEST_MAP);
		String objectId    = ChangeUtil.isNullOrEmpty((String)paramMap.get(ChangeConstants.OBJECT_ID))? (String)requestMap.get(ChangeConstants.OBJECT_ID) : (String)paramMap.get(ChangeConstants.OBJECT_ID);
		String cancelReason  = ChangeUtil.isNullOrEmpty((String)paramMap.get("cancelReason"))? (String)requestMap.get("Reason") : (String)paramMap.get("cancelReason");



		ChangeAction changeAction = new ChangeAction(objectId);
		changeAction.cancelChangeAction(context,cancelReason);
    }
    
    /**
     * @author
     * this method performs the hold process of Change Action.
     *
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: - The ObjectID of the
     *            Change Process
     * @throws Exception
     *             if the operation fails.
     * @since ECM R418.
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void holdChangeAction(Context context, String[] args)throws Exception {

        HashMap paramMap   = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)paramMap.get(ChangeConstants.REQUEST_MAP);
		String objectId    = ChangeUtil.isNullOrEmpty((String)paramMap.get(ChangeConstants.OBJECT_ID))? (String)requestMap.get(ChangeConstants.OBJECT_ID) : (String)paramMap.get(ChangeConstants.OBJECT_ID);
		String holdReason  = ChangeUtil.isNullOrEmpty((String)paramMap.get("holdReason"))? (String)requestMap.get("Reason") : (String)paramMap.get("holdReason");
		ChangeAction changeAction = new ChangeAction(objectId);
		changeAction.holdChangeAction(context,holdReason);
    }
	/**
	 * This method is used as access function for Governing CO editable field.
	 * @param context
	 * @return True or False
	 * @throws Exception
	 */
    public boolean isGoverningCOEditable(Context context,String []args) throws Exception {
    	//unpacking the Arguments from variable args
    	boolean flag=false;
    	if(isNewLegacyMode(context, args)){
    		flag=true;
    		HashMap programMap = (HashMap)JPO.unpackArgs(args);
    		String strObjId   = (String)programMap.get(ChangeConstants.OBJECT_ID);
    		String strRelChangeAction = PropertyUtil.getSchemaProperty(context, "relationship_ChangeAction");
    		this.setId(strObjId);
    		String strChangeActionRelFromId = getInfo(context, "to["+ strRelChangeAction +"].from.id");
    		if(!ChangeUtil.isNullOrEmpty(strChangeActionRelFromId)){
    			DomainObject dbObj=new DomainObject(strChangeActionRelFromId);
    			flag= dbObj.isKindOf(context,ChangeConstants.TYPE_CHANGE_REQUEST)? false:true;
    		}
    	}
    	return flag;
    }
	/**
	 * This method is used as access function for Governing CO non editable field.
	 * @param context
	 * @return True or False
	 * @throws Exception
	 */
	public boolean isGoverningCONonEditable(Context context,String []args) throws Exception {
		return !isGoverningCOEditable(context, args);
		
	}
	/**
	 * This method is used as access function for editCA command .
	 * @param context
	 * @return True or False
	 * @throws Exception
	 * //Temporary made changes once modeler API is available then replace this for edit access
	 */
	public boolean isChangeActionEditable(Context context,String []args) throws Exception {
		//unpacking the Arguments from variable args
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strObjId   = (String)programMap.get(ChangeConstants.OBJECT_ID);
		return ChangeAction.getAccess(context, "Edit", strObjId);	
		}
	/**
	 * This method is used as access function for Transfer ownership command .
	 * @param context
	 * @return True or False
	 * @throws Exception
	 */
	public boolean isChangeActionTransferable(Context context,String []args) throws Exception {
		//unpacking the Arguments from variable args
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strObjId   = (String)programMap.get(ChangeConstants.OBJECT_ID);
		return ChangeAction.getAccess(context, "Transfer", strObjId);		
				}
	/**
	 * This method is used as access function for add/remove Team .
	 * @param context
	 * @param String Function name
	 * @param Change Action id
	 * @return True or False
	 * @throws Exception
	 */
	public boolean isTeamEditable(Context context,String functionName,String objectID) throws Exception {
		return ChangeAction.getAccess(context, functionName, objectID);		
	}
    
}

