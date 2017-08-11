/**
 * The <code>${CLASSNAME}</code> class contains methods for Deliverable.
 */


import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Company;
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
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.DeliverableIntent;
import java.util.List;

/**
 * The <code>${CLASSNAME}</code> class contains methods for Deliverable Intent.
 *
 */

public class emxDeliverableBase_mxJPO extends DomainObject
{
	public static String DYNAMIC_ATTRIBUTE = "DYNATR";
	public static String INTERFACE = "Interface";
	public static String INTERFACE_ATTRIBUTE = "Interface Attribute";
	public static String NAVIGATE_OBJECTIVE_ID = "from[ParameterUsage].to.id";
	public static String NAVIGATE_OBJECTIVE_NAME = "from[ParameterUsage].to.name";
	
	public emxDeliverableBase_mxJPO(Context context, String[] args){
		
	}

	/**
	 * gets top level deliverables created by a user.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getDeliverables(Context context, String[] args) throws Exception {
		MapList mlAll = new MapList();
		StringList objectSelects = new StringList();
		
		objectSelects.add(DomainObject.SELECT_ID);
		objectSelects.add(DomainObject.SELECT_NAME);
		
		String strWhereAll = ProgramCentralConstants.EMPTY_STRING;
		String strWhereSub = "to[Sub Deliverable] ~~ true";
		
		mlAll = DomainObject.findObjects(context, ProgramCentralConstants.TYPE_DELIVERABLE_INTENT, "*",
				strWhereAll, objectSelects);
		
		MapList mlSub = DomainObject.findObjects(context, ProgramCentralConstants.TYPE_DELIVERABLE_INTENT,
				DomainConstants.QUERY_WILDCARD, strWhereSub, objectSelects);
		
		MapList mlTemplate = DomainObject.findObjects(context,
				ProgramCentralConstants.TYPE_DELIVERABLE_TEMPLATE, DomainConstants.QUERY_WILDCARD, strWhereAll, objectSelects);
		
		for (Iterator iterator = mlSub.iterator(); iterator.hasNext();) {
			Map object = (Map) iterator.next();
			mlAll.remove(object);
		}
		for (Iterator iterator = mlTemplate.iterator(); iterator.hasNext();) {
			Map object = (Map) iterator.next();
			mlAll.remove(object);
		}
		return mlAll;
	}
	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getDeliverablesTemplates(Context context, String[] args) throws Exception{
		DeliverableIntent deliverableIntent = new DeliverableIntent(context);
		MapList mlTemplate = deliverableIntent.getDeliverablesTemplates(context, args);
		return mlTemplate;
	}
	
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getSubDeliverables(Context context, String[] args) throws Exception{
		MapList mlSubDeliverableList = new MapList();
		
		HashMap arguMap = (HashMap)JPO.unpackArgs(args);
		String strObjectId = (String) arguMap.get("objectId");
		
		DeliverableIntent deliverableIntent = new DeliverableIntent(context);
		mlSubDeliverableList = deliverableIntent.getSubDeliverables(context, strObjectId);
		
		return mlSubDeliverableList;
	}
	
	/*public Vector getNameColumn (Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		Vector columnValues = new Vector(objectList.size());
		Iterator objectListIterator = objectList.iterator();
		
		while (objectListIterator.hasNext())
		{
			StringBuffer sBuff = new StringBuffer();
			Map objectMap = (Map) objectListIterator.next();
			String objId = (String) objectMap.get(DomainObject.SELECT_ID);
			String strName = (String)objectMap.get(DomainObject.SELECT_NAME);
			sBuff.append("<a href ='javascript:showModalDialog(\"../common/emxTree.jsp?objectId="+objId+"&amp;treeMenu=type_DeliverableIntent\", \"875\", \"550\", \"false\", \"popup\")' title=\""+strName+"\" >"+strName+"</a>");
			columnValues.add(sBuff.toString());
		}
		
		return columnValues;
	}*/
	
