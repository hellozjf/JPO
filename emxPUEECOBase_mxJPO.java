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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
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
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.effectivity.EffectivityFramework;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.Part;
import com.matrixone.apps.engineering.RelToRelUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.unresolvedebom.CFFUtil;
import com.matrixone.apps.unresolvedebom.UnresolvedEBOMConstants;

/**
 * The <code>emxPUEECOBase</code> class contains implementation code for emxPUEECO.
 *
 * @version EC Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxPUEECOBase_mxJPO extends emxChange_mxJPO
{
    /** state "Approved" for the "EC Part" policy. */
    public static final String STATE_ECPART_APPROVED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_EC_PART,
                                           "state_Approved");

    /** state "Release" for the "EC Part" policy. */
    public static final String STATE_ECPART_RELEASE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_EC_PART,
                                           "state_Release");


    /** state "Obsolete" for the "EC Part" policy. */
    public static final String STATE_ECPART_OBSOLETE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_EC_PART,
                                           "state_Obsolete");

    /** policy "CAD Drawing" */
    public static final String POLICY_CAD_DRAWING =
            PropertyUtil.getSchemaProperty("policy_CADDrawing");

    /** state "Approved" for the "CAD Drawing" policy. */
    public static final String STATE_CADDRAWING_APPROVED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_CAD_DRAWING,
                                           "state_Approved");

    /** state "Release" for the "CAD Drawing" policy. */
    public static final String STATE_CADDRAWING_RELEASE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_CAD_DRAWING,
                                           "state_Release");

    /** policy "CAD Model" */
    public static final String POLICY_CAD_MODEL =
            PropertyUtil.getSchemaProperty("policy_CADModel");

    /** state "Approved" for the "CAD Model" policy. */
    public static final String STATE_CADMODEL_APPROVED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_CAD_MODEL,
                                           "state_Approved");

    /** state "Release" for the "CAD Model" policy. */
    public static final String STATE_CADMODEL_RELEASE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_CAD_MODEL,
                                           "state_Release");

    /** state "Approved" for the "Drawing Print" policy. */
    public static final String STATE_DRAWINGPRINT_APPROVED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_DRAWINGPRINT,
                                           "state_Approved");

    /** state "Release" for the "Drawing Print" policy. */
    public static final String STATE_DRAWINGPRINT_RELEASE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_DRAWINGPRINT,
                                           "state_Release");

public static final String STATE_ECO_CANCELLED = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Cancelled");


   /** Relationship "ECO Change Request Input". */
    public static final String RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT =
    PropertyUtil.getSchemaProperty("relationship_ECOChangeRequestInput");

     //Relationship Affected Item.
    public static final String RELATIONSHIP_AFFECTED_ITEM = PropertyUtil.getSchemaProperty("relationship_AffectedItem");

    public static final String RESOURCE_BUNDLE_EC_STR = "emxUnresolvedEBOMStringResource";

    //added for the fix 366148
    public String strClear = "";
    //366148 fix ends
	
    public static final int LT = 0;
    public static final int GT = 1;
    public static final int EQ = 2;
    public static final int LE = 3;
    public static final int GE = 4;
    public static final int NE = 5;
	
    /** policy "ECR" */
    public static final String POLICY_ECR = PropertyUtil.getSchemaProperty("policy_ECR");

    /** Person Admin Person */
	String personAdminType = "person";

	/** name of preference properties */
	String PREFERENCE_ENC_DEFAULT_VAULT = "preference_ENCDefaultVault";

	/** name of preference properties */
	String PREFERENCE_DESIGN_RESPONSIBILITY = "preference_DesignResponsibility";

	private static final String delimiter = matrix.db.SelectConstants.cSelectDelimiter;

	/**
	* Relationship Prerequisite
	*/
	public static final String RELATIONSHIP_PREREQUISITE = PropertyUtil.getSchemaProperty("relationship_Prerequisite") ;

	/**
	* type PUE ECO
	*/
	public static final String TYPE_PUEECO = PropertyUtil.getSchemaProperty("type_PUEECO") ;

	EffectivityFramework effectivity  = new EffectivityFramework();//2011x

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     */
    public emxPUEECOBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
        //Added for the fix 366148
        strClear = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",context.getSession().getLanguage());
        //366148 fix ends
        //DebugUtil.setDebug(true);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return an int.
     * @throws Exception if the operation fails.
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxPUEECO invocation");
        }
        return 0;
    }

  /**
   * Display Related Part Name in the WebForm
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns String
   * @throws Exception if the operation fails
   */
        public String  displayRelatedPartName (Context context, String[] args) throws Exception {
         try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String strObjId = (String) requestMap.get("objectId");
            StringBuilder strBuilder = new StringBuilder(3);
            if (strObjId != null && strObjId.length() > 0)
            {
            setId(strObjId);
            DomainObject dPart = DomainObject.newInstance(context, strObjId);
           // getting the value
            String sPartName =  dPart.getInfo(context,DomainObject.SELECT_NAME);
            String sPartRevision =  dPart.getInfo(context,DomainObject.SELECT_REVISION);
            //displaying the part name and revision
            strBuilder.append(sPartName);
            strBuilder.append(" : Revision ");
            strBuilder.append(sPartRevision);
            }
            return strBuilder.toString();
        }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
    }

 /**
  * Contains the HTML code to display the distribution List field for PUEECO UI
  * @param context the eMatrix <code>Context</code> object.
  * @returns string that contains the HTML code to display the distribution List field for ECO UI.
  * @throws Exception if the operation fails.
  * @since Engineeringcentral X3
  */
    public String getDistributionList (Context context, String[] args) throws Exception {

        StringBuffer strBuf = new StringBuffer(2048);
        Locale Local = context.getLocale();
        String alertMsg = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.Common.DesignResponsibilityAlert");

        strBuf.append("<input type='text' name='DistributionListDisplay' value=''   readOnly='true'> </input>");
        strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript: showDistributionList();' > </input>");
        //Modified for the fix 366148
        strBuf.append("<a href=\"javascript:clearDistributionList()\">");
        strBuf.append(strClear);
        strBuf.append("</a>");
        //366148 fix ends
        strBuf.append("<input type ='hidden' name='DistributionListOID' value='Unassigned' > </input>");
        strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
        strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
        strBuf.append(" <script> ");
        strBuf.append("function showDistributionList() { ");
        strBuf.append(" var changeResId = document.emxCreateForm.DesignResponsibilityOID.value;");
        strBuf.append(" if (changeResId == null || changeResId == \"\" || changeResId == \" \") {");
        strBuf.append(" alert (\""+alertMsg+"\");");
        strBuf.append("} else {");
        strBuf.append("var designResName = document.emxCreateForm.DesignResponsibilityDisplay.value;");
        strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_MemberList:REL_MEMBERLIST_OWNINGORGANIZATION=\" + designResName + \":CURRENT=policy_MemberList.state_Active&amp;table=APPECMemberListsSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameDisplay=DistributionListDisplay&amp;fieldNameActual=DistributionListOID&amp;submitURL=../unresolvedebom/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\",850,630);");
        strBuf.append('}');
        strBuf.append(" } ");
        strBuf.append("function clearDistributionList() { ");
        strBuf.append(" document.emxCreateForm.DistributionListDisplay.value = \"\"; ");
        strBuf.append(" document.emxCreateForm.DistributionListOID.value     = \"\"; ");
        strBuf.append(" }");
        strBuf.append("</script>");

        return strBuf.toString();
    }

/**
     * Display the ReviewerList Item field in PUEECO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context PUEECO object Id
     * @throws Exception if the operations fails
     * @since EC - X3
     */
public Object  displayReviewerListItem(Context context,String[] args)throws Exception{
        StringBuffer strBuf = new StringBuffer();
        try{
            String strLanguage        =  context.getSession().getLanguage();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String relChange = (String) requestMap.get("OBJId");
            String strModCreate= (String)requestMap.get("CreateMode");

            //Modified for IR-064357V6R2011x
            String sDisplayValue = "";
            String sHiddenValue  = "";

            String strRelationship = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
            if(relChange!=null)
            {
            DomainObject domObj = new DomainObject(relChange);
            if(domObj.isKindOf(context, DomainConstants.TYPE_ECR) || domObj.isKindOf(context, DomainConstants.TYPE_ECO) || domObj.isKindOf(context, DomainConstants.TYPE_PART)){
                //Business Objects are selected by its Ids
                StringList objectSelects = new StringList();
                objectSelects.addElement(DomainConstants.SELECT_NAME);
                objectSelects.addElement(DomainConstants.SELECT_ID);
                //Stringlist containing the relselects
                StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
            String sObjectName ="";
            String sObjectId ="";
            if((strModCreate!=null && strModCreate.equalsIgnoreCase("AssignToECO"))||(strModCreate!=null && strModCreate.equalsIgnoreCase("MoveToECO")) ||(strModCreate!=null && strModCreate.equalsIgnoreCase("AddToECO")))
                {
            //Calling getRelatedObjects to get the relationship ids
            relationshipIdList = domObj.getRelatedObjects(context,
                                                    strRelationship,
                                                    DomainConstants.QUERY_WILDCARD,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);
            }
          if (relationshipIdList.size() > 0)
            {
            Iterator itr = relationshipIdList.iterator();
            while(itr.hasNext())
            {
                Map newMap = (Map)itr.next();
                sObjectName=(String) newMap.get(DomainConstants.SELECT_NAME);
                sObjectId=(String) newMap.get(DomainConstants.SELECT_ID);
                DomainObject newValue =  new DomainObject(sObjectId);
                String strAttribute = newValue.getAttributeValue(context,ATTRIBUTE_ROUTE_BASE_PURPOSE);
                if("Review".equals(strAttribute)){
                sDisplayValue =sObjectName;
                sHiddenValue = sObjectId;
                    strBuf.append("<input type='text' name='ReviewersListDisplay' value=\"");
                    strBuf.append(sDisplayValue);
                    strBuf.append("\" > </input>");
                    strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showReviewersList();' > </input>");
                    strBuf.append("<a href=\"javascript:clearReviewersList()\">Clear</a>");
                    strBuf.append("<input type ='hidden' name='ReviewersListOID' value=\"");
                    strBuf.append(sHiddenValue);
                    strBuf.append("\" > </input>");
                    strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
                    strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
                    strBuf.append(" <script> ");
                    strBuf.append("function showReviewersList() { ");
                    strBuf.append(" var changeResId = document.emxCreateForm.DesignResponsibilityOID.value;");
                    strBuf.append(" if (changeResId == null || changeResId == \"\" || changeResId == \" \") {");
                    strBuf.append(" alert (\""+EngineeringUtil.i18nStringNow(context,"emxUnresolvedEBOM.Common.DesignResponsibilityAlert",strLanguage)    +"\");");
                    strBuf.append("} else {");
                    strBuf.append("var designResName = document.emxCreateForm.DesignResponsibilityDisplay.value;");
                    strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:REL_ROUTETEMPLATE_OWNINGORGANIZATION=\" + designResName + \":ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active&amp;table=APPECRouteTemplateSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameActual=ReviewersListOID&amp;fieldNameDisplay=ReviewersListDisplay&amp;submitURL=../unresolvedebom/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\" ,850,630); ");

                    strBuf.append('}');
                    strBuf.append(" } ");
                    strBuf.append("function clearReviewersList() { ");
                    strBuf.append(" document.emxCreateForm.ReviewersListDisplay.value = \"\"; ");
                    strBuf.append(" document.emxCreateForm.ReviewersListOID.value     = \"\"; ");
                    strBuf.append(" }");
                    strBuf.append("</script>");
                }

            }
            }else{
                strBuf.append("<input type='text' name='ReviewersListDisplay' value='' > </input>");
                strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showReviewersList();' > </input>");
                strBuf.append("<a href=\"javascript:clearReviewersList()\">Clear</a>");
                strBuf.append("<input type ='hidden' name='ReviewersListOID' value='Unassigned' > </input>");
                strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
                strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
                strBuf.append(" <script> ");
                strBuf.append("function showReviewersList() { ");
                strBuf.append(" var changeResId = document.emxCreateForm.DesignResponsibilityOID.value;");
                strBuf.append(" if (changeResId == null || changeResId == \"\" || changeResId == \" \") {");
                strBuf.append(" alert (\""+EngineeringUtil.i18nStringNow(context,"emxUnresolvedEBOM.Common.DesignResponsibilityAlert",strLanguage)    +"\");");
                strBuf.append("} else {");
                strBuf.append("var designResName = document.emxCreateForm.DesignResponsibilityDisplay.value;");
                strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:REL_ROUTETEMPLATE_OWNINGORGANIZATION=\" + designResName + \":ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active&amp;table=APPECRouteTemplateSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameActual=ReviewersListOID&amp;fieldNameDisplay=ReviewersListDisplay&amp;submitURL=../unresolvedebom/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\" ,850,630); ");
                strBuf.append('}');
                strBuf.append(" } ");
                strBuf.append("function clearReviewersList() { ");
                strBuf.append(" document.emxCreateForm.ReviewersListDisplay.value = \"\"; ");
                strBuf.append(" document.emxCreateForm.ReviewersListOID.value     = \"\"; ");
                strBuf.append(" }");
                strBuf.append("</script>");
            }

             }else{
                strBuf.append("");
             }
            }
        }catch(Exception ex)
        {
        }
        return strBuf.toString();
    }

