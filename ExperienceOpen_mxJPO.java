
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ExperienceOpen_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMExpAbstract3DExper = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMExpAbstract3DExperience");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__PLMExp_getSRPointedEx = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMExp_getSRPointedExps");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PLMExp_getSRPointedAs = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMExp_getSRPointedAssets");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__PRODUCTCFG_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__ProductCfg_AddChildre = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddChildrenProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__PRODUCTCFG_div_VPMRef = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__VPMEditor_OpenComplet = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMEditor_OpenCompletionOnReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__Logical_ExportReferen = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Logical_ExportReference_Design");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__RFLVPMLogical_div_RFL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical/RFLVPMLogicalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__SimulationOpen_Genera = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SimulationOpen_GenerativeStress");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__SIMObjSimulation_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SIMObjSimulation/SIMObjSimulationObject");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet Ids_SRPointedExps = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet Ids_SRPointedAssets = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet Ids_OpenedPrds = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet Ids_OpenedLogical = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet Ids_OpenedSimu = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet Rts_SRPointedExps = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet Rts_SRPointedAssets = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet Rts_ChildrenPrds = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		Rts_SRPointedExps.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMExpAbstract3DExper, _STRING_1__PLMExp_getSRPointedEx, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		Ids_SRPointedExps.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( Rts_SRPointedExps ) );
		Rts_SRPointedAssets.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMExpAbstract3DExper, _STRING_2__PLMExp_getSRPointedAs, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { Ids_SRPointedExps } ) );
		Ids_SRPointedAssets.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( Rts_SRPointedAssets ) );
		Rts_ChildrenPrds.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_3__PRODUCTCFG_, _STRING_4__ProductCfg_AddChildre, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , Ids_SRPointedAssets, _STRING_5__PRODUCTCFG_div_VPMRef ) } ) );
		Ids_OpenedPrds.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_6__VPMEditor_OpenComplet, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( Rts_ChildrenPrds ) } ) );
		Ids_OpenedLogical.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_7__Logical_ExportReferen, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , Ids_SRPointedAssets, _STRING_8__RFLVPMLogical_div_RFL ) } ) );
		Ids_OpenedSimu.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_9__SimulationOpen_Genera, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , Ids_SRPointedAssets, _STRING_10__SIMObjSimulation_div_ ) } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( Ids_SRPointedExps, Ids_SRPointedAssets ), Ids_OpenedPrds ), Ids_OpenedLogical ), Ids_OpenedSimu ) );
	}
}
