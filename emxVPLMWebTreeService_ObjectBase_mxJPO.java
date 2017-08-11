import java.util.Map;
import java.util.Stack;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.requirements.RequirementsCommon;
import com.matrixone.apps.requirements.RequirementsUtil;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;

public class emxVPLMWebTreeService_ObjectBase_mxJPO extends emxDomainObject_mxJPO {
	/**
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @throws Exception
	 *             if the operation fails
	 * @since WhereUsed R212
	 * @grade 0
	 */
	public emxVPLMWebTreeService_ObjectBase_mxJPO (Context context, String[] args) throws Exception{
		super(context,args);
	}
	/**
	 * @param args
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		return 0;
	}
	
	
	private MapList putVPLMObjectsOnJSONObject(Context context, JSONObject object) throws MatrixException{
		String[] strRowId = new String[1];
		String strTypeName = (String)object.get(SELECT_TYPE);
		MapList mapLstAllVPLMObjects = new MapList();
		JSONArray JSONArrChildren = null;
		if(object.isNull("children")){
			JSONArrChildren = new JSONArray();
		}else{
			JSONArrChildren = object.getJSONArray("children");
		}
		if(strTypeName.equalsIgnoreCase(ReqSchemaUtil.getRequirementSpecificationType(context))||strTypeName.equalsIgnoreCase(ReqSchemaUtil.getRequirementType(context))){
			strRowId[0] = (String)object.get(SELECT_ID);
			MapList functionalList = RequirementsCommon.getVPLMFRoots(context, "Functional", strRowId);
			for(int i = 0; i<functionalList.size();i++)
			{
				Map childMap = (Map)functionalList.get(i);
				JSONObject JSONObjNewChild = new JSONObject();
				JSONObjNewChild.put("id", (String)childMap.get("PLM_FunctionRoot"));
				JSONObjNewChild.put("name", (String)childMap.get("PLM_FunctionName"));
				JSONArrChildren.put(JSONObjNewChild);
			}
			MapList LogicalList = RequirementsCommon.getVPLMFRoots(context,"Logical", strRowId);
			for(int i = 0; i<LogicalList.size();i++)
			{
				Map childMap = (Map)LogicalList.get(i);
				JSONObject JSONObjNewChild = new JSONObject();
				JSONObjNewChild.put("id", (String)childMap.get("PLM_LogicalRoot"));
				JSONObjNewChild.put("name", (String)childMap.get("PLM_LogicalName"));
				JSONArrChildren.put(JSONObjNewChild);
			}
			MapList PhysicalList = RequirementsCommon.getVPLMFRoots(context, "Physical", strRowId);
			for(int i = 0; i<PhysicalList.size();i++)
			{
				Map childMap = (Map)PhysicalList.get(i);
				JSONObject JSONObjNewChild = new JSONObject();
				JSONObjNewChild.put("id", (String)childMap.get("PLM_PhysicalRoot"));
				JSONObjNewChild.put("name", (String)childMap.get("PLM_PhysicalName"));
				JSONArrChildren.put(JSONObjNewChild);
			}
			mapLstAllVPLMObjects.addAll(functionalList);
			mapLstAllVPLMObjects.addAll(LogicalList);
			mapLstAllVPLMObjects.addAll(PhysicalList);
		}else{
			mapLstAllVPLMObjects = null;
		}
		return mapLstAllVPLMObjects;
	}
	
	private Stack getChildrenStruct(Context context, Stack objectStack, MapList listOfMap, boolean boolGetFLObjects) throws MatrixException{
			int level = 0;
			for(int i = 0; i<listOfMap.size(); i++){
				JSONObject JSONObjStackObject = (JSONObject)objectStack.peek();
				String strStackLevel = (String)JSONObjStackObject.get(SELECT_LEVEL);
				int iStackLevel = Integer.parseInt(strStackLevel);
				
				Map MapObject = (Map)listOfMap.get(i);
				String strId       = (String)MapObject.get(SELECT_ID);
				String strName     = (String)MapObject.get(SELECT_NAME);
				String strType     = (String)MapObject.get(SELECT_TYPE);
				String strLevel     = (String)MapObject.get(SELECT_LEVEL);
				String strRelType    = (String)MapObject.get(SELECT_RELATIONSHIP_TYPE);
				
				JSONObject JSONObject = new JSONObject();
				JSONObject.put(SELECT_ID,strId);
				JSONObject.put(SELECT_NAME,strName);
				JSONObject.put(SELECT_TYPE,strType);
				JSONObject.put(SELECT_LEVEL,strLevel);
				JSONObject.put(SELECT_RELATIONSHIP_TYPE,strRelType);
				String strIconPath = RequirementsCommon.getIconForType(context, strType, strRelType);
				JSONObject.put("icon",strIconPath);
				int iActualLevel = Integer.parseInt(strLevel);
				
				if(iActualLevel>iStackLevel){
					//List<JSONObject> list = new List<JSONObject>();
					JSONArray array = new JSONArray();
					//list.add(JSONObject);
					array.put(JSONObject);
					objectStack.add(JSONObject);
					//stackObject.put("children", list);
					JSONObjStackObject.put("children", array);
					if(boolGetFLObjects == true){
						putVPLMObjectsOnJSONObject(context, JSONObjStackObject);
					}					
				}else{
					//boolean isLeaf=false;
					while( iStackLevel >=  iActualLevel){
						JSONObject temp2 = (JSONObject)objectStack.pop();
						//if(isLeaf==false)
						//{
							//isLeaf=true;
							//temp2.put("leaf", "true");
						//}
						JSONObject JSONOTemp = (JSONObject)objectStack.peek();
						iStackLevel = Integer.parseInt((String)JSONOTemp.get(SELECT_LEVEL));
					}
					JSONObject previousObjectStack =  (JSONObject)objectStack.peek();
					//List tempList = (List)previousObjectStack.get("children");
					JSONArray tempList = (JSONArray)previousObjectStack.get("children");
					//tempList.add(JSONObject);
					tempList.put(JSONObject);
					previousObjectStack.put("children", tempList);
					objectStack.push(JSONObject);
				}
			}
		JSONObject JSONObjLastObject =  (JSONObject)objectStack.peek();
		//lastObject.put("leaf", "true");
		if(boolGetFLObjects == true){
			putVPLMObjectsOnJSONObject(context, JSONObjLastObject);
		}
		return objectStack;
	}
	
	private JSONObject createJSONStruct(Context context, Map rootObject, MapList children, boolean boolGetFLObjects) throws MatrixException{
		JSONObject JSONStruct = new JSONObject();
		JSONStruct.put(SELECT_ID, rootObject.get(SELECT_ID));
		JSONStruct.put(SELECT_NAME, rootObject.get(SELECT_NAME));
		String strType = (String)rootObject.get(SELECT_TYPE);
		JSONStruct.put(SELECT_TYPE, strType);
		String strIconPath = RequirementsCommon.getIconForType(context, strType, null);
		JSONStruct.put("icon", strIconPath);
		JSONStruct.put(SELECT_LEVEL, "0");
		Stack objectStack = new Stack();
		objectStack.push(JSONStruct);
		if(children != null){
			getChildrenStruct(context,objectStack, children, boolGetFLObjects);
		}
		return JSONStruct;
	}

	
	private String getChildrenObjectTypesFromId(Context context, String objectId) throws FrameworkException{
		DomainObject domOject = DomainObject.newInstance(context, objectId);
		domOject.openObject(context);
		String strObjectType = domOject.getType(context);
		String strObjTypes = null;;
		if(ReqSchemaUtil.getRequirementSpecificationType(context).equalsIgnoreCase(strObjectType)||
			ReqSchemaUtil.getChapterType(context).equalsIgnoreCase(strObjectType)){
			strObjTypes = ReqSchemaUtil.getRequirementType(context) + "," +
            ReqSchemaUtil.getChapterType(context) + "," +
            ReqSchemaUtil.getCommentType(context);
		}else if(ReqSchemaUtil.getRequirementType(context).equalsIgnoreCase(strObjectType)){
			strObjTypes = ReqSchemaUtil.getRequirementType(context);
		}else if(ReqSchemaUtil.getCommentType(context).equalsIgnoreCase(strObjectType)){
			strObjTypes = "";
		}else{
			strObjTypes = null;
		}
		return strObjTypes;
	}
	
	private String getToRelTypesFromId(Context context, String objectId, int depth) throws FrameworkException{
		DomainObject domOject = DomainObject.newInstance(context, objectId);
		domOject.openObject(context);
		String strObjectType = domOject.getType(context);
		String strRelTypes = null;;
		if(ReqSchemaUtil.getRequirementSpecificationType(context).equalsIgnoreCase(strObjectType)||
			ReqSchemaUtil.getChapterType(context).equalsIgnoreCase(strObjectType)){
			if(depth == 1){
				strRelTypes = 	ReqSchemaUtil.getSpecStructureRelationship(context);
			}else{
				strRelTypes = 	ReqSchemaUtil.getSpecStructureRelationship(context) + "," + 
				ReqSchemaUtil.getSubRequirementRelationship(context)+ "," +
				ReqSchemaUtil.getDerivedRequirementRelationship(context);
			}
		}else if(ReqSchemaUtil.getRequirementType(context).equalsIgnoreCase(strObjectType)){
			strRelTypes = ReqSchemaUtil.getSubRequirementRelationship(context)+ "," +
							ReqSchemaUtil.getDerivedRequirementRelationship(context);;
		}else if(ReqSchemaUtil.getCommentType(context).equalsIgnoreCase(strObjectType)){
			strRelTypes = "";
		}else{
			strRelTypes = null;
		}
		return strRelTypes;
	}
	
	private String getParentObjectTypesFromId(Context context, String objectId) throws FrameworkException{
		DomainObject domOject = DomainObject.newInstance(context, objectId);
		domOject.openObject(context);
		String strObjectType = domOject.getType(context);
		String strObjTypes = null;;
		if(ReqSchemaUtil.getRequirementSpecificationType(context).equalsIgnoreCase(strObjectType)){
			strObjTypes = "";
		}else if(ReqSchemaUtil.getChapterType(context).equalsIgnoreCase(strObjectType)){
			strObjTypes =   ReqSchemaUtil.getChapterType(context) + "," +
							ReqSchemaUtil.getRequirementSpecificationType(context);
		}else if(ReqSchemaUtil.getRequirementType(context).equalsIgnoreCase(strObjectType)){
			strObjTypes =   ReqSchemaUtil.getRequirementType(context)+ "," +
							ReqSchemaUtil.getChapterType(context) + "," +
							ReqSchemaUtil.getRequirementSpecificationType(context);
		}else if(ReqSchemaUtil.getCommentType(context).equalsIgnoreCase(strObjectType)){
			strObjTypes = ReqSchemaUtil.getChapterType(context) + "," +
						  ReqSchemaUtil.getRequirementSpecificationType(context);
		}else{
			strObjTypes = null;
		}
		return strObjTypes;
	}
	
	private String getFromRelTypesFromId(Context context, String objectId) throws FrameworkException{
		DomainObject domOject = DomainObject.newInstance(context, objectId);
		domOject.openObject(context);
		String strObjectType = domOject.getType(context);
		String strRelTypes = null;;
		if(ReqSchemaUtil.getRequirementSpecificationType(context).equalsIgnoreCase(strObjectType)){
			strRelTypes = 	"";

		}else if(ReqSchemaUtil.getChapterType(context).equalsIgnoreCase(strObjectType)){
			strRelTypes = ReqSchemaUtil.getSpecStructureRelationship(context);
		}else if(ReqSchemaUtil.getRequirementType(context).equalsIgnoreCase(strObjectType)){
			strRelTypes =   ReqSchemaUtil.getSpecStructureRelationship(context) + "," + 
							ReqSchemaUtil.getSubRequirementRelationship(context)+ "," +
							ReqSchemaUtil.getDerivedRequirementRelationship(context);;
		}else if(ReqSchemaUtil.getCommentType(context).equalsIgnoreCase(strObjectType)){
			strRelTypes = ReqSchemaUtil.getSpecStructureRelationship(context);
		}else{
			strRelTypes = null;
		}
		return strRelTypes;
	}
	
	public JSONObject getChildren(Context context, String[] args)
	{
		JSONObject JSONOStruct = null;
		try {
			Map paramMap = (Map)JPO.unpackArgs(args);
			
			String strObjectId = ((String[])paramMap.get("id"))[0];
			String strDepth = ((String[])paramMap.get("depth"))[0];
			int iDepth = Integer.parseInt(strDepth);
			String strObjTypes = getChildrenObjectTypesFromId(context, strObjectId);
			String strRelTypes = getToRelTypesFromId(context, strObjectId, iDepth);
			
	        StringList strListSelectStmts = new StringList(5);
	        strListSelectStmts.addElement(SELECT_ID);
	        strListSelectStmts.addElement(SELECT_TYPE);
	        strListSelectStmts.addElement(SELECT_NAME);
	        strListSelectStmts.addElement(SELECT_REVISION);
	        
	        StringList strListRelSelect = new StringList(SELECT_RELATIONSHIP_ID);
	        strListRelSelect.addElement("attribute[" + RequirementsUtil.getSequenceOrderAttribute(context) + "]");
	        strListRelSelect.add(DomainRelationship.SELECT_FROM_ID);
	        strListRelSelect.add(DomainRelationship.SELECT_TO_ID);
	        strListRelSelect.addElement(SELECT_FROM_ID);
	        strListRelSelect.addElement(SELECT_RELATIONSHIP_TYPE);  
	        DomainObject domObject = DomainObject.newInstance(context, strObjectId);
	        
	        MapList childObjects = null;
	        if(iDepth != 0){
	        	if(iDepth == -1){
		        	iDepth = 0;
		        }
	        	iDepth=iDepth==-1?0:iDepth;
	        	childObjects = domObject.getRelatedObjects(context, strRelTypes, strObjTypes,
						strListSelectStmts, strListRelSelect, false, true, (short) iDepth, null, null);
	        }
			domObject.openObject(context);
			StringList strListAttibuteList = new StringList();
			strListAttibuteList.add(SELECT_ID);
			strListAttibuteList.add(SELECT_LEVEL);
			strListAttibuteList.add(SELECT_TYPE);
			strListAttibuteList.add(SELECT_NAME);
			strListAttibuteList.add(SELECT_REVISION);
			Map infos = domObject.getInfo(context, strListAttibuteList);
			domObject.close(context);
			JSONOStruct = createJSONStruct(context, infos, childObjects,true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return JSONOStruct;
	}
	
	private JSONObject getTestCaseUseCaseObjects(Context context, String strObjectId) throws Exception{
		MapList childObjects = null;
		JSONObject JSONOStruct = null;
		
		StringList strListSelectStmts = new StringList(5);
        strListSelectStmts.addElement(SELECT_ID);
        strListSelectStmts.addElement(SELECT_TYPE);
        strListSelectStmts.addElement(SELECT_NAME);
        strListSelectStmts.addElement(SELECT_REVISION);
        
        StringList strListRelSelect = new StringList(SELECT_RELATIONSHIP_ID);
        strListRelSelect.addElement("attribute[" + RequirementsUtil.getSequenceOrderAttribute(context) + "]");
        strListRelSelect.add(DomainRelationship.SELECT_FROM_ID);
        strListRelSelect.add(DomainRelationship.SELECT_TO_ID);
        strListRelSelect.addElement(SELECT_FROM_ID);
        strListRelSelect.addElement(SELECT_RELATIONSHIP_TYPE);  
        
        String strRelTypes = ReqSchemaUtil.getRequirementValidationRelationship(context)+ "," +
		  					 ReqSchemaUtil.getSubTestCaseRelationship(context)/* + "," +  
		  					 ReqSchemaUtil.getSubUseCaseRelationship(context) + "," +
        					 ReqSchemaUtil.getRequirementUseCaseRelationship(context)*/;
        					  
        
        String strObjTypes = ReqSchemaUtil.getTestCaseType(context)/* + "," +
        					 ReqSchemaUtil.getUseCaseType(context)*/;
        