/**
     * Updates the ResponsibleDesignEngineer field in PUEECO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECO object Id
     * @throws Exception if the operations fails
     * @since EC - X3
     */
public void  connectResponsibleDesignEngineer(Context context,String[] args)throws Exception
{
    try
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        HashMap requestMap = (HashMap) programMap.get("requestMap");
        HashMap paramMap   = (HashMap)programMap.get("paramMap");
        String strPUEECOId = (String)paramMap.get("objectId");
        String[] strResponsibleMEOID = (String[])requestMap.get("ResponsibleManufacturingEngineerOID");
        String strResponsibleMEID = strResponsibleMEOID[0];

        //Starts: IR-0685142011x
        String[] strResponsibleDEOID = (String[])requestMap.get("ResponsibleDesignEngineerOID");
        String   strResponsibleDEID  = strResponsibleDEOID[0];
        //Ends: IR-0685142011x

        DomainObject domPUEECO = new DomainObject(strPUEECOId);
        String strRelResponsibleME  =PropertyUtil.getSchemaProperty(context,"relationship_ResponsibleManufacturingEngineer");
        String strRelResponsibleDE  =PropertyUtil.getSchemaProperty(context,"relationship_ResponsibleDesignEngineer");

        if((strResponsibleDEID != null) && !("".equals(strResponsibleDEID)) ||  "Unassigned".equalsIgnoreCase(strResponsibleDEID) || "null".equalsIgnoreCase(strResponsibleDEID))
        {
            setId(strResponsibleDEID);
            DomainRelationship.connect(context,domPUEECO,strRelResponsibleDE,this);
        }


        if((strResponsibleMEID != null) && !("".equals(strResponsibleMEID)) ||  "Unassigned".equalsIgnoreCase(strResponsibleMEID) || "null".equalsIgnoreCase(strResponsibleMEID))
        {
            setId(strResponsibleMEID);
            DomainRelationship.connect(context,domPUEECO,strRelResponsibleME,this);
        }
    }
    catch(Exception ex)
    {
    }
}

    /* This method "displayUserSettingsDefaultVault" displays default vault of the User.
     * @param context The ematrix context of the request.
     * @param args This string array contains following arguments:
     *          0 - The programMap
     * @param args an array of String arguments for this method
     * @return String Vault details in HTML format.
     * @throws Exception
     * @throws FrameworkException
     * @since EngineeringCentral X3
     */
public String displayUserSettingsDefaultVault(Context context, String[] args) throws Exception
    {
        String defaultVault = PropertyUtil.getAdminProperty(context, personAdminType,  context.getUser(),  PREFERENCE_ENC_DEFAULT_VAULT);
		// IR-093074
		String defaultVaultActual = defaultVault;
        if(defaultVault == null || "".equals(defaultVault))
        {
            defaultVault = PersonUtil.getDefaultVault(context);
            defaultVaultActual = defaultVault;
        }
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		String languageStr = (String) paramMap.get("languageStr");
		if(null!=defaultVault && null!=languageStr)
			defaultVault = i18nNow.getAdminI18NString("Vault", defaultVault, languageStr);
        String sECO           = DomainConstants.TYPE_ECO;
        StringBuffer sbReturnString = new StringBuffer(1024);

        sbReturnString.append("<input type='text' name='VaultDisplay' value=\""+ defaultVault+"\" readOnly='true'> </input>");
        sbReturnString.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript: showVaultSelector();' > </input>");
        sbReturnString.append("<input type ='hidden' name='Vault' value='"+defaultVaultActual+"' > </input>");

        sbReturnString.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
        sbReturnString.append(" <script src='../emxUIPageUtility.js'> </script> ");

        sbReturnString.append(" <script> ");
        sbReturnString.append("function showVaultSelector() { ");
        sbReturnString.append("emxShowModalDialog(\"../components/emxComponentsSelectSearchVaultsDialogFS.jsp?multiSelect=false&amp;fieldName=Vault&amp;objectType="+sECO+"&amp;suiteKey=Components&amp;\",300,350); ");

        sbReturnString.append(" } ");
        sbReturnString.append("</script>");

        return sbReturnString.toString();
    }

       /**
     * Displays the "Category of Change" drop down based on the ECR or the stored value.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following Strings, "objectId".
     * requestMap - a HashMap containing the request.
     * @return Object - String object which contains the Category of Change drop down.
     * @throws Exception if operation fails.
     * @since EngineeringCentral X3
     */

    public Object getCategoryofChange(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        //Added for the fix 366148
        String languageStr = (String) requestMap.get("languageStr");
        //366148 fix ends
        String strObjectId = (String) requestMap.get("OBJId");

        StringBuffer sbCategoryOfChange = new StringBuffer(128);
        String sCurrentCategoryOfChangeName = "";

        if (strObjectId!=null && strObjectId.length() > 0)
        {
            setId(strObjectId);
            sCurrentCategoryOfChangeName = getAttributeValue(context,ATTRIBUTE_CATEGORY_OF_CHANGE);
        }

        //Get the range values for ECO "Category Of Change" Attribute
        StringList slCategoryOfChangeRangeValues = FrameworkUtil.getRanges(context , ATTRIBUTE_CATEGORY_OF_CHANGE);
        slCategoryOfChangeRangeValues.sort();
        String strCategoryOfChangeOption = null;

        //Construct the Category Of Change dropdown
		sbCategoryOfChange.append("<select name=\"CategoryOfChange\">");
        for (int i=0; i < slCategoryOfChangeRangeValues.size(); i++)
        {
            strCategoryOfChangeOption = (String)slCategoryOfChangeRangeValues.elementAt(i);
            String sPolicySelected ="selected=\"selected\"";
            //Modified for the Fix 366148
            sbCategoryOfChange.append("<option value=\""+strCategoryOfChangeOption+"\" "+((strCategoryOfChangeOption.equals(sCurrentCategoryOfChangeName))?sPolicySelected:"")+">");
            sbCategoryOfChange.append(i18nNow.getRangeI18NString(ATTRIBUTE_CATEGORY_OF_CHANGE,strCategoryOfChangeOption,languageStr));
            sbCategoryOfChange.append("</option>");
            //366148 fix ends
        }
        sbCategoryOfChange.append("</select>");

        return sbCategoryOfChange.toString();
 }

  /**
   *Update the Category of Change value of PUEECO
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns booloen
   * @throws Exception if the operation fails
   * @since EngineeringCentral X3
   */
  public Boolean updateCategoryofChange(Context context, String[] args) throws Exception
  {
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap = (HashMap)programMap.get("paramMap");

    String strECObjectId = (String)paramMap.get("objectId");
    DomainObject domObjECO = new DomainObject(strECObjectId);

    String strNewCategoryOfChange = (String)paramMap.get("New Value");
    if (strNewCategoryOfChange == null || strNewCategoryOfChange.length() == 0)
    {
        strNewCategoryOfChange = (String)paramMap.get("New OID");
    }
    domObjECO.setAttributeValue(context, ATTRIBUTE_CATEGORY_OF_CHANGE, strNewCategoryOfChange);

    return Boolean.TRUE;
  }


  	/**
     * Checks the view mode of the web form display.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * mode - a String containing the mode.
     * @return Object - boolean true if the mode is view
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

public StringList  getResponsibleDesignEngineer(Context context,String[] args)throws Exception{
	try{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");

        String strPUEECOID = (String) requestMap.get("objectId");
        DomainObject domObj = new DomainObject(strPUEECOID);

        String strRelResponsibleDE =
        PropertyUtil.getSchemaProperty(context,"relationship_ResponsibleDesignEngineer");

        String strResponsibleName = domObj.getInfo(context,"relationship["+strRelResponsibleDE+"].to.name");

        StringList strList=new StringList();
        strList.add(strResponsibleName);

        return strList;
      }
      catch(Exception ex){
             throw  new FrameworkException((String)ex.getMessage());
      }
    }

public void  updateResponsibleDesignEngineer(Context context,String[] args)throws Exception
{
    try
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);

        HashMap paramMap   = (HashMap)programMap.get("paramMap");
        String strPUEECOId = (String)paramMap.get("objectId");
        DomainObject domPUEECO = new DomainObject(strPUEECOId);

        String strRelResponsibleDE  =PropertyUtil.getSchemaProperty(context,"relationship_ResponsibleDesignEngineer");

        //Connected RDE and RME
        String strResponsibleDEOldId = domPUEECO.getInfo(context,"relationship["+strRelResponsibleDE+"].to.id");

        // Modified RDE and RME
          String strResponsibleDENewID = ("".equals((String)paramMap.get("New OID")))?(String)paramMap.get("New Value"):(String)paramMap.get("New OID");

        //Disconnect RDE if the Update RDE is different from the new ID
        if(strResponsibleDEOldId!=null){
            if(!strResponsibleDEOldId.equalsIgnoreCase(strResponsibleDENewID)){
                String strResponsibleDERelID = domPUEECO.getInfo(context,"relationship["+strRelResponsibleDE+"].id");

                DomainRelationship.disconnect(context, strResponsibleDERelID);
            }
        }

        if(strResponsibleDENewID != null && !"".equals(strResponsibleDENewID) &&  !strResponsibleDENewID.equalsIgnoreCase(strResponsibleDEOldId))
        {
            setId(strResponsibleDENewID);
            DomainRelationship.connect(context,domPUEECO,strRelResponsibleDE,this);
        }
    }
    catch(Exception ex)
    {
    }
}

public StringList  getResponsibleManufacturingEngineer(Context context,String[] args)throws Exception{

try{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");

        String strPUEECOID = (String) requestMap.get("objectId");
        DomainObject domObj = new DomainObject(strPUEECOID);

        String strRelResponsibleDE =
        PropertyUtil.getSchemaProperty(context,"relationship_ResponsibleManufacturingEngineer");

        String strResponsibleName = domObj.getInfo(context,"relationship["+strRelResponsibleDE+"].to.name");

        StringList strList=new StringList();
        strList.add(strResponsibleName);

        return strList;
      }
      catch(Exception ex){
             throw  new FrameworkException((String)ex.getMessage());
      }
    }

    /**@author KRISHNA MOHAN
     * @param context
     * @param args
     * @return java.util.List
     * @throws Exception
     */
    public java.util.List getPrerequisiteType(Context context,String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        java.util.List lstPrerequisiteType = new StringList();
        MapList lstObjectIdsList = (MapList) programMap.get("objectList");
        Map tempMap = null ;
        String strRelId = "";
        String strAttributeValue = "" ;
        String strAttributeName = PropertyUtil.getSchemaProperty(context, "attribute_UserDefined");
        String strPrereqType = "";
        Locale Local = context.getLocale();
        try{
            for(int j=0; j<lstObjectIdsList.size();j++)
            {
                tempMap = (Map)lstObjectIdsList.get(j);
                strRelId  = (String)tempMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                strAttributeValue = DomainRelationship.getAttributeValue( context, strRelId , strAttributeName ) ;
                if( "Yes".equals(strAttributeValue))
                {
                    //strPrereqType =i18nNow.getI18nString("emxUnresolvedEBOM.PUEECO.UserDefinedPrerequisite","emxUnresolvedEBOMStringResource",context.getSession().getLanguage());
                	strPrereqType =EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.PUEECO.UserDefinedPrerequisite");
                }
                else if( "No".equals(strAttributeValue))
                {
                    //strPrereqType =i18nNow.getI18nString("emxUnresolvedEBOM.PUEECO.SystemDefinedPrerequisite","emxUnresolvedEBOMStringResource",context.getSession().getLanguage());
                	strPrereqType =EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.PUEECO.SystemDefinedPrerequisite");
                }
                lstPrerequisiteType.add( strPrereqType ) ;
            }
        }
        catch(Exception e)
        {
        }
        if(lstPrerequisiteType.size() <= 0)
        {
                lstPrerequisiteType.add( "" ) ;
        }

        return lstPrerequisiteType ;
    }

  /*
       This method is called  for retrieving the related items of the object in the given context.
        Related items are populated in the MaplList structure.
  */

 /**
  * @author Krishna Mohan
 * @param context
 * @param argv
 * @return MapList
 * @throws Exception
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getPrerequisites(Context context,String[] argv) throws Exception
    {
    HashMap programMap = (HashMap) JPO.unpackArgs(argv);        // unpacking the arguments
    String strObjectId = (String)programMap.get("objectId");
	String strExpandLevel = (String)programMap.get("expandLevel");
    MapList mListRelatedData = new MapList();
    StringList sList = new StringList(5);
    StringList selectRelStmts = new StringList(2);
    setId( strObjectId ) ;
    sList.addElement(DomainConstants.SELECT_ID);
    selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
    selectRelStmts.addElement(UnresolvedEBOMConstants.SELECT_ATTRIBUTE_USER_DEFINED);
      //get the related items in the MapList
	int iExpandLevel = "All".equals(strExpandLevel)? 0: Integer.parseInt(strExpandLevel);
   // mListRelatedData = getRelatedObjects(context, RELATIONSHIP_PREREQUISITE, TYPE_PUEECO, sList,selectRelStmts, false , true,(short) 1, "", "");
   mListRelatedData = getRelatedObjects(context, RELATIONSHIP_PREREQUISITE, TYPE_PUEECO, sList,selectRelStmts, false , true,(short) iExpandLevel, "", "");
    return  mListRelatedData ;      // returning the result...
    }

/**
 * @author Krishna Mohan
 * @param context
 * @param argv
 * @return StringList
 * @throws Exception
 */
