/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.AccessList;
import matrix.db.AttributeType;
import matrix.db.AttributeTypeList;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.db.Vault;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.domain.util.mxFtp;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.util.AttributeUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.MyOutputStream;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.VaultUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.engineering.Change;
import com.matrixone.apps.engineering.ECO;
import com.matrixone.apps.engineering.ECR;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.framework.ui.RenderPDF;
import com.matrixone.jsystem.util.StringUtils;

/**
 * The <code>emxECRBase</code> class contains implementation code for emxECR.
 *
 * @version EC Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxECRBase_mxJPO extends enoEngChange_mxJPO
{
    /** state "Plan ECO" for the "ECR Standard" policy. */
  public static final String STATE_ECR_PLANECO =
            PropertyUtil.getSchemaProperty("policy",
                                    POLICY_ECR,
                                           "state_PlanECO");
	/* Branch To Attribute */
	public static final String strBranchTo = PropertyUtil.getSchemaProperty("attribute_BranchTo");
	//start:bug 357325
    /* Route Instructions */
    public static final String ATTRIBUTE_ROUTE_INSTRUCTIONS = PropertyUtil.getSchemaProperty("attribute_RouteInstructions");
    //end:bug 357325


    //** DOCUMENTS symbolic name
    public static final String TYPE_DOCUMENTS = PropertyUtil.getSchemaProperty(SYMBOLIC_type_DOCUMENTS);
     //Relationship Affected Item.
	public static final String RELATIONSHIP_AFFECTED_ITEM =
            PropertyUtil.getSchemaProperty("relationship_AffectedItem");
	//public static final String RELATIONSHIP_CHANGE_AFFECTED_ITEM =
       // PropertyUtil.getSchemaProperty("relationship_ChangeAffectedItem");
	//public static final String RELATIONSHIP_IMPLEMENTED_ITEM =
       // PropertyUtil.getSchemaProperty("relationship_ImplementedItem");

    	  /** Person Admin Person */
  String personAdminType = "person";

  /** name of preference properties */
  String PREFERENCE_ENC_DEFAULT_VAULT = "preference_ENCDefaultVault";
  /** name of preference properties */
  String PREFERENCE_DESIGN_RESPONSIBILITY = "preference_DesignResponsibility";

	public static final String SELECT_RELATIONSHIP_DESIGN_RESPONSIBILITY = "to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id";
	public static final String SELECT_ATTRIBUTE_REQUESTED_CHANGE = "attribute[" + ATTRIBUTE_REQUESTED_CHANGE + "]";
	public static final String TYPE_EBOM_MARKUP = PropertyUtil.getSchemaProperty("type_EBOMMarkup");

    static final int LT = 0;
    static final int GT = 1;
    static final int EQ = 2;
    static final int LE = 3;
    static final int GE = 4;
    static final int NE = 5;

    public static String strClear="";

    /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @throws Exception if the operation fails.
    * @since EC Rossini.
    */
    public emxECRBase_mxJPO (Context context, String[] args)
      throws Exception
    {
        super(context, args);
        strClear =EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",context.getSession().getLanguage());
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return an int.
     * @throws Exception if the operation fails.
     * @since EC Rossini.
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxECO invocation");
        }
        return 0;
    }

    /**
    * Check the number of ECO's connected to the ECR, If more than one ECO connected display warning
    * "Automatic revisioning is not possible, because this ECR is connected to
    * more than 1 ECO". If only one ECO connected, then the trigger will evaluate
    * each affected item connected to the ECR, to check for the following conditions.
    *
    * 1) The Affected item has a revision sequence from which the next revision can be determined.
    * 2) The current user has revise access on that Affected item.
    *
    * If both these check are valid for an affected item, then the trigger will
    * get the next revision, and connect the next revision to the ECO, provided
    * the next revision of affected item not connected to any other ECO.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @return void.
    * @throws Exception if the operation fails.
    * @since EC 10.0.0.0..
    */
    public void autoRevECRAffectedItems(matrix.db.Context context, String[] args) throws Exception
    {
        try {
			String strIsFromAssignTo = PropertyUtil.getRPEValue(context, "MX_FROM_ASSIGNTO", false);
			String strECOID = null;
			if ("true".equals(strIsFromAssignTo))
			{
				return;
			}

            // If the ECR not in "Plan ECO" state, then the auto-revise
            // of affected items need not be done.
            String curState = getInfo(context, SELECT_CURRENT);//getCurrentState(context).getName();
            if ( !curState.equals(STATE_ECR_PLANECO) )
            {
                return;
            }

            StringList objectSelects = new StringList(1);
            objectSelects.addElement(DomainObject.SELECT_ID);

            MapList objList = getRelatedObjects( context,
                                                 RELATIONSHIP_ECO_CHANGEREQUESTINPUT,
                                                 TYPE_ECO,
                                                 objectSelects,
                                                 null,
                                                 true,
                                                 false,
                                                 (short)1,
                                                 null,
                                                 null);

            DomainObject ECO = new DomainObject();
            String sWarning  = null;
            Map objMap       = null;

            // Checks the number of ECO's connected to the ECR, if more or less
            // than one ECR display warning
            if (objList == null)
            {
                emxContextUtil_mxJPO.mqlNotice(context,
                    EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.AutoRevECRAffectedItemsErrorNoECO",
                    context.getSession().getLanguage()));
                return;
            }
            else if(objList.size() !=1)
            {
                emxContextUtil_mxJPO.mqlNotice(context,
                    EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.AutoRevECRAffectedItemsErrorManyECO",
                    context.getSession().getLanguage()));
                return;
            }
            else
            {
                //get the ECO that is connected to this ECR
                objMap = (Map) objList.get(0);
                ECO.setId((String)objMap.get(DomainObject.SELECT_ID)); // connected ECO
                //keep it in ths String for aff item check later
                strECOID = (String)objMap.get(DomainObject.SELECT_ID);
            }

            //need to get policyclassification for ECR and ECO
            //in order to determine proper relationships
            String ecrRelPattern = "";
            String ecoPolicy = ECO.getInfo(context, SELECT_POLICY);
            String ECOpolicyClassification = FrameworkUtil.getPolicyClassification(context, ecoPolicy);
                ecrRelPattern = RELATIONSHIP_AFFECTED_ITEM;

            // Get the list of affected items connected to the ECR
            objectSelects = new StringList(10);
            objectSelects.addElement(DomainObject.SELECT_ID);
            objectSelects.addElement(DomainObject.SELECT_NAME);
            objectSelects.addElement(DomainObject.SELECT_REVISION);
            objectSelects.addElement(DomainObject.SELECT_TYPE);
            objectSelects.addElement("next.id");
            String strAffectedItemECOId = "next.to["+RELATIONSHIP_AFFECTED_ITEM+"].from.id";
            objectSelects.addElement("policy.revision");
            objectSelects.addElement("current.access[revise]");

            StringList relSelects = new StringList(2);
            relSelects.addElement(DomainRelationship.SELECT_TYPE);
            relSelects.addElement(DomainRelationship.SELECT_ID);
            objList = getRelatedObjects(context,
                                        ecrRelPattern,
                                        "*",
                                        objectSelects,
                                        relSelects,
                                        false,
                                        true,
                                        (short)1,
                                        null,
                                        null);

            // Check whether the affected items has revision sequence and
            // the user has revise access
            // on the object, then connect the next revision available
            Iterator objItr      = objList.iterator();
            DomainObject affItem = new DomainObject();
            CommonDocument docItem = new CommonDocument();

            String sBusId         = null;
            String sNextRevId     = null;
            String sName          = null;
            String sRev           = null;
            String relType        = null;
            String relId          = null;
            String ecoId          = null;
            HashMap ecoRelMap     = new HashMap(3);

			ecoRelMap.put(RELATIONSHIP_AFFECTED_ITEM, RELATIONSHIP_AFFECTED_ITEM);

            String sCurrentUser = context.getUser();

            // as there will be access right problem when connecting
            // the revised object to the ECO, connecting the ECO as
            //superuser

            // getting the affected item of the ECR
            while (objItr.hasNext())
            {
                objMap  = (Map)objItr.next();
                sBusId  = (String)objMap.get(DomainObject.SELECT_ID);
                sName   = (String)objMap.get(DomainObject.SELECT_NAME);
                sRev    = (String)objMap.get(DomainObject.SELECT_REVISION);

                // If the ECR is already promoted from "Review" state to "Plan ECO" state,
                // back again if the user has demoted and tries to go from "Review" to
                // "Plan ECO" then alert the user and exit.

                // get the object id of the next revise object
                // Note:- getNextRevision method should not be used instead of mqlCommand

                sNextRevId = (String)objMap.get("next.id");

                //performance improvement to not call IsRevisableForECO which makes another
                //db call for each affected item, instead get the info on the select above
                boolean isRevisable = false;
                boolean hasSequence = (((String)objMap.get("policy.revision")).length()>0)?true:false;
                boolean hasReviseAccess = "TRUE".equalsIgnoreCase((String)objMap.get("current.access[revise]"));

                if ( hasSequence && hasReviseAccess)
                {
                    if (sNextRevId != null && !(sNextRevId.trim()).equals(""))
                    {
                                    ecoId  = (String)objMap.get(strAffectedItemECOId);
                                    if (ecoId == null || ecoId.length() == 0)
                                    {
                                    isRevisable = true;
                                }
                    }
                    else
                    {
                        isRevisable = true;
                    }
                }
                //fix for bug 304465
                relType = (String)objMap.get(DomainRelationship.SELECT_TYPE);
                relId   = (String)objMap.get(DomainRelationship.SELECT_ID);
                Map relAttr = DomainRelationship.getAttributeMap(context, relId);
                boolean isForObsolescence = false;
                String requestedChangeValue = (String)relAttr.get(ATTRIBUTE_REQUESTED_CHANGE);
                if (requestedChangeValue != null && RANGE_FOR_OBSOLETE.equals(requestedChangeValue))
                {
                    isForObsolescence = true;
                }
                //new ECR with old ECO
                if (isForObsolescence)
                   {
                       affItem.setId(sBusId); // set the object id
                       // set the original user as owner and Originator of the revised objects.
                       affItem.setOwner(context, sCurrentUser);
                       affItem.setAttributeValue(context, DomainObject.ATTRIBUTE_ORIGINATOR, sCurrentUser);
                       DomainRelationship newRel = ECO.connectTo( context, (String)ecoRelMap.get(relType), affItem);
                       newRel.setAttributeValues(context, relAttr);
                   }
				else if("DynamicApproval".equals(ECOpolicyClassification))
				{
                        affItem.setId(sBusId); // set the object id
                        StringList Connectedids=affItem.getInfoList(context,"to["+RELATIONSHIP_AFFECTED_ITEM+"].from.id");
                        if((Connectedids!=null && Connectedids.contains(strECOID))){
                            //Do Nothing For Now   // Part and ECO are already connected with "Affected Item" relationship no need to connect again
                        }else{
                            DomainRelationship newRel = ECO.connectTo( context, (String)ecoRelMap.get(relType), affItem);
                            newRel.setAttributeValues(context, relAttr);
                        }
				}
				else
				{
                if (isRevisable && !isForObsolescence)
                {
                   //end of fix 304465
                   try
                   {
                       // set next revise if available, if next revise not
                       // available create next revision
                       if (sNextRevId != null && !(sNextRevId.trim()).equals(""))
                        {
                            affItem.setId(sNextRevId); // set the revised object id
                        }
                        else
                        {
                            affItem.setId(sBusId); // set the current object id
                            //if this is a DOCUMENT type then we need to revise using CDM model with files.
                                //Code added for the bug 307555
                                //CommonDocument docItem = new CommonDocument();
                            String newRevId = "";
                            //added for the bug 319023
                            //To perform Non-CDM revisioning for Integration objects.
                            String sIntegrationTypes = (String)FrameworkProperties.getProperty(context, "emxEngineeringCentral.IntegrationTypes");
                            StringTokenizer tokenizer=new StringTokenizer(sIntegrationTypes,",");
                            boolean isIEFType=false;
                            while(tokenizer.hasMoreTokens())
                            {

                                String nextFilter = tokenizer.nextToken();
                                String sObjType = PropertyUtil.getSchemaProperty(context, nextFilter);
                                if (affItem.isKindOf(context, sObjType))
                                {
                                  isIEFType=true;
                                  break;
                                }
                            }
                            //if (affItem.isKindOf(context, TYPE_DOCUMENTS))
                            if (affItem.isKindOf(context, TYPE_DOCUMENTS)&& !isIEFType)
                            //till here
                            {
                                docItem.setId(sBusId);
                                CommonDocument newRev = docItem.revise(context, true);
                                newRevId = newRev.getObjectId(context);
                            }
                            else
                            {
                                objMap = (Map) affItem.createRevision(context); // revising the object
                                newRevId = (String)objMap.get(DomainObject.SELECT_ID);
                            }
                            affItem.setId(newRevId); // set the new revise object id

                           //End of the code added for the bug 307555

                            // set the original user as owner and Originator of the revised objects.
                            affItem.setOwner(context, sCurrentUser);
                            affItem.setAttributeValue(context, DomainObject.ATTRIBUTE_ORIGINATOR, sCurrentUser);
                        }

                        // connecting the revised object with ECO
                        DomainRelationship newRel = ECO.connectTo( context, (String)ecoRelMap.get(relType), affItem);
                        newRel.setAttributeValues(context, relAttr);
                   }
                    catch (Exception ex)
                    {
                        if (sWarning == null)
                        {
                            sWarning = " " + sName + "," + sRev;
                        }
                        else
                        {
                            sWarning += "; " + sName + " " + sRev;
                        }
                    }
                }//fix for bug 304465
                //else if(!isRevisable && !(relType.equalsIgnoreCase(sRelName)))
                else if(!isRevisable)
                {
                    //end of fix or bug 304465
                    // getting the non-revisable affected item name
                    if (sWarning == null)
                    {
                        sWarning = " " + sName + "," + sRev;
                    }
                    else
                    {
                        sWarning += "; " + sName + " " + sRev;
                    }
                }
            }
            }

            if (sWarning != null)
            {
                // display warning to the user on the non-revisable affected items
                emxContextUtil_mxJPO.mqlWarning(context,
                    EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.AutoRevECRAffectedItemsWarning",
                    context.getSession().getLanguage()) + sWarning);
            }
        }
        catch ( Exception ex)
        {
            throw (ex);
        }
    }

    /**
     * Perform the promote action on an ECR policy ECR Standard.
     * Checks the following.
     *   - Checks the if any ECOs are connected to the ECR.
     *   - If not, cause post processing of promote to happen at
     *     JSP level by using MQL notice.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return void.
     * @throws Exception if the operation fails.
     * @since EC 10.0.0.0.
     * @trigger PolicyECRStandardStatePartStateReviewPromoteAction.
     */
    public void autoAssociateECO(Context context, String[] args)
        throws Exception
    {
        StringList objectSelects = new StringList(1);
        objectSelects.addElement(DomainObject.SELECT_ID);

        MapList objList = getRelatedObjects( context,
                                             RELATIONSHIP_ECO_CHANGEREQUESTINPUT,
                                             TYPE_ECO,
                                             objectSelects,
                                             null,
                                             true,
                                             false,
                                             (short)1,
                                             null,
                                             null);

        // Don't cause post processing if there is already an ECO
        // associated with this ECR
        //
        if (objList.size() <= 0)
        {
            String commandName = args[0];
            String objectId = args[1];

            StringBuffer processStr = new StringBuffer();

            processStr.append("JSP:postProcess");
            processStr.append('|');
            processStr.append("commandName=");
            processStr.append(PropertyUtil.getSchemaProperty(context,commandName));
            processStr.append('|');
            processStr.append("objectId=");
            processStr.append(objectId);

            emxContextUtil_mxJPO.mqlNotice(context, processStr.toString());
        }
    }

   /**
     * Provides the style sheet information.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return String Style Sheet information.
     * @throws Exception if the operation fails.
     * @since 10.5.
    */

    public  String getStyleInfo(Context context,String args[]) throws Exception{

        StringBuffer htmlString = new StringBuffer(8192);
        htmlString.append("<html>\n<title>PDF - (Summary Report)</title>");
        //hard coded charse en.
        String Charset = FrameworkProperties.getProperty(context, "emxEngineeringCentral.Charset.en");
        if(Charset != null)
        {
            htmlString.append("<META HTTP-EQUIV=\"Content-type\" CONTENT=\"text/html;charset=");
            htmlString.append(Charset);
            htmlString.append("\">\n");
        }

        htmlString.append("<style type=\"text/css\" >");
        /* Background Appearance */
        htmlString.append("body { ");
        htmlString.append(" background-color: white; ");
        htmlString.append('}');

        /* Font Appearance */
        htmlString.append("body, th, td, p, div, layer { ");
        htmlString.append(" font-family: verdana, helvetica, arial, sans-serif; ");
        htmlString.append("font-size: 8pt; ");
        htmlString.append('}');

        /* Link Appearance */
        htmlString.append("a { ");
        htmlString.append(" color: #003366; ");
        htmlString.append('}');

        htmlString.append("a:hover { }");

        /* Object Link Appearance */
        htmlString.append("a.object{ ");
          htmlString.append("font-weight: bold; ");
        htmlString.append('}');

        htmlString.append("a.object:hover { }");

        /* Object Text (Non-link) Appearance */
        htmlString.append("span.object {  ");
        htmlString.append(" font-weight: bold; ");
        htmlString.append('}');

        /* Button Link Appearance */
        htmlString.append("a.button { }");
        htmlString.append("a.button:hover { }");

        /* Content-Specific Function Appearance */
        htmlString.append("a.contextual { }");
        htmlString.append("a.contextual:hover { }");

        /* Remove Button Appearance */
        htmlString.append("a.remove { }");
        htmlString.append("a.remove:hover { }");

        /* --------------------------------------------------------------------
        // Page Header Settings
        // -------------------------------------------------------------------- */

        /* Page Header Text Appearance */
        htmlString.append(".pageHeader {  ");
        htmlString.append(" color:#990000; ");
        htmlString.append(" font-family: Arial, Helvetica, Sans-Serif; ");
        htmlString.append(" font-weight: bold; ");
        htmlString.append(" font-size: 12pt; ");
        htmlString.append(" letter-spacing: 0pt; ");
        htmlString.append(" line-height: 22px; ");
        htmlString.append(" text-decoration: none;");
        htmlString.append('}');

        /* Page Subtitle Appearance */
        htmlString.append(".pageSubTitle {");
        htmlString.append(" color:#990000; ");
        htmlString.append(" font-family: Arial, Helvetica, Sans-Serif; ");
        htmlString.append(" font-size: 11px; ");
        htmlString.append("letter-spacing: 1pt;");
        htmlString.append(" text-decoration: none;");
        htmlString.append('}');

        /* Page Header Border Appearance */
        htmlString.append("td.pageBorder {  ");
        htmlString.append(" background-color: #003366; ");
        htmlString.append('}');

        /* --------------------------------------------------------------------
        // Page Subheader Settings
        // -------------------------------------------------------------------- */

        /* Page Header Text Appearance */
        htmlString.append("td.pageSubheader {  ");
        htmlString.append(" color: #990000; ");
        htmlString.append(" font-family: Arial, Helvetica, Sans-Serif; ");
        htmlString.append(" font-size: 13pt; ");
        htmlString.append(" font-weight: bold; ");
        htmlString.append('}');

        /* --------------------------------------------------------------------
        // Miscellaneous Settings
        // -------------------------------------------------------------------- */

        /* Welcome message for loading page */
        htmlString.append("td.welcome { ");
        htmlString.append(" color: #000000; ");
        htmlString.append(" font-family: Arial, Helvetica, sans-serif; ");
        htmlString.append(" font-size: 14px; ");
        htmlString.append(" font-weight: bold; ");
        htmlString.append('}');

        /* Small Space Appearance - for non-breaking space workaround at end of files */
        htmlString.append("td.smallSpace { ");
        htmlString.append(" font-family: verdana,arial, helvetica,sans-serif; ");
        htmlString.append(" font-size: 4pt; ");
        htmlString.append('}');
        htmlString.append("td.blackrule {  ");
        htmlString.append(" background-color: #000000;");
        htmlString.append('}');

        /* Filter/Pagination Control Appearance */
        htmlString.append("td.filter, select.filter, td.pagination, select.pagination { ");
        htmlString.append(" font-family: Verdana, Arial, Helvetica, sans-serif; ");
        htmlString.append(" font-size: 11px ");
        htmlString.append('}');

        /* Pagination Control Background Appearance */
        htmlString.append("table.pagination { ");
        htmlString.append(" background-color: #eeeeee; ");
        htmlString.append('}');

        /* History Subheader */
        htmlString.append("td.historySubheader { ");
        htmlString.append(" font-weight: bold; ");
        htmlString.append('}');


        /* Default Label Appearance */
        htmlString.append("td.label { background-color: #dddecb; color: black; font-weight: bold; height: 24px; }");

        /* Display Field Appearance */
        htmlString.append("td.field { background-color: #eeeeee; }");

        /* --------------------------------------------------------------------
        // Headings
        // -------------------------------------------------------------------- */

        /* Heading Level 1 */
        htmlString.append("td.heading1 { font-size: 10pt; font-weight: bold; border-top: 1px solid #003366;  height: 24px;}");

        /* Heading Level 2 */
        htmlString.append("td.heading2 { font-size: 8pt; font-weight: bold; background-color: #dddddd;  height: 24px;}");



        /* Table Header Appearance */
        htmlString.append("th { ");
        htmlString.append(" background-color: #336699; ");
        htmlString.append(" color: white; ");
        htmlString.append(" text-align: left; ");
        htmlString.append('}');

        /* Table Header Link Appearance */
        htmlString.append("th a { ");
        htmlString.append(" text-align: left; ");
        htmlString.append(" color: white; ");
        htmlString.append(" text-decoration: none;  ");
        htmlString.append('}');
        htmlString.append("th a:hover { ");
        htmlString.append(" text-decoration: underline; ");
        htmlString.append(" color: #ccffff; ");
        htmlString.append('}');

        /* Table Header Column Group Header */
        htmlString.append("th.groupheader { ");
        htmlString.append(" background-color: white; ");
        htmlString.append(" color: #1E4365; ");
        htmlString.append(" font-size: 12px; ");
        htmlString.append(" font-weight: bold;");
        htmlString.append(" text-align: left; ");
        htmlString.append('}');

        /* Table Header Column Group Header Rule */
        htmlString.append("th.rule { ");
        htmlString.append(" background-color: #1E4365;");
        htmlString.append('}');

        /* --------------------------------------------------------------------
        // Main Table Settings
        // -------------------------------------------------------------------- */

        /* Sorted Table Header Appearance */
        htmlString.append("th.sorted { ");
        htmlString.append(" background-color: #336699; ");
        htmlString.append('}');

        /* Sub Table Header Appearance */
        htmlString.append("th.sub { ");
        htmlString.append(" text-align: left; ");
        htmlString.append(" color: white; ");
        htmlString.append(" background-color: #999999; ");
        htmlString.append('}');

        /* Sorted Sub Table Header Appearance */
        htmlString.append("th.subSorted { ");
        htmlString.append(" background-color: #999999; ");
        htmlString.append('}');


        /* Odd Table Row Appearance */
        htmlString.append("tr.odd { ");
        htmlString.append(" background-color: #ffffff;");
        htmlString.append('}');

        /* Even Table Row Appearance */
        htmlString.append("tr.even { ");
        htmlString.append(" background-color: #eeeeee;");
        htmlString.append('}');

        /* Table Header Column Group Header Rule */
        htmlString.append("tr.rule { ");
        htmlString.append(" background-color: #1E4365;");
        htmlString.append('}');

        /* Separator Appearance */
        htmlString.append("td.separator { ");
        htmlString.append(" background-color: #DDDECB");;
        htmlString.append('}');

        /* Separator Appearance */
        htmlString.append("td.whiteseparator { ");
        htmlString.append(" background-color: white; ");
        htmlString.append("} ");

        /* --------------------------------------------------------------------
        // Pagination Control Settings
        // -------------------------------------------------------------------- */

        /* Pagination Control Appearance*/
        htmlString.append("select.pagination, option.pagination {  ");
        htmlString.append(" font-family: Verdana, Arial, Helvetica, sans-serif; ");
        htmlString.append(" font-size: 10px;");
        htmlString.append('}');

        /* --------------------------------------------------------------------
        // Filter Control Settings
        // -------------------------------------------------------------------- */

        /* Filter Appearance*/
        htmlString.append("td.filter, select.filter, ");
        htmlString.append("option.filter {  ");
        htmlString.append(" font-family: Verdana, Arial, Helvetica, sans-serif; ");
        htmlString.append(" font-size: 11px;");
        htmlString.append('}');


        /* ====================================================================
        // Spec View Stylesheet
        // Platform: Windows
        // by Don Maurer
        // ==================================================================== */

        /* --------------------------------------------------------------------
        // Default Settings
        // -------------------------------------------------------------------- */

        /* Background Appearance */
        htmlString.append("body { background-color: white; }");

        /* Font Appearance */
        htmlString.append("body, th, td, p { font-family: verdana, helvetica, arial, sans-serif; font-size: 8pt; }");

        /* Object Text (Non-link) Appearance */
        htmlString.append("span.object {  font-weight: bold; }");

        /* --------------------------------------------------------------------
        // Page Header Settings
        // -------------------------------------------------------------------- */

        /* Page Header Text Appearance */


        htmlString.append("td.pageHeader {  font-family: Arial, Helvetica, Sans-Serif; font-size: 13pt; font-weight: bold; color: #990000; }");
        htmlString.append("td.pageHeaderSubtext {  font-family: Arial, Helvetica, Sans-Serif; font-size: 8pt; color: #990000; }");

        /* --------------------------------------------------------------------
        // Page Subheader Settings
        // -------------------------------------------------------------------- */


        /* Table Title Text Appearance */
        htmlString.append("td.tableTitleMajor {  font-family: Arial, Verdana, Helvetica, Sans-Serif; font-size: 12pt; font-weight: bold; color: #990000; }");

        /* Table Description Text Appearance */
        htmlString.append("td.descriptionText {  font-family: Verdana, Helvetica, Sans-Serif; font-size: 8pt; color: #000000; border-top: 1px solid black; border-bottom: 1px solid black;}");

        /* Table Header Appearance */
        htmlString.append("th { color:#000000;text-align: left; border-bottom: 1px solid black; border-top: 1px solid black; background: #dddddd;} ");

        /* Odd Table Row Appearance */
        htmlString.append("td.listCell { border-bottom: 1px solid black; }");

        /* Horizontal Rule Appearance */
        htmlString.append("hr { color: #000000; }");

        //kf
        htmlString.append("td.heading1 { border-top: 0px; font-family: Arial, Helvetica, Sans-Serif; font-size: 13pt; font-weight: bold; color: #990000; }");
        htmlString.append("heading1 { border-top: 0px; font-family: Arial, Helvetica, Sans-Serif; font-size: 13pt; font-weight: bold; color: #990000; }");

        htmlString.append("td.label {background: #ffffff}");
        htmlString.append("td.field {background: #ffffff}");

        /* Link Appearance */
        htmlString.append("a {color: #000000;text-decoration:none}");
        htmlString.append("a:hover {color: #000000;text-decoration:none }");

        /* Table Header Link Appearance */
        htmlString.append("th a { ");
        htmlString.append("  text-align: left; ");
        htmlString.append("  color: #000000; ");
        htmlString.append("  text-decoration: none;  ");
        htmlString.append('}');
        htmlString.append("th a:hover { ");
        htmlString.append("  text-decoration: underline; ");
        htmlString.append("  color: #000000; ");
        htmlString.append('}');

        htmlString.append("td.state { border-top: 0px; font-family: Arial, Helvetica, Sans-Serif; font-size: 10pt; font-weight: bold; font-style:italic;color: #000000;background: #ffffff }");


        htmlString.append("</style>");
        htmlString.append("<body>");

        String finalStr = htmlString.toString();
        return finalStr;
    }