	public MapList getObjectives(Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String projectId = (String) programMap.get("objectId");
		MapList objectiveList = new MapList();
		MapList gateList = new MapList();
		gateList = getToplevelGatesInProject(context, projectId);
		//objectiveList = getToplevelGatesInProject(context, projectId);
		
		StringList objectiveSelects = new StringList(2);
		//objectiveSelects.add(SELECT_ID);
		//objectiveSelects.add(SELECT_NAME);
		objectiveSelects.add(NAVIGATE_OBJECTIVE_ID);
		objectiveSelects.add(NAVIGATE_OBJECTIVE_NAME);
		
		for (Iterator iterator = gateList.iterator(); iterator.hasNext();) {
			Map gateDetails = (Map) iterator.next();
			String gateId = (String)gateDetails.get(SELECT_ID);
			DomainObject gate = DomainObject.newInstance(context,gateId);
			Map objectiveDetails = gate.getInfo(context, objectiveSelects);
			String objectiveId = (String)objectiveDetails.get(NAVIGATE_OBJECTIVE_ID);
			if(null != objectiveId){
				String strMQLQuery = "print bus $1 select $2 dump $3";
				String interfacesConnected = MqlUtil.mqlCommand(context, strMQLQuery, objectiveId, "interface", "|");
				StringList interfaceList = FrameworkUtil.split(interfacesConnected, "|");
				interfaceList.remove("KPIKindObjective");
				interfaceList.remove("KPIKindMeasured");
				String interfaceName = (String)interfaceList.get(0);
				String interfaceAttributeName = "";
				if(ProgramCentralUtil.isNotNullString(interfaceName)){
					//list interface PESDelivery select attribute;
					String strMQLQueryToFindAttribute = "list interface $1 select $2 dump $3";
					String attributesInInterface = MqlUtil.mqlCommand(context, strMQLQueryToFindAttribute, interfaceName, "attribute", "|");
					StringList attributeList = FrameworkUtil.split(attributesInInterface+"|", "|");
					interfaceAttributeName = (String)attributeList.get(0);
				}
				objectiveDetails.put("level", "1");
				objectiveDetails.put(INTERFACE, interfaceName);
				objectiveDetails.put(INTERFACE_ATTRIBUTE, interfaceAttributeName);
				objectiveList.add(objectiveDetails);	
			}
		}
		
		return objectiveList;
	}