@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
public StringList excludePrerequisitePUEECO(Context context, String[] argv) throws Exception
{
    HashMap programMap = (HashMap) JPO.unpackArgs(argv);        // unpacking the arguments
    String strObjectId = (String)programMap.get("objectId");
    MapList mListRelatedData = new MapList();
    StringList sList = new StringList(5);
    StringList selectRelStmts = new StringList(2);
    setId( strObjectId ) ;
    sList.addElement(DomainConstants.SELECT_ID);
    selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
    //get the related items in the MapList
    mListRelatedData = getRelatedObjects(context, RELATIONSHIP_PREREQUISITE , TYPE_PUEECO , sList,selectRelStmts, true , false ,(short) 0, "", "");

    StringList tempStrList = new StringList();
    String strOId= null ;
    for (int i=0; i < mListRelatedData.size(); i++)
    {
        Hashtable tempMap =  (Hashtable) mListRelatedData.get(i);
        strOId = tempMap.get("id").toString();
        tempStrList.addElement(strOId);
    }

    mListRelatedData = getRelatedObjects(context, RELATIONSHIP_PREREQUISITE, TYPE_PUEECO, sList,selectRelStmts, false,true ,(short) 1, "", "");
    for (int i=0; i < mListRelatedData.size(); i++)
    {
        Hashtable tempMap =  (Hashtable) mListRelatedData.get(i);
        strOId = tempMap.get("id").toString();
        tempStrList.addElement(strOId);
    }
    tempStrList.addElement(strObjectId);

    return tempStrList;
}

  /*
       This method is invoked to check if a PUE ECO has Prerequisites before deleting the PUE ECO.
  */
/**
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
public int checkPrerequisiteForDelete(Context context,String[] args) throws Exception
{
    MapList mListPrerequisites = null;
    Locale Local = context.getLocale();
    try{
          String objectId = args[0];
           setId(objectId);
            StringList sList = new StringList(2);
            StringList selectRelStmts = new StringList(2);
            sList.addElement(DomainConstants.SELECT_ID);
            selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
              //get the related Prerequisites in the MapList
             mListPrerequisites = getRelatedObjects(context,RELATIONSHIP_PREREQUISITE,TYPE_PUEECO,sList,null,true,true,(short)1,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
        }
        catch(Exception d){
            d.printStackTrace();
        }
        if(mListPrerequisites.size()>0)
            {
               String strAlertMessage = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.PUEECO.PrerequisiteDeleteCheck.alert");
               emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
               return 1;
            }
            else{
                return 0;
                }
        }

	/**
     * this method checks the Related object state
     * Returns Boolean determines whether the connected
     * objects are in approperiate state.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds objectId.
     * @param args
     *            holds relationship name.
     * @param args
     *            holds type name.
     * @param args
     *            holds policy name.
     * @param args
     *            holds State.
     * @param args
     *            holds TO/FROM.
     * @param args
     *            holds String Resource file name
     * @param args
     *            holds String resource filed key name.
     * @return Boolean determines whether the connected objects are in
     *         approperiate state.
     * @throws Exception if the operation fails.
     * @since Engineering Central X3.
     */
   public int checkRelatedObjectState(Context context,String args[]) throws Exception {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }
        String objectId = args[0];
        setId(objectId);
        String strRelationshipName = PropertyUtil.getSchemaProperty(context,args[1]);
        String strTypeName = PropertyUtil.getSchemaProperty(context,args[2]);
        String strPolicyName = PropertyUtil.getSchemaProperty(context,args[3]);
        String strStates = args[4];
        boolean boolTo = args[5].equalsIgnoreCase("TO")?true:false;
        boolean boolFrom = args[5].equalsIgnoreCase("FROM")?true:false;
        String strResourceFieldId = args[6];
        String strStringId = args[7];
        Locale Local = context.getLocale();
        //String strMessage = i18nNow.getI18nString(strStringId,strResourceFieldId,context.getSession().getLanguage());
        String strMessage = EnoviaResourceBundle.getProperty(context,strResourceFieldId,Local,strStringId);
        String strCurrentState = args[8];
        String strPolicy = args[9];
        StringTokenizer stz = null;
        String strSymbolicCurrentPolicy = FrameworkUtil.getAliasForAdmin(context, DomainConstants.SELECT_POLICY, strPolicy, false);
        String strSymbolicCurrentState = FrameworkUtil.reverseLookupStateName(context,strPolicy,strCurrentState);

        String RouteBasePolicy = PropertyUtil.getSchemaProperty(context,"attribute_RouteBasePolicy");
        String RouteBaseState = PropertyUtil.getSchemaProperty(context,"attribute_RouteBaseState");

        int ichkvalue = 0;
        if (strStates.indexOf(' ')>-1){
                stz = new StringTokenizer(strStates," ");
            }
        else if (strStates.indexOf(',')>-1){
                stz = new StringTokenizer(strStates,",");
            }
        else if(strStates.indexOf('~')>-1){
                stz = new StringTokenizer(strStates,"~");
            }
        else{
                stz = new StringTokenizer(strStates,"");
            }

            Vector vector = new Vector();
        while (stz.hasMoreElements()){
                String state = stz.nextToken();
                vector.addElement(PropertyUtil.getSchemaProperty(context, DomainConstants.SELECT_POLICY, strPolicyName , state));
            }

        String strRelnWhereClause = null;
        strRelnWhereClause = "attribute["+RouteBasePolicy+"] == "+strSymbolicCurrentPolicy+" && attribute["+RouteBaseState+"] == "+strSymbolicCurrentState+"";
            StringList busSelects = new StringList(2);
            busSelects.add(DomainConstants.SELECT_ID);
            busSelects.add(DomainConstants.SELECT_CURRENT);
            StringList relSelects = new StringList(2);
            relSelects.add(DomainConstants.SELECT_ID);

            MapList maplistObjects = new MapList();

            try
            {
                ContextUtil.pushContext(context);
                maplistObjects = getRelatedObjects(context,
                                          strRelationshipName,
                                          strTypeName,
                                          busSelects, // object Select
                                          relSelects, // rel Select
                                          boolFrom, // to
                                          boolTo, // from
                                          (short)1,
                                          null, // ob where
                                          strRelnWhereClause  // rel where
                                          );
            }
            catch (Exception ex)
            {
            }
            finally
            {
                ContextUtil.popContext(context);
            }

       if (maplistObjects != null && (maplistObjects.size() > 0)){
                Iterator itr = maplistObjects.iterator();
       while (itr.hasNext() && ichkvalue != 1){
                    Map mapObject = (Map) itr.next();
                    ichkvalue = vector.contains(mapObject.get("current"))?0:1;
                }

            }
       if(ichkvalue == 1){
                emxContextUtil_mxJPO.mqlNotice(context,strMessage);
            }

        return ichkvalue;
    }

public static Boolean emxCheckCreateDynamicApprovalECO(Context context, String[] args)
          throws Exception
     {
         boolean dynamicApproval = false;
         String policyClassification = FrameworkUtil.getPolicyClassification(context, POLICY_ECO);
         if ("DynamicApproval".equals(policyClassification))
         {
             dynamicApproval = true;
         }
         return Boolean.valueOf(dynamicApproval);
     }

    public MapList getallProductBuilds (Context context, String[] args) throws Exception {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String) programMap.get("objectId");
            MapList relObjIdList = new MapList();
            //Stringlists containing the objectSelects & relationshipSelects parameters
            StringList ObjectSelectsList = new StringList(DomainConstants.SELECT_ID);
            StringList RelSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //Instantiating DomainObject
            DomainObject domainObject = new DomainObject(strObjectId);
            //Using the symbolic name to get the name of the type
            //Calling the getRelatedObjects() method of DomainObject
            relObjIdList = domainObject.getRelatedObjects(context, ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD,
                 ProductLineConstants.TYPE_BUILDS, ObjectSelectsList, RelSelectsList, false, true,(short) 1,
                                            "",
                                            "");
        if ((relObjIdList == null))
            throw  new Exception();
        //Returning the Maplist of Object Ids
        return  relObjIdList;

    }

    /*
     * Get Subsequent Applicability combobox selection
     * @return Object - returns StringList containing subsequent applicability selections
     * @throws Exception if the operation fails
     * @since EC X4
     */
    public HashMap getSubsequentApplicabilityRanges (Context context, String[] args) throws Exception
    {
        HashMap mapReturn = new HashMap();
        Locale Local = context.getLocale();
        //Alert message is formulated to display the error message
        String strNo = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource", Local, "emxUnresolvedEBOM.PUEECO.Label.No");
        String strYesInc = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource", Local, "emxUnresolvedEBOM.PUEECO.Label.IncludeIntermediate");
        String strYesExc = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource", Local, "emxUnresolvedEBOM.PUEECO.Label.ExcludeIntermediate");

        StringList strFilterActualList  = new StringList();
        strFilterActualList.add(0,EnoviaResourceBundle.getProperty(context, "emxUnresolvedEBOMStringResource", new Locale("en"),"emxUnresolvedEBOM.PUEECO.Label.No"));
        strFilterActualList.add(1,EnoviaResourceBundle.getProperty(context, "emxUnresolvedEBOMStringResource", new Locale("en"),"emxUnresolvedEBOM.PUEECO.Label.IncludeIntermediate"));
        strFilterActualList.add(2,EnoviaResourceBundle.getProperty(context, "emxUnresolvedEBOMStringResource", new Locale("en"),"emxUnresolvedEBOM.PUEECO.Label.ExcludeIntermediate"));
        StringList strFilterDisplayList  = new StringList();
        strFilterDisplayList.add(0,strNo);
        strFilterDisplayList.add(1,strYesInc);
        strFilterDisplayList.add(2,strYesExc);

        mapReturn.put("field_choices", strFilterActualList);
        mapReturn.put("field_display_choices", strFilterDisplayList);

        return mapReturn;
    }

/**
 *   This method is invoked to check if a PUE ECO has Prerequisites before cancelling the PUE ECO.
 * @param context
 * @param args
 * @return int
 * @throws Exception
 */
