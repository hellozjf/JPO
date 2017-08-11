
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Drafting_ExportCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__CATDraftingDiscipline = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATDraftingDiscipline");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__CATDraftingDiscipline = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATDraftingDiscipline_AddIsAViewOfReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__all_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("all");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__CATDraftingDiscipline = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATDraftingDiscipline_AddIsAViewOfRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__CATDraftingDiscipline = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATDraftingDiscipline_AddIsAViewOfInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__CATDraftingDiscipline = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATDraftingDiscipline_AddIsAViewOfPVS");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__PLMWspSpecFilter_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMWspSpecFilter/PLMWspSpecPVS");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMWspSpecFilter_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMWspSpecFilter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__PLMWspSpecFilter_Retr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMWspSpecFilter_RetrieveFilteredRoot");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__PRODUCTCFG_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__ProductCfg_AddAggreg = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddAggregatingReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__PRODUCTCFG_div_VPMIn = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__PRODUCTCFG_div_VPMRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMRepInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__PRODUCTCFG_div_VPMRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__PRODUCTCFG_div_VPMRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__ProductCfg_AddChildr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddChildrenProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_16__ProductCfg_AddVPMRep = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddVPMRepsPortsAndConnections");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_17__PLMKnowledgewareDisc = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKnowledgewareDiscipline");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_18__KwaDiscipline_AddPoi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("KwaDiscipline_AddPointedDesignTable");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_19__RFLPLMImplementConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_20__RFLPLMImplementConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection_AddAllImplementCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_21__CATMaterial_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterial");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_22__mat_retrieveAllAppli = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mat_retrieveAllAppliedMaterial");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_23__CATMaterialRef_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_24__mat_retrieveDomains_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mat_retrieveDomains");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_25__CATMaterialRef_div_C = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef/CATMatReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_26__mat_retrieveCnx_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mat_retrieveCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_27__rdg_retrieveTexture_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rdg_retrieveTexture");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_28__CATMaterialRef_div_M = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef/MaterialDomain");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_29__SIMObjSimulationGene = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SIMObjSimulationGeneric");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_30__sim_retrieveExternal = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("sim_retrieveExternalDocumentfromFEMRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_31__Rendering_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rendering");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_32__Rendering_AddExterna = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rendering_AddExternalDocFromRenderingRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_33__CATMCXAssembly_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMCXAssembly");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_34__CATMCXAssembly_AddAl = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMCXAssembly_AddAllAggregatedMCX");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_35__CATAsmSymGeo_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATAsmSymGeo");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_36__CATAsmSymObj_AddAllS = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATAsmSymObj_AddAllSymObj");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_37__PLMDocConnection_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_38__PLMDocConnection_ret = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection_retrieveAllDocuments");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_39__PLMKnowHowRuleSet_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKnowHowRuleSet");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_40__kwe_navigate_ruleset = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kwe_navigate_ruleset");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_41__kwe_expand_rules_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kwe_expand_rules");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_42__PLMWspSpecFilter_Add = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMWspSpecFilter_AddAllAggregatedSpecPVS");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_in_0 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_out_0 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet TgtRef_RS = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet TgtRefPath_IS = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet TgtRepRef_RS = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet TgtRepRefPath_IS = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet TgtInstance_RS = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet TgtInstancePath_IS = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet TgtAllPath_IS = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet TgtPVS_RS = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet TgtPVSPath_IS = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet TgtPVSPath_IS2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet TgtFilterRoot_RS = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet TgtFilterRootPath_IS = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet TgtAgregatingRefs_RS = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet TgtAgregatingRefs_IS = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_in_1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet ProductComponents_R = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_out_1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_in_2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_out_2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet1 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet2 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet5 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet6 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet7 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet8 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet9 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet10 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet11 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet13 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet14 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet16 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsImportedRulesets = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsRuleEntity = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsAggreatedSpecPVS = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetproc = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet1Rep = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet6 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet7 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet8 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet9 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsImportedRulesets = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetRestricted = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetRestrictedRep = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMIDSet_in_0.setValue( iPLMIDSet );
		TgtRef_RS.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATDraftingDiscipline, _STRING_1__CATDraftingDiscipline, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_in_0 } ) );
		TgtRefPath_IS.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( TgtRef_RS, _STRING_2__all_ ) );
		TgtRepRef_RS.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATDraftingDiscipline, _STRING_3__CATDraftingDiscipline, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_in_0 } ) );
		TgtRepRefPath_IS.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( TgtRepRef_RS, _STRING_2__all_ ) );
		TgtInstance_RS.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATDraftingDiscipline, _STRING_4__CATDraftingDiscipline, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_in_0 } ) );
		TgtInstancePath_IS.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( TgtInstance_RS, _STRING_2__all_ ) );
		TgtPVS_RS.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATDraftingDiscipline, _STRING_5__CATDraftingDiscipline, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_in_0 } ) );
		TgtPVSPath_IS.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( TgtPVS_RS, _STRING_2__all_ ) );
		TgtPVSPath_IS2.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , TgtPVSPath_IS, _STRING_6__PLMWspSpecFilter_div_ ) );
		TgtFilterRoot_RS.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_7__PLMWspSpecFilter_, _STRING_8__PLMWspSpecFilter_Retr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { TgtPVSPath_IS2 } ) );
		TgtFilterRootPath_IS.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( TgtFilterRoot_RS, _STRING_2__all_ ) );
		TgtAllPath_IS.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( TgtRefPath_IS, TgtRepRefPath_IS ), TgtInstancePath_IS ), TgtFilterRootPath_IS ) );
		TgtAgregatingRefs_RS.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_9__PRODUCTCFG_, _STRING_10__ProductCfg_AddAggreg, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , TgtAllPath_IS, _STRING_11__PRODUCTCFG_div_VPMIn ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , TgtAllPath_IS, _STRING_12__PRODUCTCFG_div_VPMRe ) ) } ) );
		TgtAgregatingRefs_IS.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( TgtAgregatingRefs_RS ) );
		PLMIDSet_out_0.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( TgtAgregatingRefs_IS, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , TgtAllPath_IS, _STRING_13__PRODUCTCFG_div_VPMRe ) ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , TgtAllPath_IS, _STRING_14__PRODUCTCFG_div_VPMRe ) ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , TgtFilterRootPath_IS, _STRING_13__PRODUCTCFG_div_VPMRe ) ), PLMIDSet_in_0 ) );
		PLMIDSet_in_1.setValue( PLMIDSet_out_0 );
		ProductComponents_R.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_9__PRODUCTCFG_, _STRING_15__ProductCfg_AddChildr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet_in_1, _STRING_13__PRODUCTCFG_div_VPMRe ) } ) );
		PLMIDSet_out_1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( ProductComponents_R ), PLMIDSet_in_1 ) );
		PLMIDSet_in_2.setValue( PLMIDSet_out_1 );
		PLMIDSetRestricted.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet_in_2, _STRING_13__PRODUCTCFG_div_VPMRe ) );
		PLMIDSetRestrictedRep.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet_in_2, _STRING_14__PRODUCTCFG_div_VPMRe ) );
		PLMRouteSet1.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_9__PRODUCTCFG_, _STRING_16__ProductCfg_AddVPMRep, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetRestricted } ) );
		PLMIDSet1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet1 ) );
		PLMIDSet1Rep.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSetRestrictedRep, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet1, _STRING_14__PRODUCTCFG_div_VPMRe ) ) );
		PLMRouteSet2.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_17__PLMKnowledgewareDisc, _STRING_18__KwaDiscipline_AddPoi, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet1Rep } ) );
		PLMRouteSet5.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_19__RFLPLMImplementConne, _STRING_20__RFLPLMImplementConne, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetRestricted } ) );
		PLMRouteSet6.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_21__CATMaterial_, _STRING_22__mat_retrieveAllAppli, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetRestricted } ) );
		PLMIDSet6.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet6 ) );
		PLMRouteSet7.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_23__CATMaterialRef_, _STRING_24__mat_retrieveDomains_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet6, _STRING_25__CATMaterialRef_div_C ) } ) );
		PLMIDSet7.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet7 ) );
		PLMRouteSet8.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_23__CATMaterialRef_, _STRING_26__mat_retrieveCnx_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet6, _STRING_25__CATMaterialRef_div_C ) } ) );
		PLMIDSet8.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet8 ) );
		PLMRouteSet9.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_23__CATMaterialRef_, _STRING_27__rdg_retrieveTexture_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet7, _STRING_28__CATMaterialRef_div_M ) } ) );
		PLMIDSet9.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet9 ) );
		PLMRouteSet14.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_29__SIMObjSimulationGene, _STRING_30__sim_retrieveExternal, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet1Rep } ) );
		PLMRouteSet16.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_31__Rendering_, _STRING_32__Rendering_AddExterna, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet1Rep } ) );
		PLMRouteSet10.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_33__CATMCXAssembly_, _STRING_34__CATMCXAssembly_AddAl, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetRestricted } ) );
		PLMRouteSet11.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_35__CATAsmSymGeo_, _STRING_36__CATAsmSymObj_AddAllS, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetRestricted } ) );
		PLMRouteSet13.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_37__PLMDocConnection_, _STRING_38__PLMDocConnection_ret, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetRestricted } ) );
		RsImportedRulesets.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_39__PLMKnowHowRuleSet_, _STRING_40__kwe_navigate_ruleset, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet1Rep } ) );
		IdsImportedRulesets.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsImportedRulesets ) );
		RsRuleEntity.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_39__PLMKnowHowRuleSet_, _STRING_41__kwe_expand_rules_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsImportedRulesets } ) );
		RsAggreatedSpecPVS.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_7__PLMWspSpecFilter_, _STRING_42__PLMWspSpecFilter_Add, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetRestricted } ) );
		PLMIDSet_out_2.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSet_in_2, PLMIDSet1 ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet2 ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet5 ) ), PLMIDSet6 ), PLMIDSet7 ), PLMIDSet8 ), PLMIDSet9 ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet10 ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet11 ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet13 ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet14 ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet16 ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsRuleEntity ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsAggreatedSpecPVS ) ) );
		oPLMIDSet.setValue( PLMIDSet_out_2 );
	}
}