/**
     * Get HTML table information for the ECR.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @return String Attributes in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since 10.5.
    */
    public  String getBasicInfo(Context context,String args[]) throws Exception {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }
        String strLanguage        =  context.getSession().getLanguage();
        String objectId = args[0];
        StringBuffer returnString = new StringBuffer(2048);

        setId(objectId);
        String srelPattern =  RELATIONSHIP_ECR_MAIN_PRODUCT_AFFECTED;
        String relattr = "from["+srelPattern+"].businessobject.name";
        String relattrId= "from["+srelPattern+"].businessobject.id";
        String vaultName = null;

        try
        {
//            person = com.matrixone.apps.common.Person.getPerson(context);
        }
        catch (Exception e)
        {
        //fix for bug 309382
            // IR-013341
            vaultName = PersonUtil.getDefaultVault(context);
        //end of fix
        }
        BusinessType ecrBusType = new BusinessType(TYPE_ECR, new Vault(vaultName));
        String defaultVal = "Unassigned";
        String Unassigned = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Unassigned",strLanguage);

        String attrName  = null;
        StringList objectSelects = new StringList();
        objectSelects.add(SELECT_NAME);
        objectSelects.add(SELECT_TYPE);
        objectSelects.add(SELECT_REVISION);
        objectSelects.add(SELECT_CURRENT);
        objectSelects.add(SELECT_OWNER);
        objectSelects.add(SELECT_ORIGINATED);
        objectSelects.add(SELECT_ORIGINATOR);
        objectSelects.add(SELECT_DESCRIPTION);
        objectSelects.add(SELECT_MODIFIED);
        objectSelects.add(SELECT_VAULT);
        objectSelects.add(SELECT_POLICY);
        objectSelects.add("to["+RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.name");

        AttributeTypeList ecrAttrList = ecrBusType.getAttributeTypes(context);
        Iterator ecrAttrListItr = ecrAttrList.iterator();

         while (ecrAttrListItr.hasNext()){
                 attrName = ((AttributeType) ecrAttrListItr.next()).getName();
                 objectSelects.add("attribute["+attrName+"]");
          }


        objectSelects.add(SELECT_POLICY);
        objectSelects.addElement(relattr);
        objectSelects.addElement(relattrId);

        Map attributeMap = getInfo(context, objectSelects);
        String productlineicon =  "<img border=\"0\" src=\"../common/images/iconSmallProductLine.gif\" alt=\"ProductLine\">";
        String ProductLineId        = (String)attributeMap.get(relattrId);
        String ProductLineName      = (String)attributeMap.get(relattr);
        String URLtoEditProductLine = "../common/emxTree.jsp?AppendParameters=true&objectId=" + ProductLineId;
        String ProductlineHref = "<a href=\"javascript:emxShowModalDialog(\'"+URLtoEditProductLine+"\',700,600,false)\">";
        if( ProductLineName == null || "null".equals(ProductLineName) )
        {
          ProductLineName ="";
        }else {
         ProductLineName =  productlineicon + ProductLineName ;
        }
       if(args.length >1){
        String sPrinterFriendly = args[1];

        if (sPrinterFriendly != null && "false".equalsIgnoreCase(sPrinterFriendly))
        {
            ProductLineName =  ProductlineHref + ProductLineName +"</a>";
        }
       }

        String attrValue = null;
        String actiondateoriginated = getFormatDate((String)attributeMap.get(SELECT_ORIGINATED));
        String actiondateModified = getFormatDate((String)attributeMap.get(SELECT_MODIFIED));
        String ecrDesc = (String)attributeMap.get(SELECT_DESCRIPTION);
        ecrDesc = FrameworkUtil.findAndReplace(ecrDesc,"\n","<br>");
        /* below html table contains two columns (tds). First contains basics and second contains ECR related and other attributes */
        returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"2\" >");
        returnString.append("<tr>");
        returnString.append("<td><table>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</strong>:&nbsp;</td><td>"+attributeMap.get(SELECT_NAME)+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</strong>:&nbsp;</td><td>"+attributeMap.get(SELECT_TYPE)+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ProductLine",strLanguage)+"</strong>:&nbsp;</td><td>"+ProductLineName+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+"</strong>:&nbsp;</td><td>"+i18nNow.getStateI18NString((String)attributeMap.get(SELECT_POLICY),(String)attributeMap.get(SELECT_CURRENT),strLanguage)+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</strong>:&nbsp;</td><td>"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_OWNER)))+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originator",strLanguage)+"</strong>:&nbsp;</td><td>"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_ORIGINATOR)))+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originated",strLanguage)+"</strong>:&nbsp;</td><td>"+actiondateoriginated+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Modified",strLanguage)+"</strong>:&nbsp;</td><td>"+actiondateModified+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</strong>:&nbsp;</td><td>"+ecrDesc+"</td></tr>");
        returnString.append("</table></td>");
        returnString.append("<td><table>");

        //Displaying the ECR specific attributes
        ecrAttrListItr = ecrAttrList.iterator();
        ecrBusType.close(context);
        while (ecrAttrListItr.hasNext())
        {
          attrName = ((AttributeType) ecrAttrListItr.next()).getName();
          attrValue = (String)attributeMap.get("attribute[" + attrName + "]");
          if(attrName.equals(ECR.ATTRIBUTE_REASON_FOR_CHANGE)
                  ||attrName.equals(ECR.ATTRIBUTE_GENERAL_DESCRIPTION_OF_CHANGE)
                  ||attrName.equals(ECO.ATTRIBUTE_REASON_FOR_CANCEL)
                  ||attrName.equals(PropertyUtil.getSchemaProperty(context,"attribute_ReviewersComments")
                  ))
          {
              attrValue = FrameworkUtil.findAndReplace(attrValue,"\n","<br>");
          }
          if((ECO.ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER).equals(attrName)||
              (ECR.ATTRIBUTE_ECR_EVALUATOR).equals(attrName)||
              (ECR.ATTRIBUTE_CATEGORY_OF_CHANGE).equals(attrName)) {
             attrValue = (attrValue.equals(defaultVal))?Unassigned:attrValue;
          }

          if(!attrName.equals(ATTRIBUTE_ORIGINATOR) )
          {
             returnString.append("<tr>");
             returnString.append("<td><strong>"+i18nNow.getAttributeI18NString(attrName, strLanguage)+"</strong>&nbsp;</td>");
             if(!attrValue.equals(Unassigned) && ((ECO.ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER).equals(attrName)||
              (ECR.ATTRIBUTE_ECR_EVALUATOR).equals(attrName))) {
                returnString.append("<td>"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,attrValue)+"&nbsp;</td>");
             }else{
                returnString.append("<td>"+i18nNow.getRangeI18NString(attrName, attrValue, strLanguage)+"&nbsp;</td>");
             }
             returnString.append("</tr>");
          }
        }

        String sDesignResponsibility = (String)attributeMap.get("to["+RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.name");
        if(sDesignResponsibility == null || "null".equals(sDesignResponsibility)){
            sDesignResponsibility = "";
        }
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.DesignResponsibility",strLanguage)+"</strong>:&nbsp;</td><td>"+sDesignResponsibility+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Vault",strLanguage)+"</strong>:&nbsp;</td><td>"+attributeMap.get(SELECT_VAULT)+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Policy",strLanguage)+"</strong>:&nbsp;</td><td>"+attributeMap.get(SELECT_POLICY)+"</td></tr>");
        returnString.append("</table></td>");
        returnString.append("</tr>");
        returnString.append("</table>");
        String finalStr = returnString.toString();
        return finalStr;
    }

   /**
     * Constructs the HTML table of the Approvals related to this ECR.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @return String related approvals in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since 10.5.
    */

   public  String getApprovals(Context context,String args[]) throws Exception{

    if (args == null || args.length < 1) {
          throw (new IllegalArgumentException());
    }

    Route routeObj = (Route)DomainObject.newInstance(context,TYPE_ROUTE);
    String strLanguage = context.getSession().getLanguage();
    String objectId = args[0];

    String sLifeCycle         = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Route.LifeCycle",strLanguage);
    boolean bRouteSize         =false;
    boolean bSign              =false;

    setId(objectId);
    String sPolicy            = getInfo(context, SELECT_POLICY);
    MapList memberList        = new MapList();
    // get a MapList of all the approval data including routes
    MapList stateRouteList = getApprovalsInfo(context);

    SelectList objSelects  = new SelectList();
    objSelects.addElement(SELECT_NAME);
    SelectList relSelects  = new SelectList();
    relSelects.addElement(Route.SELECT_COMMENTS);
    relSelects.addElement(Route.SELECT_APPROVAL_STATUS);
    relSelects.addElement(Route.SELECT_APPROVERS_RESPONSIBILITY);
    relSelects.addElement(Route.SELECT_ROUTE_TASK_USER);

    StringBuffer returnString=new StringBuffer(2048);
    returnString.append(" <table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
    returnString.append("<tr>  <th class=\"heading1\" nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage));
    returnString.append("</th> <th class=\"heading1\"  nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Route",strLanguage));
    returnString.append("</th>  <th class=\"heading1\" nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Signature",strLanguage));
    returnString.append("</th>  <th class=\"heading1\" nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Signer",strLanguage));
    returnString.append("</th> <th class=\"heading1\" nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Status",strLanguage));
    returnString.append("</th> <th class=\"heading1\" nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage));
    returnString.append("</th> </tr>");

  Iterator mapItr = stateRouteList.iterator();
    while(mapItr.hasNext())
    {
    Map stateRouteMap = (Map)mapItr.next();
      StringItr sSignatureItr = null;
      StringItr sSignersItr   = null;
      StringItr sCommentItr   = null;
      StringItr sStatusItr    = null;
      boolean hasSigs = false;

      // Check for State Name and Ad Hoc routes
      String sStateName = (String)stateRouteMap.get(SELECT_NAME);

    if (sStateName != null) {
        sSignatureItr = new StringItr((StringList)stateRouteMap.get(KEY_SIGNATURE));
        sSignersItr   = new StringItr((StringList)stateRouteMap.get(KEY_SIGNER));
        sCommentItr   = new StringItr((StringList)stateRouteMap.get(KEY_COMMENTS));
        sStatusItr    = new StringItr((StringList)stateRouteMap.get(KEY_STATUS));
        hasSigs = sSignatureItr.next();
        sSignatureItr.reset();
      }

    if ("Ad Hoc Routes".equals(sStateName)) {
        hasSigs = false;
        sStateName = "Ad Hoc";
      }

      // Check for Routes
      Vector routes = (Vector)stateRouteMap.get(KEY_ROUTES);
      if (hasSigs) {
      bSign=true;

        returnString.append("<tr >");
        returnString.append("<td class=\"listCell\" style=\"text-align: \"  valign=\"top\">"+i18nNow.getStateI18NString(sPolicy,sStateName.trim(),strLanguage)+"&nbsp;</td> <td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sLifeCycle+"&nbsp;</td>");

      }

    String sSignStatus;
        boolean isFirst = true;
        if (sSignatureItr != null) {
          while (sSignatureItr.next()) {

             if (!isFirst) {
                  returnString.append("<tr > <td class=\"listCell\" style=\"text-align: \" >&nbsp;</td> <td class=\"listCell\" style=\"text-align: \" > &nbsp;</td>");
             }
             isFirst = false;
             // get next data for each list
             sSignersItr.next();
             sStatusItr.next();
             sCommentItr.next();

             String sSignName = sSignatureItr.obj();
             String sSigner   = sSignersItr.obj();
             String sSignDesc = sCommentItr.obj();
             String status    = sStatusItr.obj();
             // Internationalize status
             if (status.equalsIgnoreCase("Approved")){
               sSignStatus = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Approved", strLanguage);
             }
             else if (status.equalsIgnoreCase("Ignore")){
               sSignStatus = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Ignored", strLanguage);
             }
             else if (status.equalsIgnoreCase("Rejected")){
               sSignStatus = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rejected", strLanguage);
             }
             else  if (status.equalsIgnoreCase("Signed")){
               sSignStatus = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Signed", strLanguage);
             }
             else{
               sSignStatus = "";
             }

             returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sSignName+"&nbsp;</td>");
             returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sSigner+"&nbsp;</td>");
             returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">");
             if((sSignStatus != null)&&(!sSignStatus.equalsIgnoreCase("null"))){
                returnString.append(sSignStatus);
             }
             returnString.append("&nbsp;</td> <td class=\"listCell\" style=\"text-align: \"   valign=\"top\">"+sSignDesc+"&nbsp;</td></tr>");
          }
        }

    for (int rteCnt = 0; rteCnt < routes.size(); rteCnt++) {
           bRouteSize=true;
           String sRouteId = (String)routes.get(rteCnt);
           returnString.append("<tr >");
           if ((rteCnt == 0) && (!hasSigs)) {
             returnString.append("<td valign=\"top\" class=\"listCell\" style=\"text-align: \" >"+sStateName+"&nbsp;</td>");
           }
           else {
             returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">&nbsp;</td>");
           }

       String sRouteName = "";
           String routeNodeResponsibility = "";
           String sPersonName             = "";
           String routeNodeStatus         = "";
           String routeNodeComments       = "";

           Hashtable memberMap = new Hashtable();
           if(sRouteId != null && !"null".equals(sRouteId) && !"".equals(sRouteId))
           {
             routeObj.setId(sRouteId);
             sRouteName = routeObj.getInfo(context, SELECT_NAME);
             memberList = routeObj.getRouteMembersForTemplateCreate(context, objSelects, relSelects, false);
           }

           returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sRouteName+"&nbsp;</td>");

       for(int k = 0; k < memberList.size() ; k++) {
              memberMap = (Hashtable) memberList.get(k);
              routeNodeResponsibility = (String) memberMap.get(Route.SELECT_APPROVERS_RESPONSIBILITY);
              sPersonName             = (String) memberMap.get(Route.SELECT_ROUTE_TASK_USER);
              routeNodeStatus         = (String) memberMap.get(Route.SELECT_APPROVAL_STATUS);
              routeNodeComments       = (String) memberMap.get(Route.SELECT_COMMENTS);

              if(sPersonName == null || "null".equals(sPersonName) || "".equals(sPersonName)){
                sPersonName             = (String) memberMap.get(DomainConstants.SELECT_NAME);
              }
              else
              {
                sPersonName = " ";//Framework.getPropertyValue(session, sPersonName);
              }

              if(k > 0)
              {
                returnString.append("<tr > <td class=\"listCell\" style=\"text-align: \" valign=\"top\">&nbsp</td><td class=\"listCell\" style=\"text-align: \" valign=\"top\">&nbsp</td>");  //last td replaced with style class
              }

              returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+routeNodeResponsibility+"&nbsp;</td>");
              returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sPersonName+"&nbsp;</td>");
              returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+i18nNow.getRangeI18NString("", routeNodeStatus,strLanguage)+"&nbsp;</td> <td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+routeNodeComments+"&nbsp;</td>     </tr>");
            }
        }
    }

    if (!bRouteSize && !bSign)
    {
       returnString.append("<tr><td class=\"even\" style=\"text-align: \" colspan=3 align=\"center\" > No Signatures or Routes in Approvals</td></tr>");
    }

    returnString.append("</table>");
  String finalStr = returnString.toString();
  return finalStr;
  }

   /**
    * Get the list of Routes.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds the following input arguments:
    * 0 - String containing the ECR id.
    * @return String containing route details in html format.
    * @throws Exception if the operation fails.
    * @since 10.5.
    */

    public String getRoutes(Context context, String[] args)
      throws Exception, MatrixException
   {
      try
      {
        String strLanguage = context.getSession().getLanguage();
        String objectId = args[0];
    Route routeObj = (Route)DomainObject.newInstance(context,TYPE_ROUTE);
    String routeStatusAttrSel      = "attribute["+ DomainConstants.ATTRIBUTE_ROUTE_STATUS +"]";
    SelectList selectStmts = new SelectList();
    selectStmts.addName();
    selectStmts.addDescription();
    selectStmts.addCurrentState();
    selectStmts.add(routeStatusAttrSel);
    selectStmts.addOwner();
    selectStmts.addId();
    selectStmts.addPolicy();
    selectStmts.addElement(Route.SELECT_ACTUAL_COMPLETION_DATE);
    selectStmts.addElement(Route.SELECT_SCHEDULED_COMPLETION_DATE);

    StringBuffer routeInfo = new StringBuffer(1024);
    routeInfo.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
    routeInfo.append("<tr><th width=\"5%\" style=\"text-align:center\"><img border=\"0\" src=\"../common/images/iconStatus.gif\" name=\"imgstatus\" id=\"imgstatus\" alt=\"*\"></th>");

    routeInfo.append("<th nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
    routeInfo.append("<th nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
    routeInfo.append("<th nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Status",strLanguage)+"</th>");
    routeInfo.append("<th nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Routes.ScheduleCompDate",strLanguage)+"</th>");
    routeInfo.append("<th nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</th>");

    MapList totalResultList = routeObj.getRoutes(context, objectId, selectStmts, null, null, false);
	//Added for Bug-310047 on 10/May/2006
	totalResultList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
	totalResultList.sort();

	Iterator itr = totalResultList.iterator();
    String routeId;
    String scheduledCompletionDate = "";
    boolean isYellow = false;
    String sCode = "";
    String routeIcon = "";
    Date curDate = new Date();
    String routeState = "";
    while(itr.hasNext()) {
      Map routeMap = (Map)itr.next();
      routeId = (String)routeMap.get(DomainConstants.SELECT_ID);
            routeState = (String)routeMap.get(DomainConstants.SELECT_CURRENT);
      routeObj.setId(routeId);
            scheduledCompletionDate = routeObj.getSheduledCompletionDate(context);
      if(scheduledCompletionDate != null && !"".equals(scheduledCompletionDate))
      {
        Date dueDate = new Date();
        dueDate = eMatrixDateFormat.getJavaDate(scheduledCompletionDate);
        if ( dueDate != null && ( curDate.after(dueDate)) && (!(routeState.equals("Complete")))) {
          sCode = "Red";
        }
      }

        isYellow = false;
        if (!"Red".equals(sCode)) {
        MapList taskList = routeObj.getRouteTasks(context, selectStmts, null, null, false);

        // check for the status of the task.
        Map taskMap = null;
        for(int j = 0; j < taskList.size(); j++) {
          taskMap = (Map) taskList.get(j);
          String sState         = (String) taskMap.get(DomainConstants.SELECT_CURRENT);
          String CompletionDate = (String) taskMap.get(Route.SELECT_SCHEDULED_COMPLETION_DATE);
          String actualCompletionDate = (String) taskMap.get(Route.SELECT_ACTUAL_COMPLETION_DATE);

          Date dueDate = new Date();
          if( CompletionDate!=null && !"".equals(CompletionDate)) {
          dueDate = eMatrixDateFormat.getJavaDate(CompletionDate);
          }

          if ("Complete".equals(sState)) {
          Date dActualCompletionDate = new Date(actualCompletionDate);
          if (dActualCompletionDate.after(dueDate)) {
            isYellow = true;
            break;
          }
          } else if (curDate.after(dueDate)) {
          isYellow = true;
          break;
          }
        }

        if(isYellow) {
          sCode = "yellow";
        } else {
          sCode = "green";
        }
            }

            if("Red".equals(sCode)) {
        routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusRed.gif\" name=\"red\" id=\"red\" alt=\"emxComponents.TaskSummary.ToolTipRed\">";
            } else if("green".equals(sCode)) {
                routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusGreen.gif\" name=\"green\" id=\"green\" alt=\"emxComponents.TaskSummary.ToolTipGreen\">";
            } else if("yellow".equals(sCode)) {
        routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusYellow.gif\" name=\"yellow\" id=\"yellow\" alt=\"emxComponents.TaskSummary.ToolTipYellow\">";
            } else {
                routeIcon = "&nbsp;";
      }

      routeInfo.append("<tr>");
      routeInfo.append("<td>"+routeIcon+"</td>");
      routeInfo.append("<td>"+routeMap.get(DomainObject.SELECT_NAME)+"&nbsp;</td>");
      routeInfo.append("<td>"+routeMap.get(DomainObject.SELECT_DESCRIPTION)+"&nbsp;</td>");
      routeInfo.append("<td>"+routeMap.get(routeStatusAttrSel)+"&nbsp;</td>");
      routeInfo.append("<td>"+scheduledCompletionDate+"&nbsp;</td>");
      routeInfo.append("<td>"+routeMap.get(DomainObject.SELECT_OWNER)+"&nbsp;</td>");
      routeInfo.append("</tr>");
    }

    if(totalResultList.size()==0) {
      routeInfo.append("<tr><td colspan=\"6\">No objects found</td></tr>");
    }
    routeInfo.append("</table>");
    return routeInfo.toString();
      }
      catch (Exception ex)
      {
        throw ex;
      }
    }

   /**
     * Constructs the ECR Supporting Documents HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @return String Connected Supporting documents in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since 10.5.
     */

public String getSupportingDocsDetails(Context context,String[] args)
             throws Exception
     {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }

        String strLanguage = context.getSession().getLanguage();
        String objectId = args[0];
        MapList SupportDocsList = new MapList();
        ECR ecrObj = null;

        try {
            ecrObj = new ECR(objectId);
            String srelPattern = DomainRelationship.RELATIONSHIP_ECR_SUPPORTING_DOCUMENT;

            SelectList selectStmts = ecrObj.getObjectSelectList(5);
            selectStmts.addElement(DomainObject.SELECT_ID);
            selectStmts.addElement(DomainObject.SELECT_NAME);
            selectStmts.addRevision();
            selectStmts.addCurrentState();
            selectStmts.addDescription();
            selectStmts.addType();
            selectStmts.addElement("policy");

            SelectList selectRelStmts = ecrObj.getRelationshipSelectList(2);
            selectRelStmts.addElement(DomainRelationship.SELECT_ID);
            selectRelStmts.addElement(DomainRelationship.SELECT_NAME);
            SupportDocsList = ecrObj.expandSelect(context,
                                                   srelPattern,
                                                   "*",
                                                   selectStmts,
                                                   selectRelStmts,
                                                   false,
                                                   true,
                                                   (short)1,
                                                   null,
                                                   null,
                                                   null,
                                                   false);
           //Added for Bug-310047 on 10/May/2006
             SupportDocsList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
             SupportDocsList.sort();
             } catch (FrameworkException Ex) {
             throw Ex;
        }
        //String queryString = "";
        StringBuffer returnString=new StringBuffer(1024);

         returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"2\" >");
         returnString.append("<tr>  <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage));
         returnString.append("</th> <th   nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage));
         returnString.append("</th> <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage));
         returnString.append("</th> <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Revision",strLanguage));
         returnString.append("</th> <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage));
         returnString.append("</th> <th nowrap>");
         returnString.append("</th> </tr>");

         Iterator mapItr = SupportDocsList.iterator();
         String imgSpecType = "";

         while(mapItr.hasNext())
         {
            Map specMap = (Map)mapItr.next();
            imgSpecType = EngineeringUtil.getTypeIconProperty(context, (String)specMap.get(DomainConstants.SELECT_TYPE));
            if (imgSpecType == null || imgSpecType.length() == 0 )
            {
                imgSpecType = "iconSmallDefault.gif";
            }
            returnString.append("<tr>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\"><img src=\"../common/images/"+imgSpecType+"\" border=\"0\" alt=\"*\">&nbsp;"+specMap.get(DomainConstants.SELECT_NAME)+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+specMap.get(DomainConstants.SELECT_TYPE)+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+specMap.get(DomainConstants.SELECT_DESCRIPTION)+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+specMap.get(DomainConstants.SELECT_REVISION)+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+specMap.get(DomainConstants.SELECT_CURRENT)+"&nbsp;</td></tr>");
         }
         if(SupportDocsList.size()==0) {
                returnString.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ReviewECR.NorelatedSupportingDocumentsarefoundforthis",strLanguage)+"</td></tr>");
         }
         returnString.append("</table>");
         String finalStr = returnString.toString();

         return finalStr;
     }

    /**
     *
     * Checks if the given resource id is set to true or false.
     * @param context the eMatrix <code>Context</code> object.
     * @param resourceID String holding resourceID.
     * @return Boolean set to the value of the resource.
     * @throws Exception if the operation fails.
     * @since EC 10.5.
     */
    private static Boolean emxCheckAccess(Context context, String resoureID)
            throws Exception
        {
            String resourceIDValue = FrameworkProperties.getProperty(context,resoureID);
            return Boolean.valueOf(resourceIDValue);
        }

  /**
   * Checks the Obsolete Parts section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether Obsolete Parts section is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */

   public static Boolean emxCheckObsoletePartsAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.ObsoleteParts");
   }

   /**
   * Checks the Revised Parts section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether Revised Parts section is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public static Boolean emxCheckRevisedPartsAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.RevisedParts");
   }


  /**
   * Checks the Revised Specs section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether Revised Specs section is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public static Boolean emxCheckRevisedSpecsAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.RevisedSpecs");
   }


  /**
   * Checks the Supporting Docs section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether Supporting Docs section is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public static Boolean emxCheckSupportingDocsAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.SupportingDocs");
   }

    /**
     * Checks if the current selected Policy uses Dynamic or Static Approvals.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds fieldMap to get policy name.
     * @return Boolean true if selected Policy uses Dynamic Approvals otherwise returns false.
     * @throws Exception if the operation fails.
     * @since BX3.
     */

     public static Boolean emxCheckCreateDynamicApprovalECR(Context context, String[] args)
          throws Exception
     {
         boolean dynamicApproval = false;
         // 374591
         String policyClassification = FrameworkUtil.getPolicyClassification(context, POLICY_ECR);
         if ("DynamicApproval".equals(policyClassification))
         {
             dynamicApproval = true;
         }
         return Boolean.valueOf(dynamicApproval);
     }

     /**
      * Creates the ECR summary report and generates the PDF file.
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds the following input arguments:
      * 0 - String containing the ECR id.
      * @return int tells whether PDF is generated and checked into the object. 0-success 1-failure.
      * @throws Exception if the operation fails.
      * @since 10.5.
      */

     public  int createSummaryReport(Context context,String args[]) throws Exception {
        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }

        String summaryReport="";
        try {
             summaryReport=createHtmlReport(context,args);

            }
            catch (Exception e) {
               throw e;
        }

        int pdfGenerated = renderPDFFile(context,args,summaryReport);
        if(pdfGenerated!=0)
        {
             emxContextUtil_mxJPO.mqlError(context,
                                              EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.SummaryReport.NoCheckIn.ErrorMessage",
                                              context.getSession().getLanguage()));
              return 1;
        }
        else
        {
            return 0;
        }

     }


     /**
      * Creates the ECR summary report.
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds the following input arguments:
      * 0 - String containing the ECR id.
      * @return int tells whether PDF is generated and checked into the object. 0-success 1-failure.
      * @throws Exception if the operation fails.
      * @since 10.5.
      */
 public  String createHtmlReport(Context context,String args[]) throws Exception {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }
        StringBuffer summaryReport = new StringBuffer(512);
        try {

         String strLanguage = context.getSession().getLanguage();
         String objectId = args[0];
         ECR ecrObj = null;
         ecrObj = new ECR(objectId);

         java.util.Calendar cal = new GregorianCalendar(TimeZone.getDefault());
         int month = cal.get(Calendar.MONTH);
         int dates = cal.get(Calendar.DATE);
         int year =  cal.get(Calendar.YEAR);
         int hour =  cal.get(Calendar.HOUR);
         int minute = cal.get(Calendar.MINUTE);
         int AM_PM = cal.get(Calendar.AM_PM);
         String[] monthDesc = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
         String[] AMPM = new String[]{"AM","PM"};
         String smonth = monthDesc[month];
         String sAMPM = AMPM[AM_PM];
         String dateAndTime =  smonth+" "+dates+","+year+","+hour+":"+minute+" "+sAMPM;



     //Summary Report Heading
     summaryReport.append("<html>");
     summaryReport.append("<div id=\"pageHeader\">");
     summaryReport.append("<table border=\"0\" width=\"100%\">");
     summaryReport.append("<tr><td class=\"pageHeader\"><h1>"+ecrObj.getInfo(context,SELECT_NAME)+" : "+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.SummaryReport",strLanguage)+"</h1></td></tr>");
     summaryReport.append("<tr><td class=\"pageSubtitle\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Generated",strLanguage)+" "+dateAndTime+"</td></tr>");
     summaryReport.append("</table>");
     summaryReport.append("</div>");

     // Basic Attributes section display
     summaryReport.append("<table width=\"100%\"> <tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Attributes",strLanguage)+"</h2></td></tr></table>");
     summaryReport.append(getBasicInfo(context,args));


     // Approvals Display
     Boolean boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.Approvals");

     if(boolObj.booleanValue()) {
      summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.Approvals",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getApprovals(context,args));
     }

     // Routes Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.Routes");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"> <tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Routes",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getRoutes(context,args));
     }

     //Tasks Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.Tasks");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"> <tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Tasks",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getECRTasks(context,args));
     }

     // AffectedItems(Parts) Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.AffectedItems");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECRAffectedItems",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getECRAffectedPartsDetails(context,args));
     }
     // AffectedItems(Specs) Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.AffectedItems");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECRAffectedSpecs",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getECRAffectedSpecsDetails(context,args));
     }
    // Related Assignes Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.Assignees");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.Assignees",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getAssigneesOfECR(context,args));
     }
     //Related Markups Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.RelatedMarkups");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.RelatedMarkups",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECRRelatedBOMMarups(context,args));
     }
     //Related ResolvedItems Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.ResolvedItems");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.RelatedResolvedItems",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECRRelatedResolvedItems(context,args));
     }
     // Supporting documents Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.SupportingDocs");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.SupportingDocs",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getSupportingDocsDetails(context,args));
     }

     // Related ECOs Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.RelatedECOs");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.RelatedECOs",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getRelatedECOsDetails(context,args));
     }

     //Close tags
     summaryReport.append("</html>");
     } catch (Exception e) {
      throw e;
     }

     return summaryReport.toString();

     }


   /**
     * Generates the ECR Summary PDF file and checks it into the ECR object.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @param summaryReport the String that needs to be rendered into PDF.
     * @return int 0- for success, 1- failure.
     * @throws Exception if the operation fails.
     * @since 10.5.
    */

    public int renderPDFFile(Context context, String []args, String summaryReport) throws Exception {


		String objectId = args[0];

		setId(objectId);
		String objType = getInfo(context, SELECT_TYPE);
		String objName = getInfo(context,SELECT_NAME);
		String objRev = getInfo(context,SELECT_REVISION);

		String languageCode = "en";

		RenderPDF renderPDF = new RenderPDF();

		renderPDF.loadProperties(context);

		String timeStamp = Long.toString(System.currentTimeMillis());
		String folderName = objectId + "_" + timeStamp;
		folderName = folderName.replace(':','_');

		if (renderPDF.renderSoftwareInstalled == null || "false".equalsIgnoreCase(renderPDF.renderSoftwareInstalled) )
		{
		  MqlUtil.mqlCommand(context, "notice $1","Render Software not Installed");
		  return 1;
           }

		String ftpInputFolder = renderPDF.inputFolder + java.io.File.separator + folderName;
		String ftpOutputFolder = renderPDF.outputFolder + java.io.File.separator + folderName;

		try
		{
			renderPDF.createPdfInputOpuputDirectories(context, folderName);
           }
		catch (Exception ex)
		{
		  MqlUtil.mqlCommand(context, "notice $1","Unable to connect to ftp server or no write access");
		  return 1;
           }

		String fileName = objName + "-Rev" + objRev + ".htm";
		String dpiFileName = objName + "-Rev" + objRev + ".dpi";
		String pdfFileName = objName + "-Rev" + objRev + ".pdf";

		mxFtp clientHtm = new mxFtp();
		String charset = FrameworkProperties.getProperty(context, "emxFramework.Charset." + languageCode);

		try
		{
			clientHtm.connect(renderPDF.strProtocol,renderPDF.strHostName,null,renderPDF.strUserName,renderPDF.strPassword, ftpInputFolder,true);
			clientHtm.create(fileName);
			Writer outHtm = new BufferedWriter(new OutputStreamWriter(new MyOutputStream(clientHtm),charset));
			outHtm.write(summaryReport);
			outHtm.flush();
			outHtm.close();
		}
		catch (Exception ex)
		{
			MqlUtil.mqlCommand(context, "notice $1","Unable to connect to ftp server");
			return 1;
		}
		finally
		{
			clientHtm.close();
			clientHtm.disconnect();
           }

		String watermark = FrameworkProperties.getProperty(context, "emxFramework.RenderPDF.WaterMark");
		String mark = watermark;
		if (watermark == null || "null".equals(watermark))
		{
			watermark="";
              }
		else if(watermark.length() > 0)
		{
			try
			{
				//Multitenant
				watermark = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale("en"),watermark);
              }
			catch(Exception e)
			{
				watermark = mark;
              }
			watermark = MessageUtil.substituteValues(context, watermark, objectId, languageCode);
           }

		StringList files = new StringList(1);

		renderPDF.writeDPI(context, ftpInputFolder, fileName, dpiFileName, files, watermark,charset);

		boolean renderProcess = renderPDF.generatedPDFExists(context, pdfFileName, ftpOutputFolder);

		if (renderProcess)
		{

				String strTempDir = context.createWorkspace();

			java.io.File outfile = new java.io.File(strTempDir + java.io.File.separator + pdfFileName);

			FileOutputStream fos = new FileOutputStream(outfile);

			mxFtp ftpPDF = new mxFtp();
			ftpPDF.connect(renderPDF.strProtocol,renderPDF.strHostName,null,renderPDF.strUserName,renderPDF.strPassword,ftpOutputFolder,true);
			ftpPDF.open(pdfFileName);
			InputStream inSupp = new com.matrixone.apps.domain.util.MyFtpInputStream(ftpPDF);
			//363896
            emxcommonPushPopShadowAgent_mxJPO PushPopShadowAgent = new emxcommonPushPopShadowAgent_mxJPO(context, null);


				try
				{

                com.matrixone.fcs.common.TransportUtil.transport(inSupp,fos, 8*1024);
				//363896
				/* Push Shadow Agent */
				PushPopShadowAgent.pushContext(context,null);
				String cmd = "checkin bus $1 $2 $3 format $4 $5";

				MqlUtil.mqlCommand(context, cmd, objType, objName, objRev, "generic",
									strTempDir + java.io.File.separator + pdfFileName);

                }
				catch (Exception ex)
				{
					MqlUtil.mqlCommand(context, "notice $1",ex.getMessage());
					return 1;
                }
				finally
				{
				inSupp.close();
					fos.close();
				ftpPDF.disconnect();
				ftpPDF.close();
				//363896
                PushPopShadowAgent.popContext(context,null);
          }

        }
		else
		{
			MqlUtil.mqlCommand(context, "notice $1","Unable to generate pdf on adlib server");
			return 1;
        }

		return 0;

    }


   /**
     * Changes the Date format.
     *
     * @param actiondate String which holds the date in the supplied format.
     * @return String Formatted date.
     * @since 10.5.
    */

    public String getFormatDate(String actiondate) {

      String[] monthDesc = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
      Date inputDateFormat = eMatrixDateFormat.getJavaDate(actiondate);
	  int clientmonth= inputDateFormat.getMonth();
	  String smonth = monthDesc[clientmonth] +" ";
      actiondate = actiondate.substring(actiondate.indexOf('/')+1,actiondate.trim().length());
      actiondate = actiondate.replace('/',',');
      actiondate = smonth+actiondate;
      return actiondate ;
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

    /**
     * Connects the product line to ECR.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following Strings, "objectId", "old value", "New OID".
     * @return Object - boolean true if the operation is successful
     * @throws Exception if operation fails
     * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
     */

    public Object updateProductLine(Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strECRId = (String) paramMap.get("objectId");
        String strOldProductLineName = (String) paramMap.get("Old value");
        String strNewProductLineId = (String) paramMap.get("New OID"); //"New Value");
        String strOldProductLineId = (String) paramMap.get("Old OID"); //"Old Value");
        String strRelationship = DomainRelationship.RELATIONSHIP_ECR_MAIN_PRODUCT_AFFECTED;

        setId(strECRId);
        if (strOldProductLineName == null || "null".equals(strOldProductLineName))
                strOldProductLineName = "";

	    if (!"".equals(strOldProductLineName) && !strOldProductLineId.equals(strNewProductLineId))
	    {
	        DomainObject dmObj = newInstance(context, strECRId);
	        String relID = (String)dmObj.getInfo(context,"from["+strRelationship+"].id");
	        DomainRelationship.disconnect(context,relID);
	    }
	    if (strNewProductLineId != null && strNewProductLineId.length()>0)
	    {
	        DomainRelationship.connect(context,strECRId,strRelationship,strNewProductLineId,false);
	    }

        return Boolean.TRUE;
    }


    /**
 * Changes the policy of an ECR.
 * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following Strings, "objectId", "New Value".
     * requestMap - a HashMap containing the request.
 * @return Object - Boolean true if operation successful otherwise false.
 * @throws Exception if operation fails
 * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */

public Object changePolicy(Context context, String[] args)
throws Exception
 {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strECRId = (String) paramMap.get("objectId");

        setId(strECRId);
        String strCurrentPolicy = getInfo(context,SELECT_POLICY);
        String strPolicy = (String) paramMap.get("New Value");

        //Cahnge policy if the current one is modified.
        if( !strCurrentPolicy.equals(strPolicy)) {
            setPolicy(context,strPolicy);
        }
   return Boolean.TRUE;
 }

/**
     * Gets the related ECOs connected to this ECR.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the request values.
     * @return MapList - list containing the related ecos.
     * @throws Exception if operation fails
     * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRelatedECOs(Context context, String[] args)
        throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strECRId = (String) programMap.get("objectId");
        String srelPattern = RELATIONSHIP_ECO_CHANGEREQUESTINPUT;

        ECR ecrObj = new ECR(strECRId);
        SelectList selectStmts = ecrObj.getObjectSelectList(4);
        selectStmts.addElement(DomainObject.SELECT_ID);
        selectStmts.addElement(DomainObject.SELECT_NAME);
        selectStmts.addType();
        selectStmts.addDescription();

        SelectList selectRelStmts = null;
        MapList relatedECOs = ecrObj.expandSelect(context,
                                                       srelPattern,
                                                       "*",
                                                       selectStmts,
                                                       selectRelStmts,
                                                       true,
                                                       false,
                                                       (short)1,
                                                       null,
                                                       null,
                                                       null,
                                                       false);
        return relatedECOs;
    }


   /**
     * Constructs the ECR Related ECOs HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @return String Connected Related ECOs in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since 10.6.
     */

     public String getRelatedECOsDetails(Context context,String[] args)
             throws Exception
     {
        if (args == null || args.length < 1)
        {
              throw (new IllegalArgumentException());
        }

        String strLanguage = context.getSession().getLanguage();
        String objectId = args[0];

        ECR ecrObj = new ECR(objectId);
        SelectList selectStmts = ecrObj.getObjectSelectList(4);
        selectStmts.addElement(DomainObject.SELECT_ID);
        selectStmts.addElement(DomainObject.SELECT_NAME);
        selectStmts.addType();
        selectStmts.addDescription();

        SelectList selectRelStmts = null;
        MapList relatedECOs = ecrObj.expandSelect(context,
                                                       RELATIONSHIP_ECO_CHANGEREQUESTINPUT,
                                                       "*",
                                                       selectStmts,
                                                       selectRelStmts,
                                                       true,
                                                       false,
                                                       (short)1,
                                                       null,
                                                       null,
                                                       null,
                                                       false);
		//Added for Bug-310047 on 10/May/2006
		relatedECOs.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
		relatedECOs.sort();
        //String queryString = "";
        StringBuffer returnString=new StringBuffer(512);

        returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"2\" >");
        returnString.append("<tr>  <th  nowrap>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage));
        returnString.append("</th> <th   nowrap>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage));
        returnString.append("</th> <th  nowrap>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage));
        returnString.append("</th> </tr>");

        if(relatedECOs != null)
        {
            Iterator mapItr = relatedECOs.iterator();

            String imgECOType = "";
            Map ecoMap = null;
            while(mapItr.hasNext())
            {
                ecoMap = (Map)mapItr.next();
                imgECOType = EngineeringUtil.getTypeIconProperty(context, (String)ecoMap.get(DomainConstants.SELECT_TYPE));
                if (imgECOType == null || imgECOType.length() == 0 )
                {
                    imgECOType = "iconSmallDefault.gif";
                }
                returnString.append("<tr>");
                returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\"><img src=\"../common/images/"+imgECOType+"\" border=\"0\" alt=\"*\">&nbsp;"+ecoMap.get(DomainConstants.SELECT_NAME)+"&nbsp;</td>");
                returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+ecoMap.get(DomainConstants.SELECT_TYPE)+"&nbsp;</td>");
                returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+ecoMap.get(DomainConstants.SELECT_DESCRIPTION)+"&nbsp;</td>");
            }
        }
        if(relatedECOs == null || relatedECOs.size() == 0)
        {
            returnString.append("<tr><td colspan=\"3\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.NoObjectsFound",strLanguage)+"</td></tr>");
        }
        returnString.append("</table>");
        String finalStr = returnString.toString();

        return finalStr;
    }

