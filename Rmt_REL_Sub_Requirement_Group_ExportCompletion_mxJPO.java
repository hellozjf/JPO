
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Rmt_REL_Sub_Requirement_Group_ExportCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RelationClass_div_Sub = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RelationClass/Sub Requirement Group");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__Requirement_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__rmt_nav_rel_sub_req_g = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_rel_sub_req_grp");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__Rmt_ReqGroup_ExportCo = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_ReqGroup_ExportCompletion");

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
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqGroupAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsRels.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RelationClass_div_Sub ) );
		RsToEntities.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_2__rmt_nav_rel_sub_req_g, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsRels } ) );
		IdsToEntities.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsToEntities ) );
		IdsReqGroupAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_3__Rmt_ReqGroup_ExportCo, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsToEntities } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsReqGroupAndScope ) );
	}
}