public int checkPrerequisiteForCancel(Context context,String[] args) throws Exception
{
    MapList mListPrerequisites = null;
    Locale Local = context.getLocale();
    try{
        String objectId = args[0];
        setId(objectId);
        StringList sList = new StringList(2);
        StringList selectRelStmts = new StringList(2);
        sList.addElement(DomainConstants.SELECT_ID);
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
          //get the related Prerequisites in the MapList
         mListPrerequisites = getRelatedObjects(context,RELATIONSHIP_PREREQUISITE,TYPE_PUEECO,sList,null,true,false,(short)1,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
        }
        catch(Exception d){
            d.printStackTrace();
        }

        if(mListPrerequisites.size()>0)
            {
               //String strAlertMessage = i18nNow.getI18nString("emxUnresolvedEBOM.PUEECO.PUEECOCancelCheck.alert","emxUnresolvedEBOMStringResource",context.getSession().getLanguage());
        	String strAlertMessage = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.PUEECO.PUEECOCancelCheck.alert");
               emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
               return 1;
            }
        else
            return 0;

        }

      /**
         * This method will disconnects all prerequisite realtionshop
         * @author Krishna Mohan
         * @param context
         * @param args
         *          args[0] -object Id
         *          args[1] - relationship name (Prerequisite)
         * @return int
         * @throws FrameworkException
         */

        public int disConnectPrerequisites(matrix.db.Context context, String[] args) throws FrameworkException
        {
                String objectId = args[0] ;
                String  strRelType = args[1];
                String strRelName = PropertyUtil.getSchemaProperty(context,strRelType);
                MapList mListPrerequisites = null;
                setId( objectId ) ;
                StringList sList = new StringList(2);
                StringList selectRelStmts = new StringList(2);
                sList.addElement(DomainConstants.SELECT_ID);
                selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
    mListPrerequisites = getRelatedObjects( context, strRelName, TYPE_PUEECO, sList ,selectRelStmts, true , false ,(short) 1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING );
                int intSize = mListPrerequisites.size();
                String [] strArrRelIDs =  new String [intSize];
                for ( int i=0; i < mListPrerequisites.size(); i++ )
                {
                    Hashtable tempMap =  (Hashtable) mListPrerequisites.get(i);
                    String strRelId = tempMap.get(DomainConstants.SELECT_RELATIONSHIP_ID).toString();
                    strArrRelIDs[i] = strRelId ;
                }
               DomainRelationship.disconnect( context,strArrRelIDs );
            return 0 ;
        }


        /**
         * This method will disconnects Affected Item .Reference Document ,Object Route relationships based on input
         * @author Krishna Mohan
         * @param context
         * @param args
         *          0 - Object Id
         *          1 - RelationShip symbolic name
         * @return int
         * @throws FrameworkException
         */

        public int disConnectRelatedObjectsFromPUEECO(matrix.db.Context context, String[] args) throws FrameworkException
        {

                String objectId = args[0] ;
                String  strRelType = args[1];
                String strRelName = PropertyUtil.getSchemaProperty(context,strRelType);
                MapList mListPrerequisites = null;
                setId( objectId ) ;
                StringList sList = new StringList(2);
                StringList selectRelStmts = new StringList(2);
                sList.addElement(DomainConstants.SELECT_ID);
                selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
                mListPrerequisites = getRelatedObjects( context, strRelName,"*" , sList ,selectRelStmts, false , true ,(short) 1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING );
                int intSize = mListPrerequisites.size();
                String [] strArrRelIDs =  new String [intSize];
                for ( int i=0; i < mListPrerequisites.size(); i++ )
                {
                    Hashtable tempMap =  (Hashtable) mListPrerequisites.get(i);
                    String strRelId = tempMap.get(DomainConstants.SELECT_RELATIONSHIP_ID).toString();
                    strArrRelIDs[i] = strRelId ;
                }
                DomainRelationship.disconnect( context,strArrRelIDs );


            return 0 ;
        }
/**
 * This method is invoked
 * @author Krishna Mohan
 * @param context
 * @param args
 *          args[0] - Object Id
 *          args[1] - relationship name Unresolved EBOM
 * @return int
 * @throws FrameworkException
 */
public int disConnectUEBOMRelationsFromPUEECO(matrix.db.Context context, String[] args) throws FrameworkException
{
    String objectId = args[0];
    String  strRelType = args[1];
    String strRelName = PropertyUtil.getSchemaProperty(context,strRelType);
    StringList strRelIdList = null;
    String strSelectables = "from["+strRelName+"].id";
    String strAttrValue = "" ;

    try {
        setId( objectId );
        strRelIdList = getInfoList(context, strSelectables );
        String strRelId = "" ;
        String strFromToRelId = "" ;
        String[] strArrRelIds = null ;
        Vector vectRelIds = new Vector( strRelIdList.size() );
        for(int i=0 ; i<strRelIdList.size() ; i++)
        {
            strRelId =( String ) strRelIdList.get(i);
            vectRelIds.add(strRelId);
            //Modified for IR-048513V6R2012x, IR-118107V6R2012x
			strAttrValue = DomainRelationship.getAttributeValue( context, strRelId , UnresolvedEBOMConstants.ATTRIBUTE_BOM_OPERATION ) ;
            if(strAttrValue.equalsIgnoreCase("add"))
            {
                RelToRelUtil ralToRel = null ;
                try {
                    ralToRel = new RelToRelUtil(strRelId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                strFromToRelId = ralToRel.getFromToRelId( context , strRelId , false );
                vectRelIds.add(strFromToRelId);
            }
        }
        if(!vectRelIds.isEmpty())
        {
        strArrRelIds = (String[])vectRelIds.toArray(new String[vectRelIds.size()]);
        DomainRelationship.disconnect( context , strArrRelIds ) ;
        }

    } catch (FrameworkException e) {
        e.printStackTrace();
        return 1 ;
    }
    return 0 ;
}

/**
*This method is invoked to check if a PUE ECO has Prerequisites before Releasing the PUE ECO.
* @author Krishna Mohan
* @param context
* @param args
*           0 - obj id
*           1 - name of state
* @return int
* @throws Exception
*/
public int checkPrerequisitesAreInStateRelease(Context context,String[] args) throws Exception
{
	try
	{
            //ComponentsUtil.checkLicenseReserved(context, "ENO_ENG_TP");
			ComponentsUtil.checkLicenseReserved(context, "ENO_XCE_TP");
	}
	catch(Exception e){
		 emxContextUtilBase_mxJPO.mqlNotice(context, e.getMessage());
	     return 1 ;
	}

    MapList mListPrerequisites = null;
    Locale Local = context.getLocale();
    boolean boolStatus = false ;
    try{
        String objectId = args[0];
        String strStateName = args[1];
        String strState = PropertyUtil.getAdminProperty(context ,DomainConstants.SELECT_POLICY , TYPE_PUEECO , strStateName );
        setId(objectId);
        StringList sList = new StringList(2);
        sList.addElement(DomainConstants.SELECT_ID);
        sList.addElement(DomainConstants.SELECT_CURRENT);
        //get the related Prerequisites in the MapList
        mListPrerequisites = getRelatedObjects(context,RELATIONSHIP_PREREQUISITE,TYPE_PUEECO,sList,null,false,true,(short)1,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
        for (int i=0; i < mListPrerequisites.size(); i++)
        {
            Hashtable tempMap =  (Hashtable) mListPrerequisites.get(i);
            String strTemp= tempMap.get(DomainConstants.SELECT_CURRENT).toString();
            if(! strTemp.equalsIgnoreCase(strState) )
            {
                boolStatus =  true ;
                break ;
            }
        }
    }
    catch(Exception d){
        d.printStackTrace();
    }

    if( boolStatus )
    {
    	String strAlertMessage = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.PUEECO.ReleasePUEECOCheck.alert");
        emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
        return 1 ;
    }
    else{
        return 0 ;
    }
    }

/**
 * This method is invoked
 * @author Krishna Mohan
 * @param context
 * @param args
 * @return int
 * @throws FrameworkException
 */
public int changePUEECOOwner(Context context,String[] args ) throws FrameworkException
{
    String objectId = args[0];
    String  strOwnerName = args[1];
    DomainObject changeObj = null;
    changeObj = DomainObject.newInstance(context,objectId);
    changeObj.setOwner(context ,strOwnerName );
    return 0 ;
}

public int checkResponsibleDesignEngineer(Context context, String args[])
            throws Exception {
        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }

        Locale Local = context.getLocale();
        String objectId = args[0];
        setId(objectId);
        DomainObject domPUEECO = new DomainObject(objectId);
        String strResourceFieldId = args[3];
        String strStringId = args[4];
        String strRelResponsibleDE  =PropertyUtil.getSchemaProperty(context,"relationship_ResponsibleDesignEngineer");
        StringList sList = new StringList(2);
        StringList selectRelStmts = new StringList(2);
        sList.addElement(DomainConstants.SELECT_ID);
        selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList mListPerson = domPUEECO.getRelatedObjects(context,strRelResponsibleDE,"Person",sList,selectRelStmts,false,true,(short)1,DomainConstants.EMPTY_STRING,DomainConstants.EMPTY_STRING);
        String strMessage = EnoviaResourceBundle.getProperty(context,strResourceFieldId,Local,strStringId);
        if( mListPerson.size()>0)
            {return 0;}
        else {
        emxContextUtil_mxJPO.mqlNotice(context,strMessage);
        return 1;
}

}

public int checkResponsibleManufacturingEngineer(Context context, String args[])
throws Exception {
    if (args == null || args.length < 1) {
    throw (new IllegalArgumentException());
    }
        String objectId = args[0];
        setId(objectId);
        Locale Local = context.getLocale();
        DomainObject domPUEECO = new DomainObject(objectId);
        String strResourceFieldId = args[3];
        String strStringId = args[4];
        String strRelResponsibleDE  =PropertyUtil.getSchemaProperty(context,"relationship_ResponsibleManufacturingEngineer");
        String strMessage = EnoviaResourceBundle.getProperty(context,strResourceFieldId,Local,strStringId);
        //Modified for IR-048513V6R2012x, IR-118107V6R2012x
        String strSelect = "from["+strRelResponsibleDE+"].to.name";
        String strPerson = domPUEECO.getInfo(context, strSelect ) ;
    if(strPerson != null )
    {}
    else
    {
    emxContextUtil_mxJPO.mqlNotice(context,strMessage);
    return 1;
    }
    return 0;
    }

/**
 * this method assigns notifies the user
 *
 * @param context
 *            the eMatrix <code>Context</code> object.
 * @param args1
 *            holds objectId.
 * @param args2
 *            holds Attribute Name.
 * @param args3
 *            holds AssignNotify/Assign.
   * @param args4
 *            holds Notification object.
 * @return Boolean and notification is sent based on the Attribute value.
 * @throws Exception if the operation fails.
 * @since Common X3.
 */
public int assignNotifyUserByAttribute(Context context, String args[])
        throws Exception {

    if (args == null || args.length < 1) {
        throw (new IllegalArgumentException());
    }
    String objectId = args[0];
    DomainObject domPUEECO = new DomainObject(objectId);
    setId(objectId);
    String strRelName = PropertyUtil.getSchemaProperty(context,args[1]);
    String strSelect = "from["+strRelName+"].to.name";
    String strAttributeName = domPUEECO.getInfo(context, strSelect ) ;//PropertyUtil.getSchemaProperty(context,args[1]);
    String strAssignNotify = args[2];
    String strNotification = args[3];
    String strNewOwner = strAttributeName ;

    int inotify = 0;
        if (strAssignNotify.equalsIgnoreCase("AssignNotify")
                && strNewOwner != null && !strNewOwner.equals("")) {

            String strnotifyargs[] = { objectId, strNotification };
            JPO.invoke(context, "emxNotificationUtil", null,
                    "objectNotification", strnotifyargs, null);
                    setOwner(context, strNewOwner);
        } else if (strAssignNotify.equalsIgnoreCase("Assign")
                && strNewOwner != null && !strNewOwner.equals("")) {
            setOwner(context, strNewOwner);
        } else {
            inotify = 1;
        }
    return inotify;
}

        /**
         * Gets PUE ECO summary for unresolved parts
         * @param context the eMatrix <code>Context</code> object
         * @param args - Holds the HashMap containing the following arguments
         * paramMap - contains part object id
         * @return MapList - list of PUE ECOs
         * @throws Exception if the operation fails
         * @since EC X4
        */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getRelatedPUEEcosSummary (Context context,String[] args)
         throws Exception
     {

         HashMap paramMap = (HashMap)JPO.unpackArgs(args);
         String partId = (String) paramMap.get("objectId");
         MapList pueecoList = new MapList();
         try
         {
               Part partObj = new Part(partId);
               StringList selectStmts = new StringList(4);

               selectStmts.addElement(DomainConstants.SELECT_ID);
               selectStmts.addElement(DomainConstants.SELECT_TYPE);
               selectStmts.addElement(DomainConstants.SELECT_NAME);
               selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);


               Pattern relPattern = new Pattern(RELATIONSHIP_AFFECTED_ITEM);


               pueecoList = partObj.getRelatedObjects(context,
                                                   relPattern.getPattern(),  //relationship pattern
                                                   UnresolvedEBOMConstants.TYPE_PUE_ECO,  // object pattern
                                                   selectStmts,                 // object selects
                                                   null,              // relationship selects
                                                   true,                        // to direction
                                                   false,                       // from direction
                                                   (short)1,                    // recursion level
                                                   null,                        // object where clause
                                                   null);
         }catch(Exception e){
             throw new FrameworkException(e);
         }

         return pueecoList ;
     }

/**
      * Updates the Review list field values in Engineering change WebForm.
      * @param context         the eMatrix <code>Context</code> object
      * @param args            holds a Map with the following input arguments:
      *    objectId            objectId of the context Engineering Change
      *    New Value           objectId of updated Review List value
      * @throws                Exception if the operations fails
      * @since                 Common 10-6
      */
    public void updateObjectRouteReview (Context context, String[] args) throws Exception {
    	ContextUtil.pushContext(context);
        try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");

            //Relationship name
            String strRelationship              = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
            String strType                      = DomainConstants.TYPE_ROUTE_TEMPLATE;
            DomainRelationship oldRelationship = null;
            DomainObject domainObjectToType = null;
            String strNewToTypeObjId        = "";
            String strRouteBasePurpose = "";
            String strTempRelRouteBasePurpose = "";
            DomainRelationship newRelationship = null;

            //Getting the EC Object id and the new product object id
            String strECObjectId = (String)paramMap.get("objectId");
            //modified for IR-016954
            strNewToTypeObjId = ("".equals((String)paramMap.get("New OID")))?(String)paramMap.get("New Value"):(String)paramMap.get("New OID");

            if (strNewToTypeObjId != null && !strNewToTypeObjId.equalsIgnoreCase("")) {
                //Instantiating DomainObject with the new Route Template object id
                domainObjectToType = newInstance(context, strNewToTypeObjId);
                strRouteBasePurpose = domainObjectToType.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
            }


            //Business Objects are selected by its Ids
            StringList objectSelects = new StringList(2);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);

            //Stringlist containing the relselects
            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //setting the context to the Engineering Change object id
            setId(strECObjectId);

            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
            //Calling getRelatedObjects to get the relationship ids
            relationshipIdList = getRelatedObjects(context,
                                                    strRelationship,
                                                    strType,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);

            if(relationshipIdList.size()>0){
                for (int i=0;i<relationshipIdList.size();i++) {
                    //Getting the realtionship ids from the list
                    String strRelationshipId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    //Getting Route Object Id from the list
                    String strRouteId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_ID);

                    oldRelationship = new DomainRelationship(strRelationshipId);
                    strTempRelRouteBasePurpose = oldRelationship.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
                    //Modified for bug#302310,302308 by Infosys on 21 Apr 05
                    if(strTempRelRouteBasePurpose.equalsIgnoreCase(RANGE_REVIEW)) {
                        //Checking if the selected Object id is the same as the selected one and exiting the program.
                        if(strRouteId.equals(strNewToTypeObjId)) {
                            return;
                        }
                        //Disconnecting the existing relationship
                        try{
                            DomainRelationship.disconnect(context, strRelationshipId);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (domainObjectToType != null) {
                //Connecting the Engineering Change with the new Route Template object with relationship Object Route
                newRelationship = DomainRelationship.connect(context,
                                                             this,
                                                             strRelationship,
                                                             domainObjectToType);
                newRelationship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strRouteBasePurpose);
            }
        }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        } finally {
        	ContextUtil.popContext(context);
        }
    }


    /**
      * Updates the Review list field values in Engineering change WebForm.
      * @param context         the eMatrix <code>Context</code> object
      * @param args            holds a Map with the following input arguments:
      *    objectId            objectId of the context Engineering Change
      *    New Value           objectId of updated Approval List value
      * @throws                Exception if the operations fails
      * @since                 Common 10-6
      */
    public void updateObjectRouteApproval (Context context, String[] args) throws Exception {
    	ContextUtil.pushContext(context);
        try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap = (HashMap)programMap.get("paramMap");

            //Relationship name
            String strRelationship      = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
            String strType              = DomainConstants.TYPE_ROUTE_TEMPLATE;
            DomainRelationship oldRelationship = null;
            DomainObject domainObjectToType = null;
            String strNewToTypeObjId = "";
            String strRouteBasePurpose = "";
            String strTempRelRouteBasePurpose = "";
            DomainRelationship newRelationship = null;

            //Getting the EC Object id and the new product object id
            String strECObjectId = (String)paramMap.get("objectId");
            // modified for IR-016954
            strNewToTypeObjId = ("".equals((String)paramMap.get("New OID")))?(String)paramMap.get("New Value"):(String)paramMap.get("New OID");

            if (strNewToTypeObjId != null && !strNewToTypeObjId.equalsIgnoreCase("")) {
                //Instantiating DomainObject with the new Route Template object id
                domainObjectToType = newInstance(context, strNewToTypeObjId);
                strRouteBasePurpose = domainObjectToType.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
            }

            //Business Objects are selected by its Ids
            StringList objectSelects = new StringList(2);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);

            //Stringlist containing the relselects
            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //setting the context to the Engineering Change object id
            setId(strECObjectId);

            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
            //Calling getRelatedObjects to get the relationship ids
            relationshipIdList = getRelatedObjects(context,
                                                    strRelationship,
                                                    strType,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);

            if(relationshipIdList.size()>0){
                for (int i=0;i<relationshipIdList.size();i++) {
                    //Getting the realtionship ids from the list
                    String strRelationshipId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    //Getting Route Object Id from the list
                    String strRouteId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_ID);


                    oldRelationship = new DomainRelationship(strRelationshipId);
                    strTempRelRouteBasePurpose = oldRelationship.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
                    //Modified for bug#302310,302308 by Infosys on 21 Apr 05
                    if(strTempRelRouteBasePurpose.equalsIgnoreCase(RANGE_APPROVAL)) {
                        //Checking if the selected Object id is the same as the selected one and exiting the program.
                        if(strRouteId.equals(strNewToTypeObjId)) {
                            return;
                        }
                        //Disconnecting the existing relationship
                        DomainRelationship.disconnect(context, strRelationshipId);
                    }
                }
            }

            if (domainObjectToType != null) {
                //Connecting the Engineering Change with the new Route Template object with relationship Object Route
                newRelationship = DomainRelationship.connect(context,
                                                             this,
                                                             strRelationship,
                                                             domainObjectToType);
                newRelationship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strRouteBasePurpose);
            }

        }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        } finally {
        	ContextUtil.popContext(context);
        }
    }

    public Boolean checkForBOMMode(Context context,String arg[]) throws Exception
    {
        return Boolean.TRUE;
    }


    /**
     * Display the ResponsibleDesignEngineer field in PUEECO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECO object Id
     * @throws Exception if the operations fails
     * @since EC - X4
     */
    public String getRDE (Context context, String[] args) throws Exception {
        StringBuffer sbReturnString = new StringBuffer(1024);
        sbReturnString.append("<input type=\"text\"  name=\"ResponsibleDesignEngineerDisplay\" readonly=\"true\" id=\"\" value=\"");
        sbReturnString.append("");
        sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
        sbReturnString.append("</input>");
        sbReturnString.append("<input type=\"hidden\"  name=\"ResponsibleDesignEngineerOID\" value=\"");
        sbReturnString.append("");
        sbReturnString.append("\">");
        sbReturnString.append("</input>");

        sbReturnString.append("<input type=\"button\" name=\"btnResponsibleDesignEngineer\" value=\"...\"   onclick=\"javascript:showChooser('../engineeringcentral/emxEngrIntermediateSearchUtil.jsp?field=TYPES=type_Person:USERROLE=role_SeniorDesignEngineer:CURRENT=policy_Person.state_Active&amp;table=ENCAssigneeTable&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;formName=emxCreateForm&amp;fieldNameActual=ResponsibleDesignEngineerOID&amp;fieldNameDisplay=ResponsibleDesignEngineerDisplay&amp;mode=Chooser&amp;chooserType=FormChooser&amp;validateField=DesignResponsibilityOID");
        sbReturnString.append("");
        sbReturnString.append("','700','500')\">");
        sbReturnString.append("</input>");
        //modified for the fix 366148
        sbReturnString.append("<a href=\"JavaScript:basicClear('ResponsibleDesignEngineer')\">");
        sbReturnString.append(strClear);
        sbReturnString.append("</a>");
        //366148 fix ends

        return sbReturnString.toString();
    }

    //Added this function for Bug 359409
    public String getPolicyFieldPUEECO(Context context, String[] args)
    throws Exception {
        StringBuffer resultBuffer = new StringBuffer(64);
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String strLanguage = (String) requestMap.get("languageStr");
            String objectId = (String) requestMap.get("copyObjectId");
            String typeName = (String) requestMap.get("type");
            String strPolicy;
            if (typeName.indexOf("type_") > 0) {
                typeName = typeName.substring(typeName.indexOf("type_"),
                        typeName.length());
            }
            DomainObject domCompliancePartObj = null;
            String policy = "";
            resultBuffer.append("<select name=\"Policy1\" id=\"PolicyId\">");
            if (objectId != null && !objectId.equals("")
                    && !objectId.equals("null")) {
                domCompliancePartObj = new DomainObject(objectId);
                policy = domCompliancePartObj.getInfo(context, SELECT_POLICY);
                resultBuffer.append("<option value=\"");
                resultBuffer.append(policy);
                resultBuffer.append("\">");
                resultBuffer.append(policy);
                resultBuffer.append("</option>");
            } else if (typeName != null) {
                MapList policyList = new MapList();
                String partType = PropertyUtil.getSchemaProperty(context,
                        typeName);
                try {
                    policyList = mxType.getPolicies(context, partType, false);
                } catch (FrameworkException eee) {
                    System.out.println("Exception:" + eee.getMessage());
                }
                Iterator policyIterator = policyList.iterator();
                while (policyIterator.hasNext()) {
                    Map policyMap = (Map) policyIterator.next();
                    resultBuffer.append("<option value=\"");
                    String selPolicy = (String)policyMap.get("name");
                    resultBuffer.append(selPolicy);
                    strPolicy = UINavigatorUtil.getAdminI18NString("Policy", selPolicy,strLanguage);
                    resultBuffer.append("\">");
                    resultBuffer.append(strPolicy);
                    resultBuffer.append("</option>");
                }
            }
            resultBuffer.append("</select>");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultBuffer.toString();

    }

    /**
     * Floats the parent EBOM rels from old rev to new rev
     */
    public void floatParentEBOMRels(Context context, String oldRevId, String newRevId) throws Exception {
        if(isTopLevelPart(context, newRevId)) {
            return;
        }

        DomainObject oldRev = new DomainObject(oldRevId);
        DomainObject newRev = new DomainObject(newRevId);
        String ebomRelId = "";
        Map map = null;

        StringList relSels = new StringList();
        relSels.addElement(DomainRelationship.SELECT_RELATIONSHIP_ID);

        // Get only latest parent parts
        MapList mapList = oldRev.getRelatedObjects(context, DomainConstants.RELATIONSHIP_EBOM,
                                                            DomainConstants.TYPE_PART, null, relSels,
                                                            true, false, (short)1, "revision==last", null);
        //Modified for IR-025311 - Ends

        Iterator itr = mapList.iterator();
        while(itr.hasNext()) {
            map = (Map) itr.next();
            ebomRelId = (String)map.get(DomainRelationship.SELECT_RELATIONSHIP_ID);
            DomainRelationship.setToObject(context, ebomRelId, newRev);
        }
    }

    /**
     * Float pending PUE ECOs from old rev to new rev
     * @param context
     * @param selectedObjectId
     * @param contextECOId
     * @throws Exception
     */
    private void floatAffectedItemRels(Context context, String newPartId, String oldPartId) throws Exception {

        DomainObject part = new DomainObject(oldPartId);
        StringList slRelSelect = new StringList();
        slRelSelect.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        MapList pendingPUEECOs      = part.getRelatedObjects(context,
        								RELATIONSHIP_AFFECTED_ITEM,
                						UnresolvedEBOMConstants.TYPE_PUE_ECO,null,
                						slRelSelect,
                						true,
                						false,
                						(short)1,
                						"current!="+UnresolvedEBOMConstants.STATE_PUE_ECO_RELEASE,
                						null);
        String sRelId = "";
        Iterator itr = pendingPUEECOs.iterator();
        while (itr.hasNext()) {
        	Map m = (Map)itr.next();
        	sRelId = (String) m.get(DomainConstants.SELECT_RELATIONSHIP_ID);
        	DomainRelationship.setToObject(context, sRelId, new DomainObject(newPartId));
        }
    }

    /**
	 * Private method to make object to rel connections.
	 * @param context
	 * @param fromObjId
	 * @param toRelId
	 * @param operation
	 * @param relName
	 * @throws Exception
	 */
    private void connectRelToObj(Context context, String toObjId,String fromRelId, String attrName,String attrVal, String relName) throws Exception {

    	String command = "add connection '$1' to \"$2\" fromrel \"$3\"";
    	if(!isNullOrEmpty(attrVal)) {
	        command += " '$4' \"$5\"";
	        MqlUtil.mqlCommand(context, command,relName,toObjId,fromRelId,attrName,attrVal);
    	}
    	else {
    		MqlUtil.mqlCommand(context, command,relName,toObjId,fromRelId);
    	}
    }

    /**
	 * Private method to make object to rel connections.
	 * @param context
	 * @param fromObjId
	 * @param toRelId
	 * @param operation
	 * @param relName
	 * @throws Exception
	 */
    private void connectObjToRel(Context context, String fromObjId,String toRelId, String attrName,String attrVal, String relName) throws Exception {

    	String command = "add connection '$1' from \"$2\" torel \"$3\"";
        if (!isNullOrEmpty(attrVal)) {
	        command += " '$4' \"$5\"";
	        MqlUtil.mqlCommand(context, command,relName,fromObjId,toRelId,attrName,attrVal);
        }
        else {
        	MqlUtil.mqlCommand(context, command,relName,fromObjId,toRelId);
        }

    }

    /**
    * To update Requested Change attribute ofAffected Item relationship
    * @param context
    * @param ecoid
    * @param parentPart
    * @param slEbomChange
    * @throws Exception
    */
    private void updateRequestedChange(Context context, String ecoId, DomainObject parentPart) throws Exception {

        String strLanguage              = "en";
        String strRequestedChangeValue  = EnoviaResourceBundle.getProperty(context,
                                                                    "emxFrameworkStringResource",new Locale(strLanguage),"emxFramework.Range.Requested_Change.For_Update");
        StringList slBusSelect = new StringList(1);
        slBusSelect.add(DomainConstants.SELECT_ID);
        StringList slRelSelect = new StringList(2);
        slRelSelect.add(SELECT_RELATIONSHIP_ID);
        slRelSelect.add("attribute[" + ATTRIBUTE_REQUESTED_CHANGE +"]");

        MapList affectedPartECOIds      = parentPart.getRelatedObjects(context, RELATIONSHIP_AFFECTED_ITEM,
                                                                           UnresolvedEBOMConstants.TYPE_PUE_ECO,slBusSelect,
                                                                           slRelSelect,true,false,(short)1,"current!="+UnresolvedEBOMConstants.STATE_PUE_ECO_RELEASE,"");
        Iterator itrAffectedItem = affectedPartECOIds.iterator();
        Map mapObject = null;
        String objectId = "";
        String requestedChange = "";
        DomainRelationship drAffectedItem = new DomainRelationship();
        while (itrAffectedItem.hasNext()) {
            mapObject = (Map) itrAffectedItem.next();
            objectId = mapObject.get(DomainConstants.SELECT_ID).toString();
            requestedChange = mapObject.get("attribute[" + ATTRIBUTE_REQUESTED_CHANGE +"]").toString();
            if (!ecoId.equals(objectId) && !strRequestedChangeValue.equals(requestedChange)) {
                drAffectedItem = new DomainRelationship(mapObject.get(SELECT_RELATIONSHIP_ID).toString());
                drAffectedItem.setAttributeValue(context, ATTRIBUTE_REQUESTED_CHANGE, strRequestedChangeValue);
            }
        }
    }

    /**
     * Method to update EBOM Pending rels to EBOM and EBOM Change rels to EBOM Change History.
     * @param context
     * @param ebomPendingList
     * @param ebomChangeList
     * @throws Exception
     */
    public void updateRelTypes(Context context, StringList ebomPendingList, StringList ebomChangeList) throws Exception
    {
    	String key = "";
    	for (int i=0;i<ebomPendingList.size();i++) {
    		key = (String)ebomPendingList.get(i);
    		DomainRelationship.setType(context, key, DomainConstants.RELATIONSHIP_EBOM);
    		DomainRelationship.setType(context, (String)ebomChangeList.get(i), UnresolvedEBOMConstants.RELATIONSHIP_EBOM_CHANGE_HISTORY);
    	}
    }

    /**
     * Method to convert an object to a StringList
     * @param Object
     * @return StringList
     */
    private static StringList toStringList(Object obj)
    {
        if (obj == null)
            return new StringList();
        StringList slObject = new StringList(1);
        if (obj instanceof StringList) {
            slObject = (StringList) obj;
        } else {
            slObject.addElement((String) obj);
        }
        return slObject;
    }
    
    private boolean isUnConfigUnReleasedPartExistsAsChild(Context context, StringList affectedItemList) throws Exception {
    	boolean unconfiUnreleasePartExists = false;
    	String objWhere = "policy!='" + UnresolvedEBOMConstants.POLICY_CONFIGURED_PART + "' && current!='" + UnresolvedEBOMConstants.STATE_PART_RELEASE + "'";
    	
    	for (int i = 0; i < affectedItemList.size(); i++) {
    		MapList ebomList = DomainObject.newInstance(context, (String) affectedItemList.get(i))
															.getRelatedObjects(context,
																DomainConstants.RELATIONSHIP_EBOM + "," + UnresolvedEBOMConstants.RELATIONSHIP_EBOM_PENDING,
																DomainConstants.TYPE_PART,
																null,
																null,
																false,
																true,
																(short) 1,
																objWhere, 
																null);
    		
    		if (!ebomList.isEmpty()) {
    			unconfiUnreleasePartExists = true;
    			break;
    		}
    	}
    	
    	return unconfiUnreleasePartExists;
    }

    /**
    * Floats the old rev rels
    */
    public void floatOldRevRels(Context context, String oldRevId, String newRevId) throws Exception {
          if(isTopLevelPart(context, newRevId)) {
            return;
        }

        DomainObject oldRev = new DomainObject(oldRevId);
        DomainObject newRev = new DomainObject(newRevId);

        StringList objSels = new StringList(1);
        objSels.addElement(DomainConstants.SELECT_ID);

        StringList relSels = new StringList(1);
        relSels.addElement(DomainRelationship.SELECT_RELATIONSHIP_ID);
        MapList mapList = oldRev.getRelatedObjects(context, DomainConstants.RELATIONSHIP_EBOM,
                                                            DomainConstants.TYPE_PART, objSels, relSels,
                                                            true, false, (short)1, null, null);
        Iterator itr = mapList.iterator();
        while(itr.hasNext()) {
            Map map = (Map) itr.next();
            String ebomRelId = (String)map.get(DomainRelationship.SELECT_RELATIONSHIP_ID);
            String partId = (String)map.get(DomainObject.SELECT_ID);
            //float the existing EBOM rel to new revision...
            DomainRelationship.setToObject(context, ebomRelId, newRev);
            DomainRelationship.connect(context, new DomainObject(partId),
            								UnresolvedEBOMConstants.RELATIONSHIP_EBOM_HISTORY, new DomainObject(oldRev));
        }
    }
    //Added for X7 - Ends

    //2011x - Starts
    /**
     * Method to return PUE ECOs for Top Level Part
     * @param context
     * @param String[]
     * @return StringList
     */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getPUEECOs(Context context, String[] args) throws Exception {
    	MapList pueECOList = getPUEECOsImpl(context, false, args);

    	StringList listReturn = new StringList(pueECOList.size());

    	for (int i = 0, size = pueECOList.size(); i < size; i++) {
    		listReturn.add((String) ((Map) pueECOList.get(i)).get(DomainConstants.SELECT_ID));
    	}
    	return listReturn;
    }

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRelatedPUEECOs(Context context, String args[]) throws Exception {
    	MapList mlRelatedPUEECOs	= getPUEECOsImpl(context, true, args);

    	return mlRelatedPUEECOs;
    }
    //2011x - Ends

    public StringList getPUEECOEffectivity (Context context, String[] args) throws Exception {
    	StringList slPUEECOEffectivity = new StringList();
    	HashMap programMap = (HashMap)JPO.unpackArgs(args);
    	MapList objectList = (MapList) programMap.get("objectList");
    	int objectSize	   = objectList.size();
    	for (int index=0; index < objectSize; index++) {
    		Map changeMap	= (Map) objectList.get(index);
    		String changeId	= (String) changeMap.get(DomainConstants.SELECT_ID);
    		//String displayValue	= UnresolvedPart.getEffectivityValue(context, changeId, EffectivityFramework.DISPLAY_VALUE, true);
    		//String displayValue	= CFFUtil.getEffectivityOnChangeDisplayExpression(context, changeId);
    		String displayValue	= "";
    		slPUEECOEffectivity.add(displayValue);
    	}
    	return slPUEECOEffectivity;
    }
    /** This method returns the context ProdIds to be pre-populated in the effectivity selector in the CFF dialog window.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *        effectivityExpression: the actual effectivity expression from which the context Ids have to be retrieved
     * @return returns MapList containing the contextIds from the actual expression
     * @throws Exception if the operation fails
     */
    public MapList getContextsProducts(Context context, String[] args)throws Exception
    {
    	MapList ContextOIdsList  = new MapList();
    	DomainObject doObject 	 = DomainObject.newInstance(context);

    	HashMap programMap 	= (HashMap)JPO.unpackArgs(args);
    	HashMap requestMap  = (HashMap)programMap.get("RequestValuesMap");
    	String  objectId 	= (String)programMap.get("objectId");
    	String  parentID 	= (String)programMap.get("parentID");

    	String[] rootIdArr 	= (String[])requestMap.get("rootID");
    	String   rootId		= (String)rootIdArr[0];

    	StringList slTempList    	  = new StringList();
    	StringList slProductList  	  = new StringList();
    	String  actualExpression      = "";
    	objectId =  !isNullOrEmpty(rootId)?rootId:objectId;
    	//Code starts for Bom Powerview context and Edit PUE ECO context
    	if(!isNullOrEmpty(objectId)){
    		doObject.setId(objectId);
    		//code check to find whether it came from bompower view context
    		if(!doObject.isKindOf(context,PropertyUtil.getSchemaProperty(context, "type_Change"))){
    			//code to check for Toplevel part if it is ,return the same product
    			String prodIdForTopPart        =   doObject.getInfo(context, "to["+UnresolvedEBOMConstants.RELATIONSHIP_ASSIGNED_PART+"].from.id");
    			if (!"".equals(prodIdForTopPart) && prodIdForTopPart != null && !"null".equals(prodIdForTopPart)){
    				return constructProductMaplist(context,toStringList(prodIdForTopPart));

    			 }
    			//For Non top level part case, while editing current effectivity need to return empty values
    			else if (!isNullOrEmpty(rootId) && isNullOrEmpty(prodIdForTopPart)){
    				return ContextOIdsList;
    			}
    			//In case of bom filter context
			 else{
				StringList slEBOMIdList			=	doObject.getInfoList(context, "from["+DomainConstants.RELATIONSHIP_EBOM+"].id");
				StringList slEBOMPendingList	=	doObject.getInfoList(context, "from["+UnresolvedEBOMConstants.RELATIONSHIP_EBOM_PENDING+"].id");
				slTempList.addAll(constructProductListFromRelIDs(context,slEBOMIdList,true));
				slTempList.addAll(constructProductListFromRelIDs(context,slEBOMPendingList,false));
				ContextOIdsList.addAll(constructProductMaplist(context, slTempList));
			   }

    		}else{  //code to execute for edit PUEECO context
    		 //actualExpression = UnresolvedPart.getEffectivityValue(context, objectId, EffectivityFramework.ACTUAL_VALUE, true);
    		 actualExpression = "";
    		 slProductList    = effectivity.getEffectivityUsage(context, actualExpression);
    		 return constructProductMaplist(context, slProductList);
    	  }
  	  }
    	//Code to execute for Product Context
     if (isNullOrEmpty(objectId) && !isNullOrEmpty(parentID)){
    	 return constructProductMaplist(context,toStringList(parentID));
    	}

     return ContextOIdsList;
  }

    private boolean isNullOrEmpty(String testString)
	{
		return testString == null || testString.trim().length() == 0 || "null".equalsIgnoreCase(testString.trim());
	}

    /**
     * Added for IR-066748V6R2011x
     * Method to construct StringList of products for the given StringList of connectionids..
     * @param context the Ematrix code context object.
     * @param slProductList the products list.
     * @param isFromEBOM value true for EBOM and false for EBOM Pending
     * @return StringList of Products.
     * @throws Exception if the operation fails
     */
    private StringList constructProductListFromRelIDs(Context context,StringList slProductList,boolean isFromEBOM)throws Exception{
	StringList prodList		= new StringList();
	Iterator itrRelIds		= slProductList.iterator();
	while (itrRelIds.hasNext()) {
		String sRelId	    = (String) itrRelIds.next();
		//if connectionId is EBOM then execute both the funtions
		if (isFromEBOM)
			 prodList.addAll(effectivity.getRelEffectivityUsage(context, sRelId));

		//String actualExpression = UnresolvedPart.getEffectivityValue(context, sRelId, EffectivityFramework.ACTUAL_VALUE, false);
		String actualExpression = "";
		if (!"".equals(actualExpression))
			prodList.addAll(effectivity.getEffectivityUsage(context, actualExpression));
	}
	return prodList;
}


