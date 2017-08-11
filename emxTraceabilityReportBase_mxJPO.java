/*
** emxTraceabilityReportBase
**
** Copyright (c) 2007-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*/

/*

Change History:
Date       Change By  Release   Bug/Functionality        Details
-----------------------------------------------------------------------------------------------------------------------------
13-Apr-09  kyp        R207      371708                   Modified method getTraceabilityFolderData to correct structure browser
                                                         rendering, by adding the multi level expand information in the result
                                                         map list all the times.
26-May-09  kyp        V6R2010x  Derivation Matrix        getTraceabilityTableData modified to support includeParentInfo parameter
                                                         Added methods
                                                            getRequirementAllocationReportTableData
                                                            updateWithSpecInfo
                                                            processForFinalPresentation
                                                            convertToHorizontalTree
                                                            getRootRequirementNameColumnData
                                                            getRootRequirementRevisionColumnData
                                                            getDerivedRequirementNameColumnData
                                                            getDerivedRequirementRevisionColumnData
                                                            getDerivedRequirementSpecificationColumnData
                                                            getDerivedRequirementLinkStatusColumnData
                                                            getDerivedRequirementColumns

22-Jun-09  oep        V6R2010x  Enhanced Requirement     getRangeValuesForField
                                Reuse                    textNameRevList - Written an Overloading function for handling MATRIXSEARCH argument in Real Time Mode.
                                                         expandParentLinkList- Written an Overloading function for handling "MATRIXSEARCH" argument in Real Time mode.

30-Oct-09  oep	      V6R2011x  Requirement Allocation	 getTraceabilityTableData modified to support includeParentInfo parameter
							 getTableDirectionColumnData modified for TraceabilityReport Direction
							 getRequirementAllocationReportTableData Modified for Root Specification
							 updateWithSpecInfo handled parametes for Source Specification
							 processForFinalPresentation handled Parameter for Target Specification
							 Added methods
							     getTraceabilityCoverage - Called from TraceabiltyCoverage.jsp
							     getTraceabilityCoverageSpecName called from RMTTraceabilityCoverageReport Table
							     getTraceabilityCoveragePercentage called from RMTTraceabilityCoverageReport Table
							     getTableDirectionColumnData - Written overloaded function for Bi-Direction report
							     getRequirementSpecificationTargetName - Target Specification called from UI Tables

 06-Sep-12			V6R2013x Add of two program to display attributes of Parameters :  interface and Parameter Value

 17-Sep-12			V6R2013x add of a program to manage display of clones information
 
 20-May-13  qyg		IR-235513V6R2014 hide TRACEABILITY_NAME_ONLY from expand selectables since it's invalid
 
 */

/*					   MM:DD:YY
* @quickreview LX6     09:06:12 (IR-186487R2013x "NHI:V6R214:Function_026281: Requirement Parameter traceability report is KO. ")
* @quickreview JX5     04:10:13 Adapt code to use Type Icon Function & Type Icon Program
* @quickreview ZUD     06:18:13 IR-234701V6R2014x State column values in Full Traceability of Requirement are not translated into Japanese
* @quickreview T25 DJH 06:18:13 IR-234681V6R2014x.Added language support to "Sub Requirement" and "Derived Requiremnt".
* @quickreview T25 DJH 07:08:13 IR-207745V6R2014x. Modified method getExpressionColor(), added language support to "State" Column in excel during export.
* @quickreview T25 DJH 08:08:13 IR-207745V6R2014x. Integrating Qingwu's suggestion. Modified method getExpressionColor(), added language support to "Link Status" and "Validation Status" columns in excel during export.
* @quickreview JX5     09:16:14 IR-328029-3DEXPERIENCER2015x : STP: Requirement parameter traceability report KO.
* @quickreview JX5     10:03:14 Modify getParameterValue(Context context, String[] args) to retrieve the pameter value with its unit
* @quickreview JX5     11:25:14 Modify traceability table link to display the correct object 
* @quickreview KIE1 ZUD 02:02:15 IR-338259-3DEXPERIENCER2016 HTML code displayed for Requirement specificaiotn in exported excel sheet.
* @quickreview KIE1 ZUD 02:02:15 IR-338073-3DEXPERIENCER2016  HTML code is displayed in full traceability report.
* @quickreview QYG      01:06:15 IR-372849-3DEXPERIENCER2016x: reverse change made for IR-338073
* @quickreview JX5	    07:20:15 Replace old Allocation Status icons by new Parenthood icons 
* @quickreview KIE1 ZUD 10:14:15 IR-395963-3DEXPERIENCER2017x: R418-STP: Unused commands are present in Contextual Menu of Requirement Specification overview.
* @quickreview KIE1 ZUD 11:17:15 IR-386290-3DEXPERIENCER2017x: PlmParameter API migration
* @quickreview QYG      16:02:02 IR-422207-3DEXPERIENCER2015x: issue with cell editing, should use SPAN instead of DIV
*/
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Person;
import matrix.db.Relationship;
import matrix.db.RelationshipWithSelectList;
import matrix.db.SelectConstants;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.dassault_systemes.knowledge_itfs.IKweList;
import com.dassault_systemes.parameter_interfaces.IPlmParameter;
import com.dassault_systemes.parameter_interfaces.IPlmParameter.PLMParm_ValuationType;
import com.dassault_systemes.parameter_interfaces.IPlmParameterDisplay;
import com.dassault_systemes.parameter_interfaces.ParameterInterfacesServices;
import com.dassault_systemes.parameter_interfaces.ParameterTypes;
import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIRTEUtil;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.apps.requirements.RequirementsCommon;
import com.matrixone.apps.requirements.RequirementsUtil;

