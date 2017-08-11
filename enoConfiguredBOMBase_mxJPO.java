import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.dassault_systemes.enovia.bom.modeler.constants.BOMMgtConstants;
import com.dassault_systemes.enovia.bom.modeler.interfaces.services.IBOMService;
import com.dassault_systemes.enovia.bom.modeler.interfaces.input.IBOMFilterIngress;
import com.dassault_systemes.enovia.bom.modeler.util.BOMMgtUtil;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.dvo.IPart;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.services.IPartService;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.effectivity.EffectivityFramework;
import com.matrixone.apps.unresolvedebom.CFFUtil;

public class enoConfiguredBOMBase_mxJPO extends enoUnifiedBOMBase_mxJPO {

	public enoConfiguredBOMBase_mxJPO(Context context, String [] args) throws Exception { super(context, args); }
    
    /** Used to get the selected property key entry by passing the actual value to it.
     * @param context ematrix context
     * @param actualValue selected actual value choosen from UI. 
     * @return property key
     * @throws MatrixException if any operation fails.
     */
    private String getSelectedPropertyKey(Context context, String actualValue) throws MatrixException {
    	if ( BOMMgtUtil.getActualPropertyValue(context, "emxUnresolvedEBOM.BOM.Configuration.Projected", BOMMgtConstants.CMM_RESOURCE_FILE).equals(actualValue) ) { return "emxUnresolvedEBOM.BOM.Configuration.Projected"; }
    	
    	else if ( BOMMgtUtil.getActualPropertyValue(context, "emxUnresolvedEBOM.BOM.Configuration.Change", BOMMgtConstants.CMM_RESOURCE_FILE).equals(actualValue) ) { return "emxUnresolvedEBOM.BOM.Configuration.Change"; }
    	
    	else { return "emxUnresolvedEBOM.BOM.Configuration.Official"; }
    }

