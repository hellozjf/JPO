
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Clg_ExportChapter_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__ENOCLG_CLASS_div_ENOC = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_CLASS/ENOCLG_ClassReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__ENOCLG_CLASS_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_CLASS");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__clg_navigate_chapters = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("clg_navigate_chapters_hierarchy");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__ENOCLG_CLASS_div_ENOC = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_CLASS/ENOCLG_ClassInstance");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__clg_navigate_library_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("clg_navigate_library_of_chapter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__clg_expand_chapter_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("clg_expand_chapter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__clg_navigate_referenc = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("clg_navigate_reference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMCORE_div_PLMCoreRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__PLMCORE_div_PLMCoreRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__PLMCORE_div_PLMConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__PRODUCTCFG_div_VPMRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__PRODUCTCFG_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__ProductCfg_AddChildr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddChildrenProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__PRODUCTCFG_div_VPMRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__VPMEditor_GetAllRepr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMEditor_GetAllRepresentations");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__ENOCLG_CLASS_div_ENO = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_CLASS/ENOCLG_Item");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_16__CATComponentsFamily_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentsFamily");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_17__cfy_navigate_connect = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("cfy_navigate_connection_item");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_18__CATComponentsFamily_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentsFamily/CATComponentsFamilyReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_19__Cfy_ExportFamily_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Cfy_ExportFamily");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_20__PLMKnowledgeTemplate = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKnowledgeTemplate/PLMTemplateRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_21__Pkt_ExportTemplate_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Pkt_ExportTemplate");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsChaptersHierarchy = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsLibraryChapter = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsChaptersStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsItems = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsProductStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsComponentFamilies = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChaptersIn1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChaptersIn2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChaptersHierarchy = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsLibraryChapter = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChaptersStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChapters = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsItems = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPointedReferences = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPointedRepReferences = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPointedConnections = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPointedProducts = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructure1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructure2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructureInputs1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructureInputs2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsConnectionsItems = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsComponentFamilies = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsFamiliesRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsFamiliesStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsTemplatesReps = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsTemplatesStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsChaptersIn1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__ENOCLG_CLASS_div_ENOC ) );
		RsChaptersHierarchy.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__ENOCLG_CLASS_, _STRING_2__clg_navigate_chapters, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsChaptersIn1 } ) );
		IdsChaptersHierarchy.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsChaptersHierarchy ) );
		IdsChaptersIn2.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsChaptersHierarchy, _STRING_3__ENOCLG_CLASS_div_ENOC ) );
		RsLibraryChapter.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__ENOCLG_CLASS_, _STRING_4__clg_navigate_library_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsChaptersIn2 } ) );
		IdsLibraryChapter.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsLibraryChapter ) );
		RsChaptersStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__ENOCLG_CLASS_, _STRING_5__clg_expand_chapter_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsChaptersIn1 } ) );
		IdsChaptersStructure.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsChaptersStructure ) );
		IdsChapters.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsChaptersStructure, _STRING_0__ENOCLG_CLASS_div_ENOC ) );
		RsItems.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__ENOCLG_CLASS_, _STRING_6__clg_navigate_referenc, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsChapters } ) );
		IdsItems.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsItems ) );
		IdsPointedReferences.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsItems, _STRING_7__PLMCORE_div_PLMCoreRe ) );
		IdsPointedRepReferences.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsItems, _STRING_8__PLMCORE_div_PLMCoreRe ) );
		IdsPointedConnections.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsItems, _STRING_9__PLMCORE_div_PLMConnec ) );
		IdsProductStructureInputs1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPointedReferences, _STRING_10__PRODUCTCFG_div_VPMRe ) );
		RsProductStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_11__PRODUCTCFG_, _STRING_12__ProductCfg_AddChildr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsProductStructureInputs1 } ) );
		IdsProductStructure1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsProductStructure ) );
		IdsProductStructureInputs2.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsProductStructure1, _STRING_10__PRODUCTCFG_div_VPMRe ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsProductStructure1, _STRING_13__PRODUCTCFG_div_VPMRe ) ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPointedRepReferences, _STRING_13__PRODUCTCFG_div_VPMRe ) ) );
		IdsProductStructure2.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_14__VPMEditor_GetAllRepr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsProductStructureInputs2 } ) );
		IdsConnectionsItems.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPointedConnections, _STRING_15__ENOCLG_CLASS_div_ENO ) );
		RsComponentFamilies.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_16__CATComponentsFamily_, _STRING_17__cfy_navigate_connect, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsConnectionsItems } ) );
		IdsComponentFamilies.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsComponentFamilies ) );
		IdsFamiliesRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsComponentFamilies, _STRING_18__CATComponentsFamily_div_ ) );
		IdsFamiliesStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_19__Cfy_ExportFamily_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsFamiliesRef } ) );
		IdsTemplatesReps.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPointedRepReferences, _STRING_20__PLMKnowledgeTemplate ) );
		IdsTemplatesStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_21__Pkt_ExportTemplate_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsTemplatesReps } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( IdsLibraryChapter, IdsChaptersHierarchy ), IdsChaptersStructure ), IdsItems ), IdsPointedReferences ), IdsPointedRepReferences ), IdsPointedConnections ), IdsProductStructure1 ), IdsProductStructure2 ), IdsFamiliesStructure ), IdsTemplatesStructure ) );
	}
}
