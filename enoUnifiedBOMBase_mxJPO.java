import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipWithSelect;
import matrix.db.RelationshipWithSelectItr;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.dassault_systemes.enovia.bom.modeler.constants.BOMMgtConstants;
import com.dassault_systemes.enovia.bom.modeler.interfaces.services.IBOMService;
import com.dassault_systemes.enovia.bom.modeler.interfaces.dvo.IEBOM;
import com.dassault_systemes.enovia.bom.modeler.interfaces.input.IBOMIngress;
import com.dassault_systemes.enovia.bom.modeler.kernel.BOMMgtKernel;
import com.dassault_systemes.enovia.bom.modeler.util.BOMMgtUtil;
import com.dassault_systemes.enovia.partmanagement.modeler.constants.PartMgtConstants;
import com.dassault_systemes.enovia.partmanagement.modeler.exception.PartMgtException;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.dvo.IPart;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.services.IPartService;
import com.dassault_systemes.enovia.partmanagement.modeler.util.ECMUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;

public class enoUnifiedBOMBase_mxJPO {

	private static final String ROW_ID 						= "rowId";
	private static final String MARKUP 						= "markup";
	private static final String PARENT_ID 					= "parentId";
	private static final String CONNECTION_ID 				= "relId";	
	private static final String BOM_OPERATION 				= "bomOperation";
	private static final String REPLACE_CUT_ID 				= "replaceCutId";
	private static final String BOM_OPERATION_CUT 			= "cut";
	private static final String BOM_OPERATION_ADD 			= "add";
	private static final String BOM_OPERATION_NEW 			= "new";
	private static final String CURRENT_EFFECTIVITY 		= "CurrentEffectivity";
	private static final String BOM_OPERATION_REPLACE 	  	= "replace";
	private static final String BOM_OPERATION_REPLACE_CUT 	= "replaceCut";
	private static final String BOM_OPERATION_IS_DRAG_N_DROP 	= "isDragNDrop";
	
	protected static final String OBJECT_ID = "objectId";
	
	public enoUnifiedBOMBase_mxJPO(Context context, String [] args) throws Exception { }
    
    private boolean isModify(Element element) { return ( element.getAttribute(MARKUP) != null ); }
    
    private String getParentId(Map <String, String> dataMap) { return dataMap.get(PARENT_ID); }
    
    @SuppressWarnings("rawtypes")
	private String getObjectId(Map dataMap) { return BOMMgtUtil.getStringValue(dataMap, OBJECT_ID); }
    
    private String getRowId(Map <String, String> dataMap) { return dataMap.get(ROW_ID); }
    
    private String getConnectionId(Map <String, String> dataMap) { return dataMap.get(CONNECTION_ID); }
    
    private String getReplaceCutId(Map <String, String> dataMap) { return dataMap.get(REPLACE_CUT_ID); }
    
    private String getCurrentEffectivity(Map <String, String> dataMap) { return dataMap.get(CURRENT_EFFECTIVITY); }
    
    private boolean isReplaceAdd(Map <String, String> bomOperationMap) { return BOM_OPERATION_REPLACE.equals( bomOperationMap.get(BOM_OPERATION) ); }
    
    private boolean isReplaceCut(Map <String, String> bomOperationMap) { return BOM_OPERATION_REPLACE_CUT.equals( bomOperationMap.get(BOM_OPERATION) ); }
    
    private boolean isAdd(Map <String, String> bomOperationMap) { return ( BOM_OPERATION_ADD.equals( bomOperationMap.get(BOM_OPERATION) ) ) || BOM_OPERATION_NEW.equals( bomOperationMap.get(BOM_OPERATION) ); }
    
    private boolean isCut(Map <String, String> bomOperationMap) { return BOM_OPERATION_CUT.equals( bomOperationMap.get(BOM_OPERATION) ); }
    
    @SuppressWarnings("rawtypes")
	private Element getRootElement(Map requestMap) { return ( (Document) requestMap.get("XMLDoc") ).getRootElement(); }
    
    @SuppressWarnings("unchecked")
	private String getWorkUnderChangeId(Element rootElement) {
		String workUnderChangeId = null;

		List <Element> requestMapList = rootElement.getChildren("requestMap");
		
		List <Element> settingList = requestMapList.get(requestMapList.size() - 1).getChildren("setting");
		
		Element settingElement;
		for (int i = ( BOMMgtUtil.getSize(settingList) - 1 ); i > -1; i--) {
			settingElement = settingList.get(i);

			if ( "workUnderChangeId".equals( settingElement.getAttributeValue("name") ) ) {
				workUnderChangeId = settingElement.getText();
				break;
			}
		}
		
    	return workUnderChangeId;
    }

    @SuppressWarnings("rawtypes")
	private Map getRequestMap(Map programMap) { return ( programMap.get("requestMap") == null ) ? ( new HashMap() ) : ( (HashMap) programMap.get("requestMap") ); }

	private String getValueFromElement(Element element, String key) {
		if (element == null || element.getAttribute(key) == null) { return ""; }
		
		return ( element.getAttribute(key).getValue() == null ) ? "" : element.getAttribute(key).getValue();
	}

	private Map <String, String> getBOMAttributes(Map <String, String> mapChangedColumnMap) {
		Map <String, String> bomAttributeMap = new HashMap <String, String> ();
		
		String value;
		String [] ebomAttributes = BOMMgtUtil.getBOMAttributes();
		for (int i = 0; i < ebomAttributes.length; i++) {
			value = mapChangedColumnMap.get( ebomAttributes[i] );
			if ( value != null ) { bomAttributeMap.put(ebomAttributes[i], value); }
		}
		
		return bomAttributeMap;
	}
	
	@SuppressWarnings("rawtypes")
	private IBOMService getBOMService(Context context, String objectOrConnectionId, String authoringChangeId, Map requestMap, boolean isConnectionId) throws MatrixException {
		IBOMService iBOMService = IBOMService.getService(context, objectOrConnectionId, isConnectionId);
		iBOMService.setAuthoringChange(authoringChangeId);
		if ( isRollupView(requestMap) ) { iBOMService.setAuthoringMode_Rollup(); }
		
		return iBOMService;
	}
	
    @SuppressWarnings("unchecked")
	private Map <String, String> getChangedColumnMap(Context context, Element element) {
    	
		Map <String, String> dbAttributeNames = new HashMap <String, String> ();
    	
    	dbAttributeNames.put(BOMMgtConstants.UOM, BOMMgtConstants.ATTRIBUTE_UNIT_OF_MEASURE);
    	dbAttributeNames.put("VPMVisible", BOMMgtConstants.ATTRIBUTE_ISVPMVISIBLE);
    	dbAttributeNames.put("UOMType", BOMMgtConstants.ATTRIBUTE_UOM_TYPE);
    	
    	Map <String, String> dataMap = new HashMap <String, String> ();
    	
    	List <Element> colList = element.getChildren("column");
    	
    	Iterator <Element> colItr  = colList.iterator();		    	
			 
		Element clm;
		String name;		
		
		while (colItr.hasNext()) {
			clm  = colItr.next();
			
			name = clm.getAttributeValue("name");
			name = ( dbAttributeNames.get(name) == null ) ? name : dbAttributeNames.get(name);
			
			dataMap.put( name, clm.getText() );
		}
		
		return dataMap;
	}
    
