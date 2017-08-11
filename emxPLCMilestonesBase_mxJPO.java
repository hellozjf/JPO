/* emxPLCMilestonesBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.36.2.1 Thu Dec  4 07:55:16 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.36 Tue Oct 28 18:55:12 2008 przemek Experimental przemek $
*/

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.util.Pattern;
import matrix.util.StringList;
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
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.productline.MilestoneTrack;
import com.matrixone.apps.productline.Model;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;


/**
 * The <code>emxTaskBase</code> class represents the Task JPO
 * functionality for the AEF type.
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxPLCMilestonesBase_mxJPO extends emxDomainObject_mxJPO
{
	
	public static final String SUITE_KEY ="ProductLine";
	
	 /**
     * Constructs a new emxTask JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the id
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */
    public emxPLCMilestonesBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    	  super(context, args);
    }
    
    
   
 /**
  * gets the Products connected by Related Projects Relationship with Project
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  * @returns Object
  * @throws Exception if the operation fails
  * @since R215
  * @grade 0
  */
 public Vector getProductConnectedAsRoadmapProject(Context context, String[] args) throws Exception 
 {
	 Vector VProducts = new Vector();
	 try{
		 HashMap arguMap = (HashMap)JPO.unpackArgs(args);

		 HashMap paramList = (HashMap)arguMap.get("paramList");
		 MapList objectList = (MapList) arguMap.get("objectList");

		 String strProductId = (String) paramList.get("objectId");


		 Iterator objectListItr = objectList.iterator();
		 while(objectListItr.hasNext()){
			 VProducts.add(strProductId);
		 }
     
	 }catch(Exception ex){
		 ex.printStackTrace();
	 }
	 
	 return VProducts;
 }
 
 
 
 /**
  * gets the Project Milestone
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  * @returns Object
  * @throws Exception if the operation fails
  * @since R215
  * @grade 0
  */
 public Vector getProductMilestone(Context context, String[] args) throws Exception {
	 Vector VMilestonesProject = new Vector();
	 try{
		 HashMap arguMap = (HashMap)JPO.unpackArgs(args);
		 HashMap paramList = (HashMap)arguMap.get("paramList");
		 MapList objectList = (MapList) arguMap.get("objectList");
		 String reportFormat = (String)paramList.get("reportFormat");
		 Iterator objectListItr = objectList.iterator();
		
		 while(objectListItr.hasNext()){
			 StringList slMT = new StringList();
			 StringBuffer strBuffer = new StringBuffer();
			 MapList mlResult = new MapList();
			 Map object = (Map) objectListItr.next();
			 if(object!=null && object.size()>0){
				 String ProjectMilestone = (String)object.get(DomainConstants.SELECT_ID);
				 DomainObject domProjectMilestone = new DomainObject(ProjectMilestone);
				 StringList objectSelects = new StringList();
				 objectSelects.add(SELECT_ID);
				 objectSelects.add(SELECT_NAME);
				 objectSelects.add(SELECT_TYPE);
				 objectSelects.add(SELECT_REVISION);

				 String strSelectType = ProductLineConstants.TYPE_MILESTONE_TRACK + "," + ProductLineConstants.TYPE_PRODUCTS ;
				 StringList relationshipSelects = new StringList();

				 StringBuffer sbTypeIncludePattern = new StringBuffer();
				 List lstProjectSpaceChildTypes = ProductLineUtil.getChildrenTypes(context, ProductLineConstants.TYPE_PRODUCTS);
				 lstProjectSpaceChildTypes.add(ProductLineConstants.TYPE_MILESTONE_TRACK);
				 if(lstProjectSpaceChildTypes.size()>0)
					 for (int i = 0; i < lstProjectSpaceChildTypes.size(); i++) {
						 sbTypeIncludePattern.append(lstProjectSpaceChildTypes.get(i));
						 if (i != lstProjectSpaceChildTypes.size() - 1) {
							 sbTypeIncludePattern.append(",");
						 }
					 }

				 Pattern strTypeIncludePattern=new Pattern(sbTypeIncludePattern.toString());
				 String strRelation = ProductLineConstants.RELATIONSHIP_CONFIGURATION_ITEM + "," +ProductLineConstants.RELATIONSHIP_CONFIGURATION_CRITERIA;

				 MapList mlPRojMile = domProjectMilestone.getRelatedObjects(context,
						 strRelation,
						 strSelectType,
						 objectSelects,
						 relationshipSelects,
						 true,	//to relationship
						 false,	//from relationship
						 (short)0,
						 DomainConstants.EMPTY_STRING,
						 DomainConstants.EMPTY_STRING,
						 0,
						 strTypeIncludePattern,
						 null,
						 null);

				 for(int i=0;i<mlPRojMile.size();i++){
					 Map mapProjMile = (Map)mlPRojMile.get(i);
					 String strObj =(String)mapProjMile.get(SELECT_ID);
					 String strObjName = (String)mapProjMile.get(SELECT_NAME);
					 String strRevision = (String)mapProjMile.get(SELECT_REVISION);
					 String strType = (String)mapProjMile.get(SELECT_TYPE);
					 StringBuffer sbHead = new StringBuffer();
					 StringBuffer sbHref = new StringBuffer();
					 DomainObject domPord = new DomainObject(strObj);
					 if(domPord.isKindOf(context,ProductLineConstants.TYPE_HARDWARE_PRODUCT)){
						 if(!ProductLineCommon.isNotNull(reportFormat)) {
							 sbHead.append("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert");
							 sbHead.append("&amp;objectId=" + XSSUtil.encodeForHTMLAttribute(context,strObj) + "'");
							 sbHead.append(", '800', '700', 'true', 'popup')\">");
							 sbHref.append(sbHead);
							 sbHref.append(XSSUtil.encodeForXML(context, strObjName));
							 sbHref.append(XSSUtil.encodeForXML(context,strRevision));
							 sbHref.append("</a>");
							 VMilestonesProject.add(sbHref.toString());
						 }else{
							 sbHref.append(strObjName);
							 VMilestonesProject.add(sbHref.toString());
						 }
					 }
				 }
			 }
		 }

	 }catch(Exception ex){
		 ex.printStackTrace();
	 }
	 return VMilestonesProject;
 }
 
 
 /**
  * gets the Milestone Track objects discipline attribute value to display in MT Actions table
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  * @returns Object
  * @throws Exception if the operation fails
  * @since R215
  * @grade 0
  */
 public Vector getMilestoneTrackDisciplines(Context context, String[] args) throws Exception {
	 Vector VMilestoneDiscipline = new Vector();
	 try{
		 HashMap arguMap = (HashMap)JPO.unpackArgs(args);
		 MapList mlMT = (MapList)arguMap.get("objectList");
		 String strLanguage = context.getSession().getLanguage();
		 Iterator objectListItr = mlMT.iterator();
		 while (objectListItr.hasNext()) {
			 Map objectMap = (Map) objectListItr.next();
			 String strMT = (String)objectMap.get(SELECT_ID);
			 DomainObject domMT = new DomainObject(strMT);
			 Map mapMT = (Map)domMT.getAttributeMap(context,true);
			 String strDiscipline =(String)mapMT.get(ProductLineConstants.SELECT_ATTRIBUTE_MILESTONE_DISCIPLINE);
			 if("Engineering".equals(strDiscipline)){
				 strDiscipline = "emxFramework.Range.Milestone.Engineering";
			 }else if("Manufacturing".equals(strDiscipline)){
				 strDiscipline ="emxFramework.Range.Milestone.Manufacturing";
			 }
			 VMilestoneDiscipline.add(EnoviaResourceBundle.getProperty(context, "Framework",strDiscipline, strLanguage));
		 }

	 }catch(Exception ex){
		 ex.printStackTrace(); 
	 }
	 return VMilestoneDiscipline;
 }
 
 

