
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class MatAppliedExportDesign_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__CATMaterial_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterial");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__mat_retrieveAllApplie = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mat_retrieveAllAppliedMaterial");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__MaterialReferenceExpo = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("MaterialReferenceExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__CATMaterialRef_div_CA = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef/CATMatReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__MaterialUnifiedResour = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("MaterialUnifiedResources");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__mcc_retrieveAllApplie = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mcc_retrieveAllAppliedMaterial");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__MaterialUnifiedDesign = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("MaterialUnifiedDesignCompletion");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__Class_div_Internal_Ma = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Internal Material");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_Mat = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_Mcc = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_Mat = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_MatExpanded = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_Mcc = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_MccExpanded = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMRouteSet_Mat.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATMaterial_, _STRING_1__mat_retrieveAllApplie, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		PLMIDSet_Mat.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_Mat ) );
		PLMIDSet_MatExpanded.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_2__MaterialReferenceExpo, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet_Mat, _STRING_3__CATMaterialRef_div_CA ) } ) );
		PLMRouteSet_Mcc.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_4__MaterialUnifiedResour, _STRING_5__mcc_retrieveAllApplie, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		PLMIDSet_Mcc.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_Mcc ) );
		PLMIDSet_MccExpanded.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_6__MaterialUnifiedDesign, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet_Mcc, _STRING_7__Class_div_Internal_Ma ) } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSet_Mat, PLMIDSet_MatExpanded ), PLMIDSet_Mcc ), PLMIDSet_MccExpanded ) );
	}
}