/**
 * Method to construct MapList of products from the given StringList.
 * @param context the Ematrix code context object.
 * @param slProductList the products list.
 * @return MapList of Products.
 */
   private MapList constructProductMaplist(Context context,StringList slProductList) {
	   MapList ContextProdOIdsList = new MapList();
	   StringList slProdList	   = new StringList();
	   for(int i = 0; i < slProductList.size(); i++)
       {
		   Map idMap = new HashMap();
		   String prodId	= (String) slProductList.elementAt(i);
		   if (!slProdList.contains(prodId)) {
			   slProdList.add(prodId);
			   idMap.put(DomainConstants.SELECT_ID, prodId);
			   ContextProdOIdsList.add(idMap);
		   }
       }
	   return ContextProdOIdsList;
   }
/**Method to return affected product names for for the table PUEECOSummary
 * @author yoq-Added for IR-068210V6R2011x
 * @param context the Matrix code context object
 * @param args the packed hashMap of request parameters
 * @return Vector of product names
 * @throws Exception if the operation fails
 */
 public StringList getAffectedProducts(Context context, String []args)throws Exception
 {
	 HashMap programMap	    = (HashMap)JPO.unpackArgs(args);
 	 MapList objectList 	= (MapList)programMap.get("objectList");
     StringList  affProds   = new StringList(objectList.size());
     String strObjId  		= "";
     //get all the affected products for each PUEECO id
     for(int index = 0; index < objectList.size(); index++)
     {
     	Map mpObj   = (Map)objectList.get(index);
     	strObjId    = (String)mpObj.get("id");
     	StringList prodList = getStringListOfProductsForChangeId(context, strObjId);
     	affProds.addElement(getProdNamesforProducts(context,prodList));

 	 }
     return affProds;
   }

