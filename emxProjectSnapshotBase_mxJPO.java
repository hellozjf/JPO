/* emxTaskBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.36.2.1 Thu Dec  4 07:55:16 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.36 Tue Oct 28 18:55:12 2008 przemek Experimental przemek $
 */

/*
Change History:
Date       Change By  Release   Bug/Functionality         Details
----------------------------------------------------------------------------------------------------------------------------
29-Apr-09   wqy        V6R2010   373332                   Change Code for I18n
 */


import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.effectivity.EffectivityFramework;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.ProjectSnapshot;
import com.matrixone.apps.program.Task;

/**
 * The <code>emxSnapshotBase</code> class represents the Project Snapshot JPO
 * functionality .
 *
 * @version PRG 2014x - Copyright (c).
 */
public class emxProjectSnapshotBase_mxJPO 
{
	
	public emxProjectSnapshotBase_mxJPO (Context context, String[] args)
	throws Exception{
		super();
	}

	
	public MapList getProjects(Context context, String args[]) throws MatrixException	{
		Map arguMap;
		try {
			arguMap = (Map)JPO.unpackArgs(args);
		} catch (Exception e) {

			throw new MatrixException(e);
		}

		String strObjectId = (String) arguMap.get("objectId");
		String strExpandLevel = (String) arguMap.get("expandLevel");

		boolean isToExpandProject = false;
		String strProjectPlanId = ProjectSpace.getGoverningProjectPlanId(context, strObjectId);
		if(ProgramCentralUtil.isNotNullString(strProjectPlanId)){
			isToExpandProject = true;
		}
		MapList mapList = new MapList();
		if(isToExpandProject){
			StringList slBusSelect = getExpandSelectables();
			DomainObject dmoProjectId = DomainObject.newInstance(context,strProjectPlanId);
			Map mProjectInfo = dmoProjectId.getInfo(context, slBusSelect);
			mProjectInfo.put("RowEditable", "readonly");
			mapList.add(mProjectInfo);
		}
		return mapList;
	}
	
	private StringList getExpandSelectables(){
		
		StringList slBusSelect = new StringList();
		slBusSelect.add(DomainConstants.SELECT_ID);
		slBusSelect.add(DomainConstants.SELECT_NAME);
		slBusSelect.addElement(DomainConstants.SELECT_TYPE);
		slBusSelect.addElement(DomainConstants.SELECT_CURRENT);
		slBusSelect.addElement(EffectivityFramework.SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION);
		slBusSelect.addElement("to["+ProgramCentralConstants.RELATIONSHIP_RELATED_PROJECTS+"]."+EffectivityFramework.SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION);
		slBusSelect.addElement(EffectivityFramework.SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION_BINARY);
		slBusSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
		slBusSelect.addElement(ProgramCentralConstants.SELECT_IS_KINDOF_PROJECT_SNAPSHOT);
		return slBusSelect;
	}

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList expandProjectSnapshots(Context context, String args[]) throws MatrixException	{
		Map arguMap;
		try {
			arguMap = (Map)JPO.unpackArgs(args);
		} catch (Exception e) {

			throw new MatrixException(e);
		}
		String strObjectId = (String) arguMap.get("objectId");
		String strExpandLevel = (String) arguMap.get("expandLevel");

		boolean isToExpandProject = false;
		String strProjectPlanId = ProjectSpace.getGoverningProjectPlanId(context, strObjectId);
		if(ProgramCentralUtil.isNotNullString(strProjectPlanId)){
			isToExpandProject = true;
		}

		StringList slBusSelect = getExpandSelectables();
		String strRelationshipPattern =  ProgramCentralConstants.RELATIONSHIP_RELATED_PROJECTS; 		  
		String strTypePattern = ProgramCentralConstants.TYPE_PROJECT_SNAPSHOT;
		boolean getTo = false;
		boolean getFrom = true;
		short nExpandLevel =  ProgramCentralUtil.getExpandLevel(strExpandLevel);
		MapList mapList = new MapList();

		if(isToExpandProject){
			DomainObject dmoProjectObject = DomainObject.newInstance(context,strProjectPlanId);
			mapList = dmoProjectObject.getRelatedObjects(context,
					strRelationshipPattern, //pattern to match relationships
					strTypePattern, //pattern to match types
					slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
					null, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
					getTo, //get To relationships
					getFrom, //get From relationships
					nExpandLevel, //the number of levels to expand, 0 equals expand all.
					null, //where clause to apply to objects, can be empty ""
					null,
					(short)0,false,true,(short)0,null,null,null,null); //where clause to apply to relationship, can be empty ""}
		}

		Iterator it = mapList.iterator();
		while(it.hasNext()){
			Map mObjectMap = (Map)it.next();
			mObjectMap.put("RowEditable", "readonly");
		//	mObjectMap.put("disableSelection","true");
		}
		HashMap hmTemp = new HashMap();
		hmTemp.put("expandMultiLevelsJPO","true");
		mapList.add(hmTemp);

		return mapList;
	}
	
