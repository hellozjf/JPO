
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Config_GetStructConfigAndModelsContent_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMCORE_div_PLMCoreRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__VPMCfgContext_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMCfgContext");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__VPMCfgContext_AddCont = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMCfgContext_AddContextFromReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__VPMCfgContext_div_VPM = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMCfgContext/VPMCfgContext");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__VPMCfgContext_AddMode = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMCfgContext_AddModelsFromContext");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Class_div_Model_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Model");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__Product_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Product");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__Product_AddCriteriaFr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Product_AddCriteriaFromModel");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetContext = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetModel = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetModelContent = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetInputRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetContext = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetModel = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetModelContent = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMIDSetInputRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__PLMCORE_div_PLMCoreRe ) );
		PLMRouteSetContext.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__VPMCfgContext_, _STRING_2__VPMCfgContext_AddCont, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetInputRef } ) );
		PLMIDSetContext.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetContext ), _STRING_3__VPMCfgContext_div_VPM ) );
		PLMRouteSetModel.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__VPMCfgContext_, _STRING_4__VPMCfgContext_AddMode, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetContext } ) );
		PLMIDSetModel.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetModel ), _STRING_5__Class_div_Model_ ) );
		PLMRouteSetModelContent.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_6__Product_, _STRING_7__Product_AddCriteriaFr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetModel } ) );
		PLMIDSetModelContent.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetModelContent ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDSetModel ), PLMIDSetModelContent ) );
	}
}
