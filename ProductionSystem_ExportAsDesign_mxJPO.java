
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ProductionSystem_ExportAsDesign_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__DELLmiProductionSyste = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionSystemPPR/DELLmiPPRSystemReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__DELLmiProductionSyste = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionSystemPPR/DELLmiPPRSystemInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__DELLmiProductionAbstr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionAbstractOperationPPR/DELLmiPPROperationReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__DELLmiProductionAbstr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionAbstractOperationPPR/DELLmiPPROperationInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__DELLmiProductionOpera = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionOperationPPR/DELLmiOperationPPRInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__DELLmiProductionSyste = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionSystemAbstract/DELLmiAbstractProductionEntity");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__PLMCORE_div_PLMCoreRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMCORE_div_PLMCoreIn = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__DELLmiProductionPlug_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionPlug/DELLmiProductionPlugInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__Config_GetStructConfi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Config_GetStructConfig");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__DELLmiProductionSyst = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionSystemAbstract");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__ENOPsm_ProductionSys = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOPsm_ProductionSystem_addAllPortsRepInstAndCnxExceptImplCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__all_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("all");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__DELLmiProductionSyst = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionSystemIOPort/DELLmiProdSystemIOPort");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__DELLmiProductionCand = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionCandidateResCnx/DELLmiCandidateResourcesCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__DELLmiProductionTime = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionTimeConstraintCnx/DELLmiTimeConstraintCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_16__DELLmiProductionMate = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionMaterialPathCnx1/DELLmiMaterialPathCnxCust");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_17__DELLmiProductionSyst = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionSystemIOCnx/DELLmiProductionSystemIOCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_18__DELAsmAssemblyModelC = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELAsmAssemblyModelCnx/DELAsmProcessCanUseCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_19__PLMCORE_div_PLMCoreR = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreRepInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_20__DELLmiProductionSyst = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionSystemOutputCnxAbstract/DELLmiProductionSystemOutputCnxAbstract");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_21__DEL_LinkToConstraini = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DEL_LinkToConstrainingObject,DEL_LinkToConstrainedObject");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_22__DEL_MaterialPathIN_D = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DEL_MaterialPathIN,DEL_MaterialPathOUT");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_23__DELPPRContextModel_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELPPRContextModel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_24__ENOPpr_PPRData_addRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOPpr_PPRData_addRepRef");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_25__DEL_LinkToPlugObject = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DEL_LinkToPlugObject");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_26__DEL_LinkToSystemIOPo = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DEL_LinkToSystemIOPort");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_27__DELAsmAssemblyModelC = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELAsmAssemblyModelCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_28__DELAsmAssemblyModelC = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELAsmAssemblyModelCnx_addDocuments");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_29__RFLPLMImplementConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_30__RFLPLMImplementConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection_AddAllImplementCnxByNav");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_31__RFLPLMImplementConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection/RFLPLMImplementConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_32__PLMDocConnection_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_33__PLMDocConnection_ret = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection_retrieveAllDocumentsIncludingCBP");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetPortsAndCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetCapableDocuments = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetImplementedCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetAttachedDocuments = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetRepRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetSystemRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetSystemInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetOpWkiRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetOpWkiInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetOpInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetAllRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetAllInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetSystemOpInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDAllRefInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetPlugInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetSystemRefOpInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDAllRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_ConfigData = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetPortsAndCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetIOPorts = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetCandidateCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetTimeCstCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetFlowCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetIOCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetCapableCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetValidTimeCstCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetScopeMaterialFlowCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetValidMaterialFlowCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetScopeIOFlowCnx1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetValidIOCnx1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetScopeIOFlowCnx2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetValidIOCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetCapableDocuments = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetImplementedCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetAttachedDocuments = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetRepInst = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetRepRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetOutputCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMIDSetSystemRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__DELLmiProductionSyste ) );
		PLMIDSetSystemInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_1__DELLmiProductionSyste ) );
		PLMIDSetOpWkiRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__DELLmiProductionAbstr ) );
		PLMIDSetOpWkiInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_3__DELLmiProductionAbstr ) );
		PLMIDSetOpInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_4__DELLmiProductionOpera ) );
		PLMIDSetAllRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_5__DELLmiProductionSyste ) );
		PLMIDSetAllInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSetSystemInst, PLMIDSetOpWkiInst ) );
		PLMIDSetSystemOpInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSetSystemInst, PLMIDSetOpInst ) );
		PLMIDAllRefInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_6__PLMCORE_div_PLMCoreRe ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_7__PLMCORE_div_PLMCoreIn ) ) );
		PLMIDSetPlugInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_8__DELLmiProductionPlug_div_ ) );
		PLMIDSetSystemRefOpInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSetSystemRef, PLMIDSetOpInst ) );
		PLMIDAllRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_6__PLMCORE_div_PLMCoreRe ) );
		PLMIDSet_ConfigData.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_9__Config_GetStructConfi, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDAllRefInst } ) );
		PLMRouteSetPortsAndCnx.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_10__DELLmiProductionSyst, _STRING_11__ENOPsm_ProductionSys, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetAllRef } ) );
		PLMIDSetPortsAndCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetPortsAndCnx, _STRING_12__all_ ) );
		PLMIDSetIOPorts.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetPortsAndCnx, _STRING_13__DELLmiProductionSyst ) );
		PLMIDSetCandidateCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetPortsAndCnx, _STRING_14__DELLmiProductionCand ) );
		PLMIDSetTimeCstCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetPortsAndCnx, _STRING_15__DELLmiProductionTime ) );
		PLMIDSetFlowCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetPortsAndCnx, _STRING_16__DELLmiProductionMate ) );
		PLMIDSetIOCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetPortsAndCnx, _STRING_17__DELLmiProductionSyst ) );
		PLMIDSetCapableCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetPortsAndCnx, _STRING_18__DELAsmAssemblyModelC ) );
		PLMIDSetRepInst.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetPortsAndCnx, _STRING_19__PLMCORE_div_PLMCoreR ) );
		PLMIDSetOutputCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetPortsAndCnx, _STRING_20__DELLmiProductionSyst ) );
		PLMIDSetValidTimeCstCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.ValidateSRs( iContext , PLMIDSetTimeCstCnx, _STRING_21__DEL_LinkToConstraini, PLMIDSetAllInst ) );
		PLMIDSetScopeMaterialFlowCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSetSystemOpInst, PLMIDSetIOPorts ) );
		PLMIDSetValidMaterialFlowCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.ValidateSRs( iContext , PLMIDSetFlowCnx, _STRING_22__DEL_MaterialPathIN_D, PLMIDSetScopeMaterialFlowCnx ) );
		PLMRouteSetRepRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_23__DELPPRContextModel_, _STRING_24__ENOPpr_PPRData_addRe, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetRepInst } ) );
		PLMIDSetRepRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetRepRef, _STRING_12__all_ ) );
		PLMIDSetScopeIOFlowCnx1.setValue( PLMIDSetPlugInst );
		PLMIDSetValidIOCnx1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.ValidateSRs( iContext , PLMIDSetIOCnx, _STRING_25__DEL_LinkToPlugObject, PLMIDSetScopeIOFlowCnx1 ) );
		PLMIDSetScopeIOFlowCnx2.setValue( PLMIDSetIOPorts );
		PLMIDSetValidIOCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.ValidateSRs( iContext , PLMIDSetValidIOCnx1, _STRING_26__DEL_LinkToSystemIOPo, PLMIDSetScopeIOFlowCnx2 ) );
		PLMRouteSetCapableDocuments.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_27__DELAsmAssemblyModelC, _STRING_28__DELAsmAssemblyModelC, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetCapableCnx } ) );
		PLMIDSetCapableDocuments.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetCapableDocuments, _STRING_12__all_ ) );
		PLMRouteSetImplementedCnx.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_29__RFLPLMImplementConne, _STRING_30__RFLPLMImplementConne, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetSystemRefOpInst } ) );
		PLMIDSetImplementedCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetImplementedCnx, _STRING_12__all_ ), _STRING_31__RFLPLMImplementConne ) );
		PLMRouteSetAttachedDocuments.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_32__PLMDocConnection_, _STRING_33__PLMDocConnection_ret, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDAllRef } ) );
		PLMIDSetAttachedDocuments.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetAttachedDocuments, _STRING_12__all_ ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDSet_ConfigData ), PLMIDSetIOPorts ), PLMIDSetCandidateCnx ), PLMIDSetCapableCnx ), PLMIDSetValidTimeCstCnx ), PLMIDSetValidMaterialFlowCnx ), PLMIDSetValidIOCnx ), PLMIDSetCapableDocuments ), PLMIDSetImplementedCnx ), PLMIDSetAttachedDocuments ), PLMIDSetRepInst ), PLMIDSetRepRef ), PLMIDSetOutputCnx ) );
	}
}
