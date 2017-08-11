
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Configuration_GetStructConfig_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMCORE_div_PLMCoreRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__PLMBRIDGE_CfgContext_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMBRIDGE_CfgContext");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PLMBRIDGE_CfgContext_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMBRIDGE_CfgContext_AddReferenceContext");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__PLMBRIDGE_CfgContext_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMBRIDGE_CfgContext/PLMCfgContext");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__PLMBRIDGE_CfgContext_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMBRIDGE_CfgContext_AddContextQueries");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__PLMConfigQuery_div_PL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMConfigQuery/PLMCfgQuery");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__PLMBRIDGE_CfgContext_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMBRIDGE_CfgContext_AddContextVariabilitySpace");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMConfigVariabilityS = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMConfigVariabilitySpace/PLMCfgVariabilitySpace");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__PLMCORE_div_PLMCoreIn = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__PLMConfigEffectivity_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMConfigEffectivity");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__PLMConfigEffectivity = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMConfigEffectivity_AddInstanceEffectivity");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__PLMConfigEffectivity = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMConfigEffectivity/PLMCfgEffectivity");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__PLMBRIDGE_CfgStaticM = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMBRIDGE_CfgStaticMapping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__PLMBRIDGE_CfgStaticM = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMBRIDGE_CfgStaticMapping_AddInstanceStaticMapping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__PLMBRIDGE_CfgStaticM = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMBRIDGE_CfgStaticMapping/PLMCfgStaticMapping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__PLMBRIDGE_CfgStaticM = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMBRIDGE_CfgStaticMapping_AddStaticMappingQuery");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetContext = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetQueries = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetVS = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetInstEff = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetInstStcMpg = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetStcMpgQuery = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetInputRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetInputInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetContext = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetQueries = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetVS = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetInstEff = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetInstStcMpg = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetStcMpgQuery = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMIDSetInputRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__PLMCORE_div_PLMCoreRe ) );
		PLMRouteSetContext.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMBRIDGE_CfgContext_, _STRING_2__PLMBRIDGE_CfgContext_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetInputRef } ) );
		PLMIDSetContext.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetContext ), _STRING_3__PLMBRIDGE_CfgContext_div_ ) );
		PLMRouteSetQueries.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMBRIDGE_CfgContext_, _STRING_4__PLMBRIDGE_CfgContext_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetContext } ) );
		PLMIDSetQueries.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetQueries ), _STRING_5__PLMConfigQuery_div_PL ) );
		PLMRouteSetVS.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMBRIDGE_CfgContext_, _STRING_6__PLMBRIDGE_CfgContext_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetContext } ) );
		PLMIDSetVS.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetVS ), _STRING_7__PLMConfigVariabilityS ) );
		PLMIDSetInputInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_8__PLMCORE_div_PLMCoreIn ) );
		PLMRouteSetInstEff.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_9__PLMConfigEffectivity_, _STRING_10__PLMConfigEffectivity, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetInputInst } ) );
		PLMIDSetInstEff.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetInstEff ), _STRING_11__PLMConfigEffectivity ) );
		PLMRouteSetInstStcMpg.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_12__PLMBRIDGE_CfgStaticM, _STRING_13__PLMBRIDGE_CfgStaticM, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetInputInst } ) );
		PLMIDSetInstStcMpg.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetInstStcMpg ), _STRING_14__PLMBRIDGE_CfgStaticM ) );
		PLMRouteSetStcMpgQuery.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_12__PLMBRIDGE_CfgStaticM, _STRING_15__PLMBRIDGE_CfgStaticM, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetInstStcMpg } ) );
		PLMIDSetStcMpgQuery.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetStcMpgQuery ), _STRING_5__PLMConfigQuery_div_PL ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDSetContext ), PLMIDSetQueries ), PLMIDSetVS ), PLMIDSetInstEff ), PLMIDSetInstStcMpg ), PLMIDSetStcMpgQuery ) );
	}
}