/**
 * This JPO class has some methods pertaining to Traceability Reports.
 * @author Brian Casto
 * @version ProductCentral 10.7 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxTraceabilityReportBase_mxJPO extends emxDomainObject_mxJPO
{
//Added:26-May-09:kyp:V6R2010x:RMT Requirements Allocation Report
   private static final String NUM_DERIVED_COLUMNS = "$nDerivedColumns";
//End:V6R2010x:RMT Requirements Allocation Report

   /**
    * Variable for type Requirement
    */
   public static final String TYPE_REQUIREMENT = PropertyUtil.getSchemaProperty("type_Requirement");
   /**
    * Variable for type Chapter
    */
   public static final String TYPE_CHAPTER = PropertyUtil.getSchemaProperty("type_Chapter");
   /**
    * Variable for Relationship = "Specification Structure"
    */
   public static final String RELATIONSHIP_SPECIFICATION_STRUCTURE = PropertyUtil.getSchemaProperty("relationship_SpecificationStructure");
   /**
    * Variable for attribute Link Status
    */
   public static final String ATTRIBUTE_LINK_STATUS = PropertyUtil.getSchemaProperty("attribute_LinkStatus");
   /**
    *  Variable for Direction
    */
   public static final String EXPAND_RELATIONSHIP_DIRECTION = "direction";
   /**
    * Variable for Traceability Name only
    */
   public static final String TRACEABILITY_NAME_ONLY = "$$Traceability_name_only";

   static  String language;
   private MapList tableData;

    private static enum statusLink { Suspect, Valid, Invalid };
	private static final String ICON_REQUIREMENT_VALID_FROM = "../requirements/images/iconReqInfoTypeParenthoodValueValidFrom.png";
	private static final String ICON_REQUIREMENT_VALID_TO = "../requirements/images/iconReqInfoTypeParenthoodValueValidTo.png";
	private static final String ICON_REQUIREMENT_INVALID_FROM = "../requirements/images/iconReqInfoTypeParenthoodValueInvalidFrom.png";
	private static final String ICON_REQUIREMENT_INVALID_TO = "../requirements/images/iconReqInfoTypeParenthoodValueInvalidTo.png";
	private static final String ICON_REQUIREMENT_SUSPECT_FROM = "../requirements/images/iconReqInfoTypeParenthoodValueSuspectFrom.png";
	private static final String ICON_REQUIREMENT_SUSPECT_TO = "../requirements/images/iconReqInfoTypeParenthoodValueSuspectTo.png";

	private static final String IS_FULL_TRACEABILITY_REPORT = "isFullTraceabilityreport";
   /**
    * Create a new emxTraceabilityReportBase object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return a emxTraceabilityReportBase object.
    * @throws Exception if the operation fails
    * @since ProductCentral 10.7.0.0
    * @grade 0
    */
   public emxTraceabilityReportBase_mxJPO(Context context, String[] args) throws Exception
   {
      super(context, args);
      language = context.getSession().getLanguage();
   }


   /**
    * Main entry point.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return an integer status code (0 = success)
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    * @grade 0
    */
   public int mxMain (Context context, String[] args) throws Exception
   {
      if (!context.isConnected())
      {
         String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.FeaturesCheckFailed"); 
         throw  new Exception(strContentLabel);
      }
      return(0);
   }


   /**
    *  Init objects for a Full Traceability Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args      The packed programMap arguments.
    *  @return MapList  The related object maps, as found be the expand method/args
    *  @throws Exception if the operation fails
    */
   public MapList initTraceabilityFolderData(Context context, String[] args)
      throws Exception
   {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      //SpecificationStructure.printIndentedMap(programMap);

      String rootId = (String) programMap.get("objectId");
      DomainObject domObject = (/*objectId != null? DomainObject.newInstance(context, objectId):*/ null);
      String objectType = (domObject != null? domObject.getTypeName(): "Requirement");

      // Initialize the table data:
      tableData = new MapList();

      // Find all the expand Programs as defined in the emxRequirements.properties file:
      int sectionCnt = 1;
      String sectionKey = "emxRequirements.FullTraceability." + objectType + ".ExpandSet" + sectionCnt;
      String resourceKey = getSettingProperty(context, sectionKey);

      int folderCnt = 0;
      while (resourceKey != null && !resourceKey.equals(""))
      {
         String sectionName = getStringResource(context, resourceKey + ".Name");
         String sectionDesc = getStringResource(context, resourceKey + ".Desc");

         String expandRels = getSettingProperty(context, sectionKey + ".Rels");
         String expandDir = getSettingProperty(context, sectionKey + ".Dir");
         String expandTypes = getSettingProperty(context, sectionKey + ".Types");
         String expandLevel = getSettingProperty(context, sectionKey + ".Level");

         String programVal = getSettingProperty(context, sectionKey + ".Program");
         String[] params = (programVal != null && !programVal.equals("")? programVal.split("[&]"): null);
         String programStr = (params != null && params.length > 0? params[0]: null);

         HashMap paramMap = (HashMap) programMap.clone();
         paramMap.put("reportShowNoRel", "false");
         for (int ii = 1; params != null && ii < params.length; ii++)
         {
            String[] param = params[ii].split("[=]", 2);

            if (param != null && param.length > 0)
               paramMap.put(param[0], (param.length == 2? param[1]: ""));
         }
         paramMap.put("reportRelationships", expandRels);
         paramMap.put("reportDirection", expandDir);
         paramMap.put("reportTypes", expandTypes);
         paramMap.put("expandLevel", expandLevel);
         //System.out.println("paramMap = " + paramMap);

         // Execute the JPO program with the parameter map:
         String[] jpoprg = (programStr == null? null: programStr.split("[:]", 2));
         try
         {
            MapList outList;

            if (jpoprg != null && jpoprg.length == 2)
            {
               // Execute the JPO method and return the expanded objects...
               outList = (MapList) JPO.invokeLocal(context, jpoprg[0], null, jpoprg[1], params, MapList.class);
            }
            else
            {
               // Expand the input object and add any related objects to the table data...
               outList = getTraceabilityTableData(context, paramMap);
            }

            if (outList != null && outList.size() > 0)
            {
               Map folderMap = new HashMap();
               folderCnt++;

               folderMap.put(SELECT_ID, rootId);
               //folderMap.put(SELECT_ID, "0.0.0." + folderCnt);
               folderMap.put(SELECT_RELATIONSHIP_ID, "0.0.0." + folderCnt);
               folderMap.put(SELECT_TYPE, "");
               folderMap.put(SELECT_NAME, sectionName);
               folderMap.put(SELECT_REVISION, " - [" + outList.size() + "]");
               folderMap.put(SELECT_CURRENT, "");
               folderMap.put(SELECT_LEVEL, "0");
               folderMap.put("attribute[Marketing Name]", sectionDesc);
               folderMap.put("RowEditable", "readonly");
               folderMap.put("Root Node", "" + (outList.size() == 0));
               folderMap.put("child", outList);
               tableData.add(folderMap);
            }
         }
         catch (Throwable e)
         {
            e.printStackTrace();
         }

         sectionCnt++;
         sectionKey = "emxRequirements.FullTraceability." + objectType + ".ExpandSet" + sectionCnt;
         resourceKey = getSettingProperty(context, sectionKey);
      }

      //SpecificationStructure.printIndentedList("Tracability Init Data:", tableData);
      return(tableData);
   }

   /**
    *  Init objects for a Full Traceability Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args      The packed programMap arguments.
    *  @return MapList  The related object maps, as found be the expand method/args
    *  @throws Exception if the operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getTraceabilityFolderData(Context context, String[] args)
      throws Exception
   {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);

      String relId = (String) programMap.get("relId");
      String sectionId = (relId != null && relId.startsWith("0.0.0.")? relId.substring(6): "");
      String objectId = (String) programMap.get("objectId");
      DomainObject domObject = DomainObject.newInstance(context, objectId);
      // String objectType = TYPE_REQUIREMENT;
      String objectType = "";

      if (domObject.openObject(context))
      {
         objectType = domObject.getTypeName();
         domObject.close(context);
      }


      // KIE1 ZUD Added for IR-395963-3DEXPERIENCER2017x
      if(mxType.isOfParentType(context, objectType, ReqSchemaUtil.getSpecificationType(context)))
      {
    	  objectType = ReqSchemaUtil.getSpecificationType(context);
      }
      
      if(mxType.isOfParentType(context, objectType, ReqSchemaUtil.getRequirementType(context)))
      {
    	  objectType = ReqSchemaUtil.getRequirementType(context);
      }
      
      if(mxType.isOfParentType(context, objectType, ReqSchemaUtil.getWorkspaceVaultType(context)))
      {
    	  objectType = ReqSchemaUtil.getWorkspaceVaultType(context);
    	  objectType = objectType.replace(" ", "");
      }

      // Initialize the folder data:
      MapList folderData = new MapList();

      if ("".equals(sectionId))
      {
         // Find all the expand Programs as defined in the emxRequirements.properties file:
         int sectionCnt = 1;
         String sectionKey = "emxRequirements.FullTraceability." + objectType + ".ExpandSet" + sectionCnt;
         String resourceKey = getSettingProperty(context, sectionKey);

         while (resourceKey != null && !resourceKey.equals(""))
         {
            HashMap paramMap = (HashMap) programMap.clone();
            MapList sectList = fullTraceabilityFolderData(context, paramMap, objectId, sectionCnt, sectionKey, resourceKey);

            // Append any children for this section to the output list...
            if (sectList != null && !sectList.isEmpty())
            {
               folderData.addAll(sectList);
            }

            sectionCnt++;
            sectionKey = "emxRequirements.FullTraceability." + objectType + ".ExpandSet" + sectionCnt;
            resourceKey = getSettingProperty(context, sectionKey);
         }
      }
      else
      {
         // Expand the objects using the Program defined in the keyed section of the properties file:
         String sectionKey = "emxRequirements.FullTraceability." + objectType + ".ExpandSet" + sectionId;
         String resourceKey = getSettingProperty(context, sectionKey);

         if (resourceKey != null && !resourceKey.equals(""))
         {
            HashMap paramMap = (HashMap) programMap.clone();
            MapList sectList = fullTraceabilityFolderData(context, paramMap, objectId, 0, sectionKey, resourceKey);

            // Append any children for this section to the output list...
            if (sectList != null && !sectList.isEmpty())
               folderData.addAll(sectList);
         }
      }

      // Mark this list as output from a multi-level expand JPO:
//      String multiExp = "" + programMap.get("expandMultiLevelsJPO");
//      if (folderData.size() > 0 && "true".equalsIgnoreCase(multiExp))
//      {
//         HashMap expandMap = new HashMap();
//         expandMap.put("expandMultiLevelsJPO", "true");
//         expandMap.put("updateTableCache", "false");
//         folderData.add(expandMap);
//      }

      // Modified:13-Apr-09:kyp:R207:RMT Bug 371708
      //
      // The rendering of structure browser was getting disturbed when it didnt find following keys in the result
      // maplist. When this information is not there, structure browser assumes that the expanded data lies on the
      // same level and not indented.
      //
         HashMap expandMap = new HashMap();
         expandMap.put("expandMultiLevelsJPO", "true");
         expandMap.put("updateTableCache", "false");
         folderData.add(expandMap);
      // End:R207:RMT Bug 371708

      //SpecificationStructure.printIndentedList("Traceability Folder Data:", folderData);
      return(folderData);
   }

   
   /**
    *  Init objects for a Sub Requirement level 1 report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args      The packed programMap arguments.
    *  @return MapList  The related object maps, as found be the expand method/args
    *  @throws Exception if the operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getBothDirectionSubRequirements(Context context, String[] args, String direction)
      throws Exception
   {
	   MapList folderData = new MapList();
	   boolean from = true;
	   boolean to = false;
	   if(direction.equalsIgnoreCase("up")){
		   from = false;
		   to = true;
	   }
	   
	  String SELECT_RESERVED_BY = "reservedby"; 
	   
      HashMap programMap = (HashMap)JPO.unpackArgs(args);

      String relId = (String) programMap.get("relId");
      String sectionId = (relId != null && relId.startsWith("0.0.0.")? relId.substring(6): "");
      String strObjectId = (String) programMap.get("objectId");
      DomainObject domObject = DomainObject.newInstance(context, strObjectId);
      // String objectType = TYPE_REQUIREMENT;
      String objectType = "";

      MapList subRequirementsList=new MapList();
      try
      { 
          // unpack the arguments
          HashMap paramMap = (HashMap)JPO.unpackArgs(args);

          String expLevel = (String)paramMap.get("expandLevel");

          // get the type of the business object
          String strType = domObject.getInfo(context,DomainConstants.SELECT_TYPE);
          StringBuffer sbTypeSelect = new StringBuffer(60);
          StringBuffer sbRelSelect = new StringBuffer(60);

          // object selects
          StringList selectStmts = new StringList(2);
          selectStmts.addElement(DomainConstants.SELECT_ID);
          selectStmts.addElement(DomainConstants.SELECT_NAME);
          // add attributes below to support rich text editor
          selectStmts.addElement(DomainConstants.SELECT_TYPE);
          selectStmts.addElement(DomainConstants.SELECT_REVISION);
          selectStmts.addElement(DomainConstants.SELECT_DESCRIPTION);
          selectStmts.addElement(DomainConstants.SELECT_MODIFIED);
          selectStmts.addElement(SELECT_RESERVED_BY);

          // relationship selects
          StringList selectRelStmts = new StringList(1);
          selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
          // add level to support rich text editor
          selectRelStmts.addElement(DomainConstants.SELECT_LEVEL);
          selectRelStmts.addElement("attribute[" + RequirementsUtil.getSequenceOrderAttribute(context) + "]");
         	// ++ ZUD Fix For IR IR-234292  ++
          selectRelStmts.addElement(DomainRelationship.SELECT_FROM_ID);
			// -- ZUD Fix For IR IR-234292  -- 

          // calling 'getChildrenTypes' method of ProductLineUtil bean to get all the childs of type 'Requirement'
          List lstReqChildTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));

          // construct the type list
          if (lstReqChildTypes.contains(strType) || ReqSchemaUtil.getRequirementType(context).equals(strType))
          {
   
              // construct the relationship list
              sbRelSelect = sbRelSelect.append(ReqSchemaUtil.getSubRequirementRelationship(context));

              // get all the related requirement object based on type & relationship list.
              String  effectivityFilter = (String) paramMap.get("CFFExpressionFilterInput_OID");
              //BPS regression, temp fix
              if("undefined".equalsIgnoreCase(effectivityFilter)){
             	 effectivityFilter = null;
              }
      	    if(effectivityFilter != null && effectivityFilter.length() != 0)
      	    {//performed configured expand.
      	    	subRequirementsList = domObject.getRelatedObjects(context,
      	    			ReqSchemaUtil.getSubRequirementRelationship(context),     // relationship pattern
      	    			ReqSchemaUtil.getRequirementType(context),                     // object pattern
      	    			selectStmts,                      // object selects
      	    			selectRelStmts,                 // relationship selects
						  to,                               // to direction
						  from,                               // from direction
						  (short)1,                 // recursion level
						  null,                              // object where clause
						  null,                              // relationship where clause
						  (short)0,                        	 // limit
						  CHECK_HIDDEN,            				 // check hidden
						  PREVENT_DUPLICATES,   			// prevent duplicates
						  PAGE_SIZE,                   		// pagesize
						  null,                              // includeType
						  null,                              // includeRelationship
						  null,                              // includeMap
						  null,                              // relKeyPrefix
						  effectivityFilter);            // Effectivity filter expression from the SB toolbar
      	    }
      	    else
      	    {//perform standard expand
                  subRequirementsList = domObject.getRelatedObjects(context, ReqSchemaUtil.getSubRequirementRelationship(context), ReqSchemaUtil.getRequirementType(context),
                          selectStmts, selectRelStmts, to, from, (short) 1, null, null);
      	    }  	        	    
      	    
      	    
              int objCount = subRequirementsList.size();
              String usrName = context.getUser();
              for (int jj = 0; jj < objCount; jj++)
              {
                 Map mapObject = (Map) subRequirementsList.get(jj);
                 String strObjId = (String) mapObject.get(SELECT_ID);
                 String objResBy = (String) mapObject.get(SELECT_RESERVED_BY);

                 // Check that the object is not reserved by another, and that it is in a "Modifyable" state...
                 Access objAccess = DomainObject.newInstance(context, strObjId).getAccessMask(context);
                 boolean objEditable = (("".equals(objResBy) || usrName.equals(objResBy)) && objAccess.hasModifyAccess());

                 // Mark the row as non-editable if someone else has this object reserved.
                 mapObject.put("RowEditable", (objEditable? "show": "readonly"));

              }
          
          }

          subRequirementsList.sortStructure(context, "attribute[Sequence Order]", "", "integer", "");
          
          RequirementsUtil.markLeafNodes(subRequirementsList, 1);

          /*HashMap hmTemp = new HashMap();
      	hmTemp.put("expandMultiLevelsJPO","true");
      	subRequirementsList.add(hmTemp);*/
      	
  	    if(!subRequirementsList.isEmpty()){
  	    	String colName =  null;
  	    	String property = null;
  	    	String folderCnt = null;
  	    	//emxRequirements.FullTraceability.Requirement.Expand.BreakdownToRequirements.Name = Child Sub-Requirements
  	    	//emxRequirements.FullTraceability.Requirement.Expand.BreakdownToRequirements.Desc = Sub-Requirement Children
  	    	if(direction.equalsIgnoreCase("up")){	    		
  	    		property = "emxRequirements.FullTraceability.Requirement.Expand.BreakdownFromRequirements.Name";
  	    		folderCnt = "0";
  	    		
  	    	}else{
  	    		property = "emxRequirements.FullTraceability.Requirement.Expand.BreakdownToRequirements.Name";
  	    		folderCnt="1";
  	    	}
  	    	colName = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), property);
  	    	 Map folderMap = new HashMap();
             folderMap.put(SELECT_ID, strObjectId);
             //folderMap.put(SELECT_ID, "0.0.0." + folderCnt);
             folderMap.put(SELECT_RELATIONSHIP_ID, "0.0.0." + folderCnt);
             folderMap.put(SELECT_TYPE, "");
             folderMap.put(SELECT_NAME, colName);
             folderMap.put(SELECT_REVISION, " - [" + "0" + "]");
             folderMap.put(SELECT_CURRENT, "");
             folderMap.put(SELECT_LEVEL, "0");
             folderMap.put("attribute[Marketing Name]", colName);
             folderMap.put("RowEditable", "readonly");
             folderMap.put("Root Node", "" );
             folderMap.put("notAnObject", "" );
             folderMap.put("child", subRequirementsList);
             folderData.add(folderMap);
             folderData.addAll(subRequirementsList);
  	    }
  	    
      }
      catch(Exception ex)
      {
          throw(new FrameworkException(ex.getMessage()));
      }
      
     return folderData;
   }
   
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getBothDirectionSubRequirements(Context context, String[] args){
	   MapList subRequirementsList= null;
	   try {
		subRequirementsList = getBothDirectionSubRequirements(context, args, "down");
		subRequirementsList.addAll(getBothDirectionSubRequirements(context, args, "up"));
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}   
	   return subRequirementsList;
   }

   public Vector getFilterRichText(Context context, String[] args) throws Exception {
	   Vector richText = (Vector)JPO.invoke(context, "emxRMTCommon", null, "getAllHTMLSourceFromRTF",
       		args,Vector.class);
	   Map inputMap = (Map) JPO.unpackArgs(args);
	   HashMap paramList = (HashMap) inputMap.get("paramList");
	   MapList objectMap = (MapList) inputMap.get("objectList");
       
	   Vector returnVector = new Vector();
       boolean isFromDloatingDiv = (paramList.get("fromFloatingDiv") != null && ((String) paramList.get("fromFloatingDiv")).equals("true")) ? true : false;
       String idKey = isFromDloatingDiv==true?"target.id":"id";
       for(int i=0;i<objectMap.size();i++){
    	   Map<String, String> curObjectMap = (Map) objectMap.get(i);
           String objectID = (String) curObjectMap.get(idKey);
           if(curObjectMap.containsKey("notAnObject")){
        	   richText.set(i, "");
           }
           
       }
	   return richText;
   }
   /**
    * @param context
    * @param programMap
    * @param sectionKey
    * @param resourceKey
    * @param folderCnt
    * @return
    */
   private MapList fullTraceabilityFolderData(Context context, HashMap paramMap, String objectId,
                                              int sectionId, String sectionKey, String resourceKey)
   {
      MapList folderData = new MapList();
      boolean showFolder = (sectionId > 0);

      String sectionName = getStringResource(context, resourceKey + ".Name");
      String sectionDesc = getStringResource(context, resourceKey + ".Desc");

      String expandRels = getSettingProperty(context, sectionKey + ".Rels");
      String expandDir = getSettingProperty(context, sectionKey + ".Dir");
      String expandTypes = getSettingProperty(context, sectionKey + ".Types");
      String expandLevel = getSettingProperty(context, sectionKey + ".Level");

      String programVal = getSettingProperty(context, sectionKey + ".Program");
      String[] params = (programVal != null && !programVal.equals("")? programVal.split("[&]"): null);
      String programStr = (params != null && params.length > 0? params[0]: null);

//Start:LX6:display Clone information feature      
      DomainObject domObject;
      if(expandTypes.equals("Unused"))
	      try {
	    	  domObject = DomainObject.newInstance(context, objectId);
	    	  expandTypes = domObject.getType(context);
	      } catch (FrameworkException e1) {
	    	  // TODO Auto-generated catch block
	    	  e1.printStackTrace();
	      }
//End:LX6:display Clone information feature
      paramMap.put("reportShowNoRel", "false");
      for (int ii = 1; params != null && ii < params.length; ii++)
      {
         String[] param = params[ii].split("[=]", 2);

         if (param != null && param.length > 0)
            paramMap.put(param[0], (param.length == 2? param[1]: ""));
      }
      paramMap.put("reportRelationships", expandRels);
      paramMap.put("reportDirection", expandDir);
   
      paramMap.put("reportTypes", expandTypes);
      paramMap.put("expandLevel", expandLevel);
      //System.out.println("paramMap = " + paramMap);

      // Execute the JPO program with the parameter map:
      String[] jpoprg = (programStr == null? null: programStr.split("[:]", 2));
      try
      {
         MapList outList;

         if (jpoprg != null && jpoprg.length == 2)
         {
            // Execute the JPO method and return the expanded objects...
            outList = (MapList) JPO.invokeLocal(context, jpoprg[0], null, jpoprg[1], params, MapList.class);
         }
         else
         {
            // Expand the input object and add any related objects to the table data...
            outList = getTraceabilityTableData(context, paramMap);
         }

         if (outList != null && outList.size() > 0)
         {
            // Generate a unique list of any related child objects...
            int objectCnt = 0;
            TreeSet uniqueIds = new TreeSet();
            MapList childList = new MapList();

            for (Iterator mapRows = outList.iterator(); mapRows.hasNext();)
            {
               Map objectMap = (Map) mapRows.next();
               String uniqueId = (String) objectMap.get(SELECT_ID);

               if (!uniqueIds.contains(uniqueId))
               {
                  objectCnt++;
                  objectMap.put("RowEditable", "readonly");

                  // If this object is the child of a folder, bump the level by 1:
                  if (showFolder)
                     objectMap.put(SELECT_LEVEL, "2");

                  uniqueIds.add(uniqueId);
                  childList.add(objectMap);
               }
            }

            // Sort the Children by name within each expand section...
            Collections.sort(childList, new Comparator()
            {
               public int compare(Object o1, Object o2)
               {
                  try
                  {
                     Map map1 = (Map) o1;
                     Map map2 = (Map) o2;
                     String name1 = (String) map1.get(SELECT_NAME);
                     String name2 = (String) map2.get(SELECT_NAME);

                     if (name1 != null && name2 != null)
                        return(name1.compareToIgnoreCase(name2));
                     else if (name1 == null)
                        return(-1);
                     else
                        return(1);
                  }
                  catch (RuntimeException e)
                  {
                     e.printStackTrace();
                     return(0);
                  }
               }
            });

            // Only add the folder information when each numbered section is expanded:
            if (showFolder)
            {
               Map folderMap = new HashMap();
               folderMap.put(SELECT_ID, objectId);
               folderMap.put(SELECT_TYPE, "");
               folderMap.put(SELECT_NAME, sectionName);
               folderMap.put(SELECT_REVISION, " - [" + childList.size() + "]");
               folderMap.put(SELECT_CURRENT, "");
               folderMap.put(SELECT_LEVEL, "1");
               folderMap.put("attribute[Marketing Name]", sectionDesc);
               folderMap.put("RowEditable", "readonly");
               folderMap.put("hasChildren", "true");
               folderMap.put("child", childList);
               folderData.add(folderMap);
            }

            // Finally, append any children that were found...
            folderData.addAll(childList);
         }

      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }

      return(folderData);
   }

   /**
    *  Expand objects in a Full Traceability Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args      The packed programMap arguments.
    *  @return MapList  The related object maps, as found be the expand method/args
    *  @throws Exception if the operation fails
    */
   public MapList getTraceabilityFullData(Context context, String[] args)
      throws Exception
   {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      //SpecificationStructure.printIndentedMap(programMap);

      String relId = (String) programMap.get("relId");
      String sectionId = (relId.startsWith("0.0.0.")? relId.substring(6): "");
      String objectId = (String) programMap.get("objectId");
      DomainObject domObject = DomainObject.newInstance(context, objectId);
      String objectType = "Requirement";

      if (domObject.openObject(context))
      {
         objectType = domObject.getTypeName();
         domObject.close(context);
      }

      // Initialize the table data:
      MapList childList = new MapList();

      String sectionKey = "emxRequirements.FullTraceability." + (objectType == null? "Requirement": objectType) + ".ExpandSet" + sectionId;

      String expandRels = getSettingProperty(context, sectionKey + ".Rels");
      String expandDir = getSettingProperty(context, sectionKey + ".Dir");
      String expandTypes = getSettingProperty(context, sectionKey + ".Types");
      String expandLevel = getSettingProperty(context, sectionKey + ".Level");

      String programVal = getSettingProperty(context, sectionKey + ".Program");
      String[] params = (programVal != null && !programVal.equals("")? programVal.split("[&]"): null);
      String programStr = (params != null && params.length > 0? params[0]: null);

      HashMap paramMap = (HashMap) programMap.clone();
      paramMap.put("reportShowNoRel", "false");
      for (int ii = 1; params != null && ii < params.length; ii++)
      {
         String[] param = params[ii].split("[=]", 2);

         if (param != null && param.length > 0)
            paramMap.put(param[0], (param.length == 2? param[1]: ""));
      }
      paramMap.put("reportRelationships", expandRels);
      paramMap.put("reportDirection", expandDir);
      paramMap.put("reportTypes", expandTypes);
      paramMap.put("expandLevel", expandLevel);
      //System.out.println("paramMap = " + paramMap);

      // Execute the JPO program with the parameter map:
      String[] jpoprg = (programStr == null? null: programStr.split("[:]", 2));
      try
      {
         MapList outList;

         if (jpoprg != null && jpoprg.length == 2)
         {
            // Execute the JPO method and return the expanded objects...
            outList = (MapList) JPO.invokeLocal(context, jpoprg[0], null, jpoprg[1], params, MapList.class);
         }
         else
         {
            // Expand the input object and add any related objects to the table data...
            outList = getTraceabilityTableData(context, paramMap);
         }

         if (outList != null && outList.size() > 0)
         {
            TreeSet uniqueIds = new TreeSet();

            for (Iterator mapRows = outList.iterator(); mapRows.hasNext();)
            {
               Map objectMap = (Map) mapRows.next();
               String uniqueId = (String) objectMap.get(SELECT_ID);

               if (!uniqueIds.contains(uniqueId))
               {
                  objectMap.put("RowEditable", "readonly");
                  objectMap.put("Root Node", "false");

                  uniqueIds.add(uniqueId);
                  childList.add(objectMap);
               }
            }
         }
      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }

      //SpecificationStructure.printIndentedList("Tracability Full Data:", childList);
      return(childList);
   }

   /**
    *  Get objects for a Full Traceability Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args      The packed programMap arguments.
    *  @return MapList  The related object maps, as found be the expand method/args
    *  @throws Exception if the operation fails
    */
   private MapList getTraceabilityData(Context context, String[] args, boolean showFolder)
      throws Exception
   {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      //SpecificationStructure.printIndentedMap(programMap);

      String objectId = (String) programMap.get("objectId");
      DomainObject domObject = (/*objectId != null? DomainObject.newInstance(context, objectId):*/ null);
      String objectType = (domObject != null? domObject.getTypeName(): "Requirement");

      // Initialize the table data:
      tableData = new MapList();

      // Find all the expand Programs as defined in the emxRequirements.properties file:
      int sectionCnt = 1;
      String sectionKey = "emxRequirements.FullTraceability." + objectType + ".ExpandSet" + sectionCnt;
      String resourceKey = getSettingProperty(context, sectionKey);

      int folderCnt = 0;
      while (resourceKey != null && !resourceKey.equals(""))
      {
         String sectionName = getStringResource(context, resourceKey + ".Name");
         String sectionDesc = getStringResource(context, resourceKey + ".Desc");

         String expandRels = getSettingProperty(context, sectionKey + ".Rels");
         String expandDir = getSettingProperty(context, sectionKey + ".Dir");
         String expandTypes = getSettingProperty(context, sectionKey + ".Types");
         String expandLevel = getSettingProperty(context, sectionKey + ".Level");

         String programVal = getSettingProperty(context, sectionKey + ".Program");
         String[] params = (programVal != null && !programVal.equals("")? programVal.split("[&]"): null);
         String programStr = (params != null && params.length > 0? params[0]: null);

         HashMap paramMap = (HashMap) programMap.clone();
         paramMap.put("reportShowNoRel", "false");
         for (int ii = 1; params != null && ii < params.length; ii++)
         {
            String[] param = params[ii].split("[=]", 2);

            if (param != null && param.length > 0)
               paramMap.put(param[0], (param.length == 2? param[1]: ""));
         }
         paramMap.put("reportRelationships", expandRels);
         paramMap.put("reportDirection", expandDir);
         paramMap.put("reportTypes", expandTypes);
         paramMap.put("expandLevel", expandLevel);
         //System.out.println("paramMap = " + paramMap);

         // Execute the JPO program with the parameter map:
         String[] jpoprg = (programStr == null? null: programStr.split("[:]", 2));
         try
         {
            MapList outList;

            if (jpoprg != null && jpoprg.length == 2)
            {
               // Execute the JPO method and return the expanded objects...
               outList = (MapList) JPO.invokeLocal(context, jpoprg[0], null, jpoprg[1], params, MapList.class);
            }
            else
            {
               // Expand the input object and add any related objects to the table data...
               outList = getTraceabilityTableData(context, paramMap);
            }

            if (outList != null && outList.size() > 0)
            {
               int objectCnt = 0;
               TreeSet uniqueIds = new TreeSet();
               MapList childData = new MapList();

               for (Iterator mapRows = outList.iterator(); mapRows.hasNext();)
               {
                  Map objectMap = (Map) mapRows.next();
                  String uniqueId = (String) objectMap.get(SELECT_ID);

                  if (!uniqueIds.contains(uniqueId))
                  {
                     if (showFolder)
                     {
                        objectCnt++;
                        //objectMap.put("id[level]", "0," + folderCnt + "," + objectCnt);
                        //objectMap.put("level", "2");
                     }
                     objectMap.put("RowEditable", "readonly");
                     objectMap.put("Root Node", "false");

                     uniqueIds.add(uniqueId);
                     childData.add(objectMap);
                  }
               }

               // If expand folders are to be shown, add the children to the folder...
               if (showFolder)
               {
                  Map folderMap = new HashMap();
                  folderCnt++;

                  folderMap.put(SELECT_ID, objectId);
                  //folderMap.put(SELECT_RELATIONSHIP_ID, "0.0.0." + folderCnt);
                  //folderMap.put(SELECT_RELATIONSHIP_TYPE, sectionName);
                  //folderMap.put(KEY_RELATIONSHIP, sectionName);
                  folderMap.put(SELECT_CURRENT, "[" + outList.size() + "]");
                  //folderMap.put("id[level]", "0," + folderCnt);
                  folderMap.put(SELECT_LEVEL, "0");
                  folderMap.put(SELECT_TYPE, "");
                  folderMap.put(SELECT_NAME, sectionName);
                  folderMap.put(SELECT_REVISION, "");
                  folderMap.put(SELECT_DESCRIPTION, sectionDesc);
                  folderMap.put("RowEditable", "readonly");
                  folderMap.put("Root Node", "true");
                  folderMap.put("child", childData);
                  tableData.add(folderMap);
               }
//               else
//               {
                  tableData.addAll(childData);
//               }
            }
         }
         catch (Throwable e)
         {
            e.printStackTrace();
         }

         sectionCnt++;
         sectionKey = "emxRequirements.FullTraceability." + objectType + ".ExpandSet" + sectionCnt;
         resourceKey = getSettingProperty(context, sectionKey);
      }

      //SpecificationStructure.printIndentedList("Tracability Full Data:", tableData);
      return(tableData);
   }

   private static String getSettingProperty(Context context, String settingKey)
   {
      String settingVal = null;
      try
      {
          settingVal = EnoviaResourceBundle.getProperty(context, settingKey);
      }
      catch (Exception e)
      {
         // Missing property - do nothing
      }
      return(settingVal);
   }

   private static String getStringResource(Context context, String resourceKey)
   {
      String resourceVal = "";
      try
      {
         resourceVal = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), resourceKey); 
      }
      catch (Exception e)
      {
         // Missing resource - do nothing
      }
      return(resourceVal);
   }


   /**
    *  Get objects for Traceability Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args      The packed programMap arguments.
    *  @return MapList  The related object maps, as found be the expand method/args
    *  @throws Exception if the operation fails
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getTraceabilityTableData(Context context, String[] args)
      throws Exception
   {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);

      // Expand the input objects and fill the table data...
      tableData = getTraceabilityTableData(context, programMap);

      return(tableData);
   }

   private MapList getTraceabilityTableData(Context context, HashMap paramMap)
      throws Exception
   {
      MapList outList = new MapList();

      //Get info for effectivity filtering:
      String  effectivityFilter = (String) paramMap.get("CFFExpressionFilterInput_OID");
      //BPS regression, temp fix
      if("undefined".equalsIgnoreCase(effectivityFilter)){
     	 effectivityFilter = null;
      }
      
	  boolean activateEffectivity = (effectivityFilter != null && effectivityFilter.length() != 0);
	  String selectedObj = (String) paramMap.get("selectObj");
      // Get the relationships to use for traceability report
      String reportRels = (String)paramMap.get("reportRelationships");
      String extReportRels = convertInternalNames(context, reportRels);

       // Get direction to use for relationship (To, From, Both)
       boolean reportTo = false;
       boolean reportFrom = true;
       String strLang = context.getSession().getLanguage();
       String strTo = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.TraceabilityReport.Direction[To]"); 
       String strFrom = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.TraceabilityReport.Direction[From]"); 
       String relationship = ReqSchemaUtil.getSpecStructureRelationship(context); // Default
       if(selectedObj!=null){
    	   relationship += ","+ReqSchemaUtil.getDerivedRequirementRelationship(context)+"" +
    	   					","+ReqSchemaUtil.getSubRequirementRelationship(context);
    	   
       }
       String expandRelJPO = (String) paramMap.get("expandRelJPO");
       if(expandRelJPO != null)
       {
    	   String[] jpo = expandRelJPO.split("[:]");
    	   relationship = (String)JPO.invoke(context, jpo[0], null, jpo[1], null, String.class);
       }

      String reportDir = (String) paramMap.get("reportDirection");
      if (reportDir != null)
      {
         if (reportDir.equals("Both"))
         {
            reportTo = true;
            reportFrom = true;
         }
         else if (reportDir.equals("To"))
         {
            reportTo = true;
            reportFrom = false;
         }else if (reportDir.equals("From"))
         {
             reportTo = false;
             reportFrom = true;
          }
      }

      // Get the target object types/subtypes to be returned
      String reportTypes = (String) paramMap.get("reportTypes");
      String extReportTypes = convertInternalNames(context, reportTypes);
      if (reportTypes != null && reportTypes.length() > 0)
      {
         String[] splitTypes = reportTypes.split(",");
         for (int ii = 0; ii < splitTypes.length; ii++)
         {
            String reportType = convertInternalNames(context, splitTypes[ii]);
            StringList childTypes = ReqSchemaUtil.getDerivedTypesFromCache(reportType); //UINavigatorUtil.getChildrenTypeFromCache(reportType, false);
            for (int jj = 0; jj < childTypes.size(); jj++)
            {
               extReportTypes += "," + childTypes.elementAt(jj);
            }
         }
         //System.out.println(reportTypes + " => " + extReportTypes);
      }

      // Get the number of levels to be expanded
      String expandLevel = (String) paramMap.get("expandLevel");
      int maxLevels = (expandLevel == null || expandLevel.equals("")? 1: Integer.decode(expandLevel));

      // Get option to show or hide objects with no relationship
      String sShowNoRel = (String) paramMap.get("reportShowNoRel");
      boolean showObjectsWithNoRels = (sShowNoRel == null || sShowNoRel.equals("") || sShowNoRel.equalsIgnoreCase("TRUE") );

       final String SELECT_IS_KIND_OF_REQUIREMENT = "type.kindof[" + ReqSchemaUtil.getRequirementType(context) + "]";
       final String SELECT_IS_KIND_OF_CHAPTER = "type.kindof[" + ReqSchemaUtil.getChapterType(context) + "]";
      //object selects for spec
      StringList expandSelects = new StringList();
      expandSelects.addElement(SELECT_ID);
      expandSelects.addElement(SELECT_TYPE);
       expandSelects.addElement(SELECT_IS_KIND_OF_REQUIREMENT);
       expandSelects.addElement(SELECT_IS_KIND_OF_CHAPTER);
      String expandTypes = ReqSchemaUtil.getRequirementType(context) + "," + ReqSchemaUtil.getChapterType(context);

       String targertSpecObjs = (String) paramMap.get("baselineObject");
       String sourceObj = (String) paramMap.get("reportObjects");
       String targetchapterreqObjs = (String) paramMap.get("targetChapterRequirement");
      String selectType = (String) paramMap.get("selectedType");
      String indentView = (String) paramMap.get("isIndentedView");
      boolean isIndented = (indentView != null && indentView.equalsIgnoreCase("true"));

      //Added:26-May-09:kyp:V6R2010x:RMT Requirements Allocation Report
      boolean includeParentInfo = "true".equalsIgnoreCase((String) paramMap.get("includeParentInfo"));
      //End:V6R2010x:RMT Requirements Allocation Report

      // Full Traceability is requested for the current object:
      if (sourceObj == null)
      {
    	  sourceObj = (String) paramMap.get("objectId");

    	  if (sourceObj == null)
    		  return(outList);
      }
      StringTokenizer toker = null;
      StringTokenizer tokerS = null;
      tokerS = new StringTokenizer(sourceObj, ",", false);

      Vector vReqList1 = new Vector();
      Vector vReqList2 = new Vector();
      //////////////////////////////////////
      //
      // Finding all the requirements from the
      // target scope
      //
      //////////////////////////////////////

      // If Target Chapter & Requirement is selected
      String strTargetIdsToExpand = "";
      if( !(targetchapterreqObjs == null || "null".equals(targetchapterreqObjs) || targetchapterreqObjs.equals("")) )
      {
    	  strTargetIdsToExpand = targetchapterreqObjs;
      }
      else if (!(targertSpecObjs == null || "null".equals(targertSpecObjs) || targertSpecObjs.equals("")) ) {
    	  strTargetIdsToExpand = targertSpecObjs;
      }

      toker = new StringTokenizer(strTargetIdsToExpand, ",", false);
      if (toker.countTokens() > 0)
      {
    	  // Get all requirements contained in the selected specs
    	  while (toker.hasMoreTokens())
    	  {
    		  String specId = toker.nextToken();

    		  DomainObject doSpec = DomainObject.newInstance(context,specId);
    		  if (doSpec.isKindOf(context, ReqSchemaUtil.getRequirementType(context))) {
    			  if (!vReqList2.contains(specId)) {
    				  vReqList2.add(specId);
    			  }
    		  }
    		  else {
    			  MapList  mapObjsOfSpec = doSpec.getRelatedObjects(context, relationship,
    					  // expandTypes, // If there is no Requirement present at target Selection.
    					  QUERY_WILDCARD,
    					  expandSelects, null, false, true, (short)0, null, null);
    			  for (int ii = 0; ii < mapObjsOfSpec.size(); ii++)
    			  {
    				  Map mapObj = (Map) mapObjsOfSpec.get(ii);
    				  String strObjId = (String) mapObj.get(SELECT_ID);
    				  //boolean isChapter = "TRUE".equalsIgnoreCase((String) mapObj.get(SELECT_IS_KIND_OF_CHAPTER));
    				  //if (!isChapter && !vReqList2.contains(strObjId)) {
    				  vReqList2.add(strObjId);
    				  //}
    			  }
    		  }
    	  }
      }

      //////////////////////////////////////
      //
      // Finding all the requirements from the
      // source scope
      //
      //////////////////////////////////////

      if (selectType == null||selectType.equals("Specification")||selectType.equals("Requirement"))
      {

    	  //*******************

    	  if (sourceObj == null || sourceObj.equals(""))
    	  {
    		  // Use the list of Requirements found in the selected specs
    		  vReqList1 = vReqList2;
    		  vReqList2 = new Vector();
    	  }
    	  else
    	  {
    		  // If single & multiple Specification is selected.
    		  // Source ID's
    		  StringTokenizer tokerSource = new StringTokenizer(sourceObj, ",", false);
    		  if (tokerSource.countTokens() > 0)
    		  {
    			  // Get all requirements contained in the selected specs
    			  while (tokerSource.hasMoreTokens())
    			  {
    				  String objId = tokerSource.nextToken();

    				  DomainObject doBaseSpec = DomainObject.newInstance(context,objId);
    				  boolean isRequirement = doBaseSpec.isKindOf(context, ReqSchemaUtil.getRequirementType(context));
    				  StringList relSelects = new StringList();
    				  relSelects.addElement("attribute[" + RequirementsUtil.getSequenceOrderAttribute(context) + "]");
    				  MapList mapObjsOfBaseSpec = null;
    				  if(activateEffectivity)
    				  {
    					  mapObjsOfBaseSpec = doBaseSpec.getRelatedObjects(context,
    							  relationship,     					 // relationship pattern
    							  expandTypes,                     	 // object pattern
    							  expandSelects,                       // object selects
    							  relSelects,                 		 // relationship selects
    							  false,                             // to direction
    							  true,                              // from direction
    							  (short) 0,                 		 // recursion level
    							  null,                              // object where clause
    							  null,                              // relationship where clause
    							  (short)0,                        	 // limit
    							  CHECK_HIDDEN,            			 // check hidden
    							  PREVENT_DUPLICATES,   			 // prevent duplicates
    							  PAGE_SIZE,                   		 // pagesize
    							  null,                              // includeType
    							  null,                              // includeRelationship
    							  null,                              // includeMap
    							  null,                              // relKeyPrefix
    							  effectivityFilter);            // Effectivity filter expression from the SB toolbar
    				  }
    				  else
    				  {
    					  short expand = 0;
    					  expandLevel= expandLevel==null?"1":expandLevel;
    					  if( "true".equalsIgnoreCase((String)paramMap.get("matrix"))){
    						  relationship += ','+ReqSchemaUtil.getDerivedRequirementRelationship(context);
    						  expand = (short)Integer.parseInt(expandLevel);
    					  }

    					  if(expandLevel!=null&&!expandLevel.equalsIgnoreCase("-1")){
    						  mapObjsOfBaseSpec = doBaseSpec.getRelatedObjects(context, relationship ,
    								  expandTypes, expandSelects, relSelects, false, true, expand, null, null);
    					  }
    				  }
    				  if(mapObjsOfBaseSpec!=null&&!mapObjsOfBaseSpec.isEmpty()){
    					  mapObjsOfBaseSpec.sortStructure(context, "relationship,attribute[Sequence Order]", ",", "string,integer", ",");
    				  }
    				  if(selectType == null||selectType.equals("Requirement")){
    					  vReqList1.add(objId);
    				  }
    				  if(mapObjsOfBaseSpec!=null){
    					  for (int ii = 0; ii < mapObjsOfBaseSpec.size(); ii++)
    					  {
    						  Map mapObj = (Map) mapObjsOfBaseSpec.get(ii);
    						  String strObjId = (String) mapObj.get(SELECT_ID);
    						  boolean isChapter = "TRUE".equalsIgnoreCase((String) mapObj.get(SELECT_IS_KIND_OF_CHAPTER));

    						  if (!isChapter && !vReqList1.contains(strObjId)) {
    							  vReqList1.add(strObjId);
    						  }
    					  }
    				  }
    			  }//while
    		  }
    	  }
      }
      else if (selectType.equals("Product"))
      {
    	  //get all linked Product Requirements
    	  while (tokerS.hasMoreTokens())
    	  {
    		  String prodId = tokerS.nextToken();

    		  DomainObject prodObj = DomainObject.newInstance(context, prodId);
    		  MapList reqList = prodObj.getRelatedObjects(context, "Product Requirement",
    				  expandTypes, expandSelects, null, false, true, (short)0, null, null);

    		  for(int ii = 0; ii < reqList.size(); ii++)
    		  {
    			  Map reqObj = (Map) reqList.get(ii);
    			  String objId = (String) reqObj.get(SELECT_ID);
    			  vReqList1.add(objId);
    		  }
    	  }
      }
      StringList objSelects = new StringList();
      objSelects.addElement(SELECT_ID);
      objSelects.addElement(SELECT_TYPE);
      objSelects.addElement(SELECT_NAME);
      objSelects.addElement(SELECT_REVISION);
      objSelects.addElement(SELECT_OWNER);
      objSelects.addElement(SELECT_CURRENT);
      objSelects.addElement(SELECT_DESCRIPTION);
      objSelects.addElement(SELECT_POLICY);
      objSelects.addElement("attribute[" + ReqSchemaUtil.getStatusAttribute(context) + "]");
      objSelects.addElement("attribute[" + ReqSchemaUtil.getValidationStatusAttribute(context) + "]");
      objSelects.addElement("attribute[" + ReqSchemaUtil.getNotesAttribute(context) + "]");
      objSelects.addElement("attribute[Marketing Name]");
      objSelects.addElement("attribute[" + ReqSchemaUtil.getTitleAttribute(context) + "]");  // Added in R210

      StringList relSelects = new StringList();
      relSelects.addElement(SELECT_RELATIONSHIP_ID);
      relSelects.addElement(SELECT_RELATIONSHIP_TYPE);
      if (reportFrom)
      {
         relSelects.addElement(SELECT_TO_ID);
         relSelects.addElement(SELECT_TO_TYPE);
         relSelects.addElement(SELECT_TO_NAME);
         relSelects.addElement(SELECT_TO_REVISION);
         relSelects.addElement("to.policy");

         //Added:26-May-09:kyp:V6R2010x:RMT Requirements Allocation Report
         if (includeParentInfo && !reportTo)
         {
             relSelects.addElement(SELECT_FROM_ID);
             relSelects.addElement(SELECT_FROM_TYPE);
             relSelects.addElement(SELECT_FROM_NAME);
             relSelects.addElement(SELECT_FROM_REVISION);
             relSelects.addElement("from.policy");
         }
         //End:V6R2010x:RMT Requirements Allocation Report
      }
      if (reportTo)
      {
         relSelects.addElement(SELECT_FROM_ID);
         relSelects.addElement(SELECT_FROM_TYPE);
         relSelects.addElement(SELECT_FROM_NAME);
         relSelects.addElement(SELECT_FROM_REVISION);
         relSelects.addElement("from.policy");

       //Added:26-May-09:kyp:V6R2010x:RMT Requirements Allocation Report
         if (includeParentInfo && !reportFrom)
         {
             relSelects.addElement(SELECT_TO_ID);
             relSelects.addElement(SELECT_TO_TYPE);
             relSelects.addElement(SELECT_TO_NAME);
             relSelects.addElement(SELECT_TO_REVISION);
             relSelects.addElement("to.policy");
         }
         //End:V6R2010x:RMT Requirements Allocation Report
      }

      relSelects.addElement("attribute[" + ReqSchemaUtil.getLinkStatusAttrubite(context) + "]");

      // Finally, put all the rows of object data into a MapList...
      for (int jj = 0; jj < vReqList1.size(); jj++)
      {
         String thisReqId = (String)vReqList1.elementAt(jj);
         DomainObject domObject = DomainObject.newInstance(context,thisReqId);
         String thisReqRev = domObject.getRevision();
         String thisCurrent = domObject.getInfo(context, SELECT_CURRENT);
         String thisPolicy = domObject.getInfo(context, SELECT_POLICY);

        // MapList relObjects = domObject.getRelatedObjects(context, extReportRels, QUERY_WILDCARD,
                  //                                        reportTo, reportFrom, maxLevels, objSelects, relSelects,
                 //                                        null, null, null, extReportTypes, null);
         short expand = 0;
         if(extReportRels.contains(ReqSchemaUtil.getCloneRelationship(context))||selectedObj!=null||extReportRels.contains(ReqSchemaUtil.getDerivedRequirementRelationship(context))){
        	 expand = 1;
         }
         if(selectedObj==null ||thisReqId.equalsIgnoreCase(selectedObj)){      	 
	         MapList relObjects = domObject.getRelatedObjects(context,
	        		 extReportRels,     					 // relationship pattern
		    		    QUERY_WILDCARD,                     	 // object pattern
		    		    objSelects,                       // object selects
		    		    relSelects,                 		 // relationship selects
		    		    reportTo,                             // to direction
		    			reportFrom,                              // from direction
		    			expand,                 		 // recursion level
					  null,                              // object where clause
					  null,                              // relationship where clause
					  (short)0,                        	 // limit
					  CHECK_HIDDEN,            			 // check hidden
					  true,   			 // prevent duplicates
					  PAGE_SIZE,                   		 // pagesize
					  new Pattern(extReportTypes),       // includeType
					  null,                              // includeRelationship
					  null,                              // includeMap
					  null,                              // relKeyPrefix
					  effectivityFilter);            // Effectivity filter expression from the SB toolbar
	         int relObjCount = relObjects.size();
	         if (relObjCount < 1)
	         {
	            /*if (showObjectsWithNoRels)
	            {
	               // no relationships found, so return empty map
	               Map tempMap = new HashMap();
	
	               tempMap.put(SELECT_ID, thisReqId);
	               tempMap.put(SELECT_CURRENT, thisCurrent);
	               tempMap.put(SELECT_POLICY, thisPolicy);
	               tempMap.put(SELECT_LEVEL, "1"); // Sort-by-Name fails without this?!?
	               outList.add(tempMap);
	            }*/
	         }
	         else
	         {
	            MapList relList = new MapList();
	            for (int kk = 0; kk < relObjCount; kk++)
	            {
	               Map mapT2 = (Map) relObjects.get(kk);
	               String relObjId = (String) mapT2.get(SELECT_ID);
	               String relFromId = (String) mapT2.get(SELECT_FROM_ID);
	               String relCurrent = (String) mapT2.get(SELECT_CURRENT);
	               String relPolicy = (String) mapT2.get(SELECT_POLICY);
	               if (isIndented)
	               {
	                  if (relFromId == null || thisReqId.equals(relFromId))
	                     mapT2.put(EXPAND_RELATIONSHIP_DIRECTION, strTo);
	                  else
	                     mapT2.put(EXPAND_RELATIONSHIP_DIRECTION, strFrom);
	
	                  relList.add(mapT2);
	               }
	               else if (vReqList2.size() == 0 || vReqList2.contains(relObjId))
	               //else if (vReqList2.contains(relObjId))
	               {
	                  mapT2.put(SELECT_ID, thisReqId);
	                  if (relFromId == null || thisReqId.equals(relFromId))
	                  {
	                     mapT2.put(EXPAND_RELATIONSHIP_DIRECTION, strTo);
	                     mapT2.put("target.id", mapT2.get(SELECT_TO_ID));
	                     mapT2.put("target.type", mapT2.get(SELECT_TO_TYPE));
	                     mapT2.put("target.name", mapT2.get(SELECT_TO_NAME));
	                     mapT2.put("target.revision", mapT2.get(SELECT_TO_REVISION));
	                     mapT2.put("target.policy", mapT2.get("to.policy"));
	                  }
	                  else
	                  {
	                     mapT2.put(EXPAND_RELATIONSHIP_DIRECTION, strFrom);
	                     mapT2.put("target.id", mapT2.get(SELECT_FROM_ID));
	                     mapT2.put("target.type", mapT2.get(SELECT_FROM_TYPE));
	                     mapT2.put("target.name", mapT2.get(SELECT_FROM_NAME));
	                     mapT2.put("target.revision", mapT2.get(SELECT_FROM_REVISION));
	                     mapT2.put("target.policy", mapT2.get("from.policy"));
	                  }
	
	                  // Rename the related object 'current' state to 'target.current'...
	                  mapT2.put(SELECT_CURRENT, thisCurrent);
	                  mapT2.put(SELECT_POLICY, thisPolicy);
	                  mapT2.put("target.current", relCurrent);
	                  mapT2.put("target.policy", relPolicy);
	                  // Start:OEP:V6R2010:Bug 370473
	                  mapT2.put(SELECT_LEVEL,"1"); // Sort-by-Name fails without this
	                  // END:OEP:V6R2010:Bug 370473
	                relList.add(mapT2);
	               }
	            }        
	            
	            // If baseline requirement was not found in other specs, return empty map
	            if (relList.size() == 0)
	            {
	               Map tempMap = new HashMap();
	
	               tempMap.put(SELECT_ID, thisReqId);
	               tempMap.put(SELECT_REVISION, thisReqRev);
	               tempMap.put(SELECT_LEVEL, "1"); // Sort-by-Name fails without this
	               outList.add(tempMap);
	            }
	            else
	            {
	               outList.addAll(relList);
	            }
	         }
         }
      }
      //SpecificationStructure.printIndentedList("Tracability Table Data:", outList);
      //outList = processForFinalPresentation(context, outList, SELECT_TO_ID, SELECT_FROM_ID, 1);
      return(outList);
   }

   private String convertInternalNames(Context context, String names) throws Exception
   {
      StringList nameList = (names == null? new StringList(): FrameworkUtil.splitString(names, ","));
      String outNames = "";

      for (int ii = 0; ii < nameList.size(); ii++)
      {
         if (ii > 0)
            outNames += ",";

         //  get internal name if symbolic is passed
         String intName = (String) nameList.get(ii);
         String extName = PropertyUtil.getSchemaProperty(context, intName);

         if (extName.equals(""))
            outNames += intName;
         else
            outNames += extName;
      }
      return(outNames);
   }


   /**
    * Method shows icon for the Status of the Relationship between 2 Requirements.
    * @param context - the eMatrix <code>Context</code> object
    * @param args - holds the objectList of row ids
    * @return List - returns the program HTML output
    * @throws Exception if the operation fails
    */
   public List getTraceabilityStatusIcon(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);

      HashMap columnMap = (HashMap) programMap.get("columnMap");
      String attrStatus = (String) (columnMap.containsKey("expression_businessobject")?
                                    columnMap.get("expression_businessobject"):
                                    columnMap.get("expression_relationship"));

      MapList theData = (programMap.containsKey("objectList")? (MapList) programMap.get("objectList"): tableData);
      List statusIcons = this.getTableColumnData(context, theData, attrStatus);
      for (int kk = 0; kk < statusIcons.size(); kk++)
      {
         String relStat = (String) statusIcons.get(kk);

         // Skip any cells that contain no value...
         if (relStat == null || relStat.length() == 0)
            continue;

         // Remove any whitespace before looking up the color property...
         String strStat = relStat.replaceAll(" ", "");
         String strIcon = getSettingProperty(context, "emxRequirements.TraceabilityReport.Status[" + strStat + "].Icon");

         // Don't bother rendering html unless we got an icon from the properties file...
         if (strIcon != null)
         {
            relStat = "<img title=\"" + relStat + "\" src=\"../common/images/" + strIcon + "\" border=\"0\"  align=\"middle\"" + " /> ";
            statusIcons.set(kk, relStat);
         }
      }
      return(statusIcons);
   }


   /**
    * Method shows the State of the Requirement with a colored background.
    * @param context    The eMatrix <code>Context</code> object
    * @param args       The packed programArgs list
    * @return List - returns the program HTML output
    * @throws Exception if the operation fails
    */
   public List getTraceabilityStateColor(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);
      return(getExpressionColor(context, programMap, "State"));
   }

   /**
    * Method shows Status of the Relationship between 2 Requirements.
    * @param context    The eMatrix <code>Context</code> object
    * @param args       The packed programArgs list
    * @return List - returns the program HTML output
    * @throws Exception if the operation fails
    */
   public List getTraceabilityStatusColor(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);
      return(getExpressionColor(context, programMap, "Status"));
   }
   
   private void getlinkStatusValuesFromArrayList(Context context, String expValue, ArrayList<String> idList, Vector linkStatusValues) throws MatrixException{
	   StringList lstAtt = new StringList(expValue);
	   String[] idArray = (String[]) idList.toArray(new String[idList.size()]);
	   RelationshipWithSelectList resByAtts = Relationship.getSelectRelationshipData(context, idArray, lstAtt);
	   for (int oCount = 0; oCount < resByAtts.size(); oCount++)
	   {
		   String sLinkStatusValue = resByAtts.getElement(oCount).getSelectData(expValue);
		   linkStatusValues.add(sLinkStatusValue);
	   }
   	   idList.clear();
   }
   
   private List getLinkStatuAttribute(Context context, MapList rowData, String expValue) throws Exception{
	   StringList lstAtt = new StringList(expValue);
	   Vector    linkStatusValues = new Vector();
	   String[] idArray = null;
	   ArrayList<String> idList = new ArrayList<String>();
	   int rowDatasize = rowData.size();
	   for(int i=0; i<rowDatasize;i++){
		   Map setting = (Map) rowData.get(i);
		   String id = (String)setting.get(DomainConstants.SELECT_RELATIONSHIP_ID);
		   if(id != null){
			   idList.add(id);
		   }else{
			   //a id[connection] is missing on the list so we need to get previous values and fill the missing one
			   //by an empty string
			   getlinkStatusValuesFromArrayList(context, expValue, idList, linkStatusValues);
			   //fill the vector with an empty value
			   linkStatusValues.add("");
		   }
	   }
	   if(!idList.isEmpty()){
		   getlinkStatusValuesFromArrayList(context, expValue, idList, linkStatusValues);
	   }
	   return linkStatusValues;
   }
   
   
   
   
   private List getExpressionColor(Context context, Map programMap, String keyName)
      throws Exception
   {
      // If the column setting columnType="programHTMLOutput", generate a colored div string:
      HashMap columnMap = (HashMap) programMap.get("columnMap");
      boolean isBO = columnMap.containsKey("expression_businessobject")?true:false;
      String expValue = (String)((isBO==true)?columnMap.get("expression_businessobject"):columnMap.get("expression_relationship"));
      HashMap settings = (HashMap) columnMap.get("settings");
      String columnType = (String) settings.get("Column Type");
      boolean renderHTML = "programHTMLOutput".equals(columnType);

      // Check the reportFormat param to ensure that only plain text is exported:
      HashMap paramMap = (HashMap) programMap.get("paramList");
      String reportFormat = (String) paramMap.get("reportFormat");
      boolean reportHTML = (reportFormat == null? true: "HTML".equals(reportFormat));

      // Get the list of status values for this column from the table data...
      MapList rowData = (programMap.containsKey("objectList")? (MapList) programMap.get("objectList"): tableData);
      List colData = null;
      if(isBO){
    	  colData = this.getTableColumnData(context, rowData, expValue);
      }else{
    	  colData = this.getLinkStatuAttribute(context, rowData, expValue);
      }

      String adminType = (String) settings.get("Admin Type"); //IR-092589V6R2012
      Map mTranslattion = new HashMap();

      if("current".equalsIgnoreCase(expValue)  || "target.current".equalsIgnoreCase(expValue))
      {
         adminType = "State";
      }

	    if("State".equalsIgnoreCase(adminType))
	    {
	   	    String languageStr = (String)paramMap.get("languageStr");
                      List policyData = this.getTableColumnData(context, rowData, expValue.startsWith("target") ? "target.policy" : this.SELECT_POLICY);
	    	  for(int k = 0; k < colData.size(); k++)
	    	  {
	    		  String policy = (String) policyData.get(k);
	    		  String internal = (String) colData.get(k);
	    		  String translated = UINavigatorUtil.getStateI18NString(policy, internal, languageStr);
	    		  mTranslattion.put(internal, translated);
	    	  }
	    }
      

       /*
      if("attribute[Link Status]".equalsIgnoreCase(expValue))
      {
    	adminType = "attribute_LinkStatus";
      }
      else if("attribute[Validation Status]".equalsIgnoreCase(expValue))
      {
    	adminType = "attribute_ValidationStatus";
      }
      */
      if(adminType != null && adminType.startsWith("attribute_"))
      {
   	   	  String languageStr = (String)paramMap.get("languageStr");
		   String realType = PropertyUtil.getSchemaProperty(context,adminType.trim());
   	 	  for(int k = 0; k < colData.size(); k++)
   	 	  {
   	 		  String internal = (String) colData.get(k);
   	 		  String translated = UINavigatorUtil.getMXI18NString(internal, realType, languageStr, "Range");
   	 		  mTranslattion.put(internal, translated);
   	 	  }
      }
         for (int kk = 0; kk < colData.size(); kk++)
         {
            String keyValue = (String) colData.get(kk);
            String translated = (String)mTranslattion.get(keyValue);

            // Skip any cells that contain no value...
            if (keyValue == null || keyValue.length() == 0)
               continue;
            // Render the column value as a colored div block, if configured as output html...
            if (renderHTML && reportHTML) //e.g. for traceability report
            {
              // Remove any whitespace before looking up the color property...
              String strValue = keyValue.replaceAll(" ", "");
              String strColor = getSettingProperty(context, "emxRequirements.TraceabilityReport." + keyName + "[" + strValue + "].Color");

              // Don't render colored html unless we got a color from the properties file...
              if (strColor != null && strColor.length() > 0)
              {
                 keyValue = "<span title=\"" + translated + "\" style=\"color: white; background: " + strColor + "; text-align: center; display: block; padding: .25em\">" + translated + "</span>";
              }
              else
              {
                 keyValue = "<span title=\"" + translated + "\" style=\"text-align: center; display: block; padding: .25em\">" + translated + "</span>";
              }
            }
			 //Start:T25 DJH: IR-207745V6R2014x
            else
            {
            	keyValue = translated;
            }

            colData.set(kk, keyValue);
         }
             //End:T25 DJH
      return(colData);
   }

    private String checkownerAttibuteName(Object Attribute)
    {
    	String strData=null;
    	if(Attribute ==null)
		{
			strData = null;
		}
		else
		{
			String Type = Attribute.getClass().toString();
			strData = (Type.contains("Person")?((Person)Attribute).getName():(String)Attribute);
		}
		return strData;
    }
   
    private List getTableColumnData(Context context, MapList dataList, String attrName)
   {
       return getTableColumnData(context, dataList, attrName, false, false);
   }
   
    private String getTargetPath(Context context, String targetId, MapList dataList, int targetIndex, String strData){
    	Map    targetData = (Map) dataList.get(targetIndex);
    	String path = (String)targetData.get("target.name");
    	String relId = (String)targetData.get("id[connection]");
    	try {
	    	DomainRelationship rel = DomainRelationship.newInstance(context, relId);
	    	rel.openRelationship(context);
	    	String fromId = rel.getFrom().getObjectId();
	    	rel.close(context);
	    	String strDirection = (String)targetData.get("direction");
	    	if(strDirection.equalsIgnoreCase("-->")){
		    	while(targetIndex>0){
		    		targetIndex--;
		    		Map    Data = (Map) dataList.get(targetIndex);
		    		//String strDirection = (String)Data.get("direction");
			    		String toId = (String)Data.get("to.id");
			    		if(toId!=null&&toId.equalsIgnoreCase(fromId)){
			    			String name = (String)Data.get("target.name");
			    			path = name.concat(" --> " + path);
			    			
			    			String prevRelId = (String)Data.get("id[connection]");
			    	    	DomainRelationship prevRel = DomainRelationship.newInstance(context, prevRelId);
			    	    	prevRel.openRelationship(context);
			    	    	String prevFromId = prevRel.getFrom().getObjectId();
			    	    	prevRel.close(context);
			    	    	fromId = prevFromId;
			    		}
		    	}
	    	}else{
	    		path = strData;
	    	}
    	}catch (Exception exp){
    		   exp.printStackTrace();
    	}
    	return path;
    }
    
    private List getTableColumnData(Context context, MapList dataList, String attrName, boolean highlightMissingValue, boolean isFullTraceabilityReport)
    {
    	int       rowCount = (dataList == null? 0: dataList.size());
    	Vector    colData = new Vector();
		String strColor = getSettingProperty(context, "emxRequirements.TraceabilityReport.DerivedRequirementsOnly.SpecMissing.Color");
		if (strColor == null || strColor.length() == 0) {
			strColor = "red";
		}
		String strHTML = "<div style=\"color: white; background: "+strColor+"; display: block; \">" +"&nbsp;"  + "</div>";
		String strMissingData = "";
		if (highlightMissingValue) {
			strMissingData = strHTML;
		}
		for (int ii = 0; ii < rowCount; ii++)
		{
			Map    rowData = (Map) dataList.get(ii);
			String strData = null;

			// Special handling for the compound Name/Rev column...
			String strName = (String) rowData.get("name");
			String strRev = (String) rowData.get("revision");
			if ("NameRev".equalsIgnoreCase(attrName))
				//strData = (strRev == null || strRev.length() == 0? strName: XSSUtil.encodeForHTML(context, strName + " " + strRev));
				strData = (strRev == null || strRev.length() == 0? strName:  strName + " " + strRev);
			else if (attrName != null)
			{ 	
				//Start:LX6:modified to visualize owner on clone informations
				if(attrName.equals("owner"))
				{
					Object Attribute = (Object) rowData.get(attrName);
					strData = checkownerAttibuteName(Attribute);
				}else{
					strData = (String) rowData.get(attrName);
					//Start:T25 DJH:13:06:19
					if(attrName.equalsIgnoreCase("target.name")&&(strData !=null)){
						strData = getTargetPath(context, (String)rowData.get("target.id"), dataList,ii, strData);
					}
					if(strData !=null){
					 	if(strData.equals("Sub Requirement"))
					 	{
					 		strData = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Command.RMTCreateSubRequirement");
					 	}
					 	if(strData.equals("Derived Requirement"))
					 	{
					 		strData = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Command.RMTCreateDerivedRequirement");
					 	}
					}
					//End:T25 DJH:13:06:19
					}
				//End:LX6:modified to visualize owner on clone informations
			}
			colData.add(strData == null? strMissingData: strData);
		}
		if(colData.size()==1&&isFullTraceabilityReport){
			//only the root object is present : no related objects
			colData.clear();
			if("namerev".equalsIgnoreCase(attrName)){
				colData.add("NoObjectFound");
			}else{
				colData.add("");
			}
			
		}
		//System.out.println("\n" + attrName + " => " + colData);
		return(colData);
    }


  /** Called from Table RMTTraceabilityReqWithDirectionTable for Field Traceability Report Direction[Both]
   *
   * @param context Matrix context
   * @param args JPO argument
   * @return List of objectid id
   * @throws Exception if operation fails
   */
   public List getTableDirectionColumnData(Context context, String[] args) throws Exception
   {
       Map programMap = (Map) JPO.unpackArgs(args);
       Map newProgramMap = new HashMap();

       newProgramMap.putAll(programMap);

       newProgramMap.put("highlightMissingValue", "TRUE");
       newProgramMap.put("forceNoHref", "FALSE");

       args = JPO.packArgs(newProgramMap);

       return getTableColumnData(context, args);
   }

   /**
    * Method is called from getTableDirectionColumnData. method is used to return the list of object if status
    * @param context The Matrix context
    * @param args JPO argument
    * @return list of object id's
    * @throws Exception is operation fails
    */
   public List getTableColumnData(Context context, String[] args)
		   throws Exception
		   {
	   Map programMap = (HashMap) JPO.unpackArgs(args);
	   //System.out.println("\nprogramMap:");
	   boolean highlightMissingValue = "TRUE".equalsIgnoreCase((String)programMap.get("highlightMissingValue"));
	   boolean forceNoHref = "TRUE".equalsIgnoreCase((String)programMap.get("forceNoHref"));

	   HashMap columnMap = (HashMap) programMap.get("columnMap");

	   String attName = (String) (columnMap.containsKey("expression_businessobject")?
			   columnMap.get("expression_businessobject"):
				   columnMap.get("expression_relationship"));
	   if(attName.equalsIgnoreCase("evaluate[IF(attribute[Title] != '') THEN attribute[Title] ELSE attribute[V_Name]]")){
		   	attName = "attribute[Title]";
	   }
	   // Auto-filter and Column-Sort pass the table data in an objectList param?
	   //SpecificationStructure.printIndentedList("objectList:", (MapList) programMap.get("objectList"));
	   MapList theData = (programMap.containsKey("objectList")? (MapList) programMap.get("objectList"): tableData);

	   // BUG: 365827 - Check for Root Node (with id only), and fill in the missing attributes...
	   if (theData.size() == 1)
	   {
		   Map rootMap = (Map) theData.get(0);
		   String rootNode = (String) rootMap.get("Root Node");
		   String objectId = (String) rootMap.get(SELECT_ID);
		   String rootName = (String) rootMap.get(SELECT_NAME);

		   if ("true".equals(rootNode) && rootName == null)
		   {
			   DomainObject rootObj = newInstance(context, objectId);
			   // Added in R210
			   String strTitleQuery = "print bus $1 select $2 dump $3";
			   String strTitleResult = MqlUtil.mqlCommand(context, strTitleQuery, objectId, "attribute[Title]", "|");
			   //End
			   if (rootObj.openObject(context))
			   {
				   // ++ ZUD IR-234701 ++
				   rootMap.put(SELECT_POLICY, rootObj.getInfo(context, SELECT_POLICY));
				   //-- ZUD IR-234701 --
				   rootMap.put(SELECT_TYPE, rootObj.getTypeName());
				   rootMap.put(SELECT_NAME, rootObj.getName());
				   rootMap.put(SELECT_REVISION, rootObj.getRevision());
				   rootMap.put(SELECT_DESCRIPTION, rootObj.getDescription(context));
				   rootMap.put(SELECT_OWNER, rootObj.getOwner(context));
				   rootMap.put(SELECT_STATUS, rootObj.getInfo(context, SELECT_STATUS));
				   rootMap.put(SELECT_CURRENT, rootObj.getInfo(context,SELECT_CURRENT));
				   rootMap.put("attribute[Title]",  strTitleResult); // Added in R210
				   rootObj.close(context);
			   }
		   }
	   }

	   HashMap settings = (HashMap) columnMap.get("settings");
	   String columnType = (String) settings.get("Column Type");
	   boolean renderHTML = "programHTMLOutput".equals(columnType);

	   // Check the reportFormat param to ensure that only plain text is exported:
	   HashMap paramMap = (HashMap) programMap.get("paramList");
	   boolean isFullTraceabilityReport =  "true".equalsIgnoreCase((String) paramMap.get(IS_FULL_TRACEABILITY_REPORT))?true:false;
	   String reportFormat = (String) paramMap.get("reportFormat");
	   boolean reportHTML = (reportFormat == null || "HTML".equals(reportFormat));
	   boolean hyperLink = (reportFormat == null);

	   // Check for Type/Alternate Icon settings, to prepend to the name...
	   String typeIcon = (String) settings.get("Show Type Icon");
	   String altIcon = (String) settings.get("Show Alternate Icon");
	   String altOIDex = (String) settings.get("Alternate OID expression");
	   String altTypex = (String) settings.get("Alternate Type expression");
	   String targetLoc = (String) settings.get("Target Location");
	   //JX5 : retrieve Icon settings
	   String iconFunction = (String) settings.get("Type Icon Function");
	   String iconProgram = (String) settings.get("Type Icon Program");
	   //
	   boolean showIcon = ("true".equalsIgnoreCase(typeIcon));
	   boolean saltIcon = ("true".equalsIgnoreCase(altIcon));
	   boolean inPopup = ("popup".equalsIgnoreCase(targetLoc));



	   List attList = getTableColumnData(context, theData, attName, highlightMissingValue, isFullTraceabilityReport);

	   String adminType = (String) settings.get("Admin Type"); //IR-092589V6R2012
	   //for full traceability
	   if(adminType == null)
	   {
		   if("type".equalsIgnoreCase(attName))
		   {
			   adminType = "Type";
		   }
		   else if("current".equalsIgnoreCase(attName))
		   {
			   adminType = "State";
		   }

		   String languageStr = (String)paramMap.get("languageStr");
		   if("Type".equalsIgnoreCase(adminType))
		   {
			   for(int k = 0; k < attList.size(); k++)
			   {
				   String translated = UINavigatorUtil.getAdminI18NString(adminType, (String)attList.get(k),languageStr);
				   attList.set(k, translated);
			   }
		   }
		   if("State".equalsIgnoreCase(adminType))
		   {
			   List policyData = this.getTableColumnData(context, theData, attName.startsWith("target") ? "target.policy" : this.SELECT_POLICY);
			   for(int k = 0; k < attList.size(); k++)
			   {
				   String policy = (String) policyData.get(k);
				   String internal = (String) attList.get(k);
				   String translated = UINavigatorUtil.getStateI18NString(policy, internal, languageStr);
				   attList.set(k, translated);
			   }
		   }
	   }

	   /*
  if("attribute[Link Status]".equalsIgnoreCase(attName))
  {
	adminType = "attribute_LinkStatus";
  }
  else if("attribute[Validation Status]".equalsIgnoreCase(attName))
  {
	adminType = "attribute_ValidationStatus";
  }
	    */
	   if(adminType != null && adminType.startsWith("attribute_"))
	   {
		   String languageStr = (String)paramMap.get("languageStr");
		   String realType = PropertyUtil.getSchemaProperty(context,adminType.trim());
		   for(int k = 0; k < attList.size(); k++)
		   {
			   String translated = UINavigatorUtil.getMXI18NString((String)attList.get(k), realType, languageStr, "Range");
			   attList.set(k, translated);
		   }
	   }

	   if (renderHTML && reportHTML && !forceNoHref)
	   {
		   String sId = (altOIDex==null||altOIDex.isEmpty()?SELECT_ID:altOIDex);
		   List iconOIDs 	= getTableColumnData(context, theData, sId);
		   String stype = (altTypex==null||altTypex.isEmpty()?SELECT_TYPE:altTypex);
		   List iconTypes = getTableColumnData(context, theData,stype);
		   //JX5 : get list  of relationship ids
		   List iconRels 	= getTableColumnData(context,theData,(saltIcon?altTypex: SELECT_RELATIONSHIP_ID));
		   //
		   List toolTips = getTableColumnData(context, theData, "attribute[Marketing Name]");
		   List lstLinkStatus = getTableColumnData(context, theData, "attribute[Link Status]");

		   String htmlLink = "";

		   for (int ii = 0; ii < iconOIDs.size(); ii++)
		   {
			   String iconOID 	= (String) iconOIDs.get(ii);
			   String iconType = (String) iconTypes.get(ii);
			   //JX5 : get the relationship id 
			   String iconRel 	= (String) iconRels.get(ii);
			   //
			   String attData 	= (String) attList.get(ii);
			   String toolTip 	= (String) toolTips.get(ii);
			   String strStatusLink = (String) lstLinkStatus.get(ii);
			   String strData = null;

			   StringList lstChildren = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementType(context));
			   lstChildren.add(ReqSchemaUtil.getRequirementType(context));


			   if(attName.equalsIgnoreCase(EXPAND_RELATIONSHIP_DIRECTION))
			   {
				   if(iconType != null &&  !"null".equalsIgnoreCase(iconType)&& !"".equalsIgnoreCase(iconType))
				   {
					   if(lstChildren.contains(iconType))   // For Requirement type
					   {
						   if ("-->".equals(attData) || "<--".equals(attData))
						   {
							   strData = getIcon(attData, strStatusLink);
						   }
					   }
					   htmlLink = "<img src=\""+ strData + "\" border=\"0\"  align=\"middle\"" + " /> ";
				   }
				   else
				   {
					   htmlLink = strData;
				   }
			   }
			   else
			   {
				   //JX5
				   htmlLink = htmlNameRevLink(context, iconOID,iconRel, iconType, attData, null, toolTip, showIcon||saltIcon, hyperLink, inPopup, iconFunction, iconProgram);
			   }

			   if (htmlLink != null)
				   attList.set(ii, htmlLink);
		   }
	   }
	   //System.out.println("\n" + attName + " => " + attList);
	   return(attList);
	}

   private static String getIcon(String arrow, String status) throws Exception {
		String icon = arrow;
		if ("-->".equals(arrow))
			icon = getToDirIcon(status);
		else if ("<--".equals(arrow))
			icon = getFromDirIcon(status);

		return icon;
	}

  private static String getFromDirIcon(String statLink) {
	   if(statLink.equalsIgnoreCase("Valid"))
	   {
		return ICON_REQUIREMENT_VALID_FROM;
	   }
	   else if(statLink.equalsIgnoreCase("Invalid")){
		   return ICON_REQUIREMENT_INVALID_FROM;
	   }
	   else {
			return ICON_REQUIREMENT_SUSPECT_FROM;
	   }
	}

  private static String getToDirIcon(String statLink) {
	   if(statLink.equalsIgnoreCase("Valid"))
	   {
		return ICON_REQUIREMENT_VALID_TO;
	   }
	   else if(statLink.equalsIgnoreCase("Invalid")){
		   return ICON_REQUIREMENT_INVALID_TO;
	   }
	   else {
			return ICON_REQUIREMENT_SUSPECT_TO;
	   }
	}
   /**
    * @param objOID
    * @param objType
    * @param objName
    * @param objRev     (optional)
    * @param showIcon
    * @param hyperLink
    * @param relId
    * @param iconFunction (null is allowed)
    * @param iconProgram  (null is allowed)
    */
  private String htmlNameRevLink(Context context, String objOID,String relId, String objType, String objName, String objRev, String toolTip, boolean showIcon, boolean hyperLink, boolean inPopup, String iconFunction, String iconProgram)
  {
	  String objHtml = "";
	  String strHTMLObjectName = "";
	  String ObjectName = "";
	  if (objType != null && objName != null && objName.trim().length() > 0)
	  {
		  if("NoObjectFound".equalsIgnoreCase(objName)){
			  //emxRequirements.Message.NoObjectsFound
			  String objNotFound = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Message.NoObjectsFound");
			  strHTMLObjectName = objNotFound;
		  }else{
			  if (objType.length() == 0)
			  {
				  objHtml += "<b>" + objName + (objRev == null? "": objRev) + "</b>";
			  }
			  else
			  {
				  String aHref = "javascript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=" +
						  objOID + "', '700', '600', 'false', '" + (inPopup? "popup": "content") + "', '', '" + objType + "', false)";

				  String attIcon = null;
				  try
				  {
					  //IR-372849-3DEXPERIENCER2016x: reverse change made for IR-338073-3DEXPERIENCER2016
					  ObjectName	= objName; //UIRTEUtil.htmlEncode(context, objName);
					  //JX5 : we use the Icon Function & Icon Program
					  if(iconFunction!=null && iconProgram!=null)
					  {
						  MapList mapIconList = new MapList();
						  MapList objectList = new MapList();
						  HashMap tempMap = new HashMap();
						  HashMap programMap = new HashMap();
						  Map mapIcon = new HashMap();

						  tempMap.put("id", objOID);
						  tempMap.put("id[connection]", relId);
						  objectList.add(tempMap);
						  programMap.put("objectList", objectList);

						  mapIconList =(MapList) JPO.invoke(context,iconProgram,null,iconFunction,JPO.packArgs(programMap),MapList.class);
						  mapIcon = (Map) mapIconList.get(0);
						  attIcon = (String)mapIcon.get(objOID);
					  }
					  else
					  {
						  attIcon = UINavigatorUtil.getTypeIconProperty(context, objType);
					  }
				  }
				  catch (Exception e)
				  { 
					  // If there is an issue with invokation of iconProgram
					  attIcon = UINavigatorUtil.getTypeIconProperty(context, objType);
				  }

				  String summary = objName + (objRev == null? "": " " + objRev);
				  //            objHtml += "<table summary=\"" + summary + "\" border=\"0\">";
				  //            objHtml += "<tr>";

				  // Make sure there is an icon defined for this type...
				  if (attIcon != null && !attIcon.equals(""))
				  {
					  //objHtml += "<td rmb=\"\" rmbID=\"" + objOID + "\">";
					  //               objHtml += "<td>";
					  //               if (hyperLink && objOID != null && objOID.length() > 0)
					  //                  objHtml += "<a href=\"" + aHref + "\" class=\"object\">";
					  objHtml += "<img border=\"0\" src=\"../common/images/" + attIcon + "\" /> ";
					  //               if (hyperLink && objOID != null && objOID.length() > 0)
					  //                  objHtml += "</a>";
					  //               objHtml += "</td>";
				  }

				  //objHtml += "<td rmb=\"\" rmbID=\"" + objOID + "\">";
				  //            objHtml += "<td>";
				  try{
					  if (hyperLink && objOID != null && objOID.length() > 0)
					  {
						  objHtml += "<a href=\"" + aHref + "\" class=\"object\"";
						  if (toolTip != null && toolTip.length() > 0)
							  objHtml += " title=\"" + UIRTEUtil.htmlEncode(context, toolTip)+ "\"";
						  else
							  objHtml += " title=\"" + UIRTEUtil.htmlEncode(context, summary) + "\"";
						  objHtml += ">";
					  }
					  objHtml += "<b>" + ObjectName + (objRev == null? "":  objRev) + "</b>";
					  if (hyperLink && objOID != null && objOID.length() > 0)
						  objHtml += "</a>";
					  //            objHtml += "</td>";
					  //            objHtml += "</tr>";
					  //            objHtml += "</table>";

				  }catch(Exception e){

				  }
			  }
			  strHTMLObjectName = "<div style='display:none'>" + ObjectName + "</div>";
			  strHTMLObjectName+=objHtml;
		  }
	  }
	  return(strHTMLObjectName);
  }

   /**
    * @param objList
    * @param renderHTML
    * @return
    */
   private String htmlNameRevList(Context context, List objList, Map columnMap, String expTTip, boolean renderHTML, boolean hyperLink, boolean unique)
   {
      Collection outList = unique? new TreeSet(): new StringList();
      StringBuffer buffer = new StringBuffer();
      StringList idList = new StringList();
      HashMap settings = (HashMap) columnMap.get("settings");

      // Check for Alternate Icon settings, to prepend to the name...
      String altIcon = (String) settings.get("Show Alternate Icon");
      String altOIDex = (String) settings.get("Alternate OID expression");
      String altTypex = (String) settings.get("Alternate Type expression");
      String targetLoc = (String) settings.get("Target Location");

      boolean showIcon = ("TRUE".equalsIgnoreCase(altIcon));
      boolean inPopup = ("popup".equalsIgnoreCase(targetLoc));

      for (int jj = 0; jj < objList.size(); jj++)
      {
         Map    objMap = (Map) objList.get(jj);
         String objId = (String) objMap.get(showIcon? altOIDex: SELECT_ID);
         String objType = (String)objMap.get(showIcon? altTypex: SELECT_TYPE);
         String objName = (String) objMap.get(SELECT_NAME);
         String objRev = (String) objMap.get(SELECT_REVISION);

         String toolTip = (String) (expTTip == null? null: objMap.get(expTTip));

         // Don't add objects that are already in the list, if requested...
         if (objId != null)
         {
            if (unique && idList.contains(objId))
               continue;
            else
               idList.add(objId);
         }
         //JX5 : in this case no need of relId, iconFunction & iconProgram
         if (renderHTML)
            outList.add(htmlNameRevLink(context, objId, null, objType, objName, objRev, toolTip, showIcon, hyperLink, inPopup, null, null));
         else
            outList.add(objName + (objRev == null? "": " " + objRev));
      }

      for (Iterator links = outList.iterator(); links.hasNext();)
      {
         if (buffer.length() > 0)
            // buffer.append("\n");
        	 buffer.append(",");

         String link = (String) links.next();
         buffer.append(link);
      }

      String output = buffer.toString();
      if (renderHTML)
      {
         //Added:9-March-09:OEP:V6R2010: RMT:Bug :370461
         output = "<table>" +
                  "<tr><td>" +
                   (output.length() == 0? " ": output) +
                  "</td></tr></table>";
         //ENDED:9-March-09:OEP:V6R2010: RMT:Bug :370461
      }
      return(renderHTML && output.length() == 0? " ": output);
   }

   /**
    * @param objList The list of 'from' object ids to be iterated over, expanding each in turn.
    * @param attName An attribute to be returned by the expand method, and used as the Html tooltip.
    * @param unique If renderHtml is true, enables/disables hyperlinked html.
    * @return
    */
   private String textNameRevList(List objList, String attName, boolean unique) throws FrameworkException
   {
       return textNameRevList(objList, attName, unique, false);
   }

   /**
    * @param objList The list of 'from' object ids to be iterated over, expanding each in turn.
    * @param attName An attribute to be returned by the expand method, and used as the Html tooltip.
    * @param unique If renderHtml is true, enables/disables hyperlinked html.
    * @param isMatrixSearch Checking for Real time Mode "MATRIXSEARCH" argument
    * @return
    */
    private String textNameRevList(List objList, String attName, boolean unique, boolean isMatrixSearch) throws FrameworkException
   {

     String textOutput = "";
     String tempVariable = null;

     if(isMatrixSearch)
     {
         tempVariable = "|";
     }
     else
     {
         tempVariable = SelectConstants.cSelectDelimiter;
     }

     String textSep = ( attName == null ? tempVariable: ",");

     if(TRACEABILITY_NAME_ONLY.equalsIgnoreCase(attName))
     {
         textSep = tempVariable;
         attName = SELECT_NAME;
     }
      StringList idList = new StringList();

      for (int jj = 0; jj < objList.size(); jj++)
      {
         Map    objMap = (Map) objList.get(jj);
         String objId = (String) objMap.get(SELECT_ID);
         String objName = (String) objMap.get(SELECT_NAME);
         String objRev = (String) objMap.get(SELECT_REVISION);

         // Don't add objects that are already in the list, if requested...
         if (unique && idList.contains(objId))
            continue;
         else
            idList.add(objId);

         if (textOutput.length() > 0)
            textOutput += textSep;

         if (attName == null)
            textOutput += objName + (objRev == null? "": " " + objRev);
         else
            textOutput += objMap.get(attName);
      }
      return textOutput;
   }


   /**
    * Method shows the ParentSpec for from Req
    * @param context the eMatrix <code>Context</code> object
    * @param args - the objectId of the selected Requirement
    * @return List - returns the program HTML output
    * @throws Exception if the operation fails
    */
   public List getTraceabilityParentSpecs(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");

      HashMap columnMap = (HashMap) programMap.get("columnMap");
      String expData = (String) (columnMap.containsKey("expression_businessobject")?
                                 columnMap.get("expression_businessobject"):
                                 columnMap.get("expression_relationship"));

      HashMap settings = (HashMap) columnMap.get("settings");
      String columnType = (String) settings.get("Column Type");
      boolean programHTML = "programHTMLOutput".equals(columnType);

      // Check the reportFormat param to ensure that only plain text is exported:
      Map paramMap = (Map) programMap.get("paramList");
      String reportFormat = (String) paramMap.get("reportFormat");
      boolean reportHTML = ("HTML".equals(reportFormat));
      boolean hyperLink = (reportFormat == null);
      boolean renderHTML = (programHTML && (reportHTML || hyperLink));

      List reqList = getTableColumnData(context, objectList, expData);
      return(expandParentLinkList(context, reqList, null, columnMap, ReqSchemaUtil.getSpecStructureRelationship(context), "*Specification*", null, renderHTML, hyperLink));
   }

	/**
    * Method shows the ParentSpec for from Req
    * @param context the eMatrix <code>Context</code> object
    * @param args - the objectId of the selected Parameter
    * @return List - returns the program HTML output
    * @throws Exception if the operation fails
    */
   public List getTraceabilityParentReqs(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");

      HashMap columnMap = (HashMap) programMap.get("columnMap");
      String expData = (String) (columnMap.containsKey("expression_businessobject")?
                                 columnMap.get("expression_businessobject"):
                                 columnMap.get("expression_relationship"));

      HashMap settings = (HashMap) columnMap.get("settings");
      String columnType = (String) settings.get("Column Type");
      boolean programHTML = "programHTMLOutput".equals(columnType);

      // Check the reportFormat param to ensure that only plain text is exported:
      Map paramMap = (Map) programMap.get("paramList");
      String reportFormat = (String) paramMap.get("reportFormat");
      boolean reportHTML = ("HTML".equals(reportFormat));
      boolean hyperLink = (reportFormat == null);
      boolean renderHTML = (programHTML && (reportHTML || hyperLink));

      List reqList = getTableColumnData(context, objectList, expData);
      //JX5 : add ParameterAggregation to the reltypes
      String relTypes = ReqSchemaUtil.getParameterAggregationRelationship(context) + ","+ ReqSchemaUtil.getParameterUsageRelationship(context);
      return(expandParentLinkList(context, reqList, null, columnMap, relTypes, "*Requirement*", null, renderHTML, hyperLink));
   }

   /**
    * Method returns a string list of Specs that contain this Req
    * @param context the eMatrix <code>Context</code> object
    * @param args - the objectId of the selected Requirement
    * @isMatrixSearch - Passed MATRIXSEARCH String to check for Real Time mode
    * @return List - returns a '|' separated list of Specifications
    * @throws Exception if the operation fails
    */
   public String getContainedInSpecifications(Context context, String[] args)
      throws Exception
   {
      List reqList = new StringList(args[0]);
      // Added:22-Jun-09:oep:V6R2010x:RMT Enhanced Requirement Reuse
      boolean isMatrixSearch = false;
      if(args.length > 1) {
          isMatrixSearch = "MATRIXSEARCH".equalsIgnoreCase(args[1]);
      }
      // Ended:oep:V6R2010x:RMT Enhanced Requirement Reuse

      List specList = expandParentLinkList(context, reqList, null, null, ReqSchemaUtil.getSpecStructureRelationship(context), "*Specification*", TRACEABILITY_NAME_ONLY, false, false, isMatrixSearch);

      StringBuffer specBuff = new StringBuffer();
      for (Iterator specs = specList.iterator(); specs.hasNext(); )
      {
         String name = (String) specs.next();
         specBuff.append(specBuff.length() == 0? name: "|" + name);
      }
      return(specBuff.toString());
   }


   /**
    * Gets a list of Products that are linked to each of the target objects.
    * @param context    the eMatrix <code>Context</code> object.
    * @param args       the packed Program Map for the JPO method invoke.
    * @return List      the program HTML output strings for any linked Products.
    * @throws Exception if the operation fails
    */
   public List getTraceabilityParentProducts(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");

      HashMap columnMap = (HashMap) programMap.get("columnMap");
      String expData = (String) (columnMap.containsKey("expression_businessobject")?
                                 columnMap.get("expression_businessobject"):
                                 columnMap.get("expression_relationship"));

      HashMap settings = (HashMap) columnMap.get("settings");
      String columnType = (String) settings.get("Column Type");
      boolean programHTML = "programHTMLOutput".equals(columnType);

      // Check the reportFormat param to ensure that only plain text is exported:
      Map paramMap = (Map) programMap.get("paramList");
      String reportFormat = (String) paramMap.get("reportFormat");
      boolean reportHTML = ("HTML".equals(reportFormat));
      boolean hyperLink = (reportFormat == null);
      boolean renderHTML = (programHTML && (reportHTML || hyperLink));

      List featList = getTableColumnData(context, objectList, expData);
      return(expandParentLinkList(context, featList, null, columnMap, "* Features,Product Requirement,Feature List *", "*Product*", "attribute[Marketing Name]", renderHTML, hyperLink));
   }

   /**
    * Gets a list of Common Products that are linked to both the current row id and the target id.
    * @param context    the eMatrix <code>Context</code> object.
    * @param args       the packed Program Map for the JPO method invoke.
    * @return List      the program HTML output strings for any linked Products.
    * @throws Exception if the operation fails
    */
   public List getTraceabilityCommonProducts(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");

      HashMap columnMap = (HashMap) programMap.get("columnMap");
      String expData = (String) (columnMap.containsKey("expression_businessobject")?
            columnMap.get("expression_businessobject"):
               columnMap.get("expression_relationship"));

      HashMap settings = (HashMap) columnMap.get("settings");
      String columnType = (String) settings.get("Column Type");
      boolean programHTML = "programHTMLOutput".equals(columnType);

      // Check the reportFormat param to ensure that only plain text is exported:
      Map paramMap = (Map) programMap.get("paramList");
      String reportFormat = (String) paramMap.get("reportFormat");
      boolean reportHTML = ("HTML".equals(reportFormat));
      boolean hyperLink = (reportFormat == null);
      boolean renderHTML = (programHTML && (reportHTML || hyperLink));

      // For each Requirement, get a list of parent Products...
      List reqList = getTableColumnData(context, objectList, SELECT_ID);
      List incList = expandParentLinkList(context, reqList, null, null, "Product Requirement", "*Product*", SELECT_ID, false, false);
      //SpecificationStructure.printIndentedList("incList:", incList);

      // Now get a list of parent Products for each Feature, returning only those in the Req parent list...
      List featList = getTableColumnData(context, objectList, expData);
      return(expandParentLinkList(context, featList, incList, columnMap, "* Features,Feature List *", "*Product*", "attribute[Marketing Name]", renderHTML, hyperLink));
   }

   /**
    * Method returns a string list of Specs that contain this Req
    * @param context the eMatrix <code>Context</code> object
    * @param args - the objectId of the selected Requirement
    * @isMatrixSearch - Passed MATRIXSEARCH String to check for Real Time mode
    * @return List - returns a '|' separated list of Specifications
    * @throws Exception if the operation fails
    */
   public String getContainedInProducts(Context context, String[] args)
      throws Exception
   {
      List featList = new StringList(args[0]);
      // Added:22-Jun-09:oep:V6R2010x:RMT Enhanced Requirement Reuse
      boolean isMatrixSearch = false;
      if(args.length > 1) {
          isMatrixSearch = "MATRIXSEARCH".equalsIgnoreCase(args[1]);
      }
      List specList = expandParentLinkList(context, featList, null, null, "Product Requirement,Feature List *", "*Product*", TRACEABILITY_NAME_ONLY, false, false, isMatrixSearch);

      String delimeter = null;
      if(isMatrixSearch)
      {
          delimeter = "|";
      }
      else
      {
          delimeter = SelectConstants.cSelectDelimiter;
      }
      // End:oep:V6R2010x:RMT Enhanced Requirement Reuse
      StringBuffer specBuff = new StringBuffer();
      for (Iterator specs = specList.iterator(); specs.hasNext(); )
      {
         String name = (String) specs.next();
         specBuff.append(specBuff.length() == 0? name:delimeter + name);
      }
      return(specBuff.toString());
   }


   /**
    * Gets a list of Products that are linked to each of the target objects.
    * @param context    the eMatrix <code>Context</code> object.
    * @param args       the packed Program Map for the JPO method invoke.
    * @return List      the program HTML output strings for any linked Products.
    * @throws Exception if the operation fails
    */
   public List isFeatureLinkedToProduct(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList) programMap.get("objectList");

      HashMap columnMap = (HashMap) programMap.get("columnMap");
      String expData = (String) (columnMap.containsKey("expression_businessobject")?
                                 columnMap.get("expression_businessobject"):
                                 columnMap.get("expression_relationship"));

      // Get the name of the current Product, if applicable...
      Map paramMap = (Map) programMap.get("paramList");
      String objectId = (String) paramMap.get("reportObjects");
      if (objectId == null)
         return(null);

      String strLang = context.getSession().getLanguage();
      String strYes = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.TraceabilityReport.Fulfilled.Boolean[true]"); 
      String strNo = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.TraceabilityReport.Fulfilled.Boolean[false]"); 

      DomainObject prodObj = DomainObject.newInstance(context, objectId);
      prodObj.openObject(context);
      String currProd = prodObj.getName() + " " + prodObj.getRevision();
      prodObj.closeObject(context, false);

      // Get the list of Parent Products for each target Feature...
      List   featList = getTableColumnData(context, objectList, expData);
      List   prodList = expandParentLinkList(context, featList, null, columnMap, "Feature List *", "*Product*", null, false, false);
      Vector flagList = new Vector();

      // Check each target Feature to see if it's connected to the current Product...
      for (int ii = 0; ii < featList.size(); ii++)
      {
         // Return an empty string if there are no linked Features for this Requirement...
         String featId = (String) featList.get(ii);
         if (featId == null || featId.length() == 0)
         {
            flagList.add("");
            continue;
         }

         String prodNames = (String) prodList.get(ii);
         String[] prodArray = prodNames.split("\n");
         boolean isLinked = false;

         if (prodArray == null || prodArray.length == 0)
         {
            // Compare the parent Product name to the current Product name...
            isLinked = (prodNames.equals(currProd));
         }
         else
         {
            // Compare each parent Product name until a match is found with the current Product...
            for (int jj = 0; !isLinked && jj < prodArray.length; jj++)
            {
               isLinked = (prodArray[jj].equals(currProd));
            }
         }
         flagList.add(isLinked? strYes: strNo);
      }
      return(flagList);
   }

   /**
    * Function to handle chooserJPO paramter from config.xml file.
    * @param context    the eMatrix <code>Context</code> object.
    * @param args       the packed Program Map for the JPO method invoke.
    * @throws Exception if the operation fails
    * @return Map requirement objectid's
    */

   //Added:22-Jun-09:oep:V6R2010x:RMT Enhanced Requirement Reuse
   public static Map getRangeValuesForField(Context context, String args[])
   throws Exception
   {
       Map argMap = (Map)JPO.unpackArgs(args);
       String currentField = (String) argMap.get("currentField");
       Map fieldValues = (Map) argMap.get("fieldValues");
       Map requestMap = (Map) argMap.get("requestMap");
       String strObjectId = (String)requestMap.get("objectId");
       String specStructId = (String)requestMap.get("specStructId");

       StringList expandSelects = new StringList();
       expandSelects.addElement(SELECT_ID);
       expandSelects.addElement(SELECT_TYPE);
       String expandTypes = ReqSchemaUtil.getRequirementType(context);

       Map returnMap = new HashMap();
       String vaultString = QUERY_WILDCARD;
       String whereString = "";

       MapList resultsList = null;
       MapList reqList = null;

       // Call the Config file and read the select parameter
       //Config.Field field = Config.getInstance(context).indexedBOField(currentField);
       //String selectable = field.selectable;
       String selectable = "";
       if("CONTAINED_IN_PRODUCTS".equalsIgnoreCase(currentField))
       {
    	   selectable = "program[emxTraceabilityReport -method getContainedInProducts ${OBJECTID}]";
       }
       else if("CONTAINED_IN_SPECIFICATIONS".equalsIgnoreCase(currentField))
       {
    	   selectable = "program[emxTraceabilityReport -method getContainedInSpecifications ${OBJECTID}]";
       }

       // And we only want the object IDs. Here we Find all the Requirements
       SelectList resultSelects = new SelectList();
       resultSelects.add(DomainObject.SELECT_ID);
       resultSelects.add(selectable);
       resultsList = DomainObject.findObjects(context, ReqSchemaUtil.getRequirementType(context), vaultString, whereString, resultSelects);

       if(specStructId != null)
       {
           StringList sList = FrameworkUtil.split(specStructId,"|");
           if(sList.size() == 3)
           {
               strObjectId = (String) sList.get(0);
           }
           else
           {
               strObjectId = (String) sList.get(1);
           }


       }

        if (strObjectId == null) {
            reqList = DomainObject.findObjects(context, ReqSchemaUtil.getProductsType(context),
                    vaultString, whereString, resultSelects);

            // If we navigate from General Search
            for (int i = 0; i < reqList.size(); i++) {
                HashMap objectMap = (HashMap) reqList.get(i);
                String objId = (String) objectMap.get(SELECT_ID);
                for (int j = 0; j < resultsList.size(); j++) {
                    HashMap objectMap1 = (HashMap) resultsList.get(j);
                    String oid = (String) objectMap1.get(SELECT_ID);
                    if (objId.equals(oid)) {
                        resultsList.remove(j);
                        break;
                    }
                }
            }
        }
       else
       {
	   // Handled separate For loop for each context. For Bug 378038, 378045
            DomainObject prodObj = DomainObject.newInstance(context,
                    strObjectId);
            // In the context of Feature
            if(prodObj.isKindOf(context,ReqSchemaUtil.getFeatureType(context)))
            {
                String strSubRequirementReln = ReqSchemaUtil.getSubRequirementRelationship(context);
                String strFeatureRequirementReln = ReqSchemaUtil.getRequirementSatisfiedByRelationship(context);
                String strProductRequirementReln = ReqSchemaUtil.getProductRequirementRelationship(context);
                String strComma = ",";
                String strType = ReqSchemaUtil.getRequirementType(context);
                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
                StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                short sh = 1;
                String strBusWhereClause = "";
                String strRelWhereClause = DomainConstants.EMPTY_STRING;
                String strRelationship = strSubRequirementReln + strComma + strFeatureRequirementReln
                + strComma + strProductRequirementReln;

                reqList = prodObj.getRelatedObjects(context, strRelationship, strType, objectSelects,
                        relSelects, false, true, sh, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                for (int i = 0;  i < reqList.size(); i++) {
                    Hashtable objectMap = (Hashtable) reqList.get(i);
                    String objId = (String) objectMap.get(SELECT_ID);
                    for (int j = 0; j < resultsList.size(); j++) {
                        HashMap objectMap1 = (HashMap) resultsList.get(j);
                        String oid = (String) objectMap1.get(SELECT_ID);
                        // if (objId.equalsIgnoreCase(oid)) {
                        if (oid.equalsIgnoreCase(objId)) {
                            resultsList.remove(j);
                            break;
                        }
                    }
                }
            }
            // In the context of Requiremetn Specification & Chapter
            else if(prodObj.isKindOf(context,ReqSchemaUtil.getRequirementSpecificationType(context)) || prodObj.isKindOf(context,ReqSchemaUtil.getChapterType(context)))
            {
                String strRelPattern = ReqSchemaUtil.getSpecStructureRelationship(context);
                StringList lstRelSelects = new StringList(SELECT_RELATIONSHIP_ID);
                StringList lstObjSelects = new StringList(3);
                lstObjSelects.add(SELECT_ID);
                lstObjSelects.add(SELECT_RELATIONSHIP_ID);
                short sRecursionLevel = 0;
                String strBusWhereClause = "";
                String strRelWhereClause = "";
                reqList = prodObj.getRelatedObjects(context, strRelPattern, QUERY_WILDCARD,
                        lstObjSelects, null, false, true,sRecursionLevel , null, null);
              }
            // In the context of Model
            else if(prodObj.isKindOf(context,"Model")){
                String strRelName = ReqSchemaUtil.getCandidateItemRelationship(context);
                String strTypeName = ReqSchemaUtil.getFeaturesType(context);
                StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
                StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
                short sLevel = 0;
                reqList = prodObj.getRelatedObjects(context, strRelName, QUERY_WILDCARD,
                        objectSelects, relSelects, false, true, sLevel, "", "");
                for (int i = 0;  i < reqList.size(); i++) {
                    Hashtable objectMap = (Hashtable) reqList.get(i);
                    String objId = (String) objectMap.get(SELECT_ID);
                    for (int j = 0; j < resultsList.size(); j++) {
                        HashMap objectMap1 = (HashMap) resultsList.get(j);
                        String oid = (String) objectMap1.get(SELECT_ID);
                        // if (objId.equalsIgnoreCase(oid)) {
                        if (oid.equalsIgnoreCase(objId)) {
                            resultsList.remove(j);
                            break;
                        }
                    }
                }
            }
            // In the context of Product
            else
            {
                reqList = prodObj.getRelatedObjects(context, ReqSchemaUtil.getProductRequirementRelationship(context),
                    expandTypes, expandSelects, null, false, true, (short) 0,
                    null, null);
        	// If we navigate from any context Products/Specifications
        	for (int i = 0;  i < reqList.size(); i++) {
                    Hashtable objectMap = (Hashtable) reqList.get(i);
                    String objId = (String) objectMap.get(SELECT_ID);
                    for (int j = 0; j < resultsList.size(); j++) {
                        HashMap objectMap1 = (HashMap) resultsList.get(j);
                        String oid = (String) objectMap1.get(SELECT_ID);
                        // if (objId.equalsIgnoreCase(oid)) {
                        if (oid.equalsIgnoreCase(objId)) {
                            resultsList.remove(j);
                            break;
                        }
                    }
                }
            }
            // Remove the upper for Loop. Since its required only in the context of Product.
        }

       Iterator itr = resultsList.iterator();
       while(itr.hasNext()){
           HashMap objectMap = (HashMap)itr.next();
           String retKey = (String)objectMap.get(selectable);
           if (retKey == null || "".equals(retKey)) {
               continue;
           }

           StringList slProductNames = FrameworkUtil.split(retKey, SelectConstants.cSelectDelimiter);

           if(retKey.indexOf(SelectConstants.cSelectDelimiter) == -1)
           {
               slProductNames = FrameworkUtil.split(retKey, "|");
           }

           for (Iterator itrProductName = slProductNames.iterator(); itrProductName.hasNext();) {
               String strProductName = (String) itrProductName.next();
               if (!returnMap.containsKey(strProductName)) {
                   returnMap.put(strProductName, new Integer(1));
               }
               else {
                   Integer count = (Integer)returnMap.get(strProductName);
                   count = new Integer(count.intValue() + 1);
                   returnMap.put(strProductName, count);
               }
           }
       }

       // Process count of each product to form the product filter option label
       //
       for (Iterator itrProductName = returnMap.keySet().iterator(); itrProductName.hasNext();) {
           String strProductName = (String) itrProductName.next();
           Integer count = (Integer)returnMap.get(strProductName);

            returnMap.put(strProductName, strProductName + " (" + count + ")");

       }
       return returnMap;
   }
   //End:V6R2010x:RMT Enhanced Requirement Reuse
   /**
    * @param context
    * @param objList    The list of 'from' object ids to be iterated over, expanding each in turn.
    * @param incList    The possible 'to' object ids that can be returned. Each element of the list can be
    *                   a comma-separated list of ids. This list length should match the objList length.
    *                   If this arg is null, all matching 'to' ids will be returned.
    * @param columnMap  The table column settings map, so the Html can be rendered correctly (null for text)
    * @param relTypes   The relationship name(s) be be expanded (can be comma-separated)
    * @param objTypes   The object types to be expanded (can be comma-separated)
    * @param expTTip    An attribute to be returned by the expand method, and used as the Html tooltip.
    * @param renderHTML boolean arg:    True = render the 'to' objects for display in emxTable views.
    *                                   False = return a comma-separated list of 'to' object ids.
    * @param hyperLink  boolean:        If renderHtml is true, enables/disables hyperlinked html.
    * @return
    * @throws FrameworkException
    */
    private List expandParentLinkList(Context context, List objList, List incList, Map columnMap, String relTypes, String objTypes, String expTTip, boolean renderHTML, boolean hyperLink)
    throws FrameworkException
    {
        return expandParentLinkList(context, objList, incList, columnMap, relTypes, objTypes, expTTip, renderHTML, hyperLink, false);
    }

    /**
     * @param context
     * @param objList    The list of 'from' object ids to be iterated over, expanding each in turn.
     * @param incList    The possible 'to' object ids that can be returned. Each element of the list can be
     *                   a comma-separated list of ids. This list length should match the objList length.
     *                   If this arg is null, all matching 'to' ids will be returned.
     * @param columnMap  The table column settings map, so the Html can be rendered correctly (null for text)
     * @param relTypes   The relationship name(s) be be expanded (can be comma-separated)
     * @param objTypes   The object types to be expanded (can be comma-separated)
     * @param expTTip    An attribute to be returned by the expand method, and used as the Html tooltip.
     * @param renderHTML boolean arg:    True = render the 'to' objects for display in emxTable views.
     *                                   False = return a comma-separated list of 'to' object ids.
     * @param hyperLink  boolean:        If renderHtml is true, enables/disables hyperlinked html.
     * @param isMatrixSearch For Real Time mode checking the MATRIXSEARCH argument
     * @return
     * @throws FrameworkException
     */
   private List expandParentLinkList(Context context, List objList, List incList, Map columnMap, String relTypes, String objTypes, String expTTip, boolean renderHTML, boolean hyperLink, boolean isMatrixSearch)
      throws FrameworkException
   {
      Vector retList = new Vector();
      StringList objSelects = new StringList();
      objSelects.addElement(SELECT_ID);
      objSelects.addElement(SELECT_TYPE);
      objSelects.addElement(SELECT_NAME);
      objSelects.addElement(SELECT_REVISION);
      if (expTTip != null && expTTip.length() > 0 && !objSelects.contains(expTTip) && !TRACEABILITY_NAME_ONLY.equalsIgnoreCase(expTTip) )
         objSelects.addElement(expTTip);

      int incSize = incList == null? 0: incList.size();
      for (int ii = 0; ii < objList.size(); ii++)
      {
         String retString = "";
         String objId = (String) objList.get(ii);
         String incId = (incList != null && ii < incSize? (String) incList.get(ii): null);

         Map matchIds = null;
         if (incId != null)
         {
            matchIds = new HashMap();
            matchIds.put(SELECT_ID, incId);
         }

         if (objId != null && objId.length() > 0)
         {
            DomainObject domObj = DomainObject.newInstance(context, objId);
            MapList parList = domObj.getRelatedObjects(context, relTypes, QUERY_WILDCARD,
                  true, false, (short) 0, objSelects, null, EMPTY_STRING, EMPTY_STRING,
                  QUERY_WILDCARD, objTypes, matchIds);
            //SpecificationStructure.printIndentedList("\nparList:", parList);

            // Build a string list of unique Name/Rev strings (set last arg=true)
            //Added:22-Jun-09:oep:V6R2010x:RMT Enhanced Requirement Reuse
            if (columnMap == null)
                retString = textNameRevList(parList, expTTip, true, isMatrixSearch);
            //End:22-Jun-09:oep:V6R2010x:RMT Enhanced Requirement Reuse
            else
               retString = htmlNameRevList(context, parList, columnMap, expTTip, renderHTML, hyperLink, true);
         }
         retList.add(retString);
      }
      return retList;
   }

   //Added:26-May-09:kyp:V6R2010x:RMT Requirements Allocation Report

   /**
    *  Get objects for Requirement Allocation Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args      The packed programMap arguments.
    *  @return MapList  The related object maps, as found be the expand method/args
    *  @throws Exception if the operation fails
    *  @since RMT V6R2010x
    *  @author KYP
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getRequirementAllocationReportTableData(Context context, String[] args)
   throws Exception
   {
       try {
	   HashMap programMap = (HashMap)JPO.unpackArgs(args);

	   String targertSpecObjs = (String) programMap.get("baselineObject");
           String sourcespecObj = (String) programMap.get("reportObjects");

	   // Find the source spec ids
	   StringList slSourceSpecIds = new StringList();
           StringTokenizer tokenizer = new StringTokenizer(sourcespecObj, ",", false);
           while (tokenizer.hasMoreTokens())
           {
               String strSourceObjectId = tokenizer.nextToken();

               DomainObject dmoSource = DomainObject.newInstance(context,strSourceObjectId);
               boolean isSpec = dmoSource.isKindOf(context, ReqSchemaUtil.getRequirementSpecificationType(context));
               if (isSpec)
               {
        	   slSourceSpecIds.add(strSourceObjectId);
               }
           }

	   // Find the target spec ids
	   StringList slTargetSpecIds = new StringList();
	   tokenizer = new StringTokenizer(targertSpecObjs, ",", false);
           while (tokenizer.hasMoreTokens())
           {
               String strTargetObjectId = tokenizer.nextToken();

               DomainObject dmoTarget = DomainObject.newInstance(context, strTargetObjectId);
               boolean isSpec = dmoTarget.isKindOf(context, ReqSchemaUtil.getRequirementSpecificationType(context));
               if (isSpec)
               {
        	   slTargetSpecIds.add(strTargetObjectId);
               }
           }
	   String expandLevel = (String) programMap.get("expandLevel");
	   int maxLevels = (expandLevel == null || expandLevel.equals("")? 1: Integer.decode(expandLevel));

	   programMap.put("matrix", "true");
	   // Expand the input objects and fill the table data...
	   tableData = getTraceabilityTableData(context, programMap);

	   StringList slSpecSelect = new StringList();
	   slSpecSelect.addElement(DomainConstants.SELECT_NAME);
	   slSpecSelect.addElement(DomainConstants.SELECT_ID);

	   // Populate Spec information for the root requirements
	   tableData = updateWithSpecInfo(context, tableData, SELECT_ID, slSpecSelect, slSourceSpecIds, "RootParentSpecInfo");

	   // Populate Spec information for the derived requirements
	   tableData = updateWithSpecInfo(context, tableData, SELECT_TO_ID, slSpecSelect, slTargetSpecIds, "ParentSpecInfo");
	   tableData = processForFinalPresentation(context, tableData, SELECT_TO_ID, SELECT_FROM_ID, maxLevels);

	   return(tableData);
       } catch (Exception e) {
	   e.printStackTrace();
	   throw e;
       }
   }

   private MapList updateWithSpecInfo (Context context, MapList tableData, String strObjectIdSelect, StringList slSpecSelect) throws Exception
   {
       return updateWithSpecInfo (context, tableData, strObjectIdSelect, slSpecSelect, null, null);
   }
   /**
    * Updates each map in the given MapList of data, assuming they are requirement objects, with the parent requirement specification
    * object information. Assumes that key SELECT_ID in each map will be id of the root requirement in report.
    *
    * @param context The Matrix Context object
    * @param tableData The MapList containing maps, where each map has some information about the Requirement object
    * @param strObjectIdSelect The key to be used to find the object id of the Requirement object from each map of given MapList
    * @param slSpecSelect The selectable defining which information one wants to select from the map
    * @param slValidSpecIds The valid spec ids, if this value is passed then only those specs are returned which are from given values
    * @param strResultMapKeyName The key in result map against which the parent spec info maplist will be returned
    * @return The updated MapList containing specification info, new key given by strResultMapKeyName will hold the value containing as MapList
    *         of all the specifications where this request belongs to
    * @throws Exception if operation fails
    * @since RMT V6R2010x
    * @author KYP
    */
   private MapList updateWithSpecInfo (Context context, MapList tableData, String strObjectIdSelect, StringList slSpecSelect, StringList slValidSpecIds, String strResultMapKeyName) throws Exception
   {
       try
       {
	   final String SELECT_IS_KIND_OF_SPECIFICATION = "type.kindof[" + ReqSchemaUtil.getSpecificationType(context) + "]";

	   if (strResultMapKeyName == null || "".equals(strResultMapKeyName.trim()))
	   {
	       strResultMapKeyName = "ParentSpecInfo";
	   }
	   // Prepare the selectables
	   //
	   if (slSpecSelect == null)
	   {
	       slSpecSelect = new StringList();
	   }
	   if (!slSpecSelect.contains(DomainObject.SELECT_ID))
	   {
	       slSpecSelect.addElement(DomainObject.SELECT_ID);
	   }
	   if (!slSpecSelect.contains(DomainObject.SELECT_TYPE))
	   {
	       slSpecSelect.addElement(DomainObject.SELECT_TYPE);
	   }
	   if (!slSpecSelect.contains(SELECT_IS_KIND_OF_SPECIFICATION))
	   {
	       slSpecSelect.addElement(SELECT_IS_KIND_OF_SPECIFICATION);
	   }

	   StringBuffer sbTypePattern = new StringBuffer(128);
	   sbTypePattern.append(ReqSchemaUtil.getRequirementType(context))
	   		.append(",")
	   		.append(ReqSchemaUtil.getChapterType(context))
	   		.append(",")
	   		.append(ReqSchemaUtil.getCommentType(context))
	   		.append(",")
	   		.append(ReqSchemaUtil.getSpecificationType(context));
	   StringBuffer sbRelPattern = new StringBuffer(128);
	   sbRelPattern.append(ReqSchemaUtil.getSpecStructureRelationship(context))
	   		.append(",")
	   		.append(ReqSchemaUtil.getDerivedRequirementRelationship(context))
	   		.append(",")
	   		.append(ReqSchemaUtil.getSubRequirementRelationship(context));

	   // Finding specification for the requirement is costly process.
	   // A requirement can be present in the derivation hierarchy many times,
	   // so we can maintain a cache of the specification information here
	   // just to reduce some database calls
	   //
	   Map mapSpecInfoCache = new HashMap();

	   for (Iterator itrObjectInfo = tableData.iterator(); itrObjectInfo.hasNext();)
	   {
	       Map mapObjInfo = (Map) itrObjectInfo.next();
	       String strObjectId = (String)mapObjInfo.get(strObjectIdSelect);
	       if (strObjectId == null)
	       {
		   continue;
	       }

	       // Consult our cache to see if the specification information is already present with us
	       //
	       MapList mlParentSpecInfo = (MapList)mapSpecInfoCache.get(strObjectId);

	       if (mlParentSpecInfo != null)
	       {
		   mapObjInfo.put(strResultMapKeyName, mlParentSpecInfo);
	       }
	       else
	       {
		   DomainObject dmoRequirement = DomainObject.newInstance(context, strObjectId);
		   MapList mlStructureObjects = dmoRequirement.getRelatedObjects(context,
                                            			   sbRelPattern.toString(),
                                            			   sbTypePattern.toString(),
                                            			   slSpecSelect,
                                            			   null,
                                            			   true,
                                            			   false,
                                            			   (short)0,
                                            			   null,
                                            			   null);
		   mlParentSpecInfo = new MapList();
		   StringList slAlreadyAddedSpec = new StringList();
		   for (Iterator itrStructureObjects = mlStructureObjects.iterator(); itrStructureObjects.hasNext();)
		   {
		       Map mapStructureObject = (Map) itrStructureObjects.next();

		       boolean isKindOfSpec = "true".equalsIgnoreCase((String)mapStructureObject.get(SELECT_IS_KIND_OF_SPECIFICATION));
		       String strSpecId = (String)mapStructureObject.get(DomainObject.SELECT_ID);

		       if (isKindOfSpec && !slAlreadyAddedSpec.contains(strSpecId))
		       {
			   mlParentSpecInfo.add(mapStructureObject);
			   slAlreadyAddedSpec.add(strSpecId);
		       }
		   }
		// Filter as per required specs
		   if (slValidSpecIds != null && slValidSpecIds.size() > 0)
		   {
		       MapList mlFilteredParentSpecInfo = new MapList();
		       for (Iterator itrParentSpecInfo = mlParentSpecInfo.iterator(); itrParentSpecInfo.hasNext();)
		       {
			   Map mapParentSpecInfo = (Map) itrParentSpecInfo.next();
			   if (slValidSpecIds.contains(mapParentSpecInfo.get(DomainConstants.SELECT_ID)))
			   {
			       mlFilteredParentSpecInfo.add(mapParentSpecInfo);
			   }
		       }

		       mlParentSpecInfo = mlFilteredParentSpecInfo;
		   }

		   mapObjInfo.put(strResultMapKeyName, mlParentSpecInfo);

		   // Update the cache
		   //
		   mapSpecInfoCache.put(strObjectId, mlParentSpecInfo);
	       }
	   }//For each requirement

	   mapSpecInfoCache = null;
       }
       catch (Exception exp)
       {
	   exp.printStackTrace();
       }
       return tableData;
   }

   /**
    * Processes the data retrieved from getTraceabilityTableData() method for presentation in Requirement Allocation Report
    *
    * The map returned from getTraceabilityTableData() method has most of the information for further processing. The key 'id'
    * points to the root requirement object, keys like from.id provides information about the parent requirement while
    * to.id is the current requirement. This method does following thing
    * 1. Each map is modified to contain DerivedRequirements key pointing to derived requirements of current requirement
    * 2. Root requirements are found from the tableData and new maps for the root requirments are created
    * 3. Map for root requirements are modified to contain DerivedRequirements key pointing to derived requirements of this root requirement
    * 4. Only the root requirement maps are remembered now and then they are converted to convenient map of horizontal tree structures
    *    Ex. Resultant maps will have keys like 0, 1, 2, etc as column indices
    *        Key 0 points to map of root requirement
    *        Key 1 points to map of first level derived requirement information
    *        Key 2 points to map of second level derived requirement information
    *    So if a req R1 has derived requirements R11,R12 and R13, and R12 has R121 and R122 in turn then the final maplist will look like
    *    map 0=R1	1=R11   2=
    *    map 0=         1=R12	2=R121
    *    map 0=         1=      2=R122
    *    map 0=         1=R13   2=
    *
    *
    * @param context The Matrix Context object
    * @param tableData The table data retrieved from getTraceabilityTableData() method
    * @param strObjectIdSelect The selectable to be used to find the id of the requirement object from the map
    * @param strParentObjectIdSelect The selectable to be used to find the id of the parent requirement object from the map
    * @param maxLevels The max level of expansion done
    * @return The processes map list useful to be used to represent data by table column methods for table "RMTTraceabilityDerivedRequirementsOnlyTable"
    * @throws Exception if operation fails.
    * @since RMT V6R2010x
    * @author KYP
    */
   private MapList processForFinalPresentation (Context context, MapList tableData, String strObjectIdSelect, String strParentObjectIdSelect, int maxLevels) throws Exception
   {
       try {
	   // Argument check
	   //
	   if (tableData == null || tableData.size() == 0)
	   {
	       return new MapList();
	   }
	   if (strObjectIdSelect == null || "".equals(strObjectIdSelect.trim())
		   || strParentObjectIdSelect == null || "".equals(strParentObjectIdSelect.trim()))
	   {
	       throw new IllegalArgumentException("ObjectIdSelect=" + strObjectIdSelect + ", ParentObjectIdSelect="+strParentObjectIdSelect);
	   }

	   // Convert to tree structure and prepare a cache so that map can be searched w.r.t ids
	   // Also pull out the root requirements.
	   //

	   Map mapReqCache = new HashMap();
	   Map mapRootReqCache = new HashMap();

	   for (Iterator itr1 = tableData.iterator(); itr1.hasNext();)
	   {
	       Map mapObj1 = (Map) itr1.next();

	       // For Root Requirement !
	       // id represents the id root requirement, find union of all the id, these are root requirements
	       //
	       String strRootReqId = (String)mapObj1.get(SELECT_ID);

	       // Cache update
	       //
	       if (!mapRootReqCache.containsKey(strRootReqId))
	       {
		   Map mapRootReq = new HashMap();

		   //TODO Optimize this!
		   //Acquire root objects information
		   //
		   DomainObject dmoRootReq = DomainObject.newInstance(context, strRootReqId);

		   StringList slRootReqSelect = new StringList();
		   slRootReqSelect.add(SELECT_NAME);
		   slRootReqSelect.add(SELECT_REVISION);
		   slRootReqSelect.add(SELECT_TYPE);

		   Map mapRootInfo = dmoRootReq.getInfo(context, slRootReqSelect);

		   mapRootReq.put(strObjectIdSelect, strRootReqId);
		   mapRootReq.put(SELECT_NAME, (String)mapRootInfo.get(SELECT_NAME));
		   mapRootReq.put(SELECT_REVISION, (String)mapRootInfo.get(SELECT_REVISION));
		   mapRootReq.put(SELECT_TYPE, (String)mapRootInfo.get(SELECT_TYPE));
		   mapRootReq.put("DerivedRequirements", new MapList());
		   mapRootReq.put("RootParentSpecInfo", mapObj1.get("RootParentSpecInfo"));

		   mapRootReqCache.put(strRootReqId, mapRootReq);
	       }

	       // Get the current requirement
	       String strCurrObjId = (String)mapObj1.get(strObjectIdSelect);

	       // Cache update
	       //
	       mapReqCache.put(strCurrObjId, mapObj1);

	       // Find Derived Requirements for this requirement
	       //
	       for (Iterator itr2 = tableData.iterator(); itr2.hasNext();)
	       {
		   Map mapObj2 = (Map) itr2.next();
		   if (mapObj1.equals(mapObj2))
		   {
		       // Do not compare to itself.
		       continue;
		   }

		   String strCurrReqId = (String)mapObj2.get(strObjectIdSelect);
		   String strCurrReqParentId = (String)mapObj2.get(strParentObjectIdSelect);
		   // If requirement is parent of someone, then that someone is its derived requirement
		   //
		   if (strCurrReqParentId != null && strCurrReqParentId.equals(strCurrObjId))
		   {
		       MapList mlDerivedRequirements = (MapList)mapObj1.get("DerivedRequirements");
		       if (mlDerivedRequirements == null)
		       {
			   mlDerivedRequirements = new MapList();
			   mapObj1.put("DerivedRequirements", mlDerivedRequirements);
		       }

		       // Add only if not added already
		       // Not able to directly use "if (!mlDerivedRequirements.contains(mapObj2))" because of some data
		       // differences in mapObj2 the contains method gives incorrect results.
		       //
		       boolean isAddedAlready = false;
		       for (Iterator itrDerReq = mlDerivedRequirements.iterator(); itrDerReq.hasNext();) {
			   Map mapDerReq = (Map) itrDerReq.next();
			   String strAlreadyAddedReqId = (String)mapDerReq.get(strObjectIdSelect);
			   if (strAlreadyAddedReqId.equals(strCurrReqId)) {
			       isAddedAlready = true;
			       break;
			   }
		       }
		       if (!isAddedAlready)
		       {
			   mlDerivedRequirements.add(mapObj2);
		       }
		   }
	       }
	   }

	   // Find and populated derived requirements for the root requirements
	   // Add root maps to partial map list
	   //
	   MapList mlPartialTableData = new MapList();
	   for (Iterator itrRootReq = mapRootReqCache.keySet().iterator(); itrRootReq.hasNext();)
	   {
	       String strRootReqId = (String) itrRootReq.next();

	       Map mapRootReq = (Map)mapRootReqCache.get(strRootReqId);

	       // Find Derived Requirements for this requirement
	       //
	       for (Iterator itr2 = tableData.iterator(); itr2.hasNext();)
	       {
		   Map mapObj2 = (Map) itr2.next();

		   String strCurrReqId = (String)mapObj2.get(strObjectIdSelect);
		   String strCurrReqParentId = (String)mapObj2.get(strParentObjectIdSelect);
		   // If requirement is parent of someone, then that someone is its derived requirement
		   //
		   if (strCurrReqParentId != null && strCurrReqParentId.equals(strRootReqId))
		   {
		       MapList mlDerivedRequirements = (MapList)mapRootReq.get("DerivedRequirements");
		       if (mlDerivedRequirements == null)
		       {
			   mlDerivedRequirements = new MapList();
			   mapRootReq.put("DerivedRequirements", mlDerivedRequirements);
		       }

		       // Add only if not added already
		       // Not able to directly use "if (!mlDerivedRequirements.contains(mapObj2))" because of some data
		       // differences in mapObj2 the contains method gives incorrect results.
		       //
		       boolean isAddedAlready = false;
		       for (Iterator itrDerReq = mlDerivedRequirements.iterator(); itrDerReq.hasNext();) {
			   Map mapDerReq = (Map) itrDerReq.next();
			   String strAlreadyAddedReqId = (String)mapDerReq.get(strObjectIdSelect);
			   if (strAlreadyAddedReqId.equals(strCurrReqId)) {
			       isAddedAlready = true;
			       break;
			   }
		       }
		       if (!isAddedAlready)
		       {
			   mlDerivedRequirements.add(mapObj2);
		       }
		   }
	       }

	       mlPartialTableData.add(mapRootReq);
	   }

	   // Process and convert it to convenient form for table column methods
	   //
	   MapList mlFinalTableData = new MapList();
	   for (Iterator itrPartialList = mlPartialTableData.iterator(); itrPartialList.hasNext();)
	   {
	       Map mapParentReq = (Map) itrPartialList.next();
	       MapList mlDerivedReqs = (MapList) mapParentReq.get("DerivedRequirements");

	       MapList mlProcessedTableData = convertToHorizontalTree (mapParentReq, mlDerivedReqs, 1, maxLevels);
	       mlFinalTableData.addAll(mlProcessedTableData);
	   }

	   return mlFinalTableData;
       } catch (Exception e) {
	   e.printStackTrace();
	   throw e;
       }
   }

   /**
    * Converts table data to the final representable form required to render requirement allocation report knowing the parent and its
    * derived requirements. This is used in conjunction with processForFinalPresentation() method. This is recursive algorithm.
    *
    *    Ex. Resultant maps will have keys like 0, 1, 2, etc as column indices
    *        Key n points to map of parent requirement
    *        Key n+1 points to map of child derived requirement information
    *    So if a req R1 has derived requirements R11,R12 and R13, and if nCurrentLevel=3 then the final maplist will look like
    *    map 2=R1	3=R11
    *    map 2=         3=R12
    *    map 2=         3=R13
    *
    * @param mapParent The map of parent requirement
    * @param mlDerivedReqs The maplist of the children derived requirements
    * @param nCurrentLevel The current level of the recursion
    * @param nMaxLevel Max level of recursion allowed, 0 means recurse to all level
    * @return The maplist after processing as mentioned in the descriotion above.
    * @throws Exception if operation fails.
    * @since RMT V6R2010x
    * @author KYP
    */
   private MapList convertToHorizontalTree (Map mapParent, MapList mlDerivedReqs, int nCurrentLevel, int nMaxLevel) throws Exception
   {
       try {
	   // Argument check
	   //
	   if (mapParent == null)
	   {
	       throw new IllegalArgumentException("mapParent");
	   }
	   if (mlDerivedReqs == null)
	   {
	       mlDerivedReqs = new MapList();
	   }
	   if (nCurrentLevel > nMaxLevel && nMaxLevel != 0)
	   {
	       throw new IllegalStateException("Invalid state in algorithm current level" + nCurrentLevel + " and max level " + nMaxLevel);
	   }

	   MapList mlResult = new MapList();

	   if (mlDerivedReqs.size() > 0)
	   {
	       for (Iterator itrDerivedReqs = mlDerivedReqs.iterator(); itrDerivedReqs.hasNext();)
	       {
		   Map mapDerivedReq = (Map) itrDerivedReqs.next();

		   if (nCurrentLevel < nMaxLevel || nMaxLevel == 0) // Do even if recurse to all(0)
		   {
		       MapList mlSubDerivedReqs = (MapList)mapDerivedReq.get("DerivedRequirements");
		       MapList mlPartialResult = convertToHorizontalTree (mapDerivedReq, mlSubDerivedReqs, nCurrentLevel+1, nMaxLevel);
		       mlResult.addAll(mlPartialResult);
		   }
		   else
		   {
		       Map mapResult = new HashMap();
		       mapResult.put(new Integer(nCurrentLevel), mapDerivedReq);
		       mapResult.put(NUM_DERIVED_COLUMNS, new Integer(nCurrentLevel)); //IR-090024V6R2012
		       mlResult.add(mapResult);

		   }
	       }

	       Map mapResult = (Map)mlResult.get(0);
	       mapResult.put(new Integer(nCurrentLevel - 1), mapParent);
	   }
	   else
	   {
	       Map mapResult = new HashMap();
	       mapResult.put(new Integer(nCurrentLevel - 1), mapParent);
	       mapResult.put(NUM_DERIVED_COLUMNS, new Integer(nCurrentLevel - 1)); //IR-090024V6R2012
	       mlResult.add(mapResult);
	   }

	   return mlResult;
       } catch (Exception e) {
	   e.printStackTrace();
	   throw e;
       }
   }

   /**
    * Returns values for "RootRequirementsName" column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
    *
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return The Vector containing data for each row of the table
    * @throws Exception if operation fails.
    * @since RMT V6R2010x
    * @author KYP
    */
   public Vector getRootRequirementNameColumnData(Context context, String[] args) throws Exception
   {
	   try {
		   Map programMap = (Map) JPO.unpackArgs(args);
		   Map paramList = (Map)programMap.get("paramList");

		   String strReportFormat = (String)paramList.get("reportFormat");
		   boolean isPrinterFriendly = "HTML".equalsIgnoreCase(strReportFormat);
		   boolean isExportFormat = "true".equalsIgnoreCase((String)paramList.get("isExportFormat"));
		   String strExportFormat = (String) paramList.get("exportFormat");

		   MapList mlObjectList = (MapList)programMap.get("objectList");
		   Vector vecColumnData = new Vector(mlObjectList.size());

		   String strHTMLIconTemplate = "<img src=\"../common/images/${IMAGE}\" border=\"0\"/>";
		   String strHTMLLinkTemplate = "<a href=\"javascript:emxTableColumnLinkClick('emxTree.jsp?objectId=${OBJECT_ID}','800','600','false','popup','')\">${TYPE_ICON}<b>${NAME}</b></a>";

		   boolean showTypeIcon = "true".equalsIgnoreCase(getSettingProperty(context, "emxRequirements.TraceabilityReport.DerivedRequirementsOnly.ShowTypeIcon"));
		   
		   // Keep a record to combine the requirements
		   Set idTreated = new HashSet<String>();

		   try
		   {
			   for (int i=0; i < mlObjectList.size(); i++)
			   {
				   Map mapRow = (Map)mlObjectList.get(i);
				   Map mapCol = (Map)mapRow.get(new Integer(0));
				   String strRootReqId = (String) mapRow.get(SELECT_ID);
				   String strObjectId = "", strObjectType = "", strName = "";
				   String strValue = "";

				   // If we are in a Derivation Traceability Matrix
				   if (mapCol != null)
				   {
					   strObjectId = (String)mapCol.get(SELECT_TO_ID); idTreated.add(strObjectId);
					   strObjectType = (String)mapCol.get(SELECT_TYPE);
					   strName = (String)mapCol.get(SELECT_NAME);
				   }

				   // If we are in a Sub/Derivation Traceability Matrix, and if we haven't combine this requirement yet
				   else if (strRootReqId != null && !idTreated.contains(strRootReqId)) {
					   idTreated.add(strRootReqId);
					   
					   // Get information about the current requirement
					   DomainObject dmoRootReq = DomainObject.newInstance(context, strRootReqId);

					   StringList slRootReqSelect = new StringList();
					   slRootReqSelect.add(SELECT_NAME);
					   slRootReqSelect.add(SELECT_REVISION);
					   slRootReqSelect.add(SELECT_TYPE);

					   Map mapRootInfo = dmoRootReq.getInfo(context, slRootReqSelect);
					   strObjectId = strRootReqId;
					   strObjectType = (String)mapRootInfo.get(SELECT_TYPE);
					   strName = (String)mapRootInfo.get(SELECT_NAME);
				   }
				   if(strObjectId==null||strObjectId.isEmpty()){
					   String reportObject = (String)paramList.get("reportObjects");
					   if(reportObject!=null&&!reportObject.isEmpty())
					   {
						   strObjectId = reportObject;
						   DomainObject reportObj = DomainObject.newInstance(context, reportObject);
						   reportObj.open(context);
						   strName = reportObj.getName();
						   strObjectType = reportObj.getTypeName();
						   reportObj.close(context);
					   }
				   }
				   // We can combine this requirement 
				   if (!strObjectId.isEmpty()) {
					   if (isExportFormat && !isPrinterFriendly)
					   {
						   // strValue = strName;
						   if(isExportFormat && strExportFormat!= null && "CSV".equalsIgnoreCase(strExportFormat))
						   {
							   strValue = strName;
						   }
						   else
						   {
							   strValue = FrameworkUtil.findAndReplace(strHTMLLinkTemplate, "${OBJECT_ID}", strObjectId);
							   strValue = FrameworkUtil.findAndReplace(strValue, "${NAME}",  strName);
						   }
					   }
					   else
					   {
						   if (isPrinterFriendly)
						   {
							   strValue = "${TYPE_ICON}&nbsp;<b>" + strName + "</b>";
						   }
						   else
						   {
							   strValue = FrameworkUtil.findAndReplace(strHTMLLinkTemplate, "${OBJECT_ID}", strObjectId);
							   strValue = FrameworkUtil.findAndReplace(strValue, "${NAME}", strName);
						   }
					   }
					   // Take care if icons are to be shown
					   if (showTypeIcon)
					   {
						   // Find the icon
						   String strTypeIcon = UINavigatorUtil.getTypeIconProperty(context, strObjectType);;

						   String strIconHTML = FrameworkUtil.findAndReplace(strHTMLIconTemplate, "${IMAGE}", strTypeIcon);
						   strValue = FrameworkUtil.findAndReplace(strValue, "${TYPE_ICON}", strIconHTML);
					   }
					   else
					   {
						   strValue = FrameworkUtil.findAndReplace(strValue, "${TYPE_ICON}&nbsp;", "");
					   }

				   }

				   // The current requirement is already combine, strValue = ""
				   vecColumnData.addElement(strValue);
			   }
		   }
		   catch (Exception e)
		   {
			   throw new Exception(e.toString());
		   }
		   return vecColumnData;
	   } catch (Exception e) {
		   e.printStackTrace();
		   throw e;
	   }
   }

   /**
    * Returns values for "RootRequirementsRevision" column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
    *
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return The Vector containing data for each row of the table
    * @throws Exception if operation fails.
    * @since RMT V6R2010x
    * @author KYP
    */
   public Vector getRootRequirementRevisionColumnData(Context context, String[] args) throws Exception
   {
	   try
	   {
		   Map programMap = (Map) JPO.unpackArgs(args);
		   MapList mlObjectList = (MapList)programMap.get("objectList");
		   Vector vecColumnData = new Vector(mlObjectList.size());
		   
		   // Keep a record to combine the requirements
		   Set idTreated = new HashSet<String>();

		   for (int i=0; i < mlObjectList.size(); i++)
		   {
			   Map mapRow = (Map)mlObjectList.get(i);
			   String strRootReqId = (String) mapRow.get(SELECT_ID);
			   Map mapCol = (Map)mapRow.get(new Integer(0));

			   String strHTML = "";

			   // If we are in a Derivation Traceability Matrix
			   if (mapCol != null)
			   {
				   idTreated.add((String)mapCol.get(SELECT_TO_ID));
				   strHTML = (String)mapCol.get(SELECT_REVISION);
			   }

			   // If we are in a Sub/Derivation Traceability Matrix, and if we haven't combine this requirement yet
			   else if (strRootReqId != null && !idTreated.contains(strRootReqId))
			   {
				   idTreated.add(strRootReqId);
				   
				   // Get information about the current requirement
				   DomainObject dmoRootReq = DomainObject.newInstance(context, strRootReqId);

				   StringList slRootReqSelect = new StringList();
				   slRootReqSelect.add(SELECT_REVISION);

				   Map mapRootInfo = dmoRootReq.getInfo(context, slRootReqSelect);
				   strHTML = (String)mapRootInfo.get(SELECT_REVISION);
			   }
			   
			   // The current requirement is already combine, strValue = ""
			   vecColumnData.addElement(strHTML);
		   }
		   return vecColumnData;
	   }
	   catch (Exception e)
	   {
		   throw e;
	   }
   }

   /**
    * Returns values for "Derived Requirement" dynamic column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
    *
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return The Vector containing data for each row of the table
    * @throws Exception if operation fails.
    * @since RMT V6R2010x
    * @author KYP
    */
   public Vector getDerivedRequirementNameColumnData(Context context, String[] args) throws Exception
   {
       Map programMap = (Map) JPO.unpackArgs(args);
       Map paramList = (Map)programMap.get("paramList");
       Map columnMap = (Map)programMap.get("columnMap");

       String strReportFormat = (String)paramList.get("reportFormat");
       boolean isPrinterFriendly = "HTML".equalsIgnoreCase(strReportFormat);
       boolean isExportFormat = "true".equalsIgnoreCase((String)paramList.get("isExportFormat"));
       String strExportFormat = (String) paramList.get("exportFormat");

       String strLanguage = context.getSession().getLanguage();
       final String STRING_ORPHANED_REQUIREMENT = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.TraceabilityReport.OrphanedRequirement"); 

       String strColumnName = (String)columnMap.get("name");
       Integer nColumnIndex = new Integer(0);
       try
       {
	   nColumnIndex = new Integer(FrameworkUtil.findAndReplace(strColumnName, "DerReq-Name-", ""));
       }
       catch(Exception exp)
       {
	   throw new IllegalStateException("Column name:"+ strColumnName);
       }

       MapList mlObjectList = (MapList)programMap.get("objectList");
       Vector vecColumnData = new Vector(mlObjectList.size());

       String strHTMLIconTemplate = "<img src=\"../common/images/${IMAGE}\" border=\"0\"/>";
       String strHTMLLinkTemplate = "<a title=\"${TITLE}\" href=\"javascript:emxTableColumnLinkClick('emxTree.jsp?objectId=${OBJECT_ID}','800','600','false','popup','')\">${TYPE_ICON}&nbsp;<b>${NAME}</b></a>";

       boolean showTypeIcon = "true".equalsIgnoreCase(getSettingProperty(context, "emxRequirements.TraceabilityReport.DerivedRequirementsOnly.ShowTypeIcon"));

       try
       {
           for (int i=0; i < mlObjectList.size(); i++)
           {
               Map mapRow = (Map)mlObjectList.get(i);
               Map mapCol = (Map)mapRow.get(nColumnIndex);
               if (mapCol != null)
               {
        	   String strName = (String)mapCol.get(SELECT_TO_NAME);
        	   String strObjectId = (String)mapCol.get(SELECT_TO_ID);
        	   String strObjectType = (String)mapCol.get(SELECT_TO_TYPE);

        	   MapList mlParentSpecInfo = (MapList)mapCol.get("ParentSpecInfo");
        	   boolean isOrphaned = (mlParentSpecInfo == null || mlParentSpecInfo.size() == 0)?true:false;
        	   String strValue = "";

        	   if (isExportFormat && !isPrinterFriendly)
        	   {
        		// strValue = strName;
        	       if(isExportFormat && strExportFormat!= null && "CSV".equalsIgnoreCase(strExportFormat))
        	       {
        	    	   strValue = strName;
        	       }
        	       else
        	       {
	        		   strValue = FrameworkUtil.findAndReplace(strHTMLLinkTemplate, "${OBJECT_ID}", strObjectId);
	        		   strValue = FrameworkUtil.findAndReplace(strValue, "${NAME}", XSSUtil.encodeForHTML(context, strName));
	        		   if (isOrphaned)
	        		   {
	        		       strValue = FrameworkUtil.findAndReplace(strValue, "${TITLE}", STRING_ORPHANED_REQUIREMENT);
	        		   }
	        		   else
	        		   {
	        		       strValue = FrameworkUtil.findAndReplace(strValue, "${TITLE}", "");
	        		   }
        	       }
        	   }
        	   else
        	   {
        	       if (isPrinterFriendly)
        	       {
        		   strValue = "${TYPE_ICON}&nbsp;<b>" + strName + "</b>";
        	       }
        	       else
        	       {
        		   strValue = FrameworkUtil.findAndReplace(strHTMLLinkTemplate, "${OBJECT_ID}", strObjectId);
        		   strValue = FrameworkUtil.findAndReplace(strValue, "${NAME}", XSSUtil.encodeForHTML(context, strName));
        		   if (isOrphaned)
        		   {
        		       strValue = FrameworkUtil.findAndReplace(strValue, "${TITLE}", STRING_ORPHANED_REQUIREMENT);
        		   }
        		   else
        		   {
        		       strValue = FrameworkUtil.findAndReplace(strValue, "${TITLE}", "");
        		   }
        	       }

        	   }
        	       // Take care if icons are to be shown
		       //
			       if (showTypeIcon)
			       {
				   // Find the icon
					   String strTypeIcon = emxRMTCommon_mxJPO.getRelIconProperty(context, ReqSchemaUtil.getDerivedRequirementRelationship(context), ReqSchemaUtil.getRequirementType(context));

					   String strIconHTML = FrameworkUtil.findAndReplace(strHTMLIconTemplate, "${IMAGE}", strTypeIcon);
					   strValue = FrameworkUtil.findAndReplace(strValue, "${TYPE_ICON}", strIconHTML);
			       }
			       else
			       {
			    	   strValue = FrameworkUtil.findAndReplace(strValue, "${TYPE_ICON}&nbsp;", "");
			       }


        	   vecColumnData.add(strValue);
               }
               else
               {
        	   vecColumnData.add("");
               }
           }
       }
       catch (Exception e)
       {
           throw new Exception(e.toString());
       }
       return vecColumnData; 
   }

   /**
    * Returns values for "Revision" dynamic column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
    *
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return The Vector containing data for each row of the table
    * @throws Exception if operation fails.
    * @since RMT V6R2010x
    * @author KYP
    */
   public Vector getDerivedRequirementRevisionColumnData(Context context, String[] args) throws Exception
   {
       Map programMap = (Map) JPO.unpackArgs(args);
       Map columnMap = (Map)programMap.get("columnMap");

       String strColumnName = (String)columnMap.get("name");
       Integer nColumnIndex = new Integer(0);
       try
       {
	   nColumnIndex = new Integer(FrameworkUtil.findAndReplace(strColumnName, "DerReq-Rev-", ""));
       }
       catch(Exception exp)
       {
	   throw new IllegalStateException("Column name:"+ strColumnName);
       }

       MapList mlObjectList = (MapList)programMap.get("objectList");
       Vector vecColumnData = new Vector(mlObjectList.size());

       try{
           for (int i=0; i < mlObjectList.size(); i++) {
               Map mapRow = (Map)mlObjectList.get(i);
               Map mapCol = (Map)mapRow.get(nColumnIndex);
               if (mapCol != null)
               {
        	   String strValue = (String)mapCol.get(SELECT_TO_REVISION);
        	   vecColumnData.add(strValue);
               }
               else
               {
        	   vecColumnData.add("");
               }
           }
       }
       catch (Exception e) {
           throw new Exception(e.toString());
       }
       return vecColumnData;
   }

   /**
    * Returns values for "Specification Name" column root requirement column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
    *
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return The Vector containing data for each row of the table
    * @throws Exception if operation fails.
    * @since RMT V6R2011
    * @author OEP
    */
   public Vector getRootRequirementSpecificationColumnData(Context context, String[] args) throws Exception
   {
       Map programMap = (Map) JPO.unpackArgs(args);
       Integer nColumnIndex = new Integer(0);

       return getRequirementSpecificationColumnData(context, programMap, nColumnIndex, "RootParentSpecInfo");
   }

   /**
    * Returns values for "Title" dynamic column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
    * Invoked from getDerivedRequirementColumns funcion for Target Specification
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return Exception if operation fails.
    * @throws Exception if operation fails.
    * @since RMT V6R2011
    *
    */

   public Vector getDerivedRequirementSpecificationTitleColumnData(Context context, String[] args) throws Exception
   {
       Map programMap = (Map) JPO.unpackArgs(args);
       Map paramList = (Map)programMap.get("paramList");

       String strReportFormat = (String)paramList.get("reportFormat");
       boolean isPrinterFriendly = "HTML".equalsIgnoreCase(strReportFormat);
       boolean isExportFormat = "true".equalsIgnoreCase((String)paramList.get("isExportFormat"));

       Map columnMap = (Map)programMap.get("columnMap");

       String strColumnName = (String)columnMap.get("name");
       Integer nColumnIndex = new Integer(0);
       try
       {
	   nColumnIndex = new Integer(FrameworkUtil.findAndReplace(strColumnName, "DerReq-Title-", ""));
       }
       catch(Exception exp)
       {
	   throw new IllegalStateException("Column name:"+ strColumnName);
       }
       return getRequirementSpecificationTitleColumnData(context, programMap, nColumnIndex, "ParentSpecInfo");
   }

   /**
    *  Returns values for "Title" from table "RMTTraceabilityDerivedRequirementsOnlyTable" Source Specification
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return The Vector containing data for each row of the table
    * @throws Exception if operation fails.
    * @since RMT V6R2011
    * @author OEP
    */

   public Vector getRootRequirementSpecificationTitleColumnData(Context context, String[] args) throws Exception
   {
       Map programMap = (Map) JPO.unpackArgs(args);
       Integer nColumnIndex = new Integer(0);

       return getRequirementSpecificationTitleColumnData(context, programMap, nColumnIndex, "RootParentSpecInfo");
   }

   /**
    * Returns values for "Title" dynamic column in table "RMTTraceabilityDerivedRequirementsOnlyTable" For Source / Target Specification
    *
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return The Vector containing data for each row of the table
    * @throws Exception if operation fails.
    * @since RMT V6R2011
    * @author OEP
    */
   private Vector getRequirementSpecificationTitleColumnData(Context context, Map programMap, Integer nColumnIndex, String strParentSpecInfoKey) throws Exception
   {
	   Map paramList = (Map)programMap.get("paramList");

	   String strReportFormat = (String)paramList.get("reportFormat");
	   boolean isPrinterFriendly = "HTML".equalsIgnoreCase(strReportFormat);
	   boolean isExportFormat = "true".equalsIgnoreCase((String)paramList.get("isExportFormat"));
	   String strExportFormat = (String) paramList.get("exportFormat");

	   MapList mlObjectList = (MapList)programMap.get("objectList");
	   Vector vecColumnData = new Vector(mlObjectList.size());

	   String strLanguage = (String)programMap.get("languageStr");
	   final String RESOURCE_BUNDLE = "emxRequirementsStringResource";
	   final String STRING_MISSING_RSP = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE, context.getLocale(), "emxRequirements.TraceabilityReport.MissingRequirementSpecification"); 
	   
	   // Keep a record to combine the requirements
	   Set idTreated = new HashSet<String>();
	   
	   try
	   {
		   for (int i=0; i < mlObjectList.size(); i++)
		   {
			   Map mapRow = (Map)mlObjectList.get(i);
			   Map mapCol = (Map)mapRow.get(nColumnIndex);
			   String strRootReqId = (String) mapRow.get(SELECT_ID);
			   
			   if (mapCol != null)
			   {
				   idTreated.add((String)mapCol.get(SELECT_TO_ID));
				   
				   // Handle, there may be multiple parent specifications!
				   MapList mlParentSpecInfo = (MapList)mapCol.get(strParentSpecInfoKey);
				   if (mlParentSpecInfo != null && mlParentSpecInfo.size() > 0)
				   {
					   StringBuffer strHTMLBuffer = new StringBuffer();
					   for (Iterator itrParentSpecInfo = mlParentSpecInfo.iterator(); itrParentSpecInfo.hasNext();)
					   {
						   Map mapParentSpecInfo = (Map) itrParentSpecInfo.next();

						   String strSpecName = (String)mapParentSpecInfo.get(SELECT_NAME);
						   String strSpecId = (String)mapParentSpecInfo.get(SELECT_ID);
						   String strSpecType = (String)mapParentSpecInfo.get(SELECT_TYPE);
						   String strHTML = "";

						   if (strHTMLBuffer.length() > 0) {
							   strHTMLBuffer.append(", ");
						   }

						   String strTitleQuery = "print bus $1 select $2 dump $3";
						   String strTitleResult = MqlUtil.mqlCommand(context, strTitleQuery, strSpecId, "attribute[Title]", "|");

						   if (strTitleResult != null && !strTitleResult.equals("") && !"null".equals(strTitleResult))
						   {
							   strHTML = strTitleResult;
						   }
						   else
						   {
							   // strHTML = strSpecName;
							   strHTML = "-";
						   }
						   strHTMLBuffer.append(strHTML);

					   }
					   vecColumnData.add(strHTMLBuffer.toString());
				   }
				   else
				   {
					   vecColumnData.add("");
				   }
			   }
			   
			   // If we are in a Sub/Derivation Traceability Matrix, and if we haven't combine this requirement yet
			   else if (strRootReqId != null & !idTreated.contains(strRootReqId))
			   {
				   List<String> relatedSpecificationsList = null;
				   Set relatedSpecificationsTreated = new HashSet<String>();
				   try
				   {
					   relatedSpecificationsList = getSourceTitle(context, JPO.packArgs(programMap));
				   } catch (Exception ex) {
					   throw (new FrameworkException(ex.toString()));
				   }
				   
				   // Put the titles for the related specifications
				   for (String relatedSpecifications : relatedSpecificationsList) 
				   {
					   if (!relatedSpecificationsTreated.contains(relatedSpecifications))
					   {
						   relatedSpecificationsTreated.add(relatedSpecifications);
						   try
						   {
							   // To replace the empty titles by a -
							   StringBuilder bRS = new StringBuilder(relatedSpecifications);
							   relatedSpecifications = bRS.replace(relatedSpecifications.lastIndexOf(", "), relatedSpecifications.lastIndexOf(", ") + 1, ", -").toString();
							   for (int sI = 0; sI < relatedSpecificationsList.size(); sI++)
							   {
								   relatedSpecifications = relatedSpecifications.replace(", ,", ", -,");
							   }
						   } catch(Exception e)
						   {
							   // NOP
						   }
						   vecColumnData.add(relatedSpecifications);
					   }
					   else
						   vecColumnData.add("");
				   }
			   }
			   else
			   {
				   vecColumnData.add("");
			   }
		   }
	   }
	   catch (Exception e) {
		   throw new Exception(e.toString());
	   }
	   return vecColumnData;
   }
   
   /**
    * Returns values for "Specification" dynamic column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
    *
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return The Vector containing data for each row of the table
    * @throws Exception if operation fails.
    * @since RMT V6R2011
    * @author OEP
    */
   public Vector getDerivedRequirementSpecificationColumnData(Context context, String[] args) throws Exception
   {
       Map programMap = (Map) JPO.unpackArgs(args);
       Map paramList = (Map)programMap.get("paramList");

       String strReportFormat = (String)paramList.get("reportFormat");
       boolean isPrinterFriendly = "HTML".equalsIgnoreCase(strReportFormat);
       boolean isExportFormat = "true".equalsIgnoreCase((String)paramList.get("isExportFormat"));

       Map columnMap = (Map)programMap.get("columnMap");

       String strColumnName = (String)columnMap.get("name");
       Integer nColumnIndex = new Integer(0);
       try
       {
	   nColumnIndex = new Integer(FrameworkUtil.findAndReplace(strColumnName, "DerReq-Spec-", ""));
       }
       catch(Exception exp)
       {
	   throw new IllegalStateException("Column name:"+ strColumnName);
       }
       return getRequirementSpecificationColumnData(context, programMap, nColumnIndex, "ParentSpecInfo");
   }
   /**
    * Returns values for "Specification" dynamic column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
    *
    * @return The Vector containing data for each row of the table
    * @throws Exception if operation fails.
    * @since RMT V6R2010x
    * @author KYP
    */
   private Vector getRequirementSpecificationColumnData(Context context, Map programMap, Integer nColumnIndex, String strParentSpecInfoKey) throws Exception
   {
	   Map paramList = (Map)programMap.get("paramList");

	   String strReportFormat = (String)paramList.get("reportFormat");
	   boolean isPrinterFriendly = "HTML".equalsIgnoreCase(strReportFormat);
	   boolean isExportFormat = "true".equalsIgnoreCase((String)paramList.get("isExportFormat"));
	   String strExportFormat = (String) paramList.get("exportFormat");

	   MapList mlObjectList = (MapList)programMap.get("objectList");
	   Vector vecColumnData = new Vector(mlObjectList.size());

	   String strHTMLIconTemplate = "<img src=\"../common/images/${IMAGE}\" border=\"0\"/>";
	   String strHTMLLinkTemplate = "<a href=\"javascript:emxTableColumnLinkClick('emxTree.jsp?objectId=${OBJECT_ID}','800','600','false','popup','')\">${TYPE_ICON}<b>${NAME}</b></a>";

	   boolean showTypeIcon = "true".equalsIgnoreCase(getSettingProperty(context, "emxRequirements.TraceabilityReport.DerivedRequirementsOnly.ShowTypeIcon"));

	   String strColor = getSettingProperty(context, "emxRequirements.TraceabilityReport.DerivedRequirementsOnly.SpecMissing.Color");
	   if (strColor == null || strColor.length() == 0) {
		   strColor = "red";
	   }

	   String strLanguage = (String)programMap.get("languageStr");
	   final String RESOURCE_BUNDLE = "emxRequirementsStringResource";
	   final String STRING_MISSING_RSP = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE, context.getLocale(), "emxRequirements.TraceabilityReport.MissingRequirementSpecification");  
	   
	   // Keep a record to combine the requirements
	   Set idTreated = new HashSet<String>();

	   try
	   {
		   for (int i=0; i < mlObjectList.size(); i++)
		   {
			   Map mapRow = (Map)mlObjectList.get(i);
			   Map mapCol = (Map)mapRow.get(nColumnIndex);
			   String strRootReqId = (String) mapRow.get(SELECT_ID);

			   // If we are in a Derivation Traceability Matrix
			   if (mapCol != null)
			   {
				   idTreated.add((String)mapCol.get(SELECT_TO_ID));

				   // Handle, there may be multiple parent specifications!
				   MapList mlParentSpecInfo = (MapList)mapCol.get(strParentSpecInfoKey);
				   if (mlParentSpecInfo != null && mlParentSpecInfo.size() > 0)
				   {
					   StringBuffer strHTMLBuffer = new StringBuffer();
					   for (Iterator itrParentSpecInfo = mlParentSpecInfo.iterator(); itrParentSpecInfo.hasNext();)
					   {
						   Map mapParentSpecInfo = (Map) itrParentSpecInfo.next();

						   String strSpecName = (String)mapParentSpecInfo.get(SELECT_NAME);
						   String strSpecId = (String)mapParentSpecInfo.get(SELECT_ID);
						   String strSpecType = (String)mapParentSpecInfo.get(SELECT_TYPE);
						   String strHTML = "";

						   if (strHTMLBuffer.length() > 0) {
							   strHTMLBuffer.append(", ");
						   }

						   if (isExportFormat && !isPrinterFriendly)
						   {
							   // strHTMLBuffer.append(strSpecName);
							   if(isExportFormat && strExportFormat!= null && "CSV".equalsIgnoreCase(strExportFormat))
							   {
								   strHTMLBuffer.append(strSpecName);
							   }
							   else
							   {
								   strHTML = FrameworkUtil.findAndReplace(strHTMLLinkTemplate, "${OBJECT_ID}", strSpecId);
								   strHTML = FrameworkUtil.findAndReplace(strHTML, "${NAME}", strSpecName);
							   }
						   }
						   else
						   {

							   if (isPrinterFriendly)
							   {
								   strHTML = "${TYPE_ICON}&nbsp;<b>" + XSSUtil.encodeForHTML(context, strSpecName) + "</b>";
							   }
							   else
							   {
								   strHTML = FrameworkUtil.findAndReplace(strHTMLLinkTemplate, "${OBJECT_ID}", strSpecId);
								   strHTML = FrameworkUtil.findAndReplace(strHTML, "${NAME}",  strSpecName);
							   }

						   }
						   // Take care if icons are to be shown
						   //
						   if (showTypeIcon)
						   {
							   // Find the icon
							   String strTypeIcon = UINavigatorUtil.getTypeIconProperty(context, strSpecType);

							   String strIconHTML = FrameworkUtil.findAndReplace(strHTMLIconTemplate, "${IMAGE}", strTypeIcon);
							   strHTML = FrameworkUtil.findAndReplace(strHTML, "${TYPE_ICON}", strIconHTML);
						   }
						   else
						   {
							   strHTML = FrameworkUtil.findAndReplace(strHTML, "${TYPE_ICON}&nbsp;", "");
						   }

						   strHTMLBuffer.append(strHTML);

					   }

					   vecColumnData.add(strHTMLBuffer.toString());
				   }
				   else
				   {
					   if (isExportFormat || isPrinterFriendly)
					   {
						   if(isExportFormat && strExportFormat != null && !"".equals(strExportFormat) && "CSV".equals(strExportFormat))
						   {
							   vecColumnData.add("-");
						   }
						   else
						   {
							   String strHTML = "<div title=\"" + STRING_MISSING_RSP + "\" style=\"color: white; background: " + strColor + "; text-align: center; display: block; height: 1.5em; padding: .25em\"></div>";
							   vecColumnData.add(strHTML);
						   }
					   }
					   else
					   {
						   String strHTML = "<div title=\"" + STRING_MISSING_RSP + "\" style=\"color: white; background: " + strColor + "; text-align: center; display: block; height: 1.5em; padding: .25em\"></div>";
						   vecColumnData.add(strHTML);
					   }
				   }
			   }
			   
			   // If we are in a Sub/Derivation Traceability Matrix, and if we haven't combine this requirement yet
			   else if (strRootReqId != null & !idTreated.contains(strRootReqId))
			   {
				   List<String> relatedSpecificationsList = null;
				   Set relatedSpecificationsTreated = new HashSet<String>();
				   
				   try
				   {
					   // Get the related specifications for the current requirement
					   relatedSpecificationsList = (List) JPO.invoke(context,
							   "emxRequirement", null, "getRelatedSpecifications",
							   JPO.packArgs(programMap), List.class);
				   } catch (Exception ex) {
					   throw (new FrameworkException(ex.toString()));
				   }
				   if (relatedSpecificationsList != null )
				   {
					   // Put the (combined) related specifications 
					   for (String relatedSpecifications : relatedSpecificationsList) 
					   {
						   if (!relatedSpecificationsTreated.contains(relatedSpecifications))
						   {
							   relatedSpecificationsTreated.add(relatedSpecifications);
							   vecColumnData.add(relatedSpecifications);
						   }
						   else
							   vecColumnData.add("");
					   }
				   }
				   else
					   vecColumnData.add("");
			   }
			   else
			   {
				   vecColumnData.add("");
			   }
		   }
	   }
	   catch (Exception e) {
		   throw new Exception(e.toString());
	   }
	   return vecColumnData;
   }

   /**
     * This method is called from UI tables to display Target Specification.
     * This method is used to return the list of an connected Specification object
     *
     * @param context
     *                the eMatrix <code>Context</code> object
     * @param args
     *                holds arguments
     * @return List- the List of Specificaation Object names with hyperlink
     * @throws Exception
     *                 if the operation fails
     * @since RequirementManagement X3 For Target Requirement Specification
     *        00000
     */
   public List getRequirementSpecificationTargetName(Context context, String[] args) throws Exception
   {

       final String SYMB_COMMA      = ",";
       final String STR_OBJECT_LIST = "objectList";

       // unpack the arguments
       Map programMap               = (HashMap) JPO.unpackArgs(args);

       //Start
       Map paramList = (Map)programMap.get("paramList");
      // boolean isExportFormat = "true".equalsIgnoreCase((String)paramList.get("isExportFormat"));
	    String strExportFormat = (String) paramList.get("exportFormat");
	   String strReportFormat = (String)paramList.get("reportFormat");
	   boolean isPrinterFriendly = "HTML".equalsIgnoreCase(strReportFormat);
	   // Changes for IR-338259-3DEXPERIENCER2016
	 	  boolean isExportFormat = "CSV".equalsIgnoreCase(strExportFormat);
       //End

       // get the target Specification ID's (if any)
       Map paramMap                 = (Map) programMap.get("paramList");
       String objectId              = (String) paramMap.get("baselineObject");
       Vector mpSpecName            = new Vector();

       StringTokenizer toker        = new StringTokenizer(objectId, ",", false);
       while (toker.hasMoreTokens())
       {
          String sourceId           = toker.nextToken();
          DomainObject doSpec       = DomainObject.newInstance(context,sourceId);
          String strSpecName        = doSpec.getInfo(context, DomainConstants.SELECT_NAME);
          mpSpecName.add(strSpecName);
       }

       // get Specification child types
       List lstSpecChildTypes       = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getSpecificationType(context));
       StringBuffer sbSpecChildTypes= new StringBuffer(ReqSchemaUtil.getSpecificationType(context));
       sbSpecChildTypes.append(SYMB_COMMA);

       for (int i=0; i < lstSpecChildTypes.size(); i++)
       {
           sbSpecChildTypes.append(lstSpecChildTypes.get(i));

           if (i != lstSpecChildTypes.size()-1)
           {
               sbSpecChildTypes = sbSpecChildTypes.append(SYMB_COMMA);
           }
       }

       String strObjId          = DomainConstants.EMPTY_STRING;
       String strTargetId       = DomainConstants.EMPTY_STRING;
       String typeIcon          = DomainConstants.EMPTY_STRING;
       String defaultTypeIcon   = DomainConstants.EMPTY_STRING;
       String strSpecObjId      = DomainConstants.EMPTY_STRING;
       String strSpecName       = DomainConstants.EMPTY_STRING;
       String strObjType        = DomainConstants.EMPTY_STRING;
       String targetSpecName    = DomainConstants.EMPTY_STRING;

       StringList objSelects    = new StringList();
       objSelects.addElement(DomainConstants.SELECT_NAME);
       objSelects.addElement(DomainConstants.SELECT_ID);
       objSelects.addElement(DomainConstants.SELECT_TYPE);

       List lstobjectList       = (MapList) programMap.get(STR_OBJECT_LIST);
       Iterator objectListItr   = lstobjectList.iterator();
       Map objectMap            = new HashMap();

       MapList specObjMapList   = null;
       Map specObjMap           = null;
       Iterator specObjItr      = null;
       DomainObject domObj      = null;
       StringBuffer stbNameRev  = null;
       List specList            = null;
       List lstNameRev          = new StringList();
       
       String relPattern = ReqSchemaUtil.getSpecStructureRelationship(context)+","+
    		   			   ReqSchemaUtil.getDerivedRequirementRelationship(context)+","+
    		   			   ReqSchemaUtil.getSubRequirementRelationship(context);
       //  loop through all the records and returns all the
       //  related specifications with comma separated
       while(objectListItr.hasNext())
       {
          specList              = new StringList();
          stbNameRev            = new StringBuffer();
          objectMap             = (Map) objectListItr.next();
          strObjId              = (String)objectMap.get(DomainConstants.SELECT_ID);

          if(strObjId !=null)
          {
               strTargetId      = (String)objectMap.get("target.id");
               if( strTargetId != null)
               {
                   domObj       = DomainObject.newInstance(context, strTargetId);

                   // getting all related Specification Objects
                   specObjMapList   = domObj.getRelatedObjects(context,
                		   					relPattern,    // relPattern
                                           DomainConstants.QUERY_WILDCARD,          // typePattern
                                           true,                                    // to
                                           false,                                   // from
                                           0,                                       // recursionLevel
                                           objSelects,                              // objectSelects
                                           null,                                    // relationshipSelects
                                           null,                                    // busWhereClause
                                           null,                                    // relWhereClause
                                           null,                                    // postRelPattern
                                           sbSpecChildTypes.toString(),             // postTypePattern
                                           null);                                   // postPatterns

                   // Iterating through each Specification Object and making it hyperlink
                   specObjItr = specObjMapList.iterator();
                   while(specObjItr.hasNext())
                   {
                       specObjMap       = (Map)specObjItr.next();
                       strSpecObjId     = (String)specObjMap.get(DomainConstants.SELECT_ID);
                       strSpecName      = (String)specObjMap.get(DomainConstants.SELECT_NAME);
                       strObjType       = (String)specObjMap.get(DomainConstants.SELECT_TYPE);
                       typeIcon         = UINavigatorUtil.getTypeIconProperty(context, strObjType);
                       defaultTypeIcon  = "<img src=\"../common/images/" +typeIcon+ "\" border=\"0\" />";

                       // Adding to the maplist only derivatives of Specification
                       if(mpSpecName.size()==0)
                       {
                    	   if(isExportFormat || isPrinterFriendly)
                       	   {
                       		 if(isExportFormat && strExportFormat!= null && "CSV".equalsIgnoreCase(strExportFormat))
           	    	         {
                       			 stbNameRev.append(",");
                       			 stbNameRev.append(strSpecName);
           	    	         }
                       		else
                   		     {
                   			 stbNameRev.append(", <b> ");
                   			 stbNameRev.append(defaultTypeIcon);
                   			 stbNameRev = stbNameRev.append( strSpecName);
                   			 stbNameRev.append("</b> ");

                   		      }
                       	   }
                    	   else
                    	   {
	                           domObj           = DomainObject.newInstance(context, strSpecObjId);
	                           specList.add(strSpecObjId);
	                           stbNameRev.append(", <a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
	                           stbNameRev.append("objectId=");
	                           stbNameRev.append(strSpecObjId);
	                           stbNameRev.append("', '875', '550', 'false', 'popup', '')\">");
	                           stbNameRev.append(defaultTypeIcon);
	                           stbNameRev = stbNameRev.append( strSpecName);
	                           stbNameRev.append("</a> ");
                    	   }
                       }
                       else
                       {// IS targetIds are selected
                           for (int jj = 0; jj < mpSpecName.size(); jj++)
                           {
                               targetSpecName   = (String)mpSpecName.elementAt(jj);
                               if(targetSpecName.equalsIgnoreCase(strSpecName) && !specList.contains(strSpecObjId))
                               {
                            	   if(isExportFormat || isPrinterFriendly)
                               	   {
                               		 if(isExportFormat && strExportFormat!= null && "CSV".equalsIgnoreCase(strExportFormat))
                   	    	         {
                               			 stbNameRev.append(",");
                               			 stbNameRev.append(strSpecName);
                   	    	         }
                               		else
                          		     {
                          			 stbNameRev.append(", <b> ");
                          			 stbNameRev.append(defaultTypeIcon);
                          			 stbNameRev = stbNameRev.append( strSpecName);
                          			 stbNameRev.append("</b> ");

                          		      }
                               	   }
                            	   else
                            	   {
		                               specList.add(strSpecObjId);
		                               stbNameRev.append(", <a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
		                               stbNameRev.append("objectId=");
		                               stbNameRev.append(strSpecObjId);
		                               stbNameRev.append("', '875', '550', 'false', 'popup', '')\">");
		                               stbNameRev.append(defaultTypeIcon);
		                               stbNameRev = stbNameRev.append( strSpecName);
		                               stbNameRev.append("</a> ");
                            	   }
                               }
                           }
                       }
                   }// inner while
               }
           }

           if (stbNameRev.length()>0)
           {
               stbNameRev.deleteCharAt(0);
           }

           lstNameRev.add(stbNameRev.toString());
       }// outer while
       return(lstNameRev);


   }
   /**
    * Returns values for "Link Status" dynamic column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
    *
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return The Vector containing data for each row of the table
    * @throws Exception if operation fails.
    * @since RMT V6R2010x
    * @author KYP
    */
   public Vector getDerivedRequirementLinkStatusColumnData(Context context, String[] args) throws Exception
   {
       Map programMap = (Map) JPO.unpackArgs(args);
       Map columnMap = (Map)programMap.get("columnMap");
       Map paramList = (Map)programMap.get("paramList");

       String strReportFormat = (String)paramList.get("reportFormat");
       boolean isPrinterFriendly = "HTML".equalsIgnoreCase(strReportFormat);
       boolean isExportFormat = "true".equalsIgnoreCase((String)paramList.get("isExportFormat"));
       String strExportFormat = (String) paramList.get("exportFormat");

       String strColumnName = (String)columnMap.get("name");
       Integer nColumnIndex = new Integer(0);
       try
       {
	   nColumnIndex = new Integer(FrameworkUtil.findAndReplace(strColumnName, "DerReq-LinkStatus-", ""));
       }
       catch(Exception exp)
       {
	   throw new IllegalStateException("Column name:"+ strColumnName);
       }

       // Prepare cache for Link Status attribute internationalized range values
       //
       String strLanguage = context.getSession().getLanguage();

       AttributeType attributeType = new AttributeType(ReqSchemaUtil.getLinkStatusAttrubite(context));
       attributeType.open(context);
       StringList slLinkStatusRanges = attributeType.getChoices(context);
       attributeType.close(context);
       StringList slTranslatedLinkStatusRanges = com.matrixone.apps.domain.util.i18nNow.getAttrRangeI18NStringList(ReqSchemaUtil.getLinkStatusAttrubite(context),
	       slLinkStatusRanges,
               strLanguage);

       MapList mlObjectList = (MapList)programMap.get("objectList");
       Vector vecColumnData = new Vector(mlObjectList.size());
       final String SELECT_ATTRIBUTE_LINK_STATUS = "attribute[" + ReqSchemaUtil.getLinkStatusAttrubite(context) + "]";
       try{
           for (int i=0; i < mlObjectList.size(); i++) {
               Map mapRow = (Map)mlObjectList.get(i);
               Map mapCol = (Map)mapRow.get(nColumnIndex);
               if (mapCol != null)
               {
        	   String strValue = (String)mapCol.get(SELECT_ATTRIBUTE_LINK_STATUS);
        	   String strTranslatedValue = (String)slTranslatedLinkStatusRanges.get(slLinkStatusRanges.indexOf(strValue));
        	   String strColor = getSettingProperty(context, "emxRequirements.TraceabilityReport.Status[" + strValue + "].Color");

        	   if (!isExportFormat && !isPrinterFriendly)
        	   {
        	       if (strColor != null && strColor.length() > 0)
        	       {
        		   strTranslatedValue = "<div title=\"" + strTranslatedValue + "\" style=\"color: white; background: " + strColor + "; text-align: center; display: block; height: 1.5em; padding: .25em\">" + strTranslatedValue + "</div>";
        	       }
        	       else
        	       {
        		   strTranslatedValue = "<div title=\"" + strTranslatedValue + "\" style=\"text-align: center; display: block; height: 1.5em; padding: .25em\">" + strTranslatedValue + "</div>";
        	       }
        	   }
        	   else
        	   {
        	       if(strExportFormat == null && isExportFormat)
        	       {
        		   strTranslatedValue = "<div title=\"" + strTranslatedValue + "\" style=\"color: white; background: " + strColor + "; text-align: center; display: block; height: 1.5em; padding: .25em\">" + strTranslatedValue + "</div>";
        	       }
        	   }

        	   vecColumnData.add(strTranslatedValue);
               }
               else
               {
        	   vecColumnData.add("");
               }
           }
       }
       catch (Exception e) {
           throw new Exception(e.toString());
       }
       return vecColumnData;
   }

   /**
    * Used as Dynamic Function for column "DerivedRequirements" of table "RMTTraceabilityDerivedRequirementsOnlyTable"
    *
    * @param context The Matrix Context object
    * @param args The packed arguments sent by UI table component
    * @return MapList containing column configuration data. Each map in this list contains
    *         the corresponding column settings
    * @throws Exception if operation fails.
    * @since RMT V6R2010x
    * @author KYP
    */
   public MapList getDerivedRequirementColumns(Context context, String[] args) throws Exception
   {
       Map programMap = (Map)JPO.unpackArgs(args);
       MapList mlObjectList = (MapList)programMap.get("objectList");

       String strLanguage = (String)programMap.get("languageStr");
       final String RESOURCE_BUNDLE = "emxRequirementsStringResource";
       final String STRING_LEVEL = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE, context.getLocale(), "emxRequirements.Common.Level"); 

       // Decide how many columns do we need?
       // This is simple as the each map is arranged with integer keys 0 onwards,
       // so size of the map will be derived requirements column + 1
       // we need to find which row will have highest number of columns.
       //
       int nMaxColumns = 0;
       for (Iterator itrObjectList = mlObjectList.iterator(); itrObjectList.hasNext();)
       {
	   Map mapRow = (Map) itrObjectList.next();
	   int nColumnSize = ((Integer)mapRow.get(NUM_DERIVED_COLUMNS)).intValue(); //IR-090024V6R2012
	   if (nMaxColumns < nColumnSize)
	   {
	       nMaxColumns = nColumnSize;
	   }
       }
       if (nMaxColumns < 0)
       {
	   nMaxColumns = 0;
       }

       MapList mlColumns = new MapList();

       Map mapColumn = null;
       Map mapSettings = null;

       for (int i = 1; i <= nMaxColumns; i++)
       {

	   // Column Separator
	   //
	   mapColumn = new HashMap();
	   mapColumn.put("name", "DerReq-Sep-" + i );

	   mapSettings = new HashMap();
	   mapSettings.put("Column Type","separator");
	   mapSettings.put("Registered Suite","Requirements");
	   mapColumn.put("settings", mapSettings);

	   mlColumns.add(mapColumn);

	   // Column Derived Requirement
	   //
	   mapColumn = new HashMap();
	   mapColumn.put("name", "DerReq-Name-" + i );
	   mapColumn.put("label", "emxRequirements.Common.DerivedRequirement");

	   mapSettings = new HashMap();
	   mapSettings.put("Column Type","programHTMLOutput");
	   mapSettings.put("program","emxTraceabilityReport");
	   mapSettings.put("function","getDerivedRequirementNameColumnData");
	   mapSettings.put("Registered Suite","Requirements");
	   mapSettings.put("Group Header", STRING_LEVEL + " " + i);
	   mapSettings.put("Sortable","false");
	   mapSettings.put("Export","true");
	   mapColumn.put("settings", mapSettings);

	   mlColumns.add(mapColumn);
	   // Title column for Target Requirement
	   //
	   mapColumn = new HashMap();
	   mapColumn.put("name", "DerReq-TargetTitleForRequirement-" + i );
	   mapColumn.put("label", "emxFramework.Attribute.Title");

	   mapSettings = new HashMap();
	   mapSettings.put("Column Type","program");
	   mapSettings.put("program","emxTraceabilityReport");
	   mapSettings.put("function","getTargetRequirementTitleColumnData");
	   mapSettings.put("Registered Suite","Requirements");
	   mapSettings.put("Group Header", STRING_LEVEL + " " + i);
	   mapSettings.put("Sortable","false");
	   mapSettings.put("Export","true");
	   mapColumn.put("settings", mapSettings);

	   mlColumns.add(mapColumn);

	   // Column Revision
	   //
	   mapColumn = new HashMap();
	   mapColumn.put("name", "DerReq-Rev-" + i );
	   mapColumn.put("label", "emxRequirements.Common.Revision");
	   //mapColumn.put("href", "${COMMON_DIR}/emxTree.jsp");

	   mapSettings = new HashMap();
	   mapSettings.put("Column Type","program");
	   mapSettings.put("program","emxTraceabilityReport");
	   mapSettings.put("function","getDerivedRequirementRevisionColumnData");
	   mapSettings.put("Registered Suite","Requirements");
	   mapSettings.put("Group Header",STRING_LEVEL + " " + i);
	   mapSettings.put("Sortable","false");
	   mapSettings.put("Export","true");
	   mapColumn.put("settings", mapSettings);
	   mlColumns.add(mapColumn);

	   // Column Link Status
	   //
	   mapColumn = new HashMap();
	   mapColumn.put("name", "DerReq-LinkStatus-" + i );
	   mapColumn.put("label", "emxRequirements.Common.LinkStatus");
	   //mapColumn.put("href", "${COMMON_DIR}/emxTree.jsp");

	   mapSettings = new HashMap();
	   mapSettings.put("Column Type","programHTMLOutput");
	   mapSettings.put("program","emxTraceabilityReport");
	   mapSettings.put("function","getDerivedRequirementLinkStatusColumnData");
	   mapSettings.put("Registered Suite","Requirements");
	   mapSettings.put("Group Header",STRING_LEVEL + " " + i);
	   mapSettings.put("Sortable","false");
	   mapSettings.put("Export","true");
	   mapColumn.put("settings", mapSettings);
	   mlColumns.add(mapColumn);

	   // Column Specification
	   //
	   mapColumn = new HashMap();
	   mapColumn.put("name", "DerReq-Spec-" + i );
	   mapColumn.put("label", "emxRequirements.Table.ParentSpecs");
	   //mapColumn.put("href", "${COMMON_DIR}/emxTree.jsp");

	   mapSettings = new HashMap();
	   mapSettings.put("Column Type","programHTMLOutput");
	   mapSettings.put("program","emxTraceabilityReport");
	   mapSettings.put("function","getDerivedRequirementSpecificationColumnData");
	   mapSettings.put("Registered Suite","Requirements");
	   mapSettings.put("Group Header",STRING_LEVEL + " " + i);
	   mapSettings.put("Sortable","false");
	   mapSettings.put("Export","true");
	   mapColumn.put("settings", mapSettings);
	   mlColumns.add(mapColumn);
	   //Column Title
	   //
	   mapColumn = new HashMap();
	   mapColumn.put("name", "DerReq-Title-" + i );
	   mapColumn.put("label", "emxFramework.Attribute.Title");
	   //mapColumn.put("href", "${COMMON_DIR}/emxTree.jsp");

	   mapSettings = new HashMap();
	   mapSettings.put("Column Type","programHTMLOutput");
	   mapSettings.put("program","emxTraceabilityReport");
	   mapSettings.put("function","getDerivedRequirementSpecificationTitleColumnData");
	   mapSettings.put("Registered Suite","Requirements");
	   mapSettings.put("Group Header",STRING_LEVEL + " " + i);
	   mapSettings.put("Sortable","false");
	   mapSettings.put("Export","true");
	   mapColumn.put("settings", mapSettings);
	   mlColumns.add(mapColumn);
       }

       return mlColumns;
   }

   //End:V6R2010x:RMT Requirements Allocation Report
