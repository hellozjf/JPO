
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class MatAppliedExportReview_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__CATMaterial_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterial");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__mat_retrieveMatCnxUnd = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mat_retrieveMatCnxUnderProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__mat_retrieveMatRefFro = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mat_retrieveMatRefFromCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__CATMaterial_div_CATMa = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterial/CATMatConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__CATMaterialRef_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__mat_retrieveRendering = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mat_retrieveRenderingDomain");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__CATMaterialRef_div_CA = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef/CATMatReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__rdg_retrieveTexture_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rdg_retrieveTexture");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__CATMaterialRef_div_Ma = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef/MaterialDomain");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_MatCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_MatRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_RendDom = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_Text = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_MatCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_MatRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_RendDom = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_Text = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMRouteSet_MatCnx.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATMaterial_, _STRING_1__mat_retrieveMatCnxUnd, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		PLMIDSet_MatCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_MatCnx ) );
		PLMRouteSet_MatRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATMaterial_, _STRING_2__mat_retrieveMatRefFro, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet_MatCnx, _STRING_3__CATMaterial_div_CATMa ) } ) );
		PLMIDSet_MatRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_MatRef ) );
		PLMRouteSet_RendDom.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_4__CATMaterialRef_, _STRING_5__mat_retrieveRendering, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet_MatRef, _STRING_6__CATMaterialRef_div_CA ) } ) );
		PLMIDSet_RendDom.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_RendDom ) );
		PLMRouteSet_Text.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_4__CATMaterialRef_, _STRING_7__rdg_retrieveTexture_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet_RendDom, _STRING_8__CATMaterialRef_div_Ma ) } ) );
		PLMIDSet_Text.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_Text ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDSet_MatCnx ), PLMIDSet_MatRef ), PLMIDSet_RendDom ), PLMIDSet_Text ) );
	}
}