	/**
     * Get a list of delivers for a given parent deliverable.
     * @param context  - The ENOVIA context object.
     * @param args 	   
     * @return vector containing delivers list.
     * @throws Exception
     */
	public Vector getDeliversColumn(Context context, String[] args) throws MatrixException {
		Vector vec = new Vector();
		try {

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objects = (MapList) programMap.get("objectList");

			for (int index = 0; index < objects.size(); index++) {
				MapList totalresultList = new MapList();
				Map map = (Map) objects.get(index);
				String strObjectid = (String) map.get(DomainObject.SELECT_ID);
				
				DeliverableIntent deliverableIntent = new DeliverableIntent(context);
				
				totalresultList = deliverableIntent.getDeliversDetails(context, strObjectid);
				
				StringBuffer sbLinkMaker = new StringBuffer();
				
				if (totalresultList.size() == 0) 
					sbLinkMaker.append(ProgramCentralConstants.EMPTY_STRING);

				for (int i = 0; i < totalresultList.size(); i++) {
					String sName = ProgramCentralConstants.EMPTY_STRING;
					String sObjId = ProgramCentralConstants.EMPTY_STRING;

					Map deliverableMap = (Map) totalresultList.get(i);
					sObjId = (String) deliverableMap.get(DomainObject.SELECT_ID);
					sName = (String) deliverableMap.get(DomainObject.SELECT_NAME);
					if (i > 0)
						sbLinkMaker.append(",");
					String strHref = "../common/emxTree.jsp?objectId="
							+ XSSUtil.encodeForURL(context,sObjId);
					sbLinkMaker.append("<a title=\""
									+ XSSUtil.encodeForURL(context,sObjId)
									+ "\" href=\"javascript:emxTableColumnLinkClick('"
									+ strHref);
					sbLinkMaker.append("', '600', '600', 'false', 'windowshade','')\"  class='object'>");
					sbLinkMaker.append(XSSUtil.encodeForHTML(context,sName));
					sbLinkMaker.append("</a>");
				}
				
				vec.add(sbLinkMaker.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return vec;
	}
	
	/**
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 */
	public Vector gateObjectiveAttributeDetails(Context context, String[] args) throws MatrixException {
		Vector vec = new Vector();
		try{

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objects = (MapList) programMap.get("objectList");

			for(int ind=0;ind<objects.size();ind++){
				Map map = (Map)objects.get(ind);
				String objectiveId = map.get(NAVIGATE_OBJECTIVE_ID).toString();
				if(ProgramCentralUtil.isNotNullString(objectiveId)){
					String objectiveAttribute = map.get(INTERFACE_ATTRIBUTE).toString();
					DomainObject objective = DomainObject.newInstance(context,objectiveId);
					String objectiveAttributeValue = objective.getAttributeValue(context, objectiveAttribute);
					vec.add(objectiveAttributeValue);	
				}else {
					vec.add(DomainConstants.EMPTY_STRING);
				}
				
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return vec;
	}

	/**
	 * will enable Plan Snapshot creation.
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 */
	public List planSnapShotActions(Context context, String[] args) throws MatrixException {
		List listTemplateIcon = new StringList();
		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList mlObjectData = (MapList) programMap.get("objectList");
			String titleDeliTmt= DomainConstants.EMPTY_STRING;
					
			for (int i = 0; i < mlObjectData.size(); i++) {
				StringBuffer sBuff = new StringBuffer();
				Map mapObjectInfo = (Map) mlObjectData.get(i);
				String strObjectId = (String) mapObjectInfo.get(DomainConstants.SELECT_ID);
				/*DomainObject dmoObject = DomainObject.newInstance(context,
						strObjectId);
				
				StringList objectSelects = new StringList(3);
				StringList slRelSelects = new StringList();
				objectSelects.addElement(DomainConstants.SELECT_ID);
				objectSelects.addElement(DomainConstants.SELECT_NAME);

				MapList mapList = new MapList();
				mapList = dmoObject.getRelatedObjects(context,
						ProgramCentralConstants.RELATIONSHIP_GOVERNING_PROJECT,		// relationship pattern
						ProgramCentralConstants.TYPE_DELIVERABLETEMPLATE, 			// object pattern
						objectSelects, 											    // object selects
						slRelSelects,												// relationship selects
						false, 														// to direction
						true, 														// from direction
						(short) 0, 													// recursion level
						null, 														// object where clause
						ProgramCentralConstants.EMPTY_STRING,						// relationship where clause
						0);	*/														// count limit; 0 for no limit.
				
				//if (mapList.size() == 0) {
					titleDeliTmt = EnoviaResourceBundle.getProperty(context,ProgramCentralConstants.PROGRAMCENTRAL, 
							"emxProgramCentral.Common.CreateDeliverableTemplate", context.getSession().getLanguage());
					String strURL = "../programcentral/emxProgramCentralUtil.jsp?mode=cloneAsDeliverableTemplate&amp;objectId=" + XSSUtil.encodeForURL(context,strObjectId);
					String hyper = "<a href=\"" + strURL + "\" target=\"listHidden\">";
					sBuff.append(hyper+ "<img src='../common/images/iconActionCopyTo.gif' border='0' alt= \"" + XSSUtil.encodeForHTML(context,titleDeliTmt) + "\" title= \"" + XSSUtil.encodeForHTML(context,titleDeliTmt) + "\"></img></a>");
				/*} else {
					
					titleDeliTmt = EnoviaResourceBundle.getProperty(context,ProgramCentralConstants.PROGRAMCENTRAL, 
				    		"emxProgramCentral.DeliverableTemplate.Exists", context.getSession().getLanguage());
					sBuff.append("<img src='../common/images/iconActionCopyTo-disabled.gif' border='0' alt=\"" + titleDeliTmt + "\" title=\"" + titleDeliTmt + "\"></img>");
				}*/
				listTemplateIcon.add(sBuff.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return listTemplateIcon;
	}

	/**
	 * gets Governing Projects connected to the deliverable.
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 */
	public Vector getProjectPlan(Context context, String[] args) throws MatrixException {
		Vector vec = new Vector();
		try {

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objects = (MapList) programMap.get("objectList");

			DeliverableIntent deliverableIntent = new DeliverableIntent(context);

			for (int ind = 0; ind < objects.size(); ind++) {
				String strName = ProgramCentralConstants.EMPTY_STRING;
				String strProjectId = ProgramCentralConstants.EMPTY_STRING;
				Map mp = (Map) objects.get(ind);
				String strObjectid = (String) mp.get(DomainObject.SELECT_ID);
				MapList ml = deliverableIntent.getConnectedProjectPlan(context,	strObjectid);
				
				if (ml.size() == 0)
					vec.add(strName);
				
				for (int i = 0; i < ml.size(); i++) {
					Map map = (Map) ml.get(i);
					strName = (String) map.get(DomainObject.SELECT_NAME);
					strProjectId = (String) map.get(DomainObject.SELECT_ID);
					String strHref = "../common/emxTree.jsp?objectId="
							+ strProjectId;
					StringBuffer sbLinkMaker = new StringBuffer();
					sbLinkMaker.append("<a title=\"" + XSSUtil.encodeForHTML(context,strName)
							+ "\" href=\"javascript:emxTableColumnLinkClick('"
							+ strHref);
					sbLinkMaker.append("', '600', '600', 'false', 'windowshade','')\"  class='object'>");
					sbLinkMaker.append("<img src='../common/images/iconSmallProject.gif' border='0' alt='"
									+ XSSUtil.encodeForHTML(context,strName) + "' ></img>" + XSSUtil.encodeForHTML(context,strName));
					sbLinkMaker.append("</a>");
					vec.add(sbLinkMaker.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return vec;
	}
	
	/**
	 * gets Governing Projects connected to the deliverable.
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 */
	public List getProjectPlanForDeliverableTemplate(Context context, String[] args) throws MatrixException {
		List slDeliverTmt = new StringList();
		try {

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objects = (MapList) programMap.get("objectList");

			DeliverableIntent deliverableIntent = new DeliverableIntent(context);

			if(objects != null)
			{
			for (int ind = 0; ind < objects.size(); ind++) {
				String strName = ProgramCentralConstants.EMPTY_STRING;
				String strProjectId = ProgramCentralConstants.EMPTY_STRING;
				Map mp = (Map) objects.get(ind);
				String strObjectid = (String) mp.get(DomainObject.SELECT_ID);
				MapList ml = deliverableIntent.getConnectedProjectSnapshots(context, strObjectid);
				
				if (ml.size() == 0)
					slDeliverTmt.add(strName);
				
				for (int i = 0; i < ml.size(); i++) {
					Map map = (Map) ml.get(i);
					strName = (String) map.get(DomainObject.SELECT_NAME);
					strProjectId = (String) map.get(DomainObject.SELECT_ID);
					String strHref = "../common/emxTree.jsp?objectId="
							+XSSUtil.encodeForURL(context, strProjectId);
					StringBuffer sbLinkMaker = new StringBuffer();
					sbLinkMaker.append("<a title=\"" +XSSUtil.encodeForHTML(context, strName)
							+ "\" href=\"javascript:emxTableColumnLinkClick('"
							+ strHref);
					sbLinkMaker.append("', '600', '600', 'false', 'windowshade','')\"  class='object'>");
					sbLinkMaker.append("<img src='../common/images/iconSmallProject.gif' border='0' alt='"
									+XSSUtil.encodeForHTML(context, strName) + "' ></img>" + XSSUtil.encodeForHTML(context,strName));
					sbLinkMaker.append("</a>");
						slDeliverTmt.add(sbLinkMaker.toString());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return slDeliverTmt;
	}
	
	/**
	 * return associated project connected to the deliverable  
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getConnectedProjectPlan(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		DeliverableIntent deliverableIntent = new DeliverableIntent(context);
		MapList ml = deliverableIntent.getConnectedProjectPlan(context, objectId);
		return ml;
	}
	
	/**
	 * gets delivers list associated with deliverable 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getDeliversDetails(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");

		MapList totalresultList = new MapList();

		DeliverableIntent deliverableIntent = new DeliverableIntent(context);
		totalresultList = deliverableIntent.getDeliversDetails(context, objectId);
		return totalresultList;
	}
	
	/**
	 * gets start date of the project plan
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 */
	public Vector getProjectPlanStartDate(Context context, String[] args) throws MatrixException{
		Vector vec = new Vector();
		try{

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objects = (MapList) programMap.get("objectList");

			for(int ind=0;ind<objects.size();ind++){
				Map mp = (Map)objects.get(ind);
				String strObjectid = (String)mp.get(DomainObject.SELECT_ID);
				DomainObject dmoObject = DomainObject.newInstance(context, strObjectid);
				String strName = dmoObject.getInfo(context, "from[Governing Project].to.attribute[Task Estimated Start Date].value");
				if(ProgramCentralUtil.isNullString(strName))
					strName = ProgramCentralConstants.EMPTY_STRING;
				vec.add(strName);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return vec;
	}
	
	/**
	 * get Governing ProjectPlan End Date
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 */
	public Vector getProjectPlanEndDate(Context context, String[] args) throws MatrixException{
		Vector vec = new Vector();
		try{

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objects = (MapList) programMap.get("objectList");

			for(int ind=0;ind<objects.size();ind++){
				Map mp = (Map)objects.get(ind);
				String strObjectid = (String)mp.get(DomainObject.SELECT_ID);
				DomainObject dmoObject = DomainObject.newInstance(context, strObjectid);
				String strName = dmoObject.getInfo(context, "from[Governing Project].to.attribute[Task Estimated Finish Date].value");
				if(ProgramCentralUtil.isNullString(strName))
					strName = ProgramCentralConstants.EMPTY_STRING;
				vec.add(strName);
			}
		} catch (Exception e){
			e.printStackTrace();
			throw new MatrixException(e);
		}
		return vec;
	}

	/**
	 * connects newly created deliverable to its parent and updates the sequence number.
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void connectDeliverable(Context context, String[] args) throws Exception{

		Map programMap = (HashMap) JPO.unpackArgs(args);
		Map requestMap = (Map) programMap.get("requestMap");
		Map paramMap = (Map) programMap.get("paramMap");
		String strParent = (String)requestMap.get("objectId");
		String strNewObjectId = (String)paramMap.get("newObjectId");
		
		if(ProgramCentralUtil.isNotNullString(strParent)){
			DomainObject dmoParent = DomainObject.newInstance(context,strParent);
			DomainObject dmoNewObject = DomainObject.newInstance(context,strNewObjectId);
			try{
				//DomainRelationship.connect(context,dmoParent,"Sub Deliverable",dmoNewObject);
				DomainRelationship.connect(context,dmoParent,ProgramCentralConstants.RELATIONSHIP_SUB_DELIVERABLE,dmoNewObject);
			}catch(Exception e){
				e.printStackTrace();
			}
		}		
	}	
	
	/**
	 * shows search result that are not connected to deliverables.
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getExcludeOIDForDelivers(Context context, String []args)throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);		
		String strDeliverabelIntentId = (String)programMap.get("objectId"); 
		DeliverableIntent deliverableIntent = new DeliverableIntent(context);
		
        MapList mapDeliversList = null;
        StringList slExcludeId = new StringList();
        
        StringList slRelSelects  = new StringList();
		MapList totalresultList = deliverableIntent.getDeliversDetails(context, strDeliverabelIntentId);
    
       Iterator itrConnectedDelivers = totalresultList.iterator();
       while(itrConnectedDelivers.hasNext()){
           Map mapConnectedDeliver = (Map)itrConnectedDelivers.next();
           slExcludeId.add(mapConnectedDeliver.get(DomainConstants.SELECT_ID));
       }
       return slExcludeId;
    }	
	
	/**
	 * connects and disconnects the project with deliverable.
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void connectProject(Context context, String[] args) throws Exception{
		Map programMap =   (Map)JPO.unpackArgs(args);
		HashMap paramMap      = (HashMap) programMap.get("paramMap");
		String objectID = (String)paramMap.get("objectId");
		String newAttrValue = (String) paramMap.get("New Value");
		String newAttrId = (String) paramMap.get("New OID");
		DeliverableIntent deliverableIntent = new DeliverableIntent(context);
		deliverableIntent.connectProject(context, args);
	}
	
	/**
	 * To link any ENOVIA object to Deliverable
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void connectDelivers(Context context, String[] args) throws Exception {
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			Map mpParamMap = (HashMap)programMap.get("paramMap");
			String strObjToDeliver = (String)mpParamMap.get("New Value");
			StringList slDelivers = FrameworkUtil.split(strObjToDeliver, ",");
			String strDeliverableId = (String)mpParamMap.get("objectId");
			
			DeliverableIntent deliverableIntent = new DeliverableIntent(context);
			DeliverableIntent.connectDelivers(context, strDeliverableId, slDelivers);
		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}
	/**
	 * Shows project list that logged in user is member of in a combo box, list will contain create, active, review  projects.
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public HashMap getProjectsToConnect(Context context, String[] args) throws Exception {
		HashMap mapProjectDetails = new HashMap();
		
		StringList slActual = new StringList();
		slActual.add(ProgramCentralConstants.EMPTY_STRING);
		
		StringList slDisplay = new StringList();
		slDisplay.add(ProgramCentralConstants.EMPTY_STRING);
		
		DeliverableIntent deliverableIntent = new DeliverableIntent(context);
		MapList projectList = deliverableIntent.getProjectsToConnect(context, args);

		for (Iterator iterator = projectList.iterator(); iterator.hasNext();) {
			Map mpProjectDetails = (Map) iterator.next();
			String strId = (String) mpProjectDetails.get(DomainObject.SELECT_ID);
			String strName = (String) mpProjectDetails.get(DomainObject.SELECT_NAME);
			slActual.add(strId);
			slDisplay.add(strName);
		}
		mapProjectDetails.put("field_choices", slActual);
		mapProjectDetails.put("field_display_choices", slDisplay);
		return mapProjectDetails;
	}
	
	/**
	 * Shows deliverable template list.
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public HashMap getDeliverableTemplateMap(Context context, String[] args) throws Exception {
		HashMap mapDeliverableTmtDetails = new HashMap();
		
		StringList slActual = new StringList();
		slActual.add(ProgramCentralConstants.EMPTY_STRING);
		
		StringList slDisplay = new StringList();
		slDisplay.add(ProgramCentralConstants.EMPTY_STRING);
		
		MapList mlDeliverableTmt = getDeliverablesTemplates(context, args);

		for (Iterator iterator = mlDeliverableTmt.iterator(); iterator.hasNext();) {
			Map mpProjectDetails = (Map) iterator.next();
			String strId = (String) mpProjectDetails.get(DomainObject.SELECT_ID);
			String strName = (String) mpProjectDetails.get(DomainObject.SELECT_NAME);
			slActual.add(strId);
			slDisplay.add(strName);
		}
		mapDeliverableTmtDetails.put("field_choices", slActual);
		mapDeliverableTmtDetails.put("field_display_choices", slDisplay);
		return mapDeliverableTmtDetails;
	}
	
	/* Returns reload range value of project plan associated with deliverable template 
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the following input arguments:
	 *        0 - String containing the object id
	 * @throws Exception if operation fails
	 * @since PRG R211
	 */
	public static Map getConnectedPlanOfDeliverableTemplate(Context context, String[] args)throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap hmfieldValues = (HashMap) programMap.get("fieldValues");
		String strObjectId = (String) hmfieldValues.get("DeliverableTemplate");
		String strLan = context.getSession().getLanguage();
		StringList slProjectPlan = new StringList();
		StringList slProjectPlanId = new StringList();

		if(ProgramCentralUtil.isNullString(strObjectId)) {
			slProjectPlan.add(ProgramCentralConstants.EMPTY_STRING);
			slProjectPlanId.add(ProgramCentralConstants.EMPTY_STRING);
			//throw new IllegalArgumentException("Object id is null");
		}
		else {
		       MapList mlProjectPlan = new MapList();
		       mlProjectPlan = DeliverableIntent.getProjectSnapshotOfDeliverableTemplate(context, strObjectId);

		
		       for(int i =0; i< mlProjectPlan.size(); i++){
			    Map mapProjectPlan = new HashMap();
			    mapProjectPlan = (Map)mlProjectPlan.get(i);
			    slProjectPlan.add(mapProjectPlan.get(DomainConstants.SELECT_NAME).toString());
			    slProjectPlanId.add(mapProjectPlan.get(DomainConstants.SELECT_ID).toString());
		      }		
		}

		Map returnMap = new HashMap();
		returnMap.put("RangeValues", slProjectPlanId);
		returnMap.put("RangeDisplayValues", slProjectPlan);

		return returnMap;
	}
	
	/**
	 * gets Dynamic columns for Gates
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public MapList getDynamicColumnsInObjectiveView (Context context, String[] args) throws Exception
	{
		final String strDateFormat = eMatrixDateFormat.getEMatrixDateFormat();
		Map programMap = (Map) JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
		
		String strProjectId = (String)requestMap.get("objectId");
		
		MapList mlGates = getToplevelGatesInProject(context,strProjectId);

		Map mapColumn = new HashMap();
		Map<String,String> mapSettings = new HashMap<String,String>();
		MapList mlColumns = new MapList();
		
		for (Iterator iterator = mlGates.iterator(); iterator.hasNext();) {
			
			Map map = (Map) iterator.next();
			String strGateName = map.get(DomainConstants.SELECT_NAME).toString();
			String strGateId = map.get(DomainConstants.SELECT_ID).toString();
			String strType = map.get(DomainConstants.SELECT_TYPE).toString();
			
			mapColumn = new HashMap();
			mapColumn.put("name", strGateId);
			mapColumn.put("label", strGateName);
			
			mapSettings = new HashMap();
			mapSettings.put("Registered Suite","ProgramCentral");
			mapSettings.put("program","emxDeliverable");
			mapSettings.put("Group Header",strType);
			mapSettings.put("function","gateObjectiveAttributeDetails");
			mapSettings.put("Column Type","programHTMLOutput");
			mapSettings.put("Editable","true");
			mapSettings.put("Export","true");
			mapSettings.put("Field Type","Basic");
			mapSettings.put("Sortable","false");
			mapColumn.put("settings", mapSettings);
			mlColumns.add(mapColumn);
		}
		
		return mlColumns;
	}
	
	/**
	 * gets top level Gates in a Project
	 * @param context
	 * @param strProjectId
	 * @return
	 */
	private MapList getToplevelGatesInProject(Context context, String strProjectId){
		MapList mlStruct = new MapList();
		
		StringList objSelect = new StringList();
		objSelect.add(DomainConstants.SELECT_NAME);
		objSelect.add(DomainConstants.SELECT_TYPE);
		objSelect.add(DomainConstants.SELECT_ID);
		
		try {
			DomainObject projectSpace = DomainObject.newInstance(context, strProjectId);
			mlStruct = projectSpace.getRelatedObjects(context,
									DomainConstants.RELATIONSHIP_SUBTASK,
									ProgramCentralConstants.TYPE_GATE,
									objSelect,
									null,       // relationshipSelects
									false,      // getTo
									true,       // getFrom
									(short) 1,  // recurseToLevel
									null,       // objectWhere
									null,
									0);
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return mlStruct;
	}
	
	/**
	 * gets top level Gates in a Project
	 * @param context
	 * @param strProjectId
	 * @return
	 */
	private MapList getObjectivesConnectedToGates(Context context, String strGateId, StringList objectSelects){
		MapList mlStruct = new MapList();
		
		try {
			DomainObject projectSpace = DomainObject.newInstance(context, strGateId);
			mlStruct = projectSpace.getRelatedObjects(context,
									"ParameterUsage",
									"PlmParameter",
									objectSelects,
									null,       // relationshipSelects
									false,      // getTo
									true,       // getFrom
									(short) 1,  // recurseToLevel
									null,       // objectWhere
									null,
									0);
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return mlStruct;
	}
	
	/**
	 * gets checkbox for each top level gate in Project
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Map getGatesForObjectives(Context context, String[] args) throws Exception{
		Map returnMap = new HashMap();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map)programMap.get("requestMap");

			String strProjectId = (String)requestMap.get("objectId");
			MapList mlGates = getToplevelGatesInProject(context,strProjectId);

			Map mapColumn = new HashMap();
			Map<String,String> mapSettings = new HashMap<String,String>();
			MapList mlColumns = new MapList();

			StringList GateListNames = new StringList();
			StringList GateListIds = new StringList();
			for (Iterator iterator = mlGates.iterator(); iterator.hasNext();) {

				Map map = (Map) iterator.next();
				String strGateName = map.get(DomainConstants.SELECT_NAME).toString();
				String strGateId = map.get(DomainConstants.SELECT_ID).toString();
				String strType = map.get(DomainConstants.SELECT_TYPE).toString();
				GateListIds.add(strGateId);
				GateListNames.add(strGateName);
			}
			
			String selectedGateId = (String)requestMap.get("KPIGateId");
			if(ProgramCentralUtil.isNotNullString(selectedGateId)){
				DomainObject gate = DomainObject.newInstance(context,selectedGateId);
				String selectGateName = gate.getName();
				GateListIds.remove(selectedGateId);
				GateListIds.add(0, selectedGateId);
				GateListNames.remove(selectGateName);
				GateListNames.add(0, selectGateName);
			}
			
			returnMap.put("field_choices", GateListIds);
			returnMap.put("field_display_choices", GateListNames);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} return returnMap;
	}
	
	/**
	 * get Objectives Interface Attributes 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Map getObjectivesAttributes(Context context, String[] args) throws Exception{
		Map returnMap = new HashMap();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map)programMap.get("requestMap");
			String selectedKPI = (String)requestMap.get("KPITypeId");
			String strMQLQuery = "list interface $1 select $2 dump $3";
			String strResultSubType = MqlUtil.mqlCommand(context, strMQLQuery,"KPIType", "allchildren", "|");
			StringList slTaskSubTypes = FrameworkUtil.split(strResultSubType, "|");
			if(ProgramCentralUtil.isNotNullString(selectedKPI)){
				slTaskSubTypes.remove(selectedKPI);
				slTaskSubTypes.add(0, selectedKPI);
			}
			returnMap.put("field_choices", slTaskSubTypes);
			returnMap.put("field_display_choices", slTaskSubTypes);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} return returnMap;
	}
	
	/**
	 * getRevisions for Objective.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Map getRevisions (Context context, String[] args) throws Exception{
		Map returnMap = new HashMap();
		String revisions = "A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z";
		StringList revisionList = FrameworkUtil.split(revisions, "|");
		returnMap.put("field_choices", revisionList);
		returnMap.put("field_display_choices", revisionList);
		return returnMap;
	}
	
	/**
	 * get possible KPIs to connect.
	 * @param context
	 * @param args
	 * @return
	 */
	public String getKPITypes(Context context, String[] args){
		String strOuput="";
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map)programMap.get("requestMap");
			String selectedKPI = (String)requestMap.get("KPITypeId");
			StringBuffer output = new StringBuffer();
			if(ProgramCentralUtil.isNotNullString(selectedKPI)){
				String strMQLQuery = "list interface $1 select $2 dump $3";
				String attributesinKPT = MqlUtil.mqlCommand(context, strMQLQuery,selectedKPI, "attribute", "|");
				StringList attributeList = FrameworkUtil.split(attributesinKPT, "|");
				output.append("<table border=\"0\">");
				for (Iterator iterator = attributeList.iterator(); iterator
						.hasNext();) {
					String attributeInKPI = (String) iterator.next();
					output.append("<tr>");
					output.append("<td>"+"<label><strong>"+attributeInKPI+"</strong></label>"+"</td>");
					output.append("</tr>");
					output.append("<tr>");
					output.append("<td>"+"<input type='text' name='"+DYNAMIC_ATTRIBUTE+attributeInKPI+"' value=\""+""+"\"/>"+"</td>");
					output.append("</tr>");
				}
				output.append("</table>");
				
			}
			strOuput = output.toString();
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strOuput ;
	}
	
	/**
     * This is an Utility method to add Interface to Business Object or Relationship and set the Interface Attributes
     * 
      * @param context
     * @param strId - Object Id or connection Id on which Interface to be added
     * @param strAdmin - Interface to be added on bus or relationship
     *                             Possible values : bus or rel
     * @param strInterfaceName - Name of the Interface to be added
     * @param mAttrDetails - Map containing the key-value pair of interface attributes.
     * @return - bResult - true if attribute is set successfully
     *                             - false in case of failure. 
      * @throws Exception
     * @author IVU
     * @since R211
     */
     public static boolean addInterfaceAndSetAttributes(Context context, String strId, String strAdmin, 
                                                            String strInterfaceName, Map mAttrDetails)throws Exception{
           boolean bResult = true;
           try{
                 String strMessage = "";
                 /*
                 String strCommand = "modify "+ strAdmin + " " + strId +" add interface \'"+ strInterfaceName + "\'";
                 strMessage = MqlUtil.mqlCommand(context,strCommand);     
                 */
                 String strCommand = "modify $1 $2 add interface $3";
                 strMessage = MqlUtil.mqlCommand(context,strCommand, strAdmin, strId, strInterfaceName);
                 // check if the attribute Map is not null, if it is not null then set the attributes on the admin
                 if(mAttrDetails!=null && mAttrDetails.size()>0){
                       if(strAdmin.equalsIgnoreCase("connection")){
                             DomainRelationship domrel = new DomainRelationship(strId);
                             domrel.setAttributeValues(context,mAttrDetails);
                       }else{
                             DomainObject dom = new DomainObject(strId);
                             dom.setAttributeValues(context,mAttrDetails);
                       }
                 }                 
           }catch(Exception e){
                 bResult = false;
                 throw new FrameworkException(e.getMessage());
           }
           return bResult;
     }
     
    public boolean hasAccessToSaveAsDeliverableTemplate(Context context, String args[]) throws Exception
 	{
    	boolean isProjectLead = true;
    	try{
    		String strProjectLead = ProgramCentralConstants.ROLE_PROJECT_LEAD;
    		Vector vRoles = PersonUtil.getUserRoles(context);
    		StringList slVPLMRoles = new StringList(); 
			StringList VPLMRoleList = new StringList();
    		
    		for(int i=0;i<vRoles.size();i++){
				String strRole = (String)vRoles.get(i);
				if(strRole.contains("ctx::")){
					strRole = strRole.substring(5, strRole.length());
					String []role = strRole.split("\\.");
					if(role.length>0){
						if(! slVPLMRoles.contains(role[0]))
							slVPLMRoles.addElement(role[0]);
					}
				}
			}
    		
    		for(int j=0;j<slVPLMRoles.size();j++){
				String strRole = (String)slVPLMRoles.get(j);
				StringList roleList  = ProgramCentralUtil.getRoleHeirarchy(context, strRole);
				VPLMRoleList.addAll(roleList);
			}
    		if(VPLMRoleList.contains(strProjectLead))
    			isProjectLead = true;
    		else
    			isProjectLead = false;
    	}
    	catch(Exception e){
    		throw new FrameworkException(e.getMessage());
    	}
    	return isProjectLead;
 	}
}

