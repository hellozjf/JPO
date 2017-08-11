
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Validation_Complete_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__DMUValidationBase_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DMUValidationBase");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__DMUValidationBase_Add = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DMUValidationBase_AddChildren");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__DMUValidationBase_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DMUValidationBase/DMUValidationValidation");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__DMUValidationBase_Exp = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DMUValidationBase_ExpandAll");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__DMUValidationBase_Add = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DMUValidationBase_AddContexts");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__PRODUCTCFG_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__ProductCfg_AddChildre = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddChildrenProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PRODUCTCFG_div_VPMRef = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__VPMEditor_GetAllRepre = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMEditor_GetAllRepresentations");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__PLMWspFilter_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMWspFilter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__PLMWspFilter_Retriev = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMWspFilter_RetrieveFilteredRoot");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__Functional_ExportRef = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Functional_ExportReference_Design");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__RFLPLMFunctional_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctional/RFLPLMFunctionalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__Logical_ExportRefere = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Logical_ExportReference_Design");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__RFLVPMLogical_div_RF = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical/RFLVPMLogicalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__DIFModeler_GetAttach = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModeler_GetAttachedPresentations");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_16__SIMObjSimulation_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SIMObjSimulation/SIMObjSimulationObject");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_17__PLMCORE_div_PLMCoreR = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_18__DefaultSimulationExp = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DefaultSimulationExport");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetChildren = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetReviews = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetContexts = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetPSEntities = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetPSAllReps = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetFilterPSRoots = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetFilterPSEntities = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetFilterPSAllReps = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetFunctionalEntities = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLogicalEntities = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetDIFModeler = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetSimulationEntities = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetSimuTempo1Entities = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetSimuTempo2Entities = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMIDSetChildren.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__DMUValidationBase_, _STRING_1__DMUValidationBase_Add, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__DMUValidationBase_div_ ) } ) ) );
		PLMIDSetReviews.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__DMUValidationBase_, _STRING_3__DMUValidationBase_Exp, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__DMUValidationBase_div_ ) } ) ) );
		PLMIDSetContexts.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__DMUValidationBase_, _STRING_4__DMUValidationBase_Add, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__DMUValidationBase_div_ ) } ) ) );
		PLMIDSetPSEntities.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__PRODUCTCFG_, _STRING_6__ProductCfg_AddChildre, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetContexts, _STRING_7__PRODUCTCFG_div_VPMRef ) } ) ) );
		PLMIDSetPSAllReps.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_8__VPMEditor_GetAllRepre, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetPSEntities } ) );
		PLMIDSetFilterPSRoots.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_9__PLMWspFilter_, _STRING_10__PLMWspFilter_Retriev, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetContexts } ) ) );
		PLMIDSetFilterPSEntities.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__PRODUCTCFG_, _STRING_6__ProductCfg_AddChildre, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetFilterPSRoots, _STRING_7__PRODUCTCFG_div_VPMRef ) } ) ) );
		PLMIDSetFilterPSAllReps.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_8__VPMEditor_GetAllRepre, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetFilterPSEntities } ) );
		PLMIDSetFunctionalEntities.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_11__Functional_ExportRef, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetContexts, _STRING_12__RFLPLMFunctional_div_ ) } ) );
		PLMIDSetLogicalEntities.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_13__Logical_ExportRefere, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetContexts, _STRING_14__RFLVPMLogical_div_RF ) } ) );
		PLMIDSetDIFModeler.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_15__DIFModeler_GetAttach, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetContexts } ) );
		PLMIDSetSimuTempo1Entities.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetContexts, _STRING_16__SIMObjSimulation_div_ ) );
		PLMIDSetSimuTempo2Entities.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetSimuTempo1Entities, _STRING_17__PLMCORE_div_PLMCoreR ) );
		PLMIDSetSimulationEntities.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_18__DefaultSimulationExp, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetSimuTempo2Entities } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSetChildren, PLMIDSetReviews ), PLMIDSetContexts ), PLMIDSetPSEntities ), PLMIDSetPSAllReps ), PLMIDSetFilterPSRoots ), PLMIDSetFilterPSEntities ), PLMIDSetFilterPSAllReps ), PLMIDSetFunctionalEntities ), PLMIDSetLogicalEntities ), PLMIDSetSimulationEntities ), PLMIDSetDIFModeler ) );
	}
}