	public boolean hasAccessOnEffectivityDateColumn(Context context,String args[])throws Exception
	{
		boolean hasAccess = false;
		Map inputMap = (HashMap)JPO.unpackArgs(args);
		String strTable = (String)inputMap.get("selectedTable");
		String strProgram = (String)inputMap.get("selectedProgram");
		String strObjectId = (String)inputMap.get("objectId");
		
		hasAccess = hasAccessOnProjectSnapshotView(context,strTable,strProgram);
		if(!hasAccess){
			hasAccess = hasAccessOnProjectSnapshotCategoryView(context,strObjectId);
		}
		return hasAccess;
	}
	
	public boolean hasAccessOnSnapshotToolbar(Context context,String args[])throws Exception
	{
		Map inputMap = (HashMap)JPO.unpackArgs(args);
		String strTable = (String)inputMap.get("selectedTable");
		String strProgram = (String)inputMap.get("selectedProgram");
		String strObjectId = (String)inputMap.get("objectId");
		return hasAccessOnProjectSnapshotView(context,strTable,strProgram);
	}
	
	private boolean hasAccessOnProjectSnapshotView(Context context,String strTable,String strProgram)
	{
		boolean blAccess = false;
		if(ProgramCentralUtil.isNotNullString(strTable) && strTable.indexOf("WBSViewTable")>0 &&
				(ProgramCentralUtil.isNotNullString(strProgram) && strProgram.indexOf("ProjectSnapshots")>0)){
			blAccess = true;
		}else{
			blAccess = false;
		}

		return blAccess;
	}
	
	private boolean hasAccessOnProjectSnapshotCategoryView(Context context,String strObjectId)throws MatrixException
	{
		boolean blAccess = false;
		if(ProgramCentralUtil.isNotNullString(strObjectId)){
			StringList slObjectList = new StringList();
			slObjectList.addElement(ProgramCentralConstants.SELECT_GOVERNING_PROJECT_PLAN);
			DomainObject dmoObject = DomainObject.newInstance(context,strObjectId);
			Map mObjectInfo = dmoObject.getInfo(context, slObjectList);
			String strGovProjPlanId = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_GOVERNING_PROJECT_PLAN);
			if(ProgramCentralUtil.isNotNullString(strGovProjPlanId)){
				blAccess = true;	
			}
		}

