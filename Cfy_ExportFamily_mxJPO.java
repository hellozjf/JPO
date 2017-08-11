
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Cfy_ExportFamily_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__CATComponentsFamily_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentsFamily/CATComponentsFamilyReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__CATComponentsFamily_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentsFamily");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__cfy_expand_repref_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("cfy_expand_repref");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__cfy_navigate_genericm = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("cfy_navigate_genericmodel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__cfy_navigate_genericm = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("cfy_navigate_genericmodel_rep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__cfy_navigate_resolved = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("cfy_navigate_resolveditems");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__PRODUCTCFG_div_VPMRef = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PRODUCTCFG_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__ProductCfg_AddChildre = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddChildrenProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__PRODUCTCFG_div_VPMRep = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__VPMEditor_GetAllRepr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMEditor_GetAllRepresentations");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsFamilyRep = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsFamilyGenericModelRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsFamilyGenericModelRepRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsFamilyResolvedItems = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsProductStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsFamilyRep = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsFamilyGenericModelRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsFamilyGenericModelRepRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsFamilyResolvedItems = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructureInputs1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructureInputs2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructure1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructure2 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsRestrictedToFamilyReference = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsRestrictedToFamilyReference.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__CATComponentsFamily_div_ ) );
		RsFamilyRep.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__CATComponentsFamily_, _STRING_2__cfy_expand_repref_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsRestrictedToFamilyReference } ) );
		IdsFamilyRep.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsFamilyRep ) );
		RsFamilyGenericModelRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__CATComponentsFamily_, _STRING_3__cfy_navigate_genericm, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsFamilyRep } ) );
		RsFamilyGenericModelRepRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__CATComponentsFamily_, _STRING_4__cfy_navigate_genericm, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsFamilyRep } ) );
		IdsFamilyGenericModelRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsFamilyGenericModelRef ) );
		IdsFamilyGenericModelRepRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsFamilyGenericModelRepRef ) );
		RsFamilyResolvedItems.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__CATComponentsFamily_, _STRING_5__cfy_navigate_resolved, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsFamilyRep } ) );
		IdsFamilyResolvedItems.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsFamilyResolvedItems ) );
		IdsProductStructureInputs1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsFamilyGenericModelRef, _STRING_6__PRODUCTCFG_div_VPMRef ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsFamilyResolvedItems, _STRING_6__PRODUCTCFG_div_VPMRef ) ) );
		RsProductStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_7__PRODUCTCFG_, _STRING_8__ProductCfg_AddChildre, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsProductStructureInputs1 } ) );
		IdsProductStructure1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsProductStructure ) );
		IdsProductStructureInputs2.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsProductStructure1, _STRING_6__PRODUCTCFG_div_VPMRef ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsProductStructure1, _STRING_9__PRODUCTCFG_div_VPMRep ) ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsFamilyGenericModelRepRef, _STRING_9__PRODUCTCFG_div_VPMRep ) ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsFamilyResolvedItems, _STRING_6__PRODUCTCFG_div_VPMRef ) ) );
		IdsProductStructure2.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_10__VPMEditor_GetAllRepr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsProductStructureInputs2 } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsFamilyRep ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsFamilyGenericModelRef ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsFamilyGenericModelRepRef ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsFamilyResolvedItems ) ), IdsProductStructure1 ), IdsProductStructure2 ) );
	}
}