/**
 * Function called from RMTTraceabilityCoverageReport table to display Requirement Specification value for column Specification.
 * @param context The Matrix Context object
 * @param args The packed arguments sent by UI table component
 * @return The Vector containing data for each row of the table
 * @throws Exception if operation fails.
 * @since RMT V6R2011
 * @author OEP
 */
public Vector getTraceabilityCoverageSpecName(Context context, String[] args) throws Exception
{
    try{
    Map programMap = (Map) JPO.unpackArgs(args);
    Map paramList = (Map)programMap.get("paramList");
    String reportFormat = (String) paramList.get("reportFormat");

    MapList mlObjectList = (MapList)programMap.get("objectList");
    Vector vecColumnData = new Vector(mlObjectList.size());


    String strColor = getSettingProperty(context, "emxRequirements.TraceabilityReport.ReqReqReport.Percentage.Color");
    if (strColor == null || strColor.length() == 0) {
 	   strColor = "#336699";
   }

    try
	   {
		for (int i = 0; i < mlObjectList.size(); i++) {
		    Map map = (Map) mlObjectList.get(i);
		    String strId = (String) map.get(DomainConstants.SELECT_ID);
		    String strName = (String) map
			    .get(DomainConstants.SELECT_NAME);
		    String strTotal = (String) map.get("Total");
		    if (strId != null && !"".equals(strId)) {
			vecColumnData.addElement(strName);
		    }
		    if (strTotal != null && !"".equals(strTotal)) {
                if(reportFormat != null && !"".equals(reportFormat) && "CSV".equals(reportFormat)) {
                    vecColumnData.add(strTotal);
                }else {
                    String strHTML = "<B><div align=\"left\" title=\""
                        + strTotal
                        + "\" style=\"color: white; background: "
                        + strColor
                        + "; display: block; height: 1.5em; padding: .25em\">"
                        + strTotal + "</div></B>";
                    vecColumnData.add(strHTML);
                }
            }
        } //for
        }
           catch(Exception ex)
       {
           throw new Exception(ex.toString());
       }
       return vecColumnData;
    }
    catch (Exception e) {
	    e.printStackTrace();
	    throw e;
	}

}
/**
 * Function called from RMTTraceabilityCoverageReport table to display Percentage value for column percentage.
 * @param context The Matrix Context object
 * @param args The packed arguments sent by UI table component
 * @return The Vector containing data for each row of the table
 * @throws Exception if operation fails.
 * @since RMT V6R2011
 * @author OEP
 */
