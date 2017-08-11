/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 */

import java.io.BufferedWriter;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.AccessList;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MatrixWriter;
import matrix.db.Policy;
import matrix.db.PolicyItr;
import matrix.db.PolicyList;
import matrix.db.Relationship;
import matrix.db.RelationshipType;
import matrix.db.RelationshipWithSelect;
import matrix.db.RelationshipWithSelectItr;
import matrix.db.RelationshipWithSelectList;
import matrix.db.Role;
import matrix.db.UserItr;
import matrix.db.UserList;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.CacheUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.OrganizationUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PolicyUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.SetUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.domain.util.VaultUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.dassault_systemes.enovia.bom.ReleasePhase;
import com.dassault_systemes.enovia.bom.modeler.interfaces.services.IBOMService;
import com.dassault_systemes.enovia.bom.modeler.util.BOMMgtUtil;
import com.matrixone.apps.engineering.ChartUtil;
import com.matrixone.apps.engineering.EBOMAutoSync;
import com.matrixone.apps.engineering.EBOMMarkup;
import com.matrixone.apps.engineering.ECO;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.EBOMFloat;
import com.matrixone.apps.engineering.IPartMaster;
import com.matrixone.apps.engineering.Part;
import com.matrixone.apps.engineering.PartDefinition;
import com.matrixone.apps.engineering.PartFamily;
import com.matrixone.apps.engineering.RelToRelUtil;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UISearchUtil;
import com.matrixone.apps.framework.ui.UITableCommon;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.jdom.output.XMLOutputter;
import com.matrixone.jsystem.util.StringUtils;

import com.matrixone.search.AttributeRefinement;
import com.matrixone.search.ComplexRefinement;
import com.matrixone.search.SearchRecord;
import com.matrixone.search.SearchRefinement;
import com.matrixone.search.SearchResult;
import com.matrixone.search.SortRefinement;
import com.matrixone.search.TaxonomyRefinement;
import com.matrixone.search.XLSearch;
import com.dassault_systemes.enovia.bps.widget.UIWidget;
import com.dassault_systemes.enovia.e6w.foundation.jaxb.FieldValue;
import com.dassault_systemes.enovia.e6w.foundation.jaxb.Status;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrder;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;
import com.dassault_systemes.enovia.partmanagement.modeler.impl.PartCollaborationService;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.services.IPartCollaborationService;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.validator.IPartValidator;

import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.input.IPartIngress;

/**
 * The <code>emxPartBase</code> class contains implementation code for emxPart.
 *
 * @version EC 9.5.JCI.0 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxECPartBase_mxJPO extends emxBOMPartManagement_mxJPO
{
    /** helper for promote action triggers. */
    public static final String POLICY_ECR = PropertyUtil.getSchemaProperty("policy_ECR");
    public static final String STATE_ECR_COMPLETE = PropertyUtil.getSchemaProperty("policy", POLICY_ECR, "state_Complete");
    public static final String ATTRIBUTE_REFERENCE_TYPE = PropertyUtil.getSchemaProperty("attribute_ReferenceType");
    public static final String INTERFACE_PART_FAMILY_REFERENCE = PropertyUtil.getSchemaProperty("interface_PartFamilyReference");
    public static final String SYMB_U = "U" ;
    public static final String SYMB_R = "R" ;
    public static final String SYMB_M = "M" ;
    public static final String RELATIONSHIP_PART_FAMILY_REFERENCE = PropertyUtil.getSchemaProperty("relationship_PartFamilyReference");
   

    /** The EBOM History Relationship Object. */
    static protected final RelationshipType _ebomHistory =
            new RelationshipType(DomainConstants.RELATIONSHIP_EBOM_HISTORY);

    /** emxContextUtil for push/pop context */
    static protected emxContextUtil_mxJPO contextUtil = null;

    /** relationship "Manufacturer Equivalent". */
    public static final String RELATIONSHIP_MANUFACTURER_EQUIVALENT =
        PropertyUtil.getSchemaProperty("relationship_ManufacturerEquivalent");

    /** relationship "Manufacturer Equivalent". */
    public static final String RELATIONSHIP_MANUFACTURER_EQUIVALENT_HISTORY =
        PropertyUtil.getSchemaProperty("relationship_ManufacturerEquivalentHistory");

    /** The Manufacturer Equivalent History Relationship Object. */
    static protected final RelationshipType _mepHistory =
            new RelationshipType(RELATIONSHIP_MANUFACTURER_EQUIVALENT_HISTORY);

    /** type "Substitute". */
    public static final String TYPE_SUBSTITUTE =
        PropertyUtil.getSchemaProperty("type_Substitute");

    /* Added for Substitue Part */
    static final String SELECT_STANDARD_COMPONENT_EBOM_STRING =
           "from[" + RELATIONSHIP_STANDARD_COMPONENT + "].to.to[" + RELATIONSHIP_EBOM + "].";

    /** selects the standard components parent objectId. */
    public static final String SELECT_STANDARD_COMPONENT_EBOM_FROM_ID =
          SELECT_STANDARD_COMPONENT_EBOM_STRING + "from.id";

    /** selects the standard components quantity in the ebom. */
    public static final String SELECT_STANDARD_COMPONENT_EBOM_QUANTITY =
          SELECT_STANDARD_COMPONENT_EBOM_STRING + "attribute[" + ATTRIBUTE_QUANTITY + "]";
    /*Static string for substitute part*/

	public static final String POLICY_CLASSIFICATION = "policy.property[PolicyClassification].value"; // Added for HF-190770

    static final int LT = 0;
    static final int GT = 1;
    static final int EQ = 2;
    static final int LE = 3;
    static final int GE = 4;
    static final int NE = 5;

/** The String which is used to hold Key for AllocationResp rel id. */
   protected static final String KEY_ALLOCATION_REL_ID = "AllocRespRelId";
/** The String which is used to display content as label. */
   protected static final String KEY_LABEL_LOC_EQUIV   = "labelLocEquiv";
/** The String which is used to display content as label. */
   protected static final String KEY_AVLData   = "AVLData";

   public static final String RELATIONSHIP_DESIGN_RESPONSIBILITY =
                 PropertyUtil.getSchemaProperty("relationship_DesignResponsibility");

   public static final String DESIGN_RESPONSIBILITY_NAME = "to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY + "]";

   public static final String RELATIONSHIP_MANUFACTURING_RESPONSIBILITY =
             PropertyUtil.getSchemaProperty("relationship_ManufacturingResponsibility");

   public static final String RELATIONSHIP_PART_FAMILY_MEMBER =
             PropertyUtil.getSchemaProperty("relationship_PartFamilyMember");
    /** The AFFECTED_ITEM Relationship Object. Added by Arun B */
   public static final String RELATIONSHIP_AFFECTED_ITEM =
             PropertyUtil.getSchemaProperty("relationship_AffectedItem");
   /** The PART VERSION Relationship Object.  Added by Arun B*/
   public static final String RELATIONSHIP_PART_VERSION =
        PropertyUtil.getSchemaProperty("relationship_PartVersion");
   public static final String RELATIONSHIP_PART_REVISION =
       PropertyUtil.getSchemaProperty("relationship_PartRevision");
   public static final String RELATIONSHIP_ASSIGNED_AFFECTED_ITEM =
    	PropertyUtil.getSchemaProperty("relationship_AssignedAffectedItem");
   public final static String RELATIONSHIP_EBOM_PENDING = PropertyUtil.getSchemaProperty("relationship_EBOMPending");
       /** policy "Part Specification". */
    public static final String POLICY_PART_SPECIFICATION =PropertyUtil.getSchemaProperty("policy_PartSpecification");
    /** state "Approved" for the "Part Specification" policy. */
    public static final String STATE_PART_SPECIFICATION_APPROVED = PropertyUtil.getSchemaProperty("policy",POLICY_PART_SPECIFICATION,"state_Approved");

   public static final String RELATIONSHIP_DERIVED = PropertyUtil.getSchemaProperty("relationship_Derived");
   private static final String ATTRIBUTE_DERIVED_CONTEXT = PropertyUtil.getSchemaProperty("attribute_DerivedContext");
   private static final String SELECT_FROM_DERIVED_IDS = "from[" + RELATIONSHIP_DERIVED + "]." + DomainConstants.SELECT_TO_ID;
   private static final String MARKUP_ADD = "add";
   private static final String MARKUP_NEW = "new";
   private static final String MARKUP_CUT = "cut";

   protected String LABEL_REVISION = "";
   protected String LABEL_PART_FAMILY = "";
   protected String LABEL_DESIGN_RESPONSIBILITY = "";
   protected String LABEL_ECO_TO_RELEASE = "";
   protected String LABEL_POLICY = "";
   protected String LABEL_VAULT = "";
   protected String LABEL_OWNER = "";
   protected String LABEL_PART_ORIGIN = "";
   protected String LABEL_MANUFACTURER = "";
   protected String LABEL_CUSTOM_REVISION_LEVEL = "";
   protected String LABEL_MANUFACTURING_EQUIVALENT = "";
   protected String LABEL_CLEAR = "";
   protected String LABEL_ORIGINATOR  = "";
   protected String LABEL_ACTIVEECRECO = "";

   public static final String PERSON_REQUEST_ACCESS_GRANTOR = PropertyUtil.getSchemaProperty("person_RequestAccessGrantor");
// Added for IR-021267V6R2011
   public static final String  RELATIONSHIP_EBOM_SUBSTITUTE =   PropertyUtil.getSchemaProperty("relationship_EBOMSubstitute");

	public static final String POLICY_PARTMARKUP = PropertyUtil.getSchemaProperty("policy_PartMarkup");
	public static final String proposedState = PropertyUtil.getSchemaProperty("policy", POLICY_PARTMARKUP, "state_Proposed");
	public static final String approvedState = PropertyUtil.getSchemaProperty("policy", POLICY_PARTMARKUP, "state_Approved");

	public static final String POLICY_ECO = PropertyUtil.getSchemaProperty("policy_ECO");

	private static final String SELECT_ATTRIBUTE_DEFAULT_PART_POLICY = "attribute[" + PropertyUtil.getSchemaProperty("attribute_DefaultPartPolicy") + "]";
	private static final String ATTRIBUTE_PART_MODE = PropertyUtil.getSchemaProperty("attribute_PartMode");
	private static final String SELECT_PART_MODE = "to[" + RELATIONSHIP_PART_REVISION + "].from.attribute[" + ATTRIBUTE_PART_MODE + "]";
	private static final String SELECT_RAISED_AGAINST_ECR = "to[" + RELATIONSHIP_RAISED_AGAINST_ECR + "].from[" + TYPE_ECR + "].name";
	private static final String SELECT_RAISED_AGAINST_ECR_CURRENT = "to[" + RELATIONSHIP_RAISED_AGAINST_ECR + "].from[" + TYPE_ECR + "].current";
	public static final String RELATIONSHIP_PRECISE_BOM = PropertyUtil.getSchemaProperty("relationship_PreciseBOM");
	public static final String RELATIONSHIP_ASSIGNED_PART = PropertyUtil.getSchemaProperty("relationship_AssignedPart");
	public static final String RELATIONSHIP_EBOM_SUBSTITUE = PropertyUtil.getSchemaProperty("relationship_EBOMSubstitute");
	public static final String RELATIONSHIP_GBOM = PropertyUtil.getSchemaProperty("relationship_GBOM");
	public static final String POLICY_CONFIGURED_PART = PropertyUtil.getSchemaProperty("policy_ConfiguredPart");
	public static final String REL_TO_EBOM_EXISTS = "to[" + RELATIONSHIP_EBOM + "]";
	public static final String SELECT_REL_FROM_EBOM_EXISTS = "from[" + RELATIONSHIP_EBOM + "]";
	public static final String SELECT_PART_TO_ECO_CURRENT = "to[" + RELATIONSHIP_AFFECTED_ITEM + "].from[" + TYPE_ECO + "].current";
	public static final String SELECT_PART_TO_ECR_CURRENT = "to[" + RELATIONSHIP_AFFECTED_ITEM + "].from[" + TYPE_ECR + "].current";
	public static final String SELECT_LAST_REVISION = "last.revision";
	public static final String SELECT_LAST_CURRENT = "last.current";


	// Added for SB Performance
	private String fnLength = "";
	private String fnDisplayLeadingZeros = "";

	public final static String RELATIONSHIP_PLBOM = PropertyUtil.getSchemaProperty("relationship_PLBOM");//"PLBOM";
	public final static String RELATIONSHIP_PLBOM_PENDING = PropertyUtil.getSchemaProperty("relationship_PLBOMPending");//"PLBOM Pending";

	//Widget
	public final static String MATRIX_DELIMITER = matrix.db.SelectConstants.cSelectDelimiter;

	/* IR-297719:Create/Clone triggers should relax TNR restriction for special Renault use cases - Added for VMP-Sync usecase*/
	public static final String SYNC_ENV_BYPASS_FOR_CLONE = "SYNC_ENV_BYPASS_FOR_CLONE";

	
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since EC 9.5.JCI.0.
     */
    public emxECPartBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
        contextUtil = new emxContextUtil_mxJPO(context, null);
        LABEL_ORIGINATOR = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originator",context.getSession().getLanguage());
        LABEL_ACTIVEECRECO = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.ActiveECRorECO",context.getSession().getLanguage());
        LABEL_REVISION = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Revision",context.getSession().getLanguage());
        LABEL_PART_FAMILY = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.PartFamily",context.getSession().getLanguage());
        LABEL_DESIGN_RESPONSIBILITY = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.DesignResponsibility",context.getSession().getLanguage());
        LABEL_ECO_TO_RELEASE = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.ECOToRelease",context.getSession().getLanguage());
        LABEL_POLICY = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Policy",context.getSession().getLanguage());
        LABEL_VAULT = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Vault",context.getSession().getLanguage());
        LABEL_OWNER = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",context.getSession().getLanguage());
        LABEL_PART_ORIGIN = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.PartOrigin",context.getSession().getLanguage());
        LABEL_MANUFACTURER = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.Manufacturer",context.getSession().getLanguage());
        LABEL_CUSTOM_REVISION_LEVEL = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.CustomRevisionLevel",context.getSession().getLanguage());
        LABEL_MANUFACTURING_EQUIVALENT = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.MfgEquivalent",context.getSession().getLanguage());
        LABEL_CLEAR = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",context.getSession().getLanguage());
//Added for IR-021267
        EBOM_FROZEN_STATES = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.EBOMFrozenStates");

        fnLength = EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.FindNumberLength");
    	fnDisplayLeadingZeros = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.FindNumberDisplayLeadingZeros");
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
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxPart invocation");
        }
        return 0;
    }


    /**
     * Clears the Start and End Effectivity dates on the EBOM or Manufacturer Equivalent connection
     * between this assembly and any child components.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *        0 - the symbolic name of the relationship to float, either
     *            relationship_ManufacturerEquivalent or
     *            relationship_EBOM (default if none specified)
     * @throws Exception if the operation fails.
     * @since EC 9.5.JCI.0.
     * @trigger TypePartReviseAction.
     */
    public void clearEBOMEffectivity(Context context, String[] args)
                    throws Exception
    {
        DebugUtil.debug("TypePartReviseAction:clearEBOMEffectivity");

        try
        {
            // This method is now also used to clear the Manufacturer Equivalent Rel attributes
            // First get the symbolic name of the relationship to expand on
            String rel_SymbolicName = args[0];
            String select = null;

            // If there is no relationship name passed in then default to EBOM
            if ("relationship_ManufacturerEquivalent".equals(rel_SymbolicName))
            {
                select = "next.from[" + DomainObject.RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].id";
            }
            else
            {
                select = "next.from[" + DomainObject.RELATIONSHIP_EBOM + "].id";
            }

            StringList sList = getInfoList(context, select);
            Iterator i = sList.iterator();

            HashMap attributes = new HashMap();
            attributes.put(DomainConstants.ATTRIBUTE_START_EFFECTIVITY_DATE, "");
            attributes.put(DomainConstants.ATTRIBUTE_END_EFFECTIVITY_DATE, "");

            String relId = null;

            while (i.hasNext())
            {
                relId = (String)i.next();

                // Set start and end effectivity to blank for this new revision of
                // assembly part
                DomainRelationship.setAttributeValues(context, relId, attributes);
            }

        }
        catch (Exception ex)
        {
            throw ex;
        }
    }





    /**
     * Synchronizes the changes made on an EBOM relationship with any Substitutes the EBOM has.
     *      When the Find Number or Reference Designator attributes are updated on an EBOM,
     *      we need to update the same attributes on any of its Substitue objects.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *        0 - the EBOM relationship id that is being updated
     *        1 - the attribute name being updated
     *        2 - the current value of the attribute
     *        3 - the new value of the attribute
     * @throws Exception if the operation fails.
     * @since 10.0.0.0.
     * @trigger RelationshipEBOMModifyAttributeAction.
     */
    public void syncSubstitutes(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("RelationshipEBOMModifyAttributeAction:syncSubstitutes");

        // args[] parameters
        String ebomRelId = args[0];
        String attrbName = args[1];
        String currentValue = args[2];
        String newValue = args[3];

        //Added for X3 Start
        StringList slEBOMSubstituteRel = new StringList();
        String  RELATIONSHIP_EBOM_SUBSTITUTE        =
        PropertyUtil.getSchemaProperty(context,"relationship_EBOMSubstitute");
        //Added for X3 end
        DebugUtil.debug("ebomRelId = " + ebomRelId);
        DebugUtil.debug("attrName  = " + attrbName);
        DebugUtil.debug("currentValue = " + currentValue);
        DebugUtil.debug("newValue  = " + newValue);
         //added for the bug 329326
             if("*".equals(currentValue.trim())) {
            currentValue = "";
        }
      //till here
        if (attrbName.equals(ATTRIBUTE_FIND_NUMBER) || attrbName.equals(ATTRIBUTE_REFERENCE_DESIGNATOR) || attrbName.equals(ATTRIBUTE_QUANTITY))
        {
            try
            {
                //Added for X3 Start
                RelToRelUtil ebomReltoRel = new RelToRelUtil();
                //getting conneted EBOM Substitute connection id
                slEBOMSubstituteRel = ebomReltoRel.getFrommids(context, ebomRelId, RELATIONSHIP_EBOM_SUBSTITUTE);
                Iterator ebomsubsItr        = slEBOMSubstituteRel.iterator();
                String sEBOMSubstituteRelid = "";
//Added for IR-021267
                RelToRelUtil domRel = null;
                while(ebomsubsItr.hasNext())
                {
                  //EBOM Substitute Id
                  sEBOMSubstituteRelid            = (String) ebomsubsItr.next();
//Added for IR-021267
                  domRel = new RelToRelUtil(sEBOMSubstituteRelid);
                  //Synchronizes the changes made on an EBOM relationship with any Substitutes the EBOM has.
                  domRel.setAttributeValue(context, attrbName, newValue);

               }//end While ebomsubsItr
            //Added for X3 End
            // removed commented code
            }
            catch (Exception e)
            {
                DebugUtil.debug("triggerRelationshipEBOMModifyAction-----Exception=", e.toString());
                throw (e);
            }
            finally
            {
            }
        }
    }

    /**
     * Ensure Part Specification or Reference Document Exists for the Part and it has at
     *     least 1 file checked in.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments
     * @return 1 to block, 0 to process event.
     * @throws Exception if the operation fails.
     * @since 10.0.0.0.
     * @trigger PolicyDevelopmentPartStateCreatePromoteCheck.
     * @trigger PolicyECPartStatePreliminaryPromoteCheck.
     */
    public int ensureSpecsConnected(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("in ensureSpecsConnected");
         // Added for bug no. 306588.
        String noCheckRequiredTypes = args[0];

        //  Modified for 364582
        String noFileRequiredTypes = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.PartSpecification.NoFileRequiredTypes");
        if(!noCheckRequiredTypes.equals(noFileRequiredTypes)){
            if("".equals(noCheckRequiredTypes) || (noCheckRequiredTypes == null)){
                noCheckRequiredTypes = noFileRequiredTypes;
            }
            else if(!"".equals(noFileRequiredTypes) || (noFileRequiredTypes != null)){
                StringTokenizer noFileTokenizer = new StringTokenizer(noFileRequiredTypes, ",");
                String noFileToken = "";
                while(noFileTokenizer.hasMoreTokens()){
                    noFileToken = noFileTokenizer.nextToken();
                    if(noCheckRequiredTypes.indexOf(noFileToken) == -1){
                        noCheckRequiredTypes = noCheckRequiredTypes + "," + noFileToken;
                    }
                }
            }
        }
        //  Fix for 364582 ends

        // Ensure Part Specification or Reference Document Exists and each has at least 1 file check in
        StringList relTypes = new StringList(2);
        relTypes.addElement(RELATIONSHIP_PART_SPECIFICATION);
        relTypes.addElement(RELATIONSHIP_REFERENCE_DOCUMENT);
        // Modified for bug no. 306588.
        if (!relsExistWithFileCheckedIn(context, relTypes, noCheckRequiredTypes))
        {
          String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckIfSpecificationConnectedToPart.Message",context.getSession().getLanguage());
          emxContextUtil_mxJPO.mqlNotice(context,strMessage);
            return 1;
        }
        return 0;
    }

    /**
     * Ensure Design Responsibility relationship exists for the Part.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return 1 to block, 0 to process event.
     * @throws Exception if the operation fails.
     * @since 10.0.0.0.
     * @trigger PolicyDevelopmentPartStateCreatePromoteCheck.
     * @trigger PolicyECPartStatePreliminaryPromoteCheck.
     */
    public int ensureDesignResponsibilityExists(Context context, String[] args)
      throws Exception
    {
       DebugUtil.debug("in ensureDesignResponsibilityExists()");

        //Ensure Design Responsibility Exists
        if (!hasRelatedObjects(context,RELATIONSHIP_DESIGN_RESPONSIBILITY,false))
        {
          String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckDesignResponsibilitySetForObject.Message",context.getSession().getLanguage());
          emxContextUtil_mxJPO.mqlNotice(context,strMessage);
          return 1;
        }
        return 0;
    }

    /**
     * Checks the specified relationships to see if the connected object has
     *    at least one file checked in.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param relTypes StringList of relationships to check.
     * @return true if connected object has file checked in else returns false.
     * @throws Exception if the operation fails.
     * @since 10.0.0.0.
     */
     protected boolean relsExistWithFileCheckedIn(Context context, StringList relTypes, String noCheckRequiredTypes)
                    throws Exception
    {
        Pattern relPattern = new Pattern(null);
        Enumeration e = relTypes.elements();
        while (e.hasMoreElements())
            relPattern.addPattern((String)e.nextElement());
        StringList selectRelStmts = new StringList();
        StringList selectStmts  = new StringList(5);
        selectStmts.addElement(SELECT_ID);
        selectStmts.addElement(SELECT_FORMAT_HASFILE);
        selectStmts.addElement(SELECT_TYPE);
        selectStmts.add("vcfile");
        selectStmts.add("vcfolder");
        selectStmts.addElement(CommonDocument.SELECT_VCFILE_EXISTS);
        selectStmts.addElement(CommonDocument.SELECT_VCFOLDER_EXISTS);
        selectStmts.addElement("from[" + CommonDocument.RELATIONSHIP_ACTIVE_VERSION + "]");

        MapList mapList = getRelatedObjects(context,
                                         relPattern.getPattern(),     // relationship pattern
                                         "*",                         // object pattern
                                         selectStmts,                 // object selects
                                         selectRelStmts,              // relationship selects
                                         false,                       // to direction
                                         true,                        // from direction
                                         (short) 1,                   // recursion level
                                         null,                        // object where clause
                                         null);                       // relationship where clause

        // If no specs, return false
        if (mapList.size() == 0){
            return false;
        }
        else{
            return isFileCheckedIntoSpec(context, mapList, noCheckRequiredTypes);
        }

    }

     /**
      * Checks the specifications has at least one file checked in.
      * @param context the eMatrix <code>Context</code> object.
      * @return true if connected object has file checked in else returns false.
      * @throws Exception if the operation fails.
      * @since R207
      * @author ZGQ
      */
      protected boolean isFileCheckedIntoSpec(Context context, MapList mapList, String noCheckRequiredTypes)
         throws Exception{

         StringList typesList = FrameworkUtil.split(noCheckRequiredTypes, ",");
         StringList noCheckList = new StringList();
         StringList subTypesList = new StringList();
         int typeListSize = typesList.size();
         String type = "";
         String sResult = "";
         for (int j=0; j < typeListSize; j++)
         {
             type = PropertyUtil.getSchemaProperty(context,(String)typesList.elementAt(j));
             sResult = MqlUtil.mqlCommand(context,"print type $1 select $2 dump $3",type,"derivative","|");
             subTypesList = FrameworkUtil.split(sResult, "|");
             if (subTypesList.size() > 0)
             {
                 noCheckList.addAll(subTypesList);
             }
             noCheckList.addElement(type);
         }

         String objectType = "";
         String vcFileExists = "";
         String vcFolderExists = "";
         String vcfile = "";
         String vcfolder = "";
         Map map = null;
         int mapListSize = mapList.size();

         for (int i = 0; i < mapListSize; i++)
         {
             map = (Map)mapList.get(i);
             objectType = (String)map.get(SELECT_TYPE);
             vcfile = (String)map.get("vcfile");
             vcfolder = (String)map.get("vcfolder");

             if (noCheckList.contains(objectType))
             {
                 return true;
             }

             //added for case with move files to version setting
             String activeVersionRel = (String)map.get("from[" + CommonDocument.RELATIONSHIP_ACTIVE_VERSION + "]");
             if (activeVersionRel != null && "true".equalsIgnoreCase(activeVersionRel))
             {
        	 return true;
             }

             String hasFile ="";
             if(vcfile !=null && "true".equalsIgnoreCase(vcfile))
             {
               vcFileExists=(String)map.get(CommonDocument.SELECT_VCFILE_EXISTS);
               if(vcFileExists!=null && vcFileExists.equalsIgnoreCase("TRUE")){
                   return true;
               }
             }
             else if(vcfolder!=null && "true".equalsIgnoreCase(vcfolder))
             {
               vcFolderExists = (String)map.get(CommonDocument.SELECT_VCFOLDER_EXISTS);
               if(vcFolderExists!=null && vcFolderExists.equalsIgnoreCase("TRUE")){
                   return true;
               }
             }
             else {
                 Object hasFileObject = map.get(SELECT_FORMAT_HASFILE);
                 if (hasFileObject instanceof String)
                 {
                     hasFile = (String)map.get(SELECT_FORMAT_HASFILE);
                     if (hasFile.equalsIgnoreCase("TRUE")) {
                             return true;
                     }
                 }

                 else if (hasFileObject instanceof StringList)
                 {
                     StringList hasFiles = (StringList)map.get(SELECT_FORMAT_HASFILE);
                     for (int j=0;j<hasFiles.size();j++) {
                         hasFile = (String)hasFiles.elementAt(j);
                         if (hasFile.equalsIgnoreCase("TRUE")) {
                             return true;
                         }
                     }
                 }
             }
         }
         return false;
      }

    /**
     * Floats all unfulfilled ECRs to the Released Part.
     * The following steps are performed:
     *   - Checks the previous rev for connected ECRs that are not
     *     fulfilled or rejected.
     *   - Floats them to this revision if they do not have an ECO attached.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since 10.0.0.0.
     * @trigger PolicyECPartStateReviewPromoteAction.
     */
    public void floatUnfulfilledECRs(Context context, String[] args)
        throws Exception
    {    	
    	//BGP: In case the Release Process is Developement, Do not execute the trigger program functionality
    	String sPartId = getId(context);
    	if(ReleasePhase.isECPartWithDevMode(context, sPartId))
    		return;
    	//BGP: In case the Release Process is Developement, Do not execute the trigger program functionality

        DebugUtil.debug("PolicyECPartStateReviewPromoteAction:floatUnfulfilledECRs");

        // Float Unfulfilled ECR to Released Part
        StringList relTypes = new StringList();
        relTypes.addElement(RELATIONSHIP_AFFECTED_ITEM);
        String triggerPolicyStates;
	    triggerPolicyStates = POLICY_ECR;
	    triggerPolicyStates += "|";
	    triggerPolicyStates += STATE_ECR_COMPLETE;
	    triggerPolicyStates += "|";
        floatUnfulfilledECR(context,relTypes,triggerPolicyStates);
    }

    /**
     * Floats all unfulfilled ECRs to the Released Part.
     * The following steps are performed:
     *   - Checks the previous rev for connected ECRs that are not
     *     fulfilled or rejected.
     *   - Floats them to this revision if they do not have an ECO attached.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param relTypes StringList of relationship types.
     * @param policyStates String of policies and states delimited by "|".
     * @throws Exception If the operation fails.
     * @since 10.0.0.0.
     */
     protected void floatUnfulfilledECR(Context context, StringList relTypes, String policyStates)
                    throws Exception
    {
        // Need shadow agent to modify attributes
        //
        contextUtil.pushContext(context, null);
        Pattern relPattern = new Pattern(null);
        Enumeration e = relTypes.elements();
        while (e.hasMoreElements())
            relPattern.addPattern((String)e.nextElement());
        StringList selectRelStmts = new StringList(1);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_TYPE);
        StringList selectStmts  = new StringList(2);
        selectStmts.addElement(SELECT_ID);
        selectStmts.addElement(SELECT_CURRENT);
        selectStmts.addElement(SELECT_POLICY);
        try
        {
            String strPreviousId = "";
            String checkPartVersion= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Check.PartVersion");
            if (checkPartVersion != null && "TRUE".equalsIgnoreCase(checkPartVersion))
            {
                String sRelPartVersion = PropertyUtil.getSchemaProperty(context,"relationship_PartVersion");
                String objectSelect="relationship["+sRelPartVersion+"].from.id";
                strPreviousId = getInfo(context,objectSelect);
            }
            else
            {
                strPreviousId = getInfo(context,"previous.id");
            }
            if ( strPreviousId == null || "".equals(strPreviousId)){
                return;
             }
            Part prevRevPart = new Part(strPreviousId);
            MapList mapList = prevRevPart.getRelatedObjects(context,
                                         relPattern.getPattern(),     // relationship pattern
                                          DomainConstants.TYPE_ECR,                         // object pattern //Changed from "*"
                                         selectStmts,                 // object selects
                                         selectRelStmts,              // relationship selects
                                         true,                        // to direction
                                         false,                       // from direction
                                         (short) 1,                   // recursion level
                                         null,                        // object where clause
                                         null);                        // relationship where clause
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
                    MapList ecrMapList = eco.getItems(context
                                                        ,RELATIONSHIP_ECO_CHANGEREQUESTINPUT
                                                        ,selectStmts
                                                        ,selectRelStmts
                                                        ,""
                                                        ,"");
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
                // disconnect from prev rev
                String strRelName = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"name");
                if (strRelName != null && strRelName.length() > 0 && strRelName.equals(RELATIONSHIP_AFFECTED_ITEM)){

				String strAssignedECRelId = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"tomid["+RELATIONSHIP_ASSIGNED_AFFECTED_ITEM+"].fromrel["+DomainConstants.RELATIONSHIP_ASSIGNED_EC+"].id");
                DomainRelationship domRel = new DomainRelationship(relId);
                String strRequestedChangeValue = domRel.getAttributeValue(context,ATTRIBUTE_REQUESTED_CHANGE);
                DomainRelationship.disconnect(context,relId);
                // connect to this rev
                MqlUtil.mqlCommand(context,"trigger off");
                String strNewAffectedRelId = "";
                try{//IR:054393
				DomainRelationship dNewAffectedRel = DomainRelationship.connect(context,new DomainObject(objId),relType,this);
                strNewAffectedRelId = dNewAffectedRel.toString();
                if (strRequestedChangeValue != null && strRequestedChangeValue.length() > 0){
                DomainRelationship domNewAffectedRel = new DomainRelationship(strNewAffectedRelId);
                domNewAffectedRel.setAttributeValue(context,ATTRIBUTE_REQUESTED_CHANGE,strRequestedChangeValue);
                }
                }finally {
                    MqlUtil.mqlCommand(context,"trigger on");
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
        finally
        {
            contextUtil.popContext(context, null);
        }
    }

 
/**
 * Gets the EBOM Summary information.
 * @param context the eMatrix <code>Context</code> object.
 * @param args contains a packed HashMap with the following entries:
 * objectId - a String containing the Part id.
 * reportType - a String either BOM or AVL.
 * location - a String containing the Location id, only needed for AVL report.
 * @return MapList of EBOM object ids and EBOM relationship ids.
 * @throws Exception if the operation fails.
 * @since AEF 10.0.0.0.
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getEBOMs (Context context,
                         String[] args)
    throws Exception
{
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);

    String partId = (String) paramMap.get("objectId");
    MapList ebomList = new MapList();

    // reportType can be either BOM or AVL. Depending on this value Location Name is set.
    String reportType ="";
   // location variable holds the value of Location Name
    String location = "";
    // retrieve the selected reportType from the paramMap
    reportType = (String) paramMap.get("reportType");
    // retrieve the selected location by the user
    location = (String) paramMap.get("location");
    // To display AVL data for the first time with default Host Company of the user.
    try {
        Part partObj = new Part(partId);
        StringList selectStmts = new StringList(3);
        selectStmts.addElement(DomainConstants.SELECT_ID);
        StringList selectRelStmts = new StringList(1);
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

        ebomList = partObj.getRelatedObjects(context,
                DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
                DomainConstants.TYPE_PART,                  // object pattern
                                         selectStmts,                 // object selects
                                         selectRelStmts,              // relationship selects
                                         false,                        // to direction
                                         true,                       // from direction
                                         (short)1,                    // recursion level
                                         null,                        // object where clause
                                         null);                       // relationship where clause

        if (location!=null && ("").equals(location) && reportType!=null && reportType.equals("AVL"))
        {
           // retrieve the Host Company attached to the User.
             location =com.matrixone.apps.common.Person.getPerson(context).getCompany(context).getObjectId(context);
         }
         if (location!=null && reportType!=null && reportType.equals("AVL"))
         {
             MapList tempList = new MapList();
             String locationId = com.matrixone.apps.common.Person.getPerson(context).getCompany(context).getObjectId(context);
             if (locationId.equals(location))
            {
                // In case of Host Company
                tempList = partObj.getCorporateMEPData(context, ebomList, locationId, true, partId);
            }
            else {
                // In case of selected location and All locations
                tempList = partObj.getCorporateMEPData(context, ebomList, location, false, partId);
            }
            ebomList.clear();
            ebomList.addAll(tempList);
         }
    }

    catch (FrameworkException Ex) {
        throw Ex;
    }

    return ebomList;
}

/**
 * Gets the "level" entry for each element in the MapList.
 * @param context the eMatrix <code>Context</code> object.
 * @param args contains a packed HashMap with the following entries:
 * objectList - a MapList of object information.
 * @return Vector of "level" values for each row.
 * @throws Exception if the operation fails.
 * @since AEF 10.0.0.0.
 */
public Vector getLevel (Context context,
                        String[] args)
    throws Exception
{
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)programMap.get("objectList");
    Vector columnVals = new Vector(objList.size());

    Iterator i = objList.iterator();
    while (i.hasNext())
    {
        Map m = (Map) i.next();
        String level = (String)m.get("level");

        columnVals.addElement(level);

    }

    return columnVals;
}

/**
 * Gets the Consolidated EBOM Summary.
 * @param context the eMatrix <code>Context</code> object.
 * @param args contains a packed HashMap with the following entries:
 * languageStr - a String continaing the language
 * objectId - a String containing the Part id.
 * slevels - a String containing the number of levels to expand.
 * @return Maplist of EBOM Objects containing Parts id, type, revision and description.
 * @throws Exception if the operation fails.
 * @since AEF 10.0.0.0.
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getConsolidatedEBOMs (Context context,
                                     String[] args)
    throws Exception
{
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    String langStr = (String) paramMap.get("languageStr");
    String partId = (String) paramMap.get("objectId");
    String sLevels= (String) paramMap.get("slevels");
    Integer lev = new Integer(sLevels);
    int levels = lev.intValue();

    MapList rollupList = new MapList();
    String relPattern =RELATIONSHIP_EBOM;
    Part partObj = new Part(partId);
    StringList selects = new StringList(5);
    selects.addElement(DomainConstants.SELECT_DESCRIPTION);
    selects.addElement(DomainConstants.SELECT_TYPE);
    selects.addElement(DomainConstants.SELECT_REVISION);
    selects.addElement(DomainConstants.SELECT_ID);

    StringList sortList = new StringList(3);
    sortList.addElement("sortKey");
    rollupList = partObj.doRollup(context,selects,relPattern,sortList,levels);
    boolean noPartsAttached = rollupList.isEmpty();
    boolean partialRollup = false;
    if (!noPartsAttached && partObj.isPartialRollup()) {
        partialRollup = true;
    }
    if (partialRollup) {
        String sErrorMsg=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.BOMRollup.ConfigTableSomePartsNotShown",langStr);
        emxContextUtil_mxJPO.mqlNotice(context,sErrorMsg);
    }
    return rollupList;
}

/**
 * Gets the "Quantity" attribute entry for each element in the MapList.
 * @param context the eMatrix <code>Context</code> object.
 * @param args contains packed HashMap with the following entries:
 * objectList - a MapList of object information.
 * @return Vector of "level" values for each row.
 * @throws Exception if the operation fails.
 * @since AEF 10.0.0.0.
 */
public Vector getQuantity(Context context, String[] args)
    throws Exception
{
    HashMap programMap = (HashMap)JPO.unpackArgs(args);

    MapList objList = (MapList)programMap.get("objectList");
    Vector columnVals = new Vector(objList.size());
    Iterator i = objList.iterator();
//Modified for bug # 359417 - starts
        HashMap paramList = (HashMap)programMap.get("paramList");
        String strReportFormat = (String) paramList.get("expandProgram");

        // For IR-091668 .st

        if (strReportFormat == null) {
		      strReportFormat = (String) paramList.get("repFormat");
        }

        if ((strReportFormat == null || "".equals(strReportFormat))&& paramList.get("selectedFilter")!= null){
        	strReportFormat = (String) paramList.get("selectedFilter");
        	strReportFormat = strReportFormat.replaceAll("^.*:", "").trim();
        }

        //  For IR-091668 .en

        if (strReportFormat != null && (strReportFormat.indexOf("getConsolidatedData") != -1 || strReportFormat.indexOf("getExpandedEBOM") != -1 || strReportFormat.indexOf("getDelimitedRollupEBOM") != -1 || strReportFormat.indexOf("getRangeRollupEBOM") != -1))
        {
			while (i.hasNext())
			{
				Map m = (Map) i.next();
			   String refDes = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY);
				columnVals.addElement(refDes);
			}
    }
		else
		{
//Modified for bug # 359417 - ends

    while (i.hasNext())
    {
        Map m = (Map) i.next();
        String strRelId = (String)m.get(SELECT_RELATIONSHIP_ID);
//Modified for Bug # 359414 - starts
        String quantity = (String)m.get(SELECT_ATTRIBUTE_QUANTITY);
//Modified for Bug # 359414 - ends
        if (strRelId != null && strRelId.length() > 0)
        {
            try
            {
                DomainRelationship doRel = new DomainRelationship(strRelId);
                quantity = doRel.getAttributeValue(context, DomainConstants.ATTRIBUTE_QUANTITY);
            }
            catch (Exception excep)
            {
               quantity = "";
            }
        }
//Modified for Bug # 359414 - starts
		else if (quantity == null || quantity.length() <= 0)
		{
			quantity = "";
	        }
//Modified for Bug # 359414 - ends
		        columnVals.addElement(quantity);
	        }
//Modified for bug # 359417 - starts
    }
//Modified for bug # 359417 - ends

    return columnVals;
}

/**
 * Gets the "Quantity" attribute on the EBOM relationship for the substitute.
 * @param context the eMatrix <code>Context</code> object.
 * @param args contains packed HashMap with the following entries:
 * objectList - a MapList of object information.
 * @return Vector of "quantity" values for each row.
 * @throws Exception if the operation fails.
 * @since EC 10.6.
 */
public Vector getSubstituteEBOMQuantity(Context context,
                          String[] args)
    throws Exception
{
    HashMap programMap = (HashMap)JPO.unpackArgs(args);

    MapList objList = (MapList)programMap.get("objectList");
    Vector columnVals = new Vector(objList.size());

    Iterator i = objList.iterator();
    while (i.hasNext())
    {
        Map m = (Map) i.next();
        String quantity = (String)m.get(DomainConstants.SELECT_EBOM_QUANTITY);
        columnVals.addElement(quantity);
    }

    return columnVals;
}

/**
 * Returns a Vector of "History" icons for rows where the relationship type is "EBOM History".
 * @param context the eMatrix <code>Context</code> object.
 * @param args contains a packed HashMap with the following entries:
 * objectList - a MapList of object information.
 * @return Vector of "HistoryImage" for each row.
 * @throws Exception if the operation fails.
 * @since AEF 10.0.0.0.
 */
public Vector getHistoryImage(Context context, String[] args)
    throws Exception
{
    String imgSrc = "<img src='images/iconSmallPartHistory.gif' border='0'>";
    String imgNothing =" ";

    HashMap programMap = (HashMap)JPO.unpackArgs(args);

    MapList objList = (MapList)programMap.get("objectList");
    Vector columnVals = new Vector(objList.size());

    Iterator i = objList.iterator();
    while (i.hasNext())
    {
        Map m = (Map) i.next();
        String relType = (String)m.get("type[connection]");
        if (relType.equalsIgnoreCase(DomainRelationship.RELATIONSHIP_EBOM_HISTORY)) {
            columnVals.addElement(imgSrc);
        }
        else {
            columnVals.addElement(imgNothing);
        }
    }
    return columnVals;
}

/**
 * Returns a Vector of "ECROActive" icons based on
 * whether the part has an active ECR or ECO connected to it.
 * @param context the eMatrix <code>Context</code> object.
 * @param args contains a packed HashMap with the following entries:
 * paramList - a HashMap of parameter information. languageStr needs to be set for Tool Tip display.
 * objectList - a MapList of object information.
 * @return Vector of "ECROActive" image for each row.
 * @throws Exception if the operation fails.
 * @since AEF 10.0.0.0.
 */
public Vector getECRECOImage(Context context,
                             String[] args)
    throws Exception
{
    HashMap programMap = (HashMap)JPO.unpackArgs(args);

    HashMap paramMap= (HashMap)programMap.get("paramList");

    String langStr = (String) paramMap.get("languageStr");

    String sActiveTip=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.ActiveECRorECO",langStr);

    String imgSrc = "<img src=\"images/iconSmallECRO.gif\" alt =\""+sActiveTip+"\" border=\"0\"></img>";

    String imgNothing =" ";

    MapList objList = (MapList)programMap.get("objectList");

    Vector columnVals = new Vector(objList.size());

    boolean hasActiveECRECO=false;
    String activeECRState = "";
    String activeECOState = "";
    String strRelAffectedItem = PropertyUtil.getSchemaProperty(context, "relationship_AffectedItem");

    try {
      // get the active states of ECR from Engineering central properties file
      activeECRState = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ActiveECRStates");
    }catch(Exception e){}

    try {
      // get the active states of ECO from Engineering central properties file
      activeECOState = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ActiveECOStates");
    }catch(Exception f){}

    if(activeECRState==null || "null".equals(activeECRState) || "".equals(activeECRState.trim()))
    {
      activeECRState = "state_Create,state_Review,state_PlanECO";
    }
    if(activeECOState==null || "null".equals(activeECOState) || "".equals(activeECOState.trim()))
    {
      activeECOState = "state_Create,state_DefineComponents,state_DesignWork,state_Review";
    }

    try {
        StringList activeecrStateList = new StringList();
        StringTokenizer stringtokenizer = new StringTokenizer(activeECRState, ",");
        String stateName = null;
        while (stringtokenizer.hasMoreTokens())
        {
            stateName = FrameworkUtil.lookupStateName(context,
                                                  POLICY_ECR,
                                                  stringtokenizer.nextToken().trim());
            activeecrStateList.addElement(stateName);
        }
        stringtokenizer = new StringTokenizer(activeECOState, ",");
        StringList activeecoStateList = new StringList();
        while (stringtokenizer.hasMoreTokens())
        {
            stateName = FrameworkUtil.lookupStateName(context,
                                                  POLICY_ECO,
                                                  stringtokenizer.nextToken().trim());
            activeecoStateList.addElement(stateName);
        }

        if(objList != null && objList.size() > 0)
        {
            //construct array of ids
            int objListSize = objList.size();
            String[] oidList = new String[objListSize];
            Map dataMap = null;
            for(int i = 0; i < objListSize; i++) {
               dataMap = (Map)objList.get(i);
               oidList[i] = (String)dataMap.get("id");
            }

            //construct selects
            SelectList objSelects = new SelectList(2);
            objSelects.add("relationship["+strRelAffectedItem + "].from.current");


           //modified for bug 318452
            objSelects.add(SELECT_ID);
            objSelects.add("relationship["+strRelAffectedItem+"].from.type");  //069732
            //end of modification

            DomainObject.MULTI_VALUE_LIST.add("relationship[" + strRelAffectedItem + "].from.current");
           //069732

            DomainObject.MULTI_VALUE_LIST.add("relationship["+strRelAffectedItem+"].from.type"); //069732
            //get ECR/ECO states
            MapList totalresult = DomainObject.getInfo(context, oidList, objSelects);

            if(totalresult != null && totalresult.size() > 0)
            {
                int objectCounts = totalresult.size();
                Map m = null;
                StringList currentECRStateList = null;
                StringList typeList = null;
				//069732- starts
				String typeECO=PropertyUtil.getSchemaProperty(context,"type_ECO");
				String typeECR=PropertyUtil.getSchemaProperty(context,"type_ECR");
				//069732- Ends

                for(int n=0; n < objectCounts; n++)
                {
                    m = (Map)totalresult.get(n);
                    //Get ActiveECRECOs only if the object is of type part or spec
                    DomainObject dObj = new DomainObject((String)m.get(DomainObject.SELECT_ID));
                    //skip the Engineering Change Objects(ECRs,ECOs...)
                    if(!dObj.isKindOf(context,PropertyUtil.getSchemaProperty(context, "type_Change")))
                    {

                     if (!hasActiveECRECO)
                    {
                        currentECRStateList = (StringList)m.get("relationship[" + strRelAffectedItem + "].from.current");
                        // 069732: affected item rel can have ECR or ECO. check type.
                        typeList = (StringList)m.get("relationship[" + strRelAffectedItem + "].from.type");
                        if (currentECRStateList != null && currentECRStateList.size() > 0)
                        {
                             for (int i=0;i< currentECRStateList.size() ;i++)
                             {
                                 // 069732: check ECR type
                                 if ((activeecrStateList.contains((String)currentECRStateList.get(i))) && typeECR.equals((String)typeList.get(i)))
                                 {
                                     hasActiveECRECO=true;
                                 }
                                 // 069732: check ECO type
                                 else if ((activeecoStateList.contains((String)currentECRStateList.get(i))) && typeECO.equals((String)typeList.get(i)))
                                 {
                                     hasActiveECRECO=true;
                                 }
                                 // If even one of the ECR's is active, then do not have to go through the rest of the ECR's.
                                 if (hasActiveECRECO)
                                     break;
                             }
                        }
                    }

                    }
                    if (!hasActiveECRECO) {
                        // this object doesn't have active ECR or ECO
                        columnVals.addElement(imgNothing);
                    }
                    else
                    {
                        columnVals.addElement(imgSrc);
                    }
                    hasActiveECRECO = false;
                } //end for
            }  //end if
        }
    }
    catch (FrameworkException Ex) {
        throw Ex;
    }

    DomainObject.MULTI_VALUE_LIST.remove("relationship[" + strRelAffectedItem + "].from.type");
    DomainObject.MULTI_VALUE_LIST.remove("relationship[" + strRelAffectedItem + "].from.current");
    return columnVals;
}

 
/**
 * Gets all Manufacturer Equivalent Parts in the vault specified that are in a state prior to Release.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds a packed HashMap with the following entries:
 *   objectId - a String containing the Part id.
 *   vault - a String containing the Vault name.
 * @return MapList of Manufacturer Equivalent Part object ids and relationship ids.
 * @throws Exception if the operation fails.
 * @since AEF 10.0.0.0.
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getInProcessMEPs (Context context,String[] args) throws Exception
   {
        MapList mepList = new MapList();
        try
        {
              Part partObj = new Part();
              StringList selectStmts = partObj.getMEPSelectList(context);

              //create where clause to filter out only MEP's in release state
              //Added for Bug: 308765
              String strMEPRelease = com.matrixone.apps.engineering.EngineeringUtil.getReleaseState(context,POLICY_MANUFACTURER_EQUIVALENT);
              String whereCls = " (current == " + strMEPRelease + ") ";
              String vault=QUERY_WILDCARD;
              mepList = partObj.getManufacturerEquivalents(context,selectStmts,vault,whereCls);
        }
        catch (FrameworkException Ex) {
            throw Ex;
        }
        return mepList;
   }

    /**
     * Gets the Manufacturer Equivalent Parts for the given Enterprise Part.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap with the following entries:
     *    objectId - the Part id.
     * @return a MapList of Manufacturer Equivalent Part object ids and relationship ids.
     * @throws Exception if the operation fails.
     * @since AEF 10.0.0.0.
     */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getManufacturerEquivalents (Context context,String[] args)
         throws Exception
    {
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);

       String objectId = (String) paramMap.get("objectId");
       MapList mepList = new MapList();
       try
       {
           Part partObj = new Part(objectId);
           StringList selectStmts = new StringList(1);
           selectStmts.addElement(DomainConstants.SELECT_ID);

           StringList selectRelStmts = new StringList(1);
           selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
           mepList =  partObj.getManufacturerEquivalents(context,  selectStmts, selectRelStmts);
       }

       catch (FrameworkException Ex)
       {
           throw Ex;
       }
       return mepList;
   }

   /**
    * Gets all the Manufacturer Equivalent Parts with the specified criteria.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectId - String of the context Part object id.
    * selType - String of the Type to search.
    * txtName - Strng of the Name to search.
    * txtRev - String of the Revision to search.
    * txtDesc - String of the Description to search.
    * txtOwner - String of the Owner to search.
    * txtOriginator - String of the Originator to search.
    * revPattern - String of either ALL_REVISIONS, HIGHEST_REVISION or HIGHEST_AND_PRESTATE_REVS.
    * queryLimit - String of the Query Limit.
    * vaultOption - String of the Vault option to use.
    * @return a MapList of all Manufacturer Equivalent Part object ids and relationship ids.
    * @throws Exception if the operation fails.
    * @since AEF 10.0.0.0.
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getAllManufacturerEquivalents (Context context,String[] args)
         throws Exception
   {
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);

       String txtType = (String) paramMap.get("selType");
       String txtName = (String) paramMap.get("txtName");
       String txtRev = (String) paramMap.get("txtRev");
       String txtDesc = (String) paramMap.get("txtDesc");
       String txtOwner = (String) paramMap.get("txtOwner");
       String txtOriginator = (String) paramMap.get("txtOriginator");
       String txtrevPattern = (String) paramMap.get("revPattern");
       String queryLimit = (String) paramMap.get("queryLimit");

       // Get the user's vault option & call corresponding methods to get the vault's.

       String txtVault  = "";
       String strVaults = "";
       StringList strListVaults = new StringList();

       String txtVaultOption = (String)paramMap.get("vaultOption");
       if(txtVaultOption==null) {
        txtVaultOption="";
       }

    if("ALL_VAULTS".equals(txtVaultOption) || "".equals(txtVaultOption))
    {
          // get ALL vaults
          Iterator mapItr = VaultUtil.getVaults(context).iterator();
          if(mapItr.hasNext())
          {
            txtVault =(String)((Map)mapItr.next()).get("name");

            while (mapItr.hasNext())
            {
              Map map = (Map)mapItr.next();
              txtVault += "," + (String)map.get("name");
            }
          }

     }
     else if("LOCAL_VAULTS".equals(txtVaultOption)) {
          // get All Local vaults
            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
            Company company = person.getCompany(context);
            strListVaults = OrganizationUtil.getLocalVaultsList(context, company.getObjectId());

          StringItr strItr = new StringItr(strListVaults);
          if(strItr.next()){
            strVaults =strItr.obj().trim();
          }
          while(strItr.next())
          {
            strVaults += "," + strItr.obj().trim();
          }
          txtVault = strVaults;
     }
     else if ("DEFAULT_VAULT".equals(txtVaultOption)) {
          txtVault = context.getVault().getName();
     }
     else {
          txtVault = txtVaultOption;
     }
        //trimming
     txtVault = txtVault.trim();

       MapList mepList = new MapList();
       try
       {
           StringList selectStmts = new StringList(1);
           selectStmts.addElement(SELECT_ID);

           StringList selectRelStmts = new StringList(1);
           selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

           //use matchlist 'string' 'delim' syntax to improve performance
           //this expression is indexed and only business objects that use said policies will be loaded.
           //fix for bug 308574
           StringList equivalentPolicyList = EngineeringUtil.getEquivalentPolicies(context, txtType);
           //end
           String whereExpression = "policy matchlist \'" ;
           for (int i=0; i < equivalentPolicyList.size(); i++)
           {
              if (i != 0)
              {
                  whereExpression += ",";
              }
              whereExpression += equivalentPolicyList.get(i);
           }
           whereExpression += "\' \',\' ";

           if (txtDesc != null && !txtDesc.equalsIgnoreCase("null") && txtDesc.equals("*"))
           {
               txtDesc = "";
           }
           if (txtOriginator != null && !txtOriginator.equalsIgnoreCase("null") && txtOriginator.equals("*"))
           {
               txtOriginator = "";
           }

           if (!(txtOriginator == null || txtOriginator.equalsIgnoreCase("null") || txtOriginator.length() <= 0 ))
           {
               String sOriginatorQuery = "attribute[" + DomainConstants.ATTRIBUTE_ORIGINATOR + "] ~~ " + "\'" + txtOriginator + "\'";

               whereExpression += " && " + sOriginatorQuery;
           }
           if (!(txtDesc == null || txtDesc.equalsIgnoreCase("null") || txtDesc.length() <= 0 ))
           {
               String sDescQuery = "description ~~ " + "\'"  + txtDesc + "\'" ;
               whereExpression += " && " + sDescQuery;
           }

           if (!txtrevPattern.equalsIgnoreCase("ALL_REVISIONS"))
           {
               String sRevQuery = "program[emxServiceUtils -method checkRevisions ${OBJECTID} "+DomainObject.STATE_PART_RELEASE+" " + txtrevPattern + "] == true";
               whereExpression += " && " + sRevQuery;
           }

           if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals(""))
           {
               queryLimit = "0";
           }

           mepList =  findObjects(context, txtType, txtName, txtRev, txtOwner, txtVault,
                whereExpression,  null, true, selectStmts, Short.parseShort(queryLimit));
       }
       catch (FrameworkException Ex)
       {
           throw Ex;
       }
       return mepList;
   }

   /**
    * Returns whether the objects are Enterprise Parts or Equivalent Parts.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectList - a MapList of object information.
    * paramList - a HashMap of parameters.
    * @return a Vector of part origin values.
    * @throws Exception if the operation fails.
    * @since AEF 10.0.0.0.
    */
   public Vector getPartOrigin(Context context, String[] args)
   throws Exception
   {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       MapList relBusObjPageList = (MapList)programMap.get("objectList");

       Vector columnValues = new Vector(relBusObjPageList.size());

       String bArr [] = new String[relBusObjPageList.size()];
       StringList bSel = new StringList();

       // Get the required parameter values from  "paramMap"
       String languageStr = context.getSession().getLanguage();

       // Get the object elements - OIDs and RELIDs
       for (int i = 0; i < relBusObjPageList.size(); i++)
       {
            // Get Business object Id
            bArr [i] = (String)((HashMap)relBusObjPageList.get(i)).get("id");
       }

       // Add  Business object selects
       bSel.add("policy.property[PolicyClassification].value");
       try
       {
           // Process the OIDs to get the results
           BusinessObjectWithSelectList bwsl = BusinessObject.getSelectBusinessObjectData(context, bArr, bSel);

           // Code for processing the result data obtained
           String strManufacturerEquivalent=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.MfgEquivalent",languageStr);
           String strEnterprise=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Enterprise",languageStr);

           for ( int i = 0; i < bwsl.size(); i++)
           {
               String propertyValue = bwsl.getElement(i).getSelectData("policy.property[PolicyClassification].value");

               //Build the Vector "columnValues" with the list of values to be displayed in the column
               if ("Equivalent".equals(propertyValue))
               {
                   columnValues.add(strManufacturerEquivalent);
               }
               else
               {
                   columnValues.add(strEnterprise);
               }
           }
       }
       catch (FrameworkException Ex)
       {
            throw Ex;
       }

       return columnValues;
   }

   /**
    * Gets the Enterprise Parts attached to a Manufacturer Equivalent Part.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a packed HashMap with the following entries:
    * objectId - the object id of the Manufacturer Equivalent Part.
    * @return a MapList of Enterprise Part object ids and relationship ids.
    * @throws Exception if the operation fails.
    * @since 10-5.
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getEnterpriseEquivalents (Context context,String[] args)
         throws Exception
   {
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);

       String objectId = (String) paramMap.get("objectId");
       MapList equivList = new MapList();

       MapList listMEPs = null;
       MapList corpMEPs = null;

       StringBuffer sbRelPattern = new StringBuffer(RELATIONSHIP_MANUFACTURER_EQUIVALENT);
              sbRelPattern.append(',');
              sbRelPattern.append(RELATIONSHIP_LOCATION_EQUIVALENT);

       // MCC Bug 330835 Fix to include MCC EP in the EP type pattern
       String sTypeCompliancePart = PropertyUtil.getSchemaProperty(context,"type_ComplianceEnterprisePart");

       StringBuffer sbTypePattern = new StringBuffer(TYPE_LOCATION_EQUIVALENT_OBJECT);
              sbTypePattern.append(',');
              sbTypePattern.append(TYPE_PART);
              sbTypePattern.append(',');
              sbTypePattern.append(sTypeCompliancePart);

       try
       {
           Part partObj = new Part(objectId);
           StringList selectStmts = new StringList(1);
           selectStmts.addElement(SELECT_ID);

           StringList selectRelStmts = new StringList(1);
           selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

           //fetching any connected EPs via Location Equivalent object (Location Context MEP)
           listMEPs = partObj.getRelatedObjects(context,
                                        sbRelPattern.toString(),              // relationship pattern
                                        sbTypePattern.toString(),              // object pattern
                                        selectStmts,                 // object selects
                                        selectRelStmts,              // relationship selects
                                        true,                        // to direction
                                        false,                       // from direction
                                        (short) 2,                   // recursion level
                                        null,                        // object where clause
                                        null);                        // relationship where clause

           //fetching EPs connected directly using Manf Equiv Relation (Corporate Context MEP)
           // MCC Bug 330835 Fix to include MCC EP in the EP type pattern
           StringBuffer scorpTypePattern = new StringBuffer(TYPE_PART);
           scorpTypePattern.append(',');
           scorpTypePattern.append(sTypeCompliancePart);

           corpMEPs = partObj.getRelatedObjects(context,
                                        RELATIONSHIP_MANUFACTURER_EQUIVALENT,              // relationship pattern
                                        scorpTypePattern.toString(),                   // object pattern
                                        selectStmts,                 // object selects
                                        selectRelStmts,              // relationship selects
                                        true,                        // to direction
                                        false,                       // from direction
                                        (short) 1,                   // recursion level
                                        null,                        // object where clause
                                        null);                        // relationship where clause


           for(int i=0;i<listMEPs.size();i++) {
               Map tempMap2 = (Map)listMEPs.get(i);
                 //EP id exists at level 2 if Location Context MEP
                 if("2".equals((String) tempMap2.get("level")) ) {
                    equivList.add(tempMap2);
                 }
           }//end of for listMEPs

           for(int j=0;j<corpMEPs.size();j++) {
               Map tempMap2 = (Map)corpMEPs.get(j);
               equivList.add(tempMap2);
           }//end of For corpMEPs
       }
       catch (FrameworkException Ex)
       {
            throw Ex;
       }
       return equivList;
   }

  /**
  * Returns a MapList of Reference Documents attached to an Object.
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds a packed HashMap with the following entries:
  * objectId - the object id of the context Part.
  * @return MapList of object ids and relationship ids for all objects connected with Reference Document relationship.
  * @throws Exception if the operation fails.
  * @since EC 10.0.0.0.
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getReferenceDocuments (Context context, String[] args)
    throws Exception
  {
	  ContextUtil.startTransaction(context, true);
    try
    {
      HashMap paramMap = (HashMap)JPO.unpackArgs(args);

      String partId = (String) paramMap.get("objectId");
      MapList refDocList = new MapList();


      Part partObj = new Part(partId);
      StringList selectStmts = new StringList(1);
      selectStmts.addElement(SELECT_ID);

      StringList selectRelStmts = new StringList(1);
      selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

       refDocList = FrameworkUtil.toMapList(partObj.getExpansionIterator(context, RELATIONSHIP_REFERENCE_DOCUMENT, "*",
               selectStmts, selectRelStmts, true, true, (short)1,
              null, null, (short)0,
              false, false, (short)0, false),
              (short)0, null, null, null, null);

       ContextUtil.commitTransaction(context);
      return refDocList;
    }
     catch (Exception e) {
    	 ContextUtil.abortTransaction(context);
      throw e;
     }
  }

  /**
  * Get Parts for the specified criteria.
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds a packed HashMap of the following entries:
  * selType - a String containing the Type to search for.
  * txtName - a String containing the Name to search for.
  * txtRev - a String containing the Revision to search for.
  * txtOwner - a String containing the Owner to search for.
  * txtWhere - a String containing the where clause to use in the search.
  * txtOriginator - a String containing the Originator to search for.
  * txtDesc - a String containing the Description to search for.
  * txtSearch - a String containing the Search text for the query.
  * txtFormat - a String containing the Format text for the query.
  * languageStr - a String containing the language setting.
  * setRadio - a String containing the Set name.
  * revPattern - a String of either ALL_REVISIONS, HIGHEST_REVISION or HIGHEST_AND_PRESTATE_REVS.
  * Vault - a String containing the Vault name to search in.
  * queryLimit - a String containing the Query Limit value.
  * @return Object a MapList containing search result.
  * @throws Exception if the operation fails.
  * @since 10.0.0.0
  */
   @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getPartSearchResult(Context context , String[] args)
                    throws Exception
  {
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    //  Fix for bug --022268
     int limit = UISearchUtil.getQueryLimit(context, paramMap);
    StringBuffer sbObjLimitWarning = new StringBuffer();
  //Multitenant
    sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(),"emxComponents.Warning.ObjectFindLimit"));
    sbObjLimitWarning.append(limit);
    //Multitenant
    sbObjLimitWarning.append(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(),"emxComponents.Warning.Reached"));
    //
    String slkupOriginator    = PropertyUtil.getSchemaProperty(context,"attribute_Originator");

    //Retrieve Search criteria
    String selType          = (String)paramMap.get("selType");
    String txtName          = (String)paramMap.get("txtName");
    String txtRev           = (String)paramMap.get("txtRev");
    String txtOwner         = (String)paramMap.get("txtOwner");
    String txtWhere         = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL((String)paramMap.get("txtWhere"));
    String txtOriginator    = (String)paramMap.get("txtOriginator");
    String txtDescription   = (String)paramMap.get("txtDesc");
    String txtSearch        = (String)paramMap.get("txtSearch");
    String txtFormat        = (String)paramMap.get("txtFormat");
    String sSetName         = (String)paramMap.get("setRadio");
    String sWhereExp = txtWhere;

    String sAnd         = "&&";
    char chDblQuotes    = '\"';
    String sQuote       = "\'";//Added for IR-040943V6R2011

/**************************Vault Code Start*****************************/
// Get the user's vault option & call corresponding methods to get the vault's.

      String txtVault   ="";
      String strVaults="";
      StringList strListVaults=new StringList();

      String txtVaultOption = (String)paramMap.get("vaultOption");
      if(txtVaultOption==null) {
        txtVaultOption="";
      }
      String vaultAwarenessString = (String)paramMap.get("vaultAwarenessString");

      if(vaultAwarenessString.equalsIgnoreCase("true")){

        if("ALL_VAULTS".equals(txtVaultOption) || "".equals(txtVaultOption))
        {
          strListVaults = com.matrixone.apps.common.Person.getCollaborationPartnerVaults(context,null);
          StringItr strItr = new StringItr(strListVaults);
          if(strItr.next()){
            strVaults =strItr.obj().trim();
          }
          while(strItr.next())
          {
            strVaults += "," + strItr.obj().trim();
          }
          txtVault = strVaults;
        }
        else if("LOCAL_VAULTS".equals(txtVaultOption))
        {
          com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
          Company company = person.getCompany(context);
          txtVault = company.getLocalVaults(context);
        }
        else if ("DEFAULT_VAULT".equals(txtVaultOption))
        {
          txtVault = context.getVault().getName();
        }
        else
        {
          txtVault = txtVaultOption;

        }
      }
      else
      {
        if("ALL_VAULTS".equals(txtVaultOption) || "".equals(txtVaultOption))
        {
          // get ALL vaults
          Iterator mapItr = VaultUtil.getVaults(context).iterator();
          if(mapItr.hasNext())
          {
            txtVault =(String)((Map)mapItr.next()).get("name");

            while (mapItr.hasNext())
            {
              Map map = (Map)mapItr.next();
              txtVault += "," + (String)map.get("name");
            }
          }
        }
        else if("LOCAL_VAULTS".equals(txtVaultOption))
        {
          // get All Local vaults
          com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
          Company company = person.getCompany(context);
          strListVaults = OrganizationUtil.getLocalVaultsList(context, company.getObjectId());

          StringItr strItr = new StringItr(strListVaults);
          if(strItr.next()){
            strVaults =strItr.obj().trim();
          }
          while(strItr.next())
          {
            strVaults += "," + strItr.obj().trim();
          }
          txtVault = strVaults;
        }
        else if ("DEFAULT_VAULT".equals(txtVaultOption))
        {
          txtVault = context.getVault().getName();
        }
        else
        {
          txtVault = txtVaultOption;
        }
      }
      //trimming
      txtVault = txtVault.trim();

  /*******************************Vault Code End***************************************/

    if (sSetName == null || sSetName.equals("null") || sSetName.equals("")){
      sSetName = "";
    }

    String savedQueryName   = (String)paramMap.get("savedQueryName");
    if (savedQueryName == null || savedQueryName.equals("null") || savedQueryName.equals("")){
      savedQueryName="";
    }

    String queryLimit = (String)paramMap.get("queryLimit");
    if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
      queryLimit = "0";
    }

    if (txtName == null || txtName.equalsIgnoreCase("null") || txtName.length() <= 0){
      txtName = "*";
    }
    if (txtRev == null || txtRev.equalsIgnoreCase("null") || txtRev.length() <= 0){
      txtRev = "*";
    }
    if (txtOwner == null || txtOwner.equalsIgnoreCase("null") || txtOwner.length() <= 0){
      txtOwner = "*";
    }
    if (txtDescription != null && !txtDescription.equalsIgnoreCase("null") && txtDescription.equals("*")){
      txtDescription = "";
    }
    if (txtOriginator != null && !txtOriginator.equalsIgnoreCase("null") && txtOriginator.equals("*")){
      txtOriginator = "";
    }
    if (txtWhere == null || txtWhere.equalsIgnoreCase("null")){
      txtWhere = "";
    }

    if (!(txtOriginator == null || txtOriginator.equalsIgnoreCase("null") || txtOriginator.length() <= 0 )) {
      String sOriginatorQuery = "attribute[" + slkupOriginator + "] ~~ " + chDblQuotes + txtOriginator + chDblQuotes;
      if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
        sWhereExp = sOriginatorQuery;
      } else {
        sWhereExp += sAnd + " " + sOriginatorQuery;
      }
    }

    if (!(txtDescription == null || txtDescription.equalsIgnoreCase("null") || txtDescription.length() <= 0 )) {
      String sDescQuery = "description ~~ " + chDblQuotes + txtDescription + chDblQuotes;
      if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
        sWhereExp = sDescQuery;
      } else {
        sWhereExp += sAnd + " " + sDescQuery;
      }
    }

    // Revised : Added logic to query against policy and state information
        HashMap mRequestValues = (HashMap) paramMap.get( "RequestValuesMap" );
        String[] txtPolicy = (String[])mRequestValues.get("txtPolicy");
        String sLimitState = "";
        StringBuffer sbPolicyClause = new StringBuffer(32);
        if ( txtPolicy != null && txtPolicy.length > 0 ) {
            for ( int i=0; i<txtPolicy.length; i++ ) {
                if ( i > 0 ) sbPolicyClause.append ( " || " );
                sLimitState = (String) paramMap.get( txtPolicy[i] );
                if ( sLimitState != null && !sLimitState.equals( "" ) ) {
                    sbPolicyClause.append( "( policy == \"" + txtPolicy[i] + "\" && current == \"" + sLimitState + "\" )" );
                } else {
                    sbPolicyClause.append( "( policy == \"" + txtPolicy[i] + "\" ) " );
                }
            }
            if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
                sWhereExp = "( " + sbPolicyClause.toString() + ")";
            } else {
                sWhereExp += sAnd + " (" + sbPolicyClause.toString() + ")";
            }
        }
        // End of Revisions

    //Common Document changes - filter out Version objects
    //first see if any document types are being searched
    StringTokenizer st = new StringTokenizer(selType, ",");
    String stypeDOCUMENTS = PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS");
    boolean isDocumentsType = false;

    // Added for Find Like Serach to prevent the display of Versionnable Part Starts
        boolean isPartType = false;
        String stypePART = PropertyUtil.getSchemaProperty(context,"type_Part");
        while(st.hasMoreTokens() && !isPartType)
            {
               isPartType = EngineeringUtil.isTypeOf(context, st.nextToken(), stypePART);
        }
        if (isPartType)
           {
              String sattrIsVersion  = PropertyUtil.getSchemaProperty(context,"attribute_IsVersion");
              String sIsVersionQuery = "!attribute[" + sattrIsVersion + "] == " + chDblQuotes + "True" + chDblQuotes;
              if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
                sWhereExp = sIsVersionQuery;
              }
        }
    // Added for Find Like Serach to prevent the display of Versionnable Part Ends

    while(st.hasMoreTokens() && !isDocumentsType)
    {
       isDocumentsType = EngineeringUtil.isTypeOf(context, st.nextToken(), stypeDOCUMENTS);
    }
    if (isDocumentsType)
    {
       String sattrIsVersionObject  = PropertyUtil.getSchemaProperty(context,"attribute_IsVersionObject");
       String sIsVersionObjectQuery = "!attribute[" + sattrIsVersionObject + "] == " + chDblQuotes + "True" + chDblQuotes;
       if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
         sWhereExp = sIsVersionObjectQuery;
       } else {
         sWhereExp += sAnd + " " + sIsVersionObjectQuery;
       }
    }

    SelectList resultSelects = new SelectList(7);
    resultSelects.add(DomainObject.SELECT_ID);
    // Added for MCC EC Interoperability Feature
    String strAttrEnableCompliance  =PropertyUtil.getSchemaProperty(context,"attribute_EnableCompliance");
    resultSelects.addElement("attribute["+strAttrEnableCompliance+"]");
    //end

    MapList totalresultList = null;

    // Check for a set name and use that set for results if present
    if ("".equals(sSetName))
    {
      if ("".equals(savedQueryName))
      {
          try
          {
              //Added for IR-040943V6R2011
              int index = sWhereExp.indexOf('"');
              int lastIndex = sWhereExp.lastIndexOf('"');
              boolean escape = false;
              if (index != -1) {
				  String subString = sWhereExp.substring(index + 1, lastIndex);
				  subString = FrameworkUtil.findAndReplace(subString,sQuote,"\\\'");
				  sWhereExp = sWhereExp.substring(0, index) + chDblQuotes + subString + chDblQuotes + sWhereExp.substring(lastIndex+1); // Fix for 067010
				  ContextUtil.pushContext(context);
				  MqlUtil.mqlCommand(context,"set escape on");
				  ContextUtil.popContext(context);
				  escape = true;
              }
	      try {
              // IR-040943V6R2011
                totalresultList = DomainObject.findObjects(context,
                                                           selType,
                                                           txtName,
                                                           txtRev,
                                                           txtOwner,
                                                           txtVault,
                                                           sWhereExp,
                                                           null,
                                                           true,
                                                           resultSelects,
                                                           Short.parseShort(queryLimit),
                                                           txtFormat,
                                                           txtSearch);
	      } finally {
                if (escape) {
			  	ContextUtil.pushContext(context);
			  	MqlUtil.mqlCommand(context,"set escape off");
                ContextUtil.popContext(context);
                }
	      }
                //IR-040943V6R2011 ends
       }
          catch(Exception e)
          {
              e.printStackTrace();
          }
      }
      else
      {
        matrix.db.Query query = new matrix.db.Query(savedQueryName);
        query.open(context);
        query.setObjectLimit(Short.parseShort(queryLimit));
        ContextUtil.startTransaction(context, true);
        try {
        totalresultList = FrameworkUtil.toMapList(query.getIterator(context, resultSelects, Short.parseShort(queryLimit)));
        } catch (FrameworkException fe) {
     	   ContextUtil.abortTransaction(context);
     	   throw fe;
        }
        ContextUtil.commitTransaction(context);
        query.close(context);
      }
    }
    else
    {
        totalresultList = SetUtil.getMembers(context,
                                             sSetName,
                                             resultSelects);
    }

  String removeSetName= (String)paramMap.get("removeSet");
    if (removeSetName != null && removeSetName.length() > 0)
    {
      matrix.db.Set setToRemove= new matrix.db.Set(removeSetName);
      setToRemove.remove(context);
    }

    return totalresultList;
  }

  /**
  * Returns a Vector of Strings specifying whether the Part is a Manufacturer Equivalent or Enterprise Part.
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds a packed HashMap of the following entries:
  * paramList - a Map of parameter values.
  * objectList - a MapList of object information.
  * @return Vector containing part origin.
  * @throws Exception if the operation fails.
  * @since 10.0.0.0.
  */
  public Vector getPartClassification(Context context , String[] args) throws Exception
    {
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    String objectId = null;
    MapList objectList = (MapList)paramMap.get("objectList");

    Map dataMap = null;
    Vector partOriginVect = new Vector();
    String langStr = context.getSession().getLanguage();
    String strManufacturerEquivalent=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.MfgEquivalent",langStr);
    String strEnterprise=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Enterprise",langStr);

    int objectListSize = objectList.size();
    String[] oidList = new String[objectListSize];
    for(int i = 0; i < objectListSize; i++) {
      dataMap = (HashMap)objectList.get(i);
      objectId = (String)dataMap.get("id");
      oidList[i] = objectId;
    }

    StringList objectSelect = new StringList(1);
    objectSelect.addElement("policy.property[PolicyClassification].value");

        //Retrieve value from admin property policy classification.
        String policyClassification = null;
        BusinessObjectWithSelectList bowsList = BusinessObject.getSelectBusinessObjectData(context,oidList,objectSelect);
        BusinessObjectWithSelect bows = null;
        //changed the name of veriable to enumElement for JDK1.5 issue
        Enumeration enumElement = bowsList.elements();
        while(enumElement.hasMoreElements()) {
          try {
            bows = (BusinessObjectWithSelect)enumElement.nextElement();
            policyClassification = bows.getSelectData("policy.property[PolicyClassification].value");

            if(policyClassification.equalsIgnoreCase("Equivalent")) {
                partOriginVect.add(strManufacturerEquivalent);
            } else {
                partOriginVect.add(strEnterprise);
            }
          }catch(Exception e) {
            // If admin property is not present then part is Enterprise Part.
              partOriginVect.add(strEnterprise);
          }
        }
    return partOriginVect;
  }

/**
  * Returns a Vector of hyperlinks to the Manufacturer for Equivalent Parts.
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds a packed HashMap of the following entries:
  * paramList - a Map of parameter values.
  * objectList - a MapList of object information.
  * @return Vector containing hyperlinks to the Manufacturer for the Equivalent Part.
  * @throws Exception if the operation fails.
  * @since 10.0.0.0.
  */
  public Vector getManufacturerLink(Context context , String[] args)
                                                        throws Exception {

    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    MapList objectList = (MapList)paramMap.get("objectList");

    Vector manufacturerLinkVect = new Vector();
    StringBuffer manufacturerLink = null;
    if(objectList != null && objectList.size() > 0)
    {
      //construct array of ids
      int objectListSize = objectList.size();
      String[] oidList = new String[objectListSize];
      for(int i = 0; i < objectListSize; i++) {
         Map dataMap = (Map)objectList.get(i);
         oidList[i] = (String)dataMap.get("id");
      }

      String SELECT_MANUFACTURER_EQUIVALENT_TYPE = "from["+RELATIONSHIP_MANUFACTURER_EQUIVALENT+"].to.type";
      String SELECT_MANUFACTURER_NAME = "from["+RELATIONSHIP_MANUFACTURER_EQUIVALENT+"].to.to["+RELATIONSHIP_MANUFACTURING_RESPONSIBILITY+"].from.name";
      String SELECT_MANUFACTURER_ID = "from["+RELATIONSHIP_MANUFACTURER_EQUIVALENT+"].to.to["+RELATIONSHIP_MANUFACTURING_RESPONSIBILITY+"].from.id";

      //construct selects
      SelectList resultSelects = new SelectList(2);
      DomainObject.MULTI_VALUE_LIST.add(SELECT_MANUFACTURER_EQUIVALENT_TYPE);
      resultSelects.add(SELECT_MANUFACTURER_EQUIVALENT_TYPE);
      DomainObject.MULTI_VALUE_LIST.add(SELECT_MANUFACTURER_NAME);
      resultSelects.add(SELECT_MANUFACTURER_NAME);
      DomainObject.MULTI_VALUE_LIST.add(SELECT_MANUFACTURER_ID);
      resultSelects.add(SELECT_MANUFACTURER_ID);

      //get Manufacters' name and id
      MapList totalresult = DomainObject.getInfo(context, oidList, resultSelects);

      if(totalresult != null && totalresult.size() > 0)
      {
          int objectCounts = totalresult.size();
          for(int n=0; n < objectCounts; n++)
          {
              Map temp = (Map)totalresult.get(n);
              StringList mepTypes = (StringList)temp.get(SELECT_MANUFACTURER_EQUIVALENT_TYPE);
              StringList manufacturerNames = (StringList)temp.get(SELECT_MANUFACTURER_NAME);
              StringList manufacturerIds = (StringList)temp.get(SELECT_MANUFACTURER_ID);

              if(manufacturerNames != null && manufacturerNames.size() > 0) {
                    manufacturerLink = new StringBuffer(256);
                    for(int j=0;j<manufacturerNames.size();j++) {
                      String mepType = (String)mepTypes.get(j);
                      if(mepType.equals(TYPE_MPN))  //Block MPN
                          continue;
                      String name = (String)manufacturerNames.get(j);
                      String id = (String)manufacturerIds.get(j);
                      manufacturerLink.append("<table><tr><td>");
                      manufacturerLink.append("<b><a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=components&treeMenu=type_Company&objectId=");
                      manufacturerLink.append(id);
                      manufacturerLink.append("', '");
                      manufacturerLink.append("600");
                      manufacturerLink.append("', '");
                      manufacturerLink.append("400");
                      manufacturerLink.append("', 'false', '");
                      manufacturerLink.append("popup");
                      manufacturerLink.append("')");
                      manufacturerLink.append("\">");
                      manufacturerLink.append(name);
                      manufacturerLink.append("</a></b>");
                      manufacturerLink.append("</td></tr></table>");
                    }
                    manufacturerLinkVect.add(manufacturerLink.toString());

              } else {
                    manufacturerLinkVect.add("");
              }
          }
      }
      //Added for Bug 313092
      DomainObject.MULTI_VALUE_LIST.remove(SELECT_MANUFACTURER_EQUIVALENT_TYPE);
      DomainObject.MULTI_VALUE_LIST.remove(SELECT_MANUFACTURER_NAME);
      DomainObject.MULTI_VALUE_LIST.remove(SELECT_MANUFACTURER_ID);
    }
    return manufacturerLinkVect;
  }

    /**
     * Checks that the Part being connected to the EBOM is in the State passed as an argument.
     * An error message is displayed if the Part cannot be connected.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input parameters:
     * 0 - a String containing the object id of the Part to connect.
     * 1 - a String containing the symbolic name of the State.
     * @return 1 for failure and 0 for success.
     * @throws Exception if the operation fails.
     * @trigger RelationshipEBOMCreateCheck.
     * @since 10.0.0.0.
     */
    public int CheckECPartToConnectAsEBOM(Context context, String[] args)
      throws Exception
    {
      String toId = args[0];
      String stateSymbolic = args[1];
      String stateRealName = PropertyUtil.getSchemaProperty(context,"policy",
                                            POLICY_PART,stateSymbolic);


      SelectList sPartSelStmts = new SelectList(2);
      sPartSelStmts.add(SELECT_CURRENT);
      sPartSelStmts.add(SELECT_POLICY);

      Part selObj = (Part)DomainObject.newInstance(context,DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
      selObj.setId(toId);
      Map objMap = selObj.getInfo(context, (StringList)sPartSelStmts);
      String currentState = (String)objMap.get(SELECT_CURRENT);
      String sPolicy = (String)objMap.get(SELECT_POLICY);

//added for bug 285409
      String strPolicyClass = EngineeringUtil.getPolicyClassification(context, sPolicy);

      if(sPolicy != null && strPolicyClass.equalsIgnoreCase("production")
                        && currentState.equals(stateRealName) && !selObj.canAttachECR(context))
      {
 //end of fix
        String strError = selObj.getInfo(context, SELECT_TYPE) + " " +  selObj.getInfo(context, SELECT_NAME) + " " + selObj.getInfo(context, SELECT_REVISION)  +
                            EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckIfECPartLatestReleasedRevision.Message1",
                                                           context.getSession().getLanguage()) +" " + stateRealName + " " +
                            EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckIfECPartLatestReleasedRevision.Message2",
                                                           context.getSession().getLanguage());
        emxContextUtil_mxJPO.mqlNotice(context,strError);
        return 1;
      }
      return 0;
    }

    /**
     * Returns true if this Part is a Manufacturer Equivalent Part
     * otherwise return false.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @return boolean true if Part Policy is "Equivalent", else false.
     * @throws Exception if the operation fails.
     * @since EC 10.0.0.0.
     */
    public boolean isMEP(Context context)
         throws Exception
    {
        String policyName = getPolicy(context).getName();
        String policyClass = PropertyUtil.getAdminProperty(context, "policy", policyName, "PolicyClassification");

        return "Equivalent".equals(policyClass);
    }

    /**
     * Notify all users with the Component Engineer Role
     * that a Manufacturer Equivalent Part was created by someone other than
     * a Component Engineer.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - a String containing the subject of the message.
     * 1 - a String containing the message.
     * 2 - a String containing the resouce bundle name.
     * @throws Exception if the operation fails.
     * @since EC 10.0.0.0.
     * @trigger TypePartCreateMEAction.
     */
    public void notifyComponentEngineerOfMEPCreate(Context context, String[] args)
                    throws Exception
    {
        try
        {
            // args[] parameters
            String sSubject = args[0];
            String sMessage = args[1];
            String sResourceBundle = args[2];

            //This notification only needs to happen for Manufacturer Equivalent Parts
            if (isMEP(context))
            {
                String sCompEngRoleName = PropertyUtil.getSchemaProperty(context,"role_ComponentEngineer");
                //get object's originator

                String originator = getAttributeValue(context, DomainConstants.ATTRIBUTE_ORIGINATOR);

                // Added  for the MCC Bug 330429 - Start
                // When Supplier Representative imports the data via supplier portal applications import will happen thru
                // super user as Supplier Rep does not have any previlages for create/connection
                // This method will try to notify to all component engineer user roles if mep originator is not a Component Engineer role
                // in MCC use case MEP owner will be the user user and the code of creating person object will fail
                // fix made to set an RPE variable to pass a user name before pusing the context and check whether originator is the
                // original user to check for the notification.
                String rpeUser = PropertyUtil.getGlobalRPEValue(context, "MCCPortalUser");
                if(rpeUser != null && !"".equals(rpeUser))
                {
                    originator = rpeUser;
                }

                // Added  for the MCC Bug 330429 - end

                //if object's originator does not have Component Engineer Role
                com.matrixone.apps.common.Person origPerson = com.matrixone.apps.common.Person.getPerson(context, originator);
                if (!origPerson.hasRole(context, sCompEngRoleName))
                {
                    Role matrixRole = new Role(sCompEngRoleName);
                    matrixRole.open(context);

                    UserList assignments = matrixRole.getAssignments(context);
                    UserItr itr = new UserItr(assignments);

                    StringList toList = new StringList(assignments.size());
                    while (itr.next())
                    {
                        toList.add(itr.obj().getName());
                    }
                    StringList ccList = new StringList();
                    StringList bccList = new StringList();
                    StringList objectIdList = new StringList();

                    objectIdList.add(getInfo(context, SELECT_ID));

                    // notify all Component Engineers
                    emxMailUtil_mxJPO.sendNotification(context,
                                        toList, ccList, bccList,
                                        sSubject,null, null,
                                        sMessage, null, null,
                                        objectIdList, null,
                                        sResourceBundle) ;
                }
            }
        }
        catch (Exception ex)
        {
          throw ex;
        }
    }

  /**
   * Returns a Vector of hyperlinks to the Equivalent Part for Enterprise & Manufacturer Equivalent parts.
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds a packed HashMap with the following entries:
   * objectList - a MapList of object information.
   * paramList - a Map of parameter values, SuiteDirectory and suiteKey must be set..
   * @return Vector containing Equivalents Links.
   * @exception Exception if the operation fails.
   * @since AEF 10.5.
   */
   public Vector getEquivalentLink (Context context,String[] args)
         throws Exception
   {
       Vector result = new Vector();
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       MapList objectList = (MapList)paramMap.get("objectList");

       Map paramList = (HashMap)paramMap.get("paramList");
       String suiteDir = (String)paramList.get("SuiteDirectory");
       String suiteKey = (String)paramList.get("suiteKey");

       String LocContextEntId = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].from.id";
       DomainObject.MULTI_VALUE_LIST.add(LocContextEntId);
       String LocContextEntType = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].from.type";
       DomainObject.MULTI_VALUE_LIST.add(LocContextEntType);
       String LocContextEntName = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].from.name";
       DomainObject.MULTI_VALUE_LIST.add(LocContextEntName);
       String LocContextEntRev = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].from.revision";
       DomainObject.MULTI_VALUE_LIST.add(LocContextEntRev);

       String CorpContextEntId = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.id";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextEntId);
       String CorpContextEntType = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.type";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextEntType);
       String CorpContextEntName = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.name";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextEntName);
       String CorpContextEntRev = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.revision";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextEntRev);

       String LocContextMEPId = "from[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].to.from[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].to.id";
       DomainObject.MULTI_VALUE_LIST.add(LocContextMEPId);
       String LocContextMEPType = "from[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].to.from[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].to.type";
       DomainObject.MULTI_VALUE_LIST.add(LocContextMEPType);
       String LocContextMEPName = "from[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].to.from[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].to.name";
       DomainObject.MULTI_VALUE_LIST.add(LocContextMEPName);
       String LocContextMEPRev = "from[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].to.from[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].to.revision";
       DomainObject.MULTI_VALUE_LIST.add(LocContextMEPRev);

       String CorpContextMEPId = "from[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].to.id";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextMEPId);
       String CorpContextMEPType = "from[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].to.type";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextMEPType);
       String CorpContextMEPName = "from[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].to.name";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextMEPName);
       String CorpContextMEPRev = "from[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].to.revision";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextMEPRev);

       String policyClass = "policy.property[PolicyClassification].value";

       StringList selectStmts = new StringList(19);
       selectStmts.addElement(SELECT_ID);
       selectStmts.addElement(SELECT_TYPE);
       selectStmts.addElement(SELECT_NAME);
       selectStmts.addElement(SELECT_REVISION);

       selectStmts.addElement(LocContextEntId);
       selectStmts.addElement(LocContextEntType);
       selectStmts.addElement(LocContextEntName);
       selectStmts.addElement(LocContextEntRev);
       selectStmts.addElement(CorpContextEntId);
       selectStmts.addElement(CorpContextEntType);
       selectStmts.addElement(CorpContextEntName);
       selectStmts.addElement(CorpContextEntRev);

       selectStmts.addElement(LocContextMEPId);
       selectStmts.addElement(LocContextMEPType);
       selectStmts.addElement(LocContextMEPName);
       selectStmts.addElement(LocContextMEPRev);
       selectStmts.addElement(CorpContextMEPId);
       selectStmts.addElement(CorpContextMEPType);
       selectStmts.addElement(CorpContextMEPName);
       selectStmts.addElement(CorpContextMEPRev);

       selectStmts.addElement(policyClass);

       try
       {
           Iterator itr = objectList.iterator();
           int i=0;
           int count = objectList.size();
           String[] arrobjectId = new String[count];
           while (itr.hasNext())
           {
               Map m = (Map) itr.next();
               arrobjectId[i] = (String)m.get("id");
               i++;
           }
           MapList listEquiv = DomainObject.getInfo(context, arrobjectId, selectStmts);
           StringList entIdList = null;
           StringList entTypeList = null;
           StringList entNameList = null;
           StringList entRevList = null;
           String entId = "";
           String entType = "";
           String entName = "";
           String entRev = "";
           String locId = "";
           String locType = "";
           String locName = "";
           String locRev = "";
           String corpId = "";
           String corpType = "";
           String corpName = "";
           String corpRev = "";

           boolean hasEquiv = false;
           for(i=0; i < listEquiv.size(); i++)
           {
               StringBuffer output = new StringBuffer(512);
               hasEquiv = false;
               Map map = (Map)listEquiv.get(i);
              if( map.get(policyClass) != null && ((String)map.get(policyClass)).equalsIgnoreCase("Equivalent") )
               {
                   locId = LocContextEntId;
                   locType = LocContextEntType;
                   locName = LocContextEntName;
                   locRev = LocContextEntRev;
                   corpId = CorpContextEntId;
                   corpType = CorpContextEntType;
                   corpName = CorpContextEntName;
                   corpRev = CorpContextEntRev;
               }
               else
               {
                   locId = LocContextMEPId;
                   locType = LocContextMEPType;
                   locName = LocContextMEPName;
                   locRev = LocContextMEPRev;
                   corpId = CorpContextMEPId;
                   corpType = CorpContextMEPType;
                   corpName = CorpContextMEPName;
                   corpRev = CorpContextMEPRev;
               }
               //get Location Context Enterprise parts
               entIdList = (StringList) map.get(locId);
               entTypeList = (StringList) map.get(locType);
               entNameList = (StringList) map.get(locName);
               entRevList = (StringList) map.get(locRev);
               if (entIdList != null && entIdList.size() > 0)
               {
                   for (int j=0; j < entIdList.size(); j++)
                   {
                       entType = (String)entTypeList.get(j);
                       if (entType.equals(TYPE_MPN)) //Block MPN
                       {
                           continue;
                       }
                       hasEquiv = true;
                       entId = (String)entIdList.get(j);
                       entName = (String)entNameList.get(j);
                       entRev = (String)entRevList.get(j);
                       output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory="+suiteDir+"&suiteKey="+suiteKey+"&objectId="+ entId +"', '', '', 'false', 'popup', '')\">"+entName+" " +entRev+"</a> <br>");
                   }
               }
               //get Corporate Context Enterprise parts
               entIdList = (StringList) map.get(corpId);
               entTypeList = (StringList) map.get(corpType);
               entNameList = (StringList) map.get(corpName);
               entRevList = (StringList) map.get(corpRev);
               if (entIdList != null && entIdList.size() > 0 )
               {

                   for (int j=0; j < entIdList.size(); j++)
                   {
                       //skip the location equivalent objects & MPN - these are handled in the above loop
                       entType = (String)entTypeList.get(j);
                       if (entType.equals(TYPE_LOCATION_EQUIVALENT_OBJECT) || entType.equals(TYPE_MPN))
                       {
                           continue;
                       }
                       hasEquiv = true;
                       entId = (String)entIdList.get(j);
                       entName = (String)entNameList.get(j);
                       entRev = (String)entRevList.get(j);
                       output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory="+suiteDir+"&suiteKey="+suiteKey+"&objectId="+ entId +"', '', '', 'false', 'popup', '')\">"+entName+" " +entRev+"</a> <br>");
                   }
               }
               if(!"".equals(output.toString())) {
                  result.add(output.toString());
               }

               if(!hasEquiv) {
                 result.add("&nbsp;");
               }
           }

       }catch (FrameworkException Ex) {
            throw Ex;
       }

       //Added for Bug 313092
       DomainObject.MULTI_VALUE_LIST.remove(LocContextEntId);
       DomainObject.MULTI_VALUE_LIST.remove(LocContextEntType);
       DomainObject.MULTI_VALUE_LIST.remove(LocContextEntName);
       DomainObject.MULTI_VALUE_LIST.remove(LocContextEntRev);
       DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntId);
       DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntType);
       DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntName);
       DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntRev);

       DomainObject.MULTI_VALUE_LIST.remove(LocContextMEPId);
       DomainObject.MULTI_VALUE_LIST.remove(LocContextMEPType);
       DomainObject.MULTI_VALUE_LIST.remove(LocContextMEPName);
       DomainObject.MULTI_VALUE_LIST.remove(LocContextMEPRev);

       DomainObject.MULTI_VALUE_LIST.remove(CorpContextMEPId);
       DomainObject.MULTI_VALUE_LIST.remove(CorpContextMEPType);
       DomainObject.MULTI_VALUE_LIST.remove(CorpContextMEPName);
       DomainObject.MULTI_VALUE_LIST.remove(CorpContextMEPRev);

       return result;
   }//end of method getEquivalentLink ()

 
/**
 * Retrieves the Manufacturer Equvialent Parts from the MapList.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds a packed HashMap of the following entries:
 * objectList - a MapList of object information.
 * paramList - a Map of parameter values, reportFormat and exportFormat can be set.
 * @return Vector of Manufacturer Equivalent Parts.
 * @throws Exception if the operation fails.
 * @since 10.5.
 */
public Vector getMEPart (Context context, String[] args)
    throws Exception
{
    Vector mePartVtr = new Vector();
    try {
         mePartVtr =  getObjectFromAVLList (context, args, "mePartId");
    }
    catch(Exception e){
          throw e;
   }
    return mePartVtr;
}

/**
 * Retrieves the Manufacturer related to the Manufacturer Equivalent Part from the MapList.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds a packed HashMap of the following entries:
 * objectList - a MapList of object information.
 * paramList - a Map of parameter values, reportFormat and exportFormat can be set.
 * @return Vector of Manufacturers.
 * @throws Exception if the operation fails.
 * @since 10.5.
 */
public Vector getManufacturer (Context context, String[] args)
    throws Exception
{
     Vector manufacturerVtr = new Vector();
     try {
         manufacturerVtr =  getObjectFromAVLList (context, args, "ManufacturerId");
     }
     catch(Exception e){
          throw e;
     }
    return manufacturerVtr;
}

/**
 * Retrieves the Location related to the ME Part from the MapList.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds a packed HashMap of the following entries:
 * objectList - a MapList of object information.
 * paramList - a Map of parameter values, reportFormat and exportFormat can be set.
 * @return Vector of Locations.
 * @throws Exception if the operation fails.
 * @since 10.5.
 */
public Vector getLocation (Context context, String[] args)
    throws Exception
{
     Vector locationVtr = new Vector();
     try {
         locationVtr =  getObjectFromAVLList (context, args, "Location Id");
     }
     catch(Exception e){
          throw e;
     }
    return locationVtr;
}

/**
 * Retrieves the Location Status attribute value from the MapList.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds a packed HashMap of the following entries:
 * objectList - a MapList of object information.
 * paramList - a Map of parameter values, reportFormat and exportFormat can be set.
 * @return Vector of Location Status.
 * @throws Exception if the operation fails.
 * @since 10.5.
 */
public Vector getLocationStatus (Context context, String[] args)
    throws Exception
{
    Vector locationStatusVtr = new Vector();
    try {
        locationStatusVtr = getAttributeFromAVLList (context, args, DomainConstants.ATTRIBUTE_LOCATION_STATUS);
    }
    catch(Exception e){
          throw e;
    }
    return locationStatusVtr;
}

/**
 * Retrieves the Location Preference attribute value from the MapList.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds a packed HashMap of the following entries:
 * objectList - a MapList of object information.
 * paramList - a Map of parameter values, reportFormat and exportFormat can be set.
 * @return Vector of Location Preference.
 * @throws Exception if the operation fails.
 * @since 10.5.
 */
public Vector getLocationPreference (Context context, String[] args)
    throws Exception
{
    Vector locationPrefVtr = new Vector();
    try {
        locationPrefVtr = getAttributeFromAVLList (context, args, DomainConstants.ATTRIBUTE_LOCATION_PREFERENCE);
    }
    catch(Exception e){
         throw e;
    }
    return locationPrefVtr;
}

/**
 * Retrieves Object values from the MapList.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds a packed HashMap of the following entries:
 * objectList - a MapList of object information.
 * paramList - a Map of parameter values, reportFormat and exportFormat can be set.
 * @param id String Id of the Object.
 * @return Vector of Objects.
 * @throws Exception if the operation fails.
 * @since 10-5.
 */
public Vector getObjectFromAVLList (Context context, String[] args, String id)
    throws Exception
{
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)programMap.get("objectList");

    Map paramList = (HashMap)programMap.get("paramList");
    String reportFormat = (String)paramList.get("reportFormat");
    String exportFormat = (String)paramList.get("exportFormat");

    Vector vector = new Vector(objList.size());
    MapList innerMapList = new MapList();
    StringBuffer strBuf = new StringBuffer(32);
    StringBuffer sbURL = new StringBuffer();
    String partName = "";
    String partId = "";

    try {
        //first get all Name values for ids with one DB call
        StringList arrObjIdList = new StringList(objList.size());
        Iterator i = objList.iterator();
        while (i.hasNext())
        {
            Map map = (Map) i.next();
            innerMapList = (MapList)map.get("AVLData");
            if (innerMapList.size()>0){
                Iterator j = innerMapList.iterator();
                while (j.hasNext())
                {
                     Map innerMap = (Map) j.next();
                     partId = (String)innerMap.get(id);
                     if (partId != null && !("").equals(partId))
                     {
                         arrObjIdList.addElement(partId);
                     }
                }
            }
        }
        String[] arrObjId = new String[arrObjIdList.size()];
        for (int idx=0; idx < arrObjIdList.size(); idx++)
        {
            arrObjId[idx] = (String)arrObjIdList.get(idx);
        }
//IR-055641 : added Type as selectables
        StringList selectStmts = new StringList(2);
        selectStmts.addElement(SELECT_NAME);
        selectStmts.addElement(SELECT_TYPE);
        MapList listNames = DomainObject.getInfo(context, arrObjId, selectStmts);

        //reset Iterator
        i = objList.iterator();
        int idx = 0;
        while (i.hasNext())
        {
            Map map = (Map) i.next();
            innerMapList = (MapList)map.get("AVLData");
            if (innerMapList.size()>0){
                boolean multipleRows = isMultipleRows(innerMapList, id);
                Iterator j = innerMapList.iterator();
                while (j.hasNext())
                {
                      Map innerMap = (Map) j.next();
                      partId = (String)innerMap.get(id);
                      if (partId != null && !("").equals(partId))
                      {
                        Map nameMap = (Map)listNames.get(idx);

                        //IR-055641 Starts: Checking for type. If type equals MPN then need to read the display value from property and display in the report.
                        String type = (String)nameMap.get(SELECT_TYPE);
                        if (TYPE_MPN.equals(type)) {
                            String showBlankName = EnoviaResourceBundle
                                    .getProperty(context, "emxManufacturerEquivalentPart.EngrPlaceholderMEP.ShowBlankName");
                            if ("false".equalsIgnoreCase(showBlankName)) {
                            	partName = EnoviaResourceBundle.getProperty(context, "emxManufacturerEquivalentPartStringResource", context.getLocale(),"emxManufacturerEquivalentPart.EngrPlaceholderMEP.DefaultName");

                            }
                        } else {
                            partName = (String) nameMap.get(SELECT_NAME);
                        }
                        //IR-055641 Ends
                        idx++;
                        if (!("").equals(partName) && !("").equals(partId))
                        {
                           //do not show hyperlinks if it is a printer friendly or excel export page
                           //length will be >0 when format is HTML, ExcelHTML, CSV or TXT
                           if(null!=reportFormat && !reportFormat.equals("null") && (reportFormat.length() > 0))
                           {
                                  if (multipleRows){
                                    strBuf.append(partName);
									strBuf.append(' ');  //IR-069529V6R2012 1st issue
                                  } else {
                               strBuf.append(partName);
                                  }
                           }
                           else
                           {
                               sbURL.append("../common/emxTree.jsp?objectId=");
							   sbURL.append(XSSUtil.encodeForURL(context, partId));
                               strBuf.append("<a href='javascript:showModalDialog(\""+sbURL.toString()+"\",575,575)'>");
                               strBuf.append(XSSUtil.encodeForHTMLAttribute(context, partName));
							   strBuf.append("</a>");
                               strBuf.append("&nbsp;<br/><br/>");
                           }
                        }
                        else
                        {
                            strBuf.append("&nbsp;");
                        }
                     }
                     else
                     {
                        // this line is added to remove the &nbsp; in the excel sheet.
                        if (null!=exportFormat && !exportFormat.equals("null") && (exportFormat.length() > 0))
                        {
                           strBuf.append(' ');
                        }
                        else
                        {
                           strBuf.append("&nbsp;");
                        }
                     }
                     sbURL.delete(0,sbURL.length());
                }
                vector.add(strBuf.toString());
                strBuf.delete(0,strBuf.length());
            }
        }
    }
    catch (Exception e){
        throw e;
    }
    return vector;
}

/**
 * Retrieves Attribute values from the MapList.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds a packed HashMap of the following entries:
 * objectList - a MapList of object information.
 * paramList - a Map of parameter values, reportFormat and exportFormat can be set.
 * @param attName String Name of the Attribute.
 * @return Vector of Attributes.
 * @throws Exception if the operation fails.
 * @since 10-5.
 */
public Vector getAttributeFromAVLList (Context context, String[] args, String attName)
    throws Exception
{
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)programMap.get("objectList");

    Map paramList = (HashMap)programMap.get("paramList");
    String reportFormat = (String)paramList.get("reportFormat");
    String exportFormat = (String)paramList.get("exportFormat");
    String allocRespId = "";

    Vector vector = new Vector(objList.size());
    String value = "";
    MapList innerMapList = new MapList();
    StringBuffer strBuf = new StringBuffer();
    try {
        //first get all attribute values for ids with one DB call
        StringList arrRelIdList = new StringList(objList.size());
        Iterator i = objList.iterator();
        while (i.hasNext())
        {
            Map map = (Map) i.next();
            innerMapList = (MapList)map.get("AVLData");
            if (innerMapList.size()>0){
                Iterator j = innerMapList.iterator();
                while (j.hasNext())
                {
                     Map innerMap = (Map) j.next();
                     allocRespId = (String) innerMap.get("Allocation Responsibility Id");
                     if (allocRespId!=null && !("").equals(allocRespId))
                     {
                         arrRelIdList.addElement(allocRespId);
                     }
                }
            }
        }
        String[] arrRelId = new String[arrRelIdList.size()];
        for (int idx=0; idx < arrRelIdList.size(); idx++)
        {
            arrRelId[idx] = (String)arrRelIdList.get(idx);
        }

        StringList selectRelStmts = new StringList(1);
        selectRelStmts.addElement("attribute["+ attName +"]");
        MapList listAttribute = DomainRelationship.getInfo(context, arrRelId, selectRelStmts);

        i = objList.iterator();
        int idx = 0;
        while (i.hasNext())
        {
             Map map = (Map) i.next();
             innerMapList = (MapList)map.get("AVLData");
             if (innerMapList.size()>0){
               boolean multipleRows = isMultipleRows(innerMapList, "Allocation Responsibility Id");
             Iterator j = innerMapList.iterator();
             while (j.hasNext())
            {
                 Map innerMap = (Map) j.next();
                 allocRespId = (String) innerMap.get("Allocation Responsibility Id");
                 if (allocRespId!=null && !("").equals(allocRespId))
                 {
                    Map attrMap = (Map)listAttribute.get(idx);
                    value = (String)attrMap.get("attribute["+ attName +"]");
                    if(attName.equals(DomainConstants.ATTRIBUTE_LOCATION_STATUS) || attName.equals(DomainConstants.ATTRIBUTE_LOCATION_PREFERENCE)){
                    	String attNameKey = "emxFramework.Range." + FrameworkUtil.findAndReplace(attName.trim(), " ", "_");
                    	value = attNameKey + "." + FrameworkUtil.findAndReplace(value.trim(), " ", "_");
                    }
                  //Multitenant
                    value = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),value);
                    idx++;

                    if (value != null)
                    {
                       //do not show hyperlinks if it is a printer friendly or excel export page
                      //length will be >0 when format is HTML, ExcelHTML, CSV or TXT
                       if(null!=reportFormat && !reportFormat.equals("null") && (reportFormat.length() > 0))
                       {
                           if (multipleRows)
                           {
                               strBuf.append(XSSUtil.encodeForHTML(context, value));
							   strBuf.append('\n');
                           }
                           else {
                          strBuf.append(XSSUtil.encodeForHTML(context,value));
                              }
                       }
                       else
                       {
                          strBuf.append(XSSUtil.encodeForHTML(context,value));
                          strBuf.append("&nbsp;<br/><br/>");
                       }
                    }
                    else
                    {
                       strBuf.append("&nbsp;");
                    }
                 }
                 else
                 {
                    // this line is added to remove the &nbsp; in the excel sheet.
                    if (null!=exportFormat && !exportFormat.equals("null") && (exportFormat.length() > 0))
                    {
                       strBuf.append(' ');
                    }
                    else
                    {
                       strBuf.append("&nbsp;");
                    }
                 }
             }// while inner
            vector.add(strBuf.toString());
            strBuf.delete(0,strBuf.length());
          }
        }// while outer
    }
    catch (Exception e) {
        throw e;
    }
    return vector;
}

  /**
    * Gets the Manufacturer Equivalent Parts attached to an Enterprise Part.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a HashMap of the following entries:
    * objectId - a String containing the Enterprise Part id.
    * @return a MapList of Manufaturer Equivalent Part object ids and relationship ids.
    * @throws Exception if the operation fails.
    * @since 10.5.
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getEnterpriseManufacturerEquivalents (Context context,String[] args)
           throws Exception
    {
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       String objectId = (String) paramMap.get("objectId");
       String isMPN = (String) paramMap.get("isMPN");

       MapList equivList = new MapList();

       MapList listLocEquivMEPs = new MapList();
       MapList listCorpMEPs = new MapList();

       try
       {
           DomainObject partObj = DomainObject.newInstance(context,objectId);

           StringList selectStmts = new StringList(4);
           selectStmts.addElement(SELECT_ID);
           selectStmts.addElement(SELECT_TYPE);
           selectStmts.addElement(SELECT_NAME);
           selectStmts.addElement(SELECT_REVISION);

           StringList selectRelStmts = new StringList(1);
           selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

           StringBuffer sbRelPattern = new StringBuffer(RELATIONSHIP_LOCATION_EQUIVALENT);
              sbRelPattern.append(',');
              sbRelPattern.append(RELATIONSHIP_MANUFACTURER_EQUIVALENT);

           StringBuffer typePattern = new StringBuffer(TYPE_PART);
           if(isMPN == null || isMPN.equalsIgnoreCase("True")) {
              typePattern.append(',');
              typePattern.append(TYPE_MPN);
           }

           StringBuffer sbTypePattern = new StringBuffer(typePattern.toString());
              sbTypePattern.append(',');
              sbTypePattern.append(TYPE_LOCATION_EQUIVALENT_OBJECT);

           //fetching list of related MEPs via location Equivalent Object
           listLocEquivMEPs = partObj.getRelatedObjects(context,
                                        sbRelPattern.toString() ,              // relationship pattern
                                        sbTypePattern.toString(),              // object pattern
                                        selectStmts,                 // object selects
                                        selectRelStmts,              // relationship selects
                                        false,                        // to direction
                                        true,                       // from direction
                                        (short) 2,                   // recursion level
                                        null,                        // object where clause
                                        null);                        // relationship where clause

           //fetching list of related MEPs via Manufacturer Equivalent
           listCorpMEPs = partObj.getRelatedObjects(context,
                                 RELATIONSHIP_MANUFACTURER_EQUIVALENT,              // relationship pattern
                                 typePattern.toString(),              // object pattern
                                 selectStmts,                 // object selects
                                 selectRelStmts,              // relationship selects
                                 false,                        // to direction
                                 true,                       // from direction
                                 (short) 1,                   // recursion level
                                 null,                        // object where clause
                                 null);                        // relationship where clause

           Map tempMap = null;

             //to hold ids of MEP connected to EP
             Vector vecMepId = new Vector();
             HashMap mapMepId = new HashMap();
             //to hold relIds with which MEPs are connected to EP
             // will hold Location Equiv rel id if MEP is Location Context
             // will hold Manufacturer Equiv rel id if MEP is Corporate Context
             Vector vecRelId = new Vector();


                 //Iterating to Location Context MEP list to load MEP Ids and rel Ids
           for(int i=0;i<listLocEquivMEPs.size();i++) {
              tempMap = (Map)listLocEquivMEPs.get(i);

              //Checking for level in resultlist: level 1 will have relationship id
              //level 2 will have mep object id
              // Adding id of MEP  and relationship id for type Location Equivalent
              if("2".equals((String)tempMap.get("level")) ) {
                 vecMepId.addElement(tempMap.get(SELECT_ID));
                 mapMepId.put(tempMap.get(SELECT_ID), tempMap);
              }

             if("1".equals((String)tempMap.get("level")) ) {
                   vecRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
              }
           }// end of for (listCorpMEPs)

           //Iterating to Corporate Context MEP list to load MEP Ids and rel Ids
           for(int i=0;i<listCorpMEPs.size();i++) {
              tempMap = (Map)listCorpMEPs.get(i);

              vecMepId.addElement(tempMap.get(SELECT_ID));
              mapMepId.put(tempMap.get(SELECT_ID), tempMap);
              vecRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
           }// end of for (listCorpMEPs)

          for(int k=0;k<vecMepId.size();k++) {
             Map resultMap = (Map)mapMepId.get((String)vecMepId.elementAt(k));
             resultMap.remove("level");// need to be removed , else show message as - the level sequence may not be as expected..
             resultMap.put(SELECT_RELATIONSHIP_ID, (String)vecRelId.elementAt(k));
             equivList.add(resultMap);
          }
       }
       catch (FrameworkException Ex)
       {
            throw Ex;
       }

       return equivList;
   }

  /**
    * Gets the Names of Location(s) with which a Manufacturer Equivalent Part is connected.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a HashMap of the following entries:
    * objectList - a MapList of object information.
    * paramList - a Map of parameter values, SuiteDirectory, suiteKey, reportFormat, publicPortal.
    * @return a StringList HTML output of Names of Location(s).
    * @throws Exception if the operation fails.
    * @since 10.5.
    */
   public StringList getMEPLocationNamesHTMLOutput (Context context,String[] args)
         throws Exception
   {
        StringList result = new StringList();
        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            Map paramList = (HashMap)paramMap.get("paramList");
            String suiteDir = (String)paramList.get("SuiteDirectory");
            String suiteKey = (String)paramList.get("suiteKey");
            String reportFormat = (String)paramList.get("reportFormat");

            boolean isexport = false;
            String export = (String)paramList.get("exportFormat");
            if ( export != null )
            {
                isexport = true;
            }

            String publicPortal = (String)paramList.get("publicPortal");
            String linkFile = (publicPortal != null && publicPortal.equalsIgnoreCase("true"))?"emxNavigator.jsp":"emxTree.jsp";

            MapList objectList = (MapList)paramMap.get("objectList");

            if(objectList != null && objectList.size() > 0)
            {
                //construct array of ids
                int objectListSize = objectList.size();
                String[] oidList = new String[objectListSize];
                for(int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map)objectList.get(i);
                    oidList[i] = (String)dataMap.get("id");
                }

                StringList selects = new StringList();
                //Select for Location Name and Id via the relationship route
                //Part --> Manufacture Equivalent --> Allocation Responsibility
                String manEquAloResNameSel =
                    "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" +
                    RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.name";
                String manEquAloResIdSel =
                    "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT+ "].from.to[" +
                    RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.id";

                //Select for Location Name and Id via the relationship route
                //Part --> Manufacture Equivalent --> Allocation Responsibility
                String aloResNameSel =
                    "to[" + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.name";
                String aloResIdSel =
                    "to[" + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].from.id";

                DomainObject.MULTI_VALUE_LIST.add(manEquAloResNameSel);
                selects.add(manEquAloResNameSel);

                DomainObject.MULTI_VALUE_LIST.add(manEquAloResIdSel);
                selects.add(manEquAloResIdSel);

                //Select Location Name and Id via the relationship route
                //Part --> Allocation Responsibility
                DomainObject.MULTI_VALUE_LIST.add(aloResNameSel);
                selects.add(aloResNameSel);

                DomainObject.MULTI_VALUE_LIST.add(aloResIdSel);
                selects.add(aloResIdSel);

                //Get Location Name and Id information
                MapList locMaplist = getInfo(context, oidList, selects);

                StringList manEquAloResNameList = null;
                StringList manEquAloResIdList = null;
                StringList aloResNameList = null;
                StringList aloResIdList = null;

                Iterator locMapListItr = locMaplist.iterator();
                while(locMapListItr.hasNext())
                {
                    Map locMap = (Map) locMapListItr.next();
                    //Get Location Name and Id via the relationship route
                    //Part --> Manufacture Equivalent --> Allocation Responsibility
                    manEquAloResNameList = (StringList)locMap.get(manEquAloResNameSel);
                    manEquAloResIdList = (StringList)locMap.get(manEquAloResIdSel);

                    //Get Location Name and Id via the relationship route
                    //Part --> Allocation Responsibility
                    aloResNameList = (StringList)locMap.get(aloResNameSel);
                    aloResIdList = (StringList)locMap.get(aloResIdSel);

                    if(manEquAloResNameList == null)
                    {
                        manEquAloResNameList = new StringList();
                    }
                    if(aloResNameList == null)
                    {
                        aloResNameList = new StringList();
                    }

                    if(manEquAloResIdList == null)
                    {
                        manEquAloResIdList = new StringList();
                    }
                    if(aloResIdList == null)
                    {
                        aloResIdList = new StringList();
                    }

                    //index of the last equiv location
                    int lastEquivLoc = -1;

                    //save the last equivalent location
                    if(manEquAloResNameList != null && manEquAloResNameList.size() > 0)
                    {
                        lastEquivLoc = manEquAloResNameList.size();
                    }
                    //Combine the above two Name lists intto one Name list
                    if(aloResNameList != null && aloResNameList.size() > 0)
                    {
                        for(int i=0; i < aloResNameList.size(); i++)
                        {
                            manEquAloResNameList.add((String)aloResNameList.get(i));
                        }
                    }

                    //Combine the above two Id lists into one Id list
                    if(aloResIdList != null && aloResIdList.size() > 0)
                    {
                        for(int j=0; j < aloResIdList.size(); j++)
                        {
                           manEquAloResIdList.add((String)aloResIdList.get(j));
                        }
                    }

                    StringBuffer output = new StringBuffer(" ");

                    Iterator locNameListItr    = manEquAloResNameList.iterator();
                    Iterator locIdListItr      = manEquAloResIdList.iterator();
                    int equivLocCount = 0;
                    while(locNameListItr.hasNext() && locIdListItr.hasNext())
                    {
                        String locName    = (String)locNameListItr.next();
                        String locId      = (String)locIdListItr.next();

                        //display the "(equiv)" if the relationship route is
                        //Part --> Manufacture Equivalent --> Allocation Responsibility
                        equivLocCount++;
                        String labelLocEquiv   = (lastEquivLoc >= equivLocCount) ? "(equiv)" : "";

                        if(isexport)
                        {
                            output.append(locName);
                        }
                        //do not show hyperlinks if it is a printer friendly or excel export page
                        //length will be >0 when format is HTML, ExcelHTML, CSV or TXT
                        else if(null!=reportFormat && !reportFormat.equals("null") && (reportFormat.length() > 0))
                        {
                           output.append(locName);
                        }
                        else
                        {
                            output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"+linkFile+"?emxSuiteDirectory=");
                            output.append(suiteDir);
                            output.append("&suiteKey=");
                            output.append(suiteKey);
                            output.append("&objectId=");
                            output.append(locId);
                            output.append("', '', '', 'false', 'popup', '')\">");
                            output.append(locName);
                            output.append("</a> <br>");
                            output.append(labelLocEquiv);
                            output.append(" <br>&nbsp;<br>");
                        }
                    }

                    if(!"".equals(output.toString())) {
                     result.add(output.toString());
                    }
                }//end while
            //Added for Bug 313092
            DomainObject.MULTI_VALUE_LIST.remove(manEquAloResNameSel);
            DomainObject.MULTI_VALUE_LIST.remove(manEquAloResIdSel);
            DomainObject.MULTI_VALUE_LIST.remove(aloResNameSel);
            DomainObject.MULTI_VALUE_LIST.remove(aloResIdSel);
        }//end if

       }catch (Exception Ex) {
           throw Ex;
       }

       return result;

   }//end of method getMEPLocationNamesHTMLOutput()

  /**
    * Gets the Location Status attribute value on Allocation Responsibility relationship between
    * Location(s) and an MEP.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a HashMap of the following entries:
    * objectList - a MapList of object information.
    * paramList - a Map of parameter values, SuiteDirectory, suiteKey.
    * @return a StringList HTML output of Status attribute value.
    * @throws Exception if the operation fails.
    * @since 10.5.
    */
    public StringList getMEPLocationStatusHTMLOutput (Context context,String[] args)
         throws Exception
    {
        StringList result = new StringList();
        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            Map paramList = (HashMap)paramMap.get("paramList");
            MapList objectList = (MapList)paramMap.get("objectList");

            boolean isexport = false;
            String export = (String)paramList.get("exportFormat");
            if ( export != null )
            {
                isexport = true;
            }

            if(objectList != null && objectList.size() > 0)
            {
                //construct array of ids
                int objectListSize = objectList.size();
                String[] oidList = new String[objectListSize];
                for(int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map)objectList.get(i);
                    oidList[i] = (String)dataMap.get("id");
                }

                StringList selects = new StringList();

                //Location Status select via the relationship route
                //Part --> Manufacture Equivalent --> Allocation Responsibility
                String manEquAloResStaSel =
                    "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" +
                    RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].attribute[" +
                    ATTRIBUTE_LOCATION_STATUS + "].value";

                //Location Status select via the relationship route
                //Part --> Allocation Responsibility
                String aloResStaSel =
                    "to[" + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].attribute[" +
                    ATTRIBUTE_LOCATION_STATUS + "].value";

                //Select for Location Status
                DomainObject.MULTI_VALUE_LIST.add(manEquAloResStaSel);
                selects.add(manEquAloResStaSel);

                DomainObject.MULTI_VALUE_LIST.add(aloResStaSel);
                selects.add(aloResStaSel);

                //Get Location Status Info
                MapList statusMaplist = getInfo(context, oidList, selects);

                StringList manEquAloResStaList = null;
                StringList aloResStaList = null;

                Iterator statusMapListItr = statusMaplist.iterator();
                while(statusMapListItr.hasNext())
                {
                    Map statusMap = (Map) statusMapListItr.next();
                    //Get Location Status
                    manEquAloResStaList = (StringList)statusMap.get(manEquAloResStaSel);
                    aloResStaList = (StringList)statusMap.get(aloResStaSel);

                    if(manEquAloResStaList == null)
                    {
                        manEquAloResStaList = new StringList();
                    }

                    if(aloResStaList == null)
                    {
                        aloResStaList = new StringList();
                    }

                    //Combine the above two status lists into one status list
                    if(aloResStaList != null && aloResStaList.size() > 0)
                    {
                        for(int i=0; i < aloResStaList.size(); i++)
                        {
                           manEquAloResStaList.add((String)aloResStaList.get(i));
                        }
                    }

                    StringBuffer output = new StringBuffer(" ");
                    Iterator statusListItr = manEquAloResStaList.iterator();
                    while(statusListItr.hasNext())
                    {
                        String allocStatus = (String)statusListItr.next();
                        allocStatus = allocStatus.replace(' ','_');
                        allocStatus = "emxFramework.Range.Location_Status."+ allocStatus;
                        //Multitenant
                        allocStatus = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),allocStatus);

                        output.append(allocStatus);
                        if(isexport)
                        {
                            output.append(" \n");
                        }
                        else
                        {
                            output.append(" <br>&nbsp;<br>");
                        }

                    }

                    if(!"".equals(output.toString())) {
                        result.add(output.toString());
                    }
                }//end while
                //Added for Bug 313092
                DomainObject.MULTI_VALUE_LIST.remove(manEquAloResStaSel);
                DomainObject.MULTI_VALUE_LIST.remove(aloResStaSel);

            }//end if

       }catch (Exception Ex) {
           throw Ex;
       }

       return result;
   }//end of method getMEPLocationStatusHTMLOutput()

  /**
    * Gets the Location Preference attribute value on Allocation Responsibility relationship between
    * Location(s) and MEP.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a HashMap of the following entries:
    * objectList - a MapList of object information.
    * paramList - a Map of parameter values, SuiteDirectory, suiteKey.
    * @return a StringList HTML output of Status attribute value.
    * @throws Exception if the operation fails.
    * @since 10.5.
    */
    public StringList getMEPLocationPreferenceHTMLOutput (Context context,String[] args)
         throws Exception
    {
        StringList result = new StringList();
        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            Map paramList = (HashMap)paramMap.get("paramList");
            MapList objectList = (MapList)paramMap.get("objectList");

            boolean isexport = false;
            String export = (String)paramList.get("exportFormat");
            if ( export != null )
            {
                isexport = true;
            }

            if(objectList != null && objectList.size() > 0)
            {
                //construct array of ids
                int objectListSize = objectList.size();
                String[] oidList = new String[objectListSize];
                for(int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map)objectList.get(i);
                    oidList[i] = (String)dataMap.get("id");
                }

                StringList selects = new StringList();

                //Location Preference select via the relationship route
                //Part --> Manufacture Equivalent --> Allocation Responsibility
                String manEquAloResPreSel =
                    "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" +
                    RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].attribute[" +
                    ATTRIBUTE_LOCATION_PREFERENCE + "].value";

                //Location Preference select via the relationship route
                //Part --> Allocation Responsibility
                String aloResPreSel =
                    "to[" + RELATIONSHIP_ALLOCATION_RESPONSIBILITY + "].attribute[" +
                    ATTRIBUTE_LOCATION_PREFERENCE + "].value";

                //Select for Location Status
                DomainObject.MULTI_VALUE_LIST.add(manEquAloResPreSel);
                selects.add(manEquAloResPreSel);

                DomainObject.MULTI_VALUE_LIST.add(aloResPreSel);
                selects.add(aloResPreSel);

                //Get Location Preference information
                MapList prefMaplist = getInfo(context, oidList, selects);

                StringList manEquAloResPrefList = null;
                StringList aloResPrefList = null;

                Iterator prefMapListItr = prefMaplist.iterator();
                while(prefMapListItr.hasNext())
                {
                    Map prefMap = (Map) prefMapListItr.next();

                    //Get Location Preference
                    manEquAloResPrefList = (StringList)prefMap.get(manEquAloResPreSel);
                    aloResPrefList = (StringList)prefMap.get(aloResPreSel);

                    if(manEquAloResPrefList == null)
                    {
                        manEquAloResPrefList = new StringList();
                    }

                    if(aloResPrefList == null)
                    {
                        aloResPrefList = new StringList();
                    }

                    //Combine the above two status lists into one status list
                    if(aloResPrefList != null && aloResPrefList.size() > 0)
                    {
                        for(int i=0; i < aloResPrefList.size(); i++)
                        {
                            manEquAloResPrefList.add((String)aloResPrefList.get(i));
                        }
                    }

                    StringBuffer output = new StringBuffer(" ");
                    Iterator prefListItr = manEquAloResPrefList.iterator();
                    while(prefListItr.hasNext())
                    {
                        String allocPref = (String)prefListItr.next();
                        allocPref = allocPref.replace(' ','_');
                        allocPref = "emxFramework.Range.Location_Preference."+allocPref;
                        //Multitenant
                        allocPref = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),allocPref);

                        output.append(allocPref);
                        if(isexport)
                        {
                            output.append(" \n");
                        }
                        else
                        {
                            output.append(" <br>&nbsp;<br>");
                        }
                    }

                    if(!"".equals(output.toString())) {
                        result.add(output.toString());
                    }
                }//end while
                //Added for Bug 313092
                DomainObject.MULTI_VALUE_LIST.remove(manEquAloResPreSel);
                DomainObject.MULTI_VALUE_LIST.remove(aloResPreSel);

            }//end if

        }catch (Exception Ex) {
           throw Ex;
        }

        return result;
    }//end of method getMEPLocationPreferenceHTMLOutput()

   /**
    * Gets the Locations associated with the Manufacturer Equivalent Part.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a packed HashMap with the following entries:
    * objectId - a String containing the Manufacturer Equivalent object id.
    * @return a MapList of Location information.
    * @throws Exception if the operation fails.
    * @since 10.5.
    */
   public MapList getMEPLocations (Context context,String[] args)
         throws Exception
   {
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       String objectId = (String) paramMap.get("objectId");

       StringList selectStmts = new StringList(3);
           selectStmts.addElement(SELECT_ID);
           selectStmts.addElement(SELECT_NAME);
           selectStmts.addElement(SELECT_TYPE);

       StringList validAllocRespFromTypes = new StringList(5);
           validAllocRespFromTypes.addElement(TYPE_ORGANIZATION);
           validAllocRespFromTypes.addElement(TYPE_COMPANY);
           validAllocRespFromTypes.addElement(TYPE_BUSINESS_UNIT);
           validAllocRespFromTypes.addElement(TYPE_DEPARTMENT);
           validAllocRespFromTypes.addElement(TYPE_LOCATION);

       StringList selectRelStmts = new StringList(1);
           selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

       StringBuffer sbRelPattern = new StringBuffer(RELATIONSHIP_MANUFACTURER_EQUIVALENT);
              sbRelPattern.append(',');
              sbRelPattern.append(RELATIONSHIP_ALLOCATION_RESPONSIBILITY);

       StringBuffer sbTypePattern = new StringBuffer(TYPE_LOCATION_EQUIVALENT_OBJECT);
              sbTypePattern.append(',');
              sbTypePattern.append(DomainConstants.TYPE_LOCATION);
              sbTypePattern.append(',');
              sbTypePattern.append(DomainConstants.TYPE_ORGANIZATION);

       StringBuffer sbCorpMEPLocTypePattern = new StringBuffer(DomainConstants.TYPE_LOCATION);
              sbCorpMEPLocTypePattern.append(',');
              sbCorpMEPLocTypePattern.append(DomainConstants.TYPE_ORGANIZATION);

       MapList locEquivMEPLocList = new MapList();
       MapList corpMEPLocList = new MapList();

       MapList locationList = new MapList();

       Vector vecLocId = new Vector();
             Vector vecLocName = new Vector();
             Vector vecLocEquivRelId = new Vector();
             Vector vecAllocRespRelId = new Vector();
             Vector vecIsLocEquiv = new Vector();

       try {
           setId(objectId);

          //fetching locations associated with MEP via location equib object
          //(Location Equiv MEP)
          locEquivMEPLocList = getRelatedObjects(context,
                                        sbRelPattern.toString(),              // relationship pattern
                                        sbTypePattern.toString(),              // object pattern
                                        selectStmts,                 // object selects
                                        selectRelStmts,              // relationship selects
                                        true,                        // to direction
                                        false,                       // from direction
                                        (short) 2,                   // recursion level
                                        null,                        // object where clause
                                        null);                        // relationship where clause


          //fetching locations associated with MEP directly
          //(Corporate MEP)
          corpMEPLocList = getRelatedObjects(context,
                                        RELATIONSHIP_ALLOCATION_RESPONSIBILITY,// relationship pattern
                                        sbCorpMEPLocTypePattern.toString(),              // object pattern
                                        selectStmts,                 // object selects
                                        selectRelStmts,              // relationship selects
                                        true,                        // to direction
                                        false,                       // from direction
                                        (short) 1,                   // recursion level
                                        null,                        // object where clause
                                        null);                        // relationship where clause

          Map tempMap = null;
          String strType = null;
          String strLevel = null;

          for(int i=0;i<locEquivMEPLocList.size();i++) {
             tempMap = (Map)locEquivMEPLocList.get(i);
             strType = (String)tempMap.get(SELECT_TYPE);
             strLevel = (String)tempMap.get("level");

             // The above Map List would contain Location Object at level 2
             // and Allocation Resp relationship at level 1
             if("2".equals(strLevel) ) {
                  vecLocId.addElement(tempMap.get(SELECT_ID));
                  vecLocName.addElement(tempMap.get(SELECT_NAME));
                  vecAllocRespRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
                  vecIsLocEquiv.addElement("(equiv)");
             }
             if("1".equals(strLevel) ) {
                  vecLocEquivRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
             }

                   }// end of for locEquivMEPLocations

                   for(int j=0;j<corpMEPLocList.size();j++) {
             tempMap = (Map)corpMEPLocList.get(j);
             strType = (String)tempMap.get(SELECT_TYPE);

             // The above Map List would contain Location Object at level 2
             // and Allocation Resp relationship at level 1
             if(validAllocRespFromTypes.contains(strType)) {
                  vecLocId.addElement(tempMap.get(SELECT_ID));
                  vecLocName.addElement(tempMap.get(SELECT_NAME));
                  vecAllocRespRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
                  vecLocEquivRelId.addElement(tempMap.get(SELECT_RELATIONSHIP_ID));
                  vecIsLocEquiv.addElement(" ");//dummy for corp MEP
             }
          }// end of for corpMEPLocList

                   HashMap resultMap = null;

          for(int k=0;k<vecLocId.size();k++) {
             resultMap = new HashMap();
             resultMap.put(SELECT_ID, (String)vecLocId.elementAt(k));
             resultMap.put(SELECT_NAME, (String)vecLocName.elementAt(k));
             resultMap.put(KEY_ALLOCATION_REL_ID, (String)vecAllocRespRelId.elementAt(k));
             resultMap.put(SELECT_RELATIONSHIP_ID, (String)vecLocEquivRelId.elementAt(k));
             resultMap.put(KEY_LABEL_LOC_EQUIV, (String)vecIsLocEquiv.elementAt(k));
             locationList.add(resultMap);
          }

       }catch (Exception Ex) {
            throw Ex;
       }

       return locationList;
   }//end of method getMEPLocations()

  /**
    * Gets the Enterprise Parts associated with the Manufacturer Equivalent Part.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a HashMap of the following entries:
    * objectList - a MapList of object information.
    * paramList - a Map of parameter values, SuiteDirectory, suiteKey, reportFormat, publicPortal.
    * @return a StringList of connected Enterprise Parts as HTMLOutput.
    * @throws Exception if the operation fails.
    * @since 10.5.
    */
   public StringList getMEPEnterpriseParts (Context context,String[] args)
         throws Exception
   {
       StringList result = new StringList();
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       MapList objectList = (MapList)paramMap.get("objectList");

       Map paramList = (HashMap)paramMap.get("paramList");
       String suiteDir = (String)paramList.get("SuiteDirectory");
       String suiteKey = (String)paramList.get("suiteKey");

       String reportFormat = (String)paramList.get("reportFormat");
       String publicPortal = (String)paramList.get("publicPortal");

       boolean isexport = false;
       String export = (String)paramList.get("exportFormat");
       if ( export != null )
       {
           isexport = true;
       }

       String linkFile = (publicPortal != null && publicPortal.equalsIgnoreCase("true"))?"emxNavigator.jsp":"emxTree.jsp";

       String LocContextEntId = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].from.id";
       DomainObject.MULTI_VALUE_LIST.add(LocContextEntId);
       String LocContextEntName = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].from.name";
       DomainObject.MULTI_VALUE_LIST.add(LocContextEntName);
       String LocContextEntRev = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.to[" + RELATIONSHIP_LOCATION_EQUIVALENT + "].from.revision";
       DomainObject.MULTI_VALUE_LIST.add(LocContextEntRev);

       String CorpContextEntId = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.id";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextEntId);
       String CorpContextEntType = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.type";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextEntType);
       String CorpContextEntName = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.name";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextEntName);
       String CorpContextEntRev = "to[" + RELATIONSHIP_MANUFACTURER_EQUIVALENT + "].from.revision";
       DomainObject.MULTI_VALUE_LIST.add(CorpContextEntRev);

       StringList selectStmts = new StringList(11);
       selectStmts.addElement(SELECT_ID);
       selectStmts.addElement(SELECT_TYPE);
       selectStmts.addElement(SELECT_NAME);
       selectStmts.addElement(SELECT_REVISION);
       selectStmts.addElement(LocContextEntId);
       selectStmts.addElement(LocContextEntName);
       selectStmts.addElement(LocContextEntRev);
       selectStmts.addElement(CorpContextEntId);
       selectStmts.addElement(CorpContextEntType);
       selectStmts.addElement(CorpContextEntName);
       selectStmts.addElement(CorpContextEntRev);


       StringList relSelectStmts = new StringList(2);
       relSelectStmts.addElement(SELECT_RELATIONSHIP_ID);
       relSelectStmts.addElement(SELECT_RELATIONSHIP_NAME);

       try
       {
           Iterator itr = objectList.iterator();
           int i=0;
           int count = objectList.size();
           String[] arrobjectId = new String[count];
           while (itr.hasNext())
           {
               Map m = (Map) itr.next();
               arrobjectId[i] = (String)m.get("id");
               i++;
           }
           MapList listEquiv = DomainObject.getInfo(context, arrobjectId, selectStmts);
           StringList entIdList = null;
           StringList entTypeList = null;
           StringList entNameList = null;
           StringList entRevList = null;
           String entId = "";
           String entName = "";
           String entRev = "";
           boolean hasEquiv = false;
           for(i=0; i < listEquiv.size(); i++)
           {
               StringBuffer output = new StringBuffer(512);
               hasEquiv = false;
               Map map = (Map)listEquiv.get(i);
               entIdList = (StringList) map.get(LocContextEntId);
               entNameList = (StringList) map.get(LocContextEntName);
               entRevList = (StringList) map.get(LocContextEntRev);
               if (entIdList != null && entIdList.size() > 0)
               {
                   for (int j=0; j < entIdList.size(); j++)
                   {
                       hasEquiv = true;
                       entId = (String)entIdList.get(j);
                       entName = (String)entNameList.get(j);
                       entRev = (String)entRevList.get(j);
                       if(isexport)
                       {
                            output.append(entName+" " +entRev + " \n");
                       }
                       //do not show hyperlinks if it is a printer friendly or excel export page
                       //length will be >0 when format is HTML, ExcelHTML, CSV or TXT
                       else if(null!=reportFormat && !reportFormat.equals("null") && (reportFormat.length() > 0))
                       {
                           output.append(entName+" " +entRev+"<br>");
                       }
                       else
                       {
                           output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"+linkFile+"?emxSuiteDirectory="+suiteDir+"&suiteKey="+suiteKey+"&objectId="+ entId +"', '', '', 'false', 'popup', '')\">"+entName+" " +entRev+"</a> <br>");
                       }
                   }
               }
               entIdList = (StringList) map.get(CorpContextEntId);
               entTypeList = (StringList) map.get(CorpContextEntType);
               entNameList = (StringList) map.get(CorpContextEntName);
               entRevList = (StringList) map.get(CorpContextEntRev);
               if (entIdList != null && entIdList.size() > 0 )
               {
                   for (int j=0; j < entIdList.size(); j++)
                   {
                       //skip the location equivalent objects - these are handled in the above loop
                       if (((String)entTypeList.get(j)).equals(TYPE_LOCATION_EQUIVALENT_OBJECT))
                       {
                           continue;
                       }
                       hasEquiv = true;
                       entId = (String)entIdList.get(j);
                       entName = (String)entNameList.get(j);
                       entRev = (String)entRevList.get(j);
                       if(isexport)
                       {
                            output.append(entName+" " +entRev+ " \n");
                       }
                       //do not show hyperlinks if it is a printer friendly or excel export page
                       //length will be >0 when format is HTML, ExcelHTML, CSV or TXT
                       else if(null!=reportFormat && !reportFormat.equals("null") && (reportFormat.length() > 0))
                       {
                           output.append(entName+" " +entRev+"<br>");
                       }
                       else
                       {
                           output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"+linkFile+"?emxSuiteDirectory="+suiteDir+"&suiteKey="+suiteKey+"&objectId="+ entId +"', '', '', 'false', 'popup', '')\">"+entName+" " +entRev+"</a> <br>");
                       }
                   }
               }
               if(!"".equals(output.toString())) {
                  result.add(output.toString());
               }

               if(!hasEquiv) {
                 result.add("&nbsp;");
               }

           }//end of while

       }catch (FrameworkException Ex) {
            throw Ex;
       }
       //Added for Bug 313092
       DomainObject.MULTI_VALUE_LIST.remove(LocContextEntId);
       DomainObject.MULTI_VALUE_LIST.remove(LocContextEntName);
       DomainObject.MULTI_VALUE_LIST.remove(LocContextEntRev);

       DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntId);
       DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntType);
       DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntName);
       DomainObject.MULTI_VALUE_LIST.remove(CorpContextEntRev);
       return result;
   }//end of method getMEPEnterpriseParts ()

     /**
     * Copy over all substitutes from a part. All connections
     *      found will be copied to the other Part. It is assumed the next revision is
     *      meant to be the target.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since X3
     * @trigger TypePartReviseAction
     */
    public void copySubstitutes(Context context, String[] args)
        throws Exception
    {
      DebugUtil.debug("TypePartReviseAction:copySubstitutes");
              MapList _substitutePartList                = null;
              MapList _nextEBOMList                        = null;
              MapList _finalsubstitutePartList            = new MapList();

              String  RELATIONSHIP_EBOM_SUBSTITUTE        =
                    PropertyUtil.getSchemaProperty(context,"relationship_EBOMSubstitute");
              String srealAttrFNName                    = PropertyUtil.getSchemaProperty(context,"attribute_FindNumber");
              String srealAttrRefDesName                = PropertyUtil.getSchemaProperty(context,"attribute_ReferenceDesignator");
              String sFindNumberUnique                    = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.FindNumberUnique");
              String sEBOMUniquenessOperator            = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.EBOMUniquenessOperator");
              String sReferenceDesignatorUnique            = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReferenceDesignatorUnique");

      try
      {
                DomainObject boPrev                    = new DomainObject(getId());
                StringList relsel                    = new StringList();
                DomainObject boNext                    = new DomainObject(getNextRevision(context));

                relsel.add(DomainRelationship.SELECT_ID);
                relsel.add("to.id");
                relsel.add("attribute[" + srealAttrFNName + "]");
                relsel.add("attribute[" + srealAttrRefDesName + "]");

                StringList strObjList                = new StringList(6);
                strObjList.add(DomainConstants.SELECT_ID);
                strObjList.add(DomainConstants.SELECT_TYPE);
                strObjList.add(DomainConstants.SELECT_NAME);
                strObjList.add(DomainConstants.SELECT_REVISION);

                _substitutePartList = boPrev.getRelatedObjects(context,
                                                DomainConstants.RELATIONSHIP_EBOM,
                                                TYPE_PART,
                                                strObjList,
                                                relsel,
                                                false,
                                                true,
                                                (short)1,
                                                "",
                                                "");
                Iterator itr = _substitutePartList.iterator();
                StringList slEBOMSubs                = null;

                    Map tempmap1 = null;
                    while (itr.hasNext())
                    {
                        Map relmap                    = (Map) itr.next();
                        String sEBOMID                = (String)relmap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                        String sFN                    = (String)relmap.get("attribute["+srealAttrFNName+"]");
                        String sRefDes                = (String)relmap.get("attribute["+srealAttrRefDesName+"]");

                        String strCommand            = "print connection $1 select $2 dump $3";
                        String strMessage            = MqlUtil.mqlCommand(context,strCommand,sEBOMID,"frommid["+RELATIONSHIP_EBOM_SUBSTITUTE+"].id","|");

                        slEBOMSubs                    = FrameworkUtil.split(strMessage,"|");

                        Iterator ebomsubsItr        = slEBOMSubs.iterator();
                        String sEBOMSubstituteRelid    = "";

                        while(ebomsubsItr.hasNext())
                        {
                            tempmap1                = new HashMap();
                            tempmap1.put(DomainConstants.SELECT_RELATIONSHIP_ID,sEBOMID);
                            tempmap1.put("attribute["+srealAttrFNName+"]",sFN);
                            tempmap1.put("attribute["+srealAttrRefDesName+"]",sRefDes);

                            //EBOM Substitute Id
                            sEBOMSubstituteRelid            = (String) ebomsubsItr.next();
                            DomainRelationship domRel        = new DomainRelationship(sEBOMSubstituteRelid);

                            //Getting relationship attribute on EBOM Substitute
                            Map attEbomSubstitute            = domRel.getAttributeMap(context,sEBOMSubstituteRelid);
                            tempmap1.put("Substitute Attribute",attEbomSubstitute);
                            SelectList resultSelects        = new SelectList(1);
                            String[] RelIdArray                = new String[1];
                            RelIdArray[0]                    = sEBOMSubstituteRelid;
                            resultSelects.add("to."+DomainObject.SELECT_ID);
                            MapList resultList                = DomainRelationship.getInfo(context,
                                                                       RelIdArray,
                                                                       resultSelects);

                            Iterator itr1                    = resultList.iterator();
                            Map map = (Map) itr1.next();

                            String strsubsPartId            = (String)map.get("to.id");
                            tempmap1.put("Substitute Part",strsubsPartId);
                           _finalsubstitutePartList.add((Map)tempmap1);

                        }
                    }

                    Iterator finalsubstitutePartListitr        =_finalsubstitutePartList.iterator();
					//Modified for the fix 363134
					ContextUtil.pushContext(context);
                    while(finalsubstitutePartListitr.hasNext())
                    {
                        Map relmap1          = (Map) finalsubstitutePartListitr.next();
                        String sfindnumber   = (String)relmap1.get("attribute[" + srealAttrFNName + "]");
                        String srdesignator  = (String)relmap1.get("attribute[" + srealAttrRefDesName + "]");
                        String SubId         = (String)relmap1.get("Substitute Part");
                        Map SubRelAttributeMap = (Map)relmap1.get("Substitute Attribute");

                        StringBuffer sbRelWhereCond = new StringBuffer();
                        /* if user select All, then donot add where condition */
                        //Building where condition based on property settings
                        if("or".equals(sEBOMUniquenessOperator))
                        {
                            if("true".equals(sFindNumberUnique))
                            {
                                sbRelWhereCond.append("attribute["+srealAttrFNName+"]");
                                sbRelWhereCond.append(" == '");
                                sbRelWhereCond.append(sfindnumber);
                                sbRelWhereCond.append('\'');
                             }
                             else if("true".equals(sReferenceDesignatorUnique))
                             {
                                sbRelWhereCond.append("attribute["+srealAttrFNName+"]");
                                sbRelWhereCond.append(" == '");
                                sbRelWhereCond.append(sfindnumber);
                                sbRelWhereCond.append('\'');
                            }
                        }
                        else if("and".equals(sEBOMUniquenessOperator))
                        {
                            sbRelWhereCond.append("attribute["+srealAttrFNName+"]");
                            sbRelWhereCond.append(" == '");
                            sbRelWhereCond.append(sfindnumber);
                            sbRelWhereCond.append('\'');
                            sbRelWhereCond.append("");
                            sbRelWhereCond.append("&& ");
                            sbRelWhereCond.append("attribute["+srealAttrRefDesName+"]");
                            sbRelWhereCond.append(" == '");
                            sbRelWhereCond.append(srdesignator);
                            sbRelWhereCond.append('\'');

                        }

                        _nextEBOMList = boNext.getRelatedObjects(context,
                                                    DomainConstants.RELATIONSHIP_EBOM,
                                                    TYPE_PART,
                                                    strObjList,
                                                    relsel,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    "",
                                                    sbRelWhereCond.toString());

                        Iterator nextEBOMListitr        =_nextEBOMList.iterator();
                        Map relmap2                        = (Map) nextEBOMListitr.next();
                        String snextEBOMID                = (String)relmap2.get(DomainConstants.SELECT_RELATIONSHIP_ID);

                        String strnextMessage            = MqlUtil.mqlCommand(context,
                                "add connection $1 fromrel $2 to $3 select $4 dump",
                                RELATIONSHIP_EBOM_SUBSTITUTE,
                                snextEBOMID,SubId, SELECT_ID);
   
                        strnextMessage = strnextMessage.replaceAll("ID.*", "").trim();
                        DomainRelationship domNewEBOMSubRel = new DomainRelationship(strnextMessage);
  
                        domNewEBOMSubRel.setAttributeValues(context, SubRelAttributeMap);
            }
        }
        catch (Exception e)
        {
            DebugUtil.debug("Exception=", e.toString());
            throw (e);
        }
        finally
        {
			ContextUtil.popContext(context);
        }
}

/**
 * Checks whether the Manufacturer Equivaletn Part has more than one row to be displayed.
 * @param inMapList holds the MapList for the corresponding MEP.
 * @param key holds the String to be checked for.
 * @return true if the MEP has 2 or more rows, otherwise return false.
 * @throws Exception if the operation fails.
 * @since 10.5.
 */
public boolean isMultipleRows(MapList inMapList, String key)
       throws Exception
{
    boolean flag = false;
    String oId = "";
    int ct = 0;
    Iterator m = inMapList.iterator();
    while (m.hasNext()) {
        Map inMap = (Map) m.next();
        oId = (String)inMap.get(key);
        if (oId != null && !("").equals(oId))
        {
            ct = ct + 1;
            if (ct >= 2)
            {
                flag = true;
                break;
            }
        }
    }
    return flag;
}

     /**
     * Reset Owner on demotion to Release state.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the parent object id.
     * @return void
     * @throws Exception if the operation fails
     * @since EC 10.5
     */
    public void resetOwner(Context context, String[] args)
        throws Exception
    {
        try
        {
            /*Arguments are not Packed*/
            String objectId = args[0];
            setObjectId(objectId);

            String promotedBy = null;
            StringList historyData = getHistory(context);
            for(int i=historyData.size()-1;i>=0;i--){
                String historyRecord = ((String)historyData.elementAt(i)).trim();
                // get owner at Approved state instead of Release, since User Agent promoted to Release
                if(historyRecord.startsWith("promote") && (historyRecord.endsWith(STATE_PART_APPROVED)||historyRecord.endsWith(STATE_DRAWINGPRINT_APPROVED)||historyRecord.endsWith(STATE_CADDRAWING_APPROVED)||historyRecord.endsWith(STATE_CADMODEL_APPROVED) ||historyRecord.endsWith(STATE_PART_SPECIFICATION_APPROVED))){
                    promotedBy = historyRecord.substring(historyRecord.indexOf("user:")+6, historyRecord.indexOf("time:")-2).trim();
                    break;
                }
            }

            if(promotedBy != null){
                setOwner(context, promotedBy);
            }

        }catch (Exception ex)
        {
            throw ex;
        }
}

 /**
 * Connects the ECO to a Part.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap containing the following entries:
 * requestMap - a HashMap of the request.
 * paramMap - a HashMap of containing String values for, "objectId", "Old value", "New OID".
 * @return Object - boolean true if the operation is successful
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
      public Object setECO(Context context, String[] args)
      throws Exception
      {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          HashMap paramMap = (HashMap) programMap.get("paramMap");

          String objectId = (String) paramMap.get("objectId");
          String oldECOName = (String) paramMap.get("Old value");
          String newEcoId = (String) paramMap.get("New OID");

          //364525 - Starts
          boolean blnFlag = false;
          Access accessMask = new Access();
          BusinessObjectList ecoObjects = new BusinessObjectList();
          accessMask.setAllAccess(true);
          accessMask.setUser(context.getUser());
          //364525 - Ends
          String policyClass = "";

          if(newEcoId == null || "null".equals(newEcoId))
          {
              newEcoId = "";
          }

          if (!"".equals(newEcoId))
          {
              ECO eco = new ECO(newEcoId);
              policyClass = EngineeringUtil.getPolicyClassification(context, eco.getPolicy(context).getName());
              //364525 - Starts
              ecoObjects.add(new DomainObject(newEcoId));
              //364525 - Ends
          }

          if (policyClass == null || policyClass.equals("null"))
          {
      		policyClass = "";
          }

          String strECORelationship = RELATIONSHIP_AFFECTED_ITEM;

          setId(objectId);
          java.util.List ecoList = new MapList();
          if (oldECOName == null || "null".equals(oldECOName)) {
              oldECOName = "";
          }

          //Get the existing ECO connected to the part
          if (!"".equals(oldECOName) && (newEcoId != null && !"".equals(newEcoId)))
          {
            StringList ObjectSelectsList = new StringList(DomainConstants.SELECT_ID);
            StringList RelSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            String strECOType = DomainConstants.TYPE_ECO;

            StringTokenizer ecos = new StringTokenizer(oldECOName,",");
            while(ecos.hasMoreTokens()){
            String eco = ecos.nextToken();
            StringBuffer sbWhereCondition = new StringBuffer(25);
            sbWhereCondition = sbWhereCondition.append("name==\"").append(eco).append("\"");
            String strWhereCondition = sbWhereCondition.toString();
            Pattern relPattern = new Pattern(RELATIONSHIP_AFFECTED_ITEM);

            ecoList = getRelatedObjects(context,
                                        relPattern.getPattern(),
                                        strECOType,
                                        ObjectSelectsList,
                                        RelSelectsList,
                                        true,
                                        true,
                                        (short) 1,
                                        strWhereCondition,
                                        DomainConstants.EMPTY_STRING);

            //364525 - Starts
            if (ecoList != null && !ecoList.isEmpty()) {
                int iSize = ecoList.size();
                for (int index=0; index < iSize; index++) {
					String ecoId = (String) ((Hashtable) ecoList.get(index)).get(
                                    DomainConstants.SELECT_ID);
                    ecoObjects.add(new DomainObject(ecoId));
                }
            }
            ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
            BusinessObject.grantAccessRights(context,
                                            ecoObjects,
                                            accessMask);
            ContextUtil.popContext(context);
            blnFlag = true;
            //364525 - Ends
            if (ecoList != null)
              {
                  String strRelId = (String) ((Hashtable) ecoList.get(0)).get(
                  DomainConstants.SELECT_RELATIONSHIP_ID);
                    //Disconnecting the existing relationship
                    DomainRelationship.disconnect(context, strRelId);
            }
          }
		 }

          if (newEcoId == null || "null".equals(newEcoId)) {
              newEcoId = "";
          }
            //364525 - Starts
            if (!blnFlag && !ecoObjects.isEmpty()) {
                ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
                BusinessObject.grantAccessRights(context,
                                                    ecoObjects,
                                                    accessMask);
                ContextUtil.popContext(context);
                blnFlag = true;
            }
            //364525 - Ends
          //Connect the part to ECO
            if (!"".equals(newEcoId))
              {
                 setId(newEcoId);
                 DomainObject domainObjectToType = newInstance(context, objectId);
                 // Changes done for IR-064742
                 DomainRelationship rl1 = DomainRelationship.connect(context,this,strECORelationship,domainObjectToType);
				 StringList revSelects = new StringList();
				 revSelects.add("previous.current");
				 revSelects.add("previous.policy");
				 revSelects.add("previous.id");
				 Map mRevs = domainObjectToType.getInfo(context, revSelects);
				 String revState = (String)mRevs.get("previous.current");
				 String revId = (String)mRevs.get("previous.id");
				 String revPolicy = (String)mRevs.get("previous.policy");
				 // if previous revision is released, connect it as "for revise"
				 // and connect this revision as "for release"
				 if (revId!=null) {
 					DomainObject doRev = new DomainObject(revId);
					String revRelease = PropertyUtil.getSchemaProperty(context,"policy",
																		revPolicy,
																		"state_Release");
					if (revState.equals(revRelease)){
						StringList affectedItems = new StringList();
	                    affectedItems = getInfoList(context, "relationship["+RELATIONSHIP_AFFECTED_ITEM+"].to.id");
						String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
						if (!affectedItems.contains(revId)) {
							DomainRelationship.connect(context, this, RELATIONSHIP_AFFECTED_ITEM, doRev);
						}
						rl1.setAttributeValue(context, strAttrAffectedItemCategory, "Indirect");
					}
                 }
              }
            //364525 - Starts
            if (blnFlag) {
                AccessList aclList = new AccessList(1);
                aclList.add(accessMask);
                ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
                BusinessObject.revokeAccessRights(context, ecoObjects, aclList);
                ContextUtil.popContext(context);
            }
            //364525 - Ends
          return Boolean.TRUE;
      }

 /**
 * Returns whether the Part is a Manufacturer Equivalent or Enterprise Part.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap of the following entries:
 * paramMap - a HashMap containing String values for "objectId", "languageStr".
 *          This Map contains the arguments passed to the jsp which called this method.
 * @return Object - Part Policy Classification in a StringList.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public Object getMepPartOrigin(Context context, String[] args)
throws Exception
{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strPartId = (String) paramMap.get("objectId");
        String languageStr = (String) paramMap.get("languageStr");

        setId(strPartId);
        //Get the part policy classification
        String strPartClassification = getInfo(context,"policy.property[PolicyClassification].value");
        String strManufacturerEquivalent=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.MfgEquivalent",languageStr);
        String strEnterprise=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Enterprise",languageStr);
        StringList PartOriginList = new StringList(1);

        if ("Equivalent".equals(strPartClassification))
        {
            PartOriginList.addElement(strManufacturerEquivalent);
        } else {
            PartOriginList.addElement(strEnterprise);
        }

        return PartOriginList;
}

 /**
 * Updates the Manufacturer fpr a Part.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap of the following entries:
 * paramMap - a HashMap containing String values for "objectId", "Old value" and "New OID".
 * @return Object - boolean true if the operation is successful
 * @throws Exception if operation fails
 * @since EngineeringCentral 10-6 - Copyright (c) 2004, MatrixOne, Inc.
 */
 public Object updateManufacturer(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     HashMap paramMap = (HashMap) programMap.get("paramMap");
     String strPartId = (String) paramMap.get("objectId");

     Part part = new Part(strPartId);
     String strManufacturerId = (String) paramMap.get("New OID");

     //   Fix for 368060
     if(strManufacturerId==null || "".equals(strManufacturerId)){
         strManufacturerId = (String)paramMap.get("New Value");
     }
     // Fix for 368060 Ends

     //Connect manufacturer to a part
     part.connectManufacturerResponsibility(context, strManufacturerId);
     return Boolean.TRUE;
  }

 /**
 * Informs whether the current part in context is Manufacturer Equivalent Part or not.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap with the following entries:
 * objectId - a String containing the Part id.
 * @return Object - boolean true if the part is Manufacturer Equivalent Part.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
 public Boolean isMepPart(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     String strPartId = (String) programMap.get("objectId");

     //return true if the part is a MEP part
     setId(strPartId);
     return Boolean.valueOf(isMEP(context));
 }

 /**
 * Informs whether the current part in context is Enterprise part or not.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap with the following entries:
 * objectId - a String containing the Part id.
 * @return Object - boolean true if the part is Enterprise part
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
 public Boolean isEnterprisePart(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     String strPartId = (String) programMap.get("objectId");

     //return true if the part is not a MEP part
     setId(strPartId);
     return Boolean.valueOf(!isMEP(context));
 }

 /**
 * Checks that the Part is not a Manufacturer Equivalent Part and not an Application Part..
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap with the following entries:
 * objectId - a String containing the Part id.
 * @return Object - boolean true if the part is neither the MEP Part or Application Part
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
 public Boolean checkPartType(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     String strPartId = (String) programMap.get("objectId");

     setId(strPartId);
     // check the part is MEP part or not.
     boolean isMepPart = isMEP(context);
     boolean isAppPart = false;

     // check the part is Application part or not.
     String strObjType = getInfo(context,SELECT_TYPE);
     if(strObjType.equals(TYPE_APPLICATION_PART) ) {
       isAppPart = true;
     }

     return Boolean.valueOf( !isMepPart && !isAppPart );
 }

 /**
 * Checks whether change management is allowed for Maufacturer Equivalent Parts.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap with the following entries:
 * objectId - a String containing the Part id.
 * languageStr - s String containing the language setting.
 * @return Object - boolean true we are to allow change management for the Part.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
 public Boolean checkTypeChangeMgmt(Context context, String[] args)
 throws Exception
 {
	 	//This check is needed for TBE instllation. This is to hide form fields if only TBE is installed
	 	if(!EngineeringUtil.isENGInstalled(context, args)) {
	 		return Boolean.FALSE;
	 	} else {
			return Boolean.TRUE;
		}

 }

 /**
 * Returns true if the mode of the web form display is view.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap with the following entries:
 * mode - a String containing the mode.
 * @return Object - boolean true if the mode is view.
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
 * Displays whether the part is connected to active ECR or ECO.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap with the following entries:
 * paramMap - a HashMap containing the following Strings, objectId, languageStr.
 * @return Object - String object "Yes" if connected otherwise String object "No"
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
        // return yes if part is connected to active ecr or eco otherwise no.
        boolean hasActiveECRECO = EngineeringUtil.hasActiveECRECO(context, strSpecId);
        if(hasActiveECRECO){
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
    * Gets the Alternate Parts associated with the EC Part and Development Part.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a HashMap with the following entries:
    * objectId - a String containing the Part id.
    * @return MapList of Alternate Part object id, type, name, revision, description, state, policy, relationship id.
    * @throws Exception If the operation fails.
    * @since 10.6.
    */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getAlternateParts(Context context, String[] args) throws Exception
  {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      MapList alternatePartList = null;
      Part part = (Part)DomainObject.newInstance(context,DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
      String objectId = (String)programMap.get("objectId");
      part.setId(objectId);
      StringList selectRelStmts = new StringList(1);
      selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

      StringList selectStmts = new StringList(7);
      selectStmts.addElement(DomainConstants.SELECT_ID);
      selectStmts.addElement(DomainConstants.SELECT_TYPE);
      selectStmts.addElement(DomainConstants.SELECT_NAME);
      selectStmts.addElement(DomainConstants.SELECT_REVISION);
      selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
      selectStmts.addElement(DomainConstants.SELECT_CURRENT);
      selectStmts.addElement(DomainConstants.SELECT_POLICY);
      alternatePartList  = part.getAlternateParts(context, selectStmts, selectRelStmts, false);

      return alternatePartList;
  }

/**
 * Gets the edit ECO hyperlink for the given ECOs.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds a HashMap containing the following entries:
 * objectList - a MapList of ECO information.
 * paramList - a Map of parameter values containing SuiteDirectory, languageStr, objectId.
 * @return StringList of Edit ECO urls.
 * @throws Exception if the operation fails.
 * @since AEF 10.0.0.0.
 */
public StringList getEcoDetails(Context context,
                             String[] args)
    throws Exception
{
try
{
    StringList urlList = new StringList();
    String sECOId =null;
    String sRelName = null;
    String sRelId = null;
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList ecoMap = (MapList)programMap.get("objectList");

    Map paramMap= (HashMap)programMap.get("paramList");
    String partId = (String)paramMap.get("objectId");
    Iterator itr = ecoMap.iterator();

     while (itr.hasNext())
      {
          Map m = (Map) itr.next();
          sECOId = (String)m.get("id");
          sRelName = (String)m.get("relationship");
          sRelId = (String)m.get("id[connection]");
          String strReptFormat = (String) paramMap.get("reportFormat");
          if (strReptFormat == null || "null".equals(strReptFormat)) {
              String modifyURL =  "../engineeringcentral/emxpartECOAttributesDialogFS.jsp?objectId=" + partId + "&selectedECOId=" + sECOId + "&Mode=Modify" + "&RelationshipType=" + sRelName + "&sRelId=" + sRelId;
              modifyURL = "<a href=\"JavaScript:emxTableColumnLinkClick('"+modifyURL+"', '700', '600', 'true', 'popup', '')\"><img src=\"../common/images/iconActionEdit.gif\" border=\"0\"/></a>";
              urlList.addElement(modifyURL);
            }
            else {
                String modifyURL="<img src=\"../common/images/iconActionEdit.gif\" border=\"0\"/>";
                urlList.addElement(modifyURL);
            }
       }
     return urlList;
}
  catch (Exception e)
  {
            throw new FrameworkException(e);
  }

}

 /**
     * This method decide whether to enable the checkbox for Removing the Eco from the Summary Page.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments as it is getting the params list,objects list from the Config UI table
     * @returns Vector of "true/false" values for each row
     * @throws Exception if the operation fails
     * @since Common 10.6
     */
    public Vector showCheckBoxInEco(Context context, String[] args)
    throws Exception
    {
  try
    {
        Vector columnVals   = null;
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        MapList objectList     = (MapList)programMap.get("objectList");
        HashMap paramMap    = (HashMap)programMap.get("paramList");
        String objectId     = (String) paramMap.get("objectId");

        Iterator itr = objectList.iterator();
        int listSize = objectList.size();
        columnVals   = new Vector(listSize);

       Part part = (Part)DomainObject.newInstance(context,DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
       part.setId(objectId);
     if(objectList != null && (listSize = objectList.size()) > 0 )
   {
    while (itr.hasNext())
    {
          Map m = (Map) itr.next();
          String sECOId = (String)m.get("id");
          DomainObject ECOObj = DomainObject.newInstance(context,sECOId);
       if (FrameworkUtil.hasAccess(context, part, "toDisconnect") &&
         FrameworkUtil.hasAccess(context, ECOObj, "fromDisconnect"))
        {

   columnVals.add("true");
        } else {

         columnVals.add("false");
        }
  }  // while
   }  // if
        return columnVals;
  }
   catch (Exception ex)
   {
      throw ex;
   }
}

   /**
   * Gets the Disposition Codes edit url for each ECR associated with the Part.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds a HashMap containing the following entries:
   * objectList - a MapList of object information.
   * paramList - a Map of parameter values containing SuiteDirectory, suiteKey, objectId, reportFormat.
   * @return MapList of edit ECR urls.
   * @throws FrameworkException if operation fails
   * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
   */
    public StringList getDispCodesURL(Context context, String []args)
                             throws FrameworkException
    {
        try
        {
           HashMap paramMap = (HashMap)JPO.unpackArgs(args);
           MapList objectList = (MapList)paramMap.get("objectList");

           Map paramList = (HashMap)paramMap.get("paramList");
           String objectId = (String)paramList.get("objectId");
           String reportFormat = (String)paramList.get("reportFormat");

           Iterator itr = objectList.iterator();
           StringList urlList = new StringList();
           while (itr.hasNext())
           {
              Map m = (Map) itr.next();
              String sECRId = (String)m.get("id");
              String sRelName = (String)m.get("relationship");
              String sRelId = (String)m.get("id[connection]");
              String sECRName = (String)m.get("name");
              double rndNum = java.lang.Math.random();
              String sECRRev= (String)m.get("revision");
              String tempStr = sRelName+"|"+sRelId;

              String modifyURL =  "../engineeringcentral/emxengchgECRRelAttributesFS.jsp?busId=" + sECRId + "&objectId=" + sECRId +"&changeType=" + sRelName + "&Type=ECR" + "&Name=" + sECRName + "&Rev=" + sECRRev + "&sChildToken=" + sRelId+"&selectedType=" + objectId + "&rSelectComponent=" + tempStr + "&hChildToken=" + sRelId + "&rndNum=" + rndNum;

              if(null!=reportFormat && !reportFormat.equals("null") && (reportFormat.length() > 0))
              {
                   modifyURL = "<img src=\"../common/images/iconActionEdit.gif\" border=\"0\"/>";
              }
              else
              {
                   modifyURL = "<a href=\"JavaScript:emxTableColumnLinkClick('"+modifyURL+"', '', '', 'false', 'popup', '')\"><img src=\"../common/images/iconActionEdit.gif\" border=\"0\"/></a>";
              }

              urlList.addElement(modifyURL);
           }
           return urlList;
        }
        catch (Exception e)
        {
            throw new FrameworkException(e);
        }
    }

   /**
    * Gets the Revisions of EC Objects like Parts and Specs.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds HashMap containing the following entries:
    * objectId - a String of the Part id.
    * @return a MapList list of Revision object ids.
    * @throws Exception if the operation fails.
    * @since 10.6.
    */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getObjectRevisions(Context context,String[] args) throws Exception
  {
                 MapList revisionList =null;
  try
  {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String)programMap.get("objectId");
        DomainObject bo = DomainObject.newInstance(context);
        bo.setId(objectId);
        SelectList busSelects = bo.getObjectSelectList(6);
        busSelects.add(DomainConstants.SELECT_ID);

        revisionList = bo.getRevisions(context, busSelects, false);
 } catch(Exception ex)
   {
       throw ex;
     }
      return revisionList;
  }

   /**
    * Gets the Spare Parts associated with the Part.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds HashMap containing the following entries:
    * objectId - a String of the Part id.
    * @return a MapList of Spare Part object ids and relationship ids.
    * @throws Exception if the operation fails.
    * @since 10.6.
    */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getSpareParts(Context context,String[] args) throws Exception
  {
      MapList sparePartList =null;
  try
  {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String)programMap.get("objectId");
        Part part = new Part(objectId);
        part.setId(objectId);
        StringList selectStmts = new StringList(1);
        selectStmts.addElement(DomainConstants.SELECT_ID);

        StringList selectRelStmts = new StringList(1);
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

        sparePartList = part.getSpareParts(context, selectStmts, selectRelStmts,false);
   }
   catch(Exception ex)
   {
     throw ex;
     }
      return sparePartList;
  }

    /**
    * Gets all Substitute Parts for the context Part.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds HashMap containing the following entries:
    * objectList - a MapList of object information.
    * paramList - a Map of parameter values
    * objectId - a String of the Part id.
    * @return a MapList of connected Substituted Parts.
    * @throws Exception if the operation fails.
    * @since X3.
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getSubstitutePart(Context context, String[] args)
        throws Exception
   {
    try
   {
		MapList _substitutePartList = null;
        MapList _finalsubstitutePartList        = new MapList();
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       String partId= (String) paramMap.get("objectId");

        DomainObject domObj                     = new DomainObject(partId);
        StringList slEBOMSubs                   = null;
        StringList relsel                       = new StringList();

        String srealAttrFNName                  = PropertyUtil.getSchemaProperty(context,"attribute_FindNumber");
        String srealAttrQtyName                 = PropertyUtil.getSchemaProperty(context,"attribute_Quantity");
        String RELATIONSHIP_EBOM_SUBSTITUTE     = PropertyUtil.getSchemaProperty(context,"relationship_EBOMSubstitute");


        relsel.add(DomainRelationship.SELECT_ID);
        relsel.add("attribute[" + srealAttrFNName + "]");
        relsel.add("attribute[" + srealAttrQtyName + "]");

        StringList strObjList                   = new StringList(6);
        strObjList.add(DomainConstants.SELECT_ID);
        strObjList.add(DomainConstants.SELECT_TYPE);
        strObjList.add(DomainConstants.SELECT_NAME);
        strObjList.add(DomainConstants.SELECT_REVISION);

        _substitutePartList = domObj.getRelatedObjects(context,
                                        DomainConstants.RELATIONSHIP_EBOM,
                                        TYPE_PART,
                                        strObjList,
                                        relsel,
                                        false,
                                        true,
                                        (short)1,
                                        "",
                                        "");
        Iterator itr1 = _substitutePartList.iterator();
        Map tempmap1 = null;
        while (itr1.hasNext())
                {
            Map relmap                  = (Map) itr1.next();
            String sEBOMID              = (String)relmap.get("id[connection]");

            String strCommand           = "print connection $1 select $2 dump $3";
            String strMessage           = MqlUtil.mqlCommand(context,strCommand,sEBOMID,"frommid["+RELATIONSHIP_EBOM_SUBSTITUTE+"].id","|");
            slEBOMSubs                  = FrameworkUtil.split(strMessage,"|");
            Iterator ebomsubsItr        =slEBOMSubs.iterator();

            String sEBOMSubstituteRelid ="";

            while(ebomsubsItr.hasNext())
                        {
                tempmap1                        = new HashMap();
                tempmap1.put("EBOM ID",sEBOMID);
                //EBOM Substitute Id
                sEBOMSubstituteRelid            = (String) ebomsubsItr.next();
                //Putting EBOM Substitute Rel Id as id[connection]
                tempmap1.put("id[connection]",sEBOMSubstituteRelid);
                DomainRelationship domRel       = new DomainRelationship(sEBOMSubstituteRelid);
                //Getting relationship attribute on EBOM Substitute
                Map attEbomSubstitute           = domRel.getAttributeMap(context,sEBOMSubstituteRelid);
                String strFindNumber            = (String) attEbomSubstitute.get(srealAttrFNName);
                String strQantity = (String) attEbomSubstitute.get(srealAttrQtyName);
                SelectList resultSelects        = new SelectList(1);
                String[] RelIdArray             = new String[1];
                RelIdArray[0]                   = sEBOMSubstituteRelid;
                resultSelects.add("to."+DomainObject.SELECT_ID);
                MapList resultList              = DomainRelationship.getInfo(context,
                                                           RelIdArray,
                                                           resultSelects);
                Iterator itr                    = resultList.iterator();
                Map map = (Map) itr.next();
                String strsubsPartId            = (String)map.get("to.id");
                tempmap1.put("id",strsubsPartId);
                tempmap1.put("attribute[" + srealAttrFNName + "]",strFindNumber);
                tempmap1.put("attribute[" + srealAttrQtyName + "]",strQantity);
                _finalsubstitutePartList.add((Map)tempmap1);

                        }
                    }
         ContextUtil.startTransaction(context, false);
         ContextUtil.commitTransaction(context);
         return _finalsubstitutePartList;
     }// end of try
     catch (Exception e)
         {
      ContextUtil.abortTransaction(context);
      throw new FrameworkException(e);
         }
   }

    /**
     * Gets the attribute value on the EBOM relationship for the Primary Part.
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains:
     * actual name of Attribute
     * objectList - a MapList of object information.
     * @return Vector of "attribute values for each row.
     * @throws Exception if the operation fails.
     * @since EC X3.
     */
    public Vector getAttributeOnEBOMRel(Context context,String sAttribute,MapList objectList)
        throws Exception
    {
        Vector columnVals                = new Vector();

        try{
            MapList objList                    = (MapList)objectList;
            String srealAttrName            = sAttribute;
            // Iterating through the Object List
            Iterator i                        = objList.iterator();
            while (i.hasNext())
                {
                Map m                        = (Map) i.next();
                String sEBOMSubsRelid        = (String)m.get("EBOM ID");
                DomainRelationship domRel    = new DomainRelationship(sEBOMSubsRelid);
                String strAttributeValue        =domRel.getAttributeValue(context,sEBOMSubsRelid,srealAttrName);
                columnVals.add(strAttributeValue);
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }

        return columnVals;
    }

    /**
     * Gets the "Find Number" attribute on the EBOM relationship for the Primary Part.
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains packed HashMap with the following entries:
     * objectList - a MapList of object information.
     * @return Vector of "Find Number" values for each row.
     * @throws Exception if the operation fails.
     * @since EC X3.
     */
    public Vector getPrimaryFindNumber(Context context,
                              String[] args)
        throws Exception
                    {
        Vector columnVals                = new Vector();

        try{
            HashMap programMap     = (HashMap)JPO.unpackArgs(args);
            MapList objList        = (MapList)programMap.get("objectList");
            String srealAttrFNName = PropertyUtil.getSchemaProperty(context,"attribute_FindNumber");
            columnVals=getAttributeOnEBOMRel(context,srealAttrFNName,objList);
            }
            catch(Exception ex)
                        {
                throw ex;
                        }
        return columnVals;
    }

    /**
     * Gets the "Reference Designator" attribute on the EBOM relationship for the Primary Part.
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains packed HashMap with the following entries:
     * objectList - a MapList of object information.
     * @return Vector of "Reference Designator" values for each row.
     * @throws Exception if the operation fails.
     * @since EC X3.
     */
    public Vector getPrimaryReferenceDesignator(Context context,
                              String[] args)
        throws Exception
    {
        Vector columnVals                    = new Vector();
        try{
            HashMap programMap                = (HashMap)JPO.unpackArgs(args);
            MapList objList                    = (MapList)programMap.get("objectList");
            String srealAttrRefDesName        = PropertyUtil.getSchemaProperty(context,"attribute_ReferenceDesignator");
            columnVals=getAttributeOnEBOMRel(context,srealAttrRefDesName,objList);
            }
            catch(Exception ex)
                        {
                throw ex;
            }
        return columnVals;
                        }

    /**
     * Gets the "Quantity" attribute on the EBOM relationship for the Primary Part.
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains packed HashMap with the following entries:
     * objectList - a MapList of object information.
     * @return Vector of "Quantity" values for each row.
     * @throws Exception if the operation fails.
     * @since EC X3.
     * 
     * 2017x FD02 : Modified the JPO to get the input value instead of value for Quantity attribute 
     */
    public Vector getPrimaryEBOMQty(Context context, String[] args)
        throws Exception
                        {
        Vector columnVals = new Vector();
        try{
            HashMap programMap                = (HashMap)JPO.unpackArgs(args);
            MapList objList                    = (MapList)programMap.get("objectList");
            String srealAttrQtyName            = PropertyUtil.getSchemaProperty(context,"attribute_Quantity");
               
	            Iterator i                        = objList.iterator();
	            while (i.hasNext())
	                {
	                Map m                        = (Map) i.next();
	                String sEBOMSubsRelid        = (String)m.get("EBOM ID");
	                DomainRelationship domRel    = new DomainRelationship(sEBOMSubsRelid);
	                // Get the Input value of the Quantity
	                String strQtyValue = "attribute["+srealAttrQtyName+"].inputvalue";
	               
	                StringList relSelectables = new StringList(1);
	                relSelectables.addElement(strQtyValue);
	        		
	                MapList relData     = DomainRelationship.getInfo(context, new String[]{sEBOMSubsRelid}, relSelectables);
	    			Map strAttributeValue			= (Map)relData.get(0);                
	    			String sQtyInputVal = (String)strAttributeValue.get(strQtyValue);
	    			columnVals.add(sQtyInputVal);
	            }
            }
            catch(Exception ex)
                            {
                throw ex;
            }
            
        return columnVals;
     }

    /**
     * Gets the Primary Part name for the Substitute Part .
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains packed HashMap with the following entries:
     * objectList - a MapList of object information.
     * @return Vector of "Part Name(As HTML Output)" values for each row.
     * @throws Exception if the operation fails.
     * @since EC X3.
     */
    public Vector getPrimaryForSubstitute(Context context,
    		String[] args)throws Exception
    {
    	Vector columnVals                = new Vector();
    	try{
    		HashMap programMap                = (HashMap)JPO.unpackArgs(args);
    		MapList objList                    = (MapList)programMap.get("objectList");
    		//Fix for 085380 starts
    		Map paramList = (HashMap)programMap.get("paramList");
    		String reportFormat = (String)paramList.get("reportFormat");
    		boolean isexport = false;
    		String export = (String)paramList.get("exportFormat");
    		if ( export != null ) {
    			isexport = true;
    		}
    		//Fix for 085380 ends
    		Iterator i                        = objList.iterator();
    		while (i.hasNext())
    		{
    			Map m                        = (Map) i.next();
    			String sEBOMSubsRelid        = (String)m.get("EBOM ID");
    			SelectList resultSelects    = new SelectList(2);
    			String[] RelIdArray            = new String[1];
    			RelIdArray[0]                = sEBOMSubsRelid;

    			resultSelects.add("to."+DomainObject.SELECT_ID);
    			resultSelects.add("to."+DomainObject.SELECT_NAME);

    			MapList resultList = DomainRelationship.getInfo(context, RelIdArray, resultSelects);
    			Iterator itr = resultList.iterator();
    			Map map = (Map) itr.next();
    			String sPartid      = (String)map.get("to.id");
    			String sPartName    =(String)map.get("to.name");
    			StringBuffer output = new StringBuffer(256);
    			//Fix for 085380 starts
    			//do not show hyperlinks if it is a printer friendly or excel export page
    			//length will be >0 when format is HTML, ExcelHTML, CSV or TXT
    			if (reportFormat != null && !"null".equals(reportFormat) && (reportFormat.length() > 0) || isexport)
    			{
    				output.append(XSSUtil.encodeForHTML(context, sPartName));
    			}
    			else
    			{
    				output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId="+ XSSUtil.encodeForURL(context, sPartid) +"', '700', '600', 'false', 'popup', '')\">"+sPartName+"</a> <br> </br>");
    			}
    			//Fix for 0853802 ends
    			columnVals.add(output.toString());
    		}
    	}
    	catch(Exception ex)
    	{
    		throw ex;
    	}
    	return columnVals;
    }

    /**
     * Gets the information of Primary Part of the Substitute Part .
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains following entries:
     * What to get for the primary part
     * objectList - a MapList of object information.
     * @return Vector of required iformation for each row.
     * @throws Exception if the operation fails.
     * @since EC X3.
     */
    public Vector getPrimartPartInfo(Context context,String getWhat,MapList objectList)
        throws Exception
    {
        Vector columnVals                = new Vector();

        try{
            MapList objList                    = (MapList)objectList;
            String strgetWhat                = getWhat;
            // Iterating through the Object List
            Iterator i                        = objList.iterator();
            while (i.hasNext())
                                    {
                Map m                        = (Map) i.next();
                String sEBOMSubsRelid        = (String)m.get("EBOM ID");
                SelectList resultSelects    = new SelectList(1);
                String[] RelIdArray            = new String[1];
                RelIdArray[0]                = sEBOMSubsRelid;
                if("type".equals(strgetWhat))
                {
                resultSelects.add("to."+DomainObject.SELECT_TYPE);
                }
                else if("revision".equals(strgetWhat))
                {
                resultSelects.add("to."+DomainObject.SELECT_REVISION);
                }
                else if("policy".equals(strgetWhat)){
                	resultSelects.add("to."+DomainObject.SELECT_POLICY);
                }
                MapList resultList = DomainRelationship.getInfo(context,
                                                           RelIdArray,
                                                           resultSelects);

                Iterator itr = resultList.iterator();
                Map map = (Map) itr.next();
                String strtype="";
                if("type".equals(strgetWhat))
                {
                  strtype = (String)map.get("to.type");
				}
                else if("revision".equals(strgetWhat))
				{
                  strtype = (String)map.get("to.revision");
				}
                else if("policy".equals(strgetWhat)){
                	strtype = (String)map.get("to.policy");
                }
                columnVals.add(strtype);
                            }
                        }
        catch(Exception ex)
                        {
            throw ex;
                        }

        return columnVals;
                    }
    /**
     * Gets the Type of Primary Part of the Substitute Part .
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains packed HashMap with the following entries:
     * objectList - a MapList of object information.
     * @return Vector of "type" values for each row.
     * @throws Exception if the operation fails.
     * @since EC X3.
     */
    public Vector getPrimaryComponentType(Context context,
                              String[] args)
        throws Exception
    {
        Vector columnVals                    = new Vector();
        try{
            HashMap programMap                = (HashMap)JPO.unpackArgs(args);
            MapList objList                    = (MapList)programMap.get("objectList");
            columnVals                        =getPrimartPartInfo(context,"type",objList);

                }
            catch(Exception ex)
            {
             throw ex;
            }
      return columnVals;
   }

   /**
     * Gets the Revision of Primary Part of the Substitute Part .
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains packed HashMap with the following entries:
     * objectList - a MapList of object information.
     * @return Vector of "revision" values for each row.
     * @throws Exception if the operation fails.
     * @since EC X3.
     */
    public Vector getPrimaryRev(Context context,
                              String[] args)
        throws Exception
    {
        Vector columnVals                    = new Vector();
        try
        {
            HashMap programMap                = (HashMap)JPO.unpackArgs(args);
            MapList objList                    = (MapList)programMap.get("objectList");
            columnVals                        = getPrimartPartInfo(context,"revision",objList);
		}
        catch(Exception ex)
        {
           throw ex;
        }
        return columnVals;
    }
    
    /**
     * Gets the Policy of Primary Part of the Substitute Part .
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains packed HashMap with the following entries:
     * objectList - a MapList of object information.
     * @return Vector of "policy" values for each row.
     * @throws Exception if the operation fails.
     */
    public Vector getPrimaryComponentPolicy(Context context, String[] args)
    throws Exception {
    	
    	HashMap programMap = (HashMap)JPO.unpackArgs(args);
		MapList objList    = (MapList)programMap.get("objectList");
    	Vector columnVals = new Vector(objList.size());
    	
    	try {
    		
    		columnVals = getPrimartPartInfo(context, "policy", objList);
    	}
    	catch(Exception ex){
    		throw ex;
    	}
    	
    	return columnVals;
    }

  /**
   * Checks that the Specification which is going to be connected to Part is having states
   * "Review", "Approved" and "Released". If any spec object not having these states, it cannot
   * be added as "Part Specification". Also, "Review" should not be the first state.
   *
   * @param context the eMatrix <code>Context</code> object.
   *
   * @return int 0-success 1-failure.
   * @throws Exception if the operation fails.
   * @since EC 10-6
   */
    public int ensureSpecificationStates(Context context, String[] args) throws Exception
    {
        DomainObject toObject=new DomainObject(args[0]);

        StringList strList = new StringList();
        strList.add("policy.property[state_Review].value");
        strList.add("policy.property[state_Approved].value");
        strList.add("policy.property[state_Release].value");
        strList.add(SELECT_TYPE);
        strList.add(SELECT_NAME);
        strList.add(SELECT_REVISION);
        strList.add(SELECT_STATES);

        Map map = toObject.getInfo(context,strList);

        String strStateReview = (String)map.get("policy.property[state_Review].value");
        if(strStateReview == null)
        {
            strStateReview = "";
        }
        String strStateApproved = (String)map.get("policy.property[state_Approved].value");
        if(strStateApproved == null)
        {
            strStateApproved = "";
        }
        String strStateRelease = (String)map.get("policy.property[state_Release].value");
        if(strStateRelease == null)
        {
            strStateRelease = "";
        }

        String strType   = (String)map.get(SELECT_TYPE);
        String strName   = (String)map.get(SELECT_NAME);
        String strRevision   = (String)map.get(SELECT_REVISION);

        String strMessage = "";
        if("".equals(strStateReview) || "".equals(strStateApproved) || "".equals(strStateRelease) )
        {
            // the object doesn't contain the lifecyle state of a spec.
            String [] mailArguments = new String [8];
            mailArguments[0] = "emxFramework.SpecificationObject.NoSpecificationLifeCycleStates";
            mailArguments[1] = "3";
            mailArguments[2] = "Type";
            mailArguments[3] = strType;
            mailArguments[4] = "Name";
            mailArguments[5] = strName;
            mailArguments[6] = "Rev";
            mailArguments[7] = strRevision;
            strMessage = emxMailUtil_mxJPO.getMessage(context,mailArguments);
            emxContextUtil_mxJPO.mqlNotice(context,strMessage);
            return 1;
        }

        StringList strListStates = null;
        try
        {
            strListStates = (StringList)map.get(DomainConstants.SELECT_STATES);
        }
        catch(ClassCastException e)
        {
            strListStates = new StringList(1);
            strListStates.add((String)map.get(DomainConstants.SELECT_STATES));
        }

        int intReviewIndex = strListStates.indexOf(strStateReview);

        //370754 - needs to work for single states for integration apps
        if(strListStates.size() > 1 && intReviewIndex == 0)
        {
            // State strStateReview should not be the first state for Specification

            String [] mailArguments = new String [10];
            mailArguments[0] = "emxFramework.SpecificationObject.NoFirstStateReview";
            mailArguments[1] = "4";
            mailArguments[2] = "Type";
            mailArguments[3] = strType;
            mailArguments[4] = "Name";
            mailArguments[5] = strName;
            mailArguments[6] = "Rev";
            mailArguments[7] = strRevision;
            mailArguments[8] = "State";
            mailArguments[9] = strStateReview;
            strMessage = emxMailUtil_mxJPO.getMessage(context,mailArguments);
            emxContextUtil_mxJPO.mqlNotice(context,strMessage);
            return 1;
        }

        int intApprovedIndex = strListStates.indexOf(strStateApproved);
        int intReleaseIndex = strListStates.indexOf(strStateRelease);

        //370754 - we may have less than 3 states - single state in some cases
        if( (intReleaseIndex >= intApprovedIndex) && (intApprovedIndex >= intReviewIndex) )
        {
            return 0;
        }
        else
        {
            // Object states are not same as Specification states.
            String [] mailArguments = new String [16];
            mailArguments[0] = "emxFramework.SpecificationObject.SequenceNotSameAsSpecStates";
            mailArguments[1] = "7";
            mailArguments[2] = "Type";
            mailArguments[3] = strType;
            mailArguments[4] = "Name";
            mailArguments[5] = strName;
            mailArguments[6] = "Rev";
            mailArguments[7] = strRevision;
            mailArguments[8] = "Frozen";
            mailArguments[9] = strStateReview;
            mailArguments[10] = "Approved";
            mailArguments[11] = strStateApproved;
            mailArguments[12] = "Approved";
            mailArguments[13] = strStateApproved;
            mailArguments[14] = "Released";
            mailArguments[15] = strStateRelease;
            strMessage = emxMailUtil_mxJPO.getMessage(context,mailArguments);
            emxContextUtil_mxJPO.mqlNotice(context,strMessage);
            return 1;
        }
    }

/*
 *  This method returns a Vector contains the modified Find Numbers .
 *  It appends zeros according to the property key configuration and displays.
 *  The change is display only no updation of db is done.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds param arguments.
 * @return Vector containing Find Numbers
 *  @throws Exception if the operation fails.
 * @since EC 10.6.
 */
 public Vector getFindNumber(Context context,String args[]) throws Exception
 {

     HashMap programMap = (HashMap) JPO.unpackArgs(args);

     MapList objectList = (MapList)programMap.get("objectList");

     String fnLength = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.FindNumberLength");
     String fnDisplayLeadingZeros = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.FindNumberDisplayLeadingZeros");

     boolean isStrNumber=true;
     int fnLen = Integer.parseInt(fnLength);

    Vector fnValuesVector = new Vector(objectList.size());
    Iterator bomListItr = objectList.iterator();
    String fnValue = "";
    String fnNothing=" ";
    while(bomListItr.hasNext())
    {
      Map bomMap = (Map)bomListItr.next();
      String strRelId = (String) bomMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        fnValue = "";
      if (strRelId != null && strRelId.length() > 0)
      {

            try
            {
                DomainRelationship doRel = new DomainRelationship(strRelId);
                fnValue = doRel.getAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER);
            }
           catch (Exception excep)
           {
               fnValue = "";
           }
       }

      //Added below code to fix bug 333456
      if(fnValue == null || "null".equalsIgnoreCase(fnValue)) {
          fnValue = "";
      }

      //Checking whether Find number is a number or a String
      if(!fnValue.startsWith("0"))
      {
          try
          {
            Long.valueOf(fnValue);
          }
          catch (Exception ex)
          {
               isStrNumber = false;
          }
      }
      else
      {
          isStrNumber = false;
      }

      //Display the leading zeros only if find number length >0
      //Find number is number and display leadingzeros property set to true
      if(fnLen>0 && isStrNumber && "true".equalsIgnoreCase(fnDisplayLeadingZeros))
      {
          for(int i=0;i<=fnLen;i++)
          {
             if(fnValue.length()<fnLen)
             {

                 fnValue = "0"+fnValue;
             }
             else
             {
                 fnValuesVector.addElement(fnValue);
                 break;
             }
           }
       }
       else
       {
           if(!"".equals(fnValue))
           {
            fnValuesVector.addElement(fnValue);
           }
           else
           {
               fnValuesVector.addElement(fnNothing);
           }
       }
      isStrNumber=true;
    }

     return fnValuesVector;
 }

     /**
     * This method expands the EBOM list based on Reference Designator expand format
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns MapList EBOM list with expanded Reference Designator.
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getExpandedEBOM(Context context, String[] args)
        throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String langStr = (String)paramMap.get("languageStr");
        String multilevelReport = (String) paramMap.get("multilevelReport");
        String reportFormat = (String) paramMap.get("repFormat");//IR-044467
        MapList ebomList = null;
        if(multilevelReport!=null && multilevelReport.equalsIgnoreCase("yes")) {
            ebomList = getMultiLevelEBOMsWithRelSelectables (context, args);
        } else {
            ebomList = getEBOMsWithRelSelectables (context, args);
        }

        Compare c = new Compare();
        MapList finalList = new MapList();
        //375650: reverting changes of 369074 for reports
        //below code Added for--044467
        if ((reportFormat != null) && (!"expandedFormat".equalsIgnoreCase(reportFormat))
        		&& (!"getExpandedEBOM".equalsIgnoreCase(reportFormat)))
        {
        ebomList.addSortKey("id", "ascending", "String");
        ebomList.sort();
        }

        String delimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.DelimitedReferenceDesignatorSeparator");
        char   charDecimalSymbol = PersonUtil.getDecimalSymbol(context);

        //return ebomList;
        //1. Find out Well formed of same ids.
        //2. Group them if they well formed otherwise add them with bad icon
        HashMap rdMap = new HashMap();

        Iterator itr = ebomList.iterator();
        while (itr.hasNext())
        {
            boolean wellFormed = true;
            boolean bfractional = false;
            Map m = (Map) itr.next();
            String RefDes = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
            String sQty = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY);
            StringList strlQuantity = FrameworkUtil.split(sQty, ".");
            if(strlQuantity.size()==2){
                String strFractional = (String)strlQuantity.get(1);
                if("0".equals(strFractional)){
                    bfractional = false;
                }else{
                    bfractional = true;
                }
            }
            if (charDecimalSymbol != '.')
                sQty =  sQty.replace(charDecimalSymbol,'.');

            float floatQuantity = (new Float(sQty )).floatValue();
            int qty = ( new Float(sQty ) ).intValue();
            String id = (String)m.get(DomainObject.SELECT_ID);
            StringList rdList = FrameworkUtil.split(RefDes, delimiter);
//changed the name of veriable to enumElement for JDK1.5 issue
            Enumeration enumElement = rdList.elements();
            String pref = "%#%";
            while(enumElement.hasMoreElements()) {
                String s = (String)enumElement.nextElement();
                if(!isRDTokenWellFormed(context,s,pref)) {
                    wellFormed = false;
                    break;
                } else {
                    pref = getPrefix(s);
                    }
            }
            ArrayList existedList = null;  //new ArrayList();
            if(floatQuantity>0 && bfractional ){
                wellFormed = false;
            }
            else if(wellFormed) {
                int count = getCount(context,rdList);
                if(count == qty) {
                    ArrayList al = getRDExpandList(context,rdList);
                    Iterator itr1 = al.iterator();
                    existedList = (ArrayList)rdMap.get(id);
                    if(existedList==null) {
                        existedList = new ArrayList();
                    }
                    if("true".equalsIgnoreCase(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReferenceDesignatorUnique")))
                        {
                            while(itr1.hasNext()) {
                            String sRD = (String)itr1.next();
                            if(existedList.contains(sRD))
                                {
                                    wellFormed = false;
                                    break;
                                }
                            wellFormed = true;
                            }
                        }
                    if(wellFormed) {
                        existedList.addAll(al);
                        Collections.sort(al,c);
                        itr1 = al.iterator();
                        while(itr1.hasNext()) {
                            String sRD = (String)itr1.next();
                             HashMap map = new HashMap();
                                 map.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,sRD);
                                 map.put(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY,"1.0");
                                 map.put(SELECT_ID,m.get(SELECT_ID));
                                 map.put(DomainRelationship.SELECT_RELATIONSHIP_ID,m.get(DomainRelationship.SELECT_RELATIONSHIP_ID));
                                 map.put(KEY_RELATIONSHIP,m.get(KEY_RELATIONSHIP));
                                 map.put(KEY_LEVEL,m.get(KEY_LEVEL));
                                 map.put(SELECT_RELATIONSHIP_TYPE,m.get(SELECT_RELATIONSHIP_TYPE));
                                 map.put(KEY_AVLData,m.get(KEY_AVLData));
                                 map.put(SELECT_CURRENT,m.get(SELECT_CURRENT));
                                 map.put(SELECT_STATUS,m.get(SELECT_STATUS));
                                 map.put(DomainRelationship.SELECT_FIND_NUMBER,m.get(DomainRelationship.SELECT_FIND_NUMBER));
                                 map.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,m.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
                                 map.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,m.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE));
                                 map.put(DomainConstants.SELECT_TYPE,m.get(DomainConstants.SELECT_TYPE));
                                 map.put(DomainConstants.SELECT_NAME,m.get(DomainConstants.SELECT_NAME));
                                 map.put(DomainConstants.SELECT_REVISION,m.get(DomainConstants.SELECT_REVISION));
                                 map.put(DomainConstants.SELECT_DESCRIPTION,m.get(DomainConstants.SELECT_DESCRIPTION));
                                 finalList.add(map);
                        }
                    } else {
                        wellFormed = false;
                    }
                } else {
                    wellFormed = false;
                }
            }
            if(!wellFormed) {
                if("".equals(RefDes)){
                    m.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,RefDes);
                }else{
                    String incompatibleFormatAlt = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EBOMReferenceDesignator.IncompatibleFormat",langStr);
                    RefDes = "<img src=\"../common/images/iconSmallStatusAlert.gif\" border=\"0\" alt=\""+incompatibleFormatAlt+"\" title=\""+incompatibleFormatAlt+"\"/>&#160;"+RefDes;
                    m.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,RefDes);
                    m.put(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY,sQty);
                }
                finalList.add(m);
            }

            rdMap.put(id,existedList);
       }
        //375650: reverting changes of 369074 for reports
        //below code required for--044467 and 371814
        if ("expandedFormat".equalsIgnoreCase(reportFormat)){
        HashMap hmTemp = new HashMap();
        hmTemp.put("expandMultiLevelsJPO","true");
        finalList.add(hmTemp);
        }
        return finalList;
    }

    /**
     * This method groups the EBOM list based on Reference Designator delimited rollup format
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns MapList EBOM list with delimited roll-up Reference Designator
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getDelimitedRollupEBOM (Context context, String[] args)
        throws Exception
    {
            Compare c = new Compare();
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String langStr = (String) paramMap.get("languageStr");
            String reportFormat = (String) paramMap.get("repFormat");//IR-044467
            String multilevelReport = (String) paramMap.get("multilevelReport");
            MapList ebomList = null;
            if(multilevelReport!=null && multilevelReport.equalsIgnoreCase("yes")) {
                ebomList = getMultiLevelEBOMsWithRelSelectables (context, args);
            } else {
                ebomList = getEBOMsWithRelSelectables (context, args);
            }

            MapList finalList = new MapList();
            //375650: reverting changes of 369074 for reports
            //below code Added for--044467
            if ((reportFormat != null) && (!"delimitedRollUpFormat".equalsIgnoreCase(reportFormat))
            		       && (!"getDelimitedRollupEBOM".equalsIgnoreCase(reportFormat))){
            ebomList.addSortKey("id", "ascending", "String");
            ebomList.sort();
            }
            String delimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.DelimitedReferenceDesignatorSeparator");
            char delimiterChar = delimiter.charAt(0);

            //1. Find out Well formed of same ids.
            //2. Group them if they well formed otherwise add them with bad icon
            String tempFN = "";
            String tempCL = "";
            String tempUSG = "";
            char   charDecimalSymbol = PersonUtil.getDecimalSymbol(context);

            HashMap rdMap = new HashMap();
            String ObjId = "";
            HashMap dummyMap = null;

            Iterator itr = ebomList.iterator();

            while (itr.hasNext())
            {
                boolean wellFormed = true;
                boolean bfractional = false;
                Map m = (Map) itr.next();
                String RefDes = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
                String sQty = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY);
                StringList strlQuantity = FrameworkUtil.split(sQty, ".");
                if(strlQuantity.size()==2){
                    String strFractional = (String)strlQuantity.get(1);
                    if("0".equals(strFractional)){
                        bfractional = false;
                    }else{
                        bfractional = true;
                    }
                }
                if (charDecimalSymbol != '.')
                    sQty =  sQty.replace(charDecimalSymbol,'.');

                float floatQuantity = (new Float(sQty )).floatValue();
                int qty = ( new Float(sQty ) ).intValue();
                String id = (String)m.get(DomainObject.SELECT_ID);
                StringList rdList = FrameworkUtil.split(RefDes, delimiter);
//changed the name of veriable to enumElement for JDK1.5 issue
                Enumeration enumElement = rdList.elements();
                String pref = "%#%";
                while(enumElement.hasMoreElements()) {
                    String s = (String)enumElement.nextElement();
                        if(!isRDTokenWellFormed(context,s,pref)) {
                            wellFormed = false;
                            break;
                        } else {
                            pref = getPrefix(s);
                        }
                }
                ArrayList existedList = null;
//              Added for bug #337312
                if(floatQuantity>0 && bfractional ){
                    wellFormed = false;
                }
                else if(wellFormed) {
                    int count = getCount(context,rdList);
                    if(count == qty) {
                        ArrayList al = getRDExpandList(context,rdList);
                        Iterator itr1 = al.iterator();
                        existedList = (ArrayList)rdMap.get(id);
                        if(existedList==null) {
                            existedList = new ArrayList();
                        }
                         if("true".equalsIgnoreCase(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReferenceDesignatorUnique"))) {
                        while(itr1.hasNext()) {
                            String sRD = (String)itr1.next();
                            if(existedList.contains(sRD)) {
                                wellFormed = false;
                                break;
                            }
                            wellFormed = true;
                        }
                         }
                        if(wellFormed) {
                             existedList.addAll(al);
                             HashMap map = new HashMap();
                             Collections.sort(existedList,c);
                             map.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,(existedList.toString()).replace(',',delimiterChar));
                             map.put(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY,""+new Float(existedList.size()));
                             map.put(SELECT_ID,m.get(SELECT_ID));
                             map.put(DomainRelationship.SELECT_RELATIONSHIP_ID,m.get(DomainRelationship.SELECT_RELATIONSHIP_ID));
                             map.put(KEY_RELATIONSHIP,m.get(KEY_RELATIONSHIP));
                             map.put(KEY_LEVEL,m.get(KEY_LEVEL));
                             map.put(SELECT_RELATIONSHIP_TYPE,m.get(SELECT_RELATIONSHIP_TYPE));
                             map.put(SELECT_CURRENT,m.get(SELECT_CURRENT));
                             map.put(SELECT_STATUS,m.get(SELECT_STATUS));
                             map.put(KEY_AVLData,m.get(KEY_AVLData));
                             map.put(DomainRelationship.SELECT_FIND_NUMBER,m.get(DomainRelationship.SELECT_FIND_NUMBER));
                             map.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,m.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
                             map.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,m.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE));

                             if("".equals(ObjId)) {
                                 ObjId = id;
                                 dummyMap = map;
                                 tempFN = pref+":"+m.get(DomainRelationship.SELECT_FIND_NUMBER);
                                 tempCL = pref+":"+m.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                                 tempUSG = pref+":"+m.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);
                             }else if(ObjId.equals(id)) {
                                 String strFn = (String)m.get(DomainRelationship.SELECT_FIND_NUMBER);
                                 String strCL   = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                                 String strUSG  = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);

                                 tempFN += "*"+pref+":"+strFn;
                                 map.put(DomainRelationship.SELECT_FIND_NUMBER,tempFN);

                                 tempCL  += "*"+pref+":"+strCL;
                                 tempUSG += "*"+pref+":"+strUSG;
                                 map.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,tempCL);
                                 map.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,tempUSG);

                                 dummyMap = map;
                             } else {
                                 tempFN  = pref+":"+m.get(DomainRelationship.SELECT_FIND_NUMBER);
                                 tempCL  = pref+":"+m.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                                 tempUSG = pref+":"+m.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);

                                 String refDesList = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
                                 Map delimitedMap = getDelimitedMap(context,refDesList);
                                  if(delimitedMap!=null && !delimitedMap.isEmpty()) {
                                     Iterator delimiterItr = delimitedMap.values().iterator();
                                     while(delimiterItr.hasNext()) {
                                         HashMap map1 = new HashMap();
                                         ArrayList lst = (ArrayList)delimiterItr.next();
                                         String strRef = lst.toString();
                                         strRef = strRef.substring(1,strRef.length()-1);
                                         map1.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,strRef.replace(',',delimiterChar));
                                         map1.put(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY,""+new Float(lst.size()));
                                         map1.put(SELECT_ID,dummyMap.get(SELECT_ID));

                                         String strTempFN = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_FIND_NUMBER);
                                         HashMap FNMap = null;
                                         if(strTempFN.indexOf('*') != -1) {
                                             FNMap = getModifiedEBOMRelAttributeValue(strTempFN);
                                         }
                                         if(FNMap==null) {
                                             map1.put(DomainRelationship.SELECT_FIND_NUMBER,dummyMap.get(DomainRelationship.SELECT_FIND_NUMBER));
                                         }else {
                                             map1.put(DomainRelationship.SELECT_FIND_NUMBER,""+FNMap.get(getPrefix(strRef)));
                                         }
                                         String strTempCL = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                                         HashMap CLMap = null;
                                         if(strTempCL.indexOf('*') != -1) {
                                             CLMap = getModifiedEBOMRelAttributeValue(strTempCL);
                                         }

                                         if(CLMap==null) {
                                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
                                         }else {
                                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,""+CLMap.get(getPrefix(strRef)));
                                         }

                                         String strTempUSG = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);
                                         HashMap USGMap = null;
                                         if(strTempUSG.indexOf('*') != -1) {
                                             USGMap = getModifiedEBOMRelAttributeValue(strTempUSG);
                                         }

                                         if(USGMap==null) {
                                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE));
                                         }else {
                                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,""+USGMap.get(getPrefix(strRef)));
                                         }

                                         map1.put(DomainRelationship.SELECT_RELATIONSHIP_ID,dummyMap.get(DomainRelationship.SELECT_RELATIONSHIP_ID));
                                         map1.put(KEY_RELATIONSHIP,dummyMap.get(KEY_RELATIONSHIP));
                                         map1.put(KEY_LEVEL,dummyMap.get(KEY_LEVEL));
                                         map1.put(SELECT_RELATIONSHIP_TYPE,dummyMap.get(SELECT_RELATIONSHIP_TYPE));
                                         map1.put(KEY_AVLData,dummyMap.get(KEY_AVLData));
                                         map1.put(SELECT_CURRENT,dummyMap.get(SELECT_CURRENT));
                                         map1.put(SELECT_STATUS,dummyMap.get(SELECT_STATUS));

                                         finalList.add(map1);
                                     }
                                 }
                                 dummyMap = map;
                                 ObjId = id;
                             }
                             rdMap.put(id,existedList);
                        } else {
                            wellFormed = false;
                        }
                    } else {
                        wellFormed = false;
                    }
                }
                if(!wellFormed) {
                    if("".equals(RefDes)){
                        m.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,RefDes);
                     }else{
                        String incompatibleFormatAlt = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EBOMReferenceDesignator.IncompatibleFormat",langStr);
                        RefDes = "<img src=\"../common/images/iconSmallStatusAlert.gif\" border=\"0\" alt=\""+incompatibleFormatAlt+"\" title=\""+incompatibleFormatAlt+"\"/>&#160;"+RefDes;
                        m.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,RefDes);
                    }
                    finalList.add(m);
                }
            } //end of while

            if(dummyMap != null) {
                 String refDesList = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
                 HashMap FNMap = null;
                 tempFN = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_FIND_NUMBER);
                 if(tempFN.indexOf('*') != -1) {
                     FNMap = getModifiedEBOMRelAttributeValue(tempFN);
                 }

                 String strTempCL = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                 HashMap CLMap = null;
                 if(strTempCL.indexOf('*') != -1) {
                     CLMap = getModifiedEBOMRelAttributeValue(strTempCL);
                 }

                 String strTempUSG = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);
                 HashMap USGMap = null;
                 if(strTempUSG.indexOf('*') != -1) {
                     USGMap = getModifiedEBOMRelAttributeValue(strTempUSG);
                 }

                 Map delimitedMap = getDelimitedMap(context,refDesList);
                 if(delimitedMap!=null && !delimitedMap.isEmpty()) {
                     Iterator delimiterItr = delimitedMap.values().iterator();
                     while(delimiterItr.hasNext()) {
                         HashMap map1 = new HashMap();
                         ArrayList lst = (ArrayList)delimiterItr.next();
                         String strRef = lst.toString();
                         strRef = strRef.substring(1,strRef.length()-1);

                         if(FNMap==null) {
                             map1.put(DomainRelationship.SELECT_FIND_NUMBER,dummyMap.get(DomainRelationship.SELECT_FIND_NUMBER));
                         }else {
                             map1.put(DomainRelationship.SELECT_FIND_NUMBER,""+FNMap.get(getPrefix(strRef)));
                         }

                         if(CLMap==null) {
                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
                         }else {
                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,""+CLMap.get(getPrefix(strRef)));
                         }

                         if(USGMap==null) {
                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE));
                         }else {
                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,""+USGMap.get(getPrefix(strRef)));
                         }

                         map1.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,strRef);
                         map1.put(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY,""+new Float(lst.size()));
                         map1.put(SELECT_ID,dummyMap.get(SELECT_ID));
                         map1.put(DomainRelationship.SELECT_RELATIONSHIP_ID,dummyMap.get(DomainRelationship.SELECT_RELATIONSHIP_ID));
                         map1.put(KEY_RELATIONSHIP,dummyMap.get(KEY_RELATIONSHIP));
                         map1.put(KEY_LEVEL,dummyMap.get(KEY_LEVEL));
                         map1.put(SELECT_RELATIONSHIP_TYPE,dummyMap.get(SELECT_RELATIONSHIP_TYPE));
                         map1.put(SELECT_CURRENT,dummyMap.get(SELECT_CURRENT));
                         map1.put(SELECT_STATUS,dummyMap.get(SELECT_STATUS));
                         map1.put(KEY_AVLData,dummyMap.get(KEY_AVLData));

                         finalList.add(map1);
                     }
                 }
            }
            //375650: reverting changes of 369074 for reports
            //below code required for--044467 and 371814
            if ("delimitedRollUpFormat".equalsIgnoreCase(reportFormat)){
            HashMap hmTemp = new HashMap();
            hmTemp.put("expandMultiLevelsJPO","true");
            finalList.add(hmTemp);
            }
            return finalList;
    }

    /**
     * This method ranges the EBOM list based on Reference Designator Range Roll-up format
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns MapList EBOM list with range roll-up Reference Designator.
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRangeRollupEBOM (Context context,
                             String[] args)
        throws Exception
    {
            Compare c = new Compare();
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            String multilevelReport = (String) paramMap.get("multilevelReport");
            String reportFormat = (String) paramMap.get("repFormat");//IR-044467
            MapList ebomList = null;
            if(multilevelReport!=null && multilevelReport.equalsIgnoreCase("yes")) {
                ebomList = getMultiLevelEBOMsWithRelSelectables (context, args);
            } else {
                ebomList = getEBOMsWithRelSelectables (context, args);
            }

            String langStr = (String) paramMap.get("languageStr");
            String id = "";
            MapList finalList = new MapList();
            //375650: reverting changes of 369074 for reports
            //below code Added for--044467
            if ((reportFormat != null) && (!"rangeRollUpFormat".equalsIgnoreCase(reportFormat))
            		&& (!"getRangeRollupEBOM".equalsIgnoreCase(reportFormat)))
            if ((reportFormat != null) && (!"rangeRollUpFormat".equalsIgnoreCase(reportFormat)))
            {
            ebomList.addSortKey("id", "ascending", "String");
            ebomList.sort();
            }
            String delimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.DelimitedReferenceDesignatorSeparator");
            char delimiterChar = delimiter.charAt(0);

            String tempFN = "";
            String tempCL = "";
            String tempUSG = "";
            char   charDecimalSymbol = PersonUtil.getDecimalSymbol(context);
            //return ebomList;
            //1. Find out Well formed of same ids.
            //2. Group them if they well formed otherwise add them with bad icon
            HashMap rdMap = new HashMap();
            String ObjId = "";
            HashMap dummyMap = null;

            Iterator itr = ebomList.iterator();
            while (itr.hasNext())
            {
                boolean wellFormed = true;
                boolean bfractional = false;
                Map m = (Map) itr.next();
                String RefDes = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
                String sQty = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY);
                StringList strlQuantity = FrameworkUtil.split(sQty, ".");
                if(strlQuantity.size()==2){
                    String strFractional = (String)strlQuantity.get(1);
                    if("0".equals(strFractional)){
                        bfractional = false;
                    }else{
                        bfractional = true;
                    }
                }
                if (charDecimalSymbol != '.')
                    sQty =  sQty.replace(charDecimalSymbol,'.');

                float floatQuantity = (new Float(sQty )).floatValue();
                int qty = ( new Float(sQty ) ).intValue();
                id = (String)m.get(DomainObject.SELECT_ID);
                StringList rdList = FrameworkUtil.split(RefDes, delimiter);
//changed the name of veriable to enumElement for JDK1.5 issue
                Enumeration enumElement = rdList.elements();
                String pref = "%#%";
                while(enumElement.hasMoreElements()) {
                    String s = (String)enumElement.nextElement();
                    if(!isRDTokenWellFormed(context,s,pref)) {
                        wellFormed = false;
                        break;
                    } else {
                        pref = getPrefix(s);
                    }
                }
                ArrayList existedList = null;  //new ArrayList();
                if(floatQuantity>0 && bfractional ){
                    wellFormed = false;
                }
                else if(wellFormed) {
                    int count = getCount(context,rdList);
                    //Modified for Bug NO 327334  1/12/2007 Begin

                    if(count == qty&&!(RefDes.length()==0)) {
                    //Modified for Bug NO 327334  1/12/2007 Ends
                        //Individual rd values will be placed in the below list.
                        ArrayList al = getRDExpandList(context,rdList);
                        Iterator itr1 = al.iterator();
                        existedList = (ArrayList)rdMap.get(id);
                        if(existedList==null) {
                            existedList = new ArrayList();
                        }
                        if("true".equalsIgnoreCase(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReferenceDesignatorUnique"))) {
                        while(itr1.hasNext()) {
                            String sRD = (String)itr1.next();
                            if(existedList.contains(sRD)) {
                                wellFormed = false;
                                break;
                            }
                            wellFormed = true;
                        }
                        }
                        if(wellFormed) {
                            existedList.addAll(al);
                            HashMap map = new HashMap();
                            Collections.sort(existedList,c);
                            map.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,(existedList.toString()).replace(',',delimiterChar));
                            map.put(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY,""+new Float(existedList.size() ));
                            map.put(SELECT_ID,m.get(SELECT_ID));
                            map.put(DomainRelationship.SELECT_RELATIONSHIP_ID,m.get(DomainRelationship.SELECT_RELATIONSHIP_ID));
                            map.put(KEY_RELATIONSHIP,m.get(KEY_RELATIONSHIP));
                            map.put(KEY_LEVEL,m.get(KEY_LEVEL));
                            map.put(SELECT_RELATIONSHIP_TYPE,m.get(SELECT_RELATIONSHIP_TYPE));
                            map.put(SELECT_CURRENT,m.get(SELECT_CURRENT));
                            map.put(SELECT_STATUS,m.get(SELECT_STATUS));
                            map.put(KEY_AVLData,m.get(KEY_AVLData));
                            map.put(DomainRelationship.SELECT_FIND_NUMBER,m.get(DomainRelationship.SELECT_FIND_NUMBER));
                            map.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,m.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
                            map.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,m.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE));

                            if("".equals(ObjId)) {
                                ObjId = id;
                                dummyMap = map;
                                tempFN = pref+":"+m.get(DomainRelationship.SELECT_FIND_NUMBER);
                                tempCL = pref+":"+m.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                                tempUSG = pref+":"+m.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);
                            }else if(ObjId.equals(id)) {
                                 String strFn = (String)m.get(DomainRelationship.SELECT_FIND_NUMBER);
                                 String strCL   = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                                 String strUSG  = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);

                                 tempFN += "*"+pref+":"+strFn;
                                 map.put(DomainRelationship.SELECT_FIND_NUMBER,tempFN);

                                 tempCL  += "*"+pref+":"+strCL;
                                 tempUSG += "*"+pref+":"+strUSG;
                                 map.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,tempCL);
                                 map.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,tempUSG);

                                 dummyMap = map;
                            } else {
                                tempFN  = pref+":"+m.get(DomainRelationship.SELECT_FIND_NUMBER);
                                tempCL  = pref+":"+m.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                                tempUSG = pref+":"+m.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);

                                String refDesList = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
                                Map delimitedMap = getRangeMap(context,refDesList);
                                if(delimitedMap!=null && !delimitedMap.isEmpty()) {
                                    Iterator delimiterItr = delimitedMap.values().iterator();
                                    while(delimiterItr.hasNext()) {
                                        HashMap map1 = new HashMap();
                                        String strRef = (String)delimiterItr.next();
                                        map1.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,strRef);
                                        map1.put(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY,""+getRangeQuantity(context,strRef));
                                        map1.put(SELECT_ID,dummyMap.get(SELECT_ID));

                                         String strTempFN = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_FIND_NUMBER);
                                         HashMap FNMap = null;
                                         if(strTempFN.indexOf('*') != -1) {
                                             FNMap = getModifiedEBOMRelAttributeValue(strTempFN);
                                         }
                                         if(FNMap==null) {
                                             map1.put(DomainRelationship.SELECT_FIND_NUMBER,dummyMap.get(DomainRelationship.SELECT_FIND_NUMBER));
                                         }else {
                                             map1.put(DomainRelationship.SELECT_FIND_NUMBER,""+FNMap.get(getPrefix(strRef)));
                                         }
                                         String strTempCL = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                                         HashMap CLMap = null;
                                         if(strTempCL.indexOf('*') != -1) {
                                             CLMap = getModifiedEBOMRelAttributeValue(strTempCL);
                                         }

                                         if(CLMap==null) {
                                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
                                         }else {
                                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,""+CLMap.get(getPrefix(strRef)));
                                         }

                                         String strTempUSG = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);
                                         HashMap USGMap = null;
                                         if(strTempUSG.indexOf('*') != -1) {
                                             USGMap = getModifiedEBOMRelAttributeValue(strTempUSG);
                                         }

                                         if(USGMap==null) {
                                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE));
                                         }else {
                                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,""+USGMap.get(getPrefix(strRef)));
                                         }

                                        map1.put(DomainRelationship.SELECT_RELATIONSHIP_ID,dummyMap.get(DomainRelationship.SELECT_RELATIONSHIP_ID));
                                        map1.put(KEY_RELATIONSHIP,dummyMap.get(KEY_RELATIONSHIP));
                                        map1.put(KEY_LEVEL,dummyMap.get(KEY_LEVEL));
                                        map1.put(SELECT_RELATIONSHIP_TYPE,dummyMap.get(SELECT_RELATIONSHIP_TYPE));
                                        map1.put(SELECT_CURRENT,dummyMap.get(SELECT_CURRENT));
                                        map1.put(SELECT_STATUS,dummyMap.get(SELECT_STATUS));
                                        map1.put(KEY_AVLData,m.get(KEY_AVLData));
                                        finalList.add(map1);
                                    }
                                }
                                dummyMap = map;
                                ObjId = id;
                            }
                            rdMap.put(id,existedList);
                        } else {
                            wellFormed = false;
                        }
                    } else {
                        wellFormed = false;
                    }
                }
                if(!wellFormed) {
                    if("".equals(RefDes)){
                        m.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,RefDes);
                     }else{
                        String incompatibleFormatAlt = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EBOMReferenceDesignator.IncompatibleFormat",langStr);
                        RefDes = "<img src=\"../common/images/iconSmallStatusAlert.gif\" border=\"0\" alt=\""+incompatibleFormatAlt+"\" title=\""+incompatibleFormatAlt+"\"/>&#160;"+RefDes;
                        m.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,RefDes);
                    }
                    finalList.add(m);
                }
           }

           if(dummyMap != null) {
                 String refDesList = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
                 HashMap FNMap = null;
                 tempFN = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_FIND_NUMBER);
                 if(tempFN.indexOf('*') != -1) {
                     FNMap = getModifiedEBOMRelAttributeValue(tempFN);
                 }

                 String strTempCL = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                 HashMap CLMap = null;
                 if(strTempCL.indexOf('*') != -1) {
                     CLMap = getModifiedEBOMRelAttributeValue(strTempCL);
                 }

                 String strTempUSG = (String)dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);
                 HashMap USGMap = null;
                 if(strTempUSG.indexOf('*') != -1) {
                     USGMap = getModifiedEBOMRelAttributeValue(strTempUSG);
                 }

                 Map delimitedMap = getRangeMap(context,refDesList);
                 if(delimitedMap!=null && !delimitedMap.isEmpty()) {
                     Iterator delimiterItr = delimitedMap.values().iterator();
                     while(delimiterItr.hasNext()) {
                         HashMap map1 = new HashMap();
                         String strRef = (String)delimiterItr.next();
                         if(FNMap==null) {
                             map1.put(DomainRelationship.SELECT_FIND_NUMBER,dummyMap.get(DomainRelationship.SELECT_FIND_NUMBER));
                         }else {
                             map1.put(DomainRelationship.SELECT_FIND_NUMBER,""+FNMap.get(getPrefix(strRef)));
                         }

                         if(CLMap==null) {
                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION));
                         }else {
                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION,""+CLMap.get(getPrefix(strRef)));
                         }

                         if(USGMap==null) {
                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,dummyMap.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE));
                         }else {
                             map1.put(DomainRelationship.SELECT_ATTRIBUTE_USAGE,""+USGMap.get(getPrefix(strRef)));
                         }

                         map1.put(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR,strRef);
                         map1.put(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY,""+getRangeQuantity(context,strRef));
                         map1.put(SELECT_ID,dummyMap.get(SELECT_ID));
                         map1.put(DomainRelationship.SELECT_RELATIONSHIP_ID,dummyMap.get(DomainRelationship.SELECT_RELATIONSHIP_ID));
                         map1.put(KEY_RELATIONSHIP,dummyMap.get(KEY_RELATIONSHIP));
                         map1.put(KEY_LEVEL,dummyMap.get(KEY_LEVEL));
                         map1.put(SELECT_RELATIONSHIP_TYPE,dummyMap.get(SELECT_RELATIONSHIP_TYPE));
                         map1.put(SELECT_CURRENT,dummyMap.get(SELECT_CURRENT));
                         map1.put(SELECT_STATUS,dummyMap.get(SELECT_STATUS));
                         map1.put(KEY_AVLData,dummyMap.get(KEY_AVLData));

                         finalList.add(map1);
                     }
                 }
           }
           //375650: reverting changes of 369074 for reports
           //below code required for--044467 and 371814
           if ("rangeRollUpFormat".equalsIgnoreCase(reportFormat)){
           HashMap hmTemp = new HashMap();
           hmTemp.put("expandMultiLevelsJPO","true");
           finalList.add(hmTemp);
           }
           return finalList;
    }

    /**
     * This method returns the Reference Designator after manipulating the ebom list based on Reference designator format
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns StringList contains the Reference Designator values
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    public StringList getReferenceDesignator (Context context, String[] args)
    throws Exception
{
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)paramMap.get("objectList");
	 //Added for Bug 354293
    HashMap params = (HashMap)paramMap.get("paramList");
    String format = "";
    if(params.get("repFormat")!=null){
        format = (String)params.get("repFormat");
    }
	if ("".equals(format) && params.get("selectedFilter")!=null){
        format = (String) params.get("selectedFilter");
        format = format.replaceAll("^.*:", "").trim();
    }
	//End of 354293
    StringList columnVals = new StringList();
    Iterator i = objList.iterator();
    while (i.hasNext())
    {
        Map m = (Map) i.next();
        //Added for Bug 354293
        String refDesExpFormat = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        //End of 354293
        String refDes = "";
        String strRelId = (String) m.get(DomainConstants.SELECT_RELATIONSHIP_ID);
      if (strRelId != null && strRelId.length() > 0)
      {
            try
            {
                DomainRelationship doRel = new DomainRelationship(strRelId);
                refDes = doRel.getAttributeValue(context, DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
            }
           catch (Exception excep)
           {
               refDes = "";
           }
       }
	   //Added for Bug 354293
      if(format.equalsIgnoreCase("expandedFormat")
              || format.equalsIgnoreCase("delimitedRollUpFormat")
              || format.equalsIgnoreCase("rangeRollUpFormat")
              || format.equalsIgnoreCase("getExpandedEBOM")
              || format.equalsIgnoreCase("getDelimitedRollupEBOM")
              || format.equalsIgnoreCase("getRangeRollupEBOM"))

      {
          columnVals.addElement(refDesExpFormat);
      }else{
		  //End of 354293
          columnVals.addElement(refDes);
      }
    }
    return columnVals;
}

    /**
     * This method returns the Component Location after manipulating the ebom list based on Reference designator format
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns StringList contains the component location values
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    public StringList getComponentLocation (Context context, String[] args)
        throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)paramMap.get("objectList");
        StringList columnVals = new StringList();
        Iterator i = objList.iterator();
        while (i.hasNext())
        {
            Map m = (Map) i.next();
            String refDes = "";
            String strRelId = (String) m.get(DomainConstants.SELECT_RELATIONSHIP_ID);

            if (strRelId != null && strRelId.length() > 0)
            {
                try
                {
                    DomainRelationship doRel = new DomainRelationship(strRelId);
                    refDes = doRel.getAttributeValue(context, DomainConstants.ATTRIBUTE_COMPONENT_LOCATION);
                }
               catch (Exception excep)
               {
                   refDes = "";
               }
           }
            columnVals.addElement(refDes);
        }

        return columnVals;
    }

     /**
      * This method returns the Usage after manipulating the ebom list based on Reference designator format
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - objectId - parent object OID
      * @returns StringList contains the Usage values
      * @throws Exception if the operation fails
      * @since EC 10.6
      */
     public StringList getUsage (Context context, String[] args)
         throws Exception
     {
         HashMap paramMap = (HashMap)JPO.unpackArgs(args);
         MapList objList = (MapList)paramMap.get("objectList");
         StringList columnVals = new StringList();
         Iterator i = objList.iterator();
         while (i.hasNext())
         {
             Map m = (Map) i.next();
             String refDes = "";
          String strRelId = (String) m.get(DomainConstants.SELECT_RELATIONSHIP_ID);
            if (strRelId != null && strRelId.length() > 0)
            {
                try
                {
                    DomainRelationship doRel = new DomainRelationship(strRelId);
                    refDes = doRel.getAttributeValue(context, DomainConstants.ATTRIBUTE_USAGE);
                }
               catch (Exception excep)
               {
                   refDes = "";
               }
           }
             columnVals.addElement(refDes);
         }

         return columnVals;
    }

    /**
     * This method returns the Quantity after manipulating the ebom list based on Reference designator format
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns StringList contains the quantity values
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    public StringList getEBOMQuantity (Context context, String[] args)
        throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)paramMap.get("objectList");
        StringList columnVals = new StringList();
        Iterator i = objList.iterator();
        while (i.hasNext())
        {
            Map m = (Map) i.next();
            String qty = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY);
            columnVals.addElement(qty);
        }

        return columnVals;
    }

/**
 * This method groups the reference designators based on their prefixes
 * @param String the eMatrix <code>Context</code> object
 * @param args holds the following input arguments:
 *        0 - objectId - parent object OID
 * @returns Map returns the map which contians values as grouped reference designators and keys as their prefixes
 * @throws Exception if the operation fails
 * @since EC 10.6
 */
    public Map getDelimitedMap(Context context,String expandedList) throws Exception {
    	//Multitenant
    	String delimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.DelimitedReferenceDesignatorSeparator");
        expandedList = expandedList.substring(1,expandedList.length()-1);
        HashMap map = new HashMap();
        StringTokenizer st = new StringTokenizer(expandedList,delimiter+" ");

        while(st.hasMoreElements()) {
            String token = (String)st.nextElement();
            String prefix = getPrefix(token);
            ArrayList al = (ArrayList)map.get(prefix);
            if(al==null) {
                al = new ArrayList();
            }
            al.add(token);
            map.put(prefix,al);
        }
        return map;
    }

   /**
     * This method checks whether the token is well formed against the RD rules and starts with the given prefix
     * @param String the Reference Designator token
     * @param String expected prefix
     * @returns boolean returns true if RD is well formed otherwise false
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    public boolean isRDTokenWellFormed(Context context,String token, String pref) throws Exception {
    	//Multitenant
    	String rangeDelimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.RangeReferenceDesignatorSeparator");
        boolean wellFormed = false;

        if(token.length() == 0) {
            return wellFormed;
        }
        if( token.indexOf(rangeDelimiter)!= -1 ){
            return isRangeWellFormed(context,token,pref);
        }
        return isRDWellPrefixedAndSuffixed(token,pref);
    }

    /**
     * This method checks whether the token is well formed against the RD rules and starts with the given token
     * @param String the Reference Designator token
     * @param String expected prefix
     * @returns boolean returns true if RD is well formed otherwise false
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    public boolean isRDWellPrefixedAndSuffixed(String token, String pref) {
        boolean wellFormed = true;
        String tokenPrefix = "";
        for(int i=0; i<token.length(); i++) {
            int chr = (int)token.charAt(i);
            if( (chr>=65 && chr<=90) || (chr>=97 && chr<=122) ) {
                continue;
            } else {
                tokenPrefix = token.substring(0,i);
                try {
                    // Added for the bug 311817 Begin
                    Integer.parseInt(token.substring(i));
                    // Added for the bug 311817 End
                }catch(Exception e){
                    wellFormed = false;
                    break;
                }
            }
        }
        if(wellFormed && !"%#%".equals(pref) ) {
        // Added for the bug 311817 Begin
            if( (tokenPrefix.substring(0,pref.length())).equalsIgnoreCase(pref)) {
        // Added for the bug 311817 End
                wellFormed = true;
            } else {
                wellFormed = false;
            }
        }
        return wellFormed;
    }

    /**
     * This method checks whether the token is well formed against the RD rules.
     * @param String the Reference Designator token
     * @returns boolean returns true if RD is well formed otherwise false
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    public boolean isRDWellFormed(String token) throws Exception {
        boolean wellFormed = true;
        for(int i=0; i<token.length(); i++) {
            int chr = (int)token.charAt(i);
            if( (chr>=65 && chr<=90) || (chr>=97 && chr<=122) ) {
                continue;
            } else {
                token = token.substring(i);
                try {
                    Integer.parseInt(token);
                }catch(Exception e){
                    wellFormed = false;
                    break;
                }
            }
        }
        return wellFormed;
    }

	/**
     * This method checks whether the given range is well formed against the RD rules and starts with the given token
     * @param String the Reference Designator token
     * @param String expected prefix
     * @returns boolean returns true if RD is well formed otherwise false
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    public boolean isRangeWellFormed(Context context, String token, String pref) throws Exception {
        boolean wellFormed = false;
		//Multitenant
        String rangeDelimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.RangeReferenceDesignatorSeparator");

        int indexPos = token.indexOf(rangeDelimiter);
        if( token.indexOf(rangeDelimiter) != token.lastIndexOf(rangeDelimiter) ){
            return wellFormed;
        }
        String firstToken = token.substring(0,indexPos);
        String lastToken = token.substring(indexPos+1);
        int startNum = 0;
        int lastNum = 0;
        String firstPrefix = "";
        String lastPrefix = "";
        if(isRDWellPrefixedAndSuffixed(firstToken,"%#%")) {
            if(isRDWellPrefixedAndSuffixed(lastToken,"%#%")) {
                firstPrefix = getPrefix(firstToken);
                lastPrefix = getPrefix(lastToken);
                startNum = getSuffix(firstToken);
                lastNum = getSuffix(lastToken);
                if(lastNum - startNum > 0 && firstPrefix.equalsIgnoreCase(lastPrefix)) {
                    wellFormed = true;
                }
            }
        }
        if(wellFormed && !"%#%".equals(pref) ) {
            if( firstPrefix.equalsIgnoreCase(pref)) {
                wellFormed = true;
            } else {
                wellFormed = false;
            }
        }
        return wellFormed;
    }

    /**
     * This method returns the suffix of well formed RD
     * @param String the Reference Designator token
     * @returns int returns the suffix
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    public int getSuffix(String token) {
        int suffix = 0;
        for(int i=0; i<token.length(); i++) {
            int chr = (int)token.charAt(i);
            if( (chr>=65 && chr<=90) || (chr>=97 && chr<=122) ) {
                continue;
            } else {
                token = token.substring(i);
                try {
                    suffix = Integer.parseInt(token);
                    break;
                }catch(Exception e){
                    break;
                }
            }
        }
        return suffix;
    }

   /**
      * This method returns the prefix of well formed RD
      * @param String the Reference Designator token
      * @returns String the prefix
      * @throws Exception if the operation fails
      * @since EC 10.6
      */
     public String getPrefix(String token) {
         String prefix = "";
         for(int i=0; i<token.length(); i++) {
             int chr = (int)token.charAt(i);
             if( (chr>=65 && chr<=90) || (chr>=97 && chr<=122) ) {
                 continue;
             } else {
                 prefix = token.substring(0,i);
                 break;
             }
         }
         return prefix;
     }

    /**
      * This method counts the total number of RDs in the give list
      * @param StringList List of RDs in different formats
      * @returns int count of RDs
      * @throws Exception if the operation fails
      * @since EC 10.6
      */
     public int getCount(Context context, StringList rdList) throws Exception {
    	//Multitenant
    	 String rangeDelimiter =EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.RangeReferenceDesignatorSeparator");
         int count = 0;
         //changed the name of variable to enumElement for JDK1.5 issue
         Enumeration enumElement = rdList.elements();
         while(enumElement.hasMoreElements()) {
             String token = (String)enumElement.nextElement();
             int indexPos = token.indexOf(rangeDelimiter);
             if( indexPos != -1 ){
                 String firstToken = token.substring(0,indexPos);
                 String lastToken = token.substring(indexPos+1);
                 int startNum = getSuffix(firstToken);
                 int lastNum = getSuffix(lastToken);
                 count += lastNum - startNum +1;
             } else {
                 count++;
             }
         }
         return count;
    }

    /**
      * This method expands the give RD List of all formats.
      * @param StringList List of RDs in different formats
      * @returns ArrayList list of all RDs in expanded form
      * @throws Exception if the operation fails
      * @since EC 10.6
      */
     public ArrayList getRDExpandList(Context context, StringList rdList) throws Exception {
    	 //Multitenant
    	 String rangeDelimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.RangeReferenceDesignatorSeparator");
         ArrayList rdExpandedList = new ArrayList();
         //changed the name of veriable to enumElement for JDK1.5 issue
         Enumeration enumElement = rdList.elements();
         while(enumElement.hasMoreElements()) {
             String token = (String)enumElement.nextElement();
             int indexPos = token.indexOf(rangeDelimiter);
             if( indexPos != -1 ){
                 String firstToken = token.substring(0,indexPos);
                 String lastToken = token.substring(indexPos+1);
                 String prefix = getPrefix(firstToken);
                 int startNum = getSuffix(firstToken);
                 int lastNum = getSuffix(lastToken);
                     for(int tempNum=startNum; tempNum<=lastNum; ) {
                         rdExpandedList.add(prefix+tempNum);
                         tempNum++;
                     }
             } else {
                 rdExpandedList.add(token);
             }
         }
         return rdExpandedList;
    }

     /**
      * This method puts the RDs in a map based on their prefixes
      * @param String Reference Designator values in different formats
      * @returns HashMap Map containing prefixes as keys and values as suffixes
      * @throws Exception if the operation fails
      * @since EC 10.6
      */
     public HashMap getRangeMap(Context context,String s) throws Exception {
    	 //Multitenant
         String delimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.DelimitedReferenceDesignatorSeparator");
         String rangeDelimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.RangeReferenceDesignatorSeparator");
         s = s.substring(1,s.length()-1);
         ArrayList al = new ArrayList();
         StringTokenizer st = new StringTokenizer(s, delimiter+" ");
         while(st.hasMoreTokens()) {
             al.add((String)st.nextToken());
         }

         Iterator itr = al.iterator();
         String prefix = "";
         int suffix = -1;
         String previousElement = null;
         String nextElement = null;
         String endElement = null;

         String finalStr = "";
         String element = (String)itr.next();
         boolean added = false;
         if(!itr.hasNext()){
             finalStr = element;
         }else{
             int count = 0;
             previousElement = element;
             endElement = element;
             while(itr.hasNext()) {
                 prefix = getPrefix(element);
                 suffix = getSuffix(element);
                 nextElement = prefix+(suffix+1);
                 element = (String)itr.next();

                 if(nextElement.equals(element)){
                     endElement = element;
                     count++;
                     if(!itr.hasNext()){
                         added = true;
                         if(finalStr.length()>0)
                             finalStr += delimiter;
                         if(count == 0){
                             finalStr += previousElement;
                         }else{

                             finalStr += previousElement+rangeDelimiter+endElement;
                         }
                         break;
                     }else
                         continue;
                  }else{
                     if(finalStr.length()>0)
                          finalStr += delimiter;
                     if(count == 0){
                         finalStr += previousElement;
                     }else{
                         finalStr += previousElement+rangeDelimiter+endElement;
                     }

                     count=0;
                     previousElement = element;
                     endElement = element;
                 }
                 if(!added && !itr.hasNext()){
                     if(finalStr.length()>0)
                         finalStr += delimiter;
                     if(count == 0){
                         finalStr += previousElement;
                     }else{
                         finalStr += previousElement+rangeDelimiter+endElement;
                     }
                 }
             }
         }

         HashMap finalMap = new HashMap();

         StringList sl = FrameworkUtil.split(finalStr, delimiter);
         itr = sl.iterator();
         String previousPrefix = "";
         String currentPrefix = "";
         String finalElement = "";
         previousElement = "";
         String currentElement = "";
         while(itr.hasNext()) {
             currentElement = element = (String)itr.next();
             if(element.indexOf(rangeDelimiter)!=-1) {
                 element = element.substring(0,element.indexOf(rangeDelimiter));
                 currentPrefix = getPrefix(element);
             } else {
                 currentPrefix = getPrefix(currentElement);
             }
             if(!previousPrefix.equalsIgnoreCase(currentPrefix)) {   //previousPrefix != "" &&
                 finalElement = currentElement;
                 previousPrefix = currentPrefix;
             } else {
                 finalElement += delimiter+" "+currentElement;
             }
             finalMap.put(currentPrefix,finalElement);
         }
         return finalMap;
     }

    /**
      * This method counts the RDs in the given String
      * @param String Reference Designator values in different formats
      * @returns int the total number of RDs
      * @throws Exception if the operation fails
      * @since EC 10.6
      */
     private float getRangeQuantity(Context context,String strRef) throws Exception {
    	 //Multitenant
    	 String delimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.DelimitedReferenceDesignatorSeparator");
         String rangeDelimiter = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.RangeReferenceDesignatorSeparator");

         StringTokenizer st = new StringTokenizer(strRef,delimiter+" ");
         String token = "";
         int count = 0;
         String fritstPart = "";
         String secondPart = "";
         int startIndex = 0;
         int endIndex = 0;
         int rangeIndex = -1;

         while(st.hasMoreTokens()) {
             token = (String)st.nextToken();
             rangeIndex = token.indexOf(rangeDelimiter);
             if(rangeIndex!= -1) {
                 fritstPart = token.substring(0,rangeIndex);
                 secondPart = token.substring(rangeIndex+1);
                 startIndex = getSuffix(fritstPart);
                 endIndex = getSuffix(secondPart);
                 count +=  (endIndex-startIndex+1);
             } else {
                 count++;
             }
         }
           return (new Float(count)).floatValue();
     }

   /**
     * This method returns the edit coulmn of part EBOM Page
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds param arguments.
     * @throws Exception if the operation fails.
     * @since EC 10.6
     */
    public Vector getEditColumnDisplay(Context context,
                              String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)programMap.get("objectList");
        HashMap paramList = (HashMap)programMap.get("paramList");
        String parentId = (String)paramList.get("objectId");
//Added for IR-021267
        DomainObject dmParentPart = DomainObject.newInstance(context);
        Vector columnVals = new Vector(objList.size());
        Iterator i = objList.iterator();
        String selectedFilter = (String)paramList.get("selectedFilter");
        String selectedProgram = (String)paramList.get("selectedProgram");
//Added for IR-021267
//fix for bug 311050
         //check the parent obj state
          boolean allowChanges = true;
          StringList strList  = new StringList(2);
          strList.add(SELECT_CURRENT);
          strList.add("policy");

           DomainObject domObj = new DomainObject(parentId);

          Map map = domObj.getInfo(context,strList);

          String state = (String)map.get(SELECT_CURRENT);
          String objPolicy = (String)map.get("policy");

          String propAllowLevel = (String)EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Part.RestrictPartModification");
          StringList propAllowLevelList = new StringList();

          if(propAllowLevel != null || !("".equals(propAllowLevel)) || !("null".equals(propAllowLevel)))
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }
          }
          allowChanges = (!propAllowLevelList.contains(state));

//end of fix for bug 311050
        boolean isPrinterFriendly = false;
        String printerFriendly = (String)paramList.get("reportFormat");
//Added for IR-021267
        dmParentPart.setId(parentId);

        if ( printerFriendly != null )
        {
            isPrinterFriendly = true;
        }
          while (i.hasNext())
            {
                Map m = (Map) i.next();

                String id = (String)m.get(SELECT_ID);
                String relId = (String)m.get("id[connection]");

//Added for IR-021267
                //removed allowchange condition for bug 308466. the check will be done by access function only.
                if(selectedFilter!=null)
                {
                    if(selectedFilter!=null && selectedFilter.equals("emxPart:getEBOMsWithRelSelectables") && selectedProgram==null )
                    {
                        if(!isPrinterFriendly)
                        {
                          columnVals.addElement("<a href=\"javascript:emxTableColumnLinkClick('../engineeringcentral/emxpartEditEBOMDialogFS.jsp?emxSuiteDirectory=engineeringcentral&amp;relId="+relId+"&amp;suiteKey=EngineeringCentral&amp;parentOID="+parentId+"&amp;objectId="+id+"', '700', '600', 'true', 'popup', '')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/></a>");
                        }
                        else
                        {
                            columnVals.addElement("<img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/>");
                        }
                    }
                    else
                    {
                         columnVals.addElement("&#160;");
                    }
                }
                else
                {
                    if((selectedProgram==null && allowChanges) || (selectedProgram!=null && selectedProgram.equals("emxPart:getStoredEBOM")&& allowChanges ) )
                    {
                        if(!isPrinterFriendly)
                        {
                          columnVals.addElement("<a href=\"javascript:emxTableColumnLinkClick('../engineeringcentral/emxpartEditEBOMDialogFS.jsp?emxSuiteDirectory=engineeringcentral&amp;relId="+relId+"&amp;suiteKey=EngineeringCentral&amp;parentOID="+parentId+"&amp;objectId="+id+"', '700', '600', 'true', 'popup', '')\"><img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/></a>");
                        }
                        else
                        {
                            columnVals.addElement("<img border=\"0\" src=\"images/iconActionEdit.gif\" alt=\"\"/>");
                        }
                    }
                    else
                    {
                         columnVals.addElement("&#160;");
                    }
                }
            }
        return columnVals;
    }

	/**
      * This class extends the comparator class and implements the compare method.
      * If prefixes are equal it performs the integer comparision on suffixes.
      * If prefixes are not equal then it compares the length of the prefixes.
      * returns -1 if first object and second objects in ascending order else 1.
      * This method is called when both objects prefixes starts with the same letter.
      * @since EC 10.6
      */
     class Compare implements Comparator {
         public int compare(Object first, Object second) {
             String obj1 = first.toString();
             String obj2 = second.toString();
             String prefix1 = getPrefix(obj1);
             String prefix2 = getPrefix(obj2);
             if(prefix1.equals(prefix2)) {
                 int suffix1 = getSuffix(obj1);
                 int suffix2 = getSuffix(obj2);
                 if(suffix1<suffix2) {
                     return -1;
                 } else {
                     return 1;
                 }
             } else {
                 if(prefix1.length() < prefix2.length()) {
                     return -1;
                 } else {
                     return 1;
                 }
             }
         }
     }   //end of Compare class

    /**
     * This method enables or disables the checkbox in the EBOM Summary Page.If Selected Filter is not Stored,
     * checkbox will be disabled.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments as it is getting the params list,objects list from the Config UI table
     * @returns Vector of "true/false" values for each row
     * @throws Exception if the operation fails
     * @since Common 10.5
     */
    public Vector showBOMSummaryCheckBox(Context context, String args[]) throws Exception
    {
        Vector columnVals=null;
        try
        {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            HashMap paramList = (HashMap)programMap.get("paramList");
            columnVals = new Vector(objList.size());
            Iterator i = objList.iterator();
            String selectedFilter = (String)paramList.get("selectedFilter");
            while(i.hasNext())
            {
                i.next();
                if(selectedFilter!=null && selectedFilter.equals("emxPart:getEBOMsWithRelSelectables"))
                {
                    columnVals.add("true");
                }
                else
                {
                    columnVals.add("false");
                }
            }
        }
        catch(Exception e)
        {
        }

     return columnVals;
    }

/* This method returns a boolean value .
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds param arguments.
 * @return String true or false.
 *  @throws Exception if the operation fails.
 * @since EC 10-6.
*/
public Boolean isEBOMUnique(Context context, String[] args) throws Exception
{
        MapList selBOMsList = null;
        StringList selectRelStmts = null;
        SelectList resultSelects = new SelectList(8);
        String strRd ="";
        Iterator itr1 = null;
        String key;
        String attrName;
        String refvalue=null;
        String attrRef = DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR;
        StringList RefList=new StringList();
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
        String objectId = args[0];
        String RefDesValue = args[1];
        RefList = FrameworkUtil.split(RefDesValue, ",");
        ArrayList correctList = new ArrayList();
        StringList RdRange=new StringList();
        ArrayList rdArray=new ArrayList();
        boolean isValid = true;
        Iterator refitr = RefList.iterator();
        while (refitr.hasNext())
        {
            refvalue = (String)refitr.next();
            if(refvalue.indexOf('-')!=-1)
            {
                RdRange.addElement(refvalue);
            }else
            {
                isValid= isRDWellFormed(refvalue);
                if(isValid)
                {
                    correctList.add(refvalue);
                    isValid=false;
                }
            } // else
        }
        if(RdRange!=null)
        {
            rdArray = getRDExpandList(context,RdRange);
        }
        rdArray.addAll(correctList);
        resultSelects.add(DomainObject.SELECT_ID);
        resultSelects.add(DomainObject.SELECT_TYPE);
        resultSelects.add(DomainObject.SELECT_NAME);
        resultSelects.add(DomainObject.SELECT_REVISION);
        resultSelects.add(DomainObject.SELECT_DESCRIPTION);
        resultSelects.add(DomainObject.SELECT_CURRENT);
        resultSelects.add(DomainObject.SELECT_OWNER);
        resultSelects.add(DomainObject.SELECT_TO_TYPE);
        resultSelects.add(DomainObject.SELECT_FROM_TYPE);
        resultSelects.addElement("policy");
        Part selPartObj = (Part)DomainObject.newInstance(context,DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
        selPartObj.setId(objectId);
        Map attrMap1 = DomainRelationship.getTypeAttributes(context,DomainConstants.RELATIONSHIP_EBOM);


    // Included this condition to avoid Null Pointer exception
        if(attrMap1 != null)
        {
            selectRelStmts = new StringList(attrMap1.size()+1);
            itr1 = attrMap1.keySet().iterator();
            while(itr1.hasNext())
            {
                key = (String) itr1.next();
                Map mapinfo = (Map) attrMap1.get(key);
                attrName = (String)mapinfo.get("name");
                selectRelStmts.addElement("attribute["+attrName+"]");
            }
        }
        else
        {
            selectRelStmts = new StringList(1);
        }
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
        selBOMsList = selPartObj.getEBOMs(context, resultSelects, selectRelStmts, false);
        Iterator mapItr =selBOMsList.iterator();
        int ctr=0;
        StringList RefRangeValue = new StringList();
        StringList ref = new StringList();
        ArrayList ebomArray =new ArrayList();
        String refebomvalue=null;
        while(mapItr.hasNext())
        {
            ctr++;
            Map BOMMap = (Map)mapItr.next();
            String att="attribute["+attrRef+"]";
            String refDes = (String)BOMMap.get(att);

            if(refDes.indexOf(',')!=-1)
            {
                ref=FrameworkUtil.split(refDes,",");


                Iterator refebomitr = ref.iterator();
            while (refebomitr.hasNext())
            {
             refebomvalue = (String)refebomitr.next();
             if(refebomvalue.indexOf('-')!=-1)
            {
                RefRangeValue.addElement(refebomvalue);
            }
            else
            {
              if(ctr==1)
              {
                 strRd=refebomvalue;
              }
              else
              {
                strRd=strRd + "," + refebomvalue;
              }
             }
            }
             }
         else if(refDes.indexOf('-')!=-1)
         {
           RefRangeValue.addElement(refDes);
         }
         else
         {
           ebomArray.add(refDes);

         }
        }  // while

        if (RefRangeValue !=null && RefRangeValue.size()>0)
        {
            ebomArray.addAll(getRDExpandList(context,RefRangeValue));
        }

        StringList splitstr=new StringList();
        splitstr=FrameworkUtil.split(strRd,",");

        ebomArray.addAll(splitstr);
        boolean isRDUnique=true;
        String rval=null;

        for(int i=0; i<rdArray.size(); i++)
        {
            rval=(String)rdArray.get(i);
            if(ebomArray.contains(rval)) {
                isRDUnique=false;
                break;
            }
        }

        return Boolean.valueOf(isRDUnique);
}

/** This method is called from Change Position command of EBOM Actions toolbar.
 * @param context ematrix context
 * @param args packed args
 * @return MapList containing EBOM data of 1 level.
 * @throws Exception if any operation fails.
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getEBOMDataForChangePosition(Context context, String[] args) throws Exception {
	HashMap paramMap = (HashMap) JPO.unpackArgs(args);

	String parentId   = getStringValue(paramMap, "objectId");
	String selectedId = getStringValue(paramMap, "tableRowId");

	MapList ebomList = new MapList();

	if (isValidData(parentId) && isValidData(selectedId)) {
		selectedId = (String) FrameworkUtil.split(selectedId, "|").get(1);

		DomainObject domObj = DomainObject.newInstance(context, parentId);

		String objectWhere = SELECT_ID + " != " + selectedId;

		ebomList = domObj.getRelatedObjects(context, RELATIONSHIP_EBOM, TYPE_PART, new StringList(SELECT_ID),
				new StringList(SELECT_RELATIONSHIP_ID), false, true, (short) 1, objectWhere, null, null, null, null);
	}

	return ebomList;
}

/**
 * Gets the EBOM Summary information with EBOM relationship selectables.
 * @param context the eMatrix <code>Context</code> object.
 * @param args contains a packed HashMap with the following entries:
 * objectId - a String containing the Part id.
 * reportType - a String either BOM or AVL.
 * location - a String containing the Location id, only needed for AVL report.
 * @return MapList of EBOM object ids and EBOM relationship ids with selectables.
 * @throws Exception if the operation fails.
 * @since AEF 10.6.
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getEBOMsWithRelSelectables (Context context, String[] args)
    throws Exception
{
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		int nExpandLevel = 0;
		String sExpandLevels = (String)paramMap.get("emxExpandFilter");
		if(sExpandLevels == null || sExpandLevels.equals("null") || sExpandLevels.equals("")) {
			sExpandLevels = (String)paramMap.get("ExpandFilter");
		}
         StringList selectStmts = new StringList(1);
         StringBuffer sObjWhereCond = new StringBuffer();
	if(sExpandLevels==null || sExpandLevels.length()==0)
	{
		nExpandLevel = 1;
	}
		else
	{
			if("All".equalsIgnoreCase(sExpandLevels))
            nExpandLevel = 0;
             else if (sExpandLevels != null && sExpandLevels.equalsIgnoreCase("EndItem"))
                {
                 nExpandLevel=0;
                    if(sObjWhereCond.length()>0){
                        sObjWhereCond.append(" && ");
                    }
                     sObjWhereCond.append("(" + EngineeringConstants.SELECT_END_ITEM + " == '"
                            + EngineeringConstants.STR_NO + "')");
                    selectStmts.addElement("from["+EngineeringConstants.RELATIONSHIP_EBOM+"].to.attribute[End Item]");
                    selectStmts.addElement("from["+EngineeringConstants.RELATIONSHIP_EBOM+"].id");
                }

            else
            nExpandLevel = Integer.parseInt(sExpandLevels);
	}

    String partId = (String) paramMap.get("objectId");

    String strSide = (String) paramMap.get("side");
    String strConsolidatedReport=(String)paramMap.get("isConsolidatedReport");
    MapList ebomList = new MapList();

    // reportType can be either BOM or AVL. Depending on this value Location Name is set.
    String reportType ="";
   // location variable holds the value of Location Name
    String location = "";
//Added for IR-021267

        /*
        * StringList to store the selects on the Domain Object
        */

        /*
        * StringList to store the selects on the relationship
        */
        StringList selectRelStmts = new StringList(6);

        /*
         * String buffer to prepare where condition with  End Item value
        */

        /*
        * stores the location ID
        */
        String locationId = null;
        /*
        * Maplist holds the data from the getCorporateMEPData method
        */
        MapList tempList = null;
    // retrieve the selected reportType from the paramMap
    reportType = (String) paramMap.get("reportType");
    // retrieve the selected location by the user
    location = (String) paramMap.get("location");

    // Object Where Clause added for Revision Filter

    String complete = PropertyUtil.getSchemaProperty(context,"policy", DomainConstants.POLICY_DEVELOPMENT_PART, "state_Complete");
    String release = PropertyUtil.getSchemaProperty(context, "policy", DomainConstants.POLICY_EC_PART, "state_Release");
    String selectedFilterValue = (String) paramMap.get("ENCBOMRevisionCustomFilter");

    if (selectedFilterValue == null)
    {
        if (strSide != null)
        {

            selectedFilterValue = (String) paramMap.get(strSide+"RevOption");
            if (UIUtil.isNullOrEmpty(selectedFilterValue))
            {
            	strSide="left".equalsIgnoreCase(strSide)?"Left":"Right";
            	selectedFilterValue = (String) paramMap.get(strSide+"RevOption");
            }
        }
        if (selectedFilterValue == null)
        {
            selectedFilterValue = "As Stored";
        }

    }

    // To display AVL data for the first time with default Host Company of the user.
    try {
        Part partObj = new Part(partId);
        selectStmts.addElement(DomainConstants.SELECT_ID);
        selectStmts.addElement(DomainConstants.SELECT_TYPE);
        selectStmts.addElement(DomainConstants.SELECT_NAME);
        selectStmts.addElement(DomainConstants.SELECT_REVISION);
        selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
        
        String selectPrdPhysicalId = "from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"].to["+EngineeringConstants.TYPE_VPLM_VPMREFERENCE+"].physicalid";
        String sVPLMInstanceRelId ="frommid["+EngineeringConstants.RELATIONSHIP_VPM_PROJECTION_RELID+"].torel.physicalid";
        
			//Added for hasChildren
			selectStmts.addElement("from["+DomainConstants.RELATIONSHIP_EBOM+"]");
        // Added for MCC EC Interoperability Feature
        String strAttrEnableCompliance  =PropertyUtil.getSchemaProperty(context,"attribute_EnableCompliance");
        selectStmts.addElement("attribute["+strAttrEnableCompliance+"]");
        //end
        
        selectStmts.addElement(selectPrdPhysicalId);
        selectRelStmts.addElement(sVPLMInstanceRelId); 
        
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_QUANTITY);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_FIND_NUMBER);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_USAGE);
	    int level = nExpandLevel;

		MapList tempEBOMList = new MapList();
	    MapList tempMapList ;
		   
		if(!"Latest".equals(selectedFilterValue)){
			ebomList = partObj.getRelatedObjects(context,
                                         DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
                                         DomainConstants.TYPE_PART,                  // object pattern
                                         selectStmts,                 // object selects
                                         selectRelStmts,              // relationship selects
                                         false,                        // to direction
                                         true,                       // from direction
                                         (short)level,                    // recursion level
                                         sObjWhereCond.toString(),     // object where clause
                                          null);                       // relationship where clause
		}
        else if("Latest".equals(selectedFilterValue)){
        	selectStmts.addElement("last.id");
        	ebomList = partObj.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
                    DomainConstants.TYPE_PART,                  // object pattern
                    selectStmts,                 // object selects
                    selectRelStmts,              // relationship selects
                    false,                        // to direction
                    true,                       // from direction
                    (short)1,                    // recursion level
                    sObjWhereCond.toString(),     // object where clause
                     null);                       // relationship where clause
        	      	
        	Iterator itr = ebomList.iterator();
        	while(itr.hasNext()) {
                Map newMap = (Map)itr.next();
                String ObjectId = (String)newMap.get(DomainConstants.SELECT_ID);
                String lastRevId = (String)newMap.get("last.id");
              
                if(!(ObjectId.equals(lastRevId))){                	
                	newMap.put("id",lastRevId);                	
                }
                tempEBOMList.add(newMap);
                tempMapList =new MapList();
               //EBOM exist can't use as current revison does not have child but later revision may have.as we r expanding on current revision so that check is block later revision to expand.
              //for expand "All" 
                if(level==0)
        	        //expand this object and iterate each object
                	tempMapList= expandToOnelevel(context,lastRevId,1,0,sObjWhereCond.toString());
                //For specific level expand
                if(level-1>0)
	            	//expand this object and iterate each object
	            	 tempMapList= expandToOnelevel(context,lastRevId,1,level-1,sObjWhereCond.toString());
                if(tempMapList !=null && tempMapList.size()>0){
                	for (int i=0;i<tempMapList.size();i++)
                		tempEBOMList.add(tempMapList.get(i));
                } 	
           	}
        ebomList=tempEBOMList;
        }        


//      IR023752    start
        if("EndItem".equalsIgnoreCase(sExpandLevels) )
        {
            ebomList = getEndItemMapList(context, ebomList, partObj, selectStmts, selectRelStmts);
        }

        //IR023752 end

            if("Yes".equalsIgnoreCase(strConsolidatedReport))
            {
                MapList newBOMList = new MapList();
                getFlattenedMapList(ebomList,newBOMList,null);
                ebomList = newBOMList;
            } else {
            	if(UIUtil.isNotNullAndNotEmpty(reportType) && !reportType.equalsIgnoreCase("BOM")) {
            		MapList newBOMList = new MapList();
            		getFlattenedMapList(ebomList,newBOMList,reportType);
            		ebomList = newBOMList;
            	}
            }
        // Below code get the last revision of a domain object even if it is not connected to EBOM
       int ebomSize = ebomList.size();
            //  -Modified for the fix IR-013085

       //if((ebomList!=null && ebomSize>0 && "Latest".equals(selectedFilterValue))||("Latest Complete".equals(selectedFilterValue) || ("Latest Release".equals(selectedFilterValue)))) {
       if((ebomList!=null && ebomSize>0)||("Latest Complete".equals(selectedFilterValue) || ("Latest Release".equals(selectedFilterValue)))) {
               Iterator itr = ebomList.iterator();
               MapList LRev = new MapList();
               String objID = "";
               //Iterate through the maplist and add those parts that are latest but not connected
         while(itr.hasNext()) {
                   Map newMap = (Map)itr.next();
                   String ObjectId = (String)newMap.get("id");
           String oldRev = (String)newMap.get("revision");
                   DomainObject domObj = DomainObject.newInstance(context,ObjectId);
           // get the last revision of the object
                   BusinessObject bo = domObj.getLastRevision(context);
                   bo.open(context);
                   objID = bo.getObjectId();
           String newRev = bo.getRevision();
                        //Modifed for the IR-013085
                        bo.close(context);
                        /*if ("Latest".equals(selectedFilterValue))
                        {
                            if (!oldRev.equals(newRev))
                            {
                                newMap.put("id",objID);
                            }
                        }*/
                        //Added for the IR-013085
                        //else if("Latest Complete".equals(selectedFilterValue) || "Latest Release".equals(selectedFilterValue))
                        if("Latest Complete".equals(selectedFilterValue) || "Latest Release".equals(selectedFilterValue))
                        {
                            DomainObject domObjLatest = DomainObject.newInstance(context,objID);
                            String currSta = domObjLatest.getInfo(context,DomainConstants.SELECT_CURRENT);
                            //Added for the IR-026773
                            if (oldRev.equals(newRev))
                            {
                                if (!complete.equals(currSta) || !release.equals(currSta))
                                    continue;
                                newMap.put("id",objID);

                            }//IR-026773 ends
                            else
                            {
                            while (!currSta.equalsIgnoreCase(complete)&&!currSta.equals(complete) || !currSta.equalsIgnoreCase(release)&&!currSta.equals(release))
                            {
                              BusinessObject boObj = domObjLatest.getPreviousRevision(context);
                              boObj.open(context);
                              objID = boObj.getObjectId();
                              currSta = (String)(DomainObject.newInstance(context,objID).getInfo(context,DomainConstants.SELECT_CURRENT));
                              boObj.close(context);
                            }

                             newMap.put("id",objID);
                            }
                        }// IR-013085 ends
                        //Add new map to the HashMap
                        LRev.add (newMap);
                    }
                   ebomList.clear();
                   ebomList.addAll(LRev);
           }

        if (location!=null && ("").equals(location) && reportType!=null && reportType.equals("AVL"))
        {
           // retrieve the Host Company attached to the User.
             location =com.matrixone.apps.common.Person.getPerson(context).getCompany(context).getObjectId(context);
         }
         if (location!=null && reportType!=null && reportType.equals("AVL"))
         {
                tempList = new MapList();
                locationId = com.matrixone.apps.common.Person.getPerson(context).getCompany(context).getObjectId(context);
             if (locationId.equals(location))
            {
                // In case of Host Company
                tempList = partObj.getCorporateMEPData(context, ebomList, locationId, true, partId);
            }
            else {
                // In case of selected location and All locations
                tempList = partObj.getCorporateMEPData(context, ebomList, location, false, partId);
            }
            ebomList.clear();
            ebomList.addAll(tempList);
         }

// fix for bug 311050
         //check the parent obj state
          boolean allowChanges = true;
          StringList strList  = new StringList(2);
          strList.add(SELECT_CURRENT);
          strList.add("policy");

          Map map = partObj.getInfo(context,strList);

          String objState = (String)map.get(SELECT_CURRENT);
          String objPolicy = (String)map.get("policy");

          String propAllowLevel = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Part.RestrictPartModification");
          StringList propAllowLevelList = new StringList();

          if(propAllowLevel != null || !("".equals(propAllowLevel)) || !("null".equals(propAllowLevel)))
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }
          }
          allowChanges = (!propAllowLevelList.contains(objState));

       //set row editable option
       Iterator itr = ebomList.iterator();
        MapList tList = new MapList();
        while(itr.hasNext())
        {
            Map newMap = (Map)itr.next();
            if(allowChanges)
            {
            newMap.put("RowEditable", "show");
            }else
            {
            newMap.put("RowEditable", "readonly");
            }
            tList.add (newMap);
        }
        ebomList.clear();
        ebomList.addAll(tList);
//end of fix for bug 311050

// fix for bug 311050
         //check the parent obj state
          strList.add(SELECT_CURRENT);
          strList.add("policy");

          if(propAllowLevel != null || !("".equals(propAllowLevel)) || !("null".equals(propAllowLevel)))
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }
          }
          allowChanges = (!propAllowLevelList.contains(objState));

       //set row editable option
        while(itr.hasNext())
        {
            Map newMap = (Map)itr.next();
            if(allowChanges)
            {
            newMap.put("RowEditable", "show");
            }else
            {
            newMap.put("RowEditable", "readonly");
            }
            tList.add (newMap);
        }
        ebomList.clear();
        ebomList.addAll(tList);
//end of fix for bug 311050
        
    }

    catch (FrameworkException Ex) {
        throw Ex;
    }

    return ebomList;
}

	/**
    * This method is executed for 'Range Funtion' to populate
    * 'Revision Filter' with values like As Stored, Latest and Latest Complete.
      * @param context the eMatrix <code>Context</code> object.
      * @param args contains a packed HashMap
      * @return HashMap.
      * @throws Exception if the operation fails.
      */
      public HashMap getRevisionFilterOptions(Context context, String args[]) throws Exception {

          HashMap ViewMap      = new HashMap();
          HashMap programMap     = (HashMap) JPO.unpackArgs(args);
          HashMap requestMap     = (HashMap) programMap.get("requestMap");

          String partId = (String) requestMap.get("objectId");
          StringList slDisplayValue     = new StringList();
          StringList slActualValue  = new StringList();
          StringList slIterationValue   = new StringList();

          String sDisplayValue = "";
          String sActualValue = "";
          DomainObject dmPart = null;
          String sPolicy = null;
      try {
             dmPart = DomainObject.newInstance(context,partId);
             //sPolicy = dmPart.getInfo(context,DomainConstants.SELECT_POLICY);
             
             StringList strList  = new StringList(2);
             strList.add(SELECT_POLICY);
             strList.add(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);

             Map map = dmPart.getInfo(context,strList);
             sPolicy = (String)map.get(SELECT_POLICY);
             String sRelProcess = (String)map.get(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
             
             // Get the Revision Filter Options from Property
             String strBOMFilterProp = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Filter.RevisionOptions");
             slIterationValue = FrameworkUtil.split(strBOMFilterProp,"|");

             StringItr strItr = new StringItr(slIterationValue);

        while (strItr.next()) {
                sActualValue = (String) strItr.obj();

        // Display Fields
        if (!"Latest Complete".equals(sActualValue) || !"Latest Release".equals(sActualValue)) {
                   slActualValue.add(sActualValue);
                   sDisplayValue = sActualValue.replace(' ','_');
                   //Multitenant
                   sDisplayValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.RevisionFilterOption."+sDisplayValue);
                   slDisplayValue.add(sDisplayValue);
        } else {
        // Display "Latest Complete" option only when Policy equals Development Part
            if (sPolicy.equals(DomainConstants.POLICY_DEVELOPMENT_PART) || sRelProcess.equals(EngineeringConstants.DEVELOPMENT)) {
                      slActualValue.add(sActualValue);
                      sDisplayValue = sActualValue.replace(' ','_');
                    //Multitenant
                      sDisplayValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.RevisionFilterOption."+sDisplayValue);
                      slDisplayValue.add(sDisplayValue);
                   }
                }
             }

             ViewMap.put("field_choices", slActualValue);
             ViewMap.put("field_display_choices", slDisplayValue);

           } catch(Exception ex) {
             }

             return ViewMap;
          } // end of method: getRevisionFilterOptions

      /**
       * getBOMViewFilters,this method is executed to show the
    * BOM Views like Engineering, Common and Plant Specific Views.
       * @param context the eMatrix <code>Context</code> object.
       * @param args contains a packed HashMap
    * @author Garima
       * @return HashMap.
       * @throws Exception if the operation fails.
       */
    public HashMap getBOMViewFilters(Context context, String args[])
      throws Exception {
       HashMap ViewMap = new HashMap();
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       HashMap requestMap = (HashMap) programMap.get("requestMap");
       String sENCBOMCustomFilter = (String) requestMap.get("ENCBillOfMaterialsViewCustomFilter");
       StringList slBOMFilter = new StringList();
       StringList slBOMFilterProp = new StringList();//Added for 351866.1
       //Fixed  for 351866.1
       String strBOMFilterProp = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Filter.BOMViewOptions");
       slBOMFilterProp = FrameworkUtil.split(strBOMFilterProp,"|");
       for(int i=0;i<slBOMFilterProp.size();i++){
           String strBomFilter = (String)slBOMFilterProp.get(i);
           //Multitenant
           String strBom1Filter =  EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Filter.BOMFilterOptions."+strBomFilter);
           slBOMFilter.add(strBom1Filter);
        //End : 351866.1
       }
      //Fixed  for 351866.1
      StringList slBOMFilterVals = new StringList();
      slBOMFilterVals.add("Engineering");

       if(sENCBOMCustomFilter!=null) {
            int iIndex = slBOMFilterVals.indexOf(sENCBOMCustomFilter);
       Object objInd = slBOMFilter.elementAt(iIndex);
       slBOMFilter.setElementAt(slBOMFilter.firstElement(), iIndex);
       slBOMFilter.setElementAt(objInd, 0);

            Object objIndVal = slBOMFilterVals.elementAt(iIndex);
            slBOMFilterVals.setElementAt(slBOMFilterVals.firstElement(), iIndex);
            slBOMFilterVals.setElementAt(objIndVal, 0);
        }

       ViewMap.put("field_choices", slBOMFilterVals);//End:351866.1
       ViewMap.put("field_display_choices", slBOMFilter);
       return ViewMap;
       } // end of method: getBOMViewFilters

       /**
    * This method resequences all 1st level child parts of the BOM
            * @param context the eMatrix <code>Context</code> object
            * @param args holds the following input arguments:
    * @param args 0 - relId - relationship ID
    * @param args 1 - objectId - part object OID
    * @param args 2 - parentOID - parent object ID
            * @returns MapList EBOM list after resequencing Find numbers.
            * @throws Exception if the operation fails
            * @since version X+2
            */

    public MapList resequenceBOM(Context context, String[] args)
      throws Exception {

                                 HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String objectId = (String) paramMap.get("objectId");

      // Get the Initial and Increment Value of Find Numbers in a BOM
      String newFindNumberValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Resequence.InitialValue");
      String incrementValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Resequence.IncrementValue");
      Integer newValue = new Integer(newFindNumberValue);
      Integer incrValue = new Integer(incrementValue);

      MapList ebomList = null;

      try {
        StringList selectStmts = new StringList(1);
        selectStmts.addElement(DomainConstants.SELECT_ID);
        selectStmts.addElement(DomainConstants.SELECT_TYPE);
        selectStmts.addElement(DomainConstants.SELECT_NAME);
        selectStmts.addElement(DomainConstants.SELECT_REVISION);
        selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
        // Added for MCC EC Interoperability Feature
        String strAttrEnableCompliance  =PropertyUtil.getSchemaProperty(context,"attribute_EnableCompliance");
        selectStmts.addElement("attribute["+strAttrEnableCompliance+"]");
        //end
        StringList selectRelStmts = new StringList(6);
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
        selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_QUANTITY);
        selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
        selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_USAGE);

        DomainObject domObj = DomainObject.newInstance(context,objectId);
        ebomList = domObj.getRelatedObjects(context,
                                            DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
                                            DomainConstants.TYPE_PART,          // object pattern
                                            selectStmts,                 // object selects
                                            selectRelStmts,              // relationship selects
                                            false,                       // to direction
                                            true,                        // from direction
                                            (short)1,                    // recursion level
                                            null,                        // object where clause
                                            null);                       // relationship where clause
        ebomList.addSortKey("attribute[" + DomainConstants.ATTRIBUTE_FIND_NUMBER + "]", "ascending", "String");
        ebomList.sort();

        int mlSize = ebomList.size();

        if(ebomList!=null && mlSize>0) {
          Iterator itr = ebomList.iterator();
          while(itr.hasNext()) {
            Map newMap = (Map)itr.next();
            String connId = (String)newMap.get("id[connection]");
            DomainRelationship domRel = new DomainRelationship(connId);
            // update attribute Find Number with new Value
            domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER, newValue.toString());
            newValue = new Integer(newValue.intValue() + incrValue.intValue());
          }
        }
      } catch (Exception e) {
      }
      return ebomList;
    }

    /**
     * This method Change the Position of all 1st level selected child parts of the BOM up and down in BOM
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @param args 0 - relId - relationship ID
     * @param args 1 - objectId - part object OID
     * @param args 2 - parentOID - parent object ID
     * @param args 3 - radioPartObjectId - selected Part in Dialog Page
     * @returns MapList EBOM list after resequencing Find numbers.
     * @throws Exception if the operation fails
     * @since version X+2
     */

    public MapList changeBOMPartPosition(Context context, String[] args)
        throws Exception {

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
                                 String objectId = (String) paramMap.get("objectId");
                                 String parentOID = (String) paramMap.get("parentOID");
                                 String radioPartObjectId = (String) paramMap.get("radioPartObjectId");

        // Get the Initial and Increment Value of Find Numbers in a BOM
                                 String newFindNumberValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Resequence.InitialValue");
                                 String newFindNumberIncr = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Resequence.IncrementValue");
                                 Integer newValue = new Integer(newFindNumberValue);
                                 Integer incrValue = new Integer(newFindNumberIncr);

        MapList ebomList = null;

                                 try {
               // Creating and Adding select statements for the object
                                 StringList selectStmts = new StringList(1);
                                 selectStmts.addElement(DomainConstants.SELECT_ID);
                                 selectStmts.addElement(DomainConstants.SELECT_TYPE);
                                 selectStmts.addElement(DomainConstants.SELECT_NAME);
                                 selectStmts.addElement(DomainConstants.SELECT_REVISION);
                                 selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
                                 // Added for MCC EC Interoperability Feature
                                 String strAttrEnableCompliance  =PropertyUtil.getSchemaProperty(context,"attribute_EnableCompliance");
                                 selectStmts.addElement("attribute["+strAttrEnableCompliance+"]");
                                 //end
               // Creating and Adding select statements for the relationship object
                                 StringList selectRelStmts = new StringList(6);
                                 selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
                                 selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
                                 selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_QUANTITY);
                                 selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
                                 selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
                                 selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_USAGE);

                                 DomainObject domObj = DomainObject.newInstance(context,parentOID);
                                 ebomList = domObj.getRelatedObjects(context,
                                                          DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
                                                          DomainConstants.TYPE_PART,          // object pattern
                                                                  selectStmts,                 // object selects
                                                                  selectRelStmts,              // relationship selects
                                                                  false,                        // to direction
                                                                  true,                       // from direction
                                                                  (short)1,                    // recursion level
                                                                  null,                        // object where clause
                                                                  null);                       // relationship where clause

              ebomList.addSortKey("attribute[" + DomainConstants.ATTRIBUTE_FIND_NUMBER + "]", "ascending", "String");
              ebomList.sort();

          if(ebomList!=null && ebomList.size()>0) {
                                     Iterator itr = ebomList.iterator();
                // New MapList Created
                MapList interValue = new MapList(2);

            while(itr.hasNext()) {
                                         Map newMap = (Map)itr.next();
                                         String obId = (String)newMap.get("id");
                                         String connId = (String)newMap.get("id[connection]");

              if(obId.equals(radioPartObjectId)) {
                                            Iterator iter = ebomList.iterator();
                while(iter.hasNext()) {
                                               Map mMap = (Map)iter.next();
                                               String idValue = (String)mMap.get("id");
                                               String connectId = (String)mMap.get("id[connection]");

                                               if(objectId.equals(idValue)) {
                                               DomainRelationship domRelation = new DomainRelationship(connectId);
                                               domRelation.setAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER, newValue.toString());
                                               newValue = new Integer(newValue.intValue() + incrValue.intValue());
                        // Add map in new Maplist
                                               interValue.add(mMap);
                                               }
                                            }
                                         } else if(obId.equals(objectId)) {
                                            continue;
                                         }

                                         DomainRelationship domRel = new DomainRelationship(connId);
                 // update attribute Find Number with new Value
                                         domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER, newValue.toString());
                                         newValue = new Integer(newValue.intValue() + incrValue.intValue());

                 // Add map in new Maplist
                                         interValue.add(newMap);
                                     }
                 // Clear old maplist and add new one
                                     ebomList.clear();
                                     ebomList.addAll(interValue);
                                  }
                              } catch (Exception e) {
                              }

                              return ebomList;
       }

    /**
     * This method connects business objects
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @param args 0 - fromObject - 'From' Part Object ID
     * @param args 1 - toObject - 'To' Part Object ID
     * @param args 2 - relType - Relationship Type
     * @returns MapList EBOM list after resequencing Find numbers.
     * @throws Exception if the operation fails
     * @since version X+2
     */

    public void connectPartToBOM(Context context, String[] args)
      throws Exception {

      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String fromObject = (String)paramMap.get("fromObject");
      String toObject = (String)paramMap.get("toObject");
      String relType = (String)paramMap.get("relType");
      DomainObject fromBusObj = new DomainObject(fromObject);
      DomainObject toBusObj = new DomainObject(toObject);
      // Connect Part to BOM
	  DomainRelationship.connect(context, fromBusObj, relType, toBusObj);
    }

    /**
     * This method replace Parts in BOM with new Parts created
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @param args relId - Relationship ID
     * @param args selPartObjectId - Selected part in BOM
     * @param args createdPartObjId - New part created
     * @param args parentOID - Parent ObjectId
     * @param args partFamilyContextId - part Family Context Id
     * @param args radioOption - radio button Option selected
     * @returns MapList EBOM list after resequencing Find numbers.
     * @throws Exception if the operation fails
     * @since version X+2
     */
    public void replacePartinBOM(Context context, String[] args)
      throws Exception {

      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String selPartRelId = (String)paramMap.get("relId");
      String selPartObjectId = (String)paramMap.get("selPartObjectId");
      String createdPartObjId = (String)paramMap.get("createdPartObjId");
      String parentOID = (String)paramMap.get("parentOID");
      String radioOption = (String)paramMap.get("radioOption");

      String relType = DomainConstants.RELATIONSHIP_EBOM;
      DomainObject fromBusObj = newInstance(context, parentOID);
      DomainObject toBusObj = newInstance(context, createdPartObjId);
      String DerivedRel = PropertyUtil.getSchemaProperty(context,"relationship_Derived");

      Part createdPartObj = new Part(createdPartObjId);

      MapList ebomList = new MapList();

      try {
        ContextUtil.startTransaction(context, true);

        Part part = (Part)DomainObject.newInstance(context,DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
        part.setId(parentOID);

          // Creating and Adding select statements for the object
          Part partObj = new Part(selPartObjectId);
          StringList selectStmts = new StringList(9);
          selectStmts.addElement(partObj.SELECT_ID);
          selectStmts.addElement(partObj.SELECT_NAME);
          selectStmts.addElement(partObj.SELECT_REVISION);
          selectStmts.addElement(partObj.SELECT_TYPE);
          selectStmts.addElement(partObj.SELECT_DESCRIPTION);
          selectStmts.addElement(partObj.SELECT_CURRENT);
          selectStmts.addElement(partObj.SELECT_ATTRIBUTE_UNITOFMEASURE);

          // Creating and Adding select statements for the relationship object
          StringList selectRelStmts = new StringList(5);
          selectRelStmts.addElement(partObj.SELECT_RELATIONSHIP_ID);
          selectRelStmts.addElement(partObj.SELECT_FIND_NUMBER);
          selectRelStmts.addElement(partObj.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
          selectRelStmts.addElement(partObj.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
          selectRelStmts.addElement(partObj.SELECT_ATTRIBUTE_QUANTITY);
          selectRelStmts.addElement(partObj.SELECT_ATTRIBUTE_USAGE);

        if ("replaceWithExistingBOM".equals(radioOption)) {
          ebomList = partObj.getEBOMs(context, selectStmts, selectRelStmts, false);

          // Add EBOM to the newly created part
          if(ebomList !=null && !"null".equals(ebomList)) {
            Iterator itr = ebomList.iterator();

            while(itr.hasNext()) {
              Map newMap = (Map)itr.next();
              String sEBOMId = (String)newMap.get("id");
              DomainObject domObj = newInstance(context, sEBOMId);
              String relationId = (String)newMap.get("id[connection]");

              // Get the attribute map of old relation
              DomainRelationship domRelation = new DomainRelationship(relationId);
              Map attrMap = domRelation.getAttributeMap(context, true);

              // Set the attribute map of new relation
              DomainRelationship newRelation = DomainRelationship.connect(context, toBusObj, relType, domObj);
              newRelation.setAttributeValues(context, attrMap);
          }
        }
      }
      // Get the attribute map of old relation
      DomainRelationship domRel = new DomainRelationship(selPartRelId);
      Map attr = domRel.getAttributeMap(context, true);

      // Remove old BOM
      part.removeEBOM(context, selPartRelId);

      // Set the attribute map of new relation
      DomainRelationship newRel1 = DomainRelationship.connect(context, fromBusObj, relType, toBusObj);
      newRel1.setAttributeValues(context, attr);

      // 372458
      String connectAsDerived = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReplaceBOM.Derived");
      if(connectAsDerived.equalsIgnoreCase("true")){
          String attrDerivedContext= PropertyUtil.getSchemaProperty(context,"attribute_DerivedContext");
          DomainRelationship newRel2 = DomainRelationship.connect(context, partObj, DerivedRel, createdPartObj);
          newRel2.setAttributeValue(context, attrDerivedContext, "Replace");
      }
     } catch (Exception Ex) {
       throw Ex;
     }
    }

	/**
     * This method Add Next Part in BOM.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @param args selPartRelId - Relationship ID
     * @param args selObjectId - Selected part in BOM
     * @param args selPartObjId - New part created
     * @param args selPartParentOId - Parent ObjectId
     * @param args objectId - context part Object Id
     * @param args vSelIds -  selected parts in search page
     * @returns void
     * @throws Exception if the operation fails
     * @since version X+2
     */
    public void addNextinBOM(Context context, String[] args)
      throws Exception {

      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String selObjectId = (String)paramMap.get("selObjectId");
      String selPartParentOId = (String)paramMap.get("selPartParentOId");
      String objectId = (String)paramMap.get("objectId");
      Vector vSelIds = (Vector)paramMap.get("vSelIds");

      DomainObject ctxObj = DomainObject.newInstance(context);
      ctxObj.setId(objectId);
      String ctxPolicy = ctxObj.getInfo(context, DomainConstants.SELECT_POLICY);
      String state = ctxObj.getInfo(context, DomainObject.SELECT_CURRENT);
      String relType = DomainConstants.RELATIONSHIP_EBOM ;

      MapList ebomList= new MapList();

      // Get the Initial and Increment Value of Find Numbers in a BOM
      String newFindNumberIncr = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Resequence.IncrementValue");
      Integer incrValue = new Integer(newFindNumberIncr);

      Integer newFNValue = new Integer(0);
      int incrFNValue = 0;

      try {

    Part partObj = new Part(selPartParentOId);
    // Creating and Adding select statements for the object
    StringList selectStmts = new StringList(7);
    selectStmts.addElement(partObj.SELECT_ID);
    selectStmts.addElement(partObj.SELECT_NAME);
    selectStmts.addElement(partObj.SELECT_REVISION);
    selectStmts.addElement(partObj.SELECT_TYPE);
    selectStmts.addElement(partObj.SELECT_DESCRIPTION);
    selectStmts.addElement(partObj.SELECT_CURRENT);
    selectStmts.addElement(partObj.SELECT_ATTRIBUTE_UNITOFMEASURE);

    // Creating and Adding select statements for the relationship object
    StringList selectRelStmts = new StringList(2);
    selectRelStmts.addElement(partObj.SELECT_RELATIONSHIP_ID);
        selectRelStmts.addElement(partObj.SELECT_FIND_NUMBER);

        ebomList = partObj.getEBOMs(context, selectStmts, selectRelStmts, false);
        ebomList.addSortKey("attribute[" + DomainConstants.ATTRIBUTE_FIND_NUMBER + "]", "ascending", "String");
        ebomList.sort();

        int size = vSelIds.size();
        int div = size + 1;
        DomainRelationship domRel = null;
        DomainRelationship domRelation = null;
        String sFNvalue = "";
        String oldFNvalue = "";

          // Add EBOM to the newly created part
          if(ebomList != null && !"null".equals(ebomList)) {
            Iterator itr = ebomList.iterator();

            while(itr.hasNext()) {
              Map newMap = (Map)itr.next();
              String sEBOMId = (String)newMap.get("id");

              String relId = (String)newMap.get("id[connection]");
              newFNValue = new Integer(newFNValue.intValue() + incrValue.intValue());
         // Check if Dev Part not in Complete and EC Part only in Preliminary state
      if ((ctxPolicy.equals(DomainConstants.POLICY_DEVELOPMENT_PART) && !DomainConstants.STATE_DEVELOPMENT_PART_COMPLETE.equals(state)) || (ctxPolicy.equals(DomainConstants.POLICY_EC_PART) && state.equals(DomainConstants.STATE_PART_PRELIMINARY)) ) {
              if (sEBOMId.equals(selObjectId)) {
              domRel = new DomainRelationship(relId);
              if(domRel!=null) {
              domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER, newFNValue.toString());
              }
              for(int i=0;i<vSelIds.size();i++) {
              String selId = (String) vSelIds.get(i);
                DomainObject fromBusObj = new DomainObject(selPartParentOId);
            DomainObject toBusObj = new DomainObject(selId);
            domRelation = DomainRelationship.connect(context, fromBusObj, relType, toBusObj);
            newFNValue = new Integer(newFNValue.intValue() + incrValue.intValue());

              if(domRelation!=null) {
              domRelation.setAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER, newFNValue.toString());
              }
              }
              } else {
              domRel = new DomainRelationship(relId);
              if(domRel!=null) {
              domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER, newFNValue.toString());
              }
              }
          }
          else {
                if (sEBOMId.equals(selObjectId)) {
                oldFNvalue = (String)newMap.get("attribute["+DomainConstants.ATTRIBUTE_FIND_NUMBER+"]");

                newFNValue = new Integer(oldFNvalue);

                if(itr.hasNext()) {
                newMap = (Map)itr.next();
                sFNvalue = (String)newMap.get("attribute["+DomainConstants.ATTRIBUTE_FIND_NUMBER+"]");
                }

                if(!"".equals(sFNvalue)) {
                incrFNValue = (Integer.parseInt(sFNvalue) - Integer.parseInt(oldFNvalue))/div;
                } else {
                incrFNValue = Integer.parseInt(newFindNumberIncr);
                }

                if (incrFNValue>0) {
                for(int i=0;i<vSelIds.size();i++) {
					String selId = (String) vSelIds.get(i);
					DomainObject fromBusObj = new DomainObject(selPartParentOId);
					DomainObject toBusObj = new DomainObject(selId);
					domRelation = DomainRelationship.connect(context, fromBusObj, relType, toBusObj);
					newFNValue = new Integer (newFNValue.intValue() + incrFNValue);

					if(domRelation!=null) {
						domRelation.setAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER, newFNValue.toString());
					}
                }
                } else {
                String msg = "The number of inserted parts exceeds Find Numbers available \nOperation Failed";
                emxContextUtil_mxJPO.mqlNotice(context,msg);
                }
            }
          }
        }
      }

     } catch (Exception Ex) {
       throw Ex;
     }
    }

    /**
     * This method Copy Part from another BOM to context BOM.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     * @param args selPartRelId - Relationship ID
     * @param args selPartObjectId - Selected part in BOM
     * @param args selPartObjId - New part created
     * @param args selPartParentOId - Parent ObjectId
     * @param args objectId - context part Object Id
     * @param args vSelIds -  selected parts in search page
     * @returns void
     * @throws Exception if the operation fails
     * @since version X+2
     */
    public void copyPartFromBOM(Context context, String[] args)
      throws Exception {

      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      HashMap map = (HashMap)paramMap.get("map");
      String selOid = (String)paramMap.get("selOid");
      String objectId = (String)paramMap.get("objectId");
      String AppendReplaceOption = (String)paramMap.get("AppendReplaceOption");
      String strSelPartId = (String)paramMap.get("strSelPartId");
      Boolean remove = (Boolean)paramMap.get("remove");

      String newFindNumberIncr = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Resequence.IncrementValue");

      String relType = DomainConstants.RELATIONSHIP_EBOM;
      // Creating and Adding select statements for the object
      SelectList resultSelects = new SelectList(8);
      resultSelects.add(DomainObject.SELECT_ID);
      resultSelects.add(DomainObject.SELECT_TYPE);
      resultSelects.add(DomainObject.SELECT_NAME);
      resultSelects.add(DomainObject.SELECT_REVISION);
      resultSelects.add(DomainObject.SELECT_DESCRIPTION);
      resultSelects.add(DomainObject.SELECT_CURRENT);
      resultSelects.add(DomainObject.SELECT_OWNER);
      resultSelects.addElement(DomainConstants.SELECT_POLICY);

      // Creating and Adding select statements for the relationsip object
      StringList selectRelStmts = new StringList(2);
      selectRelStmts.addElement(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
      selectRelStmts.addElement(Part.SELECT_RELATIONSHIP_ID);

      DomainObject selObj = DomainObject.newInstance(context);
      selObj.setId(selOid);
      String largestFN = "";

        Vector vecFN = new Vector();
        Part partObj = new Part(objectId);

        MapList selBOMPart = partObj.getEBOMs(context, resultSelects, selectRelStmts, false);
        // Sort selBOMPart maplist
        selBOMPart.addSortKey(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER, "ascending", "Integer");
        selBOMPart.sort();

        if (selBOMPart!=null) {
        Iterator itr = selBOMPart.iterator();
          while(itr.hasNext()) {
          Map newMap = (Map)itr.next();
          largestFN = (String)newMap.get(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
          vecFN.add(largestFN);
          }
        }

        if ("".equals(largestFN)) {
			largestFN = "0";
        }

        MapList selPart = null;

        Part selPartObj = new Part(strSelPartId);
        if(AppendReplaceOption != null && "Merge".equals(AppendReplaceOption))
        {
        selPart = selPartObj.getEBOMs(context, resultSelects, selectRelStmts, false);
        selPart.addSortKey(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER, "ascending", "Integer");
        selPart.sort();
        }

        String initialValue = "0";
        if (!remove.booleanValue() || "Append".equals(AppendReplaceOption)) {
        initialValue = largestFN;
        }
        Integer newFN = new Integer(initialValue);

        // Check if selected option is Replace
        if(AppendReplaceOption != null && "Replace".equals(AppendReplaceOption) && remove.booleanValue())
        {
          // remove old BOM
          partObj.removeEBOMs(context);
        }

          String attrFindNumber = PropertyUtil.getSchemaProperty(context, "attribute_FindNumber");

           // Check if selected option is Merge
           if(AppendReplaceOption != null && "Merge".equals(AppendReplaceOption))
       {
           if (selPart!=null) {
            Iterator itr = selPart.iterator();
             while(itr.hasNext()) {
             Map newMap = (Map)itr.next();
             String objId = (String)newMap.get(DomainObject.SELECT_ID);

               if(objId.equals(selOid)) {
               String selPartFN = (String)newMap.get(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
                 if(vecFN.contains(selPartFN)) {
                     map.put(attrFindNumber, "");
                     break;
                 }
                 else {
                     map.put(attrFindNumber, selPartFN);
                     break;
                 }
               }
             }
           }
        }
        if(AppendReplaceOption != null && ("Append".equals(AppendReplaceOption) || "Replace".equals(AppendReplaceOption)))
        {
             newFN = new Integer(newFN.intValue() + new Integer(newFindNumberIncr).intValue());
             map.put(attrFindNumber, newFN.toString());
        }
            // Connect part to BOM and set new attribute map
            DomainRelationship dr = partObj.connect(context,relType, selObj, false);
            dr.setAttributeValues(context, map);

    }

/**
 * Gets the Multi-Level EBOM Summary information with EBOM relation selectables.
 * This method can also be used to get the AVL report by specifying reportType as AVL
 * @param context the eMatrix <code>Context</code> object.
 * @param args contains a packed HashMap with the following entries:
 * objectId - a String containing the Part id.
 * relationship - a String containing the name of the relationship to expand on.  This can be "EBOM" or "EBOM,EBOM History".
 * reportType - a String specifying either BOM or AVL.
 * location - a String containing the id of the Location, only needed for AVL Report.
 * level - a String containing the number of levels to expand.
 * @return MapList of EBOM Objects containing EBOM object id, ECR state,
 * ECO state, level, relationship id, relationship type.
 * @throws Exception if the operation fails.
 * @since AEF 10.6.
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getMultiLevelEBOMsWithRelSelectables (Context context,
                                   String[] args)
    throws Exception
{

    HashMap paramMap = (HashMap)JPO.unpackArgs(args);

    String partId = (String) paramMap.get("objectId");

    String relationship = (String) paramMap.get("relationship");

    // reportType can be either BOM or AVL.
    String reportType = "";
    // location variable holds the value of Location Name
    String location = "";
    // retrieve the selected reportType from the paramMap
    reportType = (String) paramMap.get("reportType");
    // location variable holds the value of Location Name
    location = (String) paramMap.get("location");

    String level = (String) paramMap.get(KEY_LEVEL);
    Short SLevel =0;
    if(level != null && !"".equals(level)){
    	SLevel = new Short(level);
    }
    
    short shLevel = SLevel.shortValue();

    MapList ebomList = new MapList();

    try {
        Part partObj = new Part(partId);

        StringList selectStmts = new StringList(3);
        selectStmts.addElement(SELECT_ID);

        StringList selectRelStmts = new StringList(8);

        selectRelStmts.addElement(KEY_LEVEL);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_TYPE);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_QUANTITY);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_FIND_NUMBER);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_USAGE);
        ebomList = partObj.getEBOMs(context,selectStmts,selectRelStmts,relationship,false,shLevel,false);

        if (location!=null && ("").equals(location) && reportType!=null && reportType.equals("AVL"))
        {
           // retrieve the Host Company attached to the User.
           location = com.matrixone.apps.common.Person.getPerson(context).getCompany(context).getObjectId(context);
         }

        // check whether report type is AVL and get the related AVL data.
        if (location!=null && reportType!=null && reportType.equals("AVL"))
        {
            MapList tempList = new MapList();
            String locationId =com.matrixone.apps.common.Person.getPerson(context).getCompany(context).getObjectId(context);
            if (locationId.equals(location))
            {
                tempList = partObj.getCorporateMEPData(context, ebomList, locationId, true, "");
            }
            else {
                tempList = partObj.getCorporateMEPData(context, ebomList, location, false, "");
            }
            ebomList.clear();
            ebomList.addAll(tempList);
        }
    }

    catch (FrameworkException Ex) {
        throw Ex;
    }

    return ebomList;
}

  /**
    * Gets the MEP names blocking MPN Names.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds object id.
    * @return a StringList of MEP & MPN Names.
    * @throws Exception If the operation fails.
    * @since 10-6.
    */
    public StringList getMEPNames(Context context,String[] args)
             throws Exception
   {
        StringList result = new StringList();
        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            Map paramList = (HashMap)paramMap.get("paramList");
            String suiteDir = (String)paramList.get("SuiteDirectory");
            String suiteKey = (String)paramList.get("suiteKey");
            String jsTreeID = (String)paramList.get("jsTreeID");
            String reportFormat = (String)paramList.get("reportFormat");

            boolean isexport = false;
            String export = (String)paramList.get("exportFormat");
            if ( export != null )
            {
                isexport = true;
            }

            String publicPortal = (String)paramList.get("publicPortal");
            String linkFile = (publicPortal != null && publicPortal.equalsIgnoreCase("true"))?"emxNavigator.jsp":"emxTree.jsp";

            String parentOID = (String)paramList.get("objectId");

            MapList objectList = (MapList)paramMap.get("objectList");

            if(objectList != null && objectList.size() > 0)
            {
                //construct array of ids
                int objectListSize = objectList.size();
                for(int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map)objectList.get(i);

                    String type = (String)dataMap.get(SELECT_TYPE);
                    String displayValue = "&nbsp;";

                    String sTypeIcon=EngineeringUtil.getTypeIconProperty(context, type);
                    String imgSrc = "<img src='images/"+sTypeIcon+"' border='0'>";

                    if(type.equals(TYPE_MPN))
                    {
                        String showBlankName = EnoviaResourceBundle.getProperty(context, "emxManufacturerEquivalentPart.EngrPlaceholderMEP.ShowBlankName");
                        if("false".equalsIgnoreCase(showBlankName))
                        {
                            displayValue = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EngrPlaceholderMEP.DefaultName",context.getSession().getLanguage());
                        }
                        else
                        {
                            imgSrc = "";
                        }
                    }
                    else
                    {
                       displayValue = (String)dataMap.get(SELECT_NAME);
                    }

                    StringBuffer output = new StringBuffer(256);
					output.append(' ');
                    //do not show hyperlinks if it is a printer friendly or excel export page
                    //length will be >0 when format is HTML, ExcelHTML, CSV or TXT
                    if(isexport)
                    {
                        output.append(displayValue);
                    }
                    else if(null!=reportFormat && !reportFormat.equals("null") && (reportFormat.length() > 0))
                    {
                       output.append(imgSrc);
					   output.append("&nbsp;");
					   output.append(displayValue);
                    }
                    else
                    {
                        output.append("<table border=\"0\"><tr><td valign=\"top\">");
                        output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"+linkFile+"?emxSuiteDirectory=");
                        output.append(suiteDir);
                        output.append("&suiteKey=");
                        output.append(suiteKey);
                        output.append("&jsTreeID=");
                        output.append(jsTreeID);
                        output.append("&parentOID=");
                        output.append(parentOID);
                        output.append("&relId=");
                        output.append((String)dataMap.get(SELECT_RELATIONSHIP_ID));
                        output.append("&objectId=");
                        output.append((String)dataMap.get(SELECT_ID));
                        output.append("', '700', '600', 'false', 'popup', '')\" class=\"object\">");
                        output.append(imgSrc);
                        output.append("</a></td>");
                        if(!"&nbsp;".equals(displayValue)){
                            output.append("<td>&nbsp;");
                            output.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/"+linkFile+"?emxSuiteDirectory=");
                            output.append(suiteDir);
                            output.append("&suiteKey=");
                            output.append(suiteKey);
                            output.append("&jsTreeID=");
                            output.append(jsTreeID);
                            output.append("&parentOID=");
                            output.append(parentOID);
                            output.append("&relId=");
                            output.append((String)dataMap.get(SELECT_RELATIONSHIP_ID));
                            output.append("&objectId=");
                            output.append((String)dataMap.get(SELECT_ID));
                            output.append("', '700', '600', 'false', 'popup', '')\" class=\"object\">");
                            output.append(displayValue);
                            output.append("</a>&nbsp;</td>");
                        }
                        output.append("</tr></table>");
                    }

                    result.add(output.toString());
                }
            }//end if

       }catch (Exception Ex) {
           throw Ex;
       }

       return result;
   }

  /**
    * Gets the MEP Types blocking MPN Types.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds object id.
    * @return a StringList of MEP & MPN Types.
    * @throws Exception If the operation fails.
    * @since 10-6.
    */
    public StringList getMEPTypes(Context context,String[] args)
             throws Exception
   {
        StringList result = new StringList();
        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramList= (HashMap) paramMap.get("paramList");
            String languageStr = (String) paramList.get("languageStr");
            MapList objectList = (MapList)paramMap.get("objectList");

            if(objectList != null && objectList.size() > 0)
            {
                //construct array of ids
                int objectListSize = objectList.size();
                for(int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map)objectList.get(i);

                    String type = (String)dataMap.get(SELECT_TYPE);

                    if(!type.equals(TYPE_MPN))
                    {
                       result.add(i18nNow.getTypeI18NString(type,languageStr));
                    }
                    else
                    {
                       result.add(" ");
                    }
                }

            }//end if

       }catch (Exception Ex) {
           throw Ex;
       }
       return result;
   }

  /**
    * Gets the MEP Revisions blocking MPN Revisions.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds object id.
    * @return a StringList of MEP & MPN Revisions.
    * @throws Exception If the operation fails.
    * @since 10-6.
    */
    public StringList getMEPRevisions(Context context,String[] args)
             throws Exception
   {
        StringList result = new StringList();
        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            MapList objectList = (MapList)paramMap.get("objectList");

            if(objectList != null && objectList.size() > 0)
            {
                //construct array of ids
                int objectListSize = objectList.size();
                for(int i = 0; i < objectListSize; i++) {
                    Map dataMap = (Map)objectList.get(i);

                    String type = (String)dataMap.get(SELECT_TYPE);

                    if(!type.equals(TYPE_MPN))
                    {
                        result.add((String)dataMap.get(SELECT_REVISION));
                    }
                    else
                    {
                        result.add(" ");
                    }
                }

            }//end if

       }catch (Exception Ex) {
           throw Ex;
       }
       return result;
   }

 /**
    * Gets the MEPs attached to an Enterprise Part.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds object id.
    * @return a MapList of Enterprise Parts.
    * @throws Exception If the operation fails.
    * @since 10-5.
    */
    public MapList getEnterpriseValidManufacturerEquivalents (Context context,String[] args)
           throws Exception
    {
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       paramMap.put("includeMPN", "False");
       args = JPO.packArgs(paramMap);

       return getEnterpriseManufacturerEquivalents (context, args);
    }

  /**
    * Gets the new Window Column for Launch but not for Channel.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds object id.
    * @returns Boolean, whether to show new window column or not.
    * @throws Exception If the operation fails.
    * @since 10-6.
    */
    public Boolean isNewWindowViewable(Context context,String[] args)
             throws Exception
   {
        Boolean isColumnVisible = Boolean.FALSE;
        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            String launched = (String)paramMap.get("launched");
            String portalMode = (String)paramMap.get("portalMode");

            if( (launched!=null && launched.equalsIgnoreCase("true"))
                && (portalMode!=null && portalMode.equalsIgnoreCase("false")) ) {
                  isColumnVisible = Boolean.TRUE;
            }
        }catch (Exception Ex) {
           throw Ex;
        }
        return isColumnVisible;
   }

  /**
   * Displays the text field for Revision in Edit and View Part web form screen
   *
   * @param context the eMatrix <code>Context</code> object.
   *
   * @return String
   * @throws Exception if the operation fails.
   * @since EC 10-6
   */
    public Object displayPartRevision(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        Map requestMap = (Map)programMap.get("requestMap");
        String strPartId = (String) requestMap.get("objectId");
        String strMode = (String) requestMap.get("mode");
        String reportFormat = (String)requestMap.get("reportFormat");

        StringList strList  = new StringList(2);
        strList.add(SELECT_REVISION);
        strList.add("policy.property[PolicyClassification].value");

        DomainObject domObj = new DomainObject(strPartId);
        Map map = domObj.getInfo(context,strList);

        String strRevision = (String)map.get(SELECT_REVISION);

        boolean isMep = false;
        String strPolicyClassification = (String)map.get("policy.property[PolicyClassification].value");
        if("Equivalent".equals(strPolicyClassification))
        {
            isMep = true;
        }

        boolean isViewMode = false;
        if( strMode == null || "null".equals(strMode) || "view".equalsIgnoreCase(strMode) || "".equals(strMode) )
        {
            isViewMode = true;
        }

        StringBuffer strBuf = new StringBuffer();

        if(isViewMode || !isMep)
        {
            strBuf.append(strRevision);
            if (reportFormat==null || reportFormat.length()==0 || "null".equals(reportFormat))
            {
                strBuf.append("<input type=\"hidden\" name=\"Revision\" value=\"").append(strRevision).append("\">");
            }
        }
        else
        {
            String customRevision = EnoviaResourceBundle.getProperty(context, "emxManufacturerEquivalentPart.MEP.allowCustomRevisions");
            if( customRevision == null)
            {
                customRevision = "false";
            }
            else
            {
                customRevision = customRevision.trim();
            }
            if (reportFormat==null || reportFormat.length()==0 || "null".equals(reportFormat))
            {
                strBuf.append("<input type=\"text\" name=\"Revision\" value=\"").append(strRevision).append("\" ");
            }
            /* EC-MCC interoperability */
            /* Added one more condition to check MCC is installed or not. */
            if("false".equals(customRevision) || isMCCInstalled(context,args))
            {
                strBuf.append(" READONLY ");
            }
            strBuf.append('>');
        }

        return strBuf.toString();
    }

	 /**
	 * Updates the Revision for a Part.
	 * @param context the eMatrix <code>Context</code> object
	 * @return int "0" success "1" failure
	 * @throws Exception if operation fails
	 * @since EngineeringCentral 10-6
	 * Copyright (c) 2004, MatrixOne, Inc.
	 */
    public int updatePartRevision(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        Map requestMap =  (HashMap)programMap.get("requestMap");

        HashMap paramMap = (HashMap) programMap.get("paramMap");

        String strPartId = (String) paramMap.get("objectId");
        String languageStr = (String) paramMap.get("languageStr");

        String[] revisionValues = (String[])requestMap.get("Revision");
        String strRevValue = "";
        if(revisionValues != null && revisionValues.length > 0)
        {
            strRevValue = revisionValues[0];
        }
        DomainObject domObj = new DomainObject(strPartId);
        StringList strList = new StringList();
        strList.add(SELECT_TYPE);
        strList.add(SELECT_NAME);
        strList.add(SELECT_REVISION);
        strList.add("policy.property[PolicyClassification].value");

        Map map = domObj.getInfo(context,strList);

        boolean isMep = false;
        String strPolicyClassification = (String)map.get("policy.property[PolicyClassification].value");
        if("Equivalent".equals(strPolicyClassification))
        {
            isMep = true;
        }

        String strRev = (String) map.get(SELECT_REVISION);
        if (isMep && !"".equals(strRevValue) && !strRev.equals(strRevValue))
        {
            String strType = (String) map.get(SELECT_TYPE);
            String strName = (String) map.get(SELECT_NAME);
            MapList mapList = DomainObject.findObjects(context,
                                                     strType,
                                                     strName,
                                                     strRevValue,
                                                     QUERY_WILDCARD,
                                                     QUERY_WILDCARD,
                                                     "",
                                                     false,
                                                     null);
            if(mapList == null || mapList.size() == 0)
            {
                (new com.matrixone.apps.engineering.Change()).setRevision(context,strPartId,strRevValue,strName);
            }
            else
            {
                String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.MEP.Exists",languageStr);
                emxContextUtil_mxJPO.mqlNotice(context,strMessage);
                return 1;
            }
        }
        return 0;
    }

 /**
 * Gives the modified attribute (EBOM Relationship ) value after grouping them in different formats.
 * @return HashMap Key value pairs for different combinations of Reference designator
 * @throws Exception if operation fails
 * @since EngineeringCentral 10-6
 * Copyright (c) 2004, MatrixOne, Inc.
 */
    public HashMap getModifiedEBOMRelAttributeValue(String data) throws Exception {
         HashMap valueMap = null;
         StringList fnList = FrameworkUtil.split(data, "*");
         if(fnList != null && fnList.size() > 0) {
             valueMap = new HashMap();
             Iterator i = fnList.iterator();
             while(i.hasNext()) {
                 String sFn = (String)i.next();
                 String key = sFn.substring(0,sFn.indexOf(':'));
                 String value = (String)valueMap.get(key);
                 if( value !=null ) {
                     if(!value.equals( sFn.substring(sFn.indexOf(':')+1) )) {
                         valueMap.put(key,"");
                     }
                 } else {
                     valueMap.put(key,sFn.substring(sFn.indexOf(':')+1));
                 }
             }
         } else{
         }
         return valueMap;
    }

/**
    * Gets all Part Family Object connected to Part.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds HashMap containing the following entries:
    * objectId of Part.
    * mode of webform
    * @return a String  : connected  Part Family -->Part as HTMLOutput.
    * @throws Exception if the operation fails.
    * @since 10.6.
    */
    public String getPartFamilyListHTMLOutput (Context context,String[] args)
    throws Exception
    {
         StringBuffer output = new StringBuffer();
         try {
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                HashMap requestMap = (HashMap) programMap.get("requestMap");
                String strMode = (String) requestMap.get("mode");
                String reportFormat = (String)requestMap.get("reportFormat");

                String partId = (String) requestMap.get("objectId");
                Part partObj = new Part(partId);
                StringList selectStmts  = new StringList(2);
                selectStmts.addElement(SELECT_ID);
                selectStmts.addElement(SELECT_NAME);

                MapList mapList = partObj.getRelatedObjects(context,
                                                            RELATIONSHIP_CLASSIFIED_ITEM,  // relationship pattern
                                                            TYPE_PART_FAMILY,                    // object pattern
                                                            selectStmts,                 // object selects
                                                            null,              // relationship selects
                                                            true,                       // to direction
                                                            false,                        // from direction
                                                            (short) 1,                   // recursion level
                                                            null,                        // object where clause
                                                            null);                       // relationship where clause
                String strOuput ="";
                StringBuffer partFamilyName=new StringBuffer();
                StringBuffer partFamilyId=new StringBuffer();
                StringBuffer strBufNamesForExport = new StringBuffer();
                if(mapList != null && mapList.size() > 0)
                {
                      //construct array of ids
                      int mapListSize = mapList.size();
                      for(int i = 0; i < mapListSize; i++)
                      {
                          Map dataMap = (Map)mapList.get(i);
//                          String type = (String)dataMap.get(SELECT_TYPE);
                          String name = (String)dataMap.get(SELECT_NAME);
                          String objectId =(String)dataMap.get(SELECT_ID);

                          // check the mode of the web form.
                         if( strMode != null && "edit".equalsIgnoreCase(strMode) )
                         {
                              partFamilyName.append(name);
                              partFamilyName.append(',');
                              partFamilyId.append(objectId);
                              partFamilyId.append(',');
                         }
                         else
                         {

                             if(reportFormat != null && reportFormat.length() > 0){
                                 strBufNamesForExport.append(name);
                             }

                              output.append("<a href=\"JavaScript:emxFormLinkClick('../common/emxTree.jsp?AppendParameters=true&objectId=");
                              output.append(objectId);
                              output.append("','', '', '', '')\">");
                              output.append("<img border='0' src=\"../common/images/iconSmallPartFamily.gif\"></a>");
                              output.append("<a href=\"JavaScript:emxFormLinkClick('../common/emxTree.jsp?AppendParameters=true&objectId=");
                              output.append(objectId);
                              output.append("','', '', '', '')\">");
                              output.append(name);
                              output.append("</a>");
                              output.append(',');
                              strOuput = output.toString();
                              strOuput =strOuput.substring(0,strOuput.length()-1);
                         }
                      }
                }

                // check the mode of the web form.
                if( strMode != null && "edit".equalsIgnoreCase(strMode))
                {
                    String strpartFamilyName = "";
                    String strpartFamilyId = "";

                    if(partFamilyName.length()>0)
                    {
                        strpartFamilyName = partFamilyName.toString();
                        strpartFamilyName =strpartFamilyName.substring(0,strpartFamilyName.length()-1);
                        strpartFamilyId = partFamilyId.toString();
                        strpartFamilyId =strpartFamilyId.substring(0,strpartFamilyId.length()-1);
                    }

                    String strClear = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",context.getSession().getLanguage());                    output.append("<input type='text'  maxlength=\"\" readonly='readonly' size='20' name=\"partFamilyDisplay\" id='' value=\""+strpartFamilyName+"\">");
                    output.append("<input type=\"hidden\" name=\"partFamily\"  value=\""+strpartFamilyName+"\">");
                    output.append("<input type=\"hidden\" name=\"partFamilyOID\"  value=\""+strpartFamilyId+"\">");
                    output.append("<input type=\"hidden\" name=\"partFamilyAutoGenerate\"  value=\"\">");
                    output.append("<input type=button name='btnpartFamily' value='...'  onClick=\"");
                    output.append("javascript:showPartFamily()\">");
                    output.append("&nbsp;&nbsp;<a href=\"JavaScript:clearPartFamily()\">"+strClear+"</a>");
                    output.append("<script language=javascript> function clearPartFamily() { document.editDataForm.partFamily.value = \"\";");
                    output.append("document.editDataForm.partFamilyDisplay.value = \"\";");
                    output.append("document.editDataForm.partFamilyOID.value = \"\";}");
                    output.append("</script>");
                    strOuput =output.toString();
                }

                if(reportFormat != null && reportFormat.length() > 0){
                	strOuput  = strBufNamesForExport.toString();
                }

                return strOuput ;
      }
      catch (Exception ex)
      {
            throw ex;
      }
   }

/**
 * Connects the Part Family to a Part.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap containing the following entries:
 * paramMap - a HashMap containing String values for "objectId", "Old value" and "New OID".
 * @return Object - boolean true if the operation is successful.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
    public Object connectPartFamily(Context context, String[] args)
    throws Exception
    {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      HashMap paramMap = (HashMap) programMap.get("paramMap");
      String strPartId = (String) paramMap.get("objectId");
      String newPartFamilyIds = (String) paramMap.get("New OID");
      String strPartFamilyRelationship = RELATIONSHIP_CLASSIFIED_ITEM;
      StringList newPartFamilyList = FrameworkUtil.split(newPartFamilyIds, ",");

       Part partObj = new Part(strPartId);

       StringList selectStmts  = new StringList(2);
       selectStmts.addElement(SELECT_ID);
       StringList RelSelectsList = new StringList(SELECT_RELATIONSHIP_ID);
       //Get the part Family connected to part.
       MapList partFamilyList = partObj.getRelatedObjects(context,
                                                            RELATIONSHIP_CLASSIFIED_ITEM,  // relationship pattern
                                                            TYPE_PART_FAMILY,                    // object pattern
                                                            selectStmts,                 // object selects
                                                            RelSelectsList,              // relationship selects
                                                            true,                       // to direction
                                                            false,                        // from direction
                                                            (short) 1,                   // recursion level
                                                            null,                        // object where clause
                                                            null);                       // relationship where clause

       try {
         ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); //366577

        if(partFamilyList != null && partFamilyList.size() > 0)
        {
          //construct array of ids
          int partFamilyListSize = partFamilyList.size();
          for(int i = 0; i < partFamilyListSize; i++)
          {
              String strId  =(String)((Map)partFamilyList.get(i)).get(SELECT_ID);
              if(!newPartFamilyList.contains(strId))
              {
                  String strRelId  =(String) ((Map)partFamilyList.get(i)).get(SELECT_RELATIONSHIP_ID);
                  //Disconnecting the existing relationship
                  DomainRelationship.disconnect(context, strRelId);
              }
              else
              {
                  newPartFamilyList.remove(strId);
              }
          }
        }
        if ( newPartFamilyList.size() > 0)
        {
            Iterator partFamilyItr = newPartFamilyList.iterator();
            while( partFamilyItr.hasNext() )
            {
                String newPartFamily = (String)partFamilyItr.next();
                setId(newPartFamily);
                DomainObject domainObjectToType = newInstance(context, strPartId);
                DomainRelationship.connect(context,this,strPartFamilyRelationship,domainObjectToType);
            }
         }
	 }
         finally { ContextUtil.popContext(context); } //366577

        return Boolean.TRUE;
    }

     /**
     * This method displays the stored format list based on Reference Designator
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns MapList EBOM list with stored format Reference Designator.
     * @throws Exception if the operation fails
     * @since EC 10.6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getStoredEBOM(Context context, String[] args)
        throws Exception
    {
     MapList ebomList = null;
     try{
        ebomList = getEBOMsWithRelSelectables (context, args);

        Iterator itr = ebomList.iterator();
        MapList tList = new MapList();
        StringList ebomDerivativeList = EngineeringUtil.getDerivativeRelationships(context, RELATIONSHIP_EBOM, true);
        while(itr.hasNext())
        {
            HashMap newMap = new HashMap((Map)itr.next());
            newMap.put("selection", "multiple");
			//Added for hasChildren starts 
			newMap.put("hasChildren",EngineeringUtil.getHasChildren(newMap, ebomDerivativeList));
			//Added for hasChildren ends
            tList.add (newMap);
        }
        ebomList.clear();
        ebomList.addAll(tList);
		//369074
		HashMap hmTemp = new HashMap();
        hmTemp.put("expandMultiLevelsJPO","true");
		ebomList.add(hmTemp);
        }
      catch (FrameworkException Ex) {
        throw Ex;
        }

		return ebomList;
      }

 /**
 * Updates the Find Number Attribute on EBOM Relatioship.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap containing the following entries:
 * paramMap - a HashMap containing String values for "relId" and "New Value".
 * @return Boolean - true if the operation is successful.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
  public Boolean updateFindNumber(Context context, String[] args) throws Exception
  {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      HashMap paramMap = (HashMap)programMap.get("paramMap");
      String relId  = (String)paramMap.get("relId");
      String newFindNumberValue = (String)paramMap.get("New Value");
      DomainRelationship domRel = new DomainRelationship(relId);
      String relType = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"name");
      if(null!=relType && !"null".equalsIgnoreCase(relType) && !"".equals(relType) && !relType.equalsIgnoreCase(EngineeringConstants.RELATIONSHIP_ALTERNATE))
      domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER, newFindNumberValue);
      return Boolean.TRUE;
  }
  // IR-087314 : Added for BOM Compare Report For Find Number Column
  public Boolean BOMCompareupdateFindNumber(Context context, String[] args) throws Exception
  {
	  HashMap programMap = (HashMap)JPO.unpackArgs(args);
	  HashMap paramMap = (HashMap)programMap.get("paramMap");
	  String relId  = (String)paramMap.get("relId");
	  String newFindNumberValue = (String)paramMap.get("New Value");
	  DomainRelationship domRel = new DomainRelationship(relId);
	  String relType = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"name");
	  String objectID=MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"from.id");
	  DomainObject obj=new DomainObject(objectID);
      String CurrentState=obj.getInfo(context, DomainConstants.SELECT_CURRENT);
	  String  sErrorMsg=  getErrorMessageForBOMCompareSync(context, obj);
	  if(UIUtil.isNullOrEmpty(sErrorMsg) || null!=relType && !"null".equalsIgnoreCase(relType) && !"".equals(relType) && !relType.equalsIgnoreCase(EngineeringConstants.RELATIONSHIP_ALTERNATE) && CurrentState.equals(DomainConstants.STATE_PART_PRELIMINARY))
	  {
		  domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_FIND_NUMBER, newFindNumberValue);
		  return Boolean.TRUE;
	  }  else  {
		  throw new FrameworkException(sErrorMsg);
	  }

  }

 /**
 * Updates the Reference Designator Attribute on EBOM Relatioship.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap containing the following entries:
 * paramMap - a HashMap containing String values for "relId" and "New Value".
 * @return Boolean - true if the operation is successful.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
  public Boolean updateReferenceDesignator(Context context, String[] args) throws Exception
  {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      HashMap paramMap = (HashMap)programMap.get("paramMap");
      String relId  = (String)paramMap.get("relId");
      String newRDValue = (String)paramMap.get("New Value");
      DomainRelationship domRel = new DomainRelationship(relId);
      String relType = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"name");
      if(null!=relType && !"null".equalsIgnoreCase(relType) && !"".equals(relType) && !relType.equalsIgnoreCase(EngineeringConstants.RELATIONSHIP_ALTERNATE))
      domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR, newRDValue);
      return Boolean.TRUE;
  }

  //IR-087314 : Added For BOM Compare Report For Reference Designator column
  public Boolean BOMCompareupdateReferenceDesignator(Context context, String[] args) throws Exception
  {
	  HashMap programMap = (HashMap)JPO.unpackArgs(args);
	  HashMap paramMap = (HashMap)programMap.get("paramMap");
	  String relId  = (String)paramMap.get("relId");
	  String newRDValue = (String)paramMap.get("New Value");
	  DomainRelationship domRel = new DomainRelationship(relId);
	  String relType = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"name");
	  String objectID=MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"from.id");
	  DomainObject obj=new DomainObject(objectID);
      String CurrentState=obj.getInfo(context, DomainConstants.SELECT_CURRENT);
	  String  sErrorMsg=  getErrorMessageForBOMCompareSync(context, obj);
	  if(UIUtil.isNullOrEmpty(sErrorMsg) || null!=relType && !"null".equalsIgnoreCase(relType) && !"".equals(relType) && !relType.equalsIgnoreCase(EngineeringConstants.RELATIONSHIP_ALTERNATE) &&  CurrentState.equals(DomainConstants.STATE_PART_PRELIMINARY))
	  {
		  domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR, newRDValue);
		  return Boolean.TRUE;
	  }  else  {
		  throw new FrameworkException(sErrorMsg);
	  }

  }
 /**
 * Updates the Component Location Attribute on EBOM Relatioship.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap containing the following entries:
 * paramMap - a HashMap containing String values for "relId" and "New Value".
 * @return Boolean - true if the operation is successful.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
  public Boolean updateComponentLocation(Context context, String[] args) throws Exception
  {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      HashMap paramMap = (HashMap)programMap.get("paramMap");
      String relId  = (String)paramMap.get("relId");
      String newCLValue = (String)paramMap.get("New Value");
      DomainRelationship domRel = new DomainRelationship(relId);
      domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_COMPONENT_LOCATION, newCLValue);
      return Boolean.TRUE;
  }

//IR-087314 : Added For BOM Compare Report For Component Location column
  public Boolean BOMCompareupdateComponentLocation(Context context, String[] args) throws Exception
  {
	  HashMap programMap = (HashMap)JPO.unpackArgs(args);
	  HashMap paramMap = (HashMap)programMap.get("paramMap");
	  String relId  = (String)paramMap.get("relId");
	  String newCLValue = (String)paramMap.get("New Value");
	  DomainRelationship domRel = new DomainRelationship(relId);
	  String relType = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"name");
	  String objectID=MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"from.id");
	  DomainObject obj=new DomainObject(objectID);
      String CurrentState=obj.getInfo(context, DomainConstants.SELECT_CURRENT);
	  String  sErrorMsg=  getErrorMessageForBOMCompareSync(context, obj);
	  if(UIUtil.isNullOrEmpty(sErrorMsg) || null!=relType && !"null".equalsIgnoreCase(relType) && !"".equals(relType) && !relType.equalsIgnoreCase(EngineeringConstants.RELATIONSHIP_ALTERNATE) && CurrentState.equals(DomainConstants.STATE_PART_PRELIMINARY))
	  {
		  domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_COMPONENT_LOCATION, newCLValue);
		  return Boolean.TRUE;
	  }  else  {
		  throw new FrameworkException(sErrorMsg);
	  }

  }
   /**
   * Updates the Quantity Attribute on EBOM Relatioship.
   * @param context the eMatrix <code>Context</code> object
   * @param args holds a HashMap containing the following entries:
   * paramMap - a HashMap containing String values for "relId" and "New Value".
   * @return Boolean - true if the operation is successful.
   * @throws Exception if operation fails
   * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
   */
    public Boolean updateQuantity(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String relId  = (String)paramMap.get("relId");
        String newQtyValue = (String)paramMap.get("New Value");
        DomainRelationship domRel = new DomainRelationship(relId);
        domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_QUANTITY, newQtyValue);
        return Boolean.TRUE;
  }

  //IR-087314 : Added For BOM Compare Report For Quantity column
    public Boolean BOMCompareupdateQuantity(Context context, String[] args) throws Exception
    {
  	    HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String relId  = (String)paramMap.get("relId");
        String newQtyValue = (String)paramMap.get("New Value");
        DomainRelationship domRel = new DomainRelationship(relId);
        String relType = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"name");
        String objectID=MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"from.id");
        DomainObject obj=new DomainObject(objectID);
        String CurrentState=obj.getInfo(context, DomainConstants.SELECT_CURRENT);
  	    String  sErrorMsg=  getErrorMessageForBOMCompareSync(context, obj);
		if(UIUtil.isNullOrEmpty(sErrorMsg) || null!=relType && !"null".equalsIgnoreCase(relType) && !"".equals(relType) && !relType.equalsIgnoreCase(EngineeringConstants.RELATIONSHIP_ALTERNATE) && CurrentState.equals(DomainConstants.STATE_PART_PRELIMINARY))
  	    {
  	    	domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_QUANTITY, newQtyValue);
  	    	return Boolean.TRUE;
  	    }  else  {
  	    	throw new FrameworkException(sErrorMsg);
  	    }
    }

   /**
   * Updates the Usage Attribute on EBOM Relatioship.
   * @param context the eMatrix <code>Context</code> object
   * @param args holds a HashMap containing the following entries:
   * paramMap - a HashMap containing String values for "relId" and "New Value".
   * @return Boolean - true if the operation is successful.
   * @throws Exception if operation fails
   * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
   */
    public Boolean updateUsage(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        String relId  = (String)paramMap.get("relId");
        String newUsageValue = (String)paramMap.get("New Value");
        DomainRelationship domRel = new DomainRelationship(relId);
        domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_USAGE, newUsageValue);
        return Boolean.TRUE;
  }

   //IR-087314 : Added For BOM Compare Report For Usage column
    public Boolean BOMCompareupdateUsage(Context context, String[] args) throws Exception
    {
    	HashMap programMap = (HashMap)JPO.unpackArgs(args);
    	HashMap paramMap = (HashMap)programMap.get("paramMap");
    	String relId  = (String)paramMap.get("relId");
    	String newUsageValue = (String)paramMap.get("New Value");
    	DomainRelationship domRel = new DomainRelationship(relId);
    	String relType = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"name");
    	String objectID=MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",relId,"from.id");
    	DomainObject obj=new DomainObject(objectID);
        String CurrentState=obj.getInfo(context, DomainConstants.SELECT_CURRENT);
  	    String  sErrorMsg=  getErrorMessageForBOMCompareSync(context, obj);
		if(UIUtil.isNullOrEmpty(sErrorMsg) || null!=relType && !"null".equalsIgnoreCase(relType) && !"".equals(relType) && !relType.equalsIgnoreCase(EngineeringConstants.RELATIONSHIP_ALTERNATE) && CurrentState.equals(DomainConstants.STATE_PART_PRELIMINARY))
  	    {
  	    	domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_USAGE, newUsageValue);
  	    	return Boolean.TRUE;
  	    }  else  {
  	    	throw new FrameworkException(sErrorMsg);
  	    }
    }

   /**
    * This method to returns context object editable policies
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a String array with packed policy information
    * @throws Exception if the operation fails
    * @since SCA 10-6
    */
    public String[] loadPolicyMap(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs (args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        Map objMap = (Map) programMap.get("objectMap");
        String partId = (String) paramMap.get("objectId");
        Part partObj = null;

        DomainObject domPartObject = new DomainObject(partId);
        String typeOfObject = domPartObject.getInfo(context,"type");

        String partPolicy = domPartObject.getInfo(context,"policy");

        if(typeOfObject.equals(DomainConstants.TYPE_APPLICATION_PART))
        {
            partObj = (com.matrixone.apps.engineering.Part)DomainObject.newInstance(context,DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
            partObj.setId(partId);
        }
        else
        {
            partObj = (Part)DomainObject.newInstance(context,partId,DomainConstants.ENGINEERING);
        }

        Hashtable hashPolicies = partObj.getPolicyClassificationPolicies(context, objMap);
        Enumeration enumPolicies = hashPolicies.elements();

        HashMap policyMap = new HashMap();

        while(enumPolicies.hasMoreElements())
        {
            String partPolicyName = (String)enumPolicies.nextElement();

            if (partPolicy.equals(partPolicyName))
            {
                partPolicyName = partPolicyName + ":selected";
            }
            policyMap.put(partPolicyName,partPolicyName);
        }

        programMap.put("policyMap",policyMap);
        args = JPO.packArgs(programMap);

        return args;
   }

   /**
    * This method to gets displayble fields for an Application Part
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a String containing the HTML content to display Bondpad.
    * @throws Exception if the operation fails
    * @since SCA 10-6
    */
   public String getAppFields(Context context, String[] args) throws Exception
   {
        HashMap programMap = (HashMap) JPO.unpackArgs (args);
        HashMap requestMap = (HashMap) programMap.get ("requestMap");
        Map objMap = (Map)programMap.get("objectMap");
        HashMap policyMap = (HashMap)programMap.get("policyMap");
        String partType = (String) objMap.get("partType");

        StringList sRDOIds;
        String sRDOId = "";

        StringList sRDONames;
        String sRDOName = "";

        try
        {
            sRDOIds = (StringList)objMap.get("to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id");
        }
        catch(Exception e)
        {
            sRDOIds = new StringList(1);
            sRDOId = (String)objMap.get("to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id");
            sRDOIds.addElement(sRDOId);
        }
        if(sRDOIds != null)
        {
            sRDOId = (String)sRDOIds.get(0);
        }

        try
        {
            sRDONames = (StringList)objMap.get("to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.name");
        }
        catch(Exception e)
        {
            sRDONames = new StringList(1);
            sRDOName = (String)objMap.get("to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.name");
            sRDONames.addElement(sRDOName);
        }
        if(sRDONames != null)
        {
            sRDOName = (String)sRDONames.get(0);
        }


        if(sRDOName == null || "null".equals(sRDOName))
        {
            sRDOName = "";
        }
        String sPolicy  = (String)objMap.get("policy");
        String mode = (String)requestMap.get("mode");

        if(mode==null)
        {
            mode = "view";
        }

        StringBuffer returnString = new StringBuffer(1024);

        if(mode.equalsIgnoreCase("edit"))
        {
            returnString.append("<tr>");
            returnString.append("<td class=\"label\"><label for=\"" +LABEL_POLICY +  "\">" + LABEL_POLICY + "</label></td>");
            String hasPolicyEditAccess = (String)objMap.get("canEditPolicy");
            if(hasPolicyEditAccess==null)
            {
                hasPolicyEditAccess = "false";
            }
            if(hasPolicyEditAccess.equalsIgnoreCase("true"))
            {
                returnString.append("<td class=\"inputField\" ><select name=\"PolicyDisplay\" >");
                java.util.Set set = policyMap.entrySet();
                Iterator iter = set.iterator();
                while(iter.hasNext())
                {
                    Map.Entry valueEntryMap = (Map.Entry) iter.next();
                    String option = (String)valueEntryMap.getValue();

                    int valColonIndex = option.indexOf(':');
                    String strSelected = "";

                    if(valColonIndex!=-1)
                    {
                        option = option.substring(0,valColonIndex);
                        strSelected = "selected";
                    }
                    returnString.append("<option value=\"" +  option + "\"" + strSelected + ">" + i18nNow.getAdminI18NString("policy", option, context.getSession().getLanguage()) + "</option>");
                }
                returnString.append("</select>");
                returnString.append("<input type=\"hidden\" name=\"oldPolicy\" value=\"" + sPolicy + "\">");
                returnString.append("</td>");
            }
            else
            {
                returnString.append("<td class=\"inputField\">" + sPolicy + "&nbsp;</td>");
            }
            returnString.append("<td class=\"label\"><label for=\"" + LABEL_OWNER +  "\">" + LABEL_OWNER + "</label></td>");
            returnString.append("<td class=\"inputField\" ><input type=\"text\" name=\"OwnerDisplay\" id=\"\" value=\"" + (String) objMap.get("owner")+ "\" maxlength=\"\" size=\"\">&nbsp;");
            returnString.append("<input type=\"button\" value=\"...\" name=\"\" onclick=\"Javascript:showChooser('../engineeringcentral/emxEngrPersonSearchDialogFS.jsp?form=editDataForm&field=OwnerDisplay', 550, 550);\"> </td>");

            returnString.append("<input type=\"hidden\" name=\"Owner\" value=\"" + (String) objMap.get("owner") + "\">");
            returnString.append("<script language=\"JavaScript\">document.forms[0]['Owner'].fieldLabel=\"Owner\";</script>");

            returnString.append("<td class=\"label\"><label for=\"" +LABEL_DESIGN_RESPONSIBILITY +  "\">" + LABEL_DESIGN_RESPONSIBILITY + "</label></td>");
            returnString.append("<td class=\"inputField\"><input type=\"text\" name=\"RDODisplay\" id=\"\" value=\"" + sRDOName + "\" maxlength=\"\" size=\"\">&nbsp;");
            returnString.append("<input type=\"button\" value=\"...\" name=\"\" onclick=\"Javascript:showChooser('../engineeringcentral/emxpartRDOSearchDialogFS.jsp?form=editDataForm&field=RDODisplay&fieldId=RDO&searchLinkProp=SearchRDOLinks', 550,500,false);\">");
            returnString.append("&nbsp;<a href=\"JavaScript:basicClear('RDODisplay');\">" + LABEL_CLEAR + "</a></td>");
            returnString.append("<input type=\"hidden\" name=\"RDO\" value=\"" + sRDOId + "\"> ");
            returnString.append("<script language=\"JavaScript\">document.forms[0]['RDO'].fieldLabel=\"RDO\";</script>");

            returnString.append("</tr>");
        }
        else
        {
            returnString.append("<tr>");

            returnString.append("<td class=\"label\"><label for=\"" +LABEL_POLICY +  "\">" + LABEL_POLICY + "</label></td>");
            returnString.append("<td class=\"field\">" + sPolicy + "&nbsp;</td>");

            returnString.append("<td class=\"label\"><label for=\"" + LABEL_OWNER +  "\">" + LABEL_OWNER + "</label></td>");
            returnString.append("<td class=\"field\">" + (String) objMap.get("owner") + "&nbsp;</td>");

            returnString.append("<td class=\"label\" >"+ LABEL_ACTIVEECRECO + "</td>");
            returnString.append("<td class=\"field\">" + (String)objMap.get("activeECRECO") + "&nbsp;</td>");

            returnString.append("</tr>");
            returnString.append("<tr>");

            returnString.append("<td class=\"label\"><label for=\"" +LABEL_DESIGN_RESPONSIBILITY +  "\">" + LABEL_DESIGN_RESPONSIBILITY + "</label></td>");
            returnString.append("<td class=\"field\" colspan=\"5\">" + sRDOName + "&nbsp;</td>");

            returnString.append("</tr>");
        }
        returnString.append("<input type=\"hidden\" name=\"originalPartType\" value=\"" + partType + "\">");
        returnString.append("<input type=\"hidden\" name=\"oldRDO\" value=\"" + sRDOId + "\">");

        String resultHTML = returnString.toString();
        return resultHTML;
    }

   /**
    * This method to check revision of part exists
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds input arguments.
    * @return a String containing the HTML content to display Bondpad.
    * @throws Exception if the operation fails
    * @since SCA 10-6
    */
    public int checkIfPartRevisionExists(Context context,String args[]) throws Exception
    {
        if (args == null || args.length < 1)
        {
        throw (new IllegalArgumentException());
        }

        matrix.db.Query query = new matrix.db.Query ();

        String type = args[0];
        String name = args[1];
        String rev = args[2];

        query.open (context);
        query.setBusinessObjectType(type);
        query.setBusinessObjectName(name);
        query.setBusinessObjectRevision("*");
        query.setVaultPattern("*");

        BusinessObjectList busList = query.evaluate(context);

        query.close(context);

        if(busList == null || busList.size() == 0)
        {
            return 0;
        }
        else
        {
            String strMessage = MqlUtil.mqlCommand(context,"execute program $1 -method $2 $3 $4 $5 $6 $7 $8 $9 $10",
                                                   "emxMailUtil","getMessage",
                                                   "emxFramework.ProgramObject.eServiceValidRevisionChange_if.NoCreate","3",
                                                   "Type",type,"Name",name,"Rev",rev);

            emxContextUtil_mxJPO.mqlNotice(context, strMessage);
            return 1;
        }
    }

        /**
         * This method is used while connecting or disconnecting the RDOs in Part Revise functionality
         *.and Go To Production functionality in case of Development Part.
         * @param context the eMatrix <code>Context</code> object
         * @param args holds a HashMap containing the following entries:
         * paramMap - a HashMap containing the following keys, "objectId", "old RDO Ids", "New OID".
         * @return Object - boolean true if the operation is successful
         * @throws Exception if operation fails
         * @since EngineeringCentral 10.6 - Copyright (c) 2005, MatrixOne, Inc.
         */
        public Object connectRDOsForRevise(Context context, String[] args)
                throws Exception
        {
                HashMap requestMap = (HashMap) JPO.unpackArgs(args);
                String strObjId = (String) requestMap.get("objectId");
                String strNewOrganizationId = (String) requestMap.get("New OID"); //"New Value");
                String [] oldRDOIds = (String[])requestMap.get("OLDRDOID");
                String strOldRDOIds = "";
                if(oldRDOIds != null && oldRDOIds.length > 0)
                {
                    strOldRDOIds = oldRDOIds[0];
                }
                if(strOldRDOIds==null || "null".equals(strOldRDOIds))
                {
                    strOldRDOIds = "";
                }
                if(strNewOrganizationId==null || "null".equals(strNewOrganizationId))
                {
                    strNewOrganizationId = "";
                }
                if(!strOldRDOIds.equals(strNewOrganizationId))
                {
                    DomainObject domObj = new DomainObject(strObjId);
                    StringList strListOldRDOIds = domObj.getInfoList(context,"to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id");
                    StringList strListOldRDORelIds = domObj.getInfoList(context,"to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY + "].id");

                    int newOIDIndex = strListOldRDOIds.indexOf(strNewOrganizationId);
                    int size = strListOldRDOIds.size();

                    for(int i = 0 ; i < size ; i++)
                    {
                        if(i != newOIDIndex || "".equals(strNewOrganizationId))
                        {
                            DomainRelationship.disconnect(context,(String)strListOldRDORelIds.get(i));
                        }
                    }
                    if(newOIDIndex == -1 && !"".equals(strNewOrganizationId))
                    {
                        // connect the ecr object to new RDO
                        setId(strNewOrganizationId);
                        DomainRelationship.connect(context,this,DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,new DomainObject(strObjId));
                    }
                }

                return Boolean.TRUE;
        }

        /**
         * This method is used get the policy with same policy classification as the current policy of a Part
         * @param context the eMatrix <code>Context</code> object
         * @param args holds a HashMap containing the following entries:
         * paramMap - a HashMap containing the following keys, "objectId", "old RDO Ids", "New OID".
         * @return String which contains HTML code to display a dropdown with all the policies
         * @throws Exception if operation fails
         * @since EngineeringCentral 10.6 - Copyright (c) 2005, MatrixOne, Inc.
         */

   public String getPolicyClassificationPolicies(Context context,
                                        String[] args)
       throws Exception
   {
       StringBuffer returnString = new StringBuffer();

       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       HashMap requestMap = (HashMap) programMap.get("requestMap");
       String languageStr = (String) requestMap.get("languageStr");
       String strMode=(String)requestMap.get("mode");
       String strPartId = (String)requestMap.get("objectId");
       DomainObject partObj = new DomainObject(strPartId);
       StringList strList = new StringList();
       strList.add(SELECT_POLICY);
       strList.add(SELECT_TYPE);
       Map map = partObj.getInfo(context,strList);
       String currentPartPolicyName = (String)map.get(SELECT_POLICY);
       String strType = (String)map.get(SELECT_TYPE);
       String currentPolicyClassification = EngineeringUtil.getPolicyClassification(context,currentPartPolicyName);

     if("edit".equalsIgnoreCase(strMode))
       {
         String isPolicyEdit = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Policy.EnablePartPolicyEditing");
         if ("true".equalsIgnoreCase(isPolicyEdit))
         {
             boolean hasChangePolicyAccess = FrameworkUtil.hasAccess(context,partObj,"changepolicy");
             if (hasChangePolicyAccess)
             {
                 BusinessType partBusType = new BusinessType(strType, context.getVault());
                 partBusType.open(context);
                 // Get the policies of that Object
                 PolicyList partPolicyList = partBusType.getPolicies(context);
                 PolicyItr  partPolicyItr  = new PolicyItr(partPolicyList);
                 partBusType.close(context);

                 while(partPolicyItr.next())
                 {
                    Policy partPolicy = partPolicyItr.obj();
                    String partPolicyName = partPolicy.getName();
                    String policyClassification = EngineeringUtil.getPolicyClassification(context,partPolicyName);
                    if (policyClassification.equalsIgnoreCase(currentPolicyClassification))
                      {
                         if(returnString.length()==0)
                          {
                               returnString.append("<select name=\"PolicyDisplay\">");
                           }
                  returnString.append("<option value=\""+partPolicyName+"\" "+(currentPartPolicyName.equals(partPolicyName)?"selected=\"true\"":"")+">"+i18nNow.getAdminI18NString("Policy",partPolicyName,languageStr)+"</option>");

                      }
                 }
                 if(returnString.length()!=0)
                 {
                     returnString.append("</select>");
                 }
               }
           }
       }

      if(returnString.length()==0)
      {
          returnString.append(i18nNow.getAdminI18NString("Policy",currentPartPolicyName,languageStr));
      }

      return returnString.toString();
   }   // end of method

	/**
	* This method is used to update the policy of a Part
	* @param context the eMatrix <code>Context</code> object
	* @param args holds a HashMap containing the following entries:
	* paramMap - a HashMap containing the following keys, "objectId", "old RDO Ids", "New OID".
	* @return Object - boolean true if the operation is successful
	* @throws Exception if operation fails
	* @since EngineeringCentral 10.6 - Copyright (c) 2005, MatrixOne, Inc.
	*/
   public Object updatePolicy(Context context, String[] args)
      throws Exception
    {
    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    HashMap paramMap = (HashMap) programMap.get("paramMap");
    HashMap requestMap = (HashMap) programMap.get("requestMap");
    String strObjId = (String) paramMap.get("objectId");

    String [] strNewPolicies = (String[]) requestMap.get("PolicyDisplay");
    String strNewPolicy = "" ;
    if(strNewPolicies!=null && !"null".equals(strNewPolicies))
    {
        strNewPolicy = strNewPolicies[0];

        Part partObj = new Part(strObjId);
        String strCurrentPolicy = partObj.getInfo(context,SELECT_POLICY); //Old Vale
        if(strNewPolicy!=null && !"null".equals(strNewPolicy) && !strNewPolicy.equalsIgnoreCase(strCurrentPolicy))
        {
			partObj.open(context);
			partObj.setPolicy(context, strNewPolicy);
			partObj.close(context);
        }
    }

	return Boolean.TRUE;
  }

   /**
    * This method is used to display the Yield command for  below types and its sub types
    *  Probed Wafer, Performance Graded Wafer, Unit Test, UT Performance Grade, UT Burn-In
    * @param context the eMatrix <code>Context</code> object
    * @param args holds a HashMap containing the following entries:
    * paramMap - a HashMap containing the following keys, "objectId", "old RDO Ids", "New OID".
    * @return Object - boolean true if the operation is successful
    * @throws Exception if operation fails
    * @since EC 10.6.SP2
    */
   public Object showYieldData(Context context, String[] args)
      throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strTypeName = FrameworkUtil.getType(context, new BusinessObject((String) programMap.get("objectId")));
        ArrayList alLotYieldTypes = new ArrayList(5);

        alLotYieldTypes.add(PropertyUtil.getSchemaProperty(context, "type_ProbedWafer"));
        alLotYieldTypes.add(PropertyUtil.getSchemaProperty(context, "type_PerformanceGradedWafer"));
        alLotYieldTypes.add(PropertyUtil.getSchemaProperty(context, "type_UnitTest"));
        alLotYieldTypes.add(PropertyUtil.getSchemaProperty(context, "type_UTPerformanceGrade"));
        alLotYieldTypes.add(PropertyUtil.getSchemaProperty(context, "type_UTBurnIn"));

        //check if the given type name exist in Lot Yield types
        if(alLotYieldTypes.indexOf(strTypeName) != -1)
        {
            return Boolean.TRUE;
        }
        //get the list of Parent Type Names
        StringList slParentList = new BusinessType(strTypeName, context.getVault()).getParents(context);

        if(slParentList != null)
        {
            return Boolean.FALSE;
        }
            //loop through the Parent type list till it finds the configured type
        for(int i=0; i<alLotYieldTypes.size(); i++)
        {
            if(isOfParentType(context, strTypeName, (String)alLotYieldTypes.get(i) ))
            {
                alLotYieldTypes.add(strTypeName);
                    return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Verifies whether the Child Type is derived from the Parent Type.
     *
     * @param context           the eMatrix <code>Context</code> object
     * @param childType         The child business type that needs to be checked
     * @param parentType        The parent business type under which the child
     *                          needs to be checked for being derived
     * @return                  booelan true if the child type is a derived type of the parent.
     *                          booelan false if the child type is not
     *
     * @throws Exception        if operation fails
     * @since EC 10.6.SP2
     */
    public static boolean isOfParentType(Context context, String childType, String parentType)
       throws FrameworkException
       {
           try
           {
               String subtypes = MqlUtil.mqlCommand(context,"print type $1 select $2 dump $3",parentType,"derivative","|");
               StringList projectTypes = FrameworkUtil.split(subtypes, "|");
               projectTypes.add(0, parentType);

               return ( projectTypes.indexOf(childType) != -1 );
           }
           catch(Exception e){
                throw (new FrameworkException(e));
           }
    }


      /**
        * Returns location of the manufacturer based on selected manufacturer.
        * @mx.whereUsed This method will be called when the user clicks on the "Manufacturer Location" field from Part Edit Details property page.Used in "edit" or
        *                           "view" mode of "Part" Webform. This field will be displayed for MEP Part only.
        * @mx.summary   This method will display the location name that are existing on the "To" side of the relationship "Manufacturing Location" associated with
        *                          selected Manufacturer in property page. In Edit Details Page, it will show the chooser button that will allow the user to choose a location
        *                          associated with selected Manufacturer
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds HashMap containing the following entries:
        * objectId of Part.
        *
        * @return a String  : manufacturer Location -->Part(if exist) as HTMLOutput .
        * @throws Exception if the operation fails.
        * @since EC 11-0
        */
         public String showManufacturerLocation(Context context,String[] args) throws Exception
         {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            //get mode for webform
            String sMode=(String) requestMap.get("mode");
            String sPartId = (String) requestMap.get("objectId");
            DomainObject domObj = new DomainObject(sPartId);

            //Getting connected Manufacturer
            String relManufacturingResponsibility =PropertyUtil.getSchemaProperty(context,"relationship_ManufacturingResponsibility");
            //Getting connected Manufacturing Location
            String relManufacturingLocation =PropertyUtil.getSchemaProperty(context,"relationship_ManufacturingLocation");
            StringList selectStmts = new StringList(4);
            selectStmts.addElement("from["+relManufacturingLocation+"].to.id");
            selectStmts.addElement("from["+relManufacturingLocation+"].to.name");
            selectStmts.addElement("to["+relManufacturingResponsibility+"].from.id");
            Map manufacturerMap=domObj.getInfo(context,selectStmts);
            String sManufacturingLocationName =(String) manufacturerMap.get("from["+relManufacturingLocation+"].to.name");
            String sManufacturingLocationId =(String) manufacturerMap.get("from["+relManufacturingLocation+"].to.id");
            String sManufacturerId =(String) manufacturerMap.get("to["+relManufacturingResponsibility+"].from.id");
            String output="";

            if(sManufacturingLocationName == null || "null".equalsIgnoreCase(sManufacturingLocationName))
            {
               sManufacturingLocationName="";
            }

            if(sManufacturerId == null || "null".equalsIgnoreCase(sManufacturerId))
            {
               sManufacturerId="";
            }

            //Display "Manufacturing Location" Field in Edit Mode
            if(sMode != null && "edit".equalsIgnoreCase(sMode))
            {
                StringBuffer outputBuffer = new StringBuffer(1024);
                outputBuffer.append("<input type=text READONLY name=\"manufacturingLocationDisplay\" size=\"16\"");
                outputBuffer.append("value=\""+sManufacturingLocationName+"\" >&nbsp;");
                outputBuffer.append(" <input type=\"button\" value=\"...\" name=\"btnManufacturerLocation\" onclick=\"javascript:showManufacturerLocation();\">");
                outputBuffer.append("<script language=\"javascript\">");
                outputBuffer.append("function showManufacturerLocation() {");
                //Retrieve selected Manufacturer Id from "Manufacturer" field
                outputBuffer.append(" var  manufacturerOID = document.editDataForm.ManufacturerOID.value;");
                outputBuffer.append(" if(manufacturerOID ==\"\") { ");
                outputBuffer.append(" manufacturerOID = \""+sManufacturerId+"\";} ");
                outputBuffer.append("var url =\"../common/emxSearch.jsp?typename=Location&toolbar=ENCSearchCompanyLocationToolbar&title=Location");
                outputBuffer.append("&rowselect=single&helpMarker=emxhelpsearchcompany&manufacturerId=\"+manufacturerOID+\"");
                outputBuffer.append("&fieldNameDisplay=manufacturingLocationDisplay&fieldNameId=ManufacturerLocationOID\";");
                outputBuffer.append("showModalDialog(url, 500, 500); }");
                outputBuffer.append("</script>");
                outputBuffer.append("<input type=\"hidden\" name=\"ManufacturerLocation\"  value=\""+sManufacturingLocationId+"\">");
                outputBuffer.append("<input type=\"hidden\" name=\"ManufacturerLocationOID\"  value=\""+sManufacturingLocationId+"\">");
                output=outputBuffer.toString();
            }
            else
            {
                //Display Manufacturing Location Name Field in View Mode
                output =sManufacturingLocationName;
            }
            return output;
         }

      /**
        * Update location of the manufacturer associated with Part
        * @mx.whereUsed This method will be called for the "Manufacturer Location" field when the user clicks on the "Done" from "Edit Details" page. Used in "Edit"
        *                           mode of "Part" WebForm.This field will be displayed for MEP Part only
        * @mx.summary   This method will connect selected location with newly created MEP with relationship "Manufacturing Location".
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds HashMap containing the following entries:
        * objectId of Part.
        * mode of form
        * @return a 0  : for successful updation
        * @throws Exception if the operation fails.
        * @since EC 11-0
        */
         public void updateManufacturerLocation(Context context,String[] args) throws Exception
         {
            HashMap programMap  = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            String sExistManufacturerLocationId = (String) paramMap.get("New Value");
            String sNewManufacturerLocationId = (String) paramMap.get("New OID");
            String partObjectId = (String)paramMap.get("objectId");

            try
            {
                if (partObjectId != null)
                {
                    //Getting connected Manufacturing Location
                    String relManufacturingLocation =PropertyUtil.getSchemaProperty(context,"relationship_ManufacturingLocation");
                    DomainObject partObj = new DomainObject(partObjectId);

                    ContextUtil.startTransaction(context, true);
                    if(sExistManufacturerLocationId !=null && !"null".equalsIgnoreCase(sExistManufacturerLocationId) && !"".equalsIgnoreCase(sExistManufacturerLocationId))
                    {
                           //get relationship Id between Part & Location
                          String sExistRelationshipId =  partObj.getInfo(context,"from["+relManufacturingLocation+"]."+DomainObject.SELECT_RELATIONSHIP_ID);

                          if(sNewManufacturerLocationId !=null && !"null".equalsIgnoreCase(sNewManufacturerLocationId) && !"".equalsIgnoreCase(sNewManufacturerLocationId))
                          {
                              if(!sExistManufacturerLocationId.equals(sNewManufacturerLocationId))
                              {
                                 // disconnect existing connection
                                 DomainRelationship.disconnect(context, sExistRelationshipId);

                                 // Connecting the selected Location with Part
                                  DomainRelationship.connect( context,
                                                              partObj, //the Part object to connect 'From'
                                                              relManufacturingLocation, //the relationship type used for the connection
                                                              new DomainObject(sNewManufacturerLocationId) //the Location object to connect 'To'
                                                            );
                              }
                          }
                          else
                          {
                              // disconnect existing connection
                              DomainRelationship.disconnect(context, sExistRelationshipId);
                          }
                    }
                    else
                    {
                          if(sNewManufacturerLocationId !=null && !"null".equalsIgnoreCase(sNewManufacturerLocationId) && !"".equalsIgnoreCase(sNewManufacturerLocationId))
                          {
                                 // Connecting the selected Location with Part
                                  DomainRelationship.connect( context,
                                                              partObj, //the Part object to connect 'From'
                                                              relManufacturingLocation, //the relationship type used for the connection
                                                              new DomainObject(sNewManufacturerLocationId) //the Location object to connect 'To'
                                                            );
                          }
                    }
                    //end
                    // commiting transaction
                    ContextUtil.commitTransaction(context);
                }
            }
            catch(Exception e)
            {
                    ContextUtil.abortTransaction(context);
                    throw new FrameworkException(e.toString());
            }
         }

      /**
        * Returns list of location of the manufacturer based on selected manufacturer.
        * @mx.whereUsed This method will be called when the user clicks on the "Manufacturer Equivalent Parts" link from My Desk -->Engineering menu.Used
        *                           "ENCManufacturerEquivalentParts" table.
        * @mx.summary   This method will return the list of location name that are existing on the "To" side of the relationship "Manufacturing Location" associated
                                    with selected Manufacturer in the summary page.
        * @param context the eMatrix <code>Context</code> object.
        * @param args holds HashMap containing the following entries:
        * objectId of Part.
        *
        * @return a Vector  : manufacturer Location -->Part(if exist)
        * @throws Exception if the operation fails.
        * @since EC 11-0
        */
         public static Vector showManufacturerLocations(Context context,String[] args) throws Exception
         {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            Vector columnVals   = null;
            // getting the MapList of the objects.
            MapList objList     = (MapList)programMap.get("objectList");

            int listSize = 0;
            Map map = null;
            String strObjectId = "";
            DomainObject domObj =null;

            if(objList != null && (listSize = objList.size()) > 0 )
            {
               columnVals   = new Vector(listSize);
               String relManufacturingLocation =PropertyUtil.getSchemaProperty(context,"relationship_ManufacturingLocation");
               String sManufacturingLocationName ="";
               for(int i = 0; i < listSize ; i++)
               {
                   map = (Map)objList.get(i);
                   strObjectId = (String)map.get(DomainObject.SELECT_ID);
                   domObj = new DomainObject(strObjectId);
                   sManufacturingLocationName =domObj.getInfo(context,"from["+relManufacturingLocation+"].to.name");

                   //check if any Manufacturing Location is connected to any company
                   if(sManufacturingLocationName != null && !"null".equals(sManufacturingLocationName) && !"".equals(sManufacturingLocationName))
                   {
                        columnVals.add(sManufacturingLocationName);
                   }
                   else
                   {
                        columnVals.add("");
                   }
               }//end of for loop
            }

            return columnVals;
         }

       /**
         * Returns true if the MCC is installed  and passed  compliance definition exists in the system otherwise false.
         * @mx.whereUsed This method will be called from part list pages. Used as access program to show/hide the compliance definition columns added to EC
         *                           tables in EC install. It used in following tables mentioned below :ENCPartSearchResult, ENCGeneralSearchResult,
         *                           ENCEBOMIndentedSummary, ENCEBOMSummary, ENCManufacturerEquivalentParts.
         * @mx.summary   This method is for showing the Compliance columns only in the EC table if the respective Compliance Definitions exists in the system.The
         *                           logic in the access program for each column would be : First Check whether MCC is installed using the
         *                           FrameworkUtil.isSuiteRegistered(context,"appVersionMaterialsComplianceCentral",false,null,null)method, if MCC is installed then check
         *                           whether the context Compliance Definition does exists in the system by doing a find object with type as 'Compliance Definition'  and name
         *                           as Compliance Definition name passed in the Applies To setting. If all above conditions are met then returns "true" else "false".
         *                           Example : For "RoHS" column,following is the setting:
         *                           Applies To = RoHS Then used DomainObject.findObjects() which takes  Type: "Compliance Definition"
         *                            Name: "RoHS" Revision:"*" to search for compliance definition exists in the system or not.
         * @param context the eMatrix <code>Context</code> object.
         * @return boolean true or false based condition.
         * @throws Exception if the operation fails.
         * @since EC 11-0
         */
         public boolean hasAccessToComplianceDefinition (Context context ,String[] args) throws Exception
         {
            if(isMCCInstalled(context,args))
            {
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                HashMap requestValuesMap = (HashMap) programMap.get("RequestValuesMap");
                HashMap settings=(HashMap)programMap.get("SETTINGS");
                String sComplianceDefinitionName =(String)settings.get("Applies To");
                boolean isAccessMEP = false;

                if("OutOfDate".equalsIgnoreCase(sComplianceDefinitionName))
                {
                    return true;
                }

                StringList objSelects = new StringList();
                objSelects.addElement(SELECT_NAME);
                objSelects.addElement(SELECT_ID);
                String sComplianceDefinitionType  = PropertyUtil.getSchemaProperty(context, "type_ComplianceDefinition");
                //check if passed  compliance definition exists in the system otherwise false
                MapList finalList=DomainObject.findObjects(   context,
                                                              sComplianceDefinitionType,
                                                               sComplianceDefinitionName,
                                                               "*",
                                                               "*",
                                                               "*",
                                                               "",
                                                               true,
                                                               objSelects
                                                           );

                if(finalList.size() > 0)
                {
                    // check for "C/I" column as it is applicable to MEP only
                    if (settings != null && settings.get("Column Name") != null && "C/I".equals(((String) settings.get("Column Name"))))
                     {
                        // check if selected policy is MEP.if "Yes" then return true else false
                        // This check required for the EC Part search if MEP is also selected then  need to show the
                        // Equivalent sepecif Compliance Definition C/I.
                        if(requestValuesMap!=null && requestValuesMap.get("txtPolicy") !=null)
                        {
                            String str[] = (String []) requestValuesMap.get("txtPolicy");

                            for(int i=0;i<str.length;i++)
                            {
                            	if(str[i].equals(DomainConstants.POLICY_MANUFACTURER_EQUIVALENT))
                               {
                                  isAccessMEP =true;
                                  break;
                               }
                            }
                            return  isAccessMEP;
                        }
                        return true;
                     }
                     else
                     {
                        return true;
                     }
                }
                else
                {
                     return false;
                }
            }
            else
            {
                return false;
            }
         }

        /**
         * Returns true if the MCC is installed otherwise false.
         * @mx.whereUsed This method will be called from part property and list pages
         * @mx.summary   This method check whether MCC is installed or not, this method can be used as access program to show/hide the compliance definition
         *                          columns added to EC tables in EC install. Given below is a code:
         *                          FrameworkUtil.isSuiteRegistered(context,"appVersionMaterialsComplianceCentral",false,null,null)
         * @param context the eMatrix <code>Context</code> object.
         * @return boolean true or false based condition.
         * @throws Exception if the operation fails.
         * @since EC 11-0
         */
         public boolean isMCCInstalled (Context context ,String[] args) throws Exception
         {
            return FrameworkUtil.isSuiteRegistered(context,"appVersionMaterialsComplianceCentral",false,null,null);
         }

      //EC-MCC Interoperability
      /**
       * Method to associate External interface to the equivalent part
       * and set the "Enable Compliance" attribute as "Enabled".
       *
       * @mx.whereUsed Invoked by the trigger object on creation of Part-
       *               TypePartCreateAction,
       * @mx.summary This method associates External Part Data
       *             interface to a Equivalent part. Uses
       *             <code>MqlUtil.mqlCommand</code> to associate the interface
       *             to the part. Uses <code>DomainObject.setAttribute() </code>to
       *             set an attribute value
       * @param context
       *            the eMatrix <code>Context</code> object
       * @param sPartPolicyClassification
       *            <code>String</code>
       * @throws FrameworkException
       *             if the operation fails
       * @since EC 10-7
       */
      public int associateExternalInterface(Context context, String args[])throws Exception {
        int status = 0;
        try {
            // Get Created Part ObjectId from Environment Variables
            String sExternalPartData = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_interface_ExternalPartData);
            String sObjectId = MqlUtil.mqlCommand(context, "get env $1","OBJECTID");
            boolean isMEP = false;

            if (sObjectId !=null && !"".equals(sObjectId)) {
                Part part = new Part(sObjectId);
                String sPolicyClassification = part.getInfo(context, "policy.property[PolicyClassification].value");

                // enable MEP by default
                if(sPolicyClassification != null && "Equivalent".equals(sPolicyClassification)){
                    isMEP = true;
                }
                if(isMEP)
                {
                     MqlUtil.mqlCommand(context, "modify bus $1 add interface $2;",sObjectId,sExternalPartData);
                }
            }
        } catch (Exception e) {
          status = 1;
          throw new FrameworkException(e);
        } finally {
          return status;
        }
      }

    /**
    * Gives the list of type(s) for comparison based on if mbom is installed or not.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @returns an Object a hashmap containing type(s) for comparison.
    * @throws Exception if the operation fails.
    * Since EC X3
    */
    public Object getCompareType (Context context,String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        // to get the name of the field invoking the JPO method
        String sName = (String)fieldMap.get("name");
		String  strBOM ="EBOM";
        String sStrResourceValue= null;

        Map mCommandMap         =       null;
        String sCommandLabel    =       null;

        UIMenu uiMenu           =       new UIMenu();
        StringBuffer strBuf     =       new StringBuffer();
        // to get all the commands in the menu
        MapList mapList = uiMenu.getMenu(context, PropertyUtil.getSchemaProperty(context,"menu_ENCBOMCompareTypeField"), null);
        int mapListSize=mapList.size();

        if(mapListSize>0) {
            // to name the field based on the field calling the method
            if ( sName != null && "Type1".equals(sName) )   {
                strBuf.append("<SELECT name=\"Type1\" >");
            }   else {
                strBuf.append("<SELECT name=\"Type2\" >");
            }

            for(int i=0; i<mapListSize;i++)     {
                // retreive the labels of the command
                mCommandMap = (Map)mapList.get(i);
                sCommandLabel = (String) mCommandMap.get("label");
                // get the string resource values and add them to the HTML code
                //Multitenant
                sStrResourceValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),sCommandLabel);
                strBuf.append("<OPTION value="+strBOM+">"+sStrResourceValue+" </OPTION>");
            }
           strBuf.append("</SELECT>");
        }
       return strBuf.toString();
    }

    /**
    * Gives the list of revision option(s) for comparison.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @returns an Object containing hashmap of revision options for comparison.
    * @throws Exception if the operation fails.
    * Since EC X3
    *
    *  R210 - Modified for ECC Reports
    */
    public Object getRevisionOptions (Context context,String[] args)
    throws Exception
    {
        HashMap hmRangeMap = new HashMap();
        StringList result = new StringList();
        StringList choiceVal        = new StringList();
        // Obtaining Value from String Resource File
        //Reading options from propeties file which are comma seperated
        String sRevisionOptions  = EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.BOMCompareForm.RevisionOptions");
        // tokenize values
        StringTokenizer strToken    = new StringTokenizer(sRevisionOptions,",");
        String sTokenVal            = null;
        String sTokenTemp = "";
        String sTokenName = "";

        /// Added by PL for ECC Reports
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap	requestMap = (HashMap) programMap.get("requestMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");

		String sobjectId1 =(String)requestMap.get("objectId");
		String sFieldName = (String)fieldMap.get("name");
		boolean isConfigPart1 = false;
		/* PL : ECC Reports */
		if(!UIUtil.isNullOrEmpty(sobjectId1)) {
		 isConfigPart1 = isConfigurablePart(context, sobjectId1);
		}

		if ("RevisionOption1".equals(sFieldName)) {
			if (isConfigPart1) {
				result.addElement("As Stored");
				choiceVal.addElement("As Stored");
			} else {

    int iCountTokens = strToken.countTokens();
    for(int i=0 ; i < iCountTokens ; i++)   {
                sTokenTemp = strToken.nextToken();

                //Multitenant
                sTokenName = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", new Locale("en"),"emxEngineeringCentral.BOMCompareForm.RevisionOptions."+sTokenTemp);
                //Getting i18n string for tokens
                //Multitenant
                sTokenVal = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.BOMCompareForm.RevisionOptions."+sTokenTemp);
                // Add the display value to the map
                result.addElement(sTokenVal);
                // Remove the spaces and add the choice value to the map
                sTokenVal = sTokenVal.replace(' ','_');
                choiceVal.addElement(sTokenName);
            }
			}
		} else {

    int iCountTokens = strToken.countTokens();

    for(int i=0 ; i < iCountTokens ; i++)   {
        sTokenTemp = strToken.nextToken();

      //Multitenant
        sTokenName = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", new Locale("en"),"emxEngineeringCentral.BOMCompareForm.RevisionOptions."+sTokenTemp);

//      Getting i18n string for tokens
      //Multitenant
        sTokenVal = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.BOMCompareForm.RevisionOptions."+sTokenTemp);
        // Add the display value to the map
                result.addElement(sTokenVal);
                // Remove the spaces and add the choice value to the map
                sTokenVal = sTokenVal.replace(' ','_');
                choiceVal.addElement(sTokenName);
            }
		}

        // Add the display and choice value to the map
        hmRangeMap.put("field_choices",choiceVal);
        hmRangeMap.put("field_display_choices",result);

        return hmRangeMap;
    }

    /**
    * Gives the list of match based on criteria for comparison.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @returns an Object containing hashmap of match based on criterias for comparison.
    * @throws Exception if the operation fails.
    * Since EC X3
    *
    *  R201 - Modified for ECC Reports.
    *
    */
    public Object getMatchBasedOn (Context context,String[] args)
    throws Exception
    {
        MapList columns             = null;
        Map TableMap                = null;
        Map TableSettingMap         = null;
        String strMBO               = null;
        String sLabel               = null;
        String sStrResourceValue    = null;
        StringBuffer strBuf         = new StringBuffer();
        UITableCommon uiTable       = new UITableCommon();
        // to get all the table columns and assign it to map
        columns                     = uiTable.getColumns(context,PropertyUtil.getSchemaProperty(context,"table_ENCBOMCompareVisualTableReport"),null);
        int MapListSize             = columns.size();

		// ADDED FOR ECC REPORTS .st
  		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap	requestMap = (HashMap) programMap.get("requestMap");

		String sobjectId =(String)requestMap.get("objectId");

		boolean isConfigPart1 = false;

		if(null != sobjectId && !"null".equals(sobjectId))
		{
			isConfigPart1 = isConfigurablePart(context, sobjectId);
		}

		/* ECC Reports . en*/

        if(MapListSize>0)   {
        	//Multitenant
        	String sStrResFindNumber = (EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.label.FindNumber")).trim();
            strBuf.append("<SELECT name=\"MatchBasedOn\" >");
			String strResourceVal = "";
            for(int i=0 ;i<MapListSize;i++)     {

                // To get the setting of table columns
                TableMap = (Map)columns.get(i);
                TableSettingMap = (Map)TableMap.get("settings");
                strMBO  =(String)TableSettingMap.get("MatchBasedOn");
                sLabel=(String)TableMap.get("label");
                // Obtaining Value from String Resource File
              //Multitenant
				if(UIUtil.isNotNullAndNotEmpty(sLabel))
                sStrResourceValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),sLabel);
                //START 099165V6R2012
                if(null != sStrResourceValue) {
                	sStrResourceValue = sStrResourceValue.trim();
                }
                //END 099165V6R2012
              //Multitenant
				if(UIUtil.isNotNullAndNotEmpty(sLabel))
                strResourceVal = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource",new Locale("en"),sLabel);
                String sSelected = "";
                if (sStrResourceValue != null && sStrResourceValue.equalsIgnoreCase(sStrResFindNumber))
                        {
                            sSelected = "selected";
                        }
                if("true".equalsIgnoreCase(strMBO)) {

                        String sVal= strResourceVal.replace(' ','_');
                        sVal=StringUtils.replace(sVal,"_+_","_");

                   //if (!(isConfigPart1 && "Part_Name".equals(sVal))) {
                        {
                        strBuf.append("<OPTION "+sSelected +" value=\"");
                        // Add the display value to the string buffer
                        strBuf.append(XSSUtil.encodeForHTMLAttribute(context,sLabel));
                        strBuf.append("\">");
                        strBuf.append(XSSUtil.encodeForHTMLAttribute(context,sStrResourceValue));
                    }
              }
              }
              strBuf.append("</SELECT>");
        }
        return strBuf.toString();
    }

    /**
    * Gives the list of Report Differences on criteria for comparison.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @returns an Object containing HTML output for Report Differences for comparison.
    * @throws Exception if the operation fails.
    * Since EC X3
    */
	public Object getReportDifferences(Context context, String[] args)
			throws Exception {
		MapList columns = null;
		Map TableMap = null;
		Map TableSettingMap = null;
		String strComparable = null;
		String strFieldLabel = null;
		String strFieldValue = null;
		String sVal = null;

		//Multitenant
		String strName = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common.PartName");

		//Multitenant
		String strRevision = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common.Revision");

		//Multitenant
		String strQty = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common.Qty");
		boolean MBOMInstalldFlag 			= 		com.matrixone.apps.engineering.EngineeringUtil.isMBOMInstalled(context);


		StringBuilder strBuf = new StringBuilder(512);
		UITableCommon uiTable = new UITableCommon();
        String strEBOMMBOMVisualReportTable = PropertyUtil
		.getSchemaProperty(context,"table_ENCBOMCompareVisualTableReport");
		columns = uiTable.getColumns(context, strEBOMMBOMVisualReportTable,
				null);
		int MapListSize = columns.size();
		int cnt = 0;
		boolean isField = false; //Used for indentation - As part of new BOM compare feature
		 //R212 changes: get labels for select all and reset from properties
		//Multitenant
		String sSelectAll = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common.SelectAll");
        //end R212 change
		if (MapListSize > 0)
		{
			String strFieldNameForCtrl = "";
			strBuf.append("<table>");
			for (int i = 0; i < MapListSize; i++)
			{
				isField = false;
				TableMap = (Map) columns.get(i);
				TableSettingMap = (Map) TableMap.get("settings");
				strComparable = (String) TableSettingMap.get("Comparable");
				strFieldLabel = (String) TableMap.get("label");
				if (strFieldLabel != null && !"null".equals(strFieldLabel))
				{
					if (strFieldLabel.indexOf("emxMBOM") != -1)
					{
						//Multitenant
						strFieldValue = EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", context.getLocale(),strFieldLabel);
						//Multitenant
						strFieldNameForCtrl = EnoviaResourceBundle.getProperty(context, "emxMBOMStringResource", new Locale("en"),strFieldLabel);
						sVal = strFieldNameForCtrl.replace(" ", "_");
					} else
					{
						//Multitenant
						strFieldValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),strFieldLabel);
						//Multitenant
						strFieldNameForCtrl = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", new Locale("en"),strFieldLabel);
						sVal = strFieldNameForCtrl.replace(" ", "_");
					}
					if ("true".equalsIgnoreCase(strComparable)
							&& (strFieldValue.equals(strName)
									|| strFieldValue.equals(strRevision) || strFieldValue
									.equals(strQty)))
					{
						cnt++;
						isField = true;
						strBuf.append("<td>&nbsp;<input type=\"checkbox\" id=\"repDiffChk_default\" checked name=\""
										+ sVal
										+ "\" value=\"true\"/>"
										+"   "
										+ strFieldValue
										+"&nbsp;</td>");

					} else if ("true".equalsIgnoreCase(strComparable))
					{
						cnt++;
						isField = true;
						if("Substitute_For".equals(sVal) && !MBOMInstalldFlag) {
							// dispabling if MBOM is not installed
							strBuf.append("<td>&nbsp;<input type=\"checkbox\" id=\"repDiffChk\" name=\"" + sVal
									+ "\" value=\"false\" disabled/>"+"   " + strFieldValue+ "&nbsp;</td>");
							strBuf.append("</td></tr><tr><td>");
						}			  else {
						strBuf.append("<td>&nbsp;<input type=\"checkbox\" id=\"repDiffChk\" name=\"" + sVal
								+ "\" value=\"true\"/>"+"   " + strFieldValue+ "&nbsp;</td>");
					}
					}
				}
				else
				{
					continue;
				}

				if(cnt >= 3 && cnt % 3 == 0 && isField)
				{
					strBuf.append("<tr></tr>");
				}
			}
			strBuf.append("</table>");
		}
		strBuf.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		strBuf.append("<br>");

		strBuf.append("&nbsp;<input type =\"checkbox\" name = selectAll onclick=\"javascript:selectAllOptions(\'repDiffChk\');\"> ");
		strBuf.append(sSelectAll);
		return strBuf.toString();
}

    /**
    * Gives the list of Report Differences on criteria for comparison.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @returns an Object containing HTML output for Expand level criteria's for comparison.
    * @throws Exception if the operation fails.
    * Since EC X3
    */
    public Object getExpandLevel (Context context,String[] args)
    throws Exception
    {
        StringBuffer strBuf = new StringBuffer(256);
        // the Expand level drop down
        strBuf.append("<table><td>");
        strBuf.append("<SELECT name=\"ExpandLevel\" > <OPTION value=\"1\" SELECTED >1 <OPTION value=\"2\">2 <OPTION value=\"3\">3 <OPTION value=\"4\">4 <OPTION value=\"5\">5 <OPTION value=\"0\">All </SELECT>");
        strBuf.append("</td><td><div>&nbsp;&nbsp;&nbsp</div></td><td>");

        strBuf.append("</td></table>");
        return strBuf.toString();

    }

    public Object getFormats (Context context,String[] args)
    throws Exception
    {
        StringBuffer strBuf = new StringBuffer(128);
        //Added for bug 351866 starts
        //Multitenant
        String StructuredReport = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.CompareBOM.StructuredReport");
        //Multitenant
        String ConsolidatedReport = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.CompareBOM.ConsolidatedReport");
        strBuf.append("<input type=radio checked name='isConsolidatedReport' value='No'>");
		strBuf.append(XSSUtil.encodeForHTMLAttribute(context, StructuredReport));
		strBuf.append(' ');
        strBuf.append("<input type=radio name='isConsolidatedReport' value='Yes'>");
        strBuf.append(XSSUtil.encodeForHTMLAttribute(context,ConsolidatedReport));
        return strBuf.toString();
    }

    /**
    * Gives the Part Name for user to view.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds HashMap containing the following entries:
    * objectId of the context Part.
    * @returns an Object contanining the String Name for Display.
    * @throws Exception if the operation fails.
    * Since EC X3
    *
    * Modified for ECC Reports
    */
    public Object getPartNameDisplay (Context context,String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String sName = (String)fieldMap.get("name");

        StringBuffer strBuf = new StringBuffer(64);
        // to name the 2 revision fields of compare dialog differently
        if ( sName != null && sName.equals("BOM1Name") )
        {

            String sobjectId =(String)requestMap.get("objectId");

            if(null != sobjectId)
            {
	      		boolean isConfigPart = true;
	    		if(null != sobjectId)
	    		{
	    			/*  ECC Reports */
	    			isConfigPart = isConfigurablePart(context, sobjectId);
	    		}

	            // creating the Context Part domain object, retreiving the revision
	            DomainObject dPart = DomainObject.newInstance(context, sobjectId);
	            String strName =  dPart.getInfo(context,DomainObject.SELECT_NAME);
	            String sRevision = dPart.getInfo(context,DomainConstants.SELECT_REVISION);

	            if(isConfigPart)
	            {
	            	String sLastid 		= getLatestRelRevision(context,sobjectId);
	            	DomainObject dPart1 = DomainObject.newInstance(context, sLastid);
	            	sRevision 			= dPart1.getInfo(context, DomainObject.SELECT_REVISION);
	            	sobjectId			= sLastid;
	            }
	            else
	            {
	            	sRevision 			= dPart.getInfo(context,DomainConstants.SELECT_REVISION);
	            }
	            // Html code to display the part revision and to make it readonly
	            strBuf.append("<input type=\"text\" disabled name=\"BOM1NameDisplay\" value=\""+strName+"\">");
	            strBuf.append("<input type=\"hidden\" name=\"BOM1NameOID\" value=\""+sobjectId+"\">");
				strBuf.append("<input type=\"hidden\" name=\"BOM1NameDispOID\" value=\""+strName+"\">");
	            strBuf.append("<input type=\"hidden\" name=\"BOM1Rev\" value=\""+sRevision+"\">");
	            /* ECC Reports */
	            strBuf.append("<input type=\"hidden\" name=\"BOM1CofigPart\" value=\""+isConfigPart+"\">");
            }
        }
        return strBuf.toString();
    }

    //R212 changes for BOM2 Part Name Type Ahead support
    /**
     * Gives the Hidden fields required for Part Name2.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds HashMap containing the following entries:
     * objectId of the context Part.
     * @returns an Object contanining the list of hidden fields.
     * @throws Exception if the operation fails.
     * Since EC 2012x
     *
     */

     public Object getBOM2HiddenFields (Context context,String[] args)
     throws Exception
     {
         // to obtain the part objectId
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         HashMap requestMap = (HashMap) programMap.get("requestMap");

         StringBuffer strBuf = new StringBuffer(64);
         // to name the 2 revision fields of compare dialog differently
            String sobjectId =(String)requestMap.get("objectId2");
         HashMap requestValuesMap = (HashMap)requestMap.get("RequestValuesMap");

         if(null == sobjectId)
         {
        	 String[] objectId2 = (String[])requestValuesMap.get("objectId2");
        	 sobjectId = objectId2[0];
         }

            if (null != sobjectId && !"null".equals(sobjectId))
            {
                // creating the Context Part domain object, retreiving the revision
        DomainObject dPart = DomainObject.newInstance(context, sobjectId);
                String strName =  dPart.getInfo(context,DomainObject.SELECT_NAME);
				String sRevision = dPart.getInfo(context,DomainConstants.SELECT_REVISION);
                // Html code to display the part revision and to make it readonly

                /*  ECC Reports */
		boolean isConfigPart = isConfigurablePart(context, sobjectId);
 		        strBuf.append("<input type=\"hidden\" name=\"BOM2PreloadName\" value=\""+strName+"\">");
 		        strBuf.append("<input type=\"hidden\" name=\"BOM2PreloadID\" value=\""+sobjectId+"\">");
				strBuf.append("<input type=\"hidden\" name=\"BOM2NameDispOID\" value=\""+strName+"\">");
				strBuf.append("<input type=\"hidden\" name=\"BOM2Revision\" value=\""+sRevision+"\">");
		/* PL : ECC Reports */
		strBuf.append("<input type=\"hidden\" name=\"BOM2CofigPart\" value=\""+isConfigPart+"\">");
            }
            else
            {
                // Html code to display the part revision and to make it readonly
            	strBuf.append("<input type=\"hidden\" name=\"BOM2PreloadName\" value=\"\">");
  		        strBuf.append("<input type=\"hidden\" name=\"BOM2PreloadID\" value=\"\">");
				strBuf.append("<input type=\"hidden\" name=\"BOM2NameDispOID\" value=\"\">");
				strBuf.append("<input type=\"hidden\" name=\"BOM2Revision\" value=\"\">");
		/* PL : ECC Reports */
		strBuf.append("<input type=\"hidden\" name=\"BOM2CofigPart\" value=\"false\">");
            }
        return strBuf.toString();
    }
    //end BOM2 changes

     public Object getBOM1HiddenFields (Context context,String[] args)
     throws Exception
     {
         // to obtain the part objectId
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         HashMap requestMap = (HashMap) programMap.get("requestMap");

         StringBuffer strBuf = new StringBuffer(64);
         // to name the 1 revision fields of compare dialog differently
         String sobjectId =(String)requestMap.get("objectId1");
         HashMap requestValuesMap = (HashMap)requestMap.get("RequestValuesMap");

         if(null == sobjectId)
         {
        	 sobjectId =(String)requestMap.get("objectId");

        	 if(null == sobjectId)
        	 {
        		 String[] objectId1 = (String[])requestValuesMap.get("objectId");
        		 sobjectId = objectId1[0];
        	 }
         }

            if (null != sobjectId && !"null".equals(sobjectId))
            {
                // creating the Context Part domain object, retreiving the revision
            	DomainObject dPart = DomainObject.newInstance(context, sobjectId);
                String strName =  dPart.getInfo(context,DomainObject.SELECT_NAME);
				String sRevision = dPart.getInfo(context,DomainConstants.SELECT_REVISION);
                // Html code to display the part revision and to make it readonly

                /*  ECC Reports */
				boolean isConfigPart = isConfigurablePart(context, sobjectId);
 		        strBuf.append("<input type=\"hidden\" name=\"BOM1PreloadName\" value=\""+strName+"\">");
 		        strBuf.append("<input type=\"hidden\" name=\"BOM1PreloadID\" value=\""+sobjectId+"\">");
				strBuf.append("<input type=\"hidden\" name=\"BOM1NameDispOID\" value=\""+strName+"\">");
				strBuf.append("<input type=\"hidden\" name=\"BOM1Revision\" value=\""+sRevision+"\">");

				/* PL : ECC Reports */
				strBuf.append("<input type=\"hidden\" name=\"BOM1CofigPart\" value=\""+isConfigPart+"\">");
            }
            else
            {
                // Html code to display the part revision and to make it readonly
            	strBuf.append("<input type=\"hidden\" name=\"BOM1PreloadName\" value=\"\">");
  		        strBuf.append("<input type=\"hidden\" name=\"BOM1PreloadID\" value=\"\">");
				strBuf.append("<input type=\"hidden\" name=\"BOM1NameDispOID\" value=\"\">");
				strBuf.append("<input type=\"hidden\" name=\"BOM1Revision\" value=\"\">");
				/* PL : ECC Reports */
				strBuf.append("<input type=\"hidden\" name=\"BOM1CofigPart\" value=\"false\">");
            }
        return strBuf.toString();
    }
    //end BOM1 changes

    /**
    * Gives the Part Revision field with the selected parts revision for user to view.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds HashMap containing the following entries:
    * objectId of the context Part.
    * name of the webform field invoking the method
    * @returns an Object contanining HTML content for revision field and the revision of the context part to be displayed.
    * @throws Exception if the operation fails.
    * Since EC X3
    *
	* R210 Modified for ECC Reports
    */
    public Object getPartRevisionDisplay (Context context,String[] args)
    throws Exception
    {
        // to obtain the part objectId
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
//        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
 		HashMap requestValuesMap = (HashMap)requestMap.get("RequestValuesMap");

        String sName = (String)fieldMap.get("name");

        StringBuffer strBuf = new StringBuffer(64);
        // to name the 2 revision fields of compare dialog differently

        if ( sName != null && sName.equals("BOM1Revision") )
        {
            String sobjectId =(String)requestMap.get("objectId");

	       /* PL : ECC Reports */

	 	   if(null != sobjectId)
	 	   {
	 		   boolean isConfigPart = isConfigurablePart(context, sobjectId);

		 	   // creating the Context Part domain object, retreiving the revision
		 	   DomainObject dPart = DomainObject.newInstance(context, sobjectId);
		 	   String sRevison =  dPart.getInfo(context,DomainObject.SELECT_REVISION);

		 	   /* PL : ECC Reports */
		 	   //String sRevison =  "";

		 	   if(isConfigPart)
		 	   {
		 		   String sLastid 		= getLatestRelRevision(context,sobjectId);
		 		   DomainObject dPart1 = DomainObject.newInstance(context, sLastid);
		 		   sRevison 			= dPart1.getInfo(context, DomainObject.SELECT_REVISION);
		 	   }
		 	   else
		 	   {
		 		   sRevison =  dPart.getInfo(context,DomainObject.SELECT_REVISION);
		 	   }

	            // Html code to display the part revision and to make it readonly
	            strBuf.append("<input type=\"text\" disabled name=\"BOM1RevisionDisplay\" value=\""+XSSUtil.encodeForHTMLAttribute(context, sRevison)+"\">");
	            strBuf.append("<input type=\"hidden\" name=\"BOM1RevisionOID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sRevison)+"\">");
	 	   }
	 	   else
	 	   {
	 		   strBuf.append("<input type=\"text\" disabled name=\"BOM1RevisionDisplay\" value=\"\">");
	           strBuf.append("<input type=\"hidden\" name=\"BOM1RevisionOID\" value=\"\">");
	 	   }
 		   sobjectId =(String)requestMap.get("objectId1");
          if(null == sobjectId)
          {
         	 sobjectId =(String)requestMap.get("objectId");

         	 if(null == sobjectId && null!=requestValuesMap && !"null".equals(requestValuesMap))
         	 {
         		 String[] objectId1 = (String[])requestValuesMap.get("objectId");
         		 sobjectId = objectId1[0];
         	 }
          }

             if (null != sobjectId && !"null".equals(sobjectId))
             {
                 // creating the Context Part domain object, retreiving the revision
             	DomainObject dPart = DomainObject.newInstance(context, sobjectId);
                 String strName =  dPart.getInfo(context,DomainObject.SELECT_NAME);
 				String sRevision = dPart.getInfo(context,DomainConstants.SELECT_REVISION);
                 // Html code to display the part revision and to make it readonly

                 /*  ECC Reports */
 				boolean isConfigPart = isConfigurablePart(context, sobjectId);
  		        strBuf.append("<input type=\"hidden\" name=\"BOM1PreloadName\" value=\""+XSSUtil.encodeForHTMLAttribute(context,strName)+"\">");
  		        strBuf.append("<input type=\"hidden\" name=\"BOM1PreloadID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sobjectId)+"\">");
 				strBuf.append("<input type=\"hidden\" name=\"BOM1NameDispOID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,strName)+"\">");
 				strBuf.append("<input type=\"hidden\" name=\"BOM1Revision\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sRevision)+"\">");

 				/* PL : ECC Reports */
 				strBuf.append("<input type=\"hidden\" name=\"BOM1CofigPart\" value=\""+isConfigPart+"\">");
             }
             else
             {
                 // Html code to display the part revision and to make it readonly
             	strBuf.append("<input type=\"hidden\" name=\"BOM1PreloadName\" value=\"\">");
   		        strBuf.append("<input type=\"hidden\" name=\"BOM1PreloadID\" value=\"\">");
 				strBuf.append("<input type=\"hidden\" name=\"BOM1NameDispOID\" value=\"\">");
 				strBuf.append("<input type=\"hidden\" name=\"BOM1Revision\" value=\"\">");
 				/* PL : ECC Reports */
 				strBuf.append("<input type=\"hidden\" name=\"BOM1CofigPart\" value=\"false\">");
             }
        }
        else
        {
            String sobjectId =(String)requestMap.get("objectId2");
            if (sobjectId != null)
            {
                // creating the Context Part domain object, retreiving the revision
                DomainObject dPart = DomainObject.newInstance(context, sobjectId);
                String sRevison =  dPart.getInfo(context,DomainObject.SELECT_REVISION);

                // Html code to display the part revision and to make it readonly
                strBuf.append("<input type=\"text\" disabled name=\"BOM2RevisionDisplay\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sRevison)+"\">");
                strBuf.append("<input type=\"hidden\" name=\"BOM2RevisionOID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sRevison)+"\">");
            }
            else
            {
            // Html code to display the part revision and to make it readonly
            strBuf.append("<input type=\"text\" disabled name=\"BOM2RevisionDisplay\" value=\"\">");
            strBuf.append("<input type=\"hidden\" name=\"BOM2RevisionOID\" value=\"\">");
            }
          if(null == sobjectId && null!=requestValuesMap && !"null".equals(requestValuesMap))
          {
         	 String[] objectId2 = (String[])requestValuesMap.get("objectId2");
         	 sobjectId = objectId2[0];
          }
 		          if(null == sobjectId && null!=requestValuesMap && !"null".equals(requestValuesMap))
          {
         	 String[] objectId2 = (String[])requestValuesMap.get("objectId2");
         	 sobjectId = objectId2[0];
        }

             if (null != sobjectId && !"null".equals(sobjectId))
             {
                 // creating the Context Part domain object, retreiving the revision
         DomainObject dPart = DomainObject.newInstance(context, sobjectId);
                 String strName =  dPart.getInfo(context,DomainObject.SELECT_NAME);
 				String sRevision = dPart.getInfo(context,DomainConstants.SELECT_REVISION);
                 // Html code to display the part revision and to make it readonly

                 /*  ECC Reports */
 		boolean isConfigPart = isConfigurablePart(context, sobjectId);
  		        strBuf.append("<input type=\"hidden\" name=\"BOM2PreloadName\" value=\""+XSSUtil.encodeForHTMLAttribute(context,strName)+"\">");
  		        strBuf.append("<input type=\"hidden\" name=\"BOM2PreloadID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sobjectId)+"\">");
 				strBuf.append("<input type=\"hidden\" name=\"BOM2NameDispOID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,strName)+"\">");
 				strBuf.append("<input type=\"hidden\" name=\"BOM2Revision\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sRevision)+"\">");
 		/* PL : ECC Reports */
 		strBuf.append("<input type=\"hidden\" name=\"BOM2CofigPart\" value=\""+isConfigPart+"\">");
             }
             else
             {
                 // Html code to display the part revision and to make it readonly
             	strBuf.append("<input type=\"hidden\" name=\"BOM2PreloadName\" value=\"\">");
   		        strBuf.append("<input type=\"hidden\" name=\"BOM2PreloadID\" value=\"\">");
 				strBuf.append("<input type=\"hidden\" name=\"BOM2NameDispOID\" value=\"\">");
 				strBuf.append("<input type=\"hidden\" name=\"BOM2Revision\" value=\"\">");
 		/* PL : ECC Reports */
 		strBuf.append("<input type=\"hidden\" name=\"BOM2CofigPart\" value=\"false\">");
             }
    }

         return strBuf.toString();
     }
    /**
    * restricts the access for the table column.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @returns an boolean false
    * @throws Exception if the operation fails.
    * Since EC X3
    */

    public Boolean isSubstituteFor(Context context, String[] args) throws Exception
    {
        //restricts the access for the table column.
        boolean isSubstituteFor = false;
        return Boolean.valueOf(isSubstituteFor);
    }

    /**
    * restricts the access for the table column.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @returns an boolean false
    * @throws Exception if the operation fails.
    * Since EC X3
    */
      public Boolean isUsage(Context context, String[] args) throws Exception
    {
        //restricts the access for the table column.
        boolean isUsage = false;
        return Boolean.valueOf(isUsage);
    }

    /**
    * restricts the access for the table column.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @returns an boolean false
    * @throws Exception if the operation fails.
    * Since EC X3
    */
    public Boolean isPartNameRefDesClassCode(Context context, String[] args) throws Exception
    {
        //restricts the access for the table column.
        boolean isPartNameRefDesClassCode = false;
        return Boolean.valueOf(isPartNameRefDesClassCode);
    }

    /**
    * restricts the access for the table column.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @returns an boolean false
    * @throws Exception if the operation fails.
    * Since EC X3
    */

    public Boolean isPartNameRefDes(Context context, String[] args) throws Exception
    {
        //restricts the access for the table column.
        boolean isPartNameRefDesignator = false;
        return Boolean.valueOf(isPartNameRefDesignator);
        }

    /**
     * Retrieves Part Version value from Vector using the Part Version Relationship.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds a packed HashMap of the following entries:
     * objectId of the context Part.
     * paramList - a Map of parameter values.
     * @return Vector of related Type.
     * @throws Exception if the operation fails.
     * @Author by Arun Bharati
     * @since EC X3
     */

    public Vector getPartVersion(Context context,String[] args) throws Exception
      {
//        String sPartVersion = null;
        MapList getPartVersion =null;
        Vector vectPartVersion = new Vector();
        try{
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramList  = (HashMap)programMap.get("paramList");
            String objectId    = (String)paramList.get("objectId");            //To get the context part id

            MapList objectList  = (MapList)programMap.get("objectList"); // added for bug 343530

            //added/modified for bug 345983 starts
            StringList strList =new StringList();//string list for Part Rev connected to Context part Object
            HashMap tempMap = new HashMap();
            //create Part Obj
            DomainObject obj   = new DomainObject(objectId);
            StringList selectStmts = new StringList();
            //putting elements to StringList
            selectStmts.addElement(DomainObject.SELECT_ID);
            selectStmts.addElement(DomainObject.SELECT_REVISION);
            //get Related Part Revisoins for the above Part Object
            getPartVersion = obj.getRelatedObjects(context,
                                      RELATIONSHIP_PART_VERSION,                // relationship pattern
                                      DomainConstants.TYPE_PART,                // object pattern
                                      selectStmts,                              // object selects
                                      null,                           // relationship selects
                                      false,                                    // to direction
                                      true,                                     // from direction
                                      (short)1,                                 // recursion level
                                      null,                                     // object where clause
                                      null);

            if(getPartVersion != null && getPartVersion.size()>0)
            {
            Iterator iteratorPartVersion = getPartVersion.iterator();
            Hashtable tempHT = null;
            //part revision ID
            String strPRId= null;
            while (iteratorPartVersion.hasNext())
            {
                tempHT = (Hashtable) iteratorPartVersion.next();
                strPRId =(String) tempHT.get(DomainObject.SELECT_ID );
                //add to string list
                strList.add(strPRId);
                //add to Hash Map
                tempMap.put(strPRId,(String) tempHT.get(DomainObject.SELECT_REVISION ));
                }
            }
            StringList strListPR =new StringList();//stringlsit of Part revsions connected to ECO
            //create Related ECO object
            DomainObject domObje = null;
            Map mp =null;
            for (   int ii=0; ii<objectList.size();++ii){
                mp = (Map)objectList.get(ii);
                domObje = new DomainObject((String)mp.get(SELECT_ID));
                //get Part revs connected to ECO
                strListPR = domObje.getInfoList(context,"relationship[" + RELATIONSHIP_AFFECTED_ITEM+"].to.id");
               //boolean to check ECO has the Part Rev
                boolean bool = true;
               for(int kk=0;kk<strListPR.size();++kk){
               //check the part rev connected to ECO is same as the part rev connected to Context Part
               if(strList.contains(strListPR.get(kk))){
                   //if yes connetec to Vector
                   vectPartVersion.add((String)tempMap.get(strListPR.get(kk)));
                   bool=false;
            }

                }
               //if the ECO doet have the part rev then
               if(bool){
                  //add to Vector
                  vectPartVersion.add("");
            }

        }
            //added/modified for bug 345983 ends
        }
       catch(Exception ex)
       {
         throw ex;
         }

          return vectPartVersion;
      }
/**
 * Gets the ECO Summary for the Part.
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds a HashMap with the following entries:
 * objectId - a String containing the Part id.
 * @return MapList of ECO Object id, type, name, description, relationship id, relationship name.
 * @throws Exception if the operation fails.
 * @Author By Arun Bharati
 * @since EC X3
 */

  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getPartEcosSummary (Context context,String[] args)
      throws Exception
  {

      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      String partId = (String) paramMap.get("objectId");                    //To get the context part id
      MapList ecoList = new MapList();
      try
      {
            Part partObj = new Part(partId);
            StringList selectStmts = new StringList(4);                     // putting elements to StringList

            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_TYPE);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);

            StringList selectRelStmts = new StringList(2);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID); // Relationship Selects
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);
                                                                               // Retrieving related item
            Pattern relPattern = new Pattern(RELATIONSHIP_AFFECTED_ITEM);

            ecoList = partObj.getRelatedObjects(context,
                                                relPattern.getPattern(),  //relationship pattern
                                                DomainConstants.TYPE_ECO,                         // object pattern
                                                selectStmts,                 // object selects
                                                selectRelStmts,              // relationship selects
                                                true,                        // to direction
                                                false,                       // from direction
                                                (short)1,                    // recursion level
                                                null,                        // object where clause
                                                null);
      }catch(Exception e){
          throw new FrameworkException(e);
      }

      return ecoList ;
  }
  /**
  * Gets the ECRs for the Part.
  *
* @param context the eMatrix <code>Context</code> object
* @param args holds HashMap containing the following entries:
* objectId - a String of the Part id.
* @return MapList of ECR object ids, names, revisions, relationship ids.
* @throws Exception if operation fails
*/
              @com.matrixone.apps.framework.ui.ProgramCallable
              public MapList getAffetedItemforPart(Context context, String []args)
                                       throws Exception
              {
                  MapList totalresultList = new MapList();
                  try
                  {
                    String RELATIONSHIP_AFFECTED_ITEM =
			PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem");
                    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
                    String partId = (String) paramMap.get("objectId");
                    DomainObject dObj1 =  new DomainObject(partId);
                    StringList selectStmts = new StringList(3);
                    selectStmts.addElement(DomainObject.SELECT_ID);
                    selectStmts.addElement(DomainObject.SELECT_NAME);
                    selectStmts.addElement(DomainObject.SELECT_REVISION);
                    StringList selectRelStmts = new StringList(1);
                    selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
                    //also need to search for old policy ECRs
                    Pattern relPattern = new Pattern(RELATIONSHIP_AFFECTED_ITEM);


                    totalresultList = dObj1.getRelatedObjects(context,
                                                            relPattern.getPattern(), // relationship pattern
                                                            DomainConstants.TYPE_ECR,    // object pattern
                                                            selectStmts,                 // object selects
                                                            selectRelStmts,              // relationship selects
                                                            true,                        // to direction
                                                            false,                       // from direction
                                                            (short)1,                    // recursion level
                                                            null,                        // object where clause
                                                            null);                       // relationship where clause
                  }
                  catch (Exception e)
                  {
                      throw new FrameworkException(e);
                  }
                  return totalresultList;
              }

     /**
     * displayDeviationOptions,this method is executed for 'to populate
     * 'For Radio buttons with and Without for BOM1 Assembly'  in BOM Compare Dialog
     * @param context the eMatrix <code>Context</code> object.
     * @param args[]
     * @author Pallavi
     * @return Object contanining HTML content for revision field and the revision of the context part to be displayed.
      */

 public Object displayDeviationOptions(Context context, String[] args) throws Exception
    {
		StringBuffer strBuf = new StringBuffer(256);
		strBuf.append("<input type=\"radio\" name=\"Deviation1\" value=\"Without\">");
		strBuf.append("Without");
		strBuf.append("<br>");
		strBuf.append("<input type=\"radio\" name=\"Deviation1\" value=\"With\">");
		strBuf.append("With");
		return strBuf.toString();
   }
     /**
     * displayDeviationOptions,this method is executed for 'to populate
     * 'For Radio buttons with and Without for BOM2 Assembly'  in BOM Compare Dialog
     * @param context the eMatrix <code>Context</code> object.
     * @param args[]
     * @author Pallavi
     * @return Object Object contanining HTML content for revision field and the revision of the context part to be displayed.
      */
   public Object displayDeviation(Context context, String[] args) throws Exception
    {
		StringBuffer strBuf = new StringBuffer(256);
		strBuf.append("<input type=\"radio\" name=\"Deviation2\" value=\"Without\">");
		strBuf.append("Without");
		strBuf.append("<br>");
		strBuf.append("<input type=\"radio\" name=\"Deviation2\" value=\"With\">");
		strBuf.append("With");
		return strBuf.toString();
   }
// Added for Part Series Functionality : Begin
/**
     * This method is used as Access Function in the command
     * determines if the Part Series Functionality is turned on or off by ANUPAMA
     * @param context the eMatrix <code>Context</code> object.
     * @param args[]
     * @author Anupama
     * @return Boolean.value for Access Functionality of Master Command
 **/
public Boolean checkPartSeriesEnabled (Context context, String[] args)
    {
        boolean booleanValue = false;
        try
        {

            String PartSeriesEnabled = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.PartSeries.PartSeriesActive");
            //if returns true Part Series Functionality Feature is enabled else not enabled.
            if("true".equalsIgnoreCase(PartSeriesEnabled))
            {
                booleanValue = true;
            }
        }
        catch (Exception fe)
        {
            booleanValue = false;
        }
        finally {
            return Boolean.valueOf(booleanValue);
        }
   }
// Added for Part Series Functionality : End

//Added for Part Enhancement Functionality.Float on move to production

public void floatEBOMrelationship(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //To get the context part id and the new Revised Id
        String ObjId = (String) programMap.get("ObjectID");
        String sProdId = (String) programMap.get("ProductionId");

        DomainObject Obj1 = new DomainObject(ObjId);
        DomainObject Obj2 = new DomainObject(sProdId);
        //To get the Type and Relationship
        String strType = PropertyUtil.getSchemaProperty(context,"type_Part");
        String strRel = PropertyUtil.getSchemaProperty(context,"relationship_EBOM");

        //ObjectSelects
        StringList Objectselects = new StringList(2);
        Objectselects.add(DomainObject.SELECT_ID);
		Objectselects.add(POLICY_CLASSIFICATION);

        //RelationshipSelects
        StringList relSelects = new StringList(1);
        relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);

        MapList idlist = Obj1.getRelatedObjects(context,             //Context
                                                strRel,              //Relationship Pattern
                                                strType,             //Type Patten
                                                Objectselects,       //Object Selects
                                                relSelects,          //Relationship Selects
                                                true,                //To Direction
                                                false,               //From Direction
                                                (short)1,            //Recursion Level
                                                "",                  //object where clause
                                                ""                   //relationship where clause
                                                );

		//Iteration through the Maplist to get the Parent Id
        Iterator itrChildObject = idlist.iterator();
		String policyClass;
        while(itrChildObject.hasNext()) {
            Map mapValues = (Map) itrChildObject.next();
            String ebomrelId = (String) mapValues.get(DomainObject.SELECT_RELATIONSHIP_ID);
            //Doing setToObject with newly revised object instead manually connect/disconnect with parent.
            policyClass = (String)mapValues.get(POLICY_CLASSIFICATION);
			// HF-190770 : Added if condition
	        if ("Production".equalsIgnoreCase(policyClass))
            {
              DomainRelationship.setToObject(context, ebomrelId, Obj2); //HF-137619 end
            }
        }

        // The connection is made between the Development part and Production Part by Derived Relationship
        RelationshipType newRelType = new RelationshipType(PropertyUtil.getSchemaProperty(context,"relationship_Derived"));
		String sDeriveFrom  =Obj1.getInfo(context, "from["+PropertyUtil.getSchemaProperty(context,"relationship_Derived")+"]");
        if(!"true".equalsIgnoreCase(sDeriveFrom)){
			DomainRelationship newRel = DomainRelationship.connect(context,Obj1,newRelType,Obj2);

			// 372458
			String attrDerivedContext= PropertyUtil.getSchemaProperty(context,"attribute_DerivedContext");
			newRel.setAttributeValue(context, attrDerivedContext, "Go to production");
		}
    }
//Ended-for float on move to production

    /**
    * Returns the part objects with object attribute values and relationship attribute vlaues
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds arguments from bean containing the following entries:
    * objectId of the context.
    * Expand Level of objects
    * parent option for object search
    * @throws Exception if the operation fails.
    * Since EC X3
    */
    public MapList getBOMComparison(Context context,String[] args)
    throws Exception
    {
        MapList objectsMapListOne=new MapList();
        short recusrsionLevel;

        /*Retrieving the argument values through argument passed by the bean*/
        String objectId =args[0];
        String ExpandLevel =args[1];
    String sRevOptions =args[2];

        /*Retrieving the attributes of Part object and relationship EBOM*/
        String sparePart = (String) PropertyUtil.getSchemaProperty(context, "attribute_SparePart");
        String materialCategory = (String) PropertyUtil.getSchemaProperty(context, "attribute_MaterialCategory");
        String Weight = (String) PropertyUtil.getSchemaProperty(context, "attribute_Weight");
        String serviceMakeBuyCode = (String) PropertyUtil.getSchemaProperty(context, "attribute_ServiceMakeBuyCode");
        String unitofMeasure = (String) PropertyUtil.getSchemaProperty(context, "attribute_UnitofMeasure");
        String Originator = (String) PropertyUtil.getSchemaProperty(context, "attribute_Originator");
        String productionMakeBuyCode = (String) PropertyUtil.getSchemaProperty(context, "attribute_ProductionMakeBuyCode");
        String leadTime = (String) PropertyUtil.getSchemaProperty(context, "attribute_LeadTime");
        String effectiveDate = (String) PropertyUtil.getSchemaProperty(context, "attribute_EffectiveDate");
        String partClassification = (String) PropertyUtil.getSchemaProperty(context, "attribute_Classification");

        /*Retrieving the names type Part and relationship EBOM*/
        String typePart = (String) PropertyUtil.getSchemaProperty(context, "type_Part");
        String relEBOM = (String) PropertyUtil.getSchemaProperty(context, "relationship_EBOM");

        /*Retrieving the keyvalues throgh keynames from property files*/
      //Multitenant
        String propertyKeyValueForAll= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common.All");
      //Multitenant
        String propertyKeyValueForSingle= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common.Single");
        StringBuffer sObjWhereCond = new StringBuffer();
        DomainObject domObjectOne=new DomainObject(objectId);
        StringList objectSelects=new StringList();

        objectSelects.add(SELECT_TYPE);//Added to fix IR-049662V6R2011x, IR-037412V6R2011x
        objectSelects.add(SELECT_ID);
        objectSelects.add(SELECT_NAME);
        objectSelects.add(SELECT_REVISION);
        objectSelects.add(SELECT_DESCRIPTION);
        objectSelects.add("attribute["+sparePart+"]");
        objectSelects.add("attribute["+materialCategory+"]");
        objectSelects.add("attribute["+Weight+"]");
        objectSelects.add("attribute["+serviceMakeBuyCode+"]");
        objectSelects.add("attribute["+unitofMeasure+"]");
        objectSelects.add("attribute["+Originator+"]");
        objectSelects.add("attribute["+productionMakeBuyCode+"]");
        objectSelects.add("attribute["+leadTime+"]");
        objectSelects.add("attribute["+effectiveDate+"]");
        objectSelects.add("attribute["+partClassification+"]");
        StringList relationshipSelects=new StringList();
        relationshipSelects.add(SELECT_ATTRIBUTE_FIND_NUMBER);
        relationshipSelects.add(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        relationshipSelects.add(SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        relationshipSelects.add(SELECT_ATTRIBUTE_QUANTITY);
        relationshipSelects.add(SELECT_ATTRIBUTE_USAGE);
        relationshipSelects.add(SELECT_RELATIONSHIP_ID);
        /*Checking the condition for recursion level*/
        if(ExpandLevel.equals(propertyKeyValueForSingle))
        {
            recusrsionLevel=1;
        }
        else if(ExpandLevel.equals(propertyKeyValueForAll))
        {
            recusrsionLevel=0;
        }
       //IR023752   start
        else if(ExpandLevel.equalsIgnoreCase("EndItem"))
        {
            recusrsionLevel=0;
            if(sObjWhereCond.length()>0){
                sObjWhereCond.append(" && ");
            }
            sObjWhereCond.append("(" + EngineeringConstants.SELECT_END_ITEM + " == '"
                    + EngineeringConstants.STR_NO + "')");
            objectSelects.addElement("from["+EngineeringConstants.RELATIONSHIP_EBOM+"].to.attribute["+EngineeringConstants.ATTRIBUTE_END_ITEM+"]");
            objectSelects.addElement("from["+EngineeringConstants.RELATIONSHIP_EBOM+"].id");

        }//IR023752   end
        else
        {
            Short levelValue=new Short(ExpandLevel);
            recusrsionLevel=levelValue.shortValue();
        }

        Pattern typePattern=new Pattern(typePart);
        Pattern relationshipPattern=new Pattern(relEBOM);


        /*Adding all part attributes in StingList whcih will retrieved by the getRelatedObjects() method*/
       objectsMapListOne=domObjectOne.getRelatedObjects(context,
                                             relationshipPattern.getPattern(),
                                             typePattern.getPattern(),
                                             objectSelects,
                                             relationshipSelects,
                                             false,
                                             true,
                                             recusrsionLevel,
                                             sObjWhereCond.toString(),//IR023752
                                             null);
       //IR023752    start
        if("EndItem".equalsIgnoreCase(ExpandLevel) )
        {

            objectsMapListOne = getEndItemMapList(context, objectsMapListOne, domObjectOne, objectSelects, relationshipSelects);

        }

        //IR023752 end
    if (sRevOptions != null && sRevOptions.equals("As Stored"))
    {
        return objectsMapListOne;

    }else {

        Iterator i = objectsMapListOne.iterator();
        MapList newMaplist = new MapList();
            while (i.hasNext())
            {
                Map m = (Map) i.next();
                String sObjectId = (String) m.get(DomainConstants.SELECT_ID);
                if (sObjectId != null && !sObjectId.equals("") && !sObjectId.equals("null"))
                {
//                    HashMap hmMap = new HashMap();
                    DomainObject oPartDomObj = DomainObject.newInstance(context,sObjectId);
                    BusinessObject busObj = oPartDomObj.getLastRevision(context);
                    busObj.open(context);
                    DomainObject oBusDom = new DomainObject(busObj);
                    String sBusDomId = oBusDom.getId();
                    if (sBusDomId != null && sBusDomId.equals(sObjectId))
                    {
                        newMaplist.add(m);
                        continue;
                    }else
                    {
                        Map newMap = oBusDom.getAttributeMap(context);
                        m.putAll(newMap);
                        m.put(DomainConstants.SELECT_ID,sBusDomId);
                        m.put(DomainConstants.SELECT_REVISION,oBusDom.getInfo(context,DomainConstants.SELECT_REVISION));
                        newMaplist.add(m);
                    }
                    busObj.close(context);
                }

            }
            return newMaplist;

        }

    }//end of method getPartsExpandLevels()

    public MapList getEndItemMapList(Context context, MapList objectsMapListOne, DomainObject domObjectOne, StringList objectSelects, StringList relationshipSelects) throws NumberFormatException, Exception, FrameworkException {
        /* Added for RDO\RMO Access check STARTS */
        StringBuffer sbPRSelect1 = new StringBuffer();
        StringBuffer sbPRSelect2 = new StringBuffer();
        StringBuffer sbPRSelect3 = new StringBuffer();

        sbPRSelect1.append(EngineeringConstants.SELECT_TO_LEFTBRACE);
        sbPRSelect1.append(DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
        sbPRSelect1.append(EngineeringConstants.SELECT_RIGHTBRACE);
        sbPRSelect1.append(EngineeringConstants.DOT);
        sbPRSelect1.append(EngineeringConstants.SELECT_FROM);
        sbPRSelect1.append(EngineeringConstants.DOT);
        sbPRSelect1.append(EngineeringConstants.SELECT_FROM_LEFTBRACE);
        sbPRSelect1.append(DomainConstants.RELATIONSHIP_MEMBER);
        sbPRSelect2.append('|');
        sbPRSelect2.append(DomainConstants.SELECT_TO_NAME);
        sbPRSelect2.append("==");
        sbPRSelect2.append("'" + context.getUser() + "'");
        sbPRSelect3.append(EngineeringConstants.SELECT_RIGHTBRACE);
        sbPRSelect3.append(EngineeringConstants.DOT);
        sbPRSelect3.append(EngineeringConstants.SELECT_ATTRIBUTE_LEFTBRACE);
        sbPRSelect3.append(DomainConstants.ATTRIBUTE_PROJECT_ROLE);
        sbPRSelect3.append(EngineeringConstants.SELECT_RIGHTBRACE);
        objectSelects.addElement(sbPRSelect1.toString()
                + sbPRSelect2.toString() + sbPRSelect3.toString());
        String sAccessSelectable = sbPRSelect1.toString()
                + sbPRSelect3.toString();
        String slObjectSelect = sbPRSelect1.toString()
                + sbPRSelect2.toString() + sbPRSelect3.toString();
        /* Added for RDO\RMO Access check ENDS */
        MapList mlEndItem = getLeafEndItems(context,
                                   objectsMapListOne,
                                   objectSelects,
                                   relationshipSelects,
                                    sAccessSelectable,
                                    slObjectSelect);
        mlEndItem.sort("level","descending","integer");
        for(int i=0; i<mlEndItem.size();i++) {
            Map endItemMap =  (Map)mlEndItem.get(i);
            String parentEBOMId  = (String)endItemMap.get("ParentEBOMid");

            for(int j=0; j<objectsMapListOne.size();j++) {

                Map eBOMMap =  (Map)objectsMapListOne.get(j);
                String eBOMid  = (String)eBOMMap.get("id[connection]");
                if(parentEBOMId.equals(eBOMid))
                {
                    objectsMapListOne.add(j+1,endItemMap);
                }
            }
        }
        MapList mlEIBOMList = domObjectOne.getRelatedObjects(context,
                DomainConstants.RELATIONSHIP_EBOM,
                DomainConstants.TYPE_PART,
                objectSelects,
                relationshipSelects,
                false,
                true,
                (short) 1,
                "attribute["+EngineeringConstants.ATTRIBUTE_END_ITEM+"] == 'Yes'",
                "",
                0);
        objectsMapListOne.addAll(mlEIBOMList);
        return objectsMapListOne;
    }



    /**
    * Generates the visual report for 2 boms.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @return an booleam false
    * @throws Exception if the operation fails.
    * Since EC X3
    */
    public MapList getVisualReport(Context context, String[] args) throws Exception
    {
        String objectId, objectId2, ExpandLevel, matchBasedOn = null;
        MapList obj1MapList = new MapList();
        MapList obj2MapList = new MapList();
//        MapList mlReturn = new MapList();

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        // Assign values
        objectId    = (String) programMap.get("objectId");
        objectId2   = (String) programMap.get("objectId2");
        ExpandLevel = (String) programMap.get("level");
        matchBasedOn = (String) programMap.get("matchBasedOn");

        if (objectId != null && objectId2 != null)
        {
            // get the ebom maplist of the  respective part
            String [] methArgs = {objectId,ExpandLevel};
            obj1MapList = getBOMComparison(context,methArgs);
            methArgs[0] = objectId2;
            obj2MapList = getBOMComparison(context,methArgs);

            boolean bLevelCompare = false;
            String sMatchBasedOn = "";
            StringList slToCompare = new StringList();
            slToCompare.add(DomainConstants.SELECT_NAME);
            if(bLevelCompare) {
                slToCompare.add(DomainConstants.SELECT_LEVEL);
            }
            slToCompare.add(DomainConstants.SELECT_ATTRIBUTE_QUANTITY);

            if (matchBasedOn != null && matchBasedOn.equals("Find_Number"))
            {
                // add sort keys
                obj1MapList.addSortKey(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER, "ascending", "integer");
                obj2MapList.addSortKey(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER, "ascending", "integer");
                slToCompare.add(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
                sMatchBasedOn = DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER;

            } else {
                // add sort keys
                obj1MapList.addSortKey(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR, "descending", "String");
                obj2MapList.addSortKey(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR, "descending", "String");
                slToCompare.add(DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
                sMatchBasedOn = DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR;
            }
            // sort maplist
            obj1MapList.sort();
            obj2MapList.sort();
            // pass the maplist for comparison
            if(!"".equals(sMatchBasedOn)) {
                compareBOMMapLists(obj1MapList, obj2MapList, slToCompare, sMatchBasedOn);
            }
        }

        //return obj1MapList;
        return obj2MapList;
    }

    /**
    * compares the 2 boms to find if there is any difference.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @return an booleam false
    * @throws Exception if the operation fails.
    * Since EC X3
    */
    private void compareBOMMapLists(MapList mlBOM1, MapList mlBOM2, StringList slToCompare, String sMatchBasedOn) {
        // loop first maplist
        for(int iOuter=0;iOuter<mlBOM1.size();) {

            Map mBOM1 = (Map) mlBOM1.get(iOuter);
            String sMatch1 = (String) mBOM1.get(sMatchBasedOn);
            boolean bOuterRemoveCheck = false;

            // loop second maplist
            for(int iInner=0;iInner<mlBOM2.size();) {
                Map mBOM2 = (Map) mlBOM2.get(iInner);
                String sMatch2 = (String) mBOM2.get(sMatchBasedOn);
                boolean bInnerRemoveCheck = false;
                //compare to see if it diffrent or same
                if (sMatch1!=null && sMatch1.equals(sMatch2)) {
                    Iterator itrAttrToCompare = slToCompare.iterator();
                    bInnerRemoveCheck = true;

                    while(bInnerRemoveCheck && itrAttrToCompare.hasNext()) {
                        String sToCompare = (String) itrAttrToCompare.next();
                        String sToCompare1 = (String) mBOM1.get(sToCompare);
                        String sToCompare2 = (String) mBOM2.get(sToCompare);

                        if (!(sToCompare1!=null && sToCompare1.equals(sToCompare2))) {
                            bInnerRemoveCheck = false;
                        }
                    }
                }
                // remove the map if same
                if(bInnerRemoveCheck) {
                    bOuterRemoveCheck = true;
                    mlBOM2.remove(iInner);
                    break;
                }
                else {
                    iInner++;
                }
            }
            // remove the map if same
            if(bOuterRemoveCheck) {
                mlBOM1.remove(iOuter);
            }
            else {
                iOuter++;
            }
        }

    }


 /**
    * Gets the Part versions of EC
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds HashMap containing the following entries:
    * objectId - a String of the Part id.
    * @param args holds arguments.
    * @return a MapList list of version object ids.
    * @throws Exception if the operation fails.
    * @Since EC X3
    */

   public MapList getPartVersions(Context context, String args[])
                 throws Exception
   {

                MapList mapList=new MapList();;
                HashMap paramMap = (HashMap)JPO.unpackArgs(args);
                String objectId = (String) paramMap.get("objectId");
                StringList objectSelects=new StringList();
                objectSelects.add(SELECT_ID);
                DomainObject domObj1=new DomainObject(objectId);
                //fetch all the version object related to particular revision object
                //Modified for IR-048513V6R2012x, IR-118107V6R2012x
                mapList=domObj1.getRelatedObjects(context,
                		RELATIONSHIP_PART_VERSION,
                                                            DomainConstants.TYPE_PART,
                                                            objectSelects,
                                                            null,
                                                            false,
                                                            true,
                                                            (short)0,
                                                            null,
                                                            null);
                return mapList;
   }//end of getPartVersions() method



 /**
     * This function creates the new revision after version object released
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds HashMap containing the following entries:
     * objectId - a String of the Part id.
     * @return void.
     * @throws Exception if the operation fails.
     * @Since EC X3
    */

   public int createNewRevisionAfterRelease(Context context,String args[])  throws Exception
   {

       try{
		ContextUtil.startTransaction(context, true);

           String versionObjectId=args[0];
           DomainObject versionDO = newInstance(context,versionObjectId);
           
           //BGP: In case the Release Process is Developement, Do not execute the trigger program functionality
		   if(ReleasePhase.isECPartWithDevMode(context, versionDO))
        	   return 0;
           //BGP: In case the Release Process is Developement, Do not execute the trigger program functionality
           
//Added for IR-021267
		String relname= PropertyUtil.getSchemaProperty(context,"relationship_PartVersion");
           String attrIsVersion= PropertyUtil.getSchemaProperty(context,"attribute_IsVersion");
                String  ATTRIBUTE_CURRENTVERSION=PropertyUtil.getSchemaProperty(context,"attribute_CurrentVersion");
           String attrIsVersionValue=versionDO.getInfo(context,"attribute["+attrIsVersion+"]");

               if(attrIsVersionValue.equalsIgnoreCase("TRUE"))
               {

                   String objectSelect="relationship["+relname+"].from.id";
                   String parentId=versionDO.getInfo(context,objectSelect);

                   setId(parentId);

                   int verObjectCount=0;

                    //Before revising the version object set the Current Version attribute as FALSE
                   versionDO.setAttributeValue(context,attrIsVersion,"FALSE");
                   versionDO.setAttributeValue(context,ATTRIBUTE_CURRENTVERSION,String.valueOf(verObjectCount));

                    //Disconnect the version object from the relationship Part Version
                   this.disconnect(context, new RelationshipType(relname),true,versionDO);

                   BusinessObject lastRevisionBO = getLastRevision(context);

                   String strRevision = lastRevisionBO.getNextSequence(context);
                   String objectId = lastRevisionBO.getObjectId();

                   setId(objectId);

                   String strName = versionDO.getInfo(context, DomainConstants.SELECT_NAME);

                   //set Revision explicitly for version object before revising it
                   setRevision(context, versionObjectId, strRevision, strName);

                    //Revise the version object using revise method()
                   BusinessObject revisedBO = revise(context, versionDO, false);

                   objectId = revisedBO.getObjectId();

                   ContextUtil.commitTransaction(context);
              }//end if (attrIsVersionValue)
          }catch(Exception e)
          {
              e.printStackTrace();
              throw new FrameworkException(e);
          }
          return 0;
     }//end of createNewRevisionAfterRelease() method

 /**
     * This function creates the new verison object
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds HashMap containing the following entries:
     * objectId - a String of the Part id.
     * @return void.
     * @throws Exception if the operation fails.
     * @Since EC X3
    */
public int createPartVersionObject(Context context,String args[])  throws Exception
{

try {
    Boolean flagRevokeAccess = Boolean.FALSE;
    String objectId=args[0];
    boolean isMFGInstalled = com.matrixone.apps.engineering.EngineeringUtil.isMBOMInstalled(context);
    String attDesignPurchase = PropertyUtil.getSchemaProperty(context,"attribute_DesignPurchase");
    String checkPartVersion= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Check.PartVersion");
    if(checkPartVersion.equalsIgnoreCase("TRUE"))
    {
   		ContextUtil.startTransaction(context,true);
        String attrRequestedChange = PropertyUtil.getSchemaProperty(context,"attribute_RequestedChange");
        String attrCurrentVersion = PropertyUtil.getSchemaProperty(context,"attribute_CurrentVersion");
        String policyECPart = PropertyUtil.getSchemaProperty(context,"policy_ECPart");
        String attrIsVersion= PropertyUtil.getSchemaProperty(context,"attribute_IsVersion");
        String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
        String strRelAppliedMarkup = PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup");
		String strRelPartSpecification = PropertyUtil.getSchemaProperty(context,"relationship_PartSpecification");

         String verSuffix="";

         //Creating domain object for ECO object id
         DomainObject ecoDomainObject=new DomainObject(objectId);

		Vector vAssignments = PersonUtil.getAssignments(context);
		String sDesignEngineerRoleName = PropertyUtil.getSchemaProperty(context,"role_DesignEngineer");

		if(vAssignments.contains(sDesignEngineerRoleName)){
			//flagRevokeAccess = true;
            flagRevokeAccess = Boolean.TRUE;
			StringBuffer cmdBuffer = new StringBuffer(250);
			ContextUtil.pushContext( context, PERSON_REQUEST_ACCESS_GRANTOR, null, null );
			cmdBuffer.append("set context user $1;");
			MqlUtil.mqlCommand(context,cmdBuffer.toString(),context.getUser());
			MqlUtil.mqlCommand(context,"modify bus $1 grant $2 access $3, modify",objectId,context.getUser(),"fromconnect");
			ContextUtil.popContext(context);
		}

         //Fetches all the afftected items connected to ECO through the relationship Affected Item

      //Multitenant
        String attrRequestedChangeValue= EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale("en"),"emxFramework.Range.Requested_Change.For_Revise");

		StringList selectStmts = new StringList(1);
		selectStmts.addElement(SELECT_ID);

		String whereclause = "(attribute[" + attrRequestedChange + "]==\"" +
                    ""+attrRequestedChangeValue+"\")" ;
		MapList revisedPartsList=ecoDomainObject.getRelatedObjects(context,
                    RELATIONSHIP_AFFECTED_ITEM, "*", selectStmts,
                    null, false, true, (short) 1, null, whereclause);

      if(revisedPartsList.size() > 0)
         {
                 Iterator itemIterator=revisedPartsList.iterator();
                 while(itemIterator.hasNext())
            {
                    Map mapItem = (Map) itemIterator.next();
                    String affectedItemId=(String)mapItem.get(SELECT_ID);

                    String strECOId = objectId;

                    String[] inputArgs = new String[2];

                    inputArgs[0]= affectedItemId;
                    inputArgs[1]=strECOId;

                    String strNewPartId = (String) JPO.invoke(context, "enoEngChange", null, "getIndirectAffectedItems", inputArgs,String.class);

                    if (strNewPartId != null)
                 {
                        continue;
                    }

                    DomainObject itemDomainObject=new DomainObject(affectedItemId);

                     //Read name,revision,vault.description,owner for Affcted Item
                    String sName=itemDomainObject.getInfo(context,SELECT_NAME);
                    String sRevision=itemDomainObject.getInfo(context,SELECT_REVISION);
                    String sVaultName=itemDomainObject.getInfo(context,SELECT_VAULT);
                    String sDesc=itemDomainObject.getInfo(context,SELECT_DESCRIPTION);
                    String sOwner=itemDomainObject.getInfo(context,SELECT_OWNER);
                    String sRDOId=itemDomainObject.getInfo(context,"to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id");
                    String sTypePart=itemDomainObject.getInfo(context,SELECT_TYPE);
                    String strSelectedPolicy=itemDomainObject.getInfo(context,SELECT_POLICY);
		    String sSplChar = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.PartVersion.SpecialChar");
                    String verName=sName+sSplChar+sRevision;

                    if(strSelectedPolicy.equals(policyECPart)){
                     //If the Affcted Item is Production Part then
                     //Read the Current Version value of the Affected Item



                    String attrIsVersionValue=itemDomainObject.getInfo(context,"attribute["+attrIsVersion+"]");

                    if(attrIsVersionValue.equalsIgnoreCase("FALSE"))
                   {

                     int verObjectCount=Integer.parseInt(itemDomainObject.getInfo(context,"attribute["+attrCurrentVersion+"]"))+1;

                    //check for the value for current version according to that set the version suffix
                     if(verObjectCount<100 && verObjectCount>=10)
                    {
                       verSuffix="0"+verObjectCount;
                    }
                    else if(verObjectCount<10)
                    {
                        verSuffix="00"+verObjectCount;
                    }

                    //Concatenate the revision and suffix and use this for version object's revision
                    String objectRev=sRevision+"-"+verSuffix;

                    ContextUtil.pushContext(context);
                    itemDomainObject.setAttributeValue(context,attrCurrentVersion,String.valueOf(verObjectCount));
                    ContextUtil.popContext(context);

    PartFamily partFamily = (PartFamily)DomainObject.newInstance(context,
                           DomainConstants.TYPE_PART_FAMILY,DomainConstants.ENGINEERING);

        Part part = (Part)DomainObject.newInstance(context,
                           DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);

        String relEBOM = PropertyUtil.getSchemaProperty(context,"relationship_EBOM");
        String relPartVersion = PropertyUtil.getSchemaProperty(context,"relationship_PartVersion");
        String relPartFamilyMember = PropertyUtil.getSchemaProperty(context,"relationship_PartFamilyMember");
        String relDesignResponsibility = PropertyUtil.getSchemaProperty(context,"relationship_DesignResponsibility");
							String relComponentSubstitution = PropertyUtil.getSchemaProperty(context,"relationship_ComponentSubstitution");
							String relAlternate = PropertyUtil.getSchemaProperty(context,"relationship_Alternate");
							String relStructure=relEBOM+","+relPartFamilyMember+","+relDesignResponsibility+","+relComponentSubstitution+","+relAlternate;
                    //Get the last revision and its object id
                    DomainObject domObj=new DomainObject(affectedItemId);
        BusinessObject lastRevObj = domObj.getLastRevision(context);

        String revObjectId=lastRevObj.getObjectId();

                    part.setId(affectedItemId);

            SelectList select = new SelectList(2);
        String selectStr = "relationship[" + part.RELATIONSHIP_PART_FAMILY_MEMBER + "].from.id";
        select.addElement(selectStr);
        BusinessObjectWithSelect partSelect = part.select(context, select);
        String partFamilyId = partSelect.getSelectData(selectStr);
		ContextUtil.pushContext(context);
        Part partVerObj=new Part();

                    //Inherit all the features of revision object into version object by using
                    //cloneWithStructure method
        partVerObj.cloneWithStructure( context,
                                                       sTypePart,
                                                       verName,
                                                       objectRev,
                                                       strSelectedPolicy,
                                                       sVaultName,
                                                       sDesc,
                                                       new HashMap(),
                                                       sOwner,
                                                       null,
                                                       sRDOId,
                                                       partFamilyId,
                                                       null,
                                                       false,
                                                       revObjectId,
                                                       relStructure);
		ContextUtil.popContext(context);
         String partVerObjId=partVerObj.getObjectId();
         if ((partFamilyId != null) && (!partFamilyId.equals("")))
         {
              partFamily.setId(partFamilyId);
              partFamily.addPart(context, partVerObjId);
         }

         DomainObject revBusObj=new DomainObject(affectedItemId);
         DomainObject versionBusObj=new DomainObject(partVerObjId);

                    String strOwner = context.getUser();

                    ContextUtil.pushContext(context);

                    versionBusObj.setOwner(context,strOwner);

                    versionBusObj.setName(context,sName);

                    ContextUtil.popContext(context);

                    //Once the version object will get created set the Current Version attribute as TRUE
                    versionBusObj.setAttributeValue(context,attrCurrentVersion,String.valueOf(verObjectCount));
                    versionBusObj.setAttributeValue(context,attrIsVersion,"TRUE");


                    if (isMFGInstalled) {
                    	versionBusObj.setAttributeValue(context,EngineeringConstants.ATTRIBUTE_END_ITEM,domObj.getAttributeValue(context, EngineeringConstants.ATTRIBUTE_END_ITEM));
                    	versionBusObj.setAttributeValue(context,EngineeringConstants.ATTRIBUTE_END_ITEM_OVERRIDE_ENABLED,domObj.getAttributeValue(context, EngineeringConstants.ATTRIBUTE_END_ITEM_OVERRIDE_ENABLED));
                    	versionBusObj.setAttributeValue(context,attDesignPurchase,domObj.getAttributeValue(context, attDesignPurchase));
                    }

                StringList selectRelStmts = new StringList(1);
                selectRelStmts.addElement(DomainRelationship.SELECT_ID);
                 //IR-048513V6R2012x, IR-118107V6R2012x
                MapList ebomMarkupList=revBusObj.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_EBOM_MARKUP, "*", selectStmts,
                         selectRelStmts, true, true, (short) 1, "current == "+ DomainConstants.STATE_PART_APPROVED +" && to[" + strRelAppliedMarkup + "] == False", null);


                Iterator markupMapItr = ebomMarkupList.iterator();//Markup iterator
                EBOMMarkup markup;
                ContextUtil.pushContext(context);
                while (markupMapItr.hasNext()) {
                    Map marukupMap = (Map) markupMapItr.next();
                    String strMarkupId = (String) marukupMap.get(DomainConstants.SELECT_ID);
                    markup = new EBOMMarkup(strMarkupId);




                    //connect the markup with eco
                    DomainRelationship.connect(context, ecoDomainObject, PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup"), markup);


                }
                ContextUtil.popContext(context);

                    DomainRelationship doRelNew = DomainRelationship.connect(context, ecoDomainObject, RELATIONSHIP_AFFECTED_ITEM, versionBusObj);
                    doRelNew.setAttributeValue(context, strAttrAffectedItemCategory, "Indirect");

					//Added for carrying Part Specification connections to the newly created versioned object
ContextUtil.pushContext(context);
					MapList partSpecList = revBusObj.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_PART_SPECIFICATION, "*", selectStmts,
                        selectRelStmts, true, true, (short) 1,null, null);
						if(partSpecList.size()>0){
							Iterator partSpecItr = partSpecList.iterator();//partSpec iterator

							while (partSpecItr.hasNext()) {
								Map partSpecMap = (Map) partSpecItr.next();
								String strpartSpecId = (String) partSpecMap.get(DomainConstants.SELECT_ID);
								DomainObject specBusObj = new DomainObject(strpartSpecId);
								//connect the versioned object to specification object
								DomainRelationship.connect(context,versionBusObj,strRelPartSpecification,specBusObj);
							}
						}
						ContextUtil.popContext(context);


		 revBusObj.connect(context,relPartVersion, versionBusObj, false);
            }
        }
       }
     }
	ContextUtil.commitTransaction(context);
    }
        if (flagRevokeAccess.booleanValue())
        {
            ContextUtil.pushContext( context, PERSON_REQUEST_ACCESS_GRANTOR, null, null );
            String sCommand = "mod bus $1 revoke grantor $2 grantee $3";
            MqlUtil.mqlCommand(context, sCommand,objectId,PERSON_REQUEST_ACCESS_GRANTOR,context.getUser());
            ContextUtil.popContext(context);
        }
    }catch(Exception e)
    {
        e.printStackTrace();
        throw new FrameworkException(e);
    }
    return 0;


}//end OF createPartVersionObject




 /**
     * This function modifies the revision of craeted part object.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds HashMap containing the following entries:
     * objectId - a String of the Part id.
     * @return boolean if modification is successful else retuen false.
     * @throws Exception if the operation fails.
     * @Since EC X3.
    */

    public boolean setRevision(
        Context context,
        String strObjectId,
        String strRevision,
        String strName)
        throws FrameworkException
    {
        try
        {
            //The MQL String is generated to modify the business object
            String strChangeString = "modify bus $1 revision $2 name $3;";
            //New MQLCommand is instantiated to execute the command
            //The result will be true if the revision is modified.
            MqlUtil.mqlCommand(context,strChangeString,strObjectId,strRevision,strName);

            //Returns the result to the calling method
            return true;
        }
        catch (Exception e) //If the revision update fails then an exception is raised with suitable error message
        {

        	String strAlertMessage =EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Alert.REVISION-UPDATE-FAILED");
           throw new FrameworkException(strAlertMessage);
        }
    }//end of setRevision method()







//EC-Part Series
      /**
       * Method to associate an interface to the  part
       * and set the "Part Family Reference" attribute as "Master or Reference or Unassigned".
       *
       * @mx.whereUsed Invoked by the trigger object on creation of Part-
       *               TypePartCreateAction,
       * @mx.summary This method associates External Part Data
       *             interface to a Equivalent part. Uses
       *             <code>MqlUtil.mqlCommand</code> to associate the interface
       *             to the part. Uses <code>DomainObject.setAttribute() </code>to
       *             set an attribute value
       * @param context
       *            the eMatrix <code>Context</code> object
       * @throws FrameworkException
       *             if the operation fails
       * @since EC X3
       */
     public int addInterfaceToPart(Context context, String args[])throws Exception {
        int status = 0;
        String ObjectId = args[0]; // ${OBJECTID}
        String sFromObjectId = args[1]; // ${FROMOBJECTID}
        try {
            if(sFromObjectId==null || ObjectId==null || sFromObjectId.equals("") || ObjectId.equals("")) {
            return 1;
            }
            DomainObject doFromObject = new DomainObject(ObjectId);
            DomainObject doToObject = new DomainObject(sFromObjectId);

            String sFromObjectType = doFromObject.getInfo(context, DomainConstants.SELECT_TYPE);
            // Get Created Part ObjectId from Environment Variables
            String partFamilyReference =  PropertyUtil.getSchemaProperty(context,"interface_PartFamilyReference");
            boolean isMEP = false;
            boolean PartseriesActive = checkPartSeriesEnabled ( context, args).booleanValue();

             if(PartseriesActive) {
                    isMEP = true;
                }
                if(isMEP)
                {
					//372257 : Modified the check for all the subtype of Part instead of checking only type Part
                    if(sFromObjectType.equals(DomainConstants.TYPE_PART_FAMILY) && doToObject.isKindOf(context, DomainConstants.TYPE_PART)) {
						if(!(MqlUtil.mqlCommand(context,"print bus $1 select $2 dump;",sFromObjectId,"interface["+partFamilyReference+"]")).equals("TRUE")){
                     MqlUtil.mqlCommand(context, "modify bus $1 add interface $2;",true,sFromObjectId,partFamilyReference);
                   }
                }
                }
        } catch (Exception e) {
          status = 1;
          throw new FrameworkException(e);
        } finally {
          return status;
        }
      }

/**
  * Get Context Parts for the specified criteria.
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds a packed HashMap of the following entries:
  * selType - a String containing the Type to search for.
  * txtName - a String containing the Name to search for.
  * txtRev - a String containing the Revision to search for.
  * txtOwner - a String containing the Owner to search for.
  * txtWhere - a String containing the where clause to use in the search.
  * txtOriginator - a String containing the Originator to search for.
  * txtDesc - a String containing the Description to search for.
  * txtSearch - a String containing the Search text for the query.
  * txtFormat - a String containing the Format text for the query.
  * languageStr - a String containing the language setting.
  * revPattern - a String of either ALL_REVISIONS, HIGHEST_REVISION or HIGHEST_AND_PRESTATE_REVS.
  * Vault - a String containing the Vault name to search in.
  * queryLimit - a String containing the Query Limit value.
  * @return Object a MapList containing search result.
  * @throws Exception if the operation fails.
  * @since X3
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getPartsInEBOM(Context context , String[] args)
                    throws Exception
  {
        MapList partList= new MapList();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String contextId = (String) paramMap.get("contextId");
        String objectId = (String) paramMap.get("ObjectId");
        if ("0".equals(contextId))  {
            contextId = objectId;
        }
        String slkupOriginator    = PropertyUtil.getSchemaProperty(context,"attribute_Originator");
        String strFindNumber    = PropertyUtil.getSchemaProperty(context,"attribute_FindNumber");
        String strRefDes    = PropertyUtil.getSchemaProperty(context,"attribute_ReferenceDesignator");
        String strCompLocation    = PropertyUtil.getSchemaProperty(context,"attribute_ComponentLocation");
        String strQuantity    = PropertyUtil.getSchemaProperty(context,"attribute_Quantity");
        String strUsage    = PropertyUtil.getSchemaProperty(context,"attribute_Usage");

        //Retrieve Search criteria
        String txtOriginator          = (String)paramMap.get("Originator");
        String txtName           = (String)paramMap.get("txtName");
        String txtDescription           = (String)paramMap.get("Description");
        String txtOwner         = (String)paramMap.get("OwnerDisplay");
        String txtFindNumber         = (String)paramMap.get("F/N");
        String txtRefDes         = (String)paramMap.get("Ref Des");
        String txtComponentLocation         = (String)paramMap.get("Component Location");
        String txtQty         = (String)paramMap.get("Qty");
        String txtUsage    = (String)paramMap.get("Usage");
        String txtRev      = (String)paramMap.get("Rev");
        String txtWhere         = "";
        String sWhereExp = txtWhere;
        String srelWhereExp = "";
        String sAnd         = "&&";
        char chDblQuotes    = '\"';

    /**************************Vault Code Start*****************************/
    // Get the user's vault option & call corresponding methods to get the vault's.
          String txtVault   ="";
          String strVaults="";
          StringList strListVaults=new StringList();

          String txtVaultOption = (String)paramMap.get("vaultOption");
          if(txtVaultOption==null) {
            txtVaultOption="";
          }

          if("ALL_VAULTS".equals(txtVaultOption) || "".equals(txtVaultOption))
            {
              // get ALL vaults
              Iterator mapItr = VaultUtil.getVaults(context).iterator();
              if(mapItr.hasNext())
              {
                txtVault =(String)((Map)mapItr.next()).get("name");

                while (mapItr.hasNext())
                {
                  Map map = (Map)mapItr.next();
                  txtVault += "," + (String)map.get("name");
                }
              }

            }
            else if("LOCAL_VAULTS".equals(txtVaultOption))
            {
              // get All Local vaults
              com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
              Company company = person.getCompany(context);
              strListVaults = OrganizationUtil.getLocalVaultsList(context, company.getObjectId());

              StringItr strItr = new StringItr(strListVaults);
              if(strItr.next()){
                strVaults =strItr.obj().trim();
              }
              while(strItr.next())
              {
                strVaults += "," + strItr.obj().trim();
              }
              txtVault = strVaults;
            }
            else if ("DEFAULT_VAULT".equals(txtVaultOption))
            {
              txtVault = context.getVault().getName();
            }
            else
            {
              txtVault = txtVaultOption;
            }

          //trimming
          txtVault = txtVault.trim();

      /*******************************Vault Code End***************************************/

        String savedQueryName   = (String)paramMap.get("savedQueryName");
        if (savedQueryName == null || savedQueryName.equals("null") || savedQueryName.equals("")){
          savedQueryName="";
        }

        String queryLimit = (String)paramMap.get("queryLimit");
        if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
          queryLimit = "0";
        }
        if (txtName == null || txtName.equals("*") || txtName.equalsIgnoreCase("null") || txtName.length() <= 0){
          txtName = "";
        }
        if (txtRev == null || txtRev.equals("*") || txtRev.equalsIgnoreCase("null") || txtRev.length() <= 0){
          txtRev = "";
        }
        if (txtOwner == null || txtOwner.equals("*") || txtOwner.equalsIgnoreCase("null") || txtOwner.length() <= 0){
          txtOwner = "";
        }
        if (txtDescription != null && !txtDescription.equalsIgnoreCase("null") && txtDescription.equals("*")){
          txtDescription = "";
        }
        if (txtOriginator != null && !txtOriginator.equalsIgnoreCase("null") && txtOriginator.equals("*")){
          txtOriginator = "";
        }
        if (txtFindNumber != null && !txtFindNumber.equalsIgnoreCase("null") && txtFindNumber.equals("*")){
           txtFindNumber = "";
        }
        if (txtRefDes != null && !txtRefDes.equalsIgnoreCase("null") && txtRefDes.equals("*")){
           txtRefDes = "";
        }
        if (txtComponentLocation != null && !txtComponentLocation.equalsIgnoreCase("null") && txtComponentLocation.equals("*")){
           txtComponentLocation = "";
        }
        if (txtQty != null && !txtQty.equalsIgnoreCase("null") && txtQty.equals("*")){
           txtQty = "";
        }
        if (txtUsage != null && !txtUsage.equalsIgnoreCase("null") && txtUsage.equals("*")){
           txtUsage = "";
        }
        if (txtRev != null && !txtRev.equalsIgnoreCase("null") && txtRev.equals("*")){
           txtRev = "";
        }
        if (txtWhere == null || txtWhere.equalsIgnoreCase("null")){
          txtWhere = "";
        }
        if (srelWhereExp == null || srelWhereExp.equalsIgnoreCase("null")){
          srelWhereExp = "";
        }

        if (!(txtName == null || txtName.equals("") || txtName.equalsIgnoreCase("null") || txtName.length() <= 0 )) {
                  String sname = "name ~~ "+ chDblQuotes + txtName + chDblQuotes;
                  if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
                    sWhereExp = sname;
                  } else {
                    sWhereExp += sAnd + " " + sname;
                  }
        }

        if (!(txtRev == null || txtRev.equals("") || txtRev.equalsIgnoreCase("null") || txtRev.length() <= 0 )) {
                  String sRev = "revision ~~ "+ chDblQuotes + txtRev + chDblQuotes;
                  if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
                    sWhereExp = sRev;
                  } else {
                    sWhereExp += sAnd + " " + sRev;
                  }
        }

       if (!(txtOwner == null || txtOwner.equals("") || txtOwner.equalsIgnoreCase("null") || txtOwner.length() <= 0 )) {
                  String sOwner = "owner ~~ " + chDblQuotes + txtOwner + chDblQuotes;
                  if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
                    sWhereExp = sOwner;
                  } else {
                    sWhereExp += sAnd + " " + sOwner;
                  }
        }

       if (!(txtDescription == null || txtDescription.equals("") || txtDescription.equalsIgnoreCase("null") || txtDescription.length() <= 0 )) {
                  String sDescription = "description ~~ " + chDblQuotes + txtDescription + chDblQuotes;
                  if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
                    sWhereExp = sDescription;
                  } else {
                    sWhereExp += sAnd + " " + sDescription;
                  }
        }

        if (!(txtOriginator == null || txtOriginator.equals("") || txtOriginator.equalsIgnoreCase("null") || txtOriginator.length() <= 0 )) {
                  String sOriginatorQuery = "attribute[" + slkupOriginator + "] ~~ " + chDblQuotes + txtOriginator + chDblQuotes;
                  if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
                    sWhereExp = sOriginatorQuery;
                  } else {
                    sWhereExp += sAnd + " " + sOriginatorQuery;
                  }
        }

        if (!(txtFindNumber == null || txtFindNumber.equals("") || txtFindNumber.equalsIgnoreCase("null") || txtFindNumber.length() <= 0 )) {
                  String sFindNumber = "attribute[" + strFindNumber + "] ~~ " + chDblQuotes + txtFindNumber + chDblQuotes;
                  if (srelWhereExp == null || srelWhereExp.equalsIgnoreCase("null") || srelWhereExp.length()<=0 ){
                    srelWhereExp = sFindNumber;
                  } else {
                    srelWhereExp += sAnd + " " + sFindNumber;
                  }
        }

       if (!(txtRefDes == null || txtRefDes.equals("") || txtRefDes.equalsIgnoreCase("null") || txtRefDes.length() <= 0 )) {
                      String sRefDes = "attribute[" + strRefDes + "] ~~ " + chDblQuotes + txtRefDes + chDblQuotes;
                      if (srelWhereExp == null || srelWhereExp.equalsIgnoreCase("null") || srelWhereExp.length()<=0 ){
                        srelWhereExp = sRefDes;
                      } else {
                        srelWhereExp += sAnd + " " + sRefDes;
                      }
        }

       if (!(txtComponentLocation == null || txtComponentLocation.equals("") || txtComponentLocation.equalsIgnoreCase("null") || txtComponentLocation.length() <= 0 )) {
                      String sComponentLocation = "attribute[" + strCompLocation + "] ~~ " + chDblQuotes + txtComponentLocation + chDblQuotes;
                      if (srelWhereExp == null || srelWhereExp.equalsIgnoreCase("null") || srelWhereExp.length()<=0 ){
                        srelWhereExp = sComponentLocation;
                      } else {
                        srelWhereExp += sAnd + " " + sComponentLocation;
                      }
        }

       if (!(txtQty == null || txtQty.equals("") || txtQty.equalsIgnoreCase("null") || txtQty.length() <= 0 )) {
                      String sQty = "attribute[" + strQuantity + "] ~~ " + chDblQuotes + txtQty + chDblQuotes;
                      if (srelWhereExp == null || srelWhereExp.equalsIgnoreCase("null") || srelWhereExp.length()<=0 ){
                        srelWhereExp = sQty;
                      } else {
                        srelWhereExp += sAnd + " " + sQty;
                      }
        }

       if (!(txtUsage == null || txtUsage.equals("") || txtUsage.equalsIgnoreCase("null") || txtUsage.length() <= 0 )) {
                      String sUsage = "attribute[" + strUsage + "] ~~ " + chDblQuotes + txtUsage + chDblQuotes;
                      if (srelWhereExp == null || srelWhereExp.equalsIgnoreCase("null") || srelWhereExp.length()<=0 ){
                        srelWhereExp = sUsage;
                      } else {
                        srelWhereExp += sAnd + " " + sUsage;
                      }
        }

       boolean isPartType = false;
        if (isPartType) {
             String sattrIsVersion  = PropertyUtil.getSchemaProperty(context,"attribute_IsVersion");
             String sIsVersionQuery = "!attribute[" + sattrIsVersion + "] == " + chDblQuotes + "True" + chDblQuotes;
             if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
             sWhereExp = sIsVersionQuery;
             }  else {
                       sWhereExp += sAnd + " " + sIsVersionQuery;
                }
        }
     StringList selectStmts  = new StringList(2);
     selectStmts.addElement(SELECT_ID);
     selectStmts.addElement(SELECT_NAME);
     DomainObject domobj = new DomainObject(contextId);
     partList = domobj.getRelatedObjects( context,
                                                 RELATIONSHIP_EBOM,  // relationship pattern
                                                 TYPE_PART,                    // object pattern
                                                 selectStmts,                 // object selects
                                                 null,              // relationship selects
                                                 false,                       // to direction
                                                 true,                        // from direction
                                                 (short) 0,                   // recursion level
                                                 sWhereExp,                        // object where clause
                                                 srelWhereExp);
    return partList;
      }

@com.matrixone.apps.framework.ui.PreProcessCallable
public HashMap sendXML(Context context, String args[]) throws Exception
{
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap requestMap = (HashMap)programMap.get("requestMap");
    HashMap tableData = (HashMap) programMap.get("tableData");

    MapList ObjectList = (MapList) tableData.get("ObjectList");

    Hashtable htConflictBOMs = (Hashtable) requestMap.get("ConflictList");

    String strMarkupXML = (String) requestMap.get("XMLINFO");

    MapList mapListUpdated= new MapList();

    Iterator itrObjects = ObjectList.iterator();

    Map mapObj = null;

    String strType = null;
    String strName = null;
    String strRev = null;
    String strFN = null;
    String strRD = null;
    String strEBOMKey = null;

    if (htConflictBOMs != null)
    {

        while (itrObjects.hasNext())
        {
            mapObj = (Map) itrObjects.next();
            strType = (String) mapObj.get(SELECT_TYPE);
            strName = (String) mapObj.get(SELECT_NAME);
            strRev = (String) mapObj.get(SELECT_REVISION);
            strFN = (String) mapObj.get(SELECT_ATTRIBUTE_FIND_NUMBER);
            strRD = (String) mapObj.get(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);

            strEBOMKey = strType + "~" + strName + "~" + strRev + "~" + strFN + "~" + strRD;

            if (htConflictBOMs.containsKey(strEBOMKey))
            {
                mapObj.put("HasConflict", "true");
                mapObj.put("key", strEBOMKey);
            }
            else
            {
                mapObj.put("HasConflict", "false");
            }

            mapListUpdated.add(mapObj);
        }
    }

    HashMap hmpReturn = new HashMap();

    if (strMarkupXML != null && !"null".equals(strMarkupXML) && strMarkupXML.length() > 0)
    {
		hmpReturn.put ("Action", "execScript");
    	hmpReturn.put("Message", "{ main:function()  { loadMarkUpXML('" + strMarkupXML +"\', \"true\");  }}");
	}
	else
	{
		hmpReturn.put ("Action", "");
		hmpReturn.put("Message", "");
	}

    if (htConflictBOMs != null)
    {
        hmpReturn.put("ObjectList", mapListUpdated);
    }
    else
    {
        hmpReturn.put("ObjectList", ObjectList);
    }

    return hmpReturn;
}

public Vector getConflictColumnDisplay(Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);

        MapList objList = (MapList)programMap.get("objectList");

        HashMap paramList = (HashMap)programMap.get("paramList");

        String strParentId = (String)paramList.get("objectId");
        String strChangeId = (String)paramList.get("sChangeOID"); //ENG Convergence

        Vector columnVals = new Vector(objList.size());

        boolean isPrinterFriendly = false;

        String printerFriendly = (String)paramList.get("reportFormat");
        if ( printerFriendly != null )
        {
            isPrinterFriendly = true;
        }

        Iterator itr = objList.iterator();

        Map mapTemp = null;

        String hasConflict = null;
        String strRelId = null;
        String strId = null;
        String strLevel = null;
        String strKey = null;

        while (itr.hasNext())
        {
                mapTemp = (Map) itr.next();
                hasConflict = (String)mapTemp.get("HasConflict");
                strRelId = (String)mapTemp.get(SELECT_RELATIONSHIP_ID);
                strId = (String)mapTemp.get(SELECT_ID);
                strLevel = (String)mapTemp.get("id[level]");
                strKey = (String)mapTemp.get("key");

                if (hasConflict != null && "true".equals(hasConflict))
                {

                        if(!isPrinterFriendly)
                        {

                          columnVals.addElement("<a href=\"javascript:emxTableColumnLinkClick('../engineeringcentral/emxEngrConflictMarkupsFS.jsp?emxSuiteDirectory=engineeringcentral&amp;relId="+XSSUtil.encodeForURL(context, strRelId)+"&amp;suiteKey=EngineeringCentral&amp;parentOID="+XSSUtil.encodeForURL(context,strParentId)+"sChangeOID="+XSSUtil.encodeForURL(context,strChangeId)+"&amp;objectId="+XSSUtil.encodeForURL(context,strId)+"&amp;key="+XSSUtil.encodeForURL(context,strKey)+"&amp;level="+XSSUtil.encodeForURL(context,strLevel)+"', '700', '600', 'true')\"><img border=\"0\" src=\"images/iconStatusMulti.gif\" alt=\"\"/></a>");
                        }
                        else
                        {
                            columnVals.addElement("<img border=\"0\" src=\"images/iconStatusMulti.gif\" alt=\"\"/>");
                        }
                }
                else
                {
                     columnVals.addElement("&#160;");
                }
        }

        return columnVals;
     }
  /**
     * Turn off the default Clone Based On field on the Clone Part Form for 'Part' objects
     *
     * @mx.whereUsed Invoked as a Access Function in the Default Clone
     *               Based On field in common create web form.
     *
     * @mx.summary this methods checks the Typ ethat is present in the
     *             arguments. and returns true if Part is a Compliance Part and
     *             false if part is a EC part.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     * @return the <code>boolean</code> value
     * @throws Exception
     *             if the operation fails
     */
    public boolean turnOffDefCloneBasedOnforECPart(Context context, String[] args)
            throws Exception {
        try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String typeName = (String) programMap.get("type");

            if(typeName!=null && !typeName.equals("")){
                if(typeName.startsWith("_selectedType:")){
                    StringList typeList = FrameworkUtil.split(typeName, ",");
                    if (typeList.size()>1){
                        typeName = (String)typeList.get(0);
                    }if (typeName.startsWith("_selectedType:")){
                        String ary[] = StringUtils.split(typeName, "_selectedType:");
                        typeName=ary[1];
                    }
                }else{
                    typeName = PropertyUtil.getSchemaProperty(context,typeName);
                }
            }

            if (typeName != null && mxType.isOfParentType(context, typeName , DomainConstants.TYPE_PART))
            {
                return false;
            }

			} catch (Exception ex) {

        }
        return true;
    }

    /**
     * Show EC speciic Clone Based on field on Clone Form for Part objects
     *
     *
     * @mx.whereUsed Invoked as a Access Function in the EC Speciifc Clone Based
     *               On field in common Clone Part webform
     *
     * @mx.summary this methods checks the Type that is present in the
     *             arguments. and returns true if Part is a EC Part and false if
     *             part is a MCC part.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     * @return the <code>boolean</code> value
     * @throws Exception
     *             if the operation fails
     */
    public boolean showCloneBasedOnFieldforECPart(Context context, String[] args)
            throws Exception {
        return !(turnOffDefCloneBasedOnforECPart(context, args));
    }


/**
     * Display the PartFamily Item field in APPPartClone WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operations fails
     * @since EC - X3
     */
 public String getPartFamily(Context context,String[] args)  throws Exception
    {
     StringBuffer output = new StringBuffer(2048);
     String strOuput = "";
     String partFamilyDisplay = "";
     String partFamilyId = "";
     try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            //366574
            HashMap requestMap = (HashMap)programMap.get("requestMap");
            String ObjectId = (String)requestMap.get("copyObjectId");
            DomainObject dom = DomainObject.newInstance(context,ObjectId);
            String sPFId=dom.getInfo(context,"to[" + DomainConstants.RELATIONSHIP_CLASSIFIED_ITEM + "].from.id");
            String autoNameGenerate = "false";
            if(sPFId != null && sPFId.length() !=0){
                DomainObject itemDomainObject=new DomainObject(sPFId);
                partFamilyDisplay=itemDomainObject.getInfo(context,SELECT_NAME);

                autoNameGenerate = itemDomainObject.getAttributeValue(context, DomainConstants.ATTRIBUTE_PART_FAMILY_NAME_GENERATOR_ON);

                partFamilyId = sPFId;
            }
            //366574
            //Read name,revision,vault.description,owner for Affcted Item

            String strClear =EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",context.getSession().getLanguage());
            String strMPart =PropertyUtil.getSchemaProperty(context,"type_ManufacturingPart");
            String strMLPart =PropertyUtil.getSchemaProperty(context,"type_MaterialPart");
            String strPHPart =PropertyUtil.getSchemaProperty(context,"type_PhantomPart");
            String strSupPart =PropertyUtil.getSchemaProperty(context,"type_SupportPart");
            String strToolPart =PropertyUtil.getSchemaProperty(context,"type_ToolPart");
            String strClearMPart =EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ClonePart.ManufacturingPartNotAllowed",context.getSession().getLanguage());
            output.append("<input type='text' readOnly='true' maxlength=\"\" size='20' name=\"partFamilyDisplay\" id='' value=\""+partFamilyDisplay+"\"/>"); //Added:366574
            output.append("<input type=\"hidden\" name=\"partFamily\"  value=\"\"/>");
            output.append("<input type=\"hidden\" name=\"partFamilyOID\"  value=\""+partFamilyId+"\"/>");
            output.append("<input type=\"hidden\" name=\"partFamilyAutoGenerate\"  value=\""+autoNameGenerate+"\"/>");
            output.append("<input type=\"button\" name=\"btnpartFamily\" value=\"...\" onClick=\"");
            output.append("javascript:showChooser('../engineeringcentral/emxEngrPartFamilySearchDialogFS.jsp?form=emxCreateForm&field=partFamilyDisplay&partFamilyId=partFamilyOID&partFamilyAutoGenName=partFamilyAutoGenerate&suiteKey=EngineeringCentral',");
            output.append("'600','600')\"");
            output.append(" />");
            output.append("<a href=\"JavaScript:clearPartFamily()\">");
            output.append(strClear);
            output.append("</a>");
            output.append("<script>function clearPartFamily(){document.emxCreateForm.partFamilyDisplay.value=\"\";document.emxCreateForm.partFamily.value=\"\";document.emxCreateForm.partFamilyOID.value=\"\";document.emxCreateForm.partFamilyAutoGenerate.value=\"TRUE\";document.emxCreateForm.Name.value=\"\";document.emxCreateForm.autoNameCheck.disabled=false;document.emxCreateForm.Name.disabled=false;}</script>");
            //for the bug 347114 starts
            output.append("<script> if(document.emxCreateForm.TypeActual.value==\"" );
            output.append(strMPart);
            output.append("\" || document.emxCreateForm.TypeActual.value==\"" );
            output.append(strMLPart);
            output.append("\" || document.emxCreateForm.TypeActual.value==\"" );
            output.append(strPHPart);
            output.append("\" || document.emxCreateForm.TypeActual.value==\"" );
            output.append(strSupPart);
            output.append("\" || document.emxCreateForm.TypeActual.value==\"" );
            output.append(strToolPart);
            output.append("\") {document.emxCreateForm.TypeActualDisplay.value='Part'; document.emxCreateForm.TypeActual.value='Part'; alert(\"");
            output.append(strClearMPart);
            output.append("\"); }</script>");
             //for the bug 347114 ends
            strOuput =output.toString();
            strOuput=StringUtils.replace(strOuput,"&","&amp;");


     } catch(Exception e) {
     }
     finally {
         return strOuput;
     }
     }
    // To Perform Post Process Actions after creating PartClone
    // Engineering  Central
    /**
     * This method is used to Post Process Actions after creating PartClone
     * This method Connect with Part Family using Part Family Member Relationship
     *  Connect to ECO object
     * Connect with Company using Design Responsibility Relationship
    *  @param context
     *            the eMatrix <code>Context</code> object
     * @return List Nothing
     * @throws Exception
     *             if the operation fails
     * @since EC X+3
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public int performPostProcessActions (Context context,String[] args)
    throws Exception
    {
        HashMap ProgramMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)ProgramMap.get("paramMap");
        HashMap requestMap = (HashMap)ProgramMap.get("requestMap");
        String ObjectId = (String) paramMap.get("objectId");

        DomainObject domObj = DomainObject.newInstance(context, ObjectId);
		// check TBE license for development parts
		String policyClass = domObj.getInfo(context, "policy.property[PolicyClassification].value");
		if ("Development".equalsIgnoreCase(policyClass)) {
			String[] app = {"ENO_TBE_TP", "ENO_LIB_TP", "ENO_LBC_TP" };
			ComponentsUtil.checkLicenseReserved(context, app);
		}

        DomainObject dom=DomainObject.newInstance(context,ObjectId);
        String strMP = PropertyUtil.getSchemaProperty(context,"type_ManufacturingPart");
		boolean bmpart=mxType.isOfParentType(context,dom.getType(context),strMP);
        if(!bmpart)
        {
        String partFamilyID = (String)requestMap.get("partFamilyOID");
        String ecoID = (String)requestMap.get("ECOOID");
        String DesignResponsibilityID = (String)requestMap.get("DesignResponsibilityOID");

        String attrPartFamilyNameGeneratorOn = ATTRIBUTE_PART_FAMILY_NAME_GENERATOR_ON;
        String partFamilyMemberRel = RELATIONSHIP_CLASSIFIED_ITEM;
        String designResponsibilityRel = RELATIONSHIP_DESIGN_RESPONSIBILITY;

        //072884V6R2012 start
        boolean isMFGInstalled = com.matrixone.apps.engineering.EngineeringUtil.isMBOMInstalled(context);
        if (isMFGInstalled) {
            com.matrixone.apps.engineering.Part mepPart = new com.matrixone.apps.engineering.Part(ObjectId);
            mepPart.setEndItem(context);
        }
        //072884V6R2012 end
        String usePartFamilyForName = "false";
        String name="";

        PartFamily partFamily = null;
        if (partFamilyID != null
                    && !("null").equalsIgnoreCase(partFamilyID)
                    && partFamilyID.length() > 0) {
                try {
                    ContextUtil.pushContext(context);

                    partFamily = new PartFamily(partFamilyID);
                    partFamily.open(context);
                    if (partFamily.exists(context)) {
                        // check the "Part Family Name Generator On" attribute
                        usePartFamilyForName = partFamily.getAttributeValue(
                                context, attrPartFamilyNameGeneratorOn);
                        if (usePartFamilyForName.equalsIgnoreCase("TRUE")) {
                            name = partFamily.getPartFamilyMemberName(context);
                            dom.setName(context, name);

							//IR-059862V6R2011x
							String relationshipPartRevision = PropertyUtil.getSchemaProperty(context, "relationship_PartRevision");
							String strPartMasterId = dom.getInfo(context,"to["+relationshipPartRevision+"].from.id");
							if(strPartMasterId != null){
								DomainObject.newInstance(context,strPartMasterId).setName(context, name);
							}
							//IR-059862V6R2011x Ends
                        }
                        // connect part family to part
                        DomainRelationship.connect(context,partFamily,new RelationshipType(partFamilyMemberRel),dom);

                    } else {
                        usePartFamilyForName = "false";
                        partFamily.close(context);
                    }
                } catch (Exception exp) {
                    partFamily.close(context);
                    usePartFamilyForName = "false";
                } finally {
                    partFamily.close(context);
                    ContextUtil.popContext(context);
                }
            }

             // Connect to ECO object
            if( ecoID != null && !("null").equalsIgnoreCase(ecoID) && ecoID.length()>0)
            {
                BusinessObject ecoObject = null;
                boolean bECOExists= false;
                try
                {
                  ecoObject = new BusinessObject(ecoID);
                  ecoObject.open(context);
                   bECOExists = ecoObject.exists(context);

                }
                catch (Exception exp)
                {
                    bECOExists= false;
                }

                if (bECOExists)
                {
					DomainRelationship.connect(context,new DomainObject(ecoID), new RelationshipType(RELATIONSHIP_AFFECTED_ITEM),dom);

                    ecoObject.close(context);
                }
            }
           // Connect with Company using Design Responsibility Relationship
            if( DesignResponsibilityID != null && !("null").equalsIgnoreCase(DesignResponsibilityID) && DesignResponsibilityID.length()>0)
            {
                BusinessObject company = null;
                boolean companyExists= false;
                StringList rdoList = FrameworkUtil.split(DesignResponsibilityID, ",");
                String strRDOId = "";

                //Added the while condition to support Creation with Multiple RDOs
                Iterator itrRDO = rdoList.iterator();
                while (itrRDO.hasNext())
                {
                    strRDOId = (String)itrRDO.next();
                    DomainObject dom1=DomainObject.newInstance(context,strRDOId);
                    try
                    {
                        company = new BusinessObject(strRDOId);
                        company.open(context);
                        companyExists = company.exists(context);

                    }
                    catch (Exception exp)
                    {
                        companyExists= false;
                    }
                    if (companyExists)
                    {
                        if (company != null && designResponsibilityRel != null)
                        {
                            DomainRelationship.connect(context,dom1,new RelationshipType(designResponsibilityRel),dom);
                        }
                        company.close(context);
                    }
                }
	        }

        }else{
        	String strMessage = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.ClonePart.ManufacturingPartNotAllowed");
            emxContextUtil_mxJPO.mqlNotice(context,strMessage);
            return 1;
        }
        return 0;
    }

/**
     * Display the Design Responsibility Item field in APPPartClone WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operations fails
     * @since EC - X3
     */

    public Object  showDesignResponsibilityItem(Context context,String[] args)throws Exception
    {
        StringBuffer sbReturnString = new StringBuffer(1024);
        try{
            String strClear =EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",context.getSession().getLanguage());
            sbReturnString.append("<input type=\"text\"  name=\"DesignResponsibilityDisplay\" id=\"\" value=\"");
            sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
            sbReturnString.append("</input>");
            sbReturnString.append("<input type=\"hidden\"  name=\"DesignResponsibilityOID\" value=\"");
            sbReturnString.append("\">");
            sbReturnString.append("</input>");
            sbReturnString.append("<input type=\"button\" name=\"btnCompany\" value=\"...\" onclick=\"javascript:showChooser('../engineeringcentral/emxpartRDOSearchDialogFS.jsp?form=emxCreateForm&amp;field=DesignResponsibilityDisplay&amp;fieldId=DesignResponsibilityOID&amp;searchLinkProp=SearchRDOLinks&amp;objectId=");      sbReturnString.append("&amp;fieldNameOID=DesignResponsibilityOID&amp;relId=null&amp;suiteKey=Components','700','500')\">");
            sbReturnString.append("</input>");
            sbReturnString.append("<a href=\"JavaScript:basicClear('DesignResponsibility')\">");
            sbReturnString.append(strClear);
            sbReturnString.append("</a>");
        }
        catch(Exception ex){
        }
        return sbReturnString.toString();
    }

     /**
 * Checks whether change management is allowed for Maufacturer Equivalent Parts.
 * @param context the eMatrix <code>Context</code> object
 * @param args holds a HashMap with the following entries:
 * objectId - a String containing the Part id.
 * languageStr - s String containing the language setting.
 * @return Object - boolean true we are to allow change management for the Part.
 * @throws Exception if operation fails
 * @since EngineeringCentral BX3
 */
 public boolean checkTypeChangeMgmtForMEP(Context context, String[] args)
 throws Exception
 {
        boolean bAllowChangeMgmt = false;
        // Read the change management property.
        String sAllowChangeMgmt =EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.AllowChangeManagementForManufacturerEquivalentParts");
        if (sAllowChangeMgmt!=null && sAllowChangeMgmt.equalsIgnoreCase("true")) {
              bAllowChangeMgmt = true;
        }
        return bAllowChangeMgmt;
 }

    /**
     * Show the Usage Location Status field.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a packed HashMap.
     * @return a html String to show the Usage location field.
     * @throws Exception
     *             if the operation fails.
     */
    public String showUsageLocationStatus(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String languageStr = (String) paramMap.get("languageStr");
        StringList strListLocStatus = FrameworkUtil.getRanges(context, PropertyUtil.getSchemaProperty(context,"attribute_LocationStatus"));

        int size = strListLocStatus.size();
        int j = 0;
        StringBuffer outPut = new StringBuffer(128);
        outPut.append("<select name=\"selStatus\" id=\"selStatus\" >");
        while( j < size)
        {
            String location = (String)strListLocStatus.get(j);
            String strListLocStatusDisplay=i18nNow.getRangeI18NString(PropertyUtil.getSchemaProperty(context,"attribute_LocationStatus"), location,languageStr);
                outPut.append("<option value=\""+XSSUtil.encodeForHTMLAttribute(context,location)+"\">"+XSSUtil.encodeForXML(context,strListLocStatusDisplay)+"</option>");
            j++;
        }
        outPut.append("</select>");
        return outPut.toString();
    }

/**
     * Connects the created MPN for the given
     * Enterprise Part and location objects.
     *
     * @param context
     *            The Matrix Context.
     * @param args
     *            holds a packed HashMap which contains usagelcoation,mepid ep
     *            id,manufacturer,mfg location,loc status
     * @throws FrameworkException
     *             If the operation fails.
     */

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void createMPN(Context context, String[] args) throws Exception {
        final String LOCATION_EQUIVALENT_OBJECT = "type_LocationEquivalentObject";
        final String LOCATION_EQUIVALENT_POLICY = "policy_LocationEquivalent";
        String[] smepObjId={};
        String smepId="";
        try {
            DomainObject doEntPart = null;
            DomainObject doLocation = null;
            DomainObject doManufacturer = null;
            DomainObject doLocEquiv = null;
            DomainObject mepObject = null;
            String sLocEquivObjId = null;
            ContextUtil.startTransaction(context, true);
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");

            HashMap paramMap = (HashMap) programMap.get("paramMap");
            smepId = (String) paramMap.get("objectId");
            smepObjId =new String[]{smepId};

            String sLocId = (String) requestMap.get("UsageLocation");
            String sLocPreference = (String) requestMap.get("selPreference");
            String sLocStatus = (String) requestMap.get("selStatus");
            String manufacturerId = (String) requestMap.get("Manufacturer");
            String sEpId = (String) requestMap.get("parentOID");

            if (manufacturerId != null && !manufacturerId.equals("null")
                    && manufacturerId.length() > 0) {
                doManufacturer = new DomainObject(manufacturerId);
            } else {
                throw new MatrixException(
                        "emxManufacturerEquivalent.Part.InvalidManufacturer");
            }
            Company contextComp = com.matrixone.apps.common.Person.getPerson(
                    context).getCompany(context);
            StringList selectList = new StringList(2);
            selectList.addElement(DomainObject.SELECT_NAME);
            selectList.addElement(DomainObject.SELECT_ID);
            Map compMap = contextComp.getInfo(context, selectList);

            String sDefaultLocId = (String) compMap.get(DomainObject.SELECT_ID);
            DomainObject doDefaultLocation = new DomainObject(sDefaultLocId);
            if (sLocId != null && sDefaultLocId.equals(sLocId)) {
                sLocId = null;
            }
            if (sLocId != null && !"null".equals(sLocId) && sLocId.length() > 0) {
                doLocation = new DomainObject(sLocId);
            }
            if (smepId != null && !"null".equals(smepId) && smepId.length() > 0) {
                mepObject = DomainObject.newInstance(context, smepId);
            }
            if (sEpId != null && !sEpId.equals("null") && sEpId.length() > 0) {
                doEntPart = new DomainObject(sEpId);
            }
            String sCompEngrRole = PropertyUtil.getSchemaProperty(context,"role_ComponentEngineer");
            String sDesignEngrRole = PropertyUtil.getSchemaProperty(context,"role_DesignEngineer");
            String sMfgEngrRole = PropertyUtil.getSchemaProperty(context,"role_ManufacturingEngineer");

            if (doEntPart != null) {
                // generating autonamed intermediate location equivalent object
                if (sLocId != null && !"null".equals(sLocId)
                        && sLocId.length() > 0) {
                    // starting of the code for the bug 318698
                    sLocEquivObjId = FrameworkUtil.autoName(context,
                            LOCATION_EQUIVALENT_OBJECT, "",
                            LOCATION_EQUIVALENT_POLICY, null, null, false,
                            false);
                    // end of the code for the bug 318698..
                }

                if (sLocEquivObjId != null) {
                    doLocEquiv = DomainObject.newInstance(context,
                            sLocEquivObjId);
                }
                // connect MEP to hostCompany directly using Allocation Resp
                // relation
                // (Corporate Context)
                if (doDefaultLocation != null) {
                    // connecting MEP and hostCompany with Allocation Resp
                    // relationship
                    RelationshipType relAllocResp = new RelationshipType(
                            RELATIONSHIP_ALLOCATION_RESPONSIBILITY);
                    DomainRelationship doRelationship = DomainRelationship
                            .connect(context, doDefaultLocation, relAllocResp,
                                    mepObject);

                    doRelationship.setAttributeValue(context,
                          ATTRIBUTE_LOCATION_STATUS, sLocStatus);
                    doRelationship.setAttributeValue(context,
                        ATTRIBUTE_LOCATION_PREFERENCE, sLocPreference);
                }
                // connecting Location Context MEP relationships via
                // intermediate
                // location equivalent object
                if (doLocEquiv != null) {
                    // connecting MEP from Loc Equiv Obj for selected location
                    // with rel_ManfEquivalent
                    RelationshipType relManuEquiv = new RelationshipType(
                            RELATIONSHIP_MANUFACTURER_EQUIVALENT);
                   DomainRelationship
                            .connect(context, doLocEquiv, relManuEquiv,
                                    mepObject);

                    if (doLocation != null) {
                        // connecting selected Location from Loc Equiv Obj with
                        // Allocation Responsibility relationship
                        RelationshipType relAllocResp = new RelationshipType(
                                RELATIONSHIP_ALLOCATION_RESPONSIBILITY);
                        DomainRelationship doRelationship = DomainRelationship
                                .connect(context, doLocation, relAllocResp,
                                        doLocEquiv);

                        doRelationship.setAttributeValue(context,
                                ATTRIBUTE_LOCATION_STATUS, sLocStatus);
                        doRelationship.setAttributeValue(context,
                                ATTRIBUTE_LOCATION_PREFERENCE, sLocPreference);
                    }
                }

                // If context is a component engineer, allow the connection
                // of the mep to the enterprise part by switching user to super
                // user. This will enable these connections regardless of the
                // access
                // o the component engineer in that state.
                if (context.isAssigned(sCompEngrRole) || context.isAssigned(sDesignEngrRole) || context.isAssigned(sMfgEngrRole)) {
                    if (sEpId != null && !"null".equalsIgnoreCase(sEpId)
                            && !"".equalsIgnoreCase(sEpId)) {

                        // if no location is chosen,MEP created shall be
                        // corporate context
                        // and this MEP is directly connected to EP using
                        // Manufacturer Equivalent
                        // relationship
                        if (doLocEquiv == null) {
                            // connecting EP and MEP directly with Manufacturer
                            // Equivalent relationship
                            ContextUtil.pushContext(context);
                            String conEpMEPCmd = "connect bus $1 relationship $2 from $3;";
                            try{
                            	MqlUtil.mqlCommand(context,"history off;");
                            	MqlUtil.mqlCommand(context,conEpMEPCmd, mepObject.getInfo(context, SELECT_ID), RELATIONSHIP_MANUFACTURER_EQUIVALENT,sEpId);
                            	MqlUtil.mqlCommand(context,"history on;");
							}
                              finally{ MqlUtil.mqlCommand(context,"history on;");}
                            ContextUtil.popContext(context);

                            String historyEpMEPCommand = "modify bus $1 add history $2 comment $3";
                            MqlUtil.mqlCommand(context,historyEpMEPCommand,sEpId,"connect","connect "+RELATIONSHIP_MANUFACTURER_EQUIVALENT+"  to "+mepObject.getInfo(context, SELECT_NAME));
                        }

                        ContextUtil.pushContext(context);
                        if (doLocEquiv != null) {
                            String connectLocCmd = "connect bus $1 relationship $2 from $3;";
                           try{
                        	   MqlUtil.mqlCommand(context, "history off;");
                        	   MqlUtil.mqlCommand(context, connectLocCmd, doLocEquiv.getInfo(context, SELECT_ID), RELATIONSHIP_LOCATION_EQUIVALENT, sEpId);
							   MqlUtil.mqlCommand(context, "history on;");
						   }
                                     finally{ MqlUtil.mqlCommand(context,"history on;");}

                            String historyCommand = "modify bus $1 add history $2 comment $3";
                            MqlUtil.mqlCommand(context, historyCommand, sEpId,"connect","connect "+RELATIONSHIP_LOCATION_EQUIVALENT+"  to "+doLocEquiv.getInfo(context, SELECT_NAME));
                        }
                        ContextUtil.popContext(context);
                    }
                }
                // context is not component engineer role
                else {
                    // connecting Location Context MEP relationships via
                    // intermediate
                    // location equivalent object
                    if (doLocEquiv != null) {
                        // connecting EP to Loc Equiv Obj for selected location
                        // with rel_LocationEquivalent
                        // doEntPart.connect(context,
                        // RELATIONSHIP_LOCATION_EQUIVALENT , doLocEquiv,
                        // false);
                        doEntPart.addRelatedObject(context,
                                new RelationshipType(
                                        //RELATIONSHIP_MANUFACTURER_EQUIVALENT),-IR-026840V6R2011-need to refer rel_LocationEquivalent when the location is chosen
                                        RELATIONSHIP_LOCATION_EQUIVALENT),
                                false, doLocEquiv.getInfo(context, SELECT_ID));
                    }
                    // connect corporateMEP to EP
                    else {
                        doEntPart.addRelatedObject(context,
                                new RelationshipType(
                                        RELATIONSHIP_MANUFACTURER_EQUIVALENT),
                                false, smepId);
                    }
                }
                // EPart null
            } else {
                // connect MEP to hostCompany directly using Allocation Resp
                // relation
                // (Corporate Context)
                if (doDefaultLocation != null) {
                    // connecting MEP and hostCompany with Allocation Resp
                    // relationship
                    RelationshipType relAllocResp = new RelationshipType(
                            RELATIONSHIP_ALLOCATION_RESPONSIBILITY);
                    DomainRelationship doRelationship = DomainRelationship
                            .connect(context, doDefaultLocation, relAllocResp,
                                    mepObject);

                    doRelationship.setAttributeValue(context,
                            ATTRIBUTE_LOCATION_STATUS, sLocStatus);
                    doRelationship.setAttributeValue(context,
                            ATTRIBUTE_LOCATION_PREFERENCE, sLocPreference);
                }

                // connect MEP to selected Location directly using Allocation
                // Resp relation
                // (Corporate Context)
                if (doLocation != null) {
                    // connecting MEP and selected Location with Allocation
                    // Responsibility relationship
                    RelationshipType relAllocResp = new RelationshipType(
                            RELATIONSHIP_ALLOCATION_RESPONSIBILITY);
                    DomainRelationship doRelationship = DomainRelationship
                            .connect(context, doLocation, relAllocResp,
                                    mepObject);

                    doRelationship.setAttributeValue(context,
                            ATTRIBUTE_LOCATION_STATUS, sLocStatus);
                    doRelationship.setAttributeValue(context,
                            ATTRIBUTE_LOCATION_PREFERENCE, sLocPreference);
                }

            }// end of if-else (enterprise Part != null)

            if (doManufacturer != null) {
                // connect to company with "Manufacturing Responsibility"
                mepObject.addRelatedObject(context, new RelationshipType(
                        RELATIONSHIP_MANUFACTURING_RESPONSIBILITY), true,
                        manufacturerId);

            }

            ContextUtil.commitTransaction(context);

        } catch (Exception e) {
            try{
            Part part = new Part(smepId);
            ContextUtil.abortTransaction(context);
            part.deleteMEPs(context,smepObjId);
             }catch(Exception ex){
                 throw (new Exception("MEP not created.Please create again with valid data"));
             }
            throw (new FrameworkException(e));
        }
    }


    /**
     * Show the Usage Location Preference field.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds a packed HashMap.
     * @return a html String to show the Usage location field.
     * @throws Exception
     *             if the operation fails.
     */
    public String showUsageLocationPreference(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String languageStr = (String) paramMap.get("languageStr");
        StringList strListLocStatus = FrameworkUtil.getRanges(context, PropertyUtil.getSchemaProperty(context,"attribute_LocationPreference"));
        int size = strListLocStatus.size();
        int j = 0;
        StringBuffer outPut = new StringBuffer(128);
        outPut.append("<select name=\"selPreference\" id=\"selPreference\" >");
        while( j < size)
        {
            String location = (String)strListLocStatus.get(j);
            String strListLocStatusDisplay=i18nNow.getRangeI18NString(PropertyUtil.getSchemaProperty(context,"attribute_LocationPreference"), location,languageStr);
                outPut.append("<option value=\""+XSSUtil.encodeForHTMLAttribute(context, location)+"\">"+XSSUtil.encodeForXML(context, strListLocStatusDisplay)+"</option>");
            j++;
        }
        outPut.append("</select>");
        return outPut.toString();
    }
	 /**
     * Show the Manufacturer field.
     * * @param context the eMatrix <code>Context</code> object.
     * @param args holds a packed HashMap.
     * @return a html String to show the Manufacturer field.
     * @throws Exception if the operation fails.
     */
    public String showManufacturerFieldForMPN(Context context, String[] args) throws Exception {
    StringBuffer returnString = new StringBuffer(1024);
    try{
        StringList selectList = new StringList(2);
        selectList.addElement(DomainObject.SELECT_NAME);
        selectList.addElement(DomainObject.SELECT_ID);
        com.matrixone.apps.common.Person contextPerson = com.matrixone.apps.common.Person
                .getPerson(context);
        Company contextComp = contextPerson.getCompany(context);
        Map compMap = contextComp.getInfo(context, selectList);

        String defaultManufacturer = (String) compMap
                .get(DomainObject.SELECT_NAME);

		if(defaultManufacturer.indexOf('&') != -1){
           defaultManufacturer = FrameworkUtil.findAndReplace(defaultManufacturer,"&","&amp;");
          }

        String defaultManufacturerId = (String) compMap
                .get(DomainObject.SELECT_ID);
      //Multitenant
        String strReset = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common.Reset");
        String strURL = "../engineeringcentral/emxpartRDOSearchDialogFS.jsp?form=emxCreateForm&amp;field=ManufacturerDisplay&amp;fieldId=Manufacturer&amp;searchLinkProp=SearchOrgLinks&amp;searchMode=Manufacturer&amp;fieldManufacturerLocation=manufacturingLocationDisplay&amp;fieldManufacturerLocationId=ManufacturerLocationOID";
        returnString.append("<input type=\"text\" name=\"ManufacturerDisplay\"");
        returnString.append(" size=\"20\" value=\"" + XSSUtil.encodeForHTMLAttribute(context, defaultManufacturer) + "\"");
        returnString.append(" readonly=\"readonly\">");
        returnString.append("</input>");
        returnString.append("<input class=\"button\" type=\"button\"");
        returnString.append(" name=\"btnOrginatorChooser\" size=\"200\" ");
        returnString.append("value=\"...\" alt=\"\"  onClick=\"javascript:showChooser('");
        returnString.append(strURL);
        returnString.append("', 700, 500)\">");
        returnString.append("</input>");

        returnString.append("<a href=\"JavaScript:ResetField('forms[0]','Manufacturer','ManufacturerDisplay','"
                        + XSSUtil.encodeForJavaScript(context, defaultManufacturer)
                        + "','"
                        + XSSUtil.encodeForJavaScript(context,defaultManufacturerId)+"')\"");
        returnString.append('>');
        returnString.append(XSSUtil.encodeForXML(context, strReset));
		returnString.append("</a>");

        returnString.append("<input type=\"hidden\" name=\"Manufacturer\" value=\""
                        + XSSUtil.encodeForHTMLAttribute(context, defaultManufacturerId) + "\"></input>");
        returnString.append("<input type=\"hidden\" name=\"manufacturingLocationDisplay\" value= \"\" ></input>");
        returnString.append("<input type=\"hidden\" name=\"ManufacturerLocationOID\" value= \"\" ></input>");

       }catch(Exception e){
       } finally {
           return returnString.toString();
       }
    }
	 /**
     * Disconnect/Connect EBOM relationship.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds a packed HashMap.
     * @return connection/disconnection result.
     * @throws Exception if the operation fails.
     */
    @com.matrixone.apps.framework.ui.ConnectionProgramCallable
    public HashMap updateBOM (Context context, String args[]) throws Exception
    {
        MapList mlItems = new MapList();
        HashMap doc     = new HashMap();

        HashMap request        = (HashMap)JPO.unpackArgs(args);
        Element elm            = (Element)request.get("contextData");
        String strObjectId     = (String) request.get("objectId");

        java.util.List listObjects = elm.getChildren("object");
        Iterator itrObjects = listObjects.iterator();
		// 372458
		String connectAsDerived = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReplaceBOM.Derived");
		String attrDerivedContext= PropertyUtil.getSchemaProperty(context,"attribute_DerivedContext");
//Added for IR-021267
        HashMap hmpAttributes = new HashMap();
        Element eleObject     = null;
        String sRowId         = "";
        String strChildId     = "";
        String sRelId         = "";
        String sRelTypeSymb   = "";
        String markup         = "";
        String strParam2      = "";
        String sRelType = null;
        java.util.List listColumns= null;
        Iterator itrColumns = null;
        Element eleColumn = null;
        DomainObject parentObj = new DomainObject(strObjectId);

        DomainObject childObj  = new DomainObject();
        try{
			ContextUtil.pushContext(context);
            while (itrObjects.hasNext()){
//Modified for IR-021267
				hmpAttributes.clear();
                eleObject     = (Element) itrObjects.next();
                sRowId         = eleObject.getAttributeValue("rowId");
                strChildId     = eleObject.getAttributeValue("objectId");
                sRelId         = eleObject.getAttributeValue("relId");
                sRelTypeSymb   = eleObject.getAttributeValue("relType");
                markup         = eleObject.getAttributeValue("markup");
                strParam2      = eleObject.getAttributeValue("param2");

                 sRelType = null;

                if (sRelTypeSymb != null){
                    sRelType = (String) PropertyUtil.getSchemaProperty(context,sRelTypeSymb);
				}

                if ("add".equals(markup)){
                    //Logic for ur ADD Opearation
                    //get all attributes
//Modified for IR-021267
					listColumns = eleObject.getChildren("column");
					 itrColumns = listColumns.iterator();
                    while (itrColumns.hasNext()){
                         eleColumn = (Element) itrColumns.next();
						hmpAttributes.put(eleColumn.getAttributeValue("name"), eleColumn.getText());
					}


                    childObj.setId(strChildId);

                    //connect the parent part and child part
                    DomainRelationship dr = DomainRelationship.connect(context,
                                                                       parentObj,
                                                                       sRelType,
                                                                       childObj);

                    //set attributes to EBOM realtionship
                    dr.setAttributeValues(context, (Map) hmpAttributes);

					if (strParam2 != null && strParam2.length() > 0)
					{
                        StringList slParamObjs = childObj.getInfoList(context, "from["+RELATIONSHIP_DERIVED+"]."+DomainConstants.SELECT_TO_ID);
                        if(slParamObjs == null){
							slParamObjs = new StringList();
						}
                        if(!slParamObjs.contains(strParam2)){
                            // 372458
                            if(connectAsDerived.equalsIgnoreCase("true")){
                                DomainRelationship doRelDerived = DomainRelationship.connect(context,
                                new DomainObject(strParam2),
                                RELATIONSHIP_DERIVED,
                                childObj);
                                doRelDerived.setAttributeValue(context, attrDerivedContext, "Replace");
                            }
					}
					}

                    //creating a returnMap having all the details abt the changed row.
                    HashMap returnMap = new HashMap();
                    returnMap.put("oid", strChildId);
                    returnMap.put("rowId", sRowId);
                    returnMap.put("pid", parentObj.getId());
                    returnMap.put("relid", dr.toString());
                    returnMap.put("markup", markup);
                    returnMap.put("columns", hmpAttributes);
                    mlItems.add(returnMap);  //returnMap having all the details abt the changed row.

				}
                else if ("cut".equals(markup)){
                    //Logic for ur CUT Opearation
                    DomainRelationship.disconnect(context, sRelId);

                    //creating a returnMap having all the details abt the changed row.
                    HashMap returnMap = new HashMap();
                    returnMap.put("oid", strChildId);
                    returnMap.put("rowId", sRowId);
                    returnMap.put("relid", sRelId);
                    returnMap.put("markup", markup);
                    returnMap.put("columns", hmpAttributes);
                    mlItems.add(returnMap);

			}

                else if ("resequence".equals(markup)){
                    //Logic for ur resequence Opearation

                    //creating a returnMap having all the details abt the changed row.
                    HashMap returnMap = new HashMap();
                    returnMap.put("oid", strChildId);
                    returnMap.put("rowId", sRowId);
                    returnMap.put("relid", sRelId);
                    returnMap.put("markup", markup);
                    returnMap.put("columns", hmpAttributes);
                    mlItems.add(returnMap);
		}
		}
             doc.put("Action", "success"); //Here the action can be "Success" or "refresh"
             doc.put("changedRows", mlItems);//Adding the key "ChangedRows" which having all the data for changed Rows
			ContextUtil.popContext(context);
		}
        catch(Exception e){
            doc.put("Action", "ERROR"); // If any exeception is there send "Action" as "ERROR"
            doc.put("Message", e.getMessage()); // Error message to Display
        }

        return doc;
    }

	/**
     * connects the cloned part to source part with Derived relationship.
     * @param context the eMatrix <code>Context</code> object
     * @param args the Program Map
     * @throws Exception if the operations fails
     * @since Engineering Central - X3
     */
	public void connectClonedPart(Context context, String args[]) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		HashMap paramMap = (HashMap) programMap.get("paramMap");

		String strClonedPartId = (String) paramMap.get("objectId");

		String [] strClonedIds = (String []) requestMap.get("copyObjectId");

		String strSourcePartId = (String) paramMap.get("New OID");

		if (strSourcePartId == null || strSourcePartId.length() <= 0)
		{
			if (strClonedIds != null)
			{
				strSourcePartId = strClonedIds[0];
			}
		}

		if (strSourcePartId != null && strSourcePartId.length() > 0)
		{
			 Part partBean = new Part();
			 try
			 {
				 ContextUtil.pushContext(context);
				 partBean.connectClonedObject(context, strSourcePartId, strClonedPartId);
			 }
			 catch (Exception ex)
			 {
			 }
			 finally
			 {
				 ContextUtil.popContext(context);
			 }
		}

	}

	/**
     * handleNotificationForSubstitutes.
     * This method used to Send Subscription notification to the user.
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            param args holds objectId as first argument and Subscription event as second argument
	 *
     * @return a html String to show the Usage location field.
     * @throws Exception
     *             if the operation fails.
* @since Engineering Central - X3
     */
    public void handleNotificationForSubstitutes(Context context, String[] args)
            throws Exception {
		String strFromEBOMRelid = args[0];
		String strSubscribedEvent = args[1];
		String strMQLCommand = "print connection $1 select $2 dump $3";
		String strres = MqlUtil.mqlCommand(context,strMQLCommand,strFromEBOMRelid,"from.id","|");
		String[] oids = new String[3];
        String strSubPartId = args[2];
        if(strSubPartId!=null && !strSubPartId.equals("")){
            oids[0] = strres;
            oids[1] = strSubPartId;
            oids[2] = strSubscribedEvent;
            if(strSubscribedEvent.indexOf("Removed")!=-1){
                handlePartRemovedSubscriptionEvent(context,oids);
            }
            else{
                handlePartAddedSubscriptionEvent(context,oids);
            }
         }else{
             oids[0] = strres;
             oids[1] = strSubscribedEvent;
             handleSubscriptionEvent(context,oids);
        }
    }


	/**
     * Gets all the Part and it's Subtype's objects that are in released state.
	 * Used with the excludeOIDprogram setting of Autonomy Search in case of CopyTo functionality
     * * @param context the eMatrix <code>Context</code> object.
     * @param args holds a packed HashMap.
     * @return a StringList that contains.
     * @throws Exception if the operation fails.
     * @since Engineering Central - X3
     */
    public StringList getReleasedParts(Context context, String args[]) throws Exception
    {
        StringList idList = new StringList();
        String strPartPolicy= PropertyUtil.getSchemaProperty(context, "policy_ECPart");
        String stateRelease = com.matrixone.apps.engineering.EngineeringUtil.getReleaseState(context,strPartPolicy);
        String sWhereExp = "current =="+stateRelease;
        SelectList resultSelects = new SelectList(1);
        resultSelects.add(DomainObject.SELECT_ID);
        MapList totalresultList = DomainObject.findObjects(context,
                                                    DomainConstants.TYPE_PART,
                                                    "*",
                                                    "*",
                                                    "*",
                                                    "*",
                                                    sWhereExp,
                                                    null,
                                                    true,
                                                    resultSelects,
                                                    (short) 0);
        for(int i=0;i < totalresultList.size();i++)
        {
            Map resultMap = (Map) totalresultList.get(i);
            String id = (String) resultMap.get(DomainObject.SELECT_ID);
            idList.add(id);
        }
        return idList;
    }

    /**
     * Gets all the ECR  states on or after Plan ECOand ECO  after Release
     * Used with the excludeOIDprogram setting of Autonomy Search in case Save SaveAS functionality
     * * @param context the eMatrix <code>Context</code> object.
     * @param args holds a packed HashMap.
     * @return a StringList that contains.
     * @throws Exception if the operation fails.
     * @since Engineering Central - X3
     */

    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getExcludedMarkupStatesECRECO(Context context, String args[]) throws Exception
    {

    	HashMap programMap = (HashMap) JPO.unpackArgs(args);

    	String sCallerPage = (String)programMap.get("callerPage");
        StringList excludeIdList = new StringList();
        StringList busSelects = new StringList(1);
        busSelects.addElement(DomainConstants.SELECT_ID);

        try
        {
			//MFG Added to ignore the DCR's in search results
        	if(EngineeringUtil.isMBOMInstalled(context) && null != sCallerPage && "ItemMarkup".equals(sCallerPage))
        	{
        		String ecoWhere = DomainObject.SELECT_MANUFACTURING_RESPONSIBILITY_FROM_ID + " != ''";
        		StringList objectSelects = new StringList(DomainObject.SELECT_ID);

	        	MapList resultsList = DomainObject.findObjects(
								                    context,                            // eMatrix context
								                    DomainObject.TYPE_ECR,              // type pattern
								                    DomainConstants.QUERY_WILDCARD,     // name pattern
								                    DomainConstants.QUERY_WILDCARD,     // revision pattern
								                    DomainConstants.QUERY_WILDCARD,     // owner pattern
								                    DomainConstants.QUERY_WILDCARD,     // vault pattern
								                    ecoWhere,  					      	// where expression
								                    true,								// Expand Type
								                    objectSelects);     				// object selects

						if(resultsList.size() > 0)
						{
							Iterator itrECRItr = resultsList.iterator();

							while (itrECRItr.hasNext())
							{
								Map ecrMap = (Map) itrECRItr.next();

								String strECRId = (String) ecrMap.get(DomainObject.SELECT_ID);

								if (strECRId != null && !DomainObject.EMPTY_STRING.equals(strECRId))
								{
										excludeIdList.add(strECRId);
								}
							}
						}
        	}

			String strMkpWhereClause = "(current == " + proposedState + " || current == " + approvedState + ")";

			String strPartId = (String) programMap.get("objectId");

			if (strPartId != null && strPartId.length() > 0)
			{

				DomainObject doPart = new DomainObject(strPartId);

				String strAppliedRel = "to[" + PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup") + "].from.id";
				String strProposedRel = "to[" + PropertyUtil.getSchemaProperty(context,"relationship_ProposedMarkup") + "].from.id";

				StringList strlObjectSelects = new StringList(2);
				strlObjectSelects.add(strAppliedRel);
				strlObjectSelects.add(strProposedRel);

				MapList mapListMarkups = doPart.getRelatedObjects(context,
										DomainConstants.RELATIONSHIP_EBOM_MARKUP,
										PropertyUtil.getSchemaProperty(context,"type_ItemMarkup"),
										strlObjectSelects,
										null, true, true, (short) 1, strMkpWhereClause, null);

				Iterator itrMarkups = mapListMarkups.iterator();

				while (itrMarkups.hasNext())
				{
					Map mapMarkup = (Map) itrMarkups.next();

					String strProposedRelId = (String) mapMarkup.get(strProposedRel);
					String strAppliedRelId = (String) mapMarkup.get(strAppliedRel);

					if (strProposedRelId != null && strProposedRelId.length() > 0)
					{
						excludeIdList.add(strProposedRelId);
					}
					if (strAppliedRelId != null && strAppliedRelId.length() > 0)
					{
						excludeIdList.add(strAppliedRelId);
					}

				}
			}


        } catch(Exception e){
            throw e;
        } finally {
            return excludeIdList;
        }
    }


    //IR-087314 added for BOM Compare Report---- Starts

    @com.matrixone.apps.framework.ui.ConnectionProgramCallable
    public HashMap validateStateForApplyForBOMCompare(Context context, String[] args)
    throws Exception
    {


		HashMap doc = new HashMap();
		HashMap request = (HashMap) JPO.unpackArgs(args);
		String parentObjectId=(String)request.get("objectId");
		Element elm = (Element) request.get("contextData");
		MapList chgRowsMapList = UITableIndented.getChangedRowsMapFromElement(context, elm);
		MapList mlItems = new MapList();
		String sErrorMsg="";
		String childObjectId="";
		try
		{

		for (int i = 0; i < chgRowsMapList.size(); i++) {
		                    HashMap changedRowMap = (HashMap) chgRowsMapList.get(i);
							childObjectId = (String) changedRowMap.get("childObjectId");

							String sRelId = (String) changedRowMap.get("relId");
							String sRowId = (String) changedRowMap.get("rowId");
							String markup = (String) changedRowMap.get("markup");
							// get parameters for replace operation
							String strParam3 = (String) changedRowMap.get("param3");
							String strParam1 = (String) changedRowMap.get("param1");
							HashMap columnsMap = (HashMap) changedRowMap.get("columns");

							String Usage = (String) columnsMap.get("Usage");
							String findNumber = (String) columnsMap.get("Find Number");

							String strQuantity = (String) columnsMap.get("Quantity");
							String compLoc = (String) columnsMap.get("Component Location");
							String refDes = (String) columnsMap.get("Reference Designator");
							String desc = (String) columnsMap.get("Description");

							String notes = "" ;
							if(columnsMap.containsKey("Notes")) {
								notes = (String) columnsMap.get("Notes");
							}
							//Multitenant
							String strStandrd = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Attribute.Usage.Standard");

							if (Usage == null || Usage.equals("")) {
								Usage = strStandrd;
							}

							HashMap hmRelAttributesMap = new HashMap();
							hmRelAttributesMap.put("Find Number", findNumber);
							hmRelAttributesMap.put("Reference Designator", refDes);
							hmRelAttributesMap.put("Component Location", compLoc);
							hmRelAttributesMap.put("Quantity", strQuantity);
							hmRelAttributesMap.put("Usage", Usage);

							if(!"".equalsIgnoreCase(notes)){
								hmRelAttributesMap.put("Notes", notes);
							}

							String sRelType = DomainConstants.RELATIONSHIP_EBOM;

		if ("add".equals(markup)) {

								// Logic for ur ADD Opearation
								DomainObject parentObj = DomainObject.newInstance(context);
								DomainObject childObj = DomainObject.newInstance(context);
								parentObj.setId(parentObjectId);
								childObj.setId(childObjectId);
		                        ContextUtil.pushContext(context);
		                        // setToObject if operation is replace
		                        DomainRelationship domRelation = null;
		                        if ("replace".equals(strParam3)) {
		                        	DomainRelationship.setToObject(context, strParam1, childObj);
		                        	domRelation = new DomainRelationship(strParam1);
		                        } else {
		                        	domRelation = DomainRelationship.connect(context, parentObj, sRelType, childObj);
		                        }
								if(desc != null && desc.length()>0)
								childObj.setDescription(context, desc);

								//TBE

								//TBE

								domRelation.setAttributeValues(context,(Map) hmRelAttributesMap);


								// creating a returnMap having all the details abt the changed row.
								HashMap returnMap = new HashMap();
								returnMap.put("oid", childObjectId);
								returnMap.put("rowId", sRowId);
								returnMap.put("pid", parentObj.getId());
								returnMap.put("relid", domRelation.toString());
								returnMap.put("markup", markup);
								returnMap.put("columns", columnsMap);
								mlItems.add(returnMap); // returnMap having all the

							}

							else if ("cut".equals(markup)) {
//								HashMap items = new HashMap();
								// Logic for ur CUT Opearation
								// do nothing is operation is replace. setToObject done for add row.
								if (!"replace".equals(strParam1))
									DomainRelationship.disconnect(context, sRelId);

								// creating a returnMap having all the details abt the
								// changed row.
								HashMap returnMap = new HashMap();
								returnMap.put("oid", childObjectId);
								returnMap.put("rowId", sRowId);
								returnMap.put("relid", sRelId);
								returnMap.put("markup", markup);
								returnMap.put("columns", columnsMap);
								mlItems.add(returnMap);

							} else if ("resequence".equals(markup)) {
								// Logic for ur resequence Opearation

								// creating a returnMap having all the details abt the
								// changed row.
								HashMap returnMap = new HashMap();
								returnMap.put("oid", childObjectId);
								returnMap.put("rowId", sRowId);
								returnMap.put("relid", sRelId);
								returnMap.put("markup", markup);
								returnMap.put("columns", columnsMap);
								mlItems.add(returnMap);
							}

							}

							doc.put("Action", "success");
		                    doc.put("changedRows", mlItems);
		}

		catch(Exception e){}

		DomainObject Cdobj=new DomainObject(childObjectId);
		String CstrPolicy=(String)Cdobj.getInfo(context, DomainConstants.SELECT_POLICY);
		String CpolicyClassification = EngineeringUtil.getPolicyClassification(context,CstrPolicy);



		DomainObject dobj=new DomainObject(parentObjectId);

		String strPolicy=(String)dobj.getInfo(context, DomainConstants.SELECT_POLICY);
		String policyClassification = EngineeringUtil.getPolicyClassification(context,strPolicy);

		try
		{
				HashMap argMap = new HashMap();
				argMap.put("objectId", parentObjectId);

				Boolean  blnIsApplyAllowed = (Boolean)JPO.invoke(context,"emxENCActionLinkAccess",null,"isApplyAllowed",JPO.packArgs(argMap),Boolean.class);

				if (!(blnIsApplyAllowed.booleanValue()))
				{

					if ("Unresolved".equalsIgnoreCase(policyClassification))
					  {
						doc.put("Action", "ERROR");

						String langStr = context.getSession().getLanguage();
						sErrorMsg=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.BOM.ModifyErrorConfiguredPart1",langStr);
					  }
					else{
					doc.put("Action", "ERROR");

					String langStr = context.getSession().getLanguage();
					sErrorMsg=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.BOM.ModifyError",langStr);
			        String strCurrentState=dobj.getInfo(context,SELECT_CURRENT);
					if(!strCurrentState.equals(DomainConstants.STATE_PART_RELEASE)) {
						sErrorMsg  =getErrorMessageForBOMCompareSync(context, dobj);
					}
                 }
					doc.put("Message", sErrorMsg);
			}
				else{
					if ("Unresolved".equalsIgnoreCase(CpolicyClassification))
					  {
						doc.put("Action", "ERROR");

						String langStr = context.getSession().getLanguage();
						sErrorMsg=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.BOM.ModifyErrorConfiguredPart2",langStr);
						doc.put("Message", sErrorMsg);
					  }

				}
		}	catch (Exception excep)
		{
		}

		return doc;
    }

    // -- Ends

    /**
    * To allow Apply edits in EBOM emxTable/Indented  Table
    * based on the state of the Parent assembly Part
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds objectId.
    * @return HashMap.
    * @throws Exception If the operation fails.
    * @since X+3.
    *
    */

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public HashMap validateStateForApply(Context context, String[] args)
        throws Exception
    {
		HashMap retMap = new HashMap();
		// Need to pass a Refresh for Auto sync, because prod name should be shown in Specification Title column
		retMap.put("Action", EBOMAutoSync.isAutoSyncDisabled(context) ? "success" : "Refresh");

		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

			HashMap requestMap = (HashMap) programMap.get("requestMap");

			Document doc            = (Document) requestMap.get("XMLDoc");

			com.matrixone.jdom.Element rootElement = doc.getRootElement();

			java.util.List objList = rootElement.getChildren("object");
			Iterator objItr = objList.iterator();
			while(objItr.hasNext())
			{
				com.matrixone.jdom.Element eleChild = (com.matrixone.jdom.Element) objItr.next();
				com.matrixone.jdom.Attribute attrAction = eleChild.getAttribute("markup");
             	com.matrixone.jdom.Attribute attrObjectId = null;
     			String strObjectId = null;

				if (attrAction != null && "changed".equals(attrAction.getValue()))
				{
					attrObjectId = eleChild.getAttribute("parentId");
					strObjectId = attrObjectId.getValue();
                    //364281
                    retMap.put("Action", "refresh");
                    //364281
				}
				else
				{
					attrObjectId = eleChild.getAttribute("objectId");
					strObjectId = attrObjectId.getValue();

			}

				HashMap argMap = new HashMap();
				argMap.put("objectId", strObjectId);

				boolean isCamInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionX-BOMCostAnalytics",false,null,null);
				Boolean  blnIsApplyAllowed ;

				if(isCamInstalled)
	                        {
					blnIsApplyAllowed =(Boolean)JPO.invoke(context,"CAENCActionLinkAccessBase",null,"isApplyAllowed",JPO.packArgs(argMap),Boolean.class);
	                        } else
	                        {
				blnIsApplyAllowed = (Boolean)JPO.invoke(context,"emxENCActionLinkAccess",null,"isApplyAllowed",JPO.packArgs(argMap),Boolean.class);
                                }

				if (!(blnIsApplyAllowed.booleanValue()))
				{

					retMap.put("Action", "ERROR");

					String langStr = context.getSession().getLanguage();
					String sErrorMsg=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.BOM.ModifyError",langStr);

					DomainObject domObj = new DomainObject(strObjectId);
					matrix.db.Access mAccess = domObj.getAccessMask(context);
					if(!mAccess.has(Access.cModify))
						sErrorMsg = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.BOM.SaveError",langStr);

					retMap.put("Message", sErrorMsg);

					break;
				}

			}

		}
		catch (Exception excep)
		{
		}

		return retMap;

    }
/**
	 * Returns a StringList of the object ids which are connected using Part Specification Relationship and objects revisions ids
	 * for a given context.
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args contains a packed HashMap containing objectId of object
	 * @return StringList.
     * @since EngineeringCentral X3
	 * @throws Exception if the operation fails.
	*/
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeOIDPartSpecificationConnectedItems(Context context, String args[])	throws Exception
	{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String  parentObjectId = (String) programMap.get("objectId");
        StringList result = new StringList();
        if (parentObjectId == null)
        {
        	return (result);
        }
	    DomainObject domObj = new DomainObject(parentObjectId);
		String strTypeCADDrawing = PropertyUtil.getSchemaProperty(context,"type_CADDrawing");
		String strTypeCADModel = PropertyUtil.getSchemaProperty(context,"type_CADModel");
		String strTypeDrawingPrint = PropertyUtil.getSchemaProperty(context,"type_DrawingPrint");
		String strTypePartSpecification = PropertyUtil.getSchemaProperty(context,"type_PartSpecification");
		String strTypeTechnicalSpecification = PropertyUtil.getSchemaProperty(context,"type_TechnicalSpecification");
		String strTypeViewable = PropertyUtil.getSchemaProperty(context,"type_Viewable");
		StringBuffer sbTypePattern = new StringBuffer(strTypeCADDrawing);
              sbTypePattern.append(',');
              sbTypePattern.append(strTypeCADModel);
              sbTypePattern.append(',');
              sbTypePattern.append(strTypeDrawingPrint);
			  sbTypePattern.append(',');
              sbTypePattern.append(strTypePartSpecification);
			  sbTypePattern.append(',');
              sbTypePattern.append(strTypeTechnicalSpecification);
			  sbTypePattern.append(',');
              sbTypePattern.append(strTypeViewable);
		String relToExpand = PropertyUtil.getSchemaProperty(context,"relationship_PartSpecification");
		StringList selectStmts  = new StringList(1);
        selectStmts.addElement(DomainConstants.SELECT_ID);
	    MapList mapList = domObj.getRelatedObjects(context,
													relToExpand,              // relationship pattern
													sbTypePattern.toString(), // object pattern
													selectStmts,              // object selects
													null,                     // relationship selects
													false,                    // to direction
													true,                     // from direction
													(short) 1,                // recursion level
													null,                     // object where clause
													null);                    // relationship where clause
            Iterator i1 = mapList.iterator();
            while (i1.hasNext())
            {
                Map m1 = (Map) i1.next();
				String strId = (String)m1.get(DomainConstants.SELECT_ID);
				DomainObject dObj = new DomainObject(strId);

				MapList revmapList = dObj.getRevisionsInfo(context, selectStmts, new StringList());
			    Iterator i2 = revmapList.iterator();
				while (i2.hasNext()){
				Map m2 = (Map) i2.next();
				String strIds = (String)m2.get(DomainConstants.SELECT_ID);
				result.addElement(strIds);
				}
            }
		return result;
    }

	/**
	 * This utility modifies the maplist mplOut recursively to add parts of all levels. Level to 1 for all items and add
	 * customLevel instead of Level
	 * @param mplIn - Input MapList
	 * @param mplOut - Map to populate result
	 * @return void
     * @since EngineeringCentral X3
	*/
	public void getFlattenedMapList(MapList mplIn, MapList mplOut) {
		 getFlattenedMapList(mplIn, mplOut, null);// to fix IR-018596V6R2011, IR-034237V6R2011
	}

	/**
	 * Added to fix IR-018596V6R2011, IR-034237V6R2011
	 * This utility modifies the maplist mplOut recursively to add parts of all levels. Level to 1 for all items and add
	 * customLevel instead of Level
	 * @param mplIn - Input MapList
	 * @param mplOut - Map to populate result
	 * @param reportType - report type
	 * @return void
     * @since EngineeringCentral X3
	*/
	public void getFlattenedMapList(MapList mplIn, MapList mplOut, String reportType) {
		Iterator itr1 = mplIn.iterator();
		String strActualLevel = null;
		while(itr1.hasNext()) {
			Map mp = (Map)itr1.next();
			strActualLevel = (String)mp.get("level");
			mp.put("customLevel",strActualLevel);

			if(reportType!=null && reportType.equalsIgnoreCase("Difference_Only_Report"))
			{
				mp.put("level",strActualLevel);

			}
			else
			{
				mp.put("level","1");
			}

			if(mp.containsKey("children")) {
				//remove the children key and add to the Map List, make level =1
				MapList mplTemp =(MapList)mp.remove("children");
				mplOut.add(mp);
				//recurse with the new Maplist
				if(mplTemp != null && mplTemp.size() >0)
					getFlattenedMapList(mplTemp,mplOut);
			} else {
				mplOut.add(mp);
			}
		}
		return;
	}

// This function will be called while deleting the Part.
    // If there are no part revisions connected to the Part Master, Part Master also will be deleted along with the part.
    // Otherwise Part Master will not be deleted.
     public void deleteRelatedPartMaster(matrix.db.Context context, String[] args)
        throws Exception
    {

      try
      {
    	  String relationshipAssociatedManufacturingPlans = PropertyUtil.getSchemaProperty(context, "relationship_AssociatedManufacturingPlans");
    	  String relationshipPlannedFor = PropertyUtil.getSchemaProperty(context, "relationship_PlannedFor");
    	  String typeManufacturingPlan = PropertyUtil.getSchemaProperty(context, "type_ManufacturingPlan");
    	  String relationshipGBOM = PropertyUtil.getSchemaProperty(context, "relationship_GBOM");
    	  String strPlanReqd = PropertyUtil.getSchemaProperty(context, "attribute_PlanningRequired");
    	  String strendItem = PropertyUtil.getSchemaProperty(context, "attribute_EndItem");

        // 373055
        ContextUtil.pushContext(context);
        if ( args == null || args.length == 0)
        {
           throw new IllegalArgumentException();
        }
        String objectId = args[0];
        DomainObject partObj = new DomainObject(objectId);

        String strPlanningReqd = partObj.getInfo(context,"attribute["+strPlanReqd+"]" );
        String strsendItem = partObj.getInfo(context, "attribute["+strendItem+"]");


        if(strPlanningReqd.equalsIgnoreCase("Yes") && strsendItem.equalsIgnoreCase("Yes"))
        {
        	StringList busSelects = new StringList();
            busSelects.addElement(DomainObject.SELECT_ID);
            busSelects.addElement(DomainObject.SELECT_NAME);

            MapList mpList = partObj.getRelatedObjects(context,
            		relationshipPlannedFor,
            		typeManufacturingPlan,
					busSelects, null,
					false, true, (short)1,
					null, null);
            if(mpList.size()>0 && mpList !=null)
            {
            	Map mpListMap = null;
                Iterator itr = mpList.iterator();
                if(itr.hasNext()) {

                    mpListMap = (Map) itr.next();
                    String mpId = (String) mpListMap.get(DomainObject.SELECT_ID);
                    String prodId = MqlUtil.mqlCommand(context,  "print bus $1 select $2 dump",objectId,"to["+ relationshipGBOM+"].from.id");
      			    String productRelId = MqlUtil.mqlCommand(context,  "print bus $1 select $2 dump",mpId,"to["+ relationshipAssociatedManufacturingPlans+"|from.id=='"+prodId+"'].id");
                    DomainRelationship.disconnect(context, productRelId);
            }

       }
       }

        partObj.openObject(context);
        Part part = new Part();
        String strPartMasterID = part.getPartMaster(context, objectId);
        MapList revisionList = partObj.getRevisionsInfo(context, new StringList(), new StringList());

        partObj.closeObject(context, true);
        if(strPartMasterID!= null && strPartMasterID.trim().length()>0 && !(revisionList.size() > 1)) {
            DomainObject doPartMaster = new DomainObject(strPartMasterID);
            doPartMaster.deleteObject(context);
        }
        // 373055 : if part has multiple revisions, disconnect Part Master when deleting a revision
        if (revisionList.size()>1 && strPartMasterID!=null) {
            String relid = partObj.getInfo(context, "relationship["+RELATIONSHIP_PART_REVISION+"].id");
            DomainRelationship.disconnect(context, relid);
        }
      }catch(Exception e){
        throw e;
      }
      finally {
          ContextUtil.popContext(context);
      }
  }


	/**
	 * 	This function is called while revising the Part.
	 * Newly revised part will also get connect with the Part Master.
	 *
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void connectPMNewRevPart(Context context, String args[]) throws Exception {
		String relationshipPartRevision = PropertyUtil.getSchemaProperty(context, "relationship_PartRevision");
		String strPartId = args[0];

		Part part = new Part();
		String strPartMasterID = part.getPartMaster(context, strPartId);
		DomainObject doPart = DomainObject.newInstance(context, strPartId);

		BusinessObject lastRevObj = doPart.getLastRevision(context);
		strPartId = lastRevObj.getObjectId(context);

		doPart.setId(strPartId);
		if (strPartMasterID != null && !"null".equalsIgnoreCase(strPartMasterID) && !"".equalsIgnoreCase(strPartMasterID)) {
			DomainObject doPartMaster = new DomainObject(strPartMasterID);
			ContextUtil.pushContext(context);
			DomainRelationship.connect(context, doPartMaster, relationshipPartRevision, doPart);
			ContextUtil.popContext(context);
		}

	}



	/**
	 * Method to get Part Mode field
	 * @param context - matrix context
	 * @param args - String args
	 * @return String
     * @since EngineeringCentral X4
	*/
     public String showPartModeField(Context context,String args[])throws Exception
	 {
		  String strPartMode = "";
		  try
		  {
			  HashMap programMap = (HashMap) JPO.unpackArgs(args);
			  HashMap paramMap = (HashMap)programMap.get("paramMap");
			  String  parentObjectId = (String) paramMap.get("objectId");
			  DomainObject doPart = new DomainObject(parentObjectId);
			  String sRelPartRevision = PropertyUtil.getSchemaProperty(context,"relationship_PartRevision");
			  String sAttributePartMode = PropertyUtil.getSchemaProperty(context,"attribute_PartMode");
			  String objectSelects = "to["+sRelPartRevision+"].from.attribute["+sAttributePartMode+"].value";
			  strPartMode = doPart.getInfo(context,objectSelects);
			  if(strPartMode !=null && !"null".equalsIgnoreCase(strPartMode) && !"".equalsIgnoreCase(strPartMode)){
				//Multitenant
				    strPartMode = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Part_Mode."+strPartMode);
			  }
			  else{
				 strPartMode="";
			  }

		  }
		  catch(Exception Ex)
		  {
			  Ex.printStackTrace();
		  }
		  return strPartMode;
     }

     /**
      * Method to check file is checked into Specification/Refference Document
      * @param context - matrix context
      * @param args - String args
      * @return int
      * @since R207
      * @author ZGQ
     */
     public int checkForFileInSpec(Context context, String args[])
         throws Exception{

         String objectId = args[0];

         StringList selectStmts  = new StringList(5);
         selectStmts.addElement(SELECT_ID);
         selectStmts.addElement(SELECT_FORMAT_HASFILE);
         selectStmts.addElement(SELECT_TYPE);
         selectStmts.add("vcfile");
         selectStmts.add("vcfolder");
         selectStmts.addElement(CommonDocument.SELECT_VCFILE_EXISTS);
         selectStmts.addElement(CommonDocument.SELECT_VCFOLDER_EXISTS);
         selectStmts.addElement("from[" + CommonDocument.RELATIONSHIP_ACTIVE_VERSION + "]");

         String noCheckRequiredTypes = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.PartSpecification.NoFileRequiredTypes");

         DomainObject dObj = DomainObject.newInstance(context, objectId);
         Map map = dObj.getInfo(context, selectStmts);
         MapList mapList = new MapList();
         mapList.add(map);

         if (!isFileCheckedIntoSpec(context, mapList, noCheckRequiredTypes))
         {
           String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckIfSpecificationHasFiles.Message",context.getSession().getLanguage());
           emxContextUtil_mxJPO.mqlNotice(context,strMessage);
             return 1;
         }
         return 0;

     }
	/**
	 * createAndConnectPartMaster method checks for the Part Master object for
	 * the current Part. If not available, Part Master will be created. This
	 * method is invoked on creation of Part
	 *
	 * @param context
	 *            Context : User's Context.
	 * @param args
	 *            String array having the object Id of the part.
	 * @throws FrameworkException
	 *             if creation of the Part Master object fails.
	 *
	 * 364886 :added a trigger method to create a part master when part is created
	 * IR-061415V6R2011x - modified
	 *
	 */

	public void createAndConnectPartMaster(Context context, String[] args)
			throws Exception {

		String strPartId = args[0];
		String action = args[1];

		if ("copy".equals(action)) {
			String strType  = args[2];
			String strName  = args[3];
			String strRev   = args[4];
			String strVault = args[5];

			BusinessObject busObj = new BusinessObject(strType, strName, strRev, strVault);
			strPartId = busObj.getObjectId(context);
		}

        createAndConnectPartMaster(context, strPartId);

		// MFG HF66-022673V6R2009x - Start
        MqlUtil.mqlCommand(context,  "set env global $1 $2","MX_ECPART_CREATE_ACTION","TRUE");
		// MFG HF66-022673V6R2009x - End
	}

    /**
     * @param context
     * @param doPart
     * @param policyClass
     * @throws FrameworkException
     * @throws MatrixException
     */
	public String createAndConnectPartMaster(Context context, String strPartId) throws FrameworkException, MatrixException {

	    String strPartMasterId = "";
	    boolean contextPushed=false;

	    DomainObject doPart = DomainObject.newInstance(context);
	    doPart.setId(strPartId);

	    try {

	        doPart.open(context);

	        // Par Master creation and connection should happen only for
	        // Production, Development and Unresolved Parts.

	        String policyClass = doPart.getInfo(context, "policy.property[PolicyClassification].value");

	        if ("Production".equalsIgnoreCase(policyClass) || "Development".equalsIgnoreCase(policyClass) || "Unresolved".equalsIgnoreCase(policyClass)) {
	            String strPartName = doPart.getInfo(context, DomainObject.SELECT_NAME);

	            String checkPartVersion= EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Check.PartVersion");
	            if(checkPartVersion.equalsIgnoreCase("TRUE"))
	            {
	                String sSplChar = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.PartVersion.SpecialChar");
	                if(strPartName.indexOf(sSplChar)!=-1) {
	                    String sPartNametemp = strPartName.substring(0, strPartName.lastIndexOf(sSplChar));
	                    String sPartRevTemp = strPartName.substring(strPartName.lastIndexOf(sSplChar)+1, strPartName.length());
	                    BusinessObject boPartTemp = new BusinessObject(EngineeringConstants.TYPE_PART, sPartNametemp,
	                            sPartRevTemp, doPart.getVault());
	                    if(boPartTemp.exists(context)) {
	                        strPartName = sPartNametemp;
	                    }
	                }
	            }


	            DomainObject doPartMaster = null;
	            BusinessObject boPartMaster = null;

	            String typePartMaster = PropertyUtil.getSchemaProperty(context, "type_PartMaster");
	            String policyPartMaster = PropertyUtil.getSchemaProperty(context,"policy_PartMaster");
	            String relationshipPartRevision = PropertyUtil.getSchemaProperty(context, "relationship_PartRevision");

	            boPartMaster = new BusinessObject(typePartMaster, strPartName,
	                    doPart.getInfo(context, DomainObject.SELECT_TYPE), doPart.getVault());
	            if(!boPartMaster.exists(context)) {
	                boPartMaster.create(context, policyPartMaster);
	            }
	            boPartMaster.open(context);
	            strPartMasterId = boPartMaster.getObjectId();
	            boPartMaster.close(context);

	            doPartMaster = DomainObject.newInstance(context);
	            doPartMaster.setId(strPartMasterId);

	            DomainRelationship.connect(context, doPartMaster,
	                    relationshipPartRevision, doPart);
	            if ("Unresolved".equalsIgnoreCase(policyClass)) {
		            ContextUtil.pushContext(context);
		            contextPushed=true;
		            doPartMaster.setAttributeValue(context, PropertyUtil.getSchemaProperty(context,"attribute_PartMode"), policyClass);
	            }
	        }
	    } catch (FrameworkException e) {
	        e.printStackTrace();
	        throw (e);
	    } catch (MatrixException e) {
	        e.printStackTrace();
	        throw (e);
	    } finally {
	        doPart.close(context);
	        if (contextPushed)
	        	ContextUtil.popContext(context);
	    }
	    return strPartMasterId;
	}

	// end: 364886

     /**
      * Returns true if the LibraryCentral is installed otherwise false.
      * @mx.whereUsed This method will be called from part property pages
      * @mx.summary   This method check whether LibraryCentral is installed or not, this method can be used as access program to show/hide the ClassificationAttributes
      * @param context the eMatrix <code>Context</code> object.
      * @return boolean true or false based condition.
      * @throws Exception if the operation fails.
      * @since R207
      */
     public boolean isLBCInstalled(Context context,String[] args) throws Exception
     {
		return FrameworkUtil.isSuiteRegistered(context,"appVersionLibraryCentral",false,null,null);
     }

     /**
     * @param dataMap contains data like type, name, revision,...
     * @param key String which contains the key.
     * @return value from the map of that particular key.
     */
    private String getValue(HashMap dataMap, String key) {
         String value = getStringValue(dataMap, key);
         return (value == null) ? "" : value.trim();
     }

	/** Iterates through MapList and gets the Map whose current state is Released.
	 * @param list contains datas retrived from the database.
	 * @return MapList which contains only one map with released part information.
	 */
	private MapList getReleasedList(MapList list) {
		Map mapTemp = null;
		MapList listReturn = new MapList(); //Modified for IR-150912
		String strCurrent;
		for (int i = 0, size = list.size(); i < size; i++) {
			mapTemp = (Map) list.get(i);
			strCurrent = getStringValue(mapTemp, DomainObject.SELECT_CURRENT);
			if (DomainConstants.STATE_PART_RELEASE.equals(strCurrent)) {
				listReturn.add(mapTemp); //Modified for IR-150912
				//break;
			}
		}
		return listReturn;
	}

     //R208.HF1 - Starts
   /**
      * lookupEntries method checks the object entered manually is exists or not in the database
      * This method is invoked on clicking on Lookup button in EBOM
      * @param args String array having the object Id(s) of the part.
      * @throws FrameworkException if creation of the Part Master object fails.
      */
	 @com.matrixone.apps.framework.ui.LookUpCallable
     public MapList lookupEntries(Context context, String[] args) throws Exception
     {
    	HashMap inputMap   = (HashMap) JPO.unpackArgs(args);
    	HashMap requestMap = (HashMap) inputMap.get("requestMap");
    	HashMap curObjectMap;
        HashMap itemMap;

        MapList objectMapList  = (MapList) inputMap.get("objectList");
        MapList returnList = new MapList();
        MapList resultsList;

        String languageStr = (String) requestMap.get("languageStr");
        String multipleMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.MultipleError.Message", languageStr);
        String noMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.NoObject.Message", languageStr);
        String latestReleased = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.WhereUsedRevisionLatestReleased", languageStr);
        String latest = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EBOMManualAddExisting.RevisionOption.Latest", languageStr);

        //Added for IR-153213
        String LATEST_RELEASED = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.WhereUsedRevisionLatestReleased", "en");
        String LATEST = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EBOMManualAddExisting.RevisionOption.Latest", "en");

        String strLatestRelesed;
        String fromConfigBOM = "false";

        //2012x
        boolean isECCInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionEngineeringConfigurationCentral",false,null,null);
        fromConfigBOM  		   = (String) requestMap.get("fromConfigBOM");
        //2012x

        StringBuffer sbObjectWhere;

        StringList objectSelect = createStringList(new String[] {
				DomainConstants.SELECT_ID, DomainConstants.SELECT_CURRENT ,DomainConstants.SELECT_POLICY });

        Iterator objectItr = objectMapList.iterator();
        String sparePart =  PropertyUtil.getSchemaProperty(context, "attribute_SparePart");

        while (objectItr.hasNext()) {
            curObjectMap = (HashMap) objectItr.next();

            String objectName = getValue(curObjectMap, "Name");
            String objectType = getValue(curObjectMap, "Type");
            String objectRev  = getValue(curObjectMap, "Revision");
            String Policy     = getValue(curObjectMap, "Policy");

            strLatestRelesed  = objectRev;

            sbObjectWhere = new StringBuffer(128);
            sbObjectWhere.append("(policy == '").append(DomainConstants.POLICY_EC_PART).append("' || policy == '");
            String policy_Configured = "";
            if (isECCInstalled && "true".equalsIgnoreCase(fromConfigBOM)) {
            	 policy_Configured =  PropertyUtil.getSchemaProperty(context, "policy_ConfiguredPart");
            	 sbObjectWhere  = sbObjectWhere.append(policy_Configured).append("' || policy == '");
            }
            //sbObjectWhere = sbObjectWhere.append(DomainConstants.POLICY_DEVELOPMENT_PART).append("')");
            sbObjectWhere = sbObjectWhere.append("')");

            if (objectRev.equals(latestReleased) || objectRev.equalsIgnoreCase(LATEST_RELEASED)) { //Modified for IR-153213
               //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            	sbObjectWhere.append(" && ((current == '"+ DomainConstants.STATE_PART_RELEASE +"' && (revision == 'last' || (next.current != '" + DomainConstants.STATE_PART_RELEASE +"' && next.current != '" + DomainConstants.STATE_PART_OBSOLETE + "'))) || (revision == 'last' && current != '" + DomainConstants.STATE_PART_OBSOLETE + "'))");
            } else if (objectRev.equals(latest) || objectRev.equalsIgnoreCase(LATEST)) { //Modified for IR-153213
                //Modified for IR-048513V6R2012x, IR-118107V6R2012x
            	sbObjectWhere.append(" && (revision == 'last' && current != '" + DomainConstants.STATE_PART_OBSOLETE +"')");
            }

            itemMap = new HashMap();

            if ("".equals(objectRev) || objectRev.equals(latestReleased) || objectRev.equals(latest)
            		|| objectRev.equalsIgnoreCase(LATEST_RELEASED) || objectRev.equalsIgnoreCase(LATEST)) { //Modified for IR-153213
                objectRev = DomainConstants.QUERY_WILDCARD;
            }

            if (isECCInstalled && "true".equalsIgnoreCase(fromConfigBOM) && "policy_ConfiguredPart".equalsIgnoreCase(Policy)) {
            	String STATESUPERSEDED= PropertyUtil.getSchemaProperty(context,"policy",policy_Configured, "state_Superseded");
            	sbObjectWhere.append(" && (revision == 'last' && current !=" +STATESUPERSEDED+ ")");
            	objectRev = DomainConstants.QUERY_WILDCARD;
            }
            sbObjectWhere.append("&&(attribute["+sparePart+"] == No)");
            resultsList = DomainObject.findObjects(
                                        context,                            // eMatrix context
                                        objectType,                         // type pattern
                                        objectName,         				// name pattern
                                        objectRev,     						// revision pattern
                                        DomainConstants.QUERY_WILDCARD,     // owner pattern
                                        DomainConstants.QUERY_WILDCARD,     // vault pattern
                                        sbObjectWhere.toString(),        	// where expression
                                        true,								// Expand Type
                                        objectSelect);     					// object selects

            if (strLatestRelesed.equals(latestReleased)
            		|| strLatestRelesed.equalsIgnoreCase(LATEST_RELEASED)) { //Modified for IR-153213
            	// If we have 2 revisions and latest Part is not released then
				// we will get 2 objects from the above query. we have to select
				// the object which is Released so that it will be latest Released.
            	if (resultsList != null && resultsList.size() > 0) {
            		resultsList = getReleasedList(resultsList);
            	}
            }

            if (resultsList != null && resultsList.size() == 1) {
                itemMap.put("id", ((Map) resultsList.get(0)).get(DomainConstants.SELECT_ID));
            } else if (resultsList != null && resultsList.size() > 0) {
                itemMap.put("Error", multipleMessage);
            } else {
                itemMap.put("Error", noMessage);
            }

            returnList.add(itemMap);
        }

	    return returnList;
	 }

     private HashMap getAttributes(HashMap map, String[] keys) throws Exception {
    	 int length = length (keys);
    	 HashMap mapReturn = new HashMap(length);
    	 String data;
    	 for (int i = 0; i < length; i++) {
    		 data = getStringValue(map, keys[i]);
    		 if (isValidData(data)) {
    			 mapReturn.put(keys[i], data);
    		 }
    	 }
    	 return mapReturn;
     }

     private StringList getValueListFromProperties(Context context,String[] objSizeArr, String resource, String languageStr) throws Exception {
    	 int length = length (objSizeArr);
    	 StringList list = new StringList(length);
    	 String temp;
    	 for (int i = 0; i < length; i++) {
    		 temp= EnoviaResourceBundle.getProperty(context,resource,new Locale(languageStr),objSizeArr[i]);
    		 list.add(temp);
    	 }
    	 return list;
     }


 /**
      * lookupEntries method checks the object entered manually is exists or not in the database
	  * Method to Inline create & connect new Part objects in EBOM powerview IndentedTable
      * This method is invoked on clicking on Apply button in EBOM
       * @param args String array having the object Id(s) of the part.
      * @throws FrameworkException if creation of the Part Master object fails.
      */

     @com.matrixone.apps.framework.ui.ConnectionProgramCallable
     public  HashMap inlineCreateAndConnectPart(Context context, String[] args) throws Exception{

		HashMap doc = new HashMap();
		HashMap request = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) request.get("paramMap");
		HashMap hmRelAttributesMap;
		HashMap columnsMap;
        HashMap changedRowMap;
        HashMap returnMap;
        String sType = (String)paramMap.get("type");
        String sSymbolicName = com.matrixone.apps.framework.ui.UICache.getSymbolicName(context, sType, "type");

		Map smbAttribMap;

		Element elm = (Element) request.get("contextData");

		MapList chgRowsMapList = UITableIndented.getChangedRowsMapFromElement(context, elm);
		MapList mlItems = new MapList();

 		String strRelType = (String) paramMap.get("relType");
 		String parentObjectId = (String) request.get("parentOID");
 		String rowFormat = "";
 		String CONNECT_AS_DERIVED = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReplaceBOM.Derived");
        String vpmControlState = null;
        String sUser = context.getUser();String objectName = "";
        String vName = "";
        String strComponentLocation = "";
        String[] attributeKeys = { DomainConstants.ATTRIBUTE_FIND_NUMBER,
				DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR,
				DomainConstants.ATTRIBUTE_COMPONENT_LOCATION,
				DomainConstants.ATTRIBUTE_QUANTITY,
				DomainConstants.ATTRIBUTE_USAGE,
				DomainConstants.ATTRIBUTE_NOTES };


        StringList sResultList = new StringList();
        String tokValue = "";
        String tok = "";
        String sResult ="";
        String parentBusType = "";
        BusinessType busType = null;
        if(UIUtil.isNotNullAndNotEmpty(sSymbolicName)){
        sResult = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump","eService Object Generator",sSymbolicName,"*","revision");
        while(UIUtil.isNullOrEmpty(sResult)) { 
       	 busType = new BusinessType(sType, context.getVault());
       	 if (busType != null){
       		 parentBusType = busType.getParent(context);
       		 if (UIUtil.isNotNullAndNotEmpty(parentBusType))
       		 {
       			 sType = parentBusType;
       			 sSymbolicName = com.matrixone.apps.framework.ui.UICache.getSymbolicName(context, sType, "type");
       			 sResult = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump","eService Object Generator",sSymbolicName,"*","revision");
       			 if(UIUtil.isNotNullAndNotEmpty(sResult))
       			 {
       				break;
       			 }
       		 }
       	 }
        }
        }
        StringTokenizer stateTok = new StringTokenizer(sResult, "\n");
        while (stateTok.hasMoreTokens())
        	{
       	 	tok = (String)stateTok.nextToken();
       	 	tokValue = tok.substring(tok.lastIndexOf(',')+1);
       	 	sResultList.add(tokValue);
        	}
        int sResultListSize = sResultList.size();
        StringList objAutoNameList = new StringList();
        
        for(int i=0; i<sResultListSize; i++){
        	objAutoNameList.add(UINavigatorUtil.getI18nString("emxEngineeringCentral.Common."+((String)sResultList.get(i)).replace(" ", ""), "emxEngineeringCentralStringResource", "en"));
        }

        boolean isENGSMBInstalled = EngineeringUtil.isENGSMBInstalled(context, false); //Commented for IR-213006

        if (isENGSMBInstalled) { //Commented for IR-213006
        	String mqlQuery = new StringBuffer(100).append("print bus $1 select $2 dump").toString();
        	vpmControlState = MqlUtil.mqlCommand(context, mqlQuery,parentObjectId,"from["+RELATIONSHIP_PART_SPECIFICATION+"|to.type.kindof["+EngineeringConstants.TYPE_VPLM_CORE_REF+"]].to.attribute["+EngineeringConstants.ATTRIBUTE_VPM_CONTROLLED+"]");
		}
        EBOMAutoSync.backupContextUserInfo(context);
        ContextUtil.pushContext(context);

		try
		{
			DomainObject parentObj = DomainObject.newInstance(context, parentObjectId);
			DomainObject childObj;

 			DomainRelationship domRelation;

 			EBOMMarkup ebomMarkup = new EBOMMarkup();

 			for (int i = 0, size = chgRowsMapList.size(); i < size; i++) {
 				try {
 					changedRowMap = (HashMap) chgRowsMapList.get(i);

 					String childObjectId = (String) changedRowMap.get("childObjectId");
 					String sRelId = (String) changedRowMap.get("relId");
 					String sRowId = (String) changedRowMap.get("rowId");
 					rowFormat = "[rowId:" + sRowId + "]";
 					String markup = (String) changedRowMap.get("markup");
 					String strParam2 = (String) changedRowMap.get("param2");
 					// get parameters for replace operation
 					String strParam1 = (String) changedRowMap.get("param1");

					columnsMap = (HashMap) changedRowMap.get("columns");

					String strUOM = (String) columnsMap.get("UOM");
					String desc = (String) columnsMap.get("Description");
					String strUOMType = (String)columnsMap.get("UOMType");

					String sChangeControlled = (String) columnsMap.get("ChangeControlled");
					String sReleaseProcess = (String) columnsMap.get("ReleaseProcess");

					hmRelAttributesMap = getAttributes(columnsMap, attributeKeys);
					strComponentLocation = getStringValue(columnsMap, DomainConstants.ATTRIBUTE_COMPONENT_LOCATION);
					if(UIUtil.isNotNullAndNotEmpty(strComponentLocation)) {
						columnsMap.put(DomainConstants.ATTRIBUTE_COMPONENT_LOCATION, StringUtils.replace(strComponentLocation,"&", "&amp;"));
					}
					String vpmVisible = "";
					// TBE
            		 if (isENGSMBInstalled) { //Commented for IR-213006
    					vpmVisible = getStringValue(columnsMap, "VPMVisible");
    					 //If part is not in VPM Control set the isVPMVisible value according to user selection.
						if (isValidData(vpmVisible) && !"true".equalsIgnoreCase(vpmControlState))
    						hmRelAttributesMap.put(EngineeringConstants.ATTRIBUTE_VPM_VISIBLE, vpmVisible);
            		}
            		// TBE

            		changedRowMap.put("parentObj", parentObj);

 					if (MARKUP_ADD.equals(markup)) {
 						childObj = DomainObject.newInstance(context, childObjectId);

                        changedRowMap.put("childObj", childObj);
                        changedRowMap.put("strRelType", strRelType);

                        domRelation = ebomMarkup.connectToChildPart(context, changedRowMap);

						if (isValidData(desc)) {
							childObj.setDescription(context, desc);
						}

						domRelation.setAttributeValues(context, hmRelAttributesMap);

 						if ("true".equalsIgnoreCase(CONNECT_AS_DERIVED) && isValidData(strParam2)) {
 							StringList slParamObjs = childObj.getInfoList(context, SELECT_FROM_DERIVED_IDS);

 							if (slParamObjs == null || !slParamObjs.contains(strParam2)) {
 								DomainRelationship doRelDerived = DomainRelationship.connect(context, new DomainObject(strParam2), RELATIONSHIP_DERIVED, childObj);
 								doRelDerived.setAttributeValue(context, ATTRIBUTE_DERIVED_CONTEXT, "Replace");
 							}
 						}
 						
 						//UOM Management : Set UOM attribute value
 						if(!UIUtil.isNullOrEmpty(strUOM))
 						{
 							domRelation.setAttributeValue(context, ATTRIBUTE_UNIT_OF_MEASURE, strUOM);
 						}

						sRelId = domRelation.toString();
					}


 					else if (MARKUP_NEW.equals(markup)) {

						objectName = (String) columnsMap.get("Name");
						String objectType = (String) columnsMap.get("Type");
						String objectRev = (String) columnsMap.get("Revision");
						String objectPolicy = (String) columnsMap.get("Policy");
						String objectVault = (String) columnsMap.get("Vault");
						String objectPartFamily = (String) columnsMap.get("Part Family");

						smbAttribMap = new HashMap();
						smbAttribMap.put(DomainConstants.ATTRIBUTE_UNIT_OF_MEASURE, strUOM);
						smbAttribMap.put(DomainConstants.ATTRIBUTE_ORIGINATOR, sUser);
						if(UIUtil.isNotNullAndNotEmpty(sReleaseProcess)){
							smbAttribMap.put(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE, sReleaseProcess);
						}
						if(UIUtil.isNotNullAndNotEmpty(sChangeControlled)){
							smbAttribMap.put(EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED, sChangeControlled);
						}
						
						if(UIUtil.isNotNullAndNotEmpty(strUOMType)){
							smbAttribMap.put(EngineeringConstants.ATTRIBUTE_UOM_TYPE, strUOMType);
						}
						// TBE
						if (isENGSMBInstalled) { //Commented for IR-213006
        					vName = getStringValue(columnsMap, "V_Name");
        					if(UIUtil.isNullOrEmpty(vName))
        					{
        						vName = (String) columnsMap.get("V_Name1");
        					}
        					if (isValidData(vName)){
        						smbAttribMap.put(EngineeringConstants.ATTRIBUTE_V_NAME, vName);
        					}
        					if (isValidData(vpmVisible) && !"true".equalsIgnoreCase(vpmControlState))
        						smbAttribMap.put(EngineeringConstants.ATTRIBUTE_VPM_VISIBLE, "TRUE");
        				}
        			    // TBE

						// Use Part Family for naming the Part - Start

						if (isValidData(objectPartFamily)) {
							PartFamily partFamilyObject = null;
							try {
						        partFamilyObject = new PartFamily(objectPartFamily);
						        partFamilyObject.open(context);

						        if (partFamilyObject.exists(context)) {
						            // check the "Part Family Name Generator On" attribute
									String usePartFamilyForName = partFamilyObject.getAttributeValue(context, ATTRIBUTE_PART_FAMILY_NAME_GENERATOR_ON);

						            if ("TRUE".equalsIgnoreCase(usePartFamilyForName)) {
						                objectName = partFamilyObject.getPartFamilyMemberName(context);
						            }
						        }
						    } finally {
						        if (partFamilyObject != null) {
						            partFamilyObject.close(context);
						        }
						    }
						}

						// Use Part Family for naming the Part - End

						childObj = DomainObject.newInstance(context);
						childObj = createchildObj(context, objectType, objectName, objectRev, objectPolicy, objectVault, childObj, objAutoNameList.contains(objectName));

						// parts created with inline had owner - user agent. removing if condition to change owner.
						childObj.setOwner(context, sUser);

						if(UIUtil.isNullOrEmpty(vName)){
							vName=childObj.getName();
							smbAttribMap.put(EngineeringConstants.ATTRIBUTE_V_NAME, vName);
						}
						
						if (isValidData(desc)) {
							childObj.setDescription(context, desc);
						}
						childObj.setAttributeValues(context, smbAttribMap);

                        changedRowMap.put("childObj", childObj);
                        changedRowMap.put("strRelType", strRelType);

                        domRelation = ebomMarkup.connectToChildPart(context, changedRowMap);
                      //UOM Management : Set UOM attribute value
 						if(!UIUtil.isNullOrEmpty(strUOM))
 						{
 							hmRelAttributesMap.put(DomainConstants.ATTRIBUTE_UNIT_OF_MEASURE, strUOM);
 						}
 						domRelation.setAttributeValues(context, hmRelAttributesMap);

						/* Connecting Part Family with Part starts */
						if (isValidData(objectPartFamily)) {
							DomainObject PartFamilyObj = DomainObject.newInstance(context, objectPartFamily);
							DomainRelationship.connect(context, PartFamilyObj, DomainConstants.RELATIONSHIP_CLASSIFIED_ITEM, childObj);
						}
						/* Connecting Part Family with Part ends */

						//Added RDO Convergence start
						String strDefaultRDO = childObj.getAltOwner1(context).toString();
						String defaultRDOId = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump $5",DomainConstants.TYPE_ORGANIZATION,strDefaultRDO,"*","id","|");
			            defaultRDOId = defaultRDOId.substring(defaultRDOId.lastIndexOf('|')+1);
			            DomainRelationship.connect(context,
								new DomainObject(defaultRDOId), // from side object Design Responsibilty
								DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY, // Relationship
								childObj);// toSide object Document

			          //Added RDO Convergence End

						childObjectId = childObj.getId();
						sRelId = domRelation.toString();

					}


 					else if (MARKUP_CUT.equals(markup)) {
 						if (!"replace".equals(strParam1)) {
 							ebomMarkup.disconnectChildPart(context, changedRowMap);
 						}
 					}

					returnMap = new HashMap();
					returnMap.put("pid", parentObjectId);
					returnMap.put("relid", sRelId);
					returnMap.put("oid", childObjectId);
					returnMap.put("rowId", sRowId);
					returnMap.put("markup", markup);
					objectName = (String)columnsMap.get("Name");
					if(objectName != null && !"null".equals(objectName) && !"".equals(objectName)) {
						columnsMap.put("Name", StringUtils.replace(objectName,"&", "&amp;"));
					}
					if(isENGSMBInstalled) { //Commented for IR-213006
						vName = getStringValue(columnsMap, "V_Name");
						if(UIUtil.isNullOrEmpty(vName))
        					{
        						vName = (String) columnsMap.get("V_Name1");
        					}
						if(vName != null){
							columnsMap.put("V_Name", StringUtils.replace(vName,"&", "&amp;"));
							columnsMap.put("V_Name1", StringUtils.replace(vName,"&", "&amp;"));
							}
					}

					returnMap.put("columns", columnsMap);

					mlItems.add(returnMap); // returnMap having all the

 				} catch (Exception e) {
 					if (e.toString().indexOf("license") > -1) {
 						throw e;
 					}
 					throw new Exception(rowFormat + e);
 				}

 			}
 			doc.put("Action", "success"); // Here the action can be "Success" or
 			// "refresh"
 			doc.put("changedRows", mlItems);// Adding the key "ChangedRows"
 		} catch (Exception e) {
 			doc.put("Action", "ERROR"); // If any exception is there send "Action" as "ERROR"

 			if (e.toString().indexOf("license") > -1) { // If any License Issue throw the exception to user.
 				doc.put("Message", rowFormat);
 				throw e;
 			}

 			if ((e.toString().indexOf("recursion")) != -1) {
 				//Multitenant
 				String recursionMesssage = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.RecursionError.Message");
 				doc.put("Message", rowFormat + recursionMesssage);
 			}
				//Multitenant
 			else if ((e.toString().indexOf("recursion")) == -1 && ((e.toString().indexOf("Check trigger")) != -1)) {

 				String tnrMesssage = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.TNRError.Message");
 				doc.put("Message", rowFormat + tnrMesssage);
 			}

 			else {
 				String strExcpn = e.toString();
 				int j = strExcpn.indexOf(']');
 			    strExcpn = strExcpn.substring(j + 1, strExcpn.length());
 				doc.put("Message", rowFormat + strExcpn);
 			}

		} finally {
			ContextUtil.popContext(context);
			context.removeFromCustomData(EBOMAutoSync.LOGGEDIN_USER);
		}

		return doc;

	}

      public DomainObject createchildObj(Context context,
                                            String objectType,
                                            String objectName,
                                            String objectRev,
                                            String objectPolicy,
                                            String objectVault,
                                            DomainObject childObj,
                                            boolean useAutoName) throws FrameworkException, MatrixException, Exception
     {

    	 boolean policyFlag = true;
         //IR-033556 - Starts
         com.matrixone.apps.engineering.Part part = new com.matrixone.apps.engineering.Part();
         String childId =   "";
         //IR-033556 - Ends
    	 if(objectPolicy.startsWith("policy_"))
         {
       	  policyFlag = true ;
         }
         else
       	  policyFlag = false ;
           //create a new object given information
       if(policyFlag)
           {
           //IR-033556 - Starts
            childId=part.createPart(context, objectType, objectName, objectRev, PropertyUtil.getSchemaProperty(context, objectPolicy), objectVault, useAutoName);
            //IR-033556 - Ends
           }
           else
           {
              //IR-033556 - Starts
              childId=part.createPart(context, objectType, objectName, objectRev, objectPolicy, objectVault, useAutoName);
              //IR-033556 - Starts
           }
            //IR-033556 - Starts
            if(childId != null && childId.length()>0)
                childObj.setId(childId);
            //IR-033556 - Ends
    return childObj;

    }

	/* Range function for Autoname series in Name column for Listbox+Manual input control */
     public HashMap getAutoNameSeries (Context context, String[] args) throws Exception
	{
     HashMap paramMap = (HashMap)JPO.unpackArgs(args);
     HashMap requestMap = (HashMap)paramMap.get("requestMap");
     HashMap rangeMap = new HashMap();
     String sType = (String)requestMap.get("type");
     String sSymbolicName = com.matrixone.apps.framework.ui.UICache.getSymbolicName(context, sType, "type");

     StringList columnVals = new StringList();
     StringList columnVals_Choices = new StringList();
     
     StringList sResultList = new StringList();
     String tokValue = "";
     String tok = "";
     String sResult = "";
     String parentBusType = "";
     BusinessType busType = null;
     if(UIUtil.isNotNullAndNotEmpty(sSymbolicName)){
     sResult = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump","eService Object Generator",sSymbolicName,"*","revision");
     while(UIUtil.isNullOrEmpty(sResult)) { 
    	 busType = new BusinessType(sType, context.getVault());
    	 if (busType != null){
    		 parentBusType = busType.getParent(context);
    		 if (UIUtil.isNotNullAndNotEmpty(parentBusType))
    		 {
    			 sType = parentBusType;
    			 sSymbolicName = com.matrixone.apps.framework.ui.UICache.getSymbolicName(context, sType, "type");
    			 sResult = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump","eService Object Generator",sSymbolicName,"*","revision");
    			 if(UIUtil.isNotNullAndNotEmpty(sResult))
    			 {
    				break;
    			 }
    		 }
    	 }
     }
     }
     StringTokenizer stateTok = new StringTokenizer(sResult, "\n");
     while (stateTok.hasMoreTokens())
     	{
    	 	tok = (String)stateTok.nextToken();
    	 	tokValue = tok.substring(tok.lastIndexOf(',')+1);
    	 	sResultList.add(tokValue);
     	}
     
     int size = sResultList.size();
     for(int i=0; i<size; i++){
    	 columnVals.add(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common."+((String)sResultList.get(i)).replace(" ", "")));
    	 columnVals_Choices.add(EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", new Locale("en"),"emxEngineeringCentral.Common."+((String)sResultList.get(i)).replace(" ", "")));
     }

     rangeMap.put("field_choices",columnVals_Choices);
     rangeMap.put("field_display_choices", columnVals );

     return rangeMap;
}

 /* Range function to display policies in dropdown box */
     public HashMap getPolicies (Context context, String[] args)
     throws Exception
 {

	 HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	 HashMap requestMap = (HashMap)paramMap.get("requestMap");
	 String parentid = (String)requestMap.get("objectId");

	 String parentType = new DomainObject(parentid).getInfo(context, DomainConstants.SELECT_TYPE);
	 BusinessType partBusType = new BusinessType(parentType, context.getVault());
	 PolicyList partPolicyList = partBusType.getPoliciesForPerson(context,false);
	 PolicyItr  partPolicyItr  = new PolicyItr(partPolicyList);
	 boolean isMBOMInstalled = EngineeringUtil.isMBOMInstalled(context);
	 String POLICY_STANDARD_PART = PropertyUtil.getSchemaProperty(context,"policy_StandardPart");
	 Policy partPolicy = null;
	 String policyName = "";
	 String policyAdminName = "";
	 String policyClassification = "";

	 HashMap rangeMap = new HashMap();
	 StringList columnVals = new StringList();
	 StringList columnVals_Choices = new StringList();

	 while(partPolicyItr.next())
	 {
		partPolicy = partPolicyItr.obj();
		policyName = partPolicy.getName();
		policyClassification = EngineeringUtil.getPolicyClassification(context, policyName);


		// Modified for TBE Packaging & Scalability
		if (!EngineeringUtil.isENGInstalled(context, args)&&
				!EngineeringUtil.getPolicyClassification(context,policyName).equals("Development" ))
		{
				continue;
		}

        //if(isMBOMInstalled)
        //{
        	if(policyName.equals(POLICY_STANDARD_PART))
        	{
        		continue;
        	}
        //}
		if("Unresolved".equals(policyClassification) ||
			   "Equivalent".equals(policyClassification) || "Manufacturing".equals(policyClassification))
		{
			continue;
		}
		policyAdminName = FrameworkUtil.getAliasForAdmin(context, "Policy", policyName, true);

		String tempPolicyName = FrameworkUtil.findAndReplace(policyName.trim()," ", "_");

		columnVals.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Policy."+tempPolicyName));

		columnVals_Choices.add(policyAdminName);
	 }
	 rangeMap.put("field_choices",columnVals_Choices);
	 rangeMap.put("field_display_choices", columnVals );

	 return rangeMap;
}


   /* This method is used to diaplay range values(latest,latest released) for revision column*/
    public HashMap getRevisions(Context context, String[] args) throws Exception
	{
        //IR-040860 - Starts
        HashMap map = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)map.get("paramMap");
        String languageStr  = (String) paramMap.get("languageStr");
        //IR-040860 - Ends
    	HashMap revMap = new HashMap();
        //IR-040860 - Starts
        String latestReleased=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.WhereUsedRevisionLatestReleased",languageStr);
        String latest=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EBOMManualAddExisting.RevisionOption.Latest",languageStr);
        StringList fieldRangeValues = new StringList(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.WhereUsedRevisionLatestReleased","en"));
        StringList fieldDisplayRangeValues = new StringList(latestReleased);//Hardcode
        fieldRangeValues.add(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EBOMManualAddExisting.RevisionOption.Latest","en"));
        //IR-040860 - Ends
		fieldDisplayRangeValues.add(latest);
		revMap.put("field_choices", fieldRangeValues);
		revMap.put("field_display_choices", fieldDisplayRangeValues);

		return revMap;
	}


 /* This method is used to diaplay default Part value*/
 public HashMap getDefaultPartValue(Context context, String[] args) throws Exception
  {
	  HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	  HashMap requestMap = (HashMap)paramMap.get("requestMap");
	  String languageStr = (String) requestMap.get("languageStr");

	  HashMap defaultMap = new HashMap();
	  String defaultVal= PropertyUtil.getSchemaProperty(context,"type_Part");
	  String strType = com.matrixone.apps.framework.ui.UINavigatorUtil.getAdminI18NString("Type", defaultVal, languageStr);
	  defaultMap.put("Default_AddNewRow",defaultVal);
      //IR-034174 - Starts
      defaultMap.put("Default_ExistingRow",defaultVal);

      defaultMap.put("Default_AddNewRow_Display",strType);
      defaultMap.put("Default_ExistingRow_Display",strType);
      //IR-034174 - Ends
	  return defaultMap;
	}
    //R208.HF1 - Ends

    //Updated for IR-030554
   public HashMap getDefaultRevision (Context context,String[] args)throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        //IR-040860 - Starts
        HashMap paramMap  = (HashMap)programMap.get("requestMap");
        String languageStr = (String) paramMap.get("languageStr");

        //Modified for IR-153213
        String latestReleased=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.RevisionFilterOption.Latest",languageStr);

        //IR-040860 - Ends
        HashMap defaultMap = new HashMap();
        defaultMap.put("Default_AddNewRow",latestReleased);
        //IR-034174 - Starts
        defaultMap.put("Default_ExistingRow",latestReleased);
        //IR-034174 - Ends
        return defaultMap;
    }
    //End for updated for IR-030554

	/**
     * Populates the "Custom Revision" field based on the part selected for cloning.
     * @mx.whereUsed Invoked from the Custom Revision field of the APPPartClone form.
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    public String showCustomRevisionField(Context context, String[] args)
    throws Exception {
    	String customRevision = "";
		try {
		    HashMap programMap = (HashMap) JPO.unpackArgs(args);
		    HashMap requestMap = (HashMap) programMap.get("requestMap");
			String objectId = (String)requestMap.get("copyObjectId");

		    DomainObject domObj = null;
		    if (objectId != null && !objectId.equals("")
		            && !objectId.equals("null")) {
		    	domObj = new DomainObject(objectId);
		    	String policyName = domObj.getInfo(context, SELECT_POLICY);
		    	String sReleasePhase = domObj.getAttributeValue(context, EngineeringConstants.ATTRIBUTE_RELEASE_PHASE);
		    	if(UIUtil.isNotNullAndNotEmpty(sReleasePhase)){
			    	if(EngineeringConstants.DEVELOPMENT.equalsIgnoreCase(sReleasePhase))
			    		policyName = EngineeringConstants.POLICY_DEVELOPMENT_PART;		    		
		    	}
		        Policy policyObj = new Policy(policyName);
		        customRevision = policyObj.getFirstInSequence(context);
		    }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return customRevision;
    }

	/**
     * To displays the VPMProductName field value(V_Name). Displays the V_Name on part if the part is not at synchronized
     * If already Synchronized, displays the V_Name of the corresponding product and links to the Product.
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    public String displayVName(Context context, String[] args) throws Exception{
        // fix for IR-037936V6R2011
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String partId = (String)requestMap.get("objectId");
        String reportFormat = (String)requestMap.get("reportFormat");
        if(reportFormat == null) {
        	reportFormat = (String)requestMap.get("PFmode");
        }
        String sVPMProductKey = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Title.VPMProduct");
        
        DomainObject partObj = new DomainObject(partId);

        String attrVPLMVName = PropertyUtil.getSchemaProperty(context,"attribute_PLMEntity.V_Name");
        String attrVName= PropertyUtil.getSchemaProperty(context,"attribute_V_Name");
        attrVPLMVName = "attribute["+attrVPLMVName+"]";
        String productId = "";
        String productName = "";

		//Fix for IR-046283V6R2011
        StringBuffer strBuf = new StringBuffer();
        

        StringList busSelects = new StringList();
        busSelects.addElement(DomainObject.SELECT_ID);
        busSelects.addElement(attrVPLMVName);

        MapList mapList = partObj.getRelatedObjects(context,
                RELATIONSHIP_PART_SPECIFICATION, "PLMEntity", busSelects,
                null, false, true, (short) 1, null, null, 0);

        Map partDtlMap = null;
        Iterator itr = mapList.iterator();
        if(itr.hasNext()) {

            partDtlMap = (Map) itr.next();

            productId = (String) partDtlMap.get(DomainObject.SELECT_ID);
            productName = (String) partDtlMap.get(attrVPLMVName);

            if(reportFormat == null || "".equals(reportFormat)) {
	            strBuf.append("&#160;&#160;<a href=\"JavaScript:emxFormLinkClick('../common/emxTree.jsp?AppendParameters=true&objectId=");
	            strBuf.append(XSSUtil.encodeForURL(context, productId));
	            strBuf.append("','popup', '', '', '')\">");
	            strBuf.append("<img src=\"images/I_VPMNavProduct.png\" alt=\""+sVPMProductKey+" : " +
	                        XSSUtil.encodeForHTML(context, productName) + "\" title=\""+sVPMProductKey+" : " +  XSSUtil.encodeForHTML(context,productName) + "\" border=\"0\" align=\"absmiddle\"></img>");
	            strBuf.append("</a>&nbsp;");
            }
        }
        strBuf.append(partObj.getAttributeValue(context, attrVName));
    	return strBuf.toString();
    }

    /**
     * This method is to disply the VPM Product Name filed in the table
     * To displays the VPMProductName field value(V_Name). Displays the V_Name on part if the part is not at synchronized
     * If already Synchronized, displays the V_Name of the corresponding product and links to the Product.
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    public Vector displayVNameInTable(Context context, String[] args) throws Exception
    {

        Vector vNameVector = new Vector();
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");

        String productId = "";
        String productName = "";
        String attrVPLMVName = PropertyUtil.getSchemaProperty(context,"attribute_PLMEntity.V_Name");
        attrVPLMVName = "attribute["+attrVPLMVName+"]";
        String typeVPLMProd = PropertyUtil.getSchemaProperty(context,"type_PLMEntity");

        String selectProdctId = "from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"].to."+DomainConstants.SELECT_ID;
        String selectProdctIdSel = "from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"|to.type.kindof["+typeVPLMProd+"]].to."+DomainConstants.SELECT_ID;
        String selectPartVName = "attribute["+EngineeringConstants.ATTRIBUTE_V_NAME+"]";
        
        Map paramMap  = (Map) programMap.get("paramList");
        String reportFormat = (String)paramMap.get("reportFormat");
        
        Map partDtlMap = null;        
        Iterator mapListItr = objectList.iterator();
        String href = "";

        StringList prodSelects = new StringList(3);
        prodSelects.add(selectProdctIdSel);
        prodSelects.add(selectPartVName);
        String objectID_Replaced = "";
        
        /*
         * BOM UI performance: 
         * 1. Moved the two required values ( VPM Product Id and product name) to expand program.
         * 2. The product name is always retrieved from the part attribute since the value will be updated upon performing a BOM sync.
         * 3. Hyperlink is provided only when there are VPLM part specifications connected ( When the part is a resultant of a BOM sync from CAD applications)
         */
        while(mapListItr.hasNext())
        {
        	partDtlMap 		  = (Map)mapListItr.next();
        	objectID_Replaced = (String)partDtlMap.get("OBJECTID_REPLACED"); // if object id is replaced as part of any EBOM authoring, need to get latest info of that using db call 
        	String sRootNode  = (String)partDtlMap.get("Root Node");
        	if(sRootNode!=null && "true".equalsIgnoreCase(sRootNode))
        	{
        		StringList busSelects = new StringList();
        		busSelects.add(selectProdctIdSel);
        		busSelects.add(selectPartVName);

        		DomainObject dmRootObj = DomainObject.newInstance(context, (String)partDtlMap.get(EngineeringConstants.SELECT_ID));
        		partDtlMap = dmRootObj.getInfo(context, busSelects);            	
        	}

        	productName = (String) partDtlMap.get(selectPartVName);
        	
        	String sPartId = (String)partDtlMap.get(EngineeringConstants.SELECT_ID);
        	DomainObject dmObj = DomainObject.newInstance(context,sPartId);
        	if(UIUtil.isNullOrEmpty(productName)) {
        		productName = dmObj.getInfo(context,selectPartVName);
        	}
        	
        	productName = UIUtil.isNotNullAndNotEmpty(productName) ? StringUtils.replace(productName, "&", "&amp;") : "";
        	if(!"TRUE".equalsIgnoreCase(objectID_Replaced) && partDtlMap.containsKey(selectProdctId)) // if object id is replaced as part of any EBOM authoring, we should not consider the data from cache
        	{
        		vNameVector.add(getVNameHref(context, productName, partDtlMap.get(selectProdctId), reportFormat));        		
        	}         	        
    		else {    			
    			if(UIUtil.isNotNullAndNotEmpty(sPartId)){    			    			
    				Map productInfo    = dmObj.getInfo(context, prodSelects);
    	        	productName        = (String) productInfo.get(selectPartVName);
    	        	productName        = UIUtil.isNotNullAndNotEmpty(productName) ? StringUtils.replace(productName, "&", "&amp;") : "";

    				if(productInfo.get(selectProdctId) != null) {
    					productName = getVNameHref(context, productName, productInfo.get(selectProdctId), reportFormat);    					
    				}
    			}
    			vNameVector.add(productName);
    		}
        }	
        	
        return vNameVector;
    }       
   
    private String getVNameHref(Context context,String productName, Object productId,String reportFormat) {
    	String href = "";
		if(productId instanceof String) 
			productId =  StringUtils.split((String)productId, "\\a")[0];
		else if(productId instanceof StringList) 
			productId = (String)((StringList)productId).get(0);
		
		if(UIUtil.isNotNullAndNotEmpty(productName)) {productName = StringUtils.replace(productName, "&", "&amp;"); }
		
		if(reportFormat == null || "".equals(reportFormat))
		{				  	
			String modifyURL =  "../common/emxTree.jsp?objectId=" + productId ;
			//href = productName + "<a href=\"JavaScript:emxTableColumnLinkClick('"+modifyURL+"', '700', '600', 'false', 'popup', '')\"><img src=\"../common/images/I_VPMNavProduct.png\" border=\"0\"/></a>";
			href = "<a href=\"JavaScript:emxTableColumnLinkClick('"+modifyURL+"', '700', '600', 'false', 'popup', '')\"><img src=\"../common/images/I_VPMNavProduct.png\" border=\"0\"/>"+productName+"</a>";

			return href;
		} 
		else { return productName; }
    }
    

	/**
     * This method is to check the form is opened in edit mode or not.
     * @param context
     * @param args
     * @return Object
     * @throws Exception
     */
	public Object checkEditMode(Context context, String[] args)
    throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strMode = (String) programMap.get("mode");
        Boolean isEditMode = Boolean.FALSE;

        // check the mode of the web form.
        if( "edit".equalsIgnoreCase(strMode) ) {
            isEditMode = Boolean.TRUE;
        }

        return isEditMode;
    }
	    public String replaceFirst(String sourceString,String findString,String replaceString) {
	        String retString = sourceString;
		    int index = sourceString.indexOf(findString);
			if(index < 0) {
				  return retString;
			} else {
				StringBuffer sb = new StringBuffer(sourceString);
				sb.replace(index, index + findString.length(), replaceString);
				retString = sb.toString();
			}

			return retString;
		}

		/**Added for IR-044514V6R2011
	     * This method is to disply all child objects for substitute functionality
	     * @param context
	     * @param args
	     * @return MapList
	     * @throws Exception
	     */
	    @com.matrixone.apps.framework.ui.ProgramCallable
	    public MapList getBOMStructure(Context context, String[] args) throws Exception
	    {

	        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	        String partId = (String)paramMap.get("objectId");
	        Part partObj = new Part(partId);
	        MapList ebomList = new MapList();
	        StringList selectStmts = new StringList(6);
	        StringList selectRelStmts = new StringList(6);
	        selectStmts.addElement(DomainConstants.SELECT_ID);
	        selectStmts.addElement(DomainConstants.SELECT_TYPE);
	        selectStmts.addElement(DomainConstants.SELECT_NAME);
	        selectStmts.addElement(DomainConstants.SELECT_REVISION);
	        selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
	        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
	        selectRelStmts.addElement(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
	        selectRelStmts.addElement(SELECT_ATTRIBUTE_QUANTITY);
	        selectRelStmts.addElement(SELECT_ATTRIBUTE_FIND_NUMBER);
	        selectRelStmts.addElement(SELECT_ATTRIBUTE_COMPONENT_LOCATION);
	        selectRelStmts.addElement(SELECT_ATTRIBUTE_USAGE);
	        ebomList = partObj.getRelatedObjects(context,
	                						DomainConstants.RELATIONSHIP_EBOM,  		// relationship pattern
	                						DomainConstants.TYPE_PART,                  // object pattern
	                                        selectStmts,                 				// object selects
	                                        selectRelStmts,              				// relationship selects
	                                        false,                        				// to direction
	                                        true,                       				// from direction
	                                        (short)1,                    				// recursion level
	                                        null,     									// object where clause
	                                        null);                       				// relationship where clause

	        Iterator itr = ebomList.iterator();
	        MapList tList = new MapList();
	        while(itr.hasNext())
	        {
	            HashMap newMap = new HashMap((Map)itr.next());
	            newMap.put("selection", "single");
	            tList.add (newMap);
	        }
	        ebomList.clear();
	        ebomList.addAll(tList);

	        return ebomList;
    	}

    /**
     * Method to check whether VPM or DEC are installed to show the image info.
     * @param context
     * @return boolean
     * @throws Exception
     */
    public Boolean checkForDECorVPMInstallation(Context context, String[] args) throws FrameworkException {
    	return Boolean.valueOf(FrameworkUtil.isSuiteRegistered(context, "appVersionENOVIA VPM Multi-discipline Collaboration Platform", false, null, null)
    					|| FrameworkUtil.isSuiteRegistered(context, "appVersionENOVIA VPM Team Multi-discipline Collaboration Platform", false, null, null)
                        || FrameworkUtil.isSuiteRegistered(context,"appVersionDesignerCentral", false, null, null));
    }

// start - added for BAE debug kit
     /**
      * Gets the "Quantity" attribute entry for each element in the MapList.
      * @param context the eMatrix <code>Context</code> object.
      * @param args contains packed HashMap with the following entries:
      * objectList - a MapList of object information.
      * @return Vector of "level" values for each row.
      * @throws Exception if the operation fails.
      */
     public Vector getQuantityForAVL(Context context,String[] args) throws Exception
     {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)programMap.get("objectList");
        Vector columnVals = new Vector(objList.size());
        Map m = null;
        String quantity = "";
        Iterator i = objList.iterator();
        while (i.hasNext())
        {
            m = (Map) i.next();
            quantity = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_QUANTITY);
            columnVals.addElement(XSSUtil.encodeForHTML(context, quantity));
        }
        return columnVals;
     }

     /**
      * This method returns the Component Location after manipulating the ebom list based on Reference designator format
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - objectId - parent object OID
      * @returns StringList contains the component location values
      * @throws Exception if the operation fails
      */
     public StringList getComponentLocationForAVL (Context context, String[] args) throws Exception
     {
         HashMap paramMap = (HashMap)JPO.unpackArgs(args);
         MapList objList = (MapList)paramMap.get("objectList");
         StringList columnVals = new StringList();
         Map m = null;
         String CompLoc = "";
         Iterator i = objList.iterator();
         while (i.hasNext())
         {
             m = (Map) i.next();
             CompLoc = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_COMPONENT_LOCATION);
             columnVals.addElement(CompLoc);
         }
         return columnVals;
     }

     /**
      * This method returns the Usage after manipulating the ebom list based on Reference designator format
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - objectId - parent object OID
      * @returns StringList contains the Usage values
      * @throws Exception if the operation fails
      */
     public StringList getUsageForAVL (Context context, String[] args) throws Exception
     {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)paramMap.get("objectList");
        StringList columnVals = new StringList();
        Iterator i = objList.iterator();
        String usage = "";
        Map m = null;
        while (i.hasNext())
        {
            m = (Map) i.next();
            usage = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_USAGE);
            columnVals.addElement(usage);
        }
        return columnVals;
     }

     /**
      * This method returns the Reference Designator after manipulating the ebom list based on Reference designator format
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - objectId - parent object OID
      * @returns StringList contains the Reference Designator values
      * @throws Exception if the operation fails
      **/
     public StringList getReferenceDesignatorForAVL (Context context, String[] args) throws Exception
     {
         HashMap paramMap = (HashMap)JPO.unpackArgs(args);
         MapList objList = (MapList)paramMap.get("objectList");
         StringList columnVals = new StringList();
         Iterator i = objList.iterator();
         String refDes = "";
         Map m = null;
         while (i.hasNext())
         {
             m = (Map) i.next();
             refDes = (String)m.get(DomainRelationship.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
             columnVals.addElement(XSSUtil.encodeForHTML(context, refDes));
         }
         return columnVals;
     }
// end - added for BAE debug kit

     /**
      * @param context
      * @param args
      * @return
      * @throws Exception
      */
     public String showRevisionField(Context context, String[] args)
     throws Exception {
     	StringBuffer strRevision = new StringBuffer(64);
		String sRev = "";

 	  try {
 	    HashMap programMap = (HashMap) JPO.unpackArgs(args);
 	    HashMap requestMap = (HashMap) programMap.get("requestMap");
 	    String objectId = (String) requestMap.get("copyObjectId");

 		   if(objectId!=null)
 		   {
 			   DomainObject cPart = new DomainObject();
 			   cPart.setId(objectId);

 			   Policy policyObj =cPart.getPolicy(context);
 			   if(policyObj != null)
 			   {
 				  sRev = policyObj.getFirstInSequence(context);
 			   }
 			  //Modified for IR-076712V6R2012
 			   strRevision.append("<input type=\"text\" size=\"20\" value=\'"+sRev+"\' name=\"Revision\"/>");

 		   }

	 } catch (Exception ex) {
	     ex.printStackTrace();
	 }

	 return strRevision.toString();
 }

    /**
      * This method returns the OID value in maplist along with other rel attributes
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - objectId - parent object OID
      * @returns MapList contains the Effecitivity values
      * @throws Exception if the operation fails
      *
      * Since R210 Added for PUE Reports
 **/
   @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getStoredEffectivityEBOM(Context context, String[] args)
        throws Exception
    {
     MapList ebomList = null;
     try{

            ebomList = getEBOMsWithEffectivityRelSelectables (context, args);

        Iterator itr = ebomList.iterator();
        MapList tList = new MapList();
        StringList ebomDerivativeList = EngineeringUtil.getDerivativeRelationships(context, RELATIONSHIP_EBOM, true);
        while(itr.hasNext())
        {
            HashMap newMap = new HashMap((Map)itr.next());
            newMap.put("selection", "multiple");
			//Added for hasChildren starts
			newMap.put("hasChildren", EngineeringUtil.getHasChildren(newMap, ebomDerivativeList));
			//Added for hasChildren ends
            tList.add (newMap);
        }
        ebomList.clear();
        ebomList.addAll(tList);
		HashMap hmTemp = new HashMap();
        hmTemp.put("expandMultiLevelsJPO","true");
		ebomList.add(hmTemp);
        }
      catch (FrameworkException Ex) {
        throw Ex;
        }

            return ebomList;
      }

    /**
      * This method returns the maplist of ID based on effectivity filter
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - objectId - parent object OID
      * @returns MapList contains the Effecitivity values
      * @throws Exception if the operation fails
      *
      * Since R210 Added for PUE Reports
     **/
public MapList getEBOMsWithEffectivityRelSelectables (Context context, String[] args) throws Exception
{
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);

		int nExpandLevel = 0;
		String sExpandLevels = (String)paramMap.get("emxExpandFilter");
         StringList selectStmts = new StringList(1);
         StringBuffer sObjWhereCond = new StringBuffer(128);

	/* Added for PUE REPORTS  */
		String com_bin1 = (String) paramMap.get("com_bin1");
		String com_bin2 = (String) paramMap.get("com_bin2");
		
		if ("true".equals((String) paramMap.get("BOMCompare"))) {
			String effect1 = (String) paramMap.get("sEffectivityExpressionActual1");
			String effect2 = (String) paramMap.get("sEffectivityExpressionActual2");
			String pcFilter1 = (String) paramMap.get("pcObjectId1");
			String pcFilter2 = (String) paramMap.get("pcObjectId2");
			com_bin1 = getCompiledBinaryCode(context, effect1,pcFilter1);
			com_bin2 = getCompiledBinaryCode(context, effect2,pcFilter2);
		}

	if(sExpandLevels==null || sExpandLevels.length()==0)
	{
		nExpandLevel = 1;
	}
		else
	{
			if("All".equalsIgnoreCase(sExpandLevels))
            nExpandLevel = 0;
             else if (sExpandLevels != null && sExpandLevels.equalsIgnoreCase("EndItem"))
                {
                 nExpandLevel=0;
                    if(sObjWhereCond.length()>0){
                        sObjWhereCond.append(" && ");
                    }
                    // Added By Kaustav For 'End Item' MBOM X+2 Custom Filter START
                     sObjWhereCond.append("(attribute[End Item]==No)");

                     selectStmts.addElement("from[EBOM].to.attribute[End Item]");
                     selectStmts.addElement("from[EBOM].to.id");
                     selectStmts.addElement("from[EBOM].to.type");
                     selectStmts.addElement("from[EBOM].to.revision");
                     selectStmts.addElement("from[EBOM].to.attribute[Description]");
                     selectStmts.addElement("from[EBOM].to.name");
                     selectStmts.addElement("from[EBOM].id");
                }

            else
            nExpandLevel = Integer.parseInt(sExpandLevels);
	}


	String strSide = (String) paramMap.get("side");
    String strConsolidatedReport=(String)paramMap.get("isConsolidatedReport");
    MapList ebomList = new MapList();

	/* Added for PUE REPORTS  */

		String partId = (String) paramMap.get("objectId");

		String strEffBinary = "";

		/* Check to see which side of report is this processing done.
		 * If its left side, the first effectivity info is considerd and if its
		 * right, the second effectivity info is considered.
		 */

		if (strSide.equalsIgnoreCase("Left")) {
			strEffBinary = com_bin1;
		} else if (strSide.equalsIgnoreCase("Right")){
			strEffBinary =com_bin2;
		}

		/* END OF PUE REPORT CHANGES */


    // reportType can be either BOM or AVL. Depending on this value Location Name is set.
    String reportType ="";
   // location variable holds the value of Location Name
    String location = "";
//Added for IR-021267

        /*
        * StringList to store the selects on the Domain Object
        */

        /*
        * StringList to store the selects on the relationship
        */
        StringList selectRelStmts = new StringList(6);

        /*
         * String buffer to prepare where condition with  End Item value
        */

        /*
        * stores the location ID
        */
        String locationId = null;
        /*
        * Maplist holds the data from the getCorporateMEPData method
        */
        MapList tempList = null;
    // retrieve the selected reportType from the paramMap
    reportType = (String) paramMap.get("reportType");
    // retrieve the selected location by the user
    location = (String) paramMap.get("location");

    // Object Where Clause added for Revision Filter

    String complete = PropertyUtil.getSchemaProperty(context,"policy", DomainConstants.POLICY_DEVELOPMENT_PART, "state_Complete");
    String selectedFilterValue = (String) paramMap.get("ENCBOMRevisionCustomFilter");

    if (selectedFilterValue == null)
    {
        if (strSide != null)
        {
            selectedFilterValue = (String) paramMap.get(strSide+"RevOption");
        }
        if (selectedFilterValue == null)
        {
            selectedFilterValue = "As Stored";
        }

    }

    if("Latest Complete".equals(selectedFilterValue)) {
       sObjWhereCond.append("((current == " +complete+") && (revision == last))||((current == "+ complete+") && (next.current != "+complete+"))");
    }

    // To display AVL data for the first time with default Host Company of the user.
    try {
        Part partObj = new Part(partId);
        selectStmts.addElement(DomainConstants.SELECT_ID);
        selectStmts.addElement(DomainConstants.SELECT_TYPE);
        selectStmts.addElement(DomainConstants.SELECT_NAME);
        selectStmts.addElement(DomainConstants.SELECT_REVISION);
        selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
			//Added for hasChildren
			selectStmts.addElement("from["+DomainConstants.RELATIONSHIP_EBOM+"]");
        // Added for MCC EC Interoperability Feature
        String strAttrEnableCompliance  =PropertyUtil.getSchemaProperty(context,"attribute_EnableCompliance");
        selectStmts.addElement("attribute["+strAttrEnableCompliance+"]");
        //end
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_QUANTITY);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_FIND_NUMBER);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_COMPONENT_LOCATION);
        selectRelStmts.addElement(SELECT_ATTRIBUTE_USAGE);
        // int level=1;
			int level = nExpandLevel;

    	//Commented for bug 358519 to display the BOM Compare Consolidated Report based on user entered Expand Level Value-Starts
        /*if(strConsolidatedReport != null && strConsolidatedReport.equalsIgnoreCase("Yes"))
        {
           level=0;
        }*/
	//Commented for bug 358519 to display the BOM Compare Consolidated Report based on user entered Expand Level Value-Ends

/* PUE ECO CHANGES   .................*/


	ebomList =partObj.getRelatedObjects(context,
	                                DomainConstants.RELATIONSHIP_EBOM, // relationship pattern
					DomainConstants.TYPE_PART, // object pattern
					selectStmts,
					selectRelStmts,
					false,
					true,
					(short)level,
					sObjWhereCond.toString(),
					null,
					(short)0,
					false,
					false,
					(short)0,
					null,
					null,
					null,
					null,
					strEffBinary);


//      IR023752    start
        if("EndItem".equalsIgnoreCase(sExpandLevels) )
        {
            int ilevel =0;
             for(int i=0; i<ebomList.size(); i++){
                 Map ebomMap = (Map)ebomList.get(i);
               //Modified for IR-048513V6R2012x, IR-118107V6R2012x
                 Object strEndItem =  ebomMap.get("from["+DomainConstants.RELATIONSHIP_EBOM+"].to.attribute["+EngineeringConstants.ATTRIBUTE_END_ITEM+"]");
                 if(strEndItem!=null){
                     String strEI = (String)strEndItem;
                     if(strEI.equalsIgnoreCase("Yes")){
                     String strLevel = ebomMap.get("level").toString();
                     ilevel = Integer.parseInt((String)strLevel);
                     ilevel = ilevel+1;
                     String strEndItemId = ebomMap.get("from["+DomainConstants.RELATIONSHIP_EBOM+"].to.id").toString();
                     Map endItemMap = new HashMap();
                     endItemMap.put("level",Integer.toString(ilevel));
                     endItemMap.put("id",strEndItemId);
                     endItemMap.put("id[connection]",ebomMap.get("from["+DomainConstants.RELATIONSHIP_EBOM+"].id").toString());
                     ebomList.add(endItemMap);
                 }
                 }
             }
            ebomList = partObj.getRelatedObjects(context,
                     DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
                     DomainConstants.TYPE_PART,                  // object pattern
                                              selectStmts,                 // object selects
                                              selectRelStmts,              // relationship selects
                                              false,                        // to direction
                                              true,                       // from direction
                                              (short)ilevel,                    // recursion level
                                              "",     // object where clause
                                               null);                       // relationship where clause


	}

	//IR023752 end

            if("Yes".equalsIgnoreCase(strConsolidatedReport))
            {
                MapList newBOMList = new MapList();
                getFlattenedMapList(ebomList,newBOMList,null);
                ebomList = newBOMList;
            } else {
                MapList newBOMList = new MapList();
                getFlattenedMapList(ebomList,newBOMList,reportType);
                ebomList = newBOMList;
            }
        // Below code get the last revision of a domain object even if it is not connected to EBOM
       int ebomSize = ebomList.size();
            //  -Modified for the fix IR-013085

            if((ebomList!=null && ebomSize>0 && "Latest".equals(selectedFilterValue)) || ("Latest Complete".equals(selectedFilterValue))) {
               Iterator itr = ebomList.iterator();
               MapList LRev = new MapList();
               String objID = "";
               //Iterate through the maplist and add those parts that are latest but not connected
         while(itr.hasNext()) {
                   Map newMap = (Map)itr.next();
                   String ObjectId = (String)newMap.get("id");
           String oldRev = (String)newMap.get("revision");
                   DomainObject domObj = DomainObject.newInstance(context,ObjectId);
           // get the last revision of the object
                   BusinessObject bo = domObj.getLastRevision(context);
                   bo.open(context);
                   objID = bo.getObjectId();
           String newRev = bo.getRevision();
                        //Modifed for the IR-013085
                        bo.close(context);
                        if ("Latest".equals(selectedFilterValue))
                        {
                            if(!oldRev.equals(newRev))
                            {
                                newMap.put("id",objID);
                            }
                        }
                        //Added for the IR-013085
                        else if("Latest Complete".equals(selectedFilterValue))
                        {
                            DomainObject domObjLatest = DomainObject.newInstance(context,objID);
                            String currSta = domObjLatest.getInfo(context,DomainConstants.SELECT_CURRENT);
                            //Added for the IR-026773
                            if (oldRev.equals(newRev))
                            {
                                if (!complete.equals(currSta))
                                    continue;
                                newMap.put("id",objID);

                            }//IR-026773 ends
                            else
                            {
                            while (!currSta.equalsIgnoreCase(complete)&&!currSta.equals(complete))
                            {
                              BusinessObject boObj = domObjLatest.getPreviousRevision(context);
                              boObj.open(context);
                              objID = boObj.getObjectId();
                              currSta = (String)(DomainObject.newInstance(context,objID).getInfo(context,DomainConstants.SELECT_CURRENT));
                              boObj.close(context);
                            }

                             newMap.put("id",objID);
                            }
                        }// IR-013085 ends
                        //Add new map to the HashMap
                        LRev.add (newMap);
                    }
                   ebomList.clear();
                   ebomList.addAll(LRev);
           }

        if (location!=null && ("").equals(location) && reportType!=null && reportType.equals("AVL"))
        {
           // retrieve the Host Company attached to the User.
             location =com.matrixone.apps.common.Person.getPerson(context).getCompany(context).getObjectId(context);
         }
         if (location!=null && reportType!=null && reportType.equals("AVL"))
         {
                tempList = new MapList();
                locationId = com.matrixone.apps.common.Person.getPerson(context).getCompany(context).getObjectId(context);
             if (locationId.equals(location))
            {
                // In case of Host Company
                tempList = partObj.getCorporateMEPData(context, ebomList, locationId, true, partId);
            }
            else {
                // In case of selected location and All locations
                tempList = partObj.getCorporateMEPData(context, ebomList, location, false, partId);
            }
            ebomList.clear();
            ebomList.addAll(tempList);
         }

// fix for bug 311050
         //check the parent obj state
          boolean allowChanges = true;
          StringList strList  = new StringList(2);
          strList.add(SELECT_CURRENT);
          strList.add("policy");

          Map map = partObj.getInfo(context,strList);

          String objState = (String)map.get(SELECT_CURRENT);
          String objPolicy = (String)map.get("policy");

          String propAllowLevel = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Part.RestrictPartModification");
          StringList propAllowLevelList = new StringList();

          if(propAllowLevel != null || !("".equals(propAllowLevel)) || !("null".equals(propAllowLevel)))
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }
          }
          allowChanges = (!propAllowLevelList.contains(objState));

       //set row editable option
       Iterator itr = ebomList.iterator();
        MapList tList = new MapList();
        while(itr.hasNext())
        {
            Map newMap = (Map)itr.next();
            if(allowChanges)
            {
            newMap.put("RowEditable", "show");
            }else
            {
            newMap.put("RowEditable", "readonly");
            }
            tList.add (newMap);
        }
        ebomList.clear();
        ebomList.addAll(tList);
//end of fix for bug 311050

// fix for bug 311050
         //check the parent obj state
          strList.add(SELECT_CURRENT);
          strList.add("policy");

          if(propAllowLevel != null || !("".equals(propAllowLevel)) || !("null".equals(propAllowLevel)))
          {
            StringTokenizer stateTok = new StringTokenizer(propAllowLevel, ",");
            while (stateTok.hasMoreTokens())
             {
                String tok = (String)stateTok.nextToken();
                propAllowLevelList.add(FrameworkUtil.lookupStateName(context, objPolicy, tok));
             }
          }
          allowChanges = (!propAllowLevelList.contains(objState));

       //set row editable option
        while(itr.hasNext())
        {
            Map newMap = (Map)itr.next();
            if(allowChanges)
            {
            newMap.put("RowEditable", "show");
            }else
            {
            newMap.put("RowEditable", "readonly");
            }
            tList.add (newMap);
        }
        ebomList.clear();
        ebomList.addAll(tList);
//end of fix for bug 311050

    }

    catch (FrameworkException Ex) {
        throw Ex;
    }

    return ebomList;

}

    /**
      * This method returns true if the part is a configured part else returns false
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - objectId - OID of the object to inspect
      * @returns boolean true or false based on the part policy
      * @throws Exception if the operation fails
      *
      * Since R210 Added for PUE Reports
     **/
   	public boolean isConfigurablePart(Context context, String strObjectId)
	throws Exception {

		boolean returnValue 	= false;

		DomainObject sPartDO 	= new DomainObject(strObjectId);
		String policyClassification = "policy.property[PolicyClassification].value";
		String sParentPolicyClass 	= sPartDO.getInfo(context,policyClassification);

		if ("Unresolved".equals(sParentPolicyClass)){
			returnValue = true;
		}
		return returnValue;
	}

    /**
      * This method returns the latest revision of hte part
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      *        0 - objectId - OID of the object to inspect
      * @returns string revision value
      * @throws Exception if the operation fails
      *
      * Since R210 Added for PUE Reports
     **/
	public String getLatestRelRevision(Context context,String strObjId)
        throws Exception {
	 	DomainObject dPart	= DomainObject.newInstance(context, strObjId);
		String sLastid 		= dPart.getInfo(context, DomainObject.SELECT_LAST_ID);
		return sLastid;
	}

    /**
     * Added for excluding the reference documents connected to the object.
     * This can be used for generic purpose.
     * @param context
     * @param args
     * @return List of Object Ids
     * @throws Exception
     * @since R211
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList excludeConnectedObjects(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);
        String strObjectIds = (String)programMap.get("objectId");
        String strRelationship=(String)programMap.get("relName");
        //Get the From side from the URL to decide on traversal
        String strFrom=(String)programMap.get("from");
        String sMode=(String)programMap.get("sMode");
        StringList excludeList= new StringList();
        String strField = (String)programMap.get("field");
        if(strField!= null){
            //get the Field value from URL param to know the types
            strField = strField.substring(strField.indexOf('=')+1,strField.length());
            if( strField.indexOf(':')>0){
                strField = strField.substring(0,strField.indexOf(':'));
            }
        }
        StringList sSelectables= new StringList();
        sSelectables.add(DomainConstants.SELECT_ID);
        String sWhere ="";

        //Maplist to get the records from DB
        MapList childObjects = null;
        if(sMode != null && sMode.equals("ECRAddExisting")){
                  sWhere = "to["+PropertyUtil.getSchemaProperty(context,"relationship_ECRSupportingDocument")+"]== TRUE";
            childObjects = DomainObject.findObjects(context,strField,DomainConstants.QUERY_WILDCARD,sWhere,
                    sSelectables);
        }

        //fix for bug IR-067474V6R2012
        //while connecting Markup to Drawing Print through Add Existing, markups which are already connected should not be displayed on the search page.

        else if(sMode != null && sMode.equals("MarkupAddExisting")){
                sWhere = "to["+PropertyUtil.getSchemaProperty(context,"relationship_Markup")+"]== TRUE";
                childObjects = DomainObject.findObjects(context,strField,DomainConstants.QUERY_WILDCARD,sWhere,
                        sSelectables);

        }
        else {

            boolean bisTo=true;
            boolean bisFrom=false;
            DomainObject domObj= new DomainObject(strObjectIds);

            if(strFrom!=null && strFrom.equalsIgnoreCase("true")){
                bisTo=false;
                bisFrom=true;
            }
            childObjects=domObj.getRelatedObjects(context,
                    PropertyUtil.getSchemaProperty(context,strRelationship),
                    strField==null?"*":strField,
                    new StringList(DomainConstants.SELECT_ID),
                    null,
                    bisTo,
                    bisFrom,
                   (short) 1,
                    DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING);
        }
        for(int i=0;i<childObjects.size();i++){
            Map tempMap=(Map)childObjects.get(i);
            excludeList.add(tempMap.get(DomainConstants.SELECT_ID));
        }
        excludeList.add(strObjectIds);
        return excludeList;
    }

// end - added for BAE debug kit

// Added by vinod for migration of Specification to Common Component -- Start

	class SpecDocuments {
		String documentId;
		String partId;
		String ecoId;
		String rdoId;
		String rdoName; //Added for RDO Convergence
		String sTitle;
		DomainObject domobj;

		SpecDocuments(Context context, Map map, String docObjectId) throws Exception {
			this.documentId = docObjectId;
			if (!isValidData(this.ecoId = getStringValue(map, "ECOForReleaseOID"))) {
				this.ecoId = getStringValue(map, "ECOForRelease");
			}
			if (!isValidData(this.partId = getStringValue(map, "PartToConnectOID"))) {
				this.partId = getStringValue(map, "parentOID"); // Trying to create Spec from part.
			}
			this.rdoId = getStringValue(map, "DesignResponsibilityOID");
			this.domobj = DomainObject.newInstance(context, docObjectId);
			this.rdoName = getStringValue(map, "DesignResponsibility"); //Added for RDO Convergence
			this.sTitle = getStringValue(map, "Title1");
		}

		/**
		 * Connects document from Part
		 *
		 * @param context the eMatrix <code>Context</code> object
		 * @throws Exception if any operation fails.		 *
		 */
		public void connectPart(Context context) throws Exception {
			if (isValidData(this.partId)) {
				DomainRelationship.connect(context,
						new DomainObject(this.partId), // from side object Part
						DomainConstants.RELATIONSHIP_PART_SPECIFICATION, // Relationship
						this.domobj);// toSide object Document
			}
		}

		/**
		 * Connects document from ECO
		 *
		 * @param context the eMatrix <code>Context</code> object
		 * @throws Exception if any operation fails.		 *
		 */
		public void connectECO(Context context) throws Exception {
			if (isValidData(this.ecoId)) {
				DomainRelationship.connect(context,
						new DomainObject(this.ecoId), // from side object ECO
						RELATIONSHIP_AFFECTED_ITEM, // Relationship
						this.domobj); // toSide object Document
			}
		}

		/**
		 * Connects document from RDO
		 *
		 * @param context the eMatrix <code>Context</code> object
		 * @throws Exception if any operation fails.		 *
		 */
		public void connectRDO(Context context) throws Exception {
			if (isValidData(this.rdoId)) {
				String CURRENT_FROM_CONNECT_ACCESS = "current.access[fromconnect]";
				boolean hasFromConnectAccess = ((new DomainObject(this.rdoId)).getInfo(context, CURRENT_FROM_CONNECT_ACCESS)).equalsIgnoreCase("true") ? true : false;
				if (hasFromConnectAccess) {
					DomainRelationship.connect(context,
								new DomainObject(this.rdoId), // from side object Design Responsibilty
								DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY, // Relationship
								this.domobj);// toSide object Document

								//Added for IR-216979 start
						  		if(UIUtil.isNotNullAndNotEmpty(rdoName)) {
						  			this.domobj.setPrimaryOwnership(context, EngineeringUtil.getDefaultProject(context), rdoName);
						  		} else {
						  			this.domobj.setPrimaryOwnership(context, EngineeringUtil.getDefaultProject(context), EngineeringUtil.getDefaultOrganization(context));
						  		}
						  		//Added for IR-216969 End

				} else {
					PartDefinition partDefinition = new PartDefinition();
					partDefinition.setRDO(context, this.rdoId, true);
				}
			}
		}

		/**
		 * sets the attribute for the document object
		 *
		 * @param context the eMatrix <code>Context</code> object
		 * @throws Exception if any operation fails.		 *
		 */
		public void setAttributes(Context context) throws Exception {
			HashMap attributeMap = new HashMap();
			ContextUtil.pushContext(context);
			if(UIUtil.isNotNullAndNotEmpty(this.sTitle)) {
				attributeMap.put(DomainConstants.ATTRIBUTE_TITLE, this.sTitle);
	  		} else {
	  			attributeMap.put(DomainConstants.ATTRIBUTE_TITLE, this.domobj.getName(context));
	  		}
			this.domobj.setAttributeValues(context, attributeMap);
			ContextUtil.popContext(context);
		}

		// Returns Domain object instance.
		public DomainObject getDomainInstance() {
			return this.domobj;
		}

		public String toString() {
			return new StringBuffer("[PartId =").append(this.partId).append(
					", ecoId=").append(this.ecoId).append(", RDOID=").append(
					this.rdoId).append("]").toString();
		}
	}

	private StringList createStringList(String[] selectable) {
		int length = length(selectable);
		StringList list = new StringList(length);
		for (int i = 0; i < length; i++)
			list.add(selectable[i]);
		return list;
	}

	private int length(Object[] array) {
		return array == null ? 0 : array.length;
	}

	private int getListSize(List list) {
		return list == null ? 0 : list.size();
	}

	private String getStringValue(Map map, String key) {
		return (String) map.get(key);
	}

	private boolean isValidData(String data) {
		return ((data == null || "null".equals(data)) ? 0 : data.trim().length()) > 0;
	}

	public boolean hideFromECRContext(Context context, String[] args) throws Exception {
		return !hideRelatedPartToConnectField(context, args);
	}

	/**
     * Connects Sketch object from ECR
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds ParamMap
     * @throws        Exception if the operation fails
     **/
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void connectToECR(Context context, String[] args) throws Exception {
	    HashMap progMap = (HashMap) JPO.unpackArgs(args);
	    HashMap paramMap = (HashMap) progMap.get("paramMap");

	    String sketchId = getStringValue(paramMap, "objectId");
	    String ecrId = getStringValue(paramMap, "New OID");  // creating Sketch from global toolbar

	    if (!isValidData(ecrId)) {
	    	HashMap requestMap = (HashMap) progMap.get("requestMap");
	    	ecrId = getStringValue(requestMap, "parentOID"); // Trying to create Skecth from ECR (Supporting Documents command).
	    }

	    DomainObject sketchDomObj = DomainObject.newInstance(context, sketchId);

	    HashMap attributeMap = new HashMap(1);
		attributeMap.put(DomainConstants.ATTRIBUTE_TITLE, sketchDomObj.getName(context));

	    ContextUtil.pushContext(context);

	    sketchDomObj.setAttributeValues(context, attributeMap);

	    if (isValidData(ecrId)) {
	    	DomainRelationship.connect(context, new DomainObject(ecrId),
					DomainConstants.RELATIONSHIP_ECR_SUPPORTING_DOCUMENT,
					sketchDomObj);
	    }

	    ContextUtil.popContext(context);
	}

	// Returns the originator
	public String getOriginator(Context context, String[] args) throws Exception {
		String contextUser = Person.getPerson(context).getName();
		return PersonUtil.getFullName(context, contextUser);
	}

	/**
     * Returns State names that should be excluded while searching Part.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds ParamMap
     * @return        String
     * @throws        Exception if the operation fails
     **/

	public String getPartDynamicSearchQuery(Context context, String[] args) throws Exception {
	    String PROPERTY_RESTRICT_EC_PART = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ReleaseAndGreaterStates");
	    String PROPERTY_RESTRICT_DEV_PART = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.Part.RestrictDevelopmentPartEdit");

	    String POLICY_CURRENT_EC_PART = ":CURRENT!=policy_ECPart.";
	    String POLICY_CURRENT_DEV_PART = ":CURRENT!=policy_DevelopmentPart.";

	    if (!isValidData(PROPERTY_RESTRICT_EC_PART)) {
	    	PROPERTY_RESTRICT_EC_PART = "state_Release,state_Obsolete";
	    }

	    if (!isValidData(PROPERTY_RESTRICT_DEV_PART)) {
	    	PROPERTY_RESTRICT_DEV_PART = "state_Complete,state_Obsolete";
	    }

	    StringBuffer sbReturn = new StringBuffer(70);
	    sbReturn.append("TYPES=type_Part");
	    sbReturn.append(POLICY_CURRENT_EC_PART).append(StringUtils.replaceAll(PROPERTY_RESTRICT_EC_PART,",", POLICY_CURRENT_EC_PART));
	    sbReturn.append(POLICY_CURRENT_DEV_PART).append(StringUtils.replaceAll(PROPERTY_RESTRICT_DEV_PART,",", POLICY_CURRENT_DEV_PART));

	    return sbReturn.toString();
	}

	// Used to display the Part Name in Part to connect field in type_CreateSpecification form
	public String displayPartToConnect(Context context, String[] args) throws Exception {
		HashMap progMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) progMap.get("requestMap");

		String parentOID = getStringValue(requestMap, "parentOID");
		String partName = "";

		if (isValidData(parentOID)) {
			partName = DomainObject.newInstance(context, parentOID).getName(context);
		}
		return partName;
	}

	private String getObjectWhere (Context context, String policy, String[] symbStates) {
    	int length = length (symbStates);
        StringBuffer objectWhere = new StringBuffer(length * 25);

        String STATE_ACTUAL;

        for (int i = 0; i < length; i++) {
        	STATE_ACTUAL = PropertyUtil.getSchemaProperty(context, "policy", policy, symbStates[i]);
            if (i > 0)
                objectWhere.append(" || ");
            objectWhere.append("current == '").append(STATE_ACTUAL).append('\'');
        }

        return objectWhere.toString();
    }

	// Added this method for IR-075756V6R2012
	public MapList getConnectedECOFromPart(Context context, String[] args) throws Exception {
		HashMap requestMap = (HashMap) JPO.unpackArgs(args);

		String objectId = getStringValue(requestMap, "objectId");

		String[] states = {"state_Create", "state_DefineComponents", "state_DesignWork"};

		//Modified for IR-169021 start
		String objectWhere = "(policy.property[PolicyClassification].value != \"TeamCollaboration\")";
		if("".equals(objectWhere)) {
			objectWhere += getObjectWhere (context, POLICY_ECO, states);
		} else {
			objectWhere += " && " + getObjectWhere (context, POLICY_ECO, states);
		}
		//Modified for IR-169021 end

		StringList objectSelect = createStringList(new String[] {DomainConstants.SELECT_NAME, DomainConstants.SELECT_ID});

		DomainObject domObj = DomainObject.newInstance(context, objectId);

		return domObj.getRelatedObjects(context, RELATIONSHIP_AFFECTED_ITEM, DomainConstants.TYPE_ECO, objectSelect, null, true, false, (short) 1, objectWhere, null, null, null, null);
	}

	// Used to hide the Part to connect field in type_CreateSpecification form
	public boolean hidePartToConnectField(Context context, String[] args) throws Exception {
		return !hideRelatedPartToConnectField(context, args);
	}

	// Used to hide the Part to connect field in type_CreateSpecification form
	public boolean hideRelatedPartToConnectField(Context context, String[] args) throws Exception {
		HashMap fieldMap = (HashMap) JPO.unpackArgs(args);
		String parentOID = getStringValue(fieldMap, "parentOID");
		return isValidData(parentOID);
	}

	/**
	 * Does all the required connections to Document
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds ParamMap
	 * @throws Exception if any operation fails.		 *
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void performPostProcessConnect(Context context, String[] args) throws Exception {
        HashMap map = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) map.get("requestMap");
        HashMap paramMap = (HashMap) map.get("paramMap");

        String documentId = getStringValue(paramMap, "objectId");

        SpecDocuments document = new SpecDocuments(context, requestMap, documentId);
        document.connectPart(context);
        document.connectECO(context);
        document.connectRDO(context);
        document.setAttributes(context);
    }

    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public Map createSpecification(Context context, String[] args) throws Exception {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

			String sType = getStringValue(programMap, "TypeActual");
			String nameField = (String) programMap.get("nameField");
			String sAutoNameChecked	= getStringValue(programMap, "autoNameCheck");
			String sAutoNameSeries = getStringValue(programMap, "AutoNameSeries");
			String sName = getStringValue(programMap, "Name");
			if(UIUtil.isNullOrEmpty(sAutoNameChecked) && UIUtil.isNullOrEmpty(sAutoNameSeries)) { 
				sAutoNameSeries = sName;
				if("keyin".equals(nameField)){
					sAutoNameChecked = "false";
				}
				else
				sAutoNameChecked = "true";
			}
			String sCustomRevisionLevel	= getStringValue(programMap, "Revision");
			String sPolicy = getStringValue(programMap, "Policy");
			String sVault = getStringValue(programMap, "Vault");

			//EngineeringUtil.checkLicenseForDEC(context, sType);	  //checking for DEC license
			boolean autoNameSelected = "true".equals(sAutoNameChecked);
			if (!autoNameSelected) {
				sAutoNameSeries = sCustomRevisionLevel;
			}

			PartDefinition partDefinition = new PartDefinition();
			partDefinition.create(context, sType, sName, sAutoNameSeries,
					null, autoNameSelected, sPolicy, sVault, null, null, null,
					null, sCustomRevisionLevel, null);

			String documentId = partDefinition.getId();
			HashMap mapReturn = new HashMap(1);
			mapReturn.put("id", documentId);

			return mapReturn;
		}


	// Added by vinod for Migration of Specification to Common Component -- End


    /* Added for Part Create conversion to Common Component */

    /**
	 * Update program to connect the affected Item with the ECO
	 *
	 * @param context
	 * @param args
	 * @throws Exception
	 * @Since R211
	 */
	public void connectRelECO(Context context, String[] args) throws Exception {
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		HashMap progMap = (HashMap) paramMap.get("paramMap");

		String fromObject = (String) progMap.get("New OID");
		String toObject = (String) progMap.get("objectId");

		if ((fromObject != null) && !"".equals(fromObject)) {
			try {
				DomainObject fromBusObj = new DomainObject(fromObject);
				DomainObject toBusObj = new DomainObject(toObject);

				DomainRelationship.connect(context, fromBusObj,
						RELATIONSHIP_AFFECTED_ITEM, toBusObj);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
	}

	/**
	 * Update program to connect the Part/Spec with with the RDO
	 *
	 * @param context
	 * @param args
	 * @throws Exception
	 * @Since R211
	 */
	public void connectRDO(Context context, String[] args) throws Exception {

		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) paramMap.get("requestMap");
		String[] hasRDO = (String[]) requestMap.get("hasRDO");
		String strHasRDO = "";
		if (null != hasRDO) {
			strHasRDO = hasRDO[0];
		}
		HashMap progMap = (HashMap) paramMap.get("paramMap");
		String fromObject = (String) progMap.get("New OID");
		String toObject = (String) progMap.get("objectId");
		DomainObject domObj = new DomainObject(toObject);
		String strNewRDOName = (String) progMap.get("New Value");
		boolean connectRDO = true;
		boolean disConnectRDOs = true;

		//Added for IR-216979 start
		DomainObject toBusObj = new DomainObject(toObject);
		/* if(UIUtil.isNotNullAndNotEmpty(strNewRDOName)) {
			toBusObj.setPrimaryOwnership(context, EngineeringUtil.getDefaultProject(context), strNewRDOName);
		} else {
			toBusObj.setPrimaryOwnership(context, EngineeringUtil.getDefaultProject(context), EngineeringUtil.getDefaultOrganization(context));
		} */
		//Added for IR-216969 End

		// If the user doesn't change the value of RDO field , need not
		// disconnect the RDOs
		// If the page is create page, need not disconnect the RDOs
		if ((!"".equals(strNewRDOName) && "".equals(fromObject))
				|| ("False".equals(strHasRDO) || "".equals(strHasRDO))) {
			disConnectRDOs = false;
		}
		// If the user doesn't change the value of RDO field, need not connect
		// the RDO
		// If the user clears the RDO field, need not connect the RDO
		if ((!"".equals(strNewRDOName) && "".equals(fromObject))
				|| ("".equals(strNewRDOName) && "".equals(fromObject))) {
			connectRDO = false;
		}

		if (disConnectRDOs) {
			StringList strListOldRDORelIds = domObj.getInfoList(context, "to["
					+ RELATIONSHIP_DESIGN_RESPONSIBILITY + "].id");
			StringItr rdoRelItr = new StringItr(strListOldRDORelIds);
			while (rdoRelItr.next()) {
				String strRDORel = rdoRelItr.obj();
				DomainRelationship.disconnect(context, strRDORel);
			}
		}

		if (connectRDO) {
			try {
				DomainObject fromBusObj = new DomainObject(fromObject);
				DomainRelationship.connect(context, fromBusObj,
						DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,
						toBusObj);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
	}

	/**
	 * To connect the Part Family to the Part getting created.
	 *
	 * @param context
	 * @param args
	 * @return Map
	 * @throws Exception
	 * @Since R211
	 */
	public Object connectPartToPartFamily(Context context, String[] args)
			throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");

		String strPartId = (String) paramMap.get("objectId");

		String newPartFamilyIds = (String) paramMap.get("New OID");

		String strPartFamilyRelationship = RELATIONSHIP_CLASSIFIED_ITEM;
		StringList newPartFamilyList = FrameworkUtil.split(newPartFamilyIds,
				",");

		DomainObject doPartObj = newInstance(context, strPartId);
		if ((newPartFamilyList != null) && !"".equals(newPartFamilyList)) {
			try {
				ContextUtil.pushContext(context, PropertyUtil
						.getSchemaProperty(context, "person_UserAgent"),
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING); // 366577

				// construct array of ids
				if (newPartFamilyList.size() > 0) {

					Iterator partFamilyItr = newPartFamilyList.iterator();
					while (partFamilyItr.hasNext()) {
						String newPartFamily = (String) partFamilyItr.next();
						setId(strPartId);
						DomainObject domainObjectFromType = newInstance(
								context, newPartFamily);
						DomainRelationship.connect(context,
								domainObjectFromType,
								strPartFamilyRelationship, doPartObj);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			} finally {
				ContextUtil.popContext(context);
			}
		}

		return Boolean.TRUE;
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
	public Map createPartJPO(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		String strPartId = "";
		String sType = (String) programMap.get("TypeActual");
		String nameField = (String) programMap.get("nameField");
		String sAutoNameChecked = (String) programMap.get("autoNameCheck");
		String sAutoNameSeries = (String) programMap.get("AutoNameSeries");
		String sName = (String) programMap.get("Name");
		if(UIUtil.isNullOrEmpty(sAutoNameChecked) && UIUtil.isNullOrEmpty(sAutoNameSeries)) {
			sAutoNameSeries = sName;
			if("keyin".equals(nameField)){
				sAutoNameChecked = "false";
			}
			else
			sAutoNameChecked = "true";
		}
		String sPartFamilyId = (String) programMap.get("PartFamilyOID");
		String sCustomRevisionLevel = (String) programMap
				.get("CustomRevisionLevel");
		//String sPolicy = (String) programMap.get("Policy");
		String sVault = (String) programMap.get("Vault");
		String sOwner = (String) programMap.get("Owner");
		//Added for BGTP---Start
		String isConfigured = (String) programMap.get("Configured");
		String sCreateMode = (String)programMap.get("createMode");
		if(UIUtil.isNullOrEmpty(isConfigured) && "assignTopLevelPart".equalsIgnoreCase(sCreateMode)){
			isConfigured = "true";
		}
		String sReleasePhase = (String) programMap.get("ReleaseProcess");
		String sPolicy = (String) programMap.get("Policy");
		if(UIUtil.isNullOrEmpty(sPolicy)){
			if("true".equals(isConfigured)){
				sPolicy = ReleasePhase.getConfigPolicy(context, sType, sReleasePhase);
			}
			else{
				sPolicy = ReleasePhase.getPolicy(context, sType, sReleasePhase);
			}
		}
   	
	   	if("Development".equals(sReleasePhase) && !"true".equals(isConfigured)){
			String sDevPartSequence=MqlUtil.mqlCommand(context,"print policy $1 select $2 dump",EngineeringConstants.POLICY_DEVELOPMENT_PART,"minorsequence");
		    StringList sequence = FrameworkUtil.split(sDevPartSequence, ",");
		   	String sequenceOfDevPart = (String)sequence.get(0);
	   		sCustomRevisionLevel = sequenceOfDevPart;
	   	}
	   	//Added for UI Modification - Move design collaboration under Engineering Option section
	   	String isVPMVisible = (String) programMap.get("isVPMVisible");
	   	
		Map returnMap = new HashMap();

		try {
			Part part = (Part) DomainObject.newInstance(context,
					DomainConstants.TYPE_PART, DomainConstants.ENGINEERING);

			strPartId = part.createPartAndConnectRDO(context, sType, sName,
					sCustomRevisionLevel, sPolicy, sVault, sOwner,
					sAutoNameChecked, sPartFamilyId, sAutoNameSeries, null);
			
			if(!"true".equals(isVPMVisible)){
				part.setAttributeValue(context, EngineeringConstants.ATTRIBUTE_VPM_VISIBLE, "FALSE");
			} 
			
			returnMap.put("id", strPartId);

		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e);
		}

		return returnMap;
	}

	/**
	 * if objectId is of Part Family then policy will be returned from partfamily attribute "Deafult Part Policy"
	 * else it will return "Development Part" as default policy.
	 *
	 * @param context ematrix context
	 * @param map contains request map
	 * @return String policy name
	 * @throws Exception if error occurs
	 */
	private String getDefaultPolicy(Context context, HashMap map) throws Exception {
		String defaultPolicy = EnoviaResourceBundle.getProperty(context, "type_Part.defaultProdPolicy");
		String objectId = getStringValue(map, "objectId");

		if (isValidData(objectId)) {
			StringList objectSelect = createStringList(new String[] {DomainConstants.SELECT_TYPE, SELECT_ATTRIBUTE_DEFAULT_PART_POLICY});

			DomainObject domObj = DomainObject.newInstance(context, objectId);
			Map dataMap = domObj.getInfo(context, objectSelect);

			String strType = getStringValue(dataMap, DomainConstants.SELECT_TYPE);

			if (DomainConstants.TYPE_PART_FAMILY.equals(strType)) {
				String strPolicy = getStringValue(dataMap, SELECT_ATTRIBUTE_DEFAULT_PART_POLICY);

				if (isValidData(strPolicy)) {
					defaultPolicy = PropertyUtil.getSchemaProperty(context,strPolicy);
				}
			}
		}

		return defaultPolicy;
	}

	   /**
    *
    * Method to get symbolic names of policies and their 1st revision .
    * @param context the eMatrix code context object
    * @param String[] packed hashMap of request parameters
    * @throws Exception if the operation fails
    * @return MapList.
    *
    */
	public MapList getPolicyRevision(Context context, String[] args) throws Exception {

		HashMap hmPolicyRev = new HashMap();
		MapList mPolicyName = new MapList();
		MapList mlResult = new MapList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String typeString = (String) programMap.get("type");

		try {
			BusinessType partBusinessType = new BusinessType(typeString, context.getVault());

			PolicyList allPartPolicyList = partBusinessType
					.getPoliciesForPerson(context, false);
			PolicyItr partPolicyItr = new PolicyItr(allPartPolicyList);
			Policy policyValue = null;
			String policyName = "";
			String symbolicName = "";
			String sRev;

				while (partPolicyItr.next()) {
					policyValue = (Policy) partPolicyItr.obj();
					policyName = policyValue.getName();
					sRev = policyValue.getFirstInSequence(context);
					symbolicName = PropertyUtil.getAliasForAdmin(context, "policy", policyName, true);
					hmPolicyRev.put(symbolicName, sRev);
					mPolicyName.add(symbolicName);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		mlResult.add(hmPolicyRev);
		mlResult.add(mPolicyName);

		return mlResult;
	}

	/**
	 * To display the policy list in part creation page Also considers the app
	 * specific policies
	 *
	 * @param context
	 * @param args
	 * @return HashMap
	 * @throws Exception
	 * @Since R211
	 */
	public HashMap getPolicy(Context context, String[] args) throws Exception {
		HashMap hmPolicyMap = new HashMap();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String createMode = (String) requestMap.get("createMode");
		String typeString = (String) requestMap.get("type");
		String parentId =(String) requestMap.get("bomObjectId");
		try {
			String POLICY_UNRESOLVED_PART = PropertyUtil.getSchemaProperty(context, "policy_UnresolvedPart"); // When upgrading this policy should be hidden in create part page.
			if(typeString.indexOf(',')!=-1 && !typeString.startsWith("_selectedType")){
				typeString = typeString.substring(0, typeString.indexOf(','));
				}
			if(typeString.startsWith("type_")){
				typeString = PropertyUtil.getSchemaProperty(context, typeString);
			}else if(typeString.startsWith("_selectedType")){
				typeString = typeString.substring(typeString.indexOf(':')+1, typeString.indexOf(','));
			}else if("".equals(typeString) || typeString==null){
				typeString = DomainConstants.TYPE_PART;
			}

			BusinessType partBusinessType = new BusinessType(typeString, context.getVault());
			if(!mxType.isOfParentType(context, typeString, DomainConstants.TYPE_PART)){
				throw new FrameworkException();
			}

			PolicyList allPartPolicyList = partBusinessType
					.getPoliciesForPerson(context, false);
			PolicyItr partPolicyItr = new PolicyItr(allPartPolicyList);

			boolean isMBOMInstalled = FrameworkUtil.isSuiteRegistered(context,
					"appVersionX-BOMManufacturing", false, null, null);
			boolean bcamInstall = FrameworkUtil.isSuiteRegistered(context,
					"appVersionX-BOMCostAnalytics", false, null, null);
			boolean isXCEInstalled = FrameworkUtil.isSuiteRegistered(context,
					"appVersionEngineeringConfigurationCentral", false, null, null);

			String POLICY_STANDARD_PART = PropertyUtil
					.getSchemaProperty(context,"policy_StandardPart");
			String POLICY_CONFIGURED_PART = PropertyUtil
					.getSchemaProperty(context,"policy_ConfiguredPart");

			String languageStr = context.getSession().getLanguage();
			String defaultPolicy = getDefaultPolicy(context, requestMap); // IR-082946V6R2012

			Policy policyValue = null;
			String policyName = "";
			String policyClassification = "";

			StringList display = new StringList();
			StringList actualVal = new StringList();
			
			String contextPartPolicy = POLICY_CONFIGURED_PART;
			if(UIUtil.isNotNullAndNotEmpty(parentId))
			{
				DomainObject doj = DomainObject.newInstance(context, parentId);
				contextPartPolicy = doj.getInfo(context,DomainConstants.SELECT_POLICY);
			}
			
			if ("assignTopLevelPart".equals(createMode) || POLICY_CONFIGURED_PART.equals(defaultPolicy)) {
				display.addElement(i18nNow.getAdminI18NString("Policy",
						POLICY_CONFIGURED_PART, languageStr));

				actualVal.addElement(POLICY_CONFIGURED_PART);

			} else if("MFG".equals(createMode)) {
				StringList slMfgPolicy = EngineeringUtil.getManuPartPolicy(context);
				if(slMfgPolicy.size()>0) {
					defaultPolicy = (String)slMfgPolicy.get(0);
				}
				for(int i=0; i<slMfgPolicy.size(); i++)
				{
					policyName = (String)slMfgPolicy.get(i);

					if(EngineeringUtil.getPolicyClassification(context, policyName).equals("Equivalent")) {
						continue;
					} else if(policyName.equals(PropertyUtil.getSchemaProperty(context,"policy_StandardPart"))) {
						continue;
					}

					display.addElement(i18nNow.getAdminI18NString("Policy", policyName, languageStr));
					actualVal.addElement(policyName);
					if(i == 0) {
						defaultPolicy = (String)slMfgPolicy.get(i);
					}
					if(policyName.equals(PropertyUtil.getSchemaProperty(context,"policy_ManufacturingPart"))) {
						defaultPolicy = policyName;
					}
				}
			} else {

				while (partPolicyItr.next()) {
					policyValue = (Policy) partPolicyItr.obj();
					policyName = policyValue.getName();

					//when upgrading from previous release, skip this policy
				    if (policyName.equals(POLICY_UNRESOLVED_PART)) {
				        continue;
				    }

					policyClassification = EngineeringUtil
							.getPolicyClassification(context, policyName);

					// Modified for TBE Packaging & Scalability
					if (!EngineeringUtil.isENGInstalled(context, args)&&
							!EngineeringUtil.getPolicyClassification(context,policyName).equals("Development" ))
					{
						continue;
					}

					if (!isMBOMInstalled
							&& POLICY_STANDARD_PART
									.equalsIgnoreCase(policyName)) {
						continue;
					}
					if (POLICY_CONFIGURED_PART.equalsIgnoreCase(policyName) && (!isXCEInstalled || !POLICY_CONFIGURED_PART.equals(contextPartPolicy))){
						continue;
					}
					if (bcamInstall) {
						if ("Cost".equals(policyClassification)) {
							continue;
						}
					}
					if (EngineeringUtil.getPolicyClassification(context,
							policyName).equals("Equivalent")
							|| EngineeringUtil.getPolicyClassification(context,
									policyName).equals("Manufacturing")) {
						continue;
					}

					display.addElement(i18nNow.getAdminI18NString("Policy",
							policyName, languageStr));

					actualVal.addElement(policyName);
				}
			}
			int position = actualVal.indexOf(defaultPolicy);
			if (position > 0) {
				String positionDisplay = (String) display.get(position);
				String positionActual = (String) actualVal.get(position);
				display.setElementAt(display.get(0), position);
				actualVal.setElementAt(actualVal.get(0), position);
				display.setElementAt(positionDisplay, 0);
				actualVal.setElementAt(positionActual, 0);
			}
			hmPolicyMap.put("field_choices", actualVal);
			hmPolicyMap.put("field_display_choices", display);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return hmPolicyMap;
	}

	/**
	 * To display the Part Family based when invoked from the part family page
	 * Also considers the app specific policies
	 *
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 * @Since R211
	 */
	public String getPartFamilyDisplay(Context context, String[] args)
			throws Exception {
		String strPartFamilyDisplay = "";

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String strPartFamilyId = (String) requestMap.get("objectId");

			if (null != strPartFamilyId && !"".equals(strPartFamilyId)) {
				DomainObject domObj = new DomainObject(strPartFamilyId);

				if (domObj.isKindOf(context, PropertyUtil.getSchemaProperty(
						context, "type_PartFamily")))
					strPartFamilyDisplay = domObj.getInfo(context, SELECT_NAME);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		return strPartFamilyDisplay;
	}

    /**
     * To display the Part Family Field only when invoked from the Part Family page
     * Also considers the app specific policies
     *
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     * @Since R211
     */
    public boolean isNotGeneralClassPart(Context context, String[] args) throws Exception
    {
    	return (!(isGeneralClassPart(context, args) || isCustomClassPart(context, args)));
    }

    /**
     * To display the General Class Field only when invoked from the General Class page
     * Also considers the app specific policies
     *
     * @param context
     * @param args
     * @return String
     * @throws Exception
     * @Since R211
     */
    public boolean isGeneralClassPart(Context context, String[] args)
            throws Exception {
        DomainObject domObj         = null;
        boolean  isGeneralClass     = false;
        try {
            HashMap programMap      = (HashMap) JPO.unpackArgs(args);
            String strPartFamilyId  = (String) programMap.get("objectId");
            if (!UIUtil.isNullOrEmpty(strPartFamilyId)) {
                 domObj = new DomainObject(strPartFamilyId);
                 isGeneralClass     = domObj.isKindOf(context, PropertyUtil.getSchemaProperty(context, "type_GeneralClass"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return (isGeneralClass);
    }

    public boolean isCustomClassPart(Context context, String[] args) throws Exception
    {
    	DomainObject domObj         = null;
    	boolean  isCustomClass     = false;
    	try
    	{
    		HashMap programMap      = (HashMap) JPO.unpackArgs(args);
    		String strPartFamilyId  = (String) programMap.get("objectId");
    		if (!UIUtil.isNullOrEmpty(strPartFamilyId))
    		{
    			domObj = new DomainObject(strPartFamilyId);
    			boolean isPartFamily     = domObj.isKindOf(context, PropertyUtil.getSchemaProperty(context, "type_PartFamily"));
    			if(!isPartFamily)
    			{
    				boolean isGeneralClass     = domObj.isKindOf(context, PropertyUtil.getSchemaProperty(context, "type_GeneralClass"));
    				if(!isGeneralClass)
    				{
    					isCustomClass     = domObj.isKindOf(context, PropertyUtil.getSchemaProperty(context, "type_Classification"));
    				}
    			}
    		}
    	}
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    		throw ex;
    	}
    	return (isCustomClass);
    }

	/**
	 * To populate the default Revision value with respect to the default Policy
	 *
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 * @Since R211
	 */
	public String showDefaultRevision(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");

		String createMode = (String) requestMap.get("createMode");
		String defaultPolicy = "";

		if("MFG".equals(createMode)) {
			defaultPolicy = PropertyUtil.getSchemaProperty(context,"policy_ManufacturingPart");
		} else {
			defaultPolicy = getDefaultPolicy(context, requestMap); // IR-082946V6R2012
		}

		return new Policy(defaultPolicy).getFirstInSequence(context);
	}

	/**
	 * To populate the default Vault value
	 *
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 * @Since R211
	 */
	public String showDefaultVault(Context context, String[] args)
			throws Exception {
		//return context.getVault().getName();
		// Modified for 091260
		String defaultVault = context.getVault().getName();
    	HashMap programMap = (HashMap)JPO.unpackArgs(args);
    	HashMap paramMap = (HashMap)programMap.get("paramMap");
    	String languageStr = (String) paramMap.get("languageStr");
    	if(null!=defaultVault && null!=languageStr)
    		defaultVault = i18nNow.getAdminI18NString("Vault", defaultVault, languageStr);
    	return defaultVault;
	}

	/**
	 * A dummy update function used in part create page
	 *
	 * @param context
	 * @param args
	 * @return boolean Since R211
	 */
	public boolean dummyUpdateFunction(Context context, String[] args) {
		return true;
	}

	/**
	 * To populate and handle the autoname series field in part create page
	 *
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 * @Since R211
	 */
	public String partFamilyAutoGenaration(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");

		String sObjectId = (String) requestMap.get("objectId");
		String sCreateMode = (String) requestMap.get("createMode");

		StringBuffer field = new StringBuffer(128);
		try {

			if (null != sObjectId && !"".equals(sObjectId) && null != sCreateMode
					 && sCreateMode.equals("LIB")) {
				DomainObject DOBJ = new DomainObject(sObjectId);

				String TYPE_PARTFAMILY = PropertyUtil.getSchemaProperty(
						context, "type_PartFamily");
				String attrPFNameGen = PropertyUtil.getSchemaProperty(context,
						"attribute_PartFamilyNameGeneratorOn");

				if (DOBJ.isKindOf(context, TYPE_PARTFAMILY)) {
					String attrPFNameGenVal = DOBJ.getAttributeValue(context, attrPFNameGen);
					field.append("<input type=\"hidden\" value=\""
							+ XSSUtil.encodeForHTMLAttribute(context, attrPFNameGenVal)
							+ "\" name=\"PartFamilyAutoName\"/>");
					field.append(" <script language=\"javascript\"> ");
					field.append("  document.forms[\'emxCreateForm\'].elements[\'PartFamilyOID\'].value =\""
									+ XSSUtil.encodeForJavaScript(context, sObjectId) + "\"; ");

					if ("TRUE".equals(attrPFNameGenVal)) {
						//Modified for IR-097616V6R2012 starts
						//Multitenant
						String   strLangPartFamily = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Common.PartFamily");
						field.append(" var sAutoNameSeries = document.getElementById(\'AutoNameSeriesId\'); ");
						field.append(" var sAutoNameCheck = document.forms[\'emxCreateForm\'].elements[\'autoNameCheck\']; ");
						field.append(" var sNameField = document.forms[\'emxCreateForm\'].elements[\'Name\']; ");
				        field.append(" try { sAutoNameSeries.add(new Option(\""+XSSUtil.encodeForJavaScript(context,strLangPartFamily)+"\",\"Part Family\"), null); ");
						field.append("  } catch(ex){ ");
						field.append(" sAutoNameSeries.add(new Option(\""+XSSUtil.encodeForJavaScript(context,strLangPartFamily)+"\",\"Part Family\"));  } ");
						field.append(" sAutoNameSeries.value = \"Part Family\"; ");
						//Modified for IR-097616V6R2012 ends

					}
					field.append(" </script> ");
                } else if (DOBJ.isKindOf(context, PropertyUtil.getSchemaProperty(context, "type_GeneralClass"))) {
                            String strClassificationDisplay = DOBJ.getInfo(context, SELECT_NAME);
                            field.append(" <script language=\"javascript\"> ");
                          //Modified for IR-132514V6R2013 and IR-133772V6R2013 start
                            field.append("  document.forms[\'emxCreateForm\'].elements[\'GeneralClass\'].value =\""+XSSUtil.encodeForJavaScript(context, strClassificationDisplay)+"\"; ");
                            field.append("  document.forms[\'emxCreateForm\'].elements[\'GeneralClassDisplay\'].value =\""+XSSUtil.encodeForJavaScript(context, strClassificationDisplay)+"\"; ");
                          //Modified for IR-132514V6R2013 and IR-133772V6R2013 end
                            field.append("  document.forms[\'emxCreateForm\'].elements[\'GeneralClassOID\'].value =\""+XSSUtil.encodeForJavaScript(context,sObjectId)+"\"; ");
                            field.append(" </script> ");
                            field.append("<input type=\"hidden\" value=\"FALSE\" name=\"PartFamilyAutoName\"/>");
				}
                else
                {

                    String strClassificationDisplay = DOBJ.getInfo(context, SELECT_NAME);
                    field.append(" <script language=\"javascript\"> ");
                    field.append("  document.forms[\'emxCreateForm\'].elements[\'CustomClass\'].value =\""+XSSUtil.encodeForJavaScript(context, strClassificationDisplay)+"\"; ");
                    field.append("  document.forms[\'emxCreateForm\'].elements[\'CustomClassDisplay\'].value =\""+XSSUtil.encodeForJavaScript(context, strClassificationDisplay)+"\"; ");
                    field.append("  document.forms[\'emxCreateForm\'].elements[\'CustomClassOID\'].value =\""+XSSUtil.encodeForJavaScript(context,sObjectId)+"\"; ");
                    field.append(" </script> ");
                    field.append("<input type=\"hidden\" value=\"FALSE\" name=\"PartFamilyAutoName\"/>");

                }
			} else {
				if(sObjectId != null && !"".equals(sObjectId)){
					PartFamily pf = new PartFamily(sObjectId);
					boolean autoNameState = pf.isAutoNameOn(context);
					if(autoNameState){
						field.append("<input type=\"hidden\" value=\"TRUE\" name=\"PartFamilyAutoName\"/>");
					}else{
						field.append("<input type=\"hidden\" value=\"FALSE\" name=\"PartFamilyAutoName\"/>");
					}
				}else{
					field.append("<input type=\"hidden\" value=\"FALSE\" name=\"PartFamilyAutoName\"/>");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return field.toString();
	}


	/**
	 * This method is called on part edit post process action
	 *
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public HashMap partEditPostProcess(Context context, String[] args) throws Exception
	{
		HashMap programMap  = (HashMap) JPO.unpackArgs(args);
		Map requestMap      = (Map) programMap.get("requestMap");
		String objectId     = (String)requestMap.get("objectId");
		Part part 			= new Part(objectId);

		HashMap resultMap = checkLicense(context, args);

		if (resultMap.get("Message") == null) {
			boolean isMFGInstalled = EngineeringUtil.isMBOMInstalled(context);
			if(isMFGInstalled) {
				part.setEndItem(context);
	    String sPlanningReqd = (String)requestMap.get("PlanningRequired");
	    if(null != sPlanningReqd && !DomainConstants.EMPTY_STRING.equals(sPlanningReqd)){
	     part.setPlanningReq(context,sPlanningReqd);
	    }
	            Class clazz = Class.forName("com.matrixone.apps.mbom.PartMaster");
	            IPartMaster partMaster = (IPartMaster) clazz.newInstance();
	            partMaster.updateManuRespMakeBuy(context,objectId);
			}
		}
		
		/* Added for Auto collaboration .st*/
		IPartCollaborationService iPartCollabService = new PartCollaborationService();
	
		IPartIngress  iPartIngress = IPartIngress.getService();
		iPartIngress.setObjectId(objectId);
		
	    ArrayList<String> list = new ArrayList<String>();
	    list.add(objectId);
		
		iPartCollabService.setCollaboratToDesign(true);
		iPartCollabService.performCollabOperation(context, list, IPartValidator.OPERATION_MODIFY);
		/* Added for Auto collaboration .end*/

		return resultMap;
	}

	public String showDefaultDesignRes(Context context, String[] args)
throws Exception { //IR:073691
	   String strName ="";
	   String personDef = com.matrixone.apps.common.Person.getDesignResponsibility(context);
	    if(personDef != null && !"null".equals(personDef) && !"".equals(personDef)){
            if(personDef.indexOf('}') > 0) {
              personDef = personDef.substring(personDef.indexOf('}')+2);
              strName = personDef.substring(0,personDef.indexOf('}'));
            }
	    }
	 return strName;
}

	 /**
	  * This method will be called whenever ECO and Part getting connected with "Affected Item" relationship.
	  * method is called in check trigger of Affected Item relationship.
	 * @param context ematrix context
	 * @param args contains paramater passed from trigger RelationshipAffectedItemCreateCheck
	 * @return 0 or 1.
	 * @throws Exception if any error occurs.
	 */
	public int checkForChangeAlreadyConnected(Context context, String[] args) throws Exception {
	     int iCheckTrigPass = 0;

	     String partId     = args[1];
	     String partType   = args[2];
	     String partName   = args[3];
	     String partRev    = args[4];
	     String changeId = args[5];

	     DomainObject chgObj = new DomainObject(changeId);

	     if (chgObj.isKindOf(context, DomainConstants.TYPE_ECO) && isValidData(partId)) {
	         DomainObject domObj = DomainObject.newInstance(context, partId);

	         String partCurrent = domObj.getInfo(context, DomainConstants.SELECT_CURRENT);

	         if (!DomainConstants.STATE_PART_RELEASE.equals(partCurrent)) {
		         String[] states = {"state_Create", "state_DefineComponents", "state_DesignWork", "state_Review"};

		         String policyName = chgObj.getInfo(context, DomainConstants.SELECT_POLICY);
		         String objectWhere = getObjectWhere (context, policyName, states);

		         MapList changeList = domObj.getRelatedObjects(context,
		        		 											RELATIONSHIP_AFFECTED_ITEM,
		        		 											DomainConstants.TYPE_ECO,
		        		 											null,
		        		 											null,
		        		 											true,
		        		 											false,
		        		 											(short) 1,
		        		 											objectWhere,
		        		 											null, null, null, null);

		         if (changeList != null && changeList.size() > 0) {
		        	 iCheckTrigPass = 1;
					 //Modified for IR-134422V6R2013 start
		        	 String noticeMessg = "'"+partType+"' '"+partName+"' "+partRev+" ";
		        	 noticeMessg += EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.AlreadyConnected", context.getSession().getLanguage());
		        	 MqlUtil.mqlCommand(context, "notice $1", noticeMessg);
		        	 //Modified for IR-134422V6R2013 end
		         }
	         }
	     }

	     return iCheckTrigPass;
	}

	/** This methods gets all the parent parts connected to it depending upon selected options.
	 * @param context ematrix context
	 * @param args contains arguments
	 * @return MapList
	 * @throws Exception if any exception occurs.
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getPartWhereUsed(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		boolean isWhereUsedPLBOM = false;
		String sWhereUsedPLBOM = (String)programMap.get("whereUsedPLBOM");

		if("true".equalsIgnoreCase(sWhereUsedPLBOM))
			isWhereUsedPLBOM = true;

		String objectId = getStringValue(programMap, "objectId");
		MapList finalListReturn = new MapList();

		String parentId = getStringValue(programMap, "parentId");
		if(parentId!=null && !(parentId.equals(objectId)))
		{
			return finalListReturn;
		}
		if (isValidData(objectId)) {
			MapList endNodeList = null;
			MapList ebomSubstituteList = null;
			MapList spareSubAltPartList = null;
			MapList partWhereUsedEBOMList = null;
			String strCompiledBinaryCode = null;

			String strSelectedFN = null;
			String strSelectedLevel = null;
			String strEffectivityOID = null;
			String strSelectedRefDes = null;
			String strEBOMSubstitute = null;
			String strSelectedRevisions = null;
			String strSelectedLevelValue = null;
			StringList objectSelect = null;
			StringList relSelect  = null;
			String pcFilter  = null;

			String REL_TYPE = DomainConstants.RELATIONSHIP_EBOM+","+RELATIONSHIP_EBOM_PENDING;

			if(isWhereUsedPLBOM){

				String sViewFilter = (String) programMap.get("MFGPlanningMBOMWhereUsedViewFilter");
				strSelectedLevel = getStringValue(programMap, "MFGPlanningWhereUsedLevelCustomFilter");
				strSelectedLevelValue = getStringValue(programMap, "MFGPlanningMBOMWhereUsedLevelText");

				strCompiledBinaryCode = (String)programMap.get("CompiledBinaryCode");

				if (("Current").equalsIgnoreCase(sViewFilter) || UIUtil.isNullOrEmpty(sViewFilter)) {
					REL_TYPE = RELATIONSHIP_PLBOM;
				} else {
					REL_TYPE = RELATIONSHIP_PLBOM +","+RELATIONSHIP_PLBOM_PENDING;
				}

				objectSelect = new StringList(5);
				relSelect = new StringList(8);

				objectSelect.add(DomainConstants.SELECT_ID);
				objectSelect.add(DomainConstants.SELECT_DESCRIPTION);
				objectSelect.add(DomainConstants.SELECT_TYPE);
				objectSelect.add(DomainConstants.SELECT_POLICY);
				objectSelect.add(DomainConstants.SELECT_NAME);
				objectSelect.add(SELECT_PART_MODE);

				relSelect.add(DomainRelationship.SELECT_ID);
				relSelect.add(DomainRelationship.SELECT_TYPE);
				relSelect.add(DomainObject.SELECT_FIND_NUMBER);
				relSelect.add(DomainConstants.SELECT_ATTRIBUTE_QUANTITY);
				relSelect.add(EngineeringConstants.SELECT_PLANT_ID);
				relSelect.add(DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
				relSelect.add(DomainConstants.SELECT_ATTRIBUTE_USAGE);
				relSelect.add(DomainConstants.SELECT_LEVEL);


			}else{
			 strSelectedFN = getStringValue(programMap, "ENCPartWhereUsedFNTextBox");
			 strSelectedLevel = getStringValue(programMap, "ENCPartWhereUsedLevel");
			 strEffectivityOID = getStringValue(programMap, "CFFExpressionFilterInput_actualValue");
			 strSelectedRefDes = getStringValue(programMap, "ENCPartWhereUsedRefDesTextBox");
			 strEBOMSubstitute = getStringValue(programMap, "displayEBOMSub");
			 strSelectedRevisions = getStringValue(programMap, "ENCPartWhereUsedRevisions");
			 strSelectedLevelValue = getStringValue(programMap, "ENCPartWhereUsedLevelTextBox");
			 
			 pcFilter = getStringValue(programMap, "PUEUEBOMProductConfigurationFilter_actualValue");			 

			  objectSelect = createStringList(new String[] {DomainConstants.SELECT_ID, SELECT_PART_MODE, SELECT_RAISED_AGAINST_ECR,
						SELECT_PART_TO_ECR_CURRENT, SELECT_PART_TO_ECO_CURRENT, REL_TO_EBOM_EXISTS, SELECT_RAISED_AGAINST_ECR_CURRENT, POLICY_CLASSIFICATION});

				relSelect = createStringList(new String[] {DomainConstants.SELECT_LEVEL, DomainConstants.SELECT_FIND_NUMBER,
											DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR, DomainRelationship.SELECT_NAME,
											DomainConstants.SELECT_ATTRIBUTE_QUANTITY, DomainRelationship.SELECT_ID,DomainConstants.SELECT_ATTRIBUTE_UNITOFMEASURE});

				strCompiledBinaryCode = getCompiledBinaryCode(context, strEffectivityOID,pcFilter);
			}


			String REV_ALL = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", new Locale("en"),"emxEngineeringCentral.Part.WhereUsedRevisionAll");
			String LEVEL_ALL = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", new Locale("en"),"emxEngineeringCentral.Part.WhereUsedLevelAll");
			String LEVEL_UPTO = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", new Locale("en"),"emxEngineeringCentral.Part.WhereUsedLevelUpTo");
			String LEVEL_HIGHEST = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", new Locale("en"),"emxEngineeringCentral.Part.WhereUsedLevelHighest");
			String REV_LATEST_RELEASED = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", new Locale("en"),"emxEngineeringCentral.Part.WhereUsedRevisionLatestReleased");
			String LEVEL_UPTO_AND_HIGHEST = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", new Locale("en"),"emxEngineeringCentral.Part.WhereUsedLevelUpToAndHighest");
			boolean boolAddEndItemsToList = false;


			boolean isECCInstalled = FrameworkUtil.isSuiteRegistered(context, "appVersionEngineeringConfigurationCentral", false, null, null);

			String objectWhere = "revision == 'last'";

			if (REV_ALL.equals(strSelectedRevisions)) {
				objectWhere = null;

				if (isECCInstalled) {
					String STATE_SUPERSEDED = FrameworkUtil.lookupStateName(context, POLICY_CONFIGURED_PART, "state_Superseded");
					objectWhere = "current != '" + STATE_SUPERSEDED + "'";
				}
			} else if (REV_LATEST_RELEASED.equals(strSelectedRevisions)) {
				objectWhere = "(current == '" + DomainConstants.STATE_PART_RELEASE + "' && (revision == 'last' || next.current != '" + DomainConstants.STATE_PART_RELEASE + "'))";
			}

			Short shRecurseToLevel = 1;
			if (LEVEL_HIGHEST.equals(strSelectedLevel)) {
				shRecurseToLevel = -1;
			} else if (LEVEL_ALL.equals(strSelectedLevel)) {
				shRecurseToLevel = 0;
			} else if ((LEVEL_UPTO.equals(strSelectedLevel) || LEVEL_UPTO_AND_HIGHEST.equals(strSelectedLevel)) && isValidData(strSelectedLevelValue)) {
				shRecurseToLevel = Short.parseShort(strSelectedLevelValue);
			}

			DomainObject domObj = DomainObject.newInstance(context, objectId);

			partWhereUsedEBOMList = domObj.getRelatedObjects(context,
																REL_TYPE,
																DomainConstants.TYPE_PART,
																objectSelect,
																relSelect,
																true,
																false,
																shRecurseToLevel,
																objectWhere,
																null,
																(short) 0,
																false,
																false,
																(short) 0,
																null, null, null, null, strCompiledBinaryCode);

			if (!isValidData(strSelectedRefDes) && !isValidData(strSelectedFN)) {
				spareSubAltPartList = getSpareSubAltPartList(context, domObj, objectSelect, relSelect, objectWhere);
			}

			if (LEVEL_UPTO_AND_HIGHEST.equals(strSelectedLevel)) {
				boolAddEndItemsToList = true;
				endNodeList = domObj.getRelatedObjects(context,
															REL_TYPE,
															DomainConstants.TYPE_PART,
															objectSelect,
															relSelect,
															true,
															false,
															(short) -1,
															objectWhere,
															null,
															(short) 0,
															false,
															false,
															(short) 0,
															null, null, null, null, strCompiledBinaryCode);
            }

            if ("true".equalsIgnoreCase(strEBOMSubstitute)) { // Only if MFG is installed strEBOMSubstitute can be true.
            	//Modified for IR-119203V6R2012x for getting the whereused parts according to the revision filter
            	ebomSubstituteList = getEbomSustituteParts(context, objectId, objectSelect, relSelect, objectWhere);
            }

            finalListReturn = mergeList(partWhereUsedEBOMList, endNodeList, spareSubAltPartList, ebomSubstituteList, boolAddEndItemsToList, strSelectedRefDes, strSelectedFN);
		}
		addRowEditableToList(finalListReturn);
		return finalListReturn;
	}

	private void addRowEditableToList(MapList list) {
		Map map;
		for (Iterator itr=list.iterator();itr.hasNext();) {
			map = (Map)itr.next();
			map.put("RowEditable","false");
		}
	}

	/* This method is called from part where used expand program when user selects related field other than none option.
	 * From URL objectId and selected related option is passed.
	 * This method returns MapList which contains data related to selected option.
	 * @param context ematrix context
	 * @return MapList
	 * @throws Exception if any operation fails
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getWhereUsedRelatedDatas(Context context, String[] args) throws Exception {

		HashMap programMap         = (HashMap) JPO.unpackArgs(args);

        String  objectId           = getStringValue(programMap, "objectId");
        String  strSelectedRelated = getStringValue(programMap, "ENCPartWhereUsedRelated");

    	Part part = new Part(objectId);

    	StringList relSelect = createStringList(new String[] {DomainRelationship.SELECT_ID, DomainRelationship.SELECT_NAME});

        return part.getWhereUsedProductList(context, strSelectedRelated, new StringList(DomainObject.SELECT_ID), relSelect, objectId);
	}

	/** gets all the Spare, Alternate, Substitute parts connected to it.
	 * @param context ematrix context
	 * @param domObj current part domainobject instance
	 * @param objectSelect StringList object select statements
	 * @param relSelect StringList relationship select statements
	 * @param objectWhere where condition to apply on objects
	 * @return MapList
	 * @throws Exception if any exception occurs.
	 */
	private MapList getSpareSubAltPartList(Context context, DomainObject domObj, StringList objectSelect, StringList relSelect, String objectWhere) throws Exception {
		String relSpareSubAlternate = DomainConstants.RELATIONSHIP_ALTERNATE + "," + DomainConstants.RELATIONSHIP_SPARE_PART;

		MapList whereUsedSpareSubAltList = domObj.getRelatedObjects(context,
																		relSpareSubAlternate,
																		DomainConstants.QUERY_WILDCARD,
																		objectSelect,
																		relSelect,
																		true,
																		false,
																		(short) 1,
																		objectWhere,
																		null, null, null, null);

		int size = getListSize(whereUsedSpareSubAltList);
		MapList listReturn = new MapList(size);

		Map map;

		for (int i = 0; i < size; i++) {
			map = (Map) whereUsedSpareSubAltList.get(i);
				listReturn.add(map);
		}

		return listReturn;
	}

	/** This method returns compiled binary code for product effectivity.
	 * @param context ematrix context
	 * @param effectivityOID contains effectivity OID
	 * @return String
	 * @throws Exception if any exception occurs.
	 */
	public String getCompiledBinaryCode(Context context, String effectivityOID) throws Exception {

		if (isValidData(effectivityOID)) {
			Class cl = Class.forName("com.matrixone.apps.effectivity.EffectivityFramework");

	        Object EFF1 = cl.newInstance();

	        Class[] inputType = new Class[3];
	        inputType[0] = Context.class;
	        inputType[1] = String.class;
	        inputType[2] = String.class;

	        Method method = cl.getMethod("getFilterCompiledBinary", inputType);
	        Field fd1 = cl.getDeclaredField("QUERY_MODE_150");

	        String queryMode = (String) fd1.get(EFF1);
	        
	        Object[] actualParams = {context, effectivityOID, queryMode};
	        Map cBinary = (Map) method.invoke(EFF1, actualParams);

	        Field fd = cl.getDeclaredField("COMPILED_BINARY_EXPR");
	        return (String) cBinary.get(fd.get(EFF1));
		}

		return null;
	}
	
	/** This method returns compiled binary code for product effectivity.
	 * @param context ematrix context
	 * @param effectivityOID contains effectivity OID
	 * @return String
	 * @throws Exception if any exception occurs.
	 */
	public String getCompiledBinaryCode(Context context, String effectivityOID, String pcObjectId) throws Exception {
		if (isValidData(pcObjectId)) {
			Class klass = Class.forName("com.matrixone.apps.unresolvedebom.UnresolvedPart");
			
		    Method method = klass.getMethod("getExpressionForPC", new Class[] {Context.class, StringList.class});
		      
		    String foExprOID = (String) method.invoke(klass.newInstance(), new Object[] {context, FrameworkUtil.split(pcObjectId, ",")});
		    
		    if (isValidData(foExprOID)) {
		    	effectivityOID = isValidData(effectivityOID) ? "(" + ((effectivityOID) + ") OR (" + foExprOID) + ")" : foExprOID;
		    }
		}
		
		return getCompiledBinaryCode(context, effectivityOID);
	}

	/** Returns all the parent parts where this object is connected as Substitute.
	 * @param context ematrix context
	 * @param objectId
	 * @param objectSelect selectable to select from object
	 * @param relSelect selectable to select from object
	 * @return MapList
	 * @throws Exception if any exception occurs.
	 */
	public MapList getEbomSustituteParts(Context context, String objectId, StringList objectSelect, StringList relSelect) throws Exception {

		return getEbomSustituteParts(context, objectId, objectSelect, relSelect, null);
	}

	/** Returns all the parent parts where this object is connected as Substitute according to the Revision selected in the revision filter.
	 * @param context ematrix context
	 * @param objectId
	 * @param objectSelect selectable to select from object
	 * @param relSelect selectable to select from object
	 * @return MapList
	 * @throws Exception if any exception occurs.
	 */
	public MapList getEbomSustituteParts(Context context, String objectId, StringList objectSelect, StringList relSelect,String objectWhere) throws Exception {
		String strMqlQuery = "print bus $1 select $2 dump $3";
		String strSubPartIds = MqlUtil.mqlCommand(context, strMqlQuery, objectId, "relationship["+RELATIONSHIP_EBOM_SUBSTITUE+"].fromrel.to.id","|");

		MapList ebomSubList = null;

		if (isValidData(strSubPartIds)) {
			String whereExpr = "frommid[" + RELATIONSHIP_EBOM_SUBSTITUE + "].to.id == " + objectId;

			StringList slSubPartIds = FrameworkUtil.split(strSubPartIds, "|");
			HashSet hSetUniqueId = new HashSet();

			int size = slSubPartIds.size();

			MapList listTemp;
			ebomSubList = new MapList(size);

			DomainObject domObj;
			String parentObjectId;

			for (int i = 0; i < size; i++) {
				parentObjectId = (String) slSubPartIds.get(i);
				if (hSetUniqueId.add(parentObjectId)) {
					domObj = DomainObject.newInstance(context, parentObjectId);

				listTemp = domObj.getRelatedObjects(context,
								DomainConstants.RELATIONSHIP_EBOM,
								DomainConstants.TYPE_PART,
								objectSelect,
								relSelect,
								true,
								false,
								(short) 1,
									objectWhere,
								whereExpr);

				ebomSubList.addAll(listTemp);
				}
			}
		}

		return ebomSubList;
	}

	/* Merges the MapList whereUsed, EndNode, Substitute parts*/
	private MapList mergeList(MapList whereUsedList, MapList endItemList, MapList spareSubAltPartList, MapList ebomSubList, boolean boolAddEndItemsToList, String refDesFilter, String fnFilter) {
		int iWhereUsedListSize = getListSize(whereUsedList);
		int iEndItemListSize   = getListSize(endItemList);
		int iEbomSubListSize   = getListSize(ebomSubList);
		int iSpareSubAltSize   = getListSize(spareSubAltPartList);

        StringList sListEndItemId = getDataForThisKey(endItemList, DomainConstants.SELECT_ID);

		MapList listReturn = new MapList(iWhereUsedListSize);

        Map map;

		String objectId;
		String strLevel;
		String strRelEBOMExists;

		for (int i = 0; i < iWhereUsedListSize; i++) {
			map = (Map) whereUsedList.get(i);

			objectId = getStringValue(map, DomainConstants.SELECT_ID);

            if (isFNAndRefDesFilterPassed(map, refDesFilter, fnFilter)) {

                strLevel = getStringValue(map, "level");
                map.put("objectLevel", strLevel);

                strRelEBOMExists = getStringValue(map, REL_TO_EBOM_EXISTS);
                if ("False".equals(strRelEBOMExists)) {
                    map.put("EndItem", "Yes");
                    sListEndItemId.remove(objectId);
                }
                if ("Unresolved".equals(getStringValue(map, POLICY_CLASSIFICATION))) {
                	map.put("RowEditable", "readonly");
                	map.put("disableSelection", "true");
                }
                listReturn.add(map);
            }
        }

		for (int i = 0; i < iEbomSubListSize; i++) {
			map = (Map) ebomSubList.get(i);

        	if (isFNAndRefDesFilterPassed(map, refDesFilter, fnFilter)) {
                strLevel = getStringValue(map, "level");

                map.put("objectLevel", strLevel);
                map.put("relationship", RELATIONSHIP_EBOM_SUBSTITUE);

                listReturn.add(map);
        	}
        }

		if (boolAddEndItemsToList) {
            for (int i = 0; i < iEndItemListSize; i++) {
                map = (Map) endItemList.get(i);
                objectId = getStringValue(map, DomainConstants.SELECT_ID);

            	if (sListEndItemId.contains(objectId) && isFNAndRefDesFilterPassed(map, refDesFilter, fnFilter)) {
                    if ("Unresolved".equals(getStringValue(map, POLICY_CLASSIFICATION))) {
                    	map.put("RowEditable", "readonly");
                    	map.put("disableSelection", "true");
                    }
            		
                    map.put("EndItem", "Yes");
                    listReturn.add(map);
                }
            }
        }

		for (int i = 0; i < iSpareSubAltSize; i++) {
			map = (Map) spareSubAltPartList.get(i);

            strLevel = getStringValue(map, "level");
            map.put("objectLevel", strLevel);

            listReturn.add(map);
        }

		return listReturn;
	}

	/** If user has entered some valid value in Ref Des OR Findnumber it compares with attributes exists in map and if both are same only then it returns true,
	 * if both findNumber, refDes == NUll or "", then this method returns true.
	 * @param map contains properties of object like id, name, rel attributes, object attributes.
	 * @param refDes value given by user from UI in Reference Designator textfield.
	 * @param findNumber value given by user from UI in Find Number textfield.
	 * @return boolean.
	 */
	private boolean isFNAndRefDesFilterPassed(Map map, String refDes, String findNumber) {
		boolean boolRefDesFilterPass = true;
		boolean boolFNFilterPass = true;

		String strRefDes = getStringValue(map, DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
		String strFindNumber = getStringValue(map, DomainConstants.SELECT_ATTRIBUTE_FIND_NUMBER);

		if (isValidData(refDes) && !refDes.equals(strRefDes)) {
            boolRefDesFilterPass = false;
        }

        if (isValidData(findNumber) && !findNumber.equals(strFindNumber)) {
            boolFNFilterPass = false;
        }

		return (boolRefDesFilterPass && boolFNFilterPass);
	}

	/* Iterates through MapList and Returns the values in StringList depending upon key */
	private StringList getDataForThisKey(MapList list, String key) {
		int size = getListSize(list);

		StringList listReturn = new StringList(size);

		String strTemp;

		for (int i = 0; i < size; i++) {
			strTemp = (String) ((Map) list.get(i)).get(key);
			if (!isValidData(strTemp)) {
				strTemp = "";
			}
			listReturn.addElement(strTemp);
		}

		return listReturn;
	}

	/* Iterates through MapList and Returns the values in StringList depending upon key with specific to context language*/
	private StringList getDataForThisKey(Context context, MapList list, String key, String strAdminType) throws Exception {
		int size = getListSize(list);

		StringList listReturn = new StringList(size);

		String strTemp;

		for (int i = 0; i < size; i++) {
			strTemp = (String) ((Map) list.get(i)).get(key);
			if (isValidData(strTemp)) {
				listReturn.add(UINavigatorUtil.getAdminI18NString(strAdminType, strTemp, context.getSession().getLanguage()));
			} else {
				listReturn.addElement("");
			}
		}

		return listReturn;
	}

	/* Returns StringList containing values of  partmode to display in table*/
	public StringList getPartModeForPartWhereUsed(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList list = (MapList) programMap.get("objectList");
		int size = getListSize(list);

		StringList listReturn = new StringList(size);
        StringList listPartMode;

		String strPartMode;

		for (int i = 0; i < size; i++) {
			/*Modified to get the PartMode attributes as StringList, as the displayed Part may be connected more than once to the Part Master Object
			(When the Part displayed in the whereused is connected to a Plant with  Manufacturing Responsibility Relationship and is in the Release state, the Part is connected more than once with the Part Master and the Part Mode attributes returned will be StringList.)*/
			listPartMode = getListValue((Map) list.get(i), SELECT_PART_MODE);
			if (listPartMode.size() > 0) {
				//Multitenant
				strPartMode = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Part_Mode." + listPartMode.get(0)); //Modified for IR-204622
	        } else {
	        	strPartMode = "";
	        }
			listReturn.addElement(strPartMode);
		}

		return listReturn;
	}

	/* Returns StringList containing values of Findnumbers to display in table*/
	public StringList getFNForPartWhereUsed(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList list = (MapList) programMap.get("objectList");

		return getDataForThisKey(list, DomainConstants.SELECT_FIND_NUMBER);
	}

	/* Returns StringList containing values of References Designator to display in table*/
	public StringList getRefDesForPartWhereUsed(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList list = (MapList) programMap.get("objectList");

		return getDataForThisKey(list, DomainConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
	}

	/* Returns StringList containing values of Quantity to display in table*/
	public StringList getQuantityForPartWhereUsed(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList list = (MapList) programMap.get("objectList");

		return getDataForThisKey(list, DomainConstants.SELECT_ATTRIBUTE_QUANTITY);
	}
	
	/* Returns StringList containing values of Quantity to display in table*/
	public StringList getUOMForPartWhereUsed(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList list = (MapList) programMap.get("objectList");

		return getDataForThisKey(list, DomainConstants.SELECT_ATTRIBUTE_UNITOFMEASURE);
	}

	/* Returns StringList containing values of Relationship name to display in table*/
	public StringList getRelNameForPartWhereUsed(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList list = (MapList) programMap.get("objectList");

		return getDataForThisKey(context, list, "relationship", "Relationship");
	}

	/* Returns StringList containing values of Active ECR Icon to display in table*/
	private StringList getActveECRIcon(Context context, MapList list, boolean boolConcatName) throws Exception {
		int size = getListSize(list);

		StringList listReturn = new StringList(size);

		String strActveECRIconTag;
		String strImageTag = "<img src=\"../common/images/iconSmallECRO.gif\" border=\"0\" align=\"middle\" alt=\"" +
			EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.ActiveECRorECO", context.getSession().getLanguage()) + "\"/>";

		Map map;

		String activECOState;
        try {
        	activECOState = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ActiveECOStates");
        } catch (Exception e) {
        	activECOState = "state_Create,state_DefineComponents,state_DesignWork,state_Review";
        }

        String activECRState;
    	try {
			activECRState = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ActiveECRStates");
        } catch (Exception e) {
        	activECRState = "state_Create,state_Submit,state_Evaluate,state_Review,state_PlanECO";
        }

        StringList activeECOList = new StringList();
        StringList activeECRList = new StringList();

        if (isValidData(activECOState)) {
        	activeECOList = getActualStateNameList(context, FrameworkUtil.split(activECOState, ","), POLICY_ECO);
        }

        if (isValidData(activECRState)) {
        	activeECRList = getActualStateNameList(context, FrameworkUtil.split(activECRState, ","), POLICY_ECR);
        }

		for (int i = 0; i < size; i++) {
			map = (Map) list.get(i);

			strActveECRIconTag = "";

			if (boolConcatName) {
				strActveECRIconTag = getIconWithName(getListValue(map, SELECT_RAISED_AGAINST_ECR), "../common/images/iconSmallECRO.gif");
			} else {
				boolean boolHasActiveECRORECO = hasActiveECRECO(getListValue(map, SELECT_PART_TO_ECO_CURRENT), getListValue(map, SELECT_PART_TO_ECR_CURRENT), activeECOList, activeECRList);
				if (!boolHasActiveECRORECO) {
					boolHasActiveECRORECO = hasActiveECRECO(new StringList(), getListValue(map, SELECT_RAISED_AGAINST_ECR_CURRENT), new StringList(), activeECRList);
				}
				if (boolHasActiveECRORECO) {
					strActveECRIconTag = strImageTag;
				}
			}

			listReturn.addElement(strActveECRIconTag);
		}

		return listReturn;
	}

	private StringList getActualStateNameList(Context context, StringList symStateNameList, String policy) throws Exception {
		StringList listReturn = new StringList();
		String stateName;

		for (int i = 0; i < symStateNameList.size(); i++) {
			try {
				stateName = FrameworkUtil.lookupStateName(context, policy, ((String) symStateNameList.get(i)).trim());
				listReturn.add(stateName);
			} catch (Exception ex) {}
		}

		return listReturn;
	}

	/*
	 * Checks wheather active ECR OR ECO Exists.
	 * If exists returns true else returns false.
	 * */
	public boolean hasActiveECRECO(StringList ecoListCurrent, StringList ecrListCurrent, StringList activECOList, StringList activECRList) throws Exception {
		boolean activeECRECOExists = false;

    	for (int i = 0; i < ecoListCurrent.size(); i++) {
    		if (activECOList.contains(ecoListCurrent.get(i))) {
        		activeECRECOExists = true;
        		break;
    		}
    	}

        if (!activeECRECOExists) {
        	for (int i = 0; i < ecrListCurrent.size(); i++) {
        		if (activECRList.contains(ecrListCurrent.get(i))) {
	        		activeECRECOExists = true;
	        		break;
        		}
        	}
        }

		return activeECRECOExists;
	}

	/* Returns StringList containing values of Active ECR Icon with name to display in table*/
	private String getIconWithName(StringList list, String imageSrc) {
		int size = getListSize(list);
		StringBuffer sbTagReturn = new StringBuffer(size * 50);
		String imageTag = "<img src=\"" + imageSrc + "\"> ";

		for (int i = 0; i < size; i++) {
			if (i > 0) {
				sbTagReturn.append("<br/>");
			}
			sbTagReturn.append(imageTag).append((String) list.get(i)).append("</img>");
		}

		return sbTagReturn.toString();
	}

	private StringList getListValue(Map map, String key) {
		Object data = map.get(key);
		if (data == null)
			return new StringList(0);
		return (data instanceof String) ? new StringList((String) data) : (StringList) data;
	}

	/* Returns StringList containing values of Active ECR Icon to display in table*/
	public StringList getActveECRIconForPartWhereUsed(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList list = (MapList) programMap.get("objectList");

		return getActveECRIcon(context, list, false);
	}

	/* Returns StringList containing values of Active ECR Icon with name to display in table*/
	public StringList getActveECRIconWithNameForPartWhereUsed(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList list = (MapList) programMap.get("objectList");

		return getActveECRIcon(context, list, true);
	}

	/* Returns StringList containing values of Level with name to display in table*/
	public StringList getLevelForPartWhereUsed(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map paramMap = (Map) programMap.get("paramList");
		Map map;

		MapList list = (MapList) programMap.get("objectList");

		int size = getListSize(list);
		StringList listReturn = new StringList(size);

		String strLevel;
		String strRelName;
		String strSelectedLevel = getStringValue(paramMap, "ENCPartWhereUsedLevel");
		String propShowNegSymb = "true";

		try {
			String strDefaultPropValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.ShowNegativeInWhereUsed");
			if (isValidData(strDefaultPropValue)) {
				propShowNegSymb = strDefaultPropValue;
			}
		} catch (Exception e) {}

		for (int i = 0; i < size; i++) {
			map = (Map) list.get(i);
			strLevel = getStringValue(map, "objectLevel");
			strRelName = getStringValue(map, "relationship");

			if ("Highest".equals(strSelectedLevel) || (isValidData(strRelName) && (strRelName.equalsIgnoreCase(DomainConstants.RELATIONSHIP_PART_SPECIFICATION) ||
					strRelName.equalsIgnoreCase(RELATIONSHIP_ASSIGNED_PART) || strRelName.equalsIgnoreCase(RELATIONSHIP_GBOM) || strRelName.equalsIgnoreCase(RELATIONSHIP_PRECISE_BOM)))) {
				strLevel = "";
			} else if (strLevel == null) {
				strLevel = ("true".equalsIgnoreCase(propShowNegSymb)) ? "-" : "";
			} else {
				strLevel = ("true".equalsIgnoreCase(propShowNegSymb)) ? ("-" + strLevel) : strLevel;
			}

			listReturn.add(strLevel);
		}

		return listReturn;
	}

	/* Returns HashMap containing values of Levels in English and Context lang to display in filter*/
	public HashMap getLevelFilterForPartWhereUsed(Context context, String[] args) throws Exception {
		String[] levelOptions = {"emxEngineeringCentral.Part.WhereUsedLevelUpTo", "emxEngineeringCentral.Part.WhereUsedLevelAll",
				"emxEngineeringCentral.Part.WhereUsedLevelHighest", "emxEngineeringCentral.Part.WhereUsedLevelUpToAndHighest"};

		HashMap levelMap = new HashMap(2);
		levelMap.put("field_choices", getValueListFromProperties(context,levelOptions, "emxEngineeringCentralStringResource", "en"));
		levelMap.put("field_display_choices", getValueListFromProperties(context,levelOptions, "emxEngineeringCentralStringResource", context.getSession().getLanguage()));

		return levelMap;
	}

	/* Returns HashMap containing values of Revisions in English and Context lang to display in filter*/
	public HashMap getRevisionsFilterForPartWhereUsed(Context context, String[] args) throws Exception {
		String[] revisionOptions = {"emxEngineeringCentral.Part.WhereUsedRevisionLatest",
										"emxEngineeringCentral.Part.WhereUsedRevisionLatestReleased",
										"emxEngineeringCentral.Part.WhereUsedRevisionAll"};

		HashMap revisionMap = new HashMap(2);
		revisionMap.put("field_choices", getValueListFromProperties(context,revisionOptions, "emxEngineeringCentralStringResource", "en"));
		revisionMap.put("field_display_choices", getValueListFromProperties(context,revisionOptions, "emxEngineeringCentralStringResource", context.getSession().getLanguage()));

		return revisionMap;
	}

	/* Returns HashMap containing values of Related Products in English and Context lang to display in filter*/
	public HashMap getRelatedFilterForPartWhereUsed(Context context, String[] args) throws Exception {
		boolean isPrdCentralInstalled = FrameworkUtil.isSuiteRegistered(context, "appVersionVariantConfiguration", false, null, null);

		String[] relatedOptions;

		if (isPrdCentralInstalled) {
			relatedOptions = new String[7];

			relatedOptions[0] = "emxEngineeringCentral.Part.WhereUsedProductsNone";
			relatedOptions[1] = "emxEngineeringCentral.Related.Specification";
			relatedOptions[2] = "emxEngineeringCentral.Part.WhereUsedProductsAll";
			relatedOptions[3] = "emxEngineeringCentral.Part.WhereUsedProductsFeatures";
			relatedOptions[4] = "emxEngineeringCentral.Part.WhereUsedProductsProducts";
			relatedOptions[5] = "emxEngineeringCentral.Part.WhereUsedProductsBuilds";
			relatedOptions[6] = "emxEngineeringCentral.Part.WhereUsedProductsProductConfiguration";
		} else {
			relatedOptions = new String[2];

			relatedOptions[0] = "emxEngineeringCentral.Part.WhereUsedProductsNone";
			relatedOptions[1] = "emxEngineeringCentral.Related.Specification";
		}

		HashMap relatedFieldMap = new HashMap(2);
		relatedFieldMap.put("field_choices", getValueListFromProperties(context,relatedOptions, "emxEngineeringCentralStringResource", "en"));
		relatedFieldMap.put("field_display_choices", getValueListFromProperties(context,relatedOptions, "emxEngineeringCentralStringResource", context.getSession().getLanguage()));

		return relatedFieldMap;
	}

	/* Returns StringList containing values of EndNOdeIcon tag to display in filter*/
	public StringList getEndNodeIcon(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		MapList list = (MapList) programMap.get("objectList");
		int size = getListSize(list);
		StringList listReturn = new StringList(size);
		Map map;

		String strEndItem;
		String strTemp;

		String strImageTag = "<img src='../common/images/iconSmallEndNode.gif' border='0'/>";

		for (int i = 0; i < size; i++) {
			map = (Map) list.get(i);
			strEndItem = getStringValue(map, "EndItem");
			strTemp = "";

			if ("Yes".equals(strEndItem)) {
				strTemp = strImageTag;
			}

			//listReturn.add(XSSUtil.encodeForHTML(context, strTemp));
			listReturn.add(strTemp);
		}

		return listReturn;
	}

	/**
	 * To show/hide the No of Parts field in the create part page.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean displayFieldNoOfParts(Context context, String args[]) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String multiObjectCreate = (String) programMap.get("multiPartCreation");
		return ("true".equalsIgnoreCase(multiObjectCreate));
	}

	   /**
	    * Added for JSP to Common components conversion. Specification ->Related Parts
	    * @param context
	    * @param args
	    * @return
	    * @throws Exception
	    */
	   @com.matrixone.apps.framework.ui.ProgramCallable
	   public MapList getSpecRelatedParts(Context context, String[] args)   throws Exception
	   {
		   HashMap programMap = (HashMap) JPO.unpackArgs(args);
		   String strObjId = (String) programMap.get("objectId");
		   DomainObject specObj = new DomainObject(strObjId);

	 	   String relPattern = PropertyUtil.getSchemaProperty(context,"relationship_PartSpecification");
		   String typePattern = PropertyUtil.getSchemaProperty(context,"type_Part");

			MapList objMapList = new MapList();

			try{
			       SelectList selectRelStmts = new SelectList(1);
			       selectRelStmts.addElement(DomainRelationship.SELECT_ID);

			       SelectList selectStmts = new SelectList(1);
			       selectStmts.addElement(DomainObject.SELECT_ID);

					ContextUtil.startTransaction(context,false);

					objMapList = FrameworkUtil.toMapList(specObj.getExpansionIterator(context, relPattern, typePattern,
					        selectStmts, selectRelStmts, true, true, (short)1,
					                null, null, (short)0,
					                false, false, (short)1, false),
					                (short)0, null, null, null, null);

					ContextUtil.commitTransaction(context);

			}catch(Exception exp){
                exp.printStackTrace();
                ContextUtil.abortTransaction(context);
			}


	   	return objMapList ;
	   }

	/**
     * This method displays the connected Spare Parts List to a Part
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns MapList Spare Parts list
     * @throws Exception if the operation fails
     * @since R212
     */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSparePartsForExpand(Context context, String[] args) throws Exception {

		 HashMap programMap = (HashMap) JPO.unpackArgs(args);

		 String objectId = (String) programMap.get("objectId");
		 String parentId=(String) programMap.get("parentId");

		 MapList sparePartMapList = new MapList();
		 if(null != parentId) {
			 return sparePartMapList ;
		 }

		 try {
		     Part partObj = new Part(objectId);
		     StringList selectStmts = new StringList(1);
		     selectStmts.addElement(SELECT_ID);

			 StringList selectRelStmts = new StringList(1);
			 selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

			 sparePartMapList = FrameworkUtil.toMapList(partObj.getExpansionIterator(context, DomainConstants.RELATIONSHIP_SPARE_PART, "*",
													selectStmts, selectRelStmts, false, true, (short)1,
													null, null, (short)0,
													false, false, (short)0, false),
													(short)0, null, null, null, null);


			 sparePartMapList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
			 sparePartMapList.sort();

		 } catch (FrameworkException Ex){
		      throw Ex;
		 }
		 return sparePartMapList ;
      }

	 /* To create the part object from create component
	 *
	 * @param context
	 * @param args
	 * @return Map
	 * @throws Exception
	 * @Since R211
	 */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public Map revisePartJPO(Context context, String[] args) throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);

		String strPartId = (String) programMap.get("copyObjectId");
		String sCustomRevisionLevel = (String) programMap.get("CustomRevisionLevel");
		String sVault = (String) programMap.get("lastRevVault");

		Map returnMap = new HashMap();

		Part part = new Part(strPartId);
		DomainObject nextRev = new DomainObject(part.revisePart(context, sCustomRevisionLevel, sVault, true, false));
		returnMap.put("id", nextRev.getId(context));

		return returnMap;
	}

	/**
	 * To populate the default Revision value with respect to the Policy selected in the Create Part page
	 *
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 * @Since R211
	 */
	public Map updateCustomeRevisionField(Context context, String[] args) throws Exception {
		Map argMap = (Map)JPO.unpackArgs(args);
        Map fieldValues = (Map)argMap.get("fieldValues");
        Map requestMap = (Map)argMap.get("requestMap");
        String reviseAction = (String)requestMap.get("reviseAction");
        String revision = "";
        String policyStr = (String) fieldValues.get("Policy");
        String sType = (String) fieldValues.get("TypeActual");
        //Added for BGTP ---Start
		String sReleasePhase = (String) fieldValues.get("ReleaseProcess");
		if(UIUtil.isNullOrEmpty(policyStr)){
			policyStr = ReleasePhase.getPolicy(context, sType, sReleasePhase);
			if(EngineeringConstants.DEVELOPMENT.equalsIgnoreCase(sReleasePhase) && EngineeringConstants.POLICY_EC_PART.equalsIgnoreCase(policyStr))
				policyStr = EngineeringConstants.POLICY_DEVELOPMENT_PART;
		}
		if(reviseAction != null && "true".equalsIgnoreCase(reviseAction)) {
        	String lastRevVault = (String)requestMap.get("lastRevVault");
        	String lastRevPolicy = (String)requestMap.get("lastRevPolicy");
        	revision = (String)requestMap.get("nextRev");
        	String sName = (String)fieldValues.get("Name");
        	//String sType = (String) fieldValues.get("TypeActual");

        	if(policyStr != null && lastRevPolicy != null && !policyStr.equals(lastRevPolicy)) {
            	Policy policyObjNew = new Policy(policyStr);
            	revision = policyObjNew.getFirstInSequence(context);
        	    BusinessObject devBusObj = new BusinessObject(sType, sName, revision, lastRevVault);
        	    if(devBusObj.exists(context))
        	    	revision = devBusObj.getNextSequence(context);
        	}
        } else {
	        Policy policy = new Policy(policyStr);
	        revision = policy.getFirstInSequence(context);
        }

        Map fieldMap = new HashMap();
        fieldMap.put("SelectedValues", revision);
        fieldMap.put("SelectedDisplayValues", revision);
        return fieldMap;

	}

	/**
	 * Method to find the My View objects
	 * @param context
	 * @param args
	 * @return MapList
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList findMyViewObjects(Context context, String[] args)throws Exception {
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		String selType = (String) paramMap.get("type");
		String sFromExportToExcel =  (String) paramMap.get("sFromExportToExcel");
		String defaultInitialQueryLimit =EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.InitialLoad.QueryLimit");
		boolean isExaleadSearch = UISearchUtil.isAutonomySearch(context,paramMap);
		StringList objectSelects = new StringList();
		objectSelects.add(DomainConstants.SELECT_ID);
		objectSelects.add("originated");
		MapList maplist = new MapList();
		//Create Search Refinement for Exalead Search and where expression for Real Time search
		ComplexRefinement lRefnSearch = null;		
		String whereExpression = "";
		//The below search parameters are set from Show Search Criteria
		String searchType = (String) paramMap.get("Type");
		String searchName = (String) paramMap.get("Name");
		String searchDescription = (String) paramMap.get("Description");
		String searchQueryLimit = (String) paramMap.get("QueryLimit");
		String initialQueryLimit = (String) paramMap.get("initialQueryLimit");	
		HashMap usercontext= EngineeringUtil.getContextUser(context);

		if("True".equals(sFromExportToExcel)){
			selType =  (String) paramMap.get("sSelType");
		}		
		String sXBOMApp = (String) paramMap.get("XBOMApp");
		sXBOMApp = (null != sXBOMApp)?sXBOMApp:"";
		if("MFG".equals(sXBOMApp)) {
			if("Part".equals(selType)) {
				selType = EngineeringConstants.TYPE_MANUFACTURING_PART;
			} else if("Change".equals(selType)) {
				selType = EngineeringConstants.TYPE_MECO+","+EngineeringConstants.TYPE_MCO+","+EngineeringConstants.TYPE_DCR;
			}
		}		

		searchType = UIUtil.isNullOrEmpty(searchType)? selType : searchType;
		searchName = UIUtil.isNullOrEmpty(searchName)? QUERY_WILDCARD : searchName;
		if(UIUtil.isNullOrEmpty(searchQueryLimit)){
			searchQueryLimit = UIUtil.isNullOrEmpty(initialQueryLimit)? defaultInitialQueryLimit : initialQueryLimit;
		}

		if(!"Change".equals(searchType)) {
			if(isExaleadSearch){
				lRefnSearch=(ComplexRefinement) EngineeringUtil.getWhereExpressionForPnO(context, usercontext, isExaleadSearch);
			} else {
				whereExpression=(String) EngineeringUtil.getWhereExpressionForPnO(context, usercontext, isExaleadSearch);
			}

		}	
		if(UIUtil.isNotNullAndNotEmpty(searchDescription) && !"*".equals(searchDescription)){
			lRefnSearch.addRefinement(new AttributeRefinement(DomainConstants.SELECT_DESCRIPTION, searchDescription, AttributeRefinement.OPERATOR_EQUAL));
		}

		if(!("MFG".equals(sXBOMApp))){
			if("Part".equals(searchType)) {				
				if(isExaleadSearch){
					lRefnSearch=(ComplexRefinement)EngineeringUtil.getPartWhereExpression(context, whereExpression, lRefnSearch,searchType, isExaleadSearch);
				}else {
					whereExpression=(String) EngineeringUtil.getPartWhereExpression(context, whereExpression, null ,searchType, isExaleadSearch);
				}

			}
			else if("Change".equals(searchType)) {			 
				String prefdays = PropertyUtil.getAdminProperty(context, TYPE_PERSON, context.getUser(), "preference_MyViewPreference");
				if("".equals(prefdays)){
					prefdays = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.MyView.DefaultNoOfDays");
				}
				int days=Integer.parseInt(prefdays);
				String dateNumofDaysBack=getDateAfter(days);
				if(isExaleadSearch){
					lRefnSearch=(ComplexRefinement)EngineeringUtil.getChangeWhereExpression(context, whereExpression, searchType, dateNumofDaysBack, isExaleadSearch);
				} else {
					whereExpression=(String) EngineeringUtil.getChangeWhereExpression(context, whereExpression, searchType, dateNumofDaysBack, isExaleadSearch);
				}

			}
		}

		if (!(searchType.indexOf(DomainConstants.TYPE_CAD_MODEL) == -1)) {
			Map typeInfo = getTypesBasedOnVersion(context, EngineeringUtil.getPartSpecificationTypes(context));
			if(isExaleadSearch){
				//Fetch data related to version types by applying is version object where condition
				ComplexRefinement newlRefnSearch2 = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_AND);
				TaxonomyRefinement lRefnParts1 = new TaxonomyRefinement("TYPES", (String)typeInfo.get("versionTypes"), true);
				newlRefnSearch2.addRefinement(lRefnParts1);
				newlRefnSearch2.addRefinement(new AttributeRefinement("IS_VERSION_OBJECT", "False", AttributeRefinement.OPERATOR_EQUAL));
				newlRefnSearch2.addRefinement(new AttributeRefinement(DomainConstants.SELECT_POLICY, "Version", AttributeRefinement.OPERATOR_NOT_EQUAL));
				
				
				ComplexRefinement newlRefnSearch = new ComplexRefinement(ComplexRefinement.LOGICAL_OPERATOR_OR);
				TaxonomyRefinement lRefnParts = new TaxonomyRefinement("TYPES", (String)typeInfo.get("nonVersionTypes"), true);
				newlRefnSearch.addRefinement(lRefnParts);
				newlRefnSearch.addRefinement(newlRefnSearch2);
				lRefnSearch.addRefinement(newlRefnSearch);
				maplist=exaleadResult(context,lRefnSearch,searchQueryLimit,null,searchName);
				return maplist;
			}
			else if (UIUtil.isNotNullAndNotEmpty(searchType)) {
					//Need to differentiate the types between version/non version based in order to apply where condition			
					//Fetch data related to non version types with out applying is version object where condition
					maplist      = fetchObjectsWithLimit(context, args, (String)typeInfo.get("nonVersionTypes"), whereExpression, objectSelects);

					whereExpression+="&&attribute["+DomainConstants.ATTRIBUTE_IS_VERSION_OBJECT+"]==False";
					//Fetch data related to version types by applying is version object where condition
					maplist.addAll(fetchObjectsWithLimit(context, args, (String)typeInfo.get("versionTypes"), whereExpression, objectSelects));
					return maplist;
				}
		}
		if(searchType.equals(CommonDocument.TYPE_DOCUMENTS)){
			if(isExaleadSearch){
				lRefnSearch=(ComplexRefinement)EngineeringUtil.getReferenceDocumentWhereExpression(context, whereExpression,lRefnSearch, searchType, isExaleadSearch);
			}else{
				whereExpression=(String) EngineeringUtil.getReferenceDocumentWhereExpression(context, whereExpression,null, searchType, isExaleadSearch);
			}
		}
		try{
			if(isExaleadSearch) {				
				maplist=exaleadResult(context,lRefnSearch,searchQueryLimit,searchType,searchName);
			}
			else{
				maplist = fetchObjectsWithLimit(context, args, selType, whereExpression, objectSelects);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new MatrixException(e);
		}		
		return maplist;
	}
	
	//This api is added for the purpose of showing VPM specific data in My engineering view > Specifications tab
	private Map getTypesBasedOnVersion(Context context,StringList types) throws Exception {
		Map map 				   = new HashMap();
		StringList versionTypes    = new StringList();
		StringList nonVersionTypes = new StringList();
		int typeSize			   = types != null ? types.size() : 0;
		String isVersionType;
		for (int i=0; i < typeSize; i++) {			
			isVersionType = MqlUtil.mqlCommand(context, "print type $1 select $2 dump", (String)types.get(i),"attribute[Is Version Object]");						
			if("True".equalsIgnoreCase(isVersionType)) { versionTypes.add(types.get(i)); }
			else { nonVersionTypes.add(types.get(i)); }
		}
		map.put("versionTypes", versionTypes.size() > 0 ? versionTypes.join(",") : "");
		map.put("nonVersionTypes", nonVersionTypes.size() > 0 ? nonVersionTypes.join(EngineeringConstants.COMMA) : "");
		return map;
	}
	
	
	/**
	 * Method to find the My View Reference Documents
	 * @param context
	 * @param args
	 * @return MapList
	 * @throws Exception
	 */
	
	/*public MapList findMyViewRefDocuments(Context context, String[] args)throws Exception {
		
		String prefdays = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.MyView.DefaultNoOfDays");
		String relRefenceDocument = PropertyUtil.getSchemaProperty(context, CommonDocument.SYMBOLIC_relationship_ReferenceDocument);
		
		String whereExpression = new StringBuilder(SELECT_OWNER)
									.append("=='")
									.append(context.getUser())
									.append("' && ")
									.append(SELECT_MODIFIED)
									.append(" >='")
									.append(getDateAfter(Integer.parseInt(prefdays)))
		                            .append("' && to[")
		                            .append(relRefenceDocument)
		                            .append("]=='True'").toString();
		
		MapList docList = DomainObject.findObjects(context,
				CommonDocument.TYPE_DOCUMENTS, // typepattern 
				QUERY_WILDCARD, // namepattern
				QUERY_WILDCARD, // revpattern
				QUERY_WILDCARD, // owner pattern
				QUERY_WILDCARD, // vault pattern
				whereExpression, // where exp
				true, 
				new StringList(SELECT_ID)); //ObjSelect
		
		return docList;
	}*/
	
		public MapList fetchObjectsWithLimit(Context context, String[] args, String sSelType, String whereExpression,  StringList objectSelects)throws Exception {
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		String searchType = (String) paramMap.get("Type");
		String searchName = (String) paramMap.get("Name");
		String searhDescription = (String) paramMap.get("Description");
		String searchQueryLimit = (String) paramMap.get("QueryLimit");
		String initialQueryLimit = (String) paramMap.get("initialQueryLimit");
		if(UIUtil.isNullOrEmpty(searchType))
			searchType = sSelType;
		if(UIUtil.isNullOrEmpty(searchName))
			searchName = QUERY_WILDCARD;
		if(UIUtil.isNullOrEmpty(searchQueryLimit)){
			if(UIUtil.isNullOrEmpty(initialQueryLimit))
				searchQueryLimit = "0";
			else
				searchQueryLimit = initialQueryLimit;
		}
		if(UIUtil.isNotNullAndNotEmpty(searhDescription) && !"*".equals(searhDescription)){
			whereExpression = whereExpression + "&& description ~~ \""+searhDescription+"\"";
			}
	/*	MapList maplist= DomainObject.findObjects(context, searchType, searchName, // namepattern
				QUERY_WILDCARD, // revpattern
				"*", // owner pattern
				QUERY_WILDCARD, // vault pattern
				whereExpression, // where exp
				true, objectSelects);
		*/
		
		MapList ObjectList = DomainObject.findObjects(context, searchType, // type pattern
																searchName, // name pattern
															QUERY_WILDCARD, // rev Pattern
															QUERY_WILDCARD, // owner Pattern
															QUERY_WILDCARD, // vault Pattern - should be passed in
															whereExpression,null, true, objectSelects,(short)Integer.parseInt(searchQueryLimit),null,null);
		return ObjectList;
	}

	//######################################ENG WIDGETS-START#####################

	/**
	 * This method returns ENG/XCE Parts for WIDGETS which are owned or originated by context user based on days preference
	 * @param context
	 * @param args
	 * @return MapList
	 * @throws Exception
	 */
	public MapList getMyEngineeringPartsForWidget(Context context, String[] args)throws Exception {
		Map<String, Object> programMap = (Map<String, Object>) JPO.unpackArgs(args);
        Map jpoArgs = (Map) programMap.get(UIWidget.JPO_ARGS);
        String selType       = (String) jpoArgs.get("type");
        String functionality = (String) jpoArgs.get("functionality");
        String excludeTypes  =  (String) jpoArgs.get("excludeTypes");
        StringList excludeTypeList  =  FrameworkUtil.split(excludeTypes, ",");

        MapList mapList = getMyPartsForWidget(context,selType,functionality,excludeTypeList);
		loadStateIndexCache(context,mapList);
		return mapList;
	}

	public MapList getMyPartsForWidget(Context context, String selType,String functionality,StringList excludeTypes)throws Exception {

		MapList finalList = new MapList();
		String loggedInUser= context.getUser();
        String prefDays      = PropertyUtil.getAdminProperty(context, TYPE_PERSON, context.getUser(), "preference_MyViewPreference");
		if("".equals(prefDays)){
			prefDays = EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.MyView.DefaultNoOfDays");
		}
	    int days=Integer.parseInt(prefDays);
		String dateNumofDaysBack=getDateAfter(days);
		HashMap usercontext= EngineeringUtil.getContextUser(context); 
		String whereExpression = (String)EngineeringUtil.getWhereExpressionForPnO(context,usercontext,false);
		if("ENG".equalsIgnoreCase(functionality)) {
			boolean isECCInstalled = FrameworkUtil.isSuiteRegistered(context, "appVersionEngineeringConfigurationCentral", false, null, null);
			try {
				if (isECCInstalled) {
		            ComponentsUtil.checkLicenseReserved(context,"ENO_XCE_TP");
				}
			} catch (Exception ecc) {
				whereExpression+="&& policy!=\""+POLICY_CONFIGURED_PART+"\"";
			}
		}
		if (!(selType.indexOf(DomainConstants.TYPE_CAD_MODEL) == -1)) {
			whereExpression+="&&attribute["+DomainConstants.ATTRIBUTE_IS_VERSION_OBJECT+"]==False";
		}
		String POLICY_CONFIGURED_PART = PropertyUtil.getSchemaProperty(context,"policy_ConfiguredPart");
	    String STATE_PART_SUPERSEDED =PropertyUtil.getSchemaProperty(context,"policy",POLICY_CONFIGURED_PART,"state_Superseded");
		whereExpression += " && "+SELECT_CURRENT+" != '"+STATE_PART_OBSOLETE+"' && "+SELECT_CURRENT+" != '"+STATE_PART_SUPERSEDED+"'";
		String SELECT_TARGET_RELEASE_DATE = "attribute[" +  PropertyUtil.getSchemaProperty(context,"attribute_EffectivityDate") + "]";

		StringList objectSelects = new StringList();
		objectSelects.add(DomainConstants.SELECT_ID);
		objectSelects.add(DomainConstants.SELECT_CURRENT);
		objectSelects.add(DomainConstants.SELECT_TYPE);
		objectSelects.add(DomainConstants.SELECT_POLICY);
		objectSelects.add(SELECT_TARGET_RELEASE_DATE);
		MapList mapList= DomainObject.findObjects(context, selType,
															QUERY_WILDCARD,  // namepattern
															QUERY_WILDCARD,  // revpattern
															"*", 			 // owner pattern
															QUERY_WILDCARD,  // vault pattern
															whereExpression, // where exp
															true, objectSelects);
		int listSize = mapList.size();
		Map<String,String> map;String type;
		if(excludeTypes != null && excludeTypes.size() > 0 && listSize > 0) {
			for(int i=0;i<listSize;i++) {
				map = (Map<String,String>)mapList.get(i);
				type=  map.get(SELECT_TYPE);
				if(!excludeTypes.contains(type)) {
					finalList.add(map);
				}
			}
			return finalList;
		}
		return mapList;
	}

	public void getEBOMCountAndLabel(Context context, String[] args) throws Exception {

		String ARG_BASE_URI = "";
	    try {
		      Map programMap     = (Map) JPO.unpackArgs(args);
		      Map<String, String> widgetArgs = (Map<String, String>) programMap.get(UIWidget.JPO_WIDGET_ARGS);
		      ARG_BASE_URI =	widgetArgs.get(UIWidget.ARG_BASE_URI);
		      String fieldKey     = (String) programMap.get(UIWidget.JPO_WIDGET_FIELD_KEY);
		      MapList objectList  = (MapList) programMap.get(UIWidget.JPO_WIDGET_DATA);

			  String label = EnoviaResourceBundle.getProperty(context,  "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Widget.EBOMChildren");
			  String mouseOverText = EnoviaResourceBundle.getProperty(context,  "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Widget.EBOMMouseOverText");

			  getBOMCountAndLabel(context,fieldKey,objectList,label,mouseOverText,"ENCEBOMPowerViewCommand",ARG_BASE_URI);
	    }
	    catch (Exception label){
	    	System.out.println("Exception occurred in emxECPartBase :: getEBOMCountAndLabel");
	    	label.printStackTrace();
	    }
	}

	/**
	 * Updates the maplist with count of related objects like EBOM
	 * @param context
	 * @param args
	 * @throws Exception
	 */

	public void getBOMCountAndLabel(Context context,String fieldKey,MapList objectList,String label,String mouseOverText,String categoryTreeName,String baseURI) throws Exception {
    try {
	      Map<String, String> objInfo;
	      StringList relatedObjList;String id;String count;
	      if(null != objectList) {
	    	  for (int i=0; i < objectList.size(); i++) {
	              objInfo  = (Map<String, String>) objectList.get(i);
	              id = objInfo.get("id");
            	  relatedObjList = FrameworkUtil.split(objInfo.get("from[EBOM].to.id"), MATRIX_DELIMITER);
            	  relatedObjList.addAll(FrameworkUtil.split(objInfo.get("from[EBOM Pending].to.id"), MATRIX_DELIMITER));

            	  count = relatedObjList != null ? String.valueOf(relatedObjList.size()) : "0";
	              objInfo.put(fieldKey, getHTMLForCountAndLabel(count,label,id,mouseOverText,categoryTreeName,baseURI));
          	}
  	  	}
     }catch (Exception widget) {
    	 System.out.println("Exception occurred in emxECPartBase:getEBOMCountAndLabel");
    	 widget.printStackTrace();
     }
 }

	  private String getHTMLForCountAndLabel(String count,String label,String id,String mouseOverText,String categoryTreeName,String baseURI) {
		  return "<div style=\"width: 70px;text-decoration:none;text-align: center;font-size:13px;font-weight: bold;color: #333333;padding: 6px 8px;-moz-box-sizing: border-box;background: linear-gradient(to bottom, #EDF2F4 0%, #94B1BE 100%) repeat scroll 0 0 transparent;border: 1px solid #7A7A7A;box-shadow: 0 0 4px 1px #666666;height: 62px;margin-left: auto;margin-right: auto;\" title=\""+mouseOverText+"\">"+count+"<br>"+label+"</div>";
	  }

    private static HashMap _typePolicyStateIndexMap = null;
    private static String COMMA_SEPERATOR = ",";
	public void loadStateIndexCache(Context context, MapList mapList) throws Exception {
    	_typePolicyStateIndexMap = new HashMap();
    	ChangeUtil changeUtil = new ChangeUtil();
    	StringList typeList  = changeUtil.getStringListFromMapList(mapList, SELECT_TYPE);
		HashSet types = new HashSet();
    	types.addAll(typeList);
    	BusinessType busType = null;
    	Policy policy 		 = null;
    	String policyName;
    	String policyStates;
    	StringList policyStateList;
    	int stateLength =0;
    	Iterator typeItr = types.iterator();
    	while (typeItr.hasNext()) {
        	//below code will retrieve the policies based on type
        	busType = new BusinessType((String)typeItr.next(),context.getVault());
        	busType.open(context);
            PolicyList polList        = busType.getPolicies(context);
            PolicyItr  policyItr      = new PolicyItr(polList);
            busType.close(context);
            policy  = new Policy();
            while (policyItr.next()) {
                policy       = policyItr.obj();
                policyName   = policy.getName();
                policyStates = getPolicyStates(context,policyName);
                policyStateList = FrameworkUtil.split(policyStates, COMMA_SEPERATOR);
                policyStateList = removeNotNeededStates(policyStateList);
                stateLength = policyStateList.size();
                float stateLen = stateLength - 1;
                for (int j=0; j <stateLength; j++) {
                	float k = j;
                	_typePolicyStateIndexMap.put(policyName+":"+policyStateList.get(j),j==0 ? 0 : (int)((k/stateLen) * 100));
                }
            }
    	}
    }

	public static StringList removeNotNeededStates(StringList stateList) {
		StringList requiredStateList = new StringList();
    	if (stateList != null) {
    		Iterator itr = stateList.iterator();
    		String state;
    		while (itr.hasNext()) {
    			state = itr.next().toString();
    			if(state.indexOf("Obsolete") > -1 || state.indexOf("Cancelled") >-1 || state.indexOf("Superseded") >-1 || state.indexOf("solete") >-1
    					|| state.indexOf("ancel") >-1 || state.indexOf("uperseded") >-1)
    				continue;
    				requiredStateList.addElement(state);
    		}
    	}
    	return requiredStateList;
    }

	/**
     *  getPolicyStates  -  returns  all  the  Lifecycle  states  of  a  given  policy
     *  @param  context  The  eMatrix  <code>Context</code>  object
     *  @param  strPolicy  The  policy  name
     *  @return  String  of  policy  values  html.
     *  @throws  Exception  if  the  operation  fails
     */
    public  static  String  getPolicyStates(Context  context,  String  strPolicy)  throws  Exception  {

           String  strStateForPolicy  =  null;
           if(!ChangeUtil.isNullOrEmpty(strPolicy)){
               strStateForPolicy  =  MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3",  true,strPolicy,"state",COMMA_SEPERATOR);
           }
           return  strStateForPolicy;
    }

	/**
	 * Method to give the date after specified days to the current data
	 * @param days
	 * @return
	 */
	public String getDateAfter(int days){
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		int day1 = 0 - days;
		cal.add(Calendar.DAY_OF_MONTH, day1);
		String dateNumofDaysBack = (cal.get(Calendar.MONTH) + 1) + "/"+ (cal.get(Calendar.DAY_OF_MONTH)-1) + "/" + cal.get(Calendar.YEAR);
		return dateNumofDaysBack;
	}


  /**
	 * Gets the change completion percentage based on state where current change objectList
	 *
	 *e.g if MCO has 4 states, and currently at 2nd state mean returns 50% completion percentage
	 * @param context - the eMatrix <code>Context</code> object
	 * @return the same map list provided by the widget - MapList
	 * @throws Exception if the operation fails
	 */
	static public MapList getBadgeStatusForParts(Context context, String[] args) throws Exception {
		String RESOURCE_BUNDLE_ENTERPRISE_STR = "emxEngineeringCentralStringResource";
		Locale locale= context.getLocale();
		String notStarted  = EnoviaResourceBundle.getProperty(context,  RESOURCE_BUNDLE_ENTERPRISE_STR, locale,"emxEngineeringCentral.Widget.InactiveProgress");
	    String inProgress  = EnoviaResourceBundle.getProperty(context,  RESOURCE_BUNDLE_ENTERPRISE_STR, locale,"emxEngineeringCentral.Widget.InworkProgress");

	    String completedbutLate  = EnoviaResourceBundle.getProperty(context,  RESOURCE_BUNDLE_ENTERPRISE_STR, locale,"emxEngineeringCentral.Widget.CompleteButLateProgress");
	    String Late  			 = EnoviaResourceBundle.getProperty(context,  RESOURCE_BUNDLE_ENTERPRISE_STR, locale,"emxEngineeringCentral.Widget.LateProgress");
	    String Completed 		 = EnoviaResourceBundle.getProperty(context,  RESOURCE_BUNDLE_ENTERPRISE_STR, locale,"emxEngineeringCentral.Widget.CompleteProgress");

	    Map programMap     = (Map) JPO.unpackArgs(args);
	    String fieldKey    = (String) programMap.get(UIWidget.JPO_WIDGET_FIELD_KEY);
	    MapList objectList = (MapList) programMap.get(UIWidget.JPO_WIDGET_DATA);
	    String estimateCompletionDate = "";
	    FieldValue dataValue;
		String id;
	    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", context.getLocale());
	    Date currentDate = dateFormat.parse(dateFormat.format(new Date()));
	    String SELECT_TARGET_RELEASE_DATE = "attribute[" +  PropertyUtil.getSchemaProperty("attribute_EffectivityDate") + "]";

		int percentCompleted= 0;
	    for (int i=0; i < objectList.size(); i++) {
	        Map<String, Object> objInfo  = (Map<String, Object>) objectList.get(i);
	        id		= (String)objInfo.get("id");
	        estimateCompletionDate = (String)objInfo.get(SELECT_TARGET_RELEASE_DATE);
	        percentCompleted = (Integer)_typePolicyStateIndexMap.get((String)objInfo.get(SELECT_POLICY)+":"+(String)objInfo.get(SELECT_CURRENT));
	        dataValue = new FieldValue();

	        if(percentCompleted == 0) {
	    		dataValue.setBadgeTitle(notStarted);
	    		dataValue.setBadgeStatus(Status.INFO);
	    		dataValue.setValue("notStarted");
	    	}
	    	else if(percentCompleted == 100) {
	    		dataValue.setBadgeTitle(Completed);
	    		dataValue.setBadgeStatus(Status.DONE);
	    		dataValue.setValue("Completed");
	    	}
	    	else if(percentCompleted > 0 && percentCompleted < 100) {
	    		dataValue.setBadgeTitle(inProgress);
	    		dataValue.setBadgeStatus(Status.INFO);
	    		dataValue.setValue("In Progress");
	    	}

			Date estCompletionDate = null;

			estimateCompletionDate = DomainObject.newInstance(context, id).getInfo(context, SELECT_TARGET_RELEASE_DATE);
			estCompletionDate = !ChangeUtil.isNullOrEmpty(estimateCompletionDate) ?dateFormat.parse(dateFormat.format(eMatrixDateFormat.getJavaDate(estimateCompletionDate))) : null;

			if(UIUtil.isNotNullAndNotEmpty(estimateCompletionDate)) {
				if (percentCompleted >= 0 && percentCompleted < 100 && currentDate.after(estCompletionDate)) {
		    		dataValue.setBadgeTitle(Late);
		    		dataValue.setBadgeStatus(Status.ERROR);
		    		dataValue.setValue("Late");
				}
				else if (percentCompleted == 100 && currentDate.after(estCompletionDate)) {
		    		dataValue.setBadgeTitle(completedbutLate);
		    		dataValue.setBadgeStatus(Status.DONE);
		    		dataValue.setValue("CompletedButLate");
				}
			}
			objInfo.put(fieldKey, dataValue);
	    }
	    return objectList;

	}
	
	/**
	 * This method returns chart data for ENG BOM Powerview.
	 * @param context
	 * @param args
	 * @return MapList
	 * @throws Exception
	 */
	public MapList getBOMChartData(Context context, String[] args)throws Exception {
        MapList mapList = new MapList();
        Map chartData = CacheUtil.getCacheMap(context, "bomChartData");
        
        if(UIUtil.isNotNullAndNotEmpty((String)chartData.get("objIds"))) {
        	StringList objectIdList = FrameworkUtil.splitString((String)chartData.get("objIds"), "|");
        	StringList relIdList = FrameworkUtil.splitString((String)chartData.get("relIds"), "|");
        	Map m;
        	for(int i=0; i < objectIdList.size();i++) {
        		m = new HashMap();
        		m.put("id", (String)objectIdList.get(i));
        		m.put("id[connection]",(String)relIdList.get(i));
        		mapList.add(m);
        	}
        }
        		
        Map rootNodeMap = new HashMap();
        rootNodeMap.put("id[connection]",null);
        rootNodeMap.put("id",(String)chartData.get("rootObjectId"));
        rootNodeMap.put(SELECT_ATTRIBUTE_QUANTITY,"0.0");

		mapList.add(rootNodeMap);        
		return mapList;
	}
	
	
	/**
	 * This method returns chart data for My Engineering View Charts.
	 * @param context
	 * @param args
	 * @return MapList
	 * @throws Exception
	 */
	public MapList getPartsChartData(Context context, String[] args)throws Exception {
        MapList mapList = new MapList();
        Map chartData = CacheUtil.getCacheMap(context, "partsChartData");
        
        if(UIUtil.isNotNullAndNotEmpty((String)chartData.get("objIds"))) {
        	StringList objectIdList = FrameworkUtil.splitString((String)chartData.get("objIds"), "|");
        	Map m;
        	for(int i=0; i < objectIdList.size();i++) {
        		m = new HashMap();
        		m.put("id", (String)objectIdList.get(i));
        		mapList.add(m);
        	}
        }        				       
		return mapList;
	}
	
	//######################################ENG WIDGETS-ENDS#####################

    /**
     * Method to disable RDO field for "Manufacturing Part" policy parts
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     */
    public boolean disableRDOField(Context context, String[] args) throws Exception {
    	/*if(!EngineeringUtil.isENGInstalled(context, args)) {
    		return false;
    	}*/ //Commented for IR-216343

        //unpacking the Arguments from variable args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String policy = (String)programMap.get("lastRevPolicy");

        return !policy.equals(EngineeringConstants.POLICY_MANUFACTURING_PART);
    }

    public boolean hideCreatePartENGField(Context context, String[] args)
    throws Exception {
        boolean displayField = Boolean.FALSE;
        try {
            HashMap programMap      = (HashMap) JPO.unpackArgs(args);
            String strPartCreateMode  = (String) programMap.get("createMode");
            if(!"MFG".equals(strPartCreateMode)) {
                displayField = Boolean.TRUE;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return displayField;
    }
    public boolean displayCreatePartTypeField(Context context, String[] args)
    throws Exception {
        boolean displayField = Boolean.FALSE;
        displayField = hideCreatePartENGField(context, args);
        return displayField;
    }
    public boolean hideEditPartENGField(Context context, String[] args)
    throws Exception {
        boolean displayField = Boolean.FALSE;
        displayField = hideCreatePartENGField(context, args);
        return displayField;
    }

    public boolean hideCreatePartMFGField(Context context, String[] args)
    throws Exception {
        boolean displayField = Boolean.FALSE;
        try {
            HashMap programMap      = (HashMap) JPO.unpackArgs(args);
            String strPartCreateMode  = (String) programMap.get("createMode");
            if("MFG".equals(strPartCreateMode)) {
                displayField = Boolean.TRUE;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return displayField;
    }

    /*
    * Retrieves the Find Number attribute for a given object list.
    *
    * @param context the ENOVIA <code>Context</code> object
    * @param args[] programMap
    * @throws Exception if error encountered while carrying out the request
    */

     public Vector getFindNumberSB(Context context,String args[]) throws Exception
     {
     	  	HashMap programMap = (HashMap) JPO.unpackArgs(args);
     	    MapList objectList = (MapList) programMap.get("objectList");
     	    HashMap paramMap = (HashMap) programMap.get("paramList");
    	    String strBOMViewMode = (String) paramMap.get("BOMViewMode");
    	    String selectedTable = (String) paramMap.get("selectedTable");
    	    
     	    boolean fnKeyExists = false;
     	    
     	    if (objectList.size() > 0) {
     	    	Map infoMap = (Map) objectList.get(0);
     	    	fnKeyExists = infoMap.containsKey(SELECT_ATTRIBUTE_FIND_NUMBER);
     	    }

     	    Vector fnValuesVector = new Vector(objectList.size());
     	    
     	    /*if (fnKeyExists) {
     	    	Iterator iterator = objectList.iterator();
     	    	
     	    	String findNumber;
     	    	Map objectMap;
     	    	while (iterator.hasNext()) {
     	    		objectMap = (Map) iterator.next();
     	    		
     	    		findNumber = getStringValue(objectMap, "Find Number");
     	    		
     	    		if (UIUtil.isNullOrEmpty(findNumber)) {
     	    			findNumber = getStringValue(objectMap, SELECT_ATTRIBUTE_FIND_NUMBER);
     	    		}
     	    		
     	    		if (UIUtil.isNullOrEmpty(findNumber)) {
     	    			fnValuesVector.removeAllElements();
     	    			fnKeyExists = false;
     	    			break; 
     	    		}
     	    		
     	    		else if ("true".equalsIgnoreCase(fnDisplayLeadingZeros)) { findNumber = findNumberPadding(findNumber); }
     	    		
     	    		fnValuesVector.addElement(findNumber);
     	    	}
     	    }
     	    
     	    if (!fnKeyExists) {*/
	     	    int s1 = objectList.size();
	     	    String sRelId[]= new String[s1];
	     	    int i=0;
	     	    StringList selectStmts = new StringList(DomainRelationship.SELECT_ATTRIBUTE_FIND_NUMBER);
	
	     	    RelationshipWithSelectList sRelSelect = getSelectables ( context,objectList, selectStmts);
	
	     	    RelationshipWithSelectItr relWSelItr = new RelationshipWithSelectItr(sRelSelect);
	     	    RelationshipWithSelect relWithSelect = null;
	     	    String strAttributeValue = null;
	     	    if(sRelSelect != null) {
	     	    	while (relWSelItr.next()){
	     	    	 	relWithSelect = relWSelItr.obj();
	     	    	 	if("RollUp".equalsIgnoreCase(strBOMViewMode) && "ENCRevisionManagement".equalsIgnoreCase(selectedTable)){
	     	    	 		strAttributeValue = ((String)((Map)objectList.get(i)).get(EngineeringConstants.SELECT_ATTRIBUTE_FIND_NUMBER));
	     	    	 		i++;
	     	    	 	}else{
	     	    	 		strAttributeValue = relWithSelect.getSelectData (DomainRelationship.SELECT_ATTRIBUTE_FIND_NUMBER);
	     	    	 	}
	     	    	 	if (fnDisplayLeadingZeros.equalsIgnoreCase("true")){
	     	    	 		fnValuesVector.addElement(findNumberPadding(strAttributeValue));
	     	    	 	} else if(strAttributeValue == null) {
	     	    	 		fnValuesVector.addElement("");
	     	    	 	}else {
	     	    	 		fnValuesVector.addElement(strAttributeValue);
	     	    	 	}
	     	    	}
	     	    } else {
	     	    	for(i=1;i<=objectList.size();i++){
	     	    		fnValuesVector.addElement("");
	     	    	}
	     	    }
     	    //}
     	    
     	    return fnValuesVector;
     }

 /*
  * Generic method for retrieving the selectables with one DB query.
  *
  * @param context the ENOVIA <code>Context</code> object
  * @param ObjectList programMap object list as retrieved from intendet table
  * @param selectStmts query selectable
  * @return relationshipWithSelectable
  */

 public RelationshipWithSelectList getSelectables (Context context,MapList objectList, StringList selectStmts) {

 	Iterator bomListItr 		= objectList.iterator();

 	// Call to get the rel select at one shot from database
     RelationshipWithSelectList sRelSelect = null;

 	int s1 = objectList.size();
 	String strRelId = "";
     int i=0;

     // Iterating to get the Relationship ID
     try {

     if (s1 > 0 ) {
        	String[] sRelId= new String[s1];
     	while(bomListItr.hasNext())
     	{
     		Map bomMap = (Map)bomListItr.next();
     		strRelId = (String) bomMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);

     		if (strRelId != null && strRelId.length() > 0)
     		{
     			sRelId[i]=strRelId;
     		}
     		i++;
     	}

     	try {
     		if (sRelId.length >= 1 && sRelId[0] != null) {
     		//if (sRelId.length >= 1) {
     			sRelSelect = Relationship.getSelectRelationshipData(context, sRelId, selectStmts);
     		}
     	}  catch (Exception e) {
     		System.out.println ("Exception in emxECPartBase:getSelectables :"+e.getMessage());
     	}

     }
  } catch (Exception e) {
 	 System.out.println ("Exception in getSelectables - Array exception :"+e.getMessage());
  }

  	return sRelSelect;
  } // End of method

 /*
  *   Pads zero to the find number based on the emxEngineeringCentral.FindNumberLength value
  *
  *   @param : fnValue the actual string which needs to be padded
  *   @return returns the Padded string
  *   @throws Exception if error encountered while carrying out the request
  */

 String findNumberPadding(String fnValue) throws Exception {

      boolean isStrNumber=true;
      int fnLen = Integer.parseInt(fnLength);
      String fnNothing=" ";

      String fnValuesVector = "";

 	 //Added below code to fix bug 333456
 	 if(fnValue == null || "null".equalsIgnoreCase(fnValue)) {
 		 fnValue = "";
 	 }
      //Checking whether Find number is a number or a String
      if(!fnValue.startsWith("0"))
      {
          try
          {
			Long.valueOf(fnValue);
          }
          catch (Exception ex)
          {
               isStrNumber = false;
          }
      }
      else
      {
          isStrNumber = false;
      }

      //Display the leading zeros only if find number length >0
      //Find number is number and display leadingzeros property set to true
      if(fnLen>0 && isStrNumber && "true".equalsIgnoreCase(fnDisplayLeadingZeros))
      {
          for(int i=0;i<=fnLen;i++)
          {
             if(fnValue.length()<fnLen)
             {

                 fnValue = "0"+fnValue;
             }
             else
             {
             	fnValuesVector= fnValue;
                 break;
             }
           }
       }
       else
       {
           if(!"".equals(fnValue))
           {
         	  fnValuesVector = fnValue;
           }
           else
           {
         	  fnValuesVector= fnNothing;
           }
       }
      return fnValuesVector;
  }

	 /*
	  * Retrieves the EBOM data. Modified for expand issue for latest and latest complete.
	  *
	  * @param context the ENOVIA <code>Context</code> object
	  * @param args[] programMap
	  * @throws Exception if error encountered while carrying out the request
	  */
	 @com.matrixone.apps.framework.ui.ProgramCallable
	 public MapList getEBOMsWithRelSelectablesSB(Context context, String[] args) throws Exception{
		 HashMap paramMap = (HashMap) JPO.unpackArgs(args);

		 String sExpandLevels = getStringValue(paramMap, "emxExpandFilter");
		 String selectedFilterValue = getStringValue(paramMap, "ENCBOMRevisionCustomFilter");
		 MapList retList = expandEBOM(context, paramMap);

		 if("Latest".equals(selectedFilterValue) || "Latest Complete".equals(selectedFilterValue) || "Latest Release".equals(selectedFilterValue)){
			// handles manual expansion by each level for latest and latest complete
			 int expandLevel = "All".equals(sExpandLevels)? 0: Integer.parseInt(sExpandLevels);
			 MapList childList = null;
			 Map obj = null;
			 int level;
			 for(int index=0; index < retList.size(); index++){
				 obj = (Map)retList.get(index);
				 if(expandLevel == 0 || Integer.parseInt((String)obj.get("level")) < expandLevel){
					 paramMap.put("partId", (String)obj.get(SELECT_ID));
					 childList = expandEBOM(context, paramMap);
					 if(childList!=null && !childList.isEmpty()){
						 for(int cnt=0; cnt<childList.size(); cnt++){
							 level = Integer.parseInt((String)obj.get("level"))+1;
							((Map)childList.get(cnt)).put("level", String.valueOf(level));
						 }
						 retList.addAll(index+1,childList);
					 }
				}
			 }
		 }
		 
		 if ( "Rollup".equals( BOMMgtUtil.getStringValue(paramMap, "BOMViewMode") ) ) {
			 String partId = getStringValue(paramMap, "objectId");
			 partId = getStringValue(paramMap, "partId") == null ? partId : getStringValue(paramMap, "partId");
			 
			 IBOMService iBOMService = IBOMService.getService(context, partId, false); 
			 return iBOMService.getRollupList(context, retList);
		 } else {
			 return retList;
		 }
	 }

	 /*
	  * Retrieves the EBOM data. Method is added to expand the ebom and fetch the
	  * Latest or Latest Complete nodes in child.
	  *
	  * @param context the ENOVIA <code>Context</code> object
	  * @param HashMap paramMap
	  * @throws Exception if error encountered while carrying out the request
	  */

 	public MapList expandEBOM(Context context, HashMap paramMap) throws Exception {  //name modified from getEBOMsWithRelSelectablesSB
		//HashMap paramMap = (HashMap) JPO.unpackArgs(args);

		int nExpandLevel = 0;

		String partId = getStringValue(paramMap, "objectId");
		String sExpandLevels = getStringValue(paramMap, "emxExpandFilter");
		String selectedFilterValue = getStringValue(paramMap, "ENCBOMRevisionCustomFilter");
		String strAttrEnableCompliance = PropertyUtil.getSchemaProperty(context, "attribute_EnableCompliance");
		String complete = PropertyUtil.getSchemaProperty(context, "policy", DomainConstants.POLICY_DEVELOPMENT_PART, "state_Complete");
		String release = PropertyUtil.getSchemaProperty(context, "policy", DomainConstants.POLICY_EC_PART, "state_Release");
		String SELECT_ATTR_ENABLE_COMPLIANCE = "attribute[" + strAttrEnableCompliance + "]";
		String curRevision;
		String latestObjectId;
		String latestRevision;

		if (!isValidData(selectedFilterValue)) {
			selectedFilterValue = "As Stored";
		}

		if (!isValidData(sExpandLevels)) {
			sExpandLevels = getStringValue(paramMap, "ExpandFilter");
		}
		
		String SELECT_LAST_EBOM_EXISTS = "last.from[EBOM]";

		StringList objectSelect = createStringList(new String[] {SELECT_ID, SELECT_TYPE,SELECT_REVISION, SELECT_LAST_ID, SELECT_LAST_REVISION,SELECT_LAST_CURRENT,
																		SELECT_REL_FROM_EBOM_EXISTS, SELECT_ATTR_ENABLE_COMPLIANCE, SELECT_LAST_EBOM_EXISTS});
		//BOM UI Performance: Attributes required for Related Physical title column
		String attrVPLMVName = PropertyUtil.getSchemaProperty(context,"attribute_PLMEntity.V_Name");
        attrVPLMVName = "attribute["+attrVPLMVName+"]";
        String typeVPLMProd = PropertyUtil.getSchemaProperty(context,"type_PLMEntity");
        
        String selectPartVName = "attribute["+EngineeringConstants.ATTRIBUTE_V_NAME+"]";
        String selectProdctIdSel = "from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"|to.type.kindof["+typeVPLMProd+"]].to."+DomainConstants.SELECT_ID;
        String selectPrdPhysicalId = "from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"].to["+EngineeringConstants.TYPE_VPLM_VPMREFERENCE+"].physicalid";
        String sVPLMInstanceRelId ="frommid["+EngineeringConstants.RELATIONSHIP_VPM_PROJECTION_RELID+"].torel.physicalid";
        
	    objectSelect.add(selectPrdPhysicalId);
		objectSelect.add(selectProdctIdSel);
		objectSelect.add(selectPartVName);
		//BOM UI Performance: Attributes required for Related Physical title column

		StringList relSelect = createStringList(new String[] {SELECT_RELATIONSHIP_ID, SELECT_ATTRIBUTE_FIND_NUMBER, sVPLMInstanceRelId, SELECT_ATTRIBUTE_UNITOFMEASURE, SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR, SELECT_ATTRIBUTE_QUANTITY});

		if (!isValidData(sExpandLevels) || ("Latest".equals(selectedFilterValue) || "Latest Complete".equals(selectedFilterValue) ||"Latest Release".equals(selectedFilterValue))) {
			nExpandLevel = 1;
			partId = getStringValue(paramMap, "partId") == null? partId : getStringValue(paramMap, "partId");
		} else if ("All".equalsIgnoreCase(sExpandLevels)) {
			nExpandLevel = 0;
		} else {
			nExpandLevel = Integer.parseInt(sExpandLevels);
		}
		
		Part partObj = new Part(partId);

		MapList ebomList = partObj.getRelatedObjects(context,
				          							 RELATIONSHIP_EBOM,
				          							 TYPE_PART,
				          							 objectSelect,
				          							 relSelect,
					                                 false,
					                                 true,
					                                 (short) nExpandLevel,
					                                 null, null, 0);

		  Iterator itr = ebomList.iterator();
		  Map newMap;

		  StringList ebomDerivativeList = EngineeringUtil.getDerivativeRelationships(context, RELATIONSHIP_EBOM, true);
		  
	      if ("Latest".equals(selectedFilterValue) || ("Latest Complete".equals(selectedFilterValue)) || ("Latest Release".equals(selectedFilterValue))) {
	          //Iterate through the maplist and add those parts that are latest but not connected

	          while (itr.hasNext()) {
	              newMap = (Map) itr.next();

	              curRevision    = getStringValue(newMap, SELECT_REVISION);
	              latestObjectId = getStringValue(newMap, SELECT_LAST_ID);
	              latestRevision = getStringValue(newMap, SELECT_LAST_REVISION);

	              if (nExpandLevel != 0) {	            	  
		        	  newMap.put("hasChildren", EngineeringUtil.getHasChildren(newMap, ebomDerivativeList));
	              }

	              if ("Latest".equals(selectedFilterValue)) {
	            	  newMap.put(SELECT_ID, latestObjectId);
	            	  newMap.put("hasChildren", EngineeringUtil.getHasChildren(newMap, ebomDerivativeList));
	              }

	              else {
	                   DomainObject domObjLatest = DomainObject.newInstance(context, latestObjectId);
	                   String currSta = domObjLatest.getInfo(context, DomainConstants.SELECT_CURRENT);

	                   if (curRevision.equals(latestRevision)) {
	                	   if (complete.equalsIgnoreCase(currSta) || release.equalsIgnoreCase(currSta)) {
	                		   newMap.put(SELECT_ID, latestObjectId);
	                	   } else {
	                		   itr.remove();
	                	   }
	                   }
	                   else {
	                	   while(true) {
	                   		   if(currSta.equalsIgnoreCase(complete) || currSta.equalsIgnoreCase(release)) {
	                   			   newMap.put(SELECT_ID, latestObjectId);
	                   			   	break;
	                   		   } else {
	                   			   BusinessObject boObj = domObjLatest.getPreviousRevision(context);
	                   			   if(!(boObj.toString()).equals("..") ) {
	                   				   boObj.open(context);
	                   				latestObjectId = boObj.getObjectId();
	                   				   domObjLatest = DomainObject.newInstance(context,latestObjectId);
	                        		   currSta = domObjLatest.getInfo(context,DomainConstants.SELECT_CURRENT);
	                   			   } else {
	                   				   itr.remove();
	                   				   break;
	                   			}
	                   		 }
	                  	  }//End of while
	                   }//End of Else
	               }
	            }//End of While

	      	}//End of IF, Latest or Latest complete filter is selected

	      else if (nExpandLevel != 0) {
	          while (itr.hasNext()) {
	        	  newMap = (Map) itr.next();

	        	  // To display  + or - in the bom display	        	  
	        	  newMap.put("hasChildren", EngineeringUtil.getHasChildren(newMap, ebomDerivativeList));
	          }
	      }
	      else{		  
				  while (itr.hasNext()) {
					newMap = (Map) itr.next();
	    	  }
	      }
	 	return ebomList;
	 }

	/* This method verifies all the part is connected to same RDO.
	 * if the given array of part is connected to same RDO it returns RDO Id, else it returns null.
	*/

  public String getRDOId(Context context, String[] objectIds) throws Exception {
  	String strRDOId = "";

  	if (objectIds != null) {
  		String strRDOIdTemp;
  		String SELECT_RDO_ID = "to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id";

  		MapList dataMapList = DomainObject.getInfo(context, objectIds, new StringList(SELECT_RDO_ID));

  		for (int i = 0, size = dataMapList.size(); i < size; i++) {
  			strRDOIdTemp = (String) ((Map) dataMapList.get(i)).get(SELECT_RDO_ID);

  			if (isValidData(strRDOIdTemp)) {
  				if ("".equals(strRDOId)) {
  					strRDOId = strRDOIdTemp;
  				} else if (!strRDOId.equals(strRDOIdTemp)) {
					strRDOId = null;
					break;
				}
			}
  		}
  	}

  	if (isValidData(strRDOId)) {
  		strRDOId = strRDOId + "|" + DomainObject.newInstance(context, strRDOId).getName(context);
  	}

  	return strRDOId;
  }


  /**
     * This method will be called when common view expand filter selected as "End Item"
     * The method get all the leaf end items for a given EBOM maplist
     *
     * @param context
     * @param mlCommonBOMList - EBOM List
     * @param slObjectSelectStmts
     * @param slRelSelectStmts
     * @param sAccessSelectable
     * @param slObjectSelect
     *
     * @return mlEndItem - all the leaf end items for a given EBOM maplist
     * @throws NumberFormatException
     * @throws Exception
     * @throws FrameworkException
     */
    private MapList getLeafEndItems(Context context, MapList mlCommonBOMList,
            StringList slObjectSelectStmts, StringList slRelSelectStmts,
            String sAccessSelectable, String slObjectSelect)
            throws NumberFormatException, Exception, FrameworkException {
        MapList mlEndItem = new MapList();
        int ilevel =0;

        String sEBOMtoSelect = "from["+EngineeringConstants.RELATIONSHIP_EBOM+"].to.";
        String sEndItemSelect = sEBOMtoSelect+EngineeringConstants.SELECT_END_ITEM;

      DomainObject doToObj = new DomainObject();
        for(int i=0; i<mlCommonBOMList.size(); i++){
            Map ebomMap = (Map)mlCommonBOMList.get(i);
            String sEBOMId = (String)ebomMap.get(DomainObject.SELECT_RELATIONSHIP_ID);

            String strLevel = ebomMap.get("level").toString();
            ilevel = Integer.parseInt((String)strLevel);
            ilevel = ilevel+1;

            StringList slEndItem = new StringList();
            StringList slEndItemRel = new StringList();

            Object oEndItem =  ebomMap.get(sEndItemSelect);
            Object oEIrelId = ebomMap.get("from["+EngineeringConstants.RELATIONSHIP_EBOM+"].id");

            if(null != oEndItem && !"null".equals(oEndItem)) {

                if(oEndItem instanceof String) {
                    slEndItem.addElement(oEndItem);
                    slEndItemRel.addElement(oEIrelId);
                } else {
                    slEndItem = (StringList)oEndItem;
                    slEndItemRel = (StringList)oEIrelId;
                }

                for(int k=0; k<slEndItem.size(); k++) {

                    String strEI = (String)slEndItem.get(k);
                    String sEIrelId = (String)slEndItemRel.get(k);

                    if(strEI.equalsIgnoreCase("Yes")){

                      String sToObjId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump $3", sEIrelId,"to.id","|");
                      doToObj = new DomainObject(sToObjId);
                        Map map = doToObj.getInfo(context, slObjectSelectStmts);

                        Map endItemMap = new HashMap();
                        int mapsize = map.size();
                        Iterator keyValuePairs = map.entrySet().iterator();
                        for (int j=0; j<mapsize; j++)
                        {
                            Map.Entry entry = (Map.Entry) keyValuePairs.next();
                            String key = entry.getKey().toString();

                          if(!endItemMap.containsKey(key) && !"level".equals(key)) {
                              if(key.equals(slObjectSelect)) {
                                  endItemMap.put(sAccessSelectable,entry.getValue().toString());
                              } else {
                                  endItemMap.put(key, entry.getValue().toString());
                              }
                            }
                        }
                        MapList mlNextEBOM = DomainRelationship.getInfo(context, new String[]{sEIrelId}, slRelSelectStmts);
                        Map relMap = (Map)mlNextEBOM.get(0);
                        mapsize = relMap.size();
                        keyValuePairs = relMap.entrySet().iterator();
                        for (int j=0; j<mapsize; j++)
                        {
                            Map.Entry entry = (Map.Entry) keyValuePairs.next();
                            endItemMap.put(entry.getKey().toString(),entry.getValue().toString());
                        }
                        endItemMap.put("level",Integer.toString(ilevel));
                        endItemMap.put("ParentEBOMid", sEBOMId);
                        mlEndItem.add(endItemMap);
                      }
                    }
                }
        }
        return mlEndItem;
    }
    /**
     * The method is used to display Lead time attribute range values in create and edit forms.
     *
     * @param context Context : User's Context.
     * @param String[] args
     * @return HashMap.
     * @throws Exception if the operation fails.
     */
    public HashMap displayLeadTimeAttributeRangeValues(Context context,String[] args) throws Exception
    {
        String[] rangeOptions = {"emxFramework.Range.Lead_Time.1_Week","emxFramework.Range.Lead_Time.2_Weeks",
        		"emxFramework.Range.Lead_Time.3_Weeks","emxFramework.Range.Lead_Time.4_Weeks", "emxFramework.Range.Lead_Time.6_Weeks",
        		"emxFramework.Range.Lead_Time.8_Weeks","emxFramework.Range.Lead_Time.12_Weeks","emxFramework.Range.Lead_Time.20_Weeks"};

		HashMap rangeMap = new HashMap(2);
		rangeMap.put("field_choices", getValueListFromProperties(context,rangeOptions, "emxFrameworkStringResource", "en"));
		rangeMap.put("field_display_choices", getValueListFromProperties(context,rangeOptions, "emxFrameworkStringResource", context.getSession().getLanguage()));

        return rangeMap;
    }
    /**
     * Expand Program for markup view
     * @param context
     * @param args
     * @return EBOM Map List with conflict info
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getStoredEBOMForMarkupView(Context context, String[] args) throws Exception {
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		Hashtable htConflictBOMs = (Hashtable) paramMap.get("ConflictList");
		MapList ebomList = null;
		String strType = null;
		String strName = null;
		String strRev = null;
		String strFN = null;
		String strRD = null;
		String strEBOMKey = null;

		try {
			ebomList = getEBOMsWithRelSelectables(context, args);

			Iterator itr = ebomList.iterator();
			MapList tList = new MapList();
			StringList ebomDerivativeList = EngineeringUtil.getDerivativeRelationships(context, RELATIONSHIP_EBOM, true);
			while (itr.hasNext()) {
				HashMap newMap = new HashMap((Map) itr.next());
				newMap.put("selection", "multiple");
				newMap.put("hasChildren", EngineeringUtil.getHasChildren(newMap, ebomDerivativeList));

				if (htConflictBOMs != null) {
					strType = (String) newMap.get(SELECT_TYPE);
					strName = (String) newMap.get(SELECT_NAME);
					strRev = (String) newMap.get(SELECT_REVISION);
					strFN = (String) newMap.get(SELECT_ATTRIBUTE_FIND_NUMBER);
					strRD = (String) newMap.get(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);

					strEBOMKey = strType + "~" + strName + "~" + strRev + "~"
							+ strFN + "~" + strRD;

					if (htConflictBOMs.containsKey(strEBOMKey)) {
						newMap.put("HasConflict", "true");
						newMap.put("key", strEBOMKey);
					} else {
						newMap.put("HasConflict", "false");
					}
				}

				tList.add(newMap);

			}

			ebomList.clear();
			ebomList.addAll(tList);

			HashMap hmTemp = new HashMap();
			hmTemp.put("expandMultiLevelsJPO", "true");
			ebomList.add(hmTemp);
		} catch (FrameworkException Ex) {
			throw Ex;
		}

		return ebomList;
	}

	/**
     * PreProcessJPO for opening markup
     * With XSS settings also
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.PreProcessCallable
    public HashMap sendXMLForLoadMarkup(Context context, String args[]) throws Exception {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        HashMap tableData = (HashMap) programMap.get("tableData");

        MapList ObjectList = (MapList) tableData.get("ObjectList");

        String strMarkupXML = (String) requestMap.get("XMLINFO");

        HashMap hmpReturn = new HashMap();

        if (strMarkupXML != null && !"null".equals(strMarkupXML) && strMarkupXML.length() > 0)
        {
    		hmpReturn.put ("Action", "execScript");
        	hmpReturn.put("Message", "{ main:function()  { loadMarkUpXML(\"" + strMarkupXML +"\", \"true\");  }}");
    	}
    	else
    	{
    		hmpReturn.put ("Action", "");
    		hmpReturn.put("Message", "");
    	}

        hmpReturn.put("ObjectList", ObjectList);

        return hmpReturn;
    }

    /**
     * Gets the 'Production' Classification  Policies for Policy field of Go To Production page.
     *
     * @param context  Context : User's Context.
     * @param String[] args
     * @return <code>HashMap</code>
     * @throws Exception
     */
	public HashMap getProductionPolicy(Context context, String[] args) throws Exception {
		HashMap hmPolicyMap = new HashMap();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String languageStr = (String) requestMap.get("languageStr");
		String sTypePart = PropertyUtil.getSchemaProperty(context, "type_Part");
		StringList polList = EngineeringUtil.getProductionPolicies(context,	sTypePart);
		StringItr polItr = new StringItr(polList);
		String defaultPolicy = EnoviaResourceBundle.getProperty(context,	"type_Part.defaultProdPolicy");
		StringList display = new StringList();
		StringList actualVal = new StringList();
		try {
			String policyName = "";
				while (polItr.next()) {
					policyName =polItr.obj();
					display.addElement(i18nNow.getAdminI18NString("Policy",	policyName, languageStr));
					actualVal.addElement(policyName);
				}
				int position = actualVal.indexOf(defaultPolicy);
				if (position > 0) {
					String positionDisplay = (String) display.get(position);
					String positionActual = (String) actualVal.get(position);
					display.setElementAt(display.get(0), position);
					actualVal.setElementAt(actualVal.get(0), position);
					display.setElementAt(positionDisplay, 0);
					actualVal.setElementAt(positionActual, 0);
				}
			hmPolicyMap.put("field_choices", actualVal);
			hmPolicyMap.put("field_display_choices", display);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return hmPolicyMap;
	}
/**
 * Populates the default revision value with respect to default Production Policy
 *
 * @param context  Context : User's Context.
 * @param String[] args
 * @return <code>String</code>
 * @throws Exception
 */
	public String showDefaultProductionRevision(Context context, String[] args) throws Exception {
		String defaultPolicy = EnoviaResourceBundle.getProperty(context, "type_Part.defaultProdPolicy");
		return new Policy(defaultPolicy).getFirstInSequence(context);
	}

	/**Performs the Go To Production action on a part
	 *
	 * @param context Context : User's Context.
	 * @param String[] args
	 * @return <code>Map</code>
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public Map partGoToProductionJPO(Context context, String[] args) throws Exception {
		Map returnMap = new HashMap();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strPartId = (String) programMap.get("copyObjectId");
			String sCustomRevisionLevel = (String) programMap.get("CustomRevisionLevel");
			String sPolicy = (String) programMap.get("Policy");
			String sVault = (String) programMap.get("Vault");			
			Part part = new Part(strPartId);
			context.setCustomData("BOM_GO_TO_PRODUCTION", "TRUE");
			String	prodObjectId = part.triggerPolicyDevelopmentPartStateReviewPromoteAction(context, sPolicy, sCustomRevisionLevel, sVault);
			part.floatEBOMrelationship(context,strPartId,prodObjectId);
			returnMap.put("id",prodObjectId);
		} catch (Exception ex) {           
            ex.printStackTrace();
            throw new FrameworkException(ex.getMessage());
        } finally {
			context.removeFromCustomData("BOM_GO_TO_PRODUCTION");	       
		}
		return returnMap;
	}


	/**Retrieves the Part Information
	 *
	 * @param context Context : User's Context.
	 * @param String[] args
	 * @return <code>Map</code>
	 * @throws Exception
	 */
	public Map getPartInfo(Context context, String args[]) throws Exception {
		String objectId = args[0];
		DomainObject dmObj = DomainObject.newInstance(context, objectId);
		String SELECT_RDO_REL_EXISTS = "to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "]";
		//Modified RDO Convergence start
		String SELECT_RDO_REL_ID = "to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id";
		StringList selectList = createStringList(new String[] {DomainConstants.SELECT_VAULT, DomainConstants.SELECT_TYPE,
				DomainConstants.SELECT_NAME, SELECT_RDO_REL_EXISTS,SELECT_RDO_REL_ID});
		//Modified RDO Convergence End
		return dmObj.getInfo(context, selectList);

	}

	/** Checks the license for selected parts if person doesnot have license then edit mode will be disabled.
	 * @param context ematrix context.
	 * @param args ematrix arguments.
	 * @return HashMap.
	 * @throws Exception if any operayion fails.
	 */
	@com.matrixone.apps.framework.ui.PreProcessCallable
	public HashMap checkLicense(Context context, String[] args) throws Exception {
		boolean isECCInstalled = FrameworkUtil.isSuiteRegistered(context, "appVersionEngineeringConfigurationCentral", false, null, null);
		boolean isMFGInstalled = EngineeringUtil.isMBOMInstalled(context);

		String strTBELicenseMessg = null;
		String strENGLicenseMessg = null;
		String strECCLicenseMessg = null;
		String strMFGLicenseMessg = null;

		HashMap resultMap = new HashMap();

		try {
			String[] app = {"ENO_TBE_TP", "ENO_LBC_TP","ENO_MCC_TP"};
			ComponentsUtil.checkLicenseReserved(context, app);
		} catch (Exception tbe) {
			strTBELicenseMessg = tbe.toString();
		}

		try {
			String[] app = {"ENO_PRT_TP",  "ENO_LBC_TP","ENO_MCC_TP"};
			ComponentsUtil.checkLicenseReserved(context, app);
		} catch (Exception eng) {
			strENGLicenseMessg = eng.toString();
		}

		try {
			if (isECCInstalled) {
	            ComponentsUtil.checkLicenseReserved(context, "ENO_XCE_TP");
			}
		} catch (Exception ecc) {
			strECCLicenseMessg = ecc.toString();
		}

		try {
			if (isMFGInstalled) {
				ComponentsUtil.checkLicenseReserved(context, "ENO_MFG_TP"); //License Check
			}
		} catch (Exception mfg) {
			strMFGLicenseMessg = mfg.toString();
		}

		if (strTBELicenseMessg != null || strENGLicenseMessg != null || strECCLicenseMessg != null || strMFGLicenseMessg != null) {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");

			String selectedObjectIds = getStringValue(requestMap, "objIds");

			if (!isValidData(selectedObjectIds)) {
				selectedObjectIds = getStringValue(requestMap, "objectId");
			}

			selectedObjectIds = selectedObjectIds.replaceAll("~*$", "");

			if (isValidData(selectedObjectIds)) {
				String SELECT_POLICY_CLASSIFICATION = "policy.property[PolicyClassification].value";
				String policyClassification;
				String policy;

				String[] objectIds = selectedObjectIds.split("~");

				StringList objectSelect = createStringList(new String[] {SELECT_POLICY_CLASSIFICATION, DomainConstants.SELECT_POLICY});
				StringList slMfgPolicy = EngineeringUtil.getManuPartPolicy(context);

				MapList objectInfoList = DomainObject.getInfo(context, objectIds, objectSelect);
				Map map;

				Iterator iterator = objectInfoList.iterator();
				String messg = null;

				while (iterator.hasNext()) {
					map = (Map) iterator.next();
					policyClassification = getStringValue(map, SELECT_POLICY_CLASSIFICATION);

					if ("Development".equals(policyClassification)) {
						messg = (strTBELicenseMessg != null && strENGLicenseMessg != null) ? strTBELicenseMessg : null;
					} else if ("Production".equals(policyClassification)) {
						policy = getStringValue(map, DomainConstants.SELECT_POLICY);

						if (slMfgPolicy.contains(policy)) {
							messg = (strMFGLicenseMessg != null) ? strMFGLicenseMessg : null;
						} else if (strENGLicenseMessg != null) {
							messg = strENGLicenseMessg;
						}
					} else if (strECCLicenseMessg != null && "Unresolved".equals(policyClassification)) {
						messg = strECCLicenseMessg;
					}

					if (messg != null) {
						resultMap.put("Action", "stop");
						resultMap.put("Message", messg);
						break;
					}
				}
			}
		}

		return resultMap;
	}


	public boolean isTBEInstalled(Context context) {
		boolean isTBEInstall = FrameworkUtil.isSuiteRegistered(context,"appVersionTeamBOMEditor", false, null,null);
		isTBEInstall = true;
		return isTBEInstall;
	}
   /**
     * Method to check whether VPM Team is installed
     * @param context
     * @return boolean
     * @throws Exception
     */
    public Boolean checkForVPMTeamInstallation(Context context, String[] args) throws FrameworkException {
    	return (Boolean) FrameworkUtil.isSuiteRegistered(context, "appVersionENOVIA VPM Team Multi-discipline Collaboration Platform", false, null, null);
    }

	/**
	 * Method to disply V_Name field in view mode when VPM is installed
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Boolean displayVNameViewField(Context context, String[] args) throws Exception {
		return (Boolean)checkViewMode(context, args);
	}

	/**
	 * Method to disply V_Name field in edit mode when VPM is installed
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Boolean displayVNameEditField(Context context, String[] args) throws Exception {
		return (Boolean)checkEditMode(context, args);
	}

	/**
     * This method to check revision of part exists
     * @param context the eMatrix <code>Context</code> object
     * @param args holds input arguments.
     * @return a String containing the HTML content to display Bondpad.
     * @throws Exception if the operation fails
     */
    public int checkIfPartExists(Context context,String args[]) throws Exception
    {
        if (args == null || args.length < 1)
        {
        throw (new IllegalArgumentException());
        }

        final String _syncExecVar = MqlUtil.mqlCommand(context,"get env $1 ;",SYNC_ENV_BYPASS_FOR_CLONE);
        if ((_syncExecVar != null) && _syncExecVar.equalsIgnoreCase("true")) {
        	return 0;
        }
        
        String sExpand = args[0];
        String sExpandFromSpecificLevel = args[1];
        String sType = args[2];
        String sName = args[4];
        String sRevision = args[5];
        String sPolicy = args[6];

        String sMqlRes = "";
        String sCmd = "";
        String sQueryRev = "*";
        String sPolicyClass = "";
        String displayErrorMes = "";

        String policyMfgEqu  = PropertyUtil.getSchemaProperty(context, "policy_ManufacturerEquivalent");
        String sWhere = "policy != '"+policyMfgEqu+"'";
        
        if(sExpand !=null) {
	        if("EXPAND_FROM_SPECIFIC_LEVEL".equalsIgnoreCase(sExpand)) {
	        	if(!"".equals(sExpandFromSpecificLevel)) {
                    String sExpandFromSpecificLevelOrg = PropertyUtil.getSchemaProperty(context, sExpandFromSpecificLevel);
                        if(!"".equals(sExpandFromSpecificLevelOrg)) {
                            sType = sExpandFromSpecificLevelOrg;
                            sCmd = "temp query bus $1 $2 $3 where $4 dump $5";
                        }
                }
	        } else if("DO_NOT_EXPAND".equalsIgnoreCase(sExpand)) {
                    sCmd  = "temp query !expand bus $1 $2 $3 where $4 dump $5";
	        } else if("EXPAND_FROM_SAME_LEVEL".equalsIgnoreCase(sExpand)) {
                    sCmd = "temp query bus $1 $2 $3 where $4 dump $5";
            } else {
                    sCmd = "temp query bus $1 $2 $3 where $4 dump $5";
            }
        } else {
            sCmd = "temp query bus $1 $2 $3 where $4 dump $5";
        }
        if(sPolicy != null && !sPolicy.equals("")){
            sPolicyClass = MqlUtil.mqlCommand(context,"print policy $1 select $2 dump $3",sPolicy,"property[PolicyClassification].value","|");
    		if(sPolicyClass !=null && "Equivalent".equals(sPolicyClass)) {
    			sQueryRev = sRevision;
                        sCmd = "temp query bus $1 $2 $3 where $4 dump $5";
                        sWhere =  "policy == '"+policyMfgEqu+"'";
            }
    	}
        if (sCmd !=null && !"".equals(sCmd)) {
            sMqlRes = MqlUtil.mqlCommand(context, sCmd, sType, sName, sQueryRev,sWhere, "|");
        } else {
        	sMqlRes = displayErrorMes;
        }

    	if(sMqlRes != null && !"".equals(sMqlRes)) {
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
    	return 0;
    }

    /**
     * Method is called from createJPO when cloning the part from global actions and part properties page.
     * This method checks weather context user has license for creating part, If user has license only then it clones the selected object.
     */

    @com.matrixone.apps.framework.ui.CreateProcessCallable
    public HashMap checkLicenseAndCloneObject(Context context, String[] args) throws Exception {
	    HashMap programMap = (HashMap) JPO.unpackArgs(args);

   		Part part = (Part) DomainObject.newInstance(context, DomainConstants.TYPE_PART, DomainConstants.ENGINEERING);

    	String clonedObjectId = part.clonePart(context, programMap);

    	HashMap returnMap = new HashMap(1);
    	returnMap.put(DomainConstants.SELECT_ID, clonedObjectId);

		
    	return returnMap;
   }

    /**
     * Method is called from postProcessJPO when cloning the part from global actions and part properties page.
     * This method disconnectes the EBOM OR Reference Document Connections if it is unselected in UI.
     * All options selected in Include Related Data from UI will be done in this method.
     * Part Family connections.
     * Derived Relation connection.
     */

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void postProcessForClonePart(Context context, String[] args) throws Exception {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	HashMap requestMap = (HashMap) programMap.get("requestMap");
    	HashMap paramMap   = (HashMap) programMap.get("paramMap");

    	boolean boolEBOMRelSelected = false;
    	boolean boolEBOMSubSelected = false;
    	boolean boolRefDocRelSelected = false;
    	//Added for MCC modification
    	boolean boolComponentMaterial = false;
    	String RELATIONSHIP_COMPONENT_MATERIAL = PropertyUtil.getSchemaProperty("relationship_ComponentMaterial");
    	
    	String parentId = getStringValue(requestMap, "copyObjectId");
    	String partFamily = getStringValue(requestMap, "PartFamily");
    	String relToDisconnect = null;

    	String newObjectId = getStringValue(paramMap, "newObjectId");
    	String partIds = newObjectId;

    	//Modified for To Create Multiple part from Part Clone
    	StringTokenizer st = new StringTokenizer(partIds, "~");
        while(st.hasMoreTokens()){
        	newObjectId = st.nextToken();
	    	DomainObject domNewObj = DomainObject.newInstance(context, newObjectId);
		   	Part part = (Part) DomainObject.newInstance(context, DomainConstants.TYPE_PART, DomainConstants.ENGINEERING);
            String selectedIncludeRelatedData = getStringValue(requestMap, "hdnSelectedCloneOptions"); // contains all selected options in UI with comma seperator.

	    	if (isValidData(selectedIncludeRelatedData)) {
				StringList listRelatedData = FrameworkUtil.split(selectedIncludeRelatedData, ",");
				boolEBOMRelSelected   = listRelatedData.remove(RELATIONSHIP_EBOM); // As EBOM is replicate, It will be connected already.If user unchecks the option need to disconnect.
				boolEBOMSubSelected   = listRelatedData.remove(RELATIONSHIP_COMPONENT_SUBSTITUTION);
				boolRefDocRelSelected = listRelatedData.remove(RELATIONSHIP_REFERENCE_DOCUMENT);// As Reference Document is replicate, It will be connected already.If user unchecks the option need to disconnect.
				//Added for MCC convergence
				if(listRelatedData.contains(RELATIONSHIP_COMPONENT_MATERIAL))
					boolComponentMaterial = listRelatedData.remove(RELATIONSHIP_COMPONENT_MATERIAL);
				selectedIncludeRelatedData = (listRelatedData.size() > 0) ? FrameworkUtil.join(listRelatedData, ",") : "";
	    	}

		   	if (!boolEBOMRelSelected && !boolEBOMSubSelected) {
		   		relToDisconnect = RELATIONSHIP_EBOM; // If user has deselected EBOM option collect the rel to disconnect
		   	}

		   	if (!boolRefDocRelSelected) {
		   		relToDisconnect = (relToDisconnect == null) ? RELATIONSHIP_REFERENCE_DOCUMENT : (relToDisconnect + "," + RELATIONSHIP_REFERENCE_DOCUMENT); // If user has deselected Reference Doc option collect the rel to disconnect
		   	}
		   	
		   	//Added for MCC convergence
		   	
			if (!boolComponentMaterial) {
		   		relToDisconnect = (relToDisconnect == null) ? RELATIONSHIP_COMPONENT_MATERIAL : (relToDisconnect + "," + RELATIONSHIP_COMPONENT_MATERIAL); // If user has deselected Component Material option collect the rel to disconnect
		   	}
			
			if (isValidData(selectedIncludeRelatedData)) {
				Part commonPart = new Part();
	            commonPart.cloneCommonPartStructure(context, parentId, newObjectId, selectedIncludeRelatedData); // connections like Spec, Spare, Alt, Equivalent,...
			}

	        if (boolEBOMSubSelected) {
	        	part.cloneECPartStructure(context, parentId, newObjectId, RELATIONSHIP_COMPONENT_SUBSTITUTION); // Connection for EBOM Substitute.
	        }
	        EBOMAutoSync.backupContextUserInfo(context);
	        ContextUtil.pushContext(context);

	        try {
	        	if (relToDisconnect != null) {
	    	   		MapList listToDisconnect = domNewObj.getRelatedObjects(context, relToDisconnect, QUERY_WILDCARD,
	       				null, new StringList(SELECT_RELATIONSHIP_ID), false, true, (short) 1, null, null, null, null, null);

	    	   		if (getListSize(listToDisconnect) > 0) {
	    	   			StringList relIdListToDisconnect = getDataForThisKey(listToDisconnect, SELECT_RELATIONSHIP_ID);

	    		   		MqlUtil.mqlCommand(context, "history off");
	    		   		try {
	    		   			DomainRelationship.disconnect(context, (String[]) relIdListToDisconnect.toArray(new String[0])); // To disconnect EBOM OR Reference Document if user unselects which is defaulty selected.
	    		   		} catch (Exception e) {
	    		   			throw e;
	    		   		} finally {
	    		   			MqlUtil.mqlCommand(context, "history on");
	    		   		}
	    	   		}
	    	   	}

	        	part.connectClonedObject(context, parentId, newObjectId); // For connecting Derived relationship.

		        if (isValidData(partFamily)) {
					String partFamilyId = getStringValue(requestMap, "PartFamilyOID");

					if (isValidData(partFamilyId)) {
						DomainRelationship.connect(context, DomainObject.newInstance(context, partFamilyId), RELATIONSHIP_CLASSIFIED_ITEM, domNewObj); // connecting PartFamily to Part.
					}
					
				DomainObject objParentId = new DomainObject(parentId);
				String PartSeriesON = (String)objParentId.getInfo(context, "interface["+ INTERFACE_PART_FAMILY_REFERENCE +"]");
				if("TRUE".equals(PartSeriesON)){
						domNewObj.setAttributeValue(context,ATTRIBUTE_REFERENCE_TYPE,SYMB_U);
					}
				
				}
		        
		        //Starts - EBOM Auto sync
		        if(isValidData(selectedIncludeRelatedData) && selectedIncludeRelatedData.indexOf("Part Specification") > -1 && !EBOMAutoSync.isAutoSyncDisabled(context)) {
		        	new EBOMAutoSync().syncParentAnditsEBOMToVPM(context, newObjectId, "1");// In order to synchronized the newly cloned part
		        }		        
		        //Ends - EBOM Auto sync

	        } catch (Exception ex) {
				throw ex;
			} finally {
				ContextUtil.popContext(context);
				EBOMAutoSync.removeContextUserInfo(context);
			}

    	}//End of while
    }

    /**
     * Method to check 3D command enable
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Boolean show3DToggleCommand(Context context, String[] args) throws Exception {
    	String s3DPlayshow = EnoviaResourceBundle.getProperty(context,"emxComponents.Toggle.3DViewer");
    	if(!EngineeringUtil.checkForDECorVPMInstallation(context) || "true".equalsIgnoreCase(s3DPlayshow)) {
            return false;
        }

    	String pref3DLive = PropertyUtil.getAdminProperty(context, "Person", context.getUser(), "preference_3DLiveExamineToggle");
    	boolean flag = ("Hide".equalsIgnoreCase(pref3DLive) || "".equalsIgnoreCase(pref3DLive));

    	if(flag) {
    		return EngineeringUtil.isReportedAgainstItemPart(context, args);
    	}

	    return flag;
    }

    /**
     * Method to check 3D command disable
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Boolean hide3DToggleCommand(Context context, String[] args) throws Exception {
    	String s3DPlayshow = EnoviaResourceBundle.getProperty(context,"emxComponents.Toggle.3DViewer");
        if(!EngineeringUtil.checkForDECorVPMInstallation(context) || "true".equalsIgnoreCase(s3DPlayshow)) {
            return false;
        }

    	String pref3DLive = PropertyUtil.getAdminProperty(context, "Person", context.getUser(), "preference_3DLiveExamineToggle");
    	boolean flag = "Show".equalsIgnoreCase(pref3DLive);

    	if(flag) {
    		return EngineeringUtil.isReportedAgainstItemPart(context, args);
    	}

	    return flag;


    }

    /**
     * Trigger method to check if VPLM product is Published to VPM if VPMVisible is true
     *
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public int checkIfPartSynchronized(Context context, String []args)  throws Exception {
	   	String partId = args[0];

    	DomainObject partObj = DomainObject.newInstance(context, partId);
    	
    	//BGP: In case the Release Process is Production, Do not execute the trigger program functionality
    	//if(!ReleasePhase.isECPartWithDevMode(context, partObj))
    		//return 0;
    	//BGP: In case the Release Process is Production, Do not execute the trigger program functionality

    	//String vplmVisible = partObj.getInfo(context, "attribute["+EngineeringConstants.ATTRIBUTE_VPM_VISIBLE+"].value");
    	StringList busSelects = new StringList();
		busSelects.add("attribute["+EngineeringConstants.ATTRIBUTE_VPM_VISIBLE+"].value");
		busSelects.add(ATTRIBUTE_RELEASE_PHASE_VALUE);
		
		Map dataMap = partObj.getInfo(context, busSelects);
		
		String vplmVisible = (String)dataMap.get("attribute["+EngineeringConstants.ATTRIBUTE_VPM_VISIBLE+"].value");
		String sRelProcessValue = (String)dataMap.get(ATTRIBUTE_RELEASE_PHASE_VALUE);
    
        if("true".equalsIgnoreCase(vplmVisible) && ("Development".equals(sRelProcessValue) || "Production".equals(sRelProcessValue))) { 
//        if("true".equalsIgnoreCase(vplmVisible)) {
            String productId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",partId,"from["+RELATIONSHIP_PART_SPECIFICATION+"|to.type.kindof["+EngineeringConstants.TYPE_VPLM_CORE_REF+"]]" + ".to.id");

            if(productId == null || "".equals(productId)){
                String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.alert.PublishToVPM",
                    context.getSession().getLanguage());
                emxContextUtil_mxJPO.mqlNotice(context,strMessage);
                return 1;
            }
        }

        return 0;
    }

    /**
     * Trigger method to check if the VPLM product is in Released state.
     * Part promotion is possible if product is in Released or Obsolete state, if VPM Visible = true.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public int checkVPMProductInReleasedState(Context context, String []args)  throws Exception {
    	String partId = args[0];
    	DomainObject partObj = DomainObject.newInstance(context, partId);

    	//BGP: In case the Release Process is Production, Do not execute the trigger program functionality
    	if(!ReleasePhase.isECPartWithDevMode(context, partObj))
    		return 0;
    	//BGP: In case the Release Process is Production, Do not execute the trigger program functionality

    	String vplmVisible = partObj.getInfo(context, "attribute["+EngineeringConstants.ATTRIBUTE_VPM_VISIBLE+"].value");

        if("true".equalsIgnoreCase(vplmVisible)) {
            String productId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",partId,"from["+RELATIONSHIP_PART_SPECIFICATION+"|to.type.kindof["+EngineeringConstants.TYPE_VPLM_CORE_REF+"]]" + ".to.id");

            //Modified for IR-219370 start
            if(UIUtil.isNotNullAndNotEmpty(productId)) {
            	DomainObject dObj = DomainObject.newInstance(context, productId);
            	String strPolicy = (dObj.getPolicy(context)).toString();

            	if(strPolicy.equalsIgnoreCase(EngineeringConstants.POLICY_VPLM_SMB_DEFINITION)) {
	            	String targetState = PropertyUtil.getSchemaProperty(context, "policy",
	            			strPolicy , args[1]);
	            	//Modified for IR-219370 end
	                if (!PolicyUtil.checkState(context, productId, targetState, PolicyUtil.GE)) {
	                    String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.alert.releaseVPMProduct",
	                        context.getSession().getLanguage());
	                    emxContextUtil_mxJPO.mqlNotice(context,strMessage);
	                    return 1;
	                }
            	}
            }
        }
        return 0;
    }
 /**
     * Trigger method to check if the VPLM product is in Released state.
     * Part promotion is possible if product is in Released or Obsolete state, if VPM Visible = true.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public int ensureVPMProductIsUptoDate(Context context, String []args)  throws Exception {
        String partId = args[0];
        DomainObject partObj = DomainObject.newInstance(context, partId);
        String vplmVisible = partObj.getInfo(context, "attribute["+EngineeringConstants.ATTRIBUTE_VPM_VISIBLE+"].value");

        if("true".equalsIgnoreCase(vplmVisible)) {
        	
            String productId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",partId,"from["+RELATIONSHIP_PART_SPECIFICATION+"|to.type.kindof["+EngineeringConstants.TYPE_VPLM_CORE_REF+"]]" + ".to.id");

            //Modified for IR-219370 start
            if(UIUtil.isNotNullAndNotEmpty(productId)) {
            	String prodForPrevPart = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",partId,"previous.from["+RELATIONSHIP_PART_SPECIFICATION+"|to.type.kindof["+EngineeringConstants.TYPE_VPLM_CORE_REF+"]]" + ".to.id");
            	if(productId.equalsIgnoreCase(prodForPrevPart)) {
                    String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.alert.releaseVPMProduct",
	                        context.getSession().getLanguage());
                        strMessage = "VPM Product connected to this part is not up to date, please synchronize this part and then proceed";
	                    emxContextUtil_mxJPO.mqlNotice(context,strMessage);
	                    return 1;

            	}
            }
        }
        return 0;
    }	
	
/**
 * Returns alert message for Sync Operation of BOM Compare
 * @param context the eMatrix <code>Context</code> object
 * @param obj DomainObject
 * @return String containing Alert Message for Sync Operation
 * @throws Exception
 */
    public String getErrorMessageForBOMCompareSync(Context context,  DomainObject obj) throws  Exception{
    	Map doPartMap=obj.getInfo(context, new StringList( new String[]{SELECT_TYPE,SELECT_NAME,SELECT_REVISION,SELECT_POLICY,SELECT_CURRENT}));
    	String CurrentState=(String)doPartMap.get(SELECT_CURRENT);
    	String sType = (String)doPartMap.get(SELECT_TYPE);
    	String sName = (String)doPartMap.get(SELECT_NAME);
    	String sRev =  (String)doPartMap.get(SELECT_REVISION);
    	String strPolicy=(String)doPartMap.get(SELECT_POLICY);
    	String langStr = context.getSession().getLanguage();
    	String policyClassification = EngineeringUtil.getPolicyClassification(context,strPolicy);
    	String sErrorMsg=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.BOM.ModifyError",langStr);
    	if ("Unresolved".equalsIgnoreCase(policyClassification))
    	{
    		sErrorMsg=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.BOM.ModifyErrorConfiguredPart1",langStr);
    		sErrorMsg  +="\n"+ sType + " " + sName+ " " + sRev;
    	} else if("Development".equalsIgnoreCase(policyClassification)){
    		sErrorMsg ="";
    	} else if ("Production".equalsIgnoreCase(policyClassification)){
    		sErrorMsg  +="\n"+ sType + " " + sName+ " " + sRev + " " +
    		EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EBOM.ParentInReleaseError1",langStr) +
    		CurrentState + ". " +
    		EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EBOM.ParentInReleaseError2",langStr);
    	} else {
    		sErrorMsg ="";
    	}
    	return sErrorMsg;
    }
    /***
     * This method is used for displaying Classification Attributes, when only LBC is installed. If not
     * it returns an empty MapList. Invoked LBC Specific JPO methods to retrive all the Classification
     * Attributes.
     * @param context
     * @param args
     * @return MapList
     * @throws FrameworkException
     * @throws MatrixException
     */
     public MapList displayClassificationAttributes(Context context,String args[]) throws FrameworkException,MatrixException{
  	   MapList fieldMapList=new MapList();
  	   try{
	    	   if(isLBCInstalled(context, args)){
	    		   fieldMapList=(MapList)JPO.invoke(context, "emxLibraryCentralClassificationAttributes", null, "displayClassificationAttributesDuringCreate", args, MapList.class);
	    	   }
  	   }catch (Exception e) {
  		   e.printStackTrace();
		}
  	   return fieldMapList;
     }

	 //Added for RDO Convergence Start
    /* This method verifies all the part is connected to same Organization(AltOwner1).
	 * if the given array of part is connected to same Organizationa it returns Altowner1 name, else it returns null.
	*/

  public String getPartorg(Context context, String[] objectIds) throws Exception {
  	String strOrg = "";

  	if (objectIds != null) {
  		String strOrgTemp;

  		MapList dataMapList = DomainObject.getInfo(context, objectIds, new StringList("altowner1"));

  		for (int i = 0, size = dataMapList.size(); i < size; i++) {
  			strOrgTemp = (String) ((Map) dataMapList.get(i)).get("altowner1");

  			if (isValidData(strOrgTemp)) {
  				if ("".equals(strOrg)) {
  					strOrg = strOrgTemp;
  				} else if (!strOrg.equals(strOrgTemp)) {
  					strOrg = null;
					break;
				}
			}
  		}
  	}
  	return strOrg;
  }
    //Added for RDO Convergence End

  /**
   * Method to disable RDO field for "Manufacturing Part" policy parts
   * @param context
   * @param args
   * @return boolean
   * @throws Exception
   */
  public boolean displayRDOField(Context context, String[] args) throws Exception {
	//unpacking the Arguments from variable args
	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	String strCreateMode = (String)programMap.get("createMode");
	String strType = (String)programMap.get("type");
	String mfgType = PropertyUtil.getSchemaProperty(context, "type_ManufacturingPart");
	//added below condition for 223568V6R2014
	if(strType.indexOf(',')!=-1 && !strType.startsWith("_selectedType")){
	   strType = strType.substring(0, strType.indexOf(','));
	}
	StringList mfgTypeList = FrameworkUtil.split(MqlUtil.mqlCommand(context, "print type $1 select derivative dump", mfgType),",");

	if(strType.indexOf("_selectedType:")!= -1)
		strType = strType.substring(strType.indexOf("_selectedType:")+14,strType.indexOf(','));
	else if(strType.indexOf("type_")!= -1 && strType.indexOf("type_")==0)
		strType = PropertyUtil.getSchemaProperty(context, strType);

	mfgTypeList.addElement(mfgType);
	return !("MFG".equalsIgnoreCase(strCreateMode) && mfgTypeList.contains(strType));
  }

  /**
	 * Update program to connect the affected Item with the CO
	 *
	 * @param context
	 * @param args
	 * @throws Exception
	 * @Since R216
	 */
	public void connectAffectedItemToCO(Context context, String[] args) throws Exception {
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		HashMap progMap = (HashMap) paramMap.get("paramMap");

		String strChangeId = (String) progMap.get("New OID");

		String strPartId = (String) progMap.get("objectId");
		StringList slAffectedPart = new StringList();
		slAffectedPart.add(strPartId);

		if (UIUtil.isNotNullAndNotEmpty(strChangeId)) {
			try {
				ChangeOrder changeOrder = new ChangeOrder(strChangeId);
		        changeOrder.connectAffectedItems(context, slAffectedPart);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
  /**
	 * Program to Ensure Part connected to CA
	 *
	 * @param context
	 * @param args
	 * @throws Exception
	 * @Since R216
	 */
	public int ensureECOConnected(Context context, String[] args)
	  throws Exception
	  {
			String skipTriggerCheck = PropertyUtil.getRPEValue(context, "MX_SKIP_PART_PROMOTE_CHECK", false);
			if(skipTriggerCheck != null && "true".equals(skipTriggerCheck))
			{
				return 0;
			}
			//BGP: Check if Release Process is "Development Part" -> start
			String sObjectId = getId(context);
            DomainObject domPart = DomainObject.newInstance(context, sObjectId);
            StringList slObjSels = new StringList();
            slObjSels.addElement(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
            slObjSels.addElement(EngineeringConstants.SELECT_POLICY);
            slObjSels.addElement("attribute["+EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED + "].value");
            
            Map mPartMap = domPart.getInfo(context, slObjSels);
            if(EngineeringConstants.POLICY_EC_PART.equals((String)mPartMap.get(EngineeringConstants.SELECT_POLICY))
                     && EngineeringConstants.DEVELOPMENT.equals((String)mPartMap.get(EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE)) )
            {
              String changeControlled = (String)mPartMap.get("attribute["+EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED + "].value");
              if("False".equalsIgnoreCase(changeControlled))
                     return 0;
            }
			//BGP: Check if Release Process is "Development Part" -> end
			
			StringList selectStmts  = new StringList(1);
			selectStmts.addElement(SELECT_ID);

			String strRelPattern = RELATIONSHIP_AFFECTED_ITEM;
			

		String strTypePattern = DomainConstants.TYPE_ECO;
		
		Map proposedCAData  = com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil.getChangeObjectsInProposed(context, selectStmts, new String[]{sObjectId}, 1);
		MapList proposedchangeActionList = (MapList)proposedCAData.get(sObjectId);
		Map  realizedCAData = com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil.getChangeObjectsInRealized(context, selectStmts, new String[]{sObjectId}, 1);
		MapList realizedchangeActionList = (MapList)realizedCAData.get(sObjectId);
       		
		//BGP: Filter out Change orders added with Requested Change For Update. Check for impacted areas!
		String sRelWhere = ChangeConstants.SELECT_ATTRIBUTE_REQUESTED_CHANGE + " != '" + ChangeConstants.FOR_UPDATE + "'"; 
		MapList mapListECOs = getRelatedObjects(context,
		                        strRelPattern,
		                        strTypePattern, // object pattern
		                        selectStmts, // object selects
		                        null, // relationship selects
		                        true, // to direction
		                        false, // from direction
		                        (short) 1, // recursion level
		                        null, // object where clause
		                        sRelWhere); // relationship where clause

		if (mapListECOs.size() > 0 || proposedchangeActionList.size() > 0 || realizedchangeActionList.size() > 0)
		{
			return 0;
		}

			else
			{

				String langStr = context.getSession().getLanguage();
				
				String strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CheckIfChangeConnected.Message",langStr);
				emxContextUtil_mxJPO.mqlNotice(context,strMessage);
				return 1;
			}
		}
  /**
	 * Check function for edit part form
	 * @param context
	 * @param args
	 * @throws Exception
	 * @Since R216
	 */
      public Object checkForClone(Context context, String[] args)
      throws Exception
      {
    	  HashMap paramMap = (HashMap) JPO.unpackArgs(args);
    	  String editCheck = (String) paramMap.get("fromClone");
    		  if(editCheck!=null && !("".equals(editCheck)) && "true".equals(editCheck))
    		  {
    			  return Boolean.FALSE;
    		  }
    		  else
    			  return Boolean.TRUE;
      }

  	/**Retrieves the CO Name from Part Information
 	 *
 	 * @param context Context : User's Context.
 	 * @param String[] args
 	 * @return <code>Map</code>
 	 * @throws Exception
 	 */
 	/*public String getCONameForDisplay(Context context, String args[]) throws Exception {

 		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
 		HashMap progMap = (HashMap) paramMap.get("paramMap");
 		String strObjId = (String) progMap.get("objectId");
 		DomainObject dmObj = DomainObject.newInstance(context, strObjId);
 		String attrRequestedChange = PropertyUtil.getSchemaProperty(context,"attribute_RequestedChange");
 		String getCODisplay = "";
 		String SELECT_AffectedItem_Exist = "to[" + ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+ "]";
 		String SELECT_AffectedItem_CO = "to["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"|attribute["+attrRequestedChange+"]=='For Release'].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.name";
 		String SELECT_ImplementedItem_Exist = "to[" + ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM+ "]";
 		String SELECT_ImplementedItem_CO = "to["+ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM+"|attribute["+attrRequestedChange+"]=='For Release'].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.name";
 		String SELECT_AffectedItem_ECO = "to["+DomainConstants.RELATIONSHIP_AFFECTED_ITEM+"|attribute["+attrRequestedChange+"]=='For Release']].from.name";

 		StringList selectList = createStringList(new String[] {SELECT_AffectedItem_Exist,SELECT_AffectedItem_CO,SELECT_ImplementedItem_Exist,SELECT_ImplementedItem_CO,SELECT_AffectedItem_ECO});
 		Hashtable mapInfo = (Hashtable)dmObj.getInfo(context, selectList);

 			getCODisplay = (String)mapInfo.get("to["+ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.name");

 			if("True".equalsIgnoreCase((String)mapInfo.get(SELECT_ImplementedItem_Exist))&& !("True".equalsIgnoreCase((String)mapInfo.get(SELECT_AffectedItem_Exist))))
 			{
 				getCODisplay = (String)mapInfo.get("to["+ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM+"].from.to["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].from.name");
 			}
 			else if(getCODisplay == null){
 				getCODisplay = (String)mapInfo.get("to["+DomainConstants.RELATIONSHIP_AFFECTED_ITEM+"].from.name");
 			}
 		return getCODisplay;

 	}*/

	/**
     * Connects the Affected Items in ECR to CR.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECR/ECO object Id
     * New Value holds the newly selected Related ECR Object Id
     * @throws Exception if the operations fails
     * @since EC - X3
*/
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public int addAffectedItemstoCR(Context context, String[] args) throws Exception
	{
		String RELATIONSHIP_APPLIED_MARKUP = PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup");
		String RELATIONSHIP_PROPOSED_MARKUP = PropertyUtil.getSchemaProperty(context,"relationship_ProposedMarkup");

		StringList slAffectedItemsList = new StringList();
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap   = (HashMap)programMap.get("paramMap");
		HashMap requestMap= (HashMap)programMap.get("requestMap");

		String strCRId = programMap.containsKey("newObjectId") ?
			(String) programMap.get("newObjectId") : (String) paramMap.get("newObjectId");
		String strSourceECRId = programMap.containsKey("sourceECRId") ?
 			(String) programMap.get("sourceECRId") : (String) requestMap.get("sourceECRId");
 		String affectedItemsLists = programMap.containsKey("strSelectedAffectedItem") ?
	 	 	(String) programMap.get("strSelectedAffectedItem") : (String) requestMap.get("strSelectedAffectedItem");

		ChangeOrder changeOrder = new ChangeOrder(strCRId);

		StringTokenizer stToken = new StringTokenizer(affectedItemsLists, ",");
		while(stToken.hasMoreTokens()) {
			String strPartId = (stToken.nextToken()).trim();
			slAffectedItemsList.add(strPartId);
		}

		Map mCAMap = changeOrder.connectAffectedItems(context,slAffectedItemsList);
		HashMap mCAIdList = (HashMap)mCAMap.get("objIDCAMap");

		StringList objectSelects = new StringList();
		objectSelects.addElement("from["+RELATIONSHIP_EBOM_MARKUP+"].from.id");
		objectSelects.addElement("to["+RELATIONSHIP_AFFECTED_ITEM+"|from.id=="+strSourceECRId+"].id");
		objectSelects.addElement("from["+RELATIONSHIP_EBOM_MARKUP+"].to.to["+RELATIONSHIP_PROPOSED_MARKUP+"|from.id=="+strSourceECRId+"].to.id");
		objectSelects.addElement("from["+RELATIONSHIP_EBOM_MARKUP+"].to.to["+RELATIONSHIP_PROPOSED_MARKUP+"|from.id=="+strSourceECRId+"].id");

		String [] affectedItemsList = (String [])slAffectedItemsList.toArray(new String[slAffectedItemsList.size()]);
		MapList mlMarkupList = DomainObject.getInfo(context, affectedItemsList, objectSelects);
		Iterator itrMarkup = mlMarkupList.iterator();
		 while(itrMarkup.hasNext()) {
	         Map mMarkups = (Map)itrMarkup.next();
	         String strPartId = (String) mMarkups.get("from["+RELATIONSHIP_EBOM_MARKUP+"].from.id");
	         String strAffItemRelId = (String) mMarkups.get("to["+RELATIONSHIP_AFFECTED_ITEM+"].id");
	         String strMarkupRelId = (String) mMarkups.get("from["+RELATIONSHIP_EBOM_MARKUP+"].to.to["+RELATIONSHIP_PROPOSED_MARKUP+"].id");
	         String strMarkupId = (String) mMarkups.get("from["+RELATIONSHIP_EBOM_MARKUP+"].to.to["+RELATIONSHIP_PROPOSED_MARKUP+"].to.id");

	         if(UIUtil.isNotNullAndNotEmpty(strMarkupId) && UIUtil.isNotNullAndNotEmpty(strMarkupRelId)) {
				String[] sArrPartId = StringUtils.split(strPartId, "\\a");
				strPartId = sArrPartId[0];

				if(mCAIdList.containsKey(strPartId)) {
					String strCAId = (String)mCAIdList.get(sArrPartId[0]);
					DomainObject dCAObj = new DomainObject(strCAId);

					String[] sArrMarkupId = StringUtils.split(strMarkupId, "\\a");
					DomainRelationship.connect(context, dCAObj, RELATIONSHIP_APPLIED_MARKUP, true, sArrMarkupId);

					String[] sArrMarkupRelId = StringUtils.split(strMarkupRelId, "\\a");
					DomainRelationship.disconnect(context, sArrMarkupRelId);
				}
	         }
	         DomainRelationship.disconnect(context, strAffItemRelId);
         } // End of While Loop
		return 0;
	}
	/**To disable Mass Change option for Highest Level
	 *
	 * @param context Context : User's Context.
	 * @param String[] args
	 * @return <code>Map</code>
	 * @throws Exception
	 */
	public boolean getLevelForHighestCheck(Context context, String args[]) throws Exception {
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		String levelvalue = (String) paramMap.get("ENCPartWhereUsedLevel");

		return (!(levelvalue != null && "Highest".equalsIgnoreCase(levelvalue)));
	}
	public Object getClassCode(Context context, String[] args)
	throws Exception {
		String WithParent = EnoviaResourceBundle.getProperty(context,"emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.CompareBOM.WithParentClassCode");
		String WithoutParent = EnoviaResourceBundle.getProperty(context,"emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.CompareBOM.WithoutParentClassCode");
		StringBuffer strBuf = new StringBuffer(128);
		strBuf.append("<input type=radio checked name='parent' value='WithoutParent'>");
		strBuf.append(XSSUtil.encodeForHTML(context, WithoutParent)); //351866.0
		strBuf.append("&nbsp;&nbsp;");
		strBuf.append("<input type=radio name='parent' value='WithParent'>");
		strBuf.append(XSSUtil.encodeForHTML(context,WithParent)); //351866.0

		return strBuf.toString();
	}
	
	  /**
     * Establish Part Family Reference relationship between new rev part (Master/Reference Part) to old rev part (Master/Reference Part)
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *        0 - Part object id that is being revised
     * @throws Exception if the operation fails.
     * @since X3.
     */
    public void connectMasterPartsAndReferenceParts(Context context, String[] args)throws Exception {
    	String strPartId = args[0];
    	String sNewRevClassifiedItemRelId = "";
    	DomainObject dobj = null;
    	String strPreRevPartId = null;
    	String PartSeriesON = "";
    	String strResult = "";
    	StringList PART_FAMILY_REFERENCE_REL_ID = null;
		String strCommand = "";
		
		
    	DomainObject dobjRev = new DomainObject(strPartId);
    	
    	BusinessObject prevRevObj = dobjRev.getPreviousRevision(context);
    	if( prevRevObj != null &&!(prevRevObj.toString().trim().equals("..")) && !(prevRevObj.getTypeName().equals(""))){
    		strPreRevPartId = prevRevObj.getObjectId(context);
    	    dobj = new DomainObject(strPreRevPartId);
    	 
	        	
    	    PartSeriesON = (String)dobj.getInfo(context, "interface["+ INTERFACE_PART_FAMILY_REFERENCE +"]");
    	
    	    if("TRUE".equals(PartSeriesON)) {
    	   
    		   String sRefType = (String)dobj.getInfo(context, "attribute["+ ATTRIBUTE_REFERENCE_TYPE +"]");
    		   String sContextPartFamilyId = (String)dobj.getInfo(context, "relationship[Classified Item].tomid[Part Family Reference].torel.from.id");
    		  		
    		   StringList sNewRevClassifiedItemRelIdList = (StringList)dobjRev.getInfoList(context, "relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id");
    		   for(int i=0 ; i<sNewRevClassifiedItemRelIdList.size(); i++){
        		   sNewRevClassifiedItemRelId = (String)sNewRevClassifiedItemRelIdList.get(i);
        		   strResult = MqlUtil.mqlCommand(context,"print connection $1 select $2 dump",sNewRevClassifiedItemRelId,"from.id");
        		   if(strResult.equals(sContextPartFamilyId)){
        		    		break;
        		    			}
        		    		}  		
    		    		
    		
    		   if(SYMB_R.equals(sRefType)){  
				   strCommand = "modify connection $1 fromrel $2";			   
    			   PART_FAMILY_REFERENCE_REL_ID = (StringList)dobj.getInfoList(context, "relationship["+ RELATIONSHIP_CLASSIFIED_ITEM +"].frommid["+ RELATIONSHIP_PART_FAMILY_REFERENCE +"].id");
    			   for(int i=0 ; i<PART_FAMILY_REFERENCE_REL_ID.size(); i++){
        			   MqlUtil.mqlCommand(context,strCommand,(String)PART_FAMILY_REFERENCE_REL_ID.get(i),sNewRevClassifiedItemRelId);
        			 }    			   
    		     }
    		   if(SYMB_M.equals(sRefType)){
    			   strCommand = "modify connection $1 torel $2";
    			   PART_FAMILY_REFERENCE_REL_ID = (StringList)dobj.getInfoList(context, "relationship["+ RELATIONSHIP_CLASSIFIED_ITEM +"].tomid["+ RELATIONSHIP_PART_FAMILY_REFERENCE +"].id");
    			   for(int i=0 ; i<PART_FAMILY_REFERENCE_REL_ID.size(); i++){
    				   MqlUtil.mqlCommand(context,strCommand,(String)PART_FAMILY_REFERENCE_REL_ID.get(i),sNewRevClassifiedItemRelId);
    			     }    			   
    		     }
    	    }
        }
    
	}
    
    /** Program HTML Output for name column in alternate tab. 
     * This program is used to display different icons for Part
     * and MEP along with part name.
	 * @param context Context : User's Context.
	 * @param String[] args : Arguments
	 * @return <code>StringList</code> of HTML elements for 
	 * displaying name column with icon image.
	 * @throws Exception
	 */
    public StringList getAlternatePartName(Context context, String[] args)
    throws Exception {
    	
    	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
	    HashMap paramList = (HashMap) paramMap.get("paramList");
	    MapList objectList = (MapList) paramMap.get("objectList");
	    String parentOID = (String) paramList.get("objectId");
	    String suiteDir = (String) paramList.get("SuiteDirectory");
	    String suiteKey = (String) paramList.get("suiteKey");
	    String jsTreeID = (String) paramList.get("jsTreeID");
	    
	    StringList partNameList = new StringList(objectList.size());
	    String imgSrcBeg = "<img src='../common/images/";
	    String imgSrcEnd = "' border='0' alt='*'/>";
	     String nameLinkSrcBeg = new StringBuilder("<a href=\"JavaScript:emxTableColumnLinkClick('../engineeringcentral/emxEngRMBIntermediate.jsp?emxSuiteDirectory=")
	    .append(XSSUtil.encodeForJavaScript(context,suiteDir))
	    .append("&amp;suiteKey=")
	    .append(XSSUtil.encodeForJavaScript(context,suiteKey)+"&amp;PartRelatedObjectsView=true")
	    .append("&amp;jsTreeID=")
	    .append(XSSUtil.encodeForJavaScript(context,jsTreeID))
	    .append("&amp;parentOID=")
	    .append(XSSUtil.encodeForJavaScript(context,parentOID))
	    .append("&amp;objectId=").toString();
	     String nameLinkSrcEnd = "','700','600','false','slidein')\" class=\"object\">";
	    
	    for(int i=0; i<objectList.size();i++){
	    	
	    	Map<String,String> part = (Map)objectList.get(i);
	    	
	    	String sTypeIcon = POLICY_MANUFACTURER_EQUIVALENT.equals(part.get(SELECT_POLICY))? "iconSmallMEP.gif" : UINavigatorUtil.getTypeIconProperty(context,part.get(SELECT_TYPE));
	    	
	    	String imgSrc =  imgSrcBeg + sTypeIcon + imgSrcEnd;
	    	String nameLinkSrc = imgSrc + nameLinkSrcBeg + part.get(SELECT_ID)+ nameLinkSrcEnd + part.get(SELECT_NAME) + "</a>"; 
	    	
	    	partNameList.add(nameLinkSrc);
	    }
    	
	    return partNameList;
    }	
	
    
    /** Program HTML Output for name column in alternate tab. 
     * This program is used to display different icons for Part
     * and MEP along with part name.
	 * @param context Context : User's Context.
	 * @param String[] args : Arguments
	 * @return <code>StringList</code> of HTML elements for 
	 * displaying name column with icon image.
	 * @throws Exception
	 */
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPartRelatedItems(Context context,String[] args) throws Exception
    {	
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	String objectId = (String) programMap.get("objectId");
    	String parentOID = (String) programMap.get("parentOID");
    	
    	if(!objectId.equals(parentOID)){
    		return new MapList(1); 
    	}
    	
    	Pattern relPattern = new Pattern(null); 
    	MapList relatedItems = null;
    	
    	try {
    		
    		StringList relationsOfPart = FrameworkUtil.split(EnoviaResourceBundle.getProperty(context, "eServiceEngineeringCentral.PartRelatedObjects"), ",");
    		Iterator<String> itrRel = relationsOfPart.iterator();
    		while(itrRel.hasNext()){ 
    			String sReleationshipName = itrRel.next();
    			if(!"relationship_ChangeAffectedItem".equals(sReleationshipName) && (!"relationship_ImplementedItem".equals(sReleationshipName))){    			
    				relPattern.addPattern(PropertyUtil.getSchemaProperty(context, sReleationshipName));
    			}
    		
    		}

    		DomainObject domobj = DomainObject.newInstance(context, objectId);
    		
    		relatedItems = domobj.getRelatedObjects(context, 
    				relPattern.getPattern(),
					QUERY_WILDCARD,
					new StringList(SELECT_ID),
					new StringList(SELECT_RELATIONSHIP_ID),
					true,
					true,
					(short) 1,
					null,
					null, 0);
/* Refactored for handling new ECM datamodel */
    		
    		if (relationsOfPart.contains("relationship_ChangeAffectedItem")){
    			String[] idArr = {objectId};

        		Map objectMap = ChangeUtil.getChangeObjectsInProposed(context,new StringList(SELECT_ID),idArr,1);
        		MapList changeActionList = (MapList)objectMap.get(objectId);
	        	for(int i =0;i<changeActionList.size();i++){
	        		Hashtable hmap = (Hashtable)changeActionList.get(i);
	        		relatedItems.add(hmap);
        		}
    		}
    		if (relationsOfPart.contains("relationship_ImplementedItem")){
    			String[] idArr = {objectId};

        		Map objectMap = ChangeUtil.getChangeObjectsInRealized(context,new StringList(SELECT_ID),idArr,1);
        		MapList changeActionList = (MapList)objectMap.get(objectId);
	        	for(int i =0;i<changeActionList.size();i++){
	        		Hashtable hmap = (Hashtable)changeActionList.get(i);
	        		relatedItems.add(hmap);
        		}
    		}
    		/*End Refactoring*/
    		
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    	return relatedItems;
    }
    
	public String getPartConnectedProduct(Context context, String[] args)throws Exception {

	Map programMap = (HashMap) JPO.unpackArgs(args);
	Map paramMap = (HashMap) programMap.get("paramMap");
	String strObjectId = (String) paramMap.get("objectId");

	String sProduct = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",strObjectId,"to["+PropertyUtil.getSchemaProperty(context,"relationship_GBOM")+"|from.type=='"+PropertyUtil.getSchemaProperty(context,"type_HardwareProduct")+"'].from.name","|");
	if (UIUtil.isNotNullAndNotEmpty(sProduct))
		return sProduct;
	return "";
}	
	/* This method is used to diaplay default Part policy value - 16x-UI Enhancement*/
	public HashMap getDefaultPartPolicyValue(Context context, String[] args) throws Exception
	{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestMap = (HashMap)paramMap.get("requestMap");
		String languageStr = (String) requestMap.get("languageStr");

		HashMap defaultMap = new HashMap();
		String defaultVal= PropertyUtil.getSchemaProperty(context,"policy_ECPart");
		String strType = com.matrixone.apps.framework.ui.UINavigatorUtil.getAdminI18NString("Policy", defaultVal, languageStr);
		defaultMap.put("Default_AddNewRow",defaultVal);
		defaultMap.put("Default_AddNewRow_Display",strType);
		return defaultMap;
	}

	/* This method is used to diaplay default Part name value - 16x-UI Enhancement*/
	public HashMap getDefaultPartNameValue (Context context,String[] args)throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap  = (HashMap)programMap.get("requestMap");
		String languageStr = (String) paramMap.get("languageStr");

		String displayValue=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.AddNewInline.ASize",languageStr);
		String actualValue=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.AddNewInline.ASize","en");

		HashMap defaultMap = new HashMap();
		defaultMap.put("Default_AddNewRow", actualValue);
		defaultMap.put("Default_AddNewRow_Display",displayValue);
		return defaultMap;
	}
	//access function to show new Part Inline As a child, Existing part inline as child and Undo from Part RMB only in places where showRMBCommands=true
	public boolean showInlineRMBCommands(Context context, String[] args)
	throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String selectedProgram = (String)programMap.get("selectedProgram");
	    if("emxPart:getStoredEBOM".equals(selectedProgram)){
       		return Boolean.FALSE;
       	}
		String showRMBCommands    = (String) programMap.get("showRMBInlineCommands");
		Map sSETTINGS    = (Map) programMap.get("SETTINGS");
		String sComName = (String)sSETTINGS.get("Command Name");
		String frmRMB    = (String) programMap.get("frmRMB");
		String currentDisplayView    = (String) programMap.get("displayView");
		String toolbar    = (String) programMap.get("toolbar");
		String rmbObjectId    = (String) programMap.get("RMBID");
		String fromConfigBOM  = (String) programMap.get("fromConfigBOM");
		HashMap paramMap = new HashMap();
   	 	paramMap.put("fromConfigBOM", fromConfigBOM);
		boolean isConfigBOM = JPO.invoke(context, "emxENCActionLinkAccess", null, "showOrHideReplaceCommandsInConfigBOM", JPO.packArgs(paramMap),Boolean.class);
		if(UIUtil.isNotNullAndNotEmpty(sComName) && "true".equals(frmRMB)){
			if(!"ENCEditUndo".equalsIgnoreCase(sComName)){
				DomainObject domObj = UIUtil.isNotNullAndNotEmpty(rmbObjectId)?new DomainObject(rmbObjectId):null;
	  	        String currentState = UIUtil.isNotNullAndNotEmpty(rmbObjectId)?domObj.getInfo(context, DomainObject.SELECT_CURRENT): "";
	  	        if(isConfigBOM){
	  	        	return Boolean.FALSE;
	  	        }
			}
		}
		if("ENCOpenBOMMarkupToolBar".equals(toolbar) && !"ENCEditUndo".equalsIgnoreCase(sComName))
		{
			return !("true".equals(showRMBCommands) && "true".equals(frmRMB));
		}
		return ("true".equalsIgnoreCase(showRMBCommands));
	}

public String getLabel(Context context, String[] args) throws Exception{
	String sMessage =  EnoviaResourceBundle.getProperty(context ,
            "emxEngineeringCentralStringResource",
            context.getLocale(),"emxEngineeringCentral.Slidein.Message");
	if("true".equals(sMessage)){
		 sMessage =  EnoviaResourceBundle.getProperty(context ,
		            "emxEngineeringCentralStringResource",
		            context.getLocale(),"emxEngineeringCentral.Slidein.Message1");
	}
	return "<label for=\"MEP\"><i><font size = \"1\">"+sMessage+"</font></i></label>";
}
public boolean showLabel(Context context, String[] args) throws Exception{
	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	String sInitialLoad = (String)programMap.get("initialLoad");
	return ("true".equals(sInitialLoad));
}
public MapList getQueryLimitDynamic(Context context, String[] args) throws Exception{
	MapList mlSettingMapList = new MapList();
	String sDefaultValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentral.InitialLoad.QueryLimit");
	HashMap mlSettingMap = new HashMap();
	
	mlSettingMap.put("Default", sDefaultValue);
	mlSettingMap.put("Input Type", "textbox");
	mlSettingMap.put("Mode", "view");
	mlSettingMap.put("Registered Suite", "EngineeringCentral");
	mlSettingMap.put("Required", "true");
	mlSettingMap.put("Validate", "validateQueryLimit");
	
	Map formFieldMap = new HashMap();
	formFieldMap.put("label", "emxEngineeringCentral.FieldName.QueryLimit");
	formFieldMap.put("name", "QueryLimit");
	formFieldMap.put("settings", mlSettingMap);
	mlSettingMapList.add(formFieldMap);
	return mlSettingMapList;
}
public boolean hideOnFilter(Context context, String[] args) throws Exception{
	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	HashMap settingsMap = (HashMap)programMap.get("SETTINGS");
	String sFromForm = (String)programMap.get("fromForm");
	String scustomSearchCriteria = (String)programMap.get("customSearchCriteria");
	if("true".equals(scustomSearchCriteria))
	{
		if("true".equalsIgnoreCase(sFromForm))
			settingsMap.put("Image", "${COMMON_DIR}/images/iconActionFiltersApplied.gif");
		else
			settingsMap.put("Image", "${COMMON_DIR}/images/iconActionFilter.gif");
	
		return true;
	}
	return false;
}

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getFilteredChartData(Context context, String[] args)throws Exception {
		
		HashMap paramMap = (HashMap) JPO.unpackArgs(args);
		
		ChartUtil chartUtil = new ChartUtil(context);
		return chartUtil.getFilteredChartData(context, paramMap);
	}
	
public void startBackGroundJob(Context context, String[] args)
throws Exception {
	
}

public HashMap getDefaultRevisionOnPolicy (Context context,String[] args)throws Exception
   {
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       HashMap paramMap  = (HashMap)programMap.get("requestMap");
       String objectId = (String)paramMap.get("objectId");
    
       DomainObject domObj = DomainObject.newInstance(context, objectId);
       String policy = domObj.getInfo(context, SELECT_POLICY);
       String releasePhaseVal = domObj.getInfo(context, EngineeringConstants.ATTRIBUTE_RELEASE_PHASE_VALUE);
       
       Policy policyname = new Policy(policy);
       String revision = policyname.getFirstInSequence(context);
       
       if(EngineeringConstants.DEVELOPMENT.equalsIgnoreCase(releasePhaseVal) && DomainConstants.POLICY_EC_PART.equals(policy))
       {
    	   String sDevPartSequence=MqlUtil.mqlCommand(context,"print policy $1 select $2 dump",EngineeringConstants.POLICY_DEVELOPMENT_PART,"minorsequence");
    	   StringList sequence = FrameworkUtil.split(sDevPartSequence, ",");
    	   revision = (String)sequence.get(0);
       }
       
       HashMap defaultMap = new HashMap();
       defaultMap.put("Default_AddNewRow",revision);
       defaultMap.put("Default_ExistingRow",revision);
     
       return defaultMap;
   }
 
    //UOM Management -> start
	/**
	* Reload function for UOM field for EBOM SB and multi part summary table
	* @param context
	* @param args
	* @return
	* @throws Exception
	*/
	public HashMap getUOMValuesForEBOM(Context context, String[] args) throws Exception
	{	
		HashMap retMap= new HashMap();
		HashMap doc = new HashMap(); 
		String objectId = "";
		String sSelectedUOMType = "";
		HashMap request = (HashMap)JPO.unpackArgs(args);
		HashMap rowValues=(HashMap)request.get("rowValues");
		objectId = (String)rowValues.get("objectId");

		HashMap requestMap = (HashMap)request.get("requestMap");
		String sTableName = (String)requestMap.get("selectedTable");
		if(UIUtil.isNotNullAndNotEmpty(objectId))
		{
			DomainObject dmObj = DomainObject.newInstance(context, objectId);
			sSelectedUOMType = dmObj.getInfo(context, EngineeringConstants.SELECT_UOM_TYPE);
		}
		else
		{
			HashMap columnValues=(HashMap)request.get("columnValues");
			String sUomTypeColVal = (String)columnValues.get("UOMType");
			if(sUomTypeColVal!=null && !sUomTypeColVal.isEmpty())
				sSelectedUOMType = sUomTypeColVal;
		}
		
		HashMap mUOMValueMap = getMappingUOMValues(context, sSelectedUOMType, "EBOM");  
		StringList slUOMActualValList = (StringList) mUOMValueMap.get("RangeValues");
		StringList slUOMDisplayValList = (StringList) mUOMValueMap.get("RangeDisplayValues");
		
		retMap.put("RangeValues", slUOMActualValList);
		retMap.put("RangeDisplayValue", slUOMDisplayValList);
		//retMap.put("Input Type", "listbox");


		return retMap;	
	}
	
	/**
	* Reload function for UOM field for EBOM SB and multi part summary table
	* @param context
	* @param args
	* @return
	* @throws Exception
	*/
	public HashMap getUOMValuesForEBOMForMultiPartCreation(Context context, String[] args) throws Exception
	{	
		HashMap retMap= new HashMap();
		HashMap doc = new HashMap(); 
		String objectId = "";
		String sSelectedUOMType = "";
		HashMap request = (HashMap)JPO.unpackArgs(args);
		HashMap rowValues=(HashMap)request.get("rowValues");
		objectId = (String)rowValues.get("objectId");

		// For multi part creation summary page
		HashMap requestMap = (HashMap)request.get("requestMap");
		String sTableName = (String)requestMap.get("selectedTable");
		HashMap columnValues=(HashMap)request.get("columnValues");
		String sUomTypeColVal = (String)columnValues.get("UOMType");
		if(sUomTypeColVal!=null && !sUomTypeColVal.isEmpty())
			sSelectedUOMType = sUomTypeColVal;

		HashMap mUOMValueMap = getMappingUOMValues(context, sSelectedUOMType, "property");  
		StringList slUOMActualValList = (StringList) mUOMValueMap.get("RangeValues");
		StringList slUOMDisplayValList = (StringList) mUOMValueMap.get("RangeDisplayValues");		

		retMap.put("RangeValues", slUOMActualValList);
		retMap.put("RangeDisplayValue", slUOMDisplayValList);
		retMap.put("Input Type", "combobox");
		return retMap;
		
	}
	
	
	
	/**
	* Reload function for UOM form field for Part Create, edit and revision pages
	* @param context
	* @param args
	* @return
	* @throws Exception
	*/
	public Map updateUOMField(Context context, String[] args) throws Exception
	{
		Map argMap = (Map)JPO.unpackArgs(args);
		Map fieldValues = (Map)argMap.get("fieldValues");
		Map requestMap = (Map)argMap.get("requestMap");
		String sSelectedUOMType = (String) fieldValues.get("UOMType");
		String createMode  = (String)requestMap.get("createMode"); 
		
		//sSelectedUOMType = ((createMode).equals("EBOMReplaceNew") || (createMode).equals("UEBOMReplaceNew"))?(String)requestMap.get("UOMTypeOnNewPart"):sSelectedUOMType;
		//modified for IR-526695-3DEXPERIENCER2017x
		sSelectedUOMType = ("EBOMReplaceNew".equals(createMode) || "UEBOMReplaceNew".equals(createMode))?(String)requestMap.get("UOMTypeOnNewPart"):sSelectedUOMType;
		
		// Revision pages
		if(sSelectedUOMType == null || sSelectedUOMType.isEmpty() || sSelectedUOMType.equalsIgnoreCase("{}"))
		{
			String sObjectId = (String)requestMap.get("objectId");
			if(sObjectId == null || sObjectId.isEmpty())
			{
				sObjectId = (String)requestMap.get("copyObjectId");
				String sLatestRevision = getLatestRelRevision(context, sObjectId);
				if(!UIUtil.isNullOrEmpty(sLatestRevision))
					sObjectId = sLatestRevision;
			}
			
			DomainObject dmObj = DomainObject.newInstance(context, sObjectId);
			sSelectedUOMType = dmObj.getInfo(context, EngineeringConstants.SELECT_UOM_TYPE);
		}
				
		HashMap mUOMValueMap = getMappingUOMValues(context, sSelectedUOMType, "property");  
		StringList slUOMActualValList = (StringList) mUOMValueMap.get("RangeValues");
		StringList slUOMDisplayValList = (StringList) mUOMValueMap.get("RangeDisplayValues");
		
		if(slUOMActualValList.isEmpty())
		{
			if("Proportion".equalsIgnoreCase(sSelectedUOMType))
			{
				String sPropDefaultValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Unit_of_Measure.EA_(each)");
				slUOMDisplayValList.add(sPropDefaultValue);
				slUOMActualValList.add("EA_(each)");
			}
			else
			{
				slUOMActualValList.add("");  
				slUOMDisplayValList.add("");
			}
		}
		Map fieldMap = new HashMap();
		fieldMap.put("RangeValues", slUOMActualValList);
		fieldMap.put("RangeDisplayValues", slUOMDisplayValList);
		fieldMap.put("Input Type", "combobox");  

		return fieldMap;

	}
	
	/**
	*  Parser for loading UOM values from page object based on selected UOM Type
	* @param context
	* @param sUOMPageXMLValue
	* @param slUOMRangeList
	* @param sSelectedUOMType
	* @return
	* @throws Exception
	*/
	public HashMap getMappingUOMValues (Context context , String sSelectedUOMType, String display) throws Exception
	{			
		HashMap mReturnMap = new HashMap();
		
		String sUOMMappingXMLTag_UOMType = EngineeringConstants.UOM_MAPPING_XML_TAG_UOM_TYPE;
		String sUOMMappingXMLTag_UOMType_attribute_name = EngineeringConstants.UOM_MAPPING_XML_TAG_UOM_TYPE_ATTRIBUTE_NAME;
		String sUOMMappingXMLTag_Unit = EngineeringConstants.UOM_MAPPING_XML_TAG_UNIT;
		String sUOMMappingXMLTag_Unit_attribute_mode = EngineeringConstants.UOM_MAPPING_XML_TAG_UNIT_ATTRIBUTE_MODE;
		String Name1 = EngineeringConstants.UNIT_OF_MEASURE;
		
		String sUOMPageXMLValue = MqlUtil.mqlCommand(context, "print page $1 select $2 dump", "BOMUOMTypeMapping", "content");
		String sUOMrangeValues = MqlUtil.mqlCommand(context, "print attribute $1 select $2 dump ", DomainConstants.ATTRIBUTE_UNIT_OF_MEASURE, "range");
		String[] sListOfRangeVals = sUOMrangeValues.split(",");    
		StringList slUOMRangeList = new StringList(sListOfRangeVals);
		
		StringList slUOMListActual = new StringList();
		StringList slUOMListDisplay = new StringList();
		
		if(sUOMPageXMLValue!=null && !sUOMPageXMLValue.isEmpty())
		{
			SAXBuilder builder = new SAXBuilder();
			Document document = builder.build(new StringReader(sUOMPageXMLValue));
			Element root = document.getRootElement();

			List lUOMTypelIst = root.getChildren(sUOMMappingXMLTag_UOMType);
			for (int i = 0; i < lUOMTypelIst.size(); i++)
			{
				Element eUOMType = (Element)lUOMTypelIst.get(i);
				String sUOMTypeNameInXML = eUOMType.getAttributeValue(sUOMMappingXMLTag_UOMType_attribute_name);
				
				if(sUOMTypeNameInXML.trim().equals(sSelectedUOMType))
				{
					List lUOMValList = eUOMType.getChildren(sUOMMappingXMLTag_Unit);
					for (int j = 0; j < lUOMValList.size(); j++)
					{
						Element eUOM = (Element)lUOMValList.get(j);
						String sUOMNameInXML = eUOM.getTextTrim();						
						String sUOMNameInXML_custo = "= " + sUOMNameInXML;
						if(slUOMRangeList.contains(sUOMNameInXML_custo))
						{
							String sUOMModeInXML = eUOM.getAttributeValue(sUOMMappingXMLTag_Unit_attribute_mode);
							if(display.equalsIgnoreCase(sUOMModeInXML) || "both".equalsIgnoreCase(sUOMModeInXML))
							{
								slUOMListActual.add(sUOMNameInXML);
								String Rang1 = StringUtils.replace(sUOMNameInXML," ", "_");
								String attrName2 = "emxFramework.Range." + Name1 + "." + Rang1;
								String sUOMValIntValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),attrName2);
								slUOMListDisplay.add(sUOMValIntValue);
							}
						}
					}
				}
			}
		}
		
		mReturnMap.put("RangeValues", slUOMListActual);
		mReturnMap.put("RangeDisplayValues", slUOMListDisplay);
		return mReturnMap;
	}
	
	/**
	* Returns whether UOM Type field is editable or not in forms
	* @param context
	* @param args
	* @return
	* @throws Exception
	*/
	public boolean isUOMTypeEditable (Context context,String[] args) throws Exception
	{
		Map argMap = (Map)JPO.unpackArgs(args);
		Map settingsMap = (Map)argMap.get("SETTINGS");

		String sObjectId = (String)argMap.get("objectId");	
		String mode = (String)argMap.get("mode");
		if(UIUtil.isNullOrEmpty(mode))
			mode = (String)argMap.get("initialMode");
		if("view".equals(mode))
			return false;

		//Check 1: Check if the object has any previous released revision
		if(sObjectId == null || sObjectId.isEmpty())
		{
			sObjectId = (String)argMap.get("copyObjectId");		
			String sLatestRevision = getLatestRelRevision(context, sObjectId);
			if(sLatestRevision != null && !sLatestRevision.isEmpty())
			{
				DomainObject dmLatestRev = DomainObject.newInstance(context, sLatestRevision);
				StringList sAllRevisionState = new StringList();
				StringList sAllRevisionStateWithEqual = dmLatestRev.getInfoList(context, "revisions.current");
				for(int i = 0; i < sAllRevisionStateWithEqual.size();i++){
					StringList slTempList = FrameworkUtil.split(((String)sAllRevisionStateWithEqual.get(i)), "=");
					String sStateValue = (String)slTempList.get(1);
					sStateValue = StringUtil.Replace(sStateValue, " ", "");
					sAllRevisionState.add(sStateValue);
				}
						
				if(sAllRevisionState != null && (sAllRevisionState.contains(DomainConstants.STATE_PART_RELEASE) || sAllRevisionState.contains(EngineeringConstants.STATE_COMPLETE)))
				{
					settingsMap.put("Editable", "false");
					return true;
				}
				StringList hasEBOM = dmLatestRev.getInfoList(context, "revisions.to["+RELATIONSHIP_EBOM+"]");
				StringList hasEBOMPending = dmLatestRev.getInfoList(context, "revisions.to["+RELATIONSHIP_EBOM_PENDING+"]");
				if((hasEBOM != null && hasEBOM.size()>0 && hasEBOM.toString().indexOf("True") >-1) || (hasEBOMPending != null && hasEBOMPending.size()>0 && hasEBOMPending.toString().indexOf("True") >-1))
					settingsMap.put("Editable", "false");
			}
			return true;
		}
		
		DomainObject dmPartObj = DomainObject.newInstance(context, sObjectId);
		StringList sAllRevisionState = new StringList();
		StringList sAllRevisionStateWithEqual = dmPartObj.getInfoList(context, "revisions.current");
		/*for(int i = 0; i < sAllRevisionStateWithEqual.size();i++){
			StringList slTempList = FrameworkUtil.split(((String)sAllRevisionStateWithEqual.get(i)), "=");
			String sStateValue = (String)slTempList.get(1);
			sStateValue = StringUtil.Replace(sStateValue, " ", "");
			sAllRevisionState.add(sStateValue);
		}*/
		for(int i = 0; i < sAllRevisionStateWithEqual.size();i++){
			if(((String)sAllRevisionStateWithEqual.get(i)).indexOf("=")>-1){
				StringList slTempList = FrameworkUtil.split(((String)sAllRevisionStateWithEqual.get(i)), "=");
				String sStateValue = EMPTY_STRING;
				if(slTempList != null && slTempList.size()>1){
					sStateValue = (String)slTempList.get(1);
				}
				else {
					sStateValue = (String)slTempList.get(0);
				}
				sStateValue = StringUtil.Replace(sStateValue, " ", "");
				sAllRevisionState.add(sStateValue);
			}
			else {
				sAllRevisionState.add((String)sAllRevisionStateWithEqual.get(i));
			}
		}
		
		if(sAllRevisionState != null && (sAllRevisionState.contains(DomainConstants.STATE_PART_RELEASE) || sAllRevisionState.contains(EngineeringConstants.STATE_COMPLETE)))
		{
			settingsMap.put("Editable", "false");
			return true;
		}
		
		//Check 2: If the part is released, UOM Type will not be editable
		StringList sLSelList = new StringList();
		sLSelList.addElement(SELECT_CURRENT);
		sLSelList.addElement(EngineeringConstants.SELECT_UOM_TYPE);

		Map mObjectMap = dmPartObj.getInfo(context, sLSelList);
		String sObjectState = (String)mObjectMap.get(SELECT_CURRENT);
		if("Release".equalsIgnoreCase(sObjectState))
		{
			settingsMap.put("Editable", "false");
			return true;
		}

		//Check 3: If UOM Type is not defined. Allow the field to be edited
		String sUOMType = (String)mObjectMap.get(EngineeringConstants.SELECT_UOM_TYPE);
		if(UIUtil.isNullOrEmpty(sUOMType))
			return true;

		//Check 4: If the object is connected to any of the parent parts with EBOM relationships, UOM Type should be non-editable
		StringList hasEBOM = dmPartObj.getInfoList(context, "revisions.to["+RELATIONSHIP_EBOM+"]");
		StringList hasEBOMPending = dmPartObj.getInfoList(context, "revisions.to["+RELATIONSHIP_EBOM_PENDING+"]");
		if((hasEBOM != null && hasEBOM.size()>0 && hasEBOM.toString().indexOf("True") >-1) || (hasEBOMPending != null && hasEBOMPending.size()>0 && hasEBOMPending.toString().indexOf("True") >-1))
			settingsMap.put("Editable", "false");
		/*Pattern relPattern = new Pattern(RELATIONSHIP_EBOM);
		relPattern.addPattern(RELATIONSHIP_EBOM_PENDING);
		MapList mlparentEBOMList = dmPartObj.getRelatedObjects(context,
				relPattern.getPattern(),
				TYPE_PART,
				new StringList(SELECT_ID),
				new StringList(SELECT_RELATIONSHIP_ID),
				true,
				false,
				(short) 1,
				"",
				DomainConstants.EMPTY_STRING);

		if(mlparentEBOMList.size()>0)
			settingsMap.put("Editable", "false");
*/
		return true;
	}	

	/**
	 * Modify trigger to block the attempts made to change UOM Type attribute when the object is part of any EBOM assembly
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int checkForUOMTypeModification(Context context, String args[]) throws Exception
	{
		String sObjectId = args[0];
		BufferedWriter writer    = new BufferedWriter(new MatrixWriter(context));
		
		//Check if the part is part of any of the EBOM
		DomainObject dmPartObj = DomainObject.newInstance(context, sObjectId);
		StringList hasEBOM = dmPartObj.getInfoList(context, "revisions.to["+RELATIONSHIP_EBOM+"]");
		StringList hasEBOMPending = dmPartObj.getInfoList(context, "revisions.to["+RELATIONSHIP_EBOM_PENDING+"]");
		if((hasEBOM != null && hasEBOM.size()>0 && hasEBOM.toString().indexOf("True") >-1) || (hasEBOMPending != null && hasEBOMPending.size()>0 && hasEBOMPending.toString().indexOf("True") >-1))
		{
			writer.write("-------Cannot modify UOM Type. Object is connected to a parent part------------");
			return 1;
		}
/*		Pattern relPattern = new Pattern(RELATIONSHIP_EBOM);
		relPattern.addPattern(RELATIONSHIP_EBOM_PENDING);
		MapList mlparentEBOMList = dmPartObj.getRelatedObjects(context,
				relPattern.getPattern(),
				TYPE_PART,
				new StringList(SELECT_ID),
				new StringList(SELECT_RELATIONSHIP_ID),
				true,
				false,
				(short) 1,
				DomainConstants.EMPTY_STRING,
				DomainConstants.EMPTY_STRING);

		if(mlparentEBOMList.size()>0)
		{
			writer.write("-------Cannot modify UOM Type. Object is connected to a parent part------------");
			return 1;
		}*/
		//Check if a previsous revision of the part exists and if it is released
		StringList sAllRevisionState = new StringList();
		StringList sAllRevisionStateWithEqual = dmPartObj.getInfoList(context, "revisions.current");
		for(int i = 0; i < sAllRevisionStateWithEqual.size();i++){
			StringList slTempList = FrameworkUtil.split(((String)sAllRevisionStateWithEqual.get(i)), "=");
			String sStateValue = (String)slTempList.get(1);
			sStateValue = StringUtil.Replace(sStateValue, " ", "");
			sAllRevisionState.add(sStateValue);
		}
		if(sAllRevisionState != null && (sAllRevisionState.contains(DomainConstants.STATE_PART_RELEASE) || sAllRevisionState.contains(EngineeringConstants.STATE_COMPLETE)))
		{
			writer.write("-------Cannot modify UOM Type. A previous released revision of the part exists------------");
			return 1;
		}
		
		return 0;
	}
	public HashMap getDefaultUOM (Context context,String[] args)throws Exception
	   {
	       
	       HashMap defaultMap = new HashMap();
	       Locale en = new Locale("en");
	       String actualValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", en,"emxFramework.Range.Unit_of_Measure.EA_(each)");
	       String displayValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Unit_of_Measure.EA_(each)");
	       defaultMap.put("Default_AddNewRow",actualValue);
	       defaultMap.put("Default_ExistingRow","");

	       defaultMap.put("Default_AddNewRow_Display",displayValue);
	       defaultMap.put("Default_ExistingRow_Display","");
	     
	       return defaultMap;
	   }
	public HashMap getDefaultUOMType (Context context,String[] args)throws Exception
	   {
	       
	       HashMap defaultMap = new HashMap();
	       Locale en = new Locale("en");
	       String actualValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", en,"emxFramework.Range.UOM_Type.Proportion");
	       String displayValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.UOM_Type.Proportion");
	       defaultMap.put("Default_AddNewRow",actualValue);
	       defaultMap.put("Default_ExistingRow","");

	       defaultMap.put("Default_AddNewRow_Display",displayValue);
	       defaultMap.put("Default_ExistingRow_Display","");
	     
	       return defaultMap;
	   }
	public boolean isUOMTypeEditableInTable(Context context, String args[]) throws Exception{
		Map argMap = (Map)JPO.unpackArgs(args);
		Map settingsMap = (Map)argMap.get("SETTINGS");
		String fromPage = (String)argMap.get("fromPage");
		if("MyEngineeringView".equals(fromPage))
			settingsMap.put("Editable", "false");
		return true;
	}
	
	 public Boolean updateUOM(Context context, String[] args) throws Exception
	  {
	      HashMap programMap = (HashMap)JPO.unpackArgs(args);
	      HashMap paramMap = (HashMap)programMap.get("paramMap");
	      String relId  = (String)paramMap.get("relId");
	      String newUOM = (String)paramMap.get("New Value");
	      if(UIUtil.isNotNullAndNotEmpty(newUOM) && "(percent)".equalsIgnoreCase(newUOM)){
	    	  newUOM = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Unit_of_Measure.%_(percent)");
	      }
		  if(UIUtil.isNotNullAndNotEmpty(relId)){
		      DomainRelationship domRel = new DomainRelationship(relId);
		      domRel.setAttributeValue(context, DomainConstants.ATTRIBUTE_UNIT_OF_MEASURE, newUOM);
		  }
	      return Boolean.TRUE;
	  }
	
		/**
		 * To get and display Unit Of Measure
		 * @param context the eMatrix <code>Context</code> object.
		 * @param args contains args passed from the table
		 * @return <code> StringList</code> returns list of strings representing the corresponding Unit Of Measure values
		 * @throws Exception
		 */

		public StringList getUOM(Context context, String[] args) throws Exception {
			StringList slMBOMStatus = new StringList();
			Map paramMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) paramMap.get("objectList");
			Iterator objectListItr = objectList.iterator();
			HashMap objectMap = new HashMap();
			String strUOM = "";
			String strRelId = "";
			while (objectListItr.hasNext()) {
				objectMap = (HashMap) objectListItr.next();
				strUOM = (String)objectMap.get(DomainConstants.SELECT_ATTRIBUTE_UNITOFMEASURE);
				if(UIUtil.isNotNullAndNotEmpty(strUOM))
				{
				slMBOMStatus.add(strUOM);
				}
				else
				{
				strRelId = (String)objectMap.get(DomainRelationship.SELECT_ID);
				if(UIUtil.isNotNullAndNotEmpty(strRelId))
				strUOM = DomainRelationship.getAttributeValue(context, strRelId, DomainConstants.ATTRIBUTE_UNIT_OF_MEASURE);
				if(UIUtil.isNotNullAndNotEmpty(strUOM))
					slMBOMStatus.add(strUOM);
				else
					slMBOMStatus.add("");
				}
			}
			return slMBOMStatus;
		}
	//UOM Management -> end
		
		/**
		 * To check for availability of Image associated with a Part.
		 * @param context the eMatrix Context object.
		 * @param args contains args passed from the table
		 * @return boolean true if image is available, else false.
		 * @throws Exception
		 */
			
		public boolean checkForImageAvailability(Context context, String[] args) throws Exception{
			HashMap programMap      = (HashMap) JPO.unpackArgs(args);
	        //HashMap paramMap        = (HashMap) programMap.get("paramMap");
	        String sOID             = (String) programMap.get("objectId");
	        DomainObject dObject    = new DomainObject(sOID); 
	        boolean returnValue=false;

	        // Get related Product Central images
	        StringList busSelects = new StringList();        
	        busSelects.add(DomainConstants.SELECT_ID);
	        busSelects.add(DomainConstants.SELECT_ORIGINATED);
	        
	        MapList mlImages = dObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_IMAGES, DomainConstants.TYPE_IMAGE, busSelects, null, false, true, (short)1, "", "", 0);                
	        
	        // Get related Image Holders
	        String attrPrimaryImage = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_attribute_PrimaryImage);
	        String relImageHolder = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_relationship_ImageHolder);
	        String typeImageHolder = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_type_ImageHolder);
	        boolean isSketchImageAvailable = false;
	        busSelects.add("attribute["+ attrPrimaryImage +"]");
	        
	        MapList mlImageHolders = dObject.getRelatedObjects(context, relImageHolder, typeImageHolder, busSelects, null, true, false, (short)1, "", "", 0);       
	        MapList mlSketches = dObject.getRelatedObjects(context, DomainObject.RELATIONSHIP_REFERENCE_DOCUMENT, DomainObject.TYPE_SKETCH, busSelects, null, false, true, (short)1, "", "", 0);
	        if(mlSketches.size() > 0) {
	            for (int i = 0; i < mlSketches.size(); i++) {
	                Map mSketch =(Map)mlSketches.get(i);
	                String sOIDSketch = (String)mSketch.get("id");
	                DomainObject doSketch = new DomainObject(sOIDSketch);
	                MapList mlSketchImages = doSketch.getRelatedObjects(context, relImageHolder, typeImageHolder, busSelects, null, true, false, (short)1, "", "", 0);
	                if(mlSketchImages.size() > 0) {
	                	isSketchImageAvailable = true;
	                }
	            }    
	         }
	        if(mlImages.size()>0 || mlImageHolders.size()>0 || isSketchImageAvailable)
	        	returnValue=true;
	        return returnValue;
		}
		
		public boolean displayNamefieldForAddExisting(Context context, String[] args) throws Exception{
			Map paramMap = (Map) JPO.unpackArgs(args);
			String calledMethod = (String)paramMap.get("calledMethod");
			if(UIUtil.isNotNullAndNotEmpty(calledMethod) && calledMethod.equalsIgnoreCase("addExisting")){
				return true;
			}
			return false;
		}
		
		public boolean displayNamefield(Context context, String[] args) throws Exception{
			return !displayNamefieldForAddExisting(context,args);
		}
		
		/**
		 * To check whether it is in view mode.
		 * @param context the eMatrix Context object.
		 * @param args contains args passed from the form.
		 * @return boolean true if it is in view mode, else false.
		 * @throws Exception
		 */
		public boolean UOMFieldView(Context context,String[] args) throws Exception {
			boolean retValue=false;
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String mode = (String)paramMap.get("mode");
			
			if(UIUtil.isNullOrEmpty(mode))
				mode = (String)paramMap.get("initialMode");
			
			if("view".equals(mode))
				retValue = true;
	
			return retValue;
		}
		
		/**
		 * To check whether it is in edit mode.
		 * @param context the eMatrix Context object.
		 * @param args contains args passed from the form.
		 * @return boolean true if it is in edit mode, else false.
		 * @throws Exception
		 */
		public boolean UOMFieldEdit(Context context,String[] args) throws Exception {
			boolean retValue=false;
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String mode = (String)paramMap.get("mode");
			
			if("edit".equals(mode))
				retValue = true;
	
			return retValue;
		}
		/**
		 * Column Program to get Products Physical ID Pattern ,Currently works only V6 data. 
		 * @param context the eMatrix Context object.
		 * @param args contains args passed from the Structure Browser , the expandEBOM method.
		 * @return Vector Containing product Physical id pattern.
		 * @throws Exception
		 */ 
		public Vector getPhysicalIdPath(Context context, String[] args) throws Exception {
			Vector vNameVector = new Vector();
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			Iterator mapListItr = objectList.iterator();
			Map partMap = null; 
			String strPhysicalIdPattern;
			String path;
			String sProductPhyID;
			String sVPLMInstanceRelId ="frommid["+EngineeringConstants.RELATIONSHIP_VPM_PROJECTION_RELID+"].torel.physicalid";
			String strProductID="from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"].to["+EngineeringConstants.TYPE_VPLM_VPMREFERENCE+"].physicalid";
			StringList sVPLMInstanceRelPhyIDs=null;

			while(mapListItr.hasNext())
			{
				path = "";
				strPhysicalIdPattern = "";

				partMap 		  = (Map)mapListItr.next();
				//for parent physicalID	 Pattern
				String sRootNode  = (String)partMap.get("Root Node");
				if("true".equalsIgnoreCase(sRootNode))
				{
					//to get Parent Product physical ID
					StringList busSelects = new StringList();
					busSelects.add(strProductID);

					DomainObject dmRootObj = DomainObject.newInstance(context, (String)partMap.get(EngineeringConstants.SELECT_ID));
					partMap = dmRootObj.getInfo(context, busSelects);
					String rootprdphysicalID=(String) partMap.get(strProductID);
					if(UIUtil.isNotNullAndNotEmpty(rootprdphysicalID))
						path =rootprdphysicalID+":3dPlayKeyIdPath";
				}
				else{
					sProductPhyID=(String) partMap.get(strProductID);	
					sVPLMInstanceRelPhyIDs=getListValue(partMap,sVPLMInstanceRelId);

					if(sVPLMInstanceRelPhyIDs.size()>0 && UIUtil.isNotNullAndNotEmpty(sProductPhyID)){
						for(int count=0;count<sVPLMInstanceRelPhyIDs.size();count++) {	
							if(UIUtil.isNullOrEmpty(strPhysicalIdPattern))
								strPhysicalIdPattern=sVPLMInstanceRelPhyIDs.get(count)+"/"+sProductPhyID;
							else
								strPhysicalIdPattern=strPhysicalIdPattern+","+sVPLMInstanceRelPhyIDs.get(count)+"/"+sProductPhyID;
						}
					}
					//construct the String to populate physical id pattern in BOM Table Physical ID column
					if(UIUtil.isNotNullAndNotEmpty(strPhysicalIdPattern)){
						String temp[] =strPhysicalIdPattern.split(",");

						for(int i=0;i<temp.length;i++){
							if (i > 0) { path += ","; }							
							path += temp[i] + ":3dPlayKeyIdPath";							
						}						
					}
				}
				vNameVector.add(path);	
			}     	
			return vNameVector;
		}
		
		//Added to fetch exalead Result for My Engineering View Table
		public MapList exaleadResult(Context context, ComplexRefinement lRefnSearch,String searchQueryLimit ,String searchType,String searchName) throws Exception{
			MapList mapList = new MapList(); 
			//Create Refinement for selected Type and add to Main refinement
			if(searchType!=null &&!searchType.isEmpty()){
				TaxonomyRefinement lRefnParts = new TaxonomyRefinement("TYPES", searchType, true);
				lRefnSearch.addRefinement(lRefnParts);
			}
			//add name refinement to Main Refinement
			lRefnSearch.addRefinement(new AttributeRefinement(DomainConstants.SELECT_NAME, searchName, AttributeRefinement.OPERATOR_EQUAL));	
			MapList maplist = new MapList();
			SearchRefinement[] refinements = new SearchRefinement[2];
			refinements[0]=lRefnSearch;
			//Support sorted by attribute
			SortRefinement lRefSort = new SortRefinement(DomainObject.SELECT_MODIFIED, false);		
			refinements[1] = lRefSort;
			XLSearch xlsearch =new XLSearch(context);
			int querylimit=Integer.parseInt(searchQueryLimit);
			int pageSize =100;
			if(querylimit < pageSize){
				pageSize = querylimit; 	
			}
			xlsearch.setPageSize(pageSize);

			SearchResult result = xlsearch.search(context, refinements, 0);	
			//treat records
			mapList= EngineeringUtil.getExaleadRecordsList(context,result,searchType,false);
			int totalObjCount=result.getTotalCount();
			float numberOfPages=0;
			if(totalObjCount>0){
				if(totalObjCount < pageSize){
					numberOfPages = 1;
				}else if(totalObjCount < querylimit){
					numberOfPages = (float)totalObjCount/pageSize;
				}else{
					numberOfPages = querylimit/pageSize;
				}
				numberOfPages = (float) Math.ceil(numberOfPages);	
			}

			for(int counter=1; counter< numberOfPages ;counter++)
			{
				SearchResult newresult = xlsearch.search(context, refinements, counter);	
				mapList.addAll(EngineeringUtil.getExaleadRecordsList(context,newresult,searchType,false));
			}
			return mapList;
		}

		/**
		 * To fetch the immediate child level data in a BOM .
		 * @param context the eMatrix Context object.
		 * @param objectId - the Object Id of the current object whose immediate child level data in the BOM is to be fetched.
		 * @param level - the level(integer value) of the current Object in the BOM.
		 * @param expandLevel - 0 means expand all, n means expand to the nth level. 
		 * @param sObjWhereCond - Where condition to fetch the related Objects.
		 * @return MapList containg the immediate child level data in the BOM.
		 * @throws Exception
		 */
		private MapList expandToOnelevel(Context context, String objectId,int level,int expandLevel,String sObjWhereCond)throws Exception {
			StringList selectStmts = new StringList(1);
			selectStmts.addElement("last.id");
			selectStmts.addElement(DomainConstants.SELECT_ID);
		    selectStmts.addElement(DomainConstants.SELECT_TYPE);
		    selectStmts.addElement(DomainConstants.SELECT_NAME);
		    selectStmts.addElement(DomainConstants.SELECT_REVISION);
		    selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
			selectStmts.addElement("from["+DomainConstants.RELATIONSHIP_EBOM+"]");
			
			StringList selectRelStmts=new StringList();
			selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
		    selectRelStmts.addElement(SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
		    selectRelStmts.addElement(SELECT_ATTRIBUTE_QUANTITY);
		    selectRelStmts.addElement(SELECT_ATTRIBUTE_FIND_NUMBER);
		    selectRelStmts.addElement(SELECT_ATTRIBUTE_COMPONENT_LOCATION);
		    selectRelStmts.addElement(SELECT_ATTRIBUTE_USAGE);
		    
			Part partObj = new Part(objectId);
			MapList ebomList = partObj.getRelatedObjects(context,
		            DomainConstants.RELATIONSHIP_EBOM,  // relationship pattern
		            DomainConstants.TYPE_PART,                  // object pattern
		            selectStmts,                 // object selects
		            selectRelStmts,              // relationship selects
		            false,                        // to direction
		            true,                       // from direction
		            (short)1,                    // recursion level
		            sObjWhereCond,     // object where clause
		             null);                       // relationship where clause
			MapList tempEBOMList=new MapList();  
			MapList tempMapList=null;
			Iterator itr = ebomList.iterator();
			
			while(itr.hasNext()) {
		        Map newMap = (Map)itr.next();
		        //String ObjectId = (String)newMap.get("id");
		        String lastRevId = (String)newMap.get("last.id");
		       	newMap.put("id",lastRevId);
		        newMap.put("level",Integer.toString(level+1));
		        tempEBOMList.add(newMap);
		        //templist need to be clear for every iteration to avoid caching privious iteration dat
		        if(tempMapList!=null)
		        	tempMapList.clear();
		        //for expand "All" 
		        if((expandLevel==0) &&"true".equalsIgnoreCase((String)newMap.get("from["+DomainConstants.RELATIONSHIP_EBOM+"]")))
			        //expand this object and iterate each object
			        tempMapList= expandToOnelevel(context,lastRevId,level+1,0,sObjWhereCond);  
		        //For specific level expand
		        if((expandLevel-1>0) &&"true".equalsIgnoreCase((String)newMap.get("from["+DomainConstants.RELATIONSHIP_EBOM+"]")))
			        //expand this object and iterate each object
			        tempMapList= expandToOnelevel(context,lastRevId,level+1,expandLevel-1,sObjWhereCond);     
		        if(tempMapList !=null && tempMapList.size()>0){
		        	for (int i=0;i<tempMapList.size();i++)
		        		tempEBOMList.add(tempMapList.get(i));
		        }
		        	
		   	}
			return tempEBOMList;
		}
		
public static Boolean isEditAllowedInFilteredView(Context context,String[] args) throws Exception
	    {
			Map argMap = (Map)JPO.unpackArgs(args);
			String sFilteredView = (String)argMap.get("ENCBOMRevisionCustomFilter");	
	    	
	    	if(sFilteredView != null && !sFilteredView.equals("As Stored")){
	    		return false;
	    	}
	    	return true;
	    }

		/**
		 * Notifies the new owner regarding the newly assigned ownership.
		 *
		 * @param context the eMatrix <code>Context</code> object.
		 * @param args holds the following input arguments:
		 *        0 - OBJECTID
		 *        1 - KINDOFOWNER
		 *        2 - OWNER
		 * @throws Exception if the operation fails.
		 * @trigger TypePartChangeOwnerAction.
		 */
		public int notifyNewOwner(Context context, String[] args)throws Exception {
			String kindOfOwner = args[1];
			if("owner".equalsIgnoreCase(kindOfOwner)){
				String strObjectId = args[0];	
				String newOwner = args[2]; 	
				String[] inputArgs = new String[8];
				inputArgs[0]= newOwner;
				inputArgs[1]= "emxEngineeringCentral.IconMail.Subject3";
				inputArgs[2]= "0";
				inputArgs[3]= "emxEngineeringCentral.IconMail.Message3";
				inputArgs[4]= "0";
				inputArgs[5]= strObjectId;
				inputArgs[6]= "";
				inputArgs[7]= "emxEngineeringCentralStringResource";
             
				JPO.invoke(context,"emxMailUtil",null,"sendNotificationToUser",inputArgs,void.class);
			}
			return 0;
	    }
			/**
	 * Trigger method to check if the user is having the necessary license for performing deletion of ENG Types.
	 * @param context
	 * @param args contains the Policy of the Business Object
	 * @return 1 for failure and 0 for success.
	 * @throws Exception
	 */
	public int deleteLicenseCheck(Context context, String[] args)
	      throws Exception
	    {
	       String policy = args[0];
	       Part part=new Part();
		       part.deleteLicenseCheck(context, policy);
		       return 0;	    	   
	    }
		
}