/**
  * Get Late Approval ECR ECO Report for the specified criteria
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds input arguments.
  * @return Vector containing search result.
  * @exception Exception if the operation fails.
  * This is only for Started Route
  * @since EC 10.6
  */
     @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getECRECOSearchResultActive(Context context , String[] args)
                    throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        com.matrixone.apps.domain.DomainObject domObject = new com.matrixone.apps.domain.DomainObject();
        com.matrixone.apps.common.InboxTask    inboxTask = new com.matrixone.apps.common.InboxTask();

        //Retrieve Search criteria
        String selType          = (String)paramMap.get("selType");
        String txtRDO           = (String)paramMap.get("txtRDO");
        String txtDays           = (String)paramMap.get("txtDays");
        String txtOwner         = (String)paramMap.get("txtOwner");

        int lateDays = 0;
        if(txtDays!=null && !txtDays.equals(""))
        {
            lateDays = Integer.parseInt(txtDays);
        }

        /*******************************Vault Code Start. ( This is MatrixOne Standard Code)************************/
        // Get the user's vault option & call corresponding methods to get the vault's.
        String txtVault   ="";
        String strVaults="";
        StringList strListVaults=new StringList();

        String txtVaultOption = (String)paramMap.get("vaultOption");
        if(txtVaultOption==null)
        {
            txtVaultOption="";
        }
        String vaultAwarenessString = (String)paramMap.get("vaultAwarenessString");

        if(vaultAwarenessString.equalsIgnoreCase("true"))
        {

            if("ALL_VAULTS".equals(txtVaultOption) || "".equals(txtVaultOption))
            {
                strListVaults = com.matrixone.apps.common.Person.getCollaborationPartnerVaults(context,null);
                StringItr strItr = new StringItr(strListVaults);
                if(strItr.next())
                {
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
                strListVaults = VaultUtil.getLocalVaults(context);
                StringItr strItr = new StringItr(strListVaults);
                if(strItr.next())
                {
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

        // Define sWhere
        String stateSubmit   = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECR, "state_Submit");
        String stateEvaluate = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECR, "state_Evaluate");
        String stateReview   = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECR, "state_Review");
        String statePlanECO  = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECR, "state_PlanECO");

        String stateDefineComponents = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECO, "state_DefineComponents");
        String stateDesignWork       = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECO, "state_DesignWork");

        String sWhere = "";
        if (mxType.isOfParentType(context,selType,DomainConstants.TYPE_ECR))
        {
        	sWhere  += "current == \"" + stateReview + "\"";
            sWhere  +=  " || current == \"" + stateSubmit + "\"";
            sWhere  +=  " || current == \"" + stateEvaluate + "\"";
            sWhere  +=  " || current == \"" + statePlanECO + "\"";
        }
        else if (mxType.isOfParentType(context,selType,DomainConstants.TYPE_ECO))
        {
        	if(!("".equals(sWhere)))
        	{
        		sWhere += " || ";
        	}
            sWhere  +=  "current == \"" + stateDefineComponents + "\"";
            sWhere  +=  " || current == \"" + stateDesignWork + "\"";
        }

        SelectList selectListECR = new SelectList(5);
        selectListECR.addElement(domObject.SELECT_ID);
        selectListECR.addElement(domObject.SELECT_TYPE);
        selectListECR.addElement(domObject.SELECT_NAME);
        selectListECR.addElement(domObject.SELECT_POLICY);
        selectListECR.addElement(domObject.SELECT_DESCRIPTION);

        MapList totalresultList = new MapList();
        MapList objResultList = new MapList();

        if (!"*".equals(txtOwner))
        {
            sWhere ="(" + sWhere + ")" +" && (owner ~~\""+txtOwner+"\")";
        }

        if(txtRDO==null || txtRDO.length()==0 || "null".equals(txtRDO))
        {
            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
		    SelectList busSelect = new SelectList(1);
		    busSelect.add(person.SELECT_ID);

            String sWhereCond = "( current.access[read] == TRUE )";

			MapList routeList = DomainObject.findObjects(context,
            		TYPE_ROUTE,
					 "*",
					 "*",
					 "*",
					 "*",
					 sWhereCond,
					 null,
					 true,
					 busSelect,
					 (short) 0);           //Modified for IR-253617

            ArrayList uniqueRoutList=new ArrayList();
            int intRouteSize = 0;
            int intECOECRSize=0;
            String currentRoute="";
            if(routeList != null && ((intRouteSize = routeList.size()) > 0 ))
            {
                for(int i = 0 ; i < intRouteSize ; i++)
                {
                    currentRoute=(String)((Map)routeList.get(i)).get(SELECT_ID);
                    if(!uniqueRoutList.contains(currentRoute))
                    {
                        uniqueRoutList.add(currentRoute);
                    }
                }
            }
            if(uniqueRoutList != null && ((intRouteSize = uniqueRoutList.size()) > 0 ))
            {
                MapList tempResultList=new MapList();
                for(int i = 0 ; i < intRouteSize ; i++)
                {
                    domObject.setId((String)uniqueRoutList.get(i));
                    objResultList = domObject.getRelatedObjects(
                                                context
                                                ,RELATIONSHIP_OBJECT_ROUTE
                                                ,selType
                                                ,selectListECR
                                                ,null
                                                ,true
                                                ,false
                                                ,(short)1
                                                ,sWhere
                                                ,""
                                                );
                    tempResultList.addAll(objResultList);
                }
                 objResultList.clear();
                 if(tempResultList != null && ((intECOECRSize = tempResultList.size()) > 0 ))
                    {
                        ArrayList uniqueECOECRList=new ArrayList();
                        String currentECOECR="";
                        for(int i = 0 ; i < intECOECRSize ; i++)
                        {
                            currentECOECR=(String)((Map)tempResultList.get(i)).get(SELECT_ID);
                            if(!uniqueECOECRList.contains(currentECOECR))
                            {
                                    objResultList.add(tempResultList.get(i));
                                    uniqueECOECRList.add(currentECOECR);
                            }
                        }
                    }
            }
        }
        else
        {
        	if(!("".equals(sWhere)))
        	{
        		sWhere += " && ";
        	}
        	sWhere += "(from["+RELATIONSHIP_OBJECT_ROUTE+"].to.current.access[read] == TRUE ) ";
            domObject.setId(txtRDO);

            //Modified for IR-259407
            String strRDOName = domObject.getInfo(context, SELECT_NAME);
        	sWhere += "&& altowner1 == \""+strRDOName+"\"";

            objResultList = DomainObject.findObjects(context,
        			selType,
					 "*",
					 "*",
					 "*",
					 "*",
					 sWhere,
					 null,
					 true,
					 selectListECR,
					 (short) 0);

        }
        String stateComplete                  = PropertyUtil.getSchemaProperty(context, "policy",POLICY_INBOX_TASK , "state_Complete");

        String sAttrRouteAction               = PropertyUtil.getSchemaProperty(context,"attribute_RouteAction");
        String sAttrScheduledCompletionDate   = PropertyUtil.getSchemaProperty(context,"attribute_ScheduledCompletionDate");
        String sAttrTitle                     = PropertyUtil.getSchemaProperty(context,"attribute_Title");

        StringList objSelect = new StringList(6);
        objSelect.add(inboxTask.SELECT_ID);
        objSelect.add(inboxTask.SELECT_NAME);
        objSelect.add(inboxTask.SELECT_CURRENT);
        objSelect.add("attribute["+sAttrRouteAction+"]");
        objSelect.add("attribute["+sAttrScheduledCompletionDate+"]");
        objSelect.add("attribute["+sAttrTitle+"]");

        String sRelString = "].to.from["+RELATIONSHIP_OBJECT_ROUTE+"].to.to["+RELATIONSHIP_ROUTE_TASK+"].from.id";

        StringList taskList = new StringList();                       //InBox Task List in one ECR or ECO
        String taskId = "";

        Date dueDate = null;
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        int curDay  = calendar.get(Calendar.DAY_OF_YEAR);

        String taskDueDate = null;

        Map resultMap = null;
        Map objMap = null;
        Iterator objMapItr = objResultList.iterator();

        while(objMapItr.hasNext())                                     //Start ECR or ECO MapList loop to get all InboxTasks
        {
            objMap = (Map) objMapItr.next();

            domObject.setId((String)objMap.get(domObject.SELECT_ID));

            taskList = domObject.getInfoList(context, "from["+RELATIONSHIP_OBJECT_ROUTE+"].to.to["+RELATIONSHIP_ROUTE_TASK+"].from.id");

           // taskList.addAll( domObject.getInfoList(context, "from["+RELATIONSHIP_CHANGE_AFFECTED_ITEM+sRelString) );
           // taskList.addAll( domObject.getInfoList(context, "from["+RELATIONSHIP_IMPLEMENTED_ITEM+sRelString) );
            taskList.addAll( domObject.getInfoList(context, "from["+RELATIONSHIP_AFFECTED_ITEM+sRelString) );
            taskList.sort();

            taskId = "";
            for(int i=taskList.size()-1; i>=0; i--)
            {
                if(taskId.equals((String) taskList.get(i)))
                {
                    taskList.remove(i);
                    continue;
                }
                taskId = (String) taskList.get(i);
                inboxTask.setId(taskId);

                Map taskInfo = inboxTask.getInfo(context, objSelect);

                if( stateComplete.equals((String)taskInfo.get(inboxTask.SELECT_CURRENT) ))
                {
                    taskList.remove(i);
                    continue;
                }

                taskDueDate = (String) taskInfo.get("attribute["+sAttrScheduledCompletionDate+"]");

                if(taskDueDate == null || taskDueDate.equals(""))
                {
                    taskList.remove(i);
                    continue;
                }
                else
                {
                    dueDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(taskDueDate);
                }

                calendar.setTime(dueDate);

                if(curYear<calendar.get(Calendar.YEAR))
                {
                    taskList.remove(i);
                    continue;
                }
                if(curYear==calendar.get(Calendar.YEAR))
                {
                    if( curDay - (calendar.get(Calendar.DAY_OF_YEAR) ) < lateDays  )
                    {
                        taskList.remove(i);
                        continue;
                    }
                }
                if(curYear>calendar.get(Calendar.YEAR))
                {
                    if( (curDay+364-calendar.get(Calendar.DAY_OF_YEAR)) < lateDays  )
                    {
                        taskList.remove(i);
                        continue;
                    }
                }

                resultMap = new HashMap();
                resultMap.put(domObject.SELECT_ID, objMap.get(domObject.SELECT_ID));
                resultMap.put(domObject.SELECT_NAME, objMap.get(domObject.SELECT_NAME));
                resultMap.put(domObject.SELECT_DESCRIPTION, objMap.get(domObject.SELECT_DESCRIPTION));
                resultMap.put(domObject.SELECT_POLICY, objMap.get(domObject.SELECT_POLICY));
                resultMap.put(domObject.SELECT_CURRENT, objMap.get(domObject.SELECT_CURRENT));

                resultMap.put("taskId", taskId);
                if(((String)taskInfo.get("attribute["+sAttrTitle+"]"))==null || ((String)taskInfo.get("attribute["+sAttrTitle+"]")).equals(""))
                {
                    resultMap.put("taskName", taskInfo.get(inboxTask.SELECT_NAME));
                }
                else
                {
                    resultMap.put("taskName", taskInfo.get("attribute["+sAttrTitle+"]"));
                }
                resultMap.put("taskId", taskInfo.get(inboxTask.SELECT_ID));
                resultMap.put("taskAction", taskInfo.get("attribute["+sAttrRouteAction+"]"));
                resultMap.put("taskDueDate", taskInfo.get("attribute["+sAttrScheduledCompletionDate+"]"));
                resultMap.put("taskAssignee", inboxTask.getTaskAssignee(context));
                resultMap.put("taskAssigneeId", inboxTask.getTaskAssigneeId(context));

                totalresultList.add(resultMap);

            }         //End of for(int i=taskList.size(); i>=0; i--)

        }  //End of while(objMapItr.hasNext())

        return totalresultList;
    }



  /**
  * Get Late Approval ECR ECO Report for the specified criteria
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds input arguments.
  * @return Vector containing search result.
  * @exception Exception if the operation fails.
  *  This is used for both Started and not Started Routes
  * @since EC 10.6
  */
     @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getECRECOSearchResultAll(Context context , String[] args)
                    throws Exception
  {
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    com.matrixone.apps.domain.DomainObject domObject = new com.matrixone.apps.domain.DomainObject();
    com.matrixone.apps.common.InboxTask     inboxTask = new com.matrixone.apps.common.InboxTask();
    com.matrixone.apps.common.Route            route     = new com.matrixone.apps.common.Route();

    //Retrieve Search criteria
    String selType          = (String)paramMap.get("selType");
    String txtRDO           = (String)paramMap.get("txtRDO");
    String txtDays           = (String)paramMap.get("txtDays");
    String txtOwner         = (String)paramMap.get("txtOwner");

    int lateDays = 0;
    if(txtDays!=null && !txtDays.equals(""))
    {
        lateDays = Integer.parseInt(txtDays);
    }

    /*******************************Vault Code Start***************************************/
    // Get the user's vault option & call corresponding methods to get the vault's.
    String txtVault   ="";
    String strVaults="";
    StringList strListVaults=new StringList();

    String txtVaultOption = (String)paramMap.get("vaultOption");
    if(txtVaultOption==null)
    {
        txtVaultOption="";
    }
    String vaultAwarenessString = (String)paramMap.get("vaultAwarenessString");

    if(vaultAwarenessString.equalsIgnoreCase("true"))
    {
        if("ALL_VAULTS".equals(txtVaultOption) || "".equals(txtVaultOption))
        {
            strListVaults = com.matrixone.apps.common.Person.getCollaborationPartnerVaults(context,null);
            StringItr strItr = new StringItr(strListVaults);
            if(strItr.next())
            {
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
            strListVaults = VaultUtil.getLocalVaults(context);
            StringItr strItr = new StringItr(strListVaults);
            if(strItr.next())
            {
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


    String stateSubmit   = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECR, "state_Submit");
    String stateEvaluate = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECR, "state_Evaluate");
    String stateReview   = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECR, "state_Review");
    String statePlanECO  = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECR, "state_PlanECO");

    String stateDefine           = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ROUTE , "state_Define");
    String stateDefineComponents = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECO, "state_DefineComponents");
    String stateDesignWork       = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECO, "state_DesignWork");

  String sWhere = "";
	if (mxType.isOfParentType(context,selType,DomainConstants.TYPE_ECR))
    {
		sWhere  +=  "current == \"" + stateReview + "\"";
        sWhere  +=  " || current == \"" + stateSubmit + "\"";
        sWhere  +=  " || current == \"" + stateEvaluate + "\"";
        sWhere  +=  " || current == \"" + statePlanECO + "\"";
    }
    else if (mxType.isOfParentType(context,selType,DomainConstants.TYPE_ECO))
    {
    	if(!("".equals(sWhere)))
    	{
    		sWhere += " || ";
    	}
        sWhere  +=  "current == \"" + stateDefineComponents + "\"";
        sWhere  +=  " || current == \"" + stateDesignWork + "\"";
    }

    SelectList selectListECR = new SelectList(5);
    selectListECR.addElement(domObject.SELECT_ID);
    selectListECR.addElement(domObject.SELECT_TYPE);
    selectListECR.addElement(domObject.SELECT_NAME);
    selectListECR.addElement(domObject.SELECT_POLICY);
    selectListECR.addElement(domObject.SELECT_DESCRIPTION);

    if (!"*".equals(txtOwner))
            {
                sWhere ="(" + sWhere + ")" +" && (owner ~~\""+txtOwner+"\")";
            }

    MapList totalresultList = new MapList();
    MapList objResultList = new MapList();

    if(txtRDO==null || txtRDO.length()==0 || "null".equals(txtRDO))
    {
        com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
        SelectList busSelect = new SelectList(1);
        busSelect.add(person.SELECT_ID);


        String sWhereCond = "( current.access[read] == TRUE )";

              MapList routeList = DomainObject.findObjects(context,
        		TYPE_ROUTE,
				 "*",
				 "*",
				 "*",
				 "*",
				 sWhereCond,
				 null,
				 true,
				 busSelect,
				 (short) 0);           //Modified for IR-253617

        ArrayList uniqueRoutList=new ArrayList();
        int intRouteSize = 0;
        int intECOECRSize=0;
        String currentRoute="";
        if(routeList != null && ((intRouteSize = routeList.size()) > 0 ))
        {
            for(int i = 0 ; i < intRouteSize ; i++)
            {
                currentRoute=(String)((Map)routeList.get(i)).get(SELECT_ID);
                if(!uniqueRoutList.contains(currentRoute))
                {
                    uniqueRoutList.add(currentRoute);
                }
            }
        }
        if(uniqueRoutList != null && ((intRouteSize = uniqueRoutList.size()) > 0 ))
        {
            MapList tempResultList=new MapList();
            for(int i = 0 ; i < intRouteSize ; i++)
            {
                domObject.setId((String)uniqueRoutList.get(i));
                objResultList = domObject.getRelatedObjects(
                                            context
                                            ,RELATIONSHIP_OBJECT_ROUTE
                                            ,selType
                                            ,selectListECR
                                            ,null
                                            ,true
                                            ,false
                                            ,(short)1
                                            ,sWhere
                                            ,""
                                            );
                tempResultList.addAll(objResultList);
            }
            objResultList.clear();
             if(tempResultList != null && ((intECOECRSize = tempResultList.size()) > 0 ))
                {
                    ArrayList uniqueECOECRList=new ArrayList();
                    String currentECOECR="";
                    for(int i = 0 ; i < intECOECRSize ; i++)
                    {
                        currentECOECR=(String)((Map)tempResultList.get(i)).get(SELECT_ID);
                        if(!uniqueECOECRList.contains(currentECOECR))
                        {
                                objResultList.add(tempResultList.get(i));
                                uniqueECOECRList.add(currentECOECR);
                        }
                    }
                }
        }
    }
        else
        {
        	if(!("".equals(sWhere)))
        	{
        		sWhere += " && ";
        	}

        	sWhere += " (from["+RELATIONSHIP_OBJECT_ROUTE+"].to.current.access[read] == TRUE ) ";
            domObject.setId(txtRDO);

          //Modified for IR-259407
            String strRDOName = domObject.getInfo(context, SELECT_NAME);
        	sWhere += "&& altowner1 == \""+strRDOName+"\"";

            objResultList = DomainObject.findObjects(context,
        			selType,
					 "*",
					 "*",
					 "*",
					 "*",
					 sWhere,
					 null,
					 true,
					 selectListECR,
					 (short) 0);
        }

  String stateComplete                  = PropertyUtil.getSchemaProperty(context, "policy",POLICY_INBOX_TASK , "state_Complete");
  String sAttrRouteAction               = PropertyUtil.getSchemaProperty(context,"attribute_RouteAction");
  String sAttrScheduledCompletionDate   = PropertyUtil.getSchemaProperty(context,"attribute_ScheduledCompletionDate");
  String sAttrTitle                     = PropertyUtil.getSchemaProperty(context,"attribute_Title");

  StringList objSelect = new StringList(6);
  objSelect.add(inboxTask.SELECT_ID);
  objSelect.add(inboxTask.SELECT_NAME);
  objSelect.add(inboxTask.SELECT_CURRENT);
  objSelect.add("attribute["+sAttrRouteAction+"]");
  objSelect.add("attribute["+sAttrScheduledCompletionDate+"]");
  objSelect.add("attribute["+sAttrTitle+"]");


  StringList objRouteSelect = new StringList();
  objRouteSelect.add(route.SELECT_NAME);
  objRouteSelect.add(route.SELECT_CURRENT);

  StringList relatedObjSelect = new StringList();
  relatedObjSelect.add(route.SELECT_NAME);
  relatedObjSelect.add(route.SELECT_ID);

  StringList relSelect = new StringList();
  relSelect.add(route.SELECT_ROUTE_ACTION);
  relSelect.add(route.SELECT_SCHEDULED_COMPLETION_DATE);
  relSelect.add(route.SELECT_TITLE);
  relSelect.add(route.SELECT_ROUTE_SEQUENCE);
  relSelect.add(route.SELECT_ROUTE_TASK_USER);

  String sRelString = "].to.from["+RELATIONSHIP_OBJECT_ROUTE+"].to.to["+RELATIONSHIP_ROUTE_TASK+"].from.id";

  StringList taskList = new StringList();                     //InBox Task List in one ECR or ECO
  StringList routeList = new StringList();                     //Route List in one ECR or ECO NNN


  String taskId = "";
  String routeId = "";

  Date dueDate = null;
  java.util.Calendar calendar = java.util.Calendar.getInstance();
  int curYear = calendar.get(Calendar.YEAR);
  int curDay  = calendar.get(Calendar.DAY_OF_YEAR);

  String taskDueDate = null;

  Map resultMap = null;
  Map objMap = null;
  Iterator objMapItr = objResultList.iterator();

  while(objMapItr.hasNext())
    {
        objMap = (Map) objMapItr.next();
        domObject.setId((String)objMap.get(domObject.SELECT_ID));
        taskList = domObject.getInfoList(context, "from["+RELATIONSHIP_OBJECT_ROUTE+"].to.to["+RELATIONSHIP_ROUTE_TASK+"].from.id");


        //ECM
       // taskList.addAll( domObject.getInfoList(context, "from["+RELATIONSHIP_CHANGE_AFFECTED_ITEM+sRelString) );
        //taskList.addAll( domObject.getInfoList(context, "from["+RELATIONSHIP_IMPLEMENTED_ITEM+sRelString) );
        taskList.addAll( domObject.getInfoList(context, "from["+RELATIONSHIP_AFFECTED_ITEM+sRelString) );
        taskList.sort();
        taskId = "";
        for(int i=taskList.size()-1; i>=0; i--)
        {
            if(taskId.equals((String) taskList.get(i)))
            {
                taskList.remove(i);
                continue;
            }
            taskId = (String) taskList.get(i);
            inboxTask.setId(taskId);
            Map taskInfo = inboxTask.getInfo(context, objSelect);

            if( stateComplete.equals((String)taskInfo.get(inboxTask.SELECT_CURRENT) ))
            {
                taskList.remove(i);
                continue;
            }
            taskDueDate = (String) taskInfo.get("attribute["+sAttrScheduledCompletionDate+"]");
            if(taskDueDate == null || taskDueDate.equals(""))
            {
                taskList.remove(i);
                continue;
            }
            else
            {
                dueDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(taskDueDate);
            }

            calendar.setTime(dueDate);

            if(curYear<calendar.get(Calendar.YEAR))
            {
                taskList.remove(i);
                continue;
            }
            if(curYear==calendar.get(Calendar.YEAR))
            {
                if( curDay - (calendar.get(Calendar.DAY_OF_YEAR) )  < lateDays  )
                {
                    taskList.remove(i);
                    continue;
                }
            }
            if(curYear>calendar.get(Calendar.YEAR))
            {
                if( (curDay+364-calendar.get(Calendar.DAY_OF_YEAR)) < lateDays  )
                {
                    taskList.remove(i);
                    continue;
                }
            }

            resultMap = new HashMap();
            resultMap.put(domObject.SELECT_ID, objMap.get(domObject.SELECT_ID));
            resultMap.put(domObject.SELECT_NAME, objMap.get(domObject.SELECT_NAME));
            resultMap.put(domObject.SELECT_DESCRIPTION, objMap.get(domObject.SELECT_DESCRIPTION));
            resultMap.put(domObject.SELECT_POLICY, objMap.get(domObject.SELECT_POLICY));
            resultMap.put(domObject.SELECT_CURRENT, objMap.get(domObject.SELECT_CURRENT));
            resultMap.put("taskId", taskId);

            if(((String)taskInfo.get("attribute["+sAttrTitle+"]"))==null || ((String)taskInfo.get("attribute["+sAttrTitle+"]")).equals(""))
            {
                resultMap.put("taskName", taskInfo.get(inboxTask.SELECT_NAME));
            }
            else
            {
                resultMap.put("taskName", taskInfo.get("attribute["+sAttrTitle+"]"));
            }
            resultMap.put("taskId", taskInfo.get(inboxTask.SELECT_ID));
            resultMap.put("taskAction", taskInfo.get("attribute["+sAttrRouteAction+"]"));
            resultMap.put("taskDueDate", taskInfo.get("attribute["+sAttrScheduledCompletionDate+"]"));
            resultMap.put("taskAssignee", inboxTask.getTaskAssignee(context));
            resultMap.put("taskAssigneeId", inboxTask.getTaskAssigneeId(context));

            totalresultList.add(resultMap);

        }       //End of for(int i=taskList.size(); i>=0; i--)

        //Start to get not Started Routes and their Task Info

        routeList = domObject.getInfoList(context, "from["+RELATIONSHIP_OBJECT_ROUTE+"].to.id");

       // routeList.addAll( domObject.getInfoList(context, "from["+RELATIONSHIP_CHANGE_AFFECTED_ITEM+"].to.from["+RELATIONSHIP_OBJECT_ROUTE+"].to.id") );
        //routeList.addAll( domObject.getInfoList(context, "from["+RELATIONSHIP_IMPLEMENTED_ITEM+"].to.from["+RELATIONSHIP_OBJECT_ROUTE+"].to.id") );

        routeList.addAll( domObject.getInfoList(context, "from["+RELATIONSHIP_AFFECTED_ITEM+"].to.from["+RELATIONSHIP_OBJECT_ROUTE+"].to.id") );

        routeList.sort();
        routeId = "";

        for(int i=routeList.size()-1; i>=0; i--)
        {
            if(routeId.equals((String) routeList.get(i)))
            {
                routeList.remove(i);
                continue;
            }
            routeId = (String) routeList.get(i);
            route.setId(routeId);

            Map routeInfo = route.getInfo(context, objRouteSelect);

            if( !stateDefine.equals((String)routeInfo.get(route.SELECT_CURRENT) ))
            {
                routeList.remove(i);
                continue;
            }

            MapList relatedTaskInfo = route.getRelatedObjects(context,
                                        route.RELATIONSHIP_ROUTE_NODE, //java.lang.String relationshipPattern
                                        route.TYPE_PERSON,             //java.lang.String typePattern
                                        relatedObjSelect,              //matrix.util.StringList objectSelects,
                                        relSelect,                     //matrix.util.StringList relationshipSelects,
                                        false,                         //boolean getTo,
                                        true,                          //boolean getFrom,
                                        (short)1,                      //short recurseToLevel,
                                        null,                          //java.lang.String objectWhere,
                                        null);                         //java.lang.String relationshipWhere)

            for(int j =0; j < relatedTaskInfo.size(); j++)
            {
                Map tempMap    = (Hashtable) relatedTaskInfo.get(j);
                String personName = (String) tempMap.get(route.SELECT_ROUTE_TASK_USER);
                if(personName == null || "".equals(personName))
                {
                    personName =  (String) tempMap.get(route.SELECT_NAME);
                }
                // Only Show Tasks that sequence order is 1
                if( ! ((String) tempMap.get(route.SELECT_ROUTE_SEQUENCE) ).equals("1") )
                {
                    continue;
                }

                taskDueDate = (String) tempMap.get(route.SELECT_SCHEDULED_COMPLETION_DATE);

                if(taskDueDate == null || taskDueDate.equals(""))
                {
                    continue;
                }
                else
                {
                    dueDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(taskDueDate);
                }
                calendar.setTime(dueDate);

                if(curYear<calendar.get(Calendar.YEAR))
                {
                    continue;
                }
                if(curYear==calendar.get(Calendar.YEAR))
                {
                    if( curDay - (calendar.get(Calendar.DAY_OF_YEAR) ) < lateDays  )
                    {
                        continue;
                    }
                }
                if(curYear>calendar.get(Calendar.YEAR))
                {
                    if( (curDay+364*(curYear-calendar.get(Calendar.YEAR))-calendar.get(Calendar.DAY_OF_YEAR)) < lateDays  )
                    {
                        continue;
                    }
                }

                resultMap = new HashMap();
                resultMap.put(domObject.SELECT_ID, objMap.get(domObject.SELECT_ID));
                resultMap.put(domObject.SELECT_NAME, objMap.get(domObject.SELECT_NAME));
                resultMap.put(domObject.SELECT_DESCRIPTION, objMap.get(domObject.SELECT_DESCRIPTION));
                resultMap.put(domObject.SELECT_POLICY, objMap.get(domObject.SELECT_POLICY));
                resultMap.put(domObject.SELECT_CURRENT, objMap.get(domObject.SELECT_CURRENT));
                resultMap.put("taskId","");
                resultMap.put("taskName", tempMap.get(route.SELECT_TITLE));
                resultMap.put("taskAction", tempMap.get(route.SELECT_ROUTE_ACTION) );
                resultMap.put("taskDueDate",  tempMap.get(route.SELECT_SCHEDULED_COMPLETION_DATE) );
                resultMap.put("taskAssignee", personName);
                resultMap.put("taskAssigneeId", inboxTask.getTaskAssigneeId(context));
                resultMap.put("routeId", routeId);

                totalresultList.add(resultMap);

            }     //End of for(int j =0; j < relatedTaskInfo.size(); j++)

        }         //End of for(int i=routeList.size()-1; i>=0; i--)

    }  //End of while(objMapItr.hasNext())

  return totalresultList;
  }