public Vector getTraceabilityCoveragePercentage(Context context, String[] args) throws Exception
{
    try {
        Map programMap = (Map) JPO.unpackArgs(args);
        Map paramList = (Map) programMap.get("paramList");
        String reportFormat    = (String) paramList.get("reportFormat");

	    MapList mlObjectList = (MapList) programMap.get("objectList");
	    Vector vecColumnData = new Vector(mlObjectList.size());

	    String strColor = getSettingProperty(context, "emxRequirements.TraceabilityReport.ReqReqReport.Percentage.Color");
	    if (strColor == null || strColor.length() == 0) {
	 	   strColor = "#336699";
	   }
	    try {
		for (int i = 0; i < mlObjectList.size(); i++) {
		    Map map = (Map) mlObjectList.get(i);
		    String strId = (String) map.get(DomainConstants.SELECT_ID);
		    String strName = (String) map.get(DomainConstants.SELECT_NAME);
		    String strPercentage = (String) map.get("percentage");
		    String strTotalPercentage = (String) map.get("totalPercentage");
		    if (strId != null && !"".equals(strId)) {
			vecColumnData.addElement(strPercentage);
		    }
		    if (strTotalPercentage != null && !"".equals(strTotalPercentage)) {
                if(reportFormat != null && !"".equals(reportFormat) && "CSV".equals(reportFormat)) {
                    vecColumnData.add(strTotalPercentage+"%");
                }else {
                    String strHTML = "<B><div align=\"left\" title=\""
                        + strTotalPercentage
                        + "\" style=\"color: white; background: "
                        + strColor
                        + "; display: block; height: 1.5em; padding: .25em\">"
                        + strTotalPercentage + "%</div></B>";
                    vecColumnData.add(strHTML);
                }
            } // IF
        } // For

	    } catch (Exception ex) {
		throw new Exception(ex.toString());
	    }
	    return vecColumnData;
	}
    catch (Exception e) {
	    e.printStackTrace();
	    throw e;
	}
}
/**
 * Get data for Percentage Coverage Report
 * @param context The Matrix Context object
 * @param args The packed arguments sent by UI table component
 * @return The Vector containing data for each row of the table
 * @throws Exception if operation fails.
 * @since RMT V6R2011
 * @author OEP
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getTraceabilityCoverage(Context context, String[] args)
throws Exception
 {
	MapList outList = new MapList();
	HashMap programMap = (HashMap) JPO.unpackArgs(args);

	 boolean reportTo = false;
	 boolean reportFrom = true;
	String reportRelationship = (String) programMap.get("reportRelationships");
	String reportDir = (String) programMap.get("reportDirection");
	if (reportDir != null)
	      {
	         if (reportDir.equals("Both"))
	         {
	            reportTo = true;
	         }
	         else if (reportDir.equals("To"))
	         {
	            reportTo = true;
	            reportFrom = false;
	         }
	      }
	String sourceSpecObjs = (String) programMap.get("sourceObjectIDs");
	String targertSpecObjs = (String) programMap.get("targetObjectIds");
	String targetChapterRequirementObjs = (String) programMap.get("targetChapterRequirement");
	String selectedType = (String) programMap.get("selectedType");
	String sExpandTypes = ReqSchemaUtil.getRequirementType(context) + "," + ReqSchemaUtil.getChapterType(context);
	String relationship = ""; // Default
	if (reportRelationship != null)
	  relationship += "," + reportRelationship;
	String sExtNameReportRels = convertInternalNames(context, relationship);

	// object selects for spec
	StringList slExpandSelects = new StringList();
	slExpandSelects.addElement(SELECT_ID);
	slExpandSelects.addElement(SELECT_TYPE);


	MapList mapSourceList = new MapList();
	MapList mapTargetList = new MapList();
	MapList mlTargetMapNew = new MapList();
	MapList mlSourceMapNew = new MapList();
	String strSpecId = null;
	Map mapTargetIds = null;
	String strRelWhere = null;

	if (targetChapterRequirementObjs == null
		|| "null".equals(targetChapterRequirementObjs)
		|| targetChapterRequirementObjs.equals("")) {
	    // Target Map Requirement

	    if (targertSpecObjs != null) {
		StringTokenizer stTargettoken = new StringTokenizer(
			targertSpecObjs, ",", false);
		while (stTargettoken.hasMoreTokens()) {
		    String sTargetId = stTargettoken.nextToken();

		    DomainObject doTargetSpecIds = DomainObject.newInstance(context, sTargetId);
		    mapTargetList = doTargetSpecIds.getRelatedObjects(context,ReqSchemaUtil.getSpecStructureRelationship(context), sExpandTypes, slExpandSelects, null,
			    false, true, (short) 0, null, null);

		    for (int ii = 0; ii < mapTargetList.size(); ii++) {
			mapTargetIds = (Map) mapTargetList.get(ii);
			String strTargetObjId = (String) mapTargetIds
				.get(SELECT_ID);
			String strTargetObjType = (String) mapTargetIds
				.get(SELECT_TYPE);
			if (!strTargetObjType.equals(ReqSchemaUtil.getChapterType(context))
				&& !strTargetObjType.contains(strTargetObjId)) {
			    mlTargetMapNew.add(mapTargetIds);
			}
		    }
		}
	    }
	} else {
	    //Start:oep:IR-026010V6R2011
	    StringTokenizer stTargetChapReqtoken = new StringTokenizer(targetChapterRequirementObjs, ",", false);
	    String strtargetObjId = null;
	    while (stTargetChapReqtoken.hasMoreElements()) {
		String strChapOrReqId = (String) stTargetChapReqtoken.nextToken();

		DomainObject doChapOrReqIds = DomainObject.newInstance(context, strChapOrReqId);
		String strChapOrRequirement = doChapOrReqIds.getInfo(context, DomainConstants.SELECT_TYPE);

		if(strChapOrRequirement.equals(ReqSchemaUtil.getRequirementType(context)))
		{
		    mlTargetMapNew.add(strChapOrReqId);
		}
		else if(strChapOrRequirement.equals(ReqSchemaUtil.getChapterType(context)))
		{
		    mapTargetList = doChapOrReqIds.getRelatedObjects(context,ReqSchemaUtil.getSpecStructureRelationship(context), sExpandTypes, slExpandSelects, null,
			    false, true, (short) 0, null, null);

		    for (int ii = 0; ii < mapTargetList.size(); ii++) {
			mapTargetIds = (Map) mapTargetList.get(ii);
			String strTargetObjId = (String) mapTargetIds.get(SELECT_ID);
			String strTargetObjType = (String) mapTargetIds.get(SELECT_TYPE);
			if (!strTargetObjType.equals(ReqSchemaUtil.getChapterType(context))
				&& !strTargetObjType.contains(strTargetObjId)) {
			    mlTargetMapNew.add(mapTargetIds);
			}
		    }
		}
	    }
	    //END:IR-026010V6R2011
	}

	// Sorce Map Requirement
	StringTokenizer stSourceToken = new StringTokenizer(sourceSpecObjs,",", false);
	int nCountOfAllocatedReqPerSpec = 0;
	int nCountOfAllAllocatedSourceReq = 0;
	int allSize = 0;
	strRelWhere = "attribute[" + ReqSchemaUtil.getLinkStatusAttrubite(context) + "]!='Invalid'"; //TODO remove Hardcoding

	while (stSourceToken.hasMoreTokens()) {
	    nCountOfAllocatedReqPerSpec = 0;
	    strSpecId = stSourceToken.nextToken();

	    mlSourceMapNew.clear();

	    DomainObject doSourceReqIds = DomainObject.newInstance(context,strSpecId);
	    String strSpecName = doSourceReqIds.getInfo(context,DomainConstants.SELECT_NAME);
	    String strSourceType = doSourceReqIds.getInfo(context,DomainConstants.SELECT_TYPE);
	    if(strSourceType.equals("Requirement"))
	    {
	    	  Map sourceIds = new HashMap();
	    	  sourceIds.put(DomainConstants.SELECT_ID, strSpecId);
	    	  mlSourceMapNew.add(sourceIds);
	    }
	    else{
	    mapSourceList = doSourceReqIds.getRelatedObjects(context,ReqSchemaUtil.getSpecStructureRelationship(context), sExpandTypes,
		    slExpandSelects, null, false, true, (short) 0, null, null);

	    // Source Requirements
	    for (int ii = 0; ii < mapSourceList.size(); ii++) {
		Map sourceIds = (Map) mapSourceList.get(ii);
		String strsourceObjId = (String) sourceIds.get(SELECT_ID);
		String strsourceObjType = (String) sourceIds.get(SELECT_TYPE);
		if (!strsourceObjType.equals(ReqSchemaUtil.getChapterType(context))
			&& !strsourceObjType.contains(strsourceObjId)) {
		    mlSourceMapNew.add(sourceIds);
		}

				}
	    }
	    int targetSize = mlTargetMapNew.size();
	    int sourceSize = mlSourceMapNew.size();
	    allSize += sourceSize;
	    nCountOfAllocatedReqPerSpec = 0;
	    int iMaxLevels = 1;
	    String targetreqIDs = null;
	    for (int ii = 0; ii < mlSourceMapNew.size(); ii++) {
		///////
		Map sSourceReqId = (Map) mlSourceMapNew.get(ii);
		String strsourceObjId = (String) sSourceReqId.get(SELECT_ID);
		DomainObject doRequirementIds = DomainObject.newInstance(context, strsourceObjId);

		//TODO


		MapList mapSourceDerivedRequirement = new MapList();

		//IR-160176V6R2013x
		if(reportTo){
			mapSourceDerivedRequirement = doRequirementIds.getRelatedObjects(context,sExtNameReportRels, ReqSchemaUtil.getRequirementType(context), slExpandSelects, null,
			reportTo, false, (short) 0, null, strRelWhere);
		}
		if(reportFrom){
			MapList mapFrom = doRequirementIds.getRelatedObjects(context,sExtNameReportRels, ReqSchemaUtil.getRequirementType(context), slExpandSelects, null,
			false, reportFrom, (short) 0, null, strRelWhere);
			mapSourceDerivedRequirement.addAll(mapFrom);
		}

		  for (int kk = 0; kk < mapSourceDerivedRequirement.size(); kk++)
		   {
			   Map mapRelObject = (Map) mapSourceDerivedRequirement.get(kk);
			   String sRelObjId = (String) mapRelObject.get(SELECT_ID);
			   if(mlTargetMapNew.size() > 0)
			   {
			   for (int j = 0; j < mlTargetMapNew.size(); j++) {
			       Object obj = (Object) mlTargetMapNew.get(j);
			       if(obj instanceof String) {
				   targetreqIDs = (String) mlTargetMapNew.get(j);
			       }else{
				   Hashtable objectMap1 = (Hashtable) mlTargetMapNew.get(j);
				    targetreqIDs = (String) objectMap1.get(SELECT_ID);
			       }
				    if (sRelObjId.equals(targetreqIDs)) {
					 nCountOfAllocatedReqPerSpec++;
					 nCountOfAllAllocatedSourceReq++;
					 kk = mapSourceDerivedRequirement.size()-1;
					break;
				    }
				    //break;
					}
			   }
			   else
			   {
			        // form the arguments and pack them  into string array.
			        HashMap hmArgs      = new HashMap(1);
			        hmArgs.put("objectId", sRelObjId);
			        String[] strArgs    = JPO.packArgs(hmArgs);

			        // get the specifications list in which the requirement is present
			        MapList mlSpecifications   = (MapList)JPO.invoke(context, "emxRequirement", null, "getRequirementSpecifications", strArgs,MapList.class );

				   if(mlSpecifications.size() > 0)
				   {
						nCountOfAllocatedReqPerSpec++;
						nCountOfAllAllocatedSourceReq++;
						break;
				   }
				}

		   }
	    }
	    // Calculation
	    double dPercentage = 0.0;
	    if (sourceSize == 0)
		dPercentage = 0.0;
	    else
		dPercentage = (100 * nCountOfAllocatedReqPerSpec) / sourceSize;
	    Map resultMap = new HashMap();
	    resultMap.put(DomainConstants.SELECT_ID, strSpecId);
	    resultMap.put(DomainConstants.SELECT_NAME, strSpecName);
	    // resultMap.put("percentage", "" + (int) dPercentage + "%");
	    resultMap.put("percentage", "" + (int)Math.floor(dPercentage) + "%");
	    outList.add(resultMap);
	} //while

	double dTotalPercentage = 0.0;
	if (allSize == 0)
	    dTotalPercentage = 0.0;
	else
	    dTotalPercentage = (100 * nCountOfAllAllocatedSourceReq) / allSize;
	int intPercentage = (int) dTotalPercentage;

	Map tempMap = new HashMap();
	tempMap.put("Total", "Total");
	tempMap.put("totalPercentage", "" + (int)Math.floor(dTotalPercentage));

	outList.add(tempMap);
	return (outList);
}
/**
 * Get Requirement Specification Title Attribute.
 * @param context The Matrix Context object
 * @param args The packed arguments sent by UI table component
 * @return The Object containing data for each row of the table
 * @throws Exception if operation fails.
 * @since RMT V6R2011
 * @author OEP
 *
 */
	public List getSourceTitle(Context context, String args[]) throws Exception
	{
		final String SYMB_COMMA = ",";
		final String STR_OBJECT_LIST = "objectList";
        // unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);
        List lstobjectList = (MapList) programMap.get(STR_OBJECT_LIST);
        Iterator objectListItr = lstobjectList.iterator();
        Map objectMap = new HashMap();
        String strObjId = DomainConstants.EMPTY_STRING;
        List lstNameRev = new StringList();
        String typeIcon = "";
        String defaultTypeIcon = "";
        StringList derList = new StringList();
        StringList specList;
        StringBuffer sbTypeSelect=new StringBuffer(60);
        sbTypeSelect.append(ReqSchemaUtil.getSpecificationType(context));
        sbTypeSelect.append(SYMB_COMMA);
        sbTypeSelect.append(ReqSchemaUtil.getChapterType(context));

        DomainObject domObj = null;
        // mqlcommand retrieves all the derivatives of type Specification
        String strQuery = "print type $1 select $2 dump $3";
        String strResult = MqlUtil.mqlCommand(context, strQuery, ReqSchemaUtil.getSpecificationType(context), "derivative", ",");
        // derList contains all the derivatives of type Specification
        if (!"".equals(strResult) && !"null".equals(strResult) && strResult!=null)
        {
            derList =  FrameworkUtil.split(strResult, ",");
        }
        // loop through all the records and returns all the
        //  related specifications with comma separated
        while(objectListItr.hasNext())
        {
            specList = new StringList();
            StringBuffer stbNameRev = new StringBuffer();
            objectMap = (Map) objectListItr.next();
            strObjId = (String)objectMap.get(DomainConstants.SELECT_ID);
            domObj = DomainObject.newInstance(context, strObjId);
            StringList objSelect = new StringList();
            objSelect.add("id");
            objSelect.add("name");
            objSelect.add("type");

            StringList relSelect = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            // getting all related Specification Objects which are connected directly
            // or connected through the chapters */
            MapList specObjMapList = domObj.getRelatedObjects(context,ReqSchemaUtil.getSpecStructureRelationship(context),
                sbTypeSelect.toString(), objSelect, relSelect, true, false, (short)0, null, null);

            // Start:oep
			MapList dummyListSpecId = new MapList();
			ArrayList alreadyAdddedids = new ArrayList();
			for (int ii = 0; ii < specObjMapList.size(); ii++)
			{
				Map strSpecMap = (Map) specObjMapList.get(ii);
				String strId = (String)strSpecMap.get("id");
				if(!alreadyAdddedids.contains(strId))
				{
					alreadyAdddedids.add(strId);
					dummyListSpecId.add(strSpecMap);
				}
			}
			specObjMapList = dummyListSpecId;
			//End:oep



            Iterator itr = specObjMapList.iterator();
            // Iterating through each Specification Object and making it hyperlink
            while(itr.hasNext())
            {
                Map reqObjMap = (Map)itr.next();
                String strSpecObjId = (String)reqObjMap.get("id");
                String strObjName = (String)reqObjMap.get("name");
                String strObjType = (String)reqObjMap.get("type");
                typeIcon = UINavigatorUtil.getTypeIconProperty(context, strObjType);
                defaultTypeIcon = "<img src=\"../common/images/" +typeIcon+ "\" border=\"0\" />";
                // Adding to the maplist only derivatives of Specification
                // and ignoring the Chapter types
                if (derList.contains(strObjType) && !specList.contains(strSpecObjId))
                {
                	String strTitleQuery = "print bus $1 select $2 dump $3";
					String strTitleResult = MqlUtil.mqlCommand(context, strTitleQuery, strSpecObjId, "attribute[Title]", "|");

					if (strTitleResult != null && !strTitleResult.equals("") && !"null".equals(strTitleResult))
					{
						stbNameRev.append(", ");
						stbNameRev.append(strTitleResult);
					}
					else
					{
						stbNameRev.append(", ");
						//stbNameRev.append(strObjName);
					}
                }
            }

            if (stbNameRev.length()>0)
                stbNameRev.deleteCharAt(0);

            lstNameRev.add(stbNameRev.toString());
        }
        return(lstNameRev);

	}

