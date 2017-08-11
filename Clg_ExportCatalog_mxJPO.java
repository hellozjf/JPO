
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Clg_ExportCatalog_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__ENOCLG_LIBRARY_div_EN = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_LIBRARY/ENOCLG_LibraryReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__ENOCLG_CLASS_div_ENOC = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_CLASS/ENOCLG_ClassReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__ENOCLG_LIBRARY_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_LIBRARY");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__clg_expand_library_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("clg_expand_library");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__ENOCLG_CLASS_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_CLASS");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__clg_navigate_referenc = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("clg_navigate_reference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__PLMCORE_div_PLMCoreRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMCORE_div_PLMCoreRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMCoreRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__PLMCORE_div_PLMConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMCORE/PLMConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__PRODUCTCFG_div_VPMRef = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__PRODUCTCFG_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__ProductCfg_AddChildr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddChildrenProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__PRODUCTCFG_div_VPMRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__VPMEditor_GetAllRepr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMEditor_GetAllRepresentations");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__ENOCLG_CLASS_div_ENO = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOCLG_CLASS/ENOCLG_Item");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__CATComponentsFamily_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentsFamily");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_16__cfy_navigate_connect = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("cfy_navigate_connection_item");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_17__CATComponentsFamily_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentsFamily/CATComponentsFamilyReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_18__Cfy_ExportFamily_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Cfy_ExportFamily");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_19__PLMKnowledgeTemplate = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKnowledgeTemplate/PLMTemplateRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_20__Pkt_ExportTemplate_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Pkt_ExportTemplate");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsClgStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsItems = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsProductStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsComponentFamilies = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsClgIn = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsClgStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
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
		IdsClgIn.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__ENOCLG_LIBRARY_div_EN ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_1__ENOCLG_CLASS_div_ENOC ) ) );
		RsClgStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_2__ENOCLG_LIBRARY_, _STRING_3__clg_expand_library_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsClgIn } ) );
		IdsClgStructure.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsClgStructure ) );
		IdsChapters.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsClgStructure, _STRING_1__ENOCLG_CLASS_div_ENOC ) );
		RsItems.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_4__ENOCLG_CLASS_, _STRING_5__clg_navigate_referenc, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsChapters } ) );
		IdsItems.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsItems ) );
		IdsPointedReferences.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsItems, _STRING_6__PLMCORE_div_PLMCoreRe ) );
		IdsPointedRepReferences.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsItems, _STRING_7__PLMCORE_div_PLMCoreRe ) );
		IdsPointedConnections.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsItems, _STRING_8__PLMCORE_div_PLMConnec ) );
		IdsProductStructureInputs1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPointedReferences, _STRING_9__PRODUCTCFG_div_VPMRef ) );
		RsProductStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_10__PRODUCTCFG_, _STRING_11__ProductCfg_AddChildr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsProductStructureInputs1 } ) );
		IdsProductStructure1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsProductStructure ) );
		IdsProductStructureInputs2.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsProductStructure1, _STRING_9__PRODUCTCFG_div_VPMRef ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsProductStructure1, _STRING_12__PRODUCTCFG_div_VPMRe ) ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPointedRepReferences, _STRING_12__PRODUCTCFG_div_VPMRe ) ) );
		IdsProductStructure2.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_13__VPMEditor_GetAllRepr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsProductStructureInputs2 } ) );
		IdsConnectionsItems.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPointedConnections, _STRING_14__ENOCLG_CLASS_div_ENO ) );
		RsComponentFamilies.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_15__CATComponentsFamily_, _STRING_16__cfy_navigate_connect, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsConnectionsItems } ) );
		IdsComponentFamilies.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsComponentFamilies ) );
		IdsFamiliesRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsComponentFamilies, _STRING_17__CATComponentsFamily_div_ ) );
		IdsFamiliesStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_18__Cfy_ExportFamily_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsFamiliesRef } ) );
		IdsTemplatesReps.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPointedRepReferences, _STRING_19__PLMKnowledgeTemplate ) );
		IdsTemplatesStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_20__Pkt_ExportTemplate_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsTemplatesReps } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( IdsClgStructure, IdsItems ), IdsPointedReferences ), IdsPointedRepReferences ), IdsPointedConnections ), IdsProductStructure1 ), IdsProductStructure2 ), IdsFamiliesStructure ), IdsTemplatesStructure ) );
	}
}