/**
  * This method is used to to put Task name of InboxTask in HTML format for emxTable.jsp
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
  * @returns Vector of "TaskName" image for each row.
  * @since EC 10.6
  */
  public Vector getLateInboxTaskName(Context context, String[] args)   throws Exception
  {

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap= (HashMap)programMap.get("paramList");
    MapList objList = (MapList)programMap.get("objectList");

    Vector columnVals = new Vector(objList.size());
    String outString = "";
    String taskId = "";
    String strReptFormat = (String) paramMap.get("reportFormat");
    String export = (String)paramMap.get("exportFormat");

    Iterator listItr = objList.iterator();
    Map ecroMap = null;
    while ( listItr.hasNext() )
    {
        ecroMap = (Map)  listItr.next();
        taskId = (String) ecroMap.get("taskId");
        if( taskId!=null && !taskId.equals("")   && (strReptFormat == null || "null".equals(strReptFormat)) && (export == null || "null".equals(export)))
        {
        	outString = "<table border='0'><tr><td valign='top'><input type=hidden name='taskName' value='" +  XSSUtil.encodeForHTMLAttribute(context, (String) ecroMap.get("taskName")) +"'>";
            outString += "<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=";
            outString += XSSUtil.encodeForURL(context, taskId);
            outString += "', '875', '550', 'false', 'popup')\"  class='object'><img border='0' src='../common/images/iconSmallInboxTask.gif' alt='*'></a></td>";
            outString += "<td>&nbsp;<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=";
            outString += XSSUtil.encodeForURL(context,taskId);
            outString += "', '875', '550', 'false', 'popup')\"  class='object'>";
            outString += XSSUtil.encodeForHTML(context, (String) ecroMap.get("taskName"));
            outString += "</a>&nbsp;</td></tr></table>";
        }
        else
        {
            outString += "<img border='0' src='../common/images/iconSmallInboxTask.gif' alt='*'>";
            outString = XSSUtil.encodeForHTML(context, (String) ecroMap.get("taskName"));
        }

        columnVals.addElement(  outString  );
    }

    return columnVals;
  }

/**
  * This method is used to to put Task Action of InboxTask in Vector for emxTable.jsp
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
  * @returns Vector of "TaskAction" image for each row.
  * @since EC 10.6
  */
  public Vector getLateInboxTaskAction(Context context,  String[] args)
    throws Exception
  {

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList) programMap.get("objectList");
    String strAttribute = PropertyUtil.getSchemaProperty(context,"attribute_RouteAction");
    String strLanguage = context.getSession().getLanguage();


    Vector columnVals = new Vector(objList.size());
    String outString = "";
    Map ecroMap = null;
    Iterator listItr = objList.iterator();
    while ( listItr.hasNext() )
    {
        ecroMap = (Map)  listItr.next();
        outString =  XSSUtil.encodeForHTML(context,(String) ecroMap.get("taskAction"));
        columnVals.addElement( i18nNow.getRangeI18NString(strAttribute,outString,strLanguage));
    }

    return columnVals;
}



/**
 * This method is used to to put Task Assignee of InboxTask in Vector for emxTable.jsp
 * @param context the eMatrix <code>Context</code> object
 * @param args holds no arguments
 * @returns Vector of "Task Assignee" image for each row.
 * @since EC 10.6
 */
  public Vector getLateInboxTaskAssignee(Context context,  String[] args)
    throws Exception
{

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap= (HashMap)programMap.get("paramList");
    MapList objList = (MapList) programMap.get("objectList");

    Vector columnVals = new Vector(objList.size());
    String outString = "";
    Map ecroMap = null;
    String taskAssigneeId="";
    String strReptFormat = (String) paramMap.get("reportFormat");
    String export = (String)paramMap.get("exportFormat");
    Iterator listItr = objList.iterator();
    while ( listItr.hasNext() )
    {
        ecroMap = (Map)  listItr.next();
        taskAssigneeId=(String) ecroMap.get("taskAssigneeId");

        if(!"".equals(taskAssigneeId)&&(strReptFormat == null || "null".equals(strReptFormat))  &&  (export == null || "null".equals(export)))
        {
            outString = "<table border='0'><tr><td valign='top'><input type=hidden name='assigneeName' value='" +  XSSUtil.encodeForHTMLAttribute(context, (String) ecroMap.get("taskAssignee")) +"'>";
            outString+=("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=");
            outString+=(XSSUtil.encodeForURL(context, (String) ecroMap.get("taskAssigneeId")));
            outString += "', '875', '550', 'false', 'popup')\"  class='object'>";
            outString+=(XSSUtil.encodeForHTML(context, (String) ecroMap.get("taskAssignee")));
            outString += "</a></td></tr></table>";
        }
        else
        {
            outString=(XSSUtil.encodeForHTML(context,((String) ecroMap.get("taskAssignee"))));
        }

        columnVals.addElement(outString);
    }

    return columnVals;
 }


/**
 * This method is used to to put Task Due Date of InboxTask in Vector for emxTable.jsp
 * @param context the eMatrix <code>Context</code> object
 * @param args holds no arguments
 * @returns Vector of "Task Due Date" image for each row.
 * @since EC 10.6
 */
 public Vector getLateInboxTaskDueDate(Context context,  String[] args)
    throws Exception
{

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList) programMap.get("objectList");

    Vector columnVals = new Vector(objList.size());
    String outString = "";
    Map ecroMap = null;
    Iterator listItr = objList.iterator();
    while ( listItr.hasNext() )
    {
        ecroMap = (Map)  listItr.next();
        outString = XSSUtil.encodeForHTML(context, (String) ecroMap.get("taskDueDate"));
        columnVals.addElement(  outString  );
    }

    return columnVals;
}


/**
 * This method is used to to put Days Late of InboxTask in Vector for emxTable.jsp
 * @param context the eMatrix <code>Context</code> object
 * @param args holds no arguments
 * @returns Vector of "Day Late"  for each row.
 * @since EC 10.6
 */

 public Vector getLateDays(Context context,  String[] args)
    throws Exception
{

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList) programMap.get("objectList");

    Vector columnVals = new Vector(objList.size());
    int dayLate;
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    int curYear = calendar.get(Calendar.YEAR);
    int curDay  = calendar.get(Calendar.DAY_OF_YEAR);
    Date dueDate = null;
    Map ecroMap = null;
    Iterator listItr = objList.iterator();
    while ( listItr.hasNext() )
    {
        ecroMap = (Map)  listItr.next();
        dayLate=0;
        dueDate=com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate((String)      ecroMap.get("taskDueDate"));
        calendar.setTime(dueDate);
        if (curDay<calendar.get(Calendar.DAY_OF_YEAR))
        {
            dayLate=((curYear-calendar.get(Calendar.YEAR))-1)*365;
            dayLate+=365-(calendar.get(Calendar.DAY_OF_YEAR)-curDay);
        }
        else
        {
            dayLate=(curYear-calendar.get(Calendar.YEAR))*365;
            dayLate+=curDay-calendar.get(Calendar.DAY_OF_YEAR);
        }

        columnVals.addElement(XSSUtil.encodeForHTML(context, new Integer(dayLate).toString()));
    }

    return columnVals;
 }

 /**
   * All EBOM Markup in the Approved state that are related to the ECR is connected to the ECO by the relationship,
   * Applied Markup
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns nothing
   * @throws Exception if the operation fails
   * @since 10-6.
   */
    public void connectApprovedEBOMMarkupsToECO(matrix.db.Context context, String[] argsProg) throws Exception
    {
   		String strIsFromAssignTo = PropertyUtil.getRPEValue(context, "MX_FROM_ASSIGNTO", false);

		if ("true".equals(strIsFromAssignTo))
		{
			PropertyUtil.setRPEValue(context, "MX_FROM_ASSIGNTO", "false", false);
			return;
		}

      try
      {

        //DomainObject ecrObj = new DomainObject(argsProg[0]);
        //StringList objectSelects = new StringList(1);
        //objectSelects.addElement(DomainObject.SELECT_ID);

        //MapList ebomMarkupList = ecrObj.getRelatedObjects( context,
        //                                   PropertyUtil.getSchemaProperty(context,"relationship_ProposedMarkup"),
        //                                   PropertyUtil.getSchemaProperty(context,"type_PARTMARKUP") + "," + TYPE_EBOM_MARKUP,
        //                                   objectSelects,
        //                                   null,
        //                                   false,
        //                                   true,
        //                                   (short)1,
        //                                   null,
        //                                   null);

        //get the ECO that is connected to this ECR
        DomainObject ECO = new DomainObject();

        ContextUtil.pushContext(context);

        ContextUtil.startTransaction(context, true);
        ECO.setId(argsProg[1]);

			return;
      }
      catch (Exception e)
      {
        ContextUtil.abortTransaction(context);
        throw e;
      }
      finally
      {
        ContextUtil.popContext(context);
      }
    }

/**
     *  Assigns or Assign and Notifies the user.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @param args holds Relationship Name.
     * @param args holds Relationship attribute name.
     * @param args holds FROM/TO.
     * @param args holds Role value.
     * @param args holds AssignNotify/Assign.
     * @param args holds Notification Object Name.
     * @return Boolean.
     * @throws Exception if the operation fails.
     * @since X3.
    */
    public int assignNotifyUserByRelationship(Context context,String args[]) throws Exception {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }

        // If OCDX, then no need of LeadRole check & Notification
		boolean bOnCloud = false;
		String UserRole = context.getRole();
		int startIndex = UserRole.indexOf("::");
		int endIndex = UserRole.indexOf(".");
		String role = UserRole.substring(startIndex+2, endIndex);
		if (role.equalsIgnoreCase("VPLMProjectLeader")||role.equalsIgnoreCase("VPLMCreator"))
		{
			return 0;
		}        

		String sECRObjectId = args[0]; // object id of ECR
		String sRelationship = PropertyUtil.getSchemaProperty(context,args[1]); // Relationship

		boolean boolFrom = true;
		boolean boolTo = true;

		String sFromToObjcet = args[3]; // from or to
		String sNewOwnerRole = args[4]; // Role
		String sAssignNotify = args[5];
		String sNotification = args[6];
		int inotify = 0;

		if(sFromToObjcet.equalsIgnoreCase("TO"))
		{
			boolTo = false;
		}
		else
		{
			boolFrom = false;
		}
		//set the object id
		setId(sECRObjectId);

		// object Selects
		StringList slObjectSlects = new StringList(2);
		slObjectSlects.addElement(DomainConstants.SELECT_ID);
		slObjectSlects.addElement(DomainConstants.SELECT_NAME);

		// relationship Selects
		StringList slRelSlects = new StringList(1);
		slRelSlects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

		// String strTypePattern = DomainConstants.TYPE_COMPANY + "," + DomainConstants.TYPE_DEPARTMENT + "," + DomainConstants.TYPE_BUSINESS_UNIT;
		String strTypePattern = DomainConstants.TYPE_ORGANIZATION + "," + DomainConstants.TYPE_PROJECT_SPACE;


		//to fetch the from side objects or to side objects
		MapList mlECRRelatedObject = getRelatedObjects(context, sRelationship, strTypePattern, slObjectSlects, slRelSlects, boolTo, boolFrom, (short) 1, null, null);

		//Check to see if the object is connected to the relationship passed
		if(mlECRRelatedObject != null && mlECRRelatedObject.size() > 0) {
			// get the person, with Lead role from maplist
			Map mECRRelatedObject = (Map) mlECRRelatedObject.get(0);

			//get the Organization id
			String strRCOId = mECRRelatedObject.get("id").toString();
			String[] params = {sECRObjectId,strRCOId,sNewOwnerRole};

			//connects the lead role with the ECR object
			boolean bstelead = setLeadRoles(context, params);
			if(bstelead)
			{
				String[] params1 = {sECRObjectId, sNewOwnerRole};
				String sNewOwner = getLeadPerson(context, params1);
				if(sAssignNotify.equalsIgnoreCase("AssignNotify") && sNewOwner!=null && !sNewOwner.equals("")) {
					
					String argsnew[] = {sECRObjectId,sNotification};
                    JPO.invoke(context, "emxNotificationUtil", null , "objectNotification", argsnew, null);
                    setOwner(context, sNewOwner);
				}

				if(sAssignNotify.equalsIgnoreCase("Assign") && sNewOwner!=null && !sNewOwner.equals("")) {
					setOwner(context, sNewOwner);
				}
			}
			else {
				 inotify = 1;
			 }
		}
		else {

			inotify = 1;
		}

        return inotify;
    }

/**
* To get the Ranges for ECR Attribute Catagory Of Change
* @returns ranges of Catagory Of Change
* @throws Exception if the operation fails
* Since EC-X3
*Created by Preeti
*/
public StringList getCatagoryOfChange(Context context,String args[]) throws Exception
		{
				StringList tempMap1 = new StringList();
				String actName = PropertyUtil.getSchemaProperty(context,"attribute_CategoryofChange");
				tempMap1 = getAttProgramRange(context,actName);
				return tempMap1;

		}
/**
* To get the Ranges for ECR Attribute Severity
* @returnsStringList of  ranges of Severity
* @throws Exception if the operation fails
* Since EC-X3
*Created by Preeti
*/
public StringList getSeverity(Context context,String args[]) throws Exception
		{
				StringList tempMap1 = new StringList();
				String actName = PropertyUtil.getSchemaProperty(context,"attribute_Severity");
				tempMap1 = getAttProgramRange(context,actName);
				return tempMap1;

		}

/**
*To get the Ranges for ECR Attribute.
* @param context the eMatrix <code>Context</code> object
* @returns a StringList containing ranges of attribute
* @throws Exception if the operation fails
* Since EC-X3
* Created by Preeti
*/

public StringList  getAttProgramRange(Context context,String actName) throws Exception
 {
//		StringList strlist = new StringList();
		//Getting the Attribute name dynamically from the method parameter
		AttributeType attrType=new AttributeType(actName);
		attrType.open(context);
		//Getting the attributes for the given attribute
		StringList strChoiceList=attrType.getChoices(context);
		strChoiceList.sort();
		StringItr stritr=new StringItr(strChoiceList);
		StringList strlst = new StringList();
				while(stritr.next())
				 {

					String temp =  stritr.obj();
						if (temp != null || !("".equals(temp)) || !("null".equals(temp)) )
						{
							strlst.add(temp);
						}
				 }
		return strlst;
 }



    /**
     * To set the lead roles for persons.
     * This method connects ECR and Person with Lead Responsibility
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: - The ObjectID of the ECR and Oragnization and role
     *
     * @throws Exception
     *             if the operation fails.
     * @since EngineeringCental X3.
     */

 public boolean setLeadRoles (Context context, String[] args) throws Exception {

	    String strECRbjectId = args[0];
	    String strChangeResponsibilityObjId = args[1];
		String strRoleName = args[2];
		boolean bstlead = true;
		
        //Relationship name
        String strRelationshipLeadResponsibility = PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility");
		String strAttributeProjectRole = PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole");

		//Creating ECR and Organization DomainObjects.
		DomainObject doECR = new DomainObject(strECRbjectId);
		DomainObject doOrg = new DomainObject(strChangeResponsibilityObjId);

		//Getting Relationships symbolic names
        RelationshipType relRelationshipLeadResponsibility = new RelationshipType(strRelationshipLeadResponsibility);

	    //Adding selectables
		StringList relStringList = new StringList();
		relStringList.add("attribute["+strAttributeProjectRole+"]");
		relStringList.add("to.id");
	
		//Selecting person with the given lead role for Organization
		MapList mlPerson = doOrg.getRelatedObjects(context,
													strRelationshipLeadResponsibility,			PropertyUtil.getSchemaProperty(context,"type_Person"),
													DomainConstants.EMPTY_STRINGLIST,
													relStringList,
													false,
													true,
													(short)1,
													"current == " + STATE_PERSON_ACTIVE," attribute["+strAttributeProjectRole+"].value smatch '*"+strRoleName+"*'");
		if(mlPerson!=null && mlPerson.size()>0) {
			Map mPerson = (Map) mlPerson.get(0);
			String strPersonId = (String) mPerson.get("to.id");
			//Creating person object.
			DomainObject doPerson = new DomainObject(strPersonId);
		
			//Checking whether person is already connected to the ECR or not.
			MapList mlECRPerson = doECR.getRelatedObjects(context,
														strRelationshipLeadResponsibility,			PropertyUtil.getSchemaProperty(context,"type_Person"),
														DomainConstants.EMPTY_STRINGLIST,
														relStringList,
														false,
														true,
														(short)1,
														""," to.id match '"+strPersonId+"'");


			DomainRelationship domRel;
			//If person not connected yet.
			if(mlECRPerson.size()==0)
			{
				//Connect person to ECR
				domRel = DomainRelationship.connect(context,doECR,relRelationshipLeadResponsibility,doPerson);
				StringList slAttList = new StringList();
				slAttList.add(strRoleName);
				//Set attribute value
				AttributeUtil.setAttributeList(context,domRel,strAttributeProjectRole,slAttList,false,"~");
			}
			else
			{
				//Get relationship id.
				String newRelId = MqlUtil.mqlCommand(context,"print connection bus $1 to $2 relationship $3 select $4 dump",strECRbjectId,strPersonId,strRelationshipLeadResponsibility,"id");
				domRel = new DomainRelationship(newRelId);
				//Get attribute values
				StringList slAttList = AttributeUtil.getAttributeListValueList(context,domRel,strAttributeProjectRole,"~");
				slAttList.add(strRoleName);
				//Set attribute values.
				AttributeUtil.setAttributeList(context,domRel,strAttributeProjectRole,slAttList,false,"~");
			}
		}
		else
		{
			bstlead = false;
	
			String [] mailArguments = new String [8];
		    mailArguments[0] = "emxEngineeringCentral.Alert.NotALeadRole";
		    mailArguments[1] = "2";
		    mailArguments[2] = "strRoleName";
		    mailArguments[3] = strRoleName;
		    mailArguments[4] = "doOrg.getInfo()";
		    mailArguments[5] = doOrg.getInfo(context,"name");
		    mailArguments[7] = "emxEngineeringCentralStringResource";
		    String strMessage = (String)JPO.invoke(context, "emxMailUtil", new String[]{}, "getMessage", mailArguments, String.class);
			emxContextUtil_mxJPO.mqlNotice(context,strMessage);
		}
		
		return bstlead;
	}


    /**
     * This method gets Lead roles for a person who is in RCO of ECR
     *
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: - The ObjectID of the ECR and Person
     *
     * @throws Exception
     *             if the operation fails.
     * @since EngineeringCental X3.
     */