		return blAccess;
	}
	
	
	public Vector getColumnEffectivityDateDisplayValue(Context context,String args[])throws Exception
	{
		Vector slList = new Vector();
		Map inputMap = (Map)JPO.unpackArgs(args);
		Map paramList = (Map) inputMap.get("paramList");
		MapList objectInfoList = (MapList) inputMap.get("objectList");
		Map requestMap = (Map) inputMap.get("requestMap");
		
		String []strObjArr = getObjectArray(context, objectInfoList);
		DomainObject dmoObj = DomainObject.newInstance(context);
		MapList objectList = dmoObj.getInfo(context, strObjArr, getExpandSelectables());
		int size = objectList != null? objectList.size(): 0 ;
		ProjectSnapshot snapshot = (ProjectSnapshot)ProjectSnapshot.newInstance(context, ProgramCentralConstants.TYPE_PROJECT_SNAPSHOT, ProgramCentralConstants.PROGRAM);
		String strDisplayVal = "";
		for(int i=0; i<size; i++)
		{
			Map mSnapshotData = (Map)objectList.get(i);
			String strEffExpression = (String)mSnapshotData.get(EffectivityFramework.SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION);
			String strSnapshotId = (String)mSnapshotData.get(ProgramCentralConstants.SELECT_ID);
			
			if(ProgramCentralUtil.isNotNullString(strSnapshotId)){
				snapshot.setId(strSnapshotId);
				//strDisplayVal = snapshot.getDateEffectivityDisplayValue(context);
			}
			
			slList.addElement(strDisplayVal);
		}
		return slList;
	}

	public Vector getColumnEffectivityStartDateDisplayValue(Context context,String args[])throws Exception
	{
		Vector slList = new Vector();
		Map inputMap = (Map)JPO.unpackArgs(args);
		Map paramList = (Map) inputMap.get("paramList");
		MapList objectInfoList = (MapList) inputMap.get("objectList");
		Map requestMap = (Map) inputMap.get("requestMap");
		
		String []strObjArr = getObjectArray(context, objectInfoList);
		DomainObject dmoObj = DomainObject.newInstance(context);
		MapList objectList = dmoObj.getInfo(context, strObjArr, getExpandSelectables());
		int size = objectList != null? objectList.size(): 0 ;
		ProjectSpace prjSpace = (ProjectSpace)ProjectSpace.newInstance(context, ProgramCentralConstants.TYPE_PROJECT_SPACE, ProgramCentralConstants.PROGRAM);
		ProjectSnapshot snapshot = (ProjectSnapshot)ProjectSnapshot.newInstance(context, ProgramCentralConstants.TYPE_PROJECT_SNAPSHOT, ProgramCentralConstants.PROGRAM);
		
		
		for(int i=0; i<size; i++)
		{
			Map mSnapshotData = (Map)objectList.get(i);
			String isProjectSpaceVal = (String)mSnapshotData.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String isSnapshotVal = (String)mSnapshotData.get(ProgramCentralConstants.SELECT_IS_KINDOF_PROJECT_SNAPSHOT);

			String strEffExpression = (String)mSnapshotData.get(EffectivityFramework.SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION);
			String strObjId = (String)mSnapshotData.get(ProgramCentralConstants.SELECT_ID);
			String strSnapshotRelExp  = (String)mSnapshotData.get("to["+ProgramCentralConstants.RELATIONSHIP_RELATED_PROJECTS+"]."+EffectivityFramework.SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION);
			String strStarDateDisplayVal = "";
			if("true".equalsIgnoreCase(isSnapshotVal)){
				snapshot.setId(strObjId);
				strStarDateDisplayVal = snapshot.getEffectivityDisplayStartDate(context);
				
			}else if("true".equalsIgnoreCase(isProjectSpaceVal)){
				prjSpace.setId(strObjId);
				strStarDateDisplayVal = prjSpace.getEffectivityStartDate(context);
				/*if(ProgramCentralUtil.isNotNullString(strSnapshotRelExp)){
					snapshot.setId(strObjId);
					String strDisplayVal = snapshot.getDateEffectivityDisplayValue(context,false);
					strStarDateDisplayVal = getParsedEffectivityDate(context, strDisplayVal,0);
				}
				else{
					snapshot.setId(strSnapshotId);
					String strDisplayVal = snapshot.getDateEffectivityDisplayValue(context,true);
					strStarDateDisplayVal = getParsedEffectivityDate(context, strDisplayVal,0);
				}*/
			}
			slList.addElement(strStarDateDisplayVal);
		
			
			
			/*			if(ProgramCentralUtil.isNotNullString(strSnapshotId)){
			snapshot.setId(strSnapshotId);
			strDisplayVal = snapshot.getDateEffectivityDisplayValue  (context);
			strEndDateDisplayVal = getParsedEffectivityDate(context, strDisplayVal,1);
		}*/
			
		}	
			
			
		
		return slList;
	}
	
	
	
	public Vector getColumnEffectivityEndDateDisplayValue(Context context,String args[])throws Exception
	{
		Vector slList = new Vector();
		Map inputMap = (Map)JPO.unpackArgs(args);
		Map paramList = (Map) inputMap.get("paramList");
		MapList objectInfoList = (MapList) inputMap.get("objectList");
		Map requestMap = (Map) inputMap.get("requestMap");

		String []strObjArr = getObjectArray(context, objectInfoList);
		DomainObject dmoObj = DomainObject.newInstance(context);
		MapList objectList = dmoObj.getInfo(context, strObjArr, getExpandSelectables());
		int size = objectList != null? objectList.size(): 0 ;
		ProjectSpace prjSpace = (ProjectSpace)ProjectSpace.newInstance(context, ProgramCentralConstants.TYPE_PROJECT_SPACE, ProgramCentralConstants.PROGRAM);
		ProjectSnapshot snapshot = (ProjectSnapshot)ProjectSnapshot.newInstance(context, ProgramCentralConstants.TYPE_PROJECT_SNAPSHOT, ProgramCentralConstants.PROGRAM);
		String strDisplayVal = "";

		for(int i=0; i<size; i++)
		{
			Map mSnapshotData = (Map)objectList.get(i);
			String isProjectSpaceVal = (String)mSnapshotData.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String isSnapshotVal = (String)mSnapshotData.get(ProgramCentralConstants.SELECT_IS_KINDOF_PROJECT_SNAPSHOT);

			String strEffExpression = (String)mSnapshotData.get(EffectivityFramework.SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION);
			String strObjId = (String)mSnapshotData.get(ProgramCentralConstants.SELECT_ID);
			String strSnapshotRelExp  = (String)mSnapshotData.get("to["+ProgramCentralConstants.RELATIONSHIP_RELATED_PROJECTS+"]."+EffectivityFramework.SELECT_ATTRIBUTE_EFFECTIVITY_EXPRESSION);
			String strEndDateDisplayVal = "";

			if("true".equalsIgnoreCase(isSnapshotVal)){

				snapshot.setId(strObjId);
				//strEndDateDisplayVal = snapshot.getEffectivityEndDate(context);
				strEndDateDisplayVal = snapshot.getEffectivityDisplayEndDate(context);
			}else if("true".equalsIgnoreCase(isProjectSpaceVal)){
				prjSpace.setId(strObjId);
				//strEndDateDisplayVal = prjSpace.getEffectivityEndDate(context);
				strEndDateDisplayVal = prjSpace.getEffectivityEndDate(context);
			}
			
			/*if(ProgramCentralUtil.isNotNullString(strSnapshotRelExp)){
				snapshot.setId(strSnapshotId);
				 strDisplayVal = snapshot.getDateEffectivityDisplayValue(context,false);
				strEndDateDisplayVal = getParsedEffectivityDate(context, strDisplayVal,1);
			}
			else{
				snapshot.setId(strSnapshotId);
				 strDisplayVal = snapshot.getDateEffectivityDisplayValue(context,true);
				strEndDateDisplayVal = getParsedEffectivityDate(context, strDisplayVal,1);
			}
			 */			

			slList.addElement(strEndDateDisplayVal);
		}
		return slList;
	}
	
	private String getParsedEffectivityDate(Context context, String strDisplayVal, int iDate)
	{
		if(ProgramCentralUtil.isNotNullString(strDisplayVal)){
			StringList slObjectList = parseDisplayValue(context, strDisplayVal);
			if(slObjectList!=null && slObjectList.size() == 2){
				return (String)slObjectList.get(iDate);
			}
		}
		return "";
	}
	
	private StringList parseDisplayValue(Context context, String strDisplayVal)
	{
		StringList slObjectList = new StringList();
		if(ProgramCentralUtil.isNotNullString(strDisplayVal)){
			int bracketStartIndex = strDisplayVal.indexOf("[");
			int bracketEndIndex = strDisplayVal.indexOf("]");
						
			strDisplayVal = strDisplayVal.substring(bracketStartIndex+1,bracketEndIndex);
			int lenght = strDisplayVal.length();
			int endIndex = strDisplayVal.indexOf("-");
			String strStartDate = strDisplayVal.substring(0, endIndex);
			String strEndDate = strDisplayVal.substring( endIndex+1,lenght);
			slObjectList.add(strStartDate);
			slObjectList.add(strEndDate);
		}
		return slObjectList;
	}
	
	public Vector getColumnSnapshotInDeliverableTable(Context context,String args[])throws MatrixException {
		Vector vec = new Vector();
		try{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectInfoList = (MapList) programMap.get("objectList");

			String []strObjArr = getObjectArray(context, objectInfoList);
			DomainObject dmoObj = DomainObject.newInstance(context);
			MapList objectList = dmoObj.getInfo(context, strObjArr, getExpandSelectables());
			int size = objectList != null? objectList.size(): 0 ;

			for(int ind=0;ind<size;ind++){
				Map mDeliverable = (Map)objectList.get(ind);
				String strActionLink = getColumnSnapshotActionsCellInfo(context,mDeliverable,ProgramCentralConstants.MODE_CREATE_SNAPSHOT);
				vec.add(strActionLink);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return vec;
	}

	public Vector getColumnSnapshotActions(Context context,String args[])throws MatrixException {
		
		Vector vec = null;
		try{
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			String [] strObjectsArr =  getObjectArray(context,objectList); 
			vec = new Vector(strObjectsArr.length);
			
			DomainObject dmoObject = DomainObject.newInstance(context);
			StringList slObjectSelects = getExpandSelectables();
			MapList objectInfoList = dmoObject.getInfo(context, strObjectsArr, slObjectSelects);
			int size = objectInfoList != null? objectInfoList.size() : 0;
			
			for(int ind=0;ind<size;ind++){
				Map mObjectInfo = (Map)objectInfoList.get(ind);
				String strIsProjectSpace  = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
				String strIsSnapshot    = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_IS_KINDOF_PROJECT_SNAPSHOT);

				String strActionLink  = "";
				if("true".equalsIgnoreCase(strIsSnapshot)){
					strActionLink =  getColumnSnapshotActionsCellInfo(context,mObjectInfo,ProgramCentralConstants.MODE_DELETE_SNAPSHOT);
				}else if("true".equalsIgnoreCase(strIsProjectSpace)){
					strActionLink =  getColumnSnapshotActionsCellInfo(context,mObjectInfo,ProgramCentralConstants.MODE_CREATE_SNAPSHOT);
				}
				vec.add(strActionLink);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return vec;
	}

	private String getColumnSnapshotActionsCellInfo(Context context,Map mObjectInfo,String strMode){

		String sToolTipCreateSnapshot = "Create Snapshot";
		String sToolTipDeleteSnapshot = "Delete Snapshot";
		StringBuffer strSnapshotActionBuffer = new StringBuffer();
		String strObjectId = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_ID);
		if(ProgramCentralUtil.isNotNullString(strObjectId))
		{
			/*String strProjectSpace  = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			String strIsSnapshot    = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_IS_KINDOF_PROJECT_SNAPSHOT);*/

			
			
			if(ProgramCentralConstants.MODE_CREATE_SNAPSHOT.equalsIgnoreCase(strMode)){
				String strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=createSnapshot&amp;SuiteDirectory=programcentral&amp;submitAction=refreshCaller&amp;objectId=" + XSSUtil.encodeForURL(context,strObjectId);
				strSnapshotActionBuffer.append("<a href=\""+strURL+"\" target=\"listHidden\">");
				strSnapshotActionBuffer.append("<img border=\"0\" src=\"../common/images/iconActionCreate.gif\" alt=\""+sToolTipCreateSnapshot+"\" title=\""+sToolTipCreateSnapshot+"\"></img></a>&#160;");
			}else if(ProgramCentralConstants.MODE_DELETE_SNAPSHOT.equalsIgnoreCase(strMode)){
				String strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=deleteSnapshot&amp;SuiteDirectory=programcentral&amp;submitAction=refreshCaller&amp;objectId=" + XSSUtil.encodeForURL(context,strObjectId);
				strSnapshotActionBuffer.append("<a href=\""+strURL+"\" target=\"listHidden\">");
				strSnapshotActionBuffer.append("<img border=\"0\" src=\"../common/images/iconActionDelete.gif\" alt=\""+sToolTipDeleteSnapshot+"\" title=\""+sToolTipDeleteSnapshot+"\"></img></a>&#160;");
			}
		}

		return strSnapshotActionBuffer.toString();
	}
	
	private String[] getObjectArray(Context context, MapList objectList){
		
		int size = objectList != null ? objectList.size() : 0;
		String[] strObjArr = new String[size];
		
		for(int ind=0;ind<size;ind++){
			Map mObjectInfo = (Map)objectList.get(ind);
			String strObjectId = (String)mObjectInfo.get(ProgramCentralConstants.SELECT_ID);
			strObjArr[ind] = strObjectId;
		}
		return strObjArr;
	}
	
	
	/**
	 * Gets List of links for WBS column.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args holds information about objects.
	 * @return List of WBS link.
	 * @throws MatrixException if operation fails.
	 */
	public StringList getColumnProjectSnapshotWBS(Context context, String[] args) throws MatrixException
	{
		StringList snpWBSList = new StringList();
		try{
			Map programMap =  JPO.unpackArgs(args);
			Map paramList = (Map) programMap.get("paramList");
			String strProjectId = (String) paramList.get("parentOID");
			MapList objectList = (MapList) programMap.get("objectList");

			for (int i = 0; i < objectList.size(); i++){
				Map mpObjDetails = (Map) objectList.get(i);
				String strTaskId = (String) mpObjDetails.get(ProgramCentralConstants.SELECT_ID);
				String strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=launchWBS&amp;objectid="+XSSUtil.encodeForURL(context,strTaskId);
				String sbLinkMaker = "<a target='listHidden' href=\""+strURL+"\" class='object'>" ;
				float parent = 0;
				float child = 0;
				try {
					if(ProgramCentralUtil.isNotNullString(strProjectId)){
						DomainObject dmoParent = DomainObject.newInstance(context,strProjectId);
						parent = (float) Task.parseToDouble(dmoParent.getAttributeValue(context, DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION));
					}
					if(ProgramCentralUtil.isNotNullString(strTaskId)){
						DomainObject dmoChild = DomainObject.newInstance(context,strTaskId);
						child = (float) Task.parseToDouble(dmoChild.getAttributeValue(context, DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION));
					}
					DecimalFormat decim = new DecimalFormat("#.##");
					sbLinkMaker += Task.parseToDouble(decim.format(parent-child));
				} catch (Exception e) {
					e.printStackTrace();
					sbLinkMaker += DomainObject.EMPTY_STRING;
				}
				sbLinkMaker+= "</a>" ;
				snpWBSList.addElement(sbLinkMaker);
			}
			return snpWBSList;

		}catch (Exception ex){
			ex.printStackTrace();
			throw new MatrixException(ex);
		}
	}
}
