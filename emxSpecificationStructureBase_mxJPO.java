/*
** emxSpecificationStructureBase
**
** Copyright (c) 2007-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
*/
/*
* @quickreview LX6 18 Sep 12("Enhancement of import Existing Structure : filtering of the import list")
* @quickreview LX6 QYG 18 Jan 12 : recycling of a relationship
* @quickreview ZUD DJH 26 JUNE 2014 HL Sequence Order to Tree Order Migration
* @quickreview ZUD DJH 11 AUG  2014 IR-273919V6R2015 NHIV6R-041369: Title field does not get saved with in-line edition creation of Chapter and comment + Shows content field active for edition for chapter 
* @quickreview QYG     26 AUG  2014 IR-322642-3DEXPERIENCER2015x After adding Sub Requirement to a Requirement, Requirement List  page gets KO.
* @quickreview LX6	   10 JUN  2015	IR-375012-3DEXPERIENCER2015x Code is displayed in the Word document generated from "Export to Word".
* @quickreview JX5	   20 JUL  2015 Replace old Allocation Status icons by new Parenthood icons 
* @quickreview QYG     13 OCT  2015 IR-397018-3DEXPERIENCER2017x Japanese requirement name on title of pop-up page does not display properly   
* @quickreview ZUD     16:08:17 : Reserve/UnReserve Command for RMT Types in attribute Tab   
* @quickreview KIE1 ZUD  16:12:01: IR-466628-3DEXPERIENCER2017x:Unable to promote chapter with customized policy
* @quickreview ZUD  KIE1 07:03:2017  IR-488815-3DEXPERIENCER2017x: R419-FUN058646: On WebTop, from Edit menu TRM objects are getting created without "Title."
*/
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.Attribute;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.BusinessType;
import matrix.db.BusinessTypeList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Relationship;
import matrix.db.State;
import matrix.db.Vault;
import matrix.util.MatrixException;
import matrix.util.StringItr;
import matrix.util.StringList;

import org.apache.axis.encoding.Base64;

import com.dassault_systemes.i3dx.changelog.Userfact;
import com.dassault_systemes.i3dx.changelog.Userfact.UserFactType;
import com.dassault_systemes.requirements.reconciliation.ILReconciliationServices;
import com.dassault_systemes.requirements.ReqConstants;
import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.dassault_systemes.requirements.ReqStructureUtil;
import com.dassault_systemes.requirements.UnifiedAutonamingServices;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.TreeOrderUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.apps.requirements.RequirementsCommon;
import com.matrixone.apps.requirements.RequirementsUtil;
import com.matrixone.apps.requirements.SpecificationStructure;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;