public String[] getLeadRoles (Context context, String[] args) throws Exception {

     HashMap programMap = (HashMap)JPO.unpackArgs(args);
	 //Getting ECR and Person ids.
	 String sECRObjectId = (String)programMap.get("ECRObjId");
	 String sPersonObjId = (String) programMap.get("PersonObjId");
	 //Getting original names
     String strRelationshipLeadResponsibility = PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility");
	 String strAttributeProjectRole = PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole");

     //Setting Relationship Selectables.
	 StringList slRelSel = new StringList();
     slRelSel.add("attribute["+strAttributeProjectRole+"]");

     // ECR domain object creation.
     DomainObject doECR = new DomainObject(sECRObjectId);
	 // Getting Lead Roles maplist
	 MapList mlPerson = (MapList) doECR.getRelatedObjects(context,strRelationshipLeadResponsibility,PropertyUtil.getSchemaProperty(context,"type_Person"),DomainConstants.EMPTY_STRINGLIST,slRelSel,false,true,(short)1,"","to.id=="+sPersonObjId);
	 Iterator mapListItr = mlPerson.iterator();
     Map mPerson = (Map) mapListItr.next();
	 //Getting leadroles
     String sLeadRole = (String) mPerson.get("attribute["+strAttributeProjectRole+"]");
	 //Getting leadroles in to array
     String sLeadRoles[] = StringUtils.split(sLeadRole, "~");
	 return sLeadRoles;
 }


    /**
     * This method gets Lead Person for an ECR with having given lead role
     *
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: - The ObjectID of the ECR and Lead Role name
     *
     * @throws Exception
     *             if the operation fails.
     * @since EngineeringCental X3.
     */

 public String getLeadPerson(Context context, String[] args) throws Exception {

	 //Getting ECR id and Lead Role values.
	 String sECRObjectId = args[0];
	 String sLeadRole = args[1];
	 //Getting original names
     String strRelationshipLeadResponsibility = PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility");
	 String strAttributeProjectRole = PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole");

	 //Creating ECR DomainObject
	 DomainObject doECR = new DomainObject(sECRObjectId);

	 StringList slRelSel = new StringList();
	 slRelSel.add("attribute["+strAttributeProjectRole+"]");
	 slRelSel.add("to.name");
	//Getting all persons who are under RCO for that Oragnization
	 MapList mlPersons = doECR.getRelatedObjects(context,strRelationshipLeadResponsibility,PropertyUtil.getSchemaProperty(context,"type_Person"),DomainConstants.EMPTY_STRINGLIST,slRelSel,false,true,(short)1,"","");

	 String sPersonName = "";
	 Iterator mapListItr = mlPersons.iterator();
	 if (mlPersons != null)
	 while (mapListItr.hasNext())
	 {
		Map perMap = (Map) mapListItr.next();
		//Check whether this person having lead role given or not.
        if (((String) perMap.get("attribute["+strAttributeProjectRole+"]")).indexOf(sLeadRole) != -1)
		{
			sPersonName = (String) perMap.get("to.name");
		}
	 }

	 return sPersonName;
 }
 /**
     * This method gets Lead Person for an ECR with having given lead role
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the  arguments: - ObjectID of the ECR
     * @throws Exception if the operation fails.
     * @since EngineeringCental X3.
     */
 public String getAffectedOrganizations(Context context, String[] args) throws Exception {

     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     HashMap paramMap = (HashMap) programMap.get("paramMap");
     String strOrgName="";
     //added for the fix 377970
     String strResult="";

     String objectId = (String) paramMap.get("objectId");
     String RELATIONSHIP_AFFECTED_ITEM =
            PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem");
     String RELATIONSHIP_DESIGN_RESPONSIBILITY =
            PropertyUtil.getSchemaProperty(context,"relationship_DesignResponsibility");
     DomainObject objDomain = new DomainObject(objectId);
     StringList objectSelects = new StringList(2);
     objectSelects.addElement(DomainConstants.SELECT_ID);
     objectSelects.addElement(DomainConstants.SELECT_NAME);

     MapList mapPart = objDomain.getRelatedObjects( context,
                                                 RELATIONSHIP_AFFECTED_ITEM,
                                                 DomainConstants.TYPE_PART,
                                                 objectSelects,
                                                 null,
                                                 false,
                                                 true,
                                                 (short)1,
                                                 null,
                                                 null);

     if ( mapPart != null && mapPart.size() > 0 ){
         Iterator itr = mapPart.iterator();
         while (itr.hasNext()){
              Map mPart = (Map) itr.next();
              DomainObject mParts = newInstance(context, (String) mPart.get(SELECT_ID));
              DomainObject objDomainPart = new DomainObject(mParts);
              //377970 fix starts
              StringList  sList=objDomainPart.getInfoList(context,"to["+RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.name");
              if(sList.size()>0){
                  Iterator slOrgItr=sList.iterator();
                  while(slOrgItr.hasNext()){
                      strOrgName=(String)slOrgItr.next();
                      if(!"".equals(strResult) && (strResult.indexOf(strOrgName) == -1)){
                          strResult=strResult+","+strOrgName;
                      }else{
                          strResult=strOrgName;
                      }
                  }
              }
              // //377970 fix ends
          }
     }
    return strResult;
 }


    /**
     * To set the lead roles for persons.
     * This method connects ECR and RCO with Change Responsibility relationship
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: - The ObjectID of the ECR and Oragnization
     *
     * @throws Exception
     *             if the operation fails.
     * @since EngineeringCental X3.
     */


 public void updateChangeResponsibility (Context context, String[] args) throws Exception {
     try{
         HashMap programMap = (HashMap) JPO.unpackArgs(args);
         HashMap paramMap = (HashMap) programMap.get("paramMap");
         String strObjId = (String) paramMap.get("objectId");
         String RELATIONSHIP_CHANGE_RESPONSIBILITY = PropertyUtil.getSchemaProperty(context,"relationship_ChangeResponsibility");
         String strNewOrganizationId = (String) paramMap.get("New OID"); //"New Value");
          if(strNewOrganizationId == null || "".equals(strNewOrganizationId)){
             strNewOrganizationId = (String) paramMap.get("New Value");
         }
         Map requestMap = (Map)programMap.get("requestMap");

         String [] oldChangeResponsibilityIds = (String[])requestMap.get("OLDChangeResponsibilityID");
         String [] oldChangeResponsibilityRelIds = (String[])requestMap.get("ChangeResponsibilityRELID");

         String strOldChangeResponsibilityIds = "";
         String strOldChangeResponsibilityRelIds = "";
         //fix for bug 317972
                 boolean contextPushed = false;
         String ctxUser = context.getUser();
         MqlUtil.mqlCommand(context,"set env $1 $2","APPREALUSER",ctxUser);
         //end of fix

         if(oldChangeResponsibilityIds != null && oldChangeResponsibilityIds.length > 0)
         {
           strOldChangeResponsibilityIds = oldChangeResponsibilityIds[0];
           strOldChangeResponsibilityRelIds = oldChangeResponsibilityRelIds[0];
         }
         //fix for bug 317972
         try
         {
           ContextUtil.pushContext(context);
           contextPushed = true;
         //end of fix
           if(!strOldChangeResponsibilityIds.equals(strNewOrganizationId))
           {
             StringList strListOldChangeResponsibilityIds = FrameworkUtil.split(strOldChangeResponsibilityIds,",");
             StringList strListOldChangeResponsibilityRelIds = FrameworkUtil.split(strOldChangeResponsibilityRelIds,",");

             int newOIDIndex = strListOldChangeResponsibilityIds.indexOf(strNewOrganizationId);
             int size = strListOldChangeResponsibilityIds.size();

             for(int i = 0 ; i < size ; i++)
             {
               if(i != newOIDIndex || "".equals(strNewOrganizationId))
               {
                 //fix for bug 317972
                 // Get the Project Space type
                 //end
                 DomainRelationship.disconnect(context,(String)strListOldChangeResponsibilityRelIds.get(i));
                 //fix for bug 317972
               }
             }
             if(newOIDIndex == -1 && !"".equals(strNewOrganizationId))
             {
               // connect the ecr object to new ChangeResponsibility
               setId(strNewOrganizationId);
               DomainRelationship.connect(context,this,RELATIONSHIP_CHANGE_RESPONSIBILITY,new DomainObject(strObjId));
               //fix for bug 317972
             }
           }
         }
         catch (Exception exp)
         {
           throw new FrameworkException(exp);
         }
         finally
         {
           if( contextPushed)
           {
             ContextUtil.popContext(context);
           }
             //end
         }
         }catch(Exception ex){
     throw  new Exception((String)ex.getMessage());
     }

	}

	/* This method "getECRAffectedItemsDispCodes" gets DispCodes of the ECR Affected Item.
	 * @param context The ematrix context of the request.
	 * @programMap args This string array contains following arguments:
	 *          0 - The programMap
	 *
	 * @throws Exception
	 * @throws FrameworkException
	 * @since EngineeringCentral X3
	 */
	public Vector getECRAffectedItemsDispCodes(Context context, String[] args) throws Exception
    {

		Vector vECRAffectedItemsDispCodes	= new Vector();
		HashMap programMap		= (HashMap)JPO.unpackArgs(args);
		MapList objectList		= (MapList)programMap.get("objectList");
		Iterator itrML = objectList.iterator();
		while(itrML.hasNext())
			{
				Map mAffectedItem		= (Map) itrML.next();

				String sAIObjectId		= (String)mAffectedItem.get(DomainConstants.SELECT_ID);
				String sAIRelId			= (String)mAffectedItem.get(DomainConstants.SELECT_RELATIONSHIP_ID);

				DomainObject sObject = new DomainObject(sAIObjectId);

				if(sObject.isKindOf(context, DomainConstants.TYPE_PART) && (sAIObjectId != null && !"null".equals(sAIObjectId) && sAIObjectId.trim().length() > 0))
				{
					String strFieldReturn	= DomainRelationship.getAttributeValue(context, sAIRelId, DomainRelationship.ATTRIBUTE_DISPOSITION_FIELD_RETURN);
					String strOnOrder		= DomainRelationship.getAttributeValue(context, sAIRelId, DomainRelationship.ATTRIBUTE_DISPOSITION_ON_ORDER);
					String strInProcess		= DomainRelationship.getAttributeValue(context, sAIRelId, DomainRelationship.ATTRIBUTE_DISPOSITION_IN_PROCESS);
					String strInStock		= DomainRelationship.getAttributeValue(context, sAIRelId, DomainRelationship.ATTRIBUTE_DISPOSITION_IN_STOCK);
					String strInField		= DomainRelationship.getAttributeValue(context, sAIRelId, DomainRelationship.ATTRIBUTE_DISPOSITION_IN_FIELD);

					StringBuffer bufDispCodes = new StringBuffer();
					bufDispCodes.append(DomainRelationship.ATTRIBUTE_DISPOSITION_FIELD_RETURN);
					bufDispCodes.append(" :");
					bufDispCodes.append(strFieldReturn);
				    bufDispCodes.append('\n');
					bufDispCodes.append(DomainRelationship.ATTRIBUTE_DISPOSITION_ON_ORDER);
					bufDispCodes.append(" :");
					bufDispCodes.append(strOnOrder);
				    bufDispCodes.append('\n');
					bufDispCodes.append(DomainRelationship.ATTRIBUTE_DISPOSITION_IN_PROCESS);
					bufDispCodes.append(" :");
					bufDispCodes.append(strInProcess);
				    bufDispCodes.append('\n');
					bufDispCodes.append(DomainRelationship.ATTRIBUTE_DISPOSITION_IN_STOCK);
					bufDispCodes.append(" :");
					bufDispCodes.append(strInStock);
				    bufDispCodes.append('\n');
					bufDispCodes.append(DomainRelationship.ATTRIBUTE_DISPOSITION_IN_FIELD);
					bufDispCodes.append(" :");
					bufDispCodes.append(strInField);
					vECRAffectedItemsDispCodes.add(bufDispCodes.toString());
				}
				else
				{
					vECRAffectedItemsDispCodes.add("");
				}

	        }
		return vECRAffectedItemsDispCodes;
    }

	/* This method "getAffectedItemsRelatedECOs" gets Related ECOs of the Affected Item.
	 * @param context The ematrix context of the request.
	 * @programMap args This string array contains following arguments:
	 *          0 - The programMap
	 *
	 * @throws Exception
	 * @throws FrameworkException
	 * @since EngineeringCentral X3
	 */
	public Vector getAffectedItemsRelatedECOs(Context context, String[] args)throws Exception {
		Vector vAffectedItemsRelatedECOs = new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList = (HashMap) programMap.get("paramList");
		String strSuiteDir = (String) paramList.get("SuiteDirectory");
		String strJsTreeID = (String) paramList.get("jsTreeID");
		String strParentObjectId = (String) paramList.get("objectId");
		String strFullName = null;
		StringList slIds = new StringList();

		String strDest = ""; // IR-037806
		StringList objectSelects1 = new StringList();

		//Modified for HF-161480 start
		objectSelects1.add("to["+RELATIONSHIP_AFFECTED_ITEM+"|from.type=="+TYPE_ECO+"].from.from["+RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT+"|to.id == "+ strParentObjectId + "].from.id");
		objectSelects1.add("to["+RELATIONSHIP_AFFECTED_ITEM+"|from.type=="+TYPE_ECO+"].from.from["+RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT+"|to.id == "+ strParentObjectId + "].from.name");

		Iterator itrML = objectList.iterator();
		while (itrML.hasNext()) {
			Map mAffectedItem = (Map) itrML.next();
			String sObjectId = (String) mAffectedItem.get(DomainConstants.SELECT_ID);
			slIds.add(sObjectId);
		}
		String[] AIids = new String[slIds.size()];
		slIds.toArray(AIids);

		MapList objList2 = DomainObject.getInfo(context, AIids, objectSelects1);
		Iterator objItr1 = objList2.iterator();
		while (objItr1.hasNext()) {
			strDest = "";
			Map m = (Map) objItr1.next();

			String name = (String) m.get("to["+RELATIONSHIP_AFFECTED_ITEM+"].from.from["+RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT+"].from.name");
			String id = (String) m.get("to["+RELATIONSHIP_AFFECTED_ITEM+"].from.from["+RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT+"].from.id");
			if((id != null && !("").equals(id) && !("null").equals(id))
					&& (name != null && !("").equals(name) && !("null").equals(name))) {
				String[] strChgId = StringUtils.split(id, "\\a");
				String[] strChgName = StringUtils.split(name, "\\a");
				for (int i = 0; i < strChgId.length; i++) {
					if (strChgId == null) {
						vAffectedItemsRelatedECOs.add("");
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
								+ XSSUtil.encodeForURL(context,strChgId[i])
								+ "', 'null', 'null', 'false', 'content')\" class=\"object\">"
								+ XSSUtil.encodeForHTML(context,strChgName[i]) + "</A>";
						strDest += strFullName;
				}
			}
		}
vAffectedItemsRelatedECOs.add(strDest);
}
	//Modified for HF-161480 end
	return vAffectedItemsRelatedECOs;
}


	   /**
      * Creates the ECR summary report in HTML format
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds the following input arguments:
      * 0 - String containing the ECR id.
      * @return int tells whether PDF is generated and checked into the object. 0-success 1-failure.
      * @throws Exception if the operation fails.
      * @since EngineeringCental X3
      */
 public  String generateHtmlSummaryReport(Context context,String args[]) throws Exception {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }
        StringBuffer summaryReport = new StringBuffer(512);
        try {

         String strLanguage = context.getSession().getLanguage();
         String objectId = args[0];
         ECR ecrObj = null;
         ecrObj = new ECR(objectId);

         java.util.Calendar cal = new GregorianCalendar(TimeZone.getDefault());
         int month = cal.get(Calendar.MONTH);
         int dates = cal.get(Calendar.DATE);
         int year =  cal.get(Calendar.YEAR);
         int hour =  cal.get(Calendar.HOUR);
         int minute = cal.get(Calendar.MINUTE);
         int AM_PM = cal.get(Calendar.AM_PM);
         String[] monthDesc = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
         String[] AMPM = new String[]{"AM","PM"};
         String smonth = monthDesc[month];
         String sAMPM = AMPM[AM_PM];
         String dateAndTime =  smonth+" "+dates+","+year+","+hour+":"+minute+" "+sAMPM;

     //Summary Report Heading
     summaryReport.append("<html>");
     summaryReport.append("<div id=\"pageHeader\">");
     summaryReport.append("<table border=\"0\" width=\"100%\">");
     summaryReport.append("<tr><td class=\"pageHeader\"><h1>"+ecrObj.getInfo(context,SELECT_NAME)+" : "+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.SummaryReport",strLanguage)+"</h1></td></tr>");
     summaryReport.append("<tr><td class=\"pageSubtitle\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Generated",strLanguage)+" "+dateAndTime+"</td></tr>");
     summaryReport.append("</table>");
     summaryReport.append("</div>");

     // Basic Attributes section display
     summaryReport.append("<table width=\"100%\"> <tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Attributes",strLanguage)+"</h2></td></tr></table>");
     summaryReport.append(getECRBasicInfo(context,args));

     // Approvals Display
     Boolean boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.Approvals");
     if(boolObj.booleanValue()) {
      summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.Approvals",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getECRApprovals(context,args));
     }
     // Routes Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.Routes");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"> <tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Routes",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getECRRoutes(context,args));
     }
     //Tasks Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.Tasks");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"> <tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Tasks",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getECRTasks(context,args));
     }
     // AffectedItems(Parts) Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.AffectedItems");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECRAffectedItems",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getECRAffectedPartsDetails(context,args));
     }
     // AffectedItems(Specs) Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.AffectedItems");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECRAffectedSpecs",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getECRAffectedSpecsDetails(context,args));
     }
     // Supporting documents Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.SupportingDocs");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.SupportingDocs",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getECRSupportingDocsDetails(context,args));
     }
	 // Reference documents Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.ReferenceDocs");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ReferenceDocs",strLanguage)+"</h2></td></tr></table>");
      summaryReport.append(getECRReferenceDocsDetails(context,args));
     }
     // Related ECOs Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.RelatedECOs");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.RelatedECOs",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECRsRelatedECODetails(context,args));
     }
     //Related Assignes Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.Assignees");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.Assignees",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getAssigneesOfECR(context,args));
     }
     //Related Markups Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.RelatedMarkups");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.RelatedMarkups",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECRRelatedBOMMarups(context,args));
     }
	 // for bug 344292 starts
     //Related ResolvedItems Display
     boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.ResolvedItems");
     if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.RelatedResolvedItems",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECRRelatedResolvedItems(context,args));
     }
	 // for bug 344292 ends
     //Close tags
     summaryReport.append("</html>");
     } catch (Exception e) {
      throw e;
     }

     return summaryReport.toString();

     }

	 /**
     * Get HTML table information(Basic Informtaion) for the ECR.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @return String Attributes in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since EngineeringCentral X3
    */
    public  String getECRBasicInfo(Context context,String args[]) throws Exception {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }
        String strLanguage        =  context.getSession().getLanguage();
        String objectId = args[0];
        StringBuffer returnString = new StringBuffer(2048);
        String relLeadRes = PropertyUtil.getSchemaProperty(context,"relationship_LeadResponsibility");
        String relChangeRes = PropertyUtil.getSchemaProperty(context,"relationship_ChangeResponsibility");
		String sRelObjectRoute = PropertyUtil.getSchemaProperty(context,"relationship_ObjectRoute");
		String sRelECDistributionList = PropertyUtil.getSchemaProperty(context,"relationship_ECDistributionList");
        setId(objectId);
        String vaultName = null;

        try
        {
//            person = com.matrixone.apps.common.Person.getPerson(context);
        }
        catch (Exception e)
        {
            // IR-013341
            vaultName = PersonUtil.getDefaultVault(context);
        }
        BusinessType ecrBusType = new BusinessType(TYPE_ECR, new Vault(vaultName));
        String defaultVal = "Unassigned";
        String Unassigned = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Unassigned",strLanguage);

        String attrName  = null;
        StringList objectSelects = new StringList();
        objectSelects.add(SELECT_NAME);
        objectSelects.add(SELECT_TYPE);
        objectSelects.add(SELECT_REVISION);
        objectSelects.add(SELECT_CURRENT);
        objectSelects.add(SELECT_OWNER);
        objectSelects.add(SELECT_ORIGINATED);
        objectSelects.add(SELECT_ORIGINATOR);
        objectSelects.add(SELECT_DESCRIPTION);
        objectSelects.add(SELECT_MODIFIED);
        objectSelects.add(SELECT_VAULT);
        objectSelects.add(SELECT_POLICY);
        objectSelects.add("to["+relChangeRes+"].from.name");
        objectSelects.add("from["+relLeadRes+"].to.name");
		objectSelects.add("from["+sRelObjectRoute+"].to.name");
		objectSelects.add("from["+sRelECDistributionList+"].to.name");

        AttributeTypeList ecrAttrList = ecrBusType.getAttributeTypes(context);
        Iterator ecrAttrListItr = ecrAttrList.iterator();

         while (ecrAttrListItr.hasNext()){
                 attrName = ((AttributeType) ecrAttrListItr.next()).getName();
               //Start IR-010312
                if (attrName.equals(PropertyUtil.getSchemaProperty(context,"attribute_ReviewersComments"))||attrName.equals(PropertyUtil.getSchemaProperty(context,"attribute_GeneralDescriptionofChange"))){
                 ecrAttrListItr.remove();
                 }
               //END IR-010312
                 objectSelects.add("attribute["+attrName+"]");
          }

        objectSelects.add(SELECT_POLICY);

        Map attributeMap = getInfo(context, objectSelects);

        String attrValue = null;
        String actiondateoriginated = getFormatDate((String)attributeMap.get(SELECT_ORIGINATED));
        String actiondateModified = getFormatDate((String)attributeMap.get(SELECT_MODIFIED));
        String ecrDesc = (String)attributeMap.get(SELECT_DESCRIPTION);
        ecrDesc = FrameworkUtil.findAndReplace(ecrDesc,"\n","<br>");
        /* below html table contains two columns (tds). First contains basics and second contains ECR related and other attributes */
        returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"2\" >");
        returnString.append("<tr>");
        returnString.append("<td><table>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</strong>:&nbsp;</td><td>"+attributeMap.get(SELECT_NAME)+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</strong>:&nbsp;</td><td>"+attributeMap.get(SELECT_TYPE)+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+"</strong>:&nbsp;</td><td>"+i18nNow.getStateI18NString((String)attributeMap.get(SELECT_POLICY),(String)attributeMap.get(SELECT_CURRENT),strLanguage)+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</strong>:&nbsp;</td><td>"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_OWNER)))+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originator",strLanguage)+"</strong>:&nbsp;</td><td>"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_ORIGINATOR)))+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originated",strLanguage)+"</strong>:&nbsp;</td><td>"+actiondateoriginated+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Modified",strLanguage)+"</strong>:&nbsp;</td><td>"+actiondateModified+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</strong>:&nbsp;</td><td>"+ecrDesc+"</td></tr>");
        returnString.append("</table></td>");
        returnString.append("<td><table>");

        //Displaying the ECR specific attributes
        ecrAttrListItr = ecrAttrList.iterator();
        ecrBusType.close(context);
        while (ecrAttrListItr.hasNext())
        {
          attrName = ((AttributeType) ecrAttrListItr.next()).getName();
          attrValue = (String)attributeMap.get("attribute[" + attrName + "]");
          if((ECO.ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER).equals(attrName)||
              (ECR.ATTRIBUTE_CATEGORY_OF_CHANGE).equals(attrName)) {
             attrValue = (attrValue.equals(defaultVal))?Unassigned:attrValue;
          }

       if(!(attrName.equals(ATTRIBUTE_ORIGINATOR)
               ||attrName.equals(PropertyUtil.getSchemaProperty(context,"attribute_EndDate"))
               ||attrName.equals(PropertyUtil.getSchemaProperty(context,"attribute_isDeviation"))
               ||attrName.equals(PropertyUtil.getSchemaProperty(context,"attribute_StartDate"))
               ||attrName.equals(PropertyUtil.getSchemaProperty(context,"attribute_ReasonForCancel"))
               ))
          {
             returnString.append("<tr>");
             returnString.append("<td><strong>"+i18nNow.getAttributeI18NString(attrName, strLanguage)+"</strong>&nbsp;</td>");
             if(!attrValue.equals(Unassigned) && ((ECO.ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER).equals(attrName))) {
                returnString.append("<td>"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,attrValue)+"&nbsp;</td>");
             }else{
              if(attrName.equals(ECR.ATTRIBUTE_REASON_FOR_CHANGE)
                      ||attrName.equals(ECR.ATTRIBUTE_GENERAL_DESCRIPTION_OF_CHANGE)
                      ||attrName.equals(ECO.ATTRIBUTE_REASON_FOR_CANCEL)
                      ||attrName.equals(PropertyUtil.getSchemaProperty(context,"attribute_ReviewersComments")
                      ))
              {
                  attrValue = FrameworkUtil.findAndReplace(attrValue,"\n","<br>");
              }
              //added to fix IR-091260
              if(!"Unassigned".equals(attrValue))
              {
                returnString.append("<td>"+i18nNow.getRangeI18NString(attrName, attrValue, strLanguage)+"&nbsp;</td>");
              }
              else
              {
        	  returnString.append("<td>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Unassigned", strLanguage)+"&nbsp;</td>");
              }

             }
             returnString.append("</tr>");
          }
        }

        String sDesignResponsibility = (String)attributeMap.get("to["+relChangeRes+"].from.name");
        String sLeadResRel = (String)attributeMap.get("from["+relLeadRes+"].to.name");

        if(sDesignResponsibility == null || "null".equals(sDesignResponsibility)){
            sDesignResponsibility = "";
        }

        String sAttribProjectRole = PropertyUtil.getSchemaProperty(context,"attribute_ProjectRole");
        StringList relSelect = new StringList();
        relSelect.add("attribute["+sAttribProjectRole+"]");
        Map mp = getRelatedObject(context,relLeadRes,true,null,relSelect);
        String sAttibProjectRoleValue = null;
        if((mp!=null)){
	        sAttibProjectRoleValue = (String)mp.get("attribute["+sAttribProjectRole+"]");
            //start : bug 353825
        	if(sAttibProjectRoleValue.indexOf('~') != -1){
	        	String[] roles = StringUtils.split(sAttibProjectRoleValue,"~");
                for(int i = 0;i <  roles.length;i++){
                    if(roles[i].equalsIgnoreCase("role_ECRCoordinator")){
                        sAttibProjectRoleValue = roles[i];
                        break;
                    }
                }
            }
            //end : bug 353825
        }
        if(sAttibProjectRoleValue!=null && sAttibProjectRoleValue.length()>0){
            sAttibProjectRoleValue = PropertyUtil.getSchemaProperty(context,sAttibProjectRoleValue);
        }else{
            sAttibProjectRoleValue = "";
        }

        if(sAttibProjectRoleValue!=null && sLeadResRel!=null){
        returnString.append("<tr><td><strong>"+sAttibProjectRoleValue+"</strong>:&nbsp;</td><td>"+sLeadResRel+"</td></tr>");
        }

	 String strReviewalRoute = EMPTY_STRING;
	 StringList slReviewalRoute = new StringList();
	 Object objReviewalRoute = attributeMap.get("from["+sRelObjectRoute+"].to.name");
	 if (objReviewalRoute instanceof StringList) {
			slReviewalRoute = (StringList) objReviewalRoute;
			strReviewalRoute = (String)slReviewalRoute.get(0);
	 } else if (objReviewalRoute instanceof String) {
			strReviewalRoute = (String)objReviewalRoute;
	 }

     String strApprovalRoute = (String)attributeMap.get("from["+sRelECDistributionList+"].to.name");
     if(strApprovalRoute==null || "null".equals(strApprovalRoute)){
         strApprovalRoute="";
     }

     String sVault = (String)attributeMap.get(SELECT_VAULT);
     String sPolicy = (String) attributeMap.get(SELECT_POLICY);
     if(null!=sVault && null!=strLanguage)
   	    sVault = i18nNow.getAdminI18NString("Vault", sVault, strLanguage);

     if(null!=sPolicy && null!=strLanguage)
   	    sPolicy = i18nNow.getAdminI18NString("Policy", sPolicy, strLanguage);


        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Label.ChangeResponsibility",strLanguage)+"</strong>:&nbsp;</td><td>"+sDesignResponsibility+"</td></tr>");
        returnString.append("<tr><td><strong>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Policy",strLanguage)+"</strong>:&nbsp;</td><td>"+sPolicy+"</td></tr>");
        //Multitenant
          returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(),"emxComponents.Form.Label.ReviewerList")+"</strong>:&nbsp;</td><td>"+strReviewalRoute+"</td></tr>");

	 //IR-009780V6R2011
        //Multitenant
          returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(),"emxComponents.Common.DistributionList")+":</td><td class=\"inputField\">"+strApprovalRoute+"</td></tr>");

        returnString.append("</table></td>");
        returnString.append("</tr>");
        returnString.append("</table>");
        String finalStr = returnString.toString();
        return finalStr;
    }


	/**
     * Constructs the HTML table of the Approvals related to this ECR.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @return String related approvals in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since EngineeringCentral X3
    */

public  String getECRApprovals(Context context,String args[]) throws Exception{

    if (args == null || args.length < 1) {
          throw (new IllegalArgumentException());
    }

    String strLanguage = context.getSession().getLanguage();
    String objectId = args[0];

    boolean bRouteSize         =false;
    boolean bSign              =false;

    MapList mpListOfTasks     = new MapList();
    Route routeObj = (Route)DomainObject.newInstance(context,TYPE_ROUTE);
    setId(objectId);
    MapList stateRouteList = getApprovalsInfo(context);

    String sRouteName = "";
    String sRouteStatus = "";
    String sPersonName             = "";
    String routeNodeStatus         = "";
    String routeNodeComments       = "";

    Hashtable memberMap = new Hashtable();

    StringList strListRouteCheck = new StringList();
    String sRouteId = null;

     SelectList objSelects  = new SelectList();
     objSelects.addElement(Route.SELECT_COMMENTS);
     objSelects.addElement(Route.SELECT_APPROVAL_STATUS);
     objSelects.addElement("from["+RELATIONSHIP_PROJECT_TASK+"].to.name");

    StringBuffer returnString=new StringBuffer(1024);
    returnString.append(" <table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
    returnString.append("<tr>  <th class=\"heading1\" nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage));
    returnString.append("</th> <th class=\"heading1\"  nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Route",strLanguage));
    returnString.append("</th>  <th class=\"heading1\" nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Signer",strLanguage));
    returnString.append("</th> <th class=\"heading1\" nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Status",strLanguage));
    returnString.append("</th> <th class=\"heading1\" nowrap=\"nowrap\">");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage));
    returnString.append("</th> </tr>");

    Iterator mapItr = stateRouteList.iterator();
    while(mapItr.hasNext())
    {
        Map stateRouteMap = (Map)mapItr.next();

      boolean hasRoutes = false;
      // Check for State Name and Ad Hoc routes
      String sStateName = (String)stateRouteMap.get(SELECT_NAME);
     // Check for Routes
     Vector routes = new Vector();
    if (sStateName != null) {
            routes = (Vector)stateRouteMap.get(KEY_ROUTES);
        if((!routes.isEmpty())) {
           hasRoutes = true;
        }
    }
    if ("Ad Hoc Routes".equals(sStateName)) {
        sStateName = "Ad Hoc";
      }
      // Check for Routes
      routes = (Vector)stateRouteMap.get(KEY_ROUTES);


      if (hasRoutes) {

    for (int rteCnt = 0; rteCnt < routes.size(); rteCnt++) {
           bRouteSize=true;
           sRouteId = (String)routes.get(rteCnt);

           if(!strListRouteCheck.contains(sRouteId)){
               strListRouteCheck.add(sRouteId);
           returnString.append("<tr >");
           if ((rteCnt == 0)) {
               //to fix IR-091260V6R2012
               sStateName = FrameworkUtil.findAndReplace(sStateName," ", "_");
             //Multitenant
                 returnString.append("<td valign=\"top\" class=\"listCell\" style=\"text-align: \" >"+EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.State.ECR."+sStateName)+"&nbsp;</td>");
           }
           else {
             returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">&nbsp;</td>");
           }
           if(sRouteId != null && !"null".equals(sRouteId) && !"".equals(sRouteId))
           {
              routeObj.setId(sRouteId);
              sRouteName = routeObj.getInfo(context, SELECT_NAME);
              sRouteStatus = routeObj.getAttributeValue(context,PropertyUtil.getSchemaProperty(context,"attribute_RouteStatus"));

              mpListOfTasks = routeObj.getRouteTasks(context, objSelects, null, "",false);

           }

           returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sRouteName+"&nbsp;</td>");

           String strRouteStatus = i18nNow.getRangeI18NString("", "Not Started",strLanguage);
           String sRoute = "";
           if(sRouteStatus!=null && !sRouteStatus.equals(strRouteStatus)){
       for(int k = 0; k < mpListOfTasks.size() ; k++)
           {
              memberMap = (Hashtable) mpListOfTasks.get(k);

              sPersonName             = (String) memberMap.get("from["+RELATIONSHIP_PROJECT_TASK+"].to.name");
              routeNodeStatus         = (String) memberMap.get(Route.SELECT_APPROVAL_STATUS);
              routeNodeComments       = (String) memberMap.get(Route.SELECT_COMMENTS);

              if(sPersonName == null || "null".equals(sPersonName) || "".equals(sPersonName)){
                  sPersonName = " ";
              }
              sPersonName = PersonUtil.getFullName(context, sPersonName);
           }
           }
              returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sPersonName+"&nbsp;</td>");
            //to fix IR-091260V6R2012
        	   if(!"".equals(routeNodeStatus)) {//IR-141187V6R2013
        		 //Multitenant
          		   sRoute =EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Approval_Status."+routeNodeStatus);
        	   }
              returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sRoute+"&nbsp;</td> <td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+routeNodeComments+"&nbsp;</td></tr>");
            }
           sPersonName="";
           routeNodeStatus="";
           routeNodeComments="";
           }
        }
    }
    if (!bRouteSize && !bSign)
    {
	//to fix IR-091260V6R2012
       returnString.append("<tr><td class=\"even\" style=\"text-align: \" colspan=6 align=\"left\" > "+ EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.SummaryReport.NoSignOrRoutes", strLanguage)+"</td></tr>");
    }

    returnString.append("</table>");
    return returnString.toString();
  }