/**
 * Get Requirement Specification Title Attribute.
 * @param context The Matrix Context object
 * @param args The packed arguments sent by UI table component
 * @return The Object containing data for each row of the table
 * @throws Exception if operation fails.
 * @since RMT V6R2011
 * @author OEP
 */
	public Object getTargetTitle(Context context, String args[]) throws Exception
	{
	       final String SYMB_COMMA      = ",";
	       final String STR_OBJECT_LIST = "objectList";

	       // unpack the arguments
	       Map programMap               = (HashMap) JPO.unpackArgs(args);

	       // get the target Specification ID's (if any)
	       Map paramMap                 = (Map) programMap.get("paramList");
	       String objectId              = (String) paramMap.get("baselineObject");
	       Vector mpSpecName            = new Vector();

	       StringTokenizer toker        = new StringTokenizer(objectId, ",", false);
	       while (toker.hasMoreTokens())
	       {
	          String sourceId           = toker.nextToken();
	          DomainObject doSpec       = DomainObject.newInstance(context,sourceId);
	          String strSpecName        = doSpec.getInfo(context, DomainConstants.SELECT_NAME);
	          mpSpecName.add(strSpecName);
	       }

	       // get Specification child types
	       List lstSpecChildTypes       = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getSpecificationType(context));
	       StringBuffer sbSpecChildTypes= new StringBuffer(ReqSchemaUtil.getSpecificationType(context));
	       sbSpecChildTypes.append(SYMB_COMMA);

	       for (int i=0; i < lstSpecChildTypes.size(); i++)
	       {
	           sbSpecChildTypes.append(lstSpecChildTypes.get(i));

	           if (i != lstSpecChildTypes.size()-1)
	           {
	               sbSpecChildTypes = sbSpecChildTypes.append(SYMB_COMMA);
	           }
	       }

	       String strObjId          = DomainConstants.EMPTY_STRING;
	       String strTargetId       = DomainConstants.EMPTY_STRING;
	       String typeIcon          = DomainConstants.EMPTY_STRING;
	       String defaultTypeIcon   = DomainConstants.EMPTY_STRING;
	       String strSpecObjId      = DomainConstants.EMPTY_STRING;
	       String strSpecName       = DomainConstants.EMPTY_STRING;
	       String strObjType        = DomainConstants.EMPTY_STRING;
	       String targetSpecName    = DomainConstants.EMPTY_STRING;

	       StringList objSelects    = new StringList();
	       objSelects.addElement(DomainConstants.SELECT_NAME);
	       objSelects.addElement(DomainConstants.SELECT_ID);
	       objSelects.addElement(DomainConstants.SELECT_TYPE);

	       List lstobjectList       = (MapList) programMap.get(STR_OBJECT_LIST);
	       Iterator objectListItr   = lstobjectList.iterator();
	       Map objectMap            = new HashMap();

	       MapList specObjMapList   = null;
	       Map specObjMap           = null;
	       Iterator specObjItr      = null;
	       DomainObject domObj      = null;
	       StringBuffer stbNameRev  = null;
	       List specList            = null;
	       List lstNameRev          = new StringList();

	       //  loop through all the records and returns all the
	       //  related specifications with comma separated
	       while(objectListItr.hasNext())
	       {
	          specList              = new StringList();
	          stbNameRev            = new StringBuffer();
	          objectMap             = (Map) objectListItr.next();
	          strObjId              = (String)objectMap.get(DomainConstants.SELECT_ID);

	          if(strObjId !=null)
	          {
	               strTargetId      = (String)objectMap.get("target.id");
	               if( strTargetId != null)
	               {
	                   domObj       = DomainObject.newInstance(context, strTargetId);

	                   // getting all related Specification Objects
	                   specObjMapList   = domObj.getRelatedObjects(context,
	                                           ReqSchemaUtil.getSpecStructureRelationship(context),    // relPattern
	                                           DomainConstants.QUERY_WILDCARD,          // typePattern
	                                           true,                                    // to
	                                           false,                                   // from
	                                           0,                                       // recursionLevel
	                                           objSelects,                              // objectSelects
	                                           null,                                    // relationshipSelects
	                                           null,                                    // busWhereClause
	                                           null,                                    // relWhereClause
	                                           null,                                    // postRelPattern
	                                           sbSpecChildTypes.toString(),             // postTypePattern
	                                           null);                                   // postPatterns

	                   // Iterating through each Specification Object and making it hyperlink
	                   specObjItr = specObjMapList.iterator();
	                   while(specObjItr.hasNext())
	                   {
	                       specObjMap       = (Map)specObjItr.next();
	                       strSpecObjId     = (String)specObjMap.get(DomainConstants.SELECT_ID);
	                       strSpecName      = (String)specObjMap.get(DomainConstants.SELECT_NAME);
	                       strObjType       = (String)specObjMap.get(DomainConstants.SELECT_TYPE);
	                       typeIcon         = UINavigatorUtil.getTypeIconProperty(context, strObjType);
	                       defaultTypeIcon  = "<img src=\"../common/images/" +typeIcon+ "\" border=\"0\" />";

	                       // Adding to the maplist only derivatives of Specification
	                       if(mpSpecName.size()==0)
	                       {
	                           domObj           = DomainObject.newInstance(context, strSpecObjId);
	                           String strTitleQuery = "print bus $1 select $2 dump $3";
		                   String strTitleResult = MqlUtil.mqlCommand(context, strTitleQuery, strSpecObjId, "attribute[Title]", "|");

		                   if (strTitleResult != null && !strTitleResult.equals("") && !"null".equals(strTitleResult))
		                   {
		                       stbNameRev.append(", ");
		                       stbNameRev.append(strTitleResult);
		                   }
		                   else
		                   {
		                       stbNameRev.append(", ");
		                       //stbNameRev.append(strSpecName);
		                   }
	                       }
	                       else
	                       {// IS targetIds are selected
	                           for (int jj = 0; jj < mpSpecName.size(); jj++)
	                           {
	                               targetSpecName   = (String)mpSpecName.elementAt(jj);
	                               if(targetSpecName.equalsIgnoreCase(strSpecName) && !specList.contains(strSpecObjId))
	                               {
	                        	   String strTitleQuery = "print bus $1 select $2 dump $3";
			                   String strTitleResult = MqlUtil.mqlCommand(context, strTitleQuery, strSpecObjId, "attribute[Title]", "|");

			                   if (strTitleResult != null && !strTitleResult.equals("") && !"null".equals(strTitleResult))
			                   {
			                       stbNameRev.append(", ");
			                       stbNameRev.append(strTitleResult);
			                   }
			                   else
			                   {
			                       stbNameRev.append(", ");
			                       //stbNameRev.append(strSpecName);
			                   }
	                               }
	                           }
	                       }
	                   }// inner while
	               }
	           }

	           if (stbNameRev.length()>0)
	           {
	               stbNameRev.deleteCharAt(0);
	           }

	           lstNameRev.add(stbNameRev.toString());
	       }// outer while
	       return(lstNameRev);
	}
	   /**
	    * Returns values for "RootRequirementsTitle" column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
	    *
	    * @param context The Matrix Context object
	    * @param args The packed arguments sent by UI table component
	    * @return The Vector containing data for each row of the table
	    * @throws Exception if operation fails.
	    * @since RMT V6R2010x
	    * @author KYP
	    */
	   public Vector getTargetRequirementTitleColumnData(Context context, String[] args) throws Exception
	   {
	       Map programMap = (Map) JPO.unpackArgs(args);
	       Map columnMap = (Map)programMap.get("columnMap");

	       String strColumnName = (String)columnMap.get("name");
	       Integer nColumnIndex = new Integer(0);
	       String strObjId = "";
	       try
	       {
		   nColumnIndex = new Integer(FrameworkUtil.findAndReplace(strColumnName, "DerReq-TargetTitleForRequirement-", ""));
	       }
	       catch(Exception exp)
	       {
		   throw new IllegalStateException("Column name:"+ strColumnName);
	       }

	       MapList mlObjectList = (MapList)programMap.get("objectList");
	       Vector vecColumnData = new Vector(mlObjectList.size());

	       try{
	           for (int i=0; i < mlObjectList.size(); i++) {
	               Map mapRow = (Map)mlObjectList.get(i);
	               Map mapCol = (Map)mapRow.get(nColumnIndex);
	               if (mapCol != null)
	               {
	            	 strObjId = (String) mapCol.get(SELECT_TO_ID);
	        	   // String strValue = (String)mapCol.get(SELECT_TO_REVISION);
	        	   // vecColumnData.add(strValue);
		            	 if(strObjId != null)
		            	 {
		            		String strTitleQuery = "print bus $1 select $2 dump $3";
		                	String strTitleResult = MqlUtil.mqlCommand(context, strTitleQuery, strObjId, "attribute[Title]", "|");

							if (strTitleResult != null && !strTitleResult.equals("") && !"null".equals(strTitleResult))
							{
								vecColumnData.add(strTitleResult);
							}
							else
							{
								vecColumnData.add("");
							}
		               }
		               else
		               {
		        	   vecColumnData.add("");
		               }
	               }
	               else
	               {
	            	   vecColumnData.add("");
	               }
	         } //for
	       }
	       catch (Exception e) {
	           throw new Exception(e.toString());
	       }
	       return vecColumnData;
	   }

	   /**
	    * Returns values for "RootRequirementsRevision" column in table "RMTTraceabilityDerivedRequirementsOnlyTable"
	    *
	    * @param context The Matrix Context object
	    * @param args The packed arguments sent by UI table component
	    * @return The Vector containing data for each row of the table
	    * @throws Exception if operation fails.
	    * @since RMT V6R2010x
	    * @author KYP
	    */
	   public Vector getRootDerivationRequirementTitleColumnData(Context context, String[] args) throws Exception
	   {
		   try
		   {
			   Map programMap = (Map) JPO.unpackArgs(args);
			   MapList mlObjectList = (MapList)programMap.get("objectList");
			   Vector vecColumnData = new Vector(mlObjectList.size());

			   // Keep a record to combine the requirements
			   Set idTreated = new HashSet<String>();
			   String strTitleQuery = "print bus $1 select $2 dump $3";

			   for (int i=0; i < mlObjectList.size(); i++)
			   {
				   Map mapRow = (Map)mlObjectList.get(i);
				   Map mapCol = (Map)mapRow.get(new Integer(0));
				   String strRootReqId = (String) mapRow.get(SELECT_ID);
				   String strHTML = "";

				   // If we are in a Derivation Traceability Matrix
				   if (mapCol != null)
				   {
					   String strObjId = (String) mapCol.get(SELECT_TO_ID); idTreated.add(strObjId);					   
					   strHTML = MqlUtil.mqlCommand(context, strTitleQuery, strObjId, "attribute[Title]", "|");
				   }

				   // If we are in a Sub/Derivation Traceability Matrix, and if we haven't combine this requirement yet
				   else if (strRootReqId != null && !idTreated.contains(strRootReqId)) {
					   idTreated.add(strRootReqId);			   
					   strHTML = MqlUtil.mqlCommand(context, strTitleQuery, strRootReqId, "attribute[Title]", "|");
				   }

				   vecColumnData.addElement(strHTML);
			   }
			   return vecColumnData;
		   }
		   catch (Exception e)
		   {
			   throw e;
		   }
	   }

	    /**
	    * Method is used to expand Relationship for Specification Test Case
	    *
	    * @param context The Matrix Context object
	    * @param args The packed arguments sent by UI table component
	    * @return String Expand relationship name
	    * @throws Exception if operation fails.
	    * @since RMT V6R2012x
	    */
	   public String getExpandRelationshipsForSpecTestCase(Context context, String[] args) throws Exception
	   {
		   String expandRel = ReqSchemaUtil.getSpecStructureRelationship(context) + ",";
		   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_REPORT_REQ_TESTCASE_INC_DERIVED_REQ) ? ReqSchemaUtil.getDerivedRequirementRelationship(context) : "";
		   expandRel += expandRel.length() > 0 ? "," : "";
		   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_REPORT_REQ_TESTCASE_INC_SUB_REQ) ? ReqSchemaUtil.getSubRequirementRelationship(context) : "";
		   return expandRel;
	   }

	   /**
	    * Method is used to expand Relationship for Specification Feature
	    *
	    * @param context The Matrix Context object
	    * @param args The packed arguments sent by UI table component
	    * @return String Expand relationship name
	    * @throws Exception if operation fails.
	    * @since RMT V6R2012x
	    */
	   public String getExpandRelationshipsForSpecFeature(Context context, String[] args) throws Exception
	   {
		   String expandRel = ReqSchemaUtil.getSpecStructureRelationship(context) + ",";
		   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_REPORT_REQ_FEATURE_INC_DERIVED_REQ) ? ReqSchemaUtil.getDerivedRequirementRelationship(context) : "";
		   expandRel += expandRel.length() > 0 ? "," : "";
		   expandRel += RequirementsCommon.getBooleanPreference(context, RequirementsCommon.PREF_REPORT_REQ_FEATURE_INC_SUB_REQ) ? ReqSchemaUtil.getSubRequirementRelationship(context) : "";
		   return expandRel;
	   }
