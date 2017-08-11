/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 */

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import matrix.db.Attribute;
import matrix.db.AttributeItr;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.Relationship;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.util.ComponentsUtil;
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
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.UOMUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.engineering.EBOMFloat;
import com.matrixone.apps.engineering.EBOMMarkup;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.IPartMaster;
import com.matrixone.apps.engineering.Part;
import com.matrixone.apps.engineering.RelToRelUtil;
import com.matrixone.apps.engineering.ReleasePhaseManager;
import com.matrixone.apps.framework.ui.UIForm;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.output.XMLOutputter;
import com.matrixone.jsystem.util.StringUtils;
import com.matrixone.vplmintegrationitf.util.VPLMxIntegrationPublicUtilities;
import com.dassault_systemes.enovia.bom.ReleasePhase;
import com.dassault_systemes.enovia.bom.modeler.exception.BOMMgtException;
import com.dassault_systemes.enovia.bom.modeler.impl.BOMCollaborationService;
import com.dassault_systemes.enovia.bom.modeler.interfaces.input.IBOMIngress;
import com.dassault_systemes.enovia.bom.modeler.interfaces.services.IBOMCollaborationService;
import com.dassault_systemes.enovia.bom.modeler.interfaces.services.IBOMService;
import com.dassault_systemes.enovia.bom.modeler.util.BOMMgtUtil;
import com.dassault_systemes.enovia.changeaction.constants.ActivitiesOperationConstants;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeAction;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedActivity;
import com.dassault_systemes.enovia.changeaction.interfaces.IProposedChanges;
import com.dassault_systemes.enovia.changeaction.interfaces.IRealizedChange;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrder;
import com.dassault_systemes.enovia.partmanagement.modeler.impl.PartCollaborationService;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.input.IPartIngress;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.parameterization.IParameterization;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.services.IPartCollaborationService;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.validator.IPartValidator;
import com.dassault_systemes.vplmintegration.operationsync.factory.VPLMJOperationSyncFactory;
import com.dassault_systemes.vplmintegration.operationsync.itf.IVPLMJOperationSyncModeler;
import com.dassault_systemes.vplmintegration.sdk.VPLMIntegException;
import com.dassault_systemes.enovia.partmanagement.modeler.util.SynchronizationUtil;

public class emxPartMarkupBase_mxJPO extends emxDomainObject_mxJPO {

    public static final String strDevPartPolicy = PropertyUtil.getSchemaProperty("policy_DevelopmentPartMarkup");
    public static final    String strECPartPolicy = PropertyUtil.getSchemaProperty("policy_PartMarkup");
    public static final String RELATIONSHIP_CHANGE_RESPONSIBILITY = PropertyUtil.getSchemaProperty("relationship_ChangeResponsibility");
    public static final String RELATIONSHIP_EMPLOYEE_REPRESENTATIVE = PropertyUtil.getSchemaProperty("relationship_EmployeeRepresentative");

    private String fileContent = "";

    /* Added by Srikanth Anupoju for Plant Item Markup - Starts */
    public static final String RELATIONSHIP_APPLIED_MARKUP =PropertyUtil.getSchemaProperty("relationship_AppliedMarkup");
    public static final String TYPE_PLANT_ITEM_MARKUP = PropertyUtil.getSchemaProperty("type_PlantItemMarkup");
    public static final String RELATIONSHIP_AFFECTED_ITEM = PropertyUtil.getSchemaProperty("relationship_AffectedItem");
    public static final String RELATIONSHIP_PROPOSED_MARKUP = PropertyUtil.getSchemaProperty("relationship_ProposedMarkup");

    public static final String ATTRIBUTE_SEQUENCE=PropertyUtil.getSchemaProperty("attribute_Sequence");
    public static final String ATTRIBUTE_DOC_IN=PropertyUtil.getSchemaProperty("attribute_DocIn");
    public static final String ATTRIBUTE_MAKE_BUY=PropertyUtil.getSchemaProperty("attribute_ProductionMakeBuyCode");
    public static final String ATTRIBUTE_ROHS = PropertyUtil.getSchemaProperty("attribute_RoHS");
    public static final String ATTRIBUTE_ERP_STATUS=PropertyUtil.getSchemaProperty("attribute_ERPStatus");
    public static final String ATTRIBUTE_LEAD_PLANT=PropertyUtil.getSchemaProperty("attribute_LeadPlant");
    public static final String ATTRIBUTE_PLANT_ID=PropertyUtil.getSchemaProperty("attribute_PlantID");
	static String ATTRIBUTE_RELEASE_PHASE = PropertyUtil.getSchemaProperty("attribute_ReleasePhase");
	static String ATTRIBUTE_RELEASE_PHASE_VALUE = "attribute["+ATTRIBUTE_RELEASE_PHASE+"].value";

    public static final String SELECT_ATTRIBUTE_LEFTBRACE = "attribute[";
    public static final String SELECT_RIGHTBRACE = "]";
	public static final String SELECT_TECHNICAL_ASSIGNEES = "to[" + RELATIONSHIP_APPLIED_MARKUP + "].from.from[" + ChangeConstants.RELATIONSHIP_TECHNICAL_ASSIGNEE + "].to.name";

    public static final String SELECT_DOC_IN=SELECT_ATTRIBUTE_LEFTBRACE+ATTRIBUTE_DOC_IN+SELECT_RIGHTBRACE;
    public static final String SELECT_PLANT_ID= SELECT_ATTRIBUTE_LEFTBRACE+ATTRIBUTE_PLANT_ID+SELECT_RIGHTBRACE;
    public static final String SELECT_ATTRIBUTE_ROHS = SELECT_ATTRIBUTE_LEFTBRACE+ATTRIBUTE_ROHS+SELECT_RIGHTBRACE;
    public static final String SELECT_MAKE_BUY= SELECT_ATTRIBUTE_LEFTBRACE+ATTRIBUTE_MAKE_BUY+SELECT_RIGHTBRACE;
    public static final String SELECT_ERP_STATUS= SELECT_ATTRIBUTE_LEFTBRACE+ATTRIBUTE_ERP_STATUS+SELECT_RIGHTBRACE;
    public static final String SELECT_LEAD_PLANT=SELECT_ATTRIBUTE_LEFTBRACE+ATTRIBUTE_LEAD_PLANT+SELECT_RIGHTBRACE;
    public static final String SELECT_SEQUENCE= SELECT_ATTRIBUTE_LEFTBRACE+ATTRIBUTE_SEQUENCE+SELECT_RIGHTBRACE;

    public static final String TYPE_PLANT_BOM_MARKUP = PropertyUtil.getSchemaProperty("type_PlantBOMMarkup");

    public static final String ATTRIBUTE_BRANCH_TO = PropertyUtil.getSchemaProperty("attribute_BranchTo");
    static boolean sameCAForParentChild = Boolean.FALSE;

   /** state "Active" for Organization */
    public static final String STATE_ORGANIZATION_ACTIVE =
            PropertyUtil.getSchemaProperty("policy",
                                            DomainConstants.POLICY_ORGANIZATION,
                                            "state_Active");

    public static final String RELATIONSHIP_MANUFACTURING_RESPONSIBILITY_CHANGE=PropertyUtil.getSchemaProperty("relationship_ManufacturingResponsibilityChange");
    public static final String RELATIONSHIP_MANUFACTURING_RESPONSIBILITY=PropertyUtil.getSchemaProperty("relationship_ManufacturingResponsibility");
    public static final String RELATIONSHIP_PART_REVISION = PropertyUtil.getSchemaProperty("relationship_PartRevision");

    public static final String TYPE_PLANT    = PropertyUtil.getSchemaProperty("type_Plant");
    public static final String TYPE_PART_MASTER=PropertyUtil.getSchemaProperty("type_PartMaster");
    public static final String TYPE_DCR = PropertyUtil.getSchemaProperty("type_DCR");
    public static final String TYPE_MECO    = PropertyUtil.getSchemaProperty("type_MECO");
    /* Added by Srikanth Anupoju for Plant Item Markup - Ends */

  public static final  String TYPE_BOMMARKUP=PropertyUtil.getSchemaProperty("type_BOMMarkup");
  public static final  String TYPE_EBOMMARKUP=PropertyUtil.getSchemaProperty("type_EBOMMarkup");
  public static final  String TYPE_PARTMARKUP=PropertyUtil.getSchemaProperty("type_PARTMARKUP");

  public static final  String RELATIONSHIP_PARTMARKUP=PropertyUtil.getSchemaProperty("relationship_PartMarkup");
  public static final  String RELATIONSHIP_AFFECTEDITEM=PropertyUtil.getSchemaProperty("relationship_AffectedItem");
  public static final  String RELATIONSHIP_EBOMMARKUP=PropertyUtil.getSchemaProperty("relationship_EBOMMarkup");


  public static final  String POLICY_DEVELOPMENTPARTMARKUP=PropertyUtil.getSchemaProperty("policy_DevelopmentPartMarkup");
  public static final  String POLICY_PARTMARKUP=PropertyUtil.getSchemaProperty("policy_PartMarkup");
  public static final String proposedState =PropertyUtil.getSchemaProperty("policy", POLICY_DEVELOPMENTPARTMARKUP, "state_Proposed");
  public static final String rejectedState =PropertyUtil.getSchemaProperty("policy", POLICY_DEVELOPMENTPARTMARKUP, "state_Rejected");
  public static final String approvedState =PropertyUtil.getSchemaProperty("policy", POLICY_PARTMARKUP, "state_Approved");
  public static final String appliedState =PropertyUtil.getSchemaProperty("policy", POLICY_PARTMARKUP, "state_Applied");

  public static final  String POLICY_EBOMMARKUP=PropertyUtil.getSchemaProperty("policy_EBOMMarkup");
  public static final String ebommarkupproposedState =PropertyUtil.getSchemaProperty("policy", POLICY_EBOMMARKUP, "state_Proposed");
  public static final String ebommarkuprejectedState =PropertyUtil.getSchemaProperty("policy", POLICY_EBOMMARKUP, "state_Rejected");
  public static final String ebommarkupapprovedState =PropertyUtil.getSchemaProperty("policy", POLICY_EBOMMARKUP, "state_Approved");
  public static final String ebommarkupappliedState =PropertyUtil.getSchemaProperty("policy", POLICY_EBOMMARKUP, "state_Applied");

  public static final String ATTRIBUTE_STRUCTUREXML=PropertyUtil.getSchemaProperty("attribute_StructureXML");


  public static final String TYPE_ITEMMARKUP=PropertyUtil.getSchemaProperty("type_ItemMarkup");
  public static final String RELATIONSHIP_ALTERNATE_MANUFACTURING_RESPONSIBILITY = PropertyUtil.getSchemaProperty("relationship_AlternateManufacturingResponsibility");


  public static final String POLICY_DCR = PropertyUtil.getSchemaProperty("policy_DCR");
  public static final String STATE_PLAN_DCO =PropertyUtil.getSchemaProperty("policy", POLICY_DCR, "state_PlanDCO");

  public static final String POLICY_ECR = PropertyUtil.getSchemaProperty("policy_ECR");
  public static final String ECR_STATE_PLAN_ECO =PropertyUtil.getSchemaProperty("policy", POLICY_ECR, "state_PlanECO");
  public static final String ECR_STATE_REVIEW =PropertyUtil.getSchemaProperty("policy", POLICY_ECR, "state_Review");
  public static final String ECR_STATE_COMPLETE =PropertyUtil.getSchemaProperty("policy", POLICY_ECR, "state_Complete");

 
  public static final String CA_STATE_IN_APPROVAL =PropertyUtil.getSchemaProperty("policy", ChangeConstants.POLICY_CHANGE_ACTION, "state_InApproval");
  public static final String POLICY_ECO = PropertyUtil.getSchemaProperty("policy_ECO");
  public static final String ECO_STATE_REVIEW =PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Review");
  public static final String ECO_STATE_COMPLETE =PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Complete");
  public static final String ECO_STATE_CANCELLED =PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Cancelled");
  public static final String ECO_STATE_IMPLEMENTED =PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Implemented");
  public static final String RELATIONSHIP_DERIVED = PropertyUtil.getSchemaProperty("relationship_Derived");//added for bug 360985

	public static final String ATTRIBUTE_STATUS=PropertyUtil.getSchemaProperty("attribute_Status");
	public static final String SELECT_STATUS=SELECT_ATTRIBUTE_LEFTBRACE+ATTRIBUTE_STATUS+SELECT_RIGHTBRACE;
	public static final String SELECT_ASSIGNEES = "to[" + RELATIONSHIP_APPLIED_MARKUP + "].from.to[" + RELATIONSHIP_ASSIGNED_EC + "].from.name";
	public static final String SELECT_ECR_ASSIGNEES_FROM_MARKUP_OBJ = "to[" + RELATIONSHIP_PROPOSED_MARKUP + "].from.to[" + RELATIONSHIP_ASSIGNED_EC + "].from.name"; //Added for IR-162940

	//ENG Convergence start
	public static final String PART_MARKUP_PROPOSED_STATE =PropertyUtil.getSchemaProperty("policy", POLICY_PARTMARKUP, "state_Proposed");
	public static final String PART_MARKUP_REJECTED_STATE =PropertyUtil.getSchemaProperty("policy", POLICY_PARTMARKUP, "state_Rejected");
			
	public static final String POLICY_PART_MARKUP_APPROVED_State = PropertyUtil.getSchemaProperty("policy", POLICY_PARTMARKUP, "state_Approved");
	
	public static final String CAPendingState =PropertyUtil.getSchemaProperty("policy", ChangeConstants.POLICY_CHANGE_ACTION, "state_Pending");
	public static final String CAInWorkState =PropertyUtil.getSchemaProperty("policy", ChangeConstants.POLICY_CHANGE_ACTION, "state_InWork");
	public static final String RELATIONSHIP_CHANGE_AFFECTED_ITEM = PropertyUtil.getSchemaProperty("relationship_ChangeAffectedItem");
	public static final String RELATIONSHIP_CHANGE_ACTION = PropertyUtil.getSchemaProperty("relationship_ChangeAction");
	public static final String RELATIONSHIP_CHANGE_ORDER = PropertyUtil.getSchemaProperty("relationship_ChangeOrder");
	
	public static final String ECPART_RELEASE_STATE =PropertyUtil.getSchemaProperty("policy", POLICY_EC_PART, "state_Release");
	
	public static final String PENDING_ADD_OPERATION  = "Add";
	public static final String PENDING_CUT_OPERATION  = "Cut";
	public final static String RANGE_FOR_RELEASE= "For Release";
	//ENG Convergence End
	  
  public Element eleDocRoot = null;

  //377755 start
  public static final String STATE_PART_RELEASE =
      PropertyUtil.getSchemaProperty("policy",
                                      DomainConstants.POLICY_EC_PART,
                                      "state_Release");
  //377755 end

  //IR-019321 - Starts
  public static final String RELATIONSHIP_PART_VERSION = PropertyUtil.getSchemaProperty("relationship_PartVersion");

  //IR-019321 - Ends

    public emxPartMarkupBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);

    }

    /**
     * Get the Policy of the Object and returns true for ECPart Policy.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/

    public static Boolean isECPart(Context context,String[] args) throws Exception
    {
        Boolean bflag=Boolean.FALSE;
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        String objectId=(String)programMap.get("objectId");

        DomainObject domobj=new DomainObject();
        domobj.setId(objectId);

        String sPolicy=domobj.getPolicy(context).getName();
        //371781 - Modified the if condition to check for PolicyClassification instead of Policy
        String policyClassification = EngineeringUtil.getPolicyClassification(context,sPolicy);

        if("Production".equalsIgnoreCase(policyClassification))
        {
            bflag=Boolean.TRUE;
            return bflag;
        }

        return bflag;
    }

    /**
     * Get the Policy of the Object and returns true for Development Policy.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/

    public static Boolean isDevPart(Context context,String[] args) throws Exception
    {
        Boolean bflag=Boolean.FALSE;
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        String objectId=(String)programMap.get("objectId");

        DomainObject domobj=new DomainObject();
        domobj.setId(objectId);

        String sPolicy=domobj.getPolicy(context).getName();
        //371781 - Modified the if condition to check for PolicyClassification instead of Policy
        String policyClassification = EngineeringUtil.getPolicyClassification(context,sPolicy);

        if("Development".equalsIgnoreCase(policyClassification))
        {
            bflag=Boolean.TRUE;
             return bflag;
        }

        return bflag;
    }

    /**
     * Checks for any Proposed Markups for DevParts From Peer Review to Complete
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - ObjectId
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
    // Promotes from Peer Review to Complete only if all the markups are either Rejected or Applied checkMarkupStateforDevParts
    public static int checkProposedMarkupsinPeerReview(Context context,String[] args) throws Exception
    {
    	//Multitenant
    	String sApproveErr=EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.DevPartsApplyError");

        String strPartId = args[0];
        DomainObject doPart = new DomainObject(strPartId);

    	//BGP: In case the Release Process is Production, Do not execute the trigger program functionality
    	if(!ReleasePhase.isECPartWithDevMode(context, doPart))
    		return 0;
    	//BGP: In case the Release Process is Production, Do not execute the trigger program functionality
        String strWhereClause = "current =="+ proposedState;
        String strRelPattern = RELATIONSHIP_EBOM_MARKUP;
        String strTypePattern = TYPE_BOMMARKUP;

        StringList strlObjectSelects = new StringList(1);
        strlObjectSelects.addElement(DomainConstants.SELECT_ID);

        StringList strlRelSelects = new StringList(1);

        MapList mapListMarkups = doPart.getRelatedObjects(context,
                                        strRelPattern, // relationship pattern
                                        strTypePattern, // object pattern
                                        strlObjectSelects, // object selects
                                        strlRelSelects, // relationship selects
                                        false, // to direction
                                        true, // from direction
                                        (short) 1, // recursion level
                                        strWhereClause, // object where clause
                                        null,0); // relationship where clause

        if (mapListMarkups.size() > 0)
        {
            emxContextUtil_mxJPO.mqlNotice(context,sApproveErr);
            return 1;
        }

        return 0;
      }


    /**
     * Checks for any Proposed Markups for Change Process ECO(Design work) ECR(Evaluate) to Review State
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - ObjectId
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
    // Promotes from Design Work to Review(ECO) and evaluate to Review if no Proposed Markup exists for them
    public static int CheckProposedMarkupsECParts(Context context,String[] args) throws Exception
    {
    	//Multitenant
    	String sApproveErr=EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.ApproveError");
        String changID=args[0];

        DomainObject chgobj=new DomainObject();
        chgobj.setId(changID);

        StringList strlObjectSelects = new StringList(1);
        strlObjectSelects.addElement(DomainConstants.SELECT_ID);

        StringList strlRelSelects = new StringList(1);

        String sType=chgobj.getInfo(context,DomainConstants.SELECT_TYPE);
        String strWhereClause = "current =="+ proposedState;
        String strRelPattern = "";
        String strTypePattern = TYPE_PARTMARKUP + "," + TYPE_EBOMMARKUP;

        if(DomainConstants.TYPE_ECO.equalsIgnoreCase(sType)|| chgobj.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION))
        {

            strRelPattern=RELATIONSHIP_APPLIED_MARKUP;

        }
        else
        {
           strRelPattern=RELATIONSHIP_PROPOSED_MARKUP;
        }

        MapList mapListMarkups = chgobj.getRelatedObjects(context,
                strRelPattern, // relationship pattern
                strTypePattern, // object pattern
                strlObjectSelects, // object selects
                strlRelSelects, // relationship selects
                false, // to direction
                true, // from direction
                (short) 1, // recursion level
                strWhereClause, // object where clause
                null,// relationship where clause
                0); //limit is to get all the objects

            if (mapListMarkups.size() > 0)
            {
                emxContextUtil_mxJPO.mqlNotice(context,sApproveErr);
            return 1;
            }
              return 0;
    }

	/**
     * Get the Policy of the Object and returns true for Development Policy.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
    public static String mergeMarkup(Context context, String[] args ) throws Exception
    {
		Hashtable htConflictBOMs = new Hashtable();

		//Multitenant
		String sConflictErr=EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.ConflictError");

        Hashtable htChangedBOMs = new Hashtable();
        Hashtable htNewChangedBOMs = new Hashtable();
        Hashtable htAddedBOMs = new Hashtable();
        Hashtable htDeletedBOMs = new Hashtable();
        Hashtable htUnChangedBOMs = new Hashtable();

        HashMap paramMap=(HashMap)JPO.unpackArgs(args);
        String strChangeId = "";
        String strPartrelPhase = "";
        StringList strlMarkupIds=(StringList)paramMap.get("programMap");
        StringList chhId=(StringList)paramMap.get("chhId");      
        
      //Modified for ENG Convergence start
        String strMode=(String)paramMap.get("mode");
        if(UIUtil.isNotNullAndNotEmpty(strMode) && "mergeFromPartContext".equals(strMode)) {
        	strChangeId = (String)paramMap.get("COId");          	 
        } else {
        	strChangeId=(String)chhId.get(0);
        }
        //Add for ENG Convergence End
		
        DomainObject strChangedomobj=new DomainObject(strChangeId);
        String strChangeType=strChangedomobj.getInfo(context,DomainConstants.SELECT_TYPE);
        strPartrelPhase = strChangeType.equals(ChangeConstants.TYPE_CHANGE_ACTION) ? "Production" : DomainObject.newInstance(context, (String)chhId.get(0)).getInfo(context,ATTRIBUTE_RELEASE_PHASE_VALUE);
        XMLOutputter  outputter = new XMLOutputter();

        String XMLFORMAT = PropertyUtil.getSchemaProperty(context, "format_XML");

        // create a temporary workspace directory
        String strTransPath = context.createWorkspace();

        java.io.File fEmatrixWebRoot = new java.io.File(strTransPath);

        Iterator itrMarkups = strlMarkupIds.iterator();

        DomainObject boMarkup = null;

        String strMarkupId = null;
        String strMarkupName = null;
        String strXMLFileName = null;

        com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder(false);
		builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
		builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder.setValidation(false);

        com.matrixone.jdom.Document docXML = null;

        Element eleRoot = null;
        Element eleRelatioship = null;
        Element eleObjectInfo = null;
        Element eleRelatioshipInfo = null;
        Element eleAttribute = null;

        Element eleBusinessObject = null;

        Element eleMergedMarkup = new Element("ematrix");;
        Element eleMergedRelationships = null;

        Element eleConflictRelationship = null;

        List listFromRelationships = null;
        List listFromAttributes = null;

        Iterator itrRelationships = null;
        Iterator itrAttributes = null;

        com.matrixone.jdom.Attribute attrEBOMChgType = null;
        com.matrixone.jdom.Attribute attrChgType = null;

        String sRes=null;
        String strPartType =  null;
        String strPartName =  null;
        String strPartRev  =  null;
        String strPartFN   =  null;
        String strPartRD   =  null;
        String strPartNewFN   =  null;
        String strPartNewRD   =  null;
        String strEBOMChgType  =  null;
        String strEBOMKey  =  null;
        String strMarkupType=null;
        String  strPartId=null;
        String strPartPolicy=null;
        boolean bolIsConflict = false;

        while (itrMarkups.hasNext()) {
            strMarkupId = (String) itrMarkups.next();

            boMarkup = new DomainObject(strMarkupId);

            strMarkupName =boMarkup.getInfo(context,DomainConstants.SELECT_NAME);

                strMarkupType=boMarkup.getInfo(context,DomainConstants.SELECT_TYPE);
            strPartId=boMarkup.getInfo(context,"to["+DomainConstants.RELATIONSHIP_EBOM_MARKUP+"].from.id");

            DomainObject strPart=new DomainObject();
            strPart.setId(strPartId);

            strPartPolicy=strPart.getInfo(context,DomainConstants.SELECT_POLICY);

            strXMLFileName = strMarkupName + ".xml";

            boMarkup.checkoutFile(context, false, XMLFORMAT, strXMLFileName, strTransPath);

            java.io.File fMarkupXML = new java.io.File(fEmatrixWebRoot, strXMLFileName);

           if (fMarkupXML == null) {
                continue;
            }

            docXML = builder.build(fMarkupXML);
            eleRoot = docXML.getRootElement();

            eleBusinessObject = eleRoot.getChild("businessObject");

           listFromRelationships = eleRoot.getChild("businessObject").getChild("fromRelationshipList").getChildren();
           itrRelationships = listFromRelationships.iterator();

            while (itrRelationships.hasNext()) {
                eleRelatioship = (Element) itrRelationships.next();
                eleObjectInfo = eleRelatioship.getChild("relatedObject").getChild("businessObjectRef");
                eleRelatioshipInfo = eleRelatioship.getChild("attributeList");

				//IR-045004: Decoding the encoded type, name,rev
				strPartType =com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectType").getText());
				strPartName =com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectName").getText());
				strPartRev =com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectRevision").getText());
                attrEBOMChgType = eleRelatioship.getAttribute("chgtype");

                listFromAttributes = eleRelatioshipInfo.getChildren();
                itrAttributes = listFromAttributes.iterator();

                strEBOMChgType = null;

                if (attrEBOMChgType != null )
                {
                    strEBOMChgType = attrEBOMChgType.getValue();
                }

                if (strEBOMChgType == null || "A".equals(strEBOMChgType) || "D".equals(strEBOMChgType))
                {
                    while (itrAttributes.hasNext())
                    {
                        eleAttribute = (Element) itrAttributes.next();
                            //"Find Number"
                        if (DomainConstants.ATTRIBUTE_FIND_NUMBER.equals(eleAttribute.getChild("name").getText()))
                        {
                            strPartFN = eleAttribute.getChild("string").getText();
                        }
                        else if (DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR.equals(eleAttribute.getChild("name").getText()))
                        {

                            strPartRD = eleAttribute.getChild("string").getText();//"Reference Designator"
                        }
                    }
                }
                else
                {
                    while (itrAttributes.hasNext())
                    {
                        eleAttribute = (Element) itrAttributes.next();

                        attrChgType = eleAttribute.getAttribute("chgtype");

                        if (DomainConstants.ATTRIBUTE_FIND_NUMBER.equals(eleAttribute.getChild("name").getText()))
                        {
                            if (attrChgType != null)
                            {
                               strPartFN = eleAttribute.getChild("oldvalue").getText();
                                strPartNewFN = eleAttribute.getChild("newvalue").getText();
                            }
                            else
                            {
                                strPartFN = eleAttribute.getChild("string").getText();
                            }
                        }
                        else if (DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR.equals(eleAttribute.getChild("name").getText()))
                        {
                            if (attrChgType != null)
                            {
                               strPartRD = eleAttribute.getChild("oldvalue").getText();
                                strPartNewRD = eleAttribute.getChild("newvalue").getText();
                            }
                            else
                            {
                                strPartRD = eleAttribute.getChild("string").getText();
                            }
                        }
                    }
                }

                strEBOMKey = strPartType + "~" + strPartName + "~" + strPartRev + "~" + strPartFN + "~" + strPartRD;


                if (strEBOMChgType == null)
                {


                    if (!htDeletedBOMs.containsKey(strEBOMKey) && !htChangedBOMs.containsKey(strEBOMKey) && !htUnChangedBOMs.containsKey(strEBOMKey))
                    {
                        htUnChangedBOMs.put(strEBOMKey, eleRelatioship);
                    }
                }
                else if ("D".equals(strEBOMChgType))
                {
					if (htChangedBOMs.containsKey(strEBOMKey) || htAddedBOMs.containsKey(strEBOMKey) || htConflictBOMs.containsKey(strEBOMKey))
                    {
						if (htChangedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htChangedBOMs.get(strEBOMKey);
							htChangedBOMs.remove(strEBOMKey);
						}
						else if (htAddedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htAddedBOMs.get(strEBOMKey);
							htAddedBOMs.remove(strEBOMKey);
						}
						if (htConflictBOMs.containsKey(strEBOMKey))
						{
							ArrayList arlConflict = (ArrayList) htConflictBOMs.get(strEBOMKey);

							if (!arlConflict.contains(eleConflictRelationship))
							{
								arlConflict.add(eleConflictRelationship);
							}

							arlConflict.add(eleRelatioship);

							htConflictBOMs.remove(strEBOMKey);
							htConflictBOMs.put(strEBOMKey, arlConflict);
						}
						else
						{
							ArrayList arlConflict = new ArrayList();

							arlConflict.add(eleConflictRelationship);
							arlConflict.add(eleRelatioship);

							htConflictBOMs.put(strEBOMKey, arlConflict);
						}

                    }
                    else
                    {
                        htDeletedBOMs.put(strEBOMKey, eleRelatioship);
                        if (htUnChangedBOMs.containsKey(strEBOMKey))
                        {
                            htUnChangedBOMs.remove(strEBOMKey);
                        }
                    }
                }
                else if ("C".equals(strEBOMChgType))
                {
					if (htDeletedBOMs.containsKey(strEBOMKey) || htChangedBOMs.containsKey(strEBOMKey) || htAddedBOMs.containsKey(strEBOMKey) || htConflictBOMs.containsKey(strEBOMKey))
					{
						if (htDeletedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htDeletedBOMs.get(strEBOMKey);
							htDeletedBOMs.remove(strEBOMKey);
						}
						else if (htChangedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htChangedBOMs.get(strEBOMKey);
							htChangedBOMs.remove(strEBOMKey);
						}
						else if (htAddedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htAddedBOMs.get(strEBOMKey);
							htAddedBOMs.remove(strEBOMKey);
						}
						if (htConflictBOMs.containsKey(strEBOMKey))
						{
							ArrayList arlConflict = (ArrayList) htConflictBOMs.get(strEBOMKey);

							if (!arlConflict.contains(eleConflictRelationship))
							{
								arlConflict.add(eleConflictRelationship);
							}

							arlConflict.add(eleRelatioship);

							htConflictBOMs.remove(strEBOMKey);
							htConflictBOMs.put(strEBOMKey, arlConflict);
						}
						else
						{
							ArrayList arlConflict = new ArrayList();

							if (!arlConflict.contains(eleConflictRelationship))
                    {
								arlConflict.add(eleConflictRelationship);
							}

							arlConflict.add(eleRelatioship);

							htConflictBOMs.put(strEBOMKey, arlConflict);
						}
                    }
                    else
                    {
                        htChangedBOMs.put(strEBOMKey, eleRelatioship);
                        if (htUnChangedBOMs.containsKey(strEBOMKey))
                        {
                            htUnChangedBOMs.remove(strEBOMKey);
                        }
                    }
					if (strPartNewFN != null)
					{
						strEBOMKey = strPartType + "~" + strPartName + "~" + strPartRev + "~" + strPartNewFN;
					}
					else
					{
						strEBOMKey = strPartType + "~" + strPartName + "~" + strPartRev + "~" + strPartFN;
					}

					if (strPartNewRD != null)
					{
						strEBOMKey =  strEBOMKey + "~" + strPartNewRD;
					}
					else
					{
						strEBOMKey =  strEBOMKey + "~" + strPartRD;
					}

					if (!htNewChangedBOMs.containsKey(strEBOMKey))
					{
						htNewChangedBOMs.put(strEBOMKey, eleRelatioship);
					}
                }
                else if ("A".equals(strEBOMChgType))
                {
					if (htDeletedBOMs.containsKey(strEBOMKey) || htNewChangedBOMs.containsKey(strEBOMKey) || htConflictBOMs.containsKey(strEBOMKey))
					{
						if (htDeletedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htDeletedBOMs.get(strEBOMKey);
							htDeletedBOMs.remove(strEBOMKey);
						}
						else if (htNewChangedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htNewChangedBOMs.get(strEBOMKey);
							htNewChangedBOMs.remove(strEBOMKey);
						}
						else if (htAddedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htAddedBOMs.get(strEBOMKey);
							htAddedBOMs.remove(strEBOMKey);
						}
						if (htConflictBOMs.containsKey(strEBOMKey))
						{
							ArrayList arlConflict = (ArrayList) htConflictBOMs.get(strEBOMKey);

							if (!arlConflict.contains(eleConflictRelationship))
							{
								arlConflict.add(eleConflictRelationship);
							}

							arlConflict.add(eleRelatioship);

							htConflictBOMs.remove(strEBOMKey);
							htConflictBOMs.put(strEBOMKey, arlConflict);
						}
						else
                    {
							ArrayList arlConflict = new ArrayList();

							arlConflict.add(eleConflictRelationship);
							arlConflict.add(eleRelatioship);

							htConflictBOMs.put(strEBOMKey, arlConflict);
						}
					}
					else
					{
						htAddedBOMs.put(strEBOMKey, eleRelatioship);
					}

                }

            }
        }

		Enumeration enumKeys = htConflictBOMs.keys();
		ArrayList arlElements = null;
		Iterator itrList = null;
		com.matrixone.jdom.Attribute attrConflictEBOMChgType = null;
		com.matrixone.jdom.Attribute attrConflictChgType = null;
		com.matrixone.jdom.Attribute attrConflictMarkup = null;
		String strConflictEBOMMarkup = null;
		String strConflictEBOMChgType = null;
		String strAttrValue = null;
		String strAttrName = null;
		Element eleConflictAttribute = null;
		HashMap hmpAttributes = null;
		HashMap hmpConflictAttributes = null;

		java.util.List listConflictFromAttributes = null;
		Iterator itrConflictAttributes = null;

		ArrayList arlNoConflicts = new ArrayList();
		ArrayList arlConflicts = new ArrayList();

		Hashtable htNewConflicts = new Hashtable();

		boolean blnOtherChange = true;

		Element eleTemp = null;

		while (enumKeys.hasMoreElements())
		{
			String strKey = (String) enumKeys.nextElement();
			arlElements = (ArrayList) htConflictBOMs.get(strKey);
			itrList = arlElements.iterator();

			hmpAttributes = new HashMap();
			hmpConflictAttributes = new HashMap();

			arlConflicts = new ArrayList();

			blnOtherChange = true;

			while (itrList.hasNext())
			{
				strConflictEBOMChgType = null;
				strConflictEBOMMarkup = null;
				eleTemp = (Element) itrList.next();
				attrConflictEBOMChgType = eleTemp.getAttribute("chgtype");

				if (attrConflictEBOMChgType != null)
				{
					strConflictEBOMChgType = attrConflictEBOMChgType.getValue();
				}

				if ("C".equals(strConflictEBOMChgType))
				{
					Element eleConflictRelatioshipInfo = eleTemp.getChild("attributeList");
					listConflictFromAttributes = eleConflictRelatioshipInfo.getChildren();
					itrConflictAttributes = listConflictFromAttributes.iterator();

					while (itrConflictAttributes.hasNext())
					{
						eleConflictAttribute = (Element) itrConflictAttributes.next();

						strAttrName = eleConflictAttribute.getChild("name").getText();

						attrConflictChgType = eleConflictAttribute.getAttribute("chgtype");

						strAttrValue = null;

						if ("Find Number".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}

						}
						else if ("Reference Designator".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}
						}
						else if ("Quantity".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}
						}
						else if ("Component Location".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}
						}
						else if ("Usage".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}

						}else if ("Notes".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}

						}

						if (strAttrValue != null)
						{
							if (hmpAttributes.containsKey(strAttrName))
							{
								if (!strAttrValue.equals((String)hmpAttributes.get(strAttrName)))
								{
									hmpConflictAttributes.put(strAttrName, "TRUE");
								}
							}
							else
							{
								hmpAttributes.put(strAttrName, strAttrValue);
							}
                    }

					}
				}
                    else
                    {
					blnOtherChange = false;
					break;
				}
			}

			if (blnOtherChange)
			{
				HashMap hmpNonConflictAttrAdded = new HashMap();
				itrList = arlElements.iterator();

				while (itrList.hasNext())
				{
					strConflictEBOMChgType = null;
					eleTemp = (Element) itrList.next();

					attrConflictEBOMChgType = eleTemp.getAttribute("chgtype");
					attrConflictMarkup = eleTemp.getAttribute("markupname");

					if (attrConflictEBOMChgType != null)
					{
						strConflictEBOMChgType = attrConflictEBOMChgType.getValue();
					}

					if (attrConflictMarkup != null)
					{
						strConflictEBOMMarkup = attrConflictMarkup.getValue();
                    }

					if ("C".equals(strConflictEBOMChgType))
					{
						Element eleConflictObjectInfo = eleTemp.getChild("relatedObject").getChild("businessObjectRef");
						Element eleRelatioshipDescInfo = eleTemp.getChild("relationshipDefRef");
						com.matrixone.jdom.Attribute attrRelId = eleRelatioshipDescInfo.getAttribute("relid");
						String strRelId = "";
						if (attrRelId != null)
						{
							strRelId = attrRelId.getValue();
                }
						com.matrixone.jdom.Attribute attrObjId = eleTemp.getChild("relatedObject").getAttribute("relatedobjid");
						String strObjId = "";
						if (attrObjId != null)
						{
							strObjId = attrObjId.getValue();
						}


						Element eleNewRel = new Element("relationship");
						eleNewRel.setAttribute("chgtype", "C");
						if (strConflictEBOMMarkup != null)
						{
							eleNewRel.setAttribute("markupname",strConflictEBOMMarkup);
						}
						Element eleRelDesc = new Element("relationshipDefRef");
						eleRelDesc.addContent(eleRelatioshipDescInfo.getText());
						eleRelDesc.setAttribute("relid", strRelId);
						eleNewRel.addContent(eleRelDesc);
						Element eleNewRelationship = new Element("relatedObject");
						eleNewRel.addContent(eleNewRelationship);
						eleNewRelationship.setAttribute("relatedobjid", strObjId);
						Element eleNewBusInfo = new Element("businessObjectRef");
						eleNewRelationship.addContent(eleNewBusInfo);
						Element eleNewAttrInfo = new Element("attributeList");
						eleNewRel.addContent(eleNewAttrInfo);
						Element eleNewAttr = new Element("attribute");
						Element eleNewVal = new Element("newvalue");
						Element eleNameVal = new Element("name");
						Element eleOldVal = new Element("oldvalue");

						Element eleNewConflictRel = new Element("relationship");
						eleNewConflictRel.setAttribute("chgtype", "C");
						if (strConflictEBOMMarkup != null)
						{
							eleNewConflictRel.setAttribute("markupname",strConflictEBOMMarkup);
                    }
						Element eleConflictRelDesc = new Element("relationshipDefRef");
						eleConflictRelDesc.addContent(eleRelatioshipDescInfo.getText());
						eleConflictRelDesc.setAttribute("relid", strRelId);
						eleNewConflictRel.addContent(eleConflictRelDesc);
						Element eleNewConflictRelationship = new Element("relatedObject");
						eleNewConflictRel.addContent(eleNewConflictRelationship);
						eleNewConflictRelationship.setAttribute("relatedobjid", strObjId);
						Element eleNewConflictBusInfo = new Element("businessObjectRef");
						eleNewConflictRelationship.addContent(eleNewConflictBusInfo);
						Element eleNewConflictAttrInfo = new Element("attributeList");
						eleNewConflictRel.addContent(eleNewConflictAttrInfo);
						Element eleNewConflictAttr = new Element("attribute");
						Element eleNewConflictVal = new Element("newvalue");
						Element eleNameConflictVal = new Element("name");
						Element eleOldConflictVal = new Element("oldvalue");


						//IR-045004: Decoding the encoded type, name,rev,vault
						String strConflictPartType = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleConflictObjectInfo.getChild("objectType").getText());
						String strConflictPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleConflictObjectInfo.getChild("objectName").getText());
						String strConflictPartRev = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleConflictObjectInfo.getChild("objectRevision").getText());
						String strConflictPartVault = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleConflictObjectInfo.getChild("vaultRef").getText());

						String strConflictPartUOM  = eleConflictObjectInfo.getChild("UOM").getText();
						String strConflictPartState  = eleConflictObjectInfo.getChild("objectState").getText();

						Element eleObjectType = new Element("objectType");
						eleObjectType.addContent(strConflictPartType);
						Element eleObjectName = new Element("objectName");
						eleObjectName.addContent(strConflictPartName);
						Element eleObjectRevision = new Element("objectRevision");
						eleObjectRevision.addContent(strConflictPartRev);
						Element eleObjectVault = new Element("vaultRef");
						eleObjectVault.addContent(strConflictPartVault);
						Element eleObjectUOM = new Element("UOM");
						eleObjectUOM.addContent(strConflictPartUOM);
						Element eleObjectState = new Element("objectState");
						eleObjectState.addContent(strConflictPartState);

						eleNewBusInfo.addContent(eleObjectType);
						eleNewBusInfo.addContent(eleObjectName);
						eleNewBusInfo.addContent(eleObjectRevision);
						eleNewBusInfo.addContent(eleObjectVault);
						eleNewBusInfo.addContent(eleObjectUOM);
						eleNewBusInfo.addContent(eleObjectState);

						Element eleConflictObjectType = new Element("objectType");
						eleConflictObjectType.addContent(strConflictPartType);
						Element eleConflictObjectName = new Element("objectName");
						eleConflictObjectName.addContent(strConflictPartName);
						Element eleConflictObjectRevision = new Element("objectRevision");
						eleConflictObjectRevision.addContent(strConflictPartRev);
						Element eleConflictObjectVault = new Element("vaultRef");
						eleConflictObjectVault.addContent(strConflictPartVault);
						Element eleConflictObjectUOM = new Element("UOM");
						eleConflictObjectUOM.addContent(strConflictPartUOM);
						Element eleConflictObjectState = new Element("objectState");
						eleConflictObjectState.addContent(strConflictPartState);

						eleNewConflictBusInfo.addContent(eleConflictObjectType);
						eleNewConflictBusInfo.addContent(eleConflictObjectName);
						eleNewConflictBusInfo.addContent(eleConflictObjectRevision);
						eleNewConflictBusInfo.addContent(eleConflictObjectVault);
						eleNewConflictBusInfo.addContent(eleConflictObjectUOM);
						eleNewConflictBusInfo.addContent(eleConflictObjectState);

						Element eleConflictRelatioshipInfo = eleTemp.getChild("attributeList");
						listConflictFromAttributes = eleConflictRelatioshipInfo.getChildren();
						itrConflictAttributes = listConflictFromAttributes.iterator();

						int intConflictCount = 0;
						int intNonConflictCount = 0;

						String strConflictFN = null;
						String strConflictRD = null;
						boolean blnFNConflict = false;
						boolean blnRDConflict = false;
						boolean blnFNNonConflict = false;
						boolean blnRDNonConflict = false;

						Element eleFN = null;
						Element eleRD = null;

						Element eleNonConflictFN = null;
						Element eleNonConflictRD = null;

						while (itrConflictAttributes.hasNext())
						{
							eleConflictAttribute = (Element) itrConflictAttributes.next();

							strAttrName = eleConflictAttribute.getChild("name").getText();

							attrConflictChgType = eleConflictAttribute.getAttribute("chgtype");

							eleNewAttr = new Element("attribute");
							eleNewVal = new Element("newvalue");
							eleOldVal = new Element("oldvalue");
							eleNameVal = new Element("name");

							strAttrValue = null;
							String strAttrOldValue = null;

							if ("Find Number".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
									strConflictFN = eleConflictAttribute.getChild("oldvalue").getText();
								}
                    else
                    {
									strConflictFN = eleConflictAttribute.getChild("string").getText();
								}

								eleFN = new Element("attribute");

								eleNonConflictFN  = new Element("attribute");

								eleNewVal = new Element("string");
								eleNameVal = new Element("name");

								eleFN.addContent(eleNameVal);
								eleNameVal.addContent(strAttrName);
								eleFN.addContent(eleNewVal);
								eleNewVal.addContent(strConflictFN);

								eleNewVal = new Element("string");
								eleNameVal = new Element("name");

								eleNonConflictFN.addContent(eleNameVal);
								eleNameVal.addContent(strAttrName);
								eleNonConflictFN.addContent(eleNewVal);
								eleNewVal.addContent(strConflictFN);

                    }
							else if ("Reference Designator".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
									strConflictRD = eleConflictAttribute.getChild("oldvalue").getText();
								}
								else
								{
									strConflictRD = eleConflictAttribute.getChild("string").getText();
								}

								eleRD = new Element("attribute");

								eleNonConflictRD  = new Element("attribute");

								eleNewVal = new Element("string");
								eleNameVal = new Element("name");

								eleRD.addContent(eleNameVal);
								eleNameVal.addContent(strAttrName);
								eleRD.addContent(eleNewVal);
								eleNewVal.addContent(strConflictRD);

								eleNewVal = new Element("string");
								eleNameVal = new Element("name");

								eleNonConflictRD.addContent(eleNameVal);
								eleNameVal.addContent(strAttrName);
								eleNonConflictRD.addContent(eleNewVal);
								eleNewVal.addContent(strConflictRD);
							}
							else if ("Quantity".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
								}
							}
							else if ("Component Location".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
								}
							}
							else if ("Usage".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
								}
							}else if ("Notes".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
								}
							}

							if (strAttrValue != null)
							{
								if (hmpConflictAttributes.containsKey(strAttrName))
								{
									if ("Find Number".equals(strAttrName))
									{
										blnFNConflict = true;
									}
									else if ("Reference Designator".equals(strAttrName))
									{
										blnRDConflict = true;
									}

									eleNewConflictAttr = new Element("attribute");
									eleNewConflictAttr.setAttribute("chgtype", "C");
									eleNewConflictVal = new Element("newvalue");
									eleOldConflictVal = new Element("oldvalue");
									eleNameConflictVal = new Element("name");

									eleNewConflictAttrInfo.addContent(eleNewConflictAttr);
									eleNewConflictAttr.addContent(eleNameConflictVal);
									eleNameConflictVal.addContent(strAttrName);
									eleNewConflictAttr.addContent(eleOldConflictVal);
									eleOldConflictVal.addContent(strAttrOldValue);
									eleNewConflictAttr.addContent(eleNewConflictVal);
									eleNewConflictVal.addContent(strAttrValue);
									intConflictCount++;
                    }
                    else
                    {
									if (!hmpNonConflictAttrAdded.containsKey(strAttrName))
									{
										if ("Find Number".equals(strAttrName))
										{
											blnFNNonConflict = true;
										}
										else if ("Reference Designator".equals(strAttrName))
										{
											blnRDNonConflict = true;
										}

										eleNewAttr = new Element("attribute");
										eleNewAttr.setAttribute("chgtype", "C");
										eleNewVal = new Element("newvalue");
										eleOldVal = new Element("oldvalue");
										eleNameVal = new Element("name");

										eleNewAttrInfo.addContent(eleNewAttr);
										eleNewAttr.addContent(eleNameVal);
										eleNameVal.addContent(strAttrName);
										eleNewAttr.addContent(eleOldVal);
										eleOldVal.addContent(strAttrOldValue);
										eleNewAttr.addContent(eleNewVal);
										eleNewVal.addContent(strAttrValue);

										intNonConflictCount++;

										hmpNonConflictAttrAdded.put(strAttrName, strAttrValue);
									}
								}
                    }
                }

						if (!blnFNConflict)
						{
							eleNewConflictAttrInfo.addContent(eleFN);
						}
						if (!blnRDConflict)
						{
							eleNewConflictAttrInfo.addContent(eleRD);
						}

						if (!blnFNNonConflict)
						{
							eleNewAttrInfo.addContent(eleNonConflictFN);
						}
						if (!blnRDNonConflict)
						{
							eleNewAttrInfo.addContent(eleNonConflictRD);
            }


						if (intConflictCount > 0)
						{
							arlConflicts.add(eleNewConflictRel);
						}
						if (intNonConflictCount > 0)
						{
							arlNoConflicts.add(eleNewRel);
						}
					}
				}

				if (!arlConflicts.isEmpty())
				{
					htNewConflicts.put(strKey, arlConflicts);
				}
				else
				{
					htConflictBOMs.remove(strKey);
                    }
                }
        }
		htConflictBOMs.putAll(htNewConflicts);

		if (!htConflictBOMs.isEmpty())
		{
			bolIsConflict = true;
        }

        if (bolIsConflict)
        {
            sRes="false";
            emxContextUtil_mxJPO.mqlNotice(context,sConflictErr);
            return sRes;
        }
        else
        {
            //Shd confirm the use of this variable here
            boolean blnSave=true;
            if (blnSave && strlMarkupIds.size() > 1)
            {

                eleMergedRelationships = new Element("fromRelationshipList");

                eleMergedRelationships.setAttribute("count", "" + (htAddedBOMs.size() + htDeletedBOMs.size() + htChangedBOMs.size() + htUnChangedBOMs.size())+1);

                // establish this with new info, juz added for test
                eleBusinessObject.removeChild("fromRelationshipList");
                eleBusinessObject.removeChild("header");

                enumKeys = htDeletedBOMs.keys();

                while (enumKeys.hasMoreElements())
                {
                    String strKey = (String) enumKeys.nextElement();
                    eleTemp = (Element) htDeletedBOMs.get(strKey);
                    eleMergedRelationships.addContent(eleTemp.detach());
                }

                enumKeys = htChangedBOMs.keys();

                while (enumKeys.hasMoreElements())
                {
                    String strKey = (String) enumKeys.nextElement();
                    eleTemp = (Element) htChangedBOMs.get(strKey);
                    eleMergedRelationships.addContent(eleTemp.detach());
                }

				Iterator itrNoConlicts = arlNoConflicts.iterator();

				while (itrNoConlicts.hasNext())
				{
					eleTemp = (Element) itrNoConlicts.next();
					eleMergedRelationships.addContent(eleTemp.detach());
				}

                enumKeys = htAddedBOMs.keys();

                while (enumKeys.hasMoreElements())
                {
                    String strKey = (String) enumKeys.nextElement();
                    eleTemp = (Element) htAddedBOMs.get(strKey);
                    eleMergedRelationships.addContent(eleTemp.detach());
                }

                enumKeys = htUnChangedBOMs.keys();

                while (enumKeys.hasMoreElements())
                {
                    String strKey = (String) enumKeys.nextElement();
                    eleTemp = (Element) htUnChangedBOMs.get(strKey);
                    eleMergedRelationships.addContent(eleTemp.detach());
                }

                eleBusinessObject.addContent(eleMergedRelationships);

                eleMergedMarkup.addContent(eleBusinessObject.detach());


                String strMergedMarkupId = "";

               //IR-158800V6R2013x : Added EBOMMarkup check for preV6 Markup types and commented elseif block
                if (TYPE_BOMMARKUP.equalsIgnoreCase(strMarkupType) || TYPE_EBOMMARKUP.equalsIgnoreCase(strMarkupType))
                {
                    if(DomainConstants.POLICY_EC_PART.equalsIgnoreCase(strPartPolicy) && "Production".equalsIgnoreCase(strPartrelPhase))
                    {
                    strMergedMarkupId = FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
                    }
                    else
                    {
                        strMergedMarkupId = FrameworkUtil.autoName(context,"type_BOMMarkup","policy_DevelopmentPartMarkup");
                    }

                }

                // Added to support for MBOM Plant BOM Markup type
                else
                {
                    strMergedMarkupId = "";
                }

                DomainObject doMergedMarkup = new DomainObject(strMergedMarkupId);

                String sMarkupMaergedName=doMergedMarkup.getInfo(context,DomainConstants.SELECT_NAME);

                java.io.File fMergedXML = new java.io.File (fEmatrixWebRoot, sMarkupMaergedName + "." + "xml");
                FileOutputStream fosMergedXML = new FileOutputStream(fMergedXML);

                outputter.output(eleMergedMarkup, fosMergedXML);

                matrix.db.File fCheckinMergedXML = new matrix.db.File(fMergedXML.getAbsolutePath(), XMLFORMAT);
                FileList fileListCheckin = new FileList();
                fileListCheckin.addElement(fCheckinMergedXML);

                doMergedMarkup.checkinFromServer(context, false, false, XMLFORMAT, "", fileListCheckin);

                if (strChangeId != null && (DomainConstants.TYPE_ECO.equalsIgnoreCase(strChangeType) 
                		|| DomainConstants.TYPE_ECR.equalsIgnoreCase(strChangeType) || TYPE_DCR.equalsIgnoreCase(strChangeType) 
                		|| (ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(strChangeType)))) {

                    DomainObject doChange = new DomainObject(strChangeId);

                    String strRelToConnectToChange =null;

                    if (doChange.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION) || doChange.isKindOf(context, DomainConstants.TYPE_ECO) 
                    		|| doChange.isKindOf(context, TYPE_MECO)) //Modified for ENG Convergence
                    {
                        strRelToConnectToChange =RELATIONSHIP_APPLIED_MARKUP;
                        //"Applied Markup";
                    }
                    else if (doChange.isKindOf(context, DomainConstants.TYPE_ECR) || doChange.isKindOf(context, TYPE_DCR))
                    {
                        strRelToConnectToChange = RELATIONSHIP_PROPOSED_MARKUP;
                    }

					DomainRelationship.connect(context, doChange, strRelToConnectToChange, doMergedMarkup);

                    String strChangeCurrentState = doChange.getInfo(context, DomainConstants.SELECT_CURRENT);

                    if (strChangeCurrentState.equals(ECO_STATE_REVIEW) || strChangeCurrentState.equals(ECR_STATE_REVIEW) || strChangeCurrentState.equals(ECR_STATE_PLAN_ECO) || strChangeCurrentState.equals(STATE_PLAN_DCO) || strChangeCurrentState.equals(CA_STATE_IN_APPROVAL))
                    {
                    	doMergedMarkup.setAttributeValue(context, ATTRIBUTE_BRANCH_TO, "None");
                    	context.setCustomData("fromMarkupActions", "TRUE");
                    	doMergedMarkup.promote(context);
                    	context.removeFromCustomData("fromMarkupActions");
                    }
                }

                if (strPartId != null)
                {
                    DomainObject doPart = new DomainObject(strPartId);

					DomainRelationship.connect(context, doPart, DomainConstants.RELATIONSHIP_EBOM_MARKUP, doMergedMarkup);
                }

                Iterator itrDeleteMarkups = strlMarkupIds.iterator();

                //connect to a ECO/ECR using Proposed Markup or Applied Markup relationship

                //connect to a Part using EBOM Markup relationship

                while (itrDeleteMarkups.hasNext())
                {
                    String strDeletedMarkupId = (String) itrDeleteMarkups.next();
                   MqlUtil.mqlCommand(context, "delete bus $1", strDeletedMarkupId);
                }
            }
        }
        sRes="true";
        return sRes;
    }

     /**
      * Merges the Markups from the Change Process Life Cycle
      * @param    context the eMatrix <code>Context</code> object
      * @param    args holds a StringList containing the following entries:
      *            -0 holds the Change Process Id
      * paramMap - a StringList containing all selected Markup Ids
      * @return   void
      * @throws   Exception if operation fails
      * @since   EngineeringCentral X3
      */
     //Shd change the name of the method megreApprovedMarkupsOnChange
     public static int mergeMarkupsonChange(Context context, String args[]) throws Exception
    {
        HashMap finalMap=new HashMap();
        HashMap finalMapPlantBOM=new HashMap();
        String strChangeId = args[0];
        String strRelPattern = null;
        String strAIRelPattern = RELATIONSHIP_AFFECTEDITEM ;
       
        String strPartTypePattern = TYPE_PART;

        String stre="true";
        StringList changeIds=new StringList(1);
        changeIds.addElement(strChangeId);

        String strTypePattern = TYPE_BOMMARKUP + "," + TYPE_PLANT_BOM_MARKUP + "," + TYPE_EBOMMARKUP;

        StringList strlObjectSelects = new StringList(1);
        strlObjectSelects.addElement(DomainConstants.SELECT_ID);
        strlObjectSelects.addElement(DomainConstants.SELECT_TYPE);

        StringList strlRelSelects = new StringList(1);
        strlRelSelects.addElement("to["+RELATIONSHIP_EBOM_MARKUP+"].from.id");

        String strPartId = null;
        String strWhereClause=null;

        StringList programMap = new StringList();
        StringList programMapPlantBOM = new StringList();
        MapList mapListParts;
        DomainObject doChange = new DomainObject(strChangeId);
        if (doChange.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION)){
			mapListParts = EngineeringUtil.getProposedItems(context, strChangeId, strlObjectSelects );
		}
        else{

	        mapListParts = doChange.getRelatedObjects(context,
	                                        strAIRelPattern, // relationship pattern
	                                        strPartTypePattern, // object pattern
	                                        strlObjectSelects, // object selects
	                                        strlRelSelects, // relationship selects
	                                        false, // to direction
	                                        true, // from direction
	                                        (short) 1, // recursion level
	                                        null, // object where clause
                                        null,// relationship where clause
                                        0); //limit is to get all the objects

        }
        Iterator itrParts = mapListParts.iterator();

        while (itrParts.hasNext())
        {
            programMap = new StringList();
            programMapPlantBOM = new StringList();
            Map mapPart = (Map) itrParts.next();
            strPartId = (String) mapPart.get(DomainConstants.SELECT_ID);

            if (doChange.isKindOf(context, DomainConstants.TYPE_ECO) || doChange.isKindOf(context, TYPE_MECO) || doChange.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION))
            {
                strRelPattern = RELATIONSHIP_APPLIED_MARKUP;
                strWhereClause = "policy =="+"\""+POLICY_PARTMARKUP+"\""+" "+"&&"+" "+"current=="+approvedState+" "+"&&"+" "+"to["+RELATIONSHIP_EBOM_MARKUP+"].from.id == " + strPartId;
            }
            else if (doChange.isKindOf(context, DomainConstants.TYPE_ECR) || doChange.isKindOf(context, TYPE_DCR))
            {
                strRelPattern = RELATIONSHIP_PROPOSED_MARKUP;
                strWhereClause = "policy =="+"\""+POLICY_PARTMARKUP+"\""+" "+"&&"+" "+"current=="+approvedState+" "+"&&"+" "+"to["+RELATIONSHIP_EBOM_MARKUP+"].from.id == " + strPartId;
            }

            MapList mapListMarkups = doChange.getRelatedObjects(context,
                                            strRelPattern, // relationship pattern
                                            strTypePattern, // object pattern
                                            strlObjectSelects, // object selects
                                            strlRelSelects, // relationship selects
                                            false, // to direction
                                            true, // from direction
                                            (short) 1, // recursion level
                                            strWhereClause, // object where clause
                                            null, // relationship where clause
                                            0); //limit is to get all the objects

            Iterator itrMarkupIds = mapListMarkups.iterator();

            Map mapMarkup = null;
            String strMarkupObjId = null;
            String strMarkupType = null;
            finalMap.put("chhId",changeIds);
            finalMapPlantBOM.put("chhId",changeIds);
            while (itrMarkupIds.hasNext())
            {
                mapMarkup = (Map) itrMarkupIds.next();
                strMarkupObjId = (String) mapMarkup.get(DomainConstants.SELECT_ID);
                strMarkupType = (String) mapMarkup.get(DomainConstants.SELECT_TYPE);
                if(strMarkupType.equals(TYPE_PLANT_BOM_MARKUP)) {
                    programMapPlantBOM.add(strMarkupObjId);
                } else {
                programMap.add(strMarkupObjId);
                }
            }

            if(programMap.size() > 0) {
                finalMap.put("programMap",programMap);
                stre=(String)JPO.invoke(context,"emxPartMarkup",null,"mergeMarkup",JPO.packArgs(finalMap),String.class);
                if("false".equalsIgnoreCase(stre))
                {
                    return 1;
                }
            }
            if(programMapPlantBOM.size() > 0) {
                //Plant BOM Markups have to be merged one each for plants
                Map mpPlantBasedBOMs = getPlantBasedBOMs(context,programMapPlantBOM);
                Set setPlantsIds = mpPlantBasedBOMs.keySet();
                Iterator itrPlants = setPlantsIds.iterator();
                StringList strlPlantBOMs = null;
                while (itrPlants.hasNext()) {
					strlPlantBOMs = (StringList)mpPlantBasedBOMs.get(itrPlants.next());
					finalMapPlantBOM.put("programMap",strlPlantBOMs);
                stre=(String)JPO.invoke(context,"emxPartMarkup",null,"mergePlantBOMMarkups",JPO.packArgs(finalMapPlantBOM),String.class);
                if("false".equalsIgnoreCase(stre))
                {
                    return 1;
                }
            }
            }
        }
        return 0;

    }

/**
     * Get All the Markups for EC and Development Part
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllMarkupSummary(Context context,String[] args)throws Exception
    {

        ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
        MapList MarkupIds=new MapList();
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        String objectId=(String)programMap.get("objectId");
        String strPortalCommandName = (String)programMap.get("portalCmdName");
        DomainObject domobj = DomainObject.newInstance(context);
        domobj.setId(objectId);

        StringList selectStmts = new StringList(3);
        selectStmts.addElement(DomainConstants.SELECT_ID);
        selectStmts.addElement(DomainConstants.SELECT_OWNER);
        selectStmts.addElement(DomainConstants.SELECT_CURRENT);

        StringList selectRelStmts = new StringList(1);
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

        //need to select all types to include old markup types
        String strTypes = "*"; //TYPE_PARTMARKUP+","+TYPE_EBOMMARKUP;
        if("ENCItemMarkupsCommand".equals(strPortalCommandName)){
        	strTypes = TYPE_ITEMMARKUP;
        }

       MarkupIds = domobj.getRelatedObjects(context,RELATIONSHIP_EBOMMARKUP,strTypes,selectStmts,selectRelStmts,false,true,(short)1,null,null,0);

       ContextUtil.popContext(context);

        return MarkupIds;

    }

      /**
       * Apply Markup to the BOM Structure From Change Process
       * @param    context the eMatrix <code>Context</code> object
       * @param    args holds a StringList containing the following entries:
       * paramMap - a StringList containing all selected Markup Ids
       * @return   int
       * @throws   Exception if operation fails
       * @since   EngineeringCentral X3
       */
      public int applyMarkupOnChange(Context context,String[] args) throws Exception
      {
         String changeId = args[0];
         String strAIRelPattern = RELATIONSHIP_AFFECTEDITEM;
         String strPartTypePattern = TYPE_PART;

         String stre="true";
         ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());

         try
         {

			String strTypePattern = TYPE_PARTMARKUP+","+TYPE_EBOMMARKUP;
			StringList strlObjectSelects = new StringList(1);
			strlObjectSelects.addElement(DomainConstants.SELECT_ID);
			strlObjectSelects.addElement(DomainConstants.SELECT_TYPE);
			strlObjectSelects.addElement("from["+RELATIONSHIP_EBOM+"].to.id");
			
			StringList strlRelSelects = new StringList(1);
			strlRelSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
			
			String strPartId = null;
			String strWhereClause=null;
			String strRelPattern=null;
			MapList mapListParts;
			DomainObject doChange = new DomainObject(changeId);
			DomainObject.MULTI_VALUE_LIST.add("from["+RELATIONSHIP_EBOM+"].to.id");
			if (doChange.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION)){
				mapListParts = EngineeringUtil.getProposedItems(context, changeId,strlObjectSelects );
			}
			else {
				mapListParts = doChange.getRelatedObjects(context,
						strAIRelPattern, // relationship pattern
						strPartTypePattern, // object pattern
						strlObjectSelects, // object selects
						strlRelSelects, // relationship selects
						false, // to direction
						true, // from direction
						(short) 1, // recursion level
						null, // object where clause
			                                 null, // relationship where clause
			                                 0); //limit is to get all the objects
			}
			DomainObject.MULTI_VALUE_LIST.remove("from["+RELATIONSHIP_EBOM+"].to.id");
			Iterator itrParts = mapListParts.iterator();
			StringList slAffectedItemList = EngineeringUtil.getValueForKey(mapListParts, SELECT_ID);
			while(itrParts.hasNext())
			{
				Map mapPart = (Map) itrParts.next();
	            strPartId = (String) mapPart.get(DomainConstants.SELECT_ID);
	            
	            //StringList slEBOMList = (StringList)mapPart.get("from["+RELATIONSHIP_EBOM+"].to.id");
			    StringList slEBOMList = new StringList();
	            Object objectData = mapPart.get("from["+RELATIONSHIP_EBOM+"].to.id");
	            if(null != objectData){
	    			if (objectData instanceof StringList) {
	    				slEBOMList =  (StringList) objectData;
	    			} else if (objectData instanceof String) {
	    				slEBOMList.addElement(objectData);
	    			}
	            }
	            if(slEBOMList != null){
	            	for(int i = 0; i <slEBOMList.size();i++){
	            		if(slAffectedItemList.contains(slEBOMList.get(i))){
	            			sameCAForParentChild = true;
	            			break;
	            		}
	            		else {
	            			sameCAForParentChild = false;
	            		}
	            	}
	            }
                strRelPattern = RELATIONSHIP_APPLIED_MARKUP;
                strWhereClause = "(policy == " + "\"" + POLICY_PARTMARKUP + "\"" + " " + "||" + " " + "policy == " + "\"" + POLICY_EBOMMARKUP + "\"" + ") " + "&&"+" "+"current=="+approvedState+" "+"&&"+" "+"to["+RELATIONSHIP_EBOM_MARKUP+"].from.id == " + strPartId;

                MapList mapListMarkups = doChange.getRelatedObjects(context,
                                             strRelPattern, // relationship pattern
                                             strTypePattern, // object pattern
                                             strlObjectSelects, // object selects
                                             strlRelSelects, // relationship selects
                                             false, // to direction
                                             true, // from direction
                                             (short) 1, // recursion level
                                             strWhereClause, // object where clause
                                             null, // relationship where clause
                                             0); //limit is to get all the objects


	             Iterator itrMarkupIds = mapListMarkups.iterator();
                 while(itrMarkupIds.hasNext())
                 {
					Map mapMkp = (Map)itrMarkupIds.next();
					String strMarkupObjId = (String)mapMkp.get(DomainConstants.SELECT_ID);
					
					DomainObject doMarkup = new DomainObject(strMarkupObjId);
					String strMarkupType = doMarkup.getInfo(context,DomainConstants.SELECT_TYPE);

					String[] inputArgs = new String[2];
					inputArgs[0]= strPartId;
					inputArgs[1]= changeId;
					
					String strNewPartId = null;
					if (changeId != null)
					{
						DomainObject changeobj = new DomainObject (changeId);
						if (changeobj.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION)){
							strNewPartId = getImplementedItem(context, inputArgs);
						}
						else{
							strNewPartId = (String) JPO.invoke(context, "enoEngChange", null, "getIndirectAffectedItems", inputArgs,String.class);
						}
					}
					if (strNewPartId != null)
					{
						strPartId = strNewPartId;
					}
					
					DomainObject doPart = new DomainObject(strPartId);
					if (TYPE_EBOMMARKUP.equals(strMarkupType))
					{
						strPartId = doPart.getInfo(context, "next.id");
					}

					
	                if(TYPE_BOMMARKUP.equalsIgnoreCase(strMarkupType) || TYPE_EBOMMARKUP.equalsIgnoreCase(strMarkupType))
	                {
	                    int iResult = applyBOMMarkup(context,strMarkupObjId,strPartId,changeId);	
						if (iResult==1){
							stre = "false";
						}
	                }
	                // Add for Item and Plant BOM and Item Markup
	                else if(TYPE_ITEMMARKUP.equalsIgnoreCase(strMarkupType))
	                {
	                    applyingItemMarkup(context,strMarkupObjId,strPartId,changeId);
	                }
	                // Plant Item Markup
	                else if(TYPE_PLANT_ITEM_MARKUP.equalsIgnoreCase(strMarkupType))
	                {
	                     applyPlantItemMarkup(context,strMarkupObjId, strPartId, changeId);
	                }
	                // Add for Plant BOM Markup
	                else if (TYPE_PLANT_BOM_MARKUP.equalsIgnoreCase(strMarkupType))
	                {
	                    applyPlantBOMMarkup(context,strMarkupObjId, strPartId, changeId);
	                }
	                if (!TYPE_EBOMMARKUP.equalsIgnoreCase(strMarkupType))
	                {
                        doMarkup.setAttributeValue(context,ATTRIBUTE_BRANCH_TO,"None");
					}
	                
	                context.setCustomData("fromMarkupActions", "TRUE");
	                doMarkup.promote(context);
	                context.removeFromCustomData("fromMarkupActions");
	                
                 // End of itrMarkupIds while Loop
	             }
                 
             // End of Part Iterator While Loop Ends
             }

		 }
		 catch (Exception excep)
		 {
			 throw excep;
		 }
		 finally
		 {
			 ContextUtil.popContext(context);
		 }

         if("true".equalsIgnoreCase(stre))
         {
             return 0;
         }
         else
         {
             return 1;
         }
      }

      /**
       * Method to check the validations on Find Number, Ref Des for uniqueness
       * Added for the Bug#362472
       * @param context
       * @param requestMap
       * @return int
       * @throws Exception
       */
    public static int checkForUniqueness(Context context, Element eleRoot, List fnVals, List refDesVals)
              throws Exception {

        // Getting the uniqueness settings from the prop files
        String fnUniqueness = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.FindNumberUnique");
        String rdUniqueness = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReferenceDesignatorUnique");
        Element eleRelatioship = null;
        Element eleRelatioshipInfo = null;
        Element eleAttribute = null;
        java.util.List listFromRelationships = null;
        java.util.List listFromAttributes = null;
        Iterator itrRelationships = null;
        Iterator itrAttributes = null;
        com.matrixone.jdom.Attribute attrEBOMChgType = null;
        String strPartFN = "", tempFN = "";
        String strPartRD = "", tempRD = "";

        String strEBOMChgType = null;
        listFromRelationships = eleRoot.getChild("businessObject").getChild("fromRelationshipList").getChildren();
        itrRelationships = listFromRelationships.iterator();

        while (itrRelationships.hasNext()) {
            eleRelatioship = (Element) itrRelationships.next();
            eleRelatioshipInfo = eleRelatioship.getChild("attributeList");
            attrEBOMChgType = eleRelatioship.getAttribute("chgtype");
            listFromAttributes = eleRelatioshipInfo.getChildren();
            itrAttributes = listFromAttributes.iterator();
            strEBOMChgType = null;
            if (attrEBOMChgType != null) {
                strEBOMChgType = attrEBOMChgType.getValue();
            }
            // Storing the values of FN and RD in the markups
            //IR-024766 V6R2011 - start
            if ("A".equals(strEBOMChgType) || "C".equals(strEBOMChgType) || "D".equals(strEBOMChgType)) {
            //IR-024766 V6R2011 - end
                while (itrAttributes.hasNext()) {
                    eleAttribute = (Element) itrAttributes.next();
                    com.matrixone.jdom.Attribute changeAttr = eleAttribute
                            .getAttribute("chgtype");
                    String change = "";
                    if (changeAttr != null) {
                        change = changeAttr.getValue();
                    }
                    //376977-Starts
					strPartFN = "";
					strPartRD = "";
                    //376977-Ends
                    if ("Find Number".equals(eleAttribute.getChild("name").getText())) {
                        if ("C".equals(change)) {
                            strPartFN = eleAttribute.getChild("newvalue").getText();
                            tempFN = eleAttribute.getChild("oldvalue").getText();
                            if(fnVals.indexOf(tempFN) != -1) {
                                fnVals.remove(fnVals.indexOf(tempFN));
                                fnVals.add(strPartFN);
                            }
						} else if ("A".equals(change)) {
                            strPartFN = eleAttribute.getChild("string").getText();
                            fnVals.add(strPartFN);
                        } else if ("D".equals(change)) {
                            strPartFN = eleAttribute.getChild("string").getText();
                            fnVals.remove(strPartFN);
                        }
                    } else if ("Reference Designator".equals(eleAttribute.getChild("name").getText())) {
                        if ("C".equals(change)) {
                            strPartRD = eleAttribute.getChild("newvalue").getText();
                            tempRD = eleAttribute.getChild("oldvalue").getText();
                            if(refDesVals.indexOf(tempRD) != -1) {
                                refDesVals.remove(refDesVals.indexOf(tempRD));
                                refDesVals.add(strPartRD);
                            }
						} else if ("A".equals(change)) {
							strPartRD = eleAttribute.getChild("string").getText();
                            refDesVals.add(strPartRD);
                        } else if ("D".equals(change)) {
                            strPartRD = eleAttribute.getChild("string").getText();
                            refDesVals.remove(strPartRD);
                        }
                    }
                }
            }
        }

        // Checking for the duplicates in the FN
        if ("true".equals(fnUniqueness)) {
            //Removing empty strings from List
            Iterator fnItr = fnVals.iterator();
            while(fnItr.hasNext()) {
                tempFN = (String)fnItr.next();
                if(tempFN == null || "".equals(tempFN.trim()))
                    fnItr.remove();
            }

            //check for duplicates
            Set fnValSet = new HashSet(fnVals);
            if(fnVals.size() != fnValSet.size()) {
                return 1;
            }
        }

        //Checking for the duplicates in the RD
        if ("true".equals(rdUniqueness)) {
            //Removing empty strings from List
            Iterator rdItr = refDesVals.iterator();
            while(rdItr.hasNext()) {
                tempRD = (String)rdItr.next();
                if(tempRD == null || "".equals(tempRD.trim()))
                    rdItr.remove();
            }

            //check for duplicates
            Set rdValSet = new HashSet(refDesVals);
            if(refDesVals.size() != rdValSet.size()) {
                return 2;
            }
        }
        return 0;
    }

      /**
       * Apply Markup to the BOM Structure
       * @param    context the eMatrix <code>Context</code> object
       * @param    args holds a StringList containing the following entries:
       * paramMap - a StringList containing all selected Markup Ids
       * @return   void
       * @throws   Exception if operation fails
       * @since   EngineeringCentral X3
       */
      public static int applyBOMMarkup(Context context,String strMarkupId,String strPartId, String strChangeId) throws Exception
      {
          DomainObject doMarkup=new DomainObject(strMarkupId);

          // For auto collaboration
          IPartCollaborationService iPartCollabService = new PartCollaborationService();
          
         String  strMarkupName=doMarkup.getInfo(context,DomainConstants.SELECT_NAME);

          DomainObject doParentPart = new DomainObject(strPartId);

          //362472 Starts
          //Modified for IR-134046 start
          String fnUniqueness = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.FindNumberUnique");
          String rdUniqueness = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReferenceDesignatorUnique");
          //Modified for IR-134046 end

        //Multitenant
          String strFNMsg =EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.FindNumber.Unique");
          strFNMsg = strFNMsg+strMarkupName;
          //Multitenant
          String strRDMsg = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.ReferenceDesignator.Unique");
          strRDMsg = strRDMsg+strMarkupName;
          String attrFindNumber = DomainConstants.ATTRIBUTE_FIND_NUMBER;
          String attrRefDesignator = DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR;
          StringList fnList = null;
          StringList rdList = null;
          if("true".equals(fnUniqueness)) {
              fnList = doParentPart.getInfoList(context, "from["+ DomainConstants.RELATIONSHIP_EBOM+"].attribute["+attrFindNumber+"].value" );
              if(fnList == null) {
                  fnList = new StringList();
              }
          }
          if("true".equals(rdUniqueness)) {
              rdList = doParentPart.getInfoList(context, "from["+ DomainConstants.RELATIONSHIP_EBOM+"].attribute["+attrRefDesignator+"].value" );
              if(fnList == null) {
                  fnList = new StringList();
              }
          }

          String strParentState = doParentPart.getInfo(context,DomainObject.SELECT_CURRENT);

          //362472 Ends
          String XMLFORMAT = PropertyUtil.getSchemaProperty(context, "format_XML");

          String strEBOMRelType = DomainConstants.RELATIONSHIP_EBOM;
          String strSourceAtt = PropertyUtil.getSchemaProperty(context,"attribute_Source");   //IR-076128V6R2012 fix
         String strisVPMVisible =  PropertyUtil.getSchemaProperty(context,"attribute_isVPMVisible");
          com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder();
		  builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		  builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
		  builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
          String strTransPath = context.createWorkspace();

          java.io.File fEmatrixWebRoot = new java.io.File(strTransPath);

          String strXMLFileName = strMarkupName + ".xml";

          doMarkup.checkoutFile(context, false, XMLFORMAT, strXMLFileName, strTransPath);
          java.io.File fMarkupXML = new java.io.File(fEmatrixWebRoot, strXMLFileName);

          com.matrixone.jdom.Document docXML = builder.build(fMarkupXML);
          Element eleRoot = docXML.getRootElement();

          //362472 Starts
          int flag;
          if ("true".equals(fnUniqueness) || "true".equals(rdUniqueness)) {
              flag = checkForUniqueness(context, eleRoot, fnList, rdList);
              if(flag==1){
                  emxContextUtil_mxJPO.mqlNotice(context,strFNMsg);
                  return 1;
              }else if(flag==2){
                  emxContextUtil_mxJPO.mqlNotice(context,strRDMsg);
                  return 1;
              }
          }
          //362472 Ends
          List listFromRelationships = eleRoot.getChild("businessObject").getChild("fromRelationshipList").getChildren();
          Iterator itrRelationships = listFromRelationships.iterator();

          Element eleRelatioship = null;
          Element eleObjectInfo = null;
          Element eleRelatioshipInfo = null;
          Element eleAttribute = null;
          //Start - Added for EBOM Substitute Apply
          Element eleRelationshipRefDef = null;
          boolean blnIsEBOMSub = false;
          String strXMLEBOMRelId = null;
          String strEBOMSubstituteRel = PropertyUtil.getSchemaProperty(context,"relationship_EBOMSubstitute");
          //End - Added for EBOM Substitute Apply
          com.matrixone.jdom.Attribute attrEBOMChgType = null;
          com.matrixone.jdom.Attribute attrChgType = null;

          String strPartType = null;
          String strPartName = null;
          String strPartRev = null;
          String strPartVault = null;
          String strEBOMChgType = null;
          String strEBOMFN = null;
          String strEBOMRD = null;
          String strAttrValue = null;
          String strAttrName = null;
          String replaceAction = "";

          List listFromAttributes = null;
          Iterator itrAttributes = null;

          StringList strlPartSelects = new StringList(1);
          strlPartSelects.addElement(DomainConstants.SELECT_ID);

          StringList strlEBOMRelSelects = new StringList(1);
          strlEBOMRelSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

          HashMap hmpAttributes = null;

          //Added for mainstaining substitutes on replace action
          Element eleMaintainSubstitute = null;

          while (itrRelationships.hasNext())
          {

              eleRelatioship = (Element) itrRelationships.next();
              eleObjectInfo = eleRelatioship.getChild("relatedObject").getChild("businessObjectRef");
              //Start - Added for EBOM Substitute Apply
              eleRelationshipRefDef = eleRelatioship.getChild("relationshipDefRef");
              blnIsEBOMSub = eleRelationshipRefDef.getText().equals(strEBOMSubstituteRel)?true:false;
              //End - Added for EBOM Substitute Apply
              eleRelatioshipInfo = eleRelatioship.getChild("attributeList");

              //IR-045004: Decoding the encoded type, name,rev,vault
			 strPartType= com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectType").getText());
             if (null !=eleObjectInfo.getChild("objectNameEncoded")){
                    strPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectNameEncoded").getText());
                }
             else{
			 strPartName= com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectName").getText());
              }
			 strPartRev= com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectRevision").getText());
			 strPartVault= com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("vaultRef").getText());

              attrEBOMChgType = eleRelatioship.getAttribute("chgtype");
              replaceAction   = eleRelatioship.getAttributeValue("replace");
              listFromAttributes = eleRelatioshipInfo.getChildren();
              itrAttributes = listFromAttributes.iterator();

              strEBOMChgType = null;

              if (attrEBOMChgType != null )
              {
                  strEBOMChgType = attrEBOMChgType.getValue();
              }

			  //Start: Added for mainstaining substitutes on replace action
			  eleMaintainSubstitute = eleRelatioship.getChild("relatedObject").getChild("maintainSub");
			  String strSubstitute = null;
			  if(eleMaintainSubstitute!=null)
			  {
				strSubstitute = eleMaintainSubstitute.getText();
			  }
                          //End:Added for mainstaining substitutes on replace action

              hmpAttributes = new HashMap();

              if ("A".equals(strEBOMChgType))
              {
                  DomainObject doChild = new DomainObject(new BusinessObject(strPartType,strPartName,strPartRev,strPartVault));

                   String strCurrent 		= doChild.getInfo(context, DomainObject.SELECT_CURRENT);
                   BusinessObject boLastRev = doChild.getLastRevision(context);

                   DomainObject doLastRev   = new DomainObject(boLastRev);
                   String strLastRevCurrent = doLastRev.getInfo(context, DomainObject.SELECT_CURRENT);
                   //HF-050350 Starts
 
                   if(!doChild.isLastRevision(context)){                	   
                	   if(EBOMFloat.isManualFloatBehaviorEnabled(context)) { // should consider latest revision only if float trigger is active which means no manual float behavior
                		   doLastRev = doChild; // always consider current object as last revision if the auto float is not enabled.
                	   }
                	   else {
	                       String strPolicy = doChild.getInfo(context, DomainObject.SELECT_POLICY);
	                       String STATE_RELEASE =  PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Release");
	                       String STATE_OBSOLETE =  PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Obsolete");
	                       String strWhereClause = "(current == \"" + STATE_RELEASE + "\") && (!((next.current == \"" + STATE_RELEASE + "\") || (next.current == \"" + STATE_OBSOLETE + "\")))";
	                       StringList strlSelects = new StringList();
	                       strlSelects.add(SELECT_ID);
	                       MapList mapListLatestReleasedParts =  DomainObject.findObjects(context,
	                              (String) doChild.getInfo(context, DomainObject.SELECT_TYPE),
	                              (String) doChild.getInfo(context, DomainObject.SELECT_NAME),
	                              "*",
	                              null,
	                              (String) doChild.getInfo(context, DomainObject.SELECT_VAULT),
	                              strWhereClause,
	                              false,
	                              strlSelects);
	                       String strId="";
	                       if (mapListLatestReleasedParts.size() > 0)
	                       {
	                        Map mapPart = (Map) mapListLatestReleasedParts.get(0);
	                        strId = (String) mapPart.get(SELECT_ID);
	
	                       }
	                       //set the latest released object id
	                       doChild.setId(strId);
                	   } 
                   }
                  //HF-050350 Ends
                   if (strChangeId != null)
                   {

				   if(strCurrent.equals(DomainObject.STATE_PART_OBSOLETE)){
					   if(strLastRevCurrent.equals(DomainObject.STATE_PART_PRELIMINARY) || strLastRevCurrent.equals(DomainObject.STATE_PART_REVIEW) || strLastRevCurrent.equals(DomainObject.STATE_PART_APPROVED)){
						   String [] mailArguments = new String [12];
						   mailArguments[0] = "emxEngineeringCentral.Alert.ObjectState";
						   mailArguments[1] = "4";
						   mailArguments[2] = "strObjType";
						   mailArguments[3] = doLastRev.getInfo(context, DomainObject.SELECT_TYPE);
						   mailArguments[4] = "strObjName";
						   mailArguments[5] = doLastRev.getInfo(context, DomainObject.SELECT_NAME);
						   mailArguments[6] = "strObjRev";
						   mailArguments[7] = doLastRev.getInfo(context, DomainObject.SELECT_REVISION);
						   mailArguments[8] = "strLastRevCurrent";
						   mailArguments[9] = strLastRevCurrent;
						   mailArguments[11] = "emxEngineeringCentralStringResource";
						   String strMessage = (String)JPO.invoke(context, "emxMailUtil", new String[]{}, "getMessage", mailArguments, String.class);
						   emxContextUtil_mxJPO.mqlNotice(context,strMessage);
						return 1;
					   }
				   } else {
						//Modified if condition to fix 	IR-065650V6R2011x
                       if((!(strParentState.equals(DomainObject.STATE_PART_REVIEW)) && !(strParentState.equals(DomainObject.STATE_PART_APPROVED)))||(!(strCurrent.equals(DomainObject.STATE_PART_REVIEW))&&!(strCurrent.equals(DomainObject.STATE_PART_APPROVED))&& !(strCurrent.equals(DomainObject.STATE_PART_RELEASE)))){
							String [] mailArguments = new String [12];
							mailArguments[0] = "emxEngineeringCentral.Alert.ObjectState";
							mailArguments[1] = "4";
							mailArguments[2] = "strObjType";
							mailArguments[3] = doChild.getInfo(context, DomainObject.SELECT_TYPE);
							mailArguments[4] = "strObjName";
							mailArguments[5] = doChild.getInfo(context, DomainObject.SELECT_NAME);
							mailArguments[6] = "strObjRev";
							mailArguments[7] = doChild.getInfo(context, DomainObject.SELECT_REVISION);
							mailArguments[8] = "strLastRevCurrent";
							mailArguments[9] = strLastRevCurrent;
							mailArguments[11] = "emxEngineeringCentralStringResource";
//                          HF-056391 Starts
                            String strPolicy = doChild.getInfo(context, DomainObject.SELECT_POLICY);
                            if(!DomainConstants.POLICY_DEVELOPMENT_PART.equals(strPolicy)){
							String strMessage = (String)JPO.invoke(context, "emxMailUtil", new String[]{}, "getMessage", mailArguments, String.class);
							emxContextUtil_mxJPO.mqlNotice(context,strMessage);
						return 1;
                            }
					   }
				   }
				   }

                  while (itrAttributes.hasNext())
                  {
                      eleAttribute = (Element) itrAttributes.next();

                      strAttrName = eleAttribute.getChild("name").getText();

                      if (DomainConstants.ATTRIBUTE_FIND_NUMBER.equals(strAttrName))
                      {
                          strAttrValue = eleAttribute.getChild("string").getText();

                      }
                      else if (DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR.equals(strAttrName))
                      {
                          strAttrValue = eleAttribute.getChild("string").getText();
                      }
                      else if (DomainConstants.ATTRIBUTE_QUANTITY.equals(strAttrName))
                      {
                          strAttrValue = eleAttribute.getChild("real").getText();
                      }
                      else if (DomainConstants.ATTRIBUTE_COMPONENT_LOCATION.equals(strAttrName))
                      {
                          strAttrValue = eleAttribute.getChild("string").getText();
                      }
                    //UOM Management - start
                      else if (EngineeringConstants.UOM.equals(strAttrName))
                      {
                    	  strAttrValue = eleAttribute.getChild("string").getText();
                      }
                     //UOM Management - end
                      else if (DomainConstants.ATTRIBUTE_USAGE.equals(strAttrName))
                      {
                          strAttrValue = eleAttribute.getChild("string").getText();
                          if ("".equals(strAttrValue))
                          {
                            continue;
                          }

                      }
                      else if (DomainConstants.ATTRIBUTE_NOTES.equals(strAttrName))
                      {
                          strAttrValue = eleAttribute.getChild("string").getText();
                      }
                      else if (strSourceAtt.equals(strAttrName))  // //IR-076128V6R2012 fix  starts
                      {
                          strAttrValue = eleAttribute.getChild("string").getText();
                      }
					  //IR-076128V6R2012 fix  ends
                      else if (strisVPMVisible != null && strisVPMVisible.equals(strAttrName))
                      {
                    	  strAttrValue = eleAttribute.getChild("string").getText();
                      }
                      
                      if(EngineeringConstants.UOM.equals(strAttrName)) { 
                    	  strAttrValue = strAttrValue.trim();
                    	  if(UIUtil.isNotNullAndNotEmpty(strAttrValue)) {
                    		  hmpAttributes.put(DomainConstants.ATTRIBUTE_UNIT_OF_MEASURE, strAttrValue);
                    	  }
                      } 
                      else
                    	  hmpAttributes.put(strAttrName, strAttrValue);
                  }
                      // Confirm Use  of this
                       if (strCurrent.equals(DomainObject.STATE_PART_OBSOLETE) && strLastRevCurrent.equals(DomainObject.STATE_PART_RELEASE))
                  {
                      //Start - Added for EBOM Substitute Apply
                      if(blnIsEBOMSub) {
                          strXMLEBOMRelId = eleRelatioship.getChild("relatedObject").getAttributeValue("ebomId");
                          RelToRelUtil relBOMSubstitute = new RelToRelUtil();
                          String strNewRel = relBOMSubstitute.connect(context,strEBOMSubstituteRel,strXMLEBOMRelId,doLastRev.getInfo(context,DomainConstants.SELECT_ID),false,true);
                             DomainRelationship doRelNew = new DomainRelationship(strNewRel);
                             doRelNew.setAttributeValues(context, hmpAttributes);
                      } else {
                    	  
                    	String parentId = new DomainObject(doParentPart).getObjectId(context);
                  		String strdoLastRev = new DomainObject(doLastRev).getObjectId(context);
                  		
                  		IBOMService iBOMService = IBOMService.getService(context, parentId, false);
                  		IBOMIngress iBOMIngress = IBOMIngress.getService();
                  		iBOMIngress.setChildId(strdoLastRev );
                      	iBOMIngress.setBOMAttributeMap(hmpAttributes);
                      	iBOMIngress.setBOMUI("Markup"); 
                  		
                      	/*Added for Auto Sync st*/
                		iBOMService.setCollaboratToDesign(true);
                		/*Added for Auto Sync en*/
                		
                      	iBOMService.add(context, iBOMIngress);
                      	StringList slInstanceIds =iBOMService.getInstanceIds();
                    	  
                      }
                      //End - Added for EBOM Substitute Apply
                  }
                  else
                  {
                      //Start - Added for EBOM Substitute Apply
                      if(blnIsEBOMSub) {
                          strXMLEBOMRelId = eleRelatioship.getChild("relatedObject").getAttributeValue("ebomId");
                          RelToRelUtil relBOMSubstitute = new RelToRelUtil();
                          String strNewRel = relBOMSubstitute.connect(context,strEBOMSubstituteRel,strXMLEBOMRelId,doChild.getInfo(context,DomainConstants.SELECT_ID),false,true);
                          DomainRelationship doRelNew = new DomainRelationship(strNewRel);
                          doRelNew.setAttributeValues(context, hmpAttributes);
                      } else {
                    	  
                    		String parentId = new DomainObject(doParentPart).getObjectId(context);
                    		String childId = new DomainObject(doChild).getObjectId(context);
                    		
                    		IBOMService iBOMService = IBOMService.getService(context, parentId, false);
                    		IBOMIngress iBOMIngress = IBOMIngress.getService();
                    		iBOMIngress.setChildId(childId );
                        	iBOMIngress.setBOMAttributeMap(hmpAttributes);
                        	iBOMIngress.setBOMUI("Markup"); 
                    		
                        	/*Added for Auto Sync st*/
                    		iBOMService.setCollaboratToDesign(true);
                    		/*Added for Auto Sync en*/
                    	
                        	iBOMService.add(context, iBOMIngress);
                        	StringList slInstanceIds =iBOMService.getInstanceIds();
                    
                        
                          //EBOM Auto sync starts
                        // DomainRelationship doRelNew = DomainRelationship.connect(context, doParentPart, strEBOMRelType, doChild);
//                     	  String existingRelID = eleRelatioship.getChild("relationshipDefRef").getAttributeValue("relid"); 
//                     	  //If this add operation is as part of replace , then rel id will be existed already and also replace must be true
//                     	  DomainRelationship doRelNew = null;
//                     	  if(!"new".equalsIgnoreCase(existingRelID) && "True".equalsIgnoreCase(replaceAction)) {
//                     		  DomainRelationship.setToObject(context, existingRelID, doChild);
//                     		  doRelNew = new DomainRelationship(existingRelID);
//                     	  }
//                     	  else {
//                     		  doRelNew = DomainRelationship.connect(context, doParentPart, strEBOMRelType, doChild);  
//                     	  }
                     	 //EBOM Auto sync - Ends                          
                       // doRelNew.setAttributeValues(context, hmpAttributes);

			      //Start :Added for maintain substitutes
                             // String strEBOMRel				= doRelNew.toString();

				if("Yes".equals(strSubstitute))
				{
					String strOrigEBOMId = eleRelatioship.getChild("relationshipDefRef").getAttributeValue("relId");
					//find substitutes from EBOM Id
					String strCommand			= "print connection $1 select $2 dump $3";
					String strMessage			= MqlUtil.mqlCommand(context,strCommand,strOrigEBOMId,"frommid["+strEBOMSubstituteRel+"].to.id","|");
					StringList substList		= new StringList();
					substList					= FrameworkUtil.split(strMessage,"|");
					Part part = (Part)DomainObject.newInstance(context,DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
					String primid = doChild.getInfo(context,DomainConstants.SELECT_ID);
					part.setId(primid);

					for(int l=0; l < substList.size(); l++)
					{
						String strSubsId = (String) substList.get(l);
						for(int k=0;k<=slInstanceIds.size();k++){
							String ebomRelid=(String) slInstanceIds.get(k);
							if( BOMMgtUtil.isNotNullAndNotEmpty(ebomRelid)){
								part.createSubstitutePart(context, strPartId, ebomRelid, strSubsId);
							}
							
						}
					}
				}
				   //End :Added for maintain substitutes
                      }
                      //End - Added for EBOM Substitute Apply
                  }
              }
              else if ("C".equals(strEBOMChgType))
              {
                  strEBOMFN = null;
                  strEBOMRD = null;

                  while (itrAttributes.hasNext())
                  {
                      eleAttribute = (Element) itrAttributes.next();
                      strAttrName = eleAttribute.getChild("name").getText();

                      strAttrValue = null;

                      attrChgType = eleAttribute.getAttribute("chgtype");

                      if (DomainConstants.ATTRIBUTE_FIND_NUMBER.equals(strAttrName))
                      {
                          if (attrChgType != null)
                          {
                              strEBOMFN = eleAttribute.getChild("oldvalue").getText();
                              strAttrValue = eleAttribute.getChild("newvalue").getText();
                          }
                          else
                          {
                              strEBOMFN = eleAttribute.getChild("string").getText();
                              continue;
                          }
                      }
                      else if (DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR.equals(strAttrName))
                      {
                          if (attrChgType != null)
                          {
                              strEBOMRD = eleAttribute.getChild("oldvalue").getText();
                              strAttrValue = eleAttribute.getChild("newvalue").getText();
                          }
                          else
                          {
                              strEBOMRD = eleAttribute.getChild("string").getText();
                              continue;
                          }
                      }
                      else if (DomainConstants.ATTRIBUTE_QUANTITY.equals(strAttrName))
                      {
                          if (attrChgType != null)
                          {
                              strAttrValue = eleAttribute.getChild("newvalue").getText();
                          }
                          if (strAttrValue == null || "".equals(strAttrValue))
                          {
                            continue;
                          }

                      }
                      else if (DomainConstants.ATTRIBUTE_COMPONENT_LOCATION.equals(strAttrName))
                      {
                          if (attrChgType != null)
                          {
                              strAttrValue = eleAttribute.getChild("newvalue").getText();
                          }
                          if (strAttrValue == null || "".equals(strAttrValue))
                          {
                            continue;
                          }
                      }
                      else if (DomainConstants.ATTRIBUTE_USAGE.equals(strAttrName))
                      {
                          if (attrChgType != null)
                          {
                              strAttrValue = eleAttribute.getChild("newvalue").getText();
                          }
                          if (strAttrValue == null || "".equals(strAttrValue))
                          {
                            continue;
                          }

                      }
                      else if (DomainConstants.ATTRIBUTE_NOTES.equals(strAttrName))
                      {
                          if (attrChgType != null)
                          {
                              strAttrValue = eleAttribute.getChild("newvalue").getText();
                          }
                          else
                          {
                              continue;
                          }
                      }
                      //UOM Management - start
                      else if (EngineeringConstants.UOM.equals(strAttrName))
                      {
                          if (attrChgType != null)
                          {
                              strAttrValue = eleAttribute.getChild("newvalue").getText();
                          }
                          else
                          {
                              continue;
                          }
                      }
                     //UOM Management - end

					  //IR-076128V6R2012 fix   starts

                      else if (strSourceAtt.equals(strAttrName))
                      {
                          if (attrChgType != null)
                          {
                              strAttrValue = eleAttribute.getChild("newvalue").getText();
                          }
                          else
                          {
                              continue;
                          }
                      }
					  //IR-076128V6R2012 fix  ends

                      else if (strisVPMVisible.equals(strAttrName))
                      {

                              continue;

                      }
                      if(EngineeringConstants.UOM.equals(strAttrName))
                    	  hmpAttributes.put(DomainConstants.ATTRIBUTE_UNIT_OF_MEASURE, strAttrValue);
                      else
                    	  hmpAttributes.put(strAttrName, strAttrValue);
                  }
                  //Start - Added for EBOM Substitute Mass change
                  if(blnIsEBOMSub) {
                        String strSubstituteRelId = eleRelationshipRefDef.getAttributeValue("substituteRelId");
                        DomainRelationship doRelChange = new DomainRelationship(strSubstituteRelId);
                        doRelChange.setAttributeValues(context, hmpAttributes);
                  } else {
                  //End - Added for EBOM Substitute Mass change
                	  String strObjWhereClause = null;
                	  if(!sameCAForParentChild){
                		  strObjWhereClause = "name==\"" + strPartName +"\" "+ "&&" +" "+"revision==\"" + strPartRev + "\"";
                	  }
                      String strRelWhereClause=null;

                      if(!"".equalsIgnoreCase(strEBOMFN))
                      {
                          if(!"".equalsIgnoreCase(strEBOMRD))
                          {

                              strRelWhereClause="attribute["+DomainConstants.ATTRIBUTE_FIND_NUMBER+"] == \"" + strEBOMFN + "\"  && attribute["+DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR+"] == \"" + strEBOMRD + "\"";
                          }
                          else
                          {
                              strRelWhereClause="attribute["+DomainConstants.ATTRIBUTE_FIND_NUMBER+"] == \"" + strEBOMFN + "\"";
                          }
                      }
                      else
                      {
                          if(!"".equalsIgnoreCase(strEBOMRD))
                          {
                              strRelWhereClause="attribute["+DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR+"] == \"" + strEBOMRD + "\"";

                          }
                          else
                          {
                                  strRelWhereClause=null;
                          }
                      }

                      MapList mapListBOMs = doParentPart.getRelatedObjects(context,
                                              DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
                                              DomainConstants.TYPE_PART,          // object pattern
                                              strlPartSelects,                 // object selects
                                              strlEBOMRelSelects,              // relationship selects
                                              false,                        // to direction
                                              true,                       // from direction
                                              (short)1,                    // recursion level
                                              strObjWhereClause,                        // object where clause
                                              strRelWhereClause,
                                              0); //limit 0 to return all the data available.


                      Iterator itrmapListBOMs=mapListBOMs.iterator();


                      if (mapListBOMs.size() > 0)
                      {
                          while(itrmapListBOMs.hasNext())
                          {
                          Map mapEBOM = (Map) itrmapListBOMs.next();
                          String strEBOMRelId = (String) mapEBOM.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                          
                          DomainRelationship doRelChange = new DomainRelationship(strEBOMRelId);
                          doRelChange.setAttributeValues(context, hmpAttributes);
                          
                          /* Auto Collaboration st
                           * 
                           * This is a temp implementation without using the service. 
                           * Once the Markup will be converged to EBOM Service, this code will be refactored  
                           * */
                          
                          IBOMCollaborationService iCollabService = new BOMCollaborationService();
                                                    
                           if (iCollabService != null) {
                        	   List ebomRelId = new StringList();
                        	   ebomRelId.add(strEBOMRelId);
                        	   iCollabService.modify(context, ebomRelId  ,"");
                        	                         	   
                           }
                     
                          /* Auto Collaboration st*/
                          
                          }
                      }
                   //Added for EBOM Substitute Mass Change
                   }
              }
              else if ("D".equals(strEBOMChgType))
              {
                  
            	  //if("True".equalsIgnoreCase(replaceAction)) { continue;} //Added for auto sync to by pass cut operation in case of replace
            	  
            	  strEBOMFN = null;
                  strEBOMRD = null;


                  while (itrAttributes.hasNext())
                  {
                      eleAttribute = (Element) itrAttributes.next();
                      strAttrName = eleAttribute.getChild("name").getText();

                      attrChgType = eleAttribute.getAttribute("chgtype");


                      if (DomainConstants.ATTRIBUTE_FIND_NUMBER.equals(strAttrName))
                      {
                          strEBOMFN = eleAttribute.getChild("string").getText();

                      }
                      else if (DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR.equals(strAttrName))
                      {
                          strEBOMRD = eleAttribute.getChild("string").getText();

                      }
                  }
                  //Start - Added for EBOM Substitute Apply
                  if(blnIsEBOMSub) {
                        String strSubstituteRelId = eleRelationshipRefDef.getAttributeValue("substituteRelId");
                        RelToRelUtil.disconnect(context,strSubstituteRelId);
                  } else {
                  //End - Added for EBOM Substitute Apply
                	  String strObjWhereClause = null;
                	  if(!sameCAForParentChild) {
                		  strObjWhereClause = "name==\"" + strPartName +"\" "+ "&&" +" "+"revision==\"" + strPartRev + "\"";
                	  }
                       
                      String strRelWhereClause = null;

                      if(!"".equalsIgnoreCase(strEBOMFN))
                      {
                          if(!"".equalsIgnoreCase(strEBOMRD))
                          {

                              strRelWhereClause="attribute["+DomainConstants.ATTRIBUTE_FIND_NUMBER+"] == \"" + strEBOMFN + "\"  && attribute["+DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR+"] == \"" + strEBOMRD + "\"";
                          }
                          else
                          {
                              strRelWhereClause="attribute["+DomainConstants.ATTRIBUTE_FIND_NUMBER+"] == \"" + strEBOMFN + "\"";
                          }

                      }
                      else
                      {
                          if(!"".equalsIgnoreCase(strEBOMRD))
                          {
                              strRelWhereClause="attribute["+DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR+"] == \"" + strEBOMRD + "\"";

                          }
                          else
                          {
                                  strRelWhereClause=null;
                          }
                      }


                      MapList mapListBOMs = doParentPart.getRelatedObjects(context,
                                              DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
                                              DomainConstants.TYPE_PART,          // object pattern
                                              strlPartSelects,                 // object selects
                                              strlEBOMRelSelects,              // relationship selects
                                              false,                        // to direction
                                              true,                       // from direction
                                              (short)1,                    // recursion level
                                              strObjWhereClause,                        // object where clause
                                              strRelWhereClause,
                                              0); //limit 0 to return all the data available.



                      Iterator itrMapllistBOMS=mapListBOMs.iterator();

                      if (mapListBOMs.size() > 0)
                      {
                          while(itrMapllistBOMS.hasNext())
                          {

                          Map mapEBOM = (Map)itrMapllistBOMS.next();
                          String strEBOMRelId = (String) mapEBOM.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                          
                          ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
                          
                          /*Auto Collab st  */
              				
              			    IBOMCollaborationService iCollabService = new BOMCollaborationService();
                         	   List ebomRelId = new StringList();
              				ebomRelId.add(strEBOMRelId);
              			  
               	   			// To store the mirrored instance id for EBOM id
               	   			iCollabService.retrieveInstanceIdForBom(context, ebomRelId);
                          
               	   			// Remove the EBOM relationship
               	   			DomainRelationship.disconnect(context, strEBOMRelId);  
                          
               	   			// Invoke collaboration removal
                            iCollabService.remove(context, ebomRelId, "");
                          
                            ContextUtil.popContext(context);
                          
                          /* Auto Collaboration en */
                          
                          // End of While Loop
                          }
                       // End of If Loop
                       }
                   //Start - Added for EBOM Substitute Apply
                   }
                   //End - Added for EBOM Substitute Apply

              // End for Change Operation 'D'
              }

             // End of itrRelationships
          }

			String strAppliedPartMarkupRel = PropertyUtil.getSchemaProperty(context,"relationship_AppliedPartMarkup");

			DomainRelationship.connect(context, doParentPart, strAppliedPartMarkupRel, doMarkup);

		return 0;
      }

    /*
     * This method used show ECR field to select in Item markup creation webform
     * @param context
     * @return Boolean value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */

    public Boolean showECR(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);

        String sAction = (String) programMap.get("activity");
        String sParentOID = (String) programMap.get("parentOID");
        String sObjectId = (String) programMap.get("objectId");
        String relId = (String) programMap.get("relId");

        boolean boolValueReturn = false;

        if ("create".equals(sAction)) {
        	if (relId != null && !"".equals(relId)) {
        		StringList objSelect = new StringList(2);
            	objSelect.add("from.type");
            	objSelect.add("from.current");
            	objSelect.add("from.id");

            	MapList dataList = DomainRelationship.getInfo(context, new String[] {relId}, objSelect);

            	Map dataMap = (Map) dataList.get(0);

            	String strChangeType = (String) dataMap.get("from.type");
            	String strChangeCurrent = (String) dataMap.get("from.current");


            	if (!(DomainConstants.TYPE_ECR.equals(strChangeType) || DomainConstants.TYPE_ECO.equals(strChangeType))) {
            		boolValueReturn = true;
            	} else if (DomainConstants.TYPE_ECO.equals(strChangeType)) {
            		boolValueReturn = (!("Create".equals(strChangeCurrent) ||
            				"Define Components".equals(strChangeCurrent) || "Design Work".equals(strChangeCurrent)));
            	} else if (DomainConstants.TYPE_ECR.equals(strChangeType)) {
            		String strChangeID = (String) dataMap.get("from.id");
            		DomainObject doChange  =  new DomainObject(strChangeID);
            		String strFromId = (String)doChange.getInfo(context,DomainObject.SELECT_MANUFACTURING_RESPONSIBILITY_FROM_ID);

            		if(strFromId!=null && !strFromId.equals(""))
            		{
            			boolValueReturn = true;
            		}
            		else
            		{
            			boolValueReturn = (!("Create".equals(strChangeCurrent)
            					|| "Submit".equals(strChangeCurrent) || "Evaluate".equals(strChangeCurrent)));
            		}
            	}
        	} else if (sParentOID != null && sParentOID.equals(sObjectId)) {
        		boolValueReturn = true;
        	} else {
        		DomainObject doObj = DomainObject.newInstance(context, sParentOID);
        		String strType = doObj.getInfo(context, DomainConstants.SELECT_TYPE);
        		if (!(strType.equals(DomainConstants.TYPE_ECR) || strType.equals(DomainConstants.TYPE_ECO))) {
        			boolValueReturn = true;
        		}
        	}
        }

        return Boolean.valueOf(boolValueReturn);
    }

    /** Checks the Part current state if Release then both ECO and ECR are displayed to select, If Part is not Released then only ECO is displayed in ItemMarkup to select the change.
     * @param context ematrix context
     * @param args contains Program map
     * @return String
     * @throws Exception if any operation fails.
     */
    public String getChangeDynamicSearchQuery(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");

        String parentOID = (String) requestMap.get("parentOID");
        String rangeHref = "TYPES=type_ECR,type_ECO:POLICY=policy_ECR,policy_ECO:CURRENT=policy_ECR.state_Create,policy_ECR.state_Submit,policy_ECR.state_Evaluate,policy_ECO.state_Create,policy_ECO.state_DefineComponents,policy_ECO.state_DesignWork";

        if (parentOID != null && !"".equals(parentOID)) {
	        DomainObject domObj = DomainObject.newInstance(context, parentOID);

	        String strCurrent = domObj.getInfo(context, DomainConstants.SELECT_CURRENT);
	        if (DomainConstants.STATE_PART_RELEASE.equals(strCurrent)) {
	        	rangeHref = "TYPES=type_ECR,type_ECO:POLICY=policy_ECR,policy_ECO:CURRENT=policy_ECR.state_Create,policy_ECR.state_Submit,policy_ECR.state_Evaluate,policy_ECO.state_Create,policy_ECO.state_DefineComponents";
	        } else {
	        	rangeHref = "TYPES=type_ECO:POLICY=policy_ECO:CURRENT=policy_ECO.state_Create,policy_ECO.state_DefineComponents,policy_ECO.state_DesignWork";
	        }
        }

        return rangeHref;
    }

    /*
     * This method used show ECR non editable field in Item markup edit webform
     * @param context
     * @return Boolean value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    public Boolean showECREdit(Context context, String[] args) throws Exception
    {

        return Boolean.valueOf(!showECR(context,args).booleanValue());
    }

    /*
     * This method used create ECR field in Item markup creation and edit webform
     * @param context
     * @return String value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */

    public String getECREdit(Context context, String[] args) throws Exception
    {
        // Unpack arguments.
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        String relId = (String) requestMap.get("relId");
        String sReturn = "";
        if(relId==null)
         fileContent = getItemMarkupXMLContent(context,objectId);

         String sECRId = null;

        if(relId!=null)
        {
        	sECRId = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"from.id");
        }

        DomainObject doChange = null;

        if(!"".equals(sECRId) && sECRId!=null)
        {
            doChange = new DomainObject(sECRId);
        }
        else
        {
        	//Modified for ENG Convergence start
            StringList objectSelects = new StringList(3);				 	
    		objectSelects.addElement("to["+RELATIONSHIP_APPLIED_MARKUP+"].from.to["+RELATIONSHIP_CHANGE_ACTION+"].from.id"); 
    		objectSelects.addElement("to["+RELATIONSHIP_PROPOSED_MARKUP+"].from.id");
    		objectSelects.addElement("to["+RELATIONSHIP_APPLIED_MARKUP+"].from.id");
    		    		    	    	
    		DomainObject dPartObj = new DomainObject(objectId);    		
    		Map mMarkupInfo = dPartObj.getInfo(context, objectSelects);    		   		    		    				
			String id = (String) mMarkupInfo.get("to["+RELATIONSHIP_APPLIED_MARKUP+"].from.to["+RELATIONSHIP_CHANGE_ACTION+"].from.id");
			
			if(UIUtil.isNullOrEmpty(id)) {
				id = (String) mMarkupInfo.get("to["+RELATIONSHIP_PROPOSED_MARKUP+"].from.id");
			}
			
			if(UIUtil.isNullOrEmpty(id)) {
				id = (String) mMarkupInfo.get("to["+RELATIONSHIP_APPLIED_MARKUP+"].from.id");
			}
			
			 doChange = new DomainObject(id);
			//Modified for ENG Convergence End
        }

        //Added for ENG Convergence start                      
        if(doChange.isKindOf(context,ChangeConstants.TYPE_CHANGE_ACTION)) {
        	String strCRId = doChange.getInfo(context,"to["+RELATIONSHIP_CHANGE_ACTION+"].from.id");        	
        	String strCRName = doChange.getInfo(context,"to["+RELATIONSHIP_CHANGE_ACTION+"].from.name");        	
        	
        	 sReturn += strCRName;
             sReturn += "<input type=hidden name='ChangeDisplay' value='"+XSSUtil.encodeForHTMLAttribute(context, strCRName)+"'>";
             sReturn += "<input type=hidden name='ChangeOID' value='"+XSSUtil.encodeForHTMLAttribute(context,strCRId)+"'>";
        } else {              
	        sReturn += doChange.getInfo(context,"name");
	        sReturn += "<input type=hidden name='ChangeDisplay' value='"+XSSUtil.encodeForHTMLAttribute(context,doChange.getInfo(context,"name"))+"'>";
	        sReturn += "<input type=hidden name='ChangeOID' value='"+XSSUtil.encodeForHTMLAttribute(context,doChange.getInfo(context,"id"))+"'>";
        }
        //Modified for ENG Convergence End
        
        return sReturn;
    }

    /*
     * This method used to get vault.
     * @param context
     * @return String value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */

    public String getVault(Context context, String[] args) throws Exception
    {
        // Unpack arguments.
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        DomainObject doPart = new DomainObject(objectId);
        // fix for 091260 starts
        String strVault = doPart.getInfo(context,"vault");
		String languageStr = (String) requestMap.get("languageStr");
		if(null!=strVault && null!=languageStr)
			strVault = i18nNow.getAdminI18NString("Vault", strVault, languageStr);
		return strVault;
		// fix for 091260 ends
    }

    /*
     * This method used to check the access for creating item markup
     * @param context, args
     * @return Boolean value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    public Boolean showItemMarkupCreateCommand(Context context, String[] args) throws Exception
    {
        boolean bReturn = true;
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap sSettings = (HashMap)programMap.get("SETTINGS");
        String sSymType = (String) sSettings.get("Type");
        if(sSymType == null){
            sSymType = "type_ItemMarkup";
        }
        String sType = PropertyUtil.getSchemaProperty(context, sSymType);
        String objectId = (String) programMap.get("objectId");

        DomainObject doPart = new DomainObject(objectId);
        if(!doPart.getInfo(context,"policy").equals(DomainConstants.POLICY_EC_PART))
         return Boolean.FALSE;

        String sStatus = doPart.getInfo(context,DomainConstants.SELECT_CURRENT);

        String mqlResult = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump ",objectId,"first");

		String sRelPartVersion = PropertyUtil.getSchemaProperty(context,"relationship_PartVersion");

		//Multitenant
		String isPartVersion=EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.Check.PartVersion");

        if(doPart.getRevision().equals(mqlResult.substring(0,mqlResult.length()-1))) {  //Checking for number of revisions.
			if((doPart.getInfo(context,DomainConstants.SELECT_CURRENT)).equals(DomainConstants.STATE_PART_RELEASE))
				bReturn = true;
			else
{
            bReturn = false;
		       return Boolean.valueOf(bReturn);
			}
		  }

	   if("TRUE".equalsIgnoreCase(isPartVersion)) // Checking for versions.
		{
		   Map map  = doPart.getRelatedObject(context,sRelPartVersion,false,DomainConstants.EMPTY_STRINGLIST,DomainConstants.EMPTY_STRINGLIST);
		  if (map==null && !doPart.getRevision().equals(mqlResult.substring(0,mqlResult.length()-1))  )
		  {
				bReturn = false;
		  }
		}

        if(sStatus.equals(DomainConstants.STATE_PART_PRELIMINARY) || sStatus.equals(DomainConstants.STATE_PART_OBSOLETE)){
            bReturn = false;
        }else{
//ADDED For BUG #347077
            StringList objSelects = new StringList();            objSelects.add("from["+DomainConstants.RELATIONSHIP_EBOM_MARKUP+"|"+DomainConstants.SELECT_TO_TYPE+"==\""+sType+"\"]");
			objSelects.add("from["+DomainConstants.RELATIONSHIP_EBOM_MARKUP+"|"+DomainConstants.SELECT_TO_TYPE+"==\""+sType+"\"].to.current");

            // Get the corresponding Markup
            Map mMap = doPart.getInfo(context, objSelects);

			if(mMap!=null) {
            String sFlag = (String)mMap.get("from["+DomainConstants.RELATIONSHIP_EBOM_MARKUP+"]");
			String sIMStatus = (String) mMap.get("from["+DomainConstants.RELATIONSHIP_EBOM_MARKUP+"].to.current");
            if(sFlag!=null && (sFlag).equalsIgnoreCase("True") && (sIMStatus.equals(proposedState) || sIMStatus.equals(approvedState)))
                bReturn = false;
        }
			// ADDED FOR BUG # 347077 - END
        }
        return Boolean.valueOf(bReturn);
    }


    /*
     * This method used to apply the item markup data to database for new version of part.
     * @param context, args
     * @return void value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    public void applyingItemMarkup(Context context, String strMarkupId, String strPartId, String strChangeId) throws Exception
    {
        DomainObject doPart = new DomainObject(strPartId);

        String sXMLFormat = PropertyUtil.getSchemaProperty(context, "format_XML");

        // create markup object.
        BusinessObject busObjMarkup = new BusinessObject(strMarkupId);
        try
        {
            busObjMarkup.open(context);
        }
        catch(MatrixException mex)
        {
            return;
        }

        String sbusObjMarkupName = busObjMarkup.getName();

        String sTransPath = context.createWorkspace();
        java.io.File fEmatrixWebRoot = new java.io.File(sTransPath);

        // check out file from markup
        try
        {
            busObjMarkup.checkoutFile(context, false, sXMLFormat, sbusObjMarkupName+ ".xml", fEmatrixWebRoot.toString());
        }
        catch(MatrixException mex)
        {
            return ;
        }

        HashMap attValues = new HashMap();

        java.io.File srcXMLFile = new java.io.File(fEmatrixWebRoot, sbusObjMarkupName+ ".xml");

        com.matrixone.jdom.Document docXML = null;
        com.matrixone.jdom.Element rootElement = null;

        // send file to SAX builder.
        try
        {
            com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder();
			builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
			builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            docXML = builder.build(srcXMLFile);
            rootElement = docXML.getRootElement();

        }
        catch(Exception e)
        {
            throw e;
        }

        java.util.List attList = rootElement.getChildren("attribute");
        java.util.Iterator attributeItr = attList.iterator();

        // get file values into file content.
        while(attributeItr.hasNext())
        {
            com.matrixone.jdom.Element attElement = (com.matrixone.jdom.Element) attributeItr.next();
            com.matrixone.jdom.Element attNameElement = attElement.getChild("name");
            String attName = attNameElement.getText();
            com.matrixone.jdom.Element attNewElement = attElement.getChild("newvalue");
            String attNewValue = attNewElement.getText();
            
            //BGTP Changes..for changing the revision sequence of the parts depending upon property ResetRevision
            String reviseSequence = MqlUtil.mqlCommand(context,"print policy $1 select $2 dump",EngineeringConstants.POLICY_EC_PART,"property[ResetRevision].value");
            if(attName.equals(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE) && attNewValue.equals(EngineeringConstants.PRODUCTION)&& "True".equalsIgnoreCase(reviseSequence))
            {
            	ReleasePhaseManager.resetRevision(context, new StringList(strPartId));
            }

            if(UOMUtil.isAssociatedWithDimension(context,attName))
            {
                //IR-041373V6R2011 -Starts
            	if (attNewValue.indexOf('_') > -1) {
	                String attrValue =  attNewValue.substring(0, attNewValue.indexOf('_'));
	                String attrUnit = attNewValue.substring(attNewValue.indexOf('_')+1,attNewValue.length());
	                attNewValue = attrValue + " " + attrUnit;
            	}
                //IR-041373V6R2011 -Ends
            }

            attValues.put(attName,attNewValue);
        }

        doPart.setAttributeValues(context,attValues);
        String strAppliedPartMarkupRel = PropertyUtil.getSchemaProperty(context,"relationship_AppliedPartMarkup");
		DomainRelationship.connect(context, doPart, strAppliedPartMarkupRel, new DomainObject(busObjMarkup));

		/* Added for Auto collaboration */
		IPartCollaborationService iPartCollabService = new PartCollaborationService();
		iPartCollabService.setCollaboratToDesign(true);
		
		IPartIngress  iPartIngress = IPartIngress.getService();
		iPartIngress.setObjectId(strPartId);
		ArrayList<String> list = new ArrayList<String>();
		list.add(strPartId);
		iPartCollabService.performCollabOperation(context, list, IPartValidator.OPERATION_MODIFY);
		
        context.deleteWorkspace();
    }

private StringList getListValue(Map map, String key) {
	Object data = map.get(key);
	if (data == null)
		return new StringList(0);
	if (data instanceof String)
		return FrameworkUtil.split((String) data, matrix.db.SelectConstants.cSelectDelimiter);
	return (StringList) data;
}


/**
 * @param context ematrix context.
 * @param args selected markup objectids.
 * @throws Exception if any operation fails.
 * This method first validates all the selected markup objects, if all the markup objects is valid to reject only then markup objects will
 * be rejected.
 */
public void rejectMarkup(Context context, String[] args) throws Exception {
    StringList markupobjList = (StringList) JPO.unpackArgs(args);

    StringList objectSelects = new StringList(6);
    objectSelects.addElement(DomainConstants.SELECT_NAME);
    objectSelects.addElement(DomainConstants.SELECT_ID);
    objectSelects.addElement(DomainConstants.SELECT_POLICY);
    objectSelects.addElement(DomainConstants.SELECT_CURRENT);
    objectSelects.addElement(DomainConstants.SELECT_OWNER);
    objectSelects.addElement(SELECT_ASSIGNEES);
	objectSelects.addElement(SELECT_TECHNICAL_ASSIGNEES);
    objectSelects.addElement(SELECT_ECR_ASSIGNEES_FROM_MARKUP_OBJ); //Added for IR-162940

    MapList dataList = DomainObject.getInfo(context, (String[]) markupobjList.toArray(new String[0]), objectSelects);
    Map dataMap;

    int dataSize = dataList.size();

    String strMarkupPolicy;
    String strMarkupState;
    String strMarkupOwner;
    String strMarkupId;
    String strContextUser = context.getUser();

    boolean boolCanReject = true;

    /* below for loop is for doing validations:
     * 1. context user should be an owner of the markup object OR context user should be an ECO Assignee.
     * 2. For development part markups which are only in Proposed state can be rejected.
     * 3. For part markup, EBOM markup Proposed OR Approved state markups can be rejected.
     * 4. No markups can be rejected which is Applied.
     * */

    for (int i = 0; i < dataSize; i++) {
    	dataMap = (Map) dataList.get(i);

    	strMarkupPolicy = (String) dataMap.get(DomainConstants.SELECT_POLICY);
    	strMarkupState  = (String) dataMap.get(DomainConstants.SELECT_CURRENT);
    	strMarkupOwner  = (String) dataMap.get(DomainConstants.SELECT_OWNER);
    	strMarkupId  = (String) dataMap.get(DomainConstants.SELECT_ID);
		    	
    	if (ReleasePhaseManager.isItemMarkupForSetToProduction(context,strMarkupId)) {
			boolCanReject = false;
    		emxContextUtil_mxJPO.mqlNotice(context, EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"ENCBOMGoToProduction.Alert.RejectBGTPMarkupErr"));
    		break;
    	}
    	if (strMarkupOwner.equals(strContextUser)
    			|| "User Agent".equals(strMarkupOwner)
    			|| getListValue(dataMap, SELECT_ASSIGNEES).contains(strContextUser)|| getListValue(dataMap, SELECT_ECR_ASSIGNEES_FROM_MARKUP_OBJ).contains(strContextUser) || getListValue(dataMap, SELECT_TECHNICAL_ASSIGNEES).contains(strContextUser)){ //Modified for IR-162940

	    	if (!strMarkupState.equals(proposedState) && !strMarkupState.equals(approvedState)) {

	    		//Multitenant
	    		String sError =EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.RejectError");

	    		if (POLICY_DEVELOPMENTPARTMARKUP.equals(strMarkupPolicy)) {
	    			//Multitenant
	    			sError = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.DevRejectError");
	    		}

	    		emxContextUtil_mxJPO.mqlNotice(context, sError + " " + dataMap.get(DomainConstants.SELECT_NAME));
	    		boolCanReject = false;
	    		break;
	    	}
    	} else {

    		//Multitenant
    		String ownerOrAssignError = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.RejectAccess");
    		emxContextUtil_mxJPO.mqlNotice(context, ownerOrAssignError + " " + dataMap.get(DomainConstants.SELECT_NAME));
    		boolCanReject = false;
    		break;
    	}
    } // validation of markups END.


    // If all the markups are valid to reject only then below code will be executed.
    if (boolCanReject) {
    	ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());

    	try {
	    	String markupObjId;
	    	DomainObject markupDomObj;

	    	ContextUtil.startTransaction(context, true);

	    	for (int i = 0; i < dataSize; i++) {
	    		dataMap = (Map) dataList.get(i);

	        	strMarkupPolicy = (String) dataMap.get(DomainConstants.SELECT_POLICY);
	        	markupObjId     = (String) dataMap.get(DomainConstants.SELECT_ID);

	        	markupDomObj = DomainObject.newInstance(context, markupObjId);

	        	if (POLICY_EBOMMARKUP.equals(strMarkupPolicy)) {
	        		strMarkupState  = (String) dataMap.get(DomainConstants.SELECT_CURRENT);

	        		markupDomObj.promote(context);
	    			markupDomObj.promote(context);

	        		if (strMarkupState.equals(ebommarkupproposedState)) {
	        			markupDomObj.promote(context);
	                }
	        	} else {
	        		markupDomObj.setAttributeValue(context, ATTRIBUTE_BRANCH_TO, "Rejected");
	        		markupDomObj.promote(context);
	        	}
	    	}

	    	ContextUtil.commitTransaction(context);
    	} catch (Exception e) {
    		ContextUtil.abortTransaction(context);
            throw e;
    	} finally {
    		ContextUtil.popContext(context);
    	}
    } // promoting markup objects to Reject state END.
}

/**
* Sets the Markup state to Approved
* @param    context the eMatrix <code>Context</code> object
* @param    args holds a StringList containing the following entries:
* paramMap - a StringList containing all selected Markup Ids
* @return    void
* @throws    Exception if operation fails
* @since   EngineeringCentral X3
*/
public void approveMarkup(Context context, String[] args)
    throws Exception
{
	try{
		String[] app = {"ENO_PRT_TP"};
		ComponentsUtil.checkLicenseReserved(context, app); // License Check
	}
	catch(Exception e){
		emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());
		//throw e;
		return;
	}

	boolean commitAtEnd = false;
	try{
			if(!ContextUtil.isTransactionActive(context)) {
				ContextUtil.startTransaction(context, true);
				commitAtEnd = true;
			}
		    
            ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
            StringList markupobjList = (StringList)JPO.unpackArgs(args);
			// BUG #347081 - Start

          //Multitenant
            String sError= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.ApproveMarkupError");
        Iterator stritr = markupobjList.iterator();
        String currentState= "";
        String markupId="";
        while(stritr.hasNext())
        {
            markupId = (String)stritr.next();
            DomainObject markupdObj = new DomainObject(markupId);
            markupdObj.open(context);
            Policy currentPolicy =  markupdObj.getPolicy(context);
            String currentpolicyName = currentPolicy.getName();
            currentState = markupdObj.getInfo(context,com.matrixone.apps.domain.DomainConstants.SELECT_CURRENT);

               if( currentpolicyName.equals(POLICY_PARTMARKUP))
            {
                if(currentState.equals(proposedState))
                    {
                       ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
                       markupdObj.setAttributeValue(context, ATTRIBUTE_BRANCH_TO, "None");
                       markupdObj.promote(context);
                       ContextUtil.popContext(context);
                    }
                else
                    {
                        emxContextUtil_mxJPO.mqlNotice(context,sError);
									// BUG #347081 - End
                    }
            }else if( currentpolicyName.equals(POLICY_EBOMMARKUP)){
				if(currentState.equals(ebommarkupproposedState))
                    {
                       ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
                       markupdObj.promote(context);
                       ContextUtil.popContext(context);
                    }
                else
                    {
                        emxContextUtil_mxJPO.mqlNotice(context,sError);
									// BUG #347081 - End
                    }

            }
       }
    }
catch(Exception ex)
    {
		commitAtEnd = false;
		ex.printStackTrace();
		ContextUtil.abortTransaction(context);
    }
finally {
		if(commitAtEnd) { ContextUtil.commitTransaction(context); }
		ContextUtil.popContext(context);
	}	
}

/**
      * PersonSearch - determines if the checkbox needs to be enabled in the column of the Part Family Summary table
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - objectList MapList
      * @returns Object of type Vector
      * @throws Exception if the operation fails
      * @since EC X3
      * @author Ranjit Kumar Singh
      */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList PersonSearch(Context context , String[] args) throws Exception
       {
           MapList personlist = new MapList();
            try {
                 MapList companylist = null;
                 HashMap programMap = (HashMap) JPO.unpackArgs(args);
                 String objectID = (String)programMap.get("objectId");
                 DomainObject domobj = new DomainObject(objectID);
                 StringList selectStmts  = new StringList(2);
                 selectStmts.addElement(SELECT_ID);
                 selectStmts.addElement(SELECT_NAME);
                 companylist= domobj.getRelatedObjects( context,
                                                  RELATIONSHIP_CHANGE_RESPONSIBILITY + "," + RELATIONSHIP_DESIGN_RESPONSIBILITY,     // relationship pattern
                                                  TYPE_ORGANIZATION,                // object pattern
                                                  selectStmts,                 // object selects
                                                  null,                        // relationship selects
                                                  true,                       // to direction
                                                  false,                        // from direction
                                                  (short) 0,                   // recursion level
                                                  null,                        // object where clause
                                                  null,
                                                  0); //limit 0 to return all the data available.
                int size = companylist.size();
                String strrel = RELATIONSHIP_EMPLOYEE_REPRESENTATIVE+","+RELATIONSHIP_MEMBER;
                for (int i=0;i<size;i++)  {
                    Map thismap = (Map)companylist.get(i);
                    String compnyId = (String)thismap.get("id");
                    DomainObject domObj = new DomainObject(compnyId);
                    StringList selStmts  = new StringList(2);
                    selStmts.addElement(SELECT_ID);
                    selStmts.addElement(SELECT_NAME);
                    personlist = domObj.getRelatedObjects( context,
                                                  strrel,             // relationship pattern
                                                  TYPE_PERSON,        // object pattern
                                                  selStmts,           // object selects
                                                  null,               // relationship selects
                                                  false,              // to direction
                                                  true,               // from direction
                                                  (short) 0,          // recursion level
                                                  null,               // object where clause
                                                  null,
                                                  0); //limit 0 to return all the data available.
            }

           }

           catch (Exception e) {
              throw e;
              }
           return personlist;

       }

     /**
      * Add Person as a Owner - determines if the checkbox needs to be enabled in the column of the Part Family Summary table
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - objectList MapList
      * @returns Object of type Vector
      * @throws Exception if the operation fails
      * @since EC X3
      * @author Ranjit Kumar Singh
      */
  public void AddPersonasOwner(Context context , String[] args) throws Exception
       {
            try {

                 HashMap programMap = (HashMap) JPO.unpackArgs(args);
                 HashMap reqTableMap = (HashMap) programMap.get("reqTableMap");
                 String[] emxTableRowId = (String[]) reqTableMap.get("emxTableRowId");
                 HashMap reqMap = (HashMap) programMap.get("reqMap");
                 String[] rowid = (String[]) reqMap.get("emxTableRowId");
                 DomainObject personobj = new DomainObject(rowid[0]);
                 String sPersonName=personobj.getInfo(context,DomainConstants.SELECT_NAME);

                 for(int i=0;i<emxTableRowId.length;i++)
                 {
                     StringList strmarkupId = FrameworkUtil.split(emxTableRowId[i],"|");
                 String markupId = (String)strmarkupId.elementAt(1);
                 DomainObject domobj = new DomainObject(markupId);
                     domobj.setOwner(context,sPersonName);

                 }

            }

     catch (Exception e) {
         throw e;
     }

    }

    /**
     * Deletes the Markup from the DataBase
     * @param    context the eMatrix <code>Context</code> object
     * @param    args holds a StringList containing the following entries:
     * paramMap - a StringList containing all selected Markup Ids
     * @return   void
     * @throws   Exception if operation fails
     * @since   EngineeringCentral X3
     */

	public void deleteMarkup(Context context, String[] args) throws Exception {
		StringList markupobjList = (StringList) JPO.unpackArgs(args);

		//Multitenant
		String sError =EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.DeleteError");
		String currentState = "";
		String markupId = "";

		String strContextUser = context.getUser(); //Added for IR-162940

		String[] objIdArr = new String[1];
		StringList objSelects = new StringList(SELECT_NAME);
		objSelects.addElement(SELECT_ID);
		objSelects.addElement(SELECT_CURRENT);
		objSelects.addElement(SELECT_OWNER);
		objSelects.addElement(SELECT_ASSIGNEES);
		objSelects.addElement(SELECT_TECHNICAL_ASSIGNEES);
		objSelects.addElement(SELECT_ECR_ASSIGNEES_FROM_MARKUP_OBJ); //Added for IR-162940

		MapList objDtlList = DomainObject.getInfo(context, (String[]) markupobjList.toArray(objIdArr), objSelects);

		Iterator objDtlItr = objDtlList.iterator();
		Map map = null;
		boolean canDelete = false;
		String[] deleteIds = new String[objDtlList.size()];
		int count = 0;
		while (objDtlItr.hasNext()) {
			canDelete = false;
			map = (Map) objDtlItr.next();
			markupId = (String) map.get(SELECT_ID);
			String markupName = (String) map.get(SELECT_NAME);
			currentState = (String) map.get(SELECT_CURRENT);

			//Checking for the state of the markup
			canDelete = currentState.equals(proposedState);

			if (canDelete) {
				// Check whether context user is owner of the Markup or Assignee of the related ECO
				if (strContextUser.equals((String) map.get(SELECT_OWNER))
						|| "User Agent".equals((String) map.get(SELECT_OWNER))
						|| getListValue(map, SELECT_ASSIGNEES).contains(strContextUser)
							|| getListValue(map, SELECT_ECR_ASSIGNEES_FROM_MARKUP_OBJ).contains(strContextUser) || getListValue(map, SELECT_TECHNICAL_ASSIGNEES).contains(strContextUser)) { //Modified for IR-162940 ,IR-458306
					deleteIds[count++] = markupId;
				} else {

					//Multitenant
					emxContextUtil_mxJPO.mqlNotice(context, EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource",
							context.getLocale(),"emxEngineeringCentral.MarkupActions.DeleteAccess"));
					return;
				}
			} else {
				emxContextUtil_mxJPO.mqlNotice(context, sError + " " + markupName);
				return;
			}
		}

		try {
			ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
			DomainObject.deleteObjects(context, deleteIds);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			ContextUtil.popContext(context);
		}
	}

    /**
     * Deletes the selected markup objects in Affected Item node
     * @param    context the eMatrix <code>Context</code> object
     * @param    args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following keys, markup object ids
     * @return    void
     * @throws    Exception if operation fails
     * @since   EngineeringCentral X3
     */
 public void deleteAffectedItemMarkups(Context context, String[] args)
                throws Exception
   {
         HashMap programMap = (HashMap)JPO.unpackArgs(args);
         String[] strObjectIds = (String[])programMap.get("strObjectIds[]");

         String typeBOMMarkup = PropertyUtil.getSchemaProperty(context,"type_BOMMarkup");
         String typeItemMarkup = PropertyUtil.getSchemaProperty(context,"type_ItemMarkup");
                 /* Added by Srikanth Anupoju for Plant Item Markup - Starts */
         String typePlantItemMarkup = PropertyUtil.getSchemaProperty(context,"type_PlantItemMarkup");
         String typePlantBOMMarkup = PropertyUtil.getSchemaProperty(context,"type_PlantBOMMarkup");
                 /* Added by Srikanth Anupoju for Plant Item Markup - Ends */

       //Multitenant
         String strMessage=EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.AffectedItem.Delete");

         for(int i=0;i<strObjectIds.length;i++)
         {
             int t=strObjectIds[i].indexOf('|');
            String affectedItemId=(strObjectIds[i].substring(0,t));
            DomainObject domObj=new DomainObject(affectedItemId);

            String selectedIdType=domObj.getInfo(context,SELECT_TYPE);
                /* following code modified by Srikanth Anupoju for Plant Item Marlup */
            if(selectedIdType.equals(typeBOMMarkup) || selectedIdType.equals(typeItemMarkup) ||
                selectedIdType.equals(typePlantItemMarkup) || selectedIdType.equals(typePlantBOMMarkup))
            {
                domObj.deleteObject(context);
            }
            else
            {
                emxContextUtil_mxJPO.mqlNotice(context,strMessage);
            }
         }

   }//end of deleteAffectedItemMarkups

/**
* Sets the Markup state to Applied
* @param    context the eMatrix <code>Context</code> object
* @param    args holds a StringList containing the following entries:
* paramMap - a StringList containing all selected Markup Ids
* @return    void
* @throws    Exception if operation fails
* @since   EngineeringCentral X3
*/
public int applyMarkup(Context context, String[] args)
    throws Exception
{

	try{
		String[] app = {"ENO_PRT_TP"};
		ComponentsUtil.checkLicenseReserved(context, app); // License Check
	}
	catch(Exception e){
		emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());
		//throw e;
		return 1;
	}

	//Multitenant
	String sConflictErr =EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.ConflictError");
	String sBGTPMarkUpApplyErr =EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"ENCBOMGoToProduction.Alert.ApplyBGTPMarkupErr");
	String sConflict = "";
	HashMap programMap1 = (HashMap)JPO.unpackArgs(args);
	StringList markupobjList1 = (StringList)programMap1.get("markupIds");
	String strObjectId1 = "";
	DomainObject doMarkup1 = null;

	boolean isPBMExists = false;
	boolean isBMExists = false;
	boolean sBGTPMarkup = false;
	
	StringList slPlantBOMMarkups = new StringList();
	StringList slEBOMMarkups = new StringList();

	sBGTPMarkup = validateBGTPItemMarkups(context, markupobjList1);
	if(sBGTPMarkup){
		emxContextUtil_mxJPO.mqlNotice(context,sBGTPMarkUpApplyErr);
		return 1;
	}

	ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());

	for(int i=0;i<markupobjList1.size();i++) {
		strObjectId1 = (String) markupobjList1.get(i);
		doMarkup1 = new DomainObject(strObjectId1);
		if (doMarkup1.isKindOf(context, TYPE_PLANT_BOM_MARKUP)) {
			slPlantBOMMarkups.add(strObjectId1);
			isPBMExists = true;
		} else {
			slEBOMMarkups.add(strObjectId1);
			if(!isBMExists && doMarkup1.isKindOf(context, TYPE_BOMMARKUP) || doMarkup1.isKindOf(context, TYPE_EBOMMARKUP)) {
				isBMExists = true;
			}
		}
	}


	if (isBMExists)
	{
		HashMap programMapBM = new HashMap();
		programMapBM.put("markupIds", slEBOMMarkups);
	    sConflict = (String)checkBOMConflict(context,JPO.packArgs(programMapBM));
	}

	if (isPBMExists)
	{
		HashMap programMapPBM = new HashMap();
		programMapPBM.put("markupIds", slPlantBOMMarkups);
		sConflict = (String)checkPlantBOMConflict(context,JPO.packArgs(programMapPBM));
	}

if ("false".equals(sConflict))
{
  emxContextUtil_mxJPO.mqlNotice(context,sConflictErr);
  return 1;
}

	try{
	HashMap programMap = (HashMap)JPO.unpackArgs(args);
        StringList markupobjList = (StringList)programMap.get("markupIds");
        String sChangeId = (String)programMap.get("changeId");

      //Multitenant
        String sError=EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.ApplyError");
      //Multitenant
        String sDevError= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.DevApplyError");
        Iterator stritr = markupobjList.iterator();
        String currentState= "";
        String markupId="";
        while(stritr.hasNext())
        {
            markupId = (String)stritr.next();
            DomainObject markupdObj = new DomainObject(markupId);
            markupdObj.open(context);
            String markupName = markupdObj.getName();
            Policy currentPolicy =  markupdObj.getPolicy(context);
            String currentpolicyName = currentPolicy.getName();
            StringList strlSelects = new StringList(7);
            strlSelects.add("to[" + RELATIONSHIP_EBOM_MARKUP + "].from.id");
            strlSelects.add("to[" + RELATIONSHIP_EBOM_MARKUP + "].from.policy");
            strlSelects.add("to[" + RELATIONSHIP_EBOM_MARKUP + "].from.name");
            strlSelects.add("to[" + RELATIONSHIP_EBOM_MARKUP + "].from.current");
            strlSelects.add("to[" + RELATIONSHIP_EBOM_MARKUP + "].from.revision");
            strlSelects.add("to[" + RELATIONSHIP_EBOM_MARKUP + "].from.first.revision");
            strlSelects.add(DomainConstants.SELECT_TYPE);

            Map mapMarkupInfo = (Map) markupdObj.getInfo(context, strlSelects);
            String strPartId = (String) mapMarkupInfo.get("to[" + RELATIONSHIP_EBOM_MARKUP + "].from.id");
            String strPartPolicy = (String) mapMarkupInfo.get("to[" + RELATIONSHIP_EBOM_MARKUP + "].from.policy");
            String strMarkupType = (String) mapMarkupInfo.get(DomainConstants.SELECT_TYPE);
            String strPartState = (String) mapMarkupInfo.get("to[" + RELATIONSHIP_EBOM_MARKUP + "].from.current");
            String strPartName = (String) mapMarkupInfo.get("to[" + RELATIONSHIP_EBOM_MARKUP + "].from.name");
            currentState = markupdObj.getInfo(context,com.matrixone.apps.domain.DomainConstants.SELECT_CURRENT);
            if( currentpolicyName.equals(POLICY_PARTMARKUP) || currentpolicyName.equals(POLICY_EBOMMARKUP))
            {
               if(!DomainConstants.POLICY_DEVELOPMENT_PART.equals(strPartPolicy) &&(currentState.equals(approvedState)))
                {
					String[] inputArgs = new String[2];

					inputArgs[0]= strPartId;
					inputArgs[1]=sChangeId;

					String strNewPartId = null;

					DomainObject doChange = new DomainObject(sChangeId);
                    String strPolicy = doChange.getPolicy(context).toString();

					if (sChangeId != null)
					{
						DomainObject changeobj = new DomainObject (sChangeId);
						if (changeobj.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION)){
						strNewPartId = getImplementedItem(context, inputArgs);
						}
						else{
						strNewPartId = (String) JPO.invoke(context, "enoEngChange", null, "getIndirectAffectedItems", inputArgs,String.class);
					}
					}
					
					if (strNewPartId != null)
					{
						strPartId = strNewPartId;
					}

					DomainObject doPart = new DomainObject(strPartId);

                    if (TYPE_EBOMMARKUP.equals(strMarkupType))
					{
						String strNextPartId = doPart.getInfo(context, "next.id");
						if (strNextPartId != null && strNextPartId.length() > 0)
						{
							strPartId = strNextPartId;
						}
					}

					doPart = new DomainObject(strPartId);

					strPartState = doPart.getInfo(context, DomainConstants.SELECT_CURRENT);
					String strPartCurrentRev = doPart.getInfo(context, DomainConstants.SELECT_REVISION);
					String strPartCurrentFirstRev = doPart.getInfo(context, "first.revision");
					String strPartVer = doPart.getInfo(context, PropertyUtil.getSchemaProperty(context,"attribute_IsVersion"));

                        //R209 MECO changes - Starts
					if((strPartCurrentRev.equals(strPartCurrentFirstRev) && "FALSE".equals(strPartVer) && strPartState.equals(DomainConstants.STATE_PART_PRELIMINARY))  || strPartState.equals(DomainConstants.STATE_PART_REVIEW) || strPartState.equals(DomainConstants.STATE_PART_APPROVED) || TYPE_PLANT_BOM_MARKUP.equals(strMarkupType))
                        //R209 MECO changes - Ends
						{
						//IR-063855
							if (TYPE_EBOMMARKUP.equals(strMarkupType))
							{
								int i = applyBOMMarkup(context,markupId,strPartId,null);

								if (i == 1)
								{
								return 1;
								}
							}
							//For BOM Markup Change Object is required.
                            if (TYPE_BOMMARKUP.equals(strMarkupType))
                            {
                                int i = applyBOMMarkup(context,markupId,strPartId,sChangeId);

                                if (i == 1)
                                {
                                return 1;
                                }
                            }
							else if (TYPE_ITEMMARKUP.equals(strMarkupType))
							{
								applyingItemMarkup(context,markupId,strPartId,null);
							}
							else if (TYPE_PLANT_BOM_MARKUP.equals(strMarkupType))
							{
								applyPlantBOMMarkup(context,markupId,strPartId,null);
							}

							else if (TYPE_PLANT_ITEM_MARKUP.equals(strMarkupType))
							{
								applyPlantItemMarkup(context, markupId, strPartId, sChangeId);
							}

                            if (!TYPE_EBOMMARKUP.equals(strMarkupType))
                            {
                            	markupdObj.setAttributeValue(context,ATTRIBUTE_BRANCH_TO,"None");
							}
                            markupdObj.promote(context);

						}
						else
						{
                            if(DomainConstants.POLICY_DEVELOPMENT_PART.equals(strPolicy))
                            {
                                int i = applyBOMMarkup(context,markupId,strPartId,null);
                                if(i == 0){
                                    markupdObj.promote(context);
                                }
                                else if (i == 1)
                                {
                                return 1;
                                }
                            }
                            else if (strPartCurrentRev.equals(strPartCurrentFirstRev) && "FALSE".equals(strPartVer))
							{

								String [] mailArguments = new String [8];
								mailArguments[0] = "emxEngineeringCentral.Alert.InValidStateError1";
								mailArguments[1] = "2";
								mailArguments[2] = "strPartName";
								mailArguments[3] = strPartName;
								mailArguments[4] = "markupName";
								mailArguments[5] = markupName;
								mailArguments[7] = "emxEngineeringCentralStringResource";
								String strMessage = (String)JPO.invoke(context, "emxMailUtil", new String[]{}, "getMessage", mailArguments, String.class);
								emxContextUtil_mxJPO.mqlNotice(context, strMessage);
							}
							else
							{

								String [] mailArguments = new String [8];
								mailArguments[0] = "emxEngineeringCentral.Alert.InValidStateError2";
								mailArguments[1] = "2";
								mailArguments[2] = "strPartName";
								mailArguments[3] = strPartName;
								mailArguments[4] = "markupName";
								mailArguments[5] = markupName;
								mailArguments[7] = "emxEngineeringCentralStringResource";
								String strMessage = (String)JPO.invoke(context, "emxMailUtil", new String[]{}, "getMessage", mailArguments, String.class);
								emxContextUtil_mxJPO.mqlNotice(context, strMessage);
							}
							return 1;
						}
                }
               else if(DomainConstants.POLICY_DEVELOPMENT_PART.equals(strPartPolicy)){

            	   int i = applyBOMMarkup(context,markupId,strPartId,null);
                   if(i == 0){
                       markupdObj.setState(context, appliedState);


                   }
                   else if (i == 1)
                   {
                   return 1;
                   }

               }
                else
                {
                        emxContextUtil_mxJPO.mqlNotice(context,sError+" "+markupName);
                        return 1;
                }
            }
            else if (currentpolicyName.equals(POLICY_DEVELOPMENTPARTMARKUP))
            {
                if(currentState.equals(proposedState))
                {
			if (TYPE_BOMMARKUP.equals(strMarkupType))
			{
                       	     int i = applyBOMMarkup(context,markupId,strPartId,null);

				if (i == 1)
				{
				return 1;
				}
			}
			if (!TYPE_EBOMMARKUP.equals(strMarkupType))
			{
                           markupdObj.setAttributeValue(context,ATTRIBUTE_BRANCH_TO,"None");
					   }
                            markupdObj.promote(context);

                }
                else
                {
                        emxContextUtil_mxJPO.mqlNotice(context,sDevError+" "+markupName);
                        return 1;
                }
            }
        }
    }
	catch(Exception e)
    {
		throw e;
    }
    finally
    {
		ContextUtil.popContext(context);
    }
    return 0;
}

    /**
     * Gets the Range of Owner Related for that Markups from Part Context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing "requestMap"
     *             1 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/

    public StringList getOwnerFilter(Context context,String[] args) throws Exception
    {

        StringList mlallOwners=new StringList();
        mlallOwners.addElement("All");
        HashMap programMap=(HashMap) JPO.unpackArgs(args);
        HashMap requestMap=(HashMap) programMap.get("requestMap");
        String objectId=(String)requestMap.get("objectId");

        String relpattern="";
        String typepattern="";
        String sOwner="";
        MapList MarkupIds=new MapList();

        StringList selectStmts=new StringList();
        selectStmts.addElement(DomainConstants.SELECT_ID);
        selectStmts.addElement(DomainConstants.SELECT_OWNER);

        StringList selectRelStmts=new StringList();
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

        DomainObject domobj=new  DomainObject();
        domobj.setId(objectId);

        relpattern=PropertyUtil.getSchemaProperty(context,"relationship_PartMarkup");
        typepattern=PropertyUtil.getSchemaProperty(context,"type_BOMMarkup");


        MarkupIds = domobj.getRelatedObjects(context,relpattern,typepattern,selectStmts,selectRelStmts,false,true,(short)1,null,null,0);

        Iterator ItrOwner=MarkupIds.iterator();
        Map mapOwner=null;

        while(ItrOwner.hasNext())
        {
            mapOwner=(Map)ItrOwner.next();
            sOwner=(String)mapOwner.get(DomainConstants.SELECT_OWNER);

            // To check for Duplicate Values of Owners in the List
             if (!mlallOwners.contains(sOwner))
            {
                // Add Owner to to list
                 mlallOwners.addElement(sOwner);

            }

        }

        return mlallOwners;
    }

    /**
     * Used for Save and SaveAs fn, Shows Name  Field only if user selects Save As Option
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing "commandType"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
    public static Boolean isAutoName(Context context, String args[])throws Exception
    {
        Boolean status=Boolean.TRUE;
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String sType = (String)paramMap.get("commandType");
        if(sType==null || sType.equalsIgnoreCase("Save"))
            status = Boolean.FALSE;
        return status;
    }

    /**
     * Used for Save and SaveAs fn, Shows Change Field onlyfor EC Parts
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing "commandType"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
    public static Boolean isSelectedECPart(Context context, String args[])throws Exception
    {
        Boolean status=Boolean.FALSE;
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String sSelectedPart=(String)paramMap.get("sSelPart");

        if(!"DevPart".equalsIgnoreCase(sSelectedPart))
        {
            status=Boolean.TRUE;
        }

       return status;
    }

    /**
     * Gets the CommandType and returns True for SaveAs Command.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing "commandType"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
    public String isConnectedtoChangeProcess(Context context,String partId,String chgid, String relPartre)throws Exception
    {
        String isconnected="false";
        String pid=partId;
        String cid=chgid;
        String querycommand="";

        if (relPartre == null)
        {
			relPartre = RELATIONSHIP_AFFECTED_ITEM;
		}

        try
        {
            querycommand="print bus"+" "+"$1"+" "+ "select $2 dump  $3";
            String sResultPartconnectedtoChangeProcess = MqlUtil.mqlCommand(context,querycommand,pid,"to["+relPartre+"|from.id=='"+cid+"']","|").trim();
            if(sResultPartconnectedtoChangeProcess.equalsIgnoreCase("True"))
            {
                isconnected="true";
            }
        }
        catch(Exception e)
        {
            throw e;
        }

        return isconnected;
    }

    /**
     * Gets the CommandType and returns True for SaveAs Command.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing "commandType"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/

    public void createMarkup(Context context,String args[]) throws Exception
    {
    	ContextUtil.startTransaction(context, true);

    	try {

	        HashMap programMap=(HashMap)JPO.unpackArgs(args);

			String strPartId = (String) programMap.get("partId");
			String strXML = (String) programMap.get("markupXML");
			String changeOID = (String) programMap.get("ChangeOID");
			String sName = (String) programMap.get("name");
			String sVault = (String) programMap.get("vault");
			String sRevision = "-";
			String sDescription = (String) programMap.get("description");
			String sCommandType = (String) programMap.get("commandType");

			String strChangeRel = null;
			String strChangeType = null;

			String [] strArray = new String[3];

            if(changeOID != null && changeOID.length() != 0) {					
            	DomainObject doChgObj = new DomainObject(changeOID);
				if(doChgObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_ORDER) || doChgObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_REQUEST)) {
					Map changeActionMap    = EngineeringUtil.checkPartAlreadyConnectedToThisChange(context, strPartId, changeOID);
					String changeConnected = (String)changeActionMap.get("ALREADY_CONNECTED");
					if("TRUE".equalsIgnoreCase(changeConnected)) {						//
						changeOID = (String)changeActionMap.get(ChangeConstants.RELATED_CA_ID);
					}
					else {
						  ChangeOrder changeOrder = new ChangeOrder(changeOID);
				          StringList objIdList = new StringList();
				          objIdList.add(strPartId);
				          HashMap mCAList   = (HashMap) changeOrder.connectAffectedItems(context,objIdList);
					      HashMap mCAIdList = (HashMap)mCAList.get("objIDCAMap");					      
					      String strCAId    = (String)mCAIdList.get(strPartId);					     
					      changeOID = strCAId;
					}					
				}
				DomainObject doChange = new DomainObject(changeOID);								
				strChangeType   = doChange.getInfo(context, DomainConstants.SELECT_TYPE);
				strChangeRel    = RELATIONSHIP_AFFECTED_ITEM;	
				String strHasChangeConnAcc   = doChange.getInfo(context, "current.access[fromconnect]");
				
				 if(UIUtil.isNotNullAndNotEmpty(strHasChangeConnAcc) && "FALSE".equalsIgnoreCase(strHasChangeConnAcc)) {
					 String strMessage = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.EBOM.MarkupSaveConnectAccess");
					 emxContextUtil_mxJPO.mqlNotice(context, strMessage);
					 return;
				 }
			}

			DomainObject doPart = new DomainObject(strPartId);
			String strPolicy = doPart.getInfo(context, DomainConstants.SELECT_POLICY);
			//371781 - Modified the if else condition to check for PolicyClassification instead of Policy
	        String policyClassification = EngineeringUtil.getPolicyClassification(context,strPolicy);
	            
	        //BGP: Check if Release Process is "Development Part" -> start
	        boolean isDevModePart = ReleasePhase.isECPartWithDevMode(context, strPartId);
	        //BGP: Check if Release Process is "Development Part" -> end 
	        //For DevParts
	        if("Development".equalsIgnoreCase(policyClassification) || isDevModePart) {
	                //Creates an Auto Name of EBOM Markup Type
	                if(sCommandType==null || sCommandType.equalsIgnoreCase("Save")) {
					String sMarkupId = FrameworkUtil.autoName(context,"type_BOMMarkup","policy_DevelopmentPartMarkup");

					DomainObject sdomobj = new DomainObject(sMarkupId);
	                    sdomobj.setDescription(context,sDescription);

					strArray[0] = strXML;
					strArray[1] = sMarkupId;
					strArray[2] = strPartId;

				   DomainRelationship.connect(context,new DomainObject(strPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sMarkupId));

				   generateBOMMarkupXML(context,strArray);

	                }
	                //  Create an object with User choosed Name
	                else {
						DomainObject sdomobj= new DomainObject();
						sdomobj.createObject(context,TYPE_BOMMARKUP,sName,sRevision,POLICY_DEVELOPMENTPARTMARKUP,sVault);
	
						String sMarkupId = sdomobj.getInfo(context,DomainConstants.SELECT_ID);
		                sdomobj.setDescription(context,sDescription);
	
						strArray[0] = strXML;
						strArray[1] = sMarkupId;
						strArray[2] = strPartId;
	
					    DomainRelationship.connect(context,new DomainObject(strPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sMarkupId));
	
					    generateBOMMarkupXML(context,strArray);
	                }

	            }
	            // For EC Parts
	            else if("Production".equalsIgnoreCase(policyClassification)) {
	            	if(UIUtil.isNullOrEmpty(changeOID))
	            	{
	            		 String strMessage = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.ProductionPart.MarkupCreationFails");
                         emxContextUtil_mxJPO.mqlNotice(context,strMessage);
                         return;
	            	}
	                if(sCommandType==null || sCommandType.equalsIgnoreCase("Save")) {
	                	String sMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
						DomainObject sdomobj= new DomainObject(sMarkupId);
	                    sdomobj.setDescription(context,sDescription);
						strArray[0] = strXML;
						strArray[1] = sMarkupId;
						strArray[2] = strPartId;

						String[] inputArgs = new String[2];
						inputArgs[0] = strPartId;
						inputArgs[1]=changeOID;

						String strNewPartId = null;

						if (changeOID != null) {
							strNewPartId = (String) JPO.invoke(context, "enoEngChange", null, "getDirectAffectedItems", inputArgs,String.class);
						}

						if (strNewPartId != null) {
							strPartId = strNewPartId;
						}

						String isChangeconnected = "";
						if(ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(strChangeType)) {
							isChangeconnected = isChangeObjectsInProposed(context,strPartId,changeOID);
						} else {
							isChangeconnected = isConnectedtoChangeProcess(context,strPartId,changeOID, strChangeRel);
						}

	                    if(isChangeconnected.equalsIgnoreCase("true")) {
							DomainRelationship.connect(context,new DomainObject(strPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sMarkupId));
							
							if(ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(strChangeType)) {
								DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_APPLIED_MARKUP,new DomainObject(sMarkupId));
		                    } else if(DomainConstants.TYPE_ECO.equalsIgnoreCase(strChangeType)) {
								DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_APPLIED_MARKUP,new DomainObject(sMarkupId));
		                    } else {
		                        DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_PROPOSED_MARKUP,new DomainObject(sMarkupId));
		                    }
	                    }
	                    else {
							DomainRelationship.connect(context,new DomainObject(strPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sMarkupId));									
							if(ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(strChangeType)) {
								DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_APPLIED_MARKUP,new DomainObject(sMarkupId));
		                    } else if(strChangeType.equalsIgnoreCase(DomainConstants.TYPE_ECO)) {
		                    	DomainRelationship.connect(context,new DomainObject(changeOID),strChangeRel,new DomainObject(strPartId));
								DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_APPLIED_MARKUP,new DomainObject(sMarkupId));
		                    } else {
		                    	DomainRelationship.connect(context,new DomainObject(changeOID),strChangeRel,new DomainObject(strPartId));
		                        DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_PROPOSED_MARKUP,new DomainObject(sMarkupId));
	                       }
	                    }

					generateBOMMarkupXML(context,strArray);

	                }
	                // EC Part Save As Command
	                else {

	                    DomainObject sdomobj= new DomainObject();
	                    sdomobj.createObject(context,TYPE_BOMMARKUP, sName, sRevision, POLICY_PARTMARKUP, sVault);
	                    String sMarkupId =sdomobj.getInfo(context,DomainConstants.SELECT_ID);
	                    sdomobj.setDescription(context,sDescription);

					strArray[0] = strXML;
					strArray[1] = sMarkupId;
					strArray[2] = strPartId;

						String[] inputArgs = new String[2];

					inputArgs[0] = strPartId;
						inputArgs[1]=changeOID;

						String strNewPartId = null;

						if (changeOID != null)
						{
							strNewPartId = (String) JPO.invoke(context, "enoEngChange", null, "getDirectAffectedItems", inputArgs,String.class);
						}

						if (strNewPartId != null)
						{
						strPartId = strNewPartId;
						}

					String isChangeconnected = "";
					if(ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(strChangeType)) {
						isChangeconnected = isChangeObjectsInProposed(context,strPartId,changeOID);
					} else {
						isChangeconnected = isConnectedtoChangeProcess(context,strPartId,changeOID, strChangeRel);
					}

						if (strNewPartId != null)
						{
						strPartId = strNewPartId;
						}


	                    if(isChangeconnected.equalsIgnoreCase("true"))
	                    {
						DomainRelationship.connect(context,new DomainObject(strPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sMarkupId));

						if((strChangeType.equalsIgnoreCase(DomainConstants.TYPE_ECO))|| (strChangeType.equalsIgnoreCase(ChangeConstants.TYPE_CHANGE_ACTION)))
	                        {
	                        DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_APPLIED_MARKUP,new DomainObject(sMarkupId));
	                    } else {
	                        DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_PROPOSED_MARKUP,new DomainObject(sMarkupId));
	                    }
	                    }
	                    else
	                    {
						DomainRelationship.connect(context,new DomainObject(strPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sMarkupId));
												
						//Modified for ENG Convergence Start
						if(strChangeType.equalsIgnoreCase(ChangeConstants.TYPE_CHANGE_ACTION)) {
							DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_APPLIED_MARKUP,new DomainObject(sMarkupId));
	                    } else if(strChangeType.equalsIgnoreCase(DomainConstants.TYPE_ECO)) {
	                    	DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_APPLIED_MARKUP,new DomainObject(sMarkupId));
	                    	DomainRelationship.connect(context,new DomainObject(changeOID),strChangeRel,new DomainObject(strPartId));
	                    } else {
	                        DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_PROPOSED_MARKUP,new DomainObject(sMarkupId));
	                        DomainRelationship.connect(context,new DomainObject(changeOID),strChangeRel,new DomainObject(strPartId));
	                    }
						//Modified for ENG Convergence End
	                }

					generateBOMMarkupXML(context,strArray);
	                }

	            }
	        ContextUtil.commitTransaction(context);
    	} catch (Exception e) {
    		ContextUtil.abortTransaction(context);
    		throw e;
    	}
    }

    /**
     * Gets the CommandType and returns True for SaveAs Command.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing "commandType"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
    public String getMarkupXML(Context context,String[] args)throws Exception
    {
            String str = "";
            str+="<input type='hidden' name='massUpdateAction' id='massUpdateAction' value=''></input>";
            str+="<script language=javascript>";
            str+="var obj=document.getElementById(\"massUpdateAction\");";
            str+="callback = eval(getTopWindow().getWindowOpener().emxEditableTable.prototype.getMarkUpXML);";
            str+="var oxmlstatus = callback();";
            str+="obj.value=oxmlstatus.xml;";
           // str+="alert(obj.value);";
            str+="</script>";

        return str;
    }

    /**
     * Gets the CommandType and returns True for SaveAs Command.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing "commandType"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/

    public void generateBOMMarkupXML(Context context,String[] args) throws Exception
    {
        // Gets massupdateAction from Create Markup Method
        String massUpdateAction=args[0];
        String sMarkupId=args[1];
        String sSelPartId=args[2];

        DomainObject smkpdomobj=new DomainObject();
        smkpdomobj.setId(sMarkupId);

        // massupdateAction  Ends

        com.matrixone.jdom.Element ematrixrootElement = new com.matrixone.jdom.Element("ematrix");
        MapList sbMList=new MapList();
        int sbmListcount=0;

        if (massUpdateAction != null)
        {
            try
            {


        java.io.CharArrayReader reader = new java.io.CharArrayReader(massUpdateAction.toCharArray());
        com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder(false);
		builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
		builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder.setValidation(false);
        com.matrixone.jdom.Document doc = builder.build(reader);


         //Get the top Level Document
         com.matrixone.jdom.Element rootElement = doc.getRootElement();

         //IR-034520V6R2011 - Start
         String  parentobjectId = sSelPartId;
         //IR-034520V6R2011 - End

         com.matrixone.jdom.Element objectList = rootElement.getChild("object");
        java.util.List lr=objectList.getChildren("object");

        //Setting the Domain Object Id
        // This is the Id of Selected Row i.e Parent Node Id
        DomainObject domobj=new DomainObject();
        domobj.setId(sSelPartId);

        String parentType=domobj.getInfo(context,DomainConstants.SELECT_TYPE);
        String parentRevision=domobj.getInfo(context,DomainConstants.SELECT_REVISION);
        String parentPolicy=domobj.getInfo(context,DomainConstants.SELECT_POLICY);
        String parentVault=domobj.getInfo(context,DomainConstants.SELECT_VAULT);
        String parentOwner=domobj.getInfo(context,DomainConstants.SELECT_OWNER);
        String parentName=domobj.getInfo(context,DomainConstants.SELECT_NAME);
        String parentDescription=domobj.getDescription(context);

        String ATTRIBUTE_NOTES=PropertyUtil.getSchemaProperty(context,"attribute_Notes");
        StringList selStmts=new StringList(4);
        selStmts.addElement(DomainConstants.SELECT_ID);
        selStmts.addElement(DomainConstants.SELECT_TYPE);
        selStmts.addElement(DomainConstants.SELECT_NAME);
        selStmts.addElement(DomainConstants.SELECT_REVISION);
        selStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_UNITOFMEASURE);
        selStmts.addElement(DomainConstants.SELECT_VAULT);
        selStmts.addElement(DomainConstants.SELECT_CURRENT);

        StringList selRelStmts=new StringList(1);
        selRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
        selRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
        selRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_QUANTITY);
        selRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_USAGE);
        selRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        selRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        selRelStmts.addElement("attribute["+ATTRIBUTE_NOTES+"]");

        MapList ebomList=domobj.getRelatedObjects(context,DomainConstants.RELATIONSHIP_EBOM,DomainConstants.TYPE_PART,selStmts,selRelStmts,false,true,(short)1,null,null,0);

        Iterator Itre=ebomList.iterator();
        int ebomListcount = ebomList.size();

        // Creation of XML for Parent Part
        com.matrixone.jdom.Element businessObjectElement = new com.matrixone.jdom.Element("businessObject");
        businessObjectElement.setAttribute("id",parentobjectId);
        ematrixrootElement.addContent(businessObjectElement);

        com.matrixone.jdom.Element objectTypeElement = new com.matrixone.jdom.Element("objectType");
        //Start : IR-054311V6R2011x
        objectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentType));
       //End : IR-054311V6R2011x
        businessObjectElement.addContent(objectTypeElement);


        com.matrixone.jdom.Element objectNameElement = new com.matrixone.jdom.Element("objectName");
        //Start : IR-054311V6R2011x
        objectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentName));
        //End : IR-054311V6R2011x
        businessObjectElement.addContent(objectNameElement);


        com.matrixone.jdom.Element objectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
        //Start : IR-054311V6R2011x
        objectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentRevision));
        //End : IR-054311V6R2011x
        businessObjectElement.addContent(objectRevisionElement);

        com.matrixone.jdom.Element vaultRefElement = new com.matrixone.jdom.Element("vaultRef");
        //Start : IR-054311V6R2011x
        vaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentVault));
        //End : IR-054311V6R2011x
        businessObjectElement.addContent(vaultRefElement);

        com.matrixone.jdom.Element policyRefElement = new com.matrixone.jdom.Element("policyRef");
        policyRefElement.setText(parentPolicy);
        businessObjectElement.addContent(policyRefElement);

        com.matrixone.jdom.Element ownerElement = new com.matrixone.jdom.Element("owner");
        businessObjectElement.addContent(ownerElement);

        com.matrixone.jdom.Element userRefElement = new com.matrixone.jdom.Element("userRef");
       //Start : IR-054311V6R2011x
        userRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentOwner));
        //End : IR-054311V6R2011x
        ownerElement.addContent(userRefElement);

        com.matrixone.jdom.Element descriptionElement = new com.matrixone.jdom.Element("description");
        //Start : IR-054311V6R2011x
        descriptionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentDescription));
        //End : IR-054311V6R2011x
        businessObjectElement.addContent(descriptionElement);

        Element fromRelationshipListElement=new com.matrixone.jdom.Element("fromRelationshipList");
        businessObjectElement.addContent(fromRelationshipListElement);

            // Root element XML Ends
        com.matrixone.jdom.Element relationshipElement =null;
        com.matrixone.jdom.Element relationshipDefRefElement =null;
        com.matrixone.jdom.Element relatedObjectElement =null;
        com.matrixone.jdom.Element businessObjectRefElement =null;
        com.matrixone.jdom.Element subobjectTypeElement = null;
        com.matrixone.jdom.Element subobjectNameElement = null;
        com.matrixone.jdom.Element subobjectRevisionElement =null;
        com.matrixone.jdom.Element subvaultRefElement =null;
        com.matrixone.jdom.Element UOMElement = null;
        com.matrixone.jdom.Element attributeListElement=null;
        com.matrixone.jdom.Element attributeElement = null;
        com.matrixone.jdom.Element nameElement = null;
        com.matrixone.jdom.Element StringElement = null;
        com.matrixone.jdom.Element realElement = null;
        com.matrixone.jdom.Element objectStateElement =null;

        HashMap hmpDelete = new HashMap();
        HashMap hmpChanged = new HashMap();

        // to iterate toget the change List objects
       java.util.List objList=rootElement.getChildren("object");
       Iterator objitr=objList.iterator();
       String replaceParam;
        while(objitr.hasNext())
        {
            Element elesubChild = (Element) objitr.next();
            String sObjid=elesubChild.getAttributeValue("objectId");
            String smarkupAttrValue=elesubChild.getAttributeValue("markup");
            String sRelId=elesubChild.getAttributeValue("relId");

            if("changed".equalsIgnoreCase(smarkupAttrValue))
            {
                hmpChanged.put(sObjid+"|"+sRelId,"changed");
            }
            else
            {
				lr  = elesubChild.getChildren("object");
			}
        }

        //Iterates SbMapList
        if (lr != null)
        {
               Iterator itr = lr.iterator();
        while (itr.hasNext())
                {
                    Element eleChild = (Element) itr.next();
                    // These are the Objects with changes
                    String strObjectId = eleChild.getAttributeValue("objectId");
                    String strRelId = eleChild.getAttributeValue("relId");
                     sbMList.add(strObjectId);

                     DomainObject doj=new DomainObject();
                     doj.setId(strObjectId);

                    String markupAction=eleChild.getAttributeValue("markup");

                    if(markupAction.equalsIgnoreCase("cut"))
                    {
                        hmpDelete.put(strObjectId+"|"+strRelId , "delete");
                    	//Auto sync start ** 
                    	replaceParam = eleChild.getAttributeValue("param1");
                    	if("replace".equalsIgnoreCase(replaceParam)) {
                    		hmpDelete.put(strObjectId+"|~"+strRelId , "replace");
                    	}                    	
                    	//Auto sync End
                    }
                    else if (markupAction.equalsIgnoreCase("changed")){
                    }

                    // If Markup Action is Add
                    else
                    {
						String sRelId = eleChild.getAttributeValue("param1");
						if(sRelId == null)
							sRelId = "new";

						// Added To Support Normal BOM Handling In Common View START
						String relType = eleChild.getAttributeValue("relType");
						int status = checkForBOMChanges(context, relType, sRelId , null, "add");
						if(status == 1)
							continue;
						// Added To Support Normal BOM Handling In Common View END

                        sbmListcount++;
                         java.util.List columnelements=eleChild.getChildren("column");
                         Iterator columnIterator=columnelements.iterator();
                          HashMap tempmap=new HashMap();
                          while(columnIterator.hasNext())
                              {

                              Element elesubChild = (Element) columnIterator.next();
                              String Strname = elesubChild.getAttributeValue("name");
                              String StrValue=elesubChild.getText();
                              tempmap.put(Strname,StrValue);
                              }

                          relationshipElement = new com.matrixone.jdom.Element("relationship");
                          relationshipElement.setAttribute("chgtype","A");
                        	//Auto sync start **
                          replaceParam = eleChild.getAttributeValue("param3");
                          if("replace".equalsIgnoreCase(replaceParam)) {
                        	  relationshipElement.setAttribute("replace","true"); // Do replace instead add operation for this mark up
                          }
                          //Auto sync end **
                          fromRelationshipListElement.addContent(relationshipElement);


                          relationshipDefRefElement = new com.matrixone.jdom.Element("relationshipDefRef");
                          relationshipDefRefElement.setAttribute("relid",sRelId);
                          relationshipDefRefElement.setText(DomainConstants.RELATIONSHIP_EBOM);
                          relationshipElement.addContent(relationshipDefRefElement);

                          relatedObjectElement = new com.matrixone.jdom.Element("relatedObject");
                          relatedObjectElement.setAttribute("relatedobjid",strObjectId);
                          relationshipElement.addContent(relatedObjectElement);

                           businessObjectRefElement = new com.matrixone.jdom.Element("businessObjectRef");
                           relatedObjectElement.addContent(businessObjectRefElement);

                          subobjectTypeElement = new com.matrixone.jdom.Element("objectType");
                          subobjectTypeElement.setAttribute("chgtype","A");
                          //Start : IR-054311V6R2011x
                          subobjectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(doj.getInfo(context,DomainConstants.SELECT_TYPE)));
                          //End : IR-054311V6R2011x
                          businessObjectRefElement.addContent(subobjectTypeElement);

                          subobjectNameElement = new com.matrixone.jdom.Element("objectName");
                          subobjectNameElement.setAttribute("chgtype","A");
                          //Start : IR-054311V6R2011x
                          subobjectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(doj.getInfo(context,DomainConstants.SELECT_NAME)));
                          //End : IR-054311V6R2011x
                          businessObjectRefElement.addContent(subobjectNameElement);

                          subobjectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
                          subobjectRevisionElement.setAttribute("chgtype","A");
                          //Start : IR-054311V6R2011x
                          subobjectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(doj.getInfo(context,DomainConstants.SELECT_REVISION)));
                          //End : IR-054311V6R2011x
                          businessObjectRefElement.addContent(subobjectRevisionElement);

                          subvaultRefElement = new com.matrixone.jdom.Element("vaultRef");
                          //Start : IR-054311V6R2011x
                          subvaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(doj.getInfo(context,DomainConstants.SELECT_VAULT)));
                          //End : IR-054311V6R2011x
                          businessObjectRefElement.addContent(subvaultRefElement);

                          UOMElement = new com.matrixone.jdom.Element("UOM");
                          UOMElement.setAttribute("chgtype","A");
                          UOMElement.setText(doj.getInfo(context,DomainConstants.SELECT_ATTRIBUTE_UNITOFMEASURE));
                          businessObjectRefElement.addContent(UOMElement);

                          attributeListElement = new com.matrixone.jdom.Element("attributeList");
                          relationshipElement.addContent(attributeListElement);

                          attributeElement = new com.matrixone.jdom.Element("attribute");
                          attributeElement.setAttribute("chgtype","A");
                          attributeListElement.addContent(attributeElement);

                          nameElement = new com.matrixone.jdom.Element("name");
                          nameElement.setText(DomainConstants.ATTRIBUTE_FIND_NUMBER);
                          attributeElement.addContent(nameElement);

                          StringElement = new com.matrixone.jdom.Element("string");
                          StringElement.setText((String)tempmap.get("Find Number"));
                          attributeElement.addContent(StringElement);

                          attributeElement  = new com.matrixone.jdom.Element("attribute");
                          attributeElement .setAttribute("chgtype","A");
                          attributeListElement .addContent(attributeElement);

                          nameElement  = new com.matrixone.jdom.Element("name");
                          nameElement .setText(DomainConstants.ATTRIBUTE_QUANTITY);
                          attributeElement .addContent(nameElement);

                          realElement  = new com.matrixone.jdom.Element("real");
                          realElement .setText((String)tempmap.get("Quantity"));
                          attributeElement .addContent(realElement );

                          attributeElement  = new com.matrixone.jdom.Element("attribute");
                          attributeElement .setAttribute("chgtype","A");
                          attributeListElement .addContent(attributeElement);

                          nameElement  = new com.matrixone.jdom.Element("name");
                          nameElement .setText(DomainConstants.ATTRIBUTE_USAGE);
                          attributeElement .addContent(nameElement);

                          StringElement = new com.matrixone.jdom.Element("string");
                          StringElement.setText((String)tempmap.get("Usage"));
                          attributeElement.addContent(StringElement);

                          attributeElement = new com.matrixone.jdom.Element("attribute");
                          attributeElement.setAttribute("chgtype","A");
                          attributeListElement.addContent(attributeElement);

                          nameElement = new com.matrixone.jdom.Element("name");
                          nameElement.setText(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
                          attributeElement.addContent(nameElement);

                          StringElement = new com.matrixone.jdom.Element("string");
                          StringElement.setText((String)tempmap.get("Reference Designator"));
                          attributeElement.addContent(StringElement);

                          attributeElement = new com.matrixone.jdom.Element("attribute");
                          attributeElement.setAttribute("chgtype","A");
                          attributeListElement.addContent(attributeElement);

                          nameElement = new com.matrixone.jdom.Element("name");
                          nameElement.setText(DomainConstants.ATTRIBUTE_COMPONENT_LOCATION);
                          attributeElement.addContent(nameElement);

                          StringElement = new com.matrixone.jdom.Element("string");
                          StringElement.setText((String)tempmap.get("Component Location"));
                          attributeElement.addContent(StringElement);

                          attributeElement = new com.matrixone.jdom.Element("attribute");
                          attributeElement.setAttribute("chgtype","A");
                          attributeListElement.addContent(attributeElement);

                          nameElement = new com.matrixone.jdom.Element("name");
                          nameElement.setText(ATTRIBUTE_NOTES);
                          attributeElement.addContent(nameElement);

                          StringElement = new com.matrixone.jdom.Element("string");
                          StringElement.setText(( String)tempmap.get(ATTRIBUTE_NOTES));

                          attributeElement.addContent(StringElement);
                          
                          //Added for UOM Management---Start
                          
                          attributeElement  = new com.matrixone.jdom.Element("attribute");
                          attributeElement .setAttribute("chgtype","A");
                          attributeListElement .addContent(attributeElement);
                          
                          nameElement  = new com.matrixone.jdom.Element("name");
                          nameElement .setText(EngineeringConstants.UOM);
                          attributeElement .addContent(nameElement);
                          
                          StringElement = new com.matrixone.jdom.Element("string");
                          StringElement.setText(( String)tempmap.get(EngineeringConstants.UOM));

                          attributeElement.addContent(StringElement);
                          //Added for UOM Management---End
                          
                         /* Added to store the VPM Visible attribute on EBOM rel
                         * Note, the field is derived from the ENCEBOMIndentedSummarySB column 
                         */
                          attributeElement  = new com.matrixone.jdom.Element("attribute");
                          attributeElement .setAttribute("chgtype","A");
                          attributeListElement .addContent(attributeElement);
                          
                          nameElement  = new com.matrixone.jdom.Element("name");
                          nameElement .setText(EngineeringConstants.ATTRIBUTE_VPM_VISIBLE);
                          attributeElement .addContent(nameElement);
                          
                          StringElement = new com.matrixone.jdom.Element("string");
                          StringElement.setText(( String)tempmap.get("VPMVisible"));

                          attributeElement.addContent(StringElement);
                          // Added for VPM Visible on EBOM rel
                    }
                }
            }

        //Iterated ebomMapList
        while(Itre.hasNext())
        {
            Map mapebomList=(Map) Itre.next();
            String ebomObjectId=(String) mapebomList.get(DomainConstants.SELECT_ID);
            String ebomRelId=(String) mapebomList.get(DomainConstants.SELECT_RELATIONSHIP_ID);

            String ebomObjectType=(String)mapebomList.get(DomainConstants.SELECT_TYPE);
            String ebomObjectName=(String)mapebomList.get(DomainConstants.SELECT_NAME);

            String ebomObjectRevision=(String)mapebomList.get(DomainConstants.SELECT_REVISION);
            String ebomObjectcurrent=(String)mapebomList.get(DomainConstants.SELECT_CURRENT);
            String ebomObjectVault=(String)mapebomList.get(DomainConstants.SELECT_VAULT);
            String ATTRIBUTE_UNITOFMEASURE=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_UNITOFMEASURE);
            String ATTRIBUTE_FIND_NUMBER=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
            String ATTRIBUTE_QUANTITY=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_QUANTITY);
            String ATTRIBUTE_USAGE=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_USAGE);
            String ATTRIBUTE_REFERENCE_DESIGNATOR=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
            String ATTRIBUTE_COMPONENT_LOCATION=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
            String Notes=(String)mapebomList.get("attribute["+ATTRIBUTE_NOTES+"]");

            DomainObject ebomdobj=new DomainObject();
            ebomdobj.setId(ebomObjectId);

            try
            {
                if (hmpDelete.containsKey(ebomObjectId + "|" + ebomRelId))
                {

                   	// Added  To Support Normal BOM Handling In Common View START
                   	int status = checkForBOMChanges(context, null, ebomRelId , null, "delete");
					if(status == 2)
					continue;
			// Added  To Support Normal BOM Handling In Common View END
                   relationshipElement = new com.matrixone.jdom.Element("relationship");
                    relationshipElement.setAttribute("chgtype","D");
                    //Auto sync start
                    if("replace".equalsIgnoreCase((String)hmpDelete.get(ebomObjectId + "|~" + ebomRelId))) {
                    	relationshipElement.setAttribute("replace","true");
                    }
                    //Auto sync Ends
                    fromRelationshipListElement.addContent(relationshipElement);

                    relationshipDefRefElement = new com.matrixone.jdom.Element("relationshipDefRef");
                    relationshipDefRefElement.setAttribute("relid",ebomRelId);
                    relationshipDefRefElement.setText(DomainConstants.RELATIONSHIP_EBOM);
                    relationshipElement.addContent(relationshipDefRefElement);

                    relatedObjectElement = new com.matrixone.jdom.Element("relatedObject");
                    relatedObjectElement.setAttribute("relatedobjid",ebomObjectId);
                    relationshipElement.addContent(relatedObjectElement);

                    businessObjectRefElement = new com.matrixone.jdom.Element("businessObjectRef");
                    relatedObjectElement.addContent(businessObjectRefElement);

                    subobjectTypeElement = new com.matrixone.jdom.Element("objectType");
                    //Start : IR-054311V6R2011x
                    subobjectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectType));
                    //End : IR-054311V6R2011x
                    businessObjectRefElement.addContent(subobjectTypeElement);

                    subobjectNameElement = new com.matrixone.jdom.Element("objectName");
                    subobjectNameElement.setAttribute("chgtype","D");
                    //Start : IR-054311V6R2011x
                    subobjectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectName));
                    //End : IR-054311V6R2011x
                    businessObjectRefElement.addContent(subobjectNameElement);

                    subobjectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
                    //Start : IR-054311V6R2011x
                    subobjectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectRevision));
                    //End : IR-054311V6R2011x
                    businessObjectRefElement.addContent(subobjectRevisionElement);

                    subvaultRefElement = new com.matrixone.jdom.Element("vaultRef");
                    //Start : IR-054311V6R2011x
                    subvaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectVault));
                    //End : IR-054311V6R2011x
                    businessObjectRefElement.addContent(subvaultRefElement);

                    UOMElement = new com.matrixone.jdom.Element("UOM");
                    UOMElement.setText(ATTRIBUTE_UNITOFMEASURE);
                    businessObjectRefElement.addContent(UOMElement);

                    objectStateElement = new com.matrixone.jdom.Element("objectState");
                    //Start : IR-054311V6R2011x
                    objectStateElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectcurrent));
                    //End : IR-054311V6R2011x
                    businessObjectRefElement.addContent(objectStateElement);

                    attributeListElement = new com.matrixone.jdom.Element("attributeList");
                    relationshipElement.addContent(attributeListElement);

                    attributeElement = new com.matrixone.jdom.Element("attribute");
                    attributeElement.setAttribute("chgtype","D");
                    attributeListElement.addContent(attributeElement);

                    nameElement = new com.matrixone.jdom.Element("name");
                    nameElement.setText(DomainConstants.ATTRIBUTE_FIND_NUMBER);
                    attributeElement.addContent(nameElement);

                    StringElement = new com.matrixone.jdom.Element("string");
                    StringElement.setText(ATTRIBUTE_FIND_NUMBER);
                    attributeElement.addContent(StringElement);

                    attributeElement = new com.matrixone.jdom.Element("attribute");
                    attributeElement.setAttribute("chgtype","D");
                    attributeListElement.addContent(attributeElement);

                    nameElement = new com.matrixone.jdom.Element("name");
                    nameElement.setText(DomainConstants.ATTRIBUTE_QUANTITY);
                    attributeElement.addContent(nameElement);

                    realElement = new com.matrixone.jdom.Element("real");
                    realElement.setText(ATTRIBUTE_QUANTITY);
                    attributeElement.addContent(realElement);

                    attributeElement = new com.matrixone.jdom.Element("attribute");
                    attributeElement.setAttribute("chgtype","D");
                    attributeListElement.addContent(attributeElement);

                    nameElement = new com.matrixone.jdom.Element("name");
                    nameElement.setText(DomainConstants.ATTRIBUTE_USAGE);
                    attributeElement.addContent(nameElement);

                     StringElement = new com.matrixone.jdom.Element("string");
                    StringElement.setText(ATTRIBUTE_USAGE);
                    attributeElement.addContent(StringElement);

                    attributeElement = new com.matrixone.jdom.Element("attribute");
                    attributeElement.setAttribute("chgtype","D");
                    attributeListElement.addContent(attributeElement);

                    nameElement = new com.matrixone.jdom.Element("name");
                    nameElement.setText(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
                    attributeElement.addContent(nameElement);

                    StringElement = new com.matrixone.jdom.Element("string");
                    StringElement.setText(ATTRIBUTE_REFERENCE_DESIGNATOR);
                    attributeElement.addContent(StringElement);

                    attributeElement = new com.matrixone.jdom.Element("attribute");
                    attributeElement.setAttribute("chgtype","D");
                    attributeListElement.addContent(attributeElement);

                    nameElement = new com.matrixone.jdom.Element("name");
                    nameElement.setText(DomainConstants.ATTRIBUTE_COMPONENT_LOCATION);
                    attributeElement.addContent(nameElement);

                    StringElement = new com.matrixone.jdom.Element("string");
                    StringElement.setText(ATTRIBUTE_COMPONENT_LOCATION);
                    attributeElement.addContent(StringElement);

                    attributeElement = new com.matrixone.jdom.Element("attribute");
                    attributeElement.setAttribute("chgtype","D");
                    attributeListElement.addContent(attributeElement);

                    nameElement = new com.matrixone.jdom.Element("name");
                    nameElement.setText(ATTRIBUTE_NOTES);
                    attributeElement.addContent(nameElement);

                    StringElement = new com.matrixone.jdom.Element("string");
                    StringElement.setText(Notes);
                    attributeElement.addContent(StringElement);
                }

                // For Change
                else if (hmpChanged.containsKey(ebomObjectId + "|" + ebomRelId))
                {
                    java.util.List objListsubel=rootElement.getChildren("object");
                    Iterator objitrsubele=objListsubel.iterator();

                     while(objitrsubele.hasNext())
                     {
                         Element elesubChild = (Element) objitrsubele.next();
                         String sObjidele=elesubChild.getAttributeValue("objectId");
                         String smarkupAttrValueele=elesubChild.getAttributeValue("markup");
                         String sRelId=elesubChild.getAttributeValue("relId");

                         if("changed".equalsIgnoreCase(smarkupAttrValueele)&& ebomObjectId.equals(sObjidele) && ebomRelId.equals(sRelId))
                         {
                             // Added To Support Normal BOM Handling In Common View START
							int status = checkForBOMChanges(context, null, ebomRelId , elesubChild, "change");
							if(status == 1)
								continue;
			    // Added  To Support Normal BOM Handling In Common View END


                             java.util.List columnchgsubele=elesubChild.getChildren("column");
                             Iterator columchgitrsubele=columnchgsubele.iterator();
                             HashMap chgcolvaluessubele=new HashMap();

                             while(columchgitrsubele.hasNext())
                             {
                                 Element subcol=(Element)columchgitrsubele.next();
                                 String chgColName=subcol.getAttributeValue("name");
                                 String chgColValue=subcol.getText();
                                 chgcolvaluessubele.put(chgColName,chgColValue);

                             }

                             relationshipElement = new com.matrixone.jdom.Element("relationship");
                             relationshipElement.setAttribute("chgtype","C");
                             fromRelationshipListElement.addContent(relationshipElement);


                             relationshipDefRefElement = new com.matrixone.jdom.Element("relationshipDefRef");
                             relationshipDefRefElement.setAttribute("relid",ebomRelId);
                             relationshipDefRefElement.setText(DomainConstants.RELATIONSHIP_EBOM);
                             relationshipElement.addContent(relationshipDefRefElement);

                             relatedObjectElement = new com.matrixone.jdom.Element("relatedObject");
                             relatedObjectElement.setAttribute("relatedobjid",ebomObjectId);
                             relationshipElement.addContent(relatedObjectElement);

                             businessObjectRefElement = new com.matrixone.jdom.Element("businessObjectRef");
                             relatedObjectElement.addContent(businessObjectRefElement);

                             subobjectTypeElement = new com.matrixone.jdom.Element("objectType");
                             //Start : IR-054311V6R2011x
                             subobjectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectType));
                             //End : IR-054311V6R2011x
                             businessObjectRefElement.addContent(subobjectTypeElement);

                             subobjectNameElement = new com.matrixone.jdom.Element("objectName");
                             //Start : IR-054311V6R2011x
                             subobjectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectName));
                             //End : IR-054311V6R2011x
                             businessObjectRefElement.addContent(subobjectNameElement);

                             subobjectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
                             //Start : IR-054311V6R2011x
                             subobjectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectRevision));
                             //End : IR-054311V6R2011x
                             businessObjectRefElement.addContent(subobjectRevisionElement);

                             subvaultRefElement = new com.matrixone.jdom.Element("vaultRef");
                             //Start : IR-054311V6R2011x
                             subvaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectVault));
                             //End : IR-054311V6R2011x
                             businessObjectRefElement.addContent(subvaultRefElement);

                             UOMElement = new com.matrixone.jdom.Element("UOM");
                             UOMElement.setText(ATTRIBUTE_UNITOFMEASURE);
                             businessObjectRefElement.addContent(UOMElement);


                             objectStateElement = new com.matrixone.jdom.Element("objectState");
                             //Start : IR-054311V6R2011x
                             objectStateElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectcurrent));
                             //End : IR-054311V6R2011x
                             businessObjectRefElement.addContent(objectStateElement);

                             attributeListElement = new com.matrixone.jdom.Element("attributeList");
                             relationshipElement.addContent(attributeListElement);

                             attributeElement = new com.matrixone.jdom.Element("attribute");

                            if (chgcolvaluessubele.containsKey("Find Number")){
                            	attributeElement.setAttribute("chgtype","C");
                             }
                             attributeListElement.addContent(attributeElement);

                             nameElement = new com.matrixone.jdom.Element("name");
                             nameElement.setText(DomainConstants.ATTRIBUTE_FIND_NUMBER);
                             attributeElement.addContent(nameElement);

                             StringElement = new com.matrixone.jdom.Element("string");
                             StringElement.setText(ATTRIBUTE_FIND_NUMBER);
                             attributeElement.addContent(StringElement);

                            Element oldvalue=new Element("oldvalue");
                            Element newvalue=new Element("newvalue");
                            if (chgcolvaluessubele.containsKey("Find Number"))
                            {

                             oldvalue.setText(ATTRIBUTE_FIND_NUMBER);
                             attributeElement.addContent(oldvalue);

                             newvalue.setText((String)chgcolvaluessubele.get("Find Number"));
                             attributeElement.addContent(newvalue);
                            }

                             attributeElement = new com.matrixone.jdom.Element("attribute");
                            if (chgcolvaluessubele.containsKey("Quantity"))
                            {
                             attributeElement.setAttribute("chgtype","C");
                            }
                             attributeListElement.addContent(attributeElement);

                             nameElement = new com.matrixone.jdom.Element("name");
                             nameElement.setText(DomainConstants.ATTRIBUTE_QUANTITY);
                             attributeElement.addContent(nameElement);

                             realElement = new com.matrixone.jdom.Element("real");
                             realElement.setText(ATTRIBUTE_QUANTITY);
                             attributeElement.addContent(realElement);

                            if (chgcolvaluessubele.containsKey("Quantity"))
                            {

                             oldvalue=new Element("oldvalue");
                             oldvalue.setText(ATTRIBUTE_QUANTITY);
                             attributeElement.addContent(oldvalue);

                             newvalue=new Element("newvalue");
                             newvalue.setText((String)chgcolvaluessubele.get("Quantity"));
                             attributeElement.addContent(newvalue);

                            }
                             attributeElement  = new com.matrixone.jdom.Element("attribute");

                            if (chgcolvaluessubele.containsKey("Usage"))
                            {
                             attributeElement.setAttribute("chgtype","C");
                         }
                             attributeListElement .addContent(attributeElement);

                             nameElement  = new com.matrixone.jdom.Element("name");
                             nameElement.setText(DomainConstants.ATTRIBUTE_USAGE);
                             attributeElement .addContent(nameElement);

                             StringElement = new com.matrixone.jdom.Element("string");
                             StringElement.setText(ATTRIBUTE_USAGE);
                             attributeElement.addContent(StringElement);

                            if (chgcolvaluessubele.containsKey("Usage"))
                            {
                             oldvalue=new Element("oldvalue");
                             oldvalue.setText(ATTRIBUTE_USAGE);
                             attributeElement.addContent(oldvalue);

                             newvalue=new Element("newvalue");
                             newvalue.setText((String)chgcolvaluessubele.get("Usage"));
                             attributeElement.addContent(newvalue);
                            }

                             //continue from here

                             attributeElement = new com.matrixone.jdom.Element("attribute");

                            if (chgcolvaluessubele.containsKey("Reference Designator"))
                            {
                             attributeElement.setAttribute("chgtype","C");
                         }
                             attributeListElement.addContent(attributeElement);

                             nameElement = new com.matrixone.jdom.Element("name");
                             nameElement.setText(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
                             attributeElement.addContent(nameElement);

                             StringElement = new com.matrixone.jdom.Element("string");
                             StringElement.setText(ATTRIBUTE_REFERENCE_DESIGNATOR);
                             attributeElement.addContent(StringElement);

                            if (chgcolvaluessubele.containsKey("Reference Designator"))
                            {

                             oldvalue=new Element("oldvalue");
                             oldvalue.setText(ATTRIBUTE_REFERENCE_DESIGNATOR);
                             attributeElement.addContent(oldvalue);

                             newvalue=new Element("newvalue");
                             newvalue.setText((String)chgcolvaluessubele.get("Reference Designator"));
                             attributeElement.addContent(newvalue);
                            }

                             attributeElement = new com.matrixone.jdom.Element("attribute");
                            if (chgcolvaluessubele.containsKey("Component Location"))
                            {

                             attributeElement.setAttribute("chgtype","C");
                         }
                             attributeListElement.addContent(attributeElement);

                             nameElement = new com.matrixone.jdom.Element("name");
                             nameElement.setText(DomainConstants.ATTRIBUTE_COMPONENT_LOCATION);
                             attributeElement.addContent(nameElement);

                             StringElement = new com.matrixone.jdom.Element("string");
                             StringElement.setText(ATTRIBUTE_COMPONENT_LOCATION);
                             attributeElement.addContent(StringElement);

                            if (chgcolvaluessubele.containsKey("Component Location"))
                            {

                             oldvalue=new Element("oldvalue");
                             oldvalue.setText(ATTRIBUTE_COMPONENT_LOCATION);
                             attributeElement.addContent(oldvalue);

                             newvalue=new Element("newvalue");
                             newvalue.setText((String)chgcolvaluessubele.get("Component Location"));
                             attributeElement.addContent(newvalue);
                         }

                             attributeElement = new com.matrixone.jdom.Element("attribute");

                             attributeListElement.addContent(attributeElement);

                             nameElement = new com.matrixone.jdom.Element("name");
                             nameElement.setText(ATTRIBUTE_NOTES);
                             attributeElement.addContent(nameElement);

                             attributeElement = new com.matrixone.jdom.Element("attribute");

                             if (chgcolvaluessubele.containsKey(ATTRIBUTE_NOTES)){
                             	attributeElement.setAttribute("chgtype","C");
                              }
                              attributeListElement.addContent(attributeElement);

                              nameElement = new com.matrixone.jdom.Element("name");
                              nameElement.setText(ATTRIBUTE_NOTES);
                              attributeElement.addContent(nameElement);


                              StringElement = new com.matrixone.jdom.Element("string");
                              StringElement.setText(Notes);
                              attributeElement.addContent(StringElement);

                             if (chgcolvaluessubele.containsKey(ATTRIBUTE_NOTES))
                             {
                                  oldvalue=new Element("oldvalue");
	                              oldvalue.setText(Notes);
	                              attributeElement.addContent(oldvalue);
	                              newvalue=new Element("newvalue");
	                              newvalue.setText((String)chgcolvaluessubele.get(ATTRIBUTE_NOTES));
	                              attributeElement.addContent(newvalue);
                             }
                             
                             //UOM Management - start
                             attributeElement  = new com.matrixone.jdom.Element("attribute");

                             if (chgcolvaluessubele.containsKey(EngineeringConstants.UOM))
                             {
                              attributeElement.setAttribute("chgtype","C");
                             }
                              attributeListElement .addContent(attributeElement);

                              nameElement  = new com.matrixone.jdom.Element("name");
                              nameElement.setText(EngineeringConstants.UOM);
                              attributeElement .addContent(nameElement);

                              StringElement = new com.matrixone.jdom.Element("string");
                              StringElement.setText(ATTRIBUTE_UNITOFMEASURE);
                              attributeElement.addContent(StringElement);

                             if (chgcolvaluessubele.containsKey(EngineeringConstants.UOM))
                             {
                              oldvalue=new Element("oldvalue");
                              oldvalue.setText(ATTRIBUTE_UNITOFMEASURE);
                              attributeElement.addContent(oldvalue);

                              newvalue=new Element("newvalue");
                              newvalue.setText((String)chgcolvaluessubele.get(EngineeringConstants.UOM));
                              attributeElement.addContent(newvalue);
                             }
                             //UOM Management - End

                             break;

                             //end of If Loop
                         }
                             //End of While Loop while(objitrsubele.hasNext())
                         }

                      // End of  else if (hmpChanged.containsKey(ebomObjectId + "|" + ebomRelId))
                }
                //  If Key doesnt matches the delete and Change Map construct it in normal way
                else
                {
                relationshipElement = new com.matrixone.jdom.Element("relationship");
                fromRelationshipListElement.addContent(relationshipElement);

                relationshipDefRefElement = new com.matrixone.jdom.Element("relationshipDefRef");
                relationshipDefRefElement.setAttribute("relid",ebomRelId);
                relationshipDefRefElement.setText(DomainConstants.RELATIONSHIP_EBOM);
                relationshipElement.addContent(relationshipDefRefElement);

                relatedObjectElement = new com.matrixone.jdom.Element("relatedObject");
                relatedObjectElement.setAttribute("relatedobjid",ebomObjectId);
                relationshipElement.addContent(relatedObjectElement);

                businessObjectRefElement = new com.matrixone.jdom.Element("businessObjectRef");
                relatedObjectElement.addContent(businessObjectRefElement);

                subobjectTypeElement = new com.matrixone.jdom.Element("objectType");
                //Start : IR-054311V6R2011x
                subobjectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectType));
                //End : IR-054311V6R2011x
                businessObjectRefElement.addContent(subobjectTypeElement);

                subobjectNameElement = new com.matrixone.jdom.Element("objectName");
                //Start : IR-054311V6R2011x
                subobjectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectName));
                //End : IR-054311V6R2011x
                businessObjectRefElement.addContent(subobjectNameElement);

                subobjectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
                //Start : IR-054311V6R2011x
                subobjectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectRevision));
                //End : IR-054311V6R2011x
                businessObjectRefElement.addContent(subobjectRevisionElement);

                subvaultRefElement = new com.matrixone.jdom.Element("vaultRef");
                //Start : IR-054311V6R2011x
                subvaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectVault));
                //End : IR-054311V6R2011x
                businessObjectRefElement.addContent(subvaultRefElement);

                UOMElement = new com.matrixone.jdom.Element("UOM");
                UOMElement.setText(ATTRIBUTE_UNITOFMEASURE);
                businessObjectRefElement.addContent(UOMElement);

                objectStateElement = new com.matrixone.jdom.Element("objectState");
                //Start : IR-054311V6R2011x
                objectStateElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectcurrent));
                //End : IR-054311V6R2011x
                businessObjectRefElement.addContent(objectStateElement);

                attributeListElement = new com.matrixone.jdom.Element("attributeList");
                relationshipElement.addContent(attributeListElement);

                attributeElement = new com.matrixone.jdom.Element("attribute");
                attributeListElement.addContent(attributeElement);

                nameElement = new com.matrixone.jdom.Element("name");
                nameElement.setText(DomainConstants.ATTRIBUTE_FIND_NUMBER);
                attributeElement.addContent(nameElement);

                StringElement = new com.matrixone.jdom.Element("string");
                StringElement.setText(ATTRIBUTE_FIND_NUMBER);
                attributeElement.addContent(StringElement);

                attributeElement = new com.matrixone.jdom.Element("attribute");
                attributeListElement.addContent(attributeElement);

                nameElement = new com.matrixone.jdom.Element("name");
                nameElement.setText(DomainConstants.ATTRIBUTE_QUANTITY);
                attributeElement.addContent(nameElement);

                realElement = new com.matrixone.jdom.Element("real");
                realElement.setText(ATTRIBUTE_QUANTITY);
                attributeElement.addContent(realElement);

                attributeElement = new com.matrixone.jdom.Element("attribute");
                attributeListElement.addContent(attributeElement);

                nameElement = new com.matrixone.jdom.Element("name");
                nameElement.setText(DomainConstants.ATTRIBUTE_USAGE);
                attributeElement.addContent(nameElement);

                StringElement = new com.matrixone.jdom.Element("string");
                StringElement.setText(ATTRIBUTE_USAGE);
                attributeElement.addContent(StringElement);

                attributeElement = new com.matrixone.jdom.Element("attribute");
                attributeListElement.addContent(attributeElement);

                nameElement = new com.matrixone.jdom.Element("name");
                nameElement.setText(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
                attributeElement.addContent(nameElement);

                StringElement = new com.matrixone.jdom.Element("string");
                StringElement.setText(ATTRIBUTE_REFERENCE_DESIGNATOR);
                attributeElement.addContent(StringElement);

                attributeElement = new com.matrixone.jdom.Element("attribute");
                attributeListElement.addContent(attributeElement);

                nameElement = new com.matrixone.jdom.Element("name");
                nameElement.setText(DomainConstants.ATTRIBUTE_COMPONENT_LOCATION);
                attributeElement.addContent(nameElement);

                StringElement = new com.matrixone.jdom.Element("string");
                StringElement.setText(ATTRIBUTE_COMPONENT_LOCATION);
                attributeElement.addContent(StringElement);

                attributeElement = new com.matrixone.jdom.Element("attribute");
                attributeListElement.addContent(attributeElement);

                nameElement = new com.matrixone.jdom.Element("name");
                nameElement.setText(ATTRIBUTE_NOTES);
                attributeElement.addContent(nameElement);

                StringElement = new com.matrixone.jdom.Element("string");
                StringElement.setText(Notes);
                attributeElement.addContent(StringElement);
                }
            }
            catch(Exception e)
            {
                throw e;
            }
        }

        //To add Count Attribute to FromrelationshipList element
        int finalmapListcount=ebomListcount+sbmListcount+1;

        fromRelationshipListElement.setAttribute("count",Integer.toString(finalmapListcount));

        }
        catch (Exception e)
                {
                    throw e;
                }
        }

       try
        {
            String strSetElement = "false";
            if (args.length == 4)
            {
                strSetElement = args[3];
            }

            if ("true".equals(strSetElement))
            {
                eleDocRoot = ematrixrootElement;
            }
            else
            {
                       chekinToMarkupObject(context,ematrixrootElement,smkpdomobj);
            }

        }
        catch(Exception e)
        {
            throw e;
        }

    }


    /**
     * Gets the CommandType and returns True for SaveAs Command.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing "commandType"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/

    public boolean chekinToMarkupObject(Context context, com.matrixone.jdom.Element rootElement, DomainObject domMarkup) throws Exception
    {
       try{
           matrix.db.FileList files = new matrix.db.FileList();

           // create workspace.
           String sTransPath = context.createWorkspace();
           // create a file object.
           java.io.File fEmatrixWebRoot = new java.io.File(sTransPath);

           String sXMLFormat = PropertyUtil.getSchemaProperty(context, "format_XML");

           String sbusObjMarkupName = domMarkup.getInfo(context,"name");
           java.io.File srcXMLFile = new java.io.File(fEmatrixWebRoot, sbusObjMarkupName+ ".xml");

            matrix.db.File checkinFile = new matrix.db.File(srcXMLFile.getAbsolutePath(), sXMLFormat);
            files.addElement(checkinFile);

           com.matrixone.jdom.Document docXML = new com.matrixone.jdom.Document(rootElement);
           String strEBOMCharset = "UTF-8";

           // set the font properties to xml outputter object.
           com.matrixone.jdom.output.XMLOutputter  xmlOutputter = com.matrixone.util.MxXMLUtils.getOutputter(true,strEBOMCharset);

            // create io buffer writer.
            java.io.BufferedWriter buf = new java.io.BufferedWriter(new java.io.FileWriter(srcXMLFile));

            // put xml document to putter.
            xmlOutputter.output(docXML, buf);
            buf.flush();
            buf.close();

            domMarkup.checkinFromServer(context, true, false, sXMLFormat, "", files);

        srcXMLFile.delete();
       }catch(Exception e){
           throw e;
       }
       return true;
    }

/**
* Adding and Removing for Al;ternate and Substitute Part.
* @param    context the eMatrix <code>Context</code> object
* @param    args holds a StringList containing the following entries:
* paramMap - a StringList containing all selected Markup Ids
    * @return    HashMap
* @throws    Exception if operation fails
*/

     @com.matrixone.apps.framework.ui.ConnectionProgramCallable
     public HashMap visualQuesForAlternateAndSubstitute(Context context, String args[])throws Exception
     {
 		DomainObject partObj = DomainObject.newInstance(context);
 		DomainObject selObj = DomainObject.newInstance(context);
 		HashMap programMap 		= (HashMap)JPO.unpackArgs(args);
 		HashMap paramMap = (HashMap)programMap.get("paramMap");

 		String sMBOMPlantCustomFilterVal=(String)paramMap.get("MBOMPlantCustomFilter");

 		//Getting Plant Obj Id of current Plant.
 		String sPlantObjID = getLocation(context, sMBOMPlantCustomFilterVal);

 		String relEBOMSubstitute= PropertyUtil.getSchemaProperty(context, "relationship_EBOMSubstitute");
 		String relAlternate= PropertyUtil.getSchemaProperty(context, "relationship_Alternate");
 		String relEBOM= PropertyUtil.getSchemaProperty(context, "relationship_EBOM");
 		String sRelSplitQty= PropertyUtil.getSchemaProperty(context, "relationship_EBOMSplitQuantity");
 		String sEBOMMFR= PropertyUtil.getSchemaProperty(context, "relationship_EBOMManufacturingResponsibility");
 		String sMFR= PropertyUtil.getSchemaProperty(context, "relationship_ManufacturingResponsibility");
 		String sSplitQuantityMFR= PropertyUtil.getSchemaProperty(context, "relationship_SplitQuantityManufacturingResponsibility");
 		String sAttrSwitch= PropertyUtil.getSchemaProperty(context, "attribute_Switch");
 		String sTStartDate = PropertyUtil.getSchemaProperty(context, "attribute_StartDate");
 		String sTEndDate = PropertyUtil.getSchemaProperty(context, "attribute_EndDate");
 		String TARGET_START_DATE = "Target Start Date";
 		String TARGET_END_DATE = "Target End Date";

         HashMap doc              = new HashMap();
         MapList mlItems          = new MapList();
 		HashMap hmpAttributes = new HashMap();
         String sTotalRelId       = "";
        // Added for V6R2009.HF0.2 - Starts
        Map mapNewPrimary        = new HashMap();
        // Added for V6R2009.HF0.2 - Ends

         StringList slEBOMAttrs = new StringList();
         slEBOMAttrs.add(DomainConstants.ATTRIBUTE_FIND_NUMBER);
         slEBOMAttrs.add(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
         slEBOMAttrs.add(DomainConstants.ATTRIBUTE_QUANTITY);
         slEBOMAttrs.add(DomainConstants.ATTRIBUTE_COMPONENT_LOCATION);
         slEBOMAttrs.add(DomainConstants.ATTRIBUTE_USAGE);

         Element rootElement = (Element)programMap.get("contextData");
         DomainRelationship dmRelPartPM = null;
         String rowFormat ="";//082881V6R2012
        //get the Parent part Id from the Xml
 			String sParentOID = (String)rootElement.getAttributeValue("objectId");
 			java.util.List lCElement = rootElement.getChildren();
         try{
 			if(lCElement != null){
 				java.util.Iterator itrC 	= lCElement.iterator();
 				while(itrC.hasNext()){
 					com.matrixone.jdom.Element childCElement = (com.matrixone.jdom.Element)itrC.next();
 					String sObjectId = (String)childCElement.getAttributeValue("objectId");
                    String strRelId     = (String)childCElement.getAttributeValue("relId");
                    String sRowId       = (String)childCElement.getAttributeValue("rowId");
                    rowFormat = "[rowId:" + sRowId + "]";
                    String sRelTypeSymb = (String)childCElement.getAttributeValue("relType");
                    String markup       = (String)childCElement.getAttributeValue("markup");
					String strParam2    = (String)childCElement.getAttributeValue("param2");//added for bug 360985
                    String sRelType     = null;
 					String sRelId = "";

                    if(sRelTypeSymb != null){
                        StringList slSplit = FrameworkUtil.split(sRelTypeSymb,"|");
                        sRelTypeSymb = (String)slSplit.get(0);
                        sRelType     = (String)PropertyUtil.getSchemaProperty(context,sRelTypeSymb);
                        if(slSplit.size()>1){
                            sRelId = (String)slSplit.get(1);
                            // Added for V6R2009.HF0.2 - Starts
                            int index = sRelId.indexOf('^');
                            if(index != -1){
                                sRelId = sRelId.substring(index+1, sRelId.length());
                                sRelId = (String)mapNewPrimary.get(sRelId);
                            }
                            if(sRelId == null || "null".equals(sRelId)){
                                continue;
                            }
                            // Added for V6R2009.HF0.2 - Ends
                        }
                    }

                    if ("add".equals(markup)){
                         //Logic for ur ADD Opearation
                         DomainObject parentObj = DomainObject.newInstance(context);
                         parentObj.setId(sParentOID);

                         //get the attributes information
 						List lCHild = childCElement.getChildren("column");
 						Iterator itrChilds = lCHild.iterator();
 							while (itrChilds.hasNext()){
 								Element eleAttr = (Element) itrChilds.next();
 								String strAttrValue = eleAttr.getText();
 								String strAttrName = eleAttr.getAttribute("name").getValue();

                             if(TARGET_START_DATE.equals(strAttrName) || TARGET_END_DATE.equals(strAttrName)){
                                 if(TARGET_START_DATE.equals(strAttrName)){
											strAttrName = sTStartDate;
										}
                                 else{
											strAttrName = sTEndDate;
										}

                                 if(strAttrValue != null && !"".equals(strAttrValue)){
											double iClientTimeOffset = (new Double((String) paramMap.get("timeZone"))).doubleValue();
											strAttrValue= eMatrixDateFormat.getFormattedInputDate(strAttrValue,iClientTimeOffset,(java.util.Locale)(context.getLocale()));
										}
								}

 								   hmpAttributes.put(strAttrName, strAttrValue);
 							    }

						if(hmpAttributes.containsKey("Manufacturing Part Usage")) {
							hmpAttributes.remove("Manufacturing Part Usage");
						}

                         if(sRelType!=null){

                        	//Multitenant
                        	 String sMsg =EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", context.getLocale(),"emxMBOM.CommonView.NoMFR");

 //If selected part is Alternate
                             if(sRelType.equalsIgnoreCase(relAlternate)){

                                 //filter the EBOM Attributes from the HashMap "hmpAttributes"
                                 for(int indx=0;indx<slEBOMAttrs.size();indx++){
                                     String sAttrName = (String)slEBOMAttrs.get(indx);
                                     if(hmpAttributes.containsKey(sAttrName)){
                                         hmpAttributes.remove(sAttrName);
					}
 	}

 				String sMQLStatement = "print connection $1 select $2 dump $3 ;";
 				String slResult = MqlUtil.mqlCommand(context,sMQLStatement,sRelId,"to.id","|").trim();
 				partObj.setId(slResult);
 				selObj.setId(sObjectId);

                                 dmRelPartPM = DomainRelationship.connect(context,
                                                                          partObj,
                                                                          sRelType,
                                                                          selObj);

 				//Getting Alrernate Manufacturing Responsibility connected with selected Alternate Part Id.
 			String sQuery0= "print connection $1 select $2 dump $3;";
 			String sAltManuResRelIdResult=MqlUtil.mqlCommand(context,sQuery0,dmRelPartPM.toString(),"tomid.id","|");

 			//Getting one by one all connected Alternate Manufacturing Responsibility with created Alternate Rel Id in final StringList.
 			StringList slAltsManuResAttr = FrameworkUtil.split(sAltManuResRelIdResult,"|");
 			int size=slAltsManuResAttr.size();
 			for (int i=0;i<size;i++){
 			String sAMFRrelId = (String) slAltsManuResAttr.get(i);
 			String sQuery1="print connection $1 select $2 dump $3;";
 			String sAlternatePlantResult=MqlUtil.mqlCommand(context,sQuery1,sAMFRrelId,"fromrel.from.id","|");

 				//Add only those Alternate Manufacturing Responsibility which is assigned only for current Plant.
 			if(sAlternatePlantResult.equals(sPlantObjID)){
 				sTotalRelId=sAMFRrelId;
                                     }
                                 }

                                 if(sTotalRelId!=null & !("").equals(sTotalRelId)){
 					ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
 					DomainRelationship domSubRel      = new DomainRelationship(sTotalRelId);

 					//setting all the attributre value for Alternate Manu Res relationship.
 					domSubRel.setAttributeValues(context,hmpAttributes);
 					ContextUtil.popContext(context);
 			}
                                 else{
					MqlUtil.mqlCommand(context, "notice $1",sMsg);
			    }
                             }//End of if(sRelType.equalsIgnoreCase(relAlternate))

 			//If selected part is Substitute
                             else if(sRelType.equalsIgnoreCase(relEBOMSubstitute)){
								 Relationship ebomRel = new Relationship(sRelId);

                                 AttributeList ebomAttList = ebomRel.getAttributeValues(context, slEBOMAttrs);
								 AttributeItr strItrAttList = new AttributeItr(ebomAttList);
								 strItrAttList.next();
								 String sFindNumberValue = strItrAttList.obj().getValue();
								 strItrAttList.next();
								 String sRefDesValue = strItrAttList.obj().getValue();
								 strItrAttList.next();
								 String sQuantityValue = strItrAttList.obj().getValue();
								 strItrAttList.next();
								 String sComponentLocation = strItrAttList.obj().getValue();
								 strItrAttList.next();
								 String sUsage = strItrAttList.obj().getValue();

								 Map ebomSubMap=new HashMap();
								 ebomSubMap.put(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR,sRefDesValue);
								 ebomSubMap.put(DomainConstants.ATTRIBUTE_FIND_NUMBER,sFindNumberValue);
								 ebomSubMap.put(DomainConstants.ATTRIBUTE_QUANTITY,sQuantityValue);
								 ebomSubMap.put(DomainConstants.ATTRIBUTE_COMPONENT_LOCATION,sComponentLocation);
			 					ebomSubMap.put(DomainConstants.ATTRIBUTE_USAGE,sUsage);

                                 //filter the EBOM Attributes from the HashMap "hmpAttributes"
                                 for(int indx=0;indx<slEBOMAttrs.size();indx++){
                                     String sAttrName = (String)slEBOMAttrs.get(indx);
                                     if(hmpAttributes.containsKey(sAttrName)){
                                         hmpAttributes.remove(sAttrName);
				}
				}

                                 RelToRelUtil relObj=new RelToRelUtil();
                                 String strConnectSubPart = (String)relObj.connect(context,
                                                                                   sRelType,
                                                                                   sRelId,
                                                                                   sObjectId,
                                                                                   false,
                                                                                   true);

                                 dmRelPartPM = new DomainRelationship(strConnectSubPart);

 			String sQuery0= "print connection $1 select $2 dump $3;";
 			String sSubsManuResRelIdResult=MqlUtil.mqlCommand(context,sQuery0,strConnectSubPart,"tomid.id","|");

 			//Getting one by one all connected Substitute Manufacturing Responsibility with selected EBOM Rel Id in final StringList.
 			StringList slSubsManuResAttr = FrameworkUtil.split(sSubsManuResRelIdResult,"|");
 			int size=slSubsManuResAttr.size();
 			for (int i=0;i<size;i++){
 			String sSMFRrelId = (String) slSubsManuResAttr.get(i);
 			String sQuery1="print connection $1 select $2 dump $3;";
 			String sSubstitutePlantResult=MqlUtil.mqlCommand(context,sQuery1,sSMFRrelId,"fromrel.from.id","|");

 			//Add only those Substitute Manufacturing Responsibility which is assigned only for current Plant.
 			if(sSubstitutePlantResult.equals(sPlantObjID)){
 			 sTotalRelId=sSMFRrelId;
                                     }
                                 }

			 ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
 			 DomainRelationship domRel      = new DomainRelationship(strConnectSubPart);
			 domRel.setAttributeValues(context,ebomSubMap);

                                 if(sTotalRelId!=null & !("").equals(sTotalRelId)){
 			   DomainRelationship domSubRel      = new DomainRelationship(sTotalRelId);
 			   //setting all the attributre value for Substitute Manu Res relationship.
 			   domSubRel.setAttributeValues(context,hmpAttributes);
 			}
                                 else{
                            MqlUtil.mqlCommand(context, "notice $1",sMsg);
			}
			 ContextUtil.popContext(context);
                             }//End of if(sRelType.equalsIgnoreCase(relEBOMSubstitute))

			  //Added for Primary Parts
                             else if(relEBOM.equalsIgnoreCase(sRelType)){

					Map hmpRelToRelMap=new HashMap();

                                if(hmpAttributes.containsKey("Stype")){
						hmpRelToRelMap.put("Stype",(String)hmpAttributes.get("Stype"));
						hmpAttributes.remove("Stype");
					}

                                if(hmpAttributes.containsKey("Switch")){
						hmpRelToRelMap.put("Switch",(String)hmpAttributes.get("Switch"));
						hmpAttributes.remove("Switch");
					}

                                if(hmpAttributes.containsKey(sTStartDate)){
						hmpRelToRelMap.put(sTStartDate,(String)hmpAttributes.get(sTStartDate));
						hmpAttributes.remove(sTStartDate);
					}

                                if(hmpAttributes.containsKey(sTEndDate)){
						hmpRelToRelMap.put(sTEndDate,(String)hmpAttributes.get(sTEndDate));
						hmpAttributes.remove(sTEndDate);
					}

                                if(hmpAttributes.containsKey("Auth Status")){
						hmpAttributes.remove("Auth Status");
					}

                                if(hmpAttributes.containsKey("Pref Rank")){
						hmpAttributes.remove("Pref Rank");
					}

					ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
					RelToRelUtil relObj=new RelToRelUtil();
                                String sRelid = relObj.connect(context,
                                                               sRelType,
                                                               sParentOID,
                                                               sObjectId,
                                                               true,
                                                               true);

                                dmRelPartPM = new DomainRelationship(sRelid);

					relObj.setAttributeValues(context,sRelid,hmpAttributes);
					//added code for bug 360985 - starts
					//for derived parts information
					if (strParam2 != null && strParam2.length() > 0)
					{
					    // 372458
                        String connectAsDerived = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReplaceBOM.Derived");
                        if(connectAsDerived.equalsIgnoreCase("true")){
                            String attrDerivedContext= PropertyUtil.getSchemaProperty(context,"attribute_DerivedContext");
                            DomainRelationship doRelDerived = DomainRelationship.connect(context,
                                    new DomainObject(strParam2),
                                    RELATIONSHIP_DERIVED,
                                    new DomainObject(sObjectId));
                            doRelDerived.setAttributeValue(context, attrDerivedContext, "Replace");
                        }
					}
					//added code for bug 360985 - ends
					String relIds[] = new String[1];
					relIds[0] = sRelid;
					StringList slSelects = new StringList("tomid["+sEBOMMFR+"|fromrel["+sMFR+"].from.id=='"+sPlantObjID+"'].id");
					MapList mlList = DomainRelationship.getInfo(context, relIds, slSelects);

                                if(mlList.size() > 0){
						Map newMap = (Map) mlList.get(0);
						String sMRrelId = (String) newMap.get("tomid["+sEBOMMFR+"].id");
                                    if (sMRrelId != null && !"".equals(sMRrelId)){
							relObj.setAttributeValues(context,sMRrelId,hmpRelToRelMap);
						}
					}
					ContextUtil.popContext(context);
                                // Added for V6R2009.HF0.2 - Starts
                                String sFN = (String)hmpAttributes.get(DomainConstants.ATTRIBUTE_FIND_NUMBER);
                                String sRD = (String)hmpAttributes.get(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
                                mapNewPrimary.put(sObjectId+"^"+sFN+"^"+sRD, sRelid);
                                // Added for V6R2009.HF0.2 - Ends
                             }//End of else if(relEBOM.equalsIgnoreCase(sRelType))

                             // Split Quantity
                             else if(sRelType.equalsIgnoreCase(sRelSplitQty)){
				RelToRelUtil relObj=new RelToRelUtil();
                                 String sRelid = relObj.connect(context,
                                                                sRelType,
                                                                sRelId,
                                                                sObjectId,
                                                                false,
                                                                true);

                                 dmRelPartPM = new DomainRelationship(sRelid);
				String sSwitch = (String) hmpAttributes.get(sAttrSwitch);

                                 if ("Yes".equalsIgnoreCase(sSwitch)){
					String relIds[] = new String[1];
					relIds[0] = sRelId;
					StringList slSelects = new StringList("tomid["+sEBOMMFR+"|fromrel["+sMFR+"].from.id=='"+sPlantObjID+"'].fromrel.id");
					MapList mlList = DomainRelationship.getInfo(context, relIds, slSelects);

                                     if (mlList.size() > 0){
						Map newMap = (Map) mlList.get(0);
						String sMRrelId = (String) newMap.get("tomid["+sEBOMMFR+"].fromrel.id");

                                         if (sMRrelId != null && !"".equals(sMRrelId)){

                                                 relObj.connect(context,
                                                                                      sSplitQuantityMFR,
                                                                                      sMRrelId,
                                                                                      sRelid,
                                                                                      false,
                                                                                      false);
					}
				}
			}
                             }//End of else if(sRelType.equalsIgnoreCase(sRelSplitQty))
                         }//End of if(sRelType!=null)

                         //creating a returnMap having all the details abt the changed row.
                         HashMap returnMap = new HashMap();
                         returnMap.put("oid", sObjectId);
                         returnMap.put("rowId", sRowId);
                         returnMap.put("pid", parentObj.getId(context));
                         returnMap.put("relid", dmRelPartPM.toString());
                         returnMap.put("markup", markup);
                         returnMap.put("columns", hmpAttributes);
                         mlItems.add(returnMap);  //returnMap having all the details abt the changed row.
 		}
                    else if ("cut".equals(markup)){
			String sMQLStatement = "print connection $1 select $2 dump $3;";

			String errors = EMPTY_STRING;
 			try {
 				MqlUtil.mqlCommand(context,sMQLStatement,strRelId,"id","|");

			} catch(Exception ee) {
				errors = "ERROR";
			}
			if (!errors.trim().equals(EMPTY_STRING)) {
				continue;
			}
 			DomainRelationship doRel=new DomainRelationship(strRelId);
 			doRel.open(context);
 			String strRelType = doRel.getTypeName();
 			doRel.close(context);

                         if(strRelId!=null && strRelType!=null){
                             // EBOM Substitute relationship
 			if(strRelType.equals(relEBOMSubstitute)) {
 								RelToRelUtil relObj=new RelToRelUtil();
 								relObj.disconnect(context, strRelId);
 					}
                             // Alternate or EBOM relationship
                             else if(strRelType.equals(relAlternate) || strRelType.equals(relEBOM)){
 					ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
 					DomainRelationship.disconnect(context,strRelId);
 					ContextUtil.popContext(context);
 			}
                             // Split quantity relationship
                             else if(strRelType.equalsIgnoreCase(sRelSplitQty)){
				ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
				DomainObject doObj = new DomainObject(sObjectId);
 				doObj.deleteObject(context);
 				ContextUtil.popContext(context);
 	}
                         }//End of if(strRelId!=null && strRelType!=null)

                         //creating a returnMap having all the details abt the changed row.
                         HashMap returnMap = new HashMap();
                         returnMap.put("oid", sObjectId);
                         returnMap.put("rowId", sRowId);
                         returnMap.put("relid", strRelId);
                         returnMap.put("markup", markup);
                         returnMap.put("columns", hmpAttributes);
                         mlItems.add(returnMap);
 }
                    else if ("resequence".equals(markup)){
                         //Logic for ur resequence Opearation
                         //creating a returnMap having all the details abt the changed row.
                         HashMap returnMap = new HashMap();
                         returnMap.put("oid", sObjectId);
                         returnMap.put("rowId", sRowId);
                         returnMap.put("relid", sRelId);
                         returnMap.put("markup", markup);
                         returnMap.put("columns", hmpAttributes);
                         mlItems.add(returnMap);
                    }
                }//End of while loop
            }//End of if(lCElement != null)

            doc.put("Action", "success"); //Here the action can be "Success" or "refresh"
            doc.put("changedRows", mlItems);//Adding the key "ChangedRows" which having all the data for changed Rows
 }
        catch(Exception e){
            doc.put("Action", "ERROR"); // If any exeception is there send "Action" as "ERROR"
            doc.put("Message", rowFormat +e.getMessage()); // Error message to Display
         }
        return doc;
}//End of Method

    /*
     * This method used to store some data that can be used in Creation/editing of
     * Plant Item Markup
     * @param context, args
     * @return String value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     * Note: as of now this method is no where is used. if webform comes in to the picture in future
     *       for Change Process then this method is requred.
     */
    public String generateXMLforMassUpdateAction(Context context, String[] args) throws Exception
    {
        HashMap programMap           = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap           = (HashMap)programMap.get("requestMap");
        String sMassUpdateAction     = (String)requestMap.get("massUpdateAction");
        String sPlantIds             = (String)requestMap.get("filterPlants");
        String sReleasedPlants       = (String)requestMap.get("releasedPlants");
        String sRequestedFor         = (String)requestMap.get("requestedFor");
        String strHidden             = "";

        strHidden += "<input type='hidden' name='massUpdateAction' id='massUpdateAction' value='"+sMassUpdateAction+"'></input>";
        strHidden += "<input type='hidden' name='filterPlants' id='filterPlants' value='"+sPlantIds+"'></input>";
        strHidden += "<input type='hidden' name='releasedPlants' id='releasedPlants' value='"+sReleasedPlants+"'></input>";
        strHidden += "<input type='hidden' name='requestedFor' id='requestedFor' value='"+sRequestedFor+"'></input>";
        if(sReleasedPlants != null && !sReleasedPlants.equals("")){

        	//Multitenant
        	String sNoteMsg = EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource",
        			context.getLocale(),"emxMBOM.Markup.MarkupNoteMessage");

            strHidden += sNoteMsg+" "+sReleasedPlants;
        }
        return strHidden;
    }


    /*
     * This method used to Create/edit the Plant Item Markup
     * @param context, args
     * @return void
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
  public Object createPlantItemMarkup(Context context, String[] args) throws Exception
   {
         HashMap programMap         = (HashMap)JPO.unpackArgs(args);
         HashMap requestMap         = (HashMap)programMap.get("requestMap");
         String sMassUpdateAction   = (String)requestMap.get("massUpdateAction");
         if(sMassUpdateAction == null || (sMassUpdateAction != null && sMassUpdateAction.equals(""))){
             HashMap paramMap = (HashMap)programMap.get("paramMap");
             sMassUpdateAction = (String)paramMap.get("massUpdateAction");
         }

         String sNoteMsg			= null;
         String sChangeID           = (String)requestMap.get("ChangeOID");
         String sPlantIds           = (String)requestMap.get("filterPlants");
         String sRequestFor         = (String)requestMap.get("requestedFor");
         if(sRequestFor == null || "null".equals(sRequestFor)){
             sRequestFor = "create";
         }

         String sPartObjectID       = (String)requestMap.get("objectId");
		 String[] inputArgs = new String[2];

		 inputArgs[0]= sPartObjectID;
		 inputArgs[1]=sChangeID;

		 String strNewPartId = null;

		 if (sChangeID != null)
		 {
		 	strNewPartId = (String) JPO.invoke(context, "enoEngChange", null, "getDirectAffectedItems", inputArgs,String.class);
		 }

		 if (strNewPartId != null)
		 {
		 	sPartObjectID = strNewPartId;
		 }
         String sDescription        = (String)requestMap.get("Description");

         DomainObject domPart       = null;
         DomainObject domChange     = null;
         DomainObject domMarkup     = null;
         String sMarkupId           = null;
         MapList mlMarkup           = new MapList();
         MapList mlPlants           = new MapList();
         DomainRelationship drRel   = null;
         java.io.File srcXMLFile    = null;
         com.matrixone.jdom.Document docXML   = null;
         StringBuffer sQuery        = new StringBuffer(32);
         StringList slSelects       = new StringList();
         com.matrixone.jdom.Element rootElement = null;

         try{
            domPart                 = new DomainObject(sPartObjectID);
            domChange               = new DomainObject(sChangeID);

            //The following query checks the Change and part are already connedted to the Markup or not
            /*Example: "print bus 59166.14867.302.4772 select from[Applied Markup|to.to[EBOM Markup].from.id=='59166.14867.61381.55923'&&to.type=='Plant Item Markup'].to.id dump"*/
            sQuery.append("print bus ");
            sQuery.append("$1");
            sQuery.append(" select ");
            sQuery.append("$2");
            sQuery.append(" dump");

            StringBuffer param = new StringBuffer(32);  //creating the 2nd parameters of query
            param.append("from[");
            param.append(RELATIONSHIP_APPLIED_MARKUP);
            param.append('|');
            param.append("to.to[");
            param.append(DomainConstants.RELATIONSHIP_EBOM_MARKUP);
            param.append("].");
            param.append(DomainConstants.SELECT_FROM_ID);
            param.append("=='");
            param.append(sPartObjectID);
            param.append("'&&");
            param.append(DomainConstants.SELECT_TO_TYPE);
            param.append("=='");
            param.append(TYPE_PLANT_ITEM_MARKUP);
            param.append("'].");
            param.append(DomainConstants.SELECT_TO_ID);

            // if already to the Change and Part get the Markup Id
            // DataBase hit
            sMarkupId = MqlUtil.mqlCommand(context, sQuery.toString(), sChangeID, param.toString());

            // To create a new markup
            if(sMarkupId == null || sMarkupId.equals("")){
                // Create a new markup and connect that to change and part
                sMarkupId = createAndConnectMarkup(context, domPart, domChange);

                //create root element:businessObject and set the data
                rootElement   = new com.matrixone.jdom.Element("businessObject");
                rootElement.setAttribute("id",sPartObjectID);
                domMarkup     = new DomainObject(sMarkupId);
                domMarkup.setDescription(context, sDescription);
            }

            // To edit the existing markup
            else{
                // if markup is already created get the root element from the markup
                domMarkup      = new DomainObject(sMarkupId);
                srcXMLFile     = getRootElementFromXMLFile(context, domMarkup);
                com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder();
				builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
				builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                docXML         = builder.build(srcXMLFile);
                rootElement = docXML.getRootElement();

                com.matrixone.jdom.output.XMLOutputter  xmlOutputter     = com.matrixone.util.MxXMLUtils.getOutputter(true,"UTF-8");
                java.io.BufferedWriter buf                     = new java.io.BufferedWriter(new java.io.FileWriter(srcXMLFile));
                xmlOutputter.output(docXML, buf);
                buf.flush();
                buf.close();
            }

            // check for the duplicates in the markup root element
            String sOrgDupPlants          = checkForDuplicates(context, sPlantIds, rootElement);
            StringList slOrgDupPlants     = FrameworkUtil.split((" "+sOrgDupPlants+" "),"^");
            String sOrgPlants             = ((String)slOrgDupPlants.get(0)).trim();
            String sDupPlants             = ((String)slOrgDupPlants.get(1)).trim();

            // check if the Change is connected to part or not, if not returns the relationship name
            String sChangeConRel = isChangeConnectedToPart(context, domPart, domChange);
            if(sChangeConRel !=null && !sChangeConRel.equals("")){
                // if Change is not connected to Part, connect them with the desired relationship
                drRel = connectChangeToPart(context, domPart, domChange, sChangeConRel);
                // IR 044215V6R2011 -Starts
                if(DomainConstants.TYPE_ECO.equals(domChange.getInfo(context, DomainConstants.SELECT_TYPE)));
                {
                    String sAttrRequestedChange = PropertyUtil.getSchemaProperty(context,"attribute_RequestedChange");
                  //Multitenant
                    String sAttrRequestedChangeValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",new Locale("en"),"emxFramework.Range.Requested_Change.For_Update");
                    drRel.setAttributeValue(context, sAttrRequestedChange, sAttrRequestedChangeValue);
                }
                // IR 044215V6R2011 -Ends
            }

            // if user requests for the update make "sDupPlants" empty
            if(sRequestFor.equalsIgnoreCase("update")){
                sDupPlants ="";
            }

            // Generate maplist from the Structure browser generated XML
            mlMarkup = generateMarkupXML(context, sMassUpdateAction, sDupPlants);

            String sError = checkForLeadPlant(context, mlMarkup, rootElement);
            if(sError != null){
				return sError;
			}

            if(sRequestFor.equalsIgnoreCase("create")){
                // adds new markup relationships to the filter
                sOrgPlants = addNewPlantLinksToFilter(context, mlMarkup, sOrgPlants);
            }

            // get the plant information form the database if "sOrgPlants" is not empty
            if(sOrgPlants != null && !sOrgPlants.equals("")){
                mlPlants = getConnectedManufacturingPlants(context, sPartObjectID, sOrgPlants);
            }

            // Create initial markup
            if(!mlPlants.isEmpty()){
                rootElement = createInitialMarkupXML(context, rootElement, mlPlants);
            }

            if(!mlMarkup.isEmpty()){
                // apply the markup changes on the markup
                rootElement = applyMarkupChangesToXML(context, rootElement, mlMarkup, sRequestFor);
            }

            com.matrixone.jdom.output.XMLOutputter outputter = new com.matrixone.jdom.output.XMLOutputter();

            outputter.outputString(rootElement);

            // finally chekin the XML to the markup object
            chekinToMarkupObject(context, rootElement, domMarkup, srcXMLFile, docXML);

            // in Create mode if user tries to create the same maekup the following code is
            // executed to show warning popup
            if(!"".equals(sDupPlants)){
                String errPlants         = "";
                StringList slDupPlants   = FrameworkUtil.split(sDupPlants,"|");
                String sObjects[]        = new String[slDupPlants.size()];

                for(int indx=0;indx<slDupPlants.size();indx++){
                    String sPlantIdRel           = (String)slDupPlants.get(indx);
                    StringList slPlantIdRels     = FrameworkUtil.split((" "+sPlantIdRel+" "),",");
                    sObjects[indx]               = ((String)slPlantIdRels.get(0)).trim();
                }

                slSelects.add(DomainConstants.SELECT_NAME);

                // retrives the plant names from the database
                //DataBase hit
                MapList mlDupList = DomainObject.getInfo(context, sObjects, slSelects);
                Iterator itr = mlDupList.iterator();
                while(itr.hasNext()){
                    Map mDup = (Map)itr.next();
                    if(!"".equals(errPlants)){
                        errPlants +=", ";
                    }
                    errPlants += (String)mDup.get(DomainConstants.SELECT_NAME);
                }

              //Multitenant
                sNoteMsg =EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", context.getLocale(),"emxMBOM.Markup.PlantItemMarkupWarning");
                sNoteMsg = errPlants+" "+sNoteMsg;
            }
       }
       catch(Exception e){
           throw e;
       }
       return sNoteMsg;
   }

    /*
     * This method used to add filter to get the rel from database
     * @param context, mlMarkup, sOrgPlants
     * @return String value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public String addNewPlantLinksToFilter(Context context, MapList mlMarkup, String sOrgPlants) throws Exception
   {
       try{
           Iterator itr = mlMarkup.iterator();
           while(itr.hasNext()){
               Map mXML = (Map) itr.next();
               String sMarkup = (String)mXML.get("markup");
               String sPlantRel = (String)mXML.get("param1");
               String sPlantId = (String)mXML.get("objectId");
               if("add".equals(sMarkup)){
                   if(sOrgPlants.indexOf(sPlantId) == -1){
                       if(!"".equals(sOrgPlants)){
                           sOrgPlants += "|";
                       }
                       sOrgPlants += sPlantId+","+sPlantRel;
                   }
               }
           }
       }
       catch(Exception e){
           throw e;
       }
       return sOrgPlants;
   }

    /*
     * This method used to check the Lead Plant can be true for one row
     * @param context, mlMarkup, rootElement
     * @return String value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public String checkForLeadPlant(Context context, MapList mlMarkup, com.matrixone.jdom.Element rootElement) throws Exception
   {
	   String sError = null;
	   String sName = "";
	   DomainObject domObject = new DomainObject();

	   try{
		   Iterator itrXML = mlMarkup.iterator();
		   while(itrXML.hasNext()){
			   Map mXML = (Map) itrXML.next();
			   String sLeadPlant = (String)mXML.get(SELECT_LEAD_PLANT);
			   String sObjectId  = (String)mXML.get("objectId");

			   if(sLeadPlant == null || "null".equals(sLeadPlant) || sLeadPlant.equalsIgnoreCase("FALSE")){
				   continue;
			   }

			   java.util.List lElement  = rootElement.getChildren();
			   java.util.Iterator itr   = lElement.iterator();
			   while(itr.hasNext()){
				   com.matrixone.jdom.Element childElement = (com.matrixone.jdom.Element)itr.next();
				   String xObjectId = (String)childElement.getChild("plant").getAttributeValue("id");
				   domObject.setId(xObjectId);
				   java.util.List aElement = childElement.getChild("attributes").getChildren();
				   java.util.Iterator _itr = aElement.iterator();
				   while(_itr.hasNext()){
					   com.matrixone.jdom.Element attrElement = (com.matrixone.jdom.Element)_itr.next();
					   String vLeadStatus = (String)attrElement.getChild("newvalue").getText();
					   String nLeadStatus = (String)attrElement.getAttributeValue("name");
					   if(!sObjectId.equals(xObjectId) && nLeadStatus.equals(ATTRIBUTE_LEAD_PLANT) && sLeadPlant.equals(vLeadStatus)){
						   if(!"".equals(sName)){
							   sName += ", ";
						   }
						   sName +=domObject.getInfo(context, "name");
					   }
				   }
			   }
		   }
		   if(!"".equals(sName)){
			 //Multitenant
			   String sMessge = EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", context.getLocale(),"emxMBOM.Markup.PlantItemMarkupLeadPlantWarning");
			   sError = sName + " "+sMessge;
		   }
       }catch(Exception e){
           throw e;
       }
       return sError;
   }

    /*
     * This method used to check the duplicates in the markup XML file
     * @param context, sPlantIds, rootElement
     * @return String value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public String checkForDuplicates(Context context, String sPlantIds, com.matrixone.jdom.Element rootElement) throws Exception
   {
       String sOrgPlants = " ";
       String sDupPlants = " ";
       try{
           //sPlantIds: is a compination of objectid,relid. And this can be multiple seperated by |
           StringList slPlantIds = FrameworkUtil.split(sPlantIds,"|");
           for(int i=0;i<slPlantIds.size(); i++){
               String sPlantRelId         = (String)slPlantIds.get(i);

               StringList slPlantRelIds = FrameworkUtil.split((" "+sPlantRelId+" "),",");
               String sPlantId          = ((String)slPlantRelIds.get(0)).trim();
               String sPlantRel         = ((String)slPlantRelIds.get(1)).trim();

               java.util.List lElement  = rootElement.getChildren();
               java.util.Iterator itr   = lElement.iterator();
               boolean flag             = false;

               //cheking the XML for duplicates
               while(itr.hasNext()){
                   com.matrixone.jdom.Element childElement = (com.matrixone.jdom.Element)itr.next();
                   String sRelId     = "";
                   if(!"".equals(sPlantRel)){
                       sRelId = (String)childElement.getAttributeValue("id");
                   }

                   String sObjectId = (String)childElement.getChild("plant").getAttributeValue("id");
                   if(sObjectId.equals(sPlantId) && sRelId.equals(sPlantRel)){
                       sDupPlants = sDupPlants.trim();
                       if(!"".equals(sDupPlants)){
                           sDupPlants += "|";
                       }
                       sDupPlants += (sPlantId+","+sPlantRel);
                       flag = true;
                       break;
                   }
               } // end fo while

               if(!flag){
                   sOrgPlants = sOrgPlants.trim();
                   if(!"".equals(sOrgPlants)){
                       sOrgPlants += "|";
                   }
                   sOrgPlants += (sPlantId+","+sPlantRel);
               }
           }
       }catch(Exception e){
           throw e;
       }
       return (sOrgPlants+"^"+sDupPlants);
   }

    /*
     * This method used to get the rootelement file from the Structure browser XML
     * @param context, domMarkup
     * @return java.io.File
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public java.io.File getRootElementFromXMLFile(Context context, DomainObject domMarkup) throws Exception
   {
       java.io.File srcXMLFile = null;
       try{
           String sXMLFormat             = PropertyUtil.getSchemaProperty(context, "format_XML");
           String sMarkupName            = domMarkup.getInfo(context, "name");
           String sTransPath             = context.createWorkspace();
           java.io.File fEmatrixWebRoot = new java.io.File(sTransPath);

           domMarkup.checkoutFile(context, false, sXMLFormat, sMarkupName+ ".xml", fEmatrixWebRoot.toString());
           srcXMLFile = new java.io.File(fEmatrixWebRoot, sMarkupName+ ".xml");

       }catch(Exception e){
           throw e;
       }
       return srcXMLFile;
   }

    /*
     * This method used to connect the Change to the part
     * @param context, domPart, domChange, sConnectRelName
     * @return DomainRelationship
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public DomainRelationship connectChangeToPart(Context context, DomainObject domPart, DomainObject domChange, String sConnectRelName) throws Exception
   {
       DomainRelationship dmRel = null;
       try{
           dmRel = DomainRelationship.newInstance(context);
           dmRel = DomainRelationship.connect(context,
                                             domChange,
                                             sConnectRelName,
                                             domPart);
       }catch(Exception e){
           throw e;
       }
       return dmRel;
   }

    /*
     * This method checks the Change is connected to part or not
     * @param context, domPart, domChange
     * @return String
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public String isChangeConnectedToPart(Context context, DomainObject domPart, DomainObject domChange) throws Exception
   {
       String sChangeConRel = null;
       String sFlag         = "";
       String sRelName      = RELATIONSHIP_AFFECTED_ITEM;
       StringBuffer sbQuery = new StringBuffer(32);
       StringList slSelects = new StringList();

       try{
           slSelects.add(DomainConstants.SELECT_TYPE);
           slSelects.add(DomainConstants.SELECT_ID);
           slSelects.add(DomainConstants.SELECT_POLICY);
           Map changeMap = domChange.getInfo(context, slSelects);

           // DataBase hit
           String sObjectId = domPart.getInfo(context, DomainConstants.SELECT_ID);

           sbQuery.append("print bus ");
           sbQuery.append("$1");
           sbQuery.append(" select ");
           sbQuery.append("$2");
           sbQuery.append(" dump");

           StringBuffer param = new StringBuffer();
           param.append("to[");
           param.append(sRelName);
           param.append('|');
           param.append(DomainConstants.SELECT_FROM_ID);
           param.append("=='");
           param.append(changeMap.get(DomainConstants.SELECT_ID));
           param.append("']");

           // DataBase hit
           sFlag = MqlUtil.mqlCommand(context, sbQuery.toString(),sObjectId,param.toString());

           if(sFlag.equalsIgnoreCase("False")){
               sChangeConRel = sRelName;
           }
        }catch(Exception e){
           throw e;
       }
       return sChangeConRel;
   }

    /*
     * This method used to checkin the XML file to the markup object
     * @param context, rootElement, domMarkup, srcXMLFile, docXML
     * @return boolean
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public boolean chekinToMarkupObject(Context context, com.matrixone.jdom.Element rootElement, DomainObject domMarkup, java.io.File srcXMLFile, com.matrixone.jdom.Document docXML) throws Exception
    {
        java.io.File fRenameMarkupFile   = null;
        String sFileName                 = null;
        boolean unlock                   = false;
        boolean bSuccess                 = true;
       try{
           matrix.db.FileList files = new matrix.db.FileList();

           // create workspace.
           String sTransPath = context.createWorkspace();

           // create a file object.
           java.io.File fEmatrixWebRoot = new java.io.File(sTransPath);

           String sXMLFormat = PropertyUtil.getSchemaProperty(context, "format_XML");

           String sbusObjMarkupName = domMarkup.getInfo(context,"name");

           if(srcXMLFile == null){
               srcXMLFile = new java.io.File(fEmatrixWebRoot, sbusObjMarkupName+ ".xml");
               sFileName = srcXMLFile.getAbsolutePath();
           }
           else
           {
               fRenameMarkupFile = new java.io.File(srcXMLFile.getParentFile(), sbusObjMarkupName+".xml");
               srcXMLFile.renameTo(fRenameMarkupFile);
               sFileName = fRenameMarkupFile.getAbsolutePath();
               unlock = true;
           }

           if(docXML == null){
               docXML = new com.matrixone.jdom.Document(rootElement);
           }
           String strEBOMCharset = "UTF-8";

           // set the font properties to xml outputter object.
           com.matrixone.jdom.output.XMLOutputter  xmlOutputter = com.matrixone.util.MxXMLUtils.getOutputter(true,strEBOMCharset);

           // create io buffer writer.
           java.io.BufferedWriter buf = new java.io.BufferedWriter(new java.io.FileWriter(srcXMLFile));

           // put xml document to putter.
           xmlOutputter.output(docXML, buf);
           buf.flush();
           buf.close();

           matrix.db.File checkinFile = new matrix.db.File(sFileName, sXMLFormat);
           files.addElement(checkinFile);

           domMarkup.checkinFromServer(context, unlock, false, sXMLFormat, "", files);

       }catch(Exception e){
           bSuccess = false;
           throw e;
       }
       return bSuccess;
    }

    /*
     * This method used to apply the markup changes to the existing XML
     * @param context, rootElement, mlMarkup
     * @return com.matrixone.jdom.Element
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public com.matrixone.jdom.Element applyMarkupChangesToXML(Context context, com.matrixone.jdom.Element rootElement, MapList mlMarkup, String sRequestFor) throws Exception
    {
        String sAttributeList[] = new String[2];
        try{
            // these attributes are used for new addition
            sAttributeList[0]   = ATTRIBUTE_SEQUENCE;
            sAttributeList[1]   = ATTRIBUTE_STATUS;

            Iterator itr = mlMarkup.iterator();
            while(itr.hasNext()){
                Map mMap               = (Map)itr.next();
                String sObjectId       = (String)mMap.get("objectId");
                String sRelId          = (String)mMap.get("relId");
                String sMarkup         = (String)mMap.get("markup");

                if("".equals(sRelId) && sMarkup.equalsIgnoreCase("add")){
                    sRelId = (String)mMap.get("param1");
                }

                java.util.List lElement     = rootElement.getChildren();
                java.util.Iterator itrXML   = lElement.iterator();
                while(itrXML.hasNext()){
                    com.matrixone.jdom.Element childElement = (com.matrixone.jdom.Element)itrXML.next();
                    com.matrixone.jdom.Element childPlant   = childElement.getChild("plant");
                    String sXMLRelId    = (String)childElement.getAttributeValue("id");
                    String sXMLObjectId = (String)childPlant.getAttributeValue("id");

                    if(sObjectId.equals(sXMLObjectId) && sRelId.equals(sXMLRelId)){
                        childElement.setAttribute("markup",sMarkup);
                        com.matrixone.jdom.Element childAttribute = childElement.getChild("attributes");
                        java.util.List lAttrElement     = childAttribute.getChildren();
                        java.util.Iterator itrAttrXML   = lAttrElement.iterator();
                        while(itrAttrXML.hasNext()){
                            com.matrixone.jdom.Element childAttr  = (com.matrixone.jdom.Element)itrAttrXML.next();
                            String sAttrName            = (String)childAttr.getAttributeValue("name");
                            String sAttrValue           = (String)mMap.get("attribute["+sAttrName+"]");
                            if(sAttrValue != null){
                                childAttr.removeChild("newvalue");
                                com.matrixone.jdom.Element childAttrNew = new com.matrixone.jdom.Element("newvalue");
                                childAttrNew.addContent(sAttrValue);
                                childAttr.addContent(childAttrNew);
                            }
                        }

                        if(sMarkup.equalsIgnoreCase("add")){
                            com.matrixone.jdom.Element childChange = null;
                            if("update".equals(sRequestFor)){
                                childElement.removeChild("Doc-In");
                            }

                            childChange = new com.matrixone.jdom.Element("Doc-In");
                            childChange.setAttribute("id", (String)mMap.get("param2"));
                            childChange.setAttribute("name", (String)mMap.get(SELECT_DOC_IN));
                            childChange.setAttribute("current", (String)mMap.get("attribute[ECOMECOStatus]"));
                            childElement.addContent(childChange);

                            childPlant.setAttribute("plantid", (String)mMap.get("attribute["+ATTRIBUTE_PLANT_ID+"]"));
                            if("create".equals(sRequestFor)){
                                for(int i=0;i<sAttributeList.length;i++){
                                   com.matrixone.jdom.Element childAttrMB = new com.matrixone.jdom.Element("attribute");
                                   childAttrMB.setAttribute("name", sAttributeList[i]);
                                   childAttribute.addContent(childAttrMB);

                                   com.matrixone.jdom.Element childAttrMBOld = new com.matrixone.jdom.Element("oldvalue");
                                   childAttrMBOld.addContent((String)mMap.get("attribute["+sAttributeList[i]+"]")); //old value
                                   childAttrMB.addContent(childAttrMBOld);

                                   com.matrixone.jdom.Element childAttrMBNew = new com.matrixone.jdom.Element("newvalue");
                                   childAttrMBNew.addContent((String)mMap.get("attribute["+sAttributeList[i]+"]"));//new value
                                   childAttrMB.addContent(childAttrMBNew);
                               }// end of for
                           }
                        }
                        break;
                    }// end of if
               }// end of while
           }
       }catch(Exception e){
           throw e;
       }
       return rootElement;
    }

    /*
     * This method used to create the new Markup XML based on the database maplist
     * @param context, rootElement, mlPlants
     * @return com.matrixone.jdom.Element
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public com.matrixone.jdom.Element createInitialMarkupXML(Context context, com.matrixone.jdom.Element rootElement, MapList mlPlants) throws Exception
    {
        String sAttributeList[] = new String[4];

       try{
           // Manufacturing Responsibility relationship attributes
           sAttributeList[0] = ATTRIBUTE_MAKE_BUY;
           sAttributeList[1] = ATTRIBUTE_ROHS;
           sAttributeList[2] = ATTRIBUTE_ERP_STATUS;
           sAttributeList[3] = ATTRIBUTE_LEAD_PLANT;

           Iterator itr = mlPlants.iterator();
           while(itr.hasNext()){
               Map mMap = (Map)itr.next();
               com.matrixone.jdom.Element childRel = new com.matrixone.jdom.Element("torelationship");
               rootElement.addContent(childRel);

               childRel.setAttribute("id",(String)mMap.get(DomainRelationship.SELECT_ID));
               childRel.setAttribute("name",(String)mMap.get("relationship"));

               com.matrixone.jdom.Element childPlant = new com.matrixone.jdom.Element("plant");
               childPlant.setAttribute("id",(String)mMap.get(DomainObject.SELECT_ID));
               childRel.addContent(childPlant);

               com.matrixone.jdom.Element childAttribute = new com.matrixone.jdom.Element("attributes");
               childRel.addContent(childAttribute);

               for(int i=0;i<sAttributeList.length;i++){
                   com.matrixone.jdom.Element childAttrMB = new com.matrixone.jdom.Element("attribute");
                   childAttrMB.setAttribute("name", sAttributeList[i]);
                   childAttribute.addContent(childAttrMB);

                   com.matrixone.jdom.Element childAttrMBOld = new com.matrixone.jdom.Element("oldvalue");
                   childAttrMBOld.addContent((String)mMap.get("attribute["+sAttributeList[i]+"]"));
                   childAttrMB.addContent(childAttrMBOld);

                   com.matrixone.jdom.Element childAttrMBNew = new com.matrixone.jdom.Element("newvalue");
                   childAttrMBNew.addContent((String)mMap.get("attribute["+sAttributeList[i]+"]"));
                   childAttrMB.addContent(childAttrMBNew);
               }
           }
       }catch(Exception e){
           throw e;
       }
       return rootElement;
    }

    /*
     * This method used to create the new Markup XML based on the database maplist
     * @param context, domPart, sPlantIds
     * @return MapList
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
  public MapList getConnectedManufacturingPlants(Context context, String sPartId, String sPlantIds) throws Exception
    {
	  DomainObject domPart   = null;
       MapList mlPlants             = null;
       StringBuffer busWhere        = new StringBuffer();
       StringBuffer relWhere        = new StringBuffer();
       StringList slRelSelects      = new StringList();
       StringList slBusSelects      = new StringList();

       try{
    	    domPart = DomainObject.newInstance(context, sPartId);

            slBusSelects.add(DomainObject.SELECT_ID);
            slRelSelects.add(DomainRelationship.SELECT_ID);
            slRelSelects.add(SELECT_PLANT_ID);
            slRelSelects.add(SELECT_ATTRIBUTE_ROHS);
            slRelSelects.add(SELECT_MAKE_BUY);
            slRelSelects.add(SELECT_ERP_STATUS);
            slRelSelects.add(SELECT_LEAD_PLANT);

            busWhere.append(DomainConstants.SELECT_CURRENT);
            busWhere.append("=='");
            busWhere.append(STATE_ORGANIZATION_ACTIVE);
            busWhere.append('\'');

            StringList slPlantIds = FrameworkUtil.split(sPlantIds,"|");

            // for loop to construct the where condition
            for( int i=0;i<slPlantIds.size(); i++){
                if(i>0){
                    relWhere.append("||");
                }
                String sPlantRelId       = (String)slPlantIds.get(i);
                StringList slPlantRelIds = FrameworkUtil.split((" "+sPlantRelId+" "),",");
                String sPlantId          = ((String)slPlantRelIds.get(0)).trim();
                String sPlantRel         = ((String)slPlantRelIds.get(1)).trim();

                if(!"".equals(sPlantRel)){
                    relWhere.append('(');
                    relWhere.append(DomainConstants.SELECT_ID);
                    relWhere.append("=='");
                    relWhere.append(sPlantRel);
                    relWhere.append('\'');
                    relWhere.append(')');
                }
                else{
                    DomainObject domPlant     = DomainObject.newInstance(context, sPlantId);
                    Map mapHeighestSeq         = getHeighestMRSequence(context, domPart, domPlant);
                    String  strPrevLPSeq     =(String) mapHeighestSeq.get(SELECT_SEQUENCE);

                    relWhere.append('(');
                    relWhere.append(SELECT_SEQUENCE);
                    relWhere.append("=='");
                    relWhere.append(strPrevLPSeq);
                    relWhere.append("'&&");
                    relWhere.append(DomainConstants.SELECT_FROM_ID);
                    relWhere.append("=='");
                    relWhere.append(sPlantId);
                    relWhere.append('\'');
                    relWhere.append(')');
                }
            }

            // get all required plant information
            // Database hit
            mlPlants = domPart.getRelatedObjects(context,
                                                     RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,
                                                     TYPE_PLANT,
                                                     slBusSelects,
                                                     slRelSelects,
                                                     true,
                                                     false,
                                                     (short)1,
                                                     busWhere.toString(),
                                                     relWhere.toString(),
                                                     0);
       }catch(Exception e){
           throw e;
       }
       return mlPlants;
    }

    /*
     * This method used to get the rootelement from the Structure browser XML
     * @param context, childElement, sPlantRelIdList
     * @return HashMap
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
    public HashMap constructMapFromElement(Context context, com.matrixone.jdom.Element childElement, StringList sPlantRelIdList)throws Exception
    {
		HashMap hmMap = new HashMap();
        try{
		   java.util.List lElementAttr = childElement.getAttributes();
		   java.util.Iterator itrAttr  = lElementAttr.iterator();
		   boolean found               = false;
			while(itrAttr.hasNext()){
				com.matrixone.jdom.Attribute childAttribute = (com.matrixone.jdom.Attribute)itrAttr.next();
				String sName  = childAttribute.getName();
				String sValue = childAttribute.getValue();

				if("objectId".equals(sName)){
					if(sPlantRelIdList.contains(sValue)){
						found = true;
						break;
					}
                }
				hmMap.put(sName, sValue);
                }

			if(found){
				return null;
			}

		   java.util.List lCElement     = childElement.getChildren();
		   if(lCElement != null){
			   java.util.Iterator itrC     = lCElement.iterator();
				while(itrC.hasNext()){
					com.matrixone.jdom.Element childCElement = (com.matrixone.jdom.Element)itrC.next();
					String sColumnName     = childCElement.getAttributeValue("name");

					// the following patch work is done because
					// the table column names and attribute names are not matching
					if(sColumnName.equalsIgnoreCase("MakeBuy")){
						sColumnName = ATTRIBUTE_MAKE_BUY;
					}
					else if(sColumnName.equalsIgnoreCase("PlantType")){
						sColumnName = ATTRIBUTE_LEAD_PLANT;
					}
					else if(sColumnName.equalsIgnoreCase("Plant Id")){
						sColumnName = ATTRIBUTE_PLANT_ID;
					}
					String sColumnValue = childCElement.getText();
					hmMap.put("attribute["+sColumnName+"]", sColumnValue);
            }
            }
        }
        catch(Exception e){
           throw e;
        }
        return hmMap;
    }

    /*
     * This method used to generate the maplist from the Structure browser XML
     * @param context, sMassUpdateAction, sDupPlants
     * @return MapList
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public MapList generateMarkupXML(Context context, String sMassUpdateAction, String sDupPlants) throws Exception
    {
       com.matrixone.jdom.Element rootElement         = null;
       com.matrixone.jdom.Document docXML             = null;
       com.matrixone.jdom.input.SAXBuilder builder    = null;
       java.io.CharArrayReader carReader    = null;
       MapList mlResult                     = new MapList();
       StringList sPlantRelIdList           = new StringList();

       try{
           StringList slDupPlants    = FrameworkUtil.split(sDupPlants,"|");
           for(int i=0;i<slDupPlants.size();i++){
               String sPlantRelIds   = (String)slDupPlants.get(i);
               StringList slDupAll   = FrameworkUtil.split((" "+sPlantRelIds+" "),",");
               String _sPlantId      = ((String)slDupAll.get(0)).trim();
               String _sPlantRel     = ((String)slDupAll.get(1)).trim();
               sPlantRelIdList.add(_sPlantId);
               sPlantRelIdList.add(_sPlantRel);
           }

           carReader                 = new java.io.CharArrayReader(sMassUpdateAction.toCharArray());
           builder                   = new com.matrixone.jdom.input.SAXBuilder();
		   builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		   builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
		   builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
           docXML                    = builder.build(carReader);
           rootElement               = docXML.getRootElement();

           java.util.List lElement   = rootElement.getChildren();
           java.util.Iterator itr    = lElement.iterator();
           while(itr.hasNext()){
               HashMap hmMap = new HashMap();
               com.matrixone.jdom.Element childElement    = (com.matrixone.jdom.Element)itr.next();
               String sMarkup = (String)childElement.getAttributeValue("markup");

               if(sMarkup != null){
				   hmMap = constructMapFromElement(context, childElement, sPlantRelIdList);
				   if(hmMap == null){
					   continue;
                        }
				   mlResult.add(hmMap);
				   hmMap = null;
                    }
			   else{
				   java.util.List sublElement   = childElement.getChildren();
				   java.util.Iterator subitr    = sublElement.iterator();
				   while(subitr.hasNext()){
					   HashMap subhmMap = new HashMap();
					   com.matrixone.jdom.Element subchildElement    = (com.matrixone.jdom.Element)subitr.next();
					   String sSubMarkup = (String)subchildElement.getAttributeValue("markup");
					   if(sSubMarkup != null){
						   subhmMap = constructMapFromElement(context, subchildElement, sPlantRelIdList);
						   if(subhmMap == null){
                    continue;
                }
						   mlResult.add(subhmMap);
						   subhmMap = null;
                        }
                    }
                }
           }
       }
       catch(Exception e){
           throw e;
       }
       return mlResult;
    }

    /*
     * This method used to create a new markup object and it will be connected to
     * both Change and Part
     * @param context, domPart, domChange
     * @return String
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   public String createAndConnectMarkup(Context context, DomainObject domPart, DomainObject domChange) throws Exception
    {
       String sPlantMarkupId                = null;
       DomainObject domMarkup               = null;
       String sConnectRelName               = null;
       try{
           // to get the change type
           // Database hit

		   if (domChange.isKindOf(context, DomainConstants.TYPE_ECO) || domChange.isKindOf(context, TYPE_MECO) || domChange.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION) ) {
               sConnectRelName = RELATIONSHIP_APPLIED_MARKUP;
           }
           else if (domChange.isKindOf(context, DomainConstants.TYPE_ECR) || domChange.isKindOf(context, TYPE_DCR)) {
               sConnectRelName = RELATIONSHIP_PROPOSED_MARKUP;
           }

           // new markup object id is generated with auto name
           sPlantMarkupId = FrameworkUtil.autoName(context,"type_PlantItemMarkup","policy_PartMarkup");
           domMarkup = new DomainObject(sPlantMarkupId);

           // connect the markup with the part
           DomainRelationship.newInstance(context);
           //088962V6R2012 PUT THE CONNECTION BETWEEN PUSH CONTXT AND POP CONTEXT
           ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
           DomainRelationship.connect(context,
                                      domPart,
                                      DomainConstants.RELATIONSHIP_EBOM_MARKUP,
                                      domMarkup);
           
           // connect the markup with the change
           DomainRelationship.newInstance(context);
           DomainRelationship.connect(context,
                                      domChange,
                                      sConnectRelName,
                                      domMarkup);
           ContextUtil.popContext(context);
       }catch(Exception e){
           throw e;
       }
       return sPlantMarkupId;
    }

    /*
     * This method used to get the required plants from the database
     * @param context, args
     * @return MapList
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getAllManuSites(Context context, String args[])throws Exception
    {
        HashMap paramMap            = new HashMap();
        String sPartId        = null;
        String sType                = null;
        MapList mLocationList       = new MapList();
        String objectId             = null;
        StringList busSelList       = new StringList();
        StringList relSelList       = new StringList();
        String[] saArgs             = new String[1];
        String sFilterObjects       = "";
        StringBuffer sbWhere        = new StringBuffer();
        try
        {
            paramMap = (HashMap)JPO.unpackArgs(args);

            sFilterObjects = (String)paramMap.get("filterObjects");
            if(sFilterObjects != null && !sFilterObjects.equals("")){
                StringList slList = FrameworkUtil.split(sFilterObjects,"|");
                for(int indx=0;indx<slList.size();indx++){
                    if(indx!=0){
                        sbWhere.append("||");
                    }
                    sbWhere.append('(');
                    sbWhere.append(DomainObject.SELECT_ID);
                    sbWhere.append("=='");
                    sbWhere.append(slList.get(indx));
                    sbWhere.append('\'');
                    sbWhere.append(')');
                }
            }

            objectId     = (String) paramMap.get("objectId");
            saArgs[0]     = objectId;
            DomainObject doPart = DomainObject.newInstance(context);
            doPart.setId(objectId);

            sType         = doPart.getInfo(context,DomainObject.SELECT_TYPE);
            String sTopLevelType = FrameworkUtil.getBaseType(context, sType, context.getVault());

            if(sTopLevelType.equals(DomainConstants.TYPE_PART))
            {
            	sPartId =  objectId;
            }

            if(UIUtil.isNotNullAndNotEmpty(sPartId))
            {
                busSelList.add(DomainObject.SELECT_ID);
                busSelList.add(DomainObject.SELECT_NAME);
                busSelList.add(SELECT_PLANT_ID);
                relSelList.add(DomainRelationship.SELECT_ID);
                relSelList.add(SELECT_SEQUENCE);
                relSelList.add(SELECT_STATUS);
                relSelList.add(SELECT_LEAD_PLANT);
                relSelList.add(SELECT_DOC_IN);
                relSelList.add(SELECT_MAKE_BUY);
                relSelList.add("attribute["+ATTRIBUTE_ROHS+"]");

                // get the required plants based on the where condition
                mLocationList = doPart.getRelatedObjects(context,
                                                     RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,
                                                     TYPE_PLANT,
                                                     busSelList,
                                                     relSelList,
                                                     true,
                                                     false,
                                                     (short)1,
                                                     "",
                                                     sbWhere.toString(),
                                                     0,
                                                     null,
                                                     null,
                                                     null);

        }
           //initializing iterator
           Iterator objectListItr = mLocationList.iterator();
           while(objectListItr.hasNext()) {
                Map objectMap    = (Map) objectListItr.next();
                String sStatus   = (String)objectMap.get(SELECT_STATUS);
                if("Current".equals(sStatus)) {
                  objectMap.put("RowEditable","readonly");
                } else {
                    objectMap.put("RowEditable","show");
                }
            }
       }catch(Exception ex){
            throw ex;
        }
       return mLocationList;
    }

    /*
     * This method used to get the partMaster id
     * @param context, sPartId
     * @return String
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
    public String getPartMaster(Context context,String sPartId)throws Exception
    {
        // Modified for IR-061415V6R2011x
        Part objPart = new Part();
        return objPart.getPartMaster(context, sPartId);
    }

    /*
     * This method used to get the highest MR sequence
     * @param context, dmPartMaster, dmLoc
     * @return Map
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
    public  Map getHeighestMRSequence(Context context,DomainObject dmPartMaster, DomainObject dmLoc) throws Exception
    {
        /**
         *Hold the relationship where clause condition
         */
         StringBuffer strBusWhere = null;
         Map  mapHighSeq          = null;
        try
        {
            StringList busSelList = new StringList(1);
            StringList relSelList = new StringList(5);
            busSelList.add(DomainConstants.SELECT_ID);

            relSelList.add(DomainRelationship.SELECT_ID);
            relSelList.add(SELECT_SEQUENCE);
            relSelList.add(SELECT_STATUS);
            relSelList.add(SELECT_LEAD_PLANT);
            relSelList.add(SELECT_ERP_STATUS);
            relSelList.add(SELECT_MAKE_BUY);
            strBusWhere = new StringBuffer();
            strBusWhere.append(SELECT_PLANT_ID+" == '"+dmLoc.getAttributeValue(context,ATTRIBUTE_PLANT_ID)+"'");

            //getting parts connected to part master with part revision relationship and passed sRelWhereExpr condition
             MapList mlMRObjs = dmPartMaster.getRelatedObjects(context,
                                                                RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,
                                                                TYPE_PLANT,
                                                                busSelList,
                                                                relSelList,
                                                                true,
                                                                false,
                                                                (short)1,
                                                                strBusWhere.toString(),
                                                                null,
                                                                0);
             if (mlMRObjs!=null && mlMRObjs.size()>0)
            {
              mlMRObjs.sort(SELECT_SEQUENCE,"descending", "integer");
              mapHighSeq= (Map)mlMRObjs.get(0);
            }

        }
        catch (Exception ex)
        {
            throw ex;
        }
        // Return the highest sequence relationship.
        return mapHighSeq;
    }

    /*
     * This method used to check the pending MR rel is exist or not
     * @param context, sPartId, sObjectId
     * @return DomainRelationship
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
    public DomainRelationship checkForPendingMRRel(Context context, String sPartId, String sObjectId)throws Exception
    {
		DomainRelationship domRelMR = null;
		StringBuffer sbBusWhere = new StringBuffer();
		StringBuffer sbRelWhere = new StringBuffer();
		StringList slObjectSelects = new StringList();
		StringList slRelSelects = new StringList();
        try{
			sbBusWhere.append("id=='");
			sbBusWhere.append(sObjectId);
			sbBusWhere.append('\'');

			sbRelWhere.append(SELECT_STATUS);
			//Added for IR-217397V6R2014
			sbRelWhere.append("== Pending");

			slRelSelects.add(DomainRelationship.SELECT_ID);
			//Modified for IR-217397V6R2014 to change the domian object id
			DomainObject domPartMaster = new DomainObject(sPartId);// * for Partrevision
			MapList plantsList = domPartMaster.getRelatedObjects(context,
																RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,
																TYPE_PLANT,
																slObjectSelects,
																slRelSelects,
																true,
																false,
																(short)1,
																sbBusWhere.toString(),
																sbRelWhere.toString(),
																0);

			if(!plantsList.isEmpty()){
				Map mPlant = (Map)plantsList.get(0);
				String sRelId = (String)mPlant.get(DomainRelationship.SELECT_ID);
				if(sRelId!=null || !"null".equals(sRelId)){
					domRelMR = new DomainRelationship(sRelId);
				}
			}

        }catch (Exception ex)
        {
            throw ex;
        }
        return domRelMR;
	}


    /*
     * This method used to check the access for creating plant item markup
     * @param context, args
     * @return Boolean value
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth A
     */
    public boolean showPIMSaveMarkupCommand(Context context, String[] args) throws Exception
    {
        boolean bReturn    = true;
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String objectId    = (String) programMap.get("objectId");
        try{
            StringList slSelects = new StringList();
            slSelects.add(DomainConstants.SELECT_CURRENT);
            slSelects.add(DomainConstants.SELECT_REVISION);
            slSelects.add(DomainConstants.SELECT_POLICY);
            slSelects.add("first");

            DomainObject doPart = new DomainObject(objectId);
            Map mPartInfo       = doPart.getInfo(context, slSelects);
            String sStatus      = (String)mPartInfo.get(DomainConstants.SELECT_CURRENT);
            String sFirst       = (String)mPartInfo.get("first");
            String sRevision    = (String)mPartInfo.get(DomainConstants.SELECT_REVISION);
            String sPolicy      = (String)mPartInfo.get(DomainConstants.SELECT_POLICY);
            //371781 - Modified the if condition to check for PolicyClassification instead of Policy
            String policyClassification = EngineeringUtil.getPolicyClassification(context,sPolicy);
            if("Production".equalsIgnoreCase(policyClassification)){

				String sECPartStates = (String)EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ECPart.AllowSaveMarkup");
				StringList slStates = FrameworkUtil.split(sECPartStates, ",");
				StringList slStatesList = new StringList();
				for(int indx=0; indx<slStates.size();indx++){
					slStatesList.add(FrameworkUtil.lookupStateName(context, sPolicy, (String)slStates.get(indx)));
				}

				if(!slStatesList.contains(sStatus)){
					bReturn = false;
				}
				else if(sFirst.equals(sRevision) && !(sStatus.equals(DomainConstants.STATE_PART_RELEASE) || sStatus.equals(DomainConstants.STATE_PART_PRELIMINARY))){  //Checking for number of revisions.
                bReturn = false;
            }
			}
			else{
                bReturn = false;
            }
        }
        catch(Exception ex){
            throw ex;
        }
        return bReturn;
    }

  public String generatedItemMarkupView(Context context, String args[]) throws Exception
    {

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap settingsMap = (HashMap) fieldMap.get("settings");
        String sReturn = "";
        String attrWeight = PropertyUtil.getSchemaProperty(context,"attribute_Weight");
        String attrEstimatedCost = PropertyUtil.getSchemaProperty(context,"attribute_EstimatedCost");

        //Added for IR-077316V6R2013 start
        String  strPartId=DomainConstants.EMPTY_STRING;
       //Added for IR-077316V6R2013 end

        String attrName = PropertyUtil.getSchemaProperty( context,(String) fieldMap.get("name"));
      String attrName1 = attrName.replaceAll(" ", "_");
      //IR-017226 - Starts
      String strWeight          = PropertyUtil.getSchemaProperty(context,"attribute_Weight");
      String strTargetCost      = PropertyUtil.getSchemaProperty(context,"attribute_TargetCost");
      String strEstimatedCost   = PropertyUtil.getSchemaProperty(context,"attribute_EstimatedCost");
      String strEffectivityDate = PropertyUtil.getSchemaProperty(context,"attribute_EffectivityDate");
      String strLeadTimeDuration= PropertyUtil.getSchemaProperty(context,"attribute_LeadTimeDuration");
      //IR-017226 - Ends

        AttributeType attType = new AttributeType(attrName);

      //Added for bug : 351866
       String languageStr = (String) requestMap.get("languageStr");

	  // Added for 071743
       Locale locale = (Locale) requestMap.get("localeObj");
	  int DisplayFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
	  // 071743 ends here

        //Added for bug : 351866
        String strComboName  = "comboDescriptor_" + attrName;
        String strTxtName    = "txt_" + attrName;

        String attrType =attType.getDataType(context);

        String displayUOM = "";
        String selectUnit = "";

        //  My Code Start
        String sAction = (String) requestMap.get("activity");
        DomainObject doPart = null;
        String objectId = (String) requestMap.get("objectId");

        if("create".equals(sAction))
        {
            // create part object
            doPart = new DomainObject(objectId);
            //BGP: Do not make the Release Process/Phase and Change Controlled attributes editable in the Item Markup create page
            if("false".equals((String) settingsMap.get("Item Markup - Editable")) 
            		|| EngineeringConstants.ATTRIBUTE_RELEASE_PHASE.equals(attrName)
            		|| EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED.equals(attrName))
            {
                sReturn = doPart.getAttributeValue(context,attrName);
                return sReturn;
            }
        }
        else
        {
            // create markup object
            DomainObject doMarkup = new DomainObject(objectId);
            String relName = PropertyUtil.getSchemaProperty(context,"relationship_EBOMMarkup");
            StringList objSelects = new StringList();
            objSelects.add("id");
            // get part object  id
            Map map = doMarkup.getRelatedObject(context,relName,false,objSelects,DomainConstants.EMPTY_STRINGLIST);
            // create part object
            doPart = new DomainObject((String) map.get("id"));
            if("false".equals((String) settingsMap.get("Item Markup - Editable")))
            {
                sReturn = doPart.getAttributeValue(context,attrName);
                return sReturn;
            }

          //Added for IR-077316V6R2013 start
            strPartId = (String) map.get("id");
          //Added for IR-077316V6R2013 end
            if("edit".equals(sAction)&& (EngineeringConstants.ATTRIBUTE_RELEASE_PHASE.equals(attrName)
            		|| EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED.equals(attrName)))
            {
            	sReturn = doPart.getAttributeValue(context,attrName);
                return sReturn;
            }
            if("view".equals(sAction))
            {
				StringList arrFile = FrameworkUtil.split(fileContent,"#");
                for( int index=0; index < arrFile.size(); index +=3)
                {
                    if(arrFile.get(index).equals(attrName))
                    {
                    	//Added for IR-097616V6R2012 	starts

                    	String attr1="";
                    	String dimension="";
                    	String attrwithdimensiondisp="";
                    	StringList attrList=new StringList();
                    	   if(((String) arrFile.get(index+1)).indexOf("_") != -1)                     	   {
                    		   attrList=FrameworkUtil.split(arrFile.get(index+1).toString(), "_");

                    		   if(attrList.size()>=2)
                    		   {
                    		   attr1=(String)attrList.get(0);
                    		   dimension=(String)attrList.get(1);

                    		   attrwithdimensiondisp=attr1+" "+ i18nNow.getDimensionI18NString(dimension, languageStr);
                    		   }
                    	   }
                    	 attr1="";
                         dimension="";
                       	String attrwithdimensiondisp2="";
                        attrList=new StringList();
                       	   if(((String)arrFile.get(index+2)).indexOf("_") != -1)                     	   {
                       		   attrList=FrameworkUtil.split((String)arrFile.get(index+2), "_");

                       		   if(attrList.size()>=2)
                       		   {
                       		   attr1=(String)attrList.get(0);
                       		   dimension=(String)attrList.get(1);

                       		   attrwithdimensiondisp2=attr1+" "+ i18nNow.getDimensionI18NString(dimension, languageStr);
                       		   }
          				  }
                       	//Added for IR-097616V6R2012 	ends

                    if(((String)arrFile.get(index+1)).indexOf("_") != -1) arrFile.set(index+1,((String)arrFile.get(index+1)).replaceAll("_","")) ;
                        if(((String)arrFile.get(index+2)).indexOf("_") != -1)  arrFile.set(index+2,((String) arrFile.get(index+2)).replaceAll( "_",""));
						arrFile.set(index+2,((String)arrFile.get(index+2)).replaceAll(" ", "_"));
						arrFile.set(index+1,((String)arrFile.get(index+1)).replaceAll(" ", "_"));

                        if(arrFile.get(index+1).equals(arrFile.get(index+2))){
                          //IR-017226 - Starts
                          if (strWeight.equals(attrName) || strTargetCost.equals(attrName)
                                  || strEstimatedCost.equals(attrName) || strEffectivityDate.equals(attrName) 
                                  || strLeadTimeDuration.equals(attrName)) {
                        	//Modified for IR-097616V6R2012 	starts
                        	  if(!"".equals(attrwithdimensiondisp))
                        	  {
                              sReturn=attrwithdimensiondisp;
                        	  }
                              else
                              {
                                  sReturn = (String)arrFile.get(index+1);
                                  if("timestamp".equals(attrType)) {
                                	  sReturn = ((String)arrFile.get(index+1)).replaceAll("_"," ");
                                	  if(null!=sReturn && !("").equals(sReturn)){
                                		  Date dd = eMatrixDateFormat.getJavaDate(sReturn);
                                		  sReturn = DateFormat.getDateInstance(DisplayFormat, locale).format(dd);
                                	  }
                                  }
                              }
                        	//Modified for IR-097616V6R2012 	ends
                          } else {
                          //IR-017226 - Ends

                        	//Multitenant
                        	  arrFile.set(index+2, EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range." + attrName1 +"."+ arrFile.get(index+2)));

                        	//Multitenant
                        	  sReturn =EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),(String)arrFile.get(index+2));
                          //IR-017226 - Starts
                          }
                          //IR-017226 - Ends
                      }
                        else
                        {
                          //IR-017226 - Starts
                          if (!strWeight.equals(attrName) && !strTargetCost.equals(attrName)
                                  && !strEstimatedCost.equals(attrName) && !strEffectivityDate.equals(attrName)) {
                          //IR-017226 - Ends

                        	//Multitenant
                        	  arrFile.set(index+2, EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range." + attrName1 +"."+ arrFile.get(index+2)));
                        	//Multitenant
                        	  arrFile.set(index+1,EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range." + attrName1 +"."+ arrFile.get(index+1)));
                          //IR-017226 - Starts
                          }
                          //IR-017226 - Ends

                          // Modified for 071743
                          if("timestamp".equals(attrType)) {
                        	  String strShowOldValue = ((String) arrFile.get(index+1)).replaceAll("_"," ");
                        	  String strShowNewValue = ((String) arrFile.get(index+2)).replaceAll("_"," ");
                        	  if(null!=strShowOldValue && !("").equals(strShowOldValue)){
                        		  Date dd = eMatrixDateFormat.getJavaDate(strShowOldValue);
                        		  strShowOldValue = DateFormat.getDateInstance(DisplayFormat, locale).format(dd);
                        	  }
                        	  if(null!=strShowNewValue && !("").equals(strShowNewValue)){
                        		  Date ddShow = eMatrixDateFormat.getJavaDate(strShowNewValue);
                        		  strShowNewValue = DateFormat.getDateInstance(DisplayFormat, locale).format(ddShow);
                        	  }

                        	  //Modified for IR-077316V6R2013 start
							  if(strShowOldValue.equals(strShowNewValue)) {
								  sReturn = "<label>"+XSSUtil.encodeForHTML(context, strShowOldValue)+"</label>&nbsp;";
							  } else {
								  sReturn = "<label style=\"color:red\" ><s>"+XSSUtil.encodeForHTML(context,strShowOldValue)+"</s></label>&nbsp;";
                        	  	  sReturn += "<label style=\"color:green\" >"+XSSUtil.encodeForHTML(context,strShowNewValue)+"</label>";
							  }//Modified for IR-077316V6R2013 end
                          }
                          else{
                        	//Modified for IR-097616V6R2012 starts
                        	  if(!"".equals(attrwithdimensiondisp))
                        	  {
                            	  sReturn = "<label style=\"color:red\" ><s>"+XSSUtil.encodeForHTML(context,attrwithdimensiondisp)+"</s></label>&nbsp;";
                        		  sReturn += "<label style=\"color:green\" >"+XSSUtil.encodeForHTML(context,attrwithdimensiondisp2)+"</label>";
                        	  }
                        	  else
                        	  {
                        	  sReturn = "<label style=\"color:red\" ><s>"+XSSUtil.encodeForHTML(context,(String)arrFile.get(index+1))+"</s></label>&nbsp;";
                        	  sReturn += "<label style=\"color:green\" >"+XSSUtil.encodeForHTML(context,(String)arrFile.get(index+2))+"</label>";

                        	  }
                        		//Modified for IR-097616V6R2012 ends
                          }
                          // 071743 Ends here
                        }
                    }
                }
                return sReturn;
            }
        }

        StringList attrChoices = attType.getChoices();
        StringBuffer sOldValues = new StringBuffer(150);
        Attribute att = doPart.getAttributeValues(context,attrName);
        String sAttValue = att.getValue();
        String sShowValue = "";
        String sShowValue2 = "";
        sShowValue = att.getValue();
        if("edit".equals(sAction))
        {
            StringList arrFile1 = FrameworkUtil.split(fileContent,"#");
            for(int index=0;index<arrFile1.size(); index+=3)
                if(arrFile1.get(index).equals(attrName))
                {
                    sShowValue = (String) arrFile1.get(index+2);
                    sShowValue2 = (String) arrFile1.get(index+1);
                }
        }

        String sAttValueWithDimension = sAttValue;
        //Added for IR-097616V6R2012
        String sAttValueWithDimensionDisp = sAttValue;

        //Below code modified for Estimated cost
        if(UOMUtil.isAssociatedWithDimension(context, attrName)) {
            sAttValueWithDimension = UOMUtil.getInputValue(context,objectId,attrName);
            //If condition added for IR-068994
            if(sAttValueWithDimension != null && sAttValueWithDimension.length()>0) {
            sShowValue = sAttValueWithDimension;
            }else {
                sAttValueWithDimension = sAttValue;
            }
        }

        if (UOMUtil.isAssociatedWithDimension(context, attrName))
        {
        	boolean isAttrWeight = false;
        	String convertUnit = "";
            if(!attrName.equalsIgnoreCase(attrWeight) && !attrName.equalsIgnoreCase(attrEstimatedCost)){
		                    selectUnit = UOMUtil.getDBunit(context, attrName);
                            //below if block is added for Estimated cost
                            if(selectUnit == null || selectUnit.length()==0) {
                                selectUnit = UOMUtil.getInputunit(context,objectId,attrName);
                            }
                            
                            if(attrName.equalsIgnoreCase(strLeadTimeDuration) || attrName.equalsIgnoreCase(strTargetCost)){
                            	selectUnit = UOMUtil.getInputunit(context,objectId,attrName);
                    		}
		                }
		                else{
		                	//Modified for IR-077316V6R2013 start
		                	if(attrName.equalsIgnoreCase(attrWeight)) {
		                		isAttrWeight = true;
		                	}
		                    selectUnit = UOMUtil.getInputunit(context,objectId,attrName);
		                    if(isAttrWeight) {
		                    	convertUnit = UOMUtil.getInputunit(context, objectId, attrName,false);
		                    }

			      			  if (selectUnit.equals(DomainConstants.EMPTY_STRING) && sAction.equals("edit") && null != strPartId && (!strPartId.equals(DomainConstants.EMPTY_STRING))) {
			      				 selectUnit = UOMUtil.getInputunit(context, strPartId, attrName);
			      				if(isAttrWeight) {
			      					convertUnit = UOMUtil.getInputunit(context, strPartId, attrName,false);
			      				}
			      			  }
			      			//Modified for IR-077316V6R2013 end
             }
            String fieldName = "units_" + attrName;
            displayUOM = UIUtil.displayUOMComboField(context, attrName, selectUnit, fieldName, languageStr);
            if (!"".equals(displayUOM)) displayUOM = "&nbsp;" + displayUOM;
            //Modified for IR-077316V6R2013 start
            sAttValueWithDimension += " "+i18nNow.getDimensionI18NString(selectUnit, languageStr);

             //Added for IR-097616V6R2012

            if(isAttrWeight) {
                sAttValueWithDimensionDisp += " " + i18nNow.getDimensionI18NString(convertUnit, languageStr);
            } else {
            	sAttValueWithDimensionDisp += " " + i18nNow.getDimensionI18NString(selectUnit, languageStr);
            }
          //Modified for IR-077316V6R2013 end
        }

        if(!"timestamp".equals(attrType))
        {

            if (UOMUtil.isAssociatedWithDimension(context, attrName))
            {
            	//Modified for IR-097616V6R2012 starts
                if("edit".equals(sAction)){
                    if(sShowValue.equals(sShowValue2))
                        sOldValues.append("<table><tr><td width=150><label id=\"label_"+attrName+"\">"+XSSUtil.encodeForHTML(context, sAttValueWithDimensionDisp)+"</label>");
                    else
                        sOldValues.append("<table><tr><td width=150><label style=\"color:red\" id=\"label_"+attrName+"\"> <s>"+XSSUtil.encodeForHTML(context,sAttValueWithDimensionDisp)+"</s></label>");

                    //Added for IR-077316V6R2013
                    sOldValues.append("<input type=hidden id=\"hid_"+attrName+"\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sAttValueWithDimensionDisp)+"\" ></td>");
                } else {
                	sOldValues.append("<table><tr><td width=150><label id=\"label_"+attrName+"\">"+XSSUtil.encodeForHTML(context,sAttValueWithDimension)+"</label>");

                	//Added for IR-077316V6R2013
                	sOldValues.append("<input type=hidden id=\"hid_"+attrName+"\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sAttValueWithDimension)+"\" ></td>");
                }
              //Modified for IR-097616V6R2012 ends
            }
            else
            {

	// X6 Bug #: 351866
                String Name1 = FrameworkUtil.Replace(attrName," ", "_");
				String Rang1 = FrameworkUtil.Replace(sAttValue," ", "_");

				String attrName2 = "emxFramework.Range." + Name1 + "." + Rang1;

				//Multitenant
				String sAttValue11 = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),attrName2);
	// X6 Bug #: 351866
				if("edit".equals(sAction)) {
                    if(sShowValue.equals(sAttValue))
	                  //IR-017226 - Starts
	                  {
	                      if (strWeight.equals(attrName) || strTargetCost.equals(attrName)
	                              || strEstimatedCost.equals(attrName) || strEffectivityDate.equals(attrName)) {
	                          sOldValues.append("<table><tr><td width=150><label id=\"label_"+attrName+"\">"+XSSUtil.encodeForHTML(context,sAttValue)+"</label>");
	                      }
	                      else {
	                      //IR-017226 - Ends
	                          sOldValues.append("<table><tr><td width=150><label id=\"label_"+attrName+"\">"+XSSUtil.encodeForHTML(context,sAttValue11)+"</label>");
	                      //IR-017226 - Starts
	                      }
	                  }
	                  //IR-017226 - Ends
	                  else
	                  //IR-017226 - Starts
	                  {
	                      if (strWeight.equals(attrName) || strTargetCost.equals(attrName)
	                              || strEstimatedCost.equals(attrName) || strEffectivityDate.equals(attrName)) {
	                          sOldValues.append("<table><tr><td width=150><label style=\"color:red\" id=\"label_"+attrName+"\"><s>"+XSSUtil.encodeForHTML(context,sAttValue)+"</s></label>");
	                      }
	                      else {
	                   //IR-017226 - Ends
	                          sOldValues.append("<table><tr><td width=150><label style=\"color:red\" id=\"label_"+attrName+"\"><s>"+XSSUtil.encodeForHTML(context,sAttValue11)+"</s></label>");
	                   //IR-017226 - Starts
	                      }
	                  }
                    //Modified for IR-097616V6R2012

                    //Modified for IR-077316V6R2013 start
    				  String attrval=strTargetCost.equals(attrName)?sAttValue:sAttValue11;
                        sOldValues.append("<input type=hidden id=\"hid_"+attrName+"\" value=\""+XSSUtil.encodeForHTMLAttribute(context,attrval)+"\" ></td>");
                      //Modified for IR-077316V6R2013 end
				  }
                  //IR-017226 - Ends
                  else {
	                  //IR-017226 - Starts
	                  if (strWeight.equals(attrName) || strTargetCost.equals(attrName)
	                          || strEstimatedCost.equals(attrName) || strEffectivityDate.equals(attrName)) {
	                      sOldValues.append("<table><tr><td width=150><label id=\"label_"+attrName+"\">"+XSSUtil.encodeForHTML(context,sAttValue)+"</label>");
	                      sOldValues.append("<input type=hidden id=\"hid_"+attrName+"\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sAttValue)+"\" ></td>");

	                  } else {
	                  //IR-017226 - Ends
	                      sOldValues.append("<table><tr><td width=150><label id=\"label_"+attrName+"\">"+XSSUtil.encodeForHTML(context,sAttValue11)+"</label>");
	                      sOldValues.append("<input type=hidden id=\"hid_"+attrName+"\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sAttValue11)+"\" ></td>");
	                  //IR-017226 - Starts
	                  }
	                  //IR-017226 - Ends
                  }
            }
        }
        else
        {
            if(att.getValue().equals(""))
                sAttValue = att.getValue();
            else
            {
            	// Modified for 071743
            	Date dd = eMatrixDateFormat.getJavaDate(att.getValue());
            	sAttValue = DateFormat.getDateInstance(DisplayFormat, locale).format(dd);
            	Date ddShow = eMatrixDateFormat.getJavaDate(sShowValue);
            	sShowValue = DateFormat.getDateInstance(DisplayFormat, locale).format(ddShow);
            	// 071743 Ends here
            }

            sOldValues.append("<table><tr><td width=150><label id=\"label_"+attrName+"\">"+XSSUtil.encodeForHTML(context,sAttValue)+"</label>");
            sOldValues.append("<input type=hidden id=\"hid_"+attrName+"\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sAttValue)+"\" ></td>");
        }

        StringBuffer strText = new StringBuffer();
        if(attrChoices==null || attrChoices.size()<=1)
        {
            if(UOMUtil.isAssociatedWithDimension(context, attrName) && "edit".equals(sAction))
                sAttValue += "_"+ UOMUtil.getDBunit(context, attrName);
            if("edit".equals(sAction))
                if(sShowValue.equals(sAttValue) )
                    strText.append("<input type=\"text\" name=\"");
                else
                    strText.append("<input type=\"text\" style=\"color:green\" name=\"");
            else
                    strText.append("<input type=\"text\" name=\"");
            strText.append(XSSUtil.encodeForHTML(context,strTxtName));

          //Added for IR-077316V6R2013
    	  displayUOM=displayUOM.replaceAll("<select", "<select onchange=\"showChange(this,'string');\"");

            if(UOMUtil.isAssociatedWithDimension(context, attrName) && "edit".equals(sAction)) {
                StringList sShowValueArr = FrameworkUtil.split(sShowValue,"_");
                String val = (String) sShowValueArr.get(1);
                int iPlace = displayUOM.indexOf("value=\""+val+"\"");
                 displayUOM = displayUOM.substring(0,iPlace) + " selected " + displayUOM.substring(iPlace,displayUOM.length());
                displayUOM.substring(iPlace,displayUOM.length());

                strText.append("\" value=\""+XSSUtil.encodeForHTML(context, (String)sShowValueArr.get(0))+"\" size=\"20\" extra=\"yes\"    onkeyup = \" showChange(this,'"+attrType+"'); \" >");

            } else
            strText.append("\" value=\""+XSSUtil.encodeForHTML(context, sShowValue)+"\" size=\"20\" extra=\"yes\"    onkeyup = \" showChange(this,'"+attrType+"'); \" >");
            strText.append(displayUOM);
        }

     if("timestamp".equals(attrType)) {
                strText = new StringBuffer();
                strText.append("<input READONLY type=\"text\" name=\"");
                strText.append(XSSUtil.encodeForHTML(context,strTxtName));
                strText.append("\" id=\""+XSSUtil.encodeForHTML(context,strTxtName)+"\" value=\""+XSSUtil.encodeForHTML(context,sShowValue)+"\" size=\"20\" extra=\"yes\" onfocus = \'var v = document.getElementById(\""+strTxtName+"\"); showChange(v,\"string\");\'  >&nbsp;&nbsp; <a href='javascript:showCalendar(\"editDataForm\",\"");
                strText.append(XSSUtil.encodeForHTML(context,strTxtName));
                strText.append("\",\"\",true,validateEffectivityDateForItemMarkup); '  onfocus = \'var v = document.getElementById(\""+strTxtName+"\"); showChange(v,\"string\");\'><img src=\"../common/images/iconSmallCalendar.gif\" border=0></a>");

        }
        String attLeadTime = PropertyUtil.getSchemaProperty(context,"attribute_LeadTime");
        if (attLeadTime.equalsIgnoreCase(attrName)) {
        	attrChoices = new StringList ("Unassigned");
        	attrChoices.addElement("1 Week");attrChoices.addElement("2 Weeks");attrChoices.addElement("3 Weeks");
        	attrChoices.addElement("4 Weeks");attrChoices.addElement("6 Weeks");attrChoices.addElement("8 Weeks");
        	attrChoices.addElement("12 Weeks");attrChoices.addElement("20 Weeks");
        }
        //UOM Management : Added If/else block - start
        StringBuffer strChoiceSelect = new StringBuffer(16);
        String attUOM = PropertyUtil.getSchemaProperty(context,"attribute_UnitofMeasure");
        if (attUOM.equalsIgnoreCase(attrName)) 
        {
        	String sUOMTypeOnPart = doPart.getInfo(context, EngineeringConstants.SELECT_UOM_TYPE);        	
        	String[] arguments = new String[1];
        	emxECPartBase_mxJPO oECPartBase = new emxECPartBase_mxJPO(context, arguments);
        	//StringList aValues = oECPartBase.getMappingUOMValues(context, sUOMTypeOnPart, "property");	
        	HashMap mUOMValueMap = oECPartBase.getMappingUOMValues(context, sUOMTypeOnPart, "property");  
    		StringList aValues = (StringList) mUOMValueMap.get("RangeValues");
    		StringList slUOMDisplayValList = (StringList) mUOMValueMap.get("RangeDisplayValues");

        	strChoiceSelect.append("&nbsp;");
        	strChoiceSelect = new StringBuffer(150);
        	if("edit".equals(sAction))
        		if(sShowValue.equals(sAttValue))
        			strChoiceSelect.append("<select name=\"");
        		else
        			strChoiceSelect.append("<select style=\"color:green\" name=\"");
        	else
        		strChoiceSelect.append("<select name=\"");
        	strChoiceSelect.append(XSSUtil.encodeForHTML(context, strComboName));
        	strChoiceSelect.append("\"    onchange=\"showChange(this, 'string'); \" >");
        	for(int j =0; j < aValues.size();j++)
        	{
        		String attrrange = (String)aValues.get(j);

        		strChoiceSelect.append("<option value=\"");
        		strChoiceSelect.append(aValues.get(j));
        		if(sShowValue.equals(aValues.get(j)))
        			strChoiceSelect.append("\" selected>");
        		else
        			strChoiceSelect.append("\">");
        		strChoiceSelect.append(XSSUtil.encodeForHTML(context, attrrange));
        		strChoiceSelect.append("</option>");
        	}
        	strChoiceSelect.append("</select>");

        }
        //UOM Management - end
        else
        {
        	//StringBuffer strChoiceSelect = new StringBuffer(16);
        	strChoiceSelect.append("&nbsp;");
        	if(attrChoices != null && attrChoices.size() > 1) {
        		strChoiceSelect = new StringBuffer(150);
        		if("edit".equals(sAction))
        			if(sShowValue.equals(sAttValue))
        				strChoiceSelect.append("<select name=\"");
        			else
        				strChoiceSelect.append("<select style=\"color:green\" name=\"");
        		else
        			strChoiceSelect.append("<select name=\"");
        		strChoiceSelect.append(XSSUtil.encodeForHTML(context, strComboName));
        		strChoiceSelect.append("\"    onchange=\"showChange(this, 'string'); \" >");
        		for(int j =0; j < attrChoices.size();j++)
        		{
        			//Added for Bug #: 351866 starts
        			String attrrange = (String)attrChoices.get(j);

        			// X6 Bug #: 351866
        			String Name1 = FrameworkUtil.Replace(attrName," ", "_");
        			String Rang1 = FrameworkUtil.Replace(attrrange," ", "_");
        			String attrName2 = "emxFramework.Range." + Name1 + "." + Rang1;

        			//Multitenant
        			String attrChoices1 = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),attrName2);
        			// X6 Bug #: 351866
        			//Added for Bug #: 351866 ends
        			strChoiceSelect.append("<option value=\"");
        			strChoiceSelect.append(attrChoices.get(j));
        			if(sShowValue.equals(attrChoices.get(j)))
        				strChoiceSelect.append("\" selected>");
        			else
        				strChoiceSelect.append("\">");
        			strChoiceSelect.append(XSSUtil.encodeForHTML(context, attrChoices1));
        			strChoiceSelect.append("</option>");
        		}
        		strChoiceSelect.append("</select>");
        	}
        }

        StringBuffer sScript = new StringBuffer(4096);
        sScript.append("<script language=javascript>");
        sScript.append("function commaToDecimalSeparator(value) {");
        sScript.append("if (value && value.indexOf(\",\") != -1 ) {");
        sScript.append("return value.replace(\",\", \".\");");
        sScript.append('}');
        sScript.append("return value;");
        sScript.append('}');

        sScript.append("function showChange(ele, attrType) {  ");
        sScript.append("  var sName = ele.getAttribute(\"name\"); ");
        sScript.append(" var sThisValue = ele.value; ");
        sScript.append(" var  sHidElement = document.getElementById(\"hid_\"+sName.substring(sName.indexOf(\"_\",0)+1,sName.length)); ");
        sScript.append(" var  sLabelElement = document.getElementById(\"label_\"+sName.substring(sName.indexOf(\"_\",0)+1,sName.length)); ");

        //Added for IR-077316V6R2013 start
    	sScript.append("var txtEstimatedCost=document.getElementsByName(\"txt_Estimated Cost\")[0].value;");
    	sScript.append("var unitsEstimatedCost=document.getElementsByName(\"units_Estimated Cost\")[0].value; ");
    	sScript.append("var txtWeight=document.getElementsByName(\"txt_Weight\")[0].value;");
    	sScript.append("var unitsWeight=document.getElementsByName(\"units_Weight\")[0].value;");
    	//Added for IR-077316V6R2013 end

		// ADDED BY FOR BUG # 345814

    	//Multitenant
    	String sPositiveNumber = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Alert.checkPositiveNumeric");

		sScript.append(" if(attrType==\"real\" || attrType==\"integer\")  {");
        sScript.append("var value = commaToDecimalSeparator(ele.value);");
        sScript.append("if(isNaN(value) || value < 0 ) { alert(\""+sPositiveNumber+"\"); ele.value ='0.0' ;  value = value.substring(0,value.length-1) ;  return false ;} } ");

		// END FOR BUG # 345814

        //Modified for IR-077316V6R2013 start
    	sScript.append("var sStatus;  if(attrType == 'string') { if(sName.match('Estimated')!=null){  if((parseFloat(sHidElement.value)==parseFloat(txtEstimatedCost))&&(sHidElement.value.match(ele.value)!=null) )sStatus='true'; else sStatus = 'false'; } ");  //IR-077316V6R2012 fix
    	sScript.append(" else if(sName.match('Weight')!=null){if(parseFloat(sHidElement.value)==parseFloat(txtWeight+' '+ele.value)&&(sHidElement.value.match(' '+ele.value)!=null)) sStatus='true'; else sStatus = 'false';}");
  	    sScript.append("else{ if(sName.match('Date')==null) {var num = ele.selectedIndex;if(sHidElement.value==ele.options[num].text) sStatus='true'; else sStatus = 'false'; }if(sName.match('Date')!=null) {if( sHidElement.value==ele.value) sStatus='true'; else sStatus = 'false';} }} ");  //IR-077316V6R2012 fix

  	    sScript.append("  if(attrType == 'real') { if(sName.match('Estimated')!=null){  if((parseFloat(sHidElement.value)==parseFloat(value))&&((sHidElement.value).match(unitsEstimatedCost)!=null))sStatus='true'; else sStatus = 'false'; }  ");
  	    sScript.append("  else if(sName.match('Weight')!=null){  if((parseFloat(sHidElement.value)==parseFloat(value))&&((sHidElement.value).match(' '+unitsWeight)!=null))sStatus='true'; else sStatus = 'false'; }  ");
  	    sScript.append(" else{  if(parseFloat(sHidElement.value)==parseFloat(value)) sStatus='true'; else sStatus = 'false';  }} ");
        sScript.append("  if(attrType == 'integer') {  if(parseInt(sHidElement.value)==parseInt(value)) sStatus='true'; else sStatus = 'false';  } ");
        sScript.append(" if(sStatus=='true') {if(sName.match('Estimated')!=null){document.getElementsByName(\"txt_Estimated Cost\")[0].style.color = \"black\";document.getElementsByName(\"units_Estimated Cost\")[0].style.color = \"black\";}if(sName.match('Weight')!=null){document.getElementsByName(\"txt_Weight\")[0].style.color = \"black\";document.getElementsByName(\"units_Weight\")[0].style.color = \"black\";}sLabelElement.innerHTML = sHidElement.value; sLabelElement.style.color = \"black\";  ele.style.color = \"black\";if(sName.match('Date')!=null){ele.style.color = \"black\";} }");
        //Modified for IR-077316V6R2013 end

        sScript.append("  if(sStatus=='false')  {    sLabelElement.innerHTML = \"<s>\"+sHidElement.value+\"</s>\";   sLabelElement.style.color = \"red\";  ele.style.color = \"green\"; } ");
        sScript.append(" } </script>");

        sReturn = sOldValues.toString()+ "<td> " +strText.toString() + " "+strChoiceSelect.toString()+" </td></table> "+sScript.toString();

        return sReturn;
    }

  @com.matrixone.apps.framework.ui.PostProcessCallable
  public void createItemMarkup(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");


        String sAction = (String) requestMap.get("activity");
        String objectId = (String) requestMap.get("objectId");
        String sChangeName=(String) requestMap.get("ChangeDisplay");
        String sChangeOID=(String) requestMap.get( "ChangeOID");
        String sMarkupId;
        String accessExp;
        Map strSettings;

        DomainObject doMarkup;
        DomainObject doPart;

        UIForm uif = new UIForm();
        MapList mlUIForm = uif.getFields(context,"type_ItemMarkup", null,requestMap);
        Iterator itrUIForm = mlUIForm.iterator();

        StringBuffer attrValues = new StringBuffer(32);
        while(itrUIForm.hasNext())
        {
            Map map = (Map) itrUIForm.next();
            String fieldName = (String) map.get("name");
            strSettings = (Map) map.get("settings");
            accessExp = (String) strSettings.get("Access Expression");
            if("false".equalsIgnoreCase(accessExp))
            {
            	continue;
            }
            if(map.toString().indexOf(" Item Markup - Editable=false") != -1)
                continue;
            if(fieldName.indexOf("attribute_")==0)
            {
                String attrName = PropertyUtil.getSchemaProperty(context,fieldName);
                if(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE.equals(attrName)
        		|| EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED.equals(attrName)) {
                	continue;
                }

                AttributeType attType = new AttributeType(attrName);
                String attrType =attType.getDataType(context);
                 StringList attrChoices = attType.getChoices();
                 if(attrChoices==null || attrChoices.size()<=1)
                     if(UOMUtil.isAssociatedWithDimension(context, attrName)){
                    	//Modified for IR-119434V6R2012x start
                         //attrValues.append(attrName+"#"+(String) requestMap.get("txt_"+attrName) +"_"+(String) requestMap.get("units_"+attrName)+"#");
                    	 String strAttrValue = (String) requestMap.get("txt_"+attrName);
                    	 if(strAttrValue == null || strAttrValue.equals("") || "null".equals(strAttrValue)) {
                    		 attrValues.append(attrName+"#"+"0.0"+"_"+(String) requestMap.get("units_"+attrName)+"#");
                    	 } else {
                    		 attrValues.append(attrName+"#"+(String) requestMap.get("txt_"+attrName) +"_"+(String) requestMap.get("units_"+attrName)+"#");
                    	 }
                    	 //Modified for IR-119434V6R2012x end
                     }
                     else {
                         if("timestamp".equals(attrType)) {
                             String attrValue = (String)requestMap.get("txt_"+attrName);
                             DomainObject doPartTemp = new DomainObject(objectId);
                             String currentAttValue =  doPartTemp.getInfo(context, "attribute["+attrName+"]");
                             double iClientTimeOffset = (new Double((String)requestMap.get("timeZone"))).doubleValue();
                             Locale locale = (Locale)requestMap.get("localeObj");
                             String currentAttValueDis = eMatrixDateFormat.getFormattedDisplayDate(currentAttValue, iClientTimeOffset);
                             if (attrValue != null && !attrValue.equals("") && !attrValue.equals("null") ) {
                                 if(!currentAttValueDis.equals(attrValue)) {
                                     try {
                                         attrValue = eMatrixDateFormat.getFormattedInputDate(context, attrValue, iClientTimeOffset, locale);
                                     } catch (Exception e) {
                                     }
                                     attrValues.append(attrName+"#"+attrValue+"#");
                                 } else
                                     attrValues.append(attrName+"#"+(String) requestMap.get("txt_"+attrName)+"#");
                             } else
                                 attrValues.append(attrName+"#"+(String) requestMap.get("txt_"+attrName)+"#");

                         } else {
                             // IR-055011V6R2011x -Starts
                             String strValue= (String)requestMap.get("txt_"+attrName);
                             if("".equals(strValue)){
                                 strValue = " ";
                                 attrValues.append(attrName+"#"+strValue+"#");
                                 }else
                              attrValues.append(attrName+"#"+(String)requestMap.get("txt_"+attrName)+"#");
                              // IR-055011V6R2011x -Ends
                         }
                     }
                 else
                     attrValues.append(attrName+"#"+(String) requestMap.get("comboDescriptor_"+attrName)+"#");
            }
        }

        StringList attrValuesArr = FrameworkUtil.split(attrValues.toString(),"#");

        if("create".equals(sAction))  // if action is create
        {
            // creating part object from objectid.
            doPart = new DomainObject(objectId);
            doPart.open(context);

            // getting relationship type for affecteditem
            RelationshipType relType = new RelationshipType(PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem"));
            // creating change object.
            
          //Add for ENG Convergence Start
            DomainObject dChgObj = new DomainObject(sChangeOID);
            dChgObj.open(context);
            String strChgObjType = dChgObj.getInfo(context, SELECT_TYPE);
            if((ChangeConstants.TYPE_CHANGE_REQUEST.equalsIgnoreCase(strChgObjType) || ChangeConstants.TYPE_CHANGE_ORDER.equalsIgnoreCase(strChgObjType))) {					
				/*ChangeOrder changeOrder = new ChangeOrder(sChangeOID);
				StringList objIdList = new StringList();
				objIdList.add(objectId);
				HashMap mCAList = (HashMap) changeOrder.connectAffectedItems(context,objIdList);	
				HashMap mCAIdList = (HashMap)mCAList.get("objIDCAMap");				
				String strCAId = (String)mCAIdList.get(objectId);				
				sChangeOID = strCAId;	*/				
				Map changeActionMap    =  EngineeringUtil.checkPartAlreadyConnectedToThisChange(context, objectId, sChangeOID);
				String changeConnected = (String)changeActionMap.get("ALREADY_CONNECTED");
				if("TRUE".equalsIgnoreCase(changeConnected)) {						//
					sChangeOID = (String)changeActionMap.get(ChangeConstants.RELATED_CA_ID);
				}
				else {
					  ChangeOrder changeOrder = new ChangeOrder(sChangeOID);
			          StringList objIdList = new StringList();
			          objIdList.add(objectId);
			          HashMap mCAList   = (HashMap) changeOrder.connectAffectedItems(context,objIdList);
				      HashMap mCAIdList = (HashMap)mCAList.get("objIDCAMap");					      
				      String strCAId    = (String)mCAIdList.get(objectId);					     
				      sChangeOID = strCAId;
				}	
				
            }
            //Add for ENG Convergence End
           
            DomainObject fromDo = new DomainObject(sChangeOID);
            fromDo.open(context);

            try{
				String[] app = {"ENO_PRT_TP"};
            	ComponentsUtil.checkLicenseReserved(context,app);
            }
            catch(Exception ex){
            	throw new FrameworkException(ex.getMessage());
            }

            // creating markup object
            sMarkupId = FrameworkUtil.autoName(context,"type_ItemMarkup","policy_PartMarkup");
            // assigning id to object.
            doMarkup = new DomainObject(sMarkupId);

            doMarkup.open(context,true);
            // getting relationship between part and markup
            RelationshipType relTypePartToMarkup = new RelationshipType(PropertyUtil.getSchemaProperty(context,"relationship_EBOMMarkup"));
            // connecting part to markup

			String[] inputArgs = new String[2];

			inputArgs[0]= objectId;
			inputArgs[1]=sChangeOID;

			String strNewPartId = null;

			if (sChangeOID != null)
			{
				strNewPartId = (String) JPO.invoke(context, "enoEngChange", null, "getDirectAffectedItems", inputArgs,String.class);
			}

			if (strNewPartId != null)
			{
				doPart = new DomainObject(strNewPartId);
			}

			DomainObject doChange = new DomainObject(sChangeOID);

			String isChangeConnected = "false";
					
				//Modified for ENG Convergence start
				String strChangeType = "";
				String strPartId = doPart.getObjectId(context);							
				strChangeType = fromDo.getInfo(context, DomainConstants.SELECT_TYPE);														
				
				if(ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(strChangeType)) {
					isChangeConnected = isChangeObjectsInProposed(context,strPartId,sChangeOID);
                } else if(!ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(strChangeType)) {
                	relType = new RelationshipType(RELATIONSHIP_AFFECTED_ITEM);
                	isChangeConnected = isConnectedtoChangeProcess(context,strPartId,sChangeOID, RELATIONSHIP_AFFECTED_ITEM);
                }
				//Modified for ENG Convergence End
				
            // connecting part to change object.
            if (!"true".equalsIgnoreCase(isChangeConnected) && !ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(strChangeType))
            {
				DomainRelationship.connect(context,fromDo,relType,doPart);
            }

			ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
			DomainRelationship.connect(context,doPart,relTypePartToMarkup,new DomainObject(sMarkupId));

            RelationshipType relTypeChangeToMarkup = null;
            if(doChange.isKindOf(context, DomainConstants.TYPE_ECR))
            // getting relationship between change and markup
                relTypeChangeToMarkup = new RelationshipType(PropertyUtil.getSchemaProperty(context,"relationship_ProposedMarkup"));
            else
                relTypeChangeToMarkup = new RelationshipType(PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup"));
            // connecting change to markup
			DomainRelationship.connect(context,fromDo,relTypeChangeToMarkup,new DomainObject(sMarkupId));

            ContextUtil.popContext(context);

        }
        else    // if action is edit.
        {
            doMarkup = new DomainObject(objectId);
            String relName = PropertyUtil.getSchemaProperty(context,"relationship_EBOMMarkup");
            StringList objSelects = new StringList();
            objSelects.add("id");
            // get the part associated with the markup.
            Map map = doMarkup.getRelatedObject(context,relName,false,objSelects,DomainConstants.EMPTY_STRINGLIST);
            doPart = new DomainObject((String) map.get("id"));
            sMarkupId = objectId;
        }

        String sTransPath = context.createWorkspace();
        // create a file object.
        java.io.File fEmatrixWebRoot = new java.io.File(sTransPath);

        String sbusObjMarkupName = doMarkup.getInfo(context,"name");

        java.io.File srcXMLFile = new java.io.File(fEmatrixWebRoot, sbusObjMarkupName+ ".xml");
        // create a root element.
        com.matrixone.jdom.Element rootElement = new com.matrixone.jdom.Element("businessObject");
        // add attributes id and name to root element.
        rootElement.setAttribute("id",doMarkup.getInfo(context,"id"));
        rootElement.setAttribute("name",sbusObjMarkupName);  
        String strPartId = doPart.getObjectId(context);    
        for(int index = 0; index < attrValuesArr.size()-1; index+=2)
        {

            com.matrixone.jdom.Element attElement = new com.matrixone.jdom.Element("attribute");
            com.matrixone.jdom.Element attNameElement = new com.matrixone.jdom.Element("name");
            com.matrixone.jdom.Element attOldElement = new com.matrixone.jdom.Element("oldvalue");
            com.matrixone.jdom.Element attNewElement = new com.matrixone.jdom.Element("newvalue");
            attNameElement.setText(attrValuesArr.get(index).toString());
            if(UOMUtil.isAssociatedWithDimension(context, attrValuesArr.get(index).toString()))
          		attOldElement.setText(doPart.getInfo(context,"attribute[" + (String)attrValuesArr.get(index) + "].inputvalue")+"_"+UOMUtil.getInputunit(context,strPartId, (String) attrValuesArr.get(index)));
            else
                attOldElement.setText(doPart.getAttributeValue(context,(String) attrValuesArr.get(index)));

            //IR-055011V6R2011x -Starts
            if(attrValuesArr.get(index+1).equals(" "))
                attNewElement.setText("");
            else
                attNewElement.setText((String) attrValuesArr.get(index+1));
            //IR-055011V6R2011x -Ends
            attElement.addContent(attNameElement);
            attElement.addContent(attOldElement);
            attElement.addContent(attNewElement);
            rootElement.addContent(attElement);
        }

        com.matrixone.jdom.Element changeElement = new com.matrixone.jdom.Element("Change");
        changeElement.setAttribute("name",sChangeName);
        changeElement.setAttribute("id",sChangeOID);
        rootElement.addContent(changeElement);

        com.matrixone.jdom.Document docXML = new com.matrixone.jdom.Document(rootElement);


        try
        {
            String strEBOMCharset = "UTF-8";
            // set the font properties to xml outputter object.
            com.matrixone.jdom.output.XMLOutputter  xmlOutputter = com.matrixone.util.MxXMLUtils.getOutputter(true,strEBOMCharset);
            // create io buffer writer.
            java.io.BufferedWriter buf = new java.io.BufferedWriter(new java.io.FileWriter(srcXMLFile));
            // put xml document to putter.
            xmlOutputter.output(docXML, buf);
            buf.flush();
            buf.close();
			ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
            String mqlCmd = "checkin bus $1 $2 $3 $4";
            MqlUtil.mqlCommand(context, mqlCmd, TYPE_ITEMMARKUP, sbusObjMarkupName,"",sTransPath+"/"+sbusObjMarkupName+".xml");
			ContextUtil.popContext(context);

            srcXMLFile.delete();

        }
        catch(Exception e)
        {
        }

        doMarkup.close(context);

        // delete workspace.
        context.deleteWorkspace();
    }

    private String getItemMarkupXMLContent(Context context, String sMarkupId) throws Exception
    {
                Context ctx = context.getFrameContext("emxPartMarkup");
                try {
                    ctx.start(true);
                    return _getItemMarkupXMLContent(ctx,sMarkupId);
                } finally {
                    ctx.commit();
                    ctx.shutdown();
                }
    }

    private String _getItemMarkupXMLContent(Context context, String sMarkupId) throws Exception
    // must be called with a context that has an update transaction.
    {

                DomainObject doMarkup = new DomainObject(sMarkupId);

                String sXMLFormat = PropertyUtil.getSchemaProperty(context, "format_XML");
                String sTransPath = context.createWorkspace();
                java.io.File fEmatrixWebRoot = new java.io.File(sTransPath);

                doMarkup.checkoutFile(context, false, sXMLFormat, doMarkup.getInfo(context,"name")+".xml", fEmatrixWebRoot.toString());

                java.io.File srcXMLFile = new java.io.File(fEmatrixWebRoot, doMarkup.getInfo(context,"name")+ ".xml");

                com.matrixone.jdom.Document docXML = null;
                com.matrixone.jdom.Element rootElement = null;

                // send file to SAX builder.
                try
                {
                    com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder();
					builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
					builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    docXML = builder.build(srcXMLFile);
                    rootElement = docXML.getRootElement();

                }
                catch(Exception e)
                {
                }

                java.util.List attList = rootElement.getChildren("attribute");

                java.util.Iterator attributeItr = attList.iterator();

                StringBuffer sbXML = new StringBuffer();

                // get file values into file content.
                while(attributeItr.hasNext())
                {
                    com.matrixone.jdom.Element attElement = (com.matrixone.jdom.Element) attributeItr.next();
                    com.matrixone.jdom.Element attNameElement = attElement.getChild("name");
                    com.matrixone.jdom.Element attOldElement = attElement.getChild("oldvalue");
                    com.matrixone.jdom.Element attNewElement = attElement.getChild("newvalue");
                    sbXML.append(attNameElement.getText()+"#"+ attOldElement.getText()+"#"+attNewElement.getText()+"#");
                }

                com.matrixone.jdom.Element changeElement = rootElement.getChild("Change");
                sbXML.append( changeElement.getAttribute("id").getValue());

                srcXMLFile.delete();
                context.deleteWorkspace();

                return sbXML.toString();
    }


    public void commonViewPlantBOMMarkupInitialProcess(Context context,String args[]) throws Exception
    {
    	ContextUtil.startTransaction(context, true);
		try{
			HashMap programMap=(HashMap)JPO.unpackArgs(args);

			HashMap requestMap=(HashMap)programMap.get("requestMap");
			MapList mlElements = (MapList)requestMap.get("mapListElements");
			requestMap.remove("mapListElements");

			HashMap paramMap=(HashMap)programMap.get("paramMap");
			String sAction  = (String)paramMap.get("process");

			String commandType  = (String)paramMap.get("commandType");

			String sECChange = (String)paramMap.get("ECChange");
			String sPlantChange = (String)paramMap.get("PlantChange");

			Iterator itrMapList = mlElements.iterator();
			while(itrMapList.hasNext()){
				Map allMap = (Map)itrMapList.next();
				String sObjectId = (String)allMap.get("objectId");

				String rowId = (String)allMap.get("rowId");
				com.matrixone.jdom.Element rootElement = (com.matrixone.jdom.Element)allMap.get("xml");

				com.matrixone.jdom.output.XMLOutputter outputter = new com.matrixone.jdom.output.XMLOutputter();
				String sMassUpdateXML   = outputter.outputString(rootElement);


				requestMap.put("massUpdateAction", sMassUpdateXML);
				requestMap.put("objectId", sObjectId);
				requestMap.put("hasBOMChanges", (String)allMap.get("hasBOMChanges"));
				requestMap.put("hasPlantChanges", (String)allMap.get("hasPlantChanges"));
				paramMap.put("sObjId", sObjectId);

				if(sAction == null || (commandType.equalsIgnoreCase("SaveAs") &&
				   !rowId.equalsIgnoreCase("0") && sAction != null &&
				   sAction.equalsIgnoreCase("open"))){
					if(sAction != null && sAction.equalsIgnoreCase("open")){
					requestMap.put("commandType", "Save");
					}
					if(sECChange != null){
						StringList slSplitEC = FrameworkUtil.split(sECChange, "|");
						if(slSplitEC.size() == 2){
							sECChange = (String)slSplitEC.get(0);
							paramMap.put("ECChange", sECChange);
						}
					}

					if(sPlantChange != null){
						StringList slSplitPlant = FrameworkUtil.split(sPlantChange, "|");
						if(slSplitPlant.size() == 2){
							sPlantChange = (String)slSplitPlant.get(0);
							paramMap.put("PlantChange", sPlantChange);
						}
					}
					createPlantBOMMarkup(context, JPO.packArgs(programMap));
				}
				else if(rowId.equalsIgnoreCase("0") && sAction.equalsIgnoreCase("open")){
					savePlantBOMMarkup(context, JPO.packArgs(programMap));
					sAction = null;
				}
			}
	        ContextUtil.commitTransaction(context);
		}
		catch(Exception e){
    		ContextUtil.abortTransaction(context);
			throw e;
		}
	}

    public void createPlantBOMMarkup(Context context,String args[])
        throws Exception {
        /* Get Request Parameters */
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap requestMap=(HashMap)programMap.get("requestMap");

        String massUpdateAction=(String)requestMap.get("massUpdateAction");

        String hasBOMChanges =(String)requestMap.get("hasBOMChanges");
        String hasPlantChanges =(String)requestMap.get("hasPlantChanges");

        HashMap paramMap=(HashMap)programMap.get("paramMap");
        String sCommandType = (String)requestMap.get("commandType");

        String sAllPlants = (String)requestMap.get("allPlants");
        String strChangeRelNew ="";
        /* Get Object Id Of The Context Root Part */
        String sDescription=(String)paramMap.get("Description");
        String changeOID=(String)paramMap.get("ECChange");
        String changePlantID=(String)paramMap.get("PlantChange");

        String  selectedPartId = (String)paramMap.get("sObjId");
		String[] inputArgs = new String[2];

		inputArgs[0]= selectedPartId;		
		
		 if(changeOID != null && changeOID.length() != 0) {					
           	DomainObject doChgObj = new DomainObject(changeOID);
				if(doChgObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_ORDER) || doChgObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_REQUEST)) {
					 ChangeOrder changeOrder = new ChangeOrder(changeOID);
			          StringList objIdList = new StringList();
			          objIdList.add(selectedPartId);
				      HashMap mCAList = (HashMap) changeOrder.connectAffectedItems(context,objIdList);	
				      HashMap mCAIdList = (HashMap)mCAList.get("objIDCAMap");					      
				      String strCAId = (String)mCAIdList.get(selectedPartId);					     
				      changeOID = strCAId;
				}
		 }

		String strNewPartId = null;
         //Added for V6R2009.HF0.2 - Starts
        try{
         //Added for V6R2009.HF0.2 - Ends

		if (changeOID != null || changePlantID != null) {
			if(changeOID == null) {
				inputArgs[1]= changePlantID;
			} else {
				inputArgs[1]= changeOID;
			}
			strNewPartId = (String) JPO.invoke(context, "enoEngChange", null, "getDirectAffectedItems", inputArgs,String.class);
		}

		if (strNewPartId != null) {
			selectedPartId = strNewPartId;
		}
        /* Get Attribute Plant ID */
        String strAttrPlantID = (String)paramMap.get("plantID");
        /* Get Plant Object Id */
        String selectedPlantIds = "";
        if(sAllPlants == null || "null".equals(sAllPlants)){
            selectedPlantIds = getLocation(context, strAttrPlantID);
        }
        else{
            selectedPlantIds = strAttrPlantID;
        }

        /* Set Part Id */
        DomainObject domobj=new DomainObject();
        domobj.setId(selectedPartId);
        String sPolicy=domobj.getInfo(context,DomainConstants.SELECT_POLICY);

        DomainObject changeObject = null;
        String changetype = "";
        if(changeOID != null) {
        	changeObject = new DomainObject(changeOID);
        	changetype= changeObject.getInfo(context,DomainConstants.SELECT_TYPE);
		}
        DomainObject plantChangeObject = null;
        String plantChangetype = "";
        if(changePlantID != null) {
       		plantChangeObject = new DomainObject(changePlantID);
			plantChangetype= plantChangeObject.getInfo(context,DomainConstants.SELECT_TYPE);
		}

        String sBOMMarkupId=null;
        String sMarkupId=null;
        String str[]=new String[4];
        String strBOM[]=new String[4];
        StringList slPlants = FrameworkUtil.split(selectedPlantIds, "|");

		for(int indx=0;indx<slPlants.size();indx++){
            /* Get Plant ID */
            String selectedPlantId = (String)slPlants.get(indx);
            DomainObject domPlant = new DomainObject(selectedPlantId);
            strAttrPlantID = domPlant.getAttributeValue(context, ATTRIBUTE_PLANT_ID);
			String sMassUpdateAction = "";
			if(sAllPlants != null && !"null".equals(sAllPlants) && "true".equalsIgnoreCase(sAllPlants)){
				sMassUpdateAction = getActualPartMarkup(context, massUpdateAction, selectedPlantId);
			}
			else{
				sMassUpdateAction = massUpdateAction;
			}

        /* For Development Parts */
			if(DomainConstants.POLICY_DEVELOPMENT_PART.equalsIgnoreCase(sPolicy)) {
            /* Create An Object With Auto Name of Plant BOM Markup Type */
            /* Check Control Coming From Save Command */
				if(sCommandType==null || sCommandType.equalsIgnoreCase("Save")) {
					/* Create Plant BOM Markup For Development Part */
					if("true".equals(hasPlantChanges)) {
                sMarkupId=FrameworkUtil.autoName(context,"type_PlantBOMMarkup","policy_DevelopmentPartMarkup");
						DomainObject domMarkup= new DomainObject();
						domMarkup.setId(sMarkupId);
						domMarkup.setDescription(context,sDescription);
						domMarkup.setAttributeValue(context,ATTRIBUTE_PLANT_ID,strAttrPlantID);

				str[0]=sMassUpdateAction;
                str[1]=sMarkupId;
                str[2]=selectedPartId;
                str[3]=selectedPlantId;

                DomainRelationship.connect(context,new DomainObject(selectedPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sMarkupId));
                generatePlantBOMMarkupXML(context,str);
            }
					/* Create BOM Markup For Development Part */
					if("true".equals(hasBOMChanges)) {
						sBOMMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_DevelopmentPartMarkup");
						DomainObject domMarkup= new DomainObject();
						domMarkup.setId(sBOMMarkupId);
						domMarkup.setDescription(context,sDescription);
						strBOM[0]=sMassUpdateAction;
						strBOM[1]=sBOMMarkupId;
						strBOM[2]=selectedPartId;
						strBOM[3]=selectedPlantId;

						DomainRelationship.connect(context,new DomainObject(selectedPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sBOMMarkupId));
						generateBOMMarkupXML(context,strBOM);
					}
				}
            /* Check Control Coming From Save As Command */
				else {
                String sName=(String)requestMap.get("Name");
                String sRevision="-";
                // IR-013341
                String vaultName= Person.getPerson(context).getVaultName(context);

					/* Create Plant BOM Markup For Development Part */
					if("true".equals(hasPlantChanges)) {
						DomainObject domMarkup= new DomainObject();
						domMarkup.createObject(context,TYPE_PLANT_BOM_MARKUP,sName,sRevision,POLICY_DEVELOPMENTPARTMARKUP,vaultName);
						sMarkupId =domMarkup.getInfo(context,SELECT_ID);
						domMarkup.setDescription(context,sDescription);
						domMarkup.setAttributeValue(context,ATTRIBUTE_PLANT_ID,strAttrPlantID);

					str[0]=sMassUpdateAction;
                str[1]=sMarkupId;
                str[2]=selectedPartId;
                str[3]=selectedPlantId;

                DomainRelationship.connect(context,new DomainObject(selectedPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sMarkupId));
                generatePlantBOMMarkupXML(context,str);
            }
					/* Create BOM Markup For Development Part */
					if("true".equals(hasBOMChanges)) {
						DomainObject domMarkup= new DomainObject();
						domMarkup.createObject(context,TYPE_BOMMARKUP,sName,sRevision,POLICY_DEVELOPMENTPARTMARKUP,vaultName);
						sBOMMarkupId =domMarkup.getInfo(context,SELECT_ID);
						domMarkup.setDescription(context,sDescription);
						domMarkup.setAttributeValue(context,ATTRIBUTE_PLANT_ID,strAttrPlantID);

						strBOM[0]=sMassUpdateAction;
						strBOM[1]=sMarkupId;
						strBOM[2]=selectedPartId;
						strBOM[3]=selectedPlantId;

						DomainRelationship.connect(context,new DomainObject(selectedPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sBOMMarkupId));
						generateBOMMarkupXML(context,strBOM);
					}
				}
        }
        /* For Production Parts */
			else {
				/* Create Plant BOM Markup For EC Part */
				if("true".equals(hasPlantChanges)) {
					DomainObject domMarkup= new DomainObject();
            /* Check Control Coming From Save Command */
					if(sCommandType==null || sCommandType.equalsIgnoreCase("Save")) {
                sMarkupId=FrameworkUtil.autoName(context,"type_PlantBOMMarkup","policy_PartMarkup");
						domMarkup.setId(sMarkupId);
					} else {
						String sName=(String)requestMap.get("Name");
						// IR-013341
						String vaultName= Person.getPerson(context).getVaultName(context);

						String sRevision="-";
						domMarkup.createObject(context,
											 TYPE_PLANT_BOM_MARKUP,
											 sName,
											 sRevision,
											 POLICY_PARTMARKUP,
											 vaultName);
						sMarkupId = domMarkup.getInfo(context,SELECT_ID);
					}

					domMarkup.setDescription(context,sDescription);
					domMarkup.setAttributeValue(context,ATTRIBUTE_PLANT_ID,strAttrPlantID);
					str[0]=sMassUpdateAction;
                str[1]=sMarkupId;
                str[2]=selectedPartId;
                str[3]=selectedPlantId;
                    ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
                    DomainRelationship.connect(context,new DomainObject(selectedPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sMarkupId));
                    ContextUtil.popContext(context);
                    }
				/* Create BOM Markup For EC Part */
				if("true".equals(hasBOMChanges)) {
					DomainObject domMarkup= new DomainObject();
					/* Check Control Coming From Save Command */
					if(sCommandType==null || sCommandType.equalsIgnoreCase("Save")) {
						sBOMMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
						domMarkup.setId(sBOMMarkupId);
					} else {
						String sName=(String)requestMap.get("Name");
						// IR-013341
						String vaultName=Person.getPerson(context).getVaultName(context);
						String sRevision="-";
						domMarkup.createObject(context,
											 TYPE_BOMMARKUP,
											 sName,
											 sRevision,
											 POLICY_PARTMARKUP,
											 vaultName);
						sBOMMarkupId = domMarkup.getInfo(context,SELECT_ID);
                    }

					domMarkup.setDescription(context,sDescription);
					strBOM[0]=sMassUpdateAction;
					strBOM[1]=sBOMMarkupId;
					strBOM[2]=selectedPartId;
					strBOM[3]=selectedPlantId;
					DomainRelationship.connect(context,new DomainObject(selectedPartId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(sBOMMarkupId));
                }

				/* Get Part-Change Connectivity */
				String isChangeConnected = "false";
				String isPlantChangeConnected = "false";
				if(changeOID != null){
				strChangeRelNew = RELATIONSHIP_CHANGE_AFFECTED_ITEM;
				
				if(ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(changetype)) 
					isChangeConnected = isConnectedtoCA(context,selectedPartId,changeOID, strChangeRelNew);
				else
					isChangeConnected = isConnectedtoChangeProcess(context,selectedPartId,changeOID, null);
				}
				if(changePlantID != null)
					isPlantChangeConnected = isConnectedtoChangeProcess(context,selectedPartId,changePlantID, null);

				if(isChangeConnected.equalsIgnoreCase("true") || isPlantChangeConnected.equalsIgnoreCase("true")) {
					/* Default Relationship between ECR/DCR/ECO/MECO and Markup */
					String relName_2 = RELATIONSHIP_PROPOSED_MARKUP;
					if("true".equals(hasBOMChanges)) {
						/* Connect Change-Markup */
						if(changeOID != null && (changetype.equals(DomainConstants.TYPE_ECO) || changetype.equals(ChangeConstants.TYPE_CHANGE_ACTION))) {
							relName_2 = RELATIONSHIP_APPLIED_MARKUP;
                        }
                        DomainRelationship.connect(context,
                                                   new DomainObject(changeOID),
												   relName_2,
												   new DomainObject(sBOMMarkupId));

						/* Generate BOM Markup XML For EC Part */
						generateBOMMarkupXML(context,strBOM);
					}
					if("true".equals(hasPlantChanges)) {
						if(changePlantID == null) {
							relName_2 = RELATIONSHIP_PROPOSED_MARKUP;
							/* Connect Change-Markup */
							if(changeOID != null && (changetype.equals(DomainConstants.TYPE_ECO) ||  changetype.equals(ChangeConstants.TYPE_CHANGE_ACTION))) {
								relName_2 = RELATIONSHIP_APPLIED_MARKUP;
							}
                        DomainRelationship.connect(context,
                                                   new DomainObject(changeOID),
													   relName_2,
                                                   new DomainObject(sMarkupId));
						} else {
							relName_2 = RELATIONSHIP_PROPOSED_MARKUP;
							/* Connect Change-Markup */
							if(plantChangetype.equals(TYPE_MECO)) {
								relName_2 = RELATIONSHIP_APPLIED_MARKUP;
								String relName_1 = RELATIONSHIP_AFFECTED_ITEM;
								isChangeConnected = isConnectedtoChangeProcess(context,selectedPartId,changePlantID, null);
								if("false".equals(isChangeConnected))
								 DomainRelationship.connect(context,
	                                     new DomainObject(changePlantID),
										   relName_1,
										   new DomainObject(selectedPartId));
                    }
							
							
					    ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
                        DomainRelationship.connect(context,
													   new DomainObject(changePlantID),
													   relName_2,
                                                   new DomainObject(sMarkupId));
                        ContextUtil.popContext(context);

                }
            	    generatePlantBOMMarkupXML(context,str);
            }
				}
				else {
					/* Default Relationship between Part and ECR/DCR/ECO/MECO */
					String relName_1 = RELATIONSHIP_AFFECTED_ITEM;
					/* Default Relationship between ECR/DCR/ECO/MECO and Markup */
					String relName_2 = RELATIONSHIP_PROPOSED_MARKUP;
					if("true".equals(hasBOMChanges)) {
						relName_1 = RELATIONSHIP_AFFECTED_ITEM;
						
						if(changeOID != null && ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(changetype)) 
							relName_1 = RELATIONSHIP_CHANGE_AFFECTED_ITEM;
						/* Connect Change-Part */
						if(changeOID != null){
						if(ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(changetype)) 
							isChangeConnected = isConnectedtoCA(context,selectedPartId,changeOID, strChangeRelNew);
						else
							isChangeConnected = isConnectedtoChangeProcess(context,selectedPartId,changeOID, null);
						}
						if("false".equals(isChangeConnected)) {
                        DomainRelationship.connect(context,
                                                    new DomainObject(changeOID),
												   relName_1,
												   new DomainObject(selectedPartId));
						}
						/* Connect Change-Markup */
						if(changeOID != null && (changetype.equals(DomainConstants.TYPE_ECO) || (changetype.equals(ChangeConstants.TYPE_CHANGE_ACTION)))) {
							relName_2 = RELATIONSHIP_APPLIED_MARKUP;
                    }
                        DomainRelationship.connect(context,
                                                    new DomainObject(changeOID),
												   relName_2,
												   new DomainObject(sBOMMarkupId));

						/* Generate BOM Markup XML For EC Part */
						generateBOMMarkupXML(context,strBOM);
                    }
					if("true".equals(hasPlantChanges)) {
                        //R209 MECO changes - Starts
                        DomainRelationship doAffectedItemRelObj = null;
                        //R209 MECO changes - Ends
						if(changePlantID == null) {
							relName_1 = RELATIONSHIP_AFFECTED_ITEM;
							relName_2 = RELATIONSHIP_PROPOSED_MARKUP;
							if(changeOID != null && changetype.equals(ChangeConstants.TYPE_CHANGE_ACTION)) {
								relName_1 = RELATIONSHIP_CHANGE_AFFECTED_ITEM;						

                            }
							/* Connect Change-Part */
							if(changeOID != null){
								if(ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(changetype))
								 isChangeConnected = isConnectedtoCA(context,selectedPartId,changeOID, strChangeRelNew);
								else
								 isChangeConnected = isConnectedtoChangeProcess(context,selectedPartId,changeOID, null);
							}
								
							if("false".equals(isChangeConnected)) {
                                //R209 MECO changes - Starts
                                doAffectedItemRelObj = DomainRelationship.connect(context,
                                                            new DomainObject(changeOID),
                                                            relName_1,
                                                            new DomainObject(selectedPartId));
                                //R209 MECO changes - Ends

							}
							/* Connect Change-Markup */
							if(changeOID != null && (changetype.equals(DomainConstants.TYPE_ECO) || changetype.equals(ChangeConstants.TYPE_CHANGE_ACTION))) {
								relName_2 = RELATIONSHIP_APPLIED_MARKUP;
							}
                        DomainRelationship.connect(context,
                                                    new DomainObject(changeOID),
													   relName_2,
                                                    new DomainObject(sMarkupId));
						} else {
							relName_2 = RELATIONSHIP_PROPOSED_MARKUP;
							/* Connect Change-Part */
							if(changePlantID != null)
								isPlantChangeConnected = isConnectedtoChangeProcess(context,selectedPartId,changePlantID, null);
							if("false".equals(isPlantChangeConnected)) {
							//R209 MECO changes - Starts
							doAffectedItemRelObj = DomainRelationship.connect(context,
													   new DomainObject(changePlantID),
													   RELATIONSHIP_AFFECTED_ITEM,
													   new DomainObject(selectedPartId));
							//R209 MECO changes - Ends
                    }
							/* Connect Change-Markup */
							if(plantChangetype.equals(TYPE_MECO)) {
								relName_2 = RELATIONSHIP_APPLIED_MARKUP;
                        }
                        DomainRelationship.connect(context,
													   new DomainObject(changePlantID),
													   relName_2,
                                                   new DomainObject(sMarkupId));
                    }
                    //R209 MECO changes - Starts
                    if(!"true".equals(hasBOMChanges)) {
                        String strAttrRequestedChange = PropertyUtil.getSchemaProperty(context,"attribute_RequestedChange");
                      //Multitenant
                        String strRequestedChangeValue =EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",new Locale("en"),"emxFramework.Range.Requested_Change.For_Update");
                        doAffectedItemRelObj.setAttributeValue(context,strAttrRequestedChange,strRequestedChangeValue);
                    }
                    //R209 MECO changes - Ends
                generatePlantBOMMarkupXML(context,str);
					} // End Of if(hasPlantChanges.equals("true"))
            }
        }
    }
       //Added for V6R2009.HF0.2 - Starts
       } catch(Exception e){
        throw e;
      }
        //Added for V6R2009.HF0.2 - Ends
    }

    public String getActualPartMarkup(Context context,String massUpdateAction, String strSelPlantId) throws Exception
    {
        String sMassUpdateAction = "";
        try{
            com.matrixone.jdom.Element retElement = new com.matrixone.jdom.Element("mxRoot");
            if(massUpdateAction!=null && !"null".equals(massUpdateAction)){
                java.io.CharArrayReader reader = new java.io.CharArrayReader(massUpdateAction.toCharArray());
                com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder(false);
				builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
				builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                builder.setValidation(false);
                com.matrixone.jdom.Document doc = builder.build(reader);
                /* Get The Top Level Element i.e. <mxRoot> */
                com.matrixone.jdom.Element rootElement = doc.getRootElement();
                java.util.List objList = rootElement.getChildren("object");
                Iterator objItr = objList.iterator();
                while(objItr.hasNext())
                {
                    Element topElement = (Element) objItr.next();
                    String sObjectId = (String)topElement.getAttributeValue("objectId");
                    if(sObjectId.equals(strSelPlantId)){
                        com.matrixone.jdom.Element objElement = new com.matrixone.jdom.Element("object");
                        retElement.addContent(objElement);

                        objElement.setAttribute("rowId",topElement.getAttributeValue("rowId"));
                        objElement.setAttribute("objectId",topElement.getAttributeValue("parentId"));
                        objElement.setAttribute("relId",topElement.getAttributeValue("relId"));
                        objElement.setAttribute("parentId","");
                        objElement.setAttribute("markup",topElement.getAttributeValue("markup"));
                        java.util.List columList = (java.util.List)topElement.getChildren("column");
                        Iterator colItr = columList.iterator();
                        while(colItr.hasNext()){
                            Element eleAttrib = (Element) colItr.next();
                            Element attrElement = new Element("column");
                            objElement.addContent(attrElement);
                            attrElement.setAttribute("name", eleAttrib.getAttributeValue("name"));
                            attrElement.setAttribute("edited", eleAttrib.getAttributeValue("edited"));
                            attrElement.setText(eleAttrib.getText());
                        }
                        break;
                    }
                }
                com.matrixone.jdom.output.XMLOutputter outputter = new com.matrixone.jdom.output.XMLOutputter();
                sMassUpdateAction = outputter.outputString(retElement);
            }
        }
        catch(Exception e){
            throw e;
        }
        return sMassUpdateAction;
    }

    public void generatePlantBOMMarkupXML(Context context,String[] args)
    	throws Exception {
        /* Get Changes Captured From Structure Browser XML */
        String massUpdateAction=args[0];
        /* Get Created Markup Id */
        String strMarkupId=args[1];
        /* Get Selected Part Id */
        String strSelPartId=args[2];
        String strSelPlantId = args[3];
        /* Instantiate 'Plant BOM Markup' Object */
        DomainObject objectPlantBOMMarkup = new DomainObject();
        objectPlantBOMMarkup.setId(strMarkupId);
        /* Create Workspace For Output XML File Checkin */
        objectPlantBOMMarkup.open(context);

        /* Create Root Element For Output XML */
        com.matrixone.jdom.Element ematrixRootElement = new com.matrixone.jdom.Element("ematrix");
        /* If Change Data Got From Structure Browser */

        /* Get Editable Table Column Names For Plant BOM Changes */
		String strPlantBOMColumns = (String)EnoviaResourceBundle.getProperty(context, "emxMBOM.View.PlantBOMChangesAllowed");
     	StringList listPlantBOMColumn = FrameworkUtil.split(strPlantBOMColumns,"|");
     	String sessionTimeZone = context.getSession().getTimezone();
     	TimeZone tj = TimeZone.getTimeZone(sessionTimeZone);
     	int rawOffset = tj.getRawOffset();
        double iClientTimeOffset = (new Double((new Double(-rawOffset)).doubleValue() / 3600000)).doubleValue();
         //Added for V6R2009.HF0.2 - Starts
         StringList slEBOMAttrs = new StringList();
         slEBOMAttrs.addElement(DomainConstants.ATTRIBUTE_FIND_NUMBER);
         slEBOMAttrs.addElement(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
         slEBOMAttrs.addElement(DomainConstants.ATTRIBUTE_QUANTITY);
         //Added for V6R2009.HF0.2 - Ends

     	/* Variables To Hold Substitute And Alternate Part Ids To Prevent Duplicate Entry In The XML File */
     	StringList alternateList = new StringList();
     	StringList substituteList = new StringList();

        if (massUpdateAction != null) {
            try {
                /* Get Selected Part Id */
                /* Set Object Id For Selected Row i.e Parent Node Id */
                DomainObject domobj = new DomainObject();
                domobj.setId(strSelPartId);
                /* Get Parent Node Information */
                String parentType          = domobj.getInfo(context,DomainConstants.SELECT_TYPE);
                String parentRevision      = domobj.getInfo(context,DomainConstants.SELECT_REVISION);
                String parentPolicy        = domobj.getInfo(context,DomainConstants.SELECT_POLICY);
                String parentVault         = domobj.getInfo(context,DomainConstants.SELECT_VAULT);
                String parentOwner         = domobj.getInfo(context,DomainConstants.SELECT_OWNER);
                String parentName          = domobj.getInfo(context,DomainConstants.SELECT_NAME);
                String parentDescription = domobj.getDescription(context);

                /* Create XML Data For Parent Part Start */
                com.matrixone.jdom.Element businessObjectElement = new com.matrixone.jdom.Element("businessObject");
                businessObjectElement.setAttribute("id",strSelPartId);
                ematrixRootElement.addContent(businessObjectElement);
                com.matrixone.jdom.Element objectTypeElement = new com.matrixone.jdom.Element("objectType");
                //Start : IR-054311V6R2011x
                objectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentType));
                //End : IR-054311V6R2011x
                businessObjectElement.addContent(objectTypeElement);
                com.matrixone.jdom.Element objectNameElement = new com.matrixone.jdom.Element("objectName");
                //Start : IR-054311V6R2011x
                objectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentName));
                //End : IR-054311V6R2011x
                businessObjectElement.addContent(objectNameElement);
                com.matrixone.jdom.Element objectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
                //Start : IR-054311V6R2011x
                objectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentRevision));
                //End : IR-054311V6R2011x
                businessObjectElement.addContent(objectRevisionElement);
                com.matrixone.jdom.Element vaultRefElement = new com.matrixone.jdom.Element("vaultRef");
                //Start :IR-054311V6R2011x
                vaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentVault));
                //End : IR-054311V6R2011x
                businessObjectElement.addContent(vaultRefElement);
                com.matrixone.jdom.Element policyRefElement = new com.matrixone.jdom.Element("policyRef");
                policyRefElement.setText(parentPolicy);
                businessObjectElement.addContent(policyRefElement);
                com.matrixone.jdom.Element ownerElement = new com.matrixone.jdom.Element("owner");
                businessObjectElement.addContent(ownerElement);
                com.matrixone.jdom.Element userRefElement = new com.matrixone.jdom.Element("userRef");
                //Start : IR-054311V6R2011x
                userRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentOwner));
                //End : IR-054311V6R2011x
                ownerElement.addContent(userRefElement);
                com.matrixone.jdom.Element descriptionElement = new com.matrixone.jdom.Element("description");
                //Start : IR-054311V6R2011x
                descriptionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(parentDescription));
                //End : IR-054311V6R2011x
                businessObjectElement.addContent(descriptionElement);
                com.matrixone.jdom.Element fromRelationshipListElement=new com.matrixone.jdom.Element("fromRelationshipList");
                businessObjectElement.addContent(fromRelationshipListElement);
                /* Create XML Data For Parent Part End */

                /* XML Tags For Changed Items */
                com.matrixone.jdom.Element relationshipElement        = null;
                com.matrixone.jdom.Element relationshipDefRefElement  = null;
                com.matrixone.jdom.Element relatedObjectElement       = null;
                com.matrixone.jdom.Element businessObjectRefElement   = null;
                com.matrixone.jdom.Element childObjectTypeElement     = null;
                com.matrixone.jdom.Element childObjectNameElement     = null;
                com.matrixone.jdom.Element childObjectRevisionElement = null;
                com.matrixone.jdom.Element childObjectIDElement		  = null;
                com.matrixone.jdom.Element childVaultRefElement       = null;
                com.matrixone.jdom.Element attributeListElement       = null;
                com.matrixone.jdom.Element attributeElement           = null;
                com.matrixone.jdom.Element nameElement                = null;
                com.matrixone.jdom.Element StringElement              = null;

				java.io.CharArrayReader reader = new java.io.CharArrayReader(massUpdateAction.toCharArray());
                    com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder(false);
					builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
					builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                    builder.setValidation(false);
                    com.matrixone.jdom.Document doc = builder.build(reader);
                    /* Get The Top Level Element i.e. <mxRoot> */
                    com.matrixone.jdom.Element rootElement = doc.getRootElement();

                    /* Iterate the Change List Objects */
                    java.util.List objList = rootElement.getChildren("object");
                    Iterator objItr = objList.iterator();
                    ArrayList listChangeRemove = new ArrayList();
                    StringList listChangeRemoveId = new StringList();
                while(objItr.hasNext()) {
					com.matrixone.jdom.Element topElement = (com.matrixone.jdom.Element) objItr.next();
                        String strObjectId = topElement.getAttributeValue("objectId");
                        String sMarkup = "none";
                        //Modified for V6R2009.HF0.2 - Starts
                            sMarkup   = topElement.getAttributeValue("markup");
                        if(sMarkup == null || "null".equals(sMarkup) || "".equals(sMarkup)){
                            sMarkup = "none";
                        }
                        //Modified for V6R2009.HF0.2 - Ends

					if("cut".equals(sMarkup) || "changed".equals(sMarkup)) {
						String strRelationshipId = topElement.getAttributeValue("relId");
						String strKey = strObjectId;

                            if(strRelationshipId != null) {
                                       strKey = strRelationshipId+"~"+strObjectId;
                            }
						if(!listChangeRemoveId.contains(strKey)) {
                                listChangeRemove.add(topElement);
                                listChangeRemoveId.add(strKey);
                            }
                        }
                        java.util.List childList = topElement.getChildren("object");
                        Iterator childItr = childList.iterator();

					while(childItr.hasNext()) {
						/* Get Each Child Change */
						com.matrixone.jdom.Element childElement = (com.matrixone.jdom.Element) childItr.next();
                            String sObjectId = childElement.getAttributeValue("objectId");
                            sMarkup   = childElement.getAttributeValue("markup");
                            DomainObject childObject = new DomainObject();
                            childObject.setId(sObjectId);
						if(sMarkup != null && sMarkup.equalsIgnoreCase("add")) {
                                String sRelType  = childElement.getAttributeValue("relType");
							String sEBOMRelId = "";
							if(sRelType.indexOf('|') != -1) {
                                java.util.StringTokenizer token = new java.util.StringTokenizer(sRelType,"|");
                                while(token.hasMoreTokens())
                                {
									sRelType = token.nextToken();
                                    sEBOMRelId = token.nextToken();
                                }
							}
							String strRelName = PropertyUtil.getSchemaProperty(context, sRelType);
                                if(strRelName.equals(DomainConstants.RELATIONSHIP_EBOM))
                                    continue;

                                // Added To Remove Duplicate Entries While Creating A Plant BOM Markup START
                                String key = sEBOMRelId+"~"+sObjectId;
                                if(strRelName.equals(DomainConstants.RELATIONSHIP_ALTERNATE)) {
									if(alternateList.contains(key)) {
										continue;
									} else {
										alternateList.add(key);
									}
								} else {
									if(substituteList.contains(key)) {
										continue;
									} else {
										substituteList.add(key);
									}
								}
								// Added To Remove Duplicate Entries While Creating A Plant BOM Markup END

                                relationshipElement = new com.matrixone.jdom.Element("relationship");
                                relationshipElement.setAttribute("chgtype","A");
                                fromRelationshipListElement.addContent(relationshipElement);

                                relationshipDefRefElement = new com.matrixone.jdom.Element("relationshipDefRef");
                                relationshipDefRefElement.setText(strRelName);
                                relationshipDefRefElement.setAttribute("relId", "");
                                relationshipElement.addContent(relationshipDefRefElement);
                                relatedObjectElement = new com.matrixone.jdom.Element("fromRel");

                                /* Execute MQL Command */
                                // Modified for V6R2009.HF0.2 - Starts
                                String primaryId = "";
                                if(sEBOMRelId.indexOf('^') == -1){
                                	primaryId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3", sEBOMRelId, "to.id", "|");
                                }
                                else{
                                    primaryId = sEBOMRelId.substring(sEBOMRelId.indexOf('^')+1, sEBOMRelId.length());
                                    primaryId = primaryId.substring(0, primaryId.indexOf('^'));
                                }
                                // Modified for V6R2009.HF0.2 - Ends

                                relatedObjectElement.setAttribute("relId",sEBOMRelId);
                                relatedObjectElement.setAttribute("objectId",primaryId);
                                relationshipElement.addContent(relatedObjectElement);

                                businessObjectRefElement = new com.matrixone.jdom.Element("businessObjectRef");
                                relatedObjectElement.addContent(businessObjectRefElement);

                                childObjectTypeElement = new com.matrixone.jdom.Element("objectType");
                                childObjectTypeElement.setAttribute("chgtype","A");
                                //Start : IR-054311V6R2011x
                                childObjectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_TYPE)));
                                //End : IR-054311V6R2011x
                                businessObjectRefElement.addContent(childObjectTypeElement);

                                childObjectNameElement = new com.matrixone.jdom.Element("objectName");
                                childObjectNameElement.setAttribute("chgtype","A");
                                //Start : IR-054311V6R2011x
                                childObjectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_NAME)));
                                //End : IR-054311V6R2011x
                                businessObjectRefElement.addContent(childObjectNameElement);

                                childObjectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
                                childObjectRevisionElement.setAttribute("chgtype","A");
                                //Start : IR-054311V6R2011x
                                childObjectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_REVISION)));
                                //End : IR-054311V6R2011x
                                businessObjectRefElement.addContent(childObjectRevisionElement);

                                childObjectIDElement = new com.matrixone.jdom.Element("childObjectID");
                                childObjectIDElement.setAttribute("chgtype","A");
                                childObjectIDElement.setText(childObject.getInfo(context,DomainConstants.SELECT_ID));
                                businessObjectRefElement.addContent(childObjectIDElement);

                                childVaultRefElement = new com.matrixone.jdom.Element("vaultRef");
                                //Start : IR-054311V6R2011x
                                childVaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_VAULT)));
                                //End : IR-054311V6R2011x
                                businessObjectRefElement.addContent(childVaultRefElement);

                                /* <attributeList> */
                                attributeListElement = new com.matrixone.jdom.Element("attributeList");
                                attributeListElement.setAttribute("plantId", strSelPlantId);
                                relationshipElement.addContent(attributeListElement);

                                java.util.List columnElementList =
                                childElement.getChildren("column");
                                Iterator columnItr = columnElementList.iterator();

                                Map children = new HashMap();
                                //Added for V6R2009.HF0.2 - Starts
                                Map mEBOMAttrs = new HashMap();
                                //Added for V6R2009.HF0.2 - Ends
                                StringList checkList = new StringList();
                                Map map = null;
                                String dataType = "";

                                while(columnItr.hasNext()) {
                                	com.matrixone.jdom.Element columnElement = (com.matrixone.jdom.Element) columnItr.next();
                                	String name = columnElement.getAttributeValue("name");
                                	if(listPlantBOMColumn.contains(name)) {
                                		String value = columnElement.getText();
                                		dataType = "string";
                                		if("Pref Rank".equals(name)) {
                                			dataType = "integer";
                                		}
                                		else if("Target Start Date".equals(name)) {
                                			dataType = "date";
                                			name = "Start Date";
                                			 if(null!=value && !"null".equals(value) && !"".equals(value))
                                				 try{
                                			value = eMatrixDateFormat.getFormattedInputDate(context, value, iClientTimeOffset, (Locale)i18nNow.getLocale(context.getSession().getLanguage()));
                                				 }catch (Exception e) {
													// Exception should not be thrown from here
												}
                                		}
                                		else if("Target End Date".equals(name)) {
                                			dataType = "date";
                                			name = "End Date";
                                			 if(null!=value && !"null".equals(value) && !"".equals(value))
                                				 try{
                                			value = eMatrixDateFormat.getFormattedInputDate(context, value, iClientTimeOffset, (Locale)i18nNow.getLocale(context.getSession().getLanguage()));
                                				 }catch (Exception e) {
													// Exception should not be thrown from here
												}
                                		}

                                		if(!checkList.contains(name)) {

                                			map = new HashMap();
                                			map.put("name",name);
                                			map.put("datatype",dataType);
                                			map.put("value",value);
                                			checkList.add(name);
                                		} else {
                                			map = (Map)children.get(name);
                                			map.put("name",name);
                                			map.put("datatype",dataType);
                                			map.put("value",value);
                                		}
                                		children.put(name,map);
                                	}
                                	// Added for V6R2009.HF0.2 - Starts
                                	if(slEBOMAttrs.contains(name)){
                                		mEBOMAttrs.put(name, columnElement.getText());
                                	}
                                	// Added for V6R2009.HF0.2 - Ends
                                }


                                // Added for V6R2009.HF0.2 - Starts
                                if(mEBOMAttrs.size() == 3){
                                    String sFNRDQTY = (String)mEBOMAttrs.get(DomainConstants.ATTRIBUTE_FIND_NUMBER) + "^";
                                    sFNRDQTY += (String)mEBOMAttrs.get(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR) + "^";
                                    sFNRDQTY += (String)mEBOMAttrs.get(DomainConstants.ATTRIBUTE_QUANTITY);
                                    relatedObjectElement.setAttribute("fn-rd-qty",sFNRDQTY);
                                }
                                // Added for V6R2009.HF0.2 - Ends

                                java.util.Collection collection = children.values();
                                Iterator itr = collection.iterator();
                                while(itr.hasNext()) {
                                    Map childMap = (Map)itr.next();
                                    String tagName = (String)childMap.get("name");
                                    String tagType = (String)childMap.get("datatype");
                                    String tagValue = (String)childMap.get("value");

                                    attributeElement = new com.matrixone.jdom.Element("attribute");
                                    attributeElement.setAttribute("chgtype","A");
                                    attributeListElement.addContent(attributeElement);
                                    nameElement = new com.matrixone.jdom.Element("name");
                                    nameElement.setText(tagName);
                                    attributeElement.addContent(nameElement);
                                    StringElement = new com.matrixone.jdom.Element(tagType);
                                    StringElement.setText(tagValue);
                                    attributeElement.addContent(StringElement);
                                }
                            }
						else if(sMarkup != null &&
						        (sMarkup.equalsIgnoreCase("changed") ||
						         sMarkup.equalsIgnoreCase("cut"))) {
							String strRelationshipId = childElement.getAttributeValue("relId");
							String strKey = strRelationshipId+"~"+sObjectId;
							if(!listChangeRemoveId.contains(strKey)) {
                                    listChangeRemove.add(childElement);
                                    listChangeRemoveId.add(strKey);
                                }
                            }
                        } // End Of while(childItr.hasNext())
                    } // End Of while(objItr.hasNext())


				for(int k=0;k<listChangeRemove.size();k++) {
                    com.matrixone.jdom.Element childElement=(com.matrixone.jdom.Element)listChangeRemove.get(k);
                        String sObjectId = childElement.getAttributeValue("objectId");
                        String sMarkup   = childElement.getAttributeValue("markup");

                        DomainObject childObject = new DomainObject();
                        childObject.setId(sObjectId);

                            String sRelId  = childElement.getAttributeValue("relId");
					// Added For X+3 To Support Normal BOM Handling In Common View START
					DomainRelationship domRel = new DomainRelationship(sRelId);
					domRel.open(context);
					String relName = domRel.getTypeName();
					int status = checkForBOMChanges(context, null, sRelId , childElement, "change");
					if(status == 0)
						continue;
					// Added For X+3 To Support Normal BOM Handling In Common View END

					if(sMarkup.equalsIgnoreCase("changed")) {

                            /* Execute MQL Command */
                            relationshipElement = new com.matrixone.jdom.Element("relationship");
                            relationshipElement.setAttribute("chgtype","C");
                            fromRelationshipListElement.addContent(relationshipElement);

                            relationshipDefRefElement = new com.matrixone.jdom.Element("relationshipDefRef");
                            relationshipDefRefElement.setAttribute("relId", sRelId);
                            relationshipDefRefElement.setText(relName);
                            relationshipElement.addContent(relationshipDefRefElement);
                            relatedObjectElement = new com.matrixone.jdom.Element("fromRel");

                            String ebomId = "";
							String strRelToRel = PropertyUtil.getSchemaProperty(context, "relationship_EBOMManufacturingResponsibility");
                            String primaryId = "";
						if ((relName.trim()).equals(DomainConstants.RELATIONSHIP_ALTERNATE)) {
                                ebomId = "";
                                strRelToRel = PropertyUtil.getSchemaProperty(context, "relationship_AlternateManufacturingResponsibility");
                            }
						else if((relName.trim()).equals(PropertyUtil.getSchemaProperty(context, "relationship_EBOMSubstitute"))) {
								strRelToRel = PropertyUtil.getSchemaProperty(context, "relationship_SubstituteManufacturingResponsibility");
								ebomId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",sRelId,"fromrel.id","|");
                                primaryId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",ebomId,"to.id","|");
                            }

                            relatedObjectElement.setAttribute("relId",ebomId);
                            relatedObjectElement.setAttribute("objectId",primaryId);
                            relationshipElement.addContent(relatedObjectElement);

                            businessObjectRefElement = new com.matrixone.jdom.Element("businessObjectRef");
                            relatedObjectElement.addContent(businessObjectRefElement);

                            childObjectTypeElement = new com.matrixone.jdom.Element("objectType");
                            childObjectTypeElement.setAttribute("chgtype","C");
                            //Start : IR-054311V6R2011x
                            childObjectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_TYPE)));
                            //End : IR-054311V6R2011x
                            businessObjectRefElement.addContent(childObjectTypeElement);

                            childObjectNameElement = new com.matrixone.jdom.Element("objectName");
                            childObjectNameElement.setAttribute("chgtype","C");
                            //Start : IR-054311V6R2011x
                            childObjectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_NAME)));
                            //End : IR-054311V6R2011x
                            businessObjectRefElement.addContent(childObjectNameElement);

                            childObjectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
                            childObjectRevisionElement.setAttribute("chgtype","C");
                            //Start : IR-054311V6R2011x
                            childObjectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_REVISION)));
                            //End : IR-054311V6R2011x
                            businessObjectRefElement.addContent(childObjectRevisionElement);

                            childVaultRefElement = new com.matrixone.jdom.Element("vaultRef");
                            //Start : IR-054311V6R2011x
                            childVaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_VAULT)));
                            //End : IR-054311V6R2011x
                            businessObjectRefElement.addContent(childVaultRefElement);

                            /* <attributeList> */
                            attributeListElement = new com.matrixone.jdom.Element("attributeList");
                            attributeListElement.setAttribute("plantId", strSelPlantId);
                            relationshipElement.addContent(attributeListElement);

                            java.util.List columnElementList =
                            childElement.getChildren("column");
                            Iterator columnItr = columnElementList.iterator();

                            Map children = new HashMap();
                            StringList checkList = new StringList();
                            Map map = null;
                            String dataType = "";

						while(columnItr.hasNext()) {
                                Element columnElement = (Element) columnItr.next();
                                String name = columnElement.getAttributeValue("name");
							/* Changed Attribute Values */
							if(listPlantBOMColumn.contains(name)) {
                                String value = columnElement.getText();
                                dataType = "string";
								if("Pref Rank".equals(name)) {
                                    dataType = "integer";
                                }
								else if("Target Start Date".equals(name)) {
                                    dataType = "date";
                                    name = "Start Date";
                                    if(null!=value && !"null".equals(value) && !"".equals(value))
                                    	try{
                                    value = eMatrixDateFormat.getFormattedInputDate(context, value, iClientTimeOffset, (Locale)i18nNow.getLocale(context.getSession().getLanguage()));
                                    	}catch (Exception e) {
											// Exception should not be thrown from here
										}
                                }
								else if("Target End Date".equals(name)) {
                                    dataType = "date";
                                    name = "End Date";
                                    if(null!=value && !"null".equals(value) && !"".equals(value))
                                    	try{
                                    value = eMatrixDateFormat.getFormattedInputDate(context, value, iClientTimeOffset, (Locale)i18nNow.getLocale(context.getSession().getLanguage()));
                                    	}catch (Exception e) {
											// Exception should not be thrown from here
										}
                                }

                                if(!checkList.contains(name)) {
                                    map = new HashMap();
                                    map.put("name",name);
                                    map.put("datatype",dataType);
                                    map.put("value",value);
                                    checkList.add(name);
                                } else {
                                    map = (Map)children.get(name);
                                    map.put("name",name);
                                    map.put("datatype",dataType);
                                    map.put("value",value);
                                    }
                                children.put(name,map);
                            }
						}


                            java.util.Collection collection = children.values();
                            Iterator itr = collection.iterator();
                            while(itr.hasNext()) {
                                Map childMap = (Map)itr.next();
                                String tagName = (String)childMap.get("name");
                                String tagType = (String)childMap.get("datatype");
                                String tagValue = (String)childMap.get("value");

                                attributeElement = new com.matrixone.jdom.Element("attribute");
                                attributeElement.setAttribute("chgtype","C");
                                attributeListElement.addContent(attributeElement);

                                nameElement = new com.matrixone.jdom.Element("name");
                                nameElement.setText(tagName);
                                attributeElement.addContent(nameElement);

                                StringElement = new com.matrixone.jdom.Element(tagType);
                                StringElement.setText(getAsStoredAttributeValue(context, sRelId, strSelPlantId, tagName, strRelToRel));
                                attributeElement.addContent(StringElement);

                                Element oldValue=new Element("oldvalue");
                                oldValue.setText(getAsStoredAttributeValue(context, sRelId, strSelPlantId, tagName, strRelToRel));
                                attributeElement.addContent(oldValue);
                                Element newValue=new Element("newvalue");
                                newValue.setText(tagValue);
                                attributeElement.addContent(newValue);
                            }
                        }
					else {
						/* Execute MQL Command */
                            relationshipElement = new com.matrixone.jdom.Element("relationship");
                            relationshipElement.setAttribute("chgtype","D");
                            fromRelationshipListElement.addContent(relationshipElement);

                            relationshipDefRefElement = new com.matrixone.jdom.Element("relationshipDefRef");
                            relationshipDefRefElement.setAttribute("relId", sRelId);
                            relationshipDefRefElement.setText(relName);
                            relationshipElement.addContent(relationshipDefRefElement);
                            relatedObjectElement = new com.matrixone.jdom.Element("fromRel");

                            String strRelToRel = PropertyUtil.getSchemaProperty(context, "relationship_SubstituteManufacturingResponsibility");
                            String ebomId = "";
                            String primaryId = "";

						if ((relName.trim()).equals(PropertyUtil.getSchemaProperty(context, "relationship_Alternate"))) {
                                //R209 MECO changes - Starts
                                ebomId = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",sRelId,"from.to[EBOM|from.id=="+strSelPartId+"].id");
                                //R209 MECO changes - Ends
                                strRelToRel = PropertyUtil.getSchemaProperty(context, "relationship_AlternateManufacturingResponsibility");
                            }
						else if((relName.trim()).equals(PropertyUtil.getSchemaProperty(context, "relationship_EBOMSubstitute"))) {
								ebomId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",sRelId,"fromrel.id","|");
								primaryId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3",ebomId,"to.id","|");
                            }

                            relatedObjectElement.setAttribute("relId",ebomId);
                            relatedObjectElement.setAttribute("objectId",primaryId);
                            relationshipElement.addContent(relatedObjectElement);

                            businessObjectRefElement = new com.matrixone.jdom.Element("businessObjectRef");
                            relatedObjectElement.addContent(businessObjectRefElement);

                            childObjectTypeElement = new com.matrixone.jdom.Element("objectType");
                            childObjectTypeElement.setAttribute("chgtype","D");
                            //Start : IR-054311V6R2011x
                            childObjectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_TYPE)));
                            //End : IR-054311V6R2011x
                            businessObjectRefElement.addContent(childObjectTypeElement);

                            childObjectNameElement = new com.matrixone.jdom.Element("objectName");
                            childObjectNameElement.setAttribute("chgtype","D");
                            //Start : IR-054311V6R2011x
                            childObjectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_NAME)));
                            //End : IR-054311V6R2011x
                            businessObjectRefElement.addContent(childObjectNameElement);

                            childObjectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
                            childObjectRevisionElement.setAttribute("chgtype","D");
                            //Start : IR-054311V6R2011x
                            childObjectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_REVISION)));
                            //End : IR-054311V6R2011x
                            businessObjectRefElement.addContent(childObjectRevisionElement);

                            childVaultRefElement = new com.matrixone.jdom.Element("vaultRef");
                            //Start : IR-054311V6R2011x
                            childVaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(childObject.getInfo(context,DomainConstants.SELECT_VAULT)));
                            //End : IR-054311V6R2011x
                            businessObjectRefElement.addContent(childVaultRefElement);

                            /* <attributeList> */
                            attributeListElement = new com.matrixone.jdom.Element("attributeList");
                            attributeListElement.setAttribute("plantId", strSelPlantId);
                            relationshipElement.addContent(attributeListElement);

                            // Attribute Stype
                            attributeElement = new com.matrixone.jdom.Element("attribute");
                            attributeElement.setAttribute("chgtype","D");
                            attributeListElement.addContent(attributeElement);
                            nameElement = new com.matrixone.jdom.Element("name");
                            nameElement.setText("Stype");
                            attributeElement.addContent(nameElement);
                            StringElement = new com.matrixone.jdom.Element("string");
                            StringElement.setText(getAsStoredAttributeValue(context, sRelId, strSelPlantId, "Stype", strRelToRel));
                            attributeElement.addContent(StringElement);

                            // Attribute Auth Status
                            attributeElement = new com.matrixone.jdom.Element("attribute");
                            attributeElement.setAttribute("chgtype","D");
                            attributeListElement.addContent(attributeElement);
                            nameElement = new com.matrixone.jdom.Element("name");
                            nameElement.setText("Auth Status");
                            attributeElement.addContent(nameElement);
                            StringElement = new com.matrixone.jdom.Element("string");
                            StringElement.setText(getAsStoredAttributeValue(context, sRelId, strSelPlantId, "Auth Status", strRelToRel));
                            attributeElement.addContent(StringElement);

                            // Attribute Pref Rank
                            attributeElement = new com.matrixone.jdom.Element("attribute");
                            attributeElement.setAttribute("chgtype","D");
                            attributeListElement.addContent(attributeElement);
                            nameElement = new com.matrixone.jdom.Element("name");
                            nameElement.setText("Pref Rank");
                            attributeElement.addContent(nameElement);
                            StringElement = new com.matrixone.jdom.Element("integer");
                            StringElement.setText(getAsStoredAttributeValue(context, sRelId, strSelPlantId, "Pref Rank", strRelToRel));
                            attributeElement.addContent(StringElement);

                            // Attribute Switch
                            attributeElement = new com.matrixone.jdom.Element("attribute");
                            attributeElement.setAttribute("chgtype","D");
                            attributeListElement.addContent(attributeElement);
                            nameElement = new com.matrixone.jdom.Element("name");
                            nameElement.setText("Switch");
                            attributeElement.addContent(nameElement);
                            StringElement = new com.matrixone.jdom.Element("string");
                            StringElement.setText(getAsStoredAttributeValue(context, sRelId, strSelPlantId, "Switch", strRelToRel));
                            attributeElement.addContent(StringElement);

                            // Attribute Target Start Date
                            attributeElement = new com.matrixone.jdom.Element("attribute");
                            attributeElement.setAttribute("chgtype","D");
                            attributeListElement.addContent(attributeElement);
                            nameElement = new com.matrixone.jdom.Element("name");
                            nameElement.setText("Start Date");
                            attributeElement.addContent(nameElement);
                            StringElement = new com.matrixone.jdom.Element("date");
                            StringElement.setText(getAsStoredAttributeValue(context, sRelId, strSelPlantId, "Start Date", strRelToRel));
                            attributeElement.addContent(StringElement);

                            // Attribute Target End Date
                            attributeElement = new com.matrixone.jdom.Element("attribute");
                            attributeElement.setAttribute("chgtype","D");
                            attributeListElement.addContent(attributeElement);
                            nameElement = new com.matrixone.jdom.Element("name");
                            nameElement.setText("End Date");
                            attributeElement.addContent(nameElement);
                            StringElement = new com.matrixone.jdom.Element("date");
                            StringElement.setText(getAsStoredAttributeValue(context, sRelId, strSelPlantId, "End Date", strRelToRel));
                            attributeElement.addContent(StringElement);
                        }
                    }
                //check if there is an additional arg to just to set the Element
				if(args != null && (args.length == 5)) {
					eleDocRoot = ematrixRootElement;
				} else {
					/* Checkin The Output XML */
            	    chekinToMarkupObject(context,ematrixRootElement, objectPlantBOMMarkup);
				}
                objectPlantBOMMarkup.close(context);
            }
            catch(Exception e) {
                //Added for V6R2009.HF0.2 - Starts
                throw e;
                //Added for V6R2009.HF0.2 - Ends
            }
        }
    }

    private String getAsStoredAttributeValue(Context context,
                                            String sRelId,
                                            String sPlantId,
                                            String sAttrName,
                                            String sRelName) throws Exception {
        String strAttrValue = "";
        try {
            String relIds[] = new String[1];
            relIds[0] = sRelId;
            StringList slRelSelect = new StringList(1);
            slRelSelect.add("tomid["+sRelName+"|fromrel[Manufacturing Responsibility].from.id=='"+sPlantId+"'].attribute["+sAttrName+"]");
            MapList mlSubList = DomainRelationship.getInfo(context, relIds, slRelSelect);

            if (mlSubList.size() > 0)
            {
                Map newMap = (Map) mlSubList.get(0);
                strAttrValue = (String) newMap.get("tomid["+sRelName+"].attribute["+sAttrName+"]");
            }
        }
        catch (Exception exObj)
        {
            throw exObj;
        }

        return strAttrValue;
    }


    /**
    * This method reads the merged xml file and applies it to the database
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the input arguments:
    * @returns nothing
    * @throws Exception if the operation fails
    * @since X3
    */

    public void applyPlantBOMMarkup(Context context,String sMarkupObjId,String sPartId, String sChangeId) throws Exception
    {
        try
        {
            String sXMLFormat = PropertyUtil.getSchemaProperty(context, "format_XML");
            
            //377755 start
            //Multitenant
            String sConflictErr=EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MBOM.ErrorMessage");
            //377755 end
            
            // create markup object.
            BusinessObject busObjMarkup = new BusinessObject(sMarkupObjId);
            //Start : IR-046932V6R2011
            String strPartType = PropertyUtil.getSchemaProperty(context,"type_Part");

            try
            {
                busObjMarkup.open(context);
            }
            catch(MatrixException mex)
            {
                throw mex;
            }

            String sbusObjMarkupName = busObjMarkup.getName();
            String sTransPath = context.createWorkspace();
            java.io.File fEmatrixWebRoot = new java.io.File(sTransPath);

            // check out file from markup
            try
            {
                busObjMarkup.checkoutFile(context, false, sXMLFormat, sbusObjMarkupName+ ".xml", fEmatrixWebRoot.toString());
            }
            catch(MatrixException mex)
            {
                throw mex;
            }

            java.io.File srcXMLFile = new java.io.File(fEmatrixWebRoot, sbusObjMarkupName+ ".xml");
            com.matrixone.jdom.Document docXML = null;
            com.matrixone.jdom.Element rootElement = null;

            // send file to SAX builder.
            try
            {
                com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder();
				builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
				builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                docXML = builder.build(srcXMLFile);
                rootElement = docXML.getRootElement();
            }
            catch(Exception exObj)
            {
                throw exObj;
            }

            com.matrixone.jdom.Element fromRelList = rootElement.getChild("businessObject").getChild("fromRelationshipList");
            java.util.List fromRels = fromRelList.getChildren();
            java.util.Iterator checkItr = fromRels.iterator();

            //R209 MECO changes - Starts
            String sTopId = rootElement.getChild("businessObject").getAttributeValue("id");
            boolean applyNewRev = false;
            String ebomObjid = "";
            String RELATIONSHIP_SUB =  PropertyUtil.getSchemaProperty(context, "relationship_EBOMSubstitute");
            if(!sTopId.equals(sPartId)) {
                applyNewRev = true;
            }
            //R209 MECO changes - Ends
            
            while(checkItr.hasNext())
            {
                com.matrixone.jdom.Element relationshipList = (com.matrixone.jdom.Element) checkItr.next();
                String sChangeType = relationshipList.getAttributeValue("chgtype");
                String sRelName = relationshipList.getChild("relationshipDefRef").getText();
                String sPlantObjId = relationshipList.getChild("attributeList").getAttributeValue("plantId");
                String sRelId = relationshipList.getChild("relationshipDefRef").getAttributeValue("relId");
                java.util.List attList = relationshipList.getChild("attributeList").getChildren("attribute");

                //if the operation is "cut"/"delete"
                if ("D".equals(sChangeType))
                {
                    //R209 MECO changes - Starts
                    if(applyNewRev) {
                        if((RELATIONSHIP_SUB).equals(sRelName.trim())) {
                            ebomObjid = relationshipList.getChild("fromRel").getAttributeValue("objectId");
                            sRelId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",sPartId,"from["+DomainConstants.RELATIONSHIP_EBOM+"|to.id=="+ebomObjid+"].frommid.id");
                        }
                    }
                    //R209 MECO changes - Ends
                    try
                    {
                        DomainRelationship.disconnect(context,sRelId,true);
                    }
                    catch(Exception exObj)
                    {
                        throw exObj;
                    }

                }
                else if ("A".equals(sChangeType)) //if the operation is ADD
                {
                    if ((DomainConstants.RELATIONSHIP_ALTERNATE).equals(sRelName.trim()))
                    {
                        String sSelectedId = relationshipList.getChild("fromRel").getAttributeValue("objectId");

                        String sAlternatePartType = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(relationshipList.getChild("fromRel").getChild("businessObjectRef").getChild("objectType").getText());
                        if(null != sAlternatePartType && !sAlternatePartType.equals("null") && !sAlternatePartType.equals("")) {
                        	strPartType = sAlternatePartType;
                        }
                        
                        String sAlternatePartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(relationshipList.getChild("fromRel").getChild("businessObjectRef").getChild("objectName").getText());
                        String sAlternatePartRev = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(relationshipList.getChild("fromRel").getChild("businessObjectRef").getChild("objectRevision").getText());
                        String strPersonVault = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(relationshipList.getChild("fromRel").getChild("businessObjectRef").getChild("vaultRef").getText());

                        //Modified for IR-046932V6R2011
                        BusinessObject boObj = new BusinessObject(strPartType,sAlternatePartName,sAlternatePartRev,strPersonVault);
                        String sAlternateObjId = boObj.getObjectId(context);

                        //377755 start
                        DomainObject sAltObjIdObj = new DomainObject(sAlternateObjId);
                        String altState = sAltObjIdObj.getCurrentState(context).getName();
                        String altType  = sAltObjIdObj.getTypeName();

                        if(!altState.equalsIgnoreCase(STATE_PART_RELEASE))
                        {
                            String strMessage = sConflictErr+"\n"+"Object \'"+altType+"\' \'"+sAlternatePartName+"\' \'"+sAlternatePartRev+"\' "+"is in state "+altState+".";
                            emxContextUtil_mxJPO.mqlNotice(context,strMessage);
                            throw new Exception();

                        }
                        //377755 end
                        try
                        {
							// IR-387427 Starts: Add Alt-Part relns to current & newer Part Revns
                        	
                        	// Get all available Part revns of the current Part Revn
							DomainObject curPartRevn = DomainObject.newInstance(context);
							curPartRevn.setId(sSelectedId);
							StringList objectList= new StringList();	  // We need only TNR n ID; they r got by default
						    StringList multivalueList= new StringList();  // We dont need any multi-valued attributes
						    MapList partRevisionsMap = curPartRevn.getRevisionsInfo(context,objectList,multivalueList);

						    // Find the index of current revn in partRevisionsMap
						    String sCurPartRev = (String) curPartRevn.getInfo(context, DomainConstants.SELECT_REVISION);
						    int indexPos;
						    for (indexPos = 0; indexPos < partRevisionsMap.size(); indexPos++)
						    {
						    	Map revMap = (Map) partRevisionsMap.get(indexPos);
						    	String partRev = (String) revMap.get(DomainConstants.SELECT_REVISION);
						    	if(partRev.equals(sCurPartRev))
						    		break;
						    }
						    
						    // Now add Alt-Part relns to current Revn & all Revns higher than the current Revn
						    for (; indexPos < partRevisionsMap.size(); indexPos++)
						    {
						    	Map revMap = (Map) partRevisionsMap.get(indexPos);
						    	String partId = (String) revMap.get(DomainConstants.SELECT_ID);

						    	// Check if the Alt-part is already connected to the part
						    	String sRelid = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", partId, 
						    					"from["+EngineeringConstants.RELATIONSHIP_ALTERNATE+"|to.id=="+sAlternateObjId+"].id","|");
						    	
						    	if("".equals(sRelid)) 
						    	{
						    		// If not create a new relnship to add the Alt-Part & update relnship attributes
							    	RelToRelUtil relObj = new RelToRelUtil();
									sRelid = relObj.connect(context,sRelName.trim(),partId,sAlternateObjId,true,true);
									setRelToRelAttributeValues(context,sRelid,sPlantObjId,sRelName.trim(),attList);
						    	}
						    	else if (sSelectedId.equals(partId))
								{
						    		// If yes, we shld update relnship only for the current Revn & not for newer Revns. 
						    		// Coz user has manually added the Alt-Part to that Revn too; we shall retain it...
									setRelToRelAttributeValues(context,sRelid,sPlantObjId,sRelName.trim(),attList);
								}
						    }
							// IR-387427 Ends
                        }
                        catch(Exception exObj)
                        {
                            throw exObj;
                        }
                    }
                    //R209 MECO changes - Starts
                    else if (RELATIONSHIP_SUB.equals(sRelName.trim()))
                    //R209 MECO changes - Ends
                    {
                        String sEBOMRelId = relationshipList.getChild("fromRel").getAttributeValue("relId");
                        String sEBOMId = relationshipList.getChild("fromRel").getAttributeValue("objectId");

                        String sSubPartType = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(relationshipList.getChild("fromRel").getChild("businessObjectRef").getChild("objectType").getText());
                        if(null != sSubPartType && !sSubPartType.equals("null") && !sSubPartType.equals("")) {
                        	strPartType = sSubPartType;
                        }
                        String sSubPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(relationshipList.getChild("fromRel").getChild("businessObjectRef").getChild("objectName").getText());
                        String sSubPartRev = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(relationshipList.getChild("fromRel").getChild("businessObjectRef").getChild("objectRevision").getText());
                        String strPersonVault =com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(relationshipList.getChild("fromRel").getChild("businessObjectRef").getChild("vaultRef").getText());

                        //Modified for IR-046932V6R2011
                        BusinessObject boObj = new BusinessObject(strPartType,sSubPartName,sSubPartRev,strPersonVault);
                        String sSubObjId = boObj.getObjectId(context);
                        //377755 start
                        DomainObject sSubObjIdObj=new DomainObject(sSubObjId);
                        String subState=  sSubObjIdObj.getCurrentState(context).getName();
                        String subType=  sSubObjIdObj.getType(context);

                        if(!subState.equalsIgnoreCase(STATE_PART_RELEASE))
                        {

                            String strMessage = sConflictErr+"\n"+"Object \'"+subType+"\' \'"+sSubPartName+"\' \'"+sSubPartRev+"\' "+"is in state "+subState+".";
                            emxContextUtil_mxJPO.mqlNotice(context,strMessage);
                            throw new Exception();

                        }
                        //377755 end

                        try
                        {
                            //R209 MECO changes - Starts
                            if(applyNewRev) {
                                sEBOMRelId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", sPartId, "from["+DomainConstants.RELATIONSHIP_EBOM+"|to.id=="+sEBOMId.trim()+"].id");
                            }
                            //R209 MECO changes - Ends

                            com.matrixone.apps.engineering.Part part = new com.matrixone.apps.engineering.Part(sPartId);
                            part.createSubstitutePart(context,sEBOMId,sEBOMRelId,sSubObjId);
                            String sRelid = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump",sSubObjId,"relationship["+sRelName+"|fromrel.id == "+sEBOMRelId+"].id");
                            if (sRelid != null && !"".equals(sRelid))
                            {
                                setRelToRelAttributeValues(context,sRelid,sPlantObjId,sRelName.trim(),attList);
                            }
                        }
                        catch(Exception exObj)
                        {
                            throw exObj;
                        }

                    }
                }
                else if ("C".equals(sChangeType))
                {
                    try
                    {
                        //R209 MECO changes - Starts
                        if(applyNewRev) {
                            if(RELATIONSHIP_SUB.equals(sRelName.trim())) {
                                ebomObjid = relationshipList.getChild("fromRel").getAttributeValue("objectId");
                                sRelId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",sPartId,"from[EBOM|to.id=="+ebomObjid+"].frommid.id");
                            } else if(DomainConstants.RELATIONSHIP_EBOM.equals(sRelName.trim())) {
                                ebomObjid =  MqlUtil.mqlCommand(context, "print connection $1 select $2 dump",sRelId,"to.id");
                                sRelId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",sPartId,"from[EBOM|to.id=="+ebomObjid+"].id");
                            }
                        }
                        //R209 MECO changes - Ends
                        if (sRelId != null && !"".equals(sRelId))
                        {
                            setRelToRelAttributeValues(context,sRelId,sPlantObjId,sRelName.trim(),attList);
                        }

                    }
                    catch(Exception exObj)
                    {
                        throw exObj;
                    }
                }
            }
            
            String strAppliedPartMarkupRel = PropertyUtil.getSchemaProperty(context,"relationship_AppliedPartMarkup");
			DomainObject doPart = new DomainObject(sPartId);
			DomainRelationship.connect(context, doPart, strAppliedPartMarkupRel, new DomainObject(busObjMarkup));
            context.deleteWorkspace();
        }
        catch (Exception exObj)
        {
            throw exObj;
        }
    }

    /**
    * This method sets the attributes on RelToRel associated with the particular plantId that is passed
    * @param context the eMatrix <code>Context</code> object
    * @param Context - the current context
    * @param sRelId - the id of Alternate/EBOM Substitute relationship
    * @param sPlantObjId - the plant currently selected in the plant filter
    * @param sRelName - the relationship name
    * @param attList - the attribute values to be updated
    * @returns nothing
    * @throws Exception if the operation fails
    * @since X3
    */
    private void setRelToRelAttributeValues(Context context,String sRelId,String sPlantObjId,String sRelName,java.util.List attList) throws Exception
    {
        String sMRRelId = "";
        String sAttrName = "";
        String sAttrValue = "";
        Map attrMap = new HashMap();
        try
        {
            if ((DomainConstants.RELATIONSHIP_ALTERNATE).equals(sRelName.trim()))
            {
                sRelName = PropertyUtil.getSchemaProperty(context, "relationship_AlternateManufacturingResponsibility");
            }
            else if ((PropertyUtil.getSchemaProperty(context, "relationship_EBOMSubstitute")).equals(sRelName.trim()))
            {
                sRelName = PropertyUtil.getSchemaProperty(context, "relationship_SubstituteManufacturingResponsibility");
            }
            else if ((DomainConstants.RELATIONSHIP_EBOM).equals(sRelName.trim())){
                sRelName = PropertyUtil.getSchemaProperty(context, "relationship_EBOMManufacturingResponsibility");
            }

            String relIds[] = new String[1];
            relIds[0] = sRelId;
            StringList slRelSelect = new StringList(1);
            slRelSelect.add("tomid["+sRelName.trim()+"|fromrel[Manufacturing Responsibility].from.id=='"+sPlantObjId+"'].id");
            MapList mlSubList = DomainRelationship.getInfo(context, relIds, slRelSelect);

            if (mlSubList.size() > 0)
            {
                Map newMap = (Map) mlSubList.get(0);
                sMRRelId = (String) newMap.get("tomid["+sRelName.trim()+"].id");
            }
            if (sMRRelId != null && !"".equals(sMRRelId))
            {
                java.util.Iterator attItr = attList.iterator();
                while(attItr.hasNext())
                {
                    com.matrixone.jdom.Element attElement = (com.matrixone.jdom.Element) attItr.next();
                    String sChangeType = attElement.getAttributeValue("chgtype");
                    sAttrName  = attElement.getChildText("name");
                    if ("C".equals(sChangeType))
                    {
                        sAttrValue = attElement.getChildText("newvalue");
                    }
                    else if("A".equals(sChangeType) && ("Start Date".equals(sAttrName) || "End Date".equals(sAttrName)))
                    {
                        sAttrValue = attElement.getChildText("date");
                    }
                    else if("A".equals(sChangeType) && "Pref Rank".equals(sAttrName))
                    {
                        sAttrValue = attElement.getChildText("integer");
                    }
                    else if("A".equals(sChangeType))
                    {
                        sAttrValue = attElement.getChildText("string");
                    }
		    if(sAttrValue != null) sAttrValue = sAttrValue.trim();
                    attrMap.put(sAttrName,sAttrValue);
                }
                ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
                DomainRelationship.setAttributeValues(context,sMRRelId,attrMap);
                ContextUtil.popContext(context);
            }
        }
        catch (Exception exObj)
        {
            throw exObj;
        }
    }

   /**
     * Method to merge Plant BOM Markups
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args   holds a StingList of object Ids
     * @return      merge status - true/false
     * @throws      Exception if the operation fails
     * @since       EngineeringCentral X3
     **/
    public String mergePlantBOMMarkups(Context context, String[] args )
        throws Exception
    {

    	//Multitenant
    	String sConflictErr=EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.MarkupActions.ConflictError");
        //Hashtables containing relationship child nodes for each type of action
        Hashtable htChangedBOMs = new Hashtable();
        Hashtable htAddedBOMs = new Hashtable();
        Hashtable htDeletedBOMs = new Hashtable();
        Hashtable htUnChangedBOMs = new Hashtable();
        Hashtable htConflictBOMs = new Hashtable();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        //List of Markups
        StringList strlMarkupIds = (StringList)paramMap.get("programMap");
        //Dont merge if there is only one Markup
        if(strlMarkupIds != null && strlMarkupIds.size() == 1) {
			return "true";
		}
        StringList chhId = (StringList)paramMap.get("chhId");
        //ECO/ECR id
        String strChangeId = (String)chhId.get(0);

        XMLOutputter  outputter = new XMLOutputter();
        String XMLFORMAT = PropertyUtil.getSchemaProperty(context, "format_XML");
        // create a temporary workspace directory
        String strTransPath = context.createWorkspace();

        java.io.File fEmatrixWebRoot = new java.io.File(strTransPath);

        Iterator itrMarkups = strlMarkupIds.iterator();

        DomainObject boMarkup = null;

        String strMarkupId = null;
        String strMarkupName = null;
        String strXMLFileName = null;
        String strRelationshipDefRef = null;
        String strEBOMRelId = null;
        String strSelectedPartId = null;

        //Use default SAX driver
        com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder(false);
		builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
		builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder.setValidation(false);

        com.matrixone.jdom.Document docXML = null;

        Element eleRoot = null;
        Element eleRelatioship = null;
        Element eleObjectInfo = null;
        Element eleRelatioshipInfo = null;
        Element eleBusinessObject = null;

        Element eleMergedMarkup = new Element("ematrix");;
        Element eleMergedRelationships = null;

        List listFromRelationships = null;

        String strPlantId = null;
        Iterator itrRelationships = null;

        com.matrixone.jdom.Attribute attrEBOMChgType = null;

        String sRes=null;
        String strPartType =  null;
        String strPartName =  null;
        String strPartRev  =  null;
        String strEBOMChgType  =  null;
        String strKey  =  null;
        String strMarkupType=null;
        String strPartId=null;
        String strPartPolicy=null;
        String strConnectionId = null;
        boolean bolIsConflict = false;
        String strAttrPlantIdValue = null;
        List lstConflictBOMs = null;

        while (itrMarkups.hasNext())
        {
            strMarkupId = (String) itrMarkups.next();
            boMarkup = new DomainObject(strMarkupId);
            strMarkupName =boMarkup.getInfo(context,DomainConstants.SELECT_NAME);
            strMarkupType=boMarkup.getInfo(context,DomainConstants.SELECT_TYPE);
            strPartId=boMarkup.getInfo(context,"to["+DomainConstants.RELATIONSHIP_EBOM_MARKUP+"].from.id");
			strAttrPlantIdValue =  boMarkup.getInfo(context,SELECT_PLANT_ID);
            DomainObject strPart=new DomainObject();
            strPart.setId(strPartId);

            strPartPolicy=strPart.getInfo(context,DomainConstants.SELECT_POLICY);
            strXMLFileName = strMarkupName + ".xml";

            boMarkup.checkoutFile(context, false, XMLFORMAT, strXMLFileName, strTransPath);
            java.io.File fMarkupXML = new java.io.File(fEmatrixWebRoot, strXMLFileName);

            //could not checkout file.
            if (fMarkupXML == null)
            {
                continue;
            }

            docXML = builder.build(fMarkupXML);
            eleRoot = docXML.getRootElement();

            eleBusinessObject = eleRoot.getChild("businessObject");

             listFromRelationships = eleRoot.getChild("businessObject").getChild("fromRelationshipList").getChildren();
             itrRelationships = listFromRelationships.iterator();

            while (itrRelationships.hasNext())
            {
                eleRelatioship = (Element) itrRelationships.next();
                eleObjectInfo = eleRelatioship.getChild("fromRel").getChild("businessObjectRef");
                strRelationshipDefRef = eleRelatioship.getChild("relationshipDefRef").getText();
                strConnectionId = eleRelatioship.getChild("relationshipDefRef").getAttributeValue("relId");
                //EBOM Rel Id
                strEBOMRelId = eleRelatioship.getChild("fromRel").getAttributeValue("relId");
                //Object of the part selected to add/delete/modify
                strSelectedPartId = eleRelatioship.getChild("fromRel").getAttributeValue("objectId");

                eleRelatioshipInfo = eleRelatioship.getChild("attributeList");
                //Plant id
                strPlantId = eleRelatioshipInfo.getAttributeValue("plantId");

                //T N R of part to which to connect as Alt or Sub
				//IR-045004: Decoding the encoded type, name,rev
				strPartType = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectType").getText());
				strPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectName").getText());
				strPartRev = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectRevision").getText());

                attrEBOMChgType = eleRelatioship.getAttribute("chgtype");

                strEBOMChgType = null;

                if (attrEBOMChgType != null )
                {
                    strEBOMChgType = attrEBOMChgType.getValue();
                }

                //Use the relationship type, TNR of part to connect to,ebom connection id, selected part's object id and plant id as key
                strKey = strRelationshipDefRef.trim() + "~" + strPartType + "~"
                                + strPartName + "~" + strPartRev+ "~" + strConnectionId+ "~" +strEBOMRelId
                                + "~" + strSelectedPartId + "~" + strPlantId;
                if (strEBOMChgType == null)
                {
                    if (!htDeletedBOMs.containsKey(strKey) && !htChangedBOMs.containsKey(strKey) && !htUnChangedBOMs.containsKey(strKey))
                    {
                        htUnChangedBOMs.put(strKey, eleRelatioship);
                    }
                }
                else if ("D".equals(strEBOMChgType))
                {
                    //Conflict:Part id marked for change to the same plant
                    //Note: though deletion is irrespective of the plant, this check is required for conflict
                    if (htChangedBOMs.containsKey(strKey))
                    {
                        bolIsConflict = true;
                    }
                    else
                    {
                        htDeletedBOMs.put(strKey, eleRelatioship);
                        if (htUnChangedBOMs.containsKey(strKey))
                        {
                            htUnChangedBOMs.remove(strKey);
                        }
                    }
                }
                else if ("C".equals(strEBOMChgType))
                {
                    //Conflict:Part id marked for deletion or change, to the same plant
                    if (htChangedBOMs.containsKey(strKey) || htConflictBOMs.containsKey(strKey))
                    {
                        lstConflictBOMs = (List)htConflictBOMs.get(strKey);
						if(lstConflictBOMs == null) {
							lstConflictBOMs = new ArrayList(1);
							lstConflictBOMs.add(eleRelatioship);
							if(htChangedBOMs.containsKey(strKey)) {
								lstConflictBOMs.add(htChangedBOMs.get(strKey));
								htChangedBOMs.remove(strKey);
							}
							htConflictBOMs.put(strKey,lstConflictBOMs);
						} else {
							lstConflictBOMs.add(eleRelatioship);
							if(htChangedBOMs.containsKey(strKey)) {
								lstConflictBOMs.add(htChangedBOMs.get(strKey));
								htChangedBOMs.remove(strKey);
							}
							htConflictBOMs.put(strKey,lstConflictBOMs);
						}
                    }
                    else if(htDeletedBOMs.containsKey(strKey)) {
                        bolIsConflict = true;
                    }
                    else
                    {
                       htChangedBOMs.put(strKey, eleRelatioship);
                       if (htUnChangedBOMs.containsKey(strKey))
                       {
                           htUnChangedBOMs.remove(strKey);
                       }
                    }
                }
                else if ("A".equals(strEBOMChgType))
                {
                    //Conflict:Part id marked for deletion or change or addition(with diff attr values) to the same plant
                    if (htDeletedBOMs.containsKey(strKey)
                         || htChangedBOMs.containsKey(strKey))
                    {
                        bolIsConflict = true;
                    }
                    else
                    {
                        htAddedBOMs.put(strKey, eleRelatioship);
                    }
                }
            }
        } //Finished processing all markups

		if(!htConflictBOMs.isEmpty()) {
			Hashtable htResult = getAttributeLevelMerge(context,htConflictBOMs);
			Boolean objBlnResult  = (Boolean)htResult.get("result");
			if(objBlnResult != null) {
				bolIsConflict = true;
			} else {
				htChangedBOMs.putAll(htResult);
			}
		}

        if (bolIsConflict)
        {
            sRes = "false";
            emxContextUtil_mxJPO.mqlNotice(context,sConflictErr);
            return sRes;
        }
        else
        {
            //Start merging
            eleMergedRelationships = new Element("fromRelationshipList");

            //remove the existing child
            eleBusinessObject.removeChild("fromRelationshipList");

            Element eleTemp = null;

            Enumeration enumKeys = htDeletedBOMs.keys();

            //Add delete Markups first
            while (enumKeys.hasMoreElements())
            {
                String strKey1 = (String) enumKeys.nextElement();
                eleTemp = (Element) htDeletedBOMs.get(strKey1);
                eleMergedRelationships.addContent(eleTemp.detach());
            }

            enumKeys = htChangedBOMs.keys();
            //Add change markups
            while (enumKeys.hasMoreElements())
            {
                String strKey1 = (String) enumKeys.nextElement();
                eleTemp = (Element) htChangedBOMs.get(strKey1);
                eleMergedRelationships.addContent(eleTemp.detach());
            }

            enumKeys = htAddedBOMs.keys();
            //Add new Markups
            while (enumKeys.hasMoreElements())
            {
                String strKey1 = (String) enumKeys.nextElement();
                eleTemp = (Element) htAddedBOMs.get(strKey1);
                eleMergedRelationships.addContent(eleTemp.detach());
            }

            enumKeys = htUnChangedBOMs.keys();
            //Add unchanges ones
            while (enumKeys.hasMoreElements())
            {
                String strKey1 = (String) enumKeys.nextElement();
                eleTemp = (Element) htUnChangedBOMs.get(strKey1);
                eleMergedRelationships.addContent(eleTemp.detach());
            }

            eleBusinessObject.addContent(eleMergedRelationships);
            eleMergedMarkup.addContent(eleBusinessObject.detach());

            String strMergedMarkupId = "";
            String typePlantBOMMarkup = PropertyUtil.getSchemaProperty(context,"type_PlantBOMMarkup");
            if (typePlantBOMMarkup.equalsIgnoreCase(strMarkupType))
            {
                if(DomainConstants.POLICY_EC_PART.equalsIgnoreCase(strPartPolicy))
                {
                    strMergedMarkupId = FrameworkUtil.autoName(context,"type_PlantBOMMarkup","policy_PartMarkup");
                }
                else
                {
                    strMergedMarkupId = FrameworkUtil.autoName(context,"type_PlantBOMMarkup","policy_DevelopmentPartMarkup");
                }
            }

            DomainObject doMergedMarkup = new DomainObject(strMergedMarkupId);
			doMergedMarkup.setAttributeValue(context,ATTRIBUTE_PLANT_ID,strAttrPlantIdValue);
            String sMarkupMaergedName=doMergedMarkup.getInfo(context,DomainConstants.SELECT_NAME);
            java.io.File fMergedXML = new java.io.File (fEmatrixWebRoot, sMarkupMaergedName + "." + "xml");
            FileOutputStream fosMergedXML = new FileOutputStream(fMergedXML);
            //create XML file
            outputter.output(eleMergedMarkup, fosMergedXML);

            matrix.db.File fCheckinMergedXML = new matrix.db.File(fMergedXML.getAbsolutePath(), XMLFORMAT);
            FileList fileListCheckin = new FileList();
            fileListCheckin.addElement(fCheckinMergedXML);

            doMergedMarkup.checkinFromServer(context, false, false, XMLFORMAT, "", fileListCheckin);
            context.setCustomData("fromMarkupActions", "TRUE");
			doMergedMarkup.setState(context,approvedState);
			context.removeFromCustomData("fromMarkupActions");

            //Connect the new markup to ECR/ECO
             if(strChangeId !=null) {
               DomainObject doChange = new DomainObject(strChangeId);
                String strRelToConnectToChange =null;

				if (doChange.isKindOf(context, DomainConstants.TYPE_ECO) || doChange.isKindOf(context, TYPE_MECO))
                {

                    strRelToConnectToChange =RELATIONSHIP_APPLIED_MARKUP;
                    //"Applied Markup";
                }
                else if (doChange.isKindOf(context, DomainConstants.TYPE_ECR) || doChange.isKindOf(context, TYPE_DCR))
                {
                    strRelToConnectToChange = RELATIONSHIP_PROPOSED_MARKUP;
                }

				DomainRelationship.connect(context, doChange, strRelToConnectToChange, doMergedMarkup);
            }

            //Connect the new markup to Assembly
            if (strPartId != null)
            {
                DomainObject doPart = new DomainObject(strPartId);
				DomainRelationship.connect(context, doPart, DomainConstants.RELATIONSHIP_EBOM_MARKUP, doMergedMarkup);
            }

            Iterator itrDeleteMarkups = strlMarkupIds.iterator();
            while (itrDeleteMarkups.hasNext())
            {
                String strDeletedMarkupId = (String) itrDeleteMarkups.next();
                MqlUtil.mqlCommand(context, "delete bus $1",strDeletedMarkupId);
            }
        }
        sRes="true";
        return sRes;
      }

    /**
    *
    * getLocation method helps in getting Location object id for a plant.
    * @param context Context The current user's context
    * @param sPlantId String Plant id value
    * returns Location Id
    * throws Exception
    */
    public String getLocation(Context context,String sPlantId)throws Exception
    {
        /**
         * list of locations.
         */
        MapList mlLocationList =null;
        /**
         * location details
         */
        Map mLocationMap=null;
        /**
         * variable holds location object id
         */
        String sLocationId =null;
        try{
                //selectables for query
                StringList busSelList = new StringList();
                busSelList.add(DomainObject.SELECT_ID);

            mlLocationList  = DomainObject.findObjects(context,TYPE_PLANT,"*","attribute["+ATTRIBUTE_PLANT_ID+"]=='"+sPlantId+"'",busSelList);
            if (mlLocationList != null && mlLocationList.size() > 0)
                        {
            	mLocationMap=(Map)mlLocationList.get(0);
                            sLocationId= (String)mLocationMap.get(DomainObject.SELECT_ID);
                        }
        }catch(Exception e){
        sLocationId=null;
        }
    return sLocationId;
    }

    public int applyDevelopmentMarkups(Context context, String args[])  throws Exception
    {
        String strPartId = args[0];
        DomainObject doPart = new DomainObject(strPartId);

    	//BGP: In case the Release Process is Production, Do not execute the trigger program functionality
    	if(!ReleasePhase.isECPartWithDevMode(context, doPart))
    		return 0;
    	//BGP: In case the Release Process is Production, Do not execute the trigger program functionality
        String strWhereClause = "current == " + proposedState;
        String strRelPattern = DomainConstants.RELATIONSHIP_EBOM_MARKUP;
        String strTypePattern = TYPE_BOMMARKUP;

        StringList strlObjectSelects = new StringList(1);
        strlObjectSelects.addElement(DomainConstants.SELECT_ID);

        StringList strlRelSelects = new StringList(1);

        MapList mapListMarkups = doPart.getRelatedObjects(context,
                                        strRelPattern, // relationship pattern
                                        strTypePattern, // object pattern
                                        strlObjectSelects, // object selects
                                        strlRelSelects, // relationship selects
                                        false, // to direction
                                        true, // from direction
                                        (short) 1, // recursion level
                                        strWhereClause, // object where clause
                                        null, // relationship where clause
                                        0); //limit 0 to return all the data available.

        Iterator itrMarkups = mapListMarkups.iterator();

        String strMarkupId = null;
        Map mapMarkup = null;

        while (itrMarkups.hasNext())
        {
            mapMarkup = (Map) itrMarkups.next();
            strMarkupId = (String) mapMarkup.get(DomainConstants.SELECT_ID);

            int i = applyBOMMarkup(context,strMarkupId,strPartId,null);

            if (i == 1)
            {
            	return 1;
        }
        	else
        	{
				DomainObject doMarkup = new DomainObject(strMarkupId);
				ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
				doMarkup.setAttributeValue(context,ATTRIBUTE_BRANCH_TO,"None");
				doMarkup.promote(context);
				ContextUtil.popContext(context);
			}
        }
        return 0;


    }
    /*
     * This method used to apply the plant item markup to the database
     * @param context, sMarkupId, sChangeId, sPartId
     * @return void
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    public void applyPlantItemMarkup(Context context, String sMarkupId,  String sPartId, String sChangeId) throws Exception
    {
        com.matrixone.jdom.Element rootElement = null;
        try{
            DomainObject domPart = new DomainObject(sPartId);

            rootElement = checkoutXML(context, sMarkupId);
            if(rootElement == null){
                return;
            }

            java.util.List lElement     = rootElement.getChildren();
            java.util.Iterator itrXML     = lElement.iterator();
            while(itrXML.hasNext()){
		boolean leadPlantFound          = false;
                HashMap hmAttrMap                 = new HashMap();
                com.matrixone.jdom.Element childElement     = (com.matrixone.jdom.Element)itrXML.next();
                String sMarkup                     = (String)childElement.getAttributeValue("markup");
                String sMRRelId                    = (String)childElement.getAttributeValue("id");
                String sRelName                    = (String)childElement.getAttributeValue("name");
                String sPlantId                    = (String)childElement.getChild("plant").getAttributeValue("id");
                DomainObject domPlant             = new DomainObject(sPlantId);
                DomainRelationship drMR            = new DomainRelationship(sMRRelId);

                if("changed".equals(sMarkup)){
                    MapList mrList = domPart.getRelatedObjects(context,
                                                        EngineeringConstants.RELATIONSHIP_MANUFACTURING_RESPONSIBILITY,
                                                        TYPE_PLANT,
                                                        null,
                                                        null,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        DomainConstants.SELECT_ID+" == '"+sPlantId+"'",
                                                        SELECT_STATUS+"==Pending",
                                                        0);

	                if(mrList.size()==0) {
	                	continue;
	                }
                }
                com.matrixone.jdom.Element childAttribute = childElement.getChild("attributes");
                java.util.List lAttrElement = childAttribute.getChildren();
                java.util.Iterator itrAttrXML = lAttrElement.iterator();
                while(itrAttrXML.hasNext()){
                    com.matrixone.jdom.Element childAttr = (com.matrixone.jdom.Element)itrAttrXML.next();
                    String sAttrName = (String)childAttr.getAttributeValue("name");
                    if("Plant ID".equals(sAttrName)){
                        continue;
                    }
                    String sNewValue = (String)childAttr.getChild("newvalue").getText();
                    String sOldValue = (String)childAttr.getChild("oldvalue").getText();

                    if(sAttrName.equals(ATTRIBUTE_LEAD_PLANT)){
                        if(sNewValue.equalsIgnoreCase("true")){
						leadPlantFound = true;
					}
                        else{
                            leadPlantFound = false;
                        }
                    }

                    if("changed".equals(sMarkup) && !sOldValue.equals(sNewValue)){
                        hmAttrMap.put(sAttrName, sNewValue);
                    }
                    else if("add".equals(sMarkup)){
                        hmAttrMap.put(sAttrName, sNewValue);
                    }
                }

                if("changed".equals(sMarkup)){
					if(leadPlantFound){
                        String sERPStatus = (String)hmAttrMap.get(ATTRIBUTE_ERP_STATUS);
                        if(sERPStatus == null || "".equals(sERPStatus) || "null".equals(sERPStatus)){
                            sERPStatus = drMR.getAttributeValue(context, ATTRIBUTE_ERP_STATUS);
                        }

                        String sDocIn = drMR.getAttributeValue(context, ATTRIBUTE_DOC_IN);

                        String sCurrentDocInId   = null;
                        String sVault = domPart.getInfo(context,DomainConstants.SELECT_VAULT);

                        StringList objectSelects = new StringList();
                        objectSelects.add(DomainObject.SELECT_NAME);
                        objectSelects.add(DomainObject.SELECT_ID);

                        MapList findObjects = DomainObject.findObjects(context,
                                                                       "*",
                                                                       sDocIn,
                                                                       "*",
                                                                       "*",
                                                                       sVault,
                                                                       null,
                                                                       false,
                                                                       objectSelects);

                        if(!findObjects.isEmpty()){
                            Map mp = (Map)findObjects.get(0);
                            sCurrentDocInId = (String)mp.get(DomainObject.SELECT_ID);
                        }


                        HashMap hmParams = new HashMap();
                        hmParams.put("partId", sPartId);
                        hmParams.put("plantId", sPlantId);
                        hmParams.put("manuRelId", sMRRelId);
                        hmParams.put("ERPStatus", sERPStatus);
                        hmParams.put("plantType", "Lead");
                        hmParams.put("DocIn", sDocIn);
                        hmParams.put("DocInId", sCurrentDocInId);
                        changePlantToLeadPlant(context, hmParams);
						leadPlantFound = false;
					}

                    drMR.setAttributeValues(context, hmAttrMap);
                }
                else if("add".equals(sMarkup)){
                    //Migration Change
                    String sDocInId            = (String)childElement.getChild("Doc-In").getAttributeValue("name");


                    DomainObject domChange        = new DomainObject(sDocInId);
                    String sDocInName = domChange.getInfo(context,"name");
                    hmAttrMap.put("Doc-In", sDocInName);

					DomainRelationship domRelMR = checkForPendingMRRel(context, sPartId, sPlantId);
					if(domRelMR == null){
						domRelMR = connectObjectToObject(context,
                                                                        domPart,
                                                                        domPlant,
                                                                        sRelName);
					}

                    Map mapHighest = getHeighestMRSequence(context, domPart, domPlant);

                    int iHighestSeq = Integer.parseInt((String)mapHighest.get(SELECT_SEQUENCE));
                    int iCurrentSeq = Integer.parseInt((String)hmAttrMap.get(ATTRIBUTE_SEQUENCE));

                    if(iCurrentSeq<=iHighestSeq){
                        iCurrentSeq = iHighestSeq + 1;
                    }
                    hmAttrMap.put(ATTRIBUTE_SEQUENCE, ""+iCurrentSeq);

					sMRRelId = (String)domRelMR.toString();
                    domRelMR.setAttributeValues(context, hmAttrMap);

					if(leadPlantFound){
                        String sERPStatus = (String)hmAttrMap.get(ATTRIBUTE_ERP_STATUS);
                        if(sERPStatus == null || "".equals(sERPStatus) || "null".equals(sERPStatus)){
                            sERPStatus = drMR.getAttributeValue(context, ATTRIBUTE_ERP_STATUS);
                        }

                        HashMap hmParams = new HashMap();
                        hmParams.put("partId", sPartId);
                        hmParams.put("plantId", sPlantId);
                        hmParams.put("manuRelId", sMRRelId);
                        hmParams.put("ERPStatus", sERPStatus);
                        hmParams.put("plantType", "Lead");
                        hmParams.put("DocIn", sDocInName);
                        hmParams.put("DocInId", sDocInId);
                        changePlantToLeadPlant(context, hmParams);
						leadPlantFound = false;
					}


                    StringBuffer sbQuery = new StringBuffer();
                    sbQuery.append("print bus ");
                    sbQuery.append("$1");
                    sbQuery.append(" select ");
                    sbQuery.append("$2");
                    sbQuery.append(" dump");

                    StringBuffer param = new StringBuffer(); //2nd argument of query
                    param.append("to[");
                    param.append(RELATIONSHIP_MANUFACTURING_RESPONSIBILITY_CHANGE);
                    param.append('|');
                    param.append(DomainConstants.SELECT_FROM_ID);
                    param.append("=='");
                    param.append(sChangeId);
                    param.append("']");

                    String sFlag = MqlUtil.mqlCommand(context, sbQuery.toString(), sPartId, param.toString());

                    if(sFlag.equalsIgnoreCase("false")){
						connectObjectToObject(context,
                                                                             domPart,
                                                                                domChange,
                                                                                RELATIONSHIP_MANUFACTURING_RESPONSIBILITY_CHANGE);
                    }
                }
            }
			String strAppliedPartMarkupRel = PropertyUtil.getSchemaProperty(context,"relationship_AppliedPartMarkup");
			DomainObject doPart = new DomainObject(sPartId);
			DomainRelationship.connect(context, doPart, strAppliedPartMarkupRel, new DomainObject(sMarkupId));
        }
        catch(Exception e){
            throw e;
        }
    }

    public boolean changePlantToLeadPlant(Context context, HashMap hmParams) throws Exception
	{


		try{
               updateManuResp(context,(String)hmParams.get("manuRelId"),(String)hmParams.get("plantType"));

		}catch(Exception e){
			throw e;
		}
		return true;
	}

    public String getECOIdByName(Context context, String ecoName, String strVault)
        throws Exception
    {
        String strECOId = null;
        String strSystem = EnoviaResourceBundle.getProperty(context, "emxMBOM.MBOM.SystemECOName");
        StringList busSelList = new StringList();
        MapList mlObjList = null;
        try
        {
            if(!ecoName.equalsIgnoreCase(strSystem))
            {
                busSelList.add("id");
                mlObjList = DomainObject.findObjects(context, DomainObject.TYPE_ECO, ecoName, "*", "*", "*", "", true, busSelList);
                strECOId = (String)((Map)mlObjList.get(0)).get("id");
            } else
            {
                strECOId = ecoName;
            }
        }
        catch(Exception ex)
        {
            strECOId = ecoName;
        }
        return strECOId;
    }

    public Map getPreviousLeadPlantRel(Context context, DomainObject dmPartMaster)
        throws Exception
    {
        StringBuffer strRelWhere = null;
        Map mapLP = null;
        try
        {
            StringList busSelList = new StringList(1);
            StringList relSelList = new StringList(5);
            busSelList.add("id");
            relSelList.add("id[connection]");
            relSelList.add(SELECT_SEQUENCE);
            relSelList.add(SELECT_STATUS);
            relSelList.add(SELECT_LEAD_PLANT);
            relSelList.add(SELECT_ERP_STATUS);
            strRelWhere = new StringBuffer();
            strRelWhere.append((new StringBuffer()).append(SELECT_LEAD_PLANT).append(" == 'TRUE'").toString());
            strRelWhere.append(" && (");
            strRelWhere.append(SELECT_STATUS);
            strRelWhere.append(" != '");
            strRelWhere.append("History");
            strRelWhere.append("')");
            MapList mlMRObjs = dmPartMaster.getRelatedObjects(context, RELATIONSHIP_MANUFACTURING_RESPONSIBILITY, TYPE_PLANT, busSelList, relSelList, true, false, (short)1, "", strRelWhere.toString(),0);
            int iSeq = 0;
            int iHighestSeq = 0;
            DomainObject dmLoc = DomainObject.newInstance(context);
            Map mapHighest = null;
            if(mlMRObjs != null && mlMRObjs.size() > 0)
            {
                int i = 0;
                do
                {
                    if(i >= mlMRObjs.size())
                        break;
                    mapLP = (Map)mlMRObjs.get(i);
                    iSeq = Integer.parseInt((String)mapLP.get(SELECT_SEQUENCE));
                    dmLoc.setId((String)mapLP.get("id"));
                    mapHighest = getHeighestMRSequence(context, dmPartMaster, dmLoc);
                    iHighestSeq = Integer.parseInt((String)mapHighest.get(SELECT_SEQUENCE));
                    if(iSeq == iHighestSeq)
                        break;
                    i++;
                } while(true);
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        return mapLP;
    }


    /*
     * This method used to checkout the xml file from the markup object
     * @param context, sMarkupId
     * @return com.matrixone.jdom.Element
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    public com.matrixone.jdom.Element checkoutXML(Context context, String sMarkupId) throws Exception
    {
        com.matrixone.jdom.Element rootElement = null;
        try{
            DomainObject domMarkup          = new DomainObject(sMarkupId);
            String sXMLFormat              = PropertyUtil.getSchemaProperty(context, "format_XML");
            String sMarkupName              = domMarkup.getInfo(context, "name");
            String sTransPath              = context.createWorkspace();
            java.io.File fEmatrixWebRoot = new java.io.File(sTransPath);

            domMarkup.checkoutFile(context, false, sXMLFormat, sMarkupName+ ".xml", fEmatrixWebRoot.toString());
            java.io.File srcXMLFile = new java.io.File(fEmatrixWebRoot, sMarkupName+ ".xml");

            com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder();
			builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
			builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            com.matrixone.jdom.Document docXML         = builder.build(srcXMLFile);
            rootElement = docXML.getRootElement();
        }
        catch(Exception e){
            throw e;
        }
        return rootElement;
    }

    /*
     * This method used to connect the Change to the part
     * @param context, domPart, domChange, sConnectRelName
     * @return DomainRelationship
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
   public DomainRelationship connectObjectToObject(Context context, DomainObject domPart, DomainObject domChange, String sConnectRelName) throws Exception
   {
       DomainRelationship dmRel = null;
       try{
           dmRel = DomainRelationship.newInstance(context);
           dmRel = DomainRelationship.connect(context,
                                             domChange,
                                             sConnectRelName,
                                             domPart);
       }catch(Exception e){
           throw e;
       }
       return dmRel;
   }

    /**
     * Used for  generating XML from Affected Items
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing "commandType"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
    public void generateBOMMarkupXML(Context context,String massUpdateAction,String strMarkupId,String sSelPartId,Hashtable htConflictBOMs) throws Exception
    {
        // Newly Created MarkupId
        DomainObject smkpdomobj=new DomainObject();
        smkpdomobj.setId(strMarkupId);

        DomainObject dosSelPart=new DomainObject();
        dosSelPart.setId(sSelPartId);

        String sSelPartName=dosSelPart.getInfo(context,DomainConstants.SELECT_NAME);
        String sSelPartType=dosSelPart.getInfo(context,DomainConstants.SELECT_TYPE);
        String sSelPartrevision=dosSelPart.getInfo(context,DomainConstants.SELECT_REVISION);
        String sSelPartVault=dosSelPart.getInfo(context,DomainConstants.SELECT_VAULT);
        String sSelPartPolicy=dosSelPart.getInfo(context,DomainConstants.SELECT_POLICY);
        String sSelPartOwner=dosSelPart.getInfo(context,DomainConstants.SELECT_OWNER);
        String sSelPartDescription=dosSelPart.getInfo(context,DomainConstants.SELECT_DESCRIPTION);

        String ATTRIBUTE_NOTES=PropertyUtil.getSchemaProperty(context,"attribute_Notes");
        StringList selStmts=new StringList(4);
        selStmts.addElement(DomainConstants.SELECT_ID);
        selStmts.addElement(DomainConstants.SELECT_TYPE);
        selStmts.addElement(DomainConstants.SELECT_NAME);
        selStmts.addElement(DomainConstants.SELECT_REVISION);
        selStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_UNITOFMEASURE);
        selStmts.addElement(DomainConstants.SELECT_VAULT);
        selStmts.addElement(DomainConstants.SELECT_CURRENT);

        StringList selRelStmts=new StringList(1);
        selRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
        selRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
        selRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_QUANTITY);
        selRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_USAGE);
        selRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        selRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        selRelStmts.addElement("attribute["+ATTRIBUTE_NOTES+"]");

        HashMap AffectedParts=new HashMap();
        String strEBOMKeytemp =null;
        HashMap BOMMap=new HashMap();

        String ATTRIBUTE_FIND_NUMBER=null;
        String ATTRIBUTE_REFERENCE_DESIGNATOR=null;
        String ebomObjectId=null;
        String ebomObjectType=null;
        String ebomObjectName=null;
        String ebomObjectRevision=null;
        String ebomRelId=null;
        String ebomObjectcurrent=null;
        String ebomObjectVault=null;
        String ATTRIBUTE_UNITOFMEASURE=null;
        String ATTRIBUTE_QUANTITY=null;
        String ATTRIBUTE_USAGE=null;
        String ATTRIBUTE_COMPONENT_LOCATION=null;
        String Notes=null;
        String[] str=new String[4];

        MapList ebomList=dosSelPart.getRelatedObjects(context,DomainConstants.RELATIONSHIP_EBOM,DomainConstants.TYPE_PART,selStmts,selRelStmts,false,true,(short)1,null,null,0);

        Iterator ebomListItr=ebomList.iterator();

        Enumeration enumkeysitr=null;
        Iterator itr=null;

        com.matrixone.jdom.Element ematrixrootElement = new com.matrixone.jdom.Element("ematrix");

        // Creation of Root XML Starts
        com.matrixone.jdom.Element businessObjectElement = new com.matrixone.jdom.Element("businessObject");
        businessObjectElement.setAttribute("id",sSelPartId);
        ematrixrootElement.addContent(businessObjectElement);

        com.matrixone.jdom.Element objectTypeElement = new com.matrixone.jdom.Element("objectType");
        //Start : IR-054311V6R2011x
        objectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(sSelPartType));
        //End : IR-054311V6R2011x
        businessObjectElement.addContent(objectTypeElement);

        com.matrixone.jdom.Element objectNameElement = new com.matrixone.jdom.Element("objectName");
        //Start : IR-054311V6R2011x
        objectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(sSelPartName));
        //End : IR-054311V6R2011x
        businessObjectElement.addContent(objectNameElement);

        com.matrixone.jdom.Element objectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
        //Start : IR-054311V6R2011x
        objectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(sSelPartrevision));
        //End : IR-054311V6R2011x
        businessObjectElement.addContent(objectRevisionElement);

        com.matrixone.jdom.Element vaultRefElement = new com.matrixone.jdom.Element("vaultRef");
        //Start : IR-054311V6R2011x
        vaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(sSelPartVault));
        //End : IR-054311V6R2011x
        businessObjectElement.addContent(vaultRefElement);

        com.matrixone.jdom.Element policyRefElement = new com.matrixone.jdom.Element("policyRef");
        policyRefElement.setText(sSelPartPolicy);
        businessObjectElement.addContent(policyRefElement);

        com.matrixone.jdom.Element ownerElement = new com.matrixone.jdom.Element("owner");
        businessObjectElement.addContent(ownerElement);

        com.matrixone.jdom.Element userRefElement = new com.matrixone.jdom.Element("userRef");
        //Start : IR-054311V6R2011x
        userRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(sSelPartOwner));
        //End : IR-054311V6R2011x
        ownerElement.addContent(userRefElement);

        com.matrixone.jdom.Element descriptionElement = new com.matrixone.jdom.Element("description");
        //Start : IR-054311V6R2011x
        descriptionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(sSelPartDescription));
        //End : IR-054311V6R2011x
        businessObjectElement.addContent(descriptionElement);

        Element fromRelationshipListElement=new com.matrixone.jdom.Element("fromRelationshipList");
        businessObjectElement.addContent(fromRelationshipListElement);
        // Creation of root XML ends

        com.matrixone.jdom.Element relationshipElement =null;
        com.matrixone.jdom.Element relationshipDefRefElement =null;
        com.matrixone.jdom.Element relatedObjectElement =null;
        com.matrixone.jdom.Element businessObjectRefElement =null;
        com.matrixone.jdom.Element subobjectTypeElement = null;
        com.matrixone.jdom.Element subobjectNameElement = null;
        com.matrixone.jdom.Element subobjectRevisionElement =null;
        com.matrixone.jdom.Element subvaultRefElement =null;
        com.matrixone.jdom.Element UOMElement = null;
        com.matrixone.jdom.Element attributeListElement=null;
        com.matrixone.jdom.Element attributeElement = null;
        com.matrixone.jdom.Element nameElement = null;
        com.matrixone.jdom.Element StringElement = null;
        com.matrixone.jdom.Element realElement = null;
        com.matrixone.jdom.Element objectStateElement =null;

        Element eleRoot = null;

        com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder(false);
		builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
		builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder.setValidation(false);

        // Nothing has been modified after opening a markup
        if (massUpdateAction.equalsIgnoreCase("<mxRoot/>"))
        {
            while(ebomListItr.hasNext())
            {
                Map mapebomList=(Map) ebomListItr.next();
                ebomObjectId=(String) mapebomList.get(DomainConstants.SELECT_ID);
               ebomObjectType=(String)mapebomList.get(DomainConstants.SELECT_TYPE);
               ebomObjectName=(String)mapebomList.get(DomainConstants.SELECT_NAME);

                ebomObjectRevision=(String)mapebomList.get(DomainConstants.SELECT_REVISION);
                ATTRIBUTE_FIND_NUMBER=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
                ATTRIBUTE_REFERENCE_DESIGNATOR=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
                ebomRelId=(String) mapebomList.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                ebomObjectcurrent=(String)mapebomList.get(DomainConstants.SELECT_CURRENT);
               ebomObjectVault=(String)mapebomList.get(DomainConstants.SELECT_VAULT);
              ATTRIBUTE_UNITOFMEASURE=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_UNITOFMEASURE);
                 ATTRIBUTE_QUANTITY=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_QUANTITY);
                 ATTRIBUTE_USAGE=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_USAGE);
                 ATTRIBUTE_COMPONENT_LOCATION=(String)mapebomList.get(DomainConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                 Notes=(String)mapebomList.get("attribute["+ATTRIBUTE_NOTES+"]");

               strEBOMKeytemp = ebomObjectType + "~" + ebomObjectName + "~" + ebomObjectRevision + "~" + ATTRIBUTE_FIND_NUMBER + "~" + ATTRIBUTE_REFERENCE_DESIGNATOR;

               BOMMap.put("strEBOMKeytemp",strEBOMKeytemp);
            }

         // P1 Deleted and P1 changed
            if (!htConflictBOMs.isEmpty())
            {
              enumkeysitr=htConflictBOMs.keys();
              while(enumkeysitr.hasMoreElements())
              {
                  String strKey=(String)enumkeysitr.nextElement();
                  ArrayList arlConflicts=(ArrayList)htConflictBOMs.get(strKey);
                  AffectedParts.put("strKey",strKey);
                  itr=arlConflicts.iterator();
                  while(itr.hasNext())
                  {
                      Element eleRe=(Element)itr.next();
                      fromRelationshipListElement.addContent(eleRe.detach());
                  }
              }

            }
              if(!BOMMap.containsKey(AffectedParts))
              {
                  relationshipElement = new com.matrixone.jdom.Element("relationship");
                  fromRelationshipListElement.addContent(relationshipElement);

                  relationshipDefRefElement = new com.matrixone.jdom.Element("relationshipDefRef");
                  relationshipDefRefElement.setAttribute("relid",ebomRelId);
                  relationshipDefRefElement.setText(DomainConstants.RELATIONSHIP_EBOM);
                  relationshipElement.addContent(relationshipDefRefElement);

                  relatedObjectElement = new com.matrixone.jdom.Element("relatedObject");
                  relatedObjectElement.setAttribute("relatedobjid",ebomObjectId);
                  relationshipElement.addContent(relatedObjectElement);

                  businessObjectRefElement = new com.matrixone.jdom.Element("businessObjectRef");
                  relatedObjectElement.addContent(businessObjectRefElement);

                  subobjectTypeElement = new com.matrixone.jdom.Element("objectType");
                  //Start : IR-054311V6R2011x
                  subobjectTypeElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectType));
                  //End : IR-054311V6R2011x
                  businessObjectRefElement.addContent(subobjectTypeElement);

                  subobjectNameElement = new com.matrixone.jdom.Element("objectName");
                  //Start : IR-054311V6R2011x
                  subobjectNameElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectName));
                  //End : IR-054311V6R2011x
                  businessObjectRefElement.addContent(subobjectNameElement);

                  subobjectRevisionElement = new com.matrixone.jdom.Element("objectRevision");
                  //Start : IR-054311V6R2011x
                  subobjectRevisionElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectRevision));
                  //End : IR-054311V6R2011x
                  businessObjectRefElement.addContent(subobjectRevisionElement);

                  subvaultRefElement = new com.matrixone.jdom.Element("vaultRef");
                  //Start : IR-054311V6R2011x
                  subvaultRefElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectVault));
                  //End : IR-054311V6R2011x
                  businessObjectRefElement.addContent(subvaultRefElement);

                  UOMElement = new com.matrixone.jdom.Element("UOM");
                  UOMElement.setText(ATTRIBUTE_UNITOFMEASURE);
                  businessObjectRefElement.addContent(UOMElement);

                  objectStateElement = new com.matrixone.jdom.Element("objectState");
                  //Start : IR-054311V6R2011x
                  objectStateElement.setText(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ebomObjectcurrent));
                  //End : IR-054311V6R2011x
                  businessObjectRefElement.addContent(objectStateElement);

                  attributeListElement = new com.matrixone.jdom.Element("attributeList");
                  relationshipElement.addContent(attributeListElement);

                  attributeElement = new com.matrixone.jdom.Element("attribute");
                  attributeListElement.addContent(attributeElement);

                  nameElement = new com.matrixone.jdom.Element("name");
                  nameElement.setText(DomainConstants.ATTRIBUTE_FIND_NUMBER);
                  attributeElement.addContent(nameElement);

                  StringElement = new com.matrixone.jdom.Element("string");
                  StringElement.setText(ATTRIBUTE_FIND_NUMBER);
                  attributeElement.addContent(StringElement);

                  attributeElement = new com.matrixone.jdom.Element("attribute");
                  attributeListElement.addContent(attributeElement);

                  nameElement = new com.matrixone.jdom.Element("name");
                  nameElement.setText(DomainConstants.ATTRIBUTE_QUANTITY);
                  attributeElement.addContent(nameElement);

                  realElement = new com.matrixone.jdom.Element("real");
                  realElement.setText(ATTRIBUTE_QUANTITY);
                  attributeElement.addContent(realElement);

                  attributeElement = new com.matrixone.jdom.Element("attribute");
                  attributeListElement.addContent(attributeElement);

                  nameElement = new com.matrixone.jdom.Element("name");
                  nameElement.setText(DomainConstants.ATTRIBUTE_USAGE);
                  attributeElement.addContent(nameElement);

                  StringElement = new com.matrixone.jdom.Element("string");
                  StringElement.setText(ATTRIBUTE_USAGE);
                  attributeElement.addContent(StringElement);

                  attributeElement = new com.matrixone.jdom.Element("attribute");
                  attributeListElement.addContent(attributeElement);

                  nameElement = new com.matrixone.jdom.Element("name");
                  nameElement.setText(DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
                  attributeElement.addContent(nameElement);

                  StringElement = new com.matrixone.jdom.Element("string");
                  StringElement.setText(ATTRIBUTE_REFERENCE_DESIGNATOR);
                  attributeElement.addContent(StringElement);

                  attributeElement = new com.matrixone.jdom.Element("attribute");
                  attributeListElement.addContent(attributeElement);

                  nameElement = new com.matrixone.jdom.Element("name");
                  nameElement.setText(DomainConstants.ATTRIBUTE_COMPONENT_LOCATION);
                  attributeElement.addContent(nameElement);

                  StringElement = new com.matrixone.jdom.Element("string");
                  StringElement.setText(ATTRIBUTE_COMPONENT_LOCATION);
                  attributeElement.addContent(StringElement);

                  attributeElement = new com.matrixone.jdom.Element("attribute");
                  attributeListElement.addContent(attributeElement);

                  nameElement = new com.matrixone.jdom.Element("name");
                  nameElement.setText(ATTRIBUTE_NOTES);
                  attributeElement.addContent(nameElement);

                  StringElement = new com.matrixone.jdom.Element("string");
                  StringElement.setText(Notes);
                  attributeElement.addContent(StringElement);
              }

              chekinToMarkupObject(context,ematrixrootElement,smkpdomobj);

        }

            // Gets the XML From the SB other than <mxRoot/>
            else
            {
                str[0]=massUpdateAction;
                str[1]=strMarkupId;
                str[2]=sSelPartId;
                str[3]="true";

                generateBOMMarkupXML(context,str);
                if (eleDocRoot != null)
                    eleRoot = eleDocRoot;

                fromRelationshipListElement=eleRoot.getChild("businessObject").getChild("fromRelationshipList");

                // Contains Conflict
                if (!htConflictBOMs.isEmpty())
                {
                              enumkeysitr=htConflictBOMs.keys();
                                  while(enumkeysitr.hasMoreElements())
                                  {

                                      String strKey=(String)enumkeysitr.nextElement();
                                      ArrayList arlConflicts=(ArrayList)htConflictBOMs.get(strKey);
                                      AffectedParts.put("strKey",strKey);

                                      itr=arlConflicts.iterator();
                                      while(itr.hasNext())

                                      {
                                          Element eleRe=(Element)itr.next();
                                          fromRelationshipListElement.addContent(eleRe.detach());
                                      }
                                  }
                }

                smkpdomobj.unlock(context);
                chekinToMarkupObject(context,eleRoot,smkpdomobj);
            }
    }

    /**
     *  Creates Markup From Affected Items Page
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
    public void createMarkupfromAffectedItem(Context context,String[] args) throws Exception
    {
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap requestMap=(HashMap)programMap.get("requestMap");
        HashMap paramMap=(HashMap)programMap.get("paramMap");

        String sPartObjId=(String)paramMap.get("sObjId");
        String sCommandType = (String)requestMap.get("commandType");
        String sDescription=(String)paramMap.get("Description");
        String strMarkupIds=(String)requestMap.get("strMarkupIds");
        String sChangeObjId=(String)requestMap.get("sChangeObjId");
        String massUpdateAction=(String)requestMap.get("massUpdateAction");
        Hashtable htConflictBOMs=(Hashtable)requestMap.get("ConflictList");
        String strMarkUpName=(String)paramMap.get("Name");



        String snewMarkupId=null;
        StringList strlMarkupIds = FrameworkUtil.split(strMarkupIds, ",");
        
        //Added for ENG Convergence start
		if(UIUtil.isNotNullAndNotEmpty(sChangeObjId)) { //Added for IR-266950
			DomainObject dChgObj=new DomainObject(sChangeObjId);
			if(dChgObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_REQUEST)) {
				 ChangeOrder changeOrder = new ChangeOrder(sChangeObjId);
				 StringList objIdList = new StringList();
				 objIdList.add(sPartObjId);
				 HashMap mCAList = (HashMap) changeOrder.connectAffectedItems(context,objIdList);	
				 HashMap mCAIdList = (HashMap)mCAList.get("objIDCAMap");					      
				 String strCAId = (String)mCAIdList.get(sPartObjId);		     
				 sChangeObjId = strCAId;		    
			}  
		}		
        //Added for ENG Convergence End
        
        DomainObject chgObj=new DomainObject();

        if (sChangeObjId != null)
        {
        chgObj.setId(sChangeObjId);
		}

        if(!"null".equals(strlMarkupIds))
        {
            // If One Markup is Selected to Open
            if (strlMarkupIds.size()==1)
            {

                if("Save".equalsIgnoreCase(sCommandType))
                {

                DomainObject dooldmkp=new DomainObject();
                dooldmkp.setId(strMarkupIds);
                dooldmkp.setDescription(context , sDescription);
                // This Over writes the file in to the Object

                generateBOMMarkupXML(context,massUpdateAction,strMarkupIds,sPartObjId,htConflictBOMs);
                }

                // Single Markup with SaveAs opetion
                // Creates a  new Markup and Leaves the Old Markup Untouched
                else
                {  //When you selct Save As Option in Markup
                    //Bug: 371209 AM Start
                    //generateBOMMarkupXML(context,massUpdateAction,strMarkupIds,sPartObjId,htConflictBOMs);
                    //Bug: 371209 AM End

		    DomainObject doPart = new DomainObject(sPartObjId);
		    String strPartPolicy = doPart.getInfo(context, DomainConstants.SELECT_POLICY);
		    String strPartRelPhase = doPart.getInfo(context, EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);

		    if (strPartPolicy.equals(DomainConstants.POLICY_EC_PART) && "Production".equalsIgnoreCase(strPartRelPhase))
		    {
                    	snewMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
                    }
                    else if ((strPartPolicy.equals(DomainConstants.POLICY_EC_PART) && "Development".equalsIgnoreCase(strPartRelPhase)) || strPartPolicy.equals(DomainConstants.POLICY_DEVELOPMENT_PART))
                    {
                    	snewMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_DevelopmentPartMarkup");
                    }
                    else
                    {
                    snewMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
                    }
                    DomainObject sdomobj= new DomainObject();
                    sdomobj.setId(snewMarkupId);
                    sdomobj.setDescription(context,sDescription);
                    if(strMarkUpName != null && strMarkUpName.trim().length()>0){
                        sdomobj.setName(context, strMarkUpName);
                    }
                    DomainRelationship.connect(context,new DomainObject(sPartObjId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(snewMarkupId));

					if (sChangeObjId != null)
					{
						if (chgObj.isKindOf(context, DomainConstants.TYPE_ECO) || chgObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION)) //Modified for ENG Convergence
                    {
                      DomainRelationship.connect(context,new DomainObject(sChangeObjId),RELATIONSHIP_APPLIED_MARKUP,new DomainObject(snewMarkupId));
                    }
                    // If Change Type is ECR
						else if(chgObj.isKindOf(context, DomainConstants.TYPE_ECR))
                    {
                     DomainRelationship.connect(context,new DomainObject(sChangeObjId),RELATIONSHIP_PROPOSED_MARKUP,new DomainObject(snewMarkupId));
						}
                    }

                    generateBOMMarkupXML(context,massUpdateAction,snewMarkupId,sPartObjId,htConflictBOMs);
                }
            }
// If More than One Markup is Opened then Delete the Other Markups and Create a new Markup with Auto Name
            else
            {

		    DomainObject doPart = new DomainObject(sPartObjId);
		    String strPartPolicy = doPart.getInfo(context, DomainConstants.SELECT_POLICY);

		    if (strPartPolicy.equals(DomainConstants.POLICY_EC_PART))
		    {
                snewMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
		    }
		    else if (strPartPolicy.equals(DomainConstants.POLICY_DEVELOPMENT_PART))
		    {
			snewMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_DevelopmentPartMarkup");
		    }
		    else
		    {
			snewMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
		    }

                DomainObject sdomobj= new DomainObject();
                sdomobj.setId(snewMarkupId);
                sdomobj.setDescription(context,sDescription);
                if(strMarkUpName != null && strMarkUpName.trim().length()>0){
                    sdomobj.setName(context, strMarkUpName);
                }
               DomainRelationship.connect(context,new DomainObject(sPartObjId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(snewMarkupId));

				if (sChangeObjId != null)
				{
					if (chgObj.isKindOf(context, DomainConstants.TYPE_ECO) || chgObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION)) //Modified for ENG Convergence
                {
                  DomainRelationship.connect(context,new DomainObject(sChangeObjId),RELATIONSHIP_APPLIED_MARKUP,new DomainObject(snewMarkupId));
                }
                // If Change Type is ECR
					else if(chgObj.isKindOf(context, DomainConstants.TYPE_ECR))
                {
                 DomainRelationship.connect(context,new DomainObject(sChangeObjId),RELATIONSHIP_PROPOSED_MARKUP,new DomainObject(snewMarkupId));
					}
				}

                generateBOMMarkupXML(context,massUpdateAction,snewMarkupId,sPartObjId,htConflictBOMs);

                if("Save".equalsIgnoreCase(sCommandType) || "SaveAs".equalsIgnoreCase(sCommandType))
                {
                Iterator itrstrlMarupIds=strlMarkupIds.iterator();
                while(itrstrlMarupIds.hasNext())
                {
                    String markupid=(String)itrstrlMarupIds.next();
                    DomainObject domk=new DomainObject();
                    domk.setId(markupid);
                    domk.deleteObject(context);
                }
                }

          // End of Else loop for Multiple Selection of Markup
            }

         // End of If Loop to Validate it MarkupId for not being Null

        }
    }

      /*
       * This method used show Change Process Name from Affected Items  Page
       * @param context
       * @return String value
       * @throws Exception if the operation fails
       * @since Common V6R2009-1
       * @grade 0
       */

      public String getChangeObject(Context context,String[] args)throws Exception
      {

          HashMap programMap=(HashMap)JPO.unpackArgs(args);
          HashMap requestMap=(HashMap)programMap.get("requestMap");
          String sChangeObjId=(String)requestMap.get("sChangeObjId");
          DomainObject doChgObj=new DomainObject();
          if (sChangeObjId != null)
          {
          doChgObj.setId(sChangeObjId);
          String sChngObjName=doChgObj.getInfo(context,DomainConstants.SELECT_NAME);
          return sChngObjName;
	      }
	      return "";

      }

    /**
     *  Save/Save As Plant BOM Markup From Affected Items Page
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/

    public void savePlantBOMMarkup(Context context,String[] args)
		throws Exception {

        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        HashMap requestMap=(HashMap)programMap.get("requestMap");
        HashMap paramMap=(HashMap)programMap.get("paramMap");
        String sPartObjId=(String)paramMap.get("sObjId");
        String sCommandType = (String)requestMap.get("commandType");
        String sDescription=(String)paramMap.get("Description");
        String strMarkupIds=(String)requestMap.get("strMarkupIds");


        String sChangeObjId=(String)requestMap.get("sChangeObjId");

		String ECChange =(String)requestMap.get("ECChange");
		String PlantChange =(String)requestMap.get("PlantChange");


		String hasBOMChanges =(String)requestMap.get("hasBOMChanges");
		String hasPlantChanges =(String)requestMap.get("hasPlantChanges");

        String massUpdateAction=(String)requestMap.get("massUpdateAction");
        Hashtable htConflictBOMs=(Hashtable)requestMap.get("ConflictList");
        Hashtable htPlantConflictBOMs = (Hashtable)requestMap.get("PlantConflictList");

        String strPlantId = (String)paramMap.get("plantID");
        String sPlantObjID = getLocation(context, strPlantId);
        String[] strArrParams = new String[5];
        strArrParams[0] = massUpdateAction;
        strArrParams[1] = "";
        strArrParams[2] = sPartObjId;
        strArrParams[3] = sPlantObjID;
		strArrParams[4] = "true";
        String snewMarkupId=null;
        StringList strlMarkupIds = FrameworkUtil.split(strMarkupIds, ",");
		String sChgType = DomainConstants.EMPTY_STRING;

		/* Get No. Of Markups */
		int intMarkupIdSize = strlMarkupIds.size();

		ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());

		// If One Markup is selected to open i.e. only Plant BOM Markup is selected
		if (intMarkupIdSize == 1) {
			if("Save".equalsIgnoreCase(sCommandType)) {
				DomainObject dooldmkp=new DomainObject();
				dooldmkp.setId(strMarkupIds);
				dooldmkp.setDescription(context , sDescription);
				if ("true".equals(hasPlantChanges)) {
					strArrParams[1] = strMarkupIds;
					generatePlantBOMMarkupXML(context,strArrParams,htPlantConflictBOMs);
				}
				if ("true".equals(hasBOMChanges)) {
					if(null!=ECChange && !"null".equals(ECChange) && !"".equals(ECChange)) {
						snewMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
						DomainObject sdomobj= new DomainObject();
						sdomobj.setId(snewMarkupId);
						sdomobj.setDescription(context,sDescription);

						int index = ECChange.indexOf('|');
						if(index == -1) {
							sChangeObjId = ECChange;
						} else {
							sChangeObjId = ECChange.substring(0, index);
						}

						DomainObject chgBOMObj = new DomainObject(sChangeObjId);
						sChgType = chgBOMObj.getInfo(context,DomainConstants.SELECT_TYPE);
						DomainRelationship.connect(context,new DomainObject(sPartObjId),RELATIONSHIP_EBOM_MARKUP,sdomobj);

						String isChangeConnected = isConnectedtoChangeProcess(context,sPartObjId,sChangeObjId, null);
						/* Default Relationship between ECR/DCR/ECO/MECO and Markup */
						String relName = RELATIONSHIP_AFFECTED_ITEM;

						if(TYPE_ECO.equalsIgnoreCase(sChgType) || TYPE_MECO.equalsIgnoreCase(sChgType)) {
							DomainRelationship.connect(context, chgBOMObj ,RELATIONSHIP_APPLIED_MARKUP,sdomobj);
						} else {
							DomainRelationship.connect(context,chgBOMObj,RELATIONSHIP_PROPOSED_MARKUP,sdomobj);
		}
						/* Connect Change-Part */
						if ("false".equals(isChangeConnected)) {
							DomainRelationship.connect(context,
													   chgBOMObj,
													   relName,
													   new DomainObject(sPartObjId));
		}
						strMarkupIds = snewMarkupId;
                strArrParams[1]=strMarkupIds;
						generateBOMMarkupXML(context,massUpdateAction,strMarkupIds,sPartObjId,htConflictBOMs);
					}//End of if(ECChange !=...
				}//End of if(hasBOMChanges.equals...
            }else if("SaveAs".equalsIgnoreCase(sCommandType)) {
				DomainObject dooldmkp=new DomainObject();
				dooldmkp.setId(strMarkupIds);
				dooldmkp.setDescription(context , sDescription);
				if ("true".equals(hasPlantChanges)) {
					
					DomainObject domMarkup = new DomainObject();
					String sName=(String)paramMap.get("Name");
					ContextUtil.popContext(context);
					String vaultName=Person.getPerson(context).getVaultName(context);
					ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
					String sRevision="-";
					domMarkup.createObject(context,
										 TYPE_PLANT_BOM_MARKUP,
										 sName,
										 sRevision,
										 POLICY_PARTMARKUP,
										 vaultName);
					snewMarkupId = domMarkup.getInfo(context,SELECT_ID);
					
					strArrParams[1] = snewMarkupId;
					domMarkup.setAttributeValue(context,ATTRIBUTE_PLANT_ID,strPlantId);
					DomainRelationship.connect(context,new DomainObject(sPartObjId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(snewMarkupId));
					String isChangeConnected = "false";
					String isPlantChangeConnected = "false";
					String strChangeRelNew = "";
					if(sChangeObjId != null){
					strChangeRelNew = RELATIONSHIP_CHANGE_AFFECTED_ITEM;
					
					if(ChangeConstants.TYPE_CHANGE_ACTION.equalsIgnoreCase(new DomainObject(sChangeObjId).getInfo(context, "type"))) 
						isChangeConnected = isConnectedtoCA(context,sPartObjId,sChangeObjId, strChangeRelNew);
					else
						isChangeConnected = isConnectedtoChangeProcess(context,sPartObjId,sChangeObjId, null);
					}
					if(sPlantObjID != null)
						isPlantChangeConnected = isConnectedtoChangeProcess(context,sPartObjId,sPlantObjID, null);
  
					if(isChangeConnected.equalsIgnoreCase("true") || isPlantChangeConnected.equalsIgnoreCase("true")) {
						String relName_2 = RELATIONSHIP_PROPOSED_MARKUP;
			
								
									relName_2 = RELATIONSHIP_APPLIED_MARKUP;
									String relName_1 = RELATIONSHIP_AFFECTED_ITEM;
									isChangeConnected = isConnectedtoChangeProcess(context,sPartObjId,sChangeObjId, null);
									if("false".equals(isChangeConnected))
									 DomainRelationship.connect(context,
		                                     new DomainObject(sChangeObjId),
											   relName_1,
											   new DomainObject(sPartObjId));
	                  
									DomainRelationship.connect(context,
														   new DomainObject(sChangeObjId),
														   relName_2,
	                                                   new DomainObject(snewMarkupId));
	                     

	   
					}
					generatePlantBOMMarkupXML(context,strArrParams,htPlantConflictBOMs);
				}
				if ("true".equals(hasBOMChanges)) {
					if(null!=ECChange && !"null".equals(ECChange) && !"".equals(ECChange)) {
						snewMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
						DomainObject sdomobj= new DomainObject();
						sdomobj.setId(snewMarkupId);
						sdomobj.setDescription(context,sDescription);

						int index = ECChange.indexOf('|');
						if(index == -1) {
							sChangeObjId = ECChange;
						} else {
							sChangeObjId = ECChange.substring(0, index);
						}

						DomainObject chgBOMObj = new DomainObject(sChangeObjId);
						sChgType = chgBOMObj.getInfo(context,DomainConstants.SELECT_TYPE);
						DomainRelationship.connect(context,new DomainObject(sPartObjId),RELATIONSHIP_EBOM_MARKUP,sdomobj);

						String isChangeConnected = isConnectedtoChangeProcess(context,sPartObjId,sChangeObjId, null);
						/* Default Relationship between ECR/DCR/ECO/MECO and Markup */
						String relName = RELATIONSHIP_AFFECTED_ITEM;

						if(TYPE_ECO.equalsIgnoreCase(sChgType) || TYPE_MECO.equalsIgnoreCase(sChgType)) {
							DomainRelationship.connect(context, chgBOMObj ,RELATIONSHIP_APPLIED_MARKUP,sdomobj);
						} else {
							DomainRelationship.connect(context,chgBOMObj,RELATIONSHIP_PROPOSED_MARKUP,sdomobj);
		}
						/* Connect Change-Part */
						if ("false".equals(isChangeConnected)) {
							DomainRelationship.connect(context,
													   chgBOMObj,
													   relName,
													   new DomainObject(sPartObjId));
		}
						strMarkupIds = snewMarkupId;
                strArrParams[1]=strMarkupIds;
						generateBOMMarkupXML(context,massUpdateAction,strMarkupIds,sPartObjId,htConflictBOMs);
					}//End of if(ECChange !=...
				}//End of if(hasBOMChanges.equals...
            }
            // Single Plant BOM Markup with SaveAs opetion
            // Creates a  new Markup and Leaves the Old Markup Untouched
			else {
				if ("true".equals(hasPlantChanges)) {
					if((null!=PlantChange && !"null".equals(PlantChange) && !"".equals(PlantChange)) || (null!=ECChange && !"null".equals(ECChange) && !"".equals(ECChange))) {
                snewMarkupId=FrameworkUtil.autoName(context,"type_PlantBOMMarkup","policy_PartMarkup");
                DomainObject sdomobj= new DomainObject();
                sdomobj.setId(snewMarkupId);
                sdomobj.setDescription(context,sDescription);
                sdomobj.setAttributeValue(context,ATTRIBUTE_PLANT_ID,strPlantId);
                DomainRelationship.connect(context,new DomainObject(sPartObjId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(snewMarkupId));

						int index = 0;
						if(null!=PlantChange && !"null".equals(PlantChange) && !"".equals(PlantChange)) {
							index = PlantChange.indexOf('|');
							if(index == -1) {
								sChangeObjId = PlantChange;
							} else {
								sChangeObjId = PlantChange.substring(0, index);
							}
						} else {
							index = ECChange.indexOf('|');
							if(index == -1) {
								sChangeObjId = ECChange;
							} else {
								sChangeObjId = ECChange.substring(0, index);
							}
                }

						DomainObject chgPlantBOMObj = new DomainObject(sChangeObjId);
						sChgType = chgPlantBOMObj.getInfo(context,DomainConstants.SELECT_TYPE);
						if(TYPE_ECO.equalsIgnoreCase(sChgType) || TYPE_MECO.equalsIgnoreCase(sChgType)) {
							DomainRelationship.connect(context, chgPlantBOMObj ,RELATIONSHIP_APPLIED_MARKUP,sdomobj);
						} else {
							DomainRelationship.connect(context,chgPlantBOMObj,RELATIONSHIP_PROPOSED_MARKUP,sdomobj);
                }
						strMarkupIds = snewMarkupId;
						strArrParams[1] = strMarkupIds;
                		generatePlantBOMMarkupXML(context,strArrParams,htPlantConflictBOMs);
            }
        }
				if("true".equals(hasBOMChanges)) {
					if(null!=ECChange && !"null".equals(ECChange) && !"".equals(ECChange)) {
						snewMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
						DomainObject sdomobj= new DomainObject();
						sdomobj.setId(snewMarkupId);
						sdomobj.setDescription(context,sDescription);

						int index = ECChange.indexOf('|');
						if(index == -1) {
							sChangeObjId = ECChange;
						} else {
							sChangeObjId = ECChange.substring(0, index);
						}

						DomainObject chgBOMObj = new DomainObject(sChangeObjId);
						sChgType = chgBOMObj.getInfo(context,DomainConstants.SELECT_TYPE);
						DomainRelationship.connect(context,new DomainObject(sPartObjId),RELATIONSHIP_EBOM_MARKUP,sdomobj);

						String isChangeConnected = isConnectedtoChangeProcess(context,sPartObjId,sChangeObjId, null);
						/* Default Relationship between ECR/DCR/ECO/MECO and Markup */
						String relName = RELATIONSHIP_AFFECTED_ITEM;

						if(TYPE_ECO.equalsIgnoreCase(sChgType) || TYPE_MECO.equalsIgnoreCase(sChgType)) {
							DomainRelationship.connect(context, chgBOMObj ,RELATIONSHIP_APPLIED_MARKUP,sdomobj);
						} else {
							DomainRelationship.connect(context,chgBOMObj,RELATIONSHIP_PROPOSED_MARKUP,sdomobj);
						}
						/* Connect Change-Part */
						if ("false".equals(isChangeConnected)) {
							DomainRelationship.connect(context,
													   chgBOMObj,
													   relName,
													   new DomainObject(sPartObjId));
						}
						strMarkupIds = snewMarkupId;
						strArrParams[1] = strMarkupIds;
						generateBOMMarkupXML(context,massUpdateAction,strMarkupIds,sPartObjId,htConflictBOMs);
					}
				} // End Of if(hasBOMChanges.equals("true"))
			}
		}
        // If More than One Markup is Opened then Delete the Other Markups and Create a new Markup with Auto Name
		else {
			/* Variables To Check Singleton Markup Type Status */
			StringList strBOMMarkupIdList = new StringList();
			StringList strPlantBOMMarkupIdList = new StringList();
			boolean isSingletonBOMMarkup = false;
			boolean isSingletonPlantBOMMarkup = false;
			for(int i= 0 ; i < intMarkupIdSize ; i++) {
				String strMarkupId = (String) strlMarkupIds.get(i);
				DomainObject markupObject = new DomainObject(strMarkupId);
				String type = markupObject.getInfo(context, SELECT_TYPE);
				if(type.equals(TYPE_PLANT_BOM_MARKUP)) {
					strPlantBOMMarkupIdList.add(strMarkupId);
				} else {
					strBOMMarkupIdList.add(strMarkupId);
				}
			}

			if(strBOMMarkupIdList.size() == 1 && "Save".equalsIgnoreCase(sCommandType)) {
				isSingletonBOMMarkup = true;
			}
			if(strPlantBOMMarkupIdList.size() == 1 && "Save".equalsIgnoreCase(sCommandType)) {
				isSingletonPlantBOMMarkup = true;
			}

			if ("true".equals(hasPlantChanges)) {
				if(isSingletonPlantBOMMarkup) {
					strArrParams[1] = (String) strPlantBOMMarkupIdList.get(0);
					generatePlantBOMMarkupXML(context,strArrParams,htPlantConflictBOMs);
				} else {
				if((null!=PlantChange && !"null".equals(PlantChange) && !"".equals(PlantChange)) || (null!=ECChange && !"null".equals(ECChange) && !"".equals(ECChange))) {
            snewMarkupId=FrameworkUtil.autoName(context,"type_PlantBOMMarkup","policy_PartMarkup");
            DomainObject sdomobj= new DomainObject();
            sdomobj.setId(snewMarkupId);
            sdomobj.setDescription(context,sDescription);
			sdomobj.setAttributeValue(context,ATTRIBUTE_PLANT_ID,strPlantId);
            DomainRelationship.connect(context,new DomainObject(sPartObjId),RELATIONSHIP_EBOM_MARKUP,new DomainObject(snewMarkupId));

					int index = 0;
					if(null!=PlantChange && !"null".equals(PlantChange) && !"".equals(PlantChange)) {
						index = PlantChange.indexOf('|');
						if(index == -1) {
							sChangeObjId = PlantChange;
						} else {
							sChangeObjId = PlantChange.substring(0, index);
						}
					} else {
						index = ECChange.indexOf('|');
						if(index == -1) {
							sChangeObjId = ECChange;
						} else {
							sChangeObjId = ECChange.substring(0, index);
						}
            }

					DomainObject chgPlantBOMObj = new DomainObject(sChangeObjId);
					sChgType = chgPlantBOMObj.getInfo(context,DomainConstants.SELECT_TYPE);

					if(TYPE_ECO.equalsIgnoreCase(sChgType) || TYPE_MECO.equalsIgnoreCase(sChgType)) {
						DomainRelationship.connect(context, chgPlantBOMObj ,RELATIONSHIP_APPLIED_MARKUP,sdomobj);
					} else {
						DomainRelationship.connect(context,chgPlantBOMObj,RELATIONSHIP_PROPOSED_MARKUP,sdomobj);
            }
					strMarkupIds = snewMarkupId;
					strArrParams[1] = strMarkupIds;
						generatePlantBOMMarkupXML(context,strArrParams,htPlantConflictBOMs);
					}
				}
			}

			if ("true".equals(hasBOMChanges)) {
				if(isSingletonBOMMarkup) {
					strArrParams[1] = (String) strBOMMarkupIdList.get(0);
					generateBOMMarkupXML(context,massUpdateAction,strArrParams[1],sPartObjId,htConflictBOMs);
				} else {
				if(null!=ECChange && !"null".equals(ECChange) && !"".equals(ECChange)) {//083113V6R2012 --  checked also for empty condition
					snewMarkupId=FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
					DomainObject sdomobj= new DomainObject();
					sdomobj.setId(snewMarkupId);
					sdomobj.setDescription(context,sDescription);

					int index = ECChange.indexOf('|');
					if(index == -1) {
						sChangeObjId = ECChange;
					} else {
						sChangeObjId = ECChange.substring(0, index);
					}

					DomainObject chgBOMObj = new DomainObject(sChangeObjId);
					sChgType = chgBOMObj.getInfo(context,DomainConstants.SELECT_TYPE);
					DomainRelationship.connect(context,new DomainObject(sPartObjId),RELATIONSHIP_EBOM_MARKUP,sdomobj);

					String isChangeConnected = isConnectedtoChangeProcess(context,sPartObjId,sChangeObjId, null);
					/* Default Relationship between ECR/DCR/ECO/MECO and Markup */
					String relName = RELATIONSHIP_AFFECTED_ITEM;

					if(TYPE_ECO.equalsIgnoreCase(sChgType) || TYPE_MECO.equalsIgnoreCase(sChgType)) {
						DomainRelationship.connect(context, chgBOMObj ,RELATIONSHIP_APPLIED_MARKUP,sdomobj);
					} else {
						DomainRelationship.connect(context,chgBOMObj,RELATIONSHIP_PROPOSED_MARKUP,sdomobj);
					}
					/* Connect Change-Part */
					if("false".equals(isChangeConnected)) {
						DomainRelationship.connect(context,
												   chgBOMObj,
												   relName,
												   new DomainObject(sPartObjId));
					}
					strMarkupIds = snewMarkupId;
					strArrParams[1] = strMarkupIds;
					generateBOMMarkupXML(context,massUpdateAction,strMarkupIds,sPartObjId,htConflictBOMs);
				}
				}
			} // End Of if(hasBOMChanges.equals("true"))


			if("Save".equalsIgnoreCase(sCommandType)) {
                /* Delete BOM and Plant BOM Markups */
                if(!isSingletonBOMMarkup) {
					Iterator itrstrlMarupIds = strBOMMarkupIdList.iterator();
					while(itrstrlMarupIds.hasNext()) {
						String markupid=(String)itrstrlMarupIds.next();
						DomainObject domk=new DomainObject();
						domk.setId(markupid);
						domk.deleteObject(context);
					}
				}
				if (!isSingletonPlantBOMMarkup) {
					Iterator itrstrlMarupIds = strPlantBOMMarkupIdList.iterator();
				while(itrstrlMarupIds.hasNext()) {
                    String markupid=(String)itrstrlMarupIds.next();
                    DomainObject domk=new DomainObject();
                    domk.setId(markupid);
                    domk.deleteObject(context);
                }
            }
            }
          // End of Else loop for Multiple Selection of Markup
        }


        ContextUtil.popContext(context);
    }

    public void generatePlantBOMMarkupXML(Context context,String[] strArrParams,Hashtable htConflictBOMs)
    	throws Exception
    {
		generatePlantBOMMarkupXML(context,strArrParams);
		String strMarkupId = strArrParams[1];
		DomainObject doMarkup = new DomainObject(strMarkupId);
		//Code to append conflict xml tags
		Element eleRoot = null;
		if (eleDocRoot != null)
			eleRoot = eleDocRoot;

		Element fromRelationshipListElement=eleRoot.getChild("businessObject").getChild("fromRelationshipList");

		// Contains Conflict
		if (!htConflictBOMs.isEmpty())
		{
	 	  Enumeration enumkeysitr=htConflictBOMs.keys();
		  while(enumkeysitr.hasMoreElements())
		  {
			  String strKey=(String)enumkeysitr.nextElement();
			  ArrayList arlConflicts=(ArrayList)htConflictBOMs.get(strKey);
			  Iterator itr=arlConflicts.iterator();
			  while(itr.hasNext())
			  {
				  Element eleRe=(Element)itr.next();
				  fromRelationshipListElement.addContent(eleRe.detach());
			  }
		   }
		}
		chekinToMarkupObject(context,eleRoot,doMarkup);
	}

    /*
     * This method used to send the strcture browser xml to the Structure Browser
     * to show the Plant Item Markup changes
     * @param context, args
     * @return void
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
   @com.matrixone.apps.framework.ui.PreProcessCallable
   public HashMap sendPIMXML(Context context, String args[]) throws Exception
   {
       HashMap retPIMAction    = new HashMap();
       MapList mapListUpdated  = new MapList();
       try{
           HashMap programMap  = (HashMap)JPO.unpackArgs(args);
           HashMap tableData   = (HashMap) programMap.get("tableData");
           MapList ObjectList  = (MapList) tableData.get("ObjectList");
           HashMap requestMap  = (HashMap)programMap.get("requestMap");
           String strMarkupXML = (String) requestMap.get("massUpdateAction");
           String strFiltrObjs = (String) requestMap.get("filterObjects");
           StringList slFiltrList = FrameworkUtil.split(strFiltrObjs, "|");

           Iterator itrObjects = ObjectList.iterator();
           while (itrObjects.hasNext())
           {
               Map mapObj = (Map) itrObjects.next();
               String sRelId = (String)mapObj.get("id[connection]");
               if(slFiltrList.contains(sRelId)){
                   mapListUpdated.add(mapObj);
               }
           }

           retPIMAction.put ("Action", "execScript");
		   retPIMAction.put ("Message","{ main:function()  { loadMarkUpXML('" + strMarkupXML +"', \"true\");  }}");
           retPIMAction.put ("ObjectList", mapListUpdated);
       }catch(Exception e){
           throw e;
       }
       return retPIMAction;
   }

    /**
     * Checks if the current selected Part is Development Part.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds fieldMap to get policy name.
     * @return Boolean true if selected Policy is route based otherwise returns false.
     * @throws Exception if the operation fails.
     * @since BX3.
     */

     public static Boolean isNotDevelopmentPart(Context context, String[] args)
          throws Exception
     {
         boolean isNotDevPart = true;
		HashMap programMap=(HashMap)JPO.unpackArgs(args);
		String sPartObjId=(String)programMap.get("sObjId");

		DomainObject doObject = new DomainObject(sPartObjId);

		String strPolicy = doObject.getInfo(context, DomainConstants.SELECT_POLICY);

		if ((DomainConstants.POLICY_DEVELOPMENT_PART).equals(strPolicy))
		{
			isNotDevPart = false;
		}
         return Boolean.valueOf(isNotDevPart);
     }

    /*
     * This method is used to retreive BOMs based on each Plant
     * @param context, stringlist with plant bom ids
     * @return void
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
   static Map getPlantBasedBOMs(Context context, StringList strlPlantBOMsList)
   	throws Exception {
		Map mpPlantBasedBOMs = new HashMap();
		int iSize = strlPlantBOMsList.size();
		String strMarkupId = null;
		DomainObject doMarkup = new DomainObject();
		String strPlantId = null;
		StringList strlTempList = null;

		for(int i=0;i<iSize;i++) {
			strMarkupId = (String)strlPlantBOMsList.get(i);
			doMarkup.setId(strMarkupId);
			strPlantId = doMarkup.getInfo(context,"attribute["+ATTRIBUTE_PLANT_ID+"]");
			strlTempList = new StringList();
			if(mpPlantBasedBOMs.containsKey(strPlantId)) {
				strlTempList = (StringList)mpPlantBasedBOMs.get(strPlantId);
				strlTempList.add(strMarkupId);
				mpPlantBasedBOMs.put(strPlantId,strlTempList);
			} else {
				strlTempList = new StringList(strMarkupId);
				mpPlantBasedBOMs.put(strPlantId,strlTempList);
			}
		}
		return mpPlantBasedBOMs;
	}

    /*
     * This method is used check for conflict within Markups
     * @param context, Hashtable containing the conflict BOM relationships
     * @return void
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
	public Hashtable getAttributeLevelMerge (Context context, Hashtable htConflictBOMs)
	 throws Exception {
		Hashtable htResult = new Hashtable(2);
		HashMap hmpConflictAttributes = null;
		HashMap hmpAttributes = null;
		Element eleTemp = null;
		Element eleNew = null;
		Element eleAttrTemp = null;
		Element eleNewAttributeList = null;
		String strPrevAttrVal = null;

		List lstBOMs = null;
		Enumeration enumKeys = htConflictBOMs.keys();
		Set setKeys = null;
		int iSize = 0;
		List listConflictFromAttributes = null;
		Iterator itrConflictAttributes = null;
		String strAttrName = null;
		String strAttrValue = null;
		com.matrixone.jdom.Attribute attrConflictChgType = null;
		boolean blnSkipKey = false;
		Element eleAttrList = null;
		while (enumKeys.hasMoreElements())
		{
			blnSkipKey = false;
			String strKey1 = (String) enumKeys.nextElement();
			hmpConflictAttributes = new HashMap();
			hmpAttributes = new HashMap();
			lstBOMs = (List) htConflictBOMs.get(strKey1);
			iSize = lstBOMs.size();
			//for each element
			for(int i=0;i < iSize && !blnSkipKey; i++) {
				eleTemp = (Element)lstBOMs.get(i);
				eleAttrList = eleTemp.getChild("attributeList");
				listConflictFromAttributes = eleAttrList.getChildren();
				itrConflictAttributes = listConflictFromAttributes.iterator();
				//for each attribute
				while (itrConflictAttributes.hasNext())
				{
					Element eleConflictAttribute = (Element) itrConflictAttributes.next();
					strAttrName = eleConflictAttribute.getChild("name").getText();
					attrConflictChgType = eleConflictAttribute.getAttribute("chgtype");
					if (attrConflictChgType != null)
					{
						strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
						if (hmpAttributes.containsKey(strAttrName))
						{
							strPrevAttrVal = ((Element)hmpAttributes.get(strAttrName)).getChildText("newvalue");
							if (!strAttrValue.equals(strPrevAttrVal))
							{
								hmpConflictAttributes.put(strAttrName, "TRUE");
								blnSkipKey = true;
								break;
							}
						}
						else
						{
							hmpAttributes.put(strAttrName, eleConflictAttribute);
						}
					}
				}
				//Can create a new relationship
				if(!blnSkipKey) {
					if (eleTemp!=null) {
						setKeys = hmpAttributes.keySet();
						Iterator itrKeys = setKeys.iterator();
						eleNew = (Element)eleTemp.clone();
						eleNewAttributeList = eleNew.getChild("attributeList");
						eleNewAttributeList.removeChildren("attribute");
						while(itrKeys.hasNext()) {
							eleAttrTemp = (Element)hmpAttributes.get(itrKeys.next());
							eleNewAttributeList.addContent((Element)eleAttrTemp.clone());
						}
					}
				} else {
					//atleast one attribute level conflict exists.
					htResult.put("result", Boolean.FALSE);
					return htResult;
				}
			}
			htResult.put(strKey1,eleNew);
		}
		return htResult;
	}

	 /**
	 *  Method For Access Function to display Name field in Create/Save Markup webform
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return        Boolean
	 * @throws        Exception if the operation fails
	 * @since         EngineeringCentral X3
     **/
	public static Boolean isNameDisplayAllowed(Context context, String args[])
    	throws Exception {
		Boolean status=Boolean.TRUE;
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String sType = (String)paramMap.get("commandType");
		if(sType==null || sType.equalsIgnoreCase("Save"))
			status = Boolean.FALSE;
		else {
			String process = (String)paramMap.get("process");
			if(process != null) {
				status = Boolean.FALSE;
			}
		}
		return status;
    }

     /**
	 *  Method For Access Function to display Engineering Change field in Create/Save Markup webform
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return        Boolean
	 * @throws        Exception if the operation fails
	 * @since         EngineeringCentral X3
     **/

    public static Boolean hasBOMChanges(Context context, String args[])
    	throws Exception {
	    boolean status = false;
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String sSelectedPart=(String)paramMap.get("sSelPart");
		if(!"DevPart".equalsIgnoreCase(sSelectedPart)) {
		   status = true;
		}

	    return Boolean.valueOf(status);
    }

/**
	 *  Method For Access Function to display Manufacturing Change field in Create/Save Markup webform
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return        Boolean
	 * @throws        Exception if the operation fails
	 * @since         EngineeringCentral X3
     **/

    public static Boolean hasPlantChanges(Context context, String args[])
		throws Exception {
		boolean status= false;
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String hasPlantChanges = (String)paramMap.get("hasPlantChanges");
		String isPartReleased = (String)paramMap.get("isPartReleased");
		String isFirstRevision = (String)paramMap.get("isFirstRevision");
		String sSelectedPart=(String)paramMap.get("sSelPart");
		if(!"DevPart".equalsIgnoreCase(sSelectedPart) &&
		   hasPlantChanges.equalsIgnoreCase("true") &&
		   ((isPartReleased.equalsIgnoreCase("true")&&isFirstRevision.equalsIgnoreCase("true"))||
		    ((isPartReleased.equalsIgnoreCase("true") || isPartReleased.equalsIgnoreCase("false"))&&isFirstRevision.equalsIgnoreCase("false"))))
			status = true;
		return Boolean.valueOf(status);
    }

    /**
	 *  Method To Display Engineering Change field in Create/Save Markup webform
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return        String
	 * @throws        Exception if the operation fails
	 * @since         EngineeringCentral X3
     **/

    public String getECChange(Context context,String[] args)
    	throws Exception {
		HashMap programMap=(HashMap)JPO.unpackArgs(args);
		HashMap requestMap=(HashMap)programMap.get("requestMap");
		String isPartReleased = (String)requestMap.get("isPartReleased");
		String txtType = "type_ECR,type_ECO";

		//Multitenant
		String toolTip = EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", context.getLocale(),"emxMBOM.Markup.Select_1");
		if ("true".equals(isPartReleased)) {
			txtType+=",type_DCR";
			//Multitenant
			toolTip = EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", context.getLocale(),"emxMBOM.Markup.Select_2");
		}

		StringBuffer builder = new StringBuffer(128);

		String process = (String)requestMap.get("process");

		boolean showChooser = false;
		DomainObject selectedChange = null;
		String selectedChangeId = null;

		if(process == null) {
			showChooser = true;
		} else {
			String allMarkupIds = (String)requestMap.get("strMarkupIds");
			if(allMarkupIds.indexOf(',') == -1) {
				DomainObject markup = new DomainObject(allMarkupIds);
				selectedChangeId = markup.getInfo(context, "to["+RELATIONSHIP_PROPOSED_MARKUP+"].from.id");
				if(selectedChangeId == null)
					selectedChangeId = markup.getInfo(context, "to["+RELATIONSHIP_APPLIED_MARKUP+"].from.id");
				selectedChange = new DomainObject(selectedChangeId);
				if (! (selectedChange.isKindOf(context, DomainConstants.TYPE_ECO) || selectedChange.isKindOf(context, DomainConstants.TYPE_ECR))) {
					showChooser = true;
				}
			} else {
				selectedChangeId = (String)requestMap.get("sChangeObjId");
			}
			selectedChange = new DomainObject(selectedChangeId);
		}

		if(showChooser) {
			builder.append("<script>");
			builder.append("function clearECChange() {");
			builder.append("var f = document.editDataForm;");
			builder.append("f.ECChange.value = \"\";");
			builder.append("f.ECChangeDisplay.value = \"\";");
			builder.append('}');
			builder.append("</script>");
			builder.append("<input type=\"text\" name=\"ECChangeDisplay\" value=\"\" id=\"ECChangeDisplay\" title=\""+toolTip+"\" readonly=\"readonly\">");
			builder.append("<input type=\"hidden\" name=\"ECChange\" id=\"ECChange\" value=\"\">");
			builder.append("<input type=\"button\" name=\"btnECChange\" id=\"btnECChange\" value=\"...\" onclick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES="+txtType+"&selection=single&excludeOIDprogram=emxPartMarkup:excludeChanges&isPartReleased="+isPartReleased+"&table=MBOMChangeSearchResults&hideHeader=true&submitURL=../manufacturingchange/emxMBOMSelectChange.jsp&suiteKey=Framework&StringResourceFileId=emxFrameworkStringResource&SuiteDirectory=common&formName=editDataForm&frameName=pageContent&fieldNameActual=ECChange&fieldNameDisplay=ECChangeDisplay&HelpMarker=emxhelpfullsearch','850','630')\">");
			builder.append("<a href=\"JavaScript:clearECChange()\">Clear</a>");
		} else {
			String selectedChangeName = selectedChange.getInfo(context, SELECT_NAME);
			builder.append(selectedChangeName);
			String value = selectedChangeId + "|" + "connected";
			builder.append("<input type=\"hidden\" name=\"ECChange\" id=\"ECChange\" value=\""+value+"\">");
		}
		return builder.toString();
	}

	/**
	     *  Method To Display Manufacturing Change field in Create/Save Markup webform
	     *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args
	     * @return        String
	     * @throws        Exception if the operation fails
	     * @since         EngineeringCentral X3
     **/

	public String getPlantChange(Context context,String[] args)
		throws Exception {
		String txtType = "type_DCR,type_MECO";

		//Multitenant
		String toolTip = EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", context.getLocale(),"emxMBOM.Markup.Select_3");
		StringBuffer builder = new StringBuffer(128);

		Map programMap = (Map)JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
		String process = (String)requestMap.get("process");

		boolean showChooser = false;
		DomainObject selectedChange = null;
		String selectedChangeId = null;

		if(process == null) {
			showChooser = true;
		} else {
			String allMarkupIds = (String)requestMap.get("strMarkupIds");
			if(allMarkupIds.indexOf(',') == -1) {
				DomainObject markup = new DomainObject(allMarkupIds);
				selectedChangeId = markup.getInfo(context, "to["+RELATIONSHIP_PROPOSED_MARKUP+"].from.id");
				if(selectedChangeId == null)
					selectedChangeId = markup.getInfo(context, "to["+RELATIONSHIP_APPLIED_MARKUP+"].from.id");
			} else {
				selectedChangeId = (String)requestMap.get("sChangeObjId");
			}
			selectedChange = new DomainObject(selectedChangeId);
		}
		if(showChooser) {
			builder.append("<script>");
			builder.append("function clearPlantChange() {");
			builder.append("var f = document.editDataForm;");
			builder.append("f.PlantChange.value = \"\";");
			builder.append("f.PlantChangeDisplay.value = \"\";");
			builder.append('}');
			builder.append("</script>");
			builder.append("<input type=\"text\" name=\"PlantChangeDisplay\" value=\"\" id=\"PlantChangeDisplay\" title=\""+toolTip+"\" readonly=\"readonly\">");
			builder.append("<input type=\"hidden\" name=\"PlantChange\" id=\"PlantChange\" value=\"\">");
			builder.append("<input type=\"button\" name=\"btnPlantChange\" id=\"btnPlantChange\" value=\"...\" onclick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES="+txtType+"&selection=single&excludeOIDprogram=emxPartMarkup:excludeChanges&table=MBOMChangeSearchResults&hideHeader=true&submitURL=../manufacturingchange/emxMBOMSelectChange.jsp&suiteKey=Framework&StringResourceFileId=emxFrameworkStringResource&SuiteDirectory=common&formName=editDataForm&frameName=pageContent&fieldNameActual=PlantChange&fieldNameDisplay=PlantChangeDisplay&HelpMarker=emxhelpfullsearch','850','630')\">");
			builder.append("<a href=\"JavaScript:clearPlantChange()\">Clear</a>");
		} else {
			String selectedChangeName = selectedChange.getInfo(context, SELECT_NAME);
			builder.append(selectedChangeName);
			String value = selectedChangeId + "|" + "connected";
			builder.append("<input type=\"hidden\" name=\"PlantChange\" id=\"PlantChange\" value=\""+value+"\">");
		}

		return builder.toString();
	}

    /**
     *  Method To exclude Change object Ids from search list
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args
     * @return        StringList
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/


	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeChanges(Context context, String[] args)
		throws Exception {

		Map programMap = (Map)JPO.unpackArgs(args);
		String txtType = (String)programMap.get("field=TYPES=");
		String isPartReleased = (String)programMap.get("isPartReleased");

		StringList objectSelects = new StringList(SELECT_ID);
			StringBuffer whereExpression = new StringBuffer(128);
			whereExpression.append('(');
			whereExpression.append("(current=='");
			whereExpression.append(ECR_STATE_PLAN_ECO);
			whereExpression.append("')");
			whereExpression.append(" || ");
			whereExpression.append("(current=='");
			whereExpression.append(STATE_PLAN_DCO);
			whereExpression.append("')");
			whereExpression.append(" || ");
			whereExpression.append("(current=='");
			whereExpression.append(ECR_STATE_COMPLETE);
			whereExpression.append("')");
			whereExpression.append(" || ");
			whereExpression.append("(current=='");
			whereExpression.append(ECO_STATE_CANCELLED);
			whereExpression.append("')");
			whereExpression.append(" || ");
			whereExpression.append("(current=='");
			whereExpression.append(STATE_PART_RELEASE);
			whereExpression.append("')");
			whereExpression.append(" || ");
			whereExpression.append("(current=='");
			whereExpression.append(ECO_STATE_IMPLEMENTED);
			whereExpression.append("')");
			whereExpression.append(" || ");
			whereExpression.append("(current=='");
			whereExpression.append(rejectedState);
			whereExpression.append("'))");

		MapList excludedChangeMapList = DomainObject.findObjects(context,
			                                                         txtType,
		                                  					     QUERY_WILDCARD,
			                                                         whereExpression.toString(),
		                                                         objectSelects);

			StringList excludedChangeStringList = new StringList();
		int excludedChangeMapListSize = excludedChangeMapList.size();
		for(int i = 0; i < excludedChangeMapListSize; i++) {
			String excludedOID = (String)((Map) excludedChangeMapList.get(i)).get(SELECT_ID);
				excludedChangeStringList.add(excludedOID);
			}

			if(isPartReleased != null && isPartReleased.equals("false")) {
				String where = "(type=='"+TYPE_DCR+"')";
				excludedChangeMapList = DomainObject.findObjects(context,
						                                         txtType,
						                                  		 QUERY_WILDCARD,
						                                         where,
			                                                     objectSelects);
				excludedChangeMapListSize = excludedChangeMapList.size();
				for(int i = 0; i < excludedChangeMapListSize; i++) {
					String excludedOID = (String)((Map) excludedChangeMapList.get(i)).get(SELECT_ID);
				if(!excludedChangeStringList.contains(excludedOID))
					excludedChangeStringList.add(excludedOID);
			}
		}
		return excludedChangeStringList;
	}



    /**
     *  Method To check if BOM changes are made
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    Context, Relationship type, rel id, Element tag with column names, type of change
     * @return        integer value indicating whether or not BOM changes are present
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
	private int checkForBOMChanges(Context context, String sRelType, String strRelId, com.matrixone.jdom.Element element, String changeType)
    	throws Exception {
		int retVal = 0;
		boolean tempBOMVal = false;
		boolean tempMBOMVal = false;
		if(sRelType != null) {
			if(sRelType.indexOf('|') != -1) {
				java.util.StringTokenizer token = new java.util.StringTokenizer(sRelType,"|");
				while(token.hasMoreTokens()) {
					sRelType = token.nextToken();
				}
			}
			sRelType = PropertyUtil.getSchemaProperty(context, sRelType);
		} else {
			DomainRelationship domRel = new DomainRelationship(strRelId);
			domRel.open(context);
			sRelType = domRel.getTypeName();
			domRel.close(context);
		}

		/* Get Editable Table Column Names For BOM Changes */
		String strBOMColumns = (String)EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.View.BOMChangesAllowed");
		StringList listBOMColumn = FrameworkUtil.split(strBOMColumns,"|");

		if(sRelType.equals(RELATIONSHIP_EBOM)) {
			if("change".equals(changeType)) {
				java.util.List columnList = element.getChildren("column");
				Iterator columnItr = columnList.iterator();
				while(columnItr.hasNext()) {
					com.matrixone.jdom.Element columnElement = (com.matrixone.jdom.Element) columnItr.next();
					String name = columnElement.getAttributeValue("name");
					if(listBOMColumn.contains(name)) {
						tempBOMVal = true;
					} else {
						tempMBOMVal = true;
					}
				}
			} else {
				tempBOMVal = true;
			}
		} else {
			tempMBOMVal =  true;
		}

		if(tempBOMVal && !tempMBOMVal)
			retVal = 0;
		else if (!tempBOMVal && tempMBOMVal)
			retVal = 1;
		else if(tempBOMVal && tempMBOMVal) {
			retVal = 2;
		}
		return retVal;
	}

    /*
     * This method is used to exclude Persons from Search results.
     * @param context, args
     * @return StringList of objectIds od exlcluded Person objects
     * @throws Exception if the operation fails
     * @since EC X+3
     */
    public StringList excludePersonSearch(Context context , String[] args) throws Exception
       {
           MapList personlist = new MapList();
           StringList totalPersonList = new StringList();
           StringList ecoPersonList = new StringList();
           StringList excludePersonList = new StringList();
            try {
                 MapList companylist = null;
                 HashMap programMap = (HashMap) JPO.unpackArgs(args);
                 String objectID = (String)programMap.get("objectId");
                 DomainObject domobj = new DomainObject(objectID);
                 StringList selectStmts  = new StringList(2);
                 selectStmts.addElement(SELECT_ID);
                 selectStmts.addElement(SELECT_NAME);
                 companylist= domobj.getRelatedObjects( context,
                                                  RELATIONSHIP_CHANGE_RESPONSIBILITY + "," + RELATIONSHIP_DESIGN_RESPONSIBILITY,     // relationship pattern
                                                  TYPE_ORGANIZATION,                // object pattern
                                                  selectStmts,                 // object selects
                                                  null,                        // relationship selects
                                                  true,                       // to direction
                                                  false,                        // from direction
                                                  (short) 0,                   // recursion level
                                                  null,                        // object where clause
                                                  null,
                                                  0); //limit 0 to return all the data available.
                int size = companylist.size();
                String strrel = RELATIONSHIP_EMPLOYEE_REPRESENTATIVE+","+RELATIONSHIP_MEMBER;
                for (int i=0;i<size;i++)  {
                    Map thismap = (Map)companylist.get(i);
                    String compnyId = (String)thismap.get("id");
                    DomainObject domObj = new DomainObject(compnyId);
                    StringList selStmts  = new StringList(2);
                    selStmts.addElement(SELECT_ID);
                    selStmts.addElement(SELECT_NAME);
                    personlist = domObj.getRelatedObjects( context,
                                                  strrel,             // relationship pattern
                                                  TYPE_PERSON,        // object pattern
                                                  selStmts,           // object selects
                                                  null,               // relationship selects
                                                  false,              // to direction
                                                  true,               // from direction
                                                  (short) 0,          // recursion level
                                                  null,               // object where clause
                                                  null,
                                                  0); //limit 0 to return all the data available.
                    for(int cnt=0;cnt<personlist.size();cnt++)
                    {
                        Map personMap = (Map) personlist.get(cnt);
                        String id = (String) personMap.get(SELECT_ID);
                        ecoPersonList.add(id);
                    }
            }
            StringList selects = new StringList(1);
            selects.add(SELECT_ID);
            //Get all the Persons
            MapList persons =  DomainObject.findObjects(context, TYPE_PERSON, "*", "*","*", "*",
                null, null, true, selects, (short) 0);
            int personSize = persons.size();
            for(int i=0;i<personSize;i++)
            {
                Map totalPersonMap = (Map) persons.get(i);
                String personId = (String) totalPersonMap.get(SELECT_ID);
                totalPersonList.add(personId);
            }
            personSize = totalPersonList.size();
            for(int j=0;j<personSize;j++)
            {
                String personId = (String) totalPersonList.get(j);
                if(!ecoPersonList.contains(personId))
                {
                    excludePersonList.add(personId);
                }
            }

           }
           catch (Exception e) {
              throw e;
              }
           return excludePersonList;
       }
    /**
    updateManuResp to update the  Manufacturing Responsibility relationship
  * @param context Context : User's Context.
  * @param  sPartId  String value contains  Part Obect Id
  * @param  strArrLocIds  String Array value contains  Location object ids
  * @param  strArrERPStatus  String Array value contains  ERP Status values
  * @param  strArrLeadPlants  String Array value contains  Lead Plant value
  * @param  strArrECOs  String Array value contains  ECO Name
  * @param  strArrECOIds  String Array value contains  ECO Object Id
  * @return String
  * @throws FrameworkException
 */

    public void updateManuResp(Context context,String sPartId,String[]strArrLocIds,String []strArrERPStatus, String []strArrLeadPlants, String strArrECOs, String strArrECOIds)
    throws Exception
    {
        if(EngineeringUtil.isMBOMInstalled(context)) {
            Class clazz = Class.forName("com.matrixone.apps.mbom.PartMaster");
            IPartMaster partMaster = (IPartMaster) clazz.newInstance();
            partMaster.updateManuResp(context, sPartId,strArrLocIds,strArrERPStatus,  strArrLeadPlants, strArrECOs, strArrECOIds);
        }

    }

    public DomainRelationship addMR(Context context, String sPlantId, String sPartId, String sChangeId, Map mAttrMap) throws FrameworkException,Exception {
    	DomainRelationship doRel = null;
    	if(EngineeringUtil.isMBOMInstalled(context)) {
            Class clazz = Class.forName("com.matrixone.apps.mbom.PartMaster");
            IPartMaster partMaster = (IPartMaster) clazz.newInstance();
            doRel = partMaster.addMR(context, sPlantId, sPartId, sChangeId, mAttrMap);
        }
    	return doRel;
    }

    /**
       updateManuResp to update the  Manufacturing Responsibility relationship
     * @param context Context : User's Context.
     * @param  sPartId  String value contains  Part Obect Id
     * @param  strArrLocIds  String Array value contains  Location object ids
     * @param  strArrERPStatus  String Array value contains  ERP Status values
     * @param  strArrLeadPlants  String Array value contains  Lead Plant value
     * @param  strArrECOs  String Array value contains  ECO Name
     * @param  strArrECOIds  String Array value contains  ECO Object Id
     * @return String
     * @throws FrameworkException
    */

    public void updateManuResp(Context context,String strArrLocIds, String strArrLeadPlants)
    throws Exception
    {
        if(EngineeringUtil.isMBOMInstalled(context)) {
            Class clazz = Class.forName("com.matrixone.apps.mbom.PartMaster");
            IPartMaster partMaster = (IPartMaster) clazz.newInstance();
            partMaster.updateManuResp(context,strArrLocIds, strArrLeadPlants);
        }

    }
    /**
       isECOConnectedPartMaster check if ECO is connected to Part Master
     * @param context Context : User's Context.
     * @param  sPartId  String value contains  ECO Obect Id
     * @param  sPartMasterId  String value contains  Part Master object id
     * @return boolean
     * @throws Exception
    */
    public boolean isECOConnectedPartMaster(Context context,String sECOId, String sPartMasterId)
    throws Exception
    {
           boolean  bConnected = false;
           MapList mlECOList = null;
           Map mECOMap =null;
           String sObjId =null;
           try
           {
              DomainObject doPartMaster= DomainObject.newInstance(context);
              doPartMaster.setId(sPartMasterId);
              StringList busSelList = new StringList();
              busSelList.add(DomainObject.SELECT_ID);
              mlECOList= doPartMaster.getRelatedObjects(context,
                                                        RELATIONSHIP_MANUFACTURING_RESPONSIBILITY_CHANGE,
                                                        DomainConstants.TYPE_ECO,
                                                        busSelList,
                                                        null,
                                                        true,
                                                        false,
                                                        (short)1,
                                                        "",
                                                        "",
                                                        0, //limit 0 to return all the data available.
                                                        null,
                                                        null,
                                                        null);

              int size=0;
              if( (mlECOList != null) && (mlECOList.size()>0) )
                size=mlECOList.size();
              for(int i=0;i<size;i++)
              {
                mECOMap = (Map)mlECOList.get(i);
                sObjId = (String)mECOMap.get(DomainObject.SELECT_ID);
                if( (sObjId != null) && (!sObjId.equals(""))&& (sObjId.equals(sECOId)) )
                {
                    bConnected=true;
                    break;
                }

            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
        return bConnected;
    }


 /**
     * Method to check merged BOM Markups
     *
	 * Added for Bug 355092
     * @param context the eMatrix <code>Context</code> object
     * @param args   holds a Sting of object Ids
     * @return      merge status - true/false
     * @throws      Exception if the operation fails
     * @since       EngineeringCentral X3
     **/
    public static String checkBOMConflict(Context context, String[] args )
        throws Exception
   {
		Hashtable htConflictBOMs = new Hashtable();
        Hashtable htChangedBOMs = new Hashtable();
        Hashtable htNewChangedBOMs = new Hashtable();
        Hashtable htAddedBOMs = new Hashtable();
        Hashtable htDeletedBOMs = new Hashtable();
        Hashtable htUnChangedBOMs = new Hashtable();

        HashMap paramMap=(HashMap)JPO.unpackArgs(args);

      try{

        StringList strlMarkupIds=(StringList)paramMap.get("markupIds");

        String XMLFORMAT = PropertyUtil.getSchemaProperty(context, "format_XML");

        // create a temporary workspace directory
        String strTransPath = context.createWorkspace();

        java.io.File fEmatrixWebRoot = new java.io.File(strTransPath);

        Iterator itrMarkups = strlMarkupIds.iterator();

        DomainObject boMarkup = null;

        String strMarkupId = null;
        String strMarkupName = null;
        String strXMLFileName = null;

        com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder(false);
		builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
		builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder.setValidation(false);

        com.matrixone.jdom.Document docXML = null;

        Element eleRoot = null;
        Element eleRelatioship = null;
        Element eleObjectInfo = null;
        Element eleRelatioshipInfo = null;
        Element eleAttribute = null;

        Element eleConflictRelationship = null;

        List listFromRelationships = null;
        List listFromAttributes = null;

        Iterator itrRelationships = null;
        Iterator itrAttributes = null;

        com.matrixone.jdom.Attribute attrEBOMChgType = null;
        com.matrixone.jdom.Attribute attrChgType = null;

        String strPartType =  null;
        String strPartName =  null;
        String strPartRev  =  null;
        String strPartFN   =  null;
        String strPartRD   =  null;
        String strPartNewFN   =  null;
        String strPartNewRD   =  null;
        String strEBOMChgType  =  null;
        String strEBOMKey  =  null;

        while (itrMarkups.hasNext())
        {
            strMarkupId = (String) itrMarkups.next();

            boMarkup = new DomainObject(strMarkupId);

            strMarkupName =boMarkup.getInfo(context,DomainConstants.SELECT_NAME);

            strXMLFileName = strMarkupName + ".xml";

            boMarkup.checkoutFile(context, false, XMLFORMAT, strXMLFileName, strTransPath);

            java.io.File fMarkupXML = new java.io.File(fEmatrixWebRoot, strXMLFileName);

           if (fMarkupXML == null)
            {
                continue;
            }

            docXML = builder.build(fMarkupXML);
            eleRoot = docXML.getRootElement();

           //Added a condition for 375808
           String typeMarkUp = boMarkup.getInfo(context,DomainConstants.SELECT_TYPE);
           if((!TYPE_ITEMMARKUP.equals(typeMarkUp))&&(!TYPE_PLANT_ITEM_MARKUP.equals(typeMarkUp))) {
           listFromRelationships = eleRoot.getChild("businessObject").getChild("fromRelationshipList").getChildren();
           itrRelationships = listFromRelationships.iterator();

           boolean blnPlantMarkup = false;
           if(TYPE_PLANT_BOM_MARKUP.equals(boMarkup.getInfo(context,DomainConstants.SELECT_TYPE))) {
               blnPlantMarkup = true;
           }

            while (itrRelationships.hasNext())
            {
                eleRelatioship = (Element) itrRelationships.next();

                if(blnPlantMarkup) {

                    eleObjectInfo = eleRelatioship.getChild("fromRel").getChild("businessObjectRef");

                } else {

                    eleObjectInfo = eleRelatioship.getChild("relatedObject").getChild("businessObjectRef");
                }
                eleRelatioshipInfo = eleRelatioship.getChild("attributeList");

			   //IR-045004: Decoding the encoded type, name,rev
               strPartType = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectType").getText());
               strPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectName").getText());
               strPartRev = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectRevision").getText());

                attrEBOMChgType = eleRelatioship.getAttribute("chgtype");

                listFromAttributes = eleRelatioshipInfo.getChildren();
                itrAttributes = listFromAttributes.iterator();

                strEBOMChgType = null;

                if (attrEBOMChgType != null )
                {
                    strEBOMChgType = attrEBOMChgType.getValue();
                }

                if (strEBOMChgType == null || "A".equals(strEBOMChgType) || "D".equals(strEBOMChgType))
                {
                    while (itrAttributes.hasNext())
                    {
                        eleAttribute = (Element) itrAttributes.next();
                            //"Find Number"
                        if (DomainConstants.ATTRIBUTE_FIND_NUMBER.equals(eleAttribute.getChild("name").getText()))
                        {
                            strPartFN = eleAttribute.getChild("string").getText();
                        }
                        else if (DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR.equals(eleAttribute.getChild("name").getText()))
                        {

                            strPartRD = eleAttribute.getChild("string").getText();//"Reference Designator"
                        }
                    }
                }
                else
                {
                    while (itrAttributes.hasNext())
                    {
                        eleAttribute = (Element) itrAttributes.next();

                        attrChgType = eleAttribute.getAttribute("chgtype");

                        if (DomainConstants.ATTRIBUTE_FIND_NUMBER.equals(eleAttribute.getChild("name").getText()))
                        {
                            if (attrChgType != null)
                            {
                               strPartFN = eleAttribute.getChild("oldvalue").getText();
                                strPartNewFN = eleAttribute.getChild("newvalue").getText();
                            }
                            else
                            {
                                strPartFN = eleAttribute.getChild("string").getText();
                            }
                        }
                        else if (DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR.equals(eleAttribute.getChild("name").getText()))
                        {
                            if (attrChgType != null)
                            {

                               strPartRD = eleAttribute.getChild("oldvalue").getText();
                                strPartNewRD = eleAttribute.getChild("newvalue").getText();
                            }
                            else
                            {
                                strPartRD = eleAttribute.getChild("string").getText();
                            }
                        }
                    }
                }

                strEBOMKey = strPartType + "~" + strPartName + "~" + strPartRev + "~" + strPartFN + "~" + strPartRD;

                if (strEBOMChgType == null)
                {
                    if (!htDeletedBOMs.containsKey(strEBOMKey) && !htChangedBOMs.containsKey(strEBOMKey) && !htUnChangedBOMs.containsKey(strEBOMKey))
                    {
                        htUnChangedBOMs.put(strEBOMKey, eleRelatioship);
                    }
                }
                else if ("D".equals(strEBOMChgType))
                {
					if (htChangedBOMs.containsKey(strEBOMKey) || htAddedBOMs.containsKey(strEBOMKey) || htConflictBOMs.containsKey(strEBOMKey))
                    {
						if (htChangedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htChangedBOMs.get(strEBOMKey);
							htChangedBOMs.remove(strEBOMKey);
						}
						else if (htAddedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htAddedBOMs.get(strEBOMKey);
							htAddedBOMs.remove(strEBOMKey);
						}
						if (htConflictBOMs.containsKey(strEBOMKey))
						{
							ArrayList arlConflict = (ArrayList) htConflictBOMs.get(strEBOMKey);

							if (!arlConflict.contains(eleConflictRelationship))
							{
								arlConflict.add(eleConflictRelationship);
							}

							arlConflict.add(eleRelatioship);

							htConflictBOMs.remove(strEBOMKey);
							htConflictBOMs.put(strEBOMKey, arlConflict);
						}
						else
						{
							ArrayList arlConflict = new ArrayList();

							arlConflict.add(eleConflictRelationship);
							arlConflict.add(eleRelatioship);

							htConflictBOMs.put(strEBOMKey, arlConflict);
						}

                    }
                    else
                    {
                        htDeletedBOMs.put(strEBOMKey, eleRelatioship);
                        if (htUnChangedBOMs.containsKey(strEBOMKey))
                        {
                            htUnChangedBOMs.remove(strEBOMKey);
                        }
                    }
                }
                else if ("C".equals(strEBOMChgType))
                {
					if (htDeletedBOMs.containsKey(strEBOMKey) || htChangedBOMs.containsKey(strEBOMKey) || htAddedBOMs.containsKey(strEBOMKey) || htConflictBOMs.containsKey(strEBOMKey))
					{
						if (htDeletedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htDeletedBOMs.get(strEBOMKey);
							htDeletedBOMs.remove(strEBOMKey);
						}
						else if (htChangedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htChangedBOMs.get(strEBOMKey);
							htChangedBOMs.remove(strEBOMKey);
						}
						else if (htAddedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htAddedBOMs.get(strEBOMKey);
							htAddedBOMs.remove(strEBOMKey);
						}
						if (htConflictBOMs.containsKey(strEBOMKey))
						{
							ArrayList arlConflict = (ArrayList) htConflictBOMs.get(strEBOMKey);

							if (!arlConflict.contains(eleConflictRelationship))
							{
								arlConflict.add(eleConflictRelationship);
							}

							arlConflict.add(eleRelatioship);

							htConflictBOMs.remove(strEBOMKey);
							htConflictBOMs.put(strEBOMKey, arlConflict);
						}
						else
						{
							ArrayList arlConflict = new ArrayList();

							if (!arlConflict.contains(eleConflictRelationship))
                    {
								arlConflict.add(eleConflictRelationship);
							}

							arlConflict.add(eleRelatioship);

							htConflictBOMs.put(strEBOMKey, arlConflict);
						}
                    }
                    else
                    {
                        htChangedBOMs.put(strEBOMKey, eleRelatioship);
                        if (htUnChangedBOMs.containsKey(strEBOMKey))
                        {
                            htUnChangedBOMs.remove(strEBOMKey);
                        }

                    }
					if (strPartNewFN != null)
					{
						strEBOMKey = strPartType + "~" + strPartName + "~" + strPartRev + "~" + strPartNewFN;
					}
					else
					{
						strEBOMKey = strPartType + "~" + strPartName + "~" + strPartRev + "~" + strPartFN;
					}

					if (strPartNewRD != null)
					{
						strEBOMKey =  strEBOMKey + "~" + strPartNewRD;
					}
					else
					{
						strEBOMKey =  strEBOMKey + "~" + strPartRD;
					}

					if (!htNewChangedBOMs.containsKey(strEBOMKey))
					{
						htNewChangedBOMs.put(strEBOMKey, eleRelatioship);
					}

                }
                else if ("A".equals(strEBOMChgType))
                {
					if (htDeletedBOMs.containsKey(strEBOMKey) || htNewChangedBOMs.containsKey(strEBOMKey) || htConflictBOMs.containsKey(strEBOMKey))
					{
						if (htDeletedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htDeletedBOMs.get(strEBOMKey);
							htDeletedBOMs.remove(strEBOMKey);
						}
						else if (htNewChangedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htNewChangedBOMs.get(strEBOMKey);
							htNewChangedBOMs.remove(strEBOMKey);
						}
						else if (htAddedBOMs.containsKey(strEBOMKey))
						{
							eleConflictRelationship = (Element)htAddedBOMs.get(strEBOMKey);
							htAddedBOMs.remove(strEBOMKey);
						}
						if (htConflictBOMs.containsKey(strEBOMKey))
						{
							ArrayList arlConflict = (ArrayList) htConflictBOMs.get(strEBOMKey);

							if (!arlConflict.contains(eleConflictRelationship))
							{
								arlConflict.add(eleConflictRelationship);
							}

							arlConflict.add(eleRelatioship);

							htConflictBOMs.remove(strEBOMKey);
							htConflictBOMs.put(strEBOMKey, arlConflict);
						}
						else
                    {
							ArrayList arlConflict = new ArrayList();

							arlConflict.add(eleConflictRelationship);
							arlConflict.add(eleRelatioship);

							htConflictBOMs.put(strEBOMKey, arlConflict);
						}
					}
					else
					{
						htAddedBOMs.put(strEBOMKey, eleRelatioship);
					}

                }
            }
           }
                  //Added for parsing the XML itself for conflict starts

				  //Added for parsing the XML itself for conflict ends
        }

		Enumeration enumKeys = htConflictBOMs.keys();
		ArrayList arlElements = null;
		Iterator itrList = null;
		com.matrixone.jdom.Attribute attrConflictEBOMChgType = null;
		com.matrixone.jdom.Attribute attrConflictChgType = null;
		com.matrixone.jdom.Attribute attrConflictMarkup = null;
		String strConflictEBOMMarkup = null;
		String strConflictEBOMChgType = null;
		String strAttrValue = null;
		String strAttrName = null;
		Element eleConflictAttribute = null;
		HashMap hmpAttributes = null;
		HashMap hmpConflictAttributes = null;

		java.util.List listConflictFromAttributes = null;
		Iterator itrConflictAttributes = null;

		ArrayList arlNoConflicts = new ArrayList();
		ArrayList arlConflicts = new ArrayList();

		Hashtable htNewConflicts = new Hashtable();

		boolean blnOtherChange = true;

		Element eleTemp = null;

		while (enumKeys.hasMoreElements())
		{
			String strKey = (String) enumKeys.nextElement();
			arlElements = (ArrayList) htConflictBOMs.get(strKey);
			itrList = arlElements.iterator();

			hmpAttributes = new HashMap();
			hmpConflictAttributes = new HashMap();

			arlConflicts = new ArrayList();

			blnOtherChange = true;

			while (itrList.hasNext())
			{
				strConflictEBOMChgType = null;
				strConflictEBOMMarkup = null;
				eleTemp = (Element) itrList.next();
				attrConflictEBOMChgType = eleTemp.getAttribute("chgtype");

				if (attrConflictEBOMChgType != null)
				{
					strConflictEBOMChgType = attrConflictEBOMChgType.getValue();
				}

				if ("C".equals(strConflictEBOMChgType))
				{
					Element eleConflictRelatioshipInfo = eleTemp.getChild("attributeList");
					listConflictFromAttributes = eleConflictRelatioshipInfo.getChildren();
					itrConflictAttributes = listConflictFromAttributes.iterator();

					while (itrConflictAttributes.hasNext())
					{
						eleConflictAttribute = (Element) itrConflictAttributes.next();

						strAttrName = eleConflictAttribute.getChild("name").getText();

						attrConflictChgType = eleConflictAttribute.getAttribute("chgtype");

						strAttrValue = null;

						if ("Find Number".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}

						}
						else if ("Reference Designator".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}
						}
						else if ("Quantity".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}
						}
						else if ("Component Location".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}
						}
						else if ("Usage".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}

						}else if ("Notes".equals(strAttrName))
						{
							if (attrConflictChgType != null)
							{
								strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
							}

						}

						if (strAttrValue != null)
						{
							if (hmpAttributes.containsKey(strAttrName))
							{
								if (!strAttrValue.equals((String)hmpAttributes.get(strAttrName)))
								{
									hmpConflictAttributes.put(strAttrName, "TRUE");
								}
							}
							else
							{
								hmpAttributes.put(strAttrName, strAttrValue);
							}
                    }

					}
				}
                    else
                    {
					blnOtherChange = false;
					break;
				}
			}

			if (blnOtherChange)
			{
				HashMap hmpNonConflictAttrAdded = new HashMap();
				itrList = arlElements.iterator();

				while (itrList.hasNext())
				{
					strConflictEBOMChgType = null;
					eleTemp = (Element) itrList.next();

					attrConflictEBOMChgType = eleTemp.getAttribute("chgtype");
					attrConflictMarkup = eleTemp.getAttribute("markupname");

					if (attrConflictEBOMChgType != null)
					{
						strConflictEBOMChgType = attrConflictEBOMChgType.getValue();
					}

					if (attrConflictMarkup != null)
					{
						strConflictEBOMMarkup = attrConflictMarkup.getValue();
                    }

					if ("C".equals(strConflictEBOMChgType))
					{
						Element eleConflictObjectInfo = eleTemp.getChild("relatedObject").getChild("businessObjectRef");
						Element eleRelatioshipDescInfo = eleTemp.getChild("relationshipDefRef");
						com.matrixone.jdom.Attribute attrRelId = eleRelatioshipDescInfo.getAttribute("relid");
						String strRelId = "";
						if (attrRelId != null)
						{
							strRelId = attrRelId.getValue();
                }
						com.matrixone.jdom.Attribute attrObjId = eleTemp.getChild("relatedObject").getAttribute("relatedobjid");
						String strObjId = "";
						if (attrObjId != null)
						{
							strObjId = attrObjId.getValue();
						}


						Element eleNewRel = new Element("relationship");
						eleNewRel.setAttribute("chgtype", "C");
						if (strConflictEBOMMarkup != null)
						{
							eleNewRel.setAttribute("markupname",strConflictEBOMMarkup);
						}
						Element eleRelDesc = new Element("relationshipDefRef");
						eleRelDesc.addContent(eleRelatioshipDescInfo.getText());
						eleRelDesc.setAttribute("relid", strRelId);
						eleNewRel.addContent(eleRelDesc);
						Element eleNewRelationship = new Element("relatedObject");
						eleNewRel.addContent(eleNewRelationship);
						eleNewRelationship.setAttribute("relatedobjid", strObjId);
						Element eleNewBusInfo = new Element("businessObjectRef");
						eleNewRelationship.addContent(eleNewBusInfo);
						Element eleNewAttrInfo = new Element("attributeList");
						eleNewRel.addContent(eleNewAttrInfo);
						Element eleNewAttr = new Element("attribute");
						Element eleNewVal = new Element("newvalue");
						Element eleNameVal = new Element("name");
						Element eleOldVal = new Element("oldvalue");

						Element eleNewConflictRel = new Element("relationship");
						eleNewConflictRel.setAttribute("chgtype", "C");
						if (strConflictEBOMMarkup != null)
						{
							eleNewConflictRel.setAttribute("markupname",strConflictEBOMMarkup);
                    }
						Element eleConflictRelDesc = new Element("relationshipDefRef");
						eleConflictRelDesc.addContent(eleRelatioshipDescInfo.getText());
						eleConflictRelDesc.setAttribute("relid", strRelId);
						eleNewConflictRel.addContent(eleConflictRelDesc);
						Element eleNewConflictRelationship = new Element("relatedObject");
						eleNewConflictRel.addContent(eleNewConflictRelationship);
						eleNewConflictRelationship.setAttribute("relatedobjid", strObjId);
						Element eleNewConflictBusInfo = new Element("businessObjectRef");
						eleNewConflictRelationship.addContent(eleNewConflictBusInfo);
						Element eleNewConflictAttrInfo = new Element("attributeList");
						eleNewConflictRel.addContent(eleNewConflictAttrInfo);
						Element eleNewConflictAttr = new Element("attribute");
						Element eleNewConflictVal = new Element("newvalue");
						Element eleNameConflictVal = new Element("name");
						Element eleOldConflictVal = new Element("oldvalue");


						//IR-045004: Decoding the encoded type, name,rev,vault
						String strConflictPartType = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleConflictObjectInfo.getChild("objectType").getText());
						String strConflictPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleConflictObjectInfo.getChild("objectName").getText());
						String strConflictPartRev = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleConflictObjectInfo.getChild("objectRevision").getText());
						String strConflictPartVault = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleConflictObjectInfo.getChild("vaultRef").getText());

						String strConflictPartUOM  = eleConflictObjectInfo.getChild("UOM").getText();
						String strConflictPartState  = eleConflictObjectInfo.getChild("objectState").getText();

						Element eleObjectType = new Element("objectType");
						eleObjectType.addContent(strConflictPartType);
						Element eleObjectName = new Element("objectName");
						eleObjectName.addContent(strConflictPartName);
						Element eleObjectRevision = new Element("objectRevision");
						eleObjectRevision.addContent(strConflictPartRev);
						Element eleObjectVault = new Element("vaultRef");
						eleObjectVault.addContent(strConflictPartVault);
						Element eleObjectUOM = new Element("UOM");
						eleObjectUOM.addContent(strConflictPartUOM);
						Element eleObjectState = new Element("objectState");
						eleObjectState.addContent(strConflictPartState);

						eleNewBusInfo.addContent(eleObjectType);
						eleNewBusInfo.addContent(eleObjectName);
						eleNewBusInfo.addContent(eleObjectRevision);
						eleNewBusInfo.addContent(eleObjectVault);
						eleNewBusInfo.addContent(eleObjectUOM);
						eleNewBusInfo.addContent(eleObjectState);


						Element eleConflictObjectType = new Element("objectType");
						eleConflictObjectType.addContent(strConflictPartType);
						Element eleConflictObjectName = new Element("objectName");
						eleConflictObjectName.addContent(strConflictPartName);
						Element eleConflictObjectRevision = new Element("objectRevision");
						eleConflictObjectRevision.addContent(strConflictPartRev);
						Element eleConflictObjectVault = new Element("vaultRef");
						eleConflictObjectVault.addContent(strConflictPartVault);
						Element eleConflictObjectUOM = new Element("UOM");
						eleConflictObjectUOM.addContent(strConflictPartUOM);
						Element eleConflictObjectState = new Element("objectState");
						eleConflictObjectState.addContent(strConflictPartState);

						eleNewConflictBusInfo.addContent(eleConflictObjectType);
						eleNewConflictBusInfo.addContent(eleConflictObjectName);
						eleNewConflictBusInfo.addContent(eleConflictObjectRevision);
						eleNewConflictBusInfo.addContent(eleConflictObjectVault);
						eleNewConflictBusInfo.addContent(eleConflictObjectUOM);
						eleNewConflictBusInfo.addContent(eleConflictObjectState);

						Element eleConflictRelatioshipInfo = eleTemp.getChild("attributeList");
						listConflictFromAttributes = eleConflictRelatioshipInfo.getChildren();
						itrConflictAttributes = listConflictFromAttributes.iterator();

						int intConflictCount = 0;
						int intNonConflictCount = 0;

						String strConflictFN = null;
						String strConflictRD = null;
						boolean blnFNConflict = false;
						boolean blnRDConflict = false;
						boolean blnFNNonConflict = false;
						boolean blnRDNonConflict = false;


						Element eleFN = null;
						Element eleRD = null;

						Element eleNonConflictFN = null;
						Element eleNonConflictRD = null;


						while (itrConflictAttributes.hasNext())
						{
							eleConflictAttribute = (Element) itrConflictAttributes.next();

							strAttrName = eleConflictAttribute.getChild("name").getText();

							attrConflictChgType = eleConflictAttribute.getAttribute("chgtype");

							eleNewAttr = new Element("attribute");
							eleNewVal = new Element("newvalue");
							eleOldVal = new Element("oldvalue");
							eleNameVal = new Element("name");


							strAttrValue = null;
							String strAttrOldValue = null;

							if ("Find Number".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
									strConflictFN = eleConflictAttribute.getChild("oldvalue").getText();
								}
								else
								{
									strConflictFN = eleConflictAttribute.getChild("string").getText();
								}

								eleFN = new Element("attribute");

								eleNonConflictFN  = new Element("attribute");

								eleNewVal = new Element("string");
								eleNameVal = new Element("name");

								eleFN.addContent(eleNameVal);
								eleNameVal.addContent(strAttrName);
								eleFN.addContent(eleNewVal);
								eleNewVal.addContent(strConflictFN);

								eleNewVal = new Element("string");
								eleNameVal = new Element("name");

								eleNonConflictFN.addContent(eleNameVal);
								eleNameVal.addContent(strAttrName);
								eleNonConflictFN.addContent(eleNewVal);
								eleNewVal.addContent(strConflictFN);

							}
							else if ("Reference Designator".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
									strConflictRD = eleConflictAttribute.getChild("oldvalue").getText();
								}
								else
								{
									strConflictRD = eleConflictAttribute.getChild("string").getText();
								}

								eleRD = new Element("attribute");

								eleNonConflictRD  = new Element("attribute");

								eleNewVal = new Element("string");
								eleNameVal = new Element("name");

								eleRD.addContent(eleNameVal);
								eleNameVal.addContent(strAttrName);
								eleRD.addContent(eleNewVal);
								eleNewVal.addContent(strConflictRD);

								eleNewVal = new Element("string");
								eleNameVal = new Element("name");

								eleNonConflictRD.addContent(eleNameVal);
								eleNameVal.addContent(strAttrName);
								eleNonConflictRD.addContent(eleNewVal);
								eleNewVal.addContent(strConflictRD);
							}
							else if ("Quantity".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
								}
							}
							else if ("Component Location".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
								}
							}
							else if ("Usage".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
								}
							}else if ("Notes".equals(strAttrName))
							{
								if (attrConflictChgType != null)
								{
									strAttrValue = eleConflictAttribute.getChild("newvalue").getText();
									strAttrOldValue = eleConflictAttribute.getChild("oldvalue").getText();
								}
							}

							if (strAttrValue != null)
							{
								if (hmpConflictAttributes.containsKey(strAttrName))
								{
									if ("Find Number".equals(strAttrName))
									{
										blnFNConflict = true;
									}
									else if ("Reference Designator".equals(strAttrName))
									{
										blnRDConflict = true;
									}

									eleNewConflictAttr = new Element("attribute");
									eleNewConflictAttr.setAttribute("chgtype", "C");
									eleNewConflictVal = new Element("newvalue");
									eleOldConflictVal = new Element("oldvalue");
									eleNameConflictVal = new Element("name");

									eleNewConflictAttrInfo.addContent(eleNewConflictAttr);
									eleNewConflictAttr.addContent(eleNameConflictVal);
									eleNameConflictVal.addContent(strAttrName);
									eleNewConflictAttr.addContent(eleOldConflictVal);
									eleOldConflictVal.addContent(strAttrOldValue);
									eleNewConflictAttr.addContent(eleNewConflictVal);
									eleNewConflictVal.addContent(strAttrValue);
									intConflictCount++;
								}
								else
								{
									if (!hmpNonConflictAttrAdded.containsKey(strAttrName))
									{
										if ("Find Number".equals(strAttrName))
										{
											blnFNNonConflict = true;
										}
										else if ("Reference Designator".equals(strAttrName))
										{
											blnRDNonConflict = true;
										}

										eleNewAttr = new Element("attribute");
										eleNewAttr.setAttribute("chgtype", "C");
										eleNewVal = new Element("newvalue");
										eleOldVal = new Element("oldvalue");
										eleNameVal = new Element("name");

										eleNewAttrInfo.addContent(eleNewAttr);
										eleNewAttr.addContent(eleNameVal);
										eleNameVal.addContent(strAttrName);
										eleNewAttr.addContent(eleOldVal);
										eleOldVal.addContent(strAttrOldValue);
										eleNewAttr.addContent(eleNewVal);
										eleNewVal.addContent(strAttrValue);

										intNonConflictCount++;

										hmpNonConflictAttrAdded.put(strAttrName, strAttrValue);
									}
								}
                    }
                }

						if (!blnFNConflict)
						{
							eleNewConflictAttrInfo.addContent(eleFN);
						}
						if (!blnRDConflict)
						{
							eleNewConflictAttrInfo.addContent(eleRD);
						}

						if (!blnFNNonConflict)
						{
							eleNewAttrInfo.addContent(eleNonConflictFN);
						}
						if (!blnRDNonConflict)
						{
							eleNewAttrInfo.addContent(eleNonConflictRD);
            }


						if (intConflictCount > 0)
						{
							arlConflicts.add(eleNewConflictRel);
						}
						if (intNonConflictCount > 0)
						{
							arlNoConflicts.add(eleNewRel);
						}
					}
				}

				if (!(arlConflicts.isEmpty()))
				{
					htNewConflicts.put(strKey, arlConflicts);
				}
				else
				{
					htConflictBOMs.remove(strKey);
                    }
                }

        }

		htConflictBOMs.putAll(htNewConflicts);
		if (htConflictBOMs.isEmpty())
		{
			return "true";
        }
		else
		  {
			return "false";
		  }
	  }catch(Exception ex)
	   {
		  throw ex;
	   }
	}


/**
     * Method to check merged Plant BOM Markups
     *
	 * Added for Bug 355092
     * @param context the eMatrix <code>Context</code> object
     * @param args   holds a Sting of object Ids
     * @return      String staus true/false
     * @throws      Exception if the operation fails
     * @since       EngineeringCentral X3
     **/
    public static String checkPlantBOMConflict(Context context, String[] args )
        throws Exception
    {
        Hashtable htChangedBOMs = new Hashtable();
        Hashtable htAddedBOMs = new Hashtable();
        Hashtable htDeletedBOMs = new Hashtable();
        Hashtable htUnChangedBOMs = new Hashtable();
        Hashtable htConflictBOMs = new Hashtable();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);

        try
        {
        StringList strlMarkupIds=(StringList)paramMap.get("markupIds");

        String XMLFORMAT = PropertyUtil.getSchemaProperty(context, "format_XML");
        // create a temporary workspace directory
        String strTransPath = context.createWorkspace();

        java.io.File fEmatrixWebRoot = new java.io.File(strTransPath);

        Iterator itrMarkups = strlMarkupIds.iterator();

        DomainObject boMarkup = null;

        String strMarkupId = null;
        String strMarkupName = null;
        String strXMLFileName = null;
        String strRelationshipDefRef = null;
        String strEBOMRelId = null;
        String strSelectedPartId = null;

        //Use default SAX driver
        com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder(false);
		builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
		builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder.setValidation(false);

        com.matrixone.jdom.Document docXML = null;

        Element eleRoot = null;
        Element eleRelatioship = null;
        Element eleObjectInfo = null;
        Element eleRelatioshipInfo = null;
        List listFromRelationships = null;

        String strPlantId = null;
        Iterator itrRelationships = null;

        com.matrixone.jdom.Attribute attrEBOMChgType = null;

        String strPartType =  null;
        String strPartName =  null;
        String strPartRev  =  null;
        String strEBOMChgType  =  null;
        String strKey  =  null;
        String strPartId=null;
        String strConnectionId = null;
        List lstConflictBOMs = null;

        while (itrMarkups.hasNext())
        {
            strMarkupId = (String) itrMarkups.next();
            boMarkup = new DomainObject(strMarkupId);
            strMarkupName =boMarkup.getInfo(context,DomainConstants.SELECT_NAME);
            strPartId=boMarkup.getInfo(context,"to["+DomainConstants.RELATIONSHIP_EBOM_MARKUP+"].from.id");
            DomainObject strPart=new DomainObject();
            strPart.setId(strPartId);

            strXMLFileName = strMarkupName + ".xml";

            boMarkup.checkoutFile(context, false, XMLFORMAT, strXMLFileName, strTransPath);
            java.io.File fMarkupXML = new java.io.File(fEmatrixWebRoot, strXMLFileName);

            //could not checkout file.
            if (fMarkupXML == null)
            {
                continue;
            }

            docXML = builder.build(fMarkupXML);
            eleRoot = docXML.getRootElement();

             listFromRelationships = eleRoot.getChild("businessObject").getChild("fromRelationshipList").getChildren();
             itrRelationships = listFromRelationships.iterator();

            while (itrRelationships.hasNext())
            {
                eleRelatioship = (Element) itrRelationships.next();
                eleObjectInfo = eleRelatioship.getChild("fromRel").getChild("businessObjectRef");
                strRelationshipDefRef = eleRelatioship.getChild("relationshipDefRef").getText();
                strConnectionId = eleRelatioship.getChild("relationshipDefRef").getAttributeValue("relId");
                //EBOM Rel Id
                strEBOMRelId = eleRelatioship.getChild("fromRel").getAttributeValue("relId");
                //Object of the part selected to add/delete/modify
                strSelectedPartId = eleRelatioship.getChild("fromRel").getAttributeValue("objectId");

                eleRelatioshipInfo = eleRelatioship.getChild("attributeList");
                //Plant id
                strPlantId = eleRelatioshipInfo.getAttributeValue("plantId");

                //T N R of part to which to connect as Alt or Sub
				//IR-045004: Decoding the encoded type, name,rev
                strPartType = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectType").getText());
                strPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectName").getText());
                strPartRev  = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectRevision").getText());

                attrEBOMChgType = eleRelatioship.getAttribute("chgtype");

                strEBOMChgType = null;

                if (attrEBOMChgType != null )
                {
                    strEBOMChgType = attrEBOMChgType.getValue();
                }

                //Use the relationship type, TNR of part to connect to,ebom connection id, selected part's object id and plant id as key
                strKey = strRelationshipDefRef.trim() + "~" + strPartType + "~"
                                + strPartName + "~" + strPartRev+ "~" + strConnectionId+ "~" +strEBOMRelId
                                + "~" + strSelectedPartId + "~" + strPlantId;
                if (strEBOMChgType == null)
                {
                    if (!htDeletedBOMs.containsKey(strKey) && !htChangedBOMs.containsKey(strKey) && !htUnChangedBOMs.containsKey(strKey))
                    {
                        htUnChangedBOMs.put(strKey, eleRelatioship);
                    }
                }
                else if ("D".equals(strEBOMChgType))
                {
                    //Conflict:Part id marked for change to the same plant
                    //Note: though deletion is irrespective of the plant, this check is required for conflict
                    if (!(htChangedBOMs.containsKey(strKey)))
                    {
                        htDeletedBOMs.put(strKey, eleRelatioship);
                        if (htUnChangedBOMs.containsKey(strKey))
                        {
                            htUnChangedBOMs.remove(strKey);
                        }
                    }
                }
                else if ("C".equals(strEBOMChgType))
                {
                    //Conflict:Part id marked for deletion or change, to the same plant
                    if (htChangedBOMs.containsKey(strKey) || htConflictBOMs.containsKey(strKey))
                    {
                        lstConflictBOMs = (List)htConflictBOMs.get(strKey);
						if(lstConflictBOMs == null) {
							lstConflictBOMs = new ArrayList(1);
							lstConflictBOMs.add(eleRelatioship);
							if(htChangedBOMs.containsKey(strKey)) {
								lstConflictBOMs.add(htChangedBOMs.get(strKey));
								htChangedBOMs.remove(strKey);
							}
							htConflictBOMs.put(strKey,lstConflictBOMs);
						} else {
							lstConflictBOMs.add(eleRelatioship);
							if(htChangedBOMs.containsKey(strKey)) {
								lstConflictBOMs.add(htChangedBOMs.get(strKey));
								htChangedBOMs.remove(strKey);
							}
							htConflictBOMs.put(strKey,lstConflictBOMs);
						}
                    }
                    else if (!(htDeletedBOMs.containsKey(strKey))) {
                       htChangedBOMs.put(strKey, eleRelatioship);
                       if (htUnChangedBOMs.containsKey(strKey))
                       {
                           htUnChangedBOMs.remove(strKey);
                       }
                    }
                }
                else if ("A".equals(strEBOMChgType))
                {
                    //Conflict:Part id marked for deletion or change or addition(with diff attr values) to the same plant
                    if (!((htDeletedBOMs.containsKey(strKey) || htChangedBOMs.containsKey(strKey))))
                    {
                        htAddedBOMs.put(strKey, eleRelatioship);
                    }
                }
            }
        } //Finished processing all markups

		if(htConflictBOMs.isEmpty()) {
			return "true";
		}
		else
		{
			return "false";
		}
		}
        catch (Exception ex)
        {
			throw ex;
        }
	}

    //IR-019321 - Starts
    /**
    * Method  to clone and connect Markup to part
    * @param  context the eMatrix <code>Context</code> object
    * @param  eco - eco object id
    * @throws Exception if the operation fails
    * @since  EngineeringCentral X7
    **/
    public void cloneAndConnectMarkup(Context context, String ecoId) throws Exception {

        String parentObjId              = "";
        String strMarkupType            = "";
        String markupOwner              = "";
        String markupOriginator         = "";
        StringList slBOMMarkup          = null;
        DomainObject doObject           = DomainObject.newInstance(context);

        try
        {
            if (ecoId != null && !"".equals(ecoId)) {
                doObject.setId(ecoId);
                slBOMMarkup         = doObject.getInfoList(context, "from["+RELATIONSHIP_APPLIED_MARKUP+"].to.id");
                if (slBOMMarkup != null & !slBOMMarkup.isEmpty()) {
                    Iterator itrBOMMarkup = slBOMMarkup.iterator();
                    while (itrBOMMarkup.hasNext()) {
                        String markupId     = (String) itrBOMMarkup.next();
                        doObject.setId(markupId);
                        parentObjId         = doObject.getInfo(context, "to["+RELATIONSHIP_EBOM_MARKUP+"].from.id");
                        strMarkupType       = doObject.getInfo(context, DomainConstants.SELECT_TYPE);
                        markupOwner         = doObject.getInfo(context, DomainConstants.SELECT_OWNER);
                        markupOriginator    = doObject.getInfo(context, DomainConstants.SELECT_ORIGINATOR);
                        if (strMarkupType.equals(TYPE_BOMMARKUP)
                                && (!"User Agent".equals(markupOriginator)
                                        || "User Agent".equals(markupOwner))) { //propagate merged markups to version objects
                            copyMarkupToVersionObject(context, doObject, parentObjId);
                        }
                    }
                }
            }
        }
        catch (Exception exp) {
            System.out.println("Exception in cloneAndConnectMarkup " + exp.toString());
            throw exp;
        }
    }
    /**
    * Method  to return version objects
    * @param  context the eMatrix <code>Context</code> object
    * @param  partid - part object id
    * @throws Exception if the operation fails
    * @since  EngineeringCentral X7
    **/
    public StringList getVersionObjects(Context context, String partId) throws Exception
    {
        DomainObject doPart         = DomainObject.newInstance(context, partId);
        String partName             = doPart.getInfo(context, DomainConstants.SELECT_NAME);
        StringList slPartVersions   = new StringList(1);
        String expression           = "to[" + RELATIONSHIP_PART_VERSION + "] == true";
        matrix.db.Query mqlQuery    = new matrix.db.Query();
        mqlQuery.setBusinessObjectName(partName);
        mqlQuery.setVaultPattern(context.getVault().toString());
        mqlQuery.setBusinessObjectType(DomainConstants.TYPE_PART);
        mqlQuery.setWhereExpression(expression);
        Iterator itrVersionObjects = mqlQuery.evaluate(context).iterator();
        while (itrVersionObjects.hasNext()) {
            slPartVersions.add(((BusinessObject)itrVersionObjects.next()).getObjectId());
        }
        return slPartVersions;
    }
    /**
    * Method  to copy markups to version objects
    * @param  context the eMatrix <code>Context</code> object
    * @param  doMarkup markup domain object
    * @param  partId - part object id
    * @throws Exception if the operation fails
    * @since  EngineeringCentral X7
    **/
    public void copyMarkupToVersionObject (Context context, DomainObject doMarkup,
            String partId) throws Exception
    {
        try
        {
            String strMarkupOwner       = "";
            String strMarkupName        = "";
            String strXMLFileName       = "";
            DomainObject doObject       = DomainObject.newInstance(context);
            DomainObject doPart         = DomainObject.newInstance(context);
            String strTransPath         = context.createWorkspace();
            java.io.File fEmatrixWebRoot= new java.io.File(strTransPath);
            java.io.File fMarkupXML1    = null;
            FileList fileListCheckin    = null;
            String XMLFORMAT            = PropertyUtil.getSchemaProperty(context, "format_XML");
            StringList versionObjects   = getVersionObjects(context, partId);
            Iterator itrVersionObjects  = versionObjects.iterator();
            while (itrVersionObjects.hasNext()) {
                strMarkupOwner      = doMarkup.getInfo(context, DomainConstants.SELECT_OWNER);
                String strMarkupId = FrameworkUtil.autoName(context,"type_BOMMarkup","policy_PartMarkup");
                doObject.setId(strMarkupId);
                String strNewMarkupName = doObject.getInfo(context, DomainConstants.SELECT_NAME);
                com.matrixone.apps.domain.util.mxBus.setOwner(context, strMarkupId, strMarkupOwner);
                strMarkupName       = doMarkup.getInfo(context,DomainConstants.SELECT_NAME);
                strXMLFileName      = strMarkupName + ".xml";
                doMarkup.checkoutFile(context, false, XMLFORMAT, strXMLFileName, strTransPath);
                java.io.File fMarkupXML = new java.io.File(fEmatrixWebRoot, strXMLFileName);
                strXMLFileName      = strNewMarkupName + ".xml";
                fMarkupXML1         = new java.io.File(fEmatrixWebRoot, strXMLFileName);
                fMarkupXML.renameTo(fMarkupXML1);
                matrix.db.File fCheckinMergedXML = new matrix.db.File(fMarkupXML1.getAbsolutePath(), XMLFORMAT);
                fileListCheckin = new FileList(1);
                fileListCheckin.addElement(fCheckinMergedXML);
                doObject.checkinFromServer(context, false, false, XMLFORMAT, "", fileListCheckin);
                String versionObjId = itrVersionObjects.next().toString();
                doPart.setId(versionObjId);
                String changeOID     = doPart.getInfo(context, "to[" + RELATIONSHIP_AFFECTED_ITEM + "].from.id");
                String parentPartOID = doPart.getInfo(context, "to[" + RELATIONSHIP_PART_VERSION + "].from.id");
                DomainRelationship.connect(context,new DomainObject(parentPartOID),RELATIONSHIP_EBOM_MARKUP,doObject);
                DomainRelationship.connect(context,new DomainObject(changeOID),RELATIONSHIP_APPLIED_MARKUP,doObject);
                doObject.setId(strMarkupId);
                context.setCustomData("fromMarkupActions", "TRUE");
                doObject.gotoState(context, approvedState);
                context.removeFromCustomData("fromMarkupActions");
            }
        }
        catch (Exception exp) {
            System.out.println("Exception in copyMarkupToVersionObject " + exp.toString());
            throw exp;
        }
    }
    //IR-019321 - Ends

    /**
     * Method to check for display of Name field in the BOM markup form
     * @param context - DB Context
     * @param args - String array
     * @return boolean - true if SaveAs operation else false
     */
    public static boolean isModeSaveAs(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strCmdType = (String)programMap.get("commandType");
        if(strCmdType != null && strCmdType.equalsIgnoreCase("SaveAs")){
            return true;
        }
        return false;
    }

    /**
     * Method added to check for the uniqueness of the BOM Markup name
     * @param context
     * @param args
     * @throws Exception - if the specified name exists in DB
     */
    public void checkForBOMMarkupName(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strMarkUpName=(String) paramMap.get("New Value");
        String sName = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump",EngineeringConstants.TYPE_BOM_MARKUP,strMarkUpName,"*","name");
        if(sName != null && sName.length()>0){

        	//Multitenant
        	throw new FrameworkException(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.BOMMarkup.MarkUpNameNotUnique"));
        }
    }
	/**
	 * This is the postprocessJPO method for Save Markup
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object.
	 * @param args
	 *            [] packed hashMap of request parameters
	 * @throws Exception
	 *             if the operation fails.
	 * @since R212
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void saveMarkup(Context context, String args[]) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");

		String changeOID = (String) requestMap.get("ChangeOID");
		String strDescription = (String) requestMap.get("Description");
		String markupXML = (String) requestMap.get("markupXML");
		String strName = (String) requestMap.get("Name");
		String commandType = (String) requestMap.get("commandType");
		String strObjectIds=(String) requestMap.get("strObjectIds");
		String objectIds = "";
		if(null!=strObjectIds) {
			objectIds=strObjectIds;
		}
		else{
			//whereUsed usecase strObjectIds passed as hidden param with alias objectIds to avoid URL traffic  
			strObjectIds=(String) requestMap.get("objectIds");
			objectIds= strObjectIds!=null?strObjectIds:objectIds;
		}

		HashMap reqMap = new HashMap();
		reqMap.put("objectIds", objectIds);
		reqMap.put("markupXML", markupXML);
		reqMap.put("commandType", commandType);
		reqMap.put("changeOID", changeOID);
		reqMap.put("strName", strName);
		reqMap.put("strDescription", strDescription);
		 if(!"null".equals(changeOID) && null!=changeOID && !"".equals(changeOID)){
		        // - start - Prevent user from creating a deviation request to remove a part from an EBOM
		        boolean isMBOMInstalled = com.matrixone.apps.engineering.EngineeringUtil.isMBOMInstalled(context);
		        if(isMBOMInstalled) {

		            HashMap argsMap = new HashMap();
		            argsMap.put("changeOID",changeOID);
		            argsMap.put("markupXML",markupXML);

		            Boolean allowDCR = (Boolean)JPO.invoke(context, "emxDCR", new String[] {}, "isDCRAllowed", JPO.packArgs(argsMap), Boolean.class);

		            if(!allowDCR.booleanValue()) {

		            	//Multitenant
		            	String msg = EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", context.getLocale(),"emxMBOM.Deviation.Error.Remove");
		                throw new Exception(msg);
		            }
		        }
		    }
		new EBOMMarkup().saveMarkup(context, reqMap);

	}

	/*
	 * * This method returns hidden field for storing markupXML value in save Markup page
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return a String containing hidden field for markupXML
	 * @throws Exception if the operation fails
	 * @since R212
	 */
	public String getMarkupXMLForSave(Context context, String[] args)
			throws Exception {

		String str = "";
		str += "<input type='hidden' name='markupXML' id='markupXML' value=''></input>";

		return str;
	}

	/**
	 * Checks the Part current state and accordingly displays ECRs or ECOs for the Change
	 * field typechooser for Markup Save and Save As webforms
	 * in Markup to select the change.
	 *
	 * @param context
	 *            ematrix context
	 * @param args
	 *            contains Program map
	 * @return String
	 * @throws Exception
	 *             if any operation fails.
	 * @since R212
	 */
	public String getChangeDynamicSearchQueryForMarkup(Context context,
			String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");

		String parentOID = (String) requestMap.get("objectId");
		String strSearchURL = "";
		if (parentOID != null && !"".equals(parentOID)) {
			boolean blnShowChange = false;
			boolean blnShowECR = false;
			boolean blnShowECO = false;
			DomainObject doPart = null;
			String strPolicy = "";
			String strCOStateFilter = "policy_FormalChange.state_Propose,policy_FormalChange.state_Prepare,policy_FasttrackChange.state_Prepare";
			String strCRStateFilter = "policy_ChangeRequest.state_Create,policy_ChangeRequest.state_Evaluate";
			
			doPart = new DomainObject(parentOID);
			strPolicy = (String) doPart.getInfo(context,
					DomainConstants.SELECT_POLICY);
			String policyClassification = EngineeringUtil
					.getPolicyClassification(context, strPolicy);

			if ("Production".equalsIgnoreCase(policyClassification))
			{
				blnShowChange = true;
				blnShowECO = true;
				blnShowECR = true;
			}
			strSearchURL = "TYPES=";

			if (blnShowChange) {
				if (blnShowECR && blnShowECO) {
					strSearchURL = strSearchURL
							+ "type_ChangeOrder,type_ChangeRequest:CURRENT="
							+ strCOStateFilter
							+ ","
							+ strCRStateFilter;

				} else if (blnShowECO) {
					strSearchURL = strSearchURL
							+ "type_ChangeOrder:CURRENT="
							+ strCOStateFilter;

				} else if (blnShowECR) {
					strSearchURL = strSearchURL
							+ "type_ChangeRequest:CURRENT="
							+ strCRStateFilter;

        }
    }
}
		return strSearchURL;
	}

    /**
     * Checks whether the markup promotion is initiated from Markup menu or not
     * @param context
     * @param args
     * @throws Exception
     */
    public int isPromotedFromMarkupActions(Context context, String[] args) throws Exception {
   		try{
	    	String fromMarkupActions = context.getCustomData("fromMarkupActions");
			if("TRUE".equals(fromMarkupActions)){
				return 0;
			}else{
				emxContextUtil_mxJPO.mqlNotice(context,
		                EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Alert.UseMarkupActions",
		                context.getSession().getLanguage()));
			}
	    	return 1;
   		}catch(Exception e){
   			throw e;
   		}
    }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAplliedMarkupXMLContent(Context context,String[] args) throws Exception
    {

        Element eleRelatioship = null;
        Element eleObjectInfo = null;
        Element eleRelatioshipInfo = null;
        Element eleAttribute  = null;
        String strPartType = null;
        String strPartName = null;
        String strPartRev = null;
        String strEBOMChgType = null;
        String strAttrName = null;
        String strAttrValue = null;
        MapList resultList =  new MapList();

        String languageStr = context.getSession().getLanguage();
        com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder();
		builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
		builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Map attrMap = null;

        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String strMarkupId = (String)paramMap.get("markupIds");
        String objectId = "";
        String XMLFORMAT = "XML";
        String strTransPath = context.createWorkspace();
        java.io.File fEmatrixWebRoot = new java.io.File(strTransPath);
        boolean blnPlantMarkup = false;

		ContextUtil.pushContext(context,"User Agent","",context.getVault().getName()); //Added for IR-151138
        DomainObject boMarkup = new DomainObject(strMarkupId);
        String strPlantMarkup =  PropertyUtil.getSchemaProperty(context,"type_PlantBOMMarkup");
        if(strPlantMarkup.equals(boMarkup.getInfo(context,DomainConstants.SELECT_TYPE))) {
            blnPlantMarkup = true;
        }
        boMarkup.open(context);
        String strMarkupName = boMarkup.getInfo(context, DomainConstants.SELECT_NAME);
        String strXMLFileName = strMarkupName + ".xml";
        boMarkup.checkoutFile(context, false, XMLFORMAT, strMarkupName+ ".xml", fEmatrixWebRoot.toString());
        boMarkup.close(context);
        ContextUtil.popContext(context);//Added for IR-151138
        java.io.File fMarkupXML = new java.io.File(fEmatrixWebRoot, strXMLFileName);
        com.matrixone.jdom.Document docXML = builder.build(fMarkupXML);

        Element eleRoot = docXML.getRootElement();
        com.matrixone.jdom.Attribute attrEBOMChgType = null;
        com.matrixone.jdom.Attribute attrChgType = null;
        Element attrEBOName = null;

        String strNewValue = null;
        String strOldValue = null;
        java.util.List listFromAttributes = null;
        java.util.List listFromRelationships = eleRoot.getChild("businessObject").getChild("fromRelationshipList").getChildren();


        Iterator itrAttributes = null;
        Iterator itrRelationships = listFromRelationships.iterator();

        try{
            while (itrRelationships.hasNext())
            {
                attrMap = new HashMap();
                StringBuffer sbfHTML =  new StringBuffer();;
                eleRelatioship = (Element) itrRelationships.next();
                eleRelatioship.setAttribute("markupname", strMarkupName);
                if(blnPlantMarkup) {
                    eleObjectInfo = eleRelatioship.getChild("fromRel").getChild("businessObjectRef");
                } else {
                    eleObjectInfo = eleRelatioship.getChild("relatedObject").getChild("businessObjectRef");
                }
                eleRelatioshipInfo = eleRelatioship.getChild("attributeList");
                listFromAttributes = eleRelatioshipInfo.getChildren();
                itrAttributes = listFromAttributes.iterator();

                if (null != eleObjectInfo.getChild("objectNameEncoded")) {
                    strPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo
                            .getChild("objectNameEncoded").getText());
                } else {
                    strPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo
                            .getChild("objectName").getText());
                }
                strPartRev = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild(
                "objectRevision").getText());

                attrEBOMChgType = eleRelatioship.getAttribute("chgtype");
                strEBOMChgType = null;

                if (attrEBOMChgType != null) {
                    strEBOMChgType = attrEBOMChgType.getValue();
                }
                strPartType = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectType").getText());
                if (null !=eleObjectInfo.getChild("objectNameEncoded")){
                    strPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectNameEncoded").getText());
                }
                else{
                    strPartName = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectName").getText());
                }
                strPartRev  = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(eleObjectInfo.getChild("objectRevision").getText());
                objectId = MqlUtil.mqlCommand(context,"print bus $1 $2 $3 select $4 dump",strPartType,strPartName,strPartRev,"id") ;

                attrMap.put(DomainConstants.SELECT_ID,objectId);

                if (strEBOMChgType!= null && "A".equals(strEBOMChgType))
                {
                    while (itrAttributes.hasNext()) {
                        sbfHTML.append("<P>");
                        eleAttribute = (Element) itrAttributes.next();

                        strAttrName = eleAttribute.getChild("name").getText();

                        attrEBOName = eleAttribute.getChild("string");

                        if (attrEBOName != null) {
                            strAttrValue = attrEBOName.getText();
                        } else {
                            attrEBOName = eleAttribute.getChild("real");

                            if (attrEBOName != null) {
                                strAttrValue = attrEBOName.getText();
                            } else {
                                attrEBOName = eleAttribute.getChild("boolean");
                                if (attrEBOName != null) {
                                    strAttrValue = attrEBOName.getText();
                                }

                                else {
                                    attrEBOName = eleAttribute.getChild("integer");
                                    if (attrEBOName != null) {
                                        strAttrValue = attrEBOName.getText();
                                    } else {
                                        attrEBOName = eleAttribute.getChild("date");
                                        if (attrEBOName != null) {
                                            strAttrValue = attrEBOName.getText();
                                        }
                                    }
                                }

                            }
                        }
                      //Commented for IR-153615
                      //Added for IR-149745 start
                        if(strAttrName.equalsIgnoreCase("Usage")) {
                        	strAttrValue = i18nNow.getRangeI18NString(strAttrName, strAttrValue, languageStr);
                        }
                       //Added for IR-149745 end

                        sbfHTML.append("<B>").append(
                                i18nNow
                                .getAttributeI18NString(strAttrName,
                                        languageStr)).append(": </B>").append(
                                                strAttrValue);

                        strAttrValue = "";

                    }


                    attrMap.put("ChangeDesc",sbfHTML.toString());
                    attrMap.put("ChangeType",strEBOMChgType);
                    resultList.add(attrMap);
                }
                if (strEBOMChgType!= null && "D".equals(strEBOMChgType)) {
                    while (itrAttributes.hasNext()) {
                        sbfHTML.append("<P>");
                        eleAttribute = (Element) itrAttributes.next();

                        strAttrName = eleAttribute.getChild("name").getText();

                        attrEBOName = eleAttribute.getChild("string");

                        if (attrEBOName != null) {
                            strAttrValue = attrEBOName.getText();
                        } else {
                            attrEBOName = eleAttribute.getChild("real");

                            if (attrEBOName != null) {
                                strAttrValue = attrEBOName.getText();
                            } else {
                                attrEBOName = eleAttribute.getChild("boolean");
                                if (attrEBOName != null) {
                                    strAttrValue = attrEBOName.getText();
                                }


                                else {
                                    attrEBOName = eleAttribute.getChild("integer");
                                    if (attrEBOName != null) {
                                        strAttrValue = attrEBOName.getText();
                                    } else {
                                        attrEBOName = eleAttribute.getChild("date");
                                        if (attrEBOName != null) {
                                            strAttrValue = attrEBOName.getText();
                                        }
                                    }
                                }

                            }
                        }
                      //Commented for IR-153615
                      //Added for IR-149745 start
                        if(strAttrName.equalsIgnoreCase("Usage")) {
                        	strAttrValue = i18nNow.getRangeI18NString(strAttrName, strAttrValue, languageStr);
                        }
                       //Added for IR-149745 end

                        sbfHTML.append("<B>").append(
                                i18nNow
                                .getAttributeI18NString(strAttrName,
                                        languageStr)).append(": </B>").append(
                                                strAttrValue);

                        strAttrValue = "";

                    }

                    attrMap.put("ChangeDesc",sbfHTML.toString());
                    attrMap.put("ChangeType",strEBOMChgType);
                    resultList.add(attrMap);
                }

                if (strEBOMChgType!= null && "C".equals(strEBOMChgType)) {
                    while (itrAttributes.hasNext()) {
                        sbfHTML.append("<P>");
                        strNewValue = "";
                        strOldValue = "";
                        eleAttribute = (Element) itrAttributes.next();

                        strAttrName = eleAttribute.getChild("name").getText();

                        attrChgType = eleAttribute.getAttribute("chgtype");

                        if (attrChgType != null) {
                            strOldValue = eleAttribute.getChild("oldvalue").getText();
                            strNewValue = eleAttribute.getChild("newvalue").getText();

                          //Commented for IR-153615
                            //Added for IR-149745 start
                            if(strAttrName.equalsIgnoreCase("Usage")) {
                            	strOldValue = i18nNow.getRangeI18NString(strAttrName, strOldValue, languageStr);
                            	strNewValue = i18nNow.getRangeI18NString(strAttrName, strNewValue, languageStr);
                            }
                           //Added for IR-149745 end

                            sbfHTML.append("<B>").append(
                                    i18nNow.getAttributeI18NString(strAttrName,
                                            languageStr));
                            sbfHTML.append(": </B><FONT COLOR=\"#FF0000\"><S>");
                            sbfHTML.append(strOldValue);
                            sbfHTML.append("</S></FONT> ");
                            sbfHTML.append(strNewValue);
                        } else {
                            attrEBOName = eleAttribute.getChild("string");

                            if (attrEBOName != null) {
                                strAttrValue = attrEBOName.getText();
                            } else {
                                attrEBOName = eleAttribute.getChild("real");

                                if (attrEBOName != null) {
                                    strAttrValue = attrEBOName.getText();
                                } else {
                                    attrEBOName = eleAttribute.getChild("boolean");
                                    if (attrEBOName != null) {
                                        strAttrValue = attrEBOName.getText();
                                    }


                                    else {
                                        attrEBOName = eleAttribute.getChild("integer");
                                        if (attrEBOName != null) {
                                            strAttrValue = attrEBOName.getText();
                                        } else {
                                            attrEBOName = eleAttribute.getChild("date");
                                            if (attrEBOName != null) {
                                                strAttrValue = attrEBOName.getText();
                                            }
                                        }
                                    }

                                }
                            }

                            sbfHTML.append("<B>").append(
                                    i18nNow.getAttributeI18NString(strAttrName,
                                            languageStr)).append(": </B>").append(
                                                    strAttrValue);

                            strAttrValue = "";

                        }

                    }
                    attrMap.put("ChangeDesc",sbfHTML.toString());
                    attrMap.put("ChangeType",strEBOMChgType);
                    resultList.add(attrMap);

                }
            }
        }catch (Exception e) {
         System.out.println("Exception in Applied Markup ");
        }
        return resultList;
    }


    public Vector getImageType(Context context, String args[])
    throws Exception {

        Vector columnVals = new Vector();
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList paramList = (MapList) paramMap.get("objectList");

        for (int i = 0; i < paramList.size(); i++) {
            Map mapImage  = (Map)paramList.get(i);
            String strEBOMChgType = (String)mapImage.get("ChangeType");

            if ("A".equals(strEBOMChgType))
            {
                columnVals
                .addElement("<img src=\"../common/images/iconStatusAdded.gif\">");

            }
            if ("D".equals(strEBOMChgType)) {

                columnVals
                .addElement("<img src=\"../common/images/iconStatusRemoved.gif\">");
            }

            if ("C".equals(strEBOMChgType)) {

                columnVals
                .addElement("<img src=\"../common/images/iconStatusChanged.gif\">");
            }
        }

        return columnVals;
     }
    public Vector getChangeDescription(Context context, String args[])
    throws Exception {
        Vector columnVals = new Vector();
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList paramList = (MapList) paramMap.get("objectList");

        for (int i = 0; i < paramList.size(); i++) {
            Map mapChangeDesc  = (Map)paramList.get(i);
            String strEBOMChgType = (String)mapChangeDesc.get("ChangeType");
            //XSSOK
            String strEBOMChangeDesc = (String)mapChangeDesc.get("ChangeDesc");

            if ("A".equals(strEBOMChgType))
            {
                columnVals.addElement(strEBOMChangeDesc);

            }
            if ("D".equals(strEBOMChgType)) {

                columnVals.addElement(strEBOMChangeDesc);
            }

            if ("C".equals(strEBOMChgType)) {

                columnVals.addElement(strEBOMChangeDesc);
            }
        }

        return columnVals;
    }

    /*
     * This method used to commit the Pending MR rel to the Database
     * @param context, args
     * @return void
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     * @author Srikanth
     */
	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public HashMap commitMRRelationship(Context context, String args[])throws Exception
    {
        String sPartId               = "";
        MapList mlItems    = new MapList();
        HashMap doc        = new HashMap();
		MapList chgRowsMapList = null;

        //Added for MP
		String strAssocPlants = EngineeringConstants.RELATIONSHIP_ASSOCIATED_PLANTS;
        try{
			HashMap progMap    = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)progMap.get("paramMap");
            sPartId   = (String)paramMap.get("objectId");
            DomainObject domPart = new DomainObject(sPartId);
            String strLanguage = (String)paramMap.get("languageStr");
           
            String sEIVal=domPart.getAttributeValue(context, EngineeringConstants.ATTRIBUTE_END_ITEM);
            String sEIOEVal=domPart.getAttributeValue(context, EngineeringConstants.ATTRIBUTE_END_ITEM_OVERRIDE_ENABLED);
            
            if(EngineeringConstants.STR_YES.equals(sEIVal) && EngineeringConstants.STR_YES.equals(sEIOEVal)){
				MqlUtil.mqlCommand(context,  "set env global $1 $2","MX_ECPART_CREATE_ACTION","TRUE");
            	domPart.setAttributeValue(context, EngineeringConstants.ATTRIBUTE_END_ITEM_OVERRIDE_ENABLED, EngineeringConstants.STR_NO);
            }
    		if(strLanguage.contains(","))
    			strLanguage = strLanguage.substring(0, strLanguage.indexOf(','));

    		String strPartState = domPart.getInfo(context, DomainConstants.SELECT_CURRENT);
    		if(DomainConstants.STATE_PART_RELEASE.equals(strPartState) || DomainConstants.STATE_DEVELOPMENT_PART_COMPLETE.equals(strPartState) || DomainConstants.STATE_PART_OBSOLETE.equals(strPartState)){

    		String sNextRevId = domPart.getInfo(context, "next.id");
    		if(UIUtil.isNotNullAndNotEmpty(sNextRevId)){
    			DomainObject doObjNext = new DomainObject(sNextRevId);
            String  sNextRevRev = "";
        		String sNextRevState = "";
        		if(doObjNext != null){
        			sNextRevRev = doObjNext.getInfo(context, "revision");
        			sNextRevState = doObjNext.getInfo(context, "current");
            }
            if(UIUtil.isNotNullAndNotEmpty(sNextRevRev)){
        			if(sNextRevState.equalsIgnoreCase("Release") || sNextRevState.equalsIgnoreCase("Obsolete")) {
            	throw new Exception( EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", new Locale(strLanguage), "emxMBOM.Error.AddMR.OnlyLatesRevision"));
            }
    		}
    		}
    		
    		}

			Element rootElement = (Element) progMap.get("contextData");
			String sParentOID = (String) rootElement.getAttributeValue("objectId");// get the Parent part Id from the Xml
			chgRowsMapList = UITableIndented.getChangedRowsMapFromElement(context, rootElement);

			for(Iterator itr = chgRowsMapList.iterator(); itr.hasNext();){
                    boolean leadPlantFound = false;
				String stDocIn ="";
				Map changedRowMap = (Map) itr.next();
				String sObjectId 	= (String) changedRowMap.get("childObjectId");
				String sRowId       = (String)changedRowMap.get("rowId");
				String markup       = (String)changedRowMap.get("markup");
				String sRelId 		= (String) changedRowMap.get("param1");

				// get parameters for replace operation

				HashMap columnsMap = (HashMap) changedRowMap.get("columns");
				String sChangeId = (String) columnsMap.get("Doc-In");
				String sPlantType = (String) columnsMap.get("PlantType");
				String stMakeBuy = (String) columnsMap.get("MakeBuy");
				String stMRSeq = (String) columnsMap.get("Sequence");
				String stMRStatus = (String) columnsMap.get("Status");
                   //Added for MP
				String stERPStatus = (String) columnsMap.get("ERP-Status");

				HashMap relMRAttrMap = new HashMap();
				relMRAttrMap.put(ATTRIBUTE_MAKE_BUY, stMakeBuy);
				relMRAttrMap.put(ATTRIBUTE_LEAD_PLANT, sPlantType);
				relMRAttrMap.put(ATTRIBUTE_ERP_STATUS, stERPStatus);
				relMRAttrMap.put(ATTRIBUTE_SEQUENCE, stMRSeq);
				relMRAttrMap.put(ATTRIBUTE_STATUS, stMRStatus);

                    if ("add".equals(markup)){
                        //Logic for ur ADD Opearation

                        //get all attribute values
                            //Modification start for 2012 Migration to Common Components
                            if(EngineeringUtil.isMBOMInstalled(context)){
						if(UIUtil.isNullOrEmpty(sChangeId)) {
                                //076028V6R2012 start
							throw new Exception( EnoviaResourceBundle.getProperty(context,
									"emxEngineeringCentralStringResource",new Locale(strLanguage), "emxEngineeringCentral.Change1"));
                                //076028V6R2012 end
                            }
						else {
							DomainObject changeObj = DomainObject.newInstance(context, sChangeId);
							stDocIn = changeObj.getInfo(context, DomainConstants.SELECT_NAME);
							relMRAttrMap.put(ATTRIBUTE_DOC_IN, stDocIn);
                            }
						String stPlanningReq = domPart.getInfo(context, EngineeringConstants.SELECT_PLANNING_REQUIRED);
						if(EngineeringConstants.STR_YES.equalsIgnoreCase(stPlanningReq)
								&& EngineeringConstants.ERP_STATUS_NOTACTIVE.equalsIgnoreCase(stERPStatus)) {
							throw new Exception( EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", new Locale(strLanguage), "emxMBOM.PartMaster.4"));
                            }
                            //Modification end for 2012 Migration to Common Components

                            }

					String sPR = domPart.getInfo(context, EngineeringConstants.SELECT_PLANNING_REQUIRED);
					boolean planningReq = false;
					if (UIUtil.isNotNullAndNotEmpty(sPR) && "Yes".equals(sPR)) {
						planningReq = true;
					}
					
					if("true".equalsIgnoreCase(sPlantType)) {
                                     leadPlantFound = true;
                                  }
					else
                                    leadPlantFound = false;

					DomainRelationship domRelMR = checkForPendingMRRel(context, sPartId, sObjectId);
                    if(domRelMR == null){
						domRelMR = addMR(context, sObjectId, sPartId, sChangeId, relMRAttrMap);
					}
                  //Added for IR-217397V6R2014----Start
                    else {
                    	throw new Exception( EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", new Locale(strLanguage), "emxMBOM.Alert.UniqueMR"));
                    }
                  //Added for IR-217397V6R2014----End
					if(UIUtil.isNotNullAndNotEmpty(domRelMR.toString()) && planningReq){
                    	try{
        					ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
							MqlUtil.mqlCommand(context, "add connection $1 from $2 torel $3", strAssocPlants,sParentOID,domRelMR.toString());
						}
						catch (Exception ex){
							throw new FrameworkException(ex.getMessage());
        				}
						finally {
							ContextUtil.popContext(context);
						}
					}

					//if added plant is lead plant
					if(leadPlantFound){
						String sCurrentDocInId   = null;
						String sVault = domPart.getInfo(context,DomainConstants.SELECT_VAULT);

						StringList objectSelects = new StringList();
						objectSelects.add(DomainObject.SELECT_NAME);
						objectSelects.add(DomainObject.SELECT_ID);

						MapList findObjects = DomainObject.findObjects(context,
								"*",
								stDocIn,
								"*",
								"*",
								sVault,
								null,
								false,
								objectSelects);

						if(!findObjects.isEmpty()){
							Map mp = (Map)findObjects.get(0);
							sCurrentDocInId = (String)mp.get(DomainObject.SELECT_ID);
						}

						// requires 1. PartId, 2.PlantObjectId, 3. MR relId, 4. ERP Status
						// 5. Plant Type, 6. Doc-in, 7. docIn id 8. Make-Buy

						HashMap hmParams = new HashMap();
						hmParams.put("partId", sPartId);
						hmParams.put("plantId", sObjectId);
						hmParams.put("manuRelId", domRelMR.toString());
						hmParams.put("ERPStatus", stERPStatus);
						hmParams.put("plantType", "Lead");
						hmParams.put("DocIn", stDocIn);
						hmParams.put("DocInId", sCurrentDocInId);
						hmParams.put("MakeBuy", stMakeBuy);

						changePlantToLeadPlant(context, hmParams);
						leadPlantFound = false;
					}//End of if(leadPlantFound)

                        //creating a returnMap having all the details abt the changed row.
					HashMap returnMap = new HashMap();
					returnMap.put("oid", sObjectId);
					returnMap.put("rowId", sRowId);
					returnMap.put("pid", sPartId);
					returnMap.put("relid", domRelMR.toString());
					returnMap.put("markup", markup);
					returnMap.put("columns", columnsMap);
					mlItems.add(returnMap);  //returnMap having all the details abt the changed row.
				}
				else if ("cut".equals(markup)){
					//Logic for ur CUT Opearation
					//creating a returnMap having all the details abt the changed row.
					HashMap returnMap = new HashMap();
					returnMap.put("oid", sObjectId);
					returnMap.put("rowId", sRowId);
					returnMap.put("relid", sRelId);
					returnMap.put("markup", markup);
					returnMap.put("columns", columnsMap);
					mlItems.add(returnMap);
				}
				else if ("resequence".equals(markup)){
					//Logic for ur resequence Opearation
					//creating a returnMap having all the details abt the changed row.
					HashMap returnMap = new HashMap();
					returnMap.put("oid", sObjectId);
					returnMap.put("rowId", sRowId);
					returnMap.put("relid", sRelId);
					returnMap.put("markup", markup);
					returnMap.put("columns", columnsMap);
					mlItems.add(returnMap);
				}
			}
			doc.put("Action", "success"); //Here the action can be "Success" or "refresh"
			doc.put("changedRows", mlItems);//Adding the key "ChangedRows" which having all the data for changed Rows
		}
		catch(Exception e){
			doc.put("Action", "ERROR"); // If any exeception is there send "Action" as "ERROR"
			doc.put("Message", e.getMessage()); // Error message to Display
		}
		return doc;
	}
	
	//Added for ENG Convergence start	
	/**
	 * To display Markup Save and SaveAs commands only if EC Part is Released 
	 * @param context ematrix context
	 * @param args contains Program map
	 * @return String
	 * @throws Exception if any operation fails.
	 * @since R216
	 */
	public boolean isMarkupCreateAllowed(Context context,
			String[] args) throws Exception {
		 HashMap programMap = (HashMap) JPO.unpackArgs(args);			
		 String getWhereUsed = (String) programMap.get("partWhereUsed");
		 String BOMViewMode = (String) programMap.get("BOMViewMode");
		 if("RollUp".equalsIgnoreCase(BOMViewMode)){
			 return false;
		 }
		 String strPartId = (String) programMap.get("objectId");	
		 DomainObject dObj = new DomainObject(strPartId);
		 
		 StringList objectSelect = new StringList(2);
		 objectSelect.addElement(DomainObject.SELECT_POLICY);
		 objectSelect.addElement(DomainObject.SELECT_CURRENT);
		 
		 Map objectInfo = dObj.getInfo(context, objectSelect);		 
		 
		 String strPolicy = (String) objectInfo.get(DomainObject.SELECT_POLICY);
		 String strCurrent = (String) objectInfo.get(DomainObject.SELECT_CURRENT);
		 
		 if ( strPolicy.equals( PropertyUtil.getSchemaProperty(context, "policy_ConfiguredPart") ) ) {
			 return false;
		 }
		 
         String STATE_REVIEW =  PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Review");
         String STATE_APPROVED =  PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Approved");
         
		if(strCurrent.equalsIgnoreCase(STATE_REVIEW)||strCurrent.equalsIgnoreCase(STATE_APPROVED)){
			return false;
		}
	   else if ("true".equalsIgnoreCase(getWhereUsed)) {
	   		String policyClassification = EngineeringUtil.getPolicyClassification(context, strPolicy);
			return !("Unresolved".equals(policyClassification) || "Development".equals(policyClassification));
		}
		else {
			return true;
		}

	}
	
	 /**
     * Gets the CommandType and returns True for SaveAs Command.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing "commandType"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/

    public String isConnectedtoCA(Context context,String partId,String chgid, String relPartre)throws Exception
    {
        String isconnected="false";
        String pid=partId;
        String cid=chgid;
        String querycommand="";

        if (relPartre == null)
        {
			relPartre = PropertyUtil.getSchemaProperty(context,"relationship_ChangeAffectedItem");
		}

        try
        {
            querycommand="print bus"+" "+"$1"+" "+ "select $2 dump  $3";
            String sResultPartconnectedtoChangeProcess = MqlUtil.mqlCommand(context,querycommand,pid,"to["+relPartre+"|from.id=='"+cid+"']","|").trim();
            if(sResultPartconnectedtoChangeProcess.equalsIgnoreCase("True"))
            {
                isconnected="true";
            }
        }
        catch(Exception e)
        {
            throw e;
        }
        return isconnected;
    }
    

	 /**
    * Checks if part is already connected to the given Change Action.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param part id    
    * @param ca id
    * @return        true if CA is already connected to Part else false.
    * @throws        Exception if the operation fails
    * @since         EngineeringCentral R419
    **/

   public String isChangeObjectsInProposed(Context context,String partId,String chgid)throws Exception
   {
       String isAssociated="false";
       String querycommand="";
       StringList selectStmts = new StringList(SELECT_ID);
      try
       {
    	Map proposedCAData  = com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil.getChangeObjectsInProposed(context, selectStmts, new String[]{partId}, 1);
  		MapList proposedchangeActionList = (MapList)proposedCAData.get(partId);
  		for (int i =0; i<proposedchangeActionList.size();i++){
  			Map caDetails =  (Map)proposedchangeActionList.get(i);
  			String sCAId = (String)caDetails.get(SELECT_ID);
  			if(UIUtil.isNotNullAndNotEmpty(sCAId) && sCAId.equals(chgid)){
  				isAssociated = "true";
  				break;
  			}
  		}         
       }
       catch(Exception e)
       {
           throw e;
       }
       return isAssociated;
   }
                                
 	/**
     * Gets all the CR Create state Used with the excludeOIDprogram setting
     * of Autonomy Search in case Save SaveAS functionality
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds a packed HashMap.
     * @return a StringList that contains.
     * @throws Exception if the operation fails.
     * @since Engineering Central - X3
     */

    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getExcludedCR(Context context, String args[]) throws Exception
    {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
        StringList excludeIdList = new StringList();
        StringList busSelects = new StringList(1);
        busSelects.addElement(DomainConstants.SELECT_ID);

        try
        {			
			String strWhereClause = "(current == \"" + PART_MARKUP_PROPOSED_STATE + "\" || current == \"" + POLICY_PART_MARKUP_APPROVED_State + "\")";
			String strPartId = (String) programMap.get("objectId");

			if (strPartId != null && strPartId.length() > 0)
			{
				DomainObject doPart = new DomainObject(strPartId);											
				String RELATIONSHIP_APPLIED_MARKUP = PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup");			
				StringList strlObjectSelects = new StringList(2);										
				strlObjectSelects.add(DomainConstants.SELECT_ID);				
											
				MapList mapListMarkups = doPart.getRelatedObjects(context,
						DomainConstants.RELATIONSHIP_EBOM_MARKUP,
						PropertyUtil.getSchemaProperty(context,"type_ItemMarkup"),
						strlObjectSelects,
						null, true, true, (short) 0, strWhereClause, null,0);				

				Iterator itrMarkup = mapListMarkups.iterator();
				while(itrMarkup.hasNext()) {
					Map mMarkupMap = (Map)itrMarkup.next();
					String strMarkupId = mMarkupMap.get("id").toString();	
					DomainObject dMarkupObj = new DomainObject(strMarkupId);								
					String strCRId = dMarkupObj.getInfo(context, "to[" + RELATIONSHIP_APPLIED_MARKUP + "].from.to["+RELATIONSHIP_CHANGE_ACTION+"].from.id");
					
					if(UIUtil.isNotNullAndNotEmpty(strCRId))
						excludeIdList.add(strCRId);
				}									
			}
        } catch(Exception e){
            throw e;
        } finally {
            return excludeIdList;
        }
    } 
       
    /* This method getImplementedItem gets objects connected with Implemnted Item relationship
	 * @param context The ematrix context of the request.
	 * @programMap args This string contains following arguments:
	 *       
	 * @throws Exception
	 * @throws FrameworkException
	 * @since EngineeringCentral R216
	 */
    public String getImplementedItem(Context context, String args[])	throws Exception
	{
		String strPartId = args[0];
		String strCAId = args[1];

		String strNewPartId = null;

		StringList strlPartSelects =  new StringList(2);
		strlPartSelects.add(SELECT_NAME);
		strlPartSelects.add(SELECT_TYPE);

		DomainObject doPart = new DomainObject(strPartId);

		Map mapPartDetails = doPart.getInfo(context, strlPartSelects);
		String strPartName = (String) mapPartDetails.get(SELECT_NAME);
		String strPartType = (String) mapPartDetails.get(SELECT_TYPE);

		IChangeAction iCa=EngineeringUtil.getChangeAction(context, strCAId);
		List realizedChangeList=iCa.getRealizedChanges(context);
		Iterator realizedChangeItr =realizedChangeList.iterator();
		StringList objectSelects=new StringList(DomainConstants.SELECT_ID);
		objectSelects.add(DomainConstants.SELECT_NAME);
		objectSelects.add(DomainConstants.SELECT_TYPE);
		while(realizedChangeItr.hasNext())
		{
			Map realizeMap=new HashMap();
			IRealizedChange realizedChange=(IRealizedChange) realizedChangeItr.next();
			DomainObject domainObj=new DomainObject(realizedChange.getWhere().getName());
			Map objInfo = domainObj.getInfo(context, objectSelects);
			if(!objInfo.isEmpty()){
				String sType = (String)objInfo.get(SELECT_TYPE);
				String sName = (String)objInfo.get(SELECT_NAME);
				if(UIUtil.isNotNullAndNotEmpty(sType) && sType.equals(strPartType) && UIUtil.isNotNullAndNotEmpty(sName) && sName.equals(strPartName)){
					strNewPartId =  (String)objInfo.get(SELECT_ID);
					break;
				}
			}
		}
		return strNewPartId;
	}

    /* This method disconnectMarkups disconnects markups connected to CA.
	 * @param context The ematrix context of the request.
	 * @programMap args This string array contains following arguments:
	 *          0 - The programMap
	 *
	 * @throws Exception
	 * @throws FrameworkException
	 * @since EngineeringCentral R216
	 */
	public int disconnectMarkups(Context context, String args[])throws Exception
	{
		try
		{

			String strCAObjectId = args[0];
			String strAffectedItemId = args[1];
			
			String strMarkupRelName = RELATIONSHIP_APPLIED_MARKUP;
			
			String objectWhere = "(to[" + strMarkupRelName + "].from.id == " + strCAObjectId + ")";
					
			DomainObject affectedObject = new DomainObject(strAffectedItemId);
		
			
			if(affectedObject.isKindOf(context, DomainConstants.TYPE_PART)){
		      MapList mapListMarkups =    affectedObject.getRelatedObjects(context,
                      RELATIONSHIP_EBOM_MARKUP,
                      "*",
                      new StringList(SELECT_ID),
                      null,
                      false,
                      true,
                      (short)1,
                      objectWhere,
                      null,0);
		      

              Iterator itr = mapListMarkups.iterator();
              while(itr.hasNext())
              {
                  Map m2=(Map) itr.next();
                  String strID1 = (String) m2.get(SELECT_ID);
                  DomainObject dobjBOMMarkup = new DomainObject(strID1);
                  dobjBOMMarkup.deleteObject(context);

                  
              }                
			}
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
		return 0;
	}

	/* This method disconnectAffectedItemCheck Checks if any markup is in Approved state before disconnecting Affected Item.
	 * @param context The ematrix context of the request.
	 * @programMap args This string array contains following arguments:
	 *          0 - The programMap
	 *
	 * @throws Exception
	 * @throws FrameworkException
	 * @since EngineeringCentral R216
	 */
	public int disconnectAffectedItemCheck(Context context, String args[])throws Exception
	{
		try
		{
			String strCAObjectId = args[0];
			String strAffectedItemId = args[1];
			String strApprovedMarkupItems = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.AffectedItems.HasApprovedMarkups");
			String strMarkupRelName = RELATIONSHIP_APPLIED_MARKUP;
			
			String objectWhere = "(to[" + strMarkupRelName + "].from.id == " + strCAObjectId + ") && (current == \"" + DomainConstants.STATE_EBOM_MARKUP_APPROVED + "\")";
		
			DomainObject affectedObject = new DomainObject(strAffectedItemId);
			String objType = affectedObject.getInfo(context, SELECT_TYPE);
			String objName = affectedObject.getInfo(context, SELECT_NAME);
			String ObjRev = affectedObject.getInfo(context, SELECT_REVISION);
			
			
			if(affectedObject.isKindOf(context, DomainConstants.TYPE_PART)){				
		      MapList mapListMarkups =    affectedObject.getRelatedObjects(context,
                      RELATIONSHIP_EBOM_MARKUP,
                      "*",
                      new StringList(SELECT_ID),
                      null,
                      false,
                      true,
                      (short)1,
                      objectWhere,
                      null,
                      0);
		      

		      if (mapListMarkups.size() > 0)
				{
					 emxContextUtil_mxJPO.mqlNotice(context, strApprovedMarkupItems + objType +" "+ objName +" "+ObjRev);
					return 1;
				}  
		      else
		    	  return 0;
			}
			else
				return 0;
			
		}
		catch(Exception Ex)
		{
			Ex.printStackTrace();
			throw Ex;
		}
		
	}

	/**
     * Get All the Markups connected to CA
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *             0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllMarkupsConnectedToCA(Context context,String[] args)throws Exception
    {

        ContextUtil.pushContext(context,"User Agent","",context.getVault().getName());
        MapList MarkupIds=new MapList();
        HashMap programMap=(HashMap)JPO.unpackArgs(args);
        String objectId=(String)programMap.get("objectId");

        DomainObject domobj = DomainObject.newInstance(context);
        domobj.setId(objectId);

        StringList selectStmts = new StringList(3);
        selectStmts.addElement(DomainConstants.SELECT_ID);        

        StringList selectRelStmts = new StringList(1);
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

        //need to select all types to include old markup types
        String strTypes = "*"; 

       MarkupIds = domobj.getRelatedObjects(context,RELATIONSHIP_APPLIED_MARKUP,strTypes,selectStmts,selectRelStmts,false,true,(short)1,null,null,0);

       ContextUtil.popContext(context);

        return MarkupIds;

    }
    
    /* This method getMarkupRelatedECOCAs gets Related ECO/CAs of the Markups.
	 * @param context The ematrix context of the request.
	 * @programMap args This string array contains following arguments:
	 *          0 - The programMap
	 *
	 * @throws Exception
	 * @throws FrameworkException
	 * @since EngineeringCentral X3
	 */
    public Vector getMarkupRelatedECOCAs(Context context, String[] args)throws Exception {    	

		Vector vAffectedItemsRelatedECOCAs = new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");				
		HashMap mColumnMap = (HashMap) programMap.get("columnMap");
		String strColname = (String) mColumnMap.get("name");				
		HashMap paramList = (HashMap) programMap.get("paramList");
		String strSuiteDir = (String) paramList.get("SuiteDirectory");
		String strJsTreeID = (String) paramList.get("jsTreeID");
		String strParentObjectId = (String) paramList.get("objectId");
		String strFullName = null;
		StringList slIds = new StringList();	
		String strDest = "";
		StringList objectSelects = new StringList();	
		
		if(UIUtil.isNotNullAndNotEmpty(strColname) && "RelatedECO".equalsIgnoreCase(strColname)) {
			objectSelects.addElement("to["+RELATIONSHIP_APPLIED_MARKUP+"|from.type == "+ TYPE_ECO +"].from.name");
			objectSelects.addElement("to["+RELATIONSHIP_APPLIED_MARKUP+"|from.type == "+ TYPE_ECO +"].from.id");
		} else {
			objectSelects.addElement("to["+RELATIONSHIP_APPLIED_MARKUP+"|from.type == '"+ ChangeConstants.TYPE_CHANGE_ACTION +"'].from.name");
			objectSelects.addElement("to["+RELATIONSHIP_APPLIED_MARKUP+"|from.type == '"+ ChangeConstants.TYPE_CHANGE_ACTION +"'].from.id");
		}
		
		Iterator itrML = objectList.iterator(); 
		while (itrML.hasNext()) {
			Map mMarkups = (Map) itrML.next();
			String sObjectId = (String) mMarkups.get(DomainConstants.SELECT_ID);
			slIds.add(sObjectId);
		}
		String[] AIids = new String[slIds.size()];
		slIds.toArray(AIids);

		MapList objList = DomainObject.getInfo(context, AIids, objectSelects);		
		Iterator objItr1 = objList.iterator();

		while (objItr1.hasNext()) {
			strDest = "";
			Map m = (Map) objItr1.next();

			String name = (String) m.get("to["+RELATIONSHIP_APPLIED_MARKUP+"].from.name");			
			String id = (String) m.get("to["+RELATIONSHIP_APPLIED_MARKUP+"].from.id");
					
			if((UIUtil.isNotNullAndNotEmpty(id)) && (UIUtil.isNotNullAndNotEmpty(name))) {
					if (id == null) {
						vAffectedItemsRelatedECOCAs.add("");
					} else {
						if (!"".equals(strDest)) {
							strDest += ",";
						}
					// Constructing the HREF
					strFullName = "<A HREF=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp?mode=insert&amp;emxSuiteDirectory="
							+ XSSUtil.encodeForURL(context, strSuiteDir)
							+ "&amp;parentOID="
							+ XSSUtil.encodeForURL(context,strParentObjectId)
							+ "&amp;jsTreeID="
							+ XSSUtil.encodeForURL(context,strJsTreeID)
							+ "&amp;objectId="
							+ XSSUtil.encodeForURL(context,id)
							+ "', 'null', 'null', 'false', 'content')\" class=\"object\">"
							+ XSSUtil.encodeForHTML(context,name) + "</A>";
					strDest += strFullName;
				}
		}
			vAffectedItemsRelatedECOCAs.add(strDest);
	}
		return vAffectedItemsRelatedECOCAs;
    }		
    
    public int moveMarkups(Context context, String args[]) throws Exception {
	    try{
	    	String strRelId = args[2];    	
	    	String strOldCAId = args[0];
	    	String strPartId = args[1];
	    	
	    	String strNewCAId = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",strRelId,"from.id");		
			DomainObject dCAId = new DomainObject(strNewCAId);			
			
			DomainObject dPartObj = new DomainObject(strPartId);
			if(dPartObj.isKindOf(context, TYPE_PART)){
				StringList objectSelects = new StringList();			
				objectSelects.addElement("from["+RELATIONSHIP_EBOM_MARKUP+"].to.to["+RELATIONSHIP_APPLIED_MARKUP+"|from.id=="+strOldCAId+"].to.id");
				objectSelects.addElement("from["+RELATIONSHIP_EBOM_MARKUP+"].to.to["+RELATIONSHIP_APPLIED_MARKUP+"|from.id=="+strOldCAId+"].id");
						
				String[] affectedItemsList = new String[1];
				affectedItemsList[0] = strPartId;
				MapList mlMarkups = DomainObject.getInfo(context, affectedItemsList, objectSelects);				
				Iterator<Map> markupItr = mlMarkups.iterator();
				Map map = null;
				String strMarkupRelIds = "";
				String strMarkIds = "";
				while(markupItr.hasNext()) {
					map = markupItr.next();		
					if(map.containsKey("from["+RELATIONSHIP_EBOM_MARKUP+"].to.to["+RELATIONSHIP_APPLIED_MARKUP+"].id") 
							&& map.containsKey("from["+RELATIONSHIP_EBOM_MARKUP+"].to.to["+RELATIONSHIP_APPLIED_MARKUP+"].to.id")) {
						
						strMarkupRelIds = (String)map.get("from["+RELATIONSHIP_EBOM_MARKUP+"].to.to["+RELATIONSHIP_APPLIED_MARKUP+"].id");								
						strMarkIds = (String)map.get("from["+RELATIONSHIP_EBOM_MARKUP+"].to.to["+RELATIONSHIP_APPLIED_MARKUP+"].to.id");
						
						if(UIUtil.isNotNullAndNotEmpty(strMarkupRelIds) && UIUtil.isNotNullAndNotEmpty(strMarkIds)) {							
							StringList strRelIds = FrameworkUtil.split(strMarkupRelIds, "\\a");
							for (int i = 0; i < strRelIds.size(); i++) {
								DomainRelationship.setFromObject(context, strRelIds.get(i).toString(), dCAId);					
							}
						}
					}
				}
			}		    
			return 0;
	    } catch (Exception e){
	    	e.printStackTrace();
	    	return 1;
	    }
    }
    
    /**
     * Get All the Markups connected to a Part
     *
     * @param context the eMatrix <code>Context</code> object
     * @param markupIds    holds the markup ids connected to a Part.
     * @return        a boolean value as true if the MarkUp is from BGTP. 
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral X3
     **/
    public boolean validateBGTPItemMarkups(Context context,StringList markupIds) throws Exception{
    	boolean sBGTPMarkup = false;
    	for(int i=0;i<markupIds.size();i++) {
    		String strObjectId1 = (String) markupIds.get(i);
    		sBGTPMarkup = ReleasePhaseManager.isItemMarkupForSetToProduction(context,strObjectId1);
    		if (sBGTPMarkup)
    		{
    			break;
    		}
    	}
	   return sBGTPMarkup;
    }
   //For ECM Convergence Start 
    /**
     * Delete Check trigger on type Part 
     * If any approved Markups connected to part then dont allow deleting the Part
     * @param context the eMatrix <code>Context</code> object
     * @param  args[]  holds the Part Id.
     * @return        a boolean value as true if no approved MarkUp connected to Part. 
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral R419
     **/
  	public int checkApprovedMarkupsConnectedToPart(Context context, String args[])throws Exception
  	{
  		try
  		{
  			String strPartId = args[0];
  			
  			String strApprovedMarkupItems = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.AffectedItems.HasApprovedMarkups");
  			String strMarkupRelName = RELATIONSHIP_EBOM_MARKUP;
  			
  			String objectWhere = "(current == \"" + DomainConstants.STATE_EBOM_MARKUP_APPROVED + "\")";
  		
  			DomainObject affectedObject = new DomainObject(strPartId);
  			String objType = affectedObject.getInfo(context, SELECT_TYPE);
  			String objName = affectedObject.getInfo(context, SELECT_NAME);
  			String ObjRev = affectedObject.getInfo(context, SELECT_REVISION);
  			  			 						
  		    MapList mapListMarkups =    affectedObject.getRelatedObjects(context,
  		    		  strMarkupRelName,
                        "*",
                        new StringList(SELECT_ID),
                        null,
                        false,
                        true,
                        (short)1,
                        objectWhere,
                        null,0);
  		      

  		      if (mapListMarkups.size() > 0)
  				{
  					 emxContextUtil_mxJPO.mqlNotice(context, strApprovedMarkupItems + objType +" "+ objName +" "+ObjRev);
  					return 1;
  				}  
  		      else
  		    	  return 0;
  			
  			
  		}
  		catch(Exception Ex)
  		{
  			Ex.printStackTrace();
  			throw Ex;
  		}
  		
  	}
    /**
     * Delete Check trigger on Relationship Applied Markup 
     * If any approved Markups connected to change then dont allow deleting the Change
     * @param context the eMatrix <code>Context</code> object
     * @param  args[]  holds the Markup Id.
     * @return        a boolean value as true if no approved MarkUp connected to Change. 
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral R419
     **/
  	public int checkApprovedMarkups(Context context, String args[])throws Exception
  	{
  		try
  		{
  			
  			String skipTriggerCheck = context.getCustomData("MX_SKIP_APPLIED_MARKUP_DELETE_CHECK");
            if(skipTriggerCheck != null && "true".equalsIgnoreCase(skipTriggerCheck))
            {
                return 0;
            }
  		
  			String strMarkupId = args[0];
  			String strApprovedMarkupItems = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Change.HasApprovedMarkups");
  			
  			
  			
  		    DomainObject markupObj = new DomainObject (strMarkupId);

  		      if (DomainConstants.STATE_EBOM_MARKUP_APPROVED.equals(markupObj.getInfo(context, DomainConstants.SELECT_CURRENT)))
  				{
  					emxContextUtil_mxJPO.mqlNotice(context, strApprovedMarkupItems );
  					return 1;
  				}  
  		      else
  		    	  return 0;
  			
  			
  		}
  		catch(Exception Ex)
  		{
  			Ex.printStackTrace();
  			throw Ex;
  		}
  		
  	}
    /**
     * Delete Action trigger on Relationship Applied Markup 
     * If any approved Markups connected to change then dont allow deleting the Change. if no approved markup then on deletion of change deletes the connected Markup.
     * @param context the eMatrix <code>Context</code> object
     * @param  args[]  holds the Markup Id.
     * @return        a boolean value as true if no approved MarkUp connected to Change. 
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral R419
     **/
  	public int deleteConnectedMarkups(Context context, String args[])throws Exception
  	{
  		try
  		{
  		  
  		String skipTriggerCheck = context.getCustomData("MX_SKIP_APPLIED_MARKUP_DELETE_CHECK");
          if(skipTriggerCheck != null && "true".equalsIgnoreCase(skipTriggerCheck))
          {
              return 0;
          }
  			
  			String strMarkupId = args[1];
  			String strCAId = args[0];
  			String strDeletedObjectIds = args[2];
  			StringList slDeletedObjectList = new StringList();
  			if(UIUtil.isNotNullAndNotEmpty(strDeletedObjectIds)){
  				slDeletedObjectList = FrameworkUtil.split(strDeletedObjectIds, "|");
  			}
  			if( slDeletedObjectList.isEmpty() || (slDeletedObjectList.size()>0 && !slDeletedObjectList.contains(strMarkupId))){
	  			DomainObject markupObj = new DomainObject (strMarkupId);  			 			
	  			markupObj.deleteObject(context);
  			}
  			
  		}
  		catch(Exception Ex)
  		{
  			Ex.printStackTrace();
  			throw Ex;
  		}
  		return 0;
  	}
  	
    /**
     * Delete Override trigger on type Part 
     * If any approved Markups connected to Part then dont allow deleting the Change. if no approved markup then on deletion of Part deletes the connected Markup.
     * @param context the eMatrix <code>Context</code> object
     * @param  args[]  holds the Part Id.
     * @return        a boolean value as true if no approved MarkUp connected to Part. 
     * @throws        Exception if the operation fails
     * @since         EngineeringCentral R419
     **/
  	
  	public int deleteConnectedMarkupsConnectedToPart(matrix.db.Context context, String[] args)throws Exception
  	{
  		try
  		{
  			String strPartId = args[0];
  			
  			
  			String strMarkupRelName = RELATIONSHIP_EBOM_MARKUP;
  			  			  		
  			DomainObject affectedObject = new DomainObject(strPartId);  			
  			
  						
  		      MapList mapListMarkups =    affectedObject.getRelatedObjects(context,
  		    		  strMarkupRelName,
                        "*",
                        new StringList(SELECT_ID),
                        new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
                        false,
                        true,
                        (short)1,
                        null,
                        null,0);
  		      
  		      Iterator itr = mapListMarkups.iterator();
  		    context.setCustomData("MX_SKIP_APPLIED_MARKUP_DELETE_CHECK", "TRUE");
                while(itr.hasNext())
                {
                    Map m2=(Map) itr.next();
                    String strID1 = (String) m2.get(SELECT_ID);
                    String strRelId1= (String) m2.get(SELECT_RELATIONSHIP_ID);
                    DomainObject dobjBOMMarkup = new DomainObject(strID1);
                    dobjBOMMarkup.deleteObject(context);    
                    context.removeFromCustomData("MX_SKIP_APPLIED_MARKUP_DELETE_CHECK");
                }            

  			
  		}
  		catch(Exception Ex)
  		{
  			Ex.printStackTrace();
  			throw Ex;
  		}
  		return 0;
  		
  	}
  	//For ECM Convergence End
}