        DomainObject  domObject = DomainObject.newInstance(context, strObjectId);
        childObjects = domObject.getRelatedObjects(context, strRelTypes, strObjTypes,
				strListSelectStmts, strListRelSelect, false, true, (short) 0, null, null);
        
        StringList strListAttibuteList = new StringList();
		strListAttibuteList.add(SELECT_ID);
		strListAttibuteList.add(SELECT_LEVEL);
		strListAttibuteList.add(SELECT_TYPE);
		strListAttibuteList.add(SELECT_NAME);
		strListAttibuteList.add(SELECT_REVISION);
		Map infos = domObject.getInfo(context, strListAttibuteList);
		domObject.close(context);
		JSONOStruct = createJSONStruct(context, infos, childObjects,false);
        return JSONOStruct;
	}
	
	private JSONObject getParentsObjects(Context context, String strObjectId) throws Exception{
		JSONObject JSONOParentStruct = null;
		String strObjTypes = getParentObjectTypesFromId(context, strObjectId);
		String strRelTypes = getFromRelTypesFromId(context, strObjectId);
		StringList strListSelectStmts = new StringList(5);
        strListSelectStmts.addElement(SELECT_ID);
        strListSelectStmts.addElement(SELECT_TYPE);
        strListSelectStmts.addElement(SELECT_NAME);
        strListSelectStmts.addElement(SELECT_REVISION);
        
        StringList strListRelSelect = new StringList(SELECT_RELATIONSHIP_ID);
        strListRelSelect.addElement("attribute[" + RequirementsUtil.getSequenceOrderAttribute(context) + "]");
        strListRelSelect.add(DomainRelationship.SELECT_FROM_ID);
        strListRelSelect.add(DomainRelationship.SELECT_TO_ID);
        strListRelSelect.addElement(SELECT_FROM_ID);
        strListRelSelect.addElement(SELECT_RELATIONSHIP_TYPE);  
        DomainObject domObject = DomainObject.newInstance(context, strObjectId);
        
        MapList childObjects = null;
    	childObjects = domObject.getRelatedObjects(context, strRelTypes, strObjTypes,
				strListSelectStmts, strListRelSelect, true, false, (short) 0, null, null);
		domObject.openObject(context);
		StringList strListAttibuteList = new StringList();
		strListAttibuteList.add(SELECT_ID);
		strListAttibuteList.add(SELECT_LEVEL);
		strListAttibuteList.add(SELECT_TYPE);
		strListAttibuteList.add(SELECT_NAME);
		strListAttibuteList.add(SELECT_REVISION);
		Map infos = domObject.getInfo(context, strListAttibuteList);
		domObject.close(context);
		JSONOParentStruct = createJSONStruct(context, infos, childObjects,true);
		return JSONOParentStruct;
	}
	private JSONArray concat2JSONArrays(JSONArray array1,JSONArray array2) throws Exception{
		for(int i = 0 ; i<array2.length();i++){
			JSONObject object = array2.getJSONObject(i);
			array1.put(object);
		}
		return array1;
	}
	
	public JSONObject getParents(Context context, String[] args)
	{
		Map paramMap;
		JSONObject JSONOTestCaseUseCaseStruct = null;
		JSONObject JSONOParentStruct = null;
		JSONObject JSONStruct = null;
		JSONArray Children = null;
		try {
			paramMap = (Map)JPO.unpackArgs(args);
			String strObjectId = ((String[])paramMap.get("id"))[0];
			String strDepth = ((String[])paramMap.get("depth"))[0];
			JSONArray testCaseUseCaseArray = null;
			
			JSONOTestCaseUseCaseStruct = getTestCaseUseCaseObjects(context, strObjectId);
			if(JSONOTestCaseUseCaseStruct.contains("children")){
				testCaseUseCaseArray =  JSONOTestCaseUseCaseStruct.getJSONArray("children");
			}	
			
			JSONOParentStruct = getParentsObjects(context, strObjectId);
			JSONArray parentArray = null;
			if(JSONOParentStruct.contains("children")){
				parentArray = JSONOParentStruct.getJSONArray("children");
			}	
			if(parentArray != null && testCaseUseCaseArray != null){
				Children = concat2JSONArrays(parentArray,testCaseUseCaseArray);
				JSONOParentStruct.put("children", Children);
				JSONStruct = JSONOParentStruct;
			}else if(parentArray!=null){
				JSONStruct = JSONOParentStruct;
			}else if(testCaseUseCaseArray!=null){
				JSONStruct = JSONOTestCaseUseCaseStruct;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return JSONStruct;
	}
}