//Start:IR:186487R2013x:LX6
	   /**
	    *  returns the value of each parameter of the table.
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
			   MapList objectListMap = (MapList) programMap.get("objectList");
			   if(objectListMap!=null)
			   {
				   int size = objectListMap.size();
				   String[] busID = new String[size];
				   for (int i = 0; i < size; i++)
				   {
					   if (objectListMap.get(i).getClass().isAssignableFrom(HashMap.class))
						   busID[i] = (String) ((HashMap)objectListMap.get(i)).get("target.id");
					   else if (objectListMap.get(i).getClass().isAssignableFrom(Hashtable.class))
						   busID[i] = (String) ((Hashtable)objectListMap.get(i)).get("target.id");
				   }

				   for (int i = 0; i < size; i++)
				   {			
					   // ++ IR-386290-3DEXPERIENCER2017x
					   String value = "";
					   String strParameterText = "";
					   String unit = "";
					   IPlmParameter plmPara = ParameterInterfacesServices.getParameterById(context,busID[i]);
					   IPlmParameterDisplay parmDisplay = (IPlmParameterDisplay)plmPara;
					   PLMParm_ValuationType valuationType = plmPara.getValuationType(context);
					   String paraType = plmPara.getDimension(context).getName();
					  
					   if(paraType.equals(ParameterTypes.Boolean))
					   {
				    		strParameterText += parmDisplay.getValueForDisplay(context);
				       }
					   else if(paraType.equals(ParameterTypes.String))
					   {
							if(valuationType.equals(PLMParm_ValuationType.MULTI))
							{
								IKweList multiValues = parmDisplay.getMultiValuesForDisplay(context);
								
								for (int j=1; j<=multiValues.GetSize(); j++)
									strParameterText +=multiValues.GetItem(j).asString()+",";

							}
							else if(valuationType.equals(PLMParm_ValuationType.SIMPLE))
							{
								value = ""+parmDisplay.getValueForDisplay(context);	
							}
							if(!"null".equals(value) && !value.equals(""))
							{
								strParameterText += value;
							}else
							{
				        		strParameterText += "  ";
				        	}
				    	}
						else
				    	{
							if(null != plmPara.getDisplayUnit(context))
								unit = plmPara.getDisplayUnit(context).getNLSName(context);
							
				    		if(valuationType.equals(PLMParm_ValuationType.SIMPLE))
				    		{
				    			value = ""+plmPara.getValue(context);
				    			
				    			if(!"null".equals(value) && !value.equals(""))
				    			{
				    				strParameterText +=parmDisplay.getValueForDisplay(context);
				    			}
				    			else
				    			{
				    				strParameterText += value;
				    			}
				    		}
				    		else
				    		{
				        		strParameterText += "null";
				    		}
				    	}
					   // -- IR-386290-3DEXPERIENCER2017x
					   if(value != null) 
					   {
						   if(unit.length() != 0)
						   {
							   returnList.add(strParameterText + " " + unit);
						   }
						   else
						   {
							   if(plmPara.getDimension(context).getName().equals(ParameterTypes.Boolean))
							   {
								   returnList.add(strParameterText);
							   }
							   else
								   returnList.add(strParameterText);
						   }
					  }
					   else
						   returnList.add(null);
				   }	
			   }
			   else
			   {
				   returnList = null;
			   }
			   return returnList;
		 }

		 /**
		    *  returns the interface attribute of each parameter of the table.
		    *
		    * @param context the eMatrix <code>Context</code> object
		    * @param args not used
		    * @return The Parameter Value
		    * @throws Exception if the operation fails
		    * @since RequirementsManagement V6R2013x
		    */
			 public  static Object getParameterType(Context context, String[] args)
			 throws Exception
			 {
				 Vector returnList = new Vector();
				 HashMap programMap = (HashMap) JPO.unpackArgs(args);

			     // Get the 'paramMap' HashMap from the programMap (for the created Object ID)
			     MapList objectListMap = (MapList) programMap.get("objectList");
			     String[] rowIds = new String[objectListMap.size()];
			     // Put all the row Ids into an array...
				 for (int iCount = 0; iCount < objectListMap.size(); iCount++)
				 {
					 // Get the object id for each object in the table...
					 Map mapRow = (Map)objectListMap.get(iCount);
					 rowIds[iCount] = (String)mapRow.get("id");
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
					returnList.addElement(ParamValue);
				 }
				 return returnList;
			 }