/** The method returns a string contains product Names for the given stringlist of products
 *@author yoq-Added for IR-068210V6R2011x
 * @param context the eMatrix code context object
 * @param prodList the given stringlist of products
 * @return String of product Names
 * @throws FrameworkException if the operation fails
 */
 private String getProdNamesforProducts(Context context,StringList prodList)throws FrameworkException

 {
	 StringBuffer prodBuffer= new StringBuffer(256);
	 StringList selects     = new StringList(DomainConstants.SELECT_ID);
	 selects.addElement(DomainConstants.SELECT_NAME);
	 DomainObject doProdObj = null;
	 Iterator prodIterator = prodList.iterator();
	 while (prodIterator.hasNext())
	 {
		 String prodId = (String)prodIterator.next();
		 doProdObj     = DomainObject.newInstance(context, prodId);
		 Map<String,String> prodMap = (Map)doProdObj.getInfo(context, selects);
		 prodBuffer.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory="
					+ "unresolvedebom"
					+ "&amp;jsTreeID="
					+ "null"
					+ "&amp;mode=insert&amp;suiteKey="
					+ "unresolvedebom"
					+ "&amp;objectId="
					+ prodMap.get(DomainConstants.SELECT_ID)
					+ "', '700', '600', 'false', 'popup', '')\">"
					+ prodMap.get(DomainConstants.SELECT_NAME) + "</a>");
	 }
	return prodBuffer.toString();
  }

  /**
  * This method displays Affected item warning field with radio button
  * @param context the eMatrix <code>Context</code> object.
  * @param args[] packed hashMap of request parameters
  * @return String containing html data to construct with radio button.
  * @throws Exception if the operation fails.
   */