    /** returns selected configuration view, For initial load of config BOM view user preference will be considered.  
     * @param context
     * @param requestMap
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
	private String getSelectedConfigurationView(Context context, Map requestMap) throws Exception {
    	String configurationView = BOMMgtUtil.getStringValue(requestMap, "PUEUEBOMChangeViewFilter");
    	
    	if ( BOMMgtUtil.isNullOrEmpty(configurationView) ) { configurationView = BOMMgtUtil.getUserPreferedValue(context, "preference_ChangeView"); }
    	
    	return BOMMgtUtil.isNullOrEmpty(configurationView) 
				    			? BOMMgtUtil.getActualPropertyValue(context, "emxUnresolvedEBOM.BOM.Configuration.Projected", BOMMgtConstants.CMM_RESOURCE_FILE)
				    			: configurationView;
    }
    
    /** used to set all the user selected Criteria for filtering the the COnfigured BOM. 
     * @param context
     * @param dataMap
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
	private IBOMFilterIngress getFilterIngress(Context context, Map dataMap) throws Exception {
    	IBOMFilterIngress iBOMFilterIngress = IBOMFilterIngress.getService();
    	 
    	iBOMFilterIngress.setExpandLevel( BOMMgtUtil.getStringValue(dataMap, "emxExpandFilter") );
    	
    	if (  BOMMgtUtil.isNotNullAndNotEmpty(BOMMgtUtil.getStringValue(dataMap, "CFFExpressionFilterInput_actualValue") ) ) 
    			{ iBOMFilterIngress.setEffectivityExpression( BOMMgtUtil.getStringValue(dataMap, "CFFExpressionFilterInput_actualValue") ); }
    	
    	if (  BOMMgtUtil.isNotNullAndNotEmpty(BOMMgtUtil.getStringValue(dataMap, "PUEUEBOMProductConfigurationFilter_actualValue") ) ) 
    			{ iBOMFilterIngress.setPredefinedConfigurations( FrameworkUtil.split( BOMMgtUtil.getStringValue(dataMap, "PUEUEBOMProductConfigurationFilter_actualValue"), "," ) ); }
    	
    	if (  BOMMgtUtil.isNotNullAndNotEmpty(BOMMgtUtil.getStringValue(dataMap, "PUEUEBOMECOFilter_actualValue") ) ) 
    			{ iBOMFilterIngress.setFilterByChangeIdList( FrameworkUtil.split( BOMMgtUtil.getStringValue(dataMap, "PUEUEBOMECOFilter_actualValue"), "," ) ); }
    	    	
		String selectProdctId = "from[" + BOMMgtConstants.RELATIONSHIP_PART_SPECIFICATION + "].to.id";
        String selectPartVName = "attribute[" + BOMMgtConstants.ATTRIBUTE_V_NAME + "]";
        String selectProdctIdSel = "from[" + BOMMgtConstants.RELATIONSHIP_PART_SPECIFICATION + "].to[" + BOMMgtConstants.TYPE_PLM_ENTITY + "].id";
        String selectProposedExpr = "attribute[" + EffectivityFramework.ATTRIBUTE_EFFECTIVITY_PROPOSED_EXPRESSION + "]";
		String selectPrdPhysicalId = "from[" + BOMMgtConstants.RELATIONSHIP_PART_SPECIFICATION + "].to[" + BOMMgtConstants.TYPE_VPLM_VPMREFERENCE + "].physicalid";
        String sVPLMInstanceRelId ="frommid["+BOMMgtConstants.RELATIONSHIP_VPM_PROJECTION+"].torel.physicalid";
        
        iBOMFilterIngress.setObjectSelect( new String [] { BOMMgtConstants.SELECT_ID, "physicalid", selectProdctId, selectPartVName, selectProdctIdSel,selectPrdPhysicalId } );
    	
        iBOMFilterIngress.setRelationshipSelect( new String [] { BOMMgtConstants.SELECT_RELATIONSHIP_ID,
		    															 BOMMgtConstants.SELECT_ATTRIBUTE_QUANTITY, sVPLMInstanceRelId,
		    															 BOMMgtConstants.SELECT_ATTRIBUTE_REFERENCE_DESIGNATOR, // for rollup view need to select RD
		    															 selectProposedExpr,
		        														 "from.attribute[" + BOMMgtConstants.ATTRIBUTE_CHANGE_CONTROLLED + "]",
		        														 "from.policy" } );
    	
		String selectedConfigView = getSelectedConfigurationView(context, dataMap);
		iBOMFilterIngress.setBOMView( BOMMgtUtil.isNullOrEmpty(selectedConfigView) ? BOMMgtConstants.OFFICIAL_BOM_VIEW : selectedConfigView );
    	
    	return iBOMFilterIngress;
    }

    /** returns all Configuration view options defined on emxUnresolved Property file 
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
	public HashMap <String, StringList> getConfigurationViewOptions(Context context, String [] args) throws Exception {
    	Map paraMap = JPO.unpackArgs(args);
        Map requestMap = (HashMap) paraMap.get("requestMap");
        
    	String propertyKey = "emxUnresolvedEBOM.BOMPowerView.ConfigurationViewOptions";
    	
    	return BOMMgtUtil.getActualAndDisplayValues( context, 
    															propertyKey, 
    															getSelectedPropertyKey( context, getSelectedConfigurationView( context, requestMap ) ), 
    															BOMMgtConstants.CMM_RESOURCE_FILE );
    }
    
    /** this api is called from BOM powerview Current Effectivity column for Enabling/Disabling editable cells.
     * if parent part is not Configured OR if proposed Effectivity is defined then the Column will not be editable. 
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @com.matrixone.apps.framework.ui.ProgramCallable
    public StringList getColumnEditAccessList(Context context, String [] args) throws Exception {
    	StringList editAccessList = new StringList();

    	Map programMap = (Map) JPO.unpackArgs(args);

    	Map requestMap = (Map) programMap.get("requestMap");

    	MapList objectList = (MapList) programMap.get("objectList");

    	Iterator objectListIterator = objectList.iterator();

    	Map dataMap;
    	String parentPolicy, newRowParentObjectId, newConnectionId, connectionId;
    	boolean proposedEffectivityExprExists, changeControlled,isEditAllowed=true;
    	while ( objectListIterator.hasNext() ) {
    		dataMap = (Map) objectListIterator.next();

    		// gets the proposed expression defined on the Connection.
    		proposedEffectivityExprExists = BOMMgtUtil.isNotNullAndNotEmpty( BOMMgtUtil.getStringValue( dataMap, "attribute[" + EffectivityFramework.ATTRIBUTE_EFFECTIVITY_PROPOSED_EXPRESSION + "]" ) );
    		changeControlled = "True".equalsIgnoreCase( BOMMgtUtil.getStringValue(dataMap, "from.attribute[" + BOMMgtConstants.ATTRIBUTE_CHANGE_CONTROLLED + "]") ); // gets parent Change Controlled.
    		parentPolicy = BOMMgtUtil.getStringValue(dataMap, "from.policy"); // gets parent policy.
    		connectionId = BOMMgtUtil.getStringValue(dataMap, "id[connection]"); 
    		if(isRollupView(requestMap)){
    			isEditAllowed = (BOMMgtUtil.isNullOrEmpty(connectionId));
    		}
    		if ( BOMMgtUtil.isNullOrEmpty(parentPolicy) ) { // For new object added which is in green the column edit access should be provided.
    			newRowParentObjectId = BOMMgtUtil.getStringValue(dataMap, "id[parent]");
    			newConnectionId = BOMMgtUtil.getStringValue(dataMap, "id[connection]");

    			if ( BOMMgtUtil.isNotNullAndNotEmpty(newConnectionId) ) {
    				proposedEffectivityExprExists = CFFUtil.isProposedExpressionExists(context, newConnectionId);
    			}

    			if ( BOMMgtUtil.isNotNullAndNotEmpty(newRowParentObjectId) ) {
    				IPart iPart = IPartService.getInfo(context, newRowParentObjectId);
    				parentPolicy = iPart.getPolicy();
    				changeControlled = iPart.isChangeControlled();
    			}
    		}
    		
    		editAccessList.add( String.valueOf( BOMMgtConstants.POLICY_CONFIGURED_PART.equals(parentPolicy) && !proposedEffectivityExprExists && !changeControlled && (isEditAllowed) ));
    	}
    	
    	return editAccessList;
    }
    
    /** used to get the BOM List to display in the BOM powerview. Invoked as Expand prog when COnfigured Context Part is loaded.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes" })
    @com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getBOM(Context context, String [] args) throws Exception {
    	Map programMap = JPO.unpackArgs(args);
    	
    	String objectId = BOMMgtUtil.getStringValue(programMap, OBJECT_ID);
    	
    	IBOMService iBOMService = IBOMService.getService(context, objectId, false);
    	if ( isRollupView(programMap) ) { iBOMService.setAuthoringMode_Rollup(); } // set the view as Rollup to get the Rolled up data based on configurations.
    	
    	MapList bomList = iBOMService.getBOM( context, getFilterIngress(context, programMap) ); // get the Complete list of BOM
    	
    	return bomList;
    }

}

