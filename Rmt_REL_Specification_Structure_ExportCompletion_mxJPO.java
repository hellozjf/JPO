
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Rmt_REL_Specification_Structure_ExportCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RelationClass_div_Spe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RelationClass/Specification Structure");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__Requirement_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__rmt_nav_rel_spec_stru = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_rel_spec_struct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__Rmt_ReqSpec_ExportCom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_ReqSpec_ExportCompletion");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__Rmt_Chapter_ExportCom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_Chapter_ExportCompletion");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Rmt_Requirement_Expor = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_Requirement_ExportCompletion");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsToEntities = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsRels = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsToEntities = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqSpecAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChapterAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsRels.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RelationClass_div_Spe ) );
		RsToEntities.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_2__rmt_nav_rel_spec_stru, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsRels } ) );
		IdsToEntities.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsToEntities ) );
		IdsReqSpecAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_3__Rmt_ReqSpec_ExportCom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsToEntities } ) );
		IdsChapterAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_4__Rmt_Chapter_ExportCom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsToEntities } ) );
		IdsReqAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_5__Rmt_Requirement_Expor, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsToEntities } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsToEntities ), IdsReqSpecAndScope ), IdsChapterAndScope ), IdsReqAndScope ) );
	}
}