/**
    * Get the list of Routes.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds the following input arguments:
    * 0 - String containing the ECR id.
    * @return String containing route details in html format.
    * @throws Exception if the operation fails.
    * @since 10.5.
    */

    public String getECRRoutes(Context context, String[] args)
      throws Exception, MatrixException
   {
      try
      {
        String strLanguage = context.getSession().getLanguage();
        String objectId = args[0];
    Route routeObj = (Route)DomainObject.newInstance(context,TYPE_ROUTE);
    String routeStatusAttrSel      = "attribute["+ DomainConstants.ATTRIBUTE_ROUTE_STATUS +"]";
    SelectList selectStmts = new SelectList();
    selectStmts.addName();
    selectStmts.addDescription();
    selectStmts.addCurrentState();
    selectStmts.add(routeStatusAttrSel);
    selectStmts.addOwner();
    selectStmts.addId();
    selectStmts.addPolicy();
    selectStmts.addElement(Route.SELECT_ACTUAL_COMPLETION_DATE);
    selectStmts.addElement(Route.SELECT_SCHEDULED_COMPLETION_DATE);

    StringBuffer routeInfo = new StringBuffer(1024);
    routeInfo.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
    routeInfo.append("<tr><th width=\"5%\" style=\"text-align:center\"><img border=\"0\" src=\"../common/images/iconStatus.gif\" name=\"imgstatus\" id=\"imgstatus\" alt=\"*\"></th>");

    routeInfo.append("<th nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
    routeInfo.append("<th nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
    routeInfo.append("<th nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Status",strLanguage)+"</th>");
    routeInfo.append("<th nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Routes.ScheduleCompDate",strLanguage)+"</th>");
    routeInfo.append("<th nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</th>");

    MapList totalResultList = routeObj.getRoutes(context, objectId, selectStmts, null, null, false);

	totalResultList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
	totalResultList.sort();

	Iterator itr = totalResultList.iterator();
    String routeId;
    String scheduledCompletionDate = "";
    boolean isYellow = false;
    String sCode = "";
    String routeIcon = "";
    Date curDate = new Date();
    String routeState = "";
    while(itr.hasNext()) {
      Map routeMap = (Map)itr.next();
      routeId = (String)routeMap.get(DomainConstants.SELECT_ID);
            routeState = (String)routeMap.get(DomainConstants.SELECT_CURRENT);
      routeObj.setId(routeId);
            scheduledCompletionDate = routeObj.getSheduledCompletionDate(context);
      if(scheduledCompletionDate != null && !"".equals(scheduledCompletionDate))
      {
        Date dueDate = new Date();
        dueDate = eMatrixDateFormat.getJavaDate(scheduledCompletionDate);
        if ( dueDate != null && ( curDate.after(dueDate)) && (!(routeState.equals("Complete")))) {
          sCode = "Red";
        }
      }

        isYellow = false;
        if (!"Red".equals(sCode)) {
        MapList taskList = routeObj.getRouteTasks(context, selectStmts, null, null, false);

        // check for the status of the task.
        Map taskMap = null;
        for(int j = 0; j < taskList.size(); j++) {
          taskMap = (Map) taskList.get(j);
          String sState         = (String) taskMap.get(DomainConstants.SELECT_CURRENT);
          String CompletionDate = (String) taskMap.get(Route.SELECT_SCHEDULED_COMPLETION_DATE);
          String actualCompletionDate = (String) taskMap.get(Route.SELECT_ACTUAL_COMPLETION_DATE);

          Date dueDate = new Date();
          if( CompletionDate!=null && !"".equals(CompletionDate)) {
          dueDate = eMatrixDateFormat.getJavaDate(CompletionDate);
          }

          if ("Complete".equals(sState)) {
          Date dActualCompletionDate = new Date(actualCompletionDate);
          if (dActualCompletionDate.after(dueDate)) {
            isYellow = true;
            break;
          }
          } else if (curDate.after(dueDate)) {
          isYellow = true;
          break;
          }
        }

        if(isYellow) {
          sCode = "yellow";
        } else {
          sCode = "green";
        }
            }

            if("Red".equals(sCode)) {
        routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusRed.gif\" name=\"red\" id=\"red\" alt=\"emxComponents.TaskSummary.ToolTipRed\">";
            } else if("green".equals(sCode)) {
                routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusGreen.gif\" name=\"green\" id=\"green\" alt=\"emxComponents.TaskSummary.ToolTipGreen\">";
            } else if("yellow".equals(sCode)) {
        routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusYellow.gif\" name=\"yellow\" id=\"yellow\" alt=\"emxComponents.TaskSummary.ToolTipYellow\">";
            } else {
                routeIcon = "&nbsp;";
      }
          //to fix IR-091260V6R2012
          String sStatusVal = (String)routeMap.get(routeStatusAttrSel);
          sStatusVal = FrameworkUtil.findAndReplace(sStatusVal," ", "_");
          //Multitenant
              String  sStatus = (String)EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Range.Route_Status."+sStatusVal);


      routeInfo.append("<tr>");
      routeInfo.append("<td>"+routeIcon+"</td>");
      routeInfo.append("<td>"+routeMap.get(DomainObject.SELECT_NAME)+"&nbsp;</td>");
      routeInfo.append("<td>"+routeMap.get(DomainObject.SELECT_DESCRIPTION)+"&nbsp;</td>");
      routeInfo.append("<td>"+sStatus+"&nbsp;</td>");
      routeInfo.append("<td>"+scheduledCompletionDate+"&nbsp;</td>");
      routeInfo.append("<td>"+routeMap.get(DomainObject.SELECT_OWNER)+"&nbsp;</td>");
      routeInfo.append("</tr>");
    }

    if(totalResultList.size()==0) {
      routeInfo.append("<tr><td colspan=\"6\">").append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.NoObjectsFound",strLanguage)).append("</td></tr>");
    }
    routeInfo.append("</table>");
    return routeInfo.toString();
      }
      catch (Exception ex)
      {
        throw ex;
      }
    }
	 /**
     * Gets the list of Routes in HTML table format.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @return String Html table format representation of Routes info.
     * @throws Exception if the operation fails.
     * @since 10.5.
     */

    public String getECRTasks(Context context, String[] args)
        throws Exception, MatrixException
    {
        try
        {
          String strLanguage = context.getSession().getLanguage();
          String objectId = args[0];
          Route routeObj = (Route)DomainObject.newInstance(context,TYPE_ROUTE);

          SelectList selectStmts = new SelectList();
          selectStmts.addName();
          selectStmts.addCurrentState();
          selectStmts.addId();
          selectStmts.addOwner();
          String strRouteType = "attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]";
          selectStmts.add(strRouteType);
      StringBuffer routeInfo = new StringBuffer(512);
      routeInfo.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
      routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECSummary.RouteName",strLanguage)+"</th>");
      routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECSummary.TaskName",strLanguage)+"</th>");
      routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</th>");
      routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</th>");
      routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.TaskAssignee",strLanguage)+"</th>");
      routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECSummary.TaskAction",strLanguage)+"</th>");

      MapList totalResultList = routeObj.getRoutes(context, objectId, selectStmts, null, null, false);
      totalResultList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
      totalResultList.sort();

      Iterator itr = totalResultList.iterator();
      String routeId;
      String routeType = "";
      String routeOwner = "";
      String sTaskAssignee  ="";
      String sTitleName  ="";
      String sRouteTaskAction  ="";
      MapList taskList = new MapList();
      while(itr.hasNext()) {
        Map routeMap = (Map)itr.next();
        routeId = (String)routeMap.get(DomainConstants.SELECT_ID);
        routeOwner = (String)routeMap.get(DomainConstants.SELECT_OWNER);
        routeType = (String)routeMap.get(strRouteType);
        routeObj.setId(routeId);

              SelectList strTaskList = new SelectList();
              strTaskList.addName();
              strTaskList.addOwner();
              strTaskList.addCurrentState();
              String strRelName = "from["+RELATIONSHIP_PROJECT_TASK+"].to.name";
              String strTitle = "attribute["+ATTRIBUTE_TITLE+"]";
              String strComments = "attribute["+ATTRIBUTE_COMMENTS+"]";
              //start:bug 357325
              String strRouteInstruction = "attribute["+ATTRIBUTE_ROUTE_INSTRUCTIONS+"]";
              //end:bug 357325
              String strRouteAction = "attribute["+ATTRIBUTE_ROUTE_ACTION+"]";
              strTaskList.add(strRelName);
              strTaskList.add(strTitle);
              strTaskList.add(strComments);
              strTaskList.add(strRouteAction);
              //start:bug 357325
              strTaskList.add(strRouteInstruction);
              //end:bug 357325
              taskList = routeObj.getRouteTasks(context, strTaskList, null, null, false);

          // check for the status of the task.
          Map taskMap = null;
          for(int j = 0; j < taskList.size(); j++) {
            taskMap = (Map) taskList.get(j);
            sTaskAssignee  = (String)taskMap.get(strRelName);
            sTitleName  = (String)taskMap.get(strTitle);
            //start:bug 357325
            sRouteTaskAction = (String)taskMap.get(strRouteInstruction);
            //end:bug 357325
            //to fix IR-091260
          //Multitenant
            String sRouteType =EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Route_Base_Purpose."+routeType);
            routeInfo.append("<tr>");
            routeInfo.append("<td>"+(String)routeMap.get(SELECT_NAME)+"</td>");
            routeInfo.append("<td>"+sTitleName+"</td>");
            routeInfo.append("<td>"+routeOwner+"</td>");
            routeInfo.append("<td>"+sRouteType+"</td>");
            routeInfo.append("<td>"+sTaskAssignee+"</td>");
            routeInfo.append("<td>"+sRouteTaskAction+"</td>");
            routeInfo.append("</tr>");
          }

      }
      if(taskList.size()==0) {
        routeInfo.append("<tr><td colspan=\"6\">").append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.TaskSummary.NoTasksFound",strLanguage)).append("</td></tr>");
      }
      routeInfo.append("</table>");
      return routeInfo.toString();
        }
        catch (Exception ex)
        {
          throw ex;
        }
    }

	/**
     * Constructs the ECR AffectedItem(Parts) HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @return String Connected Revised Parts details in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since Engineering Central X3
     */

    public String getECRAffectedPartsDetails(Context context,String[] args)
             throws Exception
    {
       if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
       }
       String strLanguage = context.getSession().getLanguage();
       String objectId = args[0];
       MapList AffectedPartsList = new MapList();
       ECR ecrObj = null;
       try
       {
         ecrObj = new ECR(objectId);

         SelectList selectStmts = ecrObj.getObjectSelectList(11);
         selectStmts.addElement(DomainObject.SELECT_ID);
         selectStmts.addElement(DomainObject.SELECT_NAME);
         selectStmts.addType();
         selectStmts.addDescription();
         selectStmts.addRevision();
         selectStmts.addCurrentState();
         selectStmts.addElement("policy");

         SelectList selectRelStmts = ecrObj.getRelationshipSelectList(8);
         selectRelStmts.addElement(DomainRelationship.SELECT_ID);
         selectRelStmts.addElement(DomainRelationship.SELECT_NAME);

         String strRelationType = RELATIONSHIP_AFFECTED_ITEM;

         AffectedPartsList= ecrObj.getRelatedObjects(context,
                                                       strRelationType,
                                                       TYPE_PART,
                                                       selectStmts,
                                                       selectRelStmts,
                                                       false,
                                                       true,
                                                       (short)1,
                                                       null,
                                                       null);

         AffectedPartsList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
         AffectedPartsList.sort();
          }catch (FrameworkException Ex) {
             throw Ex;
       }

       StringBuffer returnString=new StringBuffer(1024);
       returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"2\" >");
       returnString.append("<tr><th nowrap>");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage));
       returnString.append("</th> <th width = \"6%\">");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage));
       returnString.append("</th> <th width = \"5%\">");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage));
       returnString.append("</th> <th width = \"4%\">");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Revision",strLanguage));
       returnString.append("</th> <th width = \"4%\">");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage));
       returnString.append("</th> <th width = \"9%\">");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EC.RequestedChangeValue",strLanguage));
       returnString.append("</th> </tr>");

       Iterator mapItr = AffectedPartsList.iterator();

       while(mapItr.hasNext())
       {
         Map mpParts = (Map)mapItr.next();
         //vamsi
         String sAttrReqChange = PropertyUtil.getSchemaProperty(context,
                 "attribute_RequestedChange");
         DomainRelationship domRel= new DomainRelationship((String) mpParts.get(SELECT_RELATIONSHIP_ID));
         String sReqChangeVal = domRel.getAttributeValue(context, sAttrReqChange);
         //vamsi
       //to fix IR-091260V6R2012
        String sReqChange = FrameworkUtil.findAndReplace(sReqChangeVal," ", "_");
      //Multitenant
          sReqChange =EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Requested_Change."+sReqChange);

        String sTypeVal = (String)mpParts.get(DomainConstants.SELECT_TYPE);
        String strType = i18nNow.getAdminI18NString("Type", sTypeVal, strLanguage);

         String sState = i18nNow.getStateI18NString((String)mpParts.get(SELECT_POLICY),(String)mpParts.get(SELECT_CURRENT),strLanguage);
         returnString.append("<tr>");
         returnString.append("<td width = \"12%\" class=\"listCell\" style=\"text-align: \" valign=\"top\"><img src=\"../common/images/iconSmallPart.gif\" border=\"0\" alt=\"Part\">&nbsp;"+mpParts.get(DomainConstants.SELECT_NAME)+"&nbsp;</td>");
         returnString.append("<td width = \"6%\" class=\"listCell\" style=\"text-align: \" valign=\"top\">"+strType+"&nbsp;</td>");
         returnString.append("<td width = \"5%\" class=\"listCell\" style=\"text-align: \" valign=\"top\">"+mpParts.get(DomainConstants.SELECT_DESCRIPTION)+"&nbsp;</td>");
         returnString.append("<td width = \"4%\" class=\"listCell\" style=\"text-align: \" valign=\"top\">"+mpParts.get(DomainConstants.SELECT_REVISION)+"&nbsp;</td>");
         returnString.append("<td width = \"4%\" class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sState+"&nbsp;</td>");
         returnString.append("<td width = \"8%\" class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sReqChange+"&nbsp;</td></tr>");

        String imgPartType = EngineeringUtil.getTypeIconProperty(context, (String)mpParts.get(DomainConstants.SELECT_TYPE));
        if (imgPartType == null || imgPartType.length() == 0 )
        {
          imgPartType = EngineeringUtil.getTypeIconProperty(context, DomainConstants.TYPE_PART);
          if (imgPartType == null || imgPartType.length() == 0 )
          {
              imgPartType = "iconSmallPart.gif";
          }
        }

       }
        if(AffectedPartsList.size()==0) {
            //Start of  IR-030281
            returnString.append("<tr><td colspan=\"12\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.NoAffectedPartsConnected",strLanguage)+"</td></tr>");
       }
       returnString.append("</table>");
       return returnString.toString();
    }

	/**
      * Constructs the ECR Specification details  HTML table.
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds the following input arguments:
      * 0 - String containing the ECR id.
      * @return String Connected Revised parts in the form of HTML table.
      * @throws Exception if the operation fails.
      * @since EngineeringCental X3
      */

      public String getECRAffectedSpecsDetails(Context context,String[] args)
              throws Exception {

        if (args == null || args.length < 1) {
               throw (new IllegalArgumentException());
        }

        String strLanguage = context.getSession().getLanguage();
        String objectId = args[0];
        MapList revisedSpecsList = new MapList();
        ECR ecrObj = null;
        try {
          ecrObj = new ECR(objectId);
          SelectList selectStmts = ecrObj.getObjectSelectList(10);
          selectStmts.addElement(DomainObject.SELECT_ID);
          selectStmts.addElement(DomainObject.SELECT_NAME);
          selectStmts.addRevision();
          selectStmts.addDescription();
          selectStmts.addCurrentState();
          selectStmts.addType();
          selectStmts.addElement("policy");

          SelectList selectRelStmts = ecrObj.getRelationshipSelectList(3);
          selectRelStmts.addElement(DomainRelationship.SELECT_ID);
          selectRelStmts.addElement(DomainRelationship.SELECT_NAME);


          String objPatrn =TYPE_CAD_MODEL + "," +
                              TYPE_CAD_DRAWING+ "," +
                              TYPE_DRAWINGPRINT + "," +
                              PropertyUtil.getSchemaProperty(context,"type_PartSpecification");

          String strRelationType = RELATIONSHIP_AFFECTED_ITEM;

          revisedSpecsList= ecrObj.getRelatedObjects(context,
                                                        strRelationType,
                                                        objPatrn,
                                                        selectStmts,
                                                        selectRelStmts,
                                                        false,
                                                        true,
                                                        (short)1,
                                                        null,
                                                        null);

      revisedSpecsList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
      revisedSpecsList.sort();

     }catch (FrameworkException Ex) {
              throw Ex;
       }
       StringBuffer returnString=new StringBuffer(1024);
       returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"2\" >");
       returnString.append("<tr> <th  nowrap>");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage));
       returnString.append("</th> <th width = \"6%\">");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage));
       returnString.append("</th> <th width = \"5%\">");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage));
       returnString.append("</th> <th width = \"4%\">");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Revision",strLanguage));
       returnString.append("</th> <th width = \"4%\">");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage));
       returnString.append("</th> <th width = \"9%\">");
       returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EC.RequestedChangeValue",strLanguage));
       returnString.append("</th> <th  nowrap>");
       returnString.append("</th> <th>");

       Iterator mapItr = revisedSpecsList.iterator();

       String imgSpecType = "";
       while(mapItr.hasNext())
       {
           Map specMap = (Map)mapItr.next();
           imgSpecType = EngineeringUtil.getTypeIconProperty(context, (String)specMap.get(DomainConstants.SELECT_TYPE));
           if (imgSpecType == null || imgSpecType.length() == 0 )
           {
               imgSpecType = "iconSmallDefault.gif";
           }
           // vamsi
           String sAttrReqChange = PropertyUtil.getSchemaProperty(context,
                   "attribute_RequestedChange");
           DomainRelationship domRel= new DomainRelationship((String) specMap.get(SELECT_RELATIONSHIP_ID));
           String sReqChangeVal = FrameworkUtil.findAndReplace(domRel.getAttributeValue(context, sAttrReqChange)," ", "_");

           String sType = i18nNow.getAdminI18NString("Type",(String)specMap.get(DomainConstants.SELECT_TYPE), strLanguage);
           String sState = i18nNow.getStateI18NString((String)specMap.get(SELECT_POLICY),(String)specMap.get(SELECT_CURRENT),strLanguage);
         //Multitenant
             sReqChangeVal =EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Requested_Change."+sReqChangeVal);
           //vamsi
           returnString.append("<tr>");
           returnString.append("<td width = \"10%\" class=\"listCell\" style=\"text-align: \" valign=\"top\"><img src=\"../common/images/"+imgSpecType+"\" border=\"0\" alt=\"*\">&nbsp;"+specMap.get(DomainConstants.SELECT_NAME)+"&nbsp;</td>");
           returnString.append("<td width = \"15%\" class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sType+"&nbsp;</td>");
           returnString.append("<td width = \"25%\" class=\"listCell\" style=\"text-align: \" valign=\"top\">"+specMap.get(DomainConstants.SELECT_DESCRIPTION)+"&nbsp;</td>");
           returnString.append("<td width = \"10%\" class=\"listCell\" style=\"text-align: \" valign=\"top\">"+specMap.get(DomainConstants.SELECT_REVISION)+"&nbsp;</td>");
           returnString.append("<td width = \"25%\" class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sState+"&nbsp;</td>");
           returnString.append("<td width = \"15%\" class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sReqChangeVal+"&nbsp;</td></tr>");

       }

       if(revisedSpecsList.size()==0) {
           //For the IR-030281
           returnString.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.NoAffectedSpecsConnected",strLanguage)+"</td></tr>");
           //end of IR-030281
       }
       returnString.append("</table>");
       String finalStr = returnString.toString();
       return finalStr;
      }

	 /**
     * Constructs the ECR Supporting Documents HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @return String Connected Supporting documents in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since EnginneringCentral X3
     */

public String getECRSupportingDocsDetails(Context context,String[] args)
             throws Exception
     {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }

        String strLanguage = context.getSession().getLanguage();
        String objectId = args[0];
        MapList SupportDocsList = new MapList();
        ECR ecrObj = null;

        try {
            ecrObj = new ECR(objectId);
            String srelPattern = DomainRelationship.RELATIONSHIP_ECR_SUPPORTING_DOCUMENT;
            SelectList selectStmts = ecrObj.getObjectSelectList(5);
            selectStmts.addElement(DomainObject.SELECT_ID);
            selectStmts.addElement(DomainObject.SELECT_NAME);
            selectStmts.addRevision();
            selectStmts.addCurrentState();
            selectStmts.addDescription();
            selectStmts.addType();
            selectStmts.addElement("policy");

            SelectList selectRelStmts = ecrObj.getRelationshipSelectList(2);
            selectRelStmts.addElement(DomainRelationship.SELECT_ID);
            selectRelStmts.addElement(DomainRelationship.SELECT_NAME);
            SupportDocsList = ecrObj.getRelatedObjects(context,
                                                   srelPattern,
                                                   "*",
                                                   selectStmts,
                                                   selectRelStmts,
                                                   false,
                                                   true,
                                                   (short)1,
                                                   null,
                                                   null);


            if(SupportDocsList!=null){
             SupportDocsList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
             SupportDocsList.sort();
            }
             } catch (FrameworkException Ex) {
             throw Ex;
        }

        StringBuffer returnString=new StringBuffer(1024);
         returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"2\" >");
         returnString.append("<tr>  <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage));
         returnString.append("</th> <th   nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage));
         returnString.append("</th> <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage));
         returnString.append("</th> <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Revision",strLanguage));
         returnString.append("</th> <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage));
         returnString.append("</th> <th nowrap>");
         returnString.append("</th> </tr>");

         Iterator mapItr = SupportDocsList.iterator();
         String imgSpecType = "";
         String sType = null;
         String sState = null;
         while(mapItr.hasNext())
         {
            Map specMap = (Map)mapItr.next();
            imgSpecType = EngineeringUtil.getTypeIconProperty(context, (String)specMap.get(DomainConstants.SELECT_TYPE));
            if (imgSpecType == null || imgSpecType.length() == 0 )
            {
                imgSpecType = "iconSmallDefault.gif";
            }

            sType = i18nNow.getAdminI18NString("Type",(String)specMap.get(DomainConstants.SELECT_TYPE), strLanguage);
            sState = i18nNow.getStateI18NString((String)specMap.get(SELECT_POLICY),(String)specMap.get(SELECT_CURRENT),strLanguage);

            returnString.append("<tr>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\"><img src=\"../common/images/"+imgSpecType+"\" border=\"0\" alt=\"*\">&nbsp;"+specMap.get(DomainConstants.SELECT_NAME)+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sType+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+specMap.get(DomainConstants.SELECT_DESCRIPTION)+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+specMap.get(DomainConstants.SELECT_REVISION)+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sState+"&nbsp;</td></tr>");
         }
         if(SupportDocsList.size()==0) {
                returnString.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ReviewECR.NorelatedSupportingDocumentsarefoundforthis",strLanguage)+"</td></tr>");
         }
         returnString.append("</table>");
         return returnString.toString();
     }

	 /**
     * Constructs the ECR Reference Documents HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @return String Connected Supporting documents in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since EnginneringCentral X3
     */

public String getECRReferenceDocsDetails(Context context,String[] args)
             throws Exception
     {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }

        String strLanguage = context.getSession().getLanguage();
        String objectId = args[0];
        MapList ReferenceDocsList = new MapList();
        ECR ecrObj = null;

        try {
            ecrObj = new ECR(objectId);
            String srelPattern = DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT;
            SelectList selectStmts = ecrObj.getObjectSelectList(5);
            selectStmts.addElement(DomainObject.SELECT_ID);
            selectStmts.addElement(DomainObject.SELECT_NAME);
            selectStmts.addRevision();
            selectStmts.addCurrentState();
            selectStmts.addDescription();
            selectStmts.addType();
            selectStmts.addElement("policy");

            SelectList selectRelStmts = ecrObj.getRelationshipSelectList(2);
            selectRelStmts.addElement(DomainRelationship.SELECT_ID);
            selectRelStmts.addElement(DomainRelationship.SELECT_NAME);
            ReferenceDocsList = ecrObj.getRelatedObjects(context,
                                                   srelPattern,
                                                   "*",
                                                   selectStmts,
                                                   selectRelStmts,
                                                   false,
                                                   true,
                                                   (short)1,
                                                   null,
                                                   null);

             ReferenceDocsList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
             ReferenceDocsList.sort();
             } catch (FrameworkException Ex) {
             throw Ex;
        }

        StringBuffer returnString=new StringBuffer(1024);
         returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"2\" >");
         returnString.append("<tr>  <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage));
         returnString.append("</th> <th   nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage));
         returnString.append("</th> <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage));
         returnString.append("</th> <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Revision",strLanguage));
         returnString.append("</th> <th  nowrap>");
         returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage));
         returnString.append("</th> <th nowrap>");
         returnString.append("</th> </tr>");

         Iterator mapItr = ReferenceDocsList.iterator();
         String imgSpecType = "";
         String sType = null;
         String sState = null;
         while(mapItr.hasNext())
         {
            Map specMap = (Map)mapItr.next();
            imgSpecType = EngineeringUtil.getTypeIconProperty(context, (String)specMap.get(DomainConstants.SELECT_TYPE));
            if (imgSpecType == null || imgSpecType.length() == 0 )
            {
                imgSpecType = "iconSmallDefault.gif";
            }

            sType = i18nNow.getAdminI18NString("Type",(String)specMap.get(DomainConstants.SELECT_TYPE), strLanguage);
            sState = i18nNow.getStateI18NString((String)specMap.get(SELECT_POLICY),(String)specMap.get(SELECT_CURRENT),strLanguage);

            returnString.append("<tr>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\"><img src=\"../common/images/"+imgSpecType+"\" border=\"0\" alt=\"*\">&nbsp;"+specMap.get(DomainConstants.SELECT_NAME)+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sType+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+specMap.get(DomainConstants.SELECT_DESCRIPTION)+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+specMap.get(DomainConstants.SELECT_REVISION)+"&nbsp;</td>");
            returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+sState+"&nbsp;</td></tr>");
         }
         if(ReferenceDocsList.size()==0) {
                returnString.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ReviewECR.NorelatedSupportingDocumentsarefoundforthis",strLanguage)+"</td></tr>");
         }
         returnString.append("</table>");
         return returnString.toString();
     }

	/**
     * Constructs the ECR Related ECOs HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the ECR id.
     * @return String Connected Related ECOs in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since EnginneringCentral X3
     */

     public String getECRsRelatedECODetails(Context context,String[] args)
             throws Exception
     {
        if (args == null || args.length < 1)
        {
              throw (new IllegalArgumentException());
        }

        String strLanguage = context.getSession().getLanguage();
        String objectId = args[0];

        ECR ecrObj = new ECR(objectId);
        SelectList selectStmts = ecrObj.getObjectSelectList(4);
        selectStmts.addElement(DomainObject.SELECT_ID);
        selectStmts.addElement(DomainObject.SELECT_NAME);
        selectStmts.addType();
        selectStmts.addDescription();

        SelectList selectRelStmts = null;
        MapList relatedECOs = ecrObj.getRelatedObjects(context,
                                                       RELATIONSHIP_ECO_CHANGEREQUESTINPUT,
                                                       "*",
                                                       selectStmts,
                                                       selectRelStmts,
                                                       true,
                                                       false,
                                                       (short)1,
                                                       null,
                                                       null);

		relatedECOs.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
		relatedECOs.sort();
        StringBuffer returnString=new StringBuffer(512);
        returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"2\" >");
        returnString.append("<tr>  <th  nowrap>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage));
        returnString.append("</th> <th   nowrap>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage));
        returnString.append("</th> <th  nowrap>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage));
        returnString.append("</th> </tr>");

        if(relatedECOs != null)
        {
            Iterator mapItr = relatedECOs.iterator();
            String imgECOType = "";
            Map ecoMap = null;
            while(mapItr.hasNext())
            {
                ecoMap = (Map)mapItr.next();
                imgECOType = EngineeringUtil.getTypeIconProperty(context, (String)ecoMap.get(DomainConstants.SELECT_TYPE));
                if (imgECOType == null || imgECOType.length() == 0 )
                {
                    imgECOType = "iconSmallDefault.gif";
                }
                returnString.append("<tr>");
                returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\"><img src=\"../common/images/"+imgECOType+"\" border=\"0\" alt=\"*\">&nbsp;"+ecoMap.get(DomainConstants.SELECT_NAME)+"&nbsp;</td>");
                returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+ecoMap.get(DomainConstants.SELECT_TYPE)+"&nbsp;</td>");
                returnString.append("<td class=\"listCell\" style=\"text-align: \" valign=\"top\">"+ecoMap.get(DomainConstants.SELECT_DESCRIPTION)+"&nbsp;</td>");
            }
        }
        if(relatedECOs == null || relatedECOs.size() == 0)
        {
            returnString.append("<tr><td colspan=\"3\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.NoObjectsFound",strLanguage)+"</td></tr>");
        }
        returnString.append("</table>");
        return returnString.toString();
    }

    /**
     * Constructs the ECR related Assignees HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @return String Html table format representation of Related ECRs data.
     * @throws Exception if the operation fails.
     * @since Engineering Central X3
    */

   public String getAssigneesOfECR(Context context,String[] args)
         throws Exception
   {
       String strLanguage = context.getSession().getLanguage();
       String objectId = args[0];
     MapList mpListAssignees = new MapList();
     StringBuffer relatedAssignees = new StringBuffer(1024);
       try
       {
           ECR ecrObj = new ECR(objectId);
           StringList selectStmts = new StringList(1);
           selectStmts.addElement(SELECT_ID);
           selectStmts.addElement(SELECT_NAME);
           StringList selectRelStmts = new StringList();
           selectRelStmts.add(SELECT_ORIGINATED);

           mpListAssignees = ecrObj.getRelatedObjects(context,RELATIONSHIP_ASSIGNED_EC,
               TYPE_PERSON, selectStmts, selectRelStmts,
               true, false, (short) 1,EMPTY_STRING,EMPTY_STRING);

           mpListAssignees.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
           mpListAssignees.sort();
       Iterator objItr = mpListAssignees.iterator();
       Map ecrMap  = null;
       relatedAssignees.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
       relatedAssignees.append("<tr>");
       relatedAssignees.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Person",strLanguage)+"</th>");
       relatedAssignees.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.FirstName",strLanguage)+"</th>");
       relatedAssignees.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.LastName",strLanguage)+"</th>");
       relatedAssignees.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Role",strLanguage)+"</th>");
       relatedAssignees.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.AssingedOn",strLanguage)+"</th>");
       relatedAssignees.append("</tr>");

       while (objItr.hasNext()) {
              ecrMap = (Map)objItr.next();
              Person person = new Person((String)ecrMap.get(SELECT_ID));
              StringList strList = new StringList ();
              strList.add(SELECT_NAME);
              strList.add("attribute["+ATTRIBUTE_FIRST_NAME+"]");
              strList.add("attribute["+ATTRIBUTE_LAST_NAME+"]");
              Map mpPersonInfo = person.getInfo(context,strList);
              relatedAssignees.append("<tr>");
              //Modified for IR-184707V6R2013x start
              relatedAssignees.append("<td><img src=\"../common/images/iconAssignee.gif\" border=\"0\" alt=\"*\">&nbsp;"+mpPersonInfo.get(SELECT_NAME)+"&nbsp;</td>");
              //Modified for IR-184707V6R2013x end
              relatedAssignees.append("<td>"+mpPersonInfo.get("attribute["+ATTRIBUTE_FIRST_NAME+"]")+"&nbsp;</td>");
              relatedAssignees.append("<td>"+mpPersonInfo.get("attribute["+ATTRIBUTE_LAST_NAME+"]")+"&nbsp;</td>");

              StringList strPersonRolesList =person.getRoleAssignments(context);
              Iterator itr = strPersonRolesList.iterator();
              if(itr!=null){
                  StringBuffer sBufList = new StringBuffer();
                  while(itr.hasNext()){
                  sBufList.append("");
                  sBufList.append(i18nNow.getRoleI18NString(PropertyUtil.getSchemaProperty(context,(String)itr.next()),strLanguage));
                  sBufList.append(',');
                  }
              relatedAssignees.append("<td>"+sBufList.toString()+"&nbsp;</td>");
              }else{
                  relatedAssignees.append("<td>&nbsp;</td>");
              }
              relatedAssignees.append("<td>"+getFormatDate((String)ecrMap.get(SELECT_ORIGINATED))+"&nbsp;</td>");

              relatedAssignees.append("</tr>");
       }
       if(mpListAssignees.size()==0) {
           relatedAssignees.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.NoAssigneesConnected",strLanguage)+"</td></tr>");
       }
       }
       catch (FrameworkException Ex)
       {
            throw Ex;
       }
       relatedAssignees.append("</table>");
       return relatedAssignees.toString();
		}

	/**
      * Constructs the ECR related Markups HTML table.
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds the following input arguments:
      * 0 - String containing object id.
      * @return String Html table format representation of Related ECRs data.
      * @throws Exception if the operation fails.
      * @since Engineering Central X3
     */

    public String getECRRelatedBOMMarups(Context context,String[] args)
          throws Exception
    {
        String strLanguage = context.getSession().getLanguage();
        String objectId = args[0];
        MapList mpListMarkups = new MapList();
        StringBuffer relatedMarkups = new StringBuffer(512);
        try
        {
            ECR ecrObj = new ECR(objectId);
            StringList selectStmts = new StringList(1);
            selectStmts.addElement(SELECT_ID);
        selectStmts.addElement(SELECT_NAME);
        selectStmts.addElement(SELECT_ORIGINATED);
        selectStmts.addElement(SELECT_OWNER);
        selectStmts.addElement(SELECT_MODIFIED);
        String relpat = PropertyUtil.getSchemaProperty(context,"type_BOMMarkup")+","+PropertyUtil.getSchemaProperty(context,"type_ItemMarkup");
        StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
            mpListMarkups = ecrObj.getRelatedObjects(context,
               PropertyUtil.getSchemaProperty(context,"relationship_ProposedMarkup"), // relationship pattern
               relpat,            // object pattern
               selectStmts,    // object selects
               selectRelStmts, // relationship selects
               false,          // to direction
               true,      // from direction
               (short) 1,      // recursion level
               "",             // object where clause
               "");         // rel where clause

            mpListMarkups.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
            mpListMarkups.sort();
        Iterator objItr = mpListMarkups.iterator();
            Map mpMarkups  = null;
            relatedMarkups.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
            relatedMarkups.append("<tr>");
        relatedMarkups.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
        relatedMarkups.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originated",strLanguage)+"</th>");
        relatedMarkups.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Modified",strLanguage)+"</th>");
        relatedMarkups.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</th>");
        relatedMarkups.append("</tr>");

        while (objItr.hasNext()) {
            mpMarkups = (Map)objItr.next();
            relatedMarkups.append("<tr>");
          //Modified for IR-184707V6R2013x start
            relatedMarkups.append("<td><img src=\"../common/images/iconSmallDefault.gif\" border=\"0\" alt=\"*\">&nbsp;"+mpMarkups.get(SELECT_NAME)+"&nbsp;</td>");
          //Modified for IR-184707V6R2013x end
            relatedMarkups.append("<td>"+mpMarkups.get(SELECT_ORIGINATED)+"&nbsp;</td>");
            //modified for the bug 345795,345796 stats
			relatedMarkups.append("<td>"+mpMarkups.get(SELECT_MODIFIED)+"&nbsp;</td>");
            relatedMarkups.append("<td>"+mpMarkups.get(SELECT_OWNER)+"&nbsp;</td>");
			//modified for the bug 345795,345796 ends
            relatedMarkups.append("</tr>");
        }
        if(mpListMarkups.size()==0) {
            relatedMarkups.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.NoRelatedMarkupsFound",strLanguage)+"</td></tr>");
        }
        }
        catch (FrameworkException Ex)
        {
             throw Ex;
        }
        relatedMarkups.append("</table>");
        return relatedMarkups.toString();
    }

	// for bug 344292 starts
    /**
     * Constructs the ECR related ResolvedItems HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @return String Html table format representation of Related ECRs data.
     * @throws Exception if the operation fails.
     * @since Engineering Central X3
    */

   public String getECRRelatedResolvedItems(Context context,String[] args)
         throws Exception
   {
       String strLanguage = context.getSession().getLanguage();
       String objectId = args[0];
       MapList mpListResolvedItems = new MapList();
       StringBuffer relatedResolvedItems = new StringBuffer(1024);

       try
       {
           setId(objectId);
           StringList selectStmts = new StringList();
           selectStmts.addElement(SELECT_ID);
       selectStmts.addElement(SELECT_NAME);
       selectStmts.addElement(SELECT_REVISION);
       selectStmts.addElement(SELECT_TYPE);
       selectStmts.addElement(SELECT_DESCRIPTION);
       selectStmts.addElement(SELECT_CURRENT);
       selectStmts.addElement(SELECT_OWNER);

       String strRelResolvedTo = DomainConstants.RELATIONSHIP_RESOLVED_TO;

       mpListResolvedItems = getRelatedObjects(context,
                                              strRelResolvedTo,
                                              "*",
                                              selectStmts,
                                              null,
                                              true,
                                              false,
                                              (short) 1,
                                              "",
                                              "");

           mpListResolvedItems.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
           mpListResolvedItems.sort();
       Iterator objItr = mpListResolvedItems.iterator();
           Map mpResolvedItems  = null;
           relatedResolvedItems.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
           relatedResolvedItems.append("<tr>");
           relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
           relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Revision",strLanguage)+"</th>");
           relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</th>");
           relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
           relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+"</th>");
           relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</th>");
           relatedResolvedItems.append("</tr>");

       while (objItr.hasNext()) {
           mpResolvedItems = (Map)objItr.next();
           relatedResolvedItems.append("<tr>");
           relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_NAME)+"&nbsp;</td>");
           relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_REVISION)+"&nbsp;</td>");
           relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_TYPE)+"&nbsp;</td>");
           relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_DESCRIPTION)+"&nbsp;</td>");
           relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_CURRENT)+"&nbsp;</td>");
           relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_OWNER)+"&nbsp;</td>");
           relatedResolvedItems.append("</tr>");
       }
       if(mpListResolvedItems.size()==0) {
           relatedResolvedItems.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.NoResolvedItemsFound",strLanguage)+"</td></tr>");
       }
       }
       catch (FrameworkException Ex)
       {
            throw Ex;
       }
       relatedResolvedItems.append("</table>");
       return relatedResolvedItems.toString();
   }


   // for bug 344292 ends

	 /**
      * Creates the ECR summary report and generates the PDF file.
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds the following input arguments:
      * 0 - String containing the ECR id.
      * @return int tells whether PDF is generated and checked into the object. 0-success 1-failure.
      * @throws Exception if the operation fails.
      * @since Engineeringcentral X3
      */

     public  int generatePDFSummaryReport(Context context,String args[]) throws Exception {
        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }

        String summaryReport="";
        try {
             summaryReport=generateHtmlSummaryReport(context,args);

            }
            catch (Exception e) {
               throw e;
        }

   		String strGeneratePDF = FrameworkProperties.getProperty(context, "emxEngineeringCentral.ECRECO.ViewPdfSummary");

   		if ("true".equalsIgnoreCase(strGeneratePDF))
   		{

        int pdfGenerated = renderPDFFile(context,args,summaryReport);

        if(pdfGenerated!=0)
        {
             emxContextUtil_mxJPO.mqlError(context,
                                              EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.SummaryReport.NoCheckIn.ErrorMessage",
                                              context.getSession().getLanguage()));
              return 0;
        }
        else
        {
            return 0;
        }
		}
		else
		{
			return 0;
		}

     }
	/**
     * Generates the ECR Summary PDF file and checks it into the ECO object.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: 0 - String containing
     *            object id.
     * @param summaryReport
     *            holds the string which need to be rendered into PDF.
     * @return int 0- for success, 1- failure.
     * @throws Exception
     *             if the operation fails.
     * @since Engineering Central X3
     */

    public int renderPDFFileForECR(Context context, String[] args,
            String summaryReport) throws Exception {

        int iResult = 1;
        String renderSoftwareInstalled = "";

      //Multitenant
        renderSoftwareInstalled = EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.RenderPDF");
        if (!renderSoftwareInstalled.equalsIgnoreCase("TRUE")) {
            return 0;
        }
        /* Code without packing */
        String objectId = args[0];

        setId(objectId);
        String objName = getInfo(context, SELECT_NAME); // dom.getName();
        String objRev = getInfo(context, SELECT_REVISION);// getRevision(context);

        String sFileName = objName + "-Rev" + objRev + ".pdf";
        String inputFolder = FrameworkProperties
                .getProperty(context, "emxEngineeringCentral.PDF.InputFolderPath");
        String outputFolder = FrameworkProperties
                .getProperty(context, "emxEngineeringCentral.PDF.OutputFolderPath");

        if (inputFolder == null) {
            inputFolder = "C:/AdLib eXpress/input";
        }
        if (outputFolder == null) {
            outputFolder = "C:/AdLib eXpress/Output";
        }

        boolean commitTransaction = false;
        ContextUtil.startTransaction(context, true);
        ContextUtil.pushContext(context);
        //  Noting Down the creation time of html file.
        long pdfLastModified = 0;
        long htmlLastModified = 0;
        java.io.File htmlFile = null;
        String htmlFileName = null;
        java.io.File generatedPDFFile = null;
        try {
        String fileName = inputFolder + java.io.File.separator + objName
                + "-Rev" + objRev;
        FileWriter txtFileID = new FileWriter(fileName, false);
        txtFileID.write(summaryReport);
        txtFileID.flush();
        txtFileID.close();

        htmlFileName = inputFolder + java.io.File.separator + objName
                + "-Rev" + objRev + ".htm";


        htmlFile = new java.io.File(htmlFileName);
        if (htmlFile.exists()) {
            htmlLastModified = htmlFile.lastModified();
        }

        boolean renderProcess = false;
        String sleepInterval = FrameworkProperties
                .getProperty(context, "emxEngineeringCentral.PDF.SleepInterval");

            if (sleepInterval == null || "".equals(sleepInterval)) {
                sleepInterval = "15";
            }
            int sleepInt = Integer.parseInt(sleepInterval);
            Thread.sleep(sleepInt * 1000);
            //
            String outputFilePath = outputFolder + java.io.File.separator
                    + sFileName;
            generatedPDFFile = new java.io.File(outputFilePath);

            if (generatedPDFFile.exists()) {
                renderProcess = true;
                pdfLastModified = generatedPDFFile.lastModified();
            }
            if (renderProcess) {
                commitTransaction = true;
            } else {

                iResult = 1; // commented
                commitTransaction = false; // commented
                ContextUtil.abortTransaction(context);// commented
            }
        } catch (Exception e) {
            iResult = 1;
            ContextUtil.abortTransaction(context);
            emxContextUtil_mxJPO.mqlError(context,
                    EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.SummaryReport.NoCheckIn.ErrorMessage1",
                    context.getSession().getLanguage()));
        }

        try {
            if (commitTransaction) {
                // COMES HERE WHEN RENDITION PROCESS IS SUCCESS.

                ContextUtil.pushContext(context);
                sFileName = objName + "-Rev" + objRev + ".pdf";
                // Attach the created file to the ECO object.
                BusinessObject obj = new BusinessObject(objectId);

                obj.unlock(context);
                obj.checkinFile(context, true, false, null, "generic",
                        sFileName, outputFolder);

                iResult = 0;
                ContextUtil.commitTransaction(context);

                // Delete the htm, dpi and pdf files if last (creation) modified
                // times and latest modified times are same.
                long htmlLatestModified = htmlFile.lastModified();
                long pdfLatestModified = generatedPDFFile.lastModified();


                if (htmlLastModified == htmlLatestModified) {
                    htmlFile.delete();
                }
                if (pdfLastModified == pdfLatestModified) {
                    generatedPDFFile.delete();
                }
            } else {
                //COMES HERE WHEN RENDITION PROCESS HAS FAILED.
                if (objectId == null || objectId.equals("")) {
                    throw new Exception("Exception...... objectid is null....");
                }
            }
        } catch (Exception e) {
            iResult = 1;
            ContextUtil.abortTransaction(context);
            emxContextUtil_mxJPO.mqlError(context,
                    EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.SummaryReport.NoCheckIn.ErrorMessage2",
                    context.getSession().getLanguage()));
            //throw e;
        } finally {
            ContextUtil.popContext(context);
        }
        return iResult;
    }

 /**
  * Contains the HTML code to display the distribution List field for ECR UI
  * @param context the eMatrix <code>Context</code> object.
  * @returns string that contains the HTML code to display the distribution List field for ECR UI.
  * @throws Exception if the operation fails.
  * @since Engineeringcentral X3
  */
	public String getDistributionList (Context context, String[] args) throws Exception {

		StringBuffer strBuf	= new StringBuffer(512);
		strBuf.append("<input type='text' name='DistributionListDisplay' value='' readOnly='true'> </input>");
		strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript: showDistributionList();' > </input>");
		strBuf.append("<a href=\"javascript:clearDistributionList()\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",context.getSession().getLanguage())+"</a>");
		strBuf.append("<input type ='hidden' name='DistributionListOID' value='Unassigned' > </input>");
		return strBuf.toString();

	}

 /**
  * Contains the HTML code to display the reviewer List field for ECR UI
  * @param context the eMatrix <code>Context</code> object.
  * @returns string that contains the HTML code to display the reviewer List field for ECR UI.
  * @throws Exception if the operation fails.
  * @since Engineeringcentral X3
  */
		public String getReviewersList (Context context, String[] args) throws Exception {

		StringBuffer strBuf	= new StringBuffer(512);
		strBuf.append("<input type='text' name='ReviewersListDisplay'  readOnly='true' > </input>");
		strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showReviewersList();' > </input>");
		strBuf.append("<a href=\"javascript:clearReviewersList()\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",context.getSession().getLanguage())+"</a>");
		strBuf.append("<input type ='hidden' name='ReviewersListOID' value='Unassigned' > </input>");
		return strBuf.toString();

	}

 /**
  * Contains the HTML code to display the Reported Against Change field for ECR UI
  * @param context the eMatrix <code>Context</code> object.
  * @returns string that contains the HTML code to display the Reported Against Change field for ECR UI.
  * @throws Exception if the operation fails.
  * @since Engineeringcentral X3
  */
		public String getReportedAgainstChange (Context context, String[] args) throws Exception {

		StringBuffer strBuf	= new StringBuffer(512);
		strBuf.append("<input type='text' name='ReportedAgainstDisplay' value='' readOnly='true'> </input>");
		strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showReportedAgainst();' > </input>");
		strBuf.append("<a href=\"javascript:clearReportedAgainst()\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",context.getSession().getLanguage())+"</a>");
		strBuf.append("<input type ='hidden' name='ReportedAgainstOID' value='Unassigned' > </input>");
		return strBuf.toString();
	}

	/**
     * Connects the Affected Items in Part tree menu in ECR WebForm.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            contains a MapList with the following as input arguments or
     *            entries: objectId holds the context ECR/ECO object Id New
     *            Value holds the newly selected Related ECR Object Id
     * @throws Exception
     *             if the operations fails
     * @since EC - X3
     */
    public int addAffectedItems(Context context, String[] args)
            throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");

        // connectAffectedItems parameter is passed from the massupdate page,
        // will be null if it comes from other pages
        // Added for bug #366190
        if (requestMap.get("connectAffectedItems") != null) {
            return 0;
        }

        // New ECR Object Id
        String strECRId = (String) paramMap.get("objectId");

        // ADDED for BUG # 345453
        // Added to ignore the selection of DCR type in create ECR.
        DomainObject doObj = new DomainObject(strECRId);
        String sType = PropertyUtil.getSchemaProperty(context, "type_DCR");
        if (doObj.isKindOf(context, sType)) {
            String strLanguage = context.getSession().getLanguage();
            String sDCRSelect = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.DCRSelect", strLanguage);
            emxContextUtil_mxJPO.mqlNotice(context, sDCRSelect);
            return 1;
        }

        // Selected Affected Item Ids
        String[] affectedItemsLists = (String[]) requestMap.get("affectedItems");
        String[] affectedItemsList = null;
        if (affectedItemsLists != null) {
            StringTokenizer strTokAffectedItemsList = new StringTokenizer(affectedItemsLists[0], "~");
            affectedItemsList = new String[strTokAffectedItemsList.countTokens()];

            int i = 0;
            while (strTokAffectedItemsList.hasMoreTokens()) {
                affectedItemsList[i++] = strTokAffectedItemsList.nextToken();
            }
        }

        String[] strCreateModes = (String[]) requestMap.get("CreateMode");
        if (strCreateModes != null) {
            String[] strParentOIDs = (String[]) requestMap.get("parentOID");
            if (strParentOIDs != null) {
                affectedItemsList = new String[1];
                affectedItemsList[0] = strParentOIDs[0];
            }
        }

        if (affectedItemsList != null) {
            ECR ecrSource = new ECR(strECRId);
            ecrSource.connectAffectedItems(context, affectedItemsList);
        }

        return 0;
    }

