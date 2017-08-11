
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Kba_ExportAppComp_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMKbaAppliComponent_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKbaAppliComponent/PLMKbaAppliComponent");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__PLMKbaAppliComponent_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKbaAppliComponent");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__kba_navigate_componen = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kba_navigate_component_prereqs");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__kba_navigate_componen = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kba_navigate_component_rzip");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__kba_navigate_componen = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kba_navigate_component_arm_ref");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__kba_navigate_componen = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kba_navigate_component_arm_repref");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__kba_navigate_componen = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kba_navigate_component_arm_repInst_3DPart");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PRODUCTCFG_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__ProductCfg_AddAggrega = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddAggregatingReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__PRODUCTCFG_div_VPMRef = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__kba_navigate_Ref_3DP = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kba_navigate_Ref_3DPart");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__ENOCLG_LIBRARY_div_E = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_LIBRARY/ENOCLG_LibraryReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__Clg_ExportCatalog_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Clg_ExportCatalog");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__ENOCLG_CLASS_div_ENO = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_CLASS/ENOCLG_ClassReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__Clg_ExportChapter_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Clg_ExportChapter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__PLMKnowledgeTemplate = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKnowledgeTemplate/PLMTemplateRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_16__Pkt_ExportTemplate_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Pkt_ExportTemplate");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_17__ProductCfg_AddChildr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddChildrenProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_18__PRODUCTCFG_div_VPMRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_19__VPMEditor_GetAllRepr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMEditor_GetAllRepresentations");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_20__PLMKnowHowRuleSet_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKnowHowRuleSet/PLMRuleSet");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_21__PLMKnowHowRuleSet_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKnowHowRuleSet");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_22__kwe_expand_rules_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kwe_expand_rules");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsPrereqsKComp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsRZipKComp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsARMpointedCoreRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsARMpointedCoreRepRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsARMpointedCoreRepRef3DPart = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsARMpointedCoreRepInst3DPart = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsARMpointedCoreRef3DPart = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsARMpointedCoreRepInst3DPart2 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsARMpointedCoreRef3DPart2 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsProductStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsRuleEntity = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsKComp0 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsKComp = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsRZipKComp = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPreqsKComp = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsARMpointedCoreRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsARMpointedCoreRefRestr = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsARMpointedCoreRepRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsARMpointedCoreRepRef3DPart = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsARMpointedCoreRepInst3DPart = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsARMpointedCoreRef3DPart = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsARMpointedCoreRef3DPart2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsARMpointedCoreRepInst3DPart2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsCatalogs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsCatalogsContents = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChapters = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChaptersContents = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsTemplates = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsTemplatesStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructureInputs1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructureInputs2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructure1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructure2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsRulesets = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsKComp0.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__PLMKbaAppliComponent_div_ ) );
		RsPrereqsKComp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMKbaAppliComponent_, _STRING_2__kba_navigate_componen, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsKComp0 } ) );
		IdsPreqsKComp.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsPrereqsKComp ) ) );
		IdsKComp.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPreqsKComp, _STRING_0__PLMKbaAppliComponent_div_ ) );
		RsRZipKComp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMKbaAppliComponent_, _STRING_3__kba_navigate_componen, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsKComp } ) );
		IdsRZipKComp.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsRZipKComp ) );
		RsARMpointedCoreRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMKbaAppliComponent_, _STRING_4__kba_navigate_componen, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsKComp } ) );
		IdsARMpointedCoreRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsARMpointedCoreRef ) );
		RsARMpointedCoreRepRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMKbaAppliComponent_, _STRING_5__kba_navigate_componen, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsKComp } ) );
		IdsARMpointedCoreRepRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsARMpointedCoreRepRef ) );
		RsARMpointedCoreRepInst3DPart.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMKbaAppliComponent_, _STRING_6__kba_navigate_componen, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsARMpointedCoreRepRef } ) );
		IdsARMpointedCoreRepInst3DPart.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsARMpointedCoreRepInst3DPart ) );
		RsARMpointedCoreRef3DPart.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_7__PRODUCTCFG_, _STRING_8__ProductCfg_AddAggrega, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsARMpointedCoreRepInst3DPart } ) );
		IdsARMpointedCoreRef3DPart.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsARMpointedCoreRef3DPart ) );
		IdsARMpointedCoreRefRestr.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsARMpointedCoreRef3DPart, _STRING_9__PRODUCTCFG_div_VPMRef ) );
		RsARMpointedCoreRepRef3DPart.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMKbaAppliComponent_, _STRING_10__kba_navigate_Ref_3DP, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsARMpointedCoreRefRestr } ) );
		IdsARMpointedCoreRepRef3DPart.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsARMpointedCoreRepRef3DPart ) );
		RsARMpointedCoreRepInst3DPart2.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMKbaAppliComponent_, _STRING_6__kba_navigate_componen, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsARMpointedCoreRepRef3DPart } ) );
		IdsARMpointedCoreRepInst3DPart2.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsARMpointedCoreRepInst3DPart2 ) );
		RsARMpointedCoreRef3DPart2.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_7__PRODUCTCFG_, _STRING_8__ProductCfg_AddAggrega, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsARMpointedCoreRepInst3DPart2 } ) );
		IdsARMpointedCoreRef3DPart2.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsARMpointedCoreRef3DPart2 ) );
		IdsCatalogs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsARMpointedCoreRef, _STRING_11__ENOCLG_LIBRARY_div_E ) );
		IdsCatalogsContents.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_12__Clg_ExportCatalog_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsCatalogs } ) );
		IdsChapters.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsARMpointedCoreRef, _STRING_13__ENOCLG_CLASS_div_ENO ) );
		IdsChaptersContents.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_14__Clg_ExportChapter_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsChapters } ) );
		IdsTemplates.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsARMpointedCoreRepRef, _STRING_15__PLMKnowledgeTemplate ) );
		IdsTemplatesStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_16__Pkt_ExportTemplate_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsTemplates } ) );
		IdsProductStructureInputs1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsARMpointedCoreRef, _STRING_9__PRODUCTCFG_div_VPMRef ) );
		RsProductStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_7__PRODUCTCFG_, _STRING_17__ProductCfg_AddChildr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsProductStructureInputs1 } ) );
		IdsProductStructure1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsProductStructure ) );
		IdsProductStructureInputs2.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsProductStructure1, _STRING_9__PRODUCTCFG_div_VPMRef ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsProductStructure1, _STRING_18__PRODUCTCFG_div_VPMRe ) ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsARMpointedCoreRepRef, _STRING_18__PRODUCTCFG_div_VPMRe ) ) );
		IdsProductStructure2.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_19__VPMEditor_GetAllRepr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsProductStructureInputs2 } ) );
		IdsRulesets.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsARMpointedCoreRef, _STRING_20__PLMKnowHowRuleSet_div_ ) );
		RsRuleEntity.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_21__PLMKnowHowRuleSet_, _STRING_22__kwe_expand_rules_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsRulesets } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( IdsPreqsKComp, IdsRZipKComp ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsARMpointedCoreRef ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsARMpointedCoreRepRef ) ), IdsCatalogsContents ), IdsChaptersContents ), IdsTemplatesStructure ), IdsProductStructure1 ), IdsProductStructure2 ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsRuleEntity ) ), IdsARMpointedCoreRepInst3DPart2 ), IdsARMpointedCoreRef3DPart2 ) );
	}
}