//End:IR:186487R2013x:LX6	 

//Start:LX6:display Clone information feature			 
			 @com.matrixone.apps.framework.ui.ProgramCallable
			 public MapList getCloneInfo(Context context, String[] args) throws Exception{
				 HashMap programMap = (HashMap)JPO.unpackArgs(args);

				 String relId = (String) programMap.get("relId");
				 String sectionId = (relId != null && relId.startsWith("0.0.0.")? relId.substring(6): "");
				 String objectId = (String) programMap.get("objectId");
				 String parentId = (String) programMap.get("parentId");
				 DomainObject domObject = DomainObject.newInstance(context, objectId);
				 // String objectType = TYPE_REQUIREMENT;
				 String objectType = "";
				 if (domObject.openObject(context))
				 {
					 objectType = domObject.getTypeName();
					 domObject.close(context);
				 }

				 // Initialize the folder data:
				 MapList folderData = new MapList();

				 // Find all the expand Programs as defined in the emxRequirements.properties file:
				 int sectionCnt = 1;
				 String sectionKey = "emxRequirements.ClonesInfo.ExpandSetClone" + sectionCnt;
				 String resourceKey = getSettingProperty(context, sectionKey);

				 while (resourceKey != null && !resourceKey.equals(""))
				 {
					 boolean isAlreadyshown = false;
					 HashMap paramMap = (HashMap) programMap.clone();
					 MapList sectList = fullTraceabilityFolderData(context, paramMap, objectId, sectionCnt, sectionKey, resourceKey);

					 // Append any children for this section to the output list...
					 if (sectList != null && !sectList.isEmpty())
					 {
						 for(int i=0;i<sectList.size();i++){
							 if(sectList.get(i).getClass().toString().contains("Hashtable")){
								 Hashtable childMap = (Hashtable)sectList.get(i);
								 String id = (String)childMap.get("id");//(String)hId.get("id");
								 if(id.equalsIgnoreCase(parentId)) {
									 isAlreadyshown = true;
								 }
							 }
						 }
						 if(isAlreadyshown == false) {
							 folderData.addAll(sectList);
						 }
					 }

					 sectionCnt++;
					 sectionKey = "emxRequirements.ClonesInfo.ExpandSetClone" + sectionCnt;
					 resourceKey = getSettingProperty(context, sectionKey);
				 }
				 HashMap expandMap = new HashMap();
				 expandMap.put("expandMultiLevelsJPO", "true");
				 expandMap.put("updateTableCache", "false");
				 folderData.add(expandMap);
				 // End:R207:RMT Bug 371708
				 //SpecificationStructure.printIndentedList("Traceability Folder Data:", folderData);
				 return(folderData);
			 }