    private Map<String, String> getBOMOperationInfoMap(Element parentElement, Element objectElement, Map <String, String> changedColumnMap) {
    	Map<String, String> bomOperationInfoMap = new HashMap<String, String>();
    	
    	String parentId = getValueFromElement(parentElement, PARENT_ID);
    	
    	if (BOMMgtUtil.isNullOrEmpty(parentId)) { parentId = getValueFromElement(parentElement, OBJECT_ID); }
    	
    	String bomOPeration = BOMMgtUtil.isNullOrEmpty( getValueFromElement(objectElement, "param1") ) ? getValueFromElement(objectElement, MARKUP) 
    																								   : getValueFromElement(objectElement, "param1"); 
    	
    	bomOperationInfoMap.put( PARENT_ID, parentId );
    	bomOperationInfoMap.put( BOM_OPERATION, bomOPeration );
    	bomOperationInfoMap.put( CONNECTION_ID, getValueFromElement(objectElement, CONNECTION_ID) );
    	bomOperationInfoMap.put( OBJECT_ID, getValueFromElement(objectElement, OBJECT_ID) );
    	bomOperationInfoMap.put( ROW_ID, getValueFromElement(objectElement, ROW_ID) );
    	bomOperationInfoMap.put( REPLACE_CUT_ID, getValueFromElement(objectElement, "param2") );
    	bomOperationInfoMap.put( MARKUP, getValueFromElement(objectElement, "markup") );
    	bomOperationInfoMap.put( CURRENT_EFFECTIVITY, changedColumnMap.get(CURRENT_EFFECTIVITY) );
    	
    	return bomOperationInfoMap;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void setReplaceCutBOMOperationInfo(Map <String, Map> replaceCutInfoMap, Map bomOperationMap) {
    	Map newMap = new HashMap();
    	
    	newMap.putAll(bomOperationMap);
    	
    	replaceCutInfoMap.put(getConnectionId(bomOperationMap), newMap);
    }

    /**
     * Method to return the parent rowId info
     * @param rowId
     * @return
     */
    private String getParentRowIdInfo(String rowId) {
        StringList emxTableRowIds = FrameworkUtil.split(rowId, ",");
        
        String newRowId = "";
        
        for (int i = 0; i < emxTableRowIds.size() - 1; i++) {
        	
            if (i == 0) { newRowId = (String) emxTableRowIds.get(i); }
            
            else { newRowId = newRowId + "," + (String) emxTableRowIds.get(i); }
        }
        
        return "|||" + newRowId;
    }
    
    private void appendUIChangesForModify(StringBuffer sbUIChangeXML, Map <String, String> bomOperationMap, IBOMService iBOMService) {
    	if ( BOMMgtUtil.isNotNullAndNotEmpty( iBOMService.getReplacedConnectionId() ) ) {
	    	String strRowInfo = getParentRowIdInfo( getRowId(bomOperationMap) );
	    	
	    	sbUIChangeXML.append(" FreezePaneregister(\"").append( strRowInfo ).append("\");").append("rebuildView();")
			
		   .append(" emxEditableTable.addToSelected('<mxRoot><action>add</action><data status=\"commited\">")
		   
		   .append(" <item oid=\"").append( getObjectId(bomOperationMap) ).append("\" relType=\"relationship_EBOM\" relId=\"").append( iBOMService.getReplacedConnectionId() )
		   
		   .append("\" pid=\"").append( getParentId(bomOperationMap) ).append("\" direction=\"\"></item>").append(" </data></mxRoot>');")
		   
		   .append(" FreezePaneunregister(\"").append( strRowInfo ).append("\");");
    	}
    }
    
    private void appendUIChangesForAdd(StringBuffer sbUIChangeXMLModAndAdd, StringBuffer sbUIChangeXML, IBOMService iBOMService, Map <String, String> bomOperationMap, boolean addOperation, boolean boolAuthoringModeQuantity) {    	
    	StringList bomIds = addOperation ? iBOMService.getInstanceIds() : new StringList( iBOMService.getReplacedConnectionId() );
    	
    	String rowInfo = getParentRowIdInfo( getRowId(bomOperationMap) );
    	
    	for (int i = 0, size = BOMMgtUtil.getSize(bomIds); i < size; i++) {
    		if (i == 0) {
    			sbUIChangeXML.append("<item oId=\"").append( getObjectId(bomOperationMap) ).append("\" rowId=\"")
				 		     .append( getRowId(bomOperationMap) ).append("\" pId=\"null\" relId=\"").append( bomIds.get(i) )
				 		     .append("\" markup=\"").append( "add" ).append("\"></item>");
    		}
    		
    		else {
    			sbUIChangeXMLModAndAdd.append(" FreezePaneregister(\"").append(rowInfo).append("\"); rebuildView();")
 			   .append(" emxEditableTable.addToSelected('<mxRoot><action>add</action><data status=\"commited\">")
 			   .append(" <item oid=\"").append( getObjectId(bomOperationMap) ).append("\" relType=\"relationship_EBOM\" relId=\"")
 			   .append( bomIds.get(i) ).append("\" pid=\"").append( getParentId(bomOperationMap) ).append("\" direction=\"\"></item>")
 			   .append(" </data></mxRoot>');").append(" FreezePaneunregister(\"").append(rowInfo).append("\");");
    		}
    		
    		if (boolAuthoringModeQuantity) { break; } // In quantity mode only one row should be displayed. 
    	}
    }
     
    private void appendUIChangesForAddinView(StringBuffer sbUIChangeXMLAdd,StringList childObjList,StringList relIDs, Map <String, String> bomOperationMap) {    	
    	String rowInfo = getParentRowIdInfo( getRowId(bomOperationMap) );
    	String isDragnDrop = bomOperationMap.get(BOM_OPERATION_IS_DRAG_N_DROP);
    	if("true".equalsIgnoreCase(isDragnDrop)){
	    	sbUIChangeXMLAdd.append(" FreezePaneregister(\"").append(rowInfo).append("\"); rebuildView();")
	 			   .append(" emxEditableTable.addToSelected('<mxRoot><action>add</action><data status=\"committed\">")
	 			   .append(" <item oid=\"").append( getObjectId(bomOperationMap) ).append("\" relType=\"EBOM\" relId=\"")
	 			   .append( relIDs.get(0) ).append("\" pid=\"").append( getParentId(bomOperationMap) ).append("\" direction=\"\"></item>")
	 			   .append(" </data></mxRoot>');").append(" FreezePaneunregister(\"").append(rowInfo).append("\"); rebuildView();");
    	}
    	else{
    		sbUIChangeXMLAdd.append(" <mxRoot>");
    		for(int i=0;i<relIDs.size();i++){
    			sbUIChangeXMLAdd.append("<action>add</action><data status=\"committed\">")
			   .append(" <item oid=\"").append( childObjList.get(i) ).append("\" relType=\"EBOM\" relId=\"")
			   .append( relIDs.get(i) ).append("\" pid=\"").append( getParentId(bomOperationMap) ).append("\" direction=\"\"></item>")
			   .append(" </data>");
    		}
    		sbUIChangeXMLAdd.append("</mxRoot>");
    	}
    }
    private void appendUIChangesForCut(StringBuffer sbUIChangeCompleteXML, StringBuffer sbUIChangeXML, Map <String, String> bomOperationMap, IBOMService iBOMService) {
    	// Check if the BOM component is removed using same Work Under Change if so, then remove the complete connection form UI
		if ( BOMMgtUtil.isNullOrEmpty( iBOMService.getConnectionId() ) || iBOMService.getConnectionId().equals( iBOMService.getReplacedConnectionId() ) ) { // same CA used to remove then its complete row remove from UI 
			sbUIChangeXML.append("<item oId=\"").append( getObjectId(bomOperationMap) ).append("\" rowId=\"")
				       	  .append( getRowId(bomOperationMap) ).append("\" pId=\"null\" relId=\"").append( getConnectionId(bomOperationMap) )
				       	  .append("\" markup=\"").append("cut").append("\"></item>");
		}
		
		else { // Just need to remove red striked out line form UI
	    	String strXML= "/mxRoot/rows//r[@status = 'cut'and @id='" + getRowId(bomOperationMap) + "']";
	    	
	    	sbUIChangeCompleteXML.append(" emxUICore.selectSingleNode(oXML.documentElement,\"").append(strXML).append("\").removeAttribute(\"status\");");
    	}
    }
    
    private void appendUIChangesForCutInView(StringBuffer sbUIChangeXMLCut, Map <String, String> bomOperationMap, IBOMService iBOMService) {
    	// Check if the BOM component is removed using same Work Under Change if so, then remove the complete connection form UI
    	sbUIChangeXMLCut.append(" emxEditableTable.removeRowsSelected([\"").append("|||"+getRowId(bomOperationMap)).append("\"]);");
    }
    
   
    private String mergeUIChangeXML(StringBuffer sbUIChangeXML1, StringBuffer sbUIChangeXML2, boolean boolWIPBOM) {
    	StringBuffer sbCompleteUIChangeXML = new StringBuffer("{ main:function() { ");
    	
    	sbCompleteUIChangeXML.append(sbUIChangeXML1);
    	
    	if ( sbUIChangeXML2.toString().isEmpty() ) {
    		if (boolWIPBOM) { sbCompleteUIChangeXML = new StringBuffer(); } //in case of WIP Mode edit just need to refresh the SB.
    		
    		else { sbCompleteUIChangeXML.append("refreshRows();arrUndoRows = new Object();postDataXML.loadXML(\"<mxRoot/>\");"); } //in case of NON WIP Mode need to refresh whole structure to add/cut information
    	}
    	
    	else {
    		String uiChangeXML = sbUIChangeXML2.toString().replaceAll("&", "&amp");
    		sbCompleteUIChangeXML.append("var objDOM = emxUICore.createXMLDOM();")
        				   .append("objDOM.loadXML('<mxRoot><action>success</action><message></message><data status=\"commited\">").append(uiChangeXML)
        				   .append("</data></mxRoot>');").append("emxUICore.checkDOMError(objDOM);updateoXML(objDOM);refreshStructureWithOutSort();")
        				   .append("arrUndoRows = new Object();").append("postDataXML.loadXML(\"<mxRoot/>\");");
    	}
    	
    	if ( !sbCompleteUIChangeXML.toString().isEmpty() ) { sbCompleteUIChangeXML.append("}}"); }
    	
    	return sbCompleteUIChangeXML.toString();
    }
    
    private Map <String, String> getUIActionMap(String uiXML) {
    	Map <String, String> uiChangeMap = new HashMap <String, String> (2);
    	
        uiChangeMap.put( "Action", BOMMgtUtil.isNullOrEmpty(uiXML) ? "refresh" : "execScript" );
        uiChangeMap.put( "Message", uiXML );
        
        return uiChangeMap;
    }
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void updateObjectList(Map programMap, Map <String, String> qtyModifiedInfoMap) {
		if ( !qtyModifiedInfoMap.isEmpty() ) {
			Map tableData = (Map) programMap.get("tableData");
	    	
	    	MapList objectList = (MapList) tableData.get("ObjectList");
	    	Iterator <Map> iterator = objectList.iterator(); 
			Map objectMap;
			String modifiedQty, modifiedFindNumber, modifiedReferenceDesignator;
			while ( iterator.hasNext() ) {
				objectMap = iterator.next();
				
				modifiedQty = qtyModifiedInfoMap.get( BOMMgtUtil.getStringValue(objectMap, BOMMgtConstants.SELECT_RELATIONSHIP_ID) + "|Quantity" );
				modifiedFindNumber = qtyModifiedInfoMap.get( BOMMgtUtil.getStringValue(objectMap, BOMMgtConstants.SELECT_RELATIONSHIP_ID) + "|FindNumber" );
				modifiedReferenceDesignator = qtyModifiedInfoMap.get( BOMMgtUtil.getStringValue(objectMap, BOMMgtConstants.SELECT_RELATIONSHIP_ID) + "|ReferenceDesignator" );

				if ( modifiedQty != null ) {
					objectMap.put( BOMMgtConstants.SELECT_ATTRIBUTE_QUANTITY, modifiedQty );
				}
				
				if ( modifiedFindNumber != null ) {
					objectMap.put( BOMMgtConstants.SELECT_ATTRIBUTE_FIND_NUMBER, modifiedFindNumber );
				}
				
				if ( modifiedReferenceDesignator != null ) {
					objectMap.put( BOMMgtConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR, modifiedReferenceDesignator );
				}
			}
		}
	}
	
    private Map <String, String> getBOMErrorActionMap(Context context, String strExceptionMsg, Map <String, String> bomOperationMap) throws Exception {
    	String rowId = (bomOperationMap == null) ? "" : bomOperationMap.get(ROW_ID);
    	
    	String errorMsg = ( BOMMgtUtil.isNullOrEmpty(rowId) ? "" : "[rowId:" + rowId + "]" ) + strExceptionMsg;
    	
    	Map <String, String> errorActionMap = new HashMap <String, String> (2);
    	errorActionMap.put("Action", "ERROR");
    	errorActionMap.put("Message", errorMsg);
    	
    	return errorActionMap;
    }

    private IBOMIngress getBOMAddIngress(Map <String, String> bomOperationMap, Map <String, String> attributeMap) {
    	IBOMIngress iBOMIngress = IBOMIngress.getService();
    	
    	iBOMIngress.setChildId( getObjectId(bomOperationMap) );
    	iBOMIngress.setEffectivityExpression( bomOperationMap.get(CURRENT_EFFECTIVITY) );
    	iBOMIngress.setBOMAttributeMap( attributeMap );
    	iBOMIngress.setBOMUI("SB"); // to avoid some validations which is already performed as part of Structure Browser UI.

    	return iBOMIngress;
    }

    private IBOMIngress getBOMReplaceIngress(Map <String, String> bomOperationMap, Map <String, String> attributeMap) {
    	IBOMIngress iBOMIngress = IBOMIngress.getService();
    	
    	iBOMIngress.setReplaceByObjectId( getObjectId(bomOperationMap) );
    	iBOMIngress.setEffectivityExpression( getCurrentEffectivity(bomOperationMap) ); // with no effectivity modifications CFF api throwing exception so commented this line.
    	iBOMIngress.setBOMAttributeMap( attributeMap );
    	iBOMIngress.setBOMUI("SB"); // to avoid some validations which is already performed as part of Structure Browser UI.
    	
    	return iBOMIngress;
    }

    private IBOMIngress getBOMModifyIngress(Map <String, String> bomOperationMap, Map <String, String> attributeMap) {
    	IBOMIngress iBOMIngress = IBOMIngress.getService();
    	
    	iBOMIngress.setEffectivityExpression( getCurrentEffectivity(bomOperationMap) );
    	iBOMIngress.setBOMAttributeMap( attributeMap );
    	iBOMIngress.setBOMUI("SB"); // to avoid some validations which is already performed as part of Structure Browser UI.
    	
    	return iBOMIngress;
    }
	 
   private IBOMIngress getBOMCutIngress(Context context, String ebomId) {
    	IBOMIngress iBOMIngress = IBOMIngress.getService();
    	iBOMIngress.setBOMUI("SB"); // to avoid some validations which is already performed as part of Structure Browser UI.
    	return iBOMIngress;
	}
    
	private void updateQtyModifiedMap(Map <String, String> qtyModifiedInfoMap, Map <String, String> bomOperationMap, Map <String, String> changedColumnMap) {
		if ( BOMMgtUtil.isNotNullAndNotEmpty( changedColumnMap.get(BOMMgtConstants.ATTRIBUTE_QUANTITY) ) ) {
			qtyModifiedInfoMap.put( bomOperationMap.get(CONNECTION_ID) + "|Quantity", changedColumnMap.get(BOMMgtConstants.ATTRIBUTE_QUANTITY) );
		}
		
		if ( BOMMgtUtil.isNotNullAndNotEmpty( changedColumnMap.get(BOMMgtConstants.ATTRIBUTE_FIND_NUMBER) ) ) {
			qtyModifiedInfoMap.put( bomOperationMap.get(CONNECTION_ID) + "|FindNumber", changedColumnMap.get(BOMMgtConstants.ATTRIBUTE_FIND_NUMBER) );
		}
		
		if ( changedColumnMap.containsKey(BOMMgtConstants.ATTRIBUTE_REFERENCE_DESIGNATOR) ) {
			qtyModifiedInfoMap.put( bomOperationMap.get(CONNECTION_ID) + "|ReferenceDesignator", changedColumnMap.get(BOMMgtConstants.ATTRIBUTE_REFERENCE_DESIGNATOR) );
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected boolean isRollupView(Map programMap) { return ( "Rollup".equals( BOMMgtUtil.getStringValue(programMap, "BOMViewMode") ) ); }
	
    /** this api is passed as connectionProgram from Part bom powerview for any modifications performed on BOM powerview
     * all updation is done as part of postprocess jpo, so this api should return null as there is nothing to update here.   
     * @param context
     * @param args
     * @return
     * @throws MatrixException
     */
    @SuppressWarnings("rawtypes")
    @com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public Map bomConnection(Context context, String [] args) throws MatrixException {
    	return null;
    }

    /** this api is passed as postprocess jpo from BOM powerview to apply any modifications performed on BOM powerview.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @com.matrixone.apps.framework.ui.PostProcessCallable 
	public Map updateBOM(Context context, String [] args) throws Exception {
    	StringBuffer sbUIChangeXMLModAndCut = new StringBuffer(), sbUIChangeXML = new StringBuffer(); // holds all UI related XML data
    	
    	Map programMap = JPO.unpackArgs(args), requestMap = getRequestMap(programMap); // holds required parameters passed from UI.
    	Map <String, Map> replaceCutInfoMap = new HashMap <String, Map> ();
    	Map <String, String> changedColumnMap, bomOperationMap = null, actionMap, qtyModifiedInfoMap = new HashMap <String, String> ();
    	
    	Element objectElement, elementChild, rootElement = getRootElement(requestMap);
    	
    	String authoringChangeId = getWorkUnderChangeId(rootElement);   // holds the WorkUnder Change Id selected from UI.
        
        List elementList = rootElement.getChildren("object");
        boolean boolAuthoringModeQuantity = isRollupView(requestMap);

        IBOMService iBOMService;
        
        try {
        	Iterator <Element> objectIterator, elementIterator = elementList.iterator();

	        while ( elementIterator.hasNext() ) {
	        	
	        	elementChild = elementIterator.next();
	         	
	         	if ( isModify(elementChild) ) { // true, if user has done only modify operation on the row. 
	         		if(isRollupView( requestMap )){throw BOMMgtUtil.createBOMException(context, "emxEngineeringCentral.validate.ModifyInRollupView", BOMMgtConstants.ENG_RESOURCE_FILE);}
	         		changedColumnMap = getChangedColumnMap(context, elementChild); // holds all columns which are modified in current UI transaction
	            	
	            	bomOperationMap = getBOMOperationInfoMap(elementChild, elementChild, changedColumnMap); // holds all required params containing BOM operations like Add, Cut modify, replace
	            	
	            	iBOMService = getBOMService(context, getConnectionId(bomOperationMap), authoringChangeId, requestMap, true);
	            	
					 //If part is not in VPM Control set the isVPMVisible value according to user selection.
					//if ( BOMMgtUtil.isVPMControlled(context, getParentId(bomOperationMap) )  ) { changedColumnMap.remove( BOMMgtUtil.getActualSchemaName(context, PartMgtConstants.SYM_ATTRIBUTE_VPM_VISIBLE) ); }
					
					// Does the Modify operations, If it is NON wip mode then bom instance will contain newly created BOM else it just returns the same modified bom info. 

	            	IBOMIngress iBOMIngress =getBOMModifyIngress(bomOperationMap, getBOMAttributes(changedColumnMap));
	            	/*Added for Auto Sync st*/
	            	iBOMService.setCollaboratToDesign(true);
	            	/*Added for Auto Sync en*/
	            	iBOMService.modify( context, iBOMIngress);
					
					appendUIChangesForModify(sbUIChangeXMLModAndCut, bomOperationMap, iBOMService); // If bom instance connection id and the bomoperation connection id is same then new object is not created.
					
					updateQtyModifiedMap( qtyModifiedInfoMap, bomOperationMap, getBOMAttributes(changedColumnMap) );
	         	}
	         	
	         	// Add, New and Cut operations
	         	else {
	         		List objectList = elementChild.getChildren("object");
	         		
	         		objectIterator = objectList.iterator();         		
	         		
	         		while ( objectIterator.hasNext() ) {
	         			objectElement = (Element) objectIterator.next();
	
	                	changedColumnMap = getChangedColumnMap(context, objectElement); // holds all columns which are modified in current UI transaction
	
	                	bomOperationMap = getBOMOperationInfoMap(elementChild, objectElement, changedColumnMap); // holds all required params containing BOM operations like Add, Cut modify, replace
	
	                	if ( isAdd(bomOperationMap) ) { // true, if operation is Add
	                		iBOMService = getBOMService(context, getParentId(bomOperationMap), authoringChangeId, requestMap, false);
	                		// If Quantity is more than 1 and Usage is selected as Each only then multi connections performed, else it connects only 1 time.
	                		IBOMIngress iBOMIngress = getBOMAddIngress(bomOperationMap, getBOMAttributes(changedColumnMap) );

	                		/*Added for Auto Sync st*/
	                		iBOMService.setCollaboratToDesign(true);
	                		/*Added for Auto Sync en*/

	                		iBOMService.add( context, iBOMIngress );
	                	
	                		appendUIChangesForAdd( sbUIChangeXMLModAndCut, sbUIChangeXML, iBOMService, bomOperationMap, true, boolAuthoringModeQuantity ); // updates the UI Change XML to reflect in UI.
	                	}
	                	                	
	                	else if ( isCut(bomOperationMap) ) { // true, if operation is Cut.
	                		iBOMService = getBOMService(context, getConnectionId(bomOperationMap), authoringChangeId, requestMap, true);
	                		
	                		IBOMIngress iBOMIngress = getBOMCutIngress(context,iBOMService.getConnectionId());
	                		
	                		/*Added for Auto Sync st*/
	                		iBOMService.setCollaboratToDesign(true);
	                		/*Added for Auto Sync to en*/
	                		
	                		iBOMService.remove(context, iBOMIngress); //If same change is selected for Cut operation which did Add then complete bom will be disconnected, else just a Effectivity Cut performed.
	                		appendUIChangesForCut( sbUIChangeXMLModAndCut, sbUIChangeXML, bomOperationMap, iBOMService ); // updates UI change XML for reflecting it in UI. 
	                	}
	                	
	                	else if ( isReplaceCut(bomOperationMap) ) { // true, if operation is Replace Cut. will not perform any back end operation as it will be taken care in Replace Add BOM operation.
	                		setReplaceCutBOMOperationInfo(replaceCutInfoMap, bomOperationMap); // updates UI change XML for reflecting it in UI.
	                	}
	                	
	                	else if ( isReplaceAdd(bomOperationMap) ) { // true, If operation Replace Add.
	                		iBOMService = getBOMService(context, getReplaceCutId(bomOperationMap), authoringChangeId, requestMap, true);
	                		IBOMIngress iBOMIngress = getBOMReplaceIngress( bomOperationMap, getBOMAttributes(changedColumnMap) );
	                		/*Added for Auto Sync st*/
	                		iBOMService.setCollaboratToDesign(true);
	                   		/*Added for Auto Sync to en*/

	                		iBOMService.replace( context,iBOMIngress ); // Does all required backend process.
	                		appendUIChangesForCut( sbUIChangeXMLModAndCut, sbUIChangeXML, replaceCutInfoMap.get( getReplaceCutId(bomOperationMap) ), iBOMService );
	                		appendUIChangesForAdd( sbUIChangeXMLModAndCut, sbUIChangeXML, iBOMService, bomOperationMap, false, boolAuthoringModeQuantity ); // Appends UI change XML for reflecting it in UI.
	                		updateQtyModifiedMap( qtyModifiedInfoMap, bomOperationMap, getBOMAttributes(changedColumnMap) );
	                	}
	         		}
	         	}
	        }

	        actionMap = getUIActionMap( mergeUIChangeXML(sbUIChangeXMLModAndCut, sbUIChangeXML, false ) );
	        
	        updateObjectList(programMap, qtyModifiedInfoMap);
    	}
        