/***********************************************************************************************************
Release Developer Reviewer  MM:DD:YYYY  Comment
2014    LX6       QYG       11:29:2012  IR-191273V6R2014  Synchronize seq Order during cut Operation (cut/paste functionality)   
2014    LX6       JX5       11:29:2012  IR-208608V6R2014  TestCase and Usecases are not copied while doing Import from existing structure 
2014    JX5       QYG       04:19:2013	Add relationship  type in expandTreeSortAndFilter
2014    LX6                 05:14:2013	IR-234463V6R2014  Importing of Parameter is KO from import existing command from Requirement specification structure. 
2014x   LX6                 08:28:2013	IR-251026V6R2014x NHIV6R-037278: Duplication of Tests Case and Use case is KO when None option is selected for anyone of them 
2014x   QYG                 09:09:2013  added DemoteFromReleaseHigherRevisionCheck check trigger to block the demote if a higher revision exists.
2015x   JX5                 01:31:2014  HL RMT Create Derivation links between Requirements -- Added Title attribute in Expand
2015x   JX5       QYG		24.04.2014	IR-291476-3DEXPERIENCER2015x Added context is not displayed in "Structure Display" of Requirement Specification
2016x   LX6          		12.18.2014	IR-341768-3DEXPERIENCER2016  No label is displayed on pop up dialogue box when user click on icons of the columns "Covered Requirements" and "Refining Requirements"
2016x   LX6                 04.01.2015  Revise behavior enhancement
2016x   JX5					05.29.2015  IR-370825-3DEXPERIENCER2016x : Create links to cover requirements take too much time to launch
2016x   HAT1	  ZUD		07.15.2015  IR-381800-3DEXPERIENCER2016x - Solving inconsistency with create link command is KO.  
2016x   HAT1	  ZUD		07.16.2015  IR-381584-3DEXPERIENCER2016x - Wrong Tree structure is displayed in Create Link to Covered Requirement and check consistency pop-up windows. 
2016x   ZUD       HAT1      08.26.2015  IR-395419-3DEXPERIENCER2016x - 	R418:STP:Unwanted error message is displayed after cut-> paste above with referenece to first object in the structure. and save operation. 
2016x   JX5					09.09.2015  IR-363767-3DEXPERIENCER2016x    Special Characters in the title field of Requirement Specification make Traceability authoring command KO.
2016x   KIE1      ZUD       09.08.2015  IR-395086-3DEXPERIENCER2016x - Relation between Target Specification or Chapter and sub-requirement cannot be seen 
2016x   ZUD       HAT1      09.16.2015  IR-397107-3DEXPERIENCER2016x: R418-STP: Wrong object sequence order is displayed after edit operation. 
2017x   QYG                 11:16:2015  IR-403622-3DEXPERIENCER2017x allow owner/admin to do unreserve/reserve   
2017x   JX5		  QYG		05:02:2016  Move reconciliation services to REQModeler
*************************************************************************************************************/
/**
 * Methods for expanding and normalizing a Specification Structure
 *
 * @author srickus
 * @version RequirementsManagement V6R2008-2.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxSpecificationStructureBase_mxJPO extends emxDomainObject_mxJPO
{
   /**
    * Variable objectId
    */
   public static final String OBJECT_ID = "objectId";
   /**
    * Variable objectList
    */
   public static final String OBJECT_LIST = "objectList";
   /**
    * Variable paramList
    */
   public static final String PARAM_LIST = "paramList";
   /**
    * Variable for Requirement String Resource Bundle
    */
   public static final String RESOURCE_BUNDLE_REQUIREMENTS_STR = "emxRequirementsStringResource";
   /**
    * Variable for Component String Resource Bundle
    */
   public static final String RESOURCE_BUNDLE_COMPONENTS_STR = "emxComponentsStringResource";
   /**
    * Variable for iconSmallReservedByUser
    */
   public static final String ICON_FILE_RESERVED_BY_USER = "../common/images/iconSmallReservedByUser.gif";
   /**
    * Variable for iconSmallReservedByOther
    */
   public static final String ICON_FILE_RESERVED_BY_OTHER = "../common/images/iconSmallReservedByOther.gif";
   /**
    * Variable for Icon Tooltip reserved by user
    */
   public static final String ICON_TOOLTIP_RESERVED_BY_USER = "emxRequirements.ReservedBy.User.ToolTip";
   /**
    * Variable for Icon Tooltip reserved by other user
    */
   public static final String ICON_TOOLTIP_RESERVED_BY_OTHER = "emxRequirements.ReservedBy.Other.ToolTip";
   //Added:19-Nov-09:kyp:V6R2010xHF1:Requirement Allocation Status Icon
   /**
    * Variable for Icon Allocation Requirement
    */
   public static final String ICON_REQUIREMENT_ALLOCATED = "../common/images/iconSmallLinkedObject.gif";
   /**
    * Variable for Icon Requirement Not allocated
    */
   public static final String ICON_REQUIREMENT_NOT_ALLOCATED = "../requirements/images/iconReqInfoTypeParenthoodValueNotAllocated.png";
   /**
    * Variable for Icon Tool tip Requirement Not Allocated Requirement
    */
   public static final String ICON_TOOLTIP_REQUIREMENT_NOT_ALLOCATED = "emxRequirements.AllocationStatus.NotAllocated.ToolTip";
   //End:V6R2010xHF1:Requirement Allocation Status Icon

   // Why aren't these already defined in DomainConstants!?!
   /**
    * Variable for Reserved
    */
   public static final String SELECT_RESERVED = "reserved";
   /**
    * Variable for Reserved By
    */
   public static final String SELECT_RESERVED_BY = "reservedby";
   /**
    * Variable for Reserved Start
    */
   public static final String SELECT_RESERVED_START = "reservedstart";
   /**
    * Variable for Reserved Comments
    */
   public static final String SELECT_RESERVED_COMMENT = "reservedcomment";
   /**
    * Variable for Equals operator
    */
   public static final String SYMB_EQUAL = " == ";
   /**
    * Variable for Not Equal Operator
    */
   public static final String SYMB_NOT_EQUAL = " != ";
   /**
    * Variable for Comma operator
    */
   public static final String SYMB_COMMA = ",";
   /**
    * Variable for Quote operator
    */
   public static final String SYMB_QUOTE = "'";
   /**
    * Variable for OR operator
    */
   public static final String SYMB_OR = " || ";
   /**
    * Variable for And Operator
    */
   public static final String SYMB_AND = " && ";
   /**
    * Variable for Open Parameter Operator
    */
   public static final String SYMB_OPEN_PARAN = "(";
   /**
    * Variable for Close Parameter Operator
    */
   public static final String SYMB_CLOSE_PARAN = ")";
   /**
    * Variable for Release State
    */
   public static final String strSymbReleaseState = "state_Release";
   /**
    * Variable for Obsolete State
    */
   public static final String strSymbObsoleteState = "state_Obsolete";
   /**
    * Variable for Preliminary State
    */
   public static final String strSymbPreliminaryState = "state_Private";
   /**
    * Variable for Draft State
    */
   public static final String strSymbDraftState = "state_InWork";
   /**
    * Variable for Review State
    */
   public static final String strSymbReviewState = "state_Frozen";
   /**
    * Variable for Active State
    */
   public static final String strSymbActiveState = "state_Active";

   protected static final String ADD            = "add";
   protected static final String CUT            = "cut";
   protected static final String RESEQUENCE     = "resequence";
   protected static final String MARKUP         = "markup";
   protected static final String EDITED         = "edited";
   protected static final String COLUMN         = "column";
   protected static final String PASTEASCHILD   = "paste-as-child";
   protected static final String PASTEBELOW     = "paste-below";
   protected static final String PASTEABOVE     = "paste-above";

	private static final String ICON_REQUIREMENT_VALID_FROM = "../requirements/images/iconReqInfoTypeParenthoodValueValidFrom.png";
	private static final String ICON_REQUIREMENT_VALID_TO = "../requirements/images/iconReqInfoTypeParenthoodValueValidTo.png";
	private static final String ICON_REQUIREMENT_INVALID_FROM = "../requirements/images/iconReqInfoTypeParenthoodValueInvalidFrom.png";
	private static final String ICON_REQUIREMENT_INVALID_TO = "../requirements/images/iconReqInfoTypeParenthoodValueInvalidTo.png";
	private static final String ICON_REQUIREMENT_SUSPECT_FROM = "../requirements/images/iconReqInfoTypeParenthoodValueSuspectFrom.png";
	private static final String ICON_REQUIREMENT_SUSPECT_TO = "../requirements/images/iconReqInfoTypeParenthoodValueSuspectTo.png";
	
	public static final String OBJECT_EDITABLE = "ObjEditable";
	public static final String ROW_EDITABLE = "RowEditable";
	public static final String SHOW = "show";
	public static final String READONLY = "readonly";
	
	public static final String SELECT_ATTRIBUTE_CONTENT_TEXT = "attribute[Content Text]";
	public static final String SELECT_ATTRIBUTE_TITLE = "attribute[Title]";
	public static final String SELECT_ATTRIBUTE_TREE_ORDER = "attribute[TreeOrder]";
	public static final String SELECT_KINDOF = "type.kindof";
	

	private static enum statusLink { none, Suspect, Valid, Invalid };
	private static enum directionLink { from, to, both };

   /**
    * Create a new Specification Structure object.
    *
    * @param context
    *                the eMatrix <code>Context</code> object
    * @param args
    *                holds no arguments
    * @return a emxSpecificationStructure object.
    * @throws Exception
    *                 if the operation fails
    */
   public emxSpecificationStructureBase_mxJPO(Context context, String[] args) throws Exception
   {
      super(context, args);
   }

   /**
    * Main entry point
    *
    * @param context
    *                context for this request
    * @param args
    *                holds no arguments
    * @return an integer status code (0 = success)
    * @exception Exception
    *                    when problems occurred
    */
   public int mxMain(Context context, String[] args) throws Exception
   {
      if (!context.isConnected())
      {
         String alertMsg = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.FeaturesCheckFailed"); 
         throw new Exception(alertMsg);
      }

      System.out.println("mxMain: args.length = " + args.length);

      for (int ii = 0; ii < args.length; ii++)
         System.out.println(ii + ".  " + args[ii]);

      return 0;
   }

   /**
    * Method shows Full Name of the Reservedby user only if the object has been Reserved.
    * This is necessary since PersonUtil.getFullName() throws an exception if the
    * input username is empty.
    * @deprecated Workaround no longer needed, since null values are rendered as empty strings.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds list of object Ids in a program map
    * @return Vector - the Fullname output strings (or '-' if not reserved)
    * @throws Exception if the operation fails
    * @since RequirementsManagement V6R2008-2.0
    */
   public static Vector getReservedbyFullName(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);
      MapList rowList = (MapList) programMap.get(OBJECT_LIST);

      int iRowCount = rowList.size();
      String[] rowIds = new String[iRowCount];
      StringList lstAtt = new StringList(SELECT_RESERVED_BY);
      Vector lstNames = new Vector(iRowCount);

      // Put all the row Ids into an array...
      for (int iCount = 0; iCount < iRowCount; iCount++)
      {
         // Get the object id for each object in the table...
         Object objList = rowList.get(iCount);
         rowIds[iCount] = (objList instanceof HashMap)?
                          (String) ((HashMap) rowList.get(iCount)).get(SELECT_ID):
                          (String) ((Hashtable) rowList.get(iCount)).get(SELECT_ID);
      }

      // Use the array of Ids to get all the reservedby names...
      BusinessObjectWithSelectList resByAtts = BusinessObject.getSelectBusinessObjectData(context, rowIds, lstAtt);
      int oRowCount = resByAtts.size();

      // Put each reservedby name into the output list of names...
      for (int oCount = 0; oCount < oRowCount; oCount++)
      {
         // Look up the full name only if the object is reserved by someone...
         String resBy = resByAtts.getElement(oCount).getSelectData(SELECT_RESERVED_BY);
         String resName = resBy == null || resBy.length() == 0? "-": PersonUtil.getFullName(context, resBy);
         lstNames.add(resName == null? resBy: resName);
      }

       return lstNames;
   }

   /**
    * Method shows Reserved by Owner/Other Icon if the object has been Reserved
    * @param context the eMatrix <code>Context</code> object
    * @param args holds list of object Ids in a program map
    * @return Vector - HTML output strings representing the reserve status icons
    * @throws Exception if the operation fails
    * @since RequirementsManagement V6R2008-2.0
    */
   public static Vector getReservedStatusIcon(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);
      MapList rowList = (MapList) programMap.get(OBJECT_LIST);
      String usrName = context.getUser();

      int iRowCount = rowList.size();
      String[] rowIds = new String[iRowCount];
      StringList lstAtt = new StringList(SELECT_RESERVED_BY);
                 lstAtt.addElement(SELECT_RESERVED_START);
                 lstAtt.addElement(SELECT_RESERVED_COMMENT);
      Vector lstIcons = new Vector(iRowCount);

      // Put all the row Ids into an array...
      for (int iCount = 0; iCount < iRowCount; iCount++)
      {
         // Get the object id for each object in the table...
         Object objList = rowList.get(iCount);
         rowIds[iCount] = (objList instanceof HashMap)?
                          (String) ((HashMap) rowList.get(iCount)).get(SELECT_ID):
                          (String) ((Hashtable) rowList.get(iCount)).get(SELECT_ID);
      }

      // Use the array of Ids to get all the reservedby names...
      BusinessObjectWithSelectList resByAtts = BusinessObject.getSelectBusinessObjectData(context, rowIds, lstAtt);
      int oRowCount = resByAtts.size();

      // Put each reservedby name into the output list of names...
      for (int oCount = 0; oCount < oRowCount; oCount++)
      {
         // Look up the full name only if the object is reserved by someone...
         String resName = resByAtts.getElement(oCount).getSelectData(SELECT_RESERVED_BY);
         String resDate = resByAtts.getElement(oCount).getSelectData(SELECT_RESERVED_START);
         String resComm = resByAtts.getElement(oCount).getSelectData(SELECT_RESERVED_COMMENT);
         //Added:16-Feb-09:OEP:V6R2010:RMT Bug:369640
         resComm=resComm.replaceAll("&","&amp;");
         //End:16-Feb-09:OEP:V6R2010:RMT Bug:369640
         String strHtml = " ";

         // Generate the program HTML output for objects that are reserved...
         if (resName != null && resName.length() >= 1)
         {
            boolean resByMe = resName.equals(usrName);
            String strIcon = resByMe? ICON_FILE_RESERVED_BY_USER: ICON_FILE_RESERVED_BY_OTHER;
            String strComm = "[" + resDate + "] " + resComm;

            strHtml = " <img src=\"" + strIcon + "\" border=\"0\"  align=\"middle\" " + "title=\"" + " " + strComm + "\"" + "/>";
         }

//         System.out.println("resName = '" + resName + "', strHtml = " + strHtml);
         lstIcons.add(strHtml);
      }

      return lstIcons;
   }

   /**
    * Method is to add Existing object in structure browser
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args JPO argument
    * @return String in XML format
    * @throws Exception if operation fails
    */
    public static String addExistingToStructure(Context context, String[] args)
      throws Exception
   {
        String xmlMessage = "";

      // Unpack the incoming arguments into a HashMap called 'programMap'
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      //SpecificationStructure.printIndentedMap(programMap);

      HashMap requestMap = (HashMap) programMap.get("reqMap");
      String[] objectIds = (String[])requestMap.get(OBJECT_ID);
      String[] findRowIds = (String[])requestMap.get("emxTableRowId");
      HashMap reqTableMap = (HashMap) programMap.get("reqTableMap");
      String[] treeRowIds = (String[]) reqTableMap.get("emxTableRowId");
      String[] strRels = (String[]) requestMap.get("Relationships");

      if (treeRowIds == null)
      {
         if (objectIds != null && objectIds.length == 1)
         {
            treeRowIds = new String[1];
            treeRowIds[0] = "|" + objectIds[0] + "||0";    // use the root Specification object
         }
         else
         {
            String errorMsg = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_REQUIREMENTS_STR, context.getLocale(), "emxRequirements.Alert.MissingSelection"); 
            throw(new FrameworkException(errorMsg));
         }
      }
      else if (treeRowIds.length > 1)
      {
         String errorMsg = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_REQUIREMENTS_STR, context.getLocale(), "emxRequirements.Alert.MultiSelectionNotAllowed"); 
         throw(new FrameworkException(errorMsg));
      }

      // Iterate through the selected search objects, connecting each one to the selected tree object:
      if (findRowIds != null)
      {
         for (int ii = findRowIds.length-1; ii >= 0; ii--)
         {
            boolean reSeq = (ii == 0);    // resequence the children if this is the last one in the loop
            xmlMessage += SpecificationStructure.insertNodeAtSelected(context, treeRowIds[0], findRowIds[ii], null, reSeq, null, strRels[ii]);
         }
      }

      return xmlMessage;
   }

   /**
    * Method is used to create sequence Order , so that the new req will be added as child by default
    * 1. CreateNewAndLink - Create new Requirement object and link the requirement with parent objectId as per operation i.e. Sub /Derived
    * 2. AddChild - Add Requirement as Child with parent selected id
    * 3. AddAbove - Add Requirement as Above with parent selected id
    * 4. AddBelow - Add Requirement as Below with parent selected id
    * @param context the eMatrix <code>Context</code> object
    * @param args JPO argument
    * @return String XML Message
    * @throws Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.PostProcessCallable
   public static String postCreateSetSequenceOrder(Context context, String[] args)
      throws Exception
   {
      String xmlMessage = "";

      // Unpack the incoming arguments into a HashMap called 'programMap'
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      //System.out.println("Inside postCreate: programMap = \n  " + programMap);

      // Get the 'paramMap' HashMap from the programMap (for the created Object ID)
      HashMap paramMap = (HashMap) programMap.get("paramMap");
      Map requestMap = (Map)programMap.get("requestMap");
      String newId = (String) paramMap.get(OBJECT_ID);
	String seqOrder = null;
	String treeOrder = null;
	String rowId = null;

	String strOperation = (String)programMap.get("operation");
	String strSubOperation = (String)programMap.get("subOperation");
	String strTargetIds = (String)programMap.get("targetIds");

	if ("CreateNewAndLink".equals(strOperation))
	{
	    // Finding the sequence order for each target
	    //DomainObject dmoParent = DomainObject.newInstance(context);
	    for (StringItr idsItr = new StringItr(FrameworkUtil.split(strTargetIds, "!")); idsItr.next();)
	    {
		rowId = idsItr.obj();

		StringList slIds = FrameworkUtil.split(rowId, "|");

		String strTargetId = (String)slIds.get(1);
		String strParentId = (String)slIds.get(2);

		if (strParentId != null && !"".equals(strParentId))
		{
		    //dmoParent.setId(strParentId);
			// Zud Deprecated code for Sequence order
		    //String SELECT_SEQ_ORDER = "relationship[" + ReqSchemaUtil.getSpecStructureRelationship(context) + "|to.id=='" + strTargetId + "'].attribute[" + ReqSchemaUtil.getSequenceOrderAttribute(context) + "]";
		    //seqOrder = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", true, strParentId, SELECT_SEQ_ORDER);
		    //seqOrder = dmoParent.getInfo (context, SELECT_SEQ_ORDER);
		    
		    // ZUD MQL for Tree Order
		    String SELECT_TREE_ORDER = "relationship[" + ReqSchemaUtil.getSpecStructureRelationship(context) + "|to.id=='" + strTargetId + "'].attribute[" + ReqSchemaUtil.getTreeOrderAttribute(context) + "]";
		    treeOrder = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", true, strParentId,  SELECT_TREE_ORDER);
		}

		if ("AddChild".equals(strSubOperation))
		{
		    // Don't pass the sequence order, so that the new req will be added as child by default
		    seqOrder = null;
		}
		else
		{
		    //TODO
		    rowId = strParentId + "|" + strParentId; // The connection should be with parent and not with selected object
		    if ("AddAbove".equals(strSubOperation))
		    {
			// Do nothing, the calculated sequence order is correct, the object will be added above the selected target object
		    	seqOrder = treeOrder + "| AddAbove" ;
		    }
		    else if ("AddBelow".equals(strSubOperation))
		    {
			// Increment the sequence order to add below the selected target object
			//seqOrder = String.valueOf(Integer.parseInt(seqOrder) + 1); // For making him child
			seqOrder = treeOrder + "| AddBelow" ;
		    }
		    else
		    {
			throw new Exception("Invalid subOperation '" + strSubOperation + "'");
		    }
		    rowId = "dummyRelId|" + rowId + "|"; // Dummy relid and level is passed as it is required in resequence logic.
		}

		xmlMessage += SpecificationStructure.insertNodeAtSelected(context, rowId, newId, seqOrder, true);
	    }//For each target id
	}
	else if ("CreateSubDerived".equals(strOperation) || "SpecStructureNewSubDerivedReq".equals(strOperation))
	{
		 String  strTableRowId = (String)requestMap.get("emxTableRowId");
		 //String[] tokens = strTableRowId.split("[|]", -1);
		 //strTableRowId = "|" + tokens[1] + "||0"; //force "Add Child";
		 String strSubMode = (String)requestMap.get("subMode");

		 String strRelName = "";
		 if ("ForSubRequirement".equalsIgnoreCase(strSubMode)) {
		    strRelName = ReqSchemaUtil.getSubRequirementRelationship(context);
		 }
		 else if ("ForDerivedRequirement".equalsIgnoreCase(strSubMode)) {
		    strRelName = ReqSchemaUtil.getDerivedRequirementRelationship(context);
		 }

		 //connect with parent requirement
		 //String newRelId = RequirementsCommon.connectObjects(context, strParentObjectId, newId, strRelName ,false);
         xmlMessage += SpecificationStructure.insertNodeAtSelected(context, strTableRowId, newId, null, true, null, strRelName);
         SAXBuilder builder = new SAXBuilder();
         builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
         builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
         builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
         String newRelId = builder.build(new StringReader(xmlMessage)).getRootElement().getAttributeValue("relId");

		 //update link status
		 String linkStatus = (String)requestMap.get("RequirementStatus");
		 DomainRelationship.setAttributeValue(context, newRelId, ReqSchemaUtil.getLinkStatusAttrubite(context), linkStatus);

		 //link with Decision
		 String strDecision = (String)requestMap.get("Decision");
		 String strSelected = (String)requestMap.get("DecisionAction");
		 RequirementsCommon.linkWithDecision(context, strDecision, strSelected, newId, newRelId);
	}
	else
	{
	    // Get the 'requestMap' HashMap from the programMap (for the selected Object ID)
	    rowId = (String) requestMap.get("emxTableRowId");

	    // Get the sequence order value from the create form, if available...
	    seqOrder = (String) requestMap.get("SequenceOrder");

	    xmlMessage += SpecificationStructure.insertNodeAtSelected(context, rowId, newId, seqOrder, true);
	}

	return(xmlMessage);
    }

   /**
    *  Method is used to check the selected object is reserved or not
    * @param context the eMatrix <code>Context</code> object
    * @param args JPO argument
    * @return integer value 0 for success
    * @throws Exception if operation fails
    */
   public static int checkForObjectReservation(Context context, String[] args)
      throws Exception
   {
      // The first argument is the objectId:
      String objId = args[0];

      //System.out.println("checkForObjectReservation(" + objId + ")");
      if (! "".equals(objId))
      {
         // Find the parent object...
         DomainObject domObject = DomainObject.newInstance(context, objId);
         //String type = domObject.getInfo(context, SELECT_TYPE);
         //System.out.println("object " + type + ": " + domObject.getName());

         // Stop right now if the selected object is reserved by someone else...
         String resBy = domObject.getInfo(context, SELECT_RESERVED_BY);
         if (!"".equals(resBy) && !resBy.equals(context.getUser()))
         {
         String[] errorArgs = new String[] {resBy};
         String errorMsg = MessageUtil.getMessage(context, null, "emxRequirements.Alert.ObjectReservedBy",
               errorArgs, null, context.getLocale(), RESOURCE_BUNDLE_REQUIREMENTS_STR);

            System.err.println("Selected Object is reserved by: " + resBy);
            throw(new MatrixException(errorMsg));
         }
      }

      return(0);
   }

   /**
    * Method is to check the selected parent reservation state
    * @param context the eMatrix <code>Context</code> object
    * @param args JPO argument
    * @return integer value 0 for success
    * @throws Exception if operation fails
    */
   public static int checkForParentReservation(Context context, String[] args)
      throws Exception
   {
      // The first argument is the fromObjectId:
      String fromId = args[0];

      //System.out.println("checkForParentReservation(" + fromId + ")");
      if (! "".equals(fromId))
      {
         // Find the parent object...
         DomainObject parObject = DomainObject.newInstance(context, fromId);
         //String type = parObject.getInfo(context, SELECT_TYPE);
         //System.out.println("from " + type + ": " + parObject.getName());

         // Stop right now if the selected object is reserved by someone else...
         String resBy = parObject.getInfo(context, SELECT_RESERVED_BY);
         if (!"".equals(resBy) && !resBy.equals(context.getUser()))
         {
         String[] errorArgs = new String[] {resBy};
         String errorMsg = MessageUtil.getMessage(context, null, "emxRequirements.Alert.ParentObjectReservedBy",
               errorArgs, null, context.getLocale(), RESOURCE_BUNDLE_REQUIREMENTS_STR);

            System.err.println("Parent Object is reserved by: " + resBy);
            throw(new MatrixException(errorMsg));
         }
      }

      return(0);
   }

   /**
    * Method is returning the Hashmap of post connected sequence order map
    * @param context the eMatrix <code>Context</code> object
    * @param args JPO arguments
    * @return HashMap having map of object ids with sequence order
    * @throws Exception if operation fails
    */
   public static HashMap postConnectSetSequenceOrder(Context context, String[] args)
      throws Exception
   {
	   /*  ++ ZUD Deprecating the Code
      // The first argument is the fromObjectId:
      String fromId = args[0];

      // A child object has been connected, normalize the order of all the children...
      if (fromId != null && !fromId.equals(""))
      {
         // Find the parent object...
         DomainObject parObject = DomainObject.newInstance(context, fromId);
         //String type = parObject.getInfo(context, SELECT_TYPE);
         //System.out.println("normalize children of " + type + ": " + parObject.getName());

         // Re-number all the relationships objects from 1...n
         SpecificationStructure.normalizeSequenceOrder(context, parObject);
      }
       ++ Deprecating the Code*/

      return(null);
   }

   /**
    * Method is returning the remove object ids having no sequence order
    * @param context the eMatrix <code>Context</code> object
    * @param args JPO arguments
    * @return HashMap having map of all removed object ids with sequence order
    * @throws Exception if operation fails
    */
   public static HashMap postRemoveResetSequenceOrder(Context context, String[] args)
      throws Exception
   {
	   /*  ++ ZUD Deprecating the Code
      // The first argument is the fromObjectId:
      String fromId = args[0];

      // Child object(s) have been removed, reset the parent link sequence...
      if (fromId != null && !fromId.equals(""))
      {
         // Find the parent object...
         DomainObject parObject = DomainObject.newInstance(context, fromId);
         //String type = parObject.getInfo(context, SELECT_TYPE);
         //System.out.println("reset parent object: " + parObject.getName() + ", type = " + type);

         // Re-number all the relationships objects from 1...n
         SpecificationStructure.normalizeSequenceOrder(context, parObject);
      }
    ++ Deprecating the Code */
      return(null);
   }

   /**
    *  Reserves a Requirement Specification structure.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args JPO args holding requestMap, which contains rowIds as a comma separated list of
    *             Requirement Specification tableRowIds, comments, reserveSub and reserveDerived
    *             indicating whether Sub & Derived Requirements should be reserved.
    * @return HashMap a HashMap contains Action of "continue" or "ERROR".
    * @throws Exception if the operation fails
    */
   @com.matrixone.apps.framework.ui.PostProcessCallable
   public static HashMap commandReserveTree(Context context, String[] args)
      throws Exception
   {
      // unpack the incoming arguments into a HashMap called 'programMap'
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
//      System.out.println("Inside ReserveTree: programMap = \n  " + programMap);

      // get the 'requestMap' HashMap from the programMap
      HashMap requestMap = (HashMap) programMap.get("requestMap");
      boolean reserveSub = "Yes".equalsIgnoreCase((String)requestMap.get("reserveSub"));
      boolean reserveDerived = "Yes".equalsIgnoreCase((String)requestMap.get("reserveDerived"));
      boolean reserveParam = "Yes".equalsIgnoreCase((String)requestMap.get("reserveParam"));
      Object rparam = requestMap.get("rowIds");
      String[] rowIds = new String[1];
      if (rparam instanceof String[])
         rowIds = (String[]) rparam;
      else
         rowIds[0] = (String) rparam;

      // The emxForm.jsp UpdateProgram passes all rowIds in a comma-separated list
      if (rowIds != null && rowIds.length == 1 && rowIds[0].indexOf(",") > 0)
         rowIds = rowIds[0].split(",");

      // ZUD Fix for Reserve command for attributes
      if(rowIds[0].compareTo("") == 0)
      {
    	  rowIds[0] = (String)requestMap.get("objectId");
      }
      // Get the comments from the Reserve form paramMap
      Object cparam = requestMap.get("comments");
      String[] comments = new String[1];
      if (cparam instanceof String[])
         comments = (String[]) cparam;
      else
         comments[0] = (String) cparam;

      String comment = (comments == null || comments.length == 0? null: comments[0]);
      return modifyTreeReserveOrUnreserve(context, rowIds, comment, true, reserveSub, reserveDerived, reserveParam);
   }

   /**
    *  Unreserves a Requirement Specification structure.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param rowIds a comma separated list of Requirement Specification tableRowIds.
	* @return HashMap map of reserved and unreserved list
    * @throws Exception if the operation fails
    */
   public static HashMap commandUnreserveTree(Context context, String[] rowIds)
      throws Exception
   {
      return(modifyTreeReserveOrUnreserve(context, rowIds, null, false, false, false, false));
   }

   /**
    *  Unreserves a Requirement Specification structure.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args JPO args holding requestMap, which contains rowIds as a comma separated list of
    *             Requirement Specification tableRowIds, unreserveSub and unreserveDerived
    *             indicating whether Sub & Derived Requirements should be reserved ("Yes"|"No").
    * @return HashMap a HashMap contains Action of "continue" or "ERROR".
    * @throws Exception if the operation fails
    * @since R2012x
    */
   @com.matrixone.apps.framework.ui.PostProcessCallable
   public static HashMap commandUnreserveExtendedTree(Context context, String[] args)
   throws Exception
{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		boolean unreserveSub = "Yes".equalsIgnoreCase((String) requestMap.get("unreserveSub"));
		boolean unreserveDerived = "Yes".equalsIgnoreCase((String) requestMap.get("unreserveDerived"));
		boolean unreserveParam = "Yes".equalsIgnoreCase((String) requestMap.get("unreserveParam"));
		Object rparam = requestMap.get("rowIds");
		String[] rowIds = new String[1];
		if (rparam instanceof String[])
			rowIds = (String[]) rparam;
		else
			rowIds[0] = (String) rparam;

		// The emxForm.jsp UpdateProgram passes all rowIds in a comma-separated list
		if (rowIds != null && rowIds.length == 1 && rowIds[0].indexOf(",") > 0)
			rowIds = rowIds[0].split(",");
		
		// ZUD Fix for Reserve command for attributes
	      if(rowIds[0].compareTo("") == 0)
	      {
	    	  rowIds[0] = (String)requestMap.get("objectId");
	      }
	      
		return modifyTreeReserveOrUnreserve(context, rowIds, null, false, unreserveSub, unreserveDerived, unreserveParam);
}

   /**
    *  Reserve or Unreserve a Requirement Specification structure.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param rowIds Array of Requirement Specification tableRowIds.
    * @param comment reserve comment
    * @param flag true for Reserve, false for Unreserve
    * @param includeSub true to include Sub Requirements in the operation
    * @param includeDerived true to include Derived Requirements in the operation
    * @param includeParam true to include Parameters in the operation
    * @return HashMap a HashMap contains Action of "continue" or "ERROR".
    * @throws Exception if the operation fails
    */
   private static HashMap modifyTreeReserveOrUnreserve(Context context, String[] rowIds, String comment, boolean flag, boolean includeSub, boolean includeDerived, boolean includeParam)
      throws FrameworkException
   {
      HashMap status = new HashMap();
      status.put("Action", "continue");
      int success = 0;
      if (rowIds != null)
      {
         for (int ii = 0; ii < rowIds.length; ii++)
         {
            String objId = rowIds[ii];

            // The list of rowIds from the indentedTable.jsp is of the form: relId|objId|parId|x,y
            if (objId.indexOf("|") >= 0)
            {
               // Extract the objectId and parentId from the emxTableRowId:
               String[] tokens = rowIds[ii].split("[|]");
               objId = tokens[1];
            }

            try
            {
               // Begin a transaction frame, in case part of the tree cannot be removed.
               ContextUtil.startTransaction(context, true);

               // Set the Reserve flag on the selected object and any unreserved children...
               DomainObject selObject = DomainObject.newInstance(context, objId);
               modifyTreeSetReserveFlag(context, selObject, comment, flag, includeSub, includeDerived, includeParam);

               // Commit the transaction, since there were no problems.
               ContextUtil.commitTransaction(context);
               success++;
            }
            catch (MatrixException mex)
            {
               // Rollback the whole transaction:
               ContextUtil.abortTransaction(context);

               // Send the status message back to the UI...
               String mess = mex.getMessage();
               status.put("Action", "ERROR");
               status.put("Message", mess == null? mex.toString(): mess);
            }
            catch (Exception ex)
            {
               // Rollback the whole transaction:
               ContextUtil.abortTransaction(context);
               throw new FrameworkException(ex.getMessage());
            }
         }
         if(success > 0)
         {
        	 status.put("Refresh", "true");
         }
      }

      return(status);
   }

   /**
    * @param context
    * @param treeObject
    * @param comment
    * @param resFlag
    * @throws FrameworkException
    * @throws MatrixException
    */
   private static void modifyTreeSetReserveFlag(Context context, DomainObject treeObject, String comment, boolean resFlag, boolean includeSub, boolean includeDerived, boolean includeParam)
      throws FrameworkException, MatrixException, Exception
   {
      String objId = treeObject.getInfo(context, SELECT_ID);
      String objType = treeObject.getInfo(context, SELECT_TYPE);
      String resAtt = treeObject.getInfo(context, SELECT_RESERVED);
      String resBy = treeObject.getInfo(context, SELECT_RESERVED_BY);
      String resStr = ("" + resFlag).toUpperCase();
      
      String project = treeObject.getInfo(context, "project");
      
      String sc = context.getRole(); //context.getSession().getRole();
      
      String role = "";
      String contextProject = "";
      if(sc != null && sc.startsWith("ctx::")) {
    	  sc = sc.substring(5);
    	  String[] roles = sc.split("[\\.]");
    	  if(roles.length == 3 ) {
    		  role = roles[0];
    		  contextProject = roles[2];
    	  }
      }
      
      boolean isAdmin = "VPLMAdmin".equals(role);
      boolean isProjAdmin =  "VPLMProjectAdministrator".equals(role);
      

      // Stop right now if the selected object is reserved by someone else...
      if (resAtt.equals("TRUE") && !resBy.equals(context.getUser()) && !(isProjAdmin && contextProject.equals(project)) && !isAdmin)
      {
         String[] errorArgs = new String[] {resBy};
         String errorMsg = MessageUtil.getMessage(context, null, "emxRequirements.Alert.ObjectReservedBy",
               errorArgs, null, context.getLocale(), RESOURCE_BUNDLE_REQUIREMENTS_STR);

         System.err.println("Object is reserved by: " + resBy);
         throw(new MatrixException(errorMsg));
      }
      // BUG: #368981: Stop if Unreserve is called on an object that is NOT reserved.
      else if (resAtt.equals("FALSE") && !resFlag)
      {
         //IR-035064V6R2011: do not throw error message when unreserving a non-reserved object
      }

      // Unreserve the selected object only if it is reserved...
      if (!resFlag && "TRUE".equals(resAtt))
         treeObject.unreserve(context);
      // Reserve (or re-Reserve) the object with the new comment...
      if(resFlag)
         treeObject.reserve(context, comment);

      {
    	 String expRel =  ReqSchemaUtil.getSpecStructureRelationship(context);
    	 if(includeSub)
    	 {
    		 expRel += "," + ReqSchemaUtil.getSubRequirementRelationship(context);
    	 }
    	 if(includeDerived)
    	 {
		 	expRel += "," + ReqSchemaUtil.getDerivedRequirementRelationship(context);
    	 }
		 if(includeParam)
    	 {
			//LX6: Set Parametrized requirements as a GA feature
			 expRel += "," + ReqSchemaUtil.getParameterUsageRelationship(context);
    	 }
		 
		 String whrStr = "";
		 if(!isAdmin) {
			 whrStr = "reserved == FALSE || reservedby == \"" + context.getUser() + "\"";
			 if(isProjAdmin) {
				 whrStr += " || project == \"" + contextProject + "\"";
			 }
		 }
         String expStr = "expand bus $1 from rel $2 recurse to all select bus $3 where $4 dump $5 terse";
         String outStr = MqlUtil.mqlCommand(context, expStr, objId, expRel, "reserved", whrStr, "|");

         if (outStr != null)
         {
            StringTokenizer stoker = new StringTokenizer(outStr, "|\n");
            while (stoker.hasMoreTokens())
            {
               String lev = stoker.nextToken();
               String rel = stoker.nextToken();
               String dir = stoker.nextToken();
               String oid = stoker.nextToken();
               String res = stoker.nextToken();

               if (!resStr.equals(res))
               {
                  DomainObject subObject = DomainObject.newInstance(context, oid);

                  if (resFlag)
                     subObject.reserve(context, comment);
                  else
                     subObject.unreserve(context);
               }
            }
         }
      }
   }

   /**
    * Delete the selected child objects
    * @param context the eMatrix <code>Context</code> object
    * @param rowIds array of selected row ids
    * @return HashMap list of objects which are deleted
    * @throws Exception if operation fails
    */
   public static HashMap commandDeleteTree(Context context, String[] rowIds)
      throws Exception
   {
      HashMap status = new HashMap();

      if (rowIds != null)
      {
         for (int ii = 0; ii < rowIds.length; ii++)
         {
            String relId = null;
            String objId = rowIds[ii];

            // The list of rowIds from the indentedTable.jsp is of the form: relId|objId|parId|x,y
            if (objId.indexOf("|") >= 0)
            {
               // Extract the objectId and parentId from the emxTableRowId:
               String[] tokens = rowIds[ii].split("[|]");
               relId = tokens[0];
               objId = tokens[1];
            }

            try
            {
               // Begin a transaction frame, in case part of the tree cannot be removed.
               ContextUtil.startTransaction(context, true);

               // If necessary, disconnect the selected object before proceeding...
               if (relId != null && !relId.equals(""))
               {
                  // Make sure this relationship still exists...
                  DomainRelationship relCheck = new DomainRelationship(relId);
                  try
                  {
                     relCheck.open(context);
                  }
                  catch (MatrixException me)
                  {
                     // This relationship no longer exists, so we don't need to remove it.
                     relCheck = null;
                  }

                  if (relCheck != null)
                     relCheck.remove(context);
               }

               // Make sure the selected object still exists...
               DomainObject selObject;
               try
               {
                  selObject = DomainObject.newInstance(context, objId);
               }
               catch (MatrixException me)
               {
                  // This object no longer exists, so we don't need to delete it.
                  selObject = null;
               }

               // Delete the selected object and all its children...
               if (selObject != null)
                  modifyTreeDeleteOrDisconnect(context, selObject);

               // Commit the transaction, since there were no problems.
               ContextUtil.commitTransaction(context);
            }
            catch (MatrixException mex)
            {
               // Rollback the whole transaction:
               ContextUtil.abortTransaction(context);

               // Send the status message back to the UI...
               String mess = mex.getMessage();
               status.put("Action", "ERROR");
               status.put("Message", mess == null? mex.toString(): mess);
            }
            catch (Exception ex)
            {
               // Rollback the whole transaction:
               ContextUtil.abortTransaction(context);
               throw new FrameworkException(ex.getMessage());
            }
         }
      }

      return(status);
   }

   private static void modifyTreeDeleteOrDisconnect(Context context, DomainObject treeObject)
      throws FrameworkException, MatrixException, Exception
   {
      String objType = treeObject.getInfo(context, SELECT_TYPE);
      String objName = treeObject.getInfo(context, SELECT_NAME);
      String resAtt = treeObject.getInfo(context, SELECT_RESERVED);
      String resBy = treeObject.getInfo(context, SELECT_RESERVED_BY);

      // Stop right now if the selected object is reserved by someone else...
      if (resAtt.equals("TRUE") && !resBy.equals(context.getUser()))
      {
         String[] errorArgs = new String[] {resBy};
         String errorMsg = MessageUtil.getMessage(context, null, "emxRequirements.Alert.ObjectReservedBy",
               errorArgs, null, context.getLocale(), RESOURCE_BUNDLE_REQUIREMENTS_STR);

         System.err.println("Object is reserved by: " + resBy);
         throw(new MatrixException(errorMsg));
      }

      // Delete or Disconnect the root object, depending on whether it's used somewhere else...
      DomainObject specObject = null;
      String relTypes = ReqSchemaUtil.getExtendedSpecStructureRelationships(context);
      String objTypes = ReqSchemaUtil.getRequirementType(context) + "," +
                        ReqSchemaUtil.getChapterType(context) + "," +
                        ReqSchemaUtil.getCommentType(context);
      StringList objSelect = new StringList(SELECT_ID);
      StringList relSelect = new StringList(SELECT_RELATIONSHIP_ID);
      MapList parObjects = treeObject.getRelatedObjects(context, relTypes, "*",
            objSelect, relSelect, true, false, (short) 1, null, null);

      //System.out.println(objType + " " + objName + " has " + parObjects.size() + " parent(s)");
      if (parObjects.size() > 0)
      {
         //System.out.println(" It has been disconnected...");
      }
      else
      {
         // If the tree object is a container, get all the elements under it...
         MapList subObjects = treeObject.getRelatedObjects(context, relTypes, objTypes,
                  objSelect, relSelect, false, true, (short) 1, null, null);

         // We have the list of children, so now we can see about deleting the parent object...
         if (treeObject.isKindOf(context, ReqSchemaUtil.getRequirementSpecificationType(context)))
         {
            // Don't delete the root Specification, just disconnect its children:
            specObject = treeObject;

            if (subObjects.size() == 0)
               throw(new MatrixException(objType + " has no children to be deleted."));
         }
         else if (treeObject.isKindOf(context, ReqSchemaUtil.getRequirementType(context)))
         {
            // Delete Requirements only if they have no Sub or Derived reqs.
            boolean hasKids = treeObject.hasRelatedObjects(context, relTypes, true);
            //System.out.println(objType + " has child Objects? " + hasKids);
            if (!hasKids)
               treeObject.deleteObject(context);
         }
         else
         {
            //System.out.println(objType + " will be deleted...");
            treeObject.deleteObject(context);
         }

         // Recursively process the children, if any...
         //System.out.println(objType + " has " + subObjects.size() + " children.");
         for (int ii = 0; ii < subObjects.size(); ii++)
         {
            Map objMap = (Map) subObjects.get(ii);
            String subId = (String) objMap.get(SELECT_ID);
            DomainObject subObject = DomainObject.newInstance(context, subId);

            // Children of a Specification are disconnected so the root Spec is not deleted.
            if (specObject != null)
            {
               String relId = (String) objMap.get(SELECT_RELATIONSHIP_ID);
               Relationship relObject = DomainRelationship.newInstance(context, relId);

               specObject.disconnect(context, relObject);
            }

            modifyTreeDeleteOrDisconnect(context, subObject);
         }
      }
   }


   /**
    * Given a container element (Specification or Chapter) export a depth-first list of all
    * child Chapters, Comments, and Requirements expanded to all levels of the Specification Structure.
    *
    * @param context    The current Matrix Context
    * @param args 		rowIds An array of object Ids for the selected object(s)
    * @return           The list of Map objects representing rows of Matrix objects
    * @throws Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList exportTreeContentData(Context context, String[] args)
      throws Exception
   {
      MapList   outList = new MapList();

      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      HashMap paramMap = (HashMap) programMap.get("paramMap");
      //SpecificationStructure.printIndentedMap(paramMap);
      HashMap requestMap = (HashMap) programMap.get("requestMap");
      //SpecificationStructure.printIndentedMap(requestMap);
      String effectivityFilter = (String) programMap.get("CFFExpressionFilterInput_OID");
      String[] rowIds = (String[]) paramMap.get("rowIds");
      if (rowIds != null)
      {
         // From the href, get any other relationships to be expanded...
         String reqRels = (String) (requestMap == null? null: requestMap.get("exportRelationships"));
         String expRels = "";
         if (reqRels == null)
         {
            expRels = null;
         }
         else
         {
            String[] arrRels = reqRels.split(",");
            for (int ii = 0; ii < arrRels.length; ii++)
            {
               if (ii > 0)
                  expRels += ",";

               // Substitute the external name for the internal relationship name...
               expRels += PropertyUtil.getSchemaProperty(context,arrRels[ii]);
            }
         }

         for (int ii = 0; ii < rowIds.length; ii++)
         {
            String objId = rowIds[ii];
            if (objId == null || objId.length() == 0)
               continue;

            // The list of rowIds from the indentedTable.jsp is of the form: relId|objId|parId|x,y
            if (objId.indexOf("|") >= 0)
            {
               String[] tokens = objId.split("[|]");
               objId = tokens[1];
            }

            // Put the root object information in the output list (for level=0)...
            DomainObject domObj = DomainObject.newInstance(context, objId);
            Map objInfo = buildObjectMap(context, domObj);
            objInfo.put(SELECT_LEVEL, "0");
            outList.add(objInfo);

            // Get all the objects under the root, sorted by SequenceOrder...
            int maxLevels = 0;  // expand all levels...
            MapList expList = expandTreeSortAndFilter(context, objId, expRels, null, null, maxLevels,effectivityFilter);

            // Add all the expanded objects to the output list...
            if (expList != null)
            {
               for (int jj = 0; jj < expList.size(); jj++)
               {
                  objInfo = (Map) expList.get(jj);
                  objId = (String) objInfo.get("id");

                  // Multi-level expand returns an extra element with no 'id' - skip it
                  if (objId != null)
                  {
                     domObj = DomainObject.newInstance(context, objId);
                     objInfo.putAll(buildObjectMap(context, domObj));

                     String relId = (String)objInfo.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                     if(relId != null)
                     {
                    	 DomainRelationship relObj = DomainRelationship.newInstance(context, relId);
                    	 objInfo.putAll(buildRelationshipMap(context, relObj));
                     }

                     outList.add(objInfo);
                  }
               }
            }
         }
      }

      SpecificationStructure.printIndentedList("Tree Content Data:", outList);
      return(outList);
   }

   private static HashMap buildRelationshipMap(Context context, DomainRelationship relObj)
   {
      HashMap   objectMap = new HashMap();
      try
      {
    	 relObj.open(context);

         Map tempMap = relObj.getAttributeMap(context, false);
         relObj.close(context);

         Object[] atts = tempMap.keySet().toArray();
         for (int ii = 0; ii < atts.length; ii++)
         {
            String val = (String) tempMap.get(atts[ii]);

            objectMap.put("attribute[" + atts[ii]+ "]", val);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(System.err);
      }
      return objectMap;
   }
   private static HashMap buildObjectMap(Context context, DomainObject domainObj)
   {
      HashMap   objectMap = new HashMap();
      try
      {
         domainObj.open(context);
         objectMap.put(SELECT_ID, domainObj.getId());
         objectMap.put(SELECT_TYPE, domainObj.getTypeName());
         objectMap.put(SELECT_NAME, domainObj.getName());
         objectMap.put(SELECT_REVISION, domainObj.getRevision());
         objectMap.put(SELECT_VAULT, domainObj.getVault());

         objectMap.put(SELECT_DESCRIPTION, domainObj.getDescription(context));
         objectMap.put(SELECT_OWNER, domainObj.getOwner(context));
         objectMap.put(SELECT_MODIFIED, domainObj.getModified(context));
         objectMap.put(SELECT_CURRENT, domainObj.getInfo(context, SELECT_CURRENT));
         objectMap.put(SELECT_RESERVED_BY, domainObj.getInfo(context, SELECT_RESERVED_BY));

         Map tempMap = domainObj.getAttributeMap(context, false);
         domainObj.close(context);

         Object[] atts = tempMap.keySet().toArray();
         for (int ii = 0; ii < atts.length; ii++)
         {
            String val = (String) tempMap.get(atts[ii]);

            if (atts[ii].equals("Content Data") || atts[ii].equals("Content Text"))
            {
               if (val.length() > 0)
               {
                  if (atts[ii].equals("Content Text"))
                  {
                     // Base64-encode the Content Text data to ensure valid Xml CDATA syntax:
                     val = Base64.encode(val.getBytes("UTF-8"));
                  }

                  // Note: Content Data is already b64 encoded in the database, so just return it...
               }
               else
               {
                  // Don't bother returning empty Content attributes...
                  continue;
               }
            }

            objectMap.put(atts[ii], val);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(System.err);
      }
      return objectMap;
   }


   /**
    * Given a container element (Specification or Chapter) expand one level to get a list of all
    * child Chapters, Comments, and Requirements.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
    * @throws 			Exception is operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithAllRequirements(Context context, String[] args)
      throws Exception
   {
      return expandTreeSortAndFilter(context, args, null, null);
   }

   /**
    * Given a container element (Specification or Chapter) expand one level to get a list of all
    * child Chapters and Comments, and any Requirements where <i>Classification='Functional'</i>.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
    * @throws			Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithFunctionalRequirements(Context context, String[] args)
      throws Exception
   {
      return expandTreeSortAndFilter(context, args, ReqSchemaUtil.getRequirementClassificationAttribute(context), "Functional");
   }

   /**
    * Given a container element (Specification or Chapter) expand one level to get a list of all
    * child Chapters and Comments, and any Requirements where <i>Classification='Non-Functional'</i>.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
    * @throws			Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithNonFunctionalRequirements(Context context, String[] args)
      throws Exception
   {
      return expandTreeSortAndFilter(context, args, ReqSchemaUtil.getRequirementClassificationAttribute(context), "Non-Functional");
   }

   /**
    * Given a container element (Specification or Chapter) expand one level to get a list of all
    * child Chapters and Comments, and any Requirements where <i>Classification='Constraint'</i>.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
    * @throws 			Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithConstraintRequirements(Context context, String[] args)
      throws Exception
   {
      return expandTreeSortAndFilter(context, args, ReqSchemaUtil.getRequirementClassificationAttribute(context), "Constraint");
   }

   /**
    * Given a container element (Specification or Chapter) expand one level to get a list of all
    * child Chapters and Comments, and any Requirements where <i>Classification='None'</i>.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
    * @throws 			Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithUnclassifiedRequirements(Context context, String[] args)
      throws Exception
   {
      return expandTreeSortAndFilter(context, args, ReqSchemaUtil.getRequirementClassificationAttribute(context), "None");
   }

   /**
    * Given a container element (Specification or Chapter) expand to get a list of all
    * child Chapters, Comments, Requirements and derived Requirements.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
	* @throws			Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithDerivedRequirements(Context context, String[] args)
   throws Exception
   {
	   return expandTreeSortAndFilter(context, args, ReqSchemaUtil.getDerivedRequirementRelationship(context), null, null);
   }

   /**
    * Given a container element (Specification or Chapter) expand to get a list of all
    * child Chapters, Comments, Requirements and sub Requirements.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
	* @throws			Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithSubRequirements(Context context, String[] args)
   throws Exception
   {
	   return expandTreeSortAndFilter(context, args, ReqSchemaUtil.getSubRequirementRelationship(context), null, null);
   }

   /**
    * Given a container element (Specification or Chapter) expand to get a list of all
    * child Chapters, Comments, Requirements, sub and derived Requirements.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
	* @throws			Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithSubDerivedRequirements(Context context, String[] args)
   throws Exception
   {   
	   return expandTreeSortAndFilter(context, args,
			   ReqSchemaUtil.getDerivedRequirementRelationship(context) + "," + ReqSchemaUtil.getSubRequirementRelationship(context), null, null);
   }

/**
    * Given a container element (Specification or Chapter) expand to get a list of all
    * child Chapters, Comments, Requirements and Parameters.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
	* @throws			Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithRefCopyObjects(Context context, String[] args)
   throws Exception
   {   
	   Map programMap = (HashMap) JPO.unpackArgs(args);
	   MapList ObjectList;
	   //START LX6 IR-208608V6R2014 TestCase and UseCase are not copied while doing Import from existing structure
       String RefCopyObjects = (String) programMap.get("RefCopyObjects");
	   String Relationships = "";
	   if(RefCopyObjects != null)
	   {
		   if(RefCopyObjects.contains( ReqSchemaUtil.getSubRequirementRelationship(context)) == true)
		   {
			   Relationships += ReqSchemaUtil.getSubRequirementRelationship(context)+ ",";
		   }
		   if(RefCopyObjects.contains( ReqSchemaUtil.getDerivedRequirementRelationship(context) ) == true)
		   {
			   Relationships += ReqSchemaUtil.getDerivedRequirementRelationship(context) + ",";
		   }
//START : LX6 IR-251026V6R2014x NHIV6R-037278: Duplication of Tests Case and Use case is KO when None option is selected for anyone of them
		   if(RefCopyObjects.contains( ReqSchemaUtil.getTestCaseType(context)) == true)
		   {
			   Relationships += ReqSchemaUtil.getRequirementValidationRelationship(context)+ ",";
			   Relationships += ReqSchemaUtil.getSubUseCaseRelationship(context)+ ",";
		   }
		   if(RefCopyObjects.contains( ReqSchemaUtil.getUseCaseType(context) ) == true)
		   {
			   Relationships += ReqSchemaUtil.getRequirementUseCaseRelationship(context)+ ",";
			   Relationships += ReqSchemaUtil.getSubTestCaseRelationship(context);
		   }
//END : LX6 IR-251026V6R2014x NHIV6R-037278: Duplication of Tests Case and Use case is KO when None option is selected for anyone of them		   
		   if(RefCopyObjects.contains(ReqSchemaUtil.getParameterType(context)) == true)
		   {
			   Relationships += ",";
			   Relationships += ReqSchemaUtil.getParameterUsageRelationship(context);
			   Relationships += ",";
			   Relationships += ReqSchemaUtil.getParameterAggregationRelationship(context);
		   }		   
		   ObjectList = expandTreeSortAndFilter(context, args, Relationships, null, null); 
	   }
	   else
	   {
		   ObjectList = expandTreeWithSubDerivedRequirements(context, args);
	   }
	   return ObjectList;
	 //END LX6 IR-208608V6R2014 TestCase and Usecases are not copied while doing Import from existing structure
   }

   /**
    * Given a container element (Specification or Chapter) expand to get a list of all
    * child Chapters, Comments, Requirements; In addition, sub and derived Requirements
    * are added to the result based on user preferences.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
	* @throws			Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithPreference(Context context, String[] args)
   throws Exception
   {
	   String expandRel = "";
	   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_STRUCTURE_COMPARE_INC_DERIVED_REQ) ? ReqSchemaUtil.getDerivedRequirementRelationship(context) : "";
	   expandRel += expandRel.length() > 0 ? "," : "";
	   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_STRUCTURE_COMPARE_INC_SUB_REQ) ? ReqSchemaUtil.getSubRequirementRelationship(context) : "";
	   return expandTreeSortAndFilter(context, args, expandRel, null, null);
   }
   
   /**
    * Given a container element (Specification or Chapter) expand to get a list of all
    * child Chapters, Comments, Requirements; In addition, sub and derived Requirements and Parameters
    * are added to the result based on user preferences.
    *
    * @param context    The current Matrix Context
    * @param args       The packed Matrix program args, containing:
    *                   <li> the selected <i>objectId</i> (required)
    * @return           The MapList of Map objects representing rows of Matrix objects
	* @throws			Exception if operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public static MapList expandTreeWithParametersPreference(Context context, String[] args)
   throws Exception
   {
	   String expandRel = "";
	   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_STRUCTURE_COMPARE_INC_DERIVED_REQ) ? ReqSchemaUtil.getDerivedRequirementRelationship(context) : "";
	   expandRel += expandRel.length() > 0 ? "," : "";
	   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_STRUCTURE_COMPARE_INC_SUB_REQ) ? ReqSchemaUtil.getSubRequirementRelationship(context) : "";
	 //LX6: Set Parametrized requirements as a GA feature
	   expandRel += expandRel.length() > 1 ? "," : "";
	   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_STRUCTURE_COMPARE_INC_PARAMETERS) ? ReqSchemaUtil.getParameterUsageRelationship(context) : "";
	   return expandTreeSortAndFilter(context, args, expandRel, null, null);
	   
   }
   
   private static MapList expandTreeSortAndFilter(Context context, String[] args, String filterAtt, String filterVal) throws Exception
   {
	   return expandTreeSortAndFilter(context, args, null, filterAtt, filterVal);
   }

   private static MapList expandTreeSortAndFilter(Context context, String[] args, String expandRels, String filterAtt, String filterVal)
      throws Exception
   {
      // Unpack the Program Arguments
      Map programMap = (HashMap) JPO.unpackArgs(args);
      String objectId = (String) programMap.get(OBJECT_ID);
      String customExpLevel = (String)programMap.get("customExpandLevel");
      String expLevel = (String)programMap.get("expandLevel");
      String isRMB = (String)programMap.get("isRMB");
      if(customExpLevel != null&&!customExpLevel.isEmpty()){
    	  expLevel = customExpLevel;
      }
      //Added:20-Mar-09:kyp:R207:RMT Bug 361315
      if (expLevel == null) {
          expLevel = (String)programMap.get("compareLevel");
      }
      //End:R207:RMT Bug 361315

      String  effectivityFilter = (String) programMap.get("CFFExpressionFilterInput_OID");
      
      
      
      String PrefCustomTypes = (String)programMap.get("RMTCustomTypes");
      if(PrefCustomTypes == null){
    	  PrefCustomTypes = PropertyUtil.getAdminProperty(context, PersonUtil.personAdminType, context.getUser(), "preference_expandTypes");
    	  if(PrefCustomTypes.isEmpty()){
    		  PrefCustomTypes = "TestCases,Parameters,SubRequirements";
    	  }
    	  if(expLevel==null || expLevel.length()==0){
    		  expLevel = PropertyUtil.getAdminProperty(context, PersonUtil.personAdminType, context.getUser(), "preference_expandFilter");
    	  }
      }
      if("All".equalsIgnoreCase(expLevel)){
    	  expLevel = "0";
      }
      if(expLevel == null || expLevel.length() == 0)
      {
    	  expLevel = "1";
      }
      /*if("true".equalsIgnoreCase(isRMB)){
    	  expLevel = (String)programMap.get("level");
    	  if(expLevel == null || expLevel.length() == 0)
          {
        	  expLevel = "0";
          }
      }*/
      int maxLevels = Integer.parseInt(expLevel);
      if(PrefCustomTypes!=null&&!PrefCustomTypes.equalsIgnoreCase("")){
    	  String[] customExpandRels = PrefCustomTypes.split(",");
    	  for(int i=0;i<customExpandRels.length;i++){
    		  if(customExpandRels[i].equalsIgnoreCase("TestCases")){
    			  expandRels += ","+ReqSchemaUtil.getRequirementValidationRelationship(context);
    		  }else if(customExpandRels[i].equalsIgnoreCase("Parameters")){
    			  expandRels += ","+ ReqSchemaUtil.getParameterAggregationRelationship(context);
    			  expandRels += ","+ ReqSchemaUtil.getParameterUsageRelationship(context);
    		  }else if(customExpandRels[i].equalsIgnoreCase("SubRequirements")){
    			  expandRels += ","+ ReqSchemaUtil.getSubRequirementRelationship(context);
    		  }
    	  }
      }
      
      MapList mlResult = expandTreeSortAndFilter(context, objectId, expandRels, filterAtt, filterVal, maxLevels,effectivityFilter);

      //Added:14-Nov-09:kyp:V6R2010xHF1:Sub/Derive Req Add Existing Enhancement
      boolean isFullTextSearch = "true".equalsIgnoreCase((String)programMap.get("fullTextSearch"));
      if (isFullTextSearch)
      {
		  String strObjectId = (String) programMap.get("objectId");
		  String strTblRowId = (String) programMap.get("emxTableRowId");

		  String[] strSplitobjectId = strTblRowId.split("[|]");
		  String selectObjectId = strSplitobjectId[1];

	  if (strObjectId != null && strObjectId.length() > 0){
	      StringList objSelects = new StringList(1);
	      objSelects.addElement(DomainConstants.SELECT_ID);
	      MapList strParentList = null;
	      int sRecurse = 0;

	      StringBuffer sbRelSelect = new StringBuffer();
	      sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubRequirementRelationship(context))
	      .append(",")
	      .append(ReqSchemaUtil.getDerivedRequirementRelationship(context));

	      DomainObject dom = new DomainObject(selectObjectId);
	      strParentList = dom.getRelatedObjects(context, sbRelSelect.toString(), ReqSchemaUtil.getRequirementType(context),
		      true, false, sRecurse, objSelects, null, null, null, null, null, null);

	      Map mapObject = new HashMap();
	      mapObject.put(DomainConstants.SELECT_ID, selectObjectId);
	      strParentList.add(mapObject);
	      // Build a list of IDs of all the Objects that should be excluded...
	      MapList mlresultClone = new MapList();
	      ArrayList listParentId = new ArrayList();
	      ArrayList listResultId = new ArrayList();
	      for (int ii = 0; ii < strParentList.size(); ii++)
	      {
		  Map strParentMap = (Map) strParentList.get(ii);
		  String parentId = (String) strParentMap.get(DomainConstants.SELECT_ID);
		  if(null!=parentId)
		  {
		      listParentId.add(parentId);
		  }
	      }
	      for(int index=0; index<mlResult.size(); index++)
	      {
		  Map mapResult = (Map)mlResult.get(index);
		  String strId = (String)mapResult.get(DomainConstants.SELECT_ID);
		  if(null!=strId && !listParentId.contains(strId))
		  {
		      mlresultClone.add(mapResult);
		  }
	      }
	      mlResult= mlresultClone;
	  }
    	  Integer fullTextObjCount = new Integer(mlResult.size()-1);
    	  if (mlResult.size() == 0)
    	  {
    		  fullTextObjCount = new Integer(0);
    	  }
    	  else
    	  {
    		  fullTextObjCount = new Integer(mlResult.size());

    		  int nLastElementIndex = fullTextObjCount - 1;
    		  Map mapLastElement = (Map)mlResult.get(nLastElementIndex);
    		  if (mapLastElement.containsKey("expandMultiLevelsJPO"))
    		  {
    			  fullTextObjCount--;
    		  }
    	  }

    	   mlResult.add(0, fullTextObjCount);
      }
      //End:V6R2010xHF1:Sub/Derive Req Add Existing Enhancement
      return mlResult;
   }

   private static MapList expandTreeSortAndFilter(Context context, String objectId, String filterAtt, String filterVal, int maxLevels, String effectivityFilter)
      throws Exception
   {
      return expandTreeSortAndFilter(context, objectId, null, filterAtt, filterVal, maxLevels,effectivityFilter);
   }

    private static MapList expandTreeSortAndFilter(Context context, String objectId, String expandRels,
            String filterAtt, String filterVal, int maxLevels, String effectivityFilter) throws Exception {
        MapList expandList = new MapList();

        if (objectId == null || objectId.length() == 0)
            return expandList;

        try {
            String usrName = context.getUser();
            DomainObject domObject = DomainObject.newInstance(context, objectId);

            // always set parent as editable, validation will be done at the
            // time of reorder
            Boolean parEditable = true;

            StringList objSelect = new StringList(SELECT_ID);
            objSelect.addElement(SELECT_TYPE);
            objSelect.addElement(SELECT_NAME);
            objSelect.addElement(SELECT_REVISION);
            objSelect.addElement(SELECT_DESCRIPTION);
            objSelect.addElement(SELECT_MODIFIED);
            objSelect.addElement(SELECT_CURRENT);
            objSelect.addElement(SELECT_RESERVED_BY);
            objSelect.addElement(ReqConstants.SELECT_READ_ACCESS);
            objSelect.addElement("current.access[modify]");
            //JX5 : Derivation Cmd
            objSelect.addElement("attribute[Title]");
            //

            StringList relSelect = new StringList(SELECT_RELATIONSHIP_ID);
            relSelect.addElement(SELECT_LEVEL);
            //zud check Tree Order
            relSelect.addElement("attribute[" + ReqSchemaUtil.getTreeOrderAttribute(context) + "]");
            //relSelect.add(DomainRelationship.SELECT_FROM_ID);
            
            // JX5
            relSelect.addElement(SELECT_RELATIONSHIP_TYPE);
            //

            if (filterAtt != null && filterAtt.length() > 0 && !filterAtt.equals("*")) {
                if (!filterAtt.startsWith("attribute["))
                    filterAtt = "attribute[" + filterAtt + "]";

                objSelect.addElement(filterAtt);
            }
            String relTypes = "";
            String objTypes = "*";
            
            // Get the child relationship objects...
            // LX6: Set Parametrized requirements as a GA feature
            // relTypes += ReqSchemaUtil.getParameterUsageRelationship(context) +
            // ",";
            // objTypes += ReqSchemaUtil.getParameterType(context) + ",";
            relTypes += ReqSchemaUtil.getSpecStructureRelationship(context)
                    + (expandRels == null ? "" : "," + expandRels);
            MapList relObjects;

            // BPS regression, temp fix
            if ("undefined".equalsIgnoreCase(effectivityFilter)) {
                effectivityFilter = null;
            }

            if (effectivityFilter == null || effectivityFilter.length() == 0) {
                relObjects = domObject.getRelatedObjects(context, relTypes, objTypes, objSelect, relSelect, false,
                        true, (short) maxLevels, "", "", 0);
            } else {
                relObjects = domObject.getRelatedObjects(context, relTypes, // relationship
                                                                            // pattern
                        objTypes, // object pattern
                        objSelect, // object selects
                        relSelect, // relationship selects
                        false, // to direction
                        true, // from direction
                        (short) maxLevels, // recursion level
                        null, // object where clause
                        null, // relationship where clause
                        (short) 0, // limit
                        CHECK_HIDDEN, // check hidden
                        PREVENT_DUPLICATES, // prevent duplicates
                        PAGE_SIZE, // pagesize
                        null, // includeType
                        null, // includeRelationship
                        null, // includeMap
                        null, // relKeyPrefix
                        effectivityFilter); // Effectivity filter expression
                                            // from the SB toolbar
            }

            // Build a new list of related objects that have only
            // missing|matching values...
            int objCount = relObjects.size();
            for (int jj = 0; jj < objCount; jj++) {
                Map mapObject = (Map) relObjects.get(jj);
                String strObjId = (String) mapObject.get(SELECT_ID);
                String strValue = (filterAtt == null ? null : (String) mapObject.get(filterAtt));
                String objResBy = (String) mapObject.get(SELECT_RESERVED_BY);

                // Check that the object is not reserved by another, and that it
                // is in a "Modifyable" state...
                boolean objEditable = (("".equals(objResBy) || usrName.equals(objResBy)) && "TRUE".equals(mapObject.get("current.access[modify]"))); 

                // Reject only objects that have values that don't match...
                if (filterAtt == null || strValue == null || strValue.length() == 0 || strValue.equals(filterVal)) {

                    // Mark the row as non-editable if someone else has this
                    // object reserved.
                    mapObject.put(OBJECT_EDITABLE, (objEditable ? SHOW : READONLY));

                    // Disable Cut/Copy/Paste edits if the parent object is
                    // "frozen"
                    mapObject.put("allowEdit", parEditable.toString());
                    expandList.add(mapObject);
                }
            }

            // ZUD Tree Order
            expandList.sortStructure(context, "relationship,attribute[TreeOrder]", ",", "string,real", ",");
            ReqStructureUtil.markLeafNodes(expandList, maxLevels);
            
            ReqStructureUtil.fillTypeInfo(context, expandList);
            fillParentId(context, expandList, objectId);
            
            int indent = -1;
            Stack statckEditable = new Stack();
            Access objAccessRoot = domObject.getAccessMask(context);
            boolean rootEditable = objAccessRoot.hasModifyAccess();
            Map mapRootObject = new HashMap<String, String>();
            mapRootObject.put(OBJECT_EDITABLE, rootEditable? SHOW : READONLY);
            
            statckEditable.push(mapRootObject);
            Map mapObjectFather = null;
            
            int sizeFilteredList = expandList.size();
            boolean rowEditable;
            for (int jj = 0; jj < sizeFilteredList; jj++) {
                Map mapObject = (Map) expandList.get(jj); 

                String relLevel = (String) mapObject.get(DomainConstants.SELECT_LEVEL);

                int level = 0;
                try {
                    level = Integer.parseInt(relLevel);
                } catch (Exception e) {
                    level = 0;
                }

                
                if (level > indent) {
                    indent = level;
                    mapObjectFather = (Map) statckEditable.peek();
                    rowEditable = SHOW.equals(mapObjectFather.get(OBJECT_EDITABLE)) || SHOW.equals(mapObject.get(OBJECT_EDITABLE));
                    mapObject.put(ROW_EDITABLE, rowEditable ? SHOW : READONLY);
                    statckEditable.push(mapObject);
                } else if (level == indent) {
                    Map M = (Map) statckEditable.pop();
                    rowEditable = SHOW.equals(mapObjectFather.get(OBJECT_EDITABLE)) || SHOW.equals(mapObject.get(OBJECT_EDITABLE));
                    mapObject.put(ROW_EDITABLE, rowEditable ? SHOW : READONLY);
                    statckEditable.push(mapObject);
                } else {
                    do {
                        Map M = (Map) statckEditable.pop();
                        indent--;
                    } while (level <= indent);
                    mapObjectFather = (Map) statckEditable.peek();
                    rowEditable = SHOW.equals(mapObjectFather.get(OBJECT_EDITABLE)) || SHOW.equals(mapObject.get(OBJECT_EDITABLE));
                    mapObject.put(ROW_EDITABLE, rowEditable ? SHOW : READONLY);
                    statckEditable.push(mapObject);
                }
            }

            HashMap hmTemp = new HashMap();
            hmTemp.put("expandMultiLevelsJPO", "true");
            expandList.add(hmTemp);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return expandList;
    }

    private static void fillParentId(Context context, MapList expList, String rootId) {
    	int indent = -1;
    	Stack path = new Stack();
    	Map<String, String> rootMap = new HashMap<String, String>();
    	rootMap.put(SELECT_ID, rootId);
    	path.push(rootMap);
    	for (int ii = 0; ii < expList.size(); ii++) {
    		Hashtable objMap = (Hashtable) expList.get(ii);
    		String relLevel = (String) objMap.get(SELECT_LEVEL);
    		int level = Integer.parseInt(relLevel);
    		if(level > indent){
    			indent = level;
    		}else if(level == indent){
    			path.pop();
    		}else{
    			do{
    				path.pop();
    				indent--;
    			}while(level < indent);
    			path.pop();
    		}
    		objMap.put("$PID", ((Map)path.peek()).get(SELECT_ID));
    		objMap.put(DomainRelationship.SELECT_FROM_ID, ((Map)path.peek()).get(SELECT_ID));
			path.push(objMap);
    	}

    }

   private static int[] unbox(Object[] array)
   {
      if (array == null)
         return(null);

      int[] result = new int[array.length];
      for (int ii = 0; ii < array.length; ii++)
         result[ii] = ((Integer) array[ii]).intValue();

      return result;
   }

   /**
    * Method is invoked to update the Sequence order
    * @param context the eMatrix <code>Context</code> object
    * @param args holds list of object Id and parent Id in a program map
    * @return HashMap
    * @throws Exception if the operation fails
    * @since RequirementsManagement V6R2008-2.0
    */
   public static HashMap updateSequenceOrder(Context context, String[] args)
      throws Exception
   {
	  //  ++ KIE1 ZUD  IR-395086-3DEXPERIENCER2016x: uncommented for creating connection
       // Unpack the  arguments
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       String newId = (String) programMap.get(OBJECT_ID);
       String strAllObjId = (String) programMap.get("selectedObjId");

       Map paramMap = (HashMap) programMap.get("paramMap");
       if(paramMap != null)
       {
    	   strAllObjId = (String) paramMap.get("New Value");
    	   newId = (String) paramMap.get(OBJECT_ID);
       }

       String objId = "";

       if (!"".equals(strAllObjId) && !"null".equals(strAllObjId) && strAllObjId!=null)
       {
           StringList objectIdList = FrameworkUtil.split(strAllObjId,",");
           //Iterating the loop and connecting newly created requirement to multiple Specifications
           for(int i=0;i<objectIdList.size();i++)
           {
               objId = (String)objectIdList.get(i);
               if (objId != null && newId != null)
               {
                   // Find the currently selected object...
                   DomainObject selObject = DomainObject.newInstance(context, objId);
                   // Find the newly created object...
                   DomainObject newObject = DomainObject.newInstance(context, newId);
                   // Create the new relationship...
                   DomainRelationship.connect(context, selObject, ReqSchemaUtil.getSpecStructureRelationship(context), newObject);
                   // Finally, re-number all the relationships objects from 1...n
                  // SpecificationStructure.normalizeSequenceOrder(context, selObject);
               }
           }
       }
       // -- KIE1 ZUD  IR-395086-3DEXPERIENCER2016x: uncommented for creating connection
       return(null);
   }


   /** Trigger Method to promote entire structure to Release
    * @param context - the eMatrix <code>Context</code> object
    * @param args - holds the Hashmap containing the object id.
    * @throws Exception if the operation fails
    * @since RequirementsManagement V6R2008-2.0
    */
   public void promoteToReleaseSpecStructure(Context context, String[] args)
       throws Exception
   {
       String strObjectId = args[0];

        try
       {
           String strRelPattern = RequirementsUtil.getSpecStructureRelationship(context);
           String strTypePattern = RequirementsUtil.getChapterType(context) + "," + RequirementsUtil.getCommentType(context);
           // ++ KIE1 ZUD IR-466628-3DEXPERIENCER2017x: Unable to promote chapter with customized policy
           StringList lstObjectSelects = new StringList(3);
           lstObjectSelects.addElement(SELECT_ID);
           lstObjectSelects.addElement(SELECT_TYPE);
           lstObjectSelects.addElement(SELECT_POLICY);
           //List lstRelSelects = new StringList(DomainConstantsSELECT_RELATIONSHIP_ID);
           boolean bGetTo = false;
           boolean bGetFrom = true;
           short sRecursionLevel = 0;
           String strBusWhereClause = "";
           String strRelWhereClause = "";
           String strReleaseChapter = "";
           String strReleaseComment = "";
           String strReleaseReqSpec = "";
        
           // initializing chapter, comment, reqSpeq policy with default 
           String strCommentPolicy 		 	= RequirementsUtil.getCommentPolicy(context);
           String strChapterPolicy 			= RequirementsUtil.getChapterPolicy(context);
           String strSpecificationPolicy 	= RequirementsUtil.getRequirementSpecificationPolicy(context);
           
           // DomainObject to fetch current object policy
           DomainObject domReq = DomainObject.newInstance(context,strObjectId);
           Map objectPolicy = domReq.getInfo(context, lstObjectSelects);
                      
           if(domReq.isKindOf(context,(RequirementsUtil.getChapterType(context))))
           {
        	   strChapterPolicy = (String)objectPolicy.get("policy"); // getting registered object policy
        	   strReleaseChapter = FrameworkUtil.lookupStateName(context, strChapterPolicy, strSymbReleaseState);
        	   strReleaseComment = FrameworkUtil.lookupStateName(context, strCommentPolicy, strSymbReleaseState);
               strReleaseReqSpec = FrameworkUtil.lookupStateName(context, strSpecificationPolicy, strSymbReleaseState);
           }
           
           if(domReq.isKindOf(context,(RequirementsUtil.getCommentType(context))))
           {
        	   strCommentPolicy = (String)objectPolicy.get("policy"); // getting registered object policy
        	   strReleaseChapter = FrameworkUtil.lookupStateName(context, strChapterPolicy, strSymbReleaseState);
        	   strReleaseComment = FrameworkUtil.lookupStateName(context, strCommentPolicy, strSymbReleaseState);
               strReleaseReqSpec = FrameworkUtil.lookupStateName(context, strSpecificationPolicy, strSymbReleaseState);
           }
           
           if(domReq.isKindOf(context,(RequirementsUtil.getRequirementSpecificationType(context))))
           {
        	   strSpecificationPolicy = (String)objectPolicy.get("policy"); // getting registered object policy
        	   strReleaseChapter = FrameworkUtil.lookupStateName(context, strChapterPolicy, strSymbReleaseState);
        	   strReleaseComment = FrameworkUtil.lookupStateName(context, strCommentPolicy, strSymbReleaseState);
               strReleaseReqSpec = FrameworkUtil.lookupStateName(context, strSpecificationPolicy, strSymbReleaseState);
           }
           
           StringBuffer sbWhereBus = new StringBuffer();

           // (((policy == 'Chapter') AND (current != 'Release')) OR ((policy == 'Comment') AND (current != 'Release')))
           sbWhereBus.append(SYMB_OPEN_PARAN);
           sbWhereBus.append(SYMB_OPEN_PARAN);
           sbWhereBus.append(SYMB_OPEN_PARAN);
           sbWhereBus.append(SELECT_POLICY);
           sbWhereBus.append(SYMB_EQUAL);
           sbWhereBus.append(SYMB_QUOTE);
           sbWhereBus.append(strChapterPolicy);
           sbWhereBus.append(SYMB_QUOTE);
           sbWhereBus.append(SYMB_CLOSE_PARAN);
           sbWhereBus.append(SYMB_AND);
           sbWhereBus.append(SYMB_OPEN_PARAN);
           sbWhereBus.append(SELECT_CURRENT);
           sbWhereBus.append(SYMB_NOT_EQUAL);
           sbWhereBus.append(SYMB_QUOTE);
           sbWhereBus.append(strReleaseChapter);
           sbWhereBus.append(SYMB_QUOTE);
           sbWhereBus.append(SYMB_CLOSE_PARAN);
           sbWhereBus.append(SYMB_CLOSE_PARAN);
           sbWhereBus.append(SYMB_OR);
           sbWhereBus.append(SYMB_OPEN_PARAN);
           sbWhereBus.append(SYMB_OPEN_PARAN);
           sbWhereBus.append(SELECT_POLICY);
           sbWhereBus.append(SYMB_EQUAL);
           sbWhereBus.append(SYMB_QUOTE);
           sbWhereBus.append(strCommentPolicy);
           sbWhereBus.append(SYMB_QUOTE);
           sbWhereBus.append(SYMB_CLOSE_PARAN);
           sbWhereBus.append(SYMB_AND);
           sbWhereBus.append(SYMB_OPEN_PARAN);
           sbWhereBus.append(SELECT_CURRENT);
           sbWhereBus.append(SYMB_NOT_EQUAL);
           sbWhereBus.append(SYMB_QUOTE);
           sbWhereBus.append(strReleaseComment);
           sbWhereBus.append(SYMB_QUOTE);
           sbWhereBus.append(SYMB_CLOSE_PARAN);
           sbWhereBus.append(SYMB_CLOSE_PARAN);
           sbWhereBus.append(SYMB_CLOSE_PARAN);

           strBusWhereClause = sbWhereBus.toString();

           
           MapList mapSubObjects = domReq.getRelatedObjects(context, strRelPattern, strTypePattern,
                 lstObjectSelects, null, bGetTo, bGetFrom, sRecursionLevel, strBusWhereClause, strRelWhereClause);
           
          
           ContextUtil.startTransaction(context, true);

           //promote all subobjects
           for(int i=0; i<mapSubObjects.size(); i++)
           {
               Map mapT=(Map)mapSubObjects.get(i);

               String strSubObjId = (String) mapT.get(SELECT_ID);
               String strSubObjPolicy = (String) mapT.get(SELECT_POLICY);
               DomainObject domSubObj = DomainObject.newInstance(context,strSubObjId);
              
			   if(domSubObj.isKindOf(context,(RequirementsUtil.getChapterType(context))))
                   domSubObj.setState(context, strReleaseChapter);
               else
                   domSubObj.setState(context, strReleaseComment);
           }
          
           if(domReq.isKindOf(context,(RequirementsUtil.getChapterType(context))))
               domReq.setState(context, strReleaseChapter);
           else
               domReq.setState(context, strReleaseReqSpec);

           ContextUtil.commitTransaction(context);
        // -- KIE1 ZUD IR-466628-3DEXPERIENCER2017x: Unable to promote chapter with customized policy
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
    }


    /** Trigger Method to demote entire structure from Release
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    public void demoteFromReleaseSpecStructure(Context context, String[] args)
        throws Exception
    {
        String strObjectId = args[0];

         try
        {
            String strRelPattern = RequirementsUtil.getSpecStructureRelationship(context);
            String strTypePattern = RequirementsUtil.getChapterType(context) + "," + RequirementsUtil.getCommentType(context);

            StringList lstObjectSelects = new StringList(3);
            lstObjectSelects.addElement(SELECT_TYPE);
            lstObjectSelects.addElement(SELECT_ID);
            lstObjectSelects.addElement(SELECT_POLICY);
            //List lstRelSelects = new StringList(DomainConstantsSELECT_RELATIONSHIP_ID);
            boolean bGetTo = false;
            boolean bGetFrom = true;
            short sRecursionLevel = 0;
            String strBusWhereClause = "";
            String strRelWhereClause = "";

            String strReleaseChapter = "";
            String strReleaseComment = "";
            String strActiveChapter = "";
            String strActiveComment = "";
            String strReviewReqSpec = "";
            
            // initializing chapter, comment, reqSpeq policy with default 
            String strCommentPolicy 		 	= RequirementsUtil.getCommentPolicy(context);
            String strChapterPolicy 			= RequirementsUtil.getChapterPolicy(context);
            String strSpecificationPolicy 		= RequirementsUtil.getRequirementSpecificationPolicy(context);
            
            // DomainObject to fetch current object policy
            DomainObject domReq = DomainObject.newInstance(context,strObjectId);
            Map objectPolicy = domReq.getInfo(context, lstObjectSelects);
            
            if(domReq.isKindOf(context,(RequirementsUtil.getChapterType(context))))
            {
               strChapterPolicy = (String)objectPolicy.get("policy"); // getting registered object policy
         	   strReleaseChapter = FrameworkUtil.lookupStateName(context, strChapterPolicy, strSymbReleaseState);
         	   strActiveChapter = FrameworkUtil.lookupStateName(context, strChapterPolicy, strSymbActiveState);
         	   strActiveComment = FrameworkUtil.lookupStateName(context, strCommentPolicy, strSymbActiveState);
         	   strReleaseComment = FrameworkUtil.lookupStateName(context, strCommentPolicy, strSymbReleaseState);
         	   strReviewReqSpec = FrameworkUtil.lookupStateName(context, strSpecificationPolicy, strSymbReviewState);
            }
            
            if(domReq.isKindOf(context,(RequirementsUtil.getCommentType(context))))
            {
            	strCommentPolicy = (String)objectPolicy.get("policy"); // getting registered object policy
            	strReleaseChapter = FrameworkUtil.lookupStateName(context, strChapterPolicy, strSymbReleaseState);
         	    strReleaseComment = FrameworkUtil.lookupStateName(context, strCommentPolicy, strSymbReleaseState);
         	    strActiveChapter = FrameworkUtil.lookupStateName(context, strChapterPolicy, strSymbActiveState);
                strActiveComment = FrameworkUtil.lookupStateName(context, strCommentPolicy, strSymbActiveState);
         	    strReviewReqSpec = FrameworkUtil.lookupStateName(context, strSpecificationPolicy, strSymbReleaseState);
            }
            
            if(domReq.isKindOf(context,(RequirementsUtil.getRequirementSpecificationType(context))))
            {
            	strSpecificationPolicy = (String)objectPolicy.get("policy"); // getting registered object policy
            	strReleaseChapter = FrameworkUtil.lookupStateName(context, strChapterPolicy, strSymbReleaseState);
         	    strReleaseComment = FrameworkUtil.lookupStateName(context, strCommentPolicy, strSymbReleaseState);
         	    strActiveChapter = FrameworkUtil.lookupStateName(context, strChapterPolicy, strSymbActiveState);
                strActiveComment = FrameworkUtil.lookupStateName(context, strCommentPolicy, strSymbActiveState);
                strReviewReqSpec = FrameworkUtil.lookupStateName(context, strSpecificationPolicy, strSymbReviewState);
            }
                        
            StringBuffer sbWhereBus = new StringBuffer();

            // (((policy == 'Chapter') AND (current == 'Release')) OR ((policy == 'Comment') AND (current == 'Release')))
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_POLICY);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strChapterPolicy);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strReleaseChapter);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_OR);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_POLICY);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strCommentPolicy);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strReleaseComment);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);

            strBusWhereClause = sbWhereBus.toString();

            MapList mapSubObjects = domReq.getRelatedObjects(context, strRelPattern, strTypePattern,
                  lstObjectSelects, null, bGetTo, bGetFrom, sRecursionLevel, strBusWhereClause, strRelWhereClause);

            ContextUtil.startTransaction(context, true);

            //demote all subobjects
            for(int i=0; i<mapSubObjects.size(); i++)
            {
                Map mapT=(Map)mapSubObjects.get(i);

                String strSubObjId = (String) mapT.get(SELECT_ID);

                if(!doReleasedParentsExist(context,strSubObjId)){ //IR-036353V6R2011
	                String strSubObjPolicy = (String) mapT.get(SELECT_POLICY);
	                DomainObject domSubObj = DomainObject.newInstance(context,strSubObjId);

					if(domSubObj.isKindOf(context,(RequirementsUtil.getChapterType(context))))
	                    domSubObj.setState(context, strActiveChapter);
	                else
	                    domSubObj.setState(context, strActiveComment);
                }
            }

            //demote the selected object
            String strObjType = domReq.getInfo(context, SELECT_TYPE);
            if(domReq.isKindOf(context,(RequirementsUtil.getChapterType(context))))
                domReq.setState(context, strActiveChapter);
            else
                domReq.setState(context, strReviewReqSpec);

            ContextUtil.commitTransaction(context);
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
    }

    /** Trigger Method to revise a Chapter/Comment/Requirement and float to non-released parents
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    public void revisionSpecStructureChildren(Context context, String[] args)
        throws Exception
    {
        String strObjectId = args[0];

        try
        {
            String event = MqlUtil.mqlCommand(context, "get env $1","EVENT");

    		MapList mapParentObjects = getActiveParents(context,strObjectId);

            DomainObject domLastRev = RequirementsCommon.getLastRevision(context, strObjectId, "MajorRevision".equalsIgnoreCase(event));

            ContextUtil.startTransaction(context, true);

            for(int i=0; i<mapParentObjects.size(); i++)
            {
                Map mapT=(Map)mapParentObjects.get(i);
                String strParentRelId = (String)mapT.get(SELECT_RELATIONSHIP_ID);

                DomainRelationship.setToObject(context, strParentRelId, domLastRev);
            }

            ContextUtil.commitTransaction(context);
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }

    }


    /** Trigger Method to check if we can revise a Chapter/Comment
     *  (i.e. the Chapter/Comment must have at least 1 non-released parent)
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - interger 0 if success
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    public int revisionSpecStructureChildrenCheck(Context context, String[] args)
        throws Exception
    {
        String strObjectId = args[0];

        try
        {
            MapList mapParentObjects = getActiveParents(context,strObjectId);

            if (mapParentObjects.size() == 0)
            {
                //push error about no parents in non-released state
                String errorString = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_REQUIREMENTS_STR, context.getLocale(), "emxRequirements.Error.AllParentsReleased"); 
                MqlUtil.mqlCommand(context, "error $1",errorString);
                return 1;
            }
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
        return 0;
    }

    /** Trigger Method to check if a Chapter/ReqSpec is able to be released
     *  (i.e. it must not contain any requirements that are active)
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - nothing
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    public int promoteToReleaseSpecStructureParentsCheck(Context context, String[] args)
        throws Exception
    {
        String strObjectId = args[0];
 
        try
        {
            if (doActiveReqsExist(context,strObjectId))
            {
                //push error about active requirements
                String errorString = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_REQUIREMENTS_STR, context.getLocale(), "emxRequirements.Error.ActiveRequirements"); 
                MqlUtil.mqlCommand(context, "error $1",errorString);
                return 1;
            }
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
        return 0;
    }

    /** Trigger Method to check if a Chapter/Comment/Req is able to be demoted from release
     *  (i.e. it must not contain any parents that are released)
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - nothing
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    public int demoteFromReleaseSpecStructureChildrenCheck(Context context, String[] args)
        throws Exception
    {
        String strObjectId = args[0];

        try
        {
            if (doReleasedParentsExist(context,strObjectId))
            {
                //push error about released parents
                String errorString = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_REQUIREMENTS_STR, context.getLocale(), "emxRequirements.Error.ReleasedParents"); 
                MqlUtil.mqlCommand(context, "error $1", errorString);
                return 1;
            }
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
        return 0;
    }


    /** Utility method for revision triggers
     * @param context - the eMatrix <code>Context</code> object
     * @param objectId - holds the object id.
     * @return - MapList of non-released parent objects
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    public MapList getActiveParents(Context context, String objectId)
        throws Exception
    {
        try
        {
            String strRelPattern = ReqSchemaUtil.getSpecStructureRelationship(context)+","+
					ReqSchemaUtil.getSubRequirementRelationship(context);

            StringList lstRelSelects = new StringList(SELECT_RELATIONSHIP_ID);
            boolean bGetTo = true;
            boolean bGetFrom = false;
            short sRecursionLevel = 1;
            String strBusWhereClause = "";
            String strRelWhereClause = "";

            String strReleaseChapter = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getChapterPolicy(context), strSymbReleaseState);
            String strReleaseReq = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementPolicy(context), strSymbReleaseState);
            String strReleaseReqSpec = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementSpecificationPolicy(context), strSymbReleaseState);
            String strObsoleteReqSpec = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementSpecificationPolicy(context), strSymbObsoleteState);

            StringBuffer sbWhereBus = new StringBuffer();

            // (((policy == 'Chapter') AND (current != 'Release')) OR ((policy == 'Requirement Specification') AND ((current != 'Release') AND (current != 'Obsolete'))))
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_POLICY);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(ReqSchemaUtil.getRequirementPolicy(context));
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_NOT_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strReleaseReq);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_OR);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_POLICY);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(ReqSchemaUtil.getChapterPolicy(context));
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_NOT_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strReleaseChapter);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_OR);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_POLICY);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(ReqSchemaUtil.getRequirementSpecificationPolicy(context));
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_NOT_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strReleaseReqSpec);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_NOT_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strObsoleteReqSpec);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);

            strBusWhereClause = sbWhereBus.toString();

            DomainObject domReq = DomainObject.newInstance(context,objectId);
            MapList mapParentObjects = domReq.getRelatedObjects(context, strRelPattern, QUERY_WILDCARD,
                  null, lstRelSelects, bGetTo, bGetFrom, sRecursionLevel, strBusWhereClause, strRelWhereClause);

            return mapParentObjects;
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
    }



    /** Utility method for revision triggers
     * @param context - the eMatrix <code>Context</code> object
     * @param objectId - holds the object id.
     * @return - MapList of non-released parent objects
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    public boolean doReleasedParentsExist(Context context, String objectId)
        throws Exception
    {
        try
        {
            String strRelPattern = ReqSchemaUtil.getSpecStructureRelationship(context);

            StringList lstRelSelects = new StringList(SELECT_RELATIONSHIP_ID);
            boolean bGetTo = true;
            boolean bGetFrom = false;
            short sRecursionLevel = 1;
            String strBusWhereClause = "";
            String strRelWhereClause = "";

            String strReleaseChapter = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getChapterPolicy(context), strSymbReleaseState);
            String strReleaseReqSpec = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementSpecificationPolicy(context), strSymbReleaseState);
            String strObsoleteReqSpec = FrameworkUtil.lookupStateName(context, ReqSchemaUtil.getRequirementSpecificationPolicy(context), strSymbObsoleteState);

            StringBuffer sbWhereBus = new StringBuffer();

            // (((policy == 'Chapter') AND (current == 'Release')) OR ((policy == 'Requirement Specification') AND ((current == 'Release') OR (current == 'Obsolete'))))
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_POLICY);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(ReqSchemaUtil.getChapterPolicy(context));
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strReleaseChapter);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_OR);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_POLICY);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(ReqSchemaUtil.getRequirementSpecificationPolicy(context));
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strReleaseReqSpec);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_OR);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strObsoleteReqSpec);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);

            strBusWhereClause = sbWhereBus.toString();

            DomainObject domReq = DomainObject.newInstance(context,objectId);
            MapList mapParentObjects = domReq.getRelatedObjects(context, strRelPattern, QUERY_WILDCARD,
                  null, lstRelSelects, bGetTo, bGetFrom, sRecursionLevel, strBusWhereClause, strRelWhereClause);

            if (mapParentObjects.size() > 0)
                return true;
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
        return false;
    }


    /** Utility method for revision triggers
     * @param context - the eMatrix <code>Context</code> object
     * @param objectId - holds the object id.
     * @return - MapList of non-released parent objects
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    public boolean doActiveReqsExist(Context context, String objectId)
        throws Exception
    {
        try
        {
            String strRelPattern = ReqSchemaUtil.getSpecStructureRelationship(context);
            String strReqPolicy = ReqSchemaUtil.getRequirementPolicy(context);

            StringList lstObjSelects = new StringList(SELECT_ID);
            boolean bGetTo = false;
            boolean bGetFrom = true;
            short sRecursionLevel = 0;
            String strBusWhereClause = "";
            String strRelWhereClause = "";

            String strReleaseReq = FrameworkUtil.lookupStateName(context, strReqPolicy, strSymbReleaseState);
            String strObsoleteReq = FrameworkUtil.lookupStateName(context, strReqPolicy, strSymbObsoleteState);

            StringBuffer sbWhereBus = new StringBuffer();

            // ((type == 'Chapter') OR ((policy == 'Requirement') AND ((current != 'Release') AND (current != 'Obsolete')))
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_TYPE);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(ReqSchemaUtil.getChapterType(context));
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_OR);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_POLICY);
            sbWhereBus.append(SYMB_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strReqPolicy);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_NOT_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strReleaseReq);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_AND);
            sbWhereBus.append(SYMB_OPEN_PARAN);
            sbWhereBus.append(SELECT_CURRENT);
            sbWhereBus.append(SYMB_NOT_EQUAL);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(strObsoleteReq);
            sbWhereBus.append(SYMB_QUOTE);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);
            sbWhereBus.append(SYMB_CLOSE_PARAN);

            strBusWhereClause = sbWhereBus.toString();
            
            List lstReqChildTypes       = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));
            
            StringBuffer sbRequirementChildTypes= new StringBuffer(ReqSchemaUtil.getRequirementType(context));
            sbRequirementChildTypes.append(SYMB_COMMA);
            
            for (int i=0; i < lstReqChildTypes.size(); i++)
            {
            	sbRequirementChildTypes.append(lstReqChildTypes.get(i));
                if (i != lstReqChildTypes.size()-1)
                {
                	sbRequirementChildTypes = sbRequirementChildTypes.append(SYMB_COMMA);
                }
            }
            DomainObject domReq = DomainObject.newInstance(context,objectId);
            MapList mapActiveReqs = domReq.getRelatedObjects(context, strRelPattern, QUERY_WILDCARD,
                  bGetTo, bGetFrom, sRecursionLevel, lstObjSelects, null, strBusWhereClause, strRelWhereClause,
                  null, sbRequirementChildTypes.toString(), null);

            if (mapActiveReqs.size() > 0)
                return true;
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }
        return false;
    }


    /**
     * Method shows path to Specification as linkable objects
     * @param context the eMatrix <code>Context</code> object
     * @param args JPO arguments
     * @return String - returns the program HTML output
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    public String getSpecificationPaths(Context context, String[] args)
        throws Exception
    {
        Map programMap = (HashMap) JPO.unpackArgs(args);
        Map relBusObjPageList = (HashMap) programMap.get("paramMap");
        String strObjectId = (String)relBusObjPageList.get(OBJECT_ID);

		Map requestMap = (Map) programMap.get("requestMap");
        String reportFormat = (String) requestMap.get("reportFormat");;
        String PFmode = (String) requestMap.get("PFmode");
        if (PFmode != null && PFmode.equals("true"))
        	reportFormat = "HTML";

        DomainObject domObject = DomainObject.newInstance(context,strObjectId);

        //object selects
        StringList selectStmts = new StringList(5);
        selectStmts.addElement(SELECT_ID);
        selectStmts.addElement(SELECT_NAME);
        selectStmts.addElement(SELECT_TYPE);
        selectStmts.addElement(SELECT_REVISION);
        selectStmts.addElement(SELECT_LEVEL);

        MapList mapPathObjects = new MapList();
        mapPathObjects = domObject.getRelatedObjects(context, ReqSchemaUtil.getSpecStructureRelationship(context),
                                                     QUERY_WILDCARD, selectStmts, null, true, false, (short)0, null, null);

        StringBuffer strBufOfAll = new StringBuffer();

        Stack pathStack = new Stack();
        int iMapSize = mapPathObjects.size();
        for(int ii = 0; ii < iMapSize; ii++)
        {
            Map mapT = (Map) mapPathObjects.get(ii);
            String strLevel = (String) mapT.get(SELECT_LEVEL);
            int iLevel = Integer.parseInt(strLevel);
            if (iLevel <= pathStack.size())
            {
                if (!strBufOfAll.toString().equals(""))
                    strBufOfAll.append("<BR>");

                //add the path
                for (int jj = pathStack.size()-1; jj >= 0; jj--)
                {
                    strBufOfAll.append((String) pathStack.elementAt(jj));
                    if (jj != 0)
                        strBufOfAll.append("<img src=\"../common/images/iconTreeToArrow.gif\"/>");
                }

                //pop some off
                int tempStackSize = pathStack.size();
                for (int j = 0; j < tempStackSize-iLevel+1; j++)
                {
                    pathStack.pop();
                }
            }

            //add to stack
            String strName = (String) mapT.get(SELECT_NAME);
            String strId = (String) mapT.get(SELECT_ID);
            String strType = (String) mapT.get(SELECT_TYPE);
            String strRev = (String) mapT.get(SELECT_REVISION);

            StringBuffer strBuf = new StringBuffer();

			if (!(reportFormat != null && "CSV".equals(reportFormat)))
            {
            	String typeIcon = UINavigatorUtil.getTypeIconProperty(context, strType);
            	if (!typeIcon.equals(""))
                	strBuf.append("<img src='../common/images/" +typeIcon+ "' border=0>");
			}

			if (reportFormat == null)
			{
            	strBuf.append("<B><a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=common&amp;suiteKey=Framework&amp;objectId="+strId+"','','','false','popup');\">");
            	strBuf.append(strName + " " + strRev);
            	strBuf.append("</a></B>");
			}
			else
			{
				strBuf.append(strName + " " + strRev);
			}

            pathStack.push(strBuf.toString());
        }

        if (pathStack.size() != 0)
        {
            if (!strBufOfAll.toString().equals(""))
                strBufOfAll.append("<BR>");

            //add the path
            for (int j=pathStack.size() - 1; j>=0; j--)
            {
                strBufOfAll.append((String)pathStack.elementAt(j));
                if (j != 0)
                    strBufOfAll.append("<img src=\"../common/images/iconTreeToArrow.gif\"/>");
            }
        }

        return strBufOfAll.toString();
    }

    /**
     * Method is to return the list of attribute content type.
     * @param context the eMatrix <code>Context</code> object
     * @param args JPO argument
     * @return List of context text or title
     * @throws Exception if operation fails
     */
    public List getContent (Context context, String[] args)
       throws Exception
    {
       //unpack the arguments
       Map programMap = (HashMap) JPO.unpackArgs(args);
       List lstobjectList = (MapList) programMap.get(OBJECT_LIST);
       Iterator objectListItr = lstobjectList.iterator();
       //initialise the local variables
       Map objectMap = new HashMap();
       String strObjId = EMPTY_STRING;
       List lstContent = new StringList();
       DomainObject domObj = null;

       List lstContentTextTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));
       lstContentTextTypes.add(ReqSchemaUtil.getRequirementType(context));
       lstContentTextTypes.add(ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getCommentType(context)));
       lstContentTextTypes.add(ReqSchemaUtil.getCommentType(context));

       //loop through all the records
       while(objectListItr.hasNext())
       {
          objectMap = (Map) objectListItr.next();
          strObjId = (String)objectMap.get(SELECT_ID);
          domObj = DomainObject.newInstance(context, strObjId);

          String thisContent = "";
          String thisType = domObj.getInfo(context, SELECT_TYPE);
          if (lstContentTextTypes.contains(thisType))
             thisContent = domObj.getInfo(context, "attribute[" + ReqSchemaUtil.getContentTextAttribute(context) + "]");
          else
             thisContent = domObj.getInfo(context, "attribute[" + ReqSchemaUtil.getTitleAttribute(context) + "]");

          lstContent.add(thisContent);
       }
       return lstContent;
    }

    protected Map relIdToTypeMap = new HashMap();
    
    /**
     * Method process the XML returned from Cut/copy/Paste as well Compare objects
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds String arguments
     * @return Map - returns Map which contains Action and Message key
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    @com.matrixone.apps.framework.ui.ConnectionProgramCallable
    public HashMap processXMLMessage(Context context, String[] args)
       throws Exception
    {
           HashMap connectionMap = new HashMap();
           
           List<Userfact> ufList = new ArrayList<Userfact>();
           // unpack the incoming arguments into a HashMap called 'programMap'
           HashMap programMap = (HashMap) JPO.unpackArgs(args);
           HashMap paramMap = (HashMap) programMap.get("paramMap");
           String strRootId = (String)paramMap.get(OBJECT_ID);
           // Get the xmlMessage Element (for now, just print it out)...
           Element contextData = (Element) programMap.get("contextData");
           String strParentObjectId = contextData.getAttributeValue(OBJECT_ID);
		   // Zud Tree Order Changes
           String InLinePasteAction = contextData.getAttributeValue("inlineEditingAction");

           //[ change for Bug 350207
           StringList objSelect = new StringList(SELECT_CURRENT);
           objSelect.addElement(SELECT_RESERVED_BY);
           DomainObject domObject1 = DomainObject.newInstance(context, strParentObjectId);
           Map current = domObject1.getInfo(context, objSelect);
           String currentstr = (String) current.get(SELECT_CURRENT);
           String resBy = (String) current.get(SELECT_RESERVED_BY);
           String doReconcile = EnoviaResourceBundle.getProperty(context, "emxRequirements.ImplementLink.DoReconcile");
           // Display error message if the moved object is reserved by someone else or if the state is release
           if ((!"".equals(resBy) && !resBy.equals(context.getUser())) ||
                 ((ReqConstants.STATE_RELEASE).equalsIgnoreCase(currentstr)))

           {
              String alertMess = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_REQUIREMENTS_STR, context.getLocale(), "emxRequirements.Alert.ObjectInReleasedState"); 
              connectionMap.put("Action", "ERROR" );
              connectionMap.put("Message", alertMess );
              return connectionMap;
           }
           try
           {

              Vector cutObjectIdVector   = new Vector();
              Vector cutRelIdVector      = new Vector();
              Vector reseqObjectIdVector = new Vector();
              ArrayList addObjectIdList  = new ArrayList();

              ArrayList addPasteAboveObjectId   = new ArrayList();
              ArrayList addPasteBelowObjectId   = new ArrayList();
              ArrayList addPasteAsChildObjectId = new ArrayList();

              ArrayList addPasteAboveRelId   = new ArrayList();
              ArrayList addPasteBelowRelId   = new ArrayList();
              ArrayList addPasteAsChildRelId = new ArrayList();

              // ++ ZUD HAT1 IR-395419-3DEXPERIENCER2016x FIX
              ArrayList addPasteAboveRowId   = new ArrayList();
              ArrayList addPasteBelowRowId  = new ArrayList();
              ArrayList addPasteAsChildRowId = new ArrayList();

              ArrayList reseqPasteAboveObjectId   = new ArrayList();
              ArrayList reseqPasteBelowObjectId   = new ArrayList();
              ArrayList reseqPasteAsChildObjectId = new ArrayList();

              ArrayList reseqPasteAboveRelId   = new ArrayList();
              ArrayList reseqPasteBelowRelId   = new ArrayList();
              ArrayList reseqPasteAsChildRelId = new ArrayList();

              StringList addMarkupPasteAboveList   = new StringList();
              StringList addMarkupPasteBelowList   = new StringList();
              StringList addMarkupPasteAsChildList = new StringList();

              StringList reseqMarkupPasteAboveList   = new StringList();
              StringList reseqMarkupPasteBelowList   = new StringList();
              StringList reseqMarkupPasteAsChildList = new StringList();
              
              //START : LX6 : recycling of a relationship
              StringList recycleRelList = new StringList();
              //END : LX6 : recycling of a relationship

              /*  --Commented the code as of now AEF is taking care of it.
                MapList columnValuesList = UITable.getColumns(context,tableName,null);
                HashMap columnMap = UITable.getColumn(context,columnValuesList,"Classification");
                String expression = UITable.getBusinessObjectSelect(columnMap);
                String strAttributeClassification = expression.substring(10, expression.length()-1);
               */

              DomainObject domParentObj = new DomainObject(strParentObjectId);
              //call normalizeSequenceOrder() of SpecificationStructure bean to arrange the objects in the sequence
              //SpecificationStructure.normalizeSequenceOrder(context,domParentObj);
              //XMLUtils.getOutputter().output(contextData, System.out);

              //get the list of Objects from the xml
              List strObjectList = contextData.getChildren("object");
              //iterate through each object element
              for (Iterator strObjectListItr = strObjectList.iterator(); strObjectListItr.hasNext();)
              {
                 Element objectElem = (Element) strObjectListItr.next();
                 //get the attributes value from the xml
                 String strMarkup   = objectElem.getAttributeValue(MARKUP);
                 String strObjectId = objectElem.getAttributeValue(OBJECT_ID);
                 String strPasteAsChild = objectElem.getAttributeValue(PASTEASCHILD);
                 String strPasteAbove = objectElem.getAttributeValue(PASTEABOVE);
                 String strPasteBelow = objectElem.getAttributeValue(PASTEBELOW);
                 String strRelId = objectElem.getAttributeValue("relId");
                 String strRowId = objectElem.getAttributeValue("rowId");
                 //START : LX6 : recycling of a relationship
                 String recycle = objectElem.getAttributeValue("recycle");
                 //END : LX6 : recycling of a relationship

                 /* --Commented the code as of now AEF is modifiying the attribute values.
                Map attributeMap  = new HashMap();

                DomainObject domObject = new DomainObject(strObjectId);

                        //nested loop to get column element if the attributes are modified.
                        List strColumnList = objectElem.getChildren(COLUMN);
                        for (Iterator strColumnListItr = strColumnList.iterator(); strColumnListItr.hasNext();) {
                                Element columnElem = (Element) strColumnListItr.next();
                                String strAttributeName = columnElem.getAttributeValue(SELECT_NAME);
                                String strAttributeValue = columnElem.getText();
                                String strAttributeEdited = columnElem.getAttributeValue(EDITED);


                                if(("true").equalsIgnoreCase(strAttributeEdited)){
                                    if(!"Description".equalsIgnoreCase(strAttributeName)){
                                            if("Classification".equalsIgnoreCase(strAttributeName)){
                                                attributeMap.put(strAttributeClassification.trim(),strAttributeValue);
                                             }else
                                                attributeMap.put(strAttributeName,strAttributeValue);
                                    }
                                    else
                                        domObject.setDescription(context,strAttributeValue);
                                }
                        }
                   // Update action: set the object attributes to the new values...
                  //set the attribute values with the updated values
                  domObject.setAttributeValues(context,attributeMap);
                  */
				// make vector for different markups
				if (CUT.equalsIgnoreCase(strMarkup)) {
					// START : LX6 : recycling of a relationship
					if (recycle == null || !recycle.equals("true")) {
						cutObjectIdVector.add(strObjectId);
						cutRelIdVector.add(strRelId);
					}
					// END : LX6 : recycling of a relationship
				}

                 if (RESEQUENCE.equalsIgnoreCase(strMarkup))
                 {

                    reseqObjectIdVector.add(strObjectId);

                    if (strPasteAsChild != null && !strPasteAsChild.equals("") && !"null".equals(strPasteAsChild))
                    {
                       reseqPasteAsChildObjectId.add(strObjectId);
                       reseqPasteAsChildRelId.add(strRelId);
                       reseqMarkupPasteAsChildList.add(strPasteAsChild);
                    }
                    else if (strPasteAbove != null && !strPasteAbove.equals("") && !"null".equals(strPasteAbove))
                    {
                       reseqPasteAboveObjectId.add(strObjectId);
                       reseqPasteAboveRelId.add(strRelId);
                       reseqMarkupPasteAboveList.add(strPasteAbove);
                    }
                    else if (strPasteBelow != null && !strPasteBelow.equals("") && !"null".equals(strPasteBelow))
                    {
                       reseqPasteBelowObjectId.add(strObjectId);
                       reseqPasteBelowRelId.add(strRelId);
                       reseqMarkupPasteBelowList.add(strPasteBelow);
                    }
                 }

                 if (ADD.equalsIgnoreCase(strMarkup))
                 {
                    
                	 addObjectIdList.add(strObjectId);

                    if (strPasteAsChild != null && !strPasteAsChild.equals("") && !"null".equals(strPasteAsChild))
                    {
                       addPasteAsChildObjectId.add(strObjectId);
                       addPasteAsChildRelId.add(strRelId);
                       addPasteAsChildRowId.add(strRowId);
                       addMarkupPasteAsChildList.add(strPasteAsChild);
                    }
                    else if (strPasteAbove != null && !strPasteAbove.equals("") && !"null".equals(strPasteAbove))
                    {
                       addPasteAboveObjectId.add(strObjectId);
                       addPasteAboveRelId.add(strRelId);
                       addPasteAboveRowId.add(strRowId);
                       addMarkupPasteAboveList.add(strPasteAbove);
                    }
                    else if (strPasteBelow != null && !strPasteBelow.equals("") && !"null".equals(strPasteBelow))
                    {
                       addPasteBelowObjectId.add(strObjectId);
                       addPasteBelowRelId.add(strRelId);
                       addPasteBelowRowId.add(strRowId);
                       addMarkupPasteBelowList.add(strPasteBelow);
                    }
                    //START : LX6 : recycling of a relationship
                    if(recycle != null  && recycle.equals("true"))
                    {
                    	recycleRelList.add(strRelId);
                    }
                    //END : LX6 : recycling of a relationship
                 }
              } //end if for loop iteration for object element
              String [] cutRelIdArray = (String[]) cutRelIdVector.toArray(new String[]{});
              
              StringList relSelects = new StringList(DomainConstants.SELECT_TYPE);
              MapList relInfoList = DomainRelationship.getInfo(context, cutRelIdArray, relSelects);
              for(int i = 0; i < relInfoList.size(); i++){
                  String relType = (String)((Map) relInfoList.get(i)).get(DomainConstants.SELECT_TYPE);
                  relIdToTypeMap.put(cutRelIdArray[i], relType);
              }


              MapList mlItems=new MapList();

              //[ change for Bug 350204: maitain a hashmap between old relId and new relId
              Map relIdMapOld2New = new HashMap();
              Vector newRelIds;
              //change for Bug 350204]

              if(addObjectIdList.size()>0)
              {

                  //invoke the method when markup is add "paste-above" to get the sorted maplist which contains map with the sequenceOrder, objectId and rowId key/value pairs
            	  //START : LX6 : recycling of a relationship
                  newRelIds = doEditOperation(context,addPasteAboveObjectId,addMarkupPasteAboveList,addPasteAboveRelId,strParentObjectId,PASTEABOVE,"add",recycleRelList,ufList,strRootId);
                  //END : LX6 : recycling of a relationship

                  for(int i = 0; i < newRelIds.size(); i++){
                      relIdMapOld2New.put(addPasteAboveRowId.get(i), newRelIds.elementAt(i));
                  }

                 //invoke the method when markup is add "paste-below" to get the sorted maplist which contains map with the sequenceOrder, objectId and rowId key/value pairs
                  //START : LX6 : recycling of a relationship
                  newRelIds = doEditOperation(context,addPasteBelowObjectId,addMarkupPasteBelowList,addPasteBelowRelId,strParentObjectId,PASTEBELOW,"add",recycleRelList,ufList,strRootId);
                  //END : LX6 : recycling of a relationship
                  for(int i = 0; i < newRelIds.size(); i++){
                      relIdMapOld2New.put(addPasteBelowRowId.get(i), newRelIds.elementAt(i));
                  }

                 //invoke the method when markup is add "paste-as-child" to get the sorted maplist which contains map with the sequenceOrder, objectId and rowId key/value pairs
                  //START : LX6 : recycling of a relationship
                  newRelIds = doEditOperation(context,addPasteAsChildObjectId,addMarkupPasteAsChildList,addPasteAsChildRelId,strParentObjectId,PASTEASCHILD,"add",recycleRelList,ufList,strRootId);
                  //END : LX6 : recycling of a relationship
                  for(int i = 0; i < newRelIds.size(); i++){
                      relIdMapOld2New.put(addPasteAsChildRowId.get(i), newRelIds.elementAt(i));
                  }

              }

              if(reseqObjectIdVector.size()>0)
              {

                 //invoke the method when markup is resequence "paste-above" to get the sorted maplist which contains map with the sequenceOrder, objectId and rowId key/value pairs
                  doEditOperation(context,reseqPasteAboveObjectId,reseqMarkupPasteAboveList,reseqPasteAboveRelId,strParentObjectId,PASTEABOVE,"resequence",null,null,null);

                 //invoke the method when markup is resequence "paste-below" to get the sorted maplist which contains map with the sequenceOrder, objectId and rowId key/value pairs
                  doEditOperation(context,reseqPasteBelowObjectId,reseqMarkupPasteBelowList,reseqPasteBelowRelId,strParentObjectId,PASTEBELOW,"resequence",null,null,null);

                 //invoke the method when markup is resequence "paste-as-Child" to get the sorted maplist which contains map with the sequenceOrder, objectId and rowId key/value pairs
                 doEditOperation(context,reseqPasteAsChildObjectId,reseqMarkupPasteAsChildList,reseqPasteAsChildRelId,strParentObjectId,PASTEASCHILD,"resequence",null,null,null);

              }
              if(cutRelIdVector.size() > 0)
              {
                    DomainRelationship.disconnect(context,cutRelIdArray);   
                    //START LX6 IR-191273V6R2014 Synchronize seq Order during cut Operation (cut/paste functionality)
                    //Some Objects are disconnected, so synchronize 
                    DomainObject parentObject = DomainObject.newInstance(context, strParentObjectId);
                    String relTypes = ReqSchemaUtil.getSpecStructureRelationship(context) + "," +
   			     					ReqSchemaUtil.getSubRequirementRelationship(context) + "," +
   			     					ReqSchemaUtil.getDerivedRequirementRelationship(context);
                    //SpecificationStructure.normalizeSequenceOrderMultiRelationship(context, parentObject, relTypes);
                    //END LX6 Synchronize seq Order during cut Operation (cut/paste functionality)
              }

              MapList chgRowsMapList = UITableIndented.getChangedRowsMapFromElement(context, contextData);

                 for (int i = 0; i < chgRowsMapList.size(); i++){
                        HashMap changedRowMap = (HashMap) chgRowsMapList.get(i);
                        String childObjectId = (String) changedRowMap.get("childObjectId");
                        String sRelId = (String) changedRowMap.get("relId");
                        String sRowId = (String) changedRowMap.get("rowId");
                        String sRelTypeSymb = (String) changedRowMap.get("relType"); //getting from postDatXML
                        String sRelType = (sRelTypeSymb == null? null: PropertyUtil.getSchemaProperty(context,sRelTypeSymb));
                        String markup = (String) changedRowMap.get("markup");
                        HashMap columnsMap = (HashMap) changedRowMap.get("columns");

                        if ("add".equals(markup)){
                            //Logic for ur ADD Opearation
                            DomainObject parentObj = DomainObject.newInstance(context);
                            DomainObject childObj = DomainObject.newInstance(context);
                            parentObj.setId(strParentObjectId);
                            childObj.setId(childObjectId);
                            
                            if (sRelType.isEmpty())
                                sRelType = ReqSchemaUtil.getSpecStructureRelationship(context);

                            //creating a returnMap having all the details abt the changed row.
                            HashMap returnMap = new HashMap();
                            returnMap.put("oid", childObjectId);
                            returnMap.put("rowId", sRowId);
                            returnMap.put("pid", parentObj.getId());
                            
                            // If we are adding an existing object in the SB
                            if (sRelId != null && relIdMapOld2New.get(sRowId) != null) {
                                // Retrieve the new relId
                                returnMap.put("relid", relIdMapOld2New.get(sRowId));
                            } else {
                                Element elm = (Element) programMap.get("contextData");
                                String parentObjectId = (String) programMap.get("parentOID");
                               
                                // Connect the new object to his parent
                                ContextUtil.startTransaction(context, true);
                                String structId = "|" + parentObjectId + "||0";
                                String xmlOutput = SpecificationStructure.insertNodeAtSelected(context, structId,
                                        childObjectId, null, true, null, sRelType);
                                
                                // Extract the relId of the new relationship
                                String[] arrayOut = xmlOutput.split(" ");
                                for (String id : arrayOut) {
                                    if (id.contains("relId")) {
                                        String relIDOut = id.split("\"")[1];
                                        returnMap.put("relid", relIDOut);
                                    }
                                }
                                ContextUtil.commitTransaction(context);
                            }
                            
                            returnMap.put("markup", markup);
                            returnMap.put("columns", columnsMap);
                            mlItems.add(returnMap);  //returnMap having all the details abt the changed row.

                        }
                        else if ("cut".equals(markup)){
                            //Logic for ur CUT Opearation
                             //DomainRelationship.disconnect(context, sRelId);

                            //creating a returnMap having all the details abt the changed row.
                            HashMap returnMap = new HashMap();
                            returnMap.put("oid", childObjectId);
                            returnMap.put("rowId", sRowId);
                            returnMap.put("relid", sRelId);
                            returnMap.put("markup", markup);
                            returnMap.put("columns", columnsMap);
                            mlItems.add(returnMap);

                        }
                        else if ("resequence".equals(markup)){

                            //creating a returnMap having all the details abt the changed row.
                            HashMap returnMap = new HashMap();
                            returnMap.put("oid", childObjectId);
                            returnMap.put("rowId", sRowId);
                            returnMap.put("relid", sRelId);
                            returnMap.put("markup", markup);
                            returnMap.put("columns", columnsMap);
                            mlItems.add(returnMap);
                        } else if ("new".equals(markup)) {

                            DomainObject parentObj = DomainObject.newInstance(context);
                            String objectName = (String) columnsMap.get("Name");
                            String objectTitle = (String) columnsMap.get("Title");
                            String objectType = (String) columnsMap.get("Type");
                            String objectRev = (String) columnsMap.get("Revision");
                            String objectPolicy = (String) columnsMap.get("Policy");
                            String objectVault = (String) columnsMap.get("Vault");

                            // ZUD Fix for Blank Name and Title                            
                            if(null == objectTitle && null != objectName)
                            {
                            	// Only Name column is present and not Title Column
                            	String TitleCount =  objectName.substring(objectName.indexOf("-") + 1); //Name.split("-")[1];
                            	objectTitle = TitleCount.substring(TitleCount.indexOf("-") + 1); 
                            	objectTitle = objectType +" " + objectTitle;
                            }
                            else if(null == objectTitle && null == objectName)
                            {
                            	// Only Dup Ttle Column is present
                            	objectTitle = (String) columnsMap.get("DUP_Title");
                            	objectName = UnifiedAutonamingServices.autoname(context, objectType);
                            	
                            }
                            // ZUD Fix for Blank Name and Title
                            DomainObject childObj = DomainObject.newInstance(context);
                            String parentObjectId = (String) programMap.get("parentOID");

                            // Create a new object given information
                            childObj.createObject(context, objectType, objectName, objectRev, objectPolicy, objectVault);
                            if(ReqSchemaUtil.isRequirement(context, childObj.getObjectId(context)) ||
                                    childObj.isKindOf(context, ReqSchemaUtil.getChapterType(context))||
                                    childObj.isKindOf(context, ReqSchemaUtil.getCommentType(context)))
                            	
                                    childObj.setAttributeValue(context, "Title", objectTitle);
                            
                            Map attributes = new HashMap();
                            HashMap returnMap = new HashMap();
                            if (parentObjectId != null && parentObjectId.length() > 0) {
                                parentObj.setId(parentObjectId);
                               
                                
                                /* Set Tree Order Attribute */
                                String[] PasteOrder = sRowId.split(",",-1);
                                String objTypes = ReqSchemaUtil.getRequirementType(context) + ","
                                + ReqSchemaUtil.getChapterType(context) + ","
                                		+ ReqSchemaUtil.getCommentType(context);
                                StringList Selectobj = new StringList(DomainConstants.SELECT_ID);
                                StringList relSelect = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                                relSelect.addElement("attribute[" + ReqSchemaUtil.getTreeOrderAttribute(context) + "]");
                                
                                // Get the child relationship objects and sort them based on the Tree Order attribute...
                                MapList relObjects = parentObj.getRelatedObjects(context, (String) columnsMap.get("Relationship Type"), objTypes, Selectobj, relSelect, false, true, (short) 1, "", "");
                                
                                relObjects.sortStructure(context, "relationship,attribute[TreeOrder]", ",", "string,real", ",");
                                int currTreeOrderIndex = Integer.parseInt(PasteOrder[0]);
                                int pastedTreeOrderIndex = 0;
                                double TreeOrderVal_pasted = 0.0;
                                double TreeOrderVal_curr =0.0;
                                boolean pasteonTop = false;
                                // ++ ZUD IR-273919V6R2015  ++
                                if(relObjects.size() > 0)
                                {
                                	Map currelMap = (Map) relObjects.get(currTreeOrderIndex);
                                	String currelId = (String) currelMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                                	Relationship relObject = DomainRelationship.newInstance(context, currelId);
                                	Attribute curseqAttrib = relObject.getAttributeValues(context, ReqSchemaUtil.getTreeOrderAttribute(context));
                                	TreeOrderVal_curr = Double.parseDouble(curseqAttrib.getValue());

                                	if(0 == InLinePasteAction.compareTo("below"))
                                	{
                                		pastedTreeOrderIndex = currTreeOrderIndex +1;
                                		if(pastedTreeOrderIndex<relObjects.size())
                                		{
                                			Map PastedrelMap = (Map) relObjects.get(pastedTreeOrderIndex);
                                			String PastedrelId = (String) PastedrelMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                                			Relationship PastedrelObject = DomainRelationship.newInstance(context, PastedrelId);
                                			Attribute PastedseqAttrib = PastedrelObject.getAttributeValues(context, ReqSchemaUtil.getTreeOrderAttribute(context));
                                			TreeOrderVal_pasted = Double.parseDouble(PastedseqAttrib.getValue());
                                			}
                                		}
                                	
                                	else if(0 == InLinePasteAction.compareTo("above"))
                                	{
                                		pastedTreeOrderIndex = currTreeOrderIndex -1;
                                		if(pastedTreeOrderIndex>0)
                                		{
                                			Map PastedrelMap = (Map) relObjects.get(pastedTreeOrderIndex);
                                			String PastedrelId = (String) PastedrelMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                                			Relationship PastedrelObject = DomainRelationship.newInstance(context, PastedrelId);
                                			Attribute PastedseqAttrib = PastedrelObject.getAttributeValues(context, ReqSchemaUtil.getTreeOrderAttribute(context));
                                			TreeOrderVal_pasted = Double.parseDouble(PastedseqAttrib.getValue());
                                			}else
                                				pasteonTop = true;
                                		}
                                	}
							    // -- ZUD IR-273919V6R2015  --
                                DomainRelationship dr = DomainRelationship.connect(context, parentObj,
                                        (String) columnsMap.get("Relationship Type"), childObj);
                                // If pastedTreeOrderIndex > number of objects then we will call getNextTreeOrderValue (Pasted at LAST)
								// If pastedTreeOrderIndex <0 then we will fetch Tree order between 0 and curr Tree order value.(Pasted on TOP) 
                                if(TreeOrderVal_pasted==0 && pasteonTop == false)
                                	
                                	dr.setAttributeValue(context, ReqSchemaUtil.getTreeOrderAttribute(context),""+TreeOrderUtil.getNextTreeOrderValue());
                                else
                                	dr.setAttributeValue(context, ReqSchemaUtil.getTreeOrderAttribute(context),""+TreeOrderUtil.getTreeOrderValueBetween(TreeOrderVal_curr,TreeOrderVal_pasted));     	
                                
                                pasteonTop = false;                          
                                /* Set Tree Order Attribute */
                                
                                
                                returnMap.put("oid", childObj.getId(context));
                                returnMap.put("rowId", sRowId);
                                returnMap.put("pid", parentObj.getId(context));
                               
                                returnMap.put("relid", dr.toString());
                                returnMap.put("markup", markup);
                                returnMap.put("columns", columnsMap);
                                mlItems.add(returnMap);
                            }
                        }
                 }
                 connectionMap.put("Action", "success"); //Here the action can be "Success" or "refresh"
                 connectionMap.put("changedRows", mlItems);//Adding the key "ChangedRows" which having all the data for changed Rows


           }
           catch(Exception e)
           {
               connectionMap.put("Action", "ERROR"); // If any exeception is there send "Action" as "ERROR"
               connectionMap.put("Message", e.getMessage()); // Error message to Display
               e.printStackTrace();
           }
           finally
           {
        	  if(!ufList.isEmpty())
        	  {
        		  
        		  //JX5 Call reconcile method
        		  try{
	    			  if(doReconcile!=null&&doReconcile.equalsIgnoreCase("true")&&!ufList.isEmpty())
	        		  {
	    				  
	    				  ILReconciliationServices.doReconcile(context, ufList);
	        		  }
        		  
        		  }catch (Exception e){
        			  e.printStackTrace();
        		  }
        		
        		  //
        	  }
              return connectionMap;
           }
    }

    /**
     * Method returns true if the object is container
     * @param context     the eMatrix <code>Context</code> object
     * @param objectId    the String objectId
     * @return boolean - returns true if the object is container.
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    private static boolean isContainer(Context context, String objectId)
       throws Exception
    {
       boolean isContainer = false;
       try
       {
          DomainObject domObject = DomainObject.newInstance(context, objectId);
          String type = domObject.getInfo(context, SELECT_TYPE);

          List lstReqSpecTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementSpecificationType(context));
          lstReqSpecTypes.add(ReqSchemaUtil.getRequirementSpecificationType(context));

          if (ReqSchemaUtil.getChapterType(context).equalsIgnoreCase(type) || lstReqSpecTypes.contains(type))
             isContainer = true;
       }
       catch (Exception ex)
       {
          ex.printStackTrace();
          throw ex;
       }
       finally
       {
          return isContainer;
       }
    }

    /**
     * Method returns string sequence number for the selected object.
     * @param context            the eMatrix <code>Context</code> object
     * @param selectedObjInfo    the String selectedObjInfo contains objId,relId,rowId separated by '|'.
     * @return string - returns sequence number for the selected object.
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    private static String getSequence(Context context, String selectedObjInfo)
       throws Exception
    {
      /* ++ zud Deprecated Code for Sequence Ordder ++
       try
       {
          String curOrder = "";
          if (!("null").equals(selectedObjInfo) && !("").equals(selectedObjInfo))
          {
             // Extract the objectId and parentId from the rowId:
             String[] tokens = selectedObjInfo.split("[|]", -1);
             String objId = tokens[0];
             String relId = tokens[1];
             String rowId = tokens[2];

             //added for bug 357211 - if pasting on spec no rel id is passed.
             if (relId.equals("null") || "".equals(relId))
                return "1000000";

             DomainRelationship expLink = DomainRelationship.newInstance(context, relId);
             curOrder = expLink.getAttributeValue(context, ReqSchemaUtil.getSequenceOrderAttribute(context));
          }
          return curOrder;
       }
       catch (Exception ex)
       {
          ex.printStackTrace();
          throw ex;
       }
         ++ zud Deprecated Code for Sequence Ordder ++*/
    	return null;
    } 
    
    private static String getTreeOrderSequence(Context context, String selectedObjInfo)
    	       throws Exception
    	    {
    	       try
    	       {
    	          String curTreeOrder = "";
    	          if (!("null").equals(selectedObjInfo) && !("").equals(selectedObjInfo))
    	          {
    	             // Extract the objectId and parentId from the rowId:
    	             String[] tokens = selectedObjInfo.split("[|]", -1);
    	             String objId = tokens[0];
    	             String relId = tokens[1];
    	             String rowId = tokens[2];

             //added for bug 357211 - if pasting on spec no rel id is passed.
             if (relId.equals("null") || "".equals(relId))
                return "1000000";

    	             DomainRelationship expLink = DomainRelationship.newInstance(context, relId);
    	             curTreeOrder = expLink.getAttributeValue(context, ReqSchemaUtil.getTreeOrderAttribute(context));
    	          }
    	          return curTreeOrder;
    	       }
    	       catch (Exception ex)
    	       {
    	          ex.printStackTrace();
    	          throw ex;
    	       }
    	    } 

    
    private void findRootObjectInStack(Context context, String rootId, Stack stackToFilter)
    {
    	Map mapObject = (Map)stackToFilter.peek();
    	String currentObject = (String)mapObject.get(SELECT_ID);
    	while(!rootId.equals(currentObject)){
    		stackToFilter.pop();
    		mapObject = (Map)stackToFilter.peek();
        	currentObject = (String)mapObject.get(SELECT_ID);
    	}
    }
    
    /**
     * Method
     * @param  strFullPath    Full rel path of an object
     * @param Attribute        Attribute to get in the structure
     * @return StringList      list of attributes
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2013x
     */
   private List<String> getAttribute(List<String[]> strFullPath, String Attribute)
   {
	   int value= 0;
	   if(Attribute.equalsIgnoreCase("from"))
		   value = 0;
	   else if(Attribute.equalsIgnoreCase("rel"))
		   value = 1;
	   else if(Attribute.equalsIgnoreCase("physicalId"))
		   value = 2;
	   else if(Attribute.equalsIgnoreCase("logicalId"))
		   value = 3;
	   else if(Attribute.equalsIgnoreCase("majorId"))
		   value = 4;
	   else
		   value = 5;
	   List<String> strAttribute = new ArrayList<String>();
	   	for(int i=0;i<strFullPath.size();i++)
	   	{
	   		strAttribute.add(strFullPath.get(i)[value]);
	   	}
	   	return strAttribute;
   }  
    
    /**
     * Method
     * @param context           the eMatrix <code>Context</code> object
     * @param objIds            the array list of ObjectIds.
     * @param markupList        the String list of markup which contains selected objectID, relId and rowId.
     * @param relIds            the array list of relationship ids.
     * @param parObjId          String containing the parentObjectId
     * @param markup            String which contains value "paste-above"/"paste-below"/"paste-as-child"
     * @param operation         String which contains value "add" or "resequence"
     * @param recycle           String used to check if a relationship must be recycled
     * @return Vector which contains new relIds as a result of the edit operation
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    private Vector doEditOperation(Context context, ArrayList objIds, StringList markupList, ArrayList relIds,String parObjId, String markup, String operation, StringList recycleRelIds, List<Userfact> ufList, String RootId)
       throws Exception
    {
       try
       {
          Vector newRelIds = new Vector();

          int objectSize = objIds.size();
          int pasteBelowNumber = 0;

          String sequenceOrder = "";
          String xmlMessage    = "";
          boolean normalize    = true;
          boolean recycle = false;
          //Userfact userFact = null;

          String [] rowIdArray    = new String[objIds.size()];
          String [] objectIdArray = (String[])objIds.toArray(new String[]{});

          if (RESEQUENCE.equalsIgnoreCase(operation) && PASTEBELOW.equalsIgnoreCase(markup))
             pasteBelowNumber = objectSize;

          for (int counter = 0 ; counter < objectSize; counter++)
          {
             String selectedObjInfo = "";
             int seqOrder = 0;

             if (!markupList.isEmpty())
                selectedObjInfo = (String)markupList.get(counter);

             if (RESEQUENCE.equalsIgnoreCase(operation) && PASTEASCHILD.equalsIgnoreCase(markup))
             {
                //give sum large no. so that it will append as last child.
                seqOrder = 1000000;
             }

             //call getSequence() to get the sequence number of the selected obj
             //String curOrder = getSequence(context,selectedObjInfo);
             
             // ZUD to fetch Selected object Tree Order
             String curTreeOrder = getTreeOrderSequence(context,selectedObjInfo);
             String sAddBelowOrAbove = "";

             //if markup is paste above assign the sequence Order same as current sequence order
             if ((PASTEABOVE).equalsIgnoreCase(markup))
             {
                //seqOrder = Integer.parseInt(curOrder); 
                sAddBelowOrAbove = "AddAbove"; 
             }
             else if ((PASTEBELOW).equalsIgnoreCase(markup))
             {
                //if markup is paste below assign the sequence Order incremented with one to current sequence order
                //seqOrder = Integer.parseInt(curOrder) + 1;
                sAddBelowOrAbove = "AddBelow"; 
             }

             // ++ HAT1 ZUD: IR-397107-3DEXPERIENCER2016x fix
             //assign sequence Order to string as bean accepts this parameter.
             if( !(PASTEASCHILD).equalsIgnoreCase(markup))
             sequenceOrder = curTreeOrder + "|"+ sAddBelowOrAbove;

             // -- HAT1 ZUD: IR-397107-3DEXPERIENCER2016x fix
             
             if (relIds.size() > 0)
             {
			 // Zud Changes for Tree Order
                //build the rowId array which contains relID,parentObject ID,current sequence order with '|' separated.
                rowIdArray[counter] = relIds.get(counter) + "|" + parObjId + "|" + parObjId + "|" + curTreeOrder;
                if(recycleRelIds!= null && recycleRelIds.contains(relIds.get(counter)))
                {
                	recycle = true;
                }
             }

             //if markup is paste below assign the sequence Order incremented with no. of objects already iterated.
             if (RESEQUENCE.equalsIgnoreCase(operation) && PASTEBELOW.equalsIgnoreCase(markup) && counter>=1)
             {
                if(objectSize != 1 && pasteBelowNumber >= 1)
                {
                   seqOrder = seqOrder + counter;
                   sequenceOrder = "" + seqOrder;
                   pasteBelowNumber = pasteBelowNumber - 1;
                }
             }
             
             String relType = (String)relIdToTypeMap.get(relIds.get(counter));
             
             if(relType == null){//must be a copy
            	 DomainRelationship dr = DomainRelationship.newInstance(context, (String)relIds.get(counter));
            	 boolean isOpen = dr.openRelationship(context);
            	 relType = dr.getTypeName();
            	 dr.closeRelationship(context, isOpen);
             }

             DomainObject bo=new DomainObject(parObjId);
             if(bo.isKindOf(context, ReqSchemaUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_SoftwareRequirementSpecification)) || 
            		 		bo.isKindOf(context, ReqSchemaUtil.getChapterType(context)))
             {
            	 relType = ReqSchemaUtil.getSpecStructureRelationship(context);
             }
             //invoke the insertNodeAtSelected method of SpecificationStructure bean for each object id to ensure the normalization and re-sequencing properly.
             //START : LX6 : recycling of a relationship
             String doReconcile = EnoviaResourceBundle.getProperty(context,"emxRequirements.ImplementLink.DoReconcile");
             List<Stack> OldStacks = new ArrayList<Stack>();
             if(doReconcile.equalsIgnoreCase("true"))
             {
            	//START LX6 FUN054695 ENOVIA GOV TRM Revision refactoring
            	 OldStacks = SpecificationStructure.getPathToRoot(context, RootId, objectIdArray[counter],false);
            	//END LX6 FUN054695 ENOVIA GOV TRM Revision refactoring
             }
             xmlMessage = SpecificationStructure.insertNodeAtSelected(context, rowIdArray[counter], objectIdArray[counter], sequenceOrder, normalize, operation, relType, recycle);
             List<Stack> NewStacks = new ArrayList<Stack>();
             if(doReconcile.equalsIgnoreCase("true")){
            	 //START LX6 FUN054695 ENOVIA GOV TRM Revision refactoring
            	 NewStacks = SpecificationStructure.getPathToRoot(context, RootId, objectIdArray[counter], false);
            	//END LX6 FUN054695 ENOVIA GOV TRM Revision refactoring
            	 SpecificationStructure.cleanStacks(context, OldStacks, NewStacks);
             }
             
             if(doReconcile.equalsIgnoreCase("true"))
             {
            	 if(!recycleRelIds.isEmpty())
                 {
                	 try {
                		 List<Stack> tempStack;
                		if(NewStacks.size()<OldStacks.size())
                			tempStack = NewStacks;
                		else
                			tempStack = OldStacks;
                		for(int ii =0; ii<tempStack.size();ii++)
                		{
							//START LX6 FUN054695 ENOVIA GOV TRM Revision refactoring
                			SpecificationStructure.getUserFacts(context,
	    								  OldStacks.get(ii),
	    								  NewStacks.get(ii),
	    							      rowIdArray[counter].split("[|]")[1],
	    							      ufList,
	    							      UserFactType.MOVERELATIONPATH
	    							      );
							//END LX6 FUN054695 ENOVIA GOV TRM Revision refactoring
                		}
    				} catch (Throwable e) {

    					e.printStackTrace();
    				}
                 } 
             }
             
             
             //END : LX6 : recycling of a relationship
             
             //[ change for Bug 350204
             //return the new relId
             xmlMessage = "<items>" + xmlMessage + "</items>";
             SAXBuilder builder = new SAXBuilder();
             builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
             builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
             builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
             Document doc = builder.build(new StringReader(xmlMessage));
             java.util.List elmlist = doc.getRootElement().getChildren("item");
             java.util.Iterator itr = elmlist.iterator();
             while(itr.hasNext()) {
                 Element elm = (Element)itr.next();
                 String newRelId = elm.getAttributeValue("relId");
                 newRelIds.add(newRelId);
             }
             //change for Bug 350204 ]
          }

          return newRelIds;
       }
       catch (Exception ex)
       {
          ex.printStackTrace();
          throw ex;
       }
    }

    /**
     * Method returns Vector of the objects.
     * @param context   the eMatrix <code>Context</code> object
     * @param args ObjectIds the String array ObjectIds.
     * @return string - returns name and HREF for Table in SpecificationSearch.
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2008-2.0
     */
    public static Vector getTargetLocation(Context context, String[] args)
       throws Exception
    {
       try
       {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          MapList objectList = (MapList)programMap.get(OBJECT_LIST);
          HashMap paramList = (HashMap)programMap.get(PARAM_LIST);
          String strLocation = (String)paramList.get("searchmode");

          Vector newIconList = new Vector();
          String uniqueFolderId = "";

          if (objectList != null)
          {
             String[] idArray = new String[objectList.size()];
             //iterate through each object in the objectList and form an array of the ids
             for(int itr = 0 ; itr < objectList.size(); itr++)
             {
                Map tempMap = (Map)objectList.get(itr);
                idArray[itr] = (String) tempMap.get(SELECT_ID);
             }

             String projectName = "";

             DomainObject dom = null;
             //Iterate for each Object ID to display hyperlink in search page as content or popup
             for (int itr = 0; itr < idArray.length; itr++)
             {
                StringBuffer sbNewIconValues = new StringBuffer();
                sbNewIconValues.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
                sbNewIconValues.append("objectId=");
                uniqueFolderId = idArray[itr];
                sbNewIconValues.append(uniqueFolderId);
                sbNewIconValues.append("&amp;mode=insert");
                sbNewIconValues.append("', '875', '550', 'false',");

                if ((strLocation!= null) && (strLocation.equalsIgnoreCase("globalsearch")))
                   sbNewIconValues.append("'content', '')\">");
                else
                   sbNewIconValues.append("'popup', '')\">");

                sbNewIconValues.append("<img src=\"images/iconReqTypeRequirementSpecification.png\" border=\"0\"/>&#160;");
                dom = DomainObject.newInstance(context, uniqueFolderId);
                projectName = dom.getInfo(context, SELECT_NAME);
                sbNewIconValues.append(XSSUtil.encodeForHTML(context, projectName));
                sbNewIconValues.append("</a>");
                newIconList.add(sbNewIconValues.toString());
             }
          }
          return newIconList;
       }
       catch(Exception ex)
       {
          ex.printStackTrace();
          throw ex;
       }
    }

    /**
     * Method is to compare and validate the specifiction structure
     * @param context  the eMatrix <code>Context</code> object
     * @param args JPO argument
     * @return Map of connection Map the changed rows list so the sync'd rows are refreshed...
     * @throws Exception if operation fails
     */
    @com.matrixone.apps.framework.ui.ConnectionProgramCallable
    public static Map validateStructureCompare(Context context, String[] args)
       throws Exception
    {
       Map connectionMap = new HashMap();
       // unpack the incoming arguments into a HashMap called 'programMap'
       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       // Map paramMap = (HashMap) programMap.get("paramMap");

       // Get the xmlMessage Element (for now, just print it out)...
       Element contextData = (Element) programMap.get("contextData");
       String strParentObjectId = contextData.getAttributeValue(OBJECT_ID);

       //Get the list of objects from the XML
       List strObjects = contextData.getChildren("object");
       Vector addObjIdVector = new Vector();
       Vector addRelIdVector = new Vector();
       Vector cutRelIdVector = new Vector();
       Vector cutObjIdVector = new Vector();

       DomainObject domParentObj1 = new DomainObject(strParentObjectId);
       //call normalizeSequenceOrder() of SpecificationStructure bean to arrange the objects in the sequence
       //SpecificationStructure.normalizeSequenceOrder(context,domParentObj1);
       //XMLUtils.getOutputter().output(contextData, System.out);

       //iterate through each object element
       for (Iterator strObjectsItr = strObjects.iterator(); strObjectsItr.hasNext(); )
       {
          Element objectElement  = (Element) strObjectsItr.next();
          String  strMarkup   = objectElement.getAttributeValue(MARKUP);
          String  sObjectId = objectElement.getAttributeValue(OBJECT_ID);
          String  strRelId = objectElement.getAttributeValue("relId");
          String  strSyncDir   = objectElement.getAttributeValue("syncDir");

          // This code is executed only if XML has the attribute SyncDir
          if (("left".equalsIgnoreCase(strSyncDir))|| ("right".equalsIgnoreCase(strSyncDir)))
          {
             StringList objSelect = new StringList(SELECT_CURRENT);
             objSelect.addElement(SELECT_RESERVED_BY);
             DomainObject domObject1 = DomainObject.newInstance(context, sObjectId);
             Map current = domObject1.getInfo(context, objSelect);
             String currentstr = (String) current.get(SELECT_CURRENT);
             String resBy = (String) current.get(SELECT_RESERVED_BY);

             // Display error message if the moved object is reserved by someone else or if the state is release
             if ((!"".equals(resBy) && !resBy.equals(context.getUser())) ||
                ((ReqConstants.STATE_RELEASE).equalsIgnoreCase(currentstr)))
             {
                String sMessage = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_REQUIREMENTS_STR, context.getLocale(), "emxRequirements.Alert.ObjectInReleasedState"); 
                connectionMap.put("Action", "ERROR" );
                connectionMap.put("Message", sMessage );
                return connectionMap;
             }

             if (ADD.equalsIgnoreCase(strMarkup))
             {
                addObjIdVector.add(sObjectId);
                addRelIdVector.add(strRelId);
             }

             if (CUT.equalsIgnoreCase(strMarkup))
             {
                cutObjIdVector.add(sObjectId);
                cutRelIdVector.add(strRelId);
             }
          }
       }

       String [] addObjIdArray = (String[]) addObjIdVector.toArray(new String[]{});
       String [] emxRowIdArray = new String[addObjIdVector.size()];
       String [] addRelIdArray = (String[]) addRelIdVector.toArray(new String[]{});
       String [] addRelTypeArray = new String[addRelIdVector.size()];
       String [] cutRelIdArray = (String[]) cutRelIdVector.toArray(new String[]{});

       if(addObjIdVector.size()>0)
       {
          for(int addCounter=0 ; addCounter< addObjIdVector.size();addCounter++)
          {
             //Commented the code as now it is invoking jpo twice - one for cut and other for add.Since it is already cut when it again comes till here so we get relid not exist exception
             DomainRelationship expLink = DomainRelationship.newInstance(context, addRelIdArray[addCounter]);
             boolean closeRel = expLink.openRelationship(context);
             // Zud Tree Order Migration
             String curOrder = expLink.getAttributeValue(context, ReqSchemaUtil.getTreeOrderAttribute(context));
	         //make an array with '|' separated containing relId, sel Id, par Id and current order
             emxRowIdArray[addCounter] = addRelIdVector.get(addCounter) + "|" + strParentObjectId + "|" + strParentObjectId + "|" + curOrder;
             addRelTypeArray[addCounter] = expLink.getTypeName();
             expLink.closeRelationship(context, closeRel);
          }
          for (int ii = 0; ii < addObjIdArray.length; ii++)
          {
             String addObjectId = addObjIdArray[ii];
             String rowId = emxRowIdArray[ii] ;
             SpecificationStructure.insertNodeAtSelected(context, rowId, addObjectId, null, false, null, addRelTypeArray[ii]);
          }
       }

       if (cutRelIdVector.size() > 0)
          DomainRelationship.disconnect(context, cutRelIdArray);

       // Bug:357892 - Return the changed rows list so the sync'd rows are refreshed...
       MapList chgRowsMapList = com.matrixone.apps.framework.ui.UITableIndented.getChangedRowsMapFromElement(context, contextData);
       if (chgRowsMapList != null && chgRowsMapList.size() > 0)
          connectionMap.put("changedRows", chgRowsMapList);
       connectionMap.put("Action", "success");

       return connectionMap;
    }

    /**
     * A trigger method to notify set Sequence Order to last element
     *
     * @param context - the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *                       0 - string containing the Object Id
     *                       1 - string containing the Rel Id
     * @return - Integer value 0 indicating success
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2009x
     */
    public int setSequenceOrderToBottom(Context context, String[] args)
    throws Exception
    {
    	//Bug 375911/IR 0666358
    	//comment out this trigger
    	/*
    	try
       {
          String strObjectId = args[0];
          String strRelId = args[1];

          StringList objSelect = new StringList(DomainConstants.SELECT_ID);
          StringList relSelect = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
          relSelect.addElement("attribute[" + ReqSchemaUtil.getSequenceOrderAttribute(context) + "]");

          DomainObject parentObject = DomainObject.newInstance(context, strObjectId);
          MapList relObjects = parentObject.getRelatedObjects(context, ReqSchemaUtil.getSpecStructureRelationship(context), "*",
                objSelect, relSelect, false, true, (short) 1, "", "");

          int highestOrder = 0;
          for (int i = 0; i < relObjects.size(); i++)
          {
             Map relMap = (Map) relObjects.get(i);
             String strSO = (String) relMap.get("attribute[" + ReqSchemaUtil.getSequenceOrderAttribute(context) + "]");
             int intSO = Integer.parseInt(strSO);
             if (intSO > highestOrder)
             {
                highestOrder = intSO;
             }
          }
          highestOrder++;

          DomainRelationship relObject = DomainRelationship.newInstance(context, strRelId);
          relObject.setAttributeValue(context, ReqSchemaUtil.getSequenceOrderAttribute(context), "" + highestOrder);
       }
       catch (Exception e)
       {
          ContextUtil.abortTransaction(context);
          e.printStackTrace(System.out);
          throw new FrameworkException(e.getMessage());
       }
       */
       return 0;
    }

   /**
    * To obtain the list of Object IDs to be excluded from the search for Add Existing Actions
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args - holds the HashMap containing the following arguments
    * @return  StringList - consisting of the object ids to be excluded from the Search Results
    * @throws Exception if the operation fails
    */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public StringList excludeBranchChapters(Context context, String[] args)
      throws Exception
   {
      Map programMap = (Map) JPO.unpackArgs(args);
      String strObjectId = (String)programMap.get("objectId");
      String strTblRowId = (String) programMap.get("emxTableRowId");
      String isFromRMB = (String) programMap.get("isFromRMB");

      String[] objectIds = strObjectId == null? strTblRowId.split("[|]"): strObjectId.split("[|]");
      String strRelName=(String)programMap.get("relName");
      String strObjType = ReqSchemaUtil.getChapterType(context);

      if (objectIds != null && objectIds.length > 1){
    	  strObjectId = objectIds[1];
      }
      return excludeBranchObjects(context, strObjectId, strRelName, "from", 0, strObjType);
   }

   /**
    * To obtain the list of Object IDs to be excluded from the search for Add Existing Actions
    *
    * @param context - the eMatrix <code>Context</code> object
    * @param args - holds the HashMap containing the following arguments
    * @return  StringList - consisting of the object ids to be excluded from the Search Results
    * @throws Exception if the operation fails
    */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public StringList excludeBranchRequirements(Context context, String[] args)
   throws Exception
   {
      Map programMap = (Map) JPO.unpackArgs(args);
      String strObjectId = (String)programMap.get("objectId");
      String strTblRowId = (String) programMap.get("emxTableRowId");

      String[] objectIds = strObjectId == null? strTblRowId.split("[|]"): strObjectId.split("[|]");
      String strRelName=(String)programMap.get("relName");
      String strObjType = ReqSchemaUtil.getRequirementType(context);

      if (objectIds != null && objectIds.length > 1)
         strObjectId = objectIds[1];
      return excludeBranchObjects(context, strObjectId, strRelName, "to", 1, strObjType);
   }

   /**
    * @param context
    * @param objectId
    * @param relName
    * @param objType
    * @return
    * @throws Exception
    * @throws FrameworkException
    */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   private StringList excludeBranchObjects(Context context, String objectId, String relName, String dirFlag, int maxLevel, String objType)
   throws Exception, FrameworkException
   {
      StringList excludeIDs = new StringList(objectId);

      StringList linkedIDs = linkedObjectIDs(context, objectId, relName, dirFlag, maxLevel, objType);
      if (linkedIDs != null)
      {
         for (int ii=0; ii < linkedIDs.size(); ii++)
         {
            String linkedID = (String) linkedIDs.elementAt(ii);
            if (!excludeIDs.contains(linkedID))
               excludeIDs.add(linkedID);
         }
      }
      return(excludeIDs);
   }


   /**
    * To obtain the list of parent Object IDs to be excluded from the search for Add Existing Actions
    *
    * @param context - the eMatrix <code>Context</code> object
    * @param args - holds the HashMap containing the following arguments
    * @return  StringList - consisting of the object ids to be excluded from the Search Results
    * @throws Exception if the operation fails
    */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public StringList excludeParentObjects(Context context, String[] args)
   throws Exception
   {
      StringList linkedIDs = new StringList();

      Map programMap = (Map) JPO.unpackArgs(args);
      String strObjectId = (String) programMap.get("objectId");
      String strRelName =  (String) programMap.get("relName");
      String strObjType =  (String) programMap.get("type");

      if (strObjectId != null && strObjectId.length() > 0)
         linkedIDs = linkedObjectIDs(context, strObjectId, strRelName, "from", 1, strObjType);

      return(linkedIDs);
   }

   /**
    * To obtain the list of child Object IDs to be excluded from the search for Add Existing Actions
    *
    * @param context - the eMatrix <code>Context</code> object
    * @param args - holds the HashMap containing the following arguments
    * @return  StringList - consisting of the object ids to be excluded from the Search Results
    * @throws Exception if the operation fails
    */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public StringList excludeChildObjects(Context context, String[] args)
   throws Exception
   {
      StringList linkedIDs = new StringList();

      Map programMap = (Map) JPO.unpackArgs(args);
      String strObjectId = (String) programMap.get("objectId");
      String strRelName =  (String) programMap.get("relName");
      String strObjType =  (String) programMap.get("type");

      if (strObjectId != null && strObjectId.length() > 0)
         linkedIDs = linkedObjectIDs(context, strObjectId, strRelName, "to", 1, strObjType);

      return(linkedIDs);
   }

   /**
    * @param context
    * @param objectId
    * @param relName
    * @param expDir
    * @param expMax
    * @param retType
    * @return
    * @throws Exception
    * @throws FrameworkException
    */
   private StringList linkedObjectIDs(Context context, String objectId, String relName, String expDir, int expMax, String retType)
      throws Exception, FrameworkException
   {
      StringList objIDs= new StringList();
      String retTypes = DomainConstants.EMPTY_STRING;
      //System.out.println("  objectId = '" + objectId + "'");

      // Convert symbolic object/relationship type names, if necessary...
      if (retType != null && retType.startsWith("type_"))
         retType = PropertyUtil.getSchemaProperty(context,retType);
      if (relName != null && relName.startsWith("relationship_"))
         relName = PropertyUtil.getSchemaProperty(context,relName);

      // Evaluate the expand direction input...
      boolean expToDir = false;
      boolean expFromDir = false;
      if (expDir == null)
      {
         expToDir = true;
      }
      else
      {
         if (expDir.equalsIgnoreCase("to") || expDir.equalsIgnoreCase("both"))
            expToDir = true;
         if (expDir.equalsIgnoreCase("from") || expDir.equalsIgnoreCase("both"))
            expFromDir = true;
      }

      // Get the return types and subtypes...
      if (retType != null && retType.length() > 0)
      {
         BusinessType busType = new BusinessType(retType, new Vault("eService Administration"));
         BusinessTypeList busList = busType.getChildren(context);
         //System.out.println(objType + ": # of subTypes = " + busList.size());

         retTypes = retType;
         for (int ii = 0; ii < busList.size(); ii++)
         {
            retTypes += "," + busList.elementAt(ii);
         }
         //System.out.println("  retTypes = '" + retTypes + "'");
      }

      // Expand the specified relationship, and post filter to return only the object type(s):
      DomainObject domObject = new DomainObject(objectId);
      MapList expandIDs = domObject.getRelatedObjects(context,
         relName,                                       // Expand this relationship
         DomainConstants.QUERY_WILDCARD,                // retrieving all object types
         expFromDir,                                    // in the from direction, if true
         expToDir,                                      // in the to direction, if true
         expMax,                                        // this many levels (0 = all)
         new StringList(DomainConstants.SELECT_ID),     // just get the objectid att
         new StringList(),								// don't bother with rel atts
         DomainConstants.EMPTY_STRING,                  // empty object where clause
         DomainConstants.EMPTY_STRING,                  // empty rel where clause
         null,                                          // don't filter out any relationships
         retTypes,                                      // post filter only the return type(s)
         null);

      //System.out.println("  ancestors: size() = " + ancestors.size());
      for (int ii = 0; ii < expandIDs.size(); ii++)
      {
         Map tempMap = (Map)expandIDs.get(ii);
         objIDs.add((String)tempMap.get(DomainConstants.SELECT_ID));
      }

      return(objIDs);
   }
