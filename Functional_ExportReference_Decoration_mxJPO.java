
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Functional_ExportReference_Decoration_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RFLPLMFunctional_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctional/RFLPLMFunctionalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__RFLPLMFunctional_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctional");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__RFLPLMFunctional_Get1 = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctional_Get1stLevelObjects");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__RFLPLMFlow_div_RFLPLM = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFlow/RFLPLMFlowReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__RFLPLMFlow_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFlow");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__RFLPLMFlow_Expand_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFlow_Expand");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__RFLPLMImplementConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__RFLPLMImplementConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection_AddAllImplementCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__RFLPLMFunctionalConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctionalConnectorMapping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__RFLPLMFunctionalConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctionalConnectorMapping_AllFctMappingCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__PLMDocConnection_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__PLMDocConnection_ret = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection_retrieveAllDocuments");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__RFLPLMFlowExposition = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFlowExposition/RFLPLMFlowExpositionInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__RFLPLMFlowExposition = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFlowExposition");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__RFLPLMFunctional_Add = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctional_AddFctContracts");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__RFLPLMFunctional_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctional/RFLPLMFunctionalRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_16__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_17__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetRepLibPointedByFunctionalRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_18__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetRefLibFromRepLib");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_19__SystemsBehavior_GetD = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SystemsBehavior_GetDependencies");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_20__DIFModeler_GetAttach = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModeler_GetAttachedPresentations");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_21__PLMParameter_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMParameter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_22__PAR_nav_params_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PAR_nav_params");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfFctTopModelerPLMRoutes = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfFctTopModelerPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfFctFlowRefTopModelerPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForFctFlows = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForImplementLinks = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForFunctionalPortMapping = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForDocuments = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMParameters = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForFctContracts = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_FunctionalRep = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_FRepRefAndRepLibDependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_FRepRefAndRepLibDependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_FirstRepLibPointed = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_LibRefRepDependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_LibRefRepDependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_RefLib_RepInstLib_RepRefLib_Dependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_RefLib_RepInstLib_RepRefLib_Dependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_RefLibDependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_BehaviorDataToExport = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_DifModeler = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_ForIL = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_FctFlowExpoInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet InputPLMIDSetRestricted = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		InputPLMIDSetRestricted.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RFLPLMFunctional_div_ ) );
		SetOfFctTopModelerPLMRoutes.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__RFLPLMFunctional_, _STRING_2__RFLPLMFunctional_Get1, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfFctTopModelerPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfFctTopModelerPLMRoutes ) );
		SetOfFctFlowRefTopModelerPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfFctTopModelerPLMIDs, _STRING_3__RFLPLMFlow_div_RFLPLM ) );
		SetOfPLMRoutesForFctFlows.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_4__RFLPLMFlow_, _STRING_5__RFLPLMFlow_Expand_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfFctFlowRefTopModelerPLMIDs } ) );
		PLMIDSet_ForIL.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( InputPLMIDSetRestricted, SetOfFctFlowRefTopModelerPLMIDs ) );
		SetOfPLMRoutesForImplementLinks.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_6__RFLPLMImplementConnec, _STRING_7__RFLPLMImplementConnec, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_ForIL } ) );
		SetOfPLMRoutesForFunctionalPortMapping.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_8__RFLPLMFunctionalConne, _STRING_9__RFLPLMFunctionalConne, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfPLMRoutesForDocuments.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_10__PLMDocConnection_, _STRING_11__PLMDocConnection_ret, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		PLMIDSet_FctFlowExpoInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfFctTopModelerPLMIDs, _STRING_12__RFLPLMFlowExposition ) );
		SetOfPLMRoutesForFctContracts.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_13__RFLPLMFlowExposition, _STRING_14__RFLPLMFunctional_Add, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_FctFlowExpoInst } ) );
		PLMIDSet_FunctionalRep.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfFctTopModelerPLMIDs, _STRING_15__RFLPLMFunctional_div_ ) );
		PLMRouteSet_FRepRefAndRepLibDependancy.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_16__CATSysBehaviorLibrar, _STRING_17__CATSysBehaviorLibrar, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_FunctionalRep } ) );
		PLMIDSet_FRepRefAndRepLibDependancy.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_FRepRefAndRepLibDependancy ) );
		PLMRouteSet_RefLib_RepInstLib_RepRefLib_Dependancy.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_16__CATSysBehaviorLibrar, _STRING_18__CATSysBehaviorLibrar, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_FRepRefAndRepLibDependancy } ) );
		PLMIDSet_RefLib_RepInstLib_RepRefLib_Dependancy.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_RefLib_RepInstLib_RepRefLib_Dependancy ) );
		PLMIDSet_BehaviorDataToExport.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_19__SystemsBehavior_GetD, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_RefLib_RepInstLib_RepRefLib_Dependancy } ) );
		PLMIDSet_DifModeler.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_20__DIFModeler_GetAttach, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfPLMParameters.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_21__PLMParameter_, _STRING_22__PAR_nav_params_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, SetOfFctTopModelerPLMIDs ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForFctFlows ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForImplementLinks ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForFunctionalPortMapping ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForDocuments ) ), PLMIDSet_BehaviorDataToExport ), PLMIDSet_DifModeler ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMParameters ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForFctContracts ) ) );
	}
}