/**
	 * Displays the Range Values on Edit for Attribute Requested Change for Static Approval policies.
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 *          paramMap hold a HashMap containing the following keys, "objectId"
     * @return HashMap contains actual and display values
	 * @throws	Exception if operation fails
	 * @since   EngineeringCentral X3
	 */
	public HashMap displayRequestedChangeRangeValues(Context context,String[] args) throws Exception
	{
		String strLanguage  =  context.getSession().getLanguage();

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap=(HashMap)programMap.get("paramMap");
		String ChangeObjectId =(String) paramMap.get("objectId");
		//Added
		DomainObject dom=new DomainObject(ChangeObjectId);

        //get all range values
        StringList strListRequestedChange = FrameworkUtil.getRanges(context , ATTRIBUTE_REQUESTED_CHANGE);

        HashMap rangeMap = new HashMap ();
        StringList listChoices = new StringList();
        StringList listDispChoices = new StringList();
        String attrValue = "";
        String dispValue = "";
        for (int i=0; i < strListRequestedChange.size(); i++)
        {
            attrValue = (String)strListRequestedChange.get(i);
            //None and For Release are invalid options for ECR Standard policy
            if (attrValue.equals(RANGE_NONE) || attrValue.equals(RANGE_FOR_RELEASE))
                continue;
            if (attrValue.equals(RANGE_FOR_UPDATE) && dom.isKindOf(context,DomainConstants.TYPE_ECR) )
                continue;
            dispValue = i18nNow.getRangeI18NString(ATTRIBUTE_REQUESTED_CHANGE, attrValue, strLanguage);
            listDispChoices.add(dispValue);
            listChoices.add(attrValue);
        }

        rangeMap.put("field_choices", listChoices);
        rangeMap.put("field_display_choices", listDispChoices);
		return rangeMap;
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
    public String displayUserSettingsDefaultVault(Context context, String[] args) throws Exception {

		String defaultVault = PropertyUtil.getAdminProperty(context, personAdminType,  context.getUser(),  PREFERENCE_ENC_DEFAULT_VAULT);
		// IR-093074
		String defaultVaultActual = defaultVault;
		if (defaultVault == null || "".equals(defaultVault)) {
            defaultVault = PersonUtil.getDefaultVault(context);
            defaultVaultActual = defaultVault;
		}
		// fix for 091260 starts
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		String languageStr = (String) paramMap.get("languageStr");
		if(null!=defaultVault && null!=languageStr)
			defaultVault = i18nNow.getAdminI18NString("Vault", defaultVault, languageStr);
		// fix for 091260 ends
		String sECR           = DomainConstants.TYPE_ECR;
		StringBuffer sbReturnString	= new StringBuffer(1024);

		sbReturnString.append("<input type='text' name='VaultDisplay' value=\""+ defaultVault+"\" readOnly='true'> </input>");
		sbReturnString.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript: showVaultSelector();' > </input>");
		sbReturnString.append("<input type ='hidden' name='Vault' value='"+defaultVaultActual+"' > </input>");
		// IR-093074
		sbReturnString.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
		sbReturnString.append(" <script src='../emxUIPageUtility.js'> </script> ");

		sbReturnString.append(" <script> ");
		sbReturnString.append("function showVaultSelector() { ");
		sbReturnString.append("emxShowModalDialog(\"../components/emxComponentsSelectSearchVaultsDialogFS.jsp?multiSelect=false&amp;fieldName=Vault&amp;objectType="+sECR+"&amp;suiteKey=Components&amp;\",300,350); ");

		sbReturnString.append(" } ");
		sbReturnString.append("</script>");

		return sbReturnString.toString();
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
      //Multitenant
          String strMessage = EnoviaResourceBundle.getProperty(context, strResourceFieldId, context.getLocale(),strStringId);
		String strCurrentState = args[8];
		String strPolicy = args[9];
        StringTokenizer stz = null;
		//365869
		String strSymbolicCurrentPolicy = FrameworkUtil.getAliasForAdmin(context, "policy", strPolicy, true);
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
                vector.addElement(PropertyUtil.getSchemaProperty(context, "policy", strPolicyName , state));
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
	      /**
     * Returns whether the attribute value is assigned or not
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds objectId.
     * @param args
     *            holds attribute name.
     * @return Boolean 0 on sucessfull assigning all Affected Items To Responsible Design Engineer.
     * @throws Exception if the operation fails.
     * @since Engineering Central X3.
     */

    public int assignAffectedItemsToResponsibleDesignEngineer(Context context, String args[])
    {
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
           int iResult = 1;

		try{
				String objectId = args[0];
				setId(objectId);
				String strAttributeName =  PropertyUtil.getSchemaProperty(context,args[1]);
				String strRDEName = getAttributeValue(context,strAttributeName);
				String sRDEID     = EngineeringUtil.getBusIdForTNR(context,DomainConstants.TYPE_PERSON,strRDEName,"-");
				StringList objectSelects         = new StringList(2);
				objectSelects.add(DomainConstants.SELECT_ID);
				objectSelects.add(DomainConstants.SELECT_NAME);

				String objectWhereSelects = "id=="+sRDEID;

				//Relationships are selected by its Ids

				StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

				MapList mlAssignedECRel = new MapList();

				mlAssignedECRel = getRelatedObjects( context,
													DomainConstants.RELATIONSHIP_ASSIGNED_EC,
													DomainConstants.TYPE_PERSON,
													objectSelects,
													relSelects,
													true,
													false,
													(short) 1,
													objectWhereSelects,
													null);

				String strCreateAssignedECRelId ="";
				if(mlAssignedECRel==null || mlAssignedECRel.size()<=0)
				{
					DomainRelationship strDR =
					DomainRelationship.connect(context,
												new DomainObject(sRDEID),
												DomainConstants.RELATIONSHIP_ASSIGNED_EC,
												new DomainObject(objectId));
					strCreateAssignedECRelId = strDR.toString();
				}
				else
				{
					Iterator mlAssignedECItr = mlAssignedECRel.iterator();
					while (mlAssignedECItr.hasNext())
					{
						Map mapAssignedECRel = (Map) mlAssignedECItr.next();
						strCreateAssignedECRelId = (String)mapAssignedECRel.get("id[connection]");
					}
				}

				String whrClause	= "id"+"!='"+sRDEID+"'";

				MapList mlPersons = getRelatedObjects( context,
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
							//Disconnecting the Assigned Affected Item Rel by discoonecting Assigned EC relationship.
							if(!("".equals(strAssignedECRelId)))
							{
								DomainRelationship.disconnect(context, strAssignedECRelId);
							}
						}

				 MapList mlBusObjAIList = getRelatedObjects(context,
																RELATIONSHIP_AFFECTED_ITEM,
																"*",
																new StringList(DomainConstants.SELECT_ID),
																new StringList (DomainConstants.SELECT_RELATIONSHIP_ID),
																false,
																true,
																(short) 1,
																null,
																null);

				Iterator mlAffectedItemsItr = mlBusObjAIList.iterator();
				String strAffectedItemRelId = "";
				while (mlAffectedItemsItr.hasNext())
				{
					Map mapAffectedItemObject = (Map) mlAffectedItemsItr.next();
					strAffectedItemRelId = (String)mapAffectedItemObject.get("id[connection]");

					StringList slExistingAssignedECRelId =	getTomids(	context,
																strAffectedItemRelId);

					//Creating the Assigned Affected Item Rel.
					if(slExistingAssignedECRelId == null || slExistingAssignedECRelId.size()==0)
					{
							connect( context,
									RELATIONSHIP_ASSIGNEED_AFFECTED_ITEM,
									strCreateAssignedECRelId,
									strAffectedItemRelId,
									false,
									false);
				}
				}
				iResult = 0;

			}catch (Exception e)
			{
				iResult = 1;
			}
			return iResult;
	}

	/**
     * Returns list of oids we want to exclude from the search
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds objectId.
     * @return StringList of objectIds we want to exclude.
     * @throws Exception if the operation fails.
     * @since Engineering Central X3.
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getRelatedECOOIDs(Context context, String args[])   throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String  parentObjectId = (String) programMap.get("objectId");
        StringList result = new StringList();

        if (parentObjectId == null)
        {
            return (result);
        }

        String stateDefineComponents = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECO, "state_DefineComponents");
        String stateDesignWork       = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECO, "state_DesignWork");
        String stateCreate       = PropertyUtil.getSchemaProperty(context, "policy",POLICY_ECO, "state_Create");


		String relapttern	=	DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT;
		String typepattern	=	DomainConstants.TYPE_ECO;

		////Business Objects are selected by its Ids
		StringList objselectStmts	=	new StringList(2);
		objselectStmts.addElement(DomainConstants.SELECT_ID);
		objselectStmts.addElement("current.access[fromconnect]");
		StringList relselectStmts	=	new StringList();
		relselectStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
		DomainObject doEcrId = new DomainObject(parentObjectId);
		MapList mpListEco = doEcrId.getRelatedObjects(context, relapttern, typepattern, objselectStmts , relselectStmts, true, false, (short)1, null, null);
		if(mpListEco!=null && mpListEco.size()>0)
		{
            Iterator itrEco =  mpListEco.iterator();
			while(itrEco.hasNext())
			{
				Map mapEco = (Map)itrEco.next();
                result.addElement((String) mapEco.get(DomainConstants.SELECT_ID));
			}
		}

        String strWhereClause = "current == \"" + stateCreate + "\" || current == \"" + stateDefineComponents + "\" || current == \"" + stateDesignWork + "\"";

		MapList totalresultList = DomainObject.findObjects(context,
														 typepattern,
														 "*",
														 "*",
														 "*",
														 "*",
														 strWhereClause,
														 null,
														 true,
														 objselectStmts,
														 (short) 0);

		Iterator itrECOs = totalresultList.iterator();

		while (itrECOs.hasNext())
		{
			Map mapECO = (Map) itrECOs.next();
			String strECOId = (String) mapECO.get(DomainConstants.SELECT_ID);
			String strFromConnectAccess = (String) mapECO.get("current.access[fromconnect]");
			if(!"TRUE".equals(strFromConnectAccess) && !result.contains(strECOId))
			{
				result.add(strECOId);
			}
		}

        return result;
    }

 /**
  * Contains the HTML code to display the distribution List field for ECR UI
  * @param context the eMatrix <code>Context</code> object.
  * @returns string that contains the HTML code to display the reviewer List field for ECR UI.
  * @throws Exception if the operation fails.
  * @since Engineeringcentral X3
  */
		public String getDistributionListForEdit (Context context, String[] args) throws Exception {
        StringBuffer outPut = new StringBuffer(1024);

        try {
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");
            HashMap requestMap = (HashMap) programMap.get("requestMap");

            // for PDF rendering
            String strPDFRendering = (String)requestMap.get("PDFrender");

            //Getting mode parameter
            String mode        = (String)requestMap.get("mode");
			//364739
            if(mode==null){
                mode="view";
            }
		    //Start of  IR-015218
			String reportFormat = (String)requestMap.get("reportFormat");
			StringBuffer strBufNamesForExport = new StringBuffer();
			//ends
            //Getting the EC Object id and the new product object id
            String strECObjectId = (String)paramMap.get("objectId");
            //Relationship name
            String strRelationship = PropertyUtil.getSchemaProperty(context,"relationship_ECDistributionList");
            String strType         = PropertyUtil.getSchemaProperty(context,"type_MemberList");

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


            StringList relIdStrList = new StringList();
            StringList routeTemplateIdStrList = new StringList();
            StringList routeTemplateNameStrList = new StringList();

			outPut.append(" <script> ");
			outPut.append("function showECDistributionList() { ");
            outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_MemberList:CURRENT=policy_MemberList.state_Active&table=APPECMemberListsSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameDisplay=DistributionListDisplay&fieldNameActual=DistributionListOID&submitURL=../engineeringcentral/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");

            outPut.append('}');
			outPut.append(" </script> ");

            if(relationshipIdList.size()>0) {// if 1:If there is any relationship object route
                //Getting the realtionship ids and relationship names from the list
                for(int i=0;i<relationshipIdList.size();i++) {
                    relIdStrList.add((String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID));
                    routeTemplateIdStrList.add((String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_ID));
                    routeTemplateNameStrList.add((String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_NAME));
                }

                if(relIdStrList.size()>0){ //if 2: Checking for non empty relId list
                            if( mode==null || mode.equalsIgnoreCase("view") ) {
								// added for PDF Rendering
								if(strPDFRendering!=null && !strPDFRendering.equalsIgnoreCase("")){
								                            		outPut.append(routeTemplateNameStrList.get(0));
                            	}else{
                                    outPut.append("<a href=\"javascript:showModalDialog('emxTree.jsp?objectId=" + routeTemplateIdStrList.get(0) + "',500,700);\">");
                                    outPut.append("<img src='../common/images/iconSmallRouteTemplate.gif' border=0>");
                                    outPut.append("&nbsp;");
                                    outPut.append(routeTemplateNameStrList.get(0));
                                    outPut.append("</a>");
								}
                            } else if( mode.equalsIgnoreCase("edit") ) {
                                outPut.append("<input type=\"text\" name=\"DistributionListDisplay");
                                outPut.append("\"size=\"20\" value=\"");
                                outPut.append(routeTemplateNameStrList.get(0));
                                outPut.append("\" readonly=\"readonly\">&nbsp;");
                                outPut.append("<input class=\"button\" type=\"button\"");
                                outPut.append(" name=\"btnECDistributionListChooser\" size=\"200\" ");
                                outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECDistributionList()\">");
                                outPut.append("<input type=\"hidden\" name=\"DistributionListOID\" value=\""+ routeTemplateIdStrList.get(0) +"\"></input>");
                                outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('DistributionList')\">");
                                outPut.append(strClear);
                                outPut.append("</a>");
                            }
						   //Start of IR-015218
							if(reportFormat != null && reportFormat.length() > 0){
								strBufNamesForExport.append(routeTemplateNameStrList.get(0));
							}
							//end
                }//End of if 2
            } else { //if there are no relationships fields are to be dispalyed only in edit mode
					if( mode.equalsIgnoreCase("edit") ) {
                    outPut.append("<input type=\"text\" name=\"DistributionListDisplay");
                    outPut.append("\"size=\"20\" value=\"");
                    outPut.append("\" readonly=\"readonly\">&nbsp;");
                    outPut.append("<input class=\"button\" type=\"button\"");
                    outPut.append(" name=\"btnECDistributionListChooser\" size=\"200\" ");
					 outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECDistributionList()\">");
                    outPut.append("<input type=\"hidden\" name=\"DistributionListOID\" value=\"\"></input>");
                    outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('DistributionList')\">");
                    outPut.append(strClear);
                    outPut.append("</a>");

                }
            }
			 //Start of IR-015218
                if((strBufNamesForExport.length() > 0 )|| (reportFormat != null && reportFormat.length() > 0))
                {
                    outPut = strBufNamesForExport;
                }
                //IR-015218 ends
        } catch(Exception ex) {
            throw  new FrameworkException((String)ex.getMessage());
        }
        return outPut.toString();
	}