//Added:14-Nov-09:oep:V6R2010xHF1:Sub/Derive Req Add Existing Enhancement

   /**
    * To obtain all the IDs of the requirments which are allocated, these are used as excludeOID program for
    * enhanced sub/derived requirement
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args JPO argument
    * @return StringList list of exclude Allocated Requirements
    * @throws MatrixException if operation fails
    */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public StringList excludeAllocatedRequirements(Context context, String[] args) throws MatrixException
   {
	   try {
		   String strTypePattern = ReqSchemaUtil.getRequirementSpecificationType(context);
		   String strWhereExpression = "";
		   StringList slObjectSelects = new StringList();
		   slObjectSelects.add(DomainConstants.SELECT_ID);
		   slObjectSelects.add(DomainConstants.SELECT_NAME);

		   // Find all specification objects
		   MapList mlSpecs = DomainObject.findObjects (context, strTypePattern, DomainConstants.QUERY_WILDCARD, strWhereExpression, slObjectSelects);

		   // For each specification find the requirements
		   StringList slAllocatedRequirements = new StringList();
		   DomainObject dmoSpec = DomainObject.newInstance(context);

		   for (Iterator itrSpecInfo = mlSpecs.iterator(); itrSpecInfo.hasNext();)
		   {
			   Map mapSpecInfo = (Map) itrSpecInfo.next();
			   String strSpecId = (String)mapSpecInfo.get(DomainConstants.SELECT_ID);

			   MapList mlSpecRequirements = getSpecificationRequirements(context, strSpecId, slObjectSelects, null);
			   for (Iterator itrReqs = mlSpecRequirements.iterator(); itrReqs.hasNext();)
			   {
				   Map mapReq = (Map) itrReqs.next();
				   String strReqId = (String)mapReq.get(DomainConstants.SELECT_ID);

				   if (!slAllocatedRequirements.contains(strReqId))
				   {
					   slAllocatedRequirements.add(strReqId);
				   }
			   }
		   }
		   return slAllocatedRequirements;
	}
	catch (Exception e)
	{
		e.printStackTrace();
		throw new MatrixException(e);
	}
   }

   /**
    * Returns the list of requirements from the requirement specifcation. Note: The Sub and Derived requirements are not returned.
    *
    * @param context The Matrix Context object
    * @param strSpecId The Requirement Specification ID
    * @param slObjectSelects The selectables on the objects, it will be ensured that at least object id is selected
    * @param slRelSelects The selectables on the relationship "Specification Structure", it will be ensured that at least relationship id is selected
    * @return the list of requirements from the requirement specifcation
    * @throws MatrixException if operation fails
    */
	public MapList getSpecificationRequirements(Context context, String strSpecId, StringList slObjectSelects, StringList slRelSelects) throws MatrixException
	{
		try {
			if (strSpecId == null || "".equals(strSpecId))
			{
				throw new IllegalArgumentException("strSpecId="+ strSpecId);
			}

			// Ensure good values in slObjectSelects
			if (slObjectSelects == null)
			{
				slObjectSelects = new StringList(DomainConstants.SELECT_ID);
			}
			else if (!slObjectSelects.contains(DomainConstants.SELECT_ID))
			{
				slObjectSelects.add(DomainConstants.SELECT_ID);
			}

			// Ensure good values in slRelSelects
			if (slRelSelects == null)
			{
				slRelSelects = new StringList(DomainRelationship.SELECT_ID);
			}
			else if (!slRelSelects.contains(DomainRelationship.SELECT_ID))
			{
				slRelSelects.add(DomainRelationship.SELECT_ID);
			}

			final String TYPE_CHAPTER = ReqSchemaUtil.getChapterType(context);
			final String TYPE_COMMENT = ReqSchemaUtil.getCommentType(context);
			final String SELECT_IS_KINDOF_REQUIREMENT = "type.kindof[" + ReqSchemaUtil.getRequirementType(context) + "]";

			String strRelationshipPattern = ReqSchemaUtil.getSpecStructureRelationship(context);
			StringBuffer strTypePattern = new StringBuffer();
			strTypePattern.append(TYPE_CHAPTER).append(",").append(TYPE_COMMENT).append(",").append(ReqSchemaUtil.getRequirementType(context));

			if (!slObjectSelects.contains(SELECT_IS_KINDOF_REQUIREMENT))
			{
				slObjectSelects.add(SELECT_IS_KINDOF_REQUIREMENT);
			}


			DomainObject dmoSpec = DomainObject.newInstance(context, strSpecId);

			MapList mlSpecObjects = dmoSpec.getRelatedObjects(context, strRelationshipPattern, strTypePattern.toString(), slObjectSelects, slRelSelects, false, true, (short)0, null, null, 0);
			MapList mlSpecReqs = new MapList();

			for (Iterator itrObjects = mlSpecObjects.iterator(); itrObjects.hasNext();)
			{
				Map mapObject = (Map) itrObjects.next();
				boolean isKindOfRequirement = "TRUE".equalsIgnoreCase((String)mapObject.get(SELECT_IS_KINDOF_REQUIREMENT));
				if (isKindOfRequirement)
				{
					mlSpecReqs.add(mapObject);
				}
			}

			return mlSpecReqs;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	/**
	 *  Validates the source and target object selection, in enhanced sub/derived requirement add existing functionality.
	 *
	 *  @param context The Matrix Context object
	 *  @param args The packed Map object containing information for validation algorithm. This information will be retrieved as below
	 *  		String strSrcSpecId                            = (String)programMap.get("SrcSpecId");
	 *  		StringList slSourceIds                       = (StringList)programMap.get("SourceIds");
	 *  		StringList slTargetIds                        = (StringList)programMap.get("TargetIds");
	 *  		String strRelationshipSymName = (String)programMap.get("RelationshipSymName");
	 *  		String strOperation                           = (String)programMap.get("Operation");
	 *  		String strSubOperation                   = (String)programMap.get("SubOperation");
	 *
	 *  @return This method will be expected to return error code
	 * 		0 means successful validation check
	 *         non-zero value means the failure in validation check
	 *         The internationalized message corresponding to this failure can be obtained from string resource key formed as below
	 *         emxRequirements.Validation.Error<return error code>
	 * 	@throws MatrixException If operation fails
	 *
	 */
	public int checkSourceAndTargetReqValidatyForLinking (Context context, String[] args) throws MatrixException
	{
		try {
			Map programMap = (Map)JPO.unpackArgs(args);

			StringList slSourceIds                       = (StringList)programMap.get("SourceIds");
			StringList slTargetIds                        = (StringList)programMap.get("TargetIds");
			StringList slSourceParentIds                       = (StringList)programMap.get("SourceParentIds");
			StringList slTargetParentIds                        = (StringList)programMap.get("TargetParentIds");
			String strRelationshipSymName = (String)programMap.get("RelationshipSymName");
			String strOperation                           = (String)programMap.get("Operation");
			String strSubOperation                   = (String)programMap.get("SubOperation");

			return checkSourceAndTargetReqValidatyForLinking (context, slSourceIds, slSourceParentIds, slTargetIds,slTargetParentIds, strRelationshipSymName, strOperation, strSubOperation);

		} catch (Exception e) {
			throw new MatrixException(e);
		}
	}
	/**
	 * For additional checking between source and target specification, customer
	 * can override JPO method emxSpecificationStructureBase.checkSourceAndTargetReqValidatyForLinking()
	 * in child JPO emxSpecificationStructure.
	 *
	 * This method is provided with following information
	 * @param context The Matrix Context object
	 * @param slSourceIds  				Selected source object ids
	 * @param slSourceParentIds			Selected source Parant object ids
	 * @param slTargetIds				Selected target object ids
	 * @param slTargetParentIds			Selected target Parent Object ids
	 * @param strRelationshipSymName	relationship_RequirementBreakdown or relationship_DerivedRequirement
	 * @param strOperation				"LinkExisting" or "CreateNewAndLink"
	 * @param strSubOperation			"AddChild" or "AddAbove" or "AddBelow"
	 * @return							This method will be expected to return error code
	 * 									0 means successful validation check
	 *         							non-zero value means the failure in validation check
	 *         							The internationalized message corresponding to this failure can be obtained from
	 *         							string resource key formed as below
	 *         							emxRequirements.Alert.Error<return error code>
	 * @throws MatrixException			If opertion fails
	 */
	public int checkSourceAndTargetReqValidatyForLinking (
		Context context,
		StringList slSourceIds,
		StringList slSourceParentIds,
		StringList slTargetIds,
		StringList slTargetParentIds,
		String strRelationshipSymName,
		String strOperation,		// "Link Existing" or "Create New and Link"
		String strSubOperation	// "Add as child" or "Add above" or "Add below"
		) throws MatrixException
	{
        	for (Iterator itrSourceIds = slSourceIds.iterator(); itrSourceIds
        		.hasNext();) {
        	    String strSourceId = (String) itrSourceIds.next();
        	    for (Iterator itrTargetIds = slTargetIds.iterator(); itrTargetIds
        		    .hasNext();) {
        		String strTargetId = (String) itrTargetIds.next();
        		for (Iterator itrSourceParentIds = slSourceParentIds.iterator(); itrSourceParentIds.hasNext();) {
        		    String strSourceParentId = (String) itrSourceParentIds.next();
        		    for (Iterator itrTargetParentIds = slTargetParentIds.iterator(); itrTargetParentIds.hasNext();) {
        		String strTargetParentId = (String) itrTargetParentIds.next();

        		int nStatus = checkSourceAndTargetReqValidatyForLinking(
        			context, strSourceId, strSourceParentId, strTargetId, strTargetParentId,
        			strRelationshipSymName, strOperation, strSubOperation);

        		if (nStatus != 0) {
        		    return nStatus;
        		}
        	      }
        		}
        	    }
        	}

        	return 0;
	}

	/**
	 * For additional checking between source and target specification, customer
	 * can override JPO method emxSpecificationStructureBase.checkSourceAndTargetReqValidatyForLinking()
	 * in child JPO emxSpecificationStructure.
	 * 	This method is provided with following information
	 * This method is provided with following information
	 * @param context The Matrix Context object
	 * @param strSourceId  				Selected source object ids
	 * @param strSourceParentId			Selected source Parant object ids
	 * @param strTargetId				Selected target object ids
	 * @param strTargetParentId			Selected target Parent Object ids
	 * @param strRelationshipSymName	relationship_RequirementBreakdown or relationship_DerivedRequirement
	 * @param strOperation				"LinkExisting" or "CreateNewAndLink"
	 * @param strSubOperation			"AddChild" or "AddAbove" or "AddBelow"
	 * @return							This method will be expected to return error code
	 * 									0 means successful validation check
	 *         							non-zero value means the failure in validation check
	 *         							The internationalized message corresponding to this failure can be obtained from
	 *         							string resource key formed as below
	 *         							emxRequirements.Alert.Error<return error code>
	 * @throws MatrixException			If opertion fails
	 *
	 * */
	public int checkSourceAndTargetReqValidatyForLinking (
		Context context,
		String strSourceId,
		String strSourceParentId,
		String strTargetId,
		String strTargetParentId,
		String strRelationshipSymName,
		String strOperation,
		String strSubOperation
		) throws MatrixException
{
	try {
	    // Some constants
	    final String OP_LINK_EXISTING = "LinkExisting";
	    final String OP_CREATE_NEW_AND_LINK = "CreateNewAndLink";
	    final String SUB_OP_ADD_AS_CHILD = "AddChild";
	    final String SUB_OP_ADD_ABOVE = "AddAbove";
	    final String SUB_OP_ADD_BELOW = "AddBelow";

	    final String TYPE_CHAPTER = ReqSchemaUtil.getChapterType(context);
	    final String TYPE_COMMENT = ReqSchemaUtil.getCommentType(context);
	    final String SELECT_IS_KINDOF_REQUIREMENT_SPECIFICATION = "type.kindof[" + ReqSchemaUtil.getRequirementSpecificationType(context) + "]";
	    final String SELECT_IS_KINDOF_REQUIREMENT = "type.kindof[" + ReqSchemaUtil.getRequirementType(context) + "]";
	    final String SELECT_IS_KINDOF_CHAPTER = "type.kindof[" + TYPE_CHAPTER + "]";
	    final String SELECT_IS_KINDOF_COMMENT = "type.kindof[" + TYPE_COMMENT + "]";

	    final int STATUS_OK = 0;
	    final int STATUS_KO_SRC_NOT_REQ = 1;
	    final int STATUS_KO_TARGET_NOT_REQ = 2;
	    final int STATUS_KO_TARGET_CHILD_OF_SRC = 3;
	    final int STATUS_KO_SRC_CHILD_OF_TARGET = 4;
	    final int STATUS_KO_TARGET_NOT_RSP_CH = 5;
	    final int STATUS_KO_TARGET_NOT_REQ_CH_COM = 6;
	    final int STATUS_KO_TARGET_ORPHAN = 7;
	    final int STATUS_KO_RECURSIVE_LINKING = 8;
	    final int STATUS_KO_RELEASE_OBSOLETE_REQ = 9;

	    //Argument check
	    if (strSourceId == null || "".equals(strSourceId))
	    {
	    	throw new IllegalArgumentException("strSourceId");
	    }
	    if (strTargetId == null || "".equals(strTargetId))
	    {
	    	throw new IllegalArgumentException("strTargetId");
	    }
	    if (strRelationshipSymName == null || "".equals(strRelationshipSymName))
	    {
	    	throw new IllegalArgumentException("strRelationshipSymName");
	    }
	    if (strOperation == null || "".equals(strOperation) || !(OP_LINK_EXISTING.equals(strOperation) || OP_CREATE_NEW_AND_LINK.equals(strOperation)))
	    {
	    	throw new IllegalArgumentException("strOperation="+strOperation);
	    }

	    // Some selectables
	    StringList slBusSelect = new StringList(4);
	    slBusSelect.add(SELECT_IS_KINDOF_REQUIREMENT_SPECIFICATION);
	    slBusSelect.add(SELECT_IS_KINDOF_REQUIREMENT);
	    slBusSelect.add(SELECT_IS_KINDOF_CHAPTER);
	    slBusSelect.add(SELECT_IS_KINDOF_COMMENT);

	    // Find out the types of the selected objects
	    DomainObject dmoSrcObject = DomainObject.newInstance(context, strSourceId);
	    Map mapSrcObject = dmoSrcObject.getInfo(context, slBusSelect);

	    State curState = dmoSrcObject.getCurrentState(context);
	    String strState = curState.getName();
	    DomainObject dmoTargetObject = DomainObject.newInstance(context, strTargetId);
	    Map mapTargetObject = dmoTargetObject.getInfo(context, slBusSelect);

	    boolean isSrcRequirement = "TRUE".equalsIgnoreCase((String)mapSrcObject.get(SELECT_IS_KINDOF_REQUIREMENT));

	    boolean isTargetRSP            = "TRUE".equalsIgnoreCase((String)mapTargetObject.get(SELECT_IS_KINDOF_REQUIREMENT_SPECIFICATION));
	    boolean isTargetRequirement = "TRUE".equalsIgnoreCase((String)mapTargetObject.get(SELECT_IS_KINDOF_REQUIREMENT));
	    boolean isTargetChapter       = "TRUE".equalsIgnoreCase((String)mapTargetObject.get(SELECT_IS_KINDOF_CHAPTER));
	    boolean isTargetComment     = "TRUE".equalsIgnoreCase((String)mapTargetObject.get(SELECT_IS_KINDOF_COMMENT));

	    // Find if the target object is orphan
	    emxRequirement_mxJPO requirement = new emxRequirement_mxJPO(context, new String[0]);
	    Map mapArgs = new HashMap();
	    mapArgs.put("objectId", strTargetId);
	    String[] args = JPO.packArgs(mapArgs);
	    MapList mlRSPs = requirement.getRequirementSpecifications(context, args);
	    boolean isTargetOrphan = (mlRSPs == null || mlRSPs.size() == 0)? true:false;

	    DomainObject dmoTargetParentObject = DomainObject.newInstance(context, strTargetParentId);
	    Map mapTargetParentObject = dmoTargetParentObject.getInfo(context, slBusSelect);
	    boolean isTargetParentOrphan = "TRUE".equalsIgnoreCase((String)mapTargetParentObject.get(SELECT_IS_KINDOF_REQUIREMENT));

	    // Source object must be requirement
	    if (!isSrcRequirement)
	    {
	    	return STATUS_KO_SRC_NOT_REQ;
	    }

	    if (strSourceId.equals(strTargetId))
	    {
		return STATUS_KO_RECURSIVE_LINKING;
	    }

	    if(strSourceId !=null)
	    {
			if(strState.equals("Release") || strState.equals("Obsolete"))
			{
			  return STATUS_KO_RELEASE_OBSOLETE_REQ;
			}
	    }

	    // Check mode
	    if (OP_LINK_EXISTING.equals(strOperation))
	    {
	    	// Target object must be requirement
	    	if (!isTargetRequirement)
	    	{
	    		return STATUS_KO_TARGET_NOT_REQ;
	    	}

	    	if (isAlreadyChild(context, strSourceId, strTargetId, strRelationshipSymName))
	    	{
	    		return STATUS_KO_TARGET_CHILD_OF_SRC;
	    	}

	    	if (isAlreadyChild(context, strTargetId, strSourceId, strRelationshipSymName))
	    	{
	    		return STATUS_KO_SRC_CHILD_OF_TARGET;
	    	}
	    }
	    else
	    {
	    	if (strSubOperation == null || "".equals(strSubOperation) || !(SUB_OP_ADD_AS_CHILD.equals(strSubOperation) || SUB_OP_ADD_ABOVE.equals(strSubOperation) || SUB_OP_ADD_BELOW.equals(strSubOperation)))
	    	{
	    		throw new IllegalArgumentException("strSubOperation="+strSubOperation);
	    	}
	    	if (isTargetRequirement && isTargetOrphan)
	    	{
	    	    return STATUS_KO_TARGET_ORPHAN;
	    	}

	    	if(isTargetRequirement && isTargetParentOrphan)
	    	{
	    	    return STATUS_KO_TARGET_ORPHAN;
	    	}

	    	if (SUB_OP_ADD_AS_CHILD.equals(strSubOperation))
	    	{
	    		if (! (isTargetRSP || isTargetChapter))
	    		{
	    			return STATUS_KO_TARGET_NOT_RSP_CH;
	    		}
	    	}
	    	else
	    	{
	    		if (! (isTargetRequirement || isTargetChapter || isTargetComment))
	    		{
	    			return STATUS_KO_TARGET_NOT_REQ_CH_COM;
	    		}
	    	}
	    }

	    return STATUS_OK;
	} catch (Exception e) {
	    throw new MatrixException(e);
	}
}

	 /** Checks if the given target requirement is found in the sub/derived requirement hierarchy of the source requirement.
	 *
	 * @param context
	 * @param strSourceReqId
	 * @param strTargetReqId
	 * @param strRelationshipSymName Either "relationship_RequirementBreakdown" or "relationship_DerivedRequirement"
	 * @return
	 * @throws MatrixException
	 */
	private boolean isAlreadyChild (Context context, String strSourceReqId, String strTargetReqId, String strRelationshipSymName) throws MatrixException
	{
		try {
			if (strSourceReqId == null || "".equals(strSourceReqId.trim()))
			{
				throw new IllegalArgumentException("strSourceReqId="+strSourceReqId);
			}
			if (strTargetReqId == null || "".equals(strTargetReqId.trim()))
			{
				throw new IllegalArgumentException("strTargetReqId="+strTargetReqId);
			}
			if (strRelationshipSymName == null || "".equals(strRelationshipSymName.trim()))
			{
				throw new IllegalArgumentException("strRelationshipSymName="+strRelationshipSymName);
			}

			String strRelPattern = PropertyUtil.getSchemaProperty(context, strRelationshipSymName);

			DomainObject dmoSourceObject = DomainObject.newInstance(context, strSourceReqId);
			MapList mlChildRequirements = dmoSourceObject.getRelatedObjects (context,
											strRelPattern,
											ReqSchemaUtil.getRequirementType(context),
											new StringList(DomainConstants.SELECT_ID),
											null,
											false,
											true,
											(short)0,
											"id=='" + strTargetReqId + "'",
											"",
											0);

			return (mlChildRequirements.size() != 0);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}
	}

	//End:V6R2010xHF1:Sub/Derive Req Add Existing Enhancement


	//Added:19-Nov-09:oep:V6R2010xHF1:Requirement Allocation Status Icon
	  /**
	    * Method shows requirement allocation status Icon if the requirement has sub/derived requirements
	    *
	    * @param context the eMatrix <code>Context</code> object
	    * @param args holds list of object Ids in a program map
	    * @return Vector - HTML output strings representing the reserve status icons
	    * @throws Exception if the operation fails
	    * @since RequirementsManagement V6R2010xHF1
	    */
	public static Vector getAllocationStatusIcon(Context context, String[] args)  throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		MapList rowList = (MapList) programMap.get(OBJECT_LIST);

		Map paramMap = (Map) programMap.get("paramList");
		String reportFormat = (String) paramMap.get("reportFormat");
		boolean reportHTML = ("HTML".equals(reportFormat));
		boolean hyperLink = (reportFormat == null);
		boolean renderHTML = (reportHTML || hyperLink);
		
		String subReqLabel = EnoviaResourceBundle.getProperty(context, ReqConstants.BUNDLE_REQUIREMENTS , context.getLocale(), "emxRequirements.Label.SubRequirements");
		
		//final String RELATIONSHIP_DERIVED_REQUIREMENT = ReqSchemaUtil.getDerivedRequirementRelationship(context);
		final String RELATIONSHIP_SUB_REQUIREMENT = ReqSchemaUtil.getSubRequirementRelationship(context);
		final String ATTRIBUTE_LINK_STATUS				= ReqSchemaUtil.getLinkStatusAttrubite(context);

		final String SELECT_IS_KINDOF_REQUIREMENT = "type.kindof[" + ReqSchemaUtil.getRequirementType(context) + "]";

		//final String SELECT_HAS_DERIVED_REQUIREMENT_FROM = "from[" + RELATIONSHIP_DERIVED_REQUIREMENT + "]";
		final String SELECT_HAS_SUB_REQUIREMENT_FROM = "from[" + RELATIONSHIP_SUB_REQUIREMENT + "]";
		//final String SELECT_HAS_DERIVED_REQUIREMENT_TO = "to[" + RELATIONSHIP_DERIVED_REQUIREMENT + "]";
		final String SELECT_HAS_SUB_REQUIREMENT_TO  = "to[" + RELATIONSHIP_SUB_REQUIREMENT + "]";
		final String SELECT_SUB_REQUIREMENT_FROM_NAMES =   "from[" + RELATIONSHIP_SUB_REQUIREMENT + "].to.name";
		//final String SELECT_DERIVED_REQUIREMENT_FROM_NAMES =   "from[" + RELATIONSHIP_DERIVED_REQUIREMENT + "].to.name";
		final String SELECT_SUB_REQUIREMENT_TO_NAMES =      "to[" + RELATIONSHIP_SUB_REQUIREMENT + "].from.name";
		//final String SELECT_DERIVED_REQUIREMENT_TO_NAMES =     "to[" + RELATIONSHIP_DERIVED_REQUIREMENT + "].from.name";


		final String SELECT_SUB_REQUIREMENT_FROM_STATUS     =   SELECT_HAS_SUB_REQUIREMENT_TO + "." + "attribute[" + ATTRIBUTE_LINK_STATUS + "].value";
		//final String SELECT_DERIVED_REQUIREMENT_FROM_STATUS   = SELECT_HAS_DERIVED_REQUIREMENT_TO + "." + "attribute[" + ATTRIBUTE_LINK_STATUS + "].value";
		final String SELECT_SUB_REQUIREMENT_TO_STATUS     =  SELECT_HAS_SUB_REQUIREMENT_FROM + "." + "attribute[" + ATTRIBUTE_LINK_STATUS + "].value";
		//final String SELECT_DERIVED_REQUIREMENT_TO_STATUS    =  SELECT_HAS_DERIVED_REQUIREMENT_FROM + "." + "attribute[" + ATTRIBUTE_LINK_STATUS + "].value";

		String strLanguage = context.getSession().getLanguage();
		final String STRING_REQUIREMENT_NOT_ALLOCATED = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), ICON_TOOLTIP_REQUIREMENT_NOT_ALLOCATED); 

		//StringList lstAtt = new StringList(SELECT_HAS_DERIVED_REQUIREMENT_FROM);
		StringList lstAtt = new StringList(SELECT_HAS_SUB_REQUIREMENT_FROM);
		//lstAtt.addElement(SELECT_HAS_DERIVED_REQUIREMENT_TO);
		lstAtt.addElement(SELECT_HAS_SUB_REQUIREMENT_TO);
		lstAtt.addElement(SELECT_SUB_REQUIREMENT_FROM_NAMES);
		//lstAtt.addElement(SELECT_DERIVED_REQUIREMENT_FROM_NAMES);
		lstAtt.addElement(SELECT_SUB_REQUIREMENT_TO_NAMES);
		//lstAtt.addElement(SELECT_DERIVED_REQUIREMENT_TO_NAMES);
		lstAtt.addElement(SELECT_SUB_REQUIREMENT_FROM_STATUS);
		//lstAtt.addElement(SELECT_DERIVED_REQUIREMENT_FROM_STATUS);
		lstAtt.addElement(SELECT_SUB_REQUIREMENT_TO_STATUS);
		//lstAtt.addElement(SELECT_DERIVED_REQUIREMENT_TO_STATUS);
		lstAtt.addElement(SELECT_IS_KINDOF_REQUIREMENT);
		
		 HashMap paramList = (HashMap) programMap.get("paramList");
	        String strExport = (String) paramList.get("exportFormat");
	        boolean toExport = false;
	        if (strExport != null) 
	                toExport = true;
	        
		int iRowCount = rowList.size();
		String[] rowIds = new String[iRowCount];
		String[] idConnetion = new String[iRowCount];
		// Put all the row Ids into an array...
		for (int iCount = 0; iCount < iRowCount; iCount++)
		{
			// Get the object id for each object in the table...
			Map mapRow = (Map)rowList.get(iCount);
			rowIds[iCount] = (String)mapRow.get(SELECT_ID);
			//get the from rel_ID
			idConnetion[iCount] = (String)mapRow.get(SELECT_RELATIONSHIP_ID);
		}
		Vector lstIcons = new Vector(iRowCount);

		// Use the array of Ids to get all the reservedby names...
		BusinessObjectWithSelectList resByAtts = BusinessObject.getSelectBusinessObjectData(context, rowIds, lstAtt);
		int oRowCount = resByAtts.size();

        String denied = EnoviaResourceBundle.getProperty(context, ReqConstants.BUNDLE_FRAMEWORK , context.getLocale(), "emxFramework.Basic.DENIED");
		// Put each info into the output list...
		for (int oCount = 0; oCount < oRowCount; oCount++)
		{
			// Look up the full name only if the object is reserved by someone...
			BusinessObjectWithSelect busWithSelect =  resByAtts.getElement(oCount);

			boolean isKindOfRequirement = "true".equalsIgnoreCase(busWithSelect.getSelectData(SELECT_IS_KINDOF_REQUIREMENT));
			//boolean hasDerivedRequirementFrom = "true".equalsIgnoreCase(busWithSelect.getSelectData(SELECT_HAS_DERIVED_REQUIREMENT_FROM));
			boolean hasSubRequirementFrom = "true".equalsIgnoreCase(busWithSelect.getSelectData(SELECT_HAS_SUB_REQUIREMENT_FROM));
			//boolean hasDerivedRequirementTo = "true".equalsIgnoreCase(busWithSelect.getSelectData(SELECT_HAS_DERIVED_REQUIREMENT_TO));
			boolean hasSubRequirementTo = "true".equalsIgnoreCase(busWithSelect.getSelectData(SELECT_HAS_SUB_REQUIREMENT_TO));

			StringList slSubReqFromNames = busWithSelect.getSelectDataList(SELECT_SUB_REQUIREMENT_FROM_NAMES);
			//StringList slDerivedReqFromNames = busWithSelect.getSelectDataList(SELECT_DERIVED_REQUIREMENT_FROM_NAMES);
			StringList slSubReqToNames = busWithSelect.getSelectDataList(SELECT_SUB_REQUIREMENT_TO_NAMES);
			//StringList slDerivedReqToNames = busWithSelect.getSelectDataList(SELECT_DERIVED_REQUIREMENT_TO_NAMES);
			//boolean hasLink = hasDerivedRequirementFrom || hasSubRequirementFrom || hasDerivedRequirementTo || hasSubRequirementTo;
			boolean hasLink = hasSubRequirementFrom || hasSubRequirementTo;

			Map mapRow = (Map)rowList.get(oCount);
			String strObjectId = (String)mapRow.get(SELECT_ID);

			String strHtml = " ";
			String strIcon = "";
			String strComm = "";
			String finalimageURL = "";
			if(isKindOfRequirement)
			{
				if (hasLink)
				{
					String strToAndFromStatus = busWithSelect.getSelectData(SELECT_SUB_REQUIREMENT_FROM_STATUS) +
							/*busWithSelect.getSelectData(SELECT_DERIVED_REQUIREMENT_FROM_STATUS) +*/ "|" +
							busWithSelect.getSelectData(SELECT_SUB_REQUIREMENT_TO_STATUS) /*+
							busWithSelect.getSelectData(SELECT_DERIVED_REQUIREMENT_TO_STATUS)*/;
					statusLink statFrom = getStatusLink(strToAndFromStatus, directionLink.from);
					statusLink statTo = getStatusLink(strToAndFromStatus, directionLink.to);
                    if(statFrom == statusLink.none && statTo == statusLink.none)
                    {
                    	strHtml = denied; //has link but couldn't get the link status
                    }
                    else
                    {

						if(idConnetion[oCount] != null && !"".equals(idConnetion[oCount])) {
							DomainRelationship rel = DomainRelationship.newInstance(context,idConnetion[oCount]);
							String allocStatusSubValue = rel.getAttributeValue(context, "Link Status");
							strToAndFromStatus = allocStatusSubValue + "|" +
									busWithSelect.getSelectData(SELECT_SUB_REQUIREMENT_TO_STATUS) /*+
									busWithSelect.getSelectData(SELECT_DERIVED_REQUIREMENT_TO_STATUS)*/;
						}
						statFrom = getStatusLink(strToAndFromStatus, directionLink.from);

    					if(renderHTML)
    					{
    						if (statFrom == statusLink.none) {
    							strIcon = getToDirIcon(statTo);
    							strComm = getToolTipBoth(context, strLanguage, statusLink.none,statTo);
    						} else {
    							if (statTo == statusLink.none) {
    								strIcon = getFromDirIcon(statFrom);
    								strComm = getToolTipBoth(context, strLanguage, statFrom,statusLink.none);
    							} else {
    								strIcon = getBothDirIcon(statFrom, statTo);
    								strComm = getToolTipBoth(context, strLanguage, statFrom,statTo);
    							}
    						}
    						// Split the two arrow images
    						StringList arr = new StringList();
    						if(strIcon.contains(",")){
    							arr = FrameworkUtil.split(strIcon, ",");
    
    						}
    						else{
    							arr.add(strIcon);
    						}
    
    
    						for(int i=0; i<arr.size(); i++){
    							String strUrl = (String )arr.get(i);
    							strHtml += "<a href=\"javascript:displaySubInformations('"+rowIds[oCount]+"','"+subReqLabel+"');\" title=\""+subReqLabel+"\">";
    							strHtml += "<img onload='javascript:getFloatingDiv(\""+ rowIds[oCount] +"\",\"\")' src=\"" + strUrl + "\" border=\"0\"  align=\"middle\" " + "title=\"" + " " + strComm + "\"" + "/>";
    							strHtml += "<span style=\"display:none\">" + strComm + "</span></a>";
    							
    							if(toExport)
    							    strHtml = strComm;
    						}
    					}
    					else
    					{
    						if (statFrom == statusLink.none) {
    							strHtml = "'-->";
    						} else {
    							if (statTo == statusLink.none) {
    								strHtml = "'<--";
    							} else {
    								strHtml = "'<-->";
    							}
    						}
    					}
    				}
				}
				else
				{
					strIcon =  ICON_REQUIREMENT_NOT_ALLOCATED;
					strComm = STRING_REQUIREMENT_NOT_ALLOCATED;
					strHtml = "<img src=\"" + strIcon + "\" border=\"0\"  align=\"middle\" " + "title=\"" + " " + strComm + "\"" + "/>";
					strHtml += "<span style=\"display:none\">" + strComm + "</span>";
					
                    if(toExport)
                        strHtml = strComm;

				}

			}
			lstIcons.add(strHtml);
		}
		return lstIcons;
	}

	   
	   /**
	    * Function to find the relationship link status attribute value.
	    * @param statFrom From side relationship link status value
	    * @param statTo To side relationship lik status value
	    * @return String value
	    */
	   private static String getToolTipBoth(Context context, String strLanguage, statusLink statFrom, statusLink statTo) throws Exception {

		   String strTooltip = "";
		   String from = statFrom.toString();
		   String to = statTo.toString();

		   if(from.equals("none") && "none".equals(from))
		   {
			    StringBuffer sbStatus = new StringBuffer("emxFramework.Range.Link_Status.");
				sbStatus.append(to);
	            strTooltip = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), sbStatus.toString()); 
		   }
		   else if(to.equals("none") && "none".equals(to))
		   {
			   StringBuffer sbStatus = new StringBuffer("emxFramework.Range.Link_Status.");
			   sbStatus.append(from);
	           strTooltip = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), sbStatus.toString());
		   }
		   else if(from != null && to!= null)
		   {
			   StringBuffer sbStatusFrom = new StringBuffer("emxFramework.Range.Link_Status.");
			   sbStatusFrom.append(from);
			   from = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), sbStatusFrom.toString()); 
			   StringBuffer sbStatusTo = new StringBuffer("emxFramework.Range.Link_Status.");
			   sbStatusTo.append(to);
			   to = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), sbStatusTo.toString());
			   strTooltip = from+","+to;
		   }
		   return strTooltip;
		}

	   /**
	    * Method return the To side direction Icon image
	    * @param statLink Link status attribute value
	    * @return String icon image
	    */
		private static String getToDirIcon(statusLink statLink) {
			switch (statLink) {
				case Valid: return ICON_REQUIREMENT_VALID_TO;
				case Invalid: return ICON_REQUIREMENT_INVALID_TO;
				case Suspect: return ICON_REQUIREMENT_SUSPECT_TO;

				default: return "";
			}
		}

		/**
		    * Method return the From side direction Icon image
		    * @param statLink Link status attribute value
		    * @return String icon image
		    */
	   private static String getFromDirIcon(statusLink statLink) {
			switch (statLink) {
				case Valid: return ICON_REQUIREMENT_VALID_FROM;
				case Invalid: return ICON_REQUIREMENT_INVALID_FROM;
				case Suspect: return ICON_REQUIREMENT_SUSPECT_FROM;

				default: return "";
			}
		}

	   /**
	    * Method return the Both side direction Icon image
	    * @param statLink Link status attribute value
	    * @return String icon image
	    */
		private static String getBothDirIcon(statusLink statFrom, statusLink statTo) {
			switch (statFrom) {
				case Valid: {
					switch (statTo) {
						case Valid: return ICON_REQUIREMENT_VALID_FROM + "," + ICON_REQUIREMENT_VALID_TO; //ICON_REQUIREMENT_VALID_BOTH;
						case Invalid: return ICON_REQUIREMENT_VALID_FROM + "," +  ICON_REQUIREMENT_INVALID_TO; // ICON_REQUIREMENT_VALID_INVALID_BOTH;
						case Suspect: return ICON_REQUIREMENT_VALID_FROM + "," + ICON_REQUIREMENT_SUSPECT_TO; //ICON_REQUIREMENT_VALID_SUSPECT_BOTH;
					}
					break;
				}

				case Invalid: {
					switch (statTo) {
						case Valid: return ICON_REQUIREMENT_INVALID_FROM +"," + ICON_REQUIREMENT_VALID_TO; //ICON_REQUIREMENT_INVALID_VALID_BOTH;
						case Invalid: return ICON_REQUIREMENT_INVALID_FROM +"," + ICON_REQUIREMENT_INVALID_TO; //ICON_REQUIREMENT_INVALID_BOTH;
						case Suspect: return ICON_REQUIREMENT_INVALID_FROM +"," + ICON_REQUIREMENT_SUSPECT_TO ;//ICON_REQUIREMENT_INVALID_SUSPECT_BOTH;
					}
					break;
				}

				case Suspect: {
					switch (statTo) {
						case Valid: return ICON_REQUIREMENT_SUSPECT_FROM + "," + ICON_REQUIREMENT_VALID_TO; //ICON_REQUIREMENT_SUSPECT_VALID_BOTH;
						case Invalid: return ICON_REQUIREMENT_SUSPECT_FROM + "," + ICON_REQUIREMENT_INVALID_TO; //ICON_REQUIREMENT_SUSPECT_INVALID_BOTH;
						case Suspect: return ICON_REQUIREMENT_SUSPECT_FROM + "," + ICON_REQUIREMENT_SUSPECT_TO; // ICON_REQUIREMENT_SUSPECT_BOTH;
					}
					break;
				}
				default: return "";
			}
			return "";
		}

		/**
		 * Method return the status attribute value of relationship
		 * @param statLink Link status attribute value
		 * @return String icon image
		 */
		 private static statusLink getStatusLink(String strToAndFromStatus, directionLink bDir) {
			statusLink result = statusLink.none;
			String partS = "";
			int dir = 0; // Left part of the list of links

			if (bDir == directionLink.to) {
				dir = 1; // Right part of the list of links
			}

			try {
				partS = strToAndFromStatus.split("\\|")[dir];
			} catch (Exception e) {
				partS = "";
			}

			/*
			 * Ranked from best to worst. First valid state, then suspect and finally invalid
			 */
			if (partS.contains("Valid")) {
				result = statusLink.Valid;
			}
			if (partS.contains("Suspect")) {
				result = statusLink.Suspect;
			}
			if (partS.contains("Invalid")) {
				result = statusLink.Invalid;
			}

			return result;
		}