//End:LX6:display Clone information feature
	@com.matrixone.apps.framework.ui.ProgramCallable		 
	public List getStatusLinkInformation(Context context, String[] args) throws Exception{
		List values = new StringList();
		Map programMap = (Map) JPO.unpackArgs(args);
		MapList theData = (MapList)programMap.get("objectList");
		HashMap columnMap = (HashMap) programMap.get("columnMap");
		String[] linkStatus = new String[theData.size()];
		String[] directions = new String[theData.size()];
		String  data ;
		String htmlLink;
		for(int i=0;i<theData.size();i++){
			Map objectInfo = (Map)theData.get(i);
			String relId = (String)objectInfo.get("id[connection]");
			linkStatus[i] = DomainRelationship.newInstance(context, relId).getAttributeValue(context, ReqSchemaUtil.getLinkStatusAttrubite(context));
			directions[i] = (String)objectInfo.get("direction");
			if ("-->".equals(directions[i]) || "<--".equals(directions[i]))
	        {
				data = getIcon(directions[i], linkStatus[i]);
				htmlLink = "<img src=\""+ getIcon(directions[i], linkStatus[i]) + "\" border=\"0\"  align=\"middle\"" + " /> ";
				htmlLink += "<span style=\"display:none\">"+linkStatus[i]+"</span>";
				values.add(htmlLink);
	        }
			
		}
		return values;
	}
	
	@com.matrixone.apps.framework.ui.ProgramCallable	
	public List isLinkStatusColumnEditable(Context context, String[] args) throws Exception {
		boolean isEditable = false;
		StringList editableValues = new StringList();
		Map programMap = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map) programMap.get("requestMap");
		MapList theData = (MapList)programMap.get("objectList");
		String isFromfloatingDiv = (String)requestMap.get("fromFloatingDiv");
		if(isFromfloatingDiv!=null&&isFromfloatingDiv.equalsIgnoreCase("true")){
			isEditable=true;
		}
		for(int i=0;i<theData.size();i++){
			editableValues.add(isEditable);
		}
		return editableValues;
	}
	
	@com.matrixone.apps.framework.ui.ProgramCallable	
	public Boolean hideRelColumn(Context context, String[] args) throws Exception
	{
		Boolean show = true;
		Map programMap = (Map) JPO.unpackArgs(args);
		HashMap columnMap = (HashMap) programMap.get("columnMap");
		MapList rowData = (programMap.containsKey("objectList")? (MapList) programMap.get("objectList"): tableData);
		String isFromfloatingDiv = (String)programMap.get("fromFloatingDiv");
		show=(isFromfloatingDiv!=null&&isFromfloatingDiv.equalsIgnoreCase("true")?false:true);
		return show;
	}

	@com.matrixone.apps.framework.ui.ProgramCallable	
	public Boolean hideContentColumn(Context context, String[] args) throws Exception
	{
		Boolean show = true;
		Map programMap = (Map) JPO.unpackArgs(args);
		HashMap columnMap = (HashMap) programMap.get("columnMap");
		MapList rowData = (programMap.containsKey("objectList")? (MapList) programMap.get("objectList"): tableData);
		String isFromfloatingDiv = (String)programMap.get("fromFloatingDiv");
		show=(isFromfloatingDiv!=null&&isFromfloatingDiv.equalsIgnoreCase("true")?true:false);
		return show;
	}
}