/**
     * Updates the Distribution List field in ECR WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECR object Id
     * New Value holds the newly selected Distribution List Object Id
     * @throws Exception if the operations fails
     * @since Common X3

*/
    public void connectRelatedECDistributionList (Context context, String[] args) throws Exception {
        try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");

            String strRelationship = PropertyUtil.getSchemaProperty(context,"relationship_ECDistributionList");
            String strType         = PropertyUtil.getSchemaProperty(context,"type_MemberList");

            StringList objectSelects = new StringList(2);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);

            //Stringlist containing the relselects
            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);


            //Modified for IR-024904 -Starts
            String strNewId = (String) paramMap.get("New OID");

            //Following lines of code commented as this is not required
            if (strNewId == null || "".equals(strNewId)){
				strNewId = (String) paramMap.get("New Value");
			}

			//Modified for IR-024904 -Ends

            //Calling getRelatedObjects to get the relationship ids
            MapList relationshipIdList = new MapList();

            String strObjectId = (String) paramMap.get("objectId");

            DomainObject doChange = new DomainObject(strObjectId);

				ContextUtil.pushContext(context);
            	relationshipIdList = doChange.getRelatedObjects(context,
                                                    strRelationship,
                                                    strType,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);

				Iterator itrLists = relationshipIdList.iterator();
				while (itrLists.hasNext())
				{
					Map mapTemp = (Map) itrLists.next();
					String strRelId = (String) mapTemp.get(DomainConstants.SELECT_RELATIONSHIP_ID);

					DomainRelationship.disconnect(context, strRelId);

				}

			if (strNewId != null && strNewId.length() > 0)
			{

				DomainRelationship.connect(context,
                                                             doChange,
                                                             strRelationship,
                                                             new DomainObject(strNewId));


			}
			ContextUtil.popContext(context);

        }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
    }

	/**
	* gets the latest released revision of the affected item
	*
	* @param context The Matrix Context.
	* @param strAffectedItemId The affected item id
	* @returns latest released id or null
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public String getLatestReleased (Context context, String strAffectedItemId)
															throws Exception
	{
		DomainObject doAffectedItem = new DomainObject(strAffectedItemId);

		StringList strlSelects = new StringList(5);
		strlSelects.add(SELECT_TYPE);
		strlSelects.add(SELECT_ID);
		strlSelects.add(SELECT_NAME);
		strlSelects.add(SELECT_POLICY);
		strlSelects.add(SELECT_VAULT);

		Map mapDetails = doAffectedItem.getInfo(context, strlSelects);

		String strPolicy = (String) mapDetails.get(SELECT_POLICY);

    	String STATE_AI_RELEASE =  PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Release");

		String strWhereClause = null;

		if (doAffectedItem.isKindOf(context, TYPE_PART))
		{
			String STATE_AI_OBSOLETE =  PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Obsolete");
			strWhereClause = "(current == \"" + STATE_AI_RELEASE + "\") && (!((next.current == \"" + STATE_AI_RELEASE + "\") || (next.current == \"" + STATE_AI_OBSOLETE + "\")))";
		}
		else
		{
			strWhereClause = "(current == \"" + STATE_AI_RELEASE + "\") && (!(next.current == \"" + STATE_AI_RELEASE + "\"))";
		}

		MapList mapListParts = DomainObject.findObjects(context,
					  (String) mapDetails.get(SELECT_TYPE),
					  (String) mapDetails.get(SELECT_NAME),
					  "*",
					  null,
					  (String) mapDetails.get(SELECT_VAULT),
					  strWhereClause,
					  false,
					  strlSelects);

		if (mapListParts.size() > 0)
		{
			Map mapPart = (Map) mapListParts.get(0);
			String strId = (String) mapPart.get(SELECT_ID);

			return strId;
		}
		else
		{
			return null;
		}
	}

	/**
	* gets the latest unreleased revision of the affected item
	*
	* @param context The Matrix Context.
	* @param strAffectedItemId The affected item id
	* @returns latest unreleased id or null
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public String getLatestUnreleased (Context context, String strAffectedItemId)
																	throws Exception
	{
		DomainObject doAffectedItem = new DomainObject(strAffectedItemId);

		BusinessObject boLastRevision = doAffectedItem.getLastRevision(context);

		DomainObject doLastRevision = new DomainObject(boLastRevision);

		String strLastRevId = doLastRevision.getObjectId(context);

		String strPolicy = doLastRevision.getInfo(context, SELECT_POLICY);

    	String STATE_AI_RELEASE =  PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Release");

    	if (checkObjState(context, strLastRevId, STATE_AI_RELEASE, LT) == 0)
    	{
			return strLastRevId;
		}
		else
		{
			return null;
		}

	}

	/**
	* gets the common attributes between two relationship types
	*
	* @param context The Matrix Context.
	* @param strSourceRelType name of first relationship
	* @param strTargetRelType name of second relationship
	* @returns latest unreleased id or null
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public StringList getCommonAttributes(Context context,
						 String strSourceRelType,
						 String strTargetRelType)
								throws Exception
	{
		String strSourceAttributes = MqlUtil.mqlCommand(context, "print relationship $1  select $2 dump $3",strSourceRelType,"attribute","|");

		StringList strlCommonAttributes = new StringList();

		if (strSourceAttributes.length() > 0)
		{
			String strTargetAttributes = MqlUtil.mqlCommand(context, "print relationship $1  select $2 dump $3",strTargetRelType,"attribute","|");

			if (strTargetAttributes.length() > 0)
			{
				StringList strlSourceAttributes = FrameworkUtil.split(strSourceAttributes, "|");

				Iterator itrstrlSourceAttributes = strlSourceAttributes.iterator();
				String strAttributeName = null;

				while (itrstrlSourceAttributes.hasNext())
				{
					strAttributeName = (String) itrstrlSourceAttributes.next();

					if (strTargetAttributes.indexOf(strAttributeName) != -1)
					{
						strlCommonAttributes.add(strAttributeName);
					}
				}
			}
		}

		return strlCommonAttributes;
	}

	/**
	* Adds the selected affected items to selected ECR
	*
	* @param context The Matrix Context.
	* @param strArrAffectedItems The selected affected item ids
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public void connectAffectedItems(Context context,
					String[] args) throws Exception
	{
		HashMap hmpProgramMap= (HashMap)JPO.unpackArgs(args);

		String strTargetECRId = (String) hmpProgramMap.get("targetECRId");
		String [] affectedItemsList = (String []) hmpProgramMap.get("affectedItems");

		StringList strlObjSelects = new StringList(1);
		strlObjSelects.add(SELECT_ID);

		StringList strlRelnSelects = new StringList(1);
		strlRelnSelects.add(SELECT_RELATIONSHIP_ID);

		StringList strlPartSelects = new StringList(4);
		strlPartSelects.add(SELECT_TYPE);
		strlPartSelects.add(SELECT_NAME);
		strlPartSelects.add(SELECT_REVISION);
		strlPartSelects.add(SELECT_RELATIONSHIP_DESIGN_RESPONSIBILITY);

		StringList strlTargetAffectedItems = new StringList();

		BusinessObjectList objectList = new BusinessObjectList();

		String strTargetRelPattern = RELATIONSHIP_AFFECTED_ITEM;

 		DomainObject doTargetECR = new DomainObject(strTargetECRId);
		objectList.add(doTargetECR);

		MapList mapListTargetAffectedItems = doTargetECR.getRelatedObjects(context, strTargetRelPattern, "*", strlObjSelects, strlRelnSelects, false, true, (short) 1, null, null);

		Iterator itrTargetAffectedItems = mapListTargetAffectedItems.iterator();

		Map mapTargetAffectedItem = null;
		String strTargetAffectedItemId = null;

		while (itrTargetAffectedItems.hasNext())
		{
			mapTargetAffectedItem = (Map) itrTargetAffectedItems.next();
			strTargetAffectedItemId = (String) mapTargetAffectedItem.get(DomainConstants.SELECT_ID);
			strlTargetAffectedItems.add(strTargetAffectedItemId);
			objectList.add(new DomainObject(strTargetAffectedItemId));
		}

		int intNumAffectedItems = affectedItemsList.length;

		StringTokenizer strTokTemp = null;
		String strTempId = null;

		for(int i=0; i < intNumAffectedItems; i++)
		{
			strTokTemp = new StringTokenizer(affectedItemsList[i], "|");
			strTempId = strTokTemp.nextToken().trim();
			objectList.add(new DomainObject(strTempId));
		}

		Access accessMask = new Access();
		accessMask.setAllAccess(true);
		accessMask.setUser(context.getUser());
		ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
		BusinessObject.grantAccessRights(context,
									 	objectList,
									 	accessMask);
		ContextUtil.popContext(context);

		String strSourceAffectedItemId = null;
		StringTokenizer strtokObjectIds = null;

		Map mapPartDetails = null;

		DomainObject doSourceAffectedItem = null;

		boolean blnAlreadyConnected = false;
		String strAlreadyConnected = "";

			for(int i=0; i < intNumAffectedItems; i++)
			{
				strtokObjectIds = new StringTokenizer(affectedItemsList[i], "|");
				strSourceAffectedItemId = strtokObjectIds.nextToken().trim();

				doSourceAffectedItem = new DomainObject(strSourceAffectedItemId);
				mapPartDetails = doSourceAffectedItem.getInfo(context, strlPartSelects);

				if (strlTargetAffectedItems.contains(strSourceAffectedItemId))
				{
					if (strAlreadyConnected.length() > 0)
					{
						strAlreadyConnected = strAlreadyConnected  + ", " + (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
					}
					else
					{
						strAlreadyConnected = (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
					}
					blnAlreadyConnected = true;
					continue;
				}

				DomainRelationship.connect(context, doTargetECR, RELATIONSHIP_AFFECTED_ITEM, doSourceAffectedItem);

			}

		if (blnAlreadyConnected)
		{
			// display warning to the user on the non-revisable affected items
			emxContextUtil_mxJPO.mqlWarning(context,
				EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.AlreadyConnectedAffectedItemsWarning",
				context.getSession().getLanguage()) + strAlreadyConnected);
		}

		//376812 - Starts
        AccessList aclList = new AccessList(1);
        aclList.add(accessMask);
        ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
        BusinessObject.revokeAccessRights(context, objectList, aclList);
        ContextUtil.popContext(context);
        //376812 - Ends
	}

    /**
    * Check the current state of the object with the target state, using the comparison operator
    * and returns the result.
    * @return an int value.
    *               0 if object state logic satisfies Comparison Operator.
    *               1 if object state logic didn't satisfies Comparison Operator.
    *               2 if a program error is encountered
    *               3 if state in state argument does not exist in the policy
    *               4 if an invalid comparison operator is passed in
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param id  String representing the id of the object whose state to be checked.
    * @param targetState String representing the target state against which the current state of the object is compared.
    * @param comparisonOperator int representing the operator used for comparison LT, GT, EQ, LE, GE, NE.
    * @since EC 10.0.0.0.
    */

    int checkObjState(matrix.db.Context context, String id, String targetState, int comparisonOperator)
    {
        try
        {
        	String sResult = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4",id,"current","state","|");

            StringTokenizer tokens = new StringTokenizer(sResult, "|");
            // get the index of target state
            int targetIndex = sResult.lastIndexOf(targetState);
            // get the index of current state
            int stateIndex  = sResult.lastIndexOf(tokens.nextToken());

            // if the target state doesn't exist in policy then break
            if (targetIndex < 0)
            {
                return 3; // State doesn't exist in the policy
            }

            // check Target State index with object Current state index
            switch (comparisonOperator)
            {
                case LT :
                    if ( stateIndex < targetIndex )
                    {
                        return 0;
                    }
                    break;

                case GT :
                    if ( stateIndex > targetIndex )
                    {
                        return 0;
                    }
                    break;

                case EQ :
                    if ( stateIndex == targetIndex )
                    {
                         return 0;
                    }
                    break;

                case LE :
                    if ( stateIndex <= targetIndex )
                    {
                         return 0;
                    }
                    break;

                case GE :
                    if ( stateIndex >= targetIndex )
                    {
                         return 0;
                    }
                    break;

                case NE :
                    if ( stateIndex != targetIndex )
                    {
                        return 0;
                    }
                    break;

                default :
                    return 4;

            }
            return 1;
        } catch (Exception ex) {
            // program error return
            return 2;
        }

    }

	/**
	* assign the selected affected items from context ECR to selected ECO
	*
	* @param context The Matrix Context.
	* @param strTargetECOId The selected ECO id
	* @param strArrAffectedItems The selected affected item ids
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public void assignAffectedItems(Context context,
								  String[] args) throws Exception
	{
		HashMap hmpProgramMap= (HashMap)JPO.unpackArgs(args);

		String strSourceECRId = (String) hmpProgramMap.get("sourceECRId");
		String strTargetECOId = (String) hmpProgramMap.get("targetECOId");
		String [] affectedItemsList = (String []) hmpProgramMap.get("affectedItems");
		boolean blnConnectECR = ((Boolean) hmpProgramMap.get("connectECRECO")).booleanValue();

		StringList strlObjSelects = new StringList(1);
		strlObjSelects.add(SELECT_ID);

		StringList strlRelnSelects = new StringList(1);
		strlRelnSelects.add(SELECT_RELATIONSHIP_ID);

		StringList strlPartSelects = new StringList(4);
		strlPartSelects.add(SELECT_TYPE);
		strlPartSelects.add(SELECT_NAME);
		strlPartSelects.add(SELECT_REVISION);
		strlPartSelects.add(SELECT_RELATIONSHIP_DESIGN_RESPONSIBILITY);
		strlPartSelects.add("altowner1"); //Added for IR-216604

		StringList strlTargetAffectedItems = new StringList();

		BusinessObjectList objectList = new BusinessObjectList();

		String strRelPattern = RELATIONSHIP_AFFECTED_ITEM;
		String strTargetRelPattern = RELATIONSHIP_AFFECTED_ITEM;

		DomainObject doSourceECR = new DomainObject(strSourceECRId);
		objectList.add(doSourceECR);

		DomainObject doTargetECO = new DomainObject(strTargetECOId);
		objectList.add(doTargetECO);

		MapList mapListTargetAffectedItems = doTargetECO.getRelatedObjects(context, strTargetRelPattern, "*", strlObjSelects, strlRelnSelects, false, true, (short) 1, null, null);

		Iterator itrTargetAffectedItems = mapListTargetAffectedItems.iterator();

		Map mapTargetAffectedItem = null;
		String strTargetAffectedItemId = null;

		while (itrTargetAffectedItems.hasNext())
		{
			mapTargetAffectedItem = (Map) itrTargetAffectedItems.next();
			strTargetAffectedItemId = (String) mapTargetAffectedItem.get(DomainConstants.SELECT_ID);
			strlTargetAffectedItems.add(strTargetAffectedItemId);
			objectList.add(new DomainObject(strTargetAffectedItemId));
		}

			String strECORDO = doTargetECO.getAltOwner1(context).toString(); //Added for IR-216604

		int intNumAffectedItems = affectedItemsList.length;

		StringTokenizer strTokTemp = null;
		String strTempId = null;

		for(int i=0; i < intNumAffectedItems; i++)
		{
			strTokTemp = new StringTokenizer(affectedItemsList[i], "|");
			strTempId = strTokTemp.nextToken().trim();
			objectList.add(new DomainObject(strTempId));
		}

		Access accessMask = new Access();
		accessMask.setAllAccess(true);
		accessMask.setUser(context.getUser());
		ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
		BusinessObject.grantAccessRights(context,
										objectList,
										accessMask);
		ContextUtil.popContext(context);

		String strSourceAffectedItemId = null;
		String strPartRDO = null;
		StringTokenizer strtokObjectIds = null;

		DomainObject doSourceAffectedItem = null;
		DomainRelationship doTargetAfectedItemRel = null;

		MapList mapListECRsECOs = new MapList();
		Map mapECRECO = null;
		Map attrMap = null;
		Map mapPartDetails = null;

		String strSourceRelId = null;
		String strWhereClause = DomainConstants.SELECT_ID + " == " + strSourceECRId;

		boolean blnShowRDOMsg = false;
		boolean blnShowReviseMsg = false;
		boolean blnShowConnectFailMsg = false;
		boolean blnAlreadyConnected = false;

		String strRDOMismatchMsg = "";
		String strRevisionFailMsg = "";
		String strConnectFailMsg = "";
		String strAlreadyConnected = "";

				for(int i=0; i < intNumAffectedItems; i++)
				{
					strtokObjectIds = new StringTokenizer(affectedItemsList[i], "|");
					strSourceAffectedItemId = strtokObjectIds.nextToken().trim();

					doSourceAffectedItem = new DomainObject(strSourceAffectedItemId);

					mapPartDetails = doSourceAffectedItem.getInfo(context, strlPartSelects);

					if (strlTargetAffectedItems.contains(strSourceAffectedItemId))
					{
						if (strAlreadyConnected.length() > 0)
						{
							strAlreadyConnected = strAlreadyConnected  + ", " + (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
						}
						else
						{
							strAlreadyConnected = (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
						}
						blnAlreadyConnected = true;
						continue;
					}

					//Modified for IR-216604 Start
					if (strECORDO != null && !"".equals(strECORDO))
	                {
	                    strPartRDO = (String) mapPartDetails.get("altowner1");

	                    if (strPartRDO != null && !"".equals(strPartRDO))
	                    {
	                        if (!strECORDO.equalsIgnoreCase(strPartRDO))
	                        {
	                            if (strRDOMismatchMsg.length() > 0)
	                            {
	                                strRDOMismatchMsg = strRDOMismatchMsg + ", " + (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
	                            }
	                            else
	                            {
	                                strRDOMismatchMsg = (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
	                            }
	                            blnShowRDOMsg = true;
	                            blnConnectECR = false;
	                            continue;
	                        }
	                    }
	                }
	              //Modified for IR-216604 End

					mapListECRsECOs = doSourceAffectedItem.getRelatedObjects(context, strRelPattern, "*", strlObjSelects, strlRelnSelects, true, false, (short) 1, strWhereClause, null);
					mapECRECO = (Map) mapListECRsECOs.get(0);
					strSourceRelId = (String) mapECRECO.get(DomainConstants.SELECT_RELATIONSHIP_ID);
					doTargetAfectedItemRel = DomainRelationship.connect(context, doTargetECO, RELATIONSHIP_AFFECTED_ITEM, doSourceAffectedItem);
					attrMap = DomainRelationship.getAttributeMap(context, strSourceRelId);
					doTargetAfectedItemRel.setAttributeValues(context, attrMap);
                    assignMarkups(context, strSourceAffectedItemId, strSourceECRId, strTargetECOId);
				}

		if (blnConnectECR)
		{
			PropertyUtil.setRPEValue(context, "MX_FROM_ASSIGNTO", "true", false);
			doTargetAfectedItemRel = DomainRelationship.connect(context, doTargetECO, DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT, doSourceECR);
		}

		if (blnShowRDOMsg)
		{
			// display warning to the user on RDO mismatch of affected items
			emxContextUtil_mxJPO.mqlWarning(context,
				EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.AffectedItemsRDOMismatchWarning",
				context.getSession().getLanguage()) + strRDOMismatchMsg);
		}

		if (blnShowReviseMsg)
		{
			// display warning to the user on the non-revisable affected items
			emxContextUtil_mxJPO.mqlWarning(context,
				EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.AutoRevECRAffectedItemsWarning",
				context.getSession().getLanguage()) + strRevisionFailMsg);
		}

		if (blnShowConnectFailMsg)
		{
			// display warning to the user on the non-revisable affected items
			emxContextUtil_mxJPO.mqlWarning(context,
				EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.ConnectAffectedItemsWarning",
				context.getSession().getLanguage()) + strConnectFailMsg);
		}

		if (blnAlreadyConnected)
		{
			// display warning to the user on the non-revisable affected items
			emxContextUtil_mxJPO.mqlWarning(context,
				EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.AlreadyConnectedAffectedItemsWarning",
				context.getSession().getLanguage()) + strAlreadyConnected);
		}

		AccessList aclList = new AccessList(1);
        aclList.add(accessMask);
        ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
        BusinessObject.revokeAccessRights(context, objectList, aclList);
        ContextUtil.popContext(context);
	}

	/**
	* assigns Markups from one ECR to a ECO for a given part
	*
	* @param context The Matrix Context.
	* @param strPartId Part Id
	* @param strSourceECR Source ECR Id
	* @param strTargetECO Target ECO Id
	* @returns latest unreleased id or null
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public void assignMarkups(Context context,
							String strPartId,
							String strSourceECR,
							String strTargetECO)
										 		throws Exception
	{
		try
		{
			ContextUtil.pushContext(context);
			StringList strlObjSelects = new StringList(1);
			strlObjSelects.add(SELECT_ID);

			StringList strlRelnSelects = new StringList(1);
			strlRelnSelects.add(DomainRelationship.SELECT_ID);

			DomainObject doSourceAffectedItem = new DomainObject(strPartId);

			String strWhereClause = "(to[" + RELATIONSHIP_PROPOSED_MARKUP + "].from.id == \"" + strSourceECR + "\") && (current == \"" + DomainConstants.STATE_EBOM_MARKUP_APPROVED + "\")";

			MapList mapListMarkups = doSourceAffectedItem.getRelatedObjects(context, RELATIONSHIP_EBOM_MARKUP, TYPE_PART_MARKUP + "," + TYPE_EBOM_MARKUP, strlObjSelects, strlRelnSelects, false, true, (short) 1, strWhereClause, null);

			if (mapListMarkups != null && mapListMarkups.size() > 0)
			{
				DomainObject doTargetECO = new DomainObject(strTargetECO);

				DomainObject doMarkup = null;

				Iterator itrMarkups = mapListMarkups.iterator();

				while (itrMarkups.hasNext())
				{
					Map mapMarkup = (Map) itrMarkups.next();
					doMarkup = new DomainObject((String) mapMarkup.get(DomainConstants.SELECT_ID));
					DomainRelationship.connect(context, doTargetECO, RELATIONSHIP_APPLIED_MARKUP, doMarkup);
				}
			}
		}
		catch (Exception ex)
		{
		}
		finally
		{
			ContextUtil.popContext(context);
		}
	}
	/**
	     * Display the ChangeResponsiblity field in ECR WebForm.
	     * @param context the eMatrix <code>Context</code> object
	     * @param args contains a MapList with the following as input arguments or entries:
	     * objectId holds the context ECR object Id
	     * @throws Exception if the operations fails
	     * @since EC - X4
	     */
	    public String getChangeResponsibility (Context context, String[] args) throws Exception {

	        StringBuffer sbReturnString = new StringBuffer(2048);
	        sbReturnString.append("<input type=\"text\"  name=\"ChangeResponsibilityDisplay\" readonly=\"true\" id=\"\" value=\"");
	        sbReturnString.append("");
	        sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
	        sbReturnString.append("</input>");
	        sbReturnString.append("<input type=\"hidden\"  name=\"ChangeResponsibility\" value=\"");
	        sbReturnString.append("");
	        sbReturnString.append("\">");
	        sbReturnString.append("</input>");

	        sbReturnString.append("<input type=\"button\" name=\"btnChangeResponsibility\" value=\"...\"   onclick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace:CURRENT=policy_Organization.state_Active&amp;table=ENCAddExistingGeneralSearchResults&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;fieldNameActual=ChangeResponsibility&amp;fieldNameDisplay=ChangeResponsibilityDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;ExcludePlant=true&amp;excludeOIDprogram=emxENCFullSearchBase:excludeOIDNonLeadOrganizations&amp;HelpMarker=emxhelpfullsearch");
	        sbReturnString.append("");
	        sbReturnString.append("','700','500')\">");
	        sbReturnString.append("</input>");
	        sbReturnString.append("<a href=\"JavaScript:basicClear('ChangeResponsibility')\">");
	        sbReturnString.append(strClear);
	        sbReturnString.append("</a>");
	        return sbReturnString.toString();

	    }
	    /**
	     * Display the ResponsibleDesignEngineer field in ECR WebForm.
	     * @param context the eMatrix <code>Context</code> object
	     * @param args contains a MapList with the following as input arguments or entries:
	     * objectId holds the context ECR object Id
	     * @throws Exception if the operations fails
	     * @since EC - X4
	     */
	    public String getRDEngineer (Context context, String[] args) throws Exception
	    {

	        StringBuffer sbReturnString = new StringBuffer(1024);
	        sbReturnString.append("<input type=\"text\"  name=\"RDEngineerDisplay\" readonly=\"true\" id=\"\" value=\"");
	        sbReturnString.append("");
	        sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
	        sbReturnString.append("</input>");
	        sbReturnString.append("<input type=\"hidden\"  name=\"RDEngineer\" value=\"");
	        sbReturnString.append("");
	        sbReturnString.append("\">");
	        sbReturnString.append("</input>");

	        sbReturnString.append("<input type=\"button\" name=\"btnRDEngineer\" value=\"...\"   onclick=\"javascript:showChooser('../engineeringcentral/emxEngrIntermediateSearchUtil.jsp?field=TYPES=type_Person:USERROLE=role_SeniorDesignEngineer:CURRENT=policy_Person.state_Active&amp;table=ENCAssigneeTable&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;formName=emxCreateForm&amp;fieldNameActual=RDEngineer&amp;fieldNameDisplay=RDEngineerDisplay&amp;mode=Chooser&amp;chooserType=PersonChooser&amp;validateField=ChangeResponsibility");
	        sbReturnString.append("");
	        sbReturnString.append("','700','500')\">");
	        sbReturnString.append("</input>");
	        sbReturnString.append("<a href=\"JavaScript:basicClear('RDEngineer')\">");
	        sbReturnString.append(strClear);
	        sbReturnString.append("</a>");
	        return sbReturnString.toString();

	    }
	    /**
	     *Update the Responsible Design Engineer
	     * @param context the Matrix Context
	     * @param args no args needed for this method
	     * @returns booloen
	     * @throws Exception if the operation fails
	     * @since EC X+4
	     */
	    public Boolean updateRDEngineer(Context context, String[] args) throws Exception
	    {

	        HashMap programMap = (HashMap)JPO.unpackArgs(args);
	        HashMap paramMap = (HashMap)programMap.get("paramMap");

	        String strECRbjectId = (String)paramMap.get("objectId");
	        DomainObject domObjECR = new DomainObject(strECRbjectId);
	        String ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER =
	            PropertyUtil.getSchemaProperty(context,"attribute_ResponsibleDesignEngineer");
	        String strNewRDE = (String)paramMap.get("New Value");
	        if (strNewRDE.length() == 0)
	        {
	            strNewRDE =  domObjECR.getAttributeValue(context,ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER);
	        }
	        domObjECR.setAttributeValue(context, ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER, strNewRDE);

	        return Boolean.TRUE;
    	}
        /**
         * Display the Design Responsibility Item field in ECO WebForm.
         * @param context the eMatrix <code>Context</code> object
         * @param args contains a MapList with the following as input arguments or entries:
         * objectId holds the context ECO object Id
         * @throws Exception if the operations fails
         * @since EC - X3
         */

    public Object  displayDesignResponsibilityItem(Context context,String[] args)throws Exception{
            StringBuffer sbReturnString = new StringBuffer();
            try{
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                HashMap requestMap = (HashMap) programMap.get("requestMap");

                String relChange = (String) requestMap.get("objectId");
                String strModCreate= (String)requestMap.get("CreateMode");
                if(strModCreate==null){
                    strModCreate = "";
                }
                String sDisplayValue = "";
                String sHiddenValue = "";
               String strPartDrawingId = (String) requestMap.get("objectId");
                boolean hasRDO= true;

                String strRelationshipDesignResponsibility =
                    PropertyUtil.getSchemaProperty(context,"relationship_DesignResponsibility");
                if("CreateECR".equals(strModCreate)){
                        sDisplayValue = "";
                        sHiddenValue = "";

                    if (strPartDrawingId != null){
                        StringList strlObjSelects = new StringList(2);
                        strlObjSelects.add(DomainConstants.SELECT_ID);
                        strlObjSelects.add(DomainConstants.SELECT_NAME);
                        StringList strlRelSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                        String strType = DomainConstants.TYPE_ORGANIZATION;

                        DomainObject doPart = new DomainObject(strPartDrawingId);

                        MapList mapListRDOs = doPart.getRelatedObjects(context,
                                                strRelationshipDesignResponsibility,
                                                strType,
                                                strlObjSelects,
                                                strlRelSelects,
                                                true,
                                                false,
                                                (short)1,
                                                null,
                                                null);

                        if (mapListRDOs.size() > 0){
                            Map mapRDO = (Map) mapListRDOs.get(0);
                            sDisplayValue = (String) mapRDO.get(DomainConstants.SELECT_NAME);
                            sHiddenValue = (String) mapRDO.get(DomainConstants.SELECT_ID);
                            hasRDO = false;
                        }
                    }

                    if (hasRDO){
                        String sRdoTNR = PropertyUtil.getAdminProperty(context, personAdminType,  context.getUser(),  PREFERENCE_DESIGN_RESPONSIBILITY);
                        if(sRdoTNR != null && !"null".equals(sRdoTNR) && !"".equals(sRdoTNR)){
                            //split the {T}{N}{R} value & get the objectId
                            if(sRdoTNR.indexOf('}') > 0) {
                                String sType = sRdoTNR.substring(1,sRdoTNR.indexOf('}'));
                                sRdoTNR =sRdoTNR.substring(sRdoTNR.indexOf('}')+2);
                                sDisplayValue = sRdoTNR.substring(0,sRdoTNR.indexOf('}'));
                              try{
                                sHiddenValue = EngineeringUtil.getBusIdForTNR(context,sType,sDisplayValue,sRdoTNR.substring(sRdoTNR.indexOf('{')+1,sRdoTNR.length()-1));
                              }catch(Exception Ex){
                                throw Ex;
                             }
                            }
                        }
                    }

                    sbReturnString.append("<input type=\"text\"  name=\"DesignResponsibilityDisplay\" readonly=\"true\" id=\"\" value=\"");
                    sbReturnString.append(sDisplayValue);
                    sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
                    sbReturnString.append("</input>");
                    sbReturnString.append("<input type=\"hidden\"  name=\"DesignResponsibilityOID\" value=\"");
                    sbReturnString.append(sHiddenValue);
                    sbReturnString.append("\">");
                    sbReturnString.append("</input>");
                    sbReturnString.append("<input type=\"button\" name=\"btnCompany\" value=\"...\" onclick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace:CURRENT=policy_Organization.state_Active&amp;table=ENCAddExistingGeneralSearchResults&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;fieldNameActual=DesignResponsibilityOID&amp;fieldNameDisplay=DesignResponsibilityDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch");
                    sbReturnString.append("&amp;fieldNameOID=DesignResponsibilityOID&amp;relId=null&amp;suiteKey=EngineeringCentral','850','630')\">");
                    sbReturnString.append("</input>");
                    sbReturnString.append("<a href=\"JavaScript:basicClear('DesignResponsibility')\">");
                    sbReturnString.append(strClear);
                    sbReturnString.append("</a>");

                }else{
                        DomainObject domObj = new DomainObject(relChange);
                        if(domObj.isKindOf(context, DomainConstants.TYPE_ECR) || domObj.isKindOf(context, DomainConstants.TYPE_ECO) || domObj.isKindOf(context, DomainConstants.TYPE_PART)){

                                //Business Objects are selected by its Ids
                                StringList objectSelects = new StringList();
                                objectSelects.addElement(DomainConstants.SELECT_NAME);
                                objectSelects.addElement(DomainConstants.SELECT_ID);
                                //Maplist containing the relationship ids
                                MapList relationshipIdList = new MapList();
                                String sObjectName ="";
                                String sObjectId ="";
                                if("".equals(strModCreate)){

                                    // Getting the Selected Object Id in array

                                    String strMemberIds= (String)requestMap.get("memberid");
                                    StringTokenizer stz = new StringTokenizer(strMemberIds,",");
                                    ArrayList arrListDom = new ArrayList();
                                    if(stz.hasMoreElements()){
                                        String token = stz.nextToken();
                                        String str1 = StringUtils.replace(token,"[", "");
                                        String str2 = StringUtils.replace(str1,"]", "");
                                        // Creating Domain Object of the affected items which is selected
                                        DomainObject domAffected = new DomainObject(str2);
                                        arrListDom.add(str2);
                                        StringList busSelects = new StringList(2);
                                        busSelects.add(DomainConstants.SELECT_ID);
                                        busSelects.add(DomainConstants.SELECT_NAME);
                                        StringList relSelectsList1 = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                                        String sType = DomainConstants.TYPE_ORGANIZATION;
                                        relationshipIdList = domAffected.getRelatedObjects(context,
                                                                        strRelationshipDesignResponsibility,
                                                                        sType,
                                                                        busSelects,
                                                                        relSelectsList1,
                                                                        true,
                                                                        false,
                                                                        (short)1,
                                                                        null,
                                                                        null);


                                    }
                                }

                                if (relationshipIdList.size() > 0){
                                    Map newMap = (Map)relationshipIdList.get(0);
                                    sObjectName=(String) newMap.get(DomainConstants.SELECT_NAME);
                                    sObjectId=(String) newMap.get(DomainConstants.SELECT_ID);
                                    sDisplayValue =sObjectName;
                                    sHiddenValue = sObjectId;
                                    sbReturnString.append("<input type=\"text\"  name=\"DesignResponsibilityDisplay\" readonly=\"true\" id=\"\" value=\"");
                                    sbReturnString.append(sDisplayValue);
                                    sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
                                    sbReturnString.append("</input>");
                                    sbReturnString.append("<input type=\"hidden\"  name=\"DesignResponsibilityOID\" value=\"");
                                    sbReturnString.append(sHiddenValue);
                                    sbReturnString.append("\">");
                                    sbReturnString.append("</input>");
                                    sbReturnString.append("<input type=\"button\" name=\"btnCompany\" disabled =\"true\" value=\"...\" onclick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace:CURRENT=policy_Organization.state_Active&amp;table=ENCAddExistingGeneralSearchResults&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;fieldNameActual=DesignResponsibilityOID&amp;fieldNameDisplay=DesignResponsibilityDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch");
                                    sbReturnString.append("&amp;fieldNameOID=DesignResponsibilityOID&amp;relId=null&amp;suiteKey=EngineeringCentral','850','630')\">");
                                    sbReturnString.append("</input>");
                                    sbReturnString.append("<a href=\"JavaScript:basicClear('DoNotClear')\" disabled=\"true\">");
                                    sbReturnString.append(strClear);
                                    sbReturnString.append("</a>");

                                }else{
                                    sbReturnString.append("<input type=\"text\"  name=\"DesignResponsibilityDisplay\" readonly=\"true\" id=\"\" value=\"");
                                    sbReturnString.append("");
                                    sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
                                    sbReturnString.append("</input>");
                                    sbReturnString.append("<input type=\"hidden\"  name=\"DesignResponsibilityOID\" value=\"");
                                    sbReturnString.append("");
                                    sbReturnString.append("\">");
                                    sbReturnString.append("</input>");
                                    sbReturnString.append("<input type=\"button\" name=\"btnCompany\" value=\"...\" onclick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace:CURRENT=policy_Organization.state_Active&amp;table=ENCAddExistingGeneralSearchResults&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;fieldNameActual=DesignResponsibilityOID&amp;fieldNameDisplay=DesignResponsibilityDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch");
                                    sbReturnString.append("");
                                    sbReturnString.append("&amp;fieldNameOID=DesignResponsibilityOID&amp;relId=null&amp;suiteKey=EngineeringCentral','850','630')\">");
                                    sbReturnString.append("</input>");
                                    sbReturnString.append("<a href=\"JavaScript:basicClear('DesignResponsibilityDisplay')\">");
                                    sbReturnString.append(strClear);
                                    sbReturnString.append("</a>");
                                }
                        }else{
                            sbReturnString.append("");
                        }
                    }
                }catch(Exception ex){
            }
            return sbReturnString.toString();
        }
    /**
     * This  is the postprocessJPO method for Cancelling ECR
     * @param context the eMatrix <code>Context</code> object.
     * @param args[] packed hashMap of request parameters
     * @throws Exception if the operation fails.
     * @since R212
      */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void cancelECRProcess(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String sDeleteAffectedItems = "false";
        String sDisconnectECRs = "true";
        String sReason=   (String) requestMap.get("Reason");
        String objectId=(String) requestMap.get("objectId");
        String[] beanargs=new String[]{objectId,sReason,sDeleteAffectedItems,sDisconnectECRs};
        Change cx= new Change();
        cx.cancelChangeProcess(context, beanargs);
    }


    //Added for HF-123851 start
    /**
	    * This method returns true if the context user has the given RCO role.
	    * @param context the eMatrix <code>Context</code> object
	    * @param args holds one argument, the symbolic role name
	    * @returns true if context user has the given RCO role, otherwise false
	    * @throws Exception if the operation fails
	    */
    public String hasRCOrole(Context context, String[] args) throws Exception {
		String SELECT_RCO_ID = "to[" + RELATIONSHIP_CHANGE_RESPONSIBILITY+ "].from.id";
		if (args == null || args.length < 1) {
			throw (new IllegalArgumentException());
		}
		String role = args[0];
		String returnValue = "false";
		try {
			// find RCO for this object
			String RCOid = getInfo(context, SELECT_RCO_ID);
			// first see if the object is wired to an RCO
			if (RCOid == null || RCOid.equals("")) {
				// no RCO, return true
				returnValue = "true";
			} else {
				// search for RCO entry in cache
				StringList roles = null;
				Map RCOmap = (Map) PersonUtil.getPersonProperty(context, "RCO");

				if (RCOmap != null) {
					roles = (StringList) RCOmap.get(RCOid);
				} else {
					RCOmap = new HashMap(1);
				}

				if (roles == null) {
					// get the Project Role attribute value from the Member rel
					StringList relSelects = new StringList(1);
					relSelects.add("attribute[" + ATTRIBUTE_PROJECT_ROLE + "]");

					// get the rel to the specific RCO only
					String objectWhere = "id == \"" + RCOid + "\"";

					Person person = Person.getPerson(context);

					// navigate from person to RCO via the member rel;
					MapList memberMapList = person.getRelatedObjects(context, // context
							RELATIONSHIP_MEMBER,// relationship pattern
							QUERY_WILDCARD, // object pattern
							null, // object selects
							relSelects, // relationship selects
							true, // to direction
							false, // from direction
							(short) 1, // recursion level
							objectWhere, // object where clause
							null); // relationship where clause

					// not found in cache, must add new entry
					if (memberMapList == null || memberMapList.size() < 1) {
						// person not member of RCO,
						// place empty string in RCOmap so search is avoided
						// next time
						roles = new StringList(1);
						roles.addElement("");
					} else {
						// get roles in the Project Role attribute on the
						// context user's Member connection
						Map personMap = (Map) memberMapList.get(0);
						String projectRole = (String) personMap
								.get("attribute[" + ATTRIBUTE_PROJECT_ROLE+ "]");
						if (projectRole != null) {
							roles = FrameworkUtil.split(projectRole, "~");
						}
						// now need to get the parent roles for the given roles
						// and add those to the list
						String tempRole = "";
						String tempCmd = "";
						String results = "";
						String parentAlias = "";

						for (int i = 0; i < roles.size(); i++) {
							tempRole = roles.elementAt(i).toString();
							tempRole = PropertyUtil.getSchemaProperty(context,tempRole);

							tempCmd = "print role $1 select $2 dump $3";
							results = MqlUtil.mqlCommand(context, tempCmd, tempRole,"parent","|");

							StringTokenizer tokens = new StringTokenizer(
									results, "|");
							while (tokens.hasMoreTokens()) {
								parentAlias = FrameworkUtil.getAliasForAdmin(
										context, "role", (String) tokens
												.nextToken(), true);
								if (!roles.contains(parentAlias)) {
									roles.add(parentAlias);
								}
							}
						}
					}
					// update RCO cache
					RCOmap.put(RCOid, roles);
					PersonUtil.setPersonProperty(context, "RCO", RCOmap);
				}
				// test if given role is in the Project Role attribute
				if (roles != null) {
					if (roles.contains(role)) {
						// role found, return true
						returnValue = "true";
					}
				}
			}

			// return value through global RPE
			String command = "set env global $1 $2";
			MqlUtil.mqlCommand(context, command, returnValue, "emxOrganization");
		} catch (FrameworkException Ex) {
			throw Ex;
		}
		return returnValue;
	}
    //Added for HF-123851 end
    
    /**
     * This method gets Lead Person for an ECR which is given lead role ECR Coordinator,if Lead role not assigned then returns owner of the ECR 
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap
     * @throws Exception if the operation fails.
     * @since EngineeringCental R418.HF2
     */	 
	public StringList getECRCoordinator(Context context, String[] args)
	throws Exception {
	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	/* Get ECR Object Id */
	String ecrId = (String)programMap.get(SELECT_ID);
	/* Get ECRCoordinator */
	return new StringList(getLeadPerson(context,new String[] {ecrId,"role_ECRCoordinator"}));
	} 
	
}