/**
  * This trigger function updates the Milestone Date attribute if the PRoject Estiamted Date is modified
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  *        0 - String containing the object id
  * @throws Exception if operation fails
  * @since PRG R215
  */
 public void updateMilestoneDate(Context context, String[] args) throws Exception
 {
	 String transHistories = args[0];
	 StringBuffer subTransHistory = new StringBuffer();
	 StringList strProjectMilestoneobjectId = new StringList();
	 subTransHistory.append(transHistories);
	 int idIndex = transHistories.indexOf("id=");
	 while (subTransHistory.length()!=0 && idIndex!=-1){
		 int itypeIndex = subTransHistory.indexOf("type=");
         if(itypeIndex != -1){
        	 String strType = (subTransHistory.substring(subTransHistory.lastIndexOf("type=")+5,subTransHistory.lastIndexOf("triggerName=")).trim());
        	 if("businessobject".equals(strType) && !"null".equals(strType)){
        	 strProjectMilestoneobjectId.add(subTransHistory.substring(subTransHistory.lastIndexOf("id=")+3,subTransHistory.lastIndexOf("type=")).trim());
        	 }
         }
         subTransHistory.delete(subTransHistory.lastIndexOf("id="),subTransHistory.length());
	 }
	 
	 if(strProjectMilestoneobjectId.size()!=0)
	 for(int i=0;i< strProjectMilestoneobjectId.size();i++){
		 if(null!=strProjectMilestoneobjectId.elementAt(i) && !"null".equalsIgnoreCase((String) strProjectMilestoneobjectId.elementAt(i))&&!"".equalsIgnoreCase((String) strProjectMilestoneobjectId.elementAt(i))){
			 DomainObject domProjectMilestone = DomainObject.newInstance(context, (String) strProjectMilestoneobjectId.elementAt(i));
			 String strEndDate = "";
	
			 String SELECT_CONFIGURATION_MILESTONE =  "to[" + ProductLineConstants.RELATIONSHIP_CONFIGURATION_ITEM + "].from.id";
	
			 String strMT = domProjectMilestone.getInfo(context, SELECT_CONFIGURATION_MILESTONE);
	
			 if(null!=strMT && !"null".equalsIgnoreCase(strMT)&&!"".equalsIgnoreCase(strMT)){		 
				 if(domProjectMilestone.isKindOf(context,ProductLineConstants.TYPE_MILESTONE)){
					 
					 int iEstimatedIndex = transHistories.indexOf("Task Estimated Finish Date");
			           if(iEstimatedIndex != -1){
			        	   String SELECT_ATTRIBUTE_MILESTONE_ESTIMATED_FINISH_DATE = "attribute["+DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE+"]";
							 strEndDate = domProjectMilestone.getInfo(context, SELECT_ATTRIBUTE_MILESTONE_ESTIMATED_FINISH_DATE) ;
							 //set Interface attribute Milestone Date
							 domProjectMilestone.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_MILESTONE_DATE, strEndDate);
			           }		
			           int iActualIndex = transHistories.indexOf("Task Actual Finish Date");
			           if(iActualIndex != -1){
			        	   String SELECT_ATTRIBUTE_MILESTONE_ACTUAL_FINISH_DATE = "attribute["+DomainConstants.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE+"]";
							 strEndDate = domProjectMilestone.getInfo(context, SELECT_ATTRIBUTE_MILESTONE_ACTUAL_FINISH_DATE) ;
							 //set Interface attribute Milestone Date
							 if(strEndDate!=null && !strEndDate.equals("")){
								 domProjectMilestone.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_MILESTONE_DATE, strEndDate);
							 }
			           }
				 }
			 }
		 }
	}
 }
 
 
 
 /**
  * This trigger function updates the Milestone Date attribute if the Project Actual Date is modified
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  *        0 - String containing the object id
  * @throws Exception if operation fails
  * @since PRG R215
  */
 public void updateMilestoneDateWithActualDate(Context context, String[] args) throws Exception
 {
	 // get values from args.
	 String strProjectMilestoneobjectId = args[0];
	 if(null!=strProjectMilestoneobjectId && !"null".equalsIgnoreCase(strProjectMilestoneobjectId)&&!"".equalsIgnoreCase(strProjectMilestoneobjectId))
	 {
		 DomainObject domProjectMilestone = DomainObject.newInstance(context, strProjectMilestoneobjectId);
		 String strActualEndDate = "";

		 String SELECT_CONFIGURATION_MILESTONE =  "to[" + ProductLineConstants.RELATIONSHIP_CONFIGURATION_ITEM + "].from.id";
		 String SELECT_ATTRIBUTE_MILESTONE_ACTUAL_FINISH_DATE = "attribute["+DomainConstants.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE+"]";
		 StringList objSelect = new StringList();
		 objSelect.add(SELECT_CONFIGURATION_MILESTONE);
		 objSelect.add(SELECT_ATTRIBUTE_MILESTONE_ACTUAL_FINISH_DATE);
		 
		 Map mapInfo = domProjectMilestone.getInfo(context, objSelect);
		 
		 String strMT = (String)mapInfo.get(SELECT_CONFIGURATION_MILESTONE);

		 if(null!=strMT && !"null".equalsIgnoreCase(strMT)&&!"".equalsIgnoreCase(strMT)){		 
			 if(domProjectMilestone.isKindOf(context,ProductLineConstants.TYPE_MILESTONE)){
				 
				 strActualEndDate = (String)mapInfo.get(SELECT_ATTRIBUTE_MILESTONE_ACTUAL_FINISH_DATE) ;

				 //set Interface attribute Milestone Date
				 domProjectMilestone.setAttributeValue(context, ProductLineConstants.ATTRIBUTE_MILESTONE_DATE, strActualEndDate);
			 }
		 }
	 }

 }
 
 
 public int dependencyRelationshipModifyCheckTrigger (Context context, String[] args) throws Exception{
	 Integer intReturn = 0;
	 try{
		 String strRelId = args[0];
		 
		 
		
		 StringList relationshipSelects1 = new StringList();
		 relationshipSelects1.add(SELECT_RELATIONSHIP_ID);
		 relationshipSelects1.add("interface["+ProductLineConstants.INTERFACE_CONFIGURATION_USAGE +"]");
		
		 DomainRelationship domRel = new DomainRelationship(strRelId);
		 Hashtable relData = (Hashtable)domRel.getRelationshipData(context, relationshipSelects1);
		 StringList pcIdList =  new StringList();
		 pcIdList  = (StringList)relData.get("interface["+ProductLineConstants.INTERFACE_CONFIGURATION_USAGE +"]");
		 
		 
			 
			 if(pcIdList!=null && pcIdList.get(0).equals("FALSE")){
				 intReturn = 0;
			 }else{
				 String sErrorMsg=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProductLine.Milestone.EditDependencyNotAllowed",context.getSession().getLanguage());
       			 emxContextUtil_mxJPO.mqlError(context,sErrorMsg);
				 intReturn = 1;
			 }
		
		
			
	 }catch(Exception ex){
		 ex.printStackTrace();
	 }
	 return intReturn;
 }
 
 
 /**
  * gets the Milestone Track objects discipline attribute value to display in PRoduct Milestone Summary table
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  * @returns Object
  * @throws Exception if the operation fails
  * @since R215
  * @grade 0
  */
 
 public Vector  getDiscipline (Context context, String[] args) throws FrameworkException{
		Vector returnVec = new Vector();
		try{
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			MapList objectMap = (MapList) inputMap.get("objectList");		 			
			StringList returnStringList = new StringList (objectMap.size());

			for (int i = 0; i < objectMap.size(); i++) {
				Map outerMap = (Map)objectMap.get(i);
				String MilestoneDiscipline = (String)outerMap.get("MilestoneDiscipline");
				String strIntMilestoneDiscipline = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Milestone."+MilestoneDiscipline, context.getSession().getLanguage());
				returnVec.add(strIntMilestoneDiscipline);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return returnVec;
		}
 
 

 /**
  * Decides the Milestone category should be displayed or not in the Product Categories
  * 
  * @param context the ENOVIA <code>Context</code> object
  * @param args holds the HashMap containing the
	 *            following arguments
  * @return Object - MapList containing the ids of all Milestones of the project
  * @throws Exception
	 *             if the operation fails
  */
 
 public boolean isMilestoneDispalyAllowed(Context context,String args[])
 {
	 boolean bProgram = false;
	 boolean bCFF = false;
	 try {

		 bProgram = FrameworkUtil.isSuiteRegistered(context,"appVersionProgramCentral",false,null,null);
		 bCFF = FrameworkUtil.isSuiteRegistered(context,"appVersionEffectivityFramework",false,null,null);

		 if(bProgram && bCFF){
			// String isMilestoneDisplayAllowed = com.matrixone.apps.domain.util.EnoviaResourceBundle.getProperty(context,"emxEffectivity.AllowMilestoneEffectivity");
			// if(!"true".equalsIgnoreCase(isMilestoneDisplayAllowed)){
			//	 bProgram = false;
			// }else{
				 HashMap inputMap = (HashMap)JPO.unpackArgs(args);
				 String strObjectId = (String) inputMap.get("objectId");
				 DomainObject dmoObject = DomainObject.newInstance(context,strObjectId);
				 
				 if(dmoObject.isKindOf(context, ProductLineConstants.TYPE_MODEL)){
					 String SELECT_PRODUCT = "from[" + ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT + "].to.id";
					 strObjectId = (String) dmoObject.getInfo(context, SELECT_PRODUCT);
					 dmoObject = DomainObject.newInstance(context,strObjectId);
				 }
				 
				 if(dmoObject.isKindOf(context, ProductLineConstants.TYPE_HARDWARE_PRODUCT)){
				 StringList objectSelects = new StringList(2);
				 objectSelects.addElement(DomainConstants.SELECT_ID);
				 objectSelects.addElement(DomainConstants.SELECT_NAME);
				 StringList relationshipSelects = new StringList();

				 MapList relatedProducts = dmoObject.getRelatedObjects(context,
						 ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS,
						 ProductLineConstants.TYPE_PROJECT_SPACE,
						 objectSelects,
						 relationshipSelects,
						 false,	//to relationship
						 true,	//from relationship
						 (short)1,
						 DomainConstants.EMPTY_STRING,
						 DomainConstants.EMPTY_STRING,
						 0);
				 if(relatedProducts.size() == 0){
					 bProgram = false;
				 }else{
					 bProgram = true; 
				 }
				 }

			// }
		 }else{
			 bProgram = false;
		 }

	 } catch (Exception e) {
		 bProgram = false;
	
	 }
	 return bProgram;
 }
 
 
 
 public Vector getNameColumn (Context context, String[] args) throws Exception
	{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList    = (HashMap) programMap.get("paramList");
		String exportFormat = (String)paramList.get("exportFormat");
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if ( strPrinterFriendly != null ) {
			isPrinterFriendly = true;
		}

		//
		// Find all the required infomration on each of the tasks here
		//
		String[] strObjectIds = new String[objectList.size()];
		int size = objectList.size();
		for (int i = 0; i < size; i++) {
			Map mapObject = (Map) objectList.get(i);
			String taskId = (String) mapObject.get(DomainObject.SELECT_ID);
			strObjectIds[i] = taskId;
		}

		StringList slBusSelect = new StringList();
		slBusSelect.add(DomainConstants.SELECT_ID);
		slBusSelect.add(DomainConstants.SELECT_TYPE);
		slBusSelect.add(DomainConstants.SELECT_NAME);
		slBusSelect.add(DomainConstants.SELECT_CURRENT);
		slBusSelect.add(DomainConstants.SELECT_POLICY);

		String IS_SUMMARY_TASK = "from["+DomainRelationship.RELATIONSHIP_SUBTASK+"].to.id";
		slBusSelect.add(DomainConstants.SELECT_DESCRIPTION);
		slBusSelect.add(IS_SUMMARY_TASK);
		Map mapTaskInfo = new HashMap();
		BusinessObjectWithSelectList objectWithSelectList = DomainObject.getSelectBusinessObjectData(context, strObjectIds, slBusSelect);
		for (BusinessObjectWithSelectItr objectWithSelectItr = new BusinessObjectWithSelectItr(objectWithSelectList); objectWithSelectItr.next();) {
			BusinessObjectWithSelect objectWithSelect = objectWithSelectItr.obj();

			Map mapTask = new HashMap();
			for (Iterator itrSelectables = slBusSelect.iterator(); itrSelectables.hasNext();) {
				String strSelectable = (String)itrSelectables.next();
				mapTask.put(strSelectable, objectWithSelect.getSelectData(strSelectable));
			}

			mapTaskInfo.put(objectWithSelect.getSelectData(SELECT_ID), mapTask);
		}

		Iterator objectListIterator = objectList.iterator();
		Vector columnValues = new Vector(objectList.size());

		while (objectListIterator.hasNext())
		{
			 StringBuffer sbHead = new StringBuffer();
   	        StringBuffer sbHref = new StringBuffer();
			Map objectMap = (Map) objectListIterator.next();
			String taskId = (String) objectMap.get(DomainObject.SELECT_ID);

			Map objectInfo = (Map)mapTaskInfo.get(taskId);
			DomainObject taskObj  = DomainObject.newInstance(context, taskId);

			String strName = (String)objectInfo.get(SELECT_NAME);
			String strID = (String)objectInfo.get(SELECT_ID);


			 if (strPrinterFriendly != null && !("null".equalsIgnoreCase(strPrinterFriendly)) && strPrinterFriendly.length()>0){
				 sbHref.append(strName);
				 columnValues.add(sbHref.toString());
			 }else{
				 sbHead.append("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert");
				 sbHead.append("&amp;objectId=" + XSSUtil.encodeForHTMLAttribute(context,strID) + "'");
				 sbHead.append(", '800', '700', 'true', 'popup')\">");
				 sbHref.append(sbHead);
				 sbHref.append(XSSUtil.encodeForHTML(context,strName));
				 sbHref.append("</a>");
				 columnValues.add(sbHref.toString());
			 }
		}
		return columnValues;
	}
 
 /**
  * Creates the Model level Milestone Track objects
  * 
  * @param context the ENOVIA <code>Context</code> object
  * @param args holds the HashMap containing the
	 *            following arguments
  * @return Object - MapList containing the ids of all Milestones of the project
  * @throws Exception
  *             if the operation fails
  */
 
 public int addInterfaceDerivationIndex (Context context, String[] args){
	 Integer intReturn = 0;
	 int index=0;
	 int dateindex=0;
	 try{
		 HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		 String strMilestoneTrack = (String) paramMap.get("strMilestoneTrack");
		 String strProductID = (String) paramMap.get("strProductID");

		 String strProductMilestoneTrackObjId = strMilestoneTrack;
		 DomainObject domProductMTObj = DomainObject.newInstance(context, strProductMilestoneTrackObjId);

		
		 String strProductMTDiscipline = domProductMTObj.getAttributeValue(context, ProductLineConstants.ATTRIBUTE_MILESTONE_TRACK_DISCIPLINE);
		 
		 DomainObject domProduct = new DomainObject(strProductID);
		 String SELECT_MAIN_PRODUCT_FROM_ID =  "to[" + ProductLineConstants.RELATIONSHIP_MAIN_PRODUCT + "].from.id";
		 
		 String strModelId= domProduct.getInfo(context, SELECT_MAIN_PRODUCT_FROM_ID);
		 if(null == strModelId ||  "".equals(strModelId)){
			 String SELECT_PRODUCTS_FROM_ID =  "to[" + ProductLineConstants.RELATIONSHIP_PRODUCTS+ "].from.id"; 
			 strModelId= domProduct.getInfo(context, SELECT_PRODUCTS_FROM_ID);
		 }
		 
		 MapList mlAllMilestones = new MapList();
		 MapList mlClubMilestones = new MapList();
		 
		 
		 StringList objectSelectables = new StringList(2);
		 objectSelectables.add(SELECT_ID);
		 objectSelectables.add(SELECT_NAME);
		 objectSelectables.add(SELECT_CURRENT);
		 objectSelectables.add(ProductLineConstants.SELECT_ATTRIBUTE_MILESTONE_DATE);
		 StringList relationshipSelectables = new StringList();
		 
		 if(null != strModelId && !"".equals(strModelId) && !"null".equals(strModelId)){
			 StringList objSelectablesMS = new StringList(4);
			 objSelectablesMS.add(SELECT_ID);
			 objSelectablesMS.add(SELECT_NAME);
			 objSelectablesMS.add(SELECT_CURRENT);
			 objSelectablesMS.add(ProductLineConstants.SELECT_ATTRIBUTE_MILESTONE_DATE);
			 objSelectablesMS.add("to["+ProductLineConstants.RELATIONSHIP_FIRST_CONFIGURATION_ITEM+"].id");
			 objSelectablesMS.add("interface["+ProductLineConstants.INTERFACE_DERIVATION_INDEX +"]");
			 MapList mapProductObjsList = new MapList();
			 MapList mlPublishedMilestones = Model.getModelMilestoneTracks(context, 
					 strModelId, new StringList(strProductMTDiscipline), objSelectablesMS, null, false,true);
			 if(mlPublishedMilestones.size()>0){
				 mlPublishedMilestones=(MapList)((Map)mlPublishedMilestones.get(0)).get(strProductMTDiscipline);
				 for(int k=0; k<mlPublishedMilestones.size();k++){
					 Map tempMap = (Map)mlPublishedMilestones.get(k);
					 String strName = (String)tempMap.get(SELECT_NAME);
					 tempMap.put("indexmilestone", ""+index++);
					 mlClubMilestones.add(tempMap);
				 }
			 }
		 }
		 //Get the Project Milestone objects from Product Milstone Track object
		 MapList mlProjectMilestones = domProductMTObj.getRelatedObjects(context,
				 ProductLineConstants.RELATIONSHIP_CONFIGURATION_ITEM,
				 ProductLineConstants.TYPE_MILESTONE,
				 objectSelectables,
				 relationshipSelectables,
				 false,	//to relationship - changed
				 true,	//from relationship
				 (short)1,
				 DomainConstants.EMPTY_STRING,
				 DomainConstants.EMPTY_STRING,
				 0); 
		 MapList mlSortedPRDProjecTmilestones = sortProjectMilestonesWithDate(context,mlProjectMilestones);
		 
		 StringList slTemp = new StringList();
		 
		 for(int m=0;m<mlProjectMilestones.size();m++){
			 Map mapMilestone = (Map)mlProjectMilestones.get(m);
			 String strID = (String)mapMilestone.get(SELECT_ID);
			 slTemp.add(strID);
			 if(!(slTemp.contains(strID))){
				 mapMilestone.put("indexmilestone", ""+index++);
				 mlClubMilestones.add(mapMilestone);
			 }
		 }
		/* 
		 for(int m=0;m<mlProjectMilestones.size();m++){
			 Map mapMilestone = (Map)mlProjectMilestones.get(m);
			 String strState = (String)mapMilestone.get(SELECT_CURRENT);
			 
			 if("Complete".equalsIgnoreCase(strState) ){
				 mlClubMilestones.remove(mapMilestone);
			 }
			
		 } */
		 MapList mlNewClubedSortedmilestoens =  sortProjectMilestonesWithDate(context,mlClubMilestones);
		 for(int k=0; k<mlNewClubedSortedmilestoens.size();k++){
			 Map tempMap = (Map)mlNewClubedSortedmilestoens.get(k);
			 tempMap.put("dateindex", ""+dateindex++);
		 }
		 mlNewClubedSortedmilestoens.addSortKey("dateindex", "ascending", "integer");
		 mlNewClubedSortedmilestoens.addSortKey("indexmilestone", "ascending", "integer");
		 mlNewClubedSortedmilestoens.sort();
		

		 
		 //Add interface_DerivationIndex
		 addInterfaceDerivationIndex(context,mlNewClubedSortedmilestoens);
	//	 }


	 }catch(Exception ex){
			ex.printStackTrace();
		}
		return intReturn;
 }
 
 public MapList sortProjectMilestonesWithDate(Context context,MapList mlSortedProjecTmilestones)throws Exception {
	 	
 	 ArrayList arrDates = new ArrayList();
 	Collections.sort(mlSortedProjecTmilestones, new Comparator() {
         public int compare(final Object object1, final Object object2) {
         	Map map1 = (Map)object1;
         	Map map2 = (Map)object2;
             return (eMatrixDateFormat.getJavaDate((String)map1.get(ProductLineConstants.SELECT_ATTRIBUTE_MILESTONE_DATE))).compareTo(eMatrixDateFormat.getJavaDate((String)map2.get(ProductLineConstants.SELECT_ATTRIBUTE_MILESTONE_DATE))); 
         }}
     );

 	return mlSortedProjecTmilestones;
 }
 
 /**
  * Add interface Derivation Index on the type Milestone
  * 
  * @param context the ENOVIA <code>Context</code> object
  * @param args holds the MAplist containing the
 	 *            sorted Milestones
  * @return 
  * @throws Exception
  *             if the operation fails
  */
  public void addInterfaceDerivationIndex(Context context,MapList mlSortedProjecTmilestones ) throws Exception{
 	 
 	 String strParentMilestoneId = null;
 	 boolean nextFound = false;
 	 for(int p=0;p<mlSortedProjecTmilestones.size();p++){
 		 Map mapMilestone = (Map)mlSortedProjecTmilestones.get(p);
 		Map mapMilestonePrev= null;
 		Map mapMilestoneNext=null;
 		nextFound = false;
 		
 		if(p>0){
 		mapMilestonePrev = (Map)mlSortedProjecTmilestones.get(p-1);
 		strParentMilestoneId = (String)mapMilestonePrev.get(SELECT_ID);
 		}
 		if(p<mlSortedProjecTmilestones.size()-1){
 		mapMilestoneNext = (Map)mlSortedProjecTmilestones.get(p+1);
 		}
 		
 		 String strProjectMilestoneObj = (String)mapMilestone.get(SELECT_ID);
 		 String strState = (String)mapMilestone.get(SELECT_CURRENT);
		 
		 if("Complete".equalsIgnoreCase(strState) ){
			 continue;
		 }
		 
 		 MilestoneTrack MileTrack = new MilestoneTrack();
 		 HashMap nodeAttributeMap = new HashMap();
 			 
            String strInterface = (String)mapMilestone.get("interface["+ProductLineConstants.INTERFACE_DERIVATION_INDEX +"]");
        	ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), "", ""); 
            if(!"TRUE".equalsIgnoreCase(strInterface)){
            	boolean isMilestoneInsert=false;
            	String strPrevMSId="";
            	String strPrevMSInterface="";
            	String strNextMSId="";
            	String strNextMSInterface="";
            	
            	String strFromId="";
            	String strToId="";
            
            	//Check if the MS prior to this MS has Node Index generated
            	if(mapMilestonePrev!=null && mapMilestoneNext!=null){
            		 strPrevMSId=(String)mapMilestonePrev.get(SELECT_ID);
            		 strPrevMSInterface = (String)mapMilestonePrev.get("interface["+ProductLineConstants.INTERFACE_DERIVATION_INDEX +"]");	
            		 strNextMSId=(String)mapMilestoneNext.get(SELECT_ID);
            		 strNextMSInterface = (String)mapMilestoneNext.get("interface["+ProductLineConstants.INTERFACE_DERIVATION_INDEX +"]");
            
            	}

            	//Check if the MS prior to this MS has Node Index generated	
            	if("TRUE".equalsIgnoreCase(strPrevMSInterface)){
            		if("TRUE".equalsIgnoreCase(strNextMSInterface)){
            			nodeAttributeMap =  MileTrack.insertDerivedNode (context,strPrevMSId,strNextMSId,MilestoneTrack.DERIVATION_LEVEL0);
            		}
            		else if(p == mlSortedProjecTmilestones.size() - 1){
            			nodeAttributeMap =  MileTrack.createDerivedNode(context, strParentMilestoneId,  MilestoneTrack.DERIVATION_LEVEL0);
            		}
            		else{
            			for(int q=p+1;q<mlSortedProjecTmilestones.size();q++){
            				mapMilestoneNext = (Map)mlSortedProjecTmilestones.get(q);
            				strNextMSInterface = (String)mapMilestoneNext.get("interface["+ProductLineConstants.INTERFACE_DERIVATION_INDEX +"]");
            				if(!"TRUE".equalsIgnoreCase(strNextMSInterface)){
            					continue;
            				}
            				strNextMSId=(String)mapMilestoneNext.get(SELECT_ID);
            				nodeAttributeMap =  MileTrack.insertDerivedNode (context,strPrevMSId,strNextMSId,MilestoneTrack.DERIVATION_LEVEL0);
            				nextFound = true;
            				break;
            			}
            			if(!nextFound){
            				nodeAttributeMap =  MileTrack.createDerivedNode(context, strParentMilestoneId,  MilestoneTrack.DERIVATION_LEVEL0);
            			}
            		}
            	}
            	else{
            		nodeAttributeMap =  MileTrack.createDerivedNode(context, strParentMilestoneId,  MilestoneTrack.DERIVATION_LEVEL0);
            	}
	 			MileTrack.addInterfaceAndSetAttributes(context, strProjectMilestoneObj, "bus", 
	 					 ProductLineConstants.INTERFACE_DERIVATION_INDEX, nodeAttributeMap);
	 			
	 			mapMilestone.put("interface["+ProductLineConstants.INTERFACE_DERIVATION_INDEX +"]", "TRUE");
             }
 			
 			ContextUtil.popContext(context);
 		 //strParentMilestoneId = strProjectMilestoneObj;
 	 }
  }
  
  
  /**
   * Add interface Derivation Index on the type Milestone
   * 
   * @param context the ENOVIA <code>Context</code> object
   * @param args holds the MAplist containing the
  	 *            sorted Milestones
   * @return 
   * @throws Exception
   *             if the operation fails
   */
  public void addDerivationIndexInterfaceonInsertion(Context context, String args[]) throws Exception{
	  HashMap programMap = (HashMap)JPO.unpackArgs(args);
	  MapList mlNewSortedMilestones = new MapList();
	  mlNewSortedMilestones = (MapList)programMap.get("mlNewSortedMilestones");
	  String strParentMilestoneId = null;
	  	  
	  String strNewMilestone = "";
	  String strParentMilestone = "";
	  String strtempParentMilestone = "";
	  String strChildMilestone = "";
	  boolean bInterfaceFalse = false;
	  HashMap nodeAttributeMap = new HashMap();
	  boolean ifTrue = false;
	  
	  for(int p=0;p<mlNewSortedMilestones.size();p++){
		  
		  if(bInterfaceFalse){
			  ifTrue = true;
		  }
		  Map mapMilestone = (Map)mlNewSortedMilestones.get(p);
		  String strProjectMilestoneObj = (String)mapMilestone.get(SELECT_ID);
		  DomainObject domProjectMilestone = new DomainObject(strProjectMilestoneObj);
		  String SELECT_INTERFACE_DERIVATION_INDEX = "interface["+ProductLineConstants.INTERFACE_DERIVATION_INDEX+"]";
		  String strInterface = domProjectMilestone.getInfo(context, SELECT_INTERFACE_DERIVATION_INDEX);
		  MilestoneTrack MileTrack = new MilestoneTrack();
		
		  if(strInterface.equalsIgnoreCase("FALSE")){
			  strNewMilestone = strProjectMilestoneObj;
			  strParentMilestone = strtempParentMilestone;
			  bInterfaceFalse = true;
		  }
		  
		  if(bInterfaceFalse && ifTrue){
			  strChildMilestone = strProjectMilestoneObj;
			  String strMilestoneFromID = strParentMilestone;
			  String strMilestoneToID = strChildMilestone;
				  nodeAttributeMap = MileTrack.insertDerivedNode(context, strMilestoneFromID, strMilestoneToID, MilestoneTrack.DERIVATION_LEVEL0);
			 
			  MileTrack.addInterfaceAndSetAttributes(context, strNewMilestone, "bus", 
	 					 ProductLineConstants.INTERFACE_DERIVATION_INDEX, nodeAttributeMap);
			  
			  bInterfaceFalse = false;
			  ifTrue = false;
		  }
		  strtempParentMilestone = strProjectMilestoneObj;
	  }
  }
  
  /**
   * Decides the Publish to Model command should be displayed or not in the Milesotne Track Categories
   * 
   * @param context the ENOVIA <code>Context</code> object
   * @param args holds the HashMap containing the
 	 *            following arguments
   * @return Object - MapList containing the ids of all Milestones of the project
   * @throws Exception
 	 *             if the operation fails
   */
  public boolean isPublishToModelDispalyAllowed(Context context,String args[])
  {
 	 boolean bDispaly = false;
 	 try {
 		 String strProductManager = ProductLineConstants.ROLE_PRODUCT_MANAGER;
 		 boolean isProdManager = PersonUtil.getAssignments(context).contains(strProductManager);
 		 String strVPLMProjectLeader = PropertyUtil.getSchemaProperty(context,"role_VPLMProjectLeader");
 		 String strRole = context.getRole();
 		 if(isProdManager || strRole.contains(strVPLMProjectLeader)){
 			 HashMap paramMap = (HashMap)JPO.unpackArgs(args);
 			 String strProductId = (String) paramMap.get("objectId");
 			 DomainObject domPrdObj= DomainObject.newInstance(context, strProductId); 

 			 String SELECT_FROM_RELATED_PROJECT_TO_ID = "from[" + ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS + "].to.id";
 			 String strRelatedPRoj = domPrdObj.getInfo(context, SELECT_FROM_RELATED_PROJECT_TO_ID);
 			 DomainObject domRelatedPRoj= DomainObject.newInstance(context, strRelatedPRoj); 
 			 if(domRelatedPRoj.isKindOf(context,ProductLineConstants.TYPE_PROJECT_SPACE)){
 				 bDispaly = true;
 			 }
 		 }
 	 } catch (Exception e) {
 		bDispaly = false;
 	
 	 }
 	 return bDispaly;
  }
    
  
  
  
 
 
 
}