public Object displayAffectedItemWarning(Context context, String[] args) throws Exception
 	{
		Locale Local = context.getLocale();
		String KeepAffectedItems  = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.CancelECODialog.KeepAffectedItems");

		 StringBuffer strBuf = new StringBuffer(256);
		 strBuf.append("<table><tr><td align=left>");
		 strBuf.append("<input type=radio checked name='deleteAffectedItems' value='false'>");
		 strBuf.append("</td><td align=left>");
		 strBuf.append(XSSUtil.encodeForHTMLAttribute(context, KeepAffectedItems));
		 strBuf.append("</td></tr><table>");

		 return strBuf.toString();
 	}

 /**
	* Connects/disconnects the selected StringList of affected items to the PUEECO
	* @author YOQ
	* @param context eMatrix context object
	* @param args packed hashMap of request parameters
	* @throws Exception If the operation fails.
	*/
	public void connectOrDisconnectAffectedItems(Context context,String[] args) throws Exception
	{
	try {
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
	    boolean  connect    = (programMap.containsKey("connect"))?(Boolean)programMap.get("connect"):false;
		String  createMode  = (String)programMap.get("CreateMode");

		String     strTargetECOId     = (String) programMap.get("targetECOId");
		StringList affectedItemsList  = new StringList();
		String     sRelType           = (String) PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem");
		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");

		if ("CreateECO".equalsIgnoreCase(createMode)){
			 affectedItemsList  = (StringList) programMap.get("affectedItems");
		     Map relIDMap = DomainRelationship.connect(context, new DomainObject(strTargetECOId), sRelType, true,(String [])affectedItemsList.toArray(new String[1]));
		     updateAffectedItemAttributes(context,relIDMap);

		} else if (connect) {
			 affectedItemsList  = (StringList) programMap.get("affectedItems");
			 Iterator itr = affectedItemsList.iterator();
			 while (itr.hasNext()) {
				 String partId = (String)itr.next();
				 String relId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",partId,"to[" + RELATIONSHIP_AFFECTED_ITEM + "|from.id=="+strTargetECOId+"].id");
				 if (relId==null || "".equals(relId))
				 {
					 DomainRelationship newRelId = DomainRelationship.connect(context, new DomainObject(strTargetECOId), sRelType, new DomainObject(partId));
					 newRelId.setAttributeValue(context, strAttrAffectedItemCategory, "Indirect");
				 }
				 else
					 DomainRelationship.newInstance(context, relId).setAttributeValue(context, strAttrAffectedItemCategory, "Indirect");

					 }
			} else {
					 affectedItemsList   = (StringList) programMap.get("affectedItems");
					 String  select      = "to[" + RELATIONSHIP_AFFECTED_ITEM + "].id";
					 String  relSelect   = "to[" + RELATIONSHIP_AFFECTED_ITEM + "|attribute[" + strAttrAffectedItemCategory + "] == Indirect].id";

					 StringList slObjSelect 	   = new StringList(relSelect);
					 StringList relIDsToDisconnect = new StringList();

					 MapList mlRelValues   = DomainObject.getInfo(context, (String [])affectedItemsList.toArray(new String[1]), slObjSelect);
			         Iterator itrRelValues = mlRelValues.iterator();
			         while (itrRelValues.hasNext()) {
			        		Map map  = (Map) itrRelValues.next();
			        		relIDsToDisconnect.addAll(FrameworkUtil.split((String)map.get(select),delimiter));
			        	}

			        if (relIDsToDisconnect.size() > 0) {
			        		DomainRelationship.disconnect(context, (String [])relIDsToDisconnect.toArray(new String[1]));
			        }
		       }
			}catch (Exception e) {
				e.printStackTrace();
				throw new Exception (e);
			}
     }

	private void updateAffectedItemAttributes(Context context, Map relIdMap) throws Exception {
		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
		Iterator relItr      =  relIdMap.values().iterator();
		while (relItr.hasNext()) {
			DomainRelationship.newInstance(context, (String)relItr.next()).setAttributeValue(context, strAttrAffectedItemCategory, "Indirect");
		}
	}

    /**
     * Get the list of all Objects which are connected to the context Change object as
     * "Affected Items"
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @return        a eMatrix <code>MapList</code> object having the list of Affected
     * Items for this Change object
     * @throws        Exception if the operation fails
     * @since         R212
     **/
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getPUEAffectedItems(Context context,String[] args)throws Exception {
	    //unpacking the arguments from variable args
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
	    String strParentId  = (String)programMap.get(SELECT_OBJECT_ID);
	   //Initializing the return type
        MapList mlAffectedItemBusObjList = new MapList();
        //Business Objects are selected by its Ids
        StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
        //Relationships are selected by its Ids
        StringList relSelects     = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //retrieving Affected Items list from context Change object
		DomainObject changeObj = new DomainObject(strParentId);
		StringBuffer bufType = new StringBuffer(SYMB_WILD);
	    String relPattern = RELATIONSHIP_AFFECTED_ITEM;

        mlAffectedItemBusObjList = changeObj.getRelatedObjects(context,
												relPattern, // relationship pattern
												bufType.toString(), // object pattern
												objectSelects, // object selects
												relSelects, // relationship selects
												false, // to direction
												true, // from direction
												(short) 1, // recursion level
												null, // object where clause
												null); // relationship where clause
		return  mlAffectedItemBusObjList;
    }

   /**
    * Gets all the PUE ECO id which is not cancelled in MapList which is connected to Model with Unit and Feature option effectivity.
    * context ematrix context
    * boolean addMultiPart
    * args required arguments
    * throws if any operation fails
    * returns MapList which contains valid ecoIds.
    */
	public MapList getPUEECOsImpl(Context context, boolean addMultiPart, String[] args) throws Exception {

		MapList listReturn = new MapList();

		HashMap programMap = (HashMap)JPO.unpackArgs(args);
    	String objectId	   = (String) programMap.get("objectId");

    	DomainObject domObj	= DomainObject.newInstance(context, objectId);

    	if (domObj.isKindOf(context, DomainConstants.TYPE_PART)) {
    		objectId = domObj.getInfo(context, "to[" + UnresolvedEBOMConstants.RELATIONSHIP_ASSIGNED_PART + "].from.id");
    		domObj.setId(objectId);
    	}

		String strChangeId;
		String strChangeType;
		String strChangeCurrent;

		String SELECT_CHANGE_ID = "to[" + EffectivityFramework.RELATIONSHIP_NAMED_EFFECTIVITY + "].from.id";
		String SELECT_CHANGE_TYPE = "to[" + EffectivityFramework.RELATIONSHIP_NAMED_EFFECTIVITY + "].from.type";
		String SELECT_CHANGE_CURRENT = "to[" + EffectivityFramework.RELATIONSHIP_NAMED_EFFECTIVITY + "].from.current";
		String SELECT_MODEL_ID_FROM_NAMED_EFFECTIVITY = "from[" + EffectivityFramework.RELATIONSHIP_EFFECTIVITY_USAGE + "].to.id";

		StringList objectSelect = new StringList(SELECT_CHANGE_ID);
		objectSelect.add(SELECT_CHANGE_TYPE);
		objectSelect.add(SELECT_CHANGE_CURRENT);
		
		if (!addMultiPart) {
			objectSelect.addElement(SELECT_MODEL_ID_FROM_NAMED_EFFECTIVITY);
		}

		MapList mapListPUEChangs = domObj.getRelatedObjects(context, EffectivityFramework.RELATIONSHIP_EFFECTIVITY_USAGE,
																		EffectivityFramework.TYPE_NAMED_EFFECTIVITY, objectSelect,
															    		null, true, false, (short) 1, null,
														    			null, (short) 0, false, false, (short) 0,
														    			null, null, null, null);

		Map map;
		StringList tempList;

		HashSet hSetFOPUEECOIds = new HashSet(getFeatureOptionEffectivityECOIds(context, objectId));

		for (int i = 0, size = mapListPUEChangs.size(); i < size; i++) {
			map = (Map) mapListPUEChangs.get(i);

			strChangeId = (String) map.get(SELECT_CHANGE_ID);
			strChangeType = (String) map.get(SELECT_CHANGE_TYPE);
			strChangeCurrent = (String) map.get(SELECT_CHANGE_CURRENT);
			
			if (!(UIUtil.isNullOrEmpty(strChangeType) || strChangeType.equals(ChangeConstants.TYPE_CHANGE_ORDER) || "Cancelled".equals(strChangeCurrent))) {
				if (addMultiPart) {
					map.put(DomainConstants.SELECT_ID, strChangeId);
					listReturn.add(map);
				} else {
					tempList = toStringList(map.get(SELECT_MODEL_ID_FROM_NAMED_EFFECTIVITY));
	
					if (tempList.size() == 1) {
						map.put(DomainConstants.SELECT_ID, strChangeId);
						listReturn.add(map);
					}
				}
			}
			
			hSetFOPUEECOIds.remove(strChangeId);
		}

		addFeatureOptionEffectivityECOIdsToList(context, listReturn, hSetFOPUEECOIds, addMultiPart);

		return listReturn;
	}

	   /**
	    * This method checks the expression for single model or multi model expression by comparing model Ids.
	    * expression effectivity expression
	    * throws exception if any operation fails
	    * returns true if it is single model effectivity
	    */
	   private boolean singleModelExpr(String expression) throws Exception {
		   boolean boolOut = true;

		   if (expression != null && expression.indexOf("OR") > -1) {

			   StringList exprList = FrameworkUtil.splitString(expression, "OR");
			   String exprTemp;

			   for (int i = 0; i < exprList.size(); i++) {
				   exprTemp = (String) exprList.get(i);

				   if (i == 0) {
					   expression = exprTemp.replaceAll("^.*~", "").trim();
				   } else if (!expression.equals(exprTemp.replaceAll("^.*~", "").trim())) {
					   boolOut = false;
					   break;
				   }
			   }
		   }

		   return boolOut;
	   }

	   /**
	    * This method gets all the PUE ECO ids connected to Model with Feature option effectivity.
	    * context ematrix context
	    * modelId Model Id
	    * throws exception if any operation fails
	    * returns StringList which contains ECO ids
	    */
	   private StringList getFeatureOptionEffectivityECOIds(Context context, String modelId) throws Exception {
		   
		   String REL_MAIN_PRODUCT = PropertyUtil.getSchemaProperty(context, "relationship_MainProduct");
		   String REL_CONFIG_OPTIONS = PropertyUtil.getSchemaProperty(context, "relationship_ConfigurationOptions");
		   String REL_CONFIG_FEATURES = PropertyUtil.getSchemaProperty(context, "relationship_ConfigurationFeatures");

		   String types = TYPE_PUEECO;
		   
		   //selectable for getting CCA / PUE ECO from Product Context.
		   String SELECT_CO_PUEECO_ID_PRODUCT_CONTEXT = "from["+REL_MAIN_PRODUCT+"].to.from[" + REL_CONFIG_FEATURES 
		   + "].to.from[" + REL_CONFIG_OPTIONS + "].tomid[" + EffectivityFramework.RELATIONSHIP_EFFECTIVITY_USAGE 
		   + "].from.to[" + EffectivityFramework.RELATIONSHIP_NAMED_EFFECTIVITY + "].from[" + types + "].id";

		   String mqlQuery = "print bus $1 select $2 dump $3";
		   String queryResult = MqlUtil.mqlCommand(context, mqlQuery, modelId, SELECT_CO_PUEECO_ID_PRODUCT_CONTEXT, "|");

		   return FrameworkUtil.split(queryResult, "|");
	   }

	   /**
	    * This method checks for PUE ECO which is cancelled and adds all the valid PUE ECO ids to dataList connected to Model with Feature option effectivity.
	    * context ematrix context
	    * dataList MapList
	    * ecoIdSet HashSet with unigue ECO ids
	    * boolean addmultipart
	    * throws exception if any operation fails
	    * returns void
	    */
	   private void addFeatureOptionEffectivityECOIdsToList(Context context, MapList dataList, HashSet ecoIdSet, boolean addMultiPart) throws Exception {
		   if (!ecoIdSet.isEmpty()) {
			    String current;

			    String SELECT_EFFECTIIVTY_EXPR = "from[" + EffectivityFramework.RELATIONSHIP_NAMED_EFFECTIVITY + "].to.attribute[" + EffectivityFramework.ATTRIBUTE_EFFECTIVITY_EXPRESSION + "]";

				StringList objectSelects = new StringList(3);
				objectSelects.addElement(DomainConstants.SELECT_CURRENT);
				objectSelects.addElement(DomainConstants.SELECT_ID);
				objectSelects.addElement(SELECT_EFFECTIIVTY_EXPR);

				MapList infoList = DomainObject.getInfo(context, (String[]) ecoIdSet.toArray(new String[0]), objectSelects);

				Iterator iterator = infoList.iterator();

				Map map;

				while (iterator.hasNext()) {
					map = (Map) iterator.next();

					current = (String) map.get(DomainConstants.SELECT_CURRENT);

					if (!"Cancelled".equals(current) && (addMultiPart || singleModelExpr((String) map.get(SELECT_EFFECTIIVTY_EXPR)))) {
						map.put("level", "1");
						dataList.add(map);
					}
				}
		   }
	   }
	   
	   private StringList getConfiguredPartIds(Context context, String[] selectedRows) throws Exception {
		   StringList confPartIdList = new StringList();
		   
		   if (selectedRows != null && selectedRows.length > 0) {
				String SELECT_POLICY_CLASSIFICATION = "policy.property[PolicyClassification].value";
				StringList objectSelect = new StringList(2);
				objectSelect.addElement(SELECT_POLICY_CLASSIFICATION);
				objectSelect.addElement(DomainConstants.SELECT_ID);
				MapList infoList = DomainObject.getInfo(context, (String[]) new ChangeUtil().getObjectIdsFromTableRowID(selectedRows).toArray(new String[0]), objectSelect);
				
				Map infoMap;
				String policyClassification;
				Iterator iterator = infoList.iterator();
				while (iterator.hasNext()) {
					infoMap = (Map) iterator.next();
					policyClassification = (String) infoMap.get(SELECT_POLICY_CLASSIFICATION);
					
					if ("Unresolved".equals(policyClassification)) {
						confPartIdList.addElement((String) infoMap.get(DomainConstants.SELECT_ID));
					}
				}
			}
		   
		   return confPartIdList; 
	   }
	
	/**
	 * The Action trigger  method on (Pending --> In Work) to Promote Connected CO to In Work State
	 * @param context
	 * @param args (Change Action Id)
	 * @throws Exception
	 */
	public void promoteConnectedCO(Context context, String args[]) throws Exception {
		String type = args[8];
		String targetState = args[9];
		
		if (ChangeConstants.TYPE_CCA.equals(type)) {
			String stateCancelled = PropertyUtil.getSchemaProperty(context, "policy", UnresolvedEBOMConstants.POLICY_PUE_ECO, "state_Cancelled");
			
			if (!stateCancelled.equals(targetState)) {
				new ChangeAction().promoteConnectedCO(context, args);
			}
		}
	}
	
	/**
	 * Method to check if the CECO connected affected items are in Release Phase as Development. Check for attribute Release Phase of Part..
	 * @param context
	 * @param args (CECO Id)
	 * @throws Exception
	 */
	public int sCheckAffectedItemsInDevelopmentReleasePhase(Context context, String[] args) throws Exception {
		String changeId = args[0];
		int checkResult = 0;
		Map map;
		String strError ="";
		String strMessage = "";
		boolean displayErrorMsg = false;
		DomainObject changeDOMObject = DomainObject.newInstance(context, changeId);
		String objWhere = "(attribute[" + EngineeringConstants.ATTRIBUTE_RELEASE_PHASE + "]==Development)" ;
		StringList busSelects = new StringList(DomainObject.SELECT_ID);
		busSelects.add(DomainObject.SELECT_NAME);
		busSelects.add(DomainObject.SELECT_REVISION);
		MapList mapList = changeDOMObject.getRelatedObjects(context,
				DomainConstants.RELATIONSHIP_AFFECTED_ITEM, DomainConstants.TYPE_PART, busSelects,
				null, false, true, (short) 1, objWhere, null, 0);
		for (int i = 0, size = mapList.size(); i < size; i++) {
			map = (Map) mapList.get(i);
			strError += (String) map.get(SELECT_NAME) + "     " +  (String) map.get(SELECT_REVISION) +"\n" ;
			displayErrorMsg = true;
		}
		if(displayErrorMsg){
			strMessage = EnoviaResourceBundle.getProperty(context, "emxUnresolvedEBOMStringResource", 
					context.getLocale(), "emxUnresolvedEBOM.Warning.AffectedItemConnectedInDevReleasePhase")+ "\n\n"+strError;

			emxContextUtilBase_mxJPO.mqlNotice(context,strMessage);
			checkResult = 1;
		}
		return checkResult;
	}
	
	
	/*
	* This method is used to automatically promote the CECO when the CO is complete. 
	* the method is used in the ECMConfig.xml for custom change RELEASEJPO entry
	*
	* Returns
	* 0 = Successful
	* 1 = Failure 
	* 
	*/
	
	public int releaseConfiguredECO(Context context, String[] ChangeInfo) throws Exception {
		
		try{

			System.out.println("~~~~~~~~~~~~~~ emxPUEECO base : releaseConfiguredECO Called ");
		 } catch (Exception e) {
				
				throw new Exception (e);
			}
		
		return 0;
	}
	
	
	/**
     * @param context
     * @param objId
     * @return
     * @throws Exception
     */
    public static boolean isTopLevelPart(Context context,String objId) throws Exception
    {
        DomainObject DO = new DomainObject(objId);
        String select = "to["+UnresolvedEBOMConstants.RELATIONSHIP_ASSIGNED_PART+"].from.id";
        String info = DO.getInfo(context, select);

        return (info!=null && !"".equals(info));
    }
    
    /**
     * Returns a StringList of affected product ids for the given PUE ECO.
     * @param context the eMatrix <code>Context</code> object
     * @param changeId String representing a change id.
     * @return StringList representing the Named Effectivity object id.
     * @throws Exception if the operation fails
     */
    public static StringList getStringListOfProductsForChangeId(Context context, String changeId)
    throws Exception
    {
    	StringList prodlist = new StringList();
    	MapList prodInfo    = new EffectivityFramework().getEffectivityOnChange(context, changeId);
        for (int index=0;index<prodInfo.size();index++){
        	HashMap<String,String> prodExprMap   = (HashMap)prodInfo.get(index);
        	String actualExpr 					 = prodExprMap.get("actualValue");
        	Map<String,String> prodIdMap         = new EffectivityFramework().getExpressionSequence(context, actualExpr);
        	Iterator prodIte = prodIdMap.keySet().iterator();
        	while (prodIte.hasNext()){
        		prodlist.addElement(prodIte.next());
        	}
        }
        return prodlist;
    }
	
	
	
}