//End::V6R2010xHF1:Requirement Allocation Status Icon
	   /** Validates the source and target object selection, in enhanced sub/derived requirement add existing functionality.
	    *  Function called from Specification Structure View for sub/Derived Requirement.
	    *
	    * @param context the eMatrix <code>Context</code> object
	    * @param args holds list of object Ids in a program map
	    * @return int - error code.
	    * @throws MatrixException if the operation fails
	    * @since RequirementsManagement V6R2010xHF1
	    */

	   public int validateAlreadyDerivedRequirement(Context context, String[] args)
	    throws MatrixException
           {
        	try {
        	    Map programMap = (Map) JPO.unpackArgs(args);

        	    String slSourceIds = (String) programMap.get("SourceIds");
        	    String slTargetIds = (String) programMap.get("TargetIds");
        	    String strRelationshipSymName = (String) programMap.get("RelationshipSymName");

        	    return validateAlreadyDerivedRequirement(context, slSourceIds,
        		    slTargetIds, strRelationshipSymName);

        	} catch (Exception e) {
        	    throw new MatrixException(e);
        	}
            }

	   /**
	    * Validates is derived requirement is already available and return the error code.
	    *
	    * @param context the eMatrix <code>Context</code> object
	    * @param strSourceReqId Selected Source Id
	    * @param strTargetReqId Selected Target Id
	    * @param strRelationshipSymName Relationship Sub and Derived
	    * @return int error code
	    * @throws Exception if operation fails
	    */
	   public int validateAlreadyDerivedRequirement(Context context, String strSourceReqId, String strTargetReqId, String strRelationshipSymName)
	   throws Exception
           {
        	final int STATUS_KO_TARGET_CHILD_OF_SRC = 3;
        	final int STATUS_OK = 0;

        	String strRelPattern = PropertyUtil.getSchemaProperty(context, strRelationshipSymName);
		    DomainObject dmoSourceObject = DomainObject.newInstance(context, strSourceReqId);

        	MapList mlChildRequirements = dmoSourceObject.getRelatedObjects (context,
										strRelPattern,
										ReqSchemaUtil.getRequirementType(context),
										new StringList(DomainConstants.SELECT_ID),
										null,
										false,
										true,
										(short)0,
										"id=='" + strTargetReqId + "'",
										"",
										0);

        	int iRowCount = mlChildRequirements.size();
                for (int jj = 0; jj < iRowCount; jj++)
                {
                    Map mapObject = (Map) mlChildRequirements.get(jj);
                    String strObjId = (String) mapObject.get(SELECT_ID);
                    String strRelationName = (String) mapObject.get("relationship");

                    if(strTargetReqId.equals(strObjId) && strRelationshipSymName.equals(strRelationName))
                    {
                	return STATUS_KO_TARGET_CHILD_OF_SRC;
                    }
                }
        	    return STATUS_OK;

            }

	   /**
	    *  Method shows "Label Function" and "Label Program" settings created for Menu type_SoftwareRequirementSpecification
	    *  such that if the object policy is "Version", its Title attribute be used as the Label,
	    *  otherwise its Name/Revision pair should be used as the Label.
	    *
	    * @param context the eMatrix <code>Context</code> object
	    * @param args args holds list of object Ids in a program map
	    * @return String label
	    * @throws Exception if the operation fails
	    * @since RequirementsManagement V6R2011x
	    */

	   public  static String getTreeLabelName(Context context, String[] args)
	   throws Exception
	 {
	   HashMap programMap = (HashMap) JPO.unpackArgs(args);
	   HashMap paramMap = (HashMap)programMap.get("paramMap");
	   String id=(String)paramMap.get("objectId");
	   BusinessObject bo=new BusinessObject(id);
	   DomainObject  dob = new DomainObject(bo);
	   List lstSelectableList = new StringList();
       lstSelectableList.add(DomainConstants.SELECT_NAME);
       lstSelectableList.add(DomainConstants.SELECT_REVISION);
       lstSelectableList.add(DomainConstants.SELECT_POLICY);
       lstSelectableList.add("attribute[Title]");
       String name = "";

	   Map map = dob.getInfo(context,(StringList)lstSelectableList);
       String strNameResult = (String) map.get(DomainConstants.SELECT_POLICY);
	   if(strNameResult.equals("Version"))
	   {
		   name = (String) map.get("attribute[Title]");
	   }
	   else
	   {
		   name = (String) map.get(DomainConstants.SELECT_NAME) + " " +(String) map.get(DomainConstants.SELECT_REVISION) ;
       }

	   //String name=dob.getInfo(context,DomainConstants.SELECT_NAME);
	   return name;
	 }

	   /**
	    *  get the child relationships to consider for "delete select object and children" operation.
	    *  returned result is based on user preference.
	    *
	    * @param context the eMatrix <code>Context</code> object
	    * @param args args
	    * @return String comma separated relantionship names
	    * @throws Exception if the operation fails
	    * @since RequirementsManagement V6R2012x
	    */
	 public String getSpecChildRelationshipsToDelete(Context context, String[] args) throws Exception
	 {
		   String expandRel = "relationship_SpecificationStructure" + ",";
		   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_DELETE_CHILDREN_INC_DERIVED_REQ) ? "relationship_DerivedRequirement": "";
		   expandRel += expandRel.length() > 0 ? "," : "";
		   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_DELETE_CHILDREN_INC_SUB_REQ) ? "relationship_RequirementBreakdown" : "";
		   return expandRel;
	 }

	   /**
	    *  returns "Yes"/"No" value for reserveSub & reserveDerived fields on RMTSpecTreeReserveForm.
	    *
	    * @param context the eMatrix <code>Context</code> object
	    * @param args args holds fieldMap
	    * @return Object field value of "Yes"/"No"
	    * @throws Exception if the operation fails
	    * @since RequirementsManagement V6R2012x
	    */
	 public  static Object getReserveSubDerived(Context context, String[] args)
	 throws Exception
	 {
		 Map programMap = (Map) JPO.unpackArgs(args);
		 Map fieldMap = (Map)programMap.get("fieldMap");
		 String name = (String)fieldMap.get("name");
		 StringList fieldValues = new StringList();
		 if("reserveSub".equalsIgnoreCase(name)){
			 fieldValues.addElement(RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_RESERVE_INC_SUB_REQ) ? "Yes" : "No");
		 }
		 else if("reserveDerived".equalsIgnoreCase(name)){
			 fieldValues.addElement(RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_RESERVE_INC_DERIVED_REQ) ? "Yes" : "No");
		 }
		 else if("reserveParam".equalsIgnoreCase(name)){
			 fieldValues.addElement(RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_RESERVE_INC_PARAM) ? "Yes" : "No");
		 }
		 return fieldValues;
	 }
	   /**
	    *  returns "Yes"/"No" value for unreserveSub & unreserveDerived fields on RMTSpecTreeUnreserveForm.
	    *
	    * @param context the eMatrix <code>Context</code> object
	    * @param args args holds fieldMap
	    * @return Object filed value of "Yes"/"No"
	    * @throws Exception if the operation fails
	    * @since RequirementsManagement V6R2012x
	    */
	 public  static Object getUnreserveSubDerived(Context context, String[] args)
	 throws Exception
	 {
		 Map programMap = (Map) JPO.unpackArgs(args);
		 Map fieldMap = (Map)programMap.get("fieldMap");
		 String name = (String)fieldMap.get("name");
		 StringList fieldValues = new StringList();
		 if("unreserveSub".equalsIgnoreCase(name)){
			 fieldValues.addElement(RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_RESERVE_INC_SUB_REQ) ? "Yes" : "No");
		 }
		 else if("unreserveDerived".equalsIgnoreCase(name)){
			 fieldValues.addElement(RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_RESERVE_INC_DERIVED_REQ) ? "Yes" : "No");
		 }
		 else if("unreserveParam".equalsIgnoreCase(name)){
			 fieldValues.addElement(RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_RESERVE_INC_PARAM) ? "Yes" : "No");
		 }
		 return fieldValues;
	 }
	   /**
	    *  returns range value of Yes/No.
	    *
	    * @param context the eMatrix <code>Context</code> object
	    * @param args args holds requestMap
	    * @return Object range value of "Yes"/"No"
	    * @throws Exception if the operation fails
	    * @since RequirementsManagement V6R2012x
	    */
	 public  static Object getRangeReserveSubDerived(Context context, String[] args)
	 throws Exception
	 {
		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 Map requestMap = (Map)programMap.get("requestMap");
		 HashMap tempMap = new HashMap();
		 StringList fieldRangeValues = new StringList();
		 fieldRangeValues.addElement("Yes");
		 fieldRangeValues.addElement("No");
		 StringList fieldDisplayRangeValues = new StringList();
		 String languageStr = context.getSession().getLanguage();
		 fieldDisplayRangeValues.addElement(EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_REQUIREMENTS_STR, context.getLocale(), "emxRequirements.Range.YesNo.Yes"));
		 fieldDisplayRangeValues.addElement(EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_REQUIREMENTS_STR, context.getLocale(), "emxRequirements.Range.YesNo.No"));
		 tempMap.put("field_choices", fieldRangeValues);
		 tempMap.put("field_display_choices", fieldDisplayRangeValues);
		 return tempMap;
	 }

	 /**
    *  returns the list of parameters value is Parameters are available on database and activated on emxRequirement.properties.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args not used
    * @return The Parameter Value
    * @throws Exception if the operation fails
    * @since RequirementsManagement V6R2013x
    */
	 public  static Object getParameterValue(Context context, String[] args)
	 throws Exception
	 {
		 Vector returnList = new Vector();
		 HashMap programMap = (HashMap) JPO.unpackArgs(args);

	     // Get the 'paramMap' HashMap from the programMap (for the created Object ID)
	     MapList objectListMap = (MapList) programMap.get(OBJECT_LIST);
	     String[] rowIds = new String[objectListMap.size()];
	     // Put all the row Ids into an array...
		 for (int iCount = 0; iCount < objectListMap.size(); iCount++)
		 {
			 // Get the object id for each object in the table...
			 Map mapRow = (Map)objectListMap.get(iCount);
			 rowIds[iCount] = (String)mapRow.get(SELECT_ID);
		 }
		 //Find for each parameter, the used interface
		 StringList lstInterfaceAtt = new StringList("interface");
		 // Use the array of Ids to get interface used for parameters.
		 BusinessObjectWithSelectList interfaceAtts = BusinessObject.getSelectBusinessObjectData(context, rowIds, lstInterfaceAtt);
	      
		 int oRowCount = interfaceAtts.size();
		 StringList lstParamValueAtt = new StringList();
		 // Put each info into the output list...
		 for (int oCount = 0; oCount < oRowCount; oCount++)
		 {
			//put value of each parameter in the output vector.
			//put an empty string for non parameters
			BusinessObjectWithSelect busWithSelect =  interfaceAtts.getElement(oCount);
			String ParamValue = busWithSelect.getSelectData("interface");
			if((ParamValue.length()>0)&&(lstParamValueAtt.contains("attribute[" + ParamValue + "Value]")==false))
			{
				 lstParamValueAtt.add("attribute[" + ParamValue + "Value]");
			}
		 }
		 BusinessObjectWithSelectList resByTest = BusinessObject.getSelectBusinessObjectData(context, rowIds, lstParamValueAtt);
		 for(int j=0;j<resByTest.size();j++)
		 {	
			 String Test = "";
			 for(int i = 0; i < lstParamValueAtt.size(); i++)
			 {
				 String ParamValue = resByTest.getElement(j).getSelectData((String)lstParamValueAtt.get(i));
				 if(ParamValue.length()>0)
				 {
					 Test = ParamValue;
				 }
			 }
			 	returnList.addElement(Test);
		 }
		 return returnList;
	 }
	 
	 
	 /**
	  * This method returns all products attached to a given model.
	  *
	  * Used in the expansion of Model for "Product Revision" Effectivity.
	  * Result is meant to be displayed in an indented table.
	  *
	  * @param context the eMatrix <code>Context</code> object
	  * @param args the packed arguments - expects at least the "objectId"  of the mode to expands
	  * 	Must be set in the "program map"
	  * @return MapList : a list of information regarding each retrieved product stored in a map.
	  *
	  * @throws Exception in case of navigation error.
	  */
	 public static MapList expandProductRevisions(Context context, String[] args)
	 throws Exception
	 {
		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
		  String strObjectId = (String) programMap.get("objectId");
		  if(strObjectId == null || strObjectId.length() == 0)
		  {
			  throw new IllegalArgumentException("expandProductRevision: no object id found");
		  }

		 DomainObject obj = new DomainObject(strObjectId);
		 String relTypes = ReqSchemaUtil.getProductsRelationship(context) ;
		 String objTypes = ReqSchemaUtil.getProductsType(context);
		  StringList objSelect = new StringList(SELECT_ID);
		  StringList relSelect = new StringList(SELECT_RELATIONSHIP_ID);

	      MapList values = obj.getRelatedObjects(context, relTypes, objTypes,
	              objSelect, relSelect, true, true, (short) 1, null, null);

		 return values;
	 }
	 
	 /**
	  * Validates reorder operation in Requirement Structure or Requirement Specification Structure.
	  * Throws exception with a trasnlated error message if validation fails  
	  * 
	  * @param context the eMatrix <code>Context</code> object
	  * @param args packed argument, which includes:
	  *        "mode" type of reorder operation, one of ReqConstants.REORDER_OP_CUT,
	  *               ReqConstants.REORDER_OP_COPY or ReqConstants.REORDER_OP_PASTE
	  *        "aRowIds" String array of row ids. Each row id in the format of:
	  *                  CUT:   rid|oid|pid
	  *                  COPY:  rid|oid|pid
	  *                  PASTE: rid|oid|pid|new pid|reference rid|reference oid	
	  * @throws Exception
	  * @since R2013
	  */
	 public String reorderValidation(Context context, String[] args) throws Exception
	 {
		Map programMap = (Map) JPO.unpackArgs(args);
		String mode = (String) programMap.get("mode");
		String[] aRowIds = (String[]) programMap.get("aRowIds");
		String acceptedLang = (String) programMap.get("lang");
		
		Locale lang = context.getLocale();
        if(ReqConstants.REORDER_OP_CUT.equalsIgnoreCase(mode))
		{
			for(String rowId : aRowIds)
			{
				String[] items = rowId.split("[|]");
				String oid = items[1];
				String pid = items[2];
				DomainObject parent = DomainObject.newInstance(context,pid);
	            String parName = parent.getInfo(context, SELECT_NAME);
	            String parRev = parent.getInfo(context, SELECT_REVISION);
	            String resBy = parent.getInfo(context, SELECT_RESERVED_BY);
	            Access access = parent.getAccessMask(context);
	            if ((!"".equals(resBy) && !resBy.equals(context.getUser())) 
	            		)
	            {
	            	return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.ReservedBy",
	            			new String[]{parName + " " + parName, resBy}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
	            }
	            if(!access.hasFromDisconnectAccess())
	            {
	            	return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.AccessDisconnect",
                            new String[]{parName + " " + parName}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
	            }
	            
				DomainObject obj = DomainObject.newInstance(context,oid);
	            String name = obj.getInfo(context, SELECT_NAME);
	            String rev = obj.getInfo(context, SELECT_REVISION);
	            String type = obj.getInfo(context, SELECT_TYPE);
	            resBy = obj.getInfo(context, SELECT_RESERVED_BY);;
	            access = obj.getAccessMask(context);
	            if ((!"".equals(resBy) && !resBy.equals(context.getUser())) 
	            		)
	            {
	            	return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.ReservedBy",
	            			new String[]{name + " " + rev, resBy}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
	            }
	            if(!access.hasToDisconnectAccess())
	            {
	            	return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.AccessDisconnect",
                            new String[]{name + " " + rev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
	            }
	            if(!access.hasToConnectAccess())
	            {
	            	return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.AccessConnect",
                            new String[]{name + " " + rev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
	            }
	            if(type.equalsIgnoreCase(ReqSchemaUtil.getParameterType(context))){
	            	return EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource",
	                        context.getLocale(), "emxRequirements.PlmParameter.CutDenied");
	            }
	            
			}
		}
		if(ReqConstants.REORDER_OP_COPY.equalsIgnoreCase(mode))
		{
			for(String rowId : aRowIds)
			{
				String[] items = rowId.split("[|]");
				String oid = items[1];
				String pid = items[2];
	            
				DomainObject obj = DomainObject.newInstance(context,oid);
	            String name = obj.getInfo(context, SELECT_NAME);
	            String rev = obj.getInfo(context, SELECT_REVISION);
	            String resBy = obj.getInfo(context, SELECT_RESERVED_BY);;
	            Access access = obj.getAccessMask(context);
	            if ((!"".equals(resBy) && !resBy.equals(context.getUser())) 
	            		)
	            {
	            	return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.ReservedBy",
	            			new String[]{name + " " + rev, resBy}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
	            }
	            if(!access.hasToConnectAccess())
	            {
	            	return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.AccessConnect",
                            new String[]{name + " " + rev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
	            }
	            
			}
		}
		else if(ReqConstants.REORDER_OP_PASTE.equalsIgnoreCase(mode))
		{
			
			for(String rowId : aRowIds)
			{
				String[] items = rowId.split("[|]");
				String rid = items[0];
				String oid = items[1];
				String pid = items[2];
				String newpid = items[3];
				String refrid = items[4];
				String refoid = items[5];

				DomainObject newParent = DomainObject.newInstance(context,newpid);
	            String parName = newParent.getInfo(context, SELECT_NAME);
	            String parRev = newParent.getInfo(context, SELECT_REVISION);
	            String resBy = newParent.getInfo(context, SELECT_RESERVED_BY);;
	            Access parAccess = newParent.getAccessMask(context);
	            if ((!"".equals(resBy) && !resBy.equals(context.getUser())) 
	            		)
	            {
	            	return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.ReservedBy",
	            			new String[]{parName + " " + parRev, resBy}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
	            }
				if(newParent.isKindOf(context, ReqSchemaUtil.getRequirementType(context)))
				{
					DomainObject object = DomainObject.newInstance(context,oid);
		            String name = object.getInfo(context, SELECT_NAME);
		            String rev = object.getInfo(context, SELECT_REVISION);
		            DomainRelationship r = DomainRelationship.newInstance(context, rid);
		            boolean close = r.openRelationship(context);
					String relType = r.getTypeName();
					r.closeRelationship(context, close);
					DomainObject objectToPaste = DomainObject.newInstance(context,oid);
					if(ReqSchemaUtil.getSpecStructureRelationship(context).equals(relType)&&!objectToPaste.isKindOf(context, ReqSchemaUtil.getRequirementType(context)))
					{
						return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.InvalidChild",
	                            new String[]{name + " " + rev, parName + " " + parRev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
					}
					if(refrid != null && !"".equals(refrid)){
						r = DomainRelationship.newInstance(context, refrid);
						close = r.openRelationship(context);
						String refRelType = r.getTypeName();
						r.closeRelationship(context, close);
						if(!refoid.equals(newpid) &&  !relType.equals(refRelType))
						{
							if(relType.equals(ReqSchemaUtil.getSubRequirementRelationship(context)))
							{
								return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.Reference1",
			                            new String[]{name + " " + rev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
							}
							else
							{
								return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.Reference2",
			                            new String[]{name + " " + rev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
							}
						}
					}
					
				}

				if(pid.equals(newpid)) //re-sequence
				{
		            if(!parAccess.hasModifyAccess())
		            {
		            	return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.AccessResequence",
	                            new String[]{parName + " " + parRev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
		            }
				}
				else
				{
		            
		            if(!parAccess.hasFromConnectAccess())
		            {
		            	return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.AccessConnect",
	                            new String[]{parName + " " + parRev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
		            }
		            
					if(newParent.isKindOf(context, ReqSchemaUtil.getCommentType(context)))
					{
						return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.NotContainer",
	                            new String[]{parName + " " + parRev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
					}
					if(newParent.isKindOf(context, ReqSchemaUtil.getParameterType(context)))
					{
						return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.NotContainer",
	                            new String[]{parName + " " + parRev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
					}
					if(newParent.isKindOf(context, ReqSchemaUtil.getTestCaseType(context)))
					{
						DomainObject objectToPaste = DomainObject.newInstance(context,oid);
						String name = objectToPaste.getInfo(context, SELECT_NAME);
			            String rev = objectToPaste.getInfo(context, SELECT_REVISION);
						if(!objectToPaste.isKindOf(context, ReqSchemaUtil.getParameterType(context))&&!objectToPaste.isKindOf(context, ReqSchemaUtil.getTestCaseType(context))){
							return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.InvalidChild",
		                            new String[]{name + " " + rev, parName + " " + parRev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
						}
					}
					if(newParent.isKindOf(context, ReqSchemaUtil.getChapterType(context))
						||newParent.isKindOf(context, ReqSchemaUtil.getCommentType(context))
						||newParent.isKindOf(context, ReqSchemaUtil.getSpecificationType(context)))
					{
						DomainObject objectToPaste = DomainObject.newInstance(context,oid);
						String name = objectToPaste.getInfo(context, SELECT_NAME);
			            String rev = objectToPaste.getInfo(context, SELECT_REVISION);
						if(objectToPaste.isKindOf(context, ReqSchemaUtil.getTestCaseType(context))||objectToPaste.isKindOf(context, ReqSchemaUtil.getParameterType(context))){
							return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.InvalidChild",
		                            new String[]{name + " " + rev, parName + " " + parRev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
						}
					}
						
					
			        StringList selectStmts = new StringList(DomainObject.SELECT_ID);
			        MapList mapPathObjects = newParent.getRelatedObjects(context, 
			        		ReqSchemaUtil.getExtendedSpecStructureRelationships(context),
	                        DomainConstants.QUERY_WILDCARD, selectStmts, null, true, false, (short)0, null, null, 0);
			        Map newParentMap = new HashMap();
			        newParentMap.put(DomainObject.SELECT_ID, newpid);
			        mapPathObjects.add(0, newParentMap);
			        
					for(int j = 0; j < mapPathObjects.size(); j++)
					{
						Map pathObject = (Map)mapPathObjects.get(j);
						if(pathObject.get(DomainObject.SELECT_ID).equals(oid))
						{
							DomainObject object = DomainObject.newInstance(context,oid);
				            String name = object.getInfo(context, SELECT_NAME);
				            String rev = object.getInfo(context, SELECT_REVISION);
				            return MessageUtil.getMessage(context, null, "emxRequirements.Reorder.Error.CyclicLink",
		                            new String[]{name + " " + rev, parName + " " + parRev}, null, lang, RESOURCE_BUNDLE_REQUIREMENTS_STR);
						}
					}
					
				}
			}
		}
		 return null;
	 }
	 
//LX6: Set Parametrized requirements as a GA feature
//Deletion of isParamsAvailable program

		/**
	    *  Find out the models for the selected objectid.
	    * @param context The Matrix Context object
	    * @param args The packed arguments sent by UI table component
	    * @return Vector
	    * @throws Exception if operation fails.
	    * @since R214
	    */
	   public Vector getColumnConfigurationData(Context context, String[] args)throws Exception
	    { 
	        try
	        {
	            // Create result vector
	            Vector vecResult = new Vector();
	            Vector modelResult = new Vector();
	            
	            // Get object list information from packed arguments
	            Map programMap = (Map) JPO.unpackArgs(args);
	            MapList objectList = (MapList) programMap.get("objectList");

				final String RELATIONSHIP_CONFIGURATIONCONTEXT = PropertyUtil.getSchemaProperty(context, "relationship_ConfigurationContext");
	        	StringList objSelect = new StringList();
	        	String selector = "from[" + RELATIONSHIP_CONFIGURATIONCONTEXT + "].to.name";
	        	objSelect.add(selector);
	        	String[] idsList = new String[objectList.size()];
	        	for(int iCount = 0;iCount<objectList.size();iCount++){
	        		String objectId = (String) ((Map) objectList.get(iCount)).get(DomainConstants.SELECT_ID);
	        		idsList[iCount] = objectId;
	        	}
	        	
	        	MapList modleList = DomainObject.getInfo(context, idsList, objSelect);

	            Map mapObjectInfo = null;
	            String strCount = null;
	            for (int iCount = 0; iCount < objectList.size(); iCount++) 
	            {
	                mapObjectInfo = (Map) objectList.get(iCount);
                    
	                String strObjectId = (String) mapObjectInfo.get("id");
	                String strRootNode = (String) mapObjectInfo.get("Root Node");
	                
	                String access = (String)mapObjectInfo.get(ReqConstants.SELECT_READ_ACCESS);
	                //JX5 : if read access not provided, we look for it ourselves
                    if(access == null){
                    	DomainObject tmpObj = new DomainObject(strObjectId);
                    	access = tmpObj.getInfo(context, ReqConstants.SELECT_READ_ACCESS);
                    }
                    
                    if(ReqConstants.DENIED.equals(access)){
                    	vecResult.add(DomainConstants.EMPTY_STRING);
                        continue;
                    }
                    String models = (String)((Map)modleList.get(iCount)).get(selector);
                    vecResult.add(models == null ? "" : models.replace('\7', ','));
	            }
	            
	            return vecResult;
	        } 
	        catch (Exception exp) 
	        {
	            exp.printStackTrace();
	            throw exp;
	        }
	    }
	   
	   
	   /**
	    *  This Method is used to find out the models for the selected object.
		 * @param context
		 * @param strObjectId
		 * @param strObjectId
		 * @return Maplist
		 * @throws FrameworkException
		 */
		private MapList getModels(Context context,String strObjectId) throws FrameworkException 
		{
			final String RELATIONSHIP_CONFIGURATIONCONTEXT = PropertyUtil.getSchemaProperty(context, "relationship_ConfigurationContext");
			DomainObject domObject = DomainObject.newInstance(context, strObjectId);
	        String strRelationshipPattern = null;
	        
	        StringList slBusSelect = new StringList();
	        slBusSelect.add(DomainObject.SELECT_ID);
	        slBusSelect.add(DomainObject.SELECT_NAME);
	        
	        StringList slRelSelect = new StringList();
	        slRelSelect.add(DomainRelationship.SELECT_ID);
	        
	        boolean getTo = false; 
	        boolean getFrom = true; 
	        
	        String strBusWhere = "";
	        String strRelWhere = "";
	        
	        MapList mlRelatedObjects = domObject.getRelatedObjects(context,
	        															   RELATIONSHIP_CONFIGURATIONCONTEXT, //pattern to match relationships
	        															   "*", //pattern to match types
	                                                                       slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
	                                                                       slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
	                                                                       getTo, //get To relationships
	                                                                       getFrom, //get From relationships
	                                                                       (short)0, //the number of levels to expand, 0 equals expand all.
	                                                                       strBusWhere, //where clause to apply to objects, can be empty ""
	                                                                       strRelWhere,0); //where clause to apply to relationship, can be empty ""
	        
	        return mlRelatedObjects;
	    }
		
		
		/**
	      * To exclude the Models that are connected to Product Lines
	      * @param context
	      * @param args
	      * @return
	      * @throws Exception
	      */
	     @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
		 public StringList excludeConnectedModels(Context context, String[] args) throws Exception
	     {
	    	 Map programMap = (Map) JPO.unpackArgs(args);
	    	 String strObjectIds = (String)programMap.get("objectId");
	    	 String specStructId = (String)programMap.get("specStructId");
	    	 String strRelationship=(String)programMap.get("relName");
	    	 
	    	 StringList excludeList= new StringList();  
	    	 
	    	StringList slStructureId =  FrameworkUtil.split(specStructId, "|");
	    	for(int i=0;i<slStructureId.size();i++){
	    	strObjectIds =     (String) slStructureId.get(i);
	    	if(!(strObjectIds == null || "null".equalsIgnoreCase(strObjectIds) || "".equalsIgnoreCase(strObjectIds)))
	    	{
	    		 
	    			 DomainObject domObj  = new DomainObject((String) slStructureId.get(i));
	    			 MapList mlModelObjects=domObj.getRelatedObjects(context, 
		                											PropertyUtil.getSchemaProperty(context,strRelationship),
													                "*",
													                new StringList(DomainConstants.SELECT_ID), 
													                null, 
													                false, 
													                true, 
													               (short) 0,
													                DomainConstants.EMPTY_STRING, 
													                DomainConstants.EMPTY_STRING,0);
	    			 
	    	  for(int ii=0;ii<mlModelObjects.size();ii++){
		            Map tempMap=(Map)mlModelObjects.get(ii);
		            excludeList.add((String)tempMap.get(DomainConstants.SELECT_ID));
		        }
	    	  
	    	  	excludeList.add(strObjectIds);
	    		 }
	    	 }
		        return excludeList;
	     }
		 
		 
		 /**
	      * To Includes the Models that are connected to context object
	      * @param context
	      * @param args
	      * @return
	      * @throws Exception
	      */
	     @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
		 public StringList includeNonConnectedModels(Context context, String[] args) throws Exception
	     {
	    	 Map programMap = (Map) JPO.unpackArgs(args);
	    	 String strObjectIds = (String)programMap.get("objectId");
	    	 String specStructId = (String)programMap.get("specStructId");
	    	 String strRelationship=(String)programMap.get("relName");
	    	 
	    	 StringList includeList= new StringList();  
	    	 
	    	StringList slStructureId =  FrameworkUtil.split(specStructId, "|");
	    	 for(int i=0;i<slStructureId.size();i++){
	    		 strObjectIds =     (String) slStructureId.get(i);
	    		 if(!(strObjectIds == null || "null".equalsIgnoreCase(strObjectIds) || "".equalsIgnoreCase(strObjectIds)))
	    		 {
	    		 
	    			 DomainObject domObj  = new DomainObject((String) slStructureId.get(i));
	    			 MapList mlModelObjects=domObj.getRelatedObjects(context, 
		                											PropertyUtil.getSchemaProperty(context,strRelationship),
													                "*",
													                new StringList(DomainConstants.SELECT_ID), 
													                null, 
													                false, 
													                true, 
													               (short) 0,
													                DomainConstants.EMPTY_STRING, 
													                DomainConstants.EMPTY_STRING,0);
	    	 
	    	  for(int ii=0;ii<mlModelObjects.size();ii++){
		            Map tempMap=(Map)mlModelObjects.get(ii);
		            includeList.add((String)tempMap.get(DomainConstants.SELECT_ID));
		        }
	    	  
	    	  		includeList.add(strObjectIds);
	    		 }
	    	 }
		        return includeList;
	     }
//Start:LX6:Enhancement of the import structure list			
			/**
			 * Returns a list containing all parents and children ids of an object of the structure
			 * 
			 * This method is used to check if a given requirement group is part of a structure (connect check - and exclude program for search)
			 * 
			 * @param context MatrixOne context object
			 * @param args packed arguments.  Expecting at least the "objectId"  of a requirement group in the Program Map
			 * @return a StringList containing all ids of parents and children of the requirement group.
			 * @throws Exception if the navigation fails.
			 */
			@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
			public StringList getParentsAndChildrenIds(final Context context,final String[] args)
			throws Exception
			{
				Map programMap = (HashMap)JPO.unpackArgs(args);
				String emxTableRowId = (String)programMap.get("emxTableRowId");
				//Only single selection is allowed on Import Structure feature so:
				String[] tokens = emxTableRowId.split("[|]");
				String objectId = tokens[1];
				if(objectId == null || objectId.length() == 0)
					throw new IllegalArgumentException("getParentsAndChildrenIds method failure: invalid object id passed as parameter.");
				
				return  getParentsAndChildrenIds(context, objectId);

			}
			
			private StringList getParentsAndChildrenIds(final Context context, final String objectId )
			throws Exception
			{
				DomainObject obj = new DomainObject(objectId); 
				
				//include current object id 
				StringList ids= new StringList(objectId); 
				//get his parents (up to the root)
				StringList objSelect = new StringList(SELECT_ID);
				objSelect.addElement(SELECT_TYPE);
				objSelect.addElement(SELECT_NAME);
				objSelect.addElement(SELECT_REVISION);
				objSelect.addElement(SELECT_DESCRIPTION);
				StringList relSelect = new StringList(SELECT_RELATIONSHIP_ID);
				

				String relTypes =   ReqSchemaUtil.getSpecStructureRelationship(context) + 
									"," +
									ReqSchemaUtil.getSubRequirementRelationship(context) +
									"," +
									ReqSchemaUtil.getDerivedRequirementRelationship(context);
				
				String objTypes =   ReqSchemaUtil.getSpecificationType(context) + 
									"," +
									ReqSchemaUtil.getRequirementType(context) + 
									"," + 
									ReqSchemaUtil.getCommentType(context) + 
									"," + 
									ReqSchemaUtil.getChapterType(context);
					
				MapList parentsInfo = obj.getRelatedObjects(context, relTypes, objTypes, objSelect, relSelect, true , false,(short) 0, "", "",0);
				//add parents ids to the list
				for (int i = 0; i < parentsInfo.size() ; i++)
				{
					Map infos = (Map)parentsInfo.get(i);
					String id = (String) infos.get(DomainObject.SELECT_ID);
					if(!ids.contains(id))
					{
						ids.add(id); 
					}
				}
				
				//get his children (down to the leaves)
				MapList childrenInfo =  obj.getRelatedObjects(context, relTypes, objTypes, objSelect, relSelect, false , true,(short) 0, "", "",0);
				//add children IDs to the list
				for (int i = 0; i < childrenInfo.size() ; i++)
				{
					Map infos = (Map)childrenInfo.get(i);
					String id = (String) infos.get(DomainObject.SELECT_ID);
					if(!ids.contains(id))
					{
						ids.add(id); 
					}
				}		
				return ids; 
			} 
//Start:LX6:Enhancement of the import structure list		
			
	public List getLockIcons(Context context, String[] args) throws Exception
    {
	    //String ICON_TOOLTIP_HIGHER_REVISION_EXISTS = "emxRequirements.Form.Label.HigherRev";
	    String RESOURCE_BUNDLE_PRODUCTS_STR = "emxRequirementsStringResource";
	    String OBJECT_LIST = "objectList";
	    Map programMap = (HashMap) JPO.unpackArgs(args);
	    MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
	    String User = context.getUser();
	    //HashMap requestMap = (HashMap) programMap.get("requestMap");
        //String strMode =  (String) requestMap.get("mode");
	    int iNumOfObjects = relBusObjPageList.size();
	    // The List to be returned
	    String arrObjId[] = new String[iNumOfObjects];
	    List LockIcons= new Vector(iNumOfObjects);
	    int iCount;
	    
	    HashMap paramList = (HashMap) programMap.get("paramList");
        String strExport = (String) paramList.get("exportFormat");
        boolean toExport = false;
        if (strExport != null) 
                toExport = true;
        
	    //Getting the bus ids for objects in the table
	    for (iCount = 0; iCount < iNumOfObjects; iCount++) {
	        Object obj = relBusObjPageList.get(iCount);
	        arrObjId[iCount] = (String)((Map)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
	    }
	    StringList selectStmts = new StringList("reserved");
	    selectStmts.addElement("reservedby");
	    MapList columnData = DomainObject.getInfo(context, arrObjId, selectStmts);
        String denied = EnoviaResourceBundle.getProperty(context, ReqConstants.BUNDLE_FRAMEWORK , context.getLocale(), "emxFramework.Basic.DENIED");
	    //Iterating through the list of objects to generate the program HTML output for each object in the table
	    for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            String readAccess = (String)((Map)relBusObjPageList.get(iCount)).get(ReqConstants.SELECT_READ_ACCESS);
            if(ReqConstants.DENIED.equals(readAccess)){
                LockIcons.add(denied);
                continue;
            }
	    	Map ReservedInfo = (Map)columnData.get(iCount);
		    String isReserved = (String)ReservedInfo.get("reserved");
	        String strIcon="";
	        String strLockTooltip="";
	        String strLockIconTag = "";
	        if(isReserved.equalsIgnoreCase("false")||"".equals(isReserved))
	        {
	        	strIcon="";
	        	strLockTooltip="";
	        	strLockIconTag = "";
	        }
	        else
	        {
	        	String reservedby = (String)ReservedInfo.get("reservedby");
	        	if(User.equalsIgnoreCase(reservedby))
		        {
		        	strIcon= EnoviaResourceBundle.getProperty(context,"emxRequirements.Icon.padLockReservedByMySelf");
		        }
	        	else
	        	{
	        		strIcon= EnoviaResourceBundle.getProperty(context,"emxRequirements.Icon.padLockReservedByOther");
	        	}
		    	String strLockedBy = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxFramework.Basic.ReservedBy");
		    	strLockTooltip = strLockedBy + " " + reservedby;
		    	strLockIconTag = 
		        	"<img src=\"" + strIcon + "\""
		            + " border=\"0\"  align=\"middle\" "
		            + "title=\""
		            + " "
		            + strLockTooltip
		            + "\""
		            + "/>" + "<span style=\"display:none\">" + strLockTooltip + "</span>";
		    	
		    	if (toExport)
		    	    strLockIconTag = strLockTooltip;
		        
	        }
	        LockIcons.add(strLockIconTag);
	        
	    }	
	    return LockIcons;
    }

    /** Trigger Method to check if a higher revision already exists. If it does, block the demote
     *  
     * @param context - the eMatrix <code>Context</code> object
     * @param args - holds the Hashmap containing the object id.
     * @return - nothing
     * @throws Exception if the operation fails
     * @since RequirementsManagement R2014x
     */
    public int DemoteFromReleaseHigherRevisionCheck(Context context, String[] args)
        throws Exception
    {
        String strObjectId = args[0];

        {
        	DomainObject domReq = DomainObject.newInstance(context, strObjectId);
        	
        	if(!RequirementsCommon.isLastRevision(context, domReq))
        	{
        		
                //push error about released parents
                String errorString = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_REQUIREMENTS_STR, context.getLocale(), "emxRequirements.Alert.HigherRevisionExists");
        		throw new FrameworkException(errorString);
            }
        }
        return 0;
    }
    String[] getStatusLinkColour(Context context, StringList StatusLinks) throws Exception{
    	//for customization
    	//StringList attributeRange = RequirementsCommon.getAttributeRange(context, "Link Status");
    	String[] result = new String[2];
    	String list = StatusLinks.toString();
    	if (list.contains("Valid")) {
			result[0] = "green";
			result[1] = "Valid";
		}
		if (list.contains("Suspect")) {
			result[0] = "orange";
			result[1] = "Suspect";
		}
		if (list.contains("Invalid")) {
			result[0] = "red";
			result[1] = "Invalid";
		}
    	return result;
    }
    
    //START :IR-341768-3DEXPERIENCER2016  No label is displayed on pop up dialogue box when user click on icons of the columns "Covered Requirements" and "Refining Requirements"
    @com.matrixone.apps.framework.ui.ProgramCallable
    public List getCoveredRequirement(Context context, String[] args) throws Exception
    {
    	String CoveredIcon = EnoviaResourceBundle.getProperty(context, "emxRequirements.TableIcon.CoveredRequirementIcon");
    	String coveredBy = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Label.CoveredBy");
    	String divTitle = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Covered.Requirements");
    	String expWordProperty = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.CoveredRefines.exportToWord.refines");
    	String OBJECT_LIST = "objectList";
    	String strHTMLTag  = "";
    	Map programMap = (HashMap) JPO.unpackArgs(args);
    	Map paramList = (Map) programMap.get("paramList");
    	String rootObjId = (String) paramList.get("objectId");
    	String reportFormat = (String) paramList.get("reportFormat");
    	String strExport = (String) paramList.get("exportFormat");
    	boolean reportHTML = (reportFormat == null? true: "HTML".equals(reportFormat));
    	MapList objectList = (MapList) programMap.get(OBJECT_LIST);
    	List columnTags = new Vector(objectList.size());
    	String refinedAttribute =  "to["+ReqSchemaUtil.getDerivedRequirementRelationship(context)+"].id";
    	String statusSelector = "to["+ReqSchemaUtil.getDerivedRequirementRelationship(context)+"].attribute[Link Status]";
    	StringList objSelect = new StringList(refinedAttribute);
    	objSelect.add(SELECT_ID);
    	objSelect.add(SELECT_TYPE);
    	objSelect.add(SELECT_NAME);
    	objSelect.add(statusSelector);
    	String[] idsList = new String[objectList.size()];
    	for(int iCount = 0;iCount<objectList.size();iCount++){
    		String objectId = (String) ((Map) objectList.get(iCount)).get(DomainConstants.SELECT_ID);
    		idsList[iCount] = objectId;
    	}
    	
    	MapList returnedMap = DomainObject.getInfo(context, idsList, objSelect);
    	
    	ReqStructureUtil.fillTypeInfo(context, returnedMap);
    	
    	//Map test = (Map)returnedMap.get(0);
    	//remove brackets and use BEL character to get all Ids
		String notAllocatedIcon = EnoviaResourceBundle.getProperty(context, "emxRequirements.TableIcon.NotAllocatedIcon");
		String noObjects = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.ToolTip.NoObjects");
    	
    	for(int i = 0;i<returnedMap.size();i++){
    		String nbFromDerivedRequirement = "0";
    		Map map = (Map)returnedMap.get(i);
    		String id = (String)map.get(SELECT_ID);
    		if(ReqSchemaUtil.getRequirementType(context).equals(map.get("kindof"))){
    				String objName = (String)map.get(SELECT_NAME);
    				String title = divTitle.replace("$<CovReq>", objName);
    				String strRefinedAttributeValues = (String)map.get(refinedAttribute);
    				if(strRefinedAttributeValues!=null&&!strRefinedAttributeValues.isEmpty()){
    					//retrieve the idList to get linkStatus attribute
            			String[] Ids = strRefinedAttributeValues.replace("[","").replace("]","").split("\\a");   
            			//get the number od derived requirements
            			nbFromDerivedRequirement = Integer.toString(Ids.length);
            			//retrieve all "link Status" attribute values 
                		StringList statusList = new StringList(((String)map.get(statusSelector)).split("\\a"));
                		String[] result = getStatusLinkColour(context, statusList);
        				String colour = result[0];
        				String status = result[1];
        				String property = "emxFramework.Range.Link_Status." + status;
        				String allocationStatus = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), property);
        				String tooltipe = MessageUtil.getMessage(context, null, "emxRequirements.ToolTip.LinkStatus",
        		                new String[] {allocationStatus}, null, context.getLocale(), RESOURCE_BUNDLE_REQUIREMENTS_STR);
//START :LX6 IR-375012-3DEXPERIENCER2016x Code is displayed in the Word document generated from "Export to Word". 
        				if("word".equalsIgnoreCase(strExport)){
        					strHTMLTag = expWordProperty.replace("$<NUM>", nbFromDerivedRequirement);
                		}else if(reportHTML){
        					strHTMLTag = "<a onclick=\"displayDerivedInforamtions('" + rootObjId + "','"+ id + "','up','"+ title +"');return false;\" title=\""+tooltipe +"\">"+  
        		    				"<img onload='getFloatingDiv(\""+ id +"\",\"up\")' src=\"" + CoveredIcon + "\"" + "  border=\"0\"  align=\"middle\" " + "title=\""
        		                    + coveredBy + "\"" + "/>"  + "<span style=\"font-size:large;background-color:"+colour+";color:#000000;\">" + nbFromDerivedRequirement + "</span></a>";
                    	}else{
                    		strHTMLTag = nbFromDerivedRequirement;
                    	}
    				}else{ 
    					if("word".equalsIgnoreCase(strExport)){
        					strHTMLTag = expWordProperty.replace("$<NUM>", "0");
    					}else if(reportHTML){
	        				strHTMLTag = "<img src=\"" + notAllocatedIcon + "\"" + "  border=\"0\"  align=\"middle\" title=\""+noObjects+"\"/>";
    					}
    				}
//END :LX6 IR-375012-3DEXPERIENCER2016x Code is displayed in the Word document generated from "Export to Word".    				
    				columnTags.add(strHTMLTag);
    		}else{
    			columnTags.add("");
    		}

    	}
    	return columnTags;
    }
    
    @com.matrixone.apps.framework.ui.ProgramCallable
	public List getRefinedRequirements(Context context, String[] args) throws Exception
	{ 
    	String refinedIcon = EnoviaResourceBundle.getProperty(context, "emxRequirements.TableIcon.RefinedRequirementIcon");
	 	String refines = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Label.Refined");
	 	String divTitle = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Refining.Requirements");
	 	String expWordProperty = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.CoveredRefines.exportToWord.covered");
    	String OBJECT_LIST = "objectList";
    	String strHTMLTag  = "";
    	Map programMap = (HashMap) JPO.unpackArgs(args);
    	Map paramList = (Map) programMap.get("paramList");
    	String rootObjId = (String) paramList.get("objectId");
    	String reportFormat = (String) paramList.get("reportFormat");
    	String strExport = (String) paramList.get("exportFormat");
    	boolean reportHTML = (reportFormat == null? true: "HTML".equals(reportFormat));
    	MapList objectList = (MapList) programMap.get(OBJECT_LIST);
    	List columnTags = new Vector(objectList.size());
    	String CoveredAttribute =  "from["+ReqSchemaUtil.getDerivedRequirementRelationship(context)+"].id";
    	String statusSelector = "from["+ReqSchemaUtil.getDerivedRequirementRelationship(context)+"].attribute[Link Status]";
    	StringList objSelect = new StringList(CoveredAttribute);
    	objSelect.add(SELECT_NAME);
    	objSelect.add(SELECT_TYPE);
    	objSelect.add(SELECT_ID);
    	objSelect.add(statusSelector);
    	String[] idsList = new String[objectList.size()];
    	for(int iCount = 0;iCount<objectList.size();iCount++){
    		String objectId = (String) ((Map) objectList.get(iCount)).get(DomainConstants.SELECT_ID);
    		idsList[iCount] = objectId;
    	}
    	
    	MapList returnedMap = DomainObject.getInfo(context, idsList, objSelect);
    	ReqStructureUtil.fillTypeInfo(context, returnedMap);
    	//Map test = (Map)returnedMap.get(0);
    	//remove brackets and use BEL character to get all Ids
		String notAllocatedIcon = EnoviaResourceBundle.getProperty(context, "emxRequirements.TableIcon.NotAllocatedIcon");
		String noObjects = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.ToolTip.NoObjects");
    	
    	for(int i = 0;i<returnedMap.size();i++){
    		String nbFromDerivedRequirement = "0";
    		Map map = (Map)returnedMap.get(i);
    		String id = (String)map.get(SELECT_ID);
    		if(ReqSchemaUtil.getRequirementType(context).equals(map.get("kindof"))){
    				String objName = (String)map.get(SELECT_NAME);;
    				String title = divTitle.replace("$<RefReq>", objName);
    				String strCoveredAttributeValues = (String)map.get(CoveredAttribute);
    				if(strCoveredAttributeValues!=null&&!strCoveredAttributeValues.isEmpty()){
    					//retrieve the idList to get linkStatus attribute
            			String[] Ids = strCoveredAttributeValues.replace("[","").replace("]","").split("\\a");   
            			//get the number od derived requirements
            			nbFromDerivedRequirement = Integer.toString(Ids.length);
            			//retrieve all "link Status" attribute values 
                		StringList statusList = new StringList(((String)map.get(statusSelector)).split("\\a"));
                		String[] result = getStatusLinkColour(context, statusList);
        				String colour = result[0];
        				String status = result[1];
        				String property = "emxFramework.Range.Link_Status." + status;
        				String allocationStatus = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), property);
        				String tooltipe = MessageUtil.getMessage(context, null, "emxRequirements.ToolTip.LinkStatus",
        		                new String[] {allocationStatus}, null, context.getLocale(), RESOURCE_BUNDLE_REQUIREMENTS_STR);
//START :LX6 IR-375012-3DEXPERIENCER2015x Code is displayed in the Word document generated from "Export to Word".        				
        				if("word".equalsIgnoreCase(strExport)){
        					strHTMLTag = expWordProperty.replace("$<NUM>", nbFromDerivedRequirement);
                		}else if(reportHTML){
            				strHTMLTag ="<a onclick=\"displayDerivedInforamtions('" + rootObjId + "','"+ id + "','down','"+ title +"');return false;\" title=\""+tooltipe +"\">"+
            					"<span style=\"font-size:large;background-color:"+colour +";color:#000000;\">" + nbFromDerivedRequirement + "</span>" + "<img onload='getFloatingDiv(\""+ id +"\",\"down\")' src=\"" + refinedIcon + "\"" + "  border=\"0\"  align=\"middle\" " + "title=\""
                             + refines + "\"" + "/></a>" ;
                    	}else{
                    		strHTMLTag = nbFromDerivedRequirement;
                    	}
    				}else{
    					if("word".equalsIgnoreCase(strExport)){
        					strHTMLTag = expWordProperty.replace("$<NUM>", "0");
    					}else if(reportHTML){
        				strHTMLTag = "<img src=\"" + notAllocatedIcon + "\"" + "  border=\"0\"  align=\"middle\" title=\""+noObjects+"\"/>";
    					}
    				}
//END :LX6 IR-375012-3DEXPERIENCER2015x Code is displayed in the Word document generated from "Export to Word".
    				columnTags.add(strHTMLTag);
    		}else{
    			columnTags.add("");
    		}

    	}
    	return columnTags;
	}
  //END : IR-341768-3DEXPERIENCER2016  No label is displayed on pop up dialogue box when user click on icons of the columns "Covered Requirements" and "Refining Requirements"
   
   // ++ HAT1 ZUD: ReqSpec dependency - enhancements. Fix for IR-381800-3DEXPERIENCER2016x and IR-381584-3DEXPERIENCER2016x
    public String getRequirementIconName(Context context, String ObjectId)
    {
    	String iconName = "";
		try {
			//getting icon for child object
			HashMap arg = new HashMap();
			MapList objLst = new MapList();
			MapList mapIconList = new MapList();
			HashMap tempMap = new HashMap();
			Map mapIcon =  new HashMap();
			
			tempMap.put(SELECT_ID,ObjectId);
			//tempMap.put(SELECT_RELATIONSHIP_ID,childObjectRelId);
			objLst.add(tempMap);
			arg.put("objectList",objLst);
			
			mapIconList = emxRMTCommon_mxJPO.getRequirementIcons(context, JPO.packArgs(arg));
			mapIcon = (Map)mapIconList.get(0);
			iconName = (String)mapIcon.get(ObjectId);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iconName;
    }
    
    
    public String getErrorWarningBranchesChkConsistency(Context context, String[] args, String rootReqSpecId)
    {
    	int curElementLevel = 0, curElementIndex = -1, lastElementLevel = 0, lastElementIndex = 0;
    	List<String> xmlDataStrArrayList     = new ArrayList<String>();
    	List<Integer> xmlDataLevelArrayList  = new ArrayList<Integer>();
    	String xmlDataStr = "", xmlDataLevel = "";
		
    	int errorCount   = 0;
    	int warningCount = 0;
    	String xmlData = "";
    	String xmlDataString = "";
    	String xmlDataStringToReturn = "";
		String warningOrError = "N/A";
		String treeDisplaySettings = "N/A";

		try {
			
			Map programMap 		= (HashMap) JPO.unpackArgs(args);
			String strExpandLevel	= (String)programMap.get("expandLevel");
			String strDerivMode = (String)programMap.get("mode");
			String strTreeMode	= (String)programMap.get("tree");
			
			//Creating objectSelects for child nodes.
			StringList objectSelects = new StringList();
			objectSelects.add(SELECT_ID);
			objectSelects.add(SELECT_NAME);
			objectSelects.add(SELECT_TYPE);
			objectSelects.add(SELECT_REVISION);
			objectSelects.add(SELECT_KINDOF);
			objectSelects.add(SELECT_ATTRIBUTE_CONTENT_TEXT);
			objectSelects.add(SELECT_ATTRIBUTE_TITLE);
			objectSelects.add(SELECT_LEVEL);
			
			StringList relSelects	= new StringList();
			relSelects.add(SELECT_RELATIONSHIP_ID);
			relSelects.add(SELECT_RELATIONSHIP_TYPE);
			relSelects.add(SELECT_ATTRIBUTE_TREE_ORDER);
			
			//Expand if needed
    		MapList objectList 	= new MapList();
    		String relTypes = "";
			
			String objectTypes = ReqSchemaUtil.getRequirementType(context) 
					+ "," +  ReqSchemaUtil.getChapterType(context);
				
			boolean isCovered = strTreeMode.equalsIgnoreCase("covered");
			boolean isRefined = strTreeMode.equalsIgnoreCase("refined");
			
			if(!strExpandLevel.equals("0")){
    			
    			int maxLevels = 0;
    			if(strExpandLevel.equalsIgnoreCase("All")){
    				maxLevels = 0;
    			}else{
    				maxLevels = Integer.parseInt(strExpandLevel);
    			}

    				relTypes = ReqSchemaUtil.getSpecStructureRelationship(context)
    						+ "," + ReqSchemaUtil.getSubRequirementRelationship(context);
    				
    				objectList = getAllRelatedReqSpecObjChapReq(context, rootReqSpecId);
    		}
    		
    		objectList.sortStructure(context, "relationship,attribute[TreeOrder]", ",", "string,real", ",");
    		
    		//Iterator objectListIdNameItr = objectList.iterator();
			Map<String, String> reqSpecChildIdName = new HashMap<String, String>();
			reqSpecChildIdName = MapListToMapIdName(context, objectList, ReqSchemaUtil.getRequirementType(context));
    		
    		Iterator objectListItr = objectList.iterator();
			while(objectListItr.hasNext())
			{
   				Map curChildObjMap     = (Map) objectListItr.next();
				String curChildObjId   = (String) curChildObjMap.get(SELECT_ID);
				
				String childObjectId 			= curChildObjId;
	    		String childObjectName 			= (String)curChildObjMap.get(SELECT_NAME);
	    		String childObjectRevision 		= (String)curChildObjMap.get(SELECT_REVISION);
	    		String childObjectType			= (String)curChildObjMap.get(SELECT_TYPE);
	    		String childObjectKindOf		= (String)curChildObjMap.get(SELECT_KINDOF);
	    		String childObjectTitle			= (String)curChildObjMap.get(SELECT_ATTRIBUTE_TITLE);
	    		String childObjectContentText	= (String)curChildObjMap.get(SELECT_ATTRIBUTE_CONTENT_TEXT);
	    		String childObjectLevel			= (String)curChildObjMap.get(SELECT_LEVEL);
	    		String childObjectRelId			= (String)curChildObjMap.get(SELECT_RELATIONSHIP_ID);
	    		String childObjectRelType		= "";
	    		String childObjectSeqOrder       = "";
				
	    		curElementLevel = Integer.parseInt(childObjectLevel);
	    		
	    		//When ReqSpec's Direct child will come, then adding in xmlDataStringToReturn complete branch of last direct child's list containing error or warning.  
	    		if(curElementLevel == 1)
	    		{
	    			while(xmlDataLevelArrayList.size() >= 1 && xmlDataLevelArrayList.get(xmlDataLevelArrayList.size() -1) > 0)
	    			{
	    				xmlDataLevelArrayList.remove(xmlDataLevelArrayList.size() -1);
	    				xmlDataStrArrayList.remove(xmlDataStrArrayList.size() -1);
	    			}
	    			
	    			if(xmlDataLevelArrayList.size() > 0 && xmlDataLevelArrayList.get(xmlDataLevelArrayList.size() -1) < 0)
	    			{
	    				for(String xmlDataStr1 : xmlDataStrArrayList)
	    				{
	    					xmlDataStringToReturn += xmlDataStr1;
	    				}
	    				xmlDataLevelArrayList.removeAll(xmlDataLevelArrayList);
	    				xmlDataStrArrayList.removeAll(xmlDataStrArrayList);
	    			}
	    		}
	    		//In ReqSpec's direct child's tree when we get the curElementLevel <= LastElementLevel and Arraylist doesn't contain error or warning element as last Element.
	    		else if((xmlDataLevelArrayList.get(xmlDataLevelArrayList.size() -1) >= curElementLevel) && (xmlDataLevelArrayList.get(xmlDataLevelArrayList.size() -1) > 0))
	    		{
	    			while((xmlDataLevelArrayList.get(xmlDataLevelArrayList.size() -1) >= curElementLevel) && (xmlDataLevelArrayList.get(xmlDataLevelArrayList.size() -1) > 0))
	    			{
	    				xmlDataLevelArrayList.remove(xmlDataLevelArrayList.size() -1);
	    				xmlDataStrArrayList.remove(xmlDataStrArrayList.size() -1);
	    			}
	    		}
	    		//getting icon for child object
	    		String childIconName = getRequirementIconName(context, childObjectId);

	    		treeDisplaySettings = emxRMTCommon_mxJPO.getTreeDisplaySettings(context, JPO.packArgs(childObjectType));
	    		
	    		//Parent xml data
	    		xmlDataString = "<r o=\""+childObjectId+"\" r=\"\" p=\"\" type=\""+childObjectType+"\" rel=\"\" level=\""+childObjectLevel+"\" revision=\""+childObjectRevision+"\" seqOrder=\"\" name=\""+childObjectName+"\" icon=\"images/"+childIconName+"\""+" isDerived=\"false\" colorCode=\"N/A\" warningOrError=\"N/A\" kindOf=\""+childObjectKindOf+ "\" title=\""+childObjectTitle+"\" contentText=\""+childObjectContentText+"\" treeDisplaySettings=\""+treeDisplaySettings+"\"></r>";
	    		//xmlData += xmlDataString;
	    		
	    		xmlDataStrArrayList.add(xmlDataString);
	    		xmlDataLevelArrayList.add(Integer.parseInt(childObjectLevel));

				//Finding the inconsistent Derived Requirements 
	    		MapList childObjectList = new MapList();
	    		String relType = ReqSchemaUtil.getDerivedRequirementRelationship(context);

	    		String reqObjectType = ReqSchemaUtil.getRequirementType(context);
				if(childObjectType.equalsIgnoreCase(ReqSchemaUtil.getRequirementType(context)))
				{
		    		DomainObject domDerivedReqObj = DomainObject.newInstance(context, curChildObjId);

		    		childObjectList = domDerivedReqObj.getRelatedObjects(context, relType, //relationship pattern
		    				"*", // object pattern
							objectSelects, // object selects
							relSelects, // relationship selects
							isCovered, // to direction isrefined
							isRefined, //from direction
							(short) 1, // recursion level
							null, // object where clause
							null, // relationship where clause
							(short) 0, // limit
							CHECK_HIDDEN, // check hidden
							PREVENT_DUPLICATES, // prevent duplicates
							PAGE_SIZE, //pagesize
							null, // include type
							null, // include relationship
							null, // include Mpa
							null, // relKeyPrefix
							null); // effectivity filter expression
		    		
		    		
		    		childObjectList.sortStructure(context, "relationship,attribute[TreeOrder]", ",", "string,real", ",");
		    		
		    		 Iterator derivReqobjListItr = childObjectList.iterator();
					while(derivReqobjListItr.hasNext())
					{
		   				Map derivReqObjMap     = (Map) derivReqobjListItr.next();
						String derivReqObjId   = (String) derivReqObjMap.get(SELECT_ID);
						
						//String derivReqObjId 			= derivReqObjId;
			    		String derivReqObjName 			= (String)derivReqObjMap.get(SELECT_NAME);
			    		String derivReqObjRevision 		= (String)derivReqObjMap.get(SELECT_REVISION);
			    		String derivReqObjType			= (String)derivReqObjMap.get(SELECT_TYPE);
			    		String derivReqObjKindOf		= (String)derivReqObjMap.get(SELECT_KINDOF);
			    		String derivReqObjTitle			= (String)derivReqObjMap.get(SELECT_ATTRIBUTE_TITLE);
			    		String derivReqObjContentText	= (String)derivReqObjMap.get(SELECT_ATTRIBUTE_CONTENT_TEXT);
			    		String derivReqObjLevel			= (String)derivReqObjMap.get(SELECT_LEVEL);
			    		String derivReqObjRelId			= (String)derivReqObjMap.get(SELECT_RELATIONSHIP_ID);
			    		String derivReqObjRelType		= "";
			    		String derivReqObjSeqOrder       = "";
						
			    		//increasing level of Derived Req by 1 to it's parent req.
			    		int intDerivReqObjLevel = Integer.parseInt(childObjectLevel) + 1;
			    		derivReqObjLevel = Integer.toString(intDerivReqObjLevel);
			    		
			    		//Getting icon of Derived Req.
	            		String derivReqIconName = "";  
	            		if(isCovered)
	            		{
	            			derivReqIconName = "iconReqTypeCoveredRequirement.png";
	            		}
	            		else
	            		{
	            			derivReqIconName = "iconReqTypeDerivedRequirement.png";
	            		}
	            		
	            		//Will display only when Derived Req is either Error or Warning.
	            		//Finding if Derived Req is an Error. 
    					if(reqSpecChildIdName.containsKey(derivReqObjId)) 						
    					{
    						warningOrError = "error";
    						errorCount ++;
    						
    			    		treeDisplaySettings = emxRMTCommon_mxJPO.getTreeDisplaySettings(context, JPO.packArgs(derivReqObjType));

    			    		//Derived Requirement xml data
    			    		xmlDataString = "<r o=\""+derivReqObjId+"\" r=\"" + derivReqObjRelId + "\" p=\"\" type=\""+derivReqObjType+"\" rel=\"\" level=\""+derivReqObjLevel +"\" revision=\""+derivReqObjRevision+"\" seqOrder=\"\" name=\""+derivReqObjName+"\" icon=\"images/"+derivReqIconName+"\""+" isDerived=\"true\" colorCode=\"N/A\" warningOrError=\"" + warningOrError + "\" kindOf=\""+derivReqObjKindOf+ "\" title=\""+derivReqObjTitle + "\" errorCount=\"" + errorCount + "\" warningCount=\"" + warningCount + "\" contentText=\""+derivReqObjContentText+"\" treeDisplaySettings=\""+treeDisplaySettings+"\"></r>";
    			    		//xmlData += xmlDataString;

    			    		xmlDataStrArrayList.add(xmlDataString);
    			    		xmlDataLevelArrayList.add( - intDerivReqObjLevel);

    					}
    					else
    					{
    						warningOrError = isDerivedReqWarning(context, rootReqSpecId , derivReqObjId, isCovered, isRefined);
    						
    						if(warningOrError.equalsIgnoreCase("warning"))
    						{
	    						warningCount ++;
	    						
	    			    		treeDisplaySettings = emxRMTCommon_mxJPO.getTreeDisplaySettings(context, JPO.packArgs(derivReqObjType));
	    			    		
	    			    		//Derived Requirement xml data
	    			    		xmlDataString = "<r o=\""+derivReqObjId+"\" r=\"" + derivReqObjRelId + "\" p=\"\" type=\""+derivReqObjType+"\" rel=\"\" level=\""+derivReqObjLevel +"\" revision=\""+derivReqObjRevision+"\" seqOrder=\"\" name=\""+derivReqObjName+"\" icon=\"images/"+derivReqIconName+"\""+" isDerived=\"true\" colorCode=\"N/A\" warningOrError=\"" + warningOrError + "\" kindOf=\""+derivReqObjKindOf+ "\" title=\""+derivReqObjTitle + "\" errorCount=\"" + errorCount + "\" warningCount=\"" + warningCount + "\" contentText=\""+derivReqObjContentText+"\" treeDisplaySettings=\""+treeDisplaySettings+"\"></r>";
	    			    		//xmlData += xmlDataString;

	    			    		xmlDataStrArrayList.add(xmlDataString);
	    			    		xmlDataLevelArrayList.add( - intDerivReqObjLevel);
    						}
    					}			    		
			    		warningOrError = "";
				}
				
			   }

			 }
			
			//For Reqspec last direct child tree processing.
			while(xmlDataLevelArrayList.size() >= 1 && xmlDataLevelArrayList.get(xmlDataLevelArrayList.size() -1) > 0)
			{
				xmlDataLevelArrayList.remove(xmlDataLevelArrayList.size() -1);
				xmlDataStrArrayList.remove(xmlDataStrArrayList.size() -1);
			}
			
			if(xmlDataLevelArrayList.size() > 0 && xmlDataLevelArrayList.get(xmlDataLevelArrayList.size() -1) < 0)
			{
				for(String xmlDataStr1 : xmlDataStrArrayList)
				{
					xmlDataStringToReturn += xmlDataStr1;
				}
				xmlDataLevelArrayList.removeAll(xmlDataLevelArrayList);
				xmlDataStrArrayList.removeAll(xmlDataStrArrayList);
			}
			
			} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return xmlDataStringToReturn;
    }
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public String getXMLDataForCheckConsistency(Context context, String[] args) throws Exception
    {
    	String xmlData = "";

    	try{
	    		Map programMap 		= (HashMap) JPO.unpackArgs(args);
	    		String strObjectId	 	= (String)programMap.get("objectId");
	    		String strCheckConsistencyMode = (String)programMap.get("strCheckConsistencyMode");
	    		String rootReqSpecId = (String)programMap.get("rootReqSpecId");
	    		
	       	 	String treeDisplaySettings = "N/A";

	    		//get Information about rootObject
	    		DomainObject rootObject = DomainObject.newInstance(context, strObjectId);
	    		
	    		StringList rootObjectSelects = new StringList();
	    		rootObjectSelects.add(SELECT_TYPE);
	    		rootObjectSelects.add(SELECT_NAME);
	    		rootObjectSelects.add(SELECT_REVISION);
	    		rootObjectSelects.add(SELECT_KINDOF);
	    		rootObjectSelects.add(SELECT_ATTRIBUTE_TITLE);
	    		rootObjectSelects.add(SELECT_ATTRIBUTE_CONTENT_TEXT);
	    		
	    		Map rootObjectInfos = rootObject.getInfo(context, rootObjectSelects);
	    		
	    		String objectId 			= strObjectId;
	    		String objectName 			= (String)rootObjectInfos.get(SELECT_NAME);
	    		String objectRevision 		= (String)rootObjectInfos.get(SELECT_REVISION);
	    		String objectType			= (String)rootObjectInfos.get(SELECT_TYPE);
	    		String objectKindOf			= (String)rootObjectInfos.get(SELECT_KINDOF);
	    		String objectTitle			= (String)rootObjectInfos.get(SELECT_ATTRIBUTE_TITLE);
	    		String objectContentText	= (String)rootObjectInfos.get(SELECT_ATTRIBUTE_CONTENT_TEXT);
	    		String objectLevel			= "0";
	    		String objectRelId			= "";
	    		String objectRelType		= "";
	    		String objectSeqOrder       = "";
	    		
	    		//retrieve root object icon
	    		String iconName = getRequirementIconName(context, strObjectId);
	    		
	    		//Tree display settings
	    		treeDisplaySettings = emxRMTCommon_mxJPO.getTreeDisplaySettings(context, JPO.packArgs(objectType));
	    		
	    		//construct xml for root object
	    		xmlData = "<r o=\""+objectId+"\" r=\"\" p=\"\" type=\""+objectType+"\" rel=\"\" level=\""+objectLevel+"\" revision=\""+objectRevision+"\" seqOrder=\"\" name=\""+objectName+"\" icon=\"images/"+iconName+"\""+" isDerived=\"false\" colorCode=\"N/A\" warningOrError=\"N/A\" kindOf=\""+objectKindOf+ "\" title=\""+objectTitle+"\" contentText=\""+objectContentText+"\" treeDisplaySettings=\""+treeDisplaySettings+"\"></r>";
	    		
	    		xmlData += getErrorWarningBranchesChkConsistency(context, args, strObjectId);
    		
	    }
    	catch(Exception ex){
    		//NOP
    	}
    	
    	return xmlData;
    }

    //Finding all the Related objects of root Req Sepc.
    public MapList getAllRelatedReqSpecObjChapReq(Context context, String strObjectId)
    {
    	MapList objectList = new MapList();

    	try {
				DomainObject rootObject = DomainObject.newInstance(context, strObjectId);
				String relTypes = ReqSchemaUtil.getSpecStructureRelationship(context)
						+ "," + ReqSchemaUtil.getSubRequirementRelationship(context);
				
				String objectTypes = ReqSchemaUtil.getRequirementType(context) 
						+ "," +  ReqSchemaUtil.getChapterType(context);
				
				//Creating objectSelects for child nodes.
				StringList objectSelects = new StringList();
				objectSelects.add(SELECT_ID);
				objectSelects.add(SELECT_NAME);
				objectSelects.add(SELECT_TYPE);
				objectSelects.add(SELECT_REVISION);
				objectSelects.add(SELECT_KINDOF);
				objectSelects.add(SELECT_ATTRIBUTE_CONTENT_TEXT);
				objectSelects.add(SELECT_ATTRIBUTE_TITLE);
				objectSelects.add(SELECT_LEVEL);
				
				StringList relSelects	= new StringList();
				relSelects.add(SELECT_RELATIONSHIP_ID);
				relSelects.add(SELECT_RELATIONSHIP_TYPE);
				relSelects.add(SELECT_ATTRIBUTE_TREE_ORDER);
	
				objectList = rootObject.getRelatedObjects(context, relTypes, //relationship pattern
						objectTypes, // object pattern
						objectSelects, // object selects
						relSelects, // relationship selects
						false, // to direction
						true, //from direction
						(short) 0, // recursion level
						null, // object where clause
						null, // relationship where clause
						(short) 0, // limit
						CHECK_HIDDEN, // check hidden
						PREVENT_DUPLICATES, // prevent duplicates
						PAGE_SIZE, //pagesize
						null, // include type
						null, // include relationship
						null, // include Mpa
						null, // relKeyPrefix
						null); // effectivity filter expression
			
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objectList;
    }
    

    @com.matrixone.apps.framework.ui.ProgramCallable
    public String isDerivedReqWarning(Context context, String rootReqSpecId , String derivReqObjId, boolean isCovered, boolean isRefined)
    {
 		boolean isWarning = false;
		String isWarningDerivReq = "";

		//Getting MapList of all dependent req spec.
		MapList dependentReqSpec = new MapList();
		dependentReqSpec = allDependentReqSpec(context, rootReqSpecId, isCovered, isRefined);
		Map dependentReqSpecMap =  MapListToMapIdName(context, dependentReqSpec, ReqSchemaUtil.getRequirementSpecificationType(context));
		
		boolean isWarningDerivReqFlag = false;
		//When zero dependent Req Spec.
		if((dependentReqSpec.size() == 0))
		{
			isWarningDerivReq = "warning";
		}
		else
		{
			String relTypes = ReqSchemaUtil.getSpecStructureRelationship(context)
	    			+ "," + ReqSchemaUtil.getSubRequirementRelationship(context);
			
	    	try {
		    		StringList selectsAttribs  = new StringList(2);
					selectsAttribs.add(SELECT_ID);
					selectsAttribs.add(SELECT_NAME);
					selectsAttribs.add(SELECT_TYPE);
					
					DomainObject domDerivedReqObj = DomainObject.newInstance(context, derivReqObjId);
	
					//Finding all Req Spec where Derived Requirement exists as their child.
					MapList derivReqRootReqSpec = domDerivedReqObj.getRelatedObjects(context,
							relTypes,
							"*",
							selectsAttribs,    // Object selects
						    null,   //relationshipSelets, // relationship selects
					        true,      // from false -
					        false,       // to true - 
					        (short)0,   //expand level
					        null,       // object where
					        null,       // relationship where
					        0);         // limit
				
					//Map<String, String> derivReqRootReqSpecs = new HashMap<String, String>();

					//Storing all root Req spec id & name of Derived Requirement into Map.
					Iterator derivReqRootReqSpecItr = derivReqRootReqSpec.iterator();
					while(derivReqRootReqSpecItr.hasNext())
					{
						Map currDerivReqRootReqSpecMap = (Map) derivReqRootReqSpecItr.next();
						String currReqSpecId = (String) currDerivReqRootReqSpecMap.get(SELECT_ID);
					
						//String currReqSpecName = (String) currDerivReqRootReqSpecMap.get(SELECT_NAME);
					    if(((String) currDerivReqRootReqSpecMap.get(SELECT_TYPE)).equalsIgnoreCase(ReqSchemaUtil.getRequirementSpecificationType(context)))
					    {	
						   //derivReqRootReqSpecs.put(currReqSpecId, currReqSpecName);
					    	isWarning = !dependentReqSpecMap.containsKey(currReqSpecId);
					    	if(isWarning == true)
							{
								isWarningDerivReq = "warning";
								break;
							}
					    }
					}
					
			} catch (FrameworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}      
		}
		return isWarningDerivReq;
    }
    
    public Map MapListToMapIdName(Context context, MapList mapList, String objTypes)
    {
    	Map mapIdName = new HashMap();
    	Iterator Itr = mapList.iterator();
		while(Itr.hasNext())
		{
			Map map = (Map) Itr.next();
			String currId = (String) map.get(SELECT_ID);
			String currName = (String) map.get(SELECT_NAME);
			if(objTypes.contains((String) map.get(SELECT_TYPE)))
			{	
				mapIdName.put(currId, currName);
			}
		}
		return mapIdName;
    }
    
    //can be called from outside of class as JPO call.
   public Map getAllDependentReqSpecMap_JPO(Context context, String[] args)
   {
	   MapList allDependentReqSpecMapList = new MapList();
	   Map allDependentReqSpecMap         = new HashMap();
	   try {
			Map programMap 		= (HashMap) JPO.unpackArgs(args);
			String rootReqSpecId	= (String)programMap.get("rootReqSpecId");
			boolean isCovered	= (boolean)programMap.get("isCovered");
			boolean isRefined   = (boolean)programMap.get("isRefined");
			
			allDependentReqSpecMapList = allDependentReqSpec(context, rootReqSpecId, isCovered, isRefined);
			allDependentReqSpecMap = MapListToMapIdName(context, allDependentReqSpecMapList, ReqSchemaUtil.getRequirementSpecificationType(context));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	return allDependentReqSpecMap;
   }
    
    //Can be called from within the class.
    public MapList allDependentReqSpec(Context context, String rootReqSpecId, boolean isCovered, boolean isRefined)
    {
    	String reqspecType = ReqSchemaUtil.getRequirementSpecificationType(context); 
    	String rel         = ReqSchemaUtil.getDerivedRequirementSpecificationRelationship(context);
    	MapList dependentReqSpecs = new MapList();
    	try {
			StringList selectsAttribs  = new StringList(3);
			selectsAttribs.add(SELECT_ID);
			selectsAttribs.add(SELECT_NAME);
			selectsAttribs.add(SELECT_TYPE);
			DomainObject domRootReqSpecId = DomainObject.newInstance(context, rootReqSpecId);
			//Finding all dependent Req Specs.
			dependentReqSpecs = domRootReqSpecId.getRelatedObjects(context,
					rel,
					reqspecType,
					selectsAttribs,    // Object selects
				    null,   //relationshipSelets, // relationship selects
				    true,      // from false -
				    true,       // to true - 
			        (short)1,   //expand level
			        null,       // object where
			        null,       // relationship where
			        0);         // limit
			
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
		return dependentReqSpecs;
    }
   
// -- HAT1 ZUD: ReqSpec dependency - enhancements.
    

  //JX5
    @com.matrixone.apps.framework.ui.ProgramCallable
    public String getXMLDataForTraceabilityAuthoring(Context context, String[] args) throws Exception
    {
    	String xmlData = "";
    	
    	try{
    		
    		Map programMap 		  = (HashMap) JPO.unpackArgs(args);
    		String strObjectId	  = (String)programMap.get("objectId");
    		String strExpandLevel = (String)programMap.get("expandLevel");
   		
                String strDerivMode   = (String)programMap.get("mode");
    		String strTreeMode	  = (String)programMap.get("tree");
    		String rootReqSpecId  = (String)programMap.get("rootReqSpecId");
    		
       	 	String treeDisplaySettings = "N/A";

       	 	Boolean getCovered = false;
       	        Boolean getRefining = false;
       	 	Boolean singleNodeExpandFlag = true;
    		
    		StringList objectSelects = new StringList();
    		objectSelects.add(SELECT_ID);
    		objectSelects.add(SELECT_NAME);
    		objectSelects.add(SELECT_TYPE);
    		objectSelects.add(SELECT_REVISION);
    		objectSelects.add(SELECT_KINDOF);
    		objectSelects.add(SELECT_ATTRIBUTE_CONTENT_TEXT);
    		objectSelects.add(SELECT_ATTRIBUTE_TITLE);
    		objectSelects.add(SELECT_LEVEL);
    		
    		StringList relSelects	= new StringList();
    		relSelects.add(SELECT_RELATIONSHIP_ID);
    		relSelects.add(SELECT_RELATIONSHIP_TYPE);
    		relSelects.add(SELECT_ATTRIBUTE_TREE_ORDER);
    		
    		//get Information about rootObject
    		DomainObject rootObject = DomainObject.newInstance(context, strObjectId);
    		
    		StringList rootObjectSelects = new StringList();
    		rootObjectSelects.add(SELECT_TYPE);
    		rootObjectSelects.add(SELECT_NAME);
    		rootObjectSelects.add(SELECT_REVISION);
    		rootObjectSelects.add(SELECT_KINDOF);
    		rootObjectSelects.add(SELECT_ATTRIBUTE_TITLE);
    		rootObjectSelects.add(SELECT_ATTRIBUTE_CONTENT_TEXT);
    		
    		Map rootObjectInfos = rootObject.getInfo(context, rootObjectSelects);
    		
    		String objectId 			= strObjectId;
    		String objectName 			= (String)rootObjectInfos.get(SELECT_NAME);
    		//encode root name
    		objectName = RequirementsUtil.encodeSpecialCharAsHTML(objectName);
    		String objectRevision 		= (String)rootObjectInfos.get(SELECT_REVISION);
    		String objectType			= (String)rootObjectInfos.get(SELECT_TYPE);
    		String objectKindOf			= (String)rootObjectInfos.get(SELECT_KINDOF);
    		String objectTitle			= (String)rootObjectInfos.get(SELECT_ATTRIBUTE_TITLE);
    		//encode root title
    		objectTitle = RequirementsUtil.encodeSpecialCharAsHTML(objectTitle);
    		String objectContentText	= (String)rootObjectInfos.get(SELECT_ATTRIBUTE_CONTENT_TEXT);
    		//encode root content text
    		objectContentText = RequirementsUtil.encodeSpecialCharAsHTML(objectContentText);
    		String objectLevel			= "0";
    		String objectRelId			= "";
    		String objectRelType		= "";
    		String objectSeqOrder       = "";
    		//
    		
    		//retrieve root object icon
    		HashMap rootArgs = new HashMap();
    		MapList rootObjLst = new MapList();
    		MapList rootmapIconList = new MapList();
    		HashMap tmpMap = new HashMap();
    		Map rootmapIcon =  new HashMap();
    		
    		tmpMap.put(SELECT_ID,objectId);
    		rootObjLst.add(tmpMap);
    		rootArgs.put("objectList",rootObjLst);
    		
    		rootmapIconList = emxRMTCommon_mxJPO.getRequirementIcons(context, JPO.packArgs(rootArgs));
    		rootmapIcon = (Map)rootmapIconList.get(0);
    		String iconName = (String)rootmapIcon.get(strObjectId);
    		//
    		
    		//Tree display settings
    		treeDisplaySettings = emxRMTCommon_mxJPO.getTreeDisplaySettings(context, JPO.packArgs(objectType));
    		//
    		
    		//construct xml for root object
    		xmlData += "<r o=\""+objectId+"\" r=\"\" p=\"\" type=\""+objectType+"\" rel=\"\" level=\""+objectLevel+"\" revision=\""+objectRevision+"\" seqOrder=\"\" name=\""+objectName+"\" icon=\"images/"+iconName+"\""+" isDerived=\"false\" colorCode=\"N/A\" warningOrError=\"N/A\" kindOf=\""+objectKindOf+ "\" title=\""+objectTitle+"\" contentText=\""+objectContentText+"\" treeDisplaySettings=\""+treeDisplaySettings+"\"></r>";
    		//
    		
    		//Expand if needed
    		MapList objectList 	= new MapList();
    		String relTypes = "";
			
			String objectTypes = ReqSchemaUtil.getRequirementType(context) 
					+ "," +  ReqSchemaUtil.getChapterType(context);
			
    		if(!strExpandLevel.equals("0")){
    			
    			int maxLevels = 0;
    			if(strExpandLevel.equalsIgnoreCase("All")){
    				maxLevels = 0;
    			}else{
    				maxLevels = Integer.parseInt(strExpandLevel);
    			}
    			
    			relTypes = ReqSchemaUtil.getSpecStructureRelationship(context)
						+ "," + ReqSchemaUtil.getSubRequirementRelationship(context);
 
    			//Standard Expand Mode : we display fushia derived requirement (from->to)
    			if((strTreeMode.equalsIgnoreCase("Source") && strDerivMode.equalsIgnoreCase("satisfy")) || (strTreeMode.equalsIgnoreCase("Target") && strDerivMode.equalsIgnoreCase("cover")))
            	{
    				getCovered = false;
    				getRefining = true;
            	}
    			//Inverse Expand Mode : we do not display fushia derived requirement, we look for requirement's parent having derived requirement rel (to->from)
            	else if((strTreeMode.equalsIgnoreCase("Target") && strDerivMode.equalsIgnoreCase("satisfy")) || (strTreeMode.equalsIgnoreCase("Source") && strDerivMode.equalsIgnoreCase("cover")))
            	{
            		getCovered = true;
    				getRefining = false;
            	}
    			
    			objectList = rootObject.getRelatedObjects(context, relTypes, //relationship pattern
						objectTypes, // object pattern
						objectSelects, // object selects
						relSelects, // relationship selects
						false, // to direction
						true, //from direction
						(short) maxLevels, // recursion level
						null, // object where clause
						null, // relationship where clause
						(short) 0, // limit
						CHECK_HIDDEN, // check hidden
						PREVENT_DUPLICATES, // prevent duplicates
						PAGE_SIZE, //pagesize
						null, // include type
						null, // include relationship
						null, // include Mpa
						null, // relKeyPrefix
						null); // effectivity filter expression
    		}
    		//
    		objectList.sortStructure(context, "relationship,attribute[TreeOrder]", ",", "string,real", ",");
    		boolean addDerivedNode = true;
    		if(objectList.size() == 0 && (objectType.equalsIgnoreCase("Requirement")||objectKindOf.equalsIgnoreCase("Requirement")))
    		{
    			relTypes =  ReqSchemaUtil.getDerivedRequirementRelationship(context);
    			objectList = rootObject.getRelatedObjects(context, relTypes, //relationship pattern
						objectTypes, // object pattern
						objectSelects, // object selects
						relSelects, // relationship selects
						getCovered, // to direction
						getRefining, //from direction
						(short) 1, // recursion level
						null, // object where clause
						null, // relationship where clause
						(short) 0, // limit
						CHECK_HIDDEN, // check hidden
						PREVENT_DUPLICATES, // prevent duplicates
						PAGE_SIZE, //pagesize
						null, // include type
						null, // include relationship
						null, // include Mpa
						null, // relKeyPrefix
						null); // effectivity filter expression
    			
    			addDerivedNode = false;
    			
    		}
    		
        		String derivedFlag = "false";
        		String colorCode = "N/A";
    		for(int i = 0; i < objectList.size(); i++)
    		{
        			//init derivedFlag and colorCode
        			derivedFlag = "false";
        			colorCode	= "N/A";
        			DomainObject dObj = null;
        			if(addDerivedNode)
        			{
	        			Hashtable currentTable = (Hashtable)objectList.get(i);
	        			objectId 			= (String)currentTable.get(SELECT_ID);
	        			objectRelId 		= (String)currentTable.get(SELECT_RELATIONSHIP_ID);
	        			objectLevel			= (String)currentTable.get(SELECT_LEVEL);
	        			objectName			= (String)currentTable.get(SELECT_NAME);
	        			objectRelType 		= (String)currentTable.get(SELECT_RELATIONSHIP_TYPE);
	        			objectType			= (String)currentTable.get(SELECT_TYPE);
	        			objectRevision		= (String)currentTable.get(SELECT_REVISION);
	        			objectTitle			= (String)currentTable.get(SELECT_ATTRIBUTE_TITLE);
	        			objectContentText	= (String)currentTable.get(SELECT_ATTRIBUTE_CONTENT_TEXT);
	        			objectKindOf		= (String)currentTable.get(SELECT_KINDOF);
	        			objectSeqOrder		= (String)currentTable.get(SELECT_ATTRIBUTE_TREE_ORDER);
	        			
	        			dObj = DomainObject.newInstance(context, objectId);
	        			
	        			//encode object Name, Title, and Content Text
	        			objectName	= RequirementsUtil.encodeSpecialCharAsHTML(objectName);
	        			objectTitle = RequirementsUtil.encodeSpecialCharAsHTML(objectTitle);
	        			if(objectContentText.length() > 60){
	        				objectContentText = objectContentText.substring(0, 59);
	        				objectContentText += "...";
	        				}
	        			objectContentText = RequirementsUtil.encodeSpecialCharAsHTML(objectContentText);
        			
	        			//use title as name for chapter when applicable
	        			if(objectKindOf.equalsIgnoreCase("Chapter"))
	        			{
	        				if(objectTitle != null && !objectTitle.equalsIgnoreCase(""))
	        					objectName = objectTitle;
	        			}
        			
	        			//retrieve icon name
	        			HashMap arg = new HashMap();
	        			MapList objLst = new MapList();
	        			MapList mapIconList = new MapList();
	        			HashMap tempMap = new HashMap();
	        			Map mapIcon =  new HashMap();
	        			
	        			tempMap.put(SELECT_ID,objectId);
	        			tempMap.put(SELECT_RELATIONSHIP_ID,objectRelId);
	        			objLst.add(tempMap);
	        			arg.put("objectList",objLst);
	        			
	        			mapIconList = emxRMTCommon_mxJPO.getRequirementIcons(context, JPO.packArgs(arg));
	        			mapIcon = (Map)mapIconList.get(0);
	        			iconName = (String)mapIcon.get(objectId);
            		
	        			//check if object is a derived or sub requirement
	        			boolean isSub		= (objectRelType.equalsIgnoreCase("Sub Requirement")) ? true : false;
	        			if(isSub)
	        			{
	        				colorCode	= "cyan";
	        				iconName	= "iconReqTypeSubRequirement.png";
	        			}
            		
            		     //check tree display settings
	        			treeDisplaySettings = emxRMTCommon_mxJPO.getTreeDisplaySettings(context, JPO.packArgs(objectType));

	        			//construct xml data
	        			xmlData += "<r o=\""+objectId+"\" r=\""+objectRelId+"\" p=\""+strObjectId+"\" type=\""+objectType+"\" rel=\""+objectRelType+"\" level=\""+objectLevel+"\" revision=\""+objectRevision+"\" seqOrder=\""+objectSeqOrder+"\" name=\""+objectName+"\" icon=\"images/"+ iconName+"\" isDerived=\""+derivedFlag+"\" colorCode=\""+colorCode+"\"  kindOf=\""+objectKindOf  + "\"   title=\""+objectTitle+"\" contentText=\""+objectContentText+"\" treeDisplaySettings=\""+treeDisplaySettings+"\"></r>";
        			}
        			
            		//process covered if needed
        			if((objectType.equalsIgnoreCase("Requirement")||objectKindOf.equalsIgnoreCase("Requirement")) && singleNodeExpandFlag){
            			
            			relTypes = ReqSchemaUtil.getDerivedRequirementRelationship(context);
            			int maxLevels = 1;
        			
            			DomainObject dObjReqRoot = DomainObject.newInstance(context); 
            			if(strExpandLevel.equalsIgnoreCase("All"))
            			{
            				dObjReqRoot = dObj;
            			}
            			else
            			{
            				dObjReqRoot = DomainObject.newInstance(context, strObjectId);
            				singleNodeExpandFlag = false;
            			}
            		
            			MapList coveredList = dObjReqRoot.getRelatedObjects(context, relTypes, //relationship pattern
        						objectTypes, // object pattern
        						objectSelects, // object selects
        						relSelects, // relationship selects
        						getCovered, // to direction
        						getRefining, //from direction
        						(short) maxLevels, // recursion level
        						null, // object where clause
        						null, // relationship where clause
        						(short) 0, // limit
        						CHECK_HIDDEN, // check hidden
        						PREVENT_DUPLICATES, // prevent duplicates
        						PAGE_SIZE, //pagesize
        						null, // include type
        						null, // include relationship
        						null, // include Mpa
        						null, // relKeyPrefix
        						null); // effectivity filter expression
            			
            			coveredList.sortStructure(context, "relationship,attribute[TreeOrder]", ",", "string,real", ",");
            			
	        			String childobjectLevel	= "0";
	        			for(int j = 0; j < coveredList.size();j++ )
	        			{
	            				Map <String, String> covChildIdName = new HashMap<String, String>();
	            				Hashtable coveredTable = (Hashtable)coveredList.get(j);
	                			objectId 			= (String)coveredTable.get(SELECT_ID);
	                			objectRelId			= (String)coveredTable.get(SELECT_RELATIONSHIP_ID);
	                			int tempLevel		= Integer.parseInt(objectLevel) + 1;
	            			childobjectLevel	= Integer.toString(tempLevel);
	                			objectRelType		= (String)coveredTable.get(SELECT_RELATIONSHIP_TYPE);
	                			objectSeqOrder		= (String)coveredTable.get(SELECT_ATTRIBUTE_TREE_ORDER);
	                			objectName			= (String)coveredTable.get(SELECT_NAME);
	                			objectRevision		= (String)coveredTable.get(SELECT_REVISION);
	                			objectTitle			= (String)coveredTable.get(SELECT_ATTRIBUTE_TITLE);
	                			objectContentText	= (String)coveredTable.get(SELECT_ATTRIBUTE_CONTENT_TEXT);
	                			
	                			DomainObject covObj = DomainObject.newInstance(context, objectId);
	                			
	                			//encode name, title and content text
	                			objectName	= RequirementsUtil.encodeSpecialCharAsHTML(objectName);
	                			objectTitle	= RequirementsUtil.encodeSpecialCharAsHTML(objectTitle);
	                			if(objectContentText.length() > 60){
	                				objectContentText = objectContentText.substring(0, 59);
	                				objectContentText += "...";
	                			}
	                			
	            			boolean isDerived 	= (objectRelType.equalsIgnoreCase("Derived Requirement")) ? true : false;
	                		if(getRefining)
	                		{
	                			colorCode	= "fushia";
	                			iconName	= "iconReqTypeDerivedRequirement.png";
	                		}
	                		else
	                		{
	        		        	colorCode	 = "yellow";
	                			iconName	 = "iconReqTypeCoveredRequirement.png";
	                		}
	                			
	            			derivedFlag = "true";
		        				
		        			treeDisplaySettings = emxRMTCommon_mxJPO.getTreeDisplaySettings(context, JPO.packArgs(objectType));
		        				
	        				xmlData += "<r o=\""+objectId+"\" r=\""+objectRelId+"\" p=\""+strObjectId+"\" type=\""+objectType+"\" rel=\""+objectRelType+"\" level=\""+childobjectLevel+"\" revision=\""+objectRevision+"\" seqOrder=\""+objectSeqOrder+"\" name=\""+objectName+"\" icon=\"images/"+iconName+"\" isDerived=\""+derivedFlag+"\" colorCode=\""+colorCode+"\" kindOf=\""+ objectKindOf + "\" title=\""+objectTitle+"\" contentText=\""+objectContentText+"\" treeDisplaySettings=\""+treeDisplaySettings+"\"></r>";
	        				
	        		}
        		}	
        	}
    		
    		
    	}catch(Exception ex){
    		//NOP
    	}
    	
    	return xmlData;
    }
}