        catch (PartMgtException handledException) {
        	if ( "emxUnresolvedEBOM.WorkUnderChange.BlankEffectivity".equals( handledException.getPropertyKey() ) ) { bomOperationMap.remove(ROW_ID); }

        	actionMap = getBOMErrorActionMap(context, handledException.getLocalizedMessage(), bomOperationMap);
    	}

        catch (Exception e) {
        	String strExceptionMsg = e.toString();
        	if (strExceptionMsg.indexOf(']') > -1) { strExceptionMsg = strExceptionMsg.substring( (strExceptionMsg.indexOf(']') + 1), strExceptionMsg.length() ); }

        	actionMap = getBOMErrorActionMap(context, e.toString(), bomOperationMap);
        }

    	return actionMap;
    }
    
	/** this api is called from UI components like commands and Menus for displaying only in Rollup view.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
    @com.matrixone.apps.framework.ui.ProgramCallable
	public boolean displayForRollupView(Context context, String [] args) throws Exception {
    	return isRollupView( JPO.unpackArgs(args) );
    }

	/** this api is called from UI components like commands and Menus for displaying only in instance view. 
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
	public boolean displayForInstanceView(Context context, String [] args) throws Exception {
    	return !displayForRollupView(context, args);
    }
    
    /** this api is called from UI component table column for displaying column values, column should be configured with map key from which this column will get the value.
     * Column settings should have setting MapKey with key specified for the Map.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
	@com.matrixone.apps.framework.ui.ProgramCallable
    public StringList getColumnValueList(Context context, String [] args) throws Exception {
    	Map programMap = JPO.unpackArgs(args);
    	
    	MapList objectList = (MapList) programMap.get("objectList");
    	
    	return BOMMgtUtil.getSListForTheKey( objectList, BOMMgtUtil.getColumnMapkey(programMap) );
    }
    
    /** some columns configured with program and function requires update program and function, as the updation will be performed in postprocess JPO this api is returning just true.
     * update BOM Column values
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public boolean updateBOMColumn(Context context, String [] args) throws MatrixException { return true; }
    
    /** gets the Quantity value for BOM Quantity column.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @com.matrixone.apps.framework.ui.ProgramCallable
	public StringList getBOMQuantity(Context context, String [] args) throws Exception {
    	Map programMap = JPO.unpackArgs(args);
    	
    	StringList quantityList = new StringList();
    	
    	MapList objectList = (MapList) programMap.get("objectList");
    	
    	Map objectMap;
    	String quantity = "";
    	Iterator <Map> objectListIterator = objectList.iterator();
    	
    	while ( objectListIterator.hasNext() ) {
    		objectMap = objectListIterator.next();
    		
    		if ( !"true".equalsIgnoreCase( BOMMgtUtil.getStringValue(objectMap, "Root Node") ) ) {
    			quantity = BOMMgtUtil.getStringValue(objectMap,BOMMgtConstants.SELECT_ATTRIBUTE_QUANTITY);
    			
    			if ( BOMMgtUtil.isNotNullAndNotEmpty(quantity) ) {
	    			Double quantityValue = Double.parseDouble(quantity);
	    			quantity = quantityValue.toString();
    			}
    			// The value will never be blank as the value for this column is updated in F/N column  
	    		/*if ( BOMMgtUtil.isNullOrEmpty(quantity) ) { // For newly added row in edit mode this check will be true.
	    			String connectionId = BOMMgtUtil.getStringValue(objectMap, BOMMgtConstants.SELECT_RELATIONSHIP_ID);
	
	    			if ( BOMMgtUtil.isNotNullAndNotEmpty(connectionId) ) {
	    				
	    				if ( boolRoolupView ) {
    						Map rollupMap = BOMMgtUtil.getRollupDataMap(context, connectionId);
    						quantity = BOMMgtUtil.getStringValue(rollupMap, BOMMgtConstants.SELECT_ATTRIBUTE_QUANTITY); 
    					} else {
    						quantity = BOMMgtKernel.getRelAttributeValue(context, connectionId, BOMMgtConstants.ATTRIBUTE_QUANTITY); // get the value from rel Attribute
    					}
		
		    			objectMap.put(BOMMgtConstants.SELECT_ATTRIBUTE_QUANTITY, quantity);
	    			}
	    		}*/
    		}

    		quantityList.add(quantity);
    	}

    	return quantityList;
    }
    
    /** gets the Reference Designator value for BOM RD column.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @com.matrixone.apps.framework.ui.ProgramCallable
    public StringList getBOMReferenceDesignator(Context context, String [] args) throws Exception {
    	Map programMap = JPO.unpackArgs(args);
    	
    	StringList rdList = new StringList();
    	
    	MapList objectList = (MapList) programMap.get("objectList");
    	
    	//boolean boolRoolupView = isRollupView(paramMap);
    	
    	Map objectMap;
    	String referenceDesignator = "";
    	Iterator <Map> objectListIterator = objectList.iterator();
    	
    	while ( objectListIterator.hasNext() ) {
    		objectMap = objectListIterator.next();
    		
    		if ( !"true".equalsIgnoreCase( BOMMgtUtil.getStringValue(objectMap, "Root Node") ) ) {
    			referenceDesignator = BOMMgtUtil.getStringValue(objectMap, BOMMgtConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
    			
    			// This value will be updated in F/N column
    			
    			/*if ( BOMMgtUtil.isNullOrEmpty(referenceDesignator) ) { // For newly added row in edit mode this check will be true.
    				String connectionId = BOMMgtUtil.getStringValue(objectMap, BOMMgtConstants.SELECT_RELATIONSHIP_ID);
    				
    				if ( BOMMgtUtil.isNotNullAndNotEmpty(connectionId) ) {
    					
    					if ( boolRoolupView ) {
    						Map rollupMap = BOMMgtUtil.getRollupDataMap(context, connectionId);
    						referenceDesignator = BOMMgtUtil.getStringValue(rollupMap, BOMMgtConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR); 
    					} else {
    						referenceDesignator = BOMMgtKernel.getRelAttributeValue(context, connectionId, BOMMgtConstants.ATTRIBUTE_REFERENCE_DESIGNATOR); // get the value from rel Attribute
    					}

    					objectMap.put(BOMMgtConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR, referenceDesignator);
    				}
    			}*/
    		}
    		
    		rdList.add(referenceDesignator);
    	}
    	
    	return rdList;
    }
    
    /** gets the Find Number value for BOM Find Number column.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @com.matrixone.apps.framework.ui.ProgramCallable
    public StringList getBOMFindNumber(Context context, String [] args) throws Exception {
    	Map programMap = JPO.unpackArgs(args);
    	
    	StringList findNumberList = new StringList();
    	
    	Map paramMap = (Map) programMap.get("paramList");
    	MapList objectList = (MapList) programMap.get("objectList");
    	
    	boolean boolRoolupView = isRollupView(paramMap);
    	
    	Map objectMap;
    	String findNumber = "";
    	Iterator <Map> objectListIterator = objectList.iterator();
    	
    	while ( objectListIterator.hasNext() ) {
    		objectMap = objectListIterator.next();
    		
    		if ( !"true".equalsIgnoreCase( BOMMgtUtil.getStringValue(objectMap, "Root Node") ) ) {
    			findNumber = BOMMgtUtil.getStringValue(objectMap, BOMMgtConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
    			
    			if ( BOMMgtUtil.isNullOrEmpty(findNumber) ) { // For newly added row in edit mode this check will be true.
    				String connectionId = BOMMgtUtil.getStringValue(objectMap, BOMMgtConstants.SELECT_RELATIONSHIP_ID);
    				
    				if ( BOMMgtUtil.isNotNullAndNotEmpty(connectionId) ) {
    					Map <String, String> relInfoMap = IBOMService.getInfo(context, connectionId).getAttributeMap();

						IEBOM iEBOM = IEBOM.getService(relInfoMap);
    					
    					if ( boolRoolupView && iEBOM.isUnitOfMeasure_Each() ) {
    						/* From Quantity Rollup view, if user edits the BOM then Auto generated F/N should be calculated based on the number of Qty added, this also updates the Qty and ED column values- Start */
    						Map rollupMap = BOMMgtUtil.getRollupDataMap(context, connectionId);
    						String rollupFN = iEBOM.getFindNumber();
    						Double rollupQty = Double.parseDouble( iEBOM.getQuantity() );
    						String rollupRD = iEBOM.getReferenceDesignator();
    						
    						StringList instanceIdList = (StringList) rollupMap.get(BOMMgtConstants.ROLLUP_CONNECTION_IDS);
    						
    						RelationshipWithSelectItr relationshipWithSelectItr = BOMMgtKernel.relWithSelectListItr(context, BOMMgtUtil.getValuesInArray(instanceIdList), BOMMgtUtil.createStringList(new String[] {BOMMgtConstants.SELECT_ATTRIBUTE_FIND_NUMBER, BOMMgtConstants.SELECT_ATTRIBUTE_QUANTITY, BOMMgtConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR}));
    						RelationshipWithSelect relationshipWithSelect;
    						
    						while ( relationshipWithSelectItr.next() ) {
    							relationshipWithSelect = relationshipWithSelectItr.value();
    							String fn = relationshipWithSelect.getSelectData(BOMMgtConstants.SELECT_ATTRIBUTE_FIND_NUMBER);
    							
    							if ( !fn.equals( iEBOM.getFindNumber() ) && fn.startsWith( iEBOM.getFindNumber() ) ) {
    								rollupFN += "," + fn;
    								
    								rollupQty += Double.parseDouble( relationshipWithSelect.getSelectData(BOMMgtConstants.SELECT_ATTRIBUTE_QUANTITY) );
    								
    								if ( BOMMgtUtil.isNotNullAndNotEmpty(rollupRD) ) {
    									rollupRD += "," + relationshipWithSelect.getSelectData(BOMMgtConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR);
    								}
    							}
    						}
    						
    						findNumber = rollupFN;
    						objectMap.put( BOMMgtConstants.SELECT_ATTRIBUTE_QUANTITY, Double.toString(rollupQty) );
    						objectMap.put( BOMMgtConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR, rollupRD );
    						/* From Quantity Rollup view, if user edits the BOM then Auto generated F/N should be calculated based on the number of Qty added, this also updates the Qty and ED column values- END */
						} else {
							findNumber = iEBOM.getFindNumber();
    						
    						objectMap.put( BOMMgtConstants.SELECT_ATTRIBUTE_QUANTITY, iEBOM.getQuantity() );
    						objectMap.put( BOMMgtConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR, iEBOM.getReferenceDesignator() );
						}
    					
    					objectMap.put(BOMMgtConstants.SELECT_ATTRIBUTE_FIND_NUMBER, findNumber);
    				}
    			}
    		}
    		
    		findNumberList.add(findNumber);
    	}
    	
    	return findNumberList;
    }

	/** this api is invoked from ui components (commands, menus) for displaying ui components for EC/Configured parts.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
    @com.matrixone.apps.framework.ui.ProgramCallable
	public boolean displayForUnifiedBOM(Context context, String [] args) throws Exception {
		String sPartID = EngineeringUtil.getTopLevelPartForProduct(context, getObjectId( JPO.unpackArgs(args)));
		if(!new DomainObject(sPartID).isKindOf(context, PartMgtConstants.TYPE_PART)){
			return false;
		}
    	IPart iPart = IPartService.getInfo(context,sPartID);
    	
    	return !( iPart.isPolicyClassification_Equivalent() || iPart.isManufacturingPart() ); 
    }
    
	/** this api is used to get all the Change Action ids connected to the Part.
	 * This api is passed as include OID prog for getting list of Change connected for selecting Authoring Change.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList getProposedChangeActionIds(Context context, String [] args) throws Exception {
		MapList caList = ECMUtil.getProposedChangeActions( context, getObjectId( JPO.unpackArgs(args) ), new String [] { BOMMgtConstants.SELECT_ID } ); 
    	return BOMMgtUtil.getSListForTheKeyWithoutEmpty(caList, BOMMgtConstants.SELECT_ID);
    }
    
	/** api called from UI components for displaying the component only if context part is Configured.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	@com.matrixone.apps.framework.ui.ProgramCallable
	public boolean displayForConfiguredContextPart(Context context, String [] args) throws Exception {
		Map programMap = JPO.unpackArgs(args);
    	
    	String policy = BOMMgtUtil.getStringValue(programMap, "ContextPolicy");
    	
    	if ( BOMMgtUtil.isNullOrEmpty(policy) ) {
    		String objectId = BOMMgtUtil.getStringValue(programMap, "objectId");
    		IPart iPart = IPartService.getInfo(context, objectId);
    		policy = iPart.getPolicy();
    	}
    	
    	return ( BOMMgtConstants.POLICY_CONFIGURED_PART.equals(policy) );
    }
    
    /*
     * Should be called from any command range value function with command setting RANGE_VALUE_PROPERTY_KEY which contains the value of range values to be displayed
     * */
    @SuppressWarnings("rawtypes")
    @com.matrixone.apps.framework.ui.ProgramCallable
	public Map <String, StringList> getRangeValues(Context context, String [] args) throws Exception {
    	Map programMap = JPO.unpackArgs(args);
    	
    	Map settingsMap = (Map) ( (Map) programMap.get("columnMap") ).get("settings");
    	
    	return BOMMgtUtil.getActualAndDisplayValues( context, 
    															BOMMgtUtil.getStringValue(settingsMap, "RANGE_VALUE_PROPERTY_KEY"), 
    															BOMMgtUtil.getStringValue(settingsMap, "Registered Suite") );
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @com.matrixone.apps.framework.ui.ProgramCallable
	public StringList applyChangeControlledColor(Context context, String [] args) throws Exception {
    	StringList colorList = new StringList();
    	
    	Map programMap = JPO.unpackArgs(args);
    	
    	MapList objectList = (MapList) programMap.get("objectList");
    	
    	Map objectMap;
    	String changeControlled;
    	Iterator <Map> objectListIterator = objectList.iterator();
    	
    	while ( objectListIterator.hasNext() ) {
    		objectMap = objectListIterator.next();
    		
    		changeControlled = BOMMgtUtil.getStringValue(objectMap, "attribute["+ BOMMgtConstants.ATTRIBUTE_CHANGE_CONTROLLED + "]");
    		
    		colorList.add( "True".equalsIgnoreCase( (changeControlled) ) ? "RemovedRow" : "" );
    	}
    	
    	return colorList; 
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @com.matrixone.apps.framework.ui.ProgramCallable
    public StringList applyRevisionColor(Context context, String [] args) throws Exception {
    	StringList colorList = new StringList();
    	
    	Map programMap = JPO.unpackArgs(args);
    	
    	MapList objectList = (MapList) programMap.get("objectList");
    	
    	Map objectMap;
    	String nextRevision;
    	Iterator <Map> objectListIterator = objectList.iterator();
    	
    	while ( objectListIterator.hasNext() ) {
    		objectMap = objectListIterator.next();
    		
    		nextRevision = BOMMgtUtil.getStringValue(objectMap, "next.revision");
    		
    		colorList.add( BOMMgtUtil.isNotNullAndNotEmpty(nextRevision) ? "RemovedRowYellow" : "" );
    	}
    	
    	return colorList; 
    }
    
@SuppressWarnings("rawtypes")
public synchronized JSONObject dragPartProcess(Context context, String[] args) throws Exception{
		
	    Map param = (Map)JPO.unpackArgs(args);
	  	    
	    JSONObject ret = new JSONObject();
	    try{
	    
			    JSONObject jDrop = (JSONObject)param.get("drop");
			    JSONObject jDropObject = jDrop.getJSONObject("object");
			    JSONObject jDrag = (JSONObject)param.get("drag");
			    JSONArray jDragObjects = jDrag.getJSONArray("objects");
			    Object jAction = jDrag.get("action");
			    
			    
			    String dropAction = jAction.toString();
			    String parentId = jDropObject.getString("oid");
			    String dropRowID = jDropObject.getString("id")+",";
			    String dropSuccessful = BOMMgtUtil.getDisplayPropertyValue(context, "emxFramework.DropProcess.DropOperationSuccessful", "emxFrameworkStringResource");
			    StringBuffer sbUIChangeXMLAdd = new StringBuffer();StringBuffer sbUIChangeXMLCut = new StringBuffer();

			    
			    IBOMService iBOMServiceForAdd;
			    IBOMService iBOMServiceForCut;
			    
			    Map<String, String> bomAddInfoMap = new HashMap<String, String>();
			    Map<String, String> bomCutInfoMap = new HashMap<String, String>();

			    bomAddInfoMap.put( PARENT_ID, parentId );
			    bomAddInfoMap.put( BOM_OPERATION, BOM_OPERATION_ADD );
			    bomAddInfoMap.put( ROW_ID, dropRowID );
			    Map <String, String> changedColumnMap = new HashMap <String, String> ();
			    
			    ContextUtil.startTransaction(context, true);
			    
			    for (int i=0; i<jDragObjects.length(); i++) {
			    	iBOMServiceForAdd = IBOMService.getService(context, parentId, false);
			    	
			    	JSONObject jDragObject = jDragObjects.getJSONObject(i);
			    	String childObjectRelId = jDragObject.getString("rid");
			    	String cutObjectRowId = jDragObject.getString("id");
			    	IPart iPart = IPartService.getInfo(context, jDragObject.getString("oid"));
			    	String chObjectId = iPart.getObjectId();
			    	
			    	bomAddInfoMap.put( OBJECT_ID, chObjectId );
			    	bomAddInfoMap.put(BOM_OPERATION_IS_DRAG_N_DROP, "true");
			    	Map <String, String> bomAttributes = new HashMap<String, String>();
			    	if(BOMMgtUtil.isNotNullAndNotEmpty(childObjectRelId)){
			    		bomAttributes = BOMMgtUtil.getEBOMAttribute(context, childObjectRelId);
						bomAttributes.remove(BOMMgtConstants.ATTRIBUTE_FIND_NUMBER);
						bomAttributes.remove(BOMMgtConstants.ATTRIBUTE_REFERENCE_DESIGNATOR);
			    	}
					changedColumnMap.putAll(bomAttributes);
					
			    	if(dropAction.equalsIgnoreCase("Move")){
						
			    		bomCutInfoMap.put(CONNECTION_ID, childObjectRelId);
			    		bomCutInfoMap.put(BOM_OPERATION, BOM_OPERATION_CUT );
			    		bomCutInfoMap.put(ROW_ID,cutObjectRowId);
			    		iBOMServiceForCut= IBOMService.getService(context, childObjectRelId, true);
			    		
			    		/* For auto collaboration .st */
			    		iBOMServiceForAdd.setCollaboratToDesign(true);
			    		iBOMServiceForCut.setCollaboratToDesign(true);
			    		/* For auto collaboration .en */
			    		
			    		iBOMServiceForAdd.add( context, getBOMAddIngress(bomAddInfoMap, getBOMAttributes(changedColumnMap) ) );
			    		appendUIChangesForAddinView( sbUIChangeXMLAdd,new StringList(chObjectId),new StringList((String)iBOMServiceForAdd.getInstanceIds().get(0)), bomAddInfoMap );
			    		
			    		iBOMServiceForCut.remove( context, getBOMAddIngress(bomCutInfoMap, changedColumnMap) );
			    		appendUIChangesForCutInView(sbUIChangeXMLCut,  bomCutInfoMap, iBOMServiceForCut);
			    		
			    		ret.put("result", "pass");
	    				ret.put("messaage", dropSuccessful);
	    				ret.put("relIds", iBOMServiceForAdd.getInstanceIds().get(0).toString());
	    				ret.put("onDrop", "function () {"+sbUIChangeXMLCut.toString()+sbUIChangeXMLAdd.toString()+"}");
					}
			    	else{
			    		/* For auto collaboration .st */
			    		iBOMServiceForAdd.setCollaboratToDesign(true);
			    		/* For auto collaboration .en */
			    		iBOMServiceForAdd.add( context, getBOMAddIngress(bomAddInfoMap, getBOMAttributes(changedColumnMap) ) );
				    	appendUIChangesForAddinView( sbUIChangeXMLAdd,new StringList(chObjectId),new StringList((String)iBOMServiceForAdd.getInstanceIds().get(0)), bomAddInfoMap );
				    	ret.put("result", "pass");
					    ret.put("onDrop", "function () {"+sbUIChangeXMLAdd.toString()+"}");
				    	
			    	}
			    }
			   
			    ContextUtil.commitTransaction(context);
			    
			}
			 catch (Exception e)
	        {
			   ContextUtil.abortTransaction(context);
			   String message = e.toString();
			   message=message.replaceFirst(":*", "");
			   ret.put("result", "fail");
			   ret.put("message", message);
			   return ret;
	        }
	    
	    return ret;
	    
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public StringBuffer  updateBOMInView(Context context, String [] args) throws Exception {
		Map bomOperationMap = JPO.unpackArgs(args);
		StringBuffer sbUIChangeXML = new StringBuffer();
		
		try{
			ContextUtil.startTransaction(context, true);
			StringList relIds = new StringList(); 
			
			if ( isAdd(bomOperationMap) ) {
				StringBuffer sbUIChangeXMLAdd = new StringBuffer();
				String parentObjId = (String) bomOperationMap.get("ParentObjId");
				StringList childObjList = (StringList) bomOperationMap.get("ChildObjectIds");
				Map changedColumnMap = (Map) bomOperationMap.get("ColumnValue");
				
				IBOMService iBOMServiceForAdd;
				Map<String, String> bomAddInfoMap = new HashMap<String, String>();
				bomAddInfoMap.put( PARENT_ID, parentObjId );
				for (int i=0; i<childObjList.size(); i++) {
					bomAddInfoMap.put( OBJECT_ID, childObjList.get(i).toString() );
					iBOMServiceForAdd = IBOMService.getService(context, parentObjId, false);
					
					IBOMIngress iBOMIngressForAdd = getBOMAddIngress(bomAddInfoMap, getBOMAttributes(changedColumnMap) );
					/* Added for EBOM Sync st*/ 
					iBOMServiceForAdd.setCollaboratToDesign(true);
					/* Added for EBOM Sync en*/ 
					iBOMServiceForAdd.add( context, iBOMIngressForAdd);
					    									
					relIds.add(iBOMServiceForAdd.getInstanceIds().get(0).toString());
				}
				
				appendUIChangesForAddinView( sbUIChangeXMLAdd,childObjList,relIds, bomAddInfoMap );
				sbUIChangeXML = sbUIChangeXMLAdd;
			}
			if ( isCut(bomOperationMap) ) {
				StringList sConnectionID = (StringList) bomOperationMap.get("relIds");
				IBOMService iBOMServiceForCut;
				for (int i=0; i<sConnectionID.size(); i++) {
					iBOMServiceForCut = IBOMService.getService(context, (String) sConnectionID.get(i), true);
					if ( isRollupView(bomOperationMap) ) { iBOMServiceForCut.setAuthoringMode_Rollup(); }
					IBOMIngress iBOMIngress = getBOMCutIngress(context,iBOMServiceForCut.getConnectionId());
					
					/* Added for EBOM Sync st*/ 
					iBOMServiceForCut.setCollaboratToDesign(true);
					/* Added for EBOM Sync en*/ 
					
	        		iBOMServiceForCut.remove(context, iBOMIngress);
				}
				
			}
			if ( isReplaceAdd(bomOperationMap) ) {
				
				StringBuffer sbUIChangeXMLAdd = new StringBuffer();
				String parentObjId = (String) bomOperationMap.get("parentObjId");
				StringList childObjList = (StringList) bomOperationMap.get("NewChildObjectIds");
				
				Map<String, String> bomReplaceInfoMap = new HashMap<String, String>();
				String replaceID = (String) bomOperationMap.get("replaceCutId");
				Map<String, String> bomAddInfoMap = new HashMap<String, String>();
				bomAddInfoMap.put( PARENT_ID, parentObjId );
				
				IBOMService iBOMServiceForAdd;
				IBOMService iBOMServiceForReplace;
	    		
				
				Map changedColumnMap = (Map) bomOperationMap.get("columnValue");
				bomReplaceInfoMap.put( PARENT_ID, parentObjId );

				Map<String, String> bomCutInfoMap = new HashMap<String, String>();
				bomCutInfoMap.put(CONNECTION_ID, replaceID);
	    		bomCutInfoMap.put(BOM_OPERATION, BOM_OPERATION_ADD );
	    		bomCutInfoMap.put( OBJECT_ID, childObjList.get(0).toString() );
	    		
	    		iBOMServiceForReplace = IBOMService.getService(context, replaceID, true);
	    		if ( isRollupView(bomOperationMap) ) { iBOMServiceForReplace.setAuthoringMode_Rollup(); }

	    		/* Added for EBOM Sync st*/ 
				iBOMServiceForReplace.setCollaboratToDesign(true);
				/* Added for EBOM Sync en*/ 
	    		
	    		changedColumnMap.put(BOMMgtConstants.ATTRIBUTE_UNIT_OF_MEASURE, IPartService.getInfo(context, childObjList.get(0).toString()).getUnitOfMeasure());
	    		
				if(iBOMServiceForReplace.isAuthoringMode_Rollup()){
	    			changedColumnMap.put(BOMMgtConstants.ATTRIBUTE_QUANTITY, BOMMgtUtil.getRollupQuantity(context, replaceID));
	    		}
	    		
	    		iBOMServiceForReplace.replace(context, getBOMReplaceIngress(bomCutInfoMap, getBOMAttributes(changedColumnMap)));
        		relIds.add(replaceID);
	    		
				for (int i=1; i<childObjList.size(); i++) {
			    	bomAddInfoMap.put( OBJECT_ID, childObjList.get(i).toString() );
			    	changedColumnMap.put(BOMMgtConstants.ATTRIBUTE_UNIT_OF_MEASURE, IPartService.getInfo(context, childObjList.get(i).toString()).getUnitOfMeasure());
		    		iBOMServiceForAdd = IBOMService.getService(context, parentObjId, false);
		    		iBOMServiceForAdd.add( context, getBOMAddIngress(bomAddInfoMap, getBOMAttributes(changedColumnMap) ) );
		    		relIds.add(iBOMServiceForAdd.getInstanceIds().get(0).toString());
		    	}

				
				appendUIChangesForAddinView( sbUIChangeXMLAdd,childObjList,relIds, bomReplaceInfoMap );
				sbUIChangeXML = sbUIChangeXMLAdd;
			}
			ContextUtil.commitTransaction(context);
		} catch (Exception e)
        {
			String strExceptionMsg = e.toString();
			ContextUtil.abortTransaction(context);
			throw new FrameworkException(strExceptionMsg);
        }
		return sbUIChangeXML;
	}
	
	/** this api is called from UI components like commands and Menus for disabling the Edit command in instance view.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
    @com.matrixone.apps.framework.ui.ProgramCallable
	public boolean disableAuthoringMenus(Context context, String [] args) throws Exception {
    	return !isRollupView( JPO.unpackArgs(args) );
    }
    
    /** this api is called from UI components like commands and Menus for disabling the Edit command in instance view.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
    @com.matrixone.apps.framework.ui.ProgramCallable
	public StringList disableModifyingColumnsInRollUp(Context context, String [] args) throws Exception {
    	StringList slReturnList = new StringList();
    	Map programMap = JPO.unpackArgs(args);
    	MapList mlObjectList = (MapList)programMap.get("objectList");
    	for(int i =0; i<mlObjectList.size();i++){
    		String sEditable = "true";
    		if(isRollupView(getRequestMap(programMap))){
    			if(BOMMgtUtil.isNotNullAndNotEmpty(((String)((Map)mlObjectList.get(i)).get(EngineeringConstants.SELECT_RELATIONSHIP_ID)))){sEditable = "false";}
    		}
    		slReturnList.add(sEditable);
    	}
    	return slReturnList;
    }
}
